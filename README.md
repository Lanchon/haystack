# Haystack
## Signature Spoofing Patcher for Android

This is a replacement for [Needle](https://github.com/moosd/Needle) (and its fork [Tingle](https://github.com/ale5000-git/tingle)) based on the thoroughly awesome and completely ignored [DexPatcher](https://dexpatcher.github.io/)(*) instead of [smali](https://github.com/JesusFreke/smali).

(*) My lawyers insisted that I disclose that I am the author of DexPatcher here.

!['Allow signature spoofing' dialog](https://github.com/Lanchon/haystack/raw/master/screenshots/settings-warning-small.png)
!['Signature Spoofing Checker' app](https://github.com/Lanchon/haystack/raw/master/screenshots/checker-enabled-small.png)

(See also: [Signature Spoofing Checker](https://github.com/Lanchon/sigspoof-checker).)

### What is wrong with Needle?

Well, nothing is _wrong_. It is just that things could be better.

Under the hood, Needle works by disassembling the complete **framework.jar** via **baksmali**, editing the resulting text files, then reassembling them via **smali**.

Unfortunately applying diff patches to smali is a very brittle affair. The bytecode output of **javac** and **dx** can completely change as a result of even minor touches to Java source code, preventing the successful application of smali patches across source changes. Also, given the low information content per line in smali, the possibility always exists that a diff patch applies cleanly but at the wrong place, causing disaster.

For these reasons, Needle's author chose not to use diff patching and instead hard-code search and replace actions in a [Python script](https://github.com/moosd/Needle/blob/master/patch.py#L95-L123) crafted specifically to apply one particular patch. This a lot of work to apply a patch that is conceptually very simple (just a method call hook that invokes [an added method](https://github.com/moosd/Needle/blob/master/smali/fillinsig.smali)), and there is little guarantee that the applied changes will continue to be correct in the face of upstream changes to **framework.jar**, even if the patch appears to apply cleanly.

On top of this, **framework.jar** is a multi-dex monster and smali does not handle multi-dex by itself. So sometimes the added method causes a dex overflow during reassembly and the process fails (in Needle at least; I believe that Tingle has a workaround to handle this case).

Also, Tingle's maintainer wanted to be able to patch the framework within the phone itself rather than requiring a PC. Unfortunately he was unable to reach his goal due to smali's massive memory usage during reassembly of the framework. (Not to mention the disk usage for all the intermediate smali files, and the patch time which I estimate would be around 30 minutes.)

### So what is Haystack?

Haystack is a proof-of-concept hack to showcase DexPatcher and ideally show that, although smali is great, it is not the best tool for every task.

The smali patching in Needle including the [patching code](https://github.com/moosd/Needle/blob/master/patch.py) and the [injected smali](https://github.com/moosd/Needle/blob/master/smali/fillinsig.smali) can be replaced by this simple [DexPatcher patch](https://gist.github.com/Lanchon/78854e97dc8f1d2a6b11a0a134d06c6c) that I wrote in less than 5 minutes (based on the original [source-level patch](https://gerrit.omnirom.org/#/c/8672/1/core/java/android/content/pm/PackageParser.java) by microG's Marvin). The DexPatcher patch is plain Java and very readable, even for people that never encountered DexPatcher before. Try to understand what Needle actually does just by looking at its source code and you are in for a ride, even if you are versed in Python and smali. Yes, you can get an idea; but are you sure it does what you think it does?

DexPatcher understands Dalvik bytecode to a deeper level than the usual smali manipulation tools, resulting in higher assurance that patches do exactly what you expect if they apply without warnings and errors. DexPatcher can patch Android 6.0's **framework.jar** with a constrained Java heap of less than 50 MiB and produces no temporary files. It is coded to be efficient, with 90+% of the typical run time used up in writing the output dex files. And it natively supports multi-dex. 

### Ok, I want to try this thing

Requirements:

- An Android device that:
  - Runs a **non-odexed rom** based on Android version 1.5 through 9.0.
  - And either has TWRP recovery installed or supports root adb in Android.
- Java runtime.
- Bash shell. (If you use Windows (dear mother of god!) read [this](https://msdn.microsoft.com/en-us/commandline/wsl/about).)
- Working adb.
- And to build the patches:
  - Java SE Development Kit (JDK/OpenJDK) 8.

Because of feature creep, Haystack is not as simple as the patch linked above. It is broken up in layers to avoid code duplication and to give you choices of what to apply.

- Hooks (required): these are simple adapters to hook all versions of Android from 1.5 Cupcake to 9.0 Pie. (See [patch source](https://github.com/Lanchon/haystack/tree/master/patches-src-gen/sigspoof-hook).)
- Core (required): this is the main code of the patch that spoofs signatures based on metadata in the application manifest. Other cores that obtain fake signatures from different sources might follow up later; the need to modify an app's manifest to spoof its signature is not ideal in some situations. (See [patch source](https://github.com/Lanchon/haystack/tree/master/patches-src-gen/sigspoof-core).)
- UI (optional): these patches add a global signature spoofing enable/disable switch to the 'Apps' section in the 'Developer options' page of the device settings. They support Android versions from 4.0 ICS to 9.0 Pie. Other UIs could be implemented in the future, such as a per-app setting using the runtime permission model introduced in Android 6.0 or the Privacy Guard framework of CyanogenMod/LineageOS-based roms. (See [patch source](https://github.com/Lanchon/haystack/tree/master/patches-src-gen/sigspoof-ui-global).)

Haystack is based on the concepts of filesets and patches. Filesets are groups of files that are typically pulled from a device to be operated upon (patched) and later pushed back to the same device. Haystack patches are sets of DexPatcher patches applied as a single unit to one or more files of a fileset. Haystack includes bash scripts to apply [binary patches](https://github.com/Lanchon/haystack/tree/master/patches) that do most of the work for you:

- `pull-fileset`: pulls a fileset from a device via adb.
- `push-fileset`: pushes a fileset back to a device via adb.
- `patch-fileset`: patches a fileset that resides on your PC.

Additionally there are scripts to build patches from source:

- `patches-src-gen/generate-patch-sources`: generate [patch sources](https://github.com/Lanchon/haystack/tree/master/patches-src) for the various supported versions of Android from a [unified source tree](https://github.com/Lanchon/haystack/tree/master/patches-src-gen).
- `build-patch`: compiles and packages a haystack patch (which might include several DexPatcher patches to be applied to several files of a fileset).
- `dedex-fileset`: dedex a fileset (ie, process with **d2j-dex2jar**) ahead-of-time for improved build performance (otherwise, `build-patch` dedexes just-in-time).

And finally there are script to bulk-build all patches:

- `bulk-patch-builder/build-all`: generate patch sources and build all haystack patches. For each haystack patch, build all its DexPatcher patches to check that they compile, and then dry-run apply the resulting binary patches to check that they apply cleanly. For each haystack patch, do these build and dry-run apply tests repeatedly against each framework version that the haystack patch supports. Finally, for each haystack patch, build and publish a binary patch against its canonical framework version.
- `bulk-patch-builder/dedex-all`: ahead-of-time dedex for all reference Android frameworks.

The resulting log of the bulk-build process can be found [here](https://github.com/Lanchon/haystack/blob/master/patches/bulk-patch-builder.log). 

### Ok, I said I wanted to try this thingy already!

Let's run through an example patching of a OnePlus 2 running Android 6.0 (which is [API level 23](https://source.android.com/source/build-numbers)).

Connect the phone to your PC via USB and either:
- Boot into TWRP, go to the `Mount` section, and mount the system partition.
- Or remain in Android and enable root USB debugging (if your rom supports it).

Then pull a fileset from the device to directory `fs-oneplus2`:

`$ ./pull-fileset fs-oneplus2`
```
>>> target directory: fs-oneplus2
>>> adb pull /system/framework/framework.jar fs-oneplus2/
[100%] /system/framework/framework.jar
>>> adb pull /system/framework/framework2.jar fs-oneplus2/
adb: error: remote object '/system/framework/framework2.jar' does not exist
>>> adb pull /system/framework/core.jar fs-oneplus2/
adb: error: remote object '/system/framework/core.jar' does not exist
>>> adb pull /system/framework/core-libart.jar fs-oneplus2/
[100%] /system/framework/core-libart.jar
>>> adb pull /system/framework/core-oj.jar fs-oneplus2/
adb: error: remote object '/system/framework/core-oj.jar' does not exist
>>> adb pull /system/framework/ext.jar fs-oneplus2/
[100%] /system/framework/ext.jar
>>> adb pull /system/framework/services.jar fs-oneplus2/
[100%] /system/framework/services.jar
>>> adb pull /system/priv-app/Settings/Settings.apk fs-oneplus2/
[100%] /system/priv-app/Settings/Settings.apk

*** pull-fileset: success
```

**IMPORTANT:** Make sure you keep a backup of the fileset you just pulled somewhere safe. You will need this backup to patch your device again or to restore it to its unpatched state. To do these things without a backup you will need to reinstall Android on your device.

Patch the fileset with the required hook for Android 6.0:

`$ ls patches`
```
bulk-patch-builder.log
sigspoof-core
sigspoof-hook-1.5-2.3
sigspoof-hook-4.0
sigspoof-hook-4.1-6.0
sigspoof-hook-7.0-9.0
sigspoof-ui-global-4.0
sigspoof-ui-global-4.1
sigspoof-ui-global-4.2-5.0
sigspoof-ui-global-5.1-6.0
sigspoof-ui-global-7.0-7.1
sigspoof-ui-global-8.0-8.1
sigspoof-ui-global-9.0
```

`$ ./patch-fileset`
```
usage: patch-fileset <patch-dex-dir> <fileset-api-level> <fileset-dir> [ <target-dir-to-create> | --dry-run [ <dexpatcher-option> ... ] ]
```

`$ ./patch-fileset patches/sigspoof-hook-4.1-6.0/ 23 fs-oneplus2/`
```
>>> target directory: fs-oneplus2__sigspoof-hook-4.1-6.0
>>> apply patch: services.jar
>>> dexpatcher --api-level 23 --verbose --output fs-oneplus2__sigspoof-hook-4.1-6.0/tmp/services.jar/patched-dex --multi-dex fs-oneplus2/services.jar patches/sigspoof-hook-4.1-6.0/services.jar.dex
info: read 'fs-oneplus2/services.jar'
info: read 'patches/sigspoof-hook-4.1-6.0/services.jar.dex'
info: write 'fs-oneplus2__sigspoof-hook-4.1-6.0/tmp/services.jar/patched-dex'
0 error(s), 0 warning(s)
>>> repack: services.jar
deleting: classes.dex
  adding: classes.dex (deflated 55%)

*** patch-fileset: success
```

The patched fileset was output to `fs-oneplus2__sigspoof-hook-4.1-6.0` by default.

Now patch this resulting fileset again to add the core patch:

`$ ./patch-fileset patches/sigspoof-core/ 23 fs-oneplus2__sigspoof-hook-4.1-6.0/`
```
>>> target directory: fs-oneplus2__sigspoof-hook-4.1-6.0__sigspoof-core
>>> apply patch: services.jar
>>> dexpatcher --api-level 23 --verbose --output fs-oneplus2__sigspoof-hook-4.1-6.0__sigspoof-core/tmp/services.jar/patched-dex --multi-dex fs-oneplus2__sigspoof-hook-4.1-6.0/services.jar patches/sigspoof-core/services.jar.dex
info: read 'fs-oneplus2__sigspoof-hook-4.1-6.0/services.jar'
info: read 'patches/sigspoof-core/services.jar.dex'
info: write 'fs-oneplus2__sigspoof-hook-4.1-6.0__sigspoof-core/tmp/services.jar/patched-dex'
0 error(s), 0 warning(s)
>>> repack: services.jar
deleting: classes.dex
  adding: classes.dex (deflated 55%)

*** patch-fileset: success
```

Finally, add the corresponding UI patch:

`$ ./patch-fileset patches/sigspoof-ui-global-5.1-6.0/ 23 fs-oneplus2__sigspoof-hook-4.1-6.0__sigspoof-core/`
```
>>> target directory: fs-oneplus2__sigspoof-hook-4.1-6.0__sigspoof-core__sigspoof-ui-global-5.1-6.0
>>> apply patch: services.jar
>>> dexpatcher --api-level 23 --verbose --output fs-oneplus2__sigspoof-hook-4.1-6.0__sigspoof-core__sigspoof-ui-global-5.1-6.0/tmp/services.jar/patched-dex --multi-dex fs-oneplus2__sigspoof-hook-4.1-6.0__sigspoof-core/services.jar patches/sigspoof-ui-global-5.1-6.0/services.jar.dex
info: read 'fs-oneplus2__sigspoof-hook-4.1-6.0__sigspoof-core/services.jar'
info: read 'patches/sigspoof-ui-global-5.1-6.0/services.jar.dex'
info: write 'fs-oneplus2__sigspoof-hook-4.1-6.0__sigspoof-core__sigspoof-ui-global-5.1-6.0/tmp/services.jar/patched-dex'
0 error(s), 0 warning(s)
>>> repack: services.jar
deleting: classes.dex
  adding: classes.dex (deflated 55%)
>>> apply patch: Settings.apk
>>> dexpatcher --api-level 23 --verbose --output fs-oneplus2__sigspoof-hook-4.1-6.0__sigspoof-core__sigspoof-ui-global-5.1-6.0/tmp/Settings.apk/patched-dex --multi-dex fs-oneplus2__sigspoof-hook-4.1-6.0__sigspoof-core/Settings.apk patches/sigspoof-ui-global-5.1-6.0/Settings.apk.dex
info: read 'fs-oneplus2__sigspoof-hook-4.1-6.0__sigspoof-core/Settings.apk'
info: read 'patches/sigspoof-ui-global-5.1-6.0/Settings.apk.dex'
info: write 'fs-oneplus2__sigspoof-hook-4.1-6.0__sigspoof-core__sigspoof-ui-global-5.1-6.0/tmp/Settings.apk/patched-dex'
0 error(s), 0 warning(s)
>>> repack: Settings.apk
deleting: classes.dex
  adding: classes.dex (deflated 56%)

*** patch-fileset: success
```

The triple-patched fileset is now in `fs-oneplus2__sigspoof-hook-4.1-6.0__sigspoof-core__sigspoof-ui-global-5.1-6.0`.

Push it to the device:

`$ ./push-fileset fs-oneplus2__sigspoof-hook-4.1-6.0__sigspoof-core__sigspoof-ui-global-5.1-6.0/`
```
>>> adb root
restarting adbd as root
>>> adb remount
remount succeeded
>>> adb push fs-oneplus2__sigspoof-hook-4.1-6.0__sigspoof-core__sigspoof-ui-global-5.1-6.0/framework.jar /system/framework/
[100%] /system/framework/framework.jar
>>> adb push fs-oneplus2__sigspoof-hook-4.1-6.0__sigspoof-core__sigspoof-ui-global-5.1-6.0/ext.jar /system/framework/
[100%] /system/framework/ext.jar
>>> adb push fs-oneplus2__sigspoof-hook-4.1-6.0__sigspoof-core__sigspoof-ui-global-5.1-6.0/services.jar /system/framework/
[100%] /system/framework/services.jar
>>> adb push fs-oneplus2__sigspoof-hook-4.1-6.0__sigspoof-core__sigspoof-ui-global-5.1-6.0/Settings.apk /system/priv-app/Settings/
[100%] /system/priv-app/Settings/Settings.apk

*** push-fileset: success
```

Ok! Now reboot to Android:

`$ adb reboot`

Upon reboot, download and install my [signature spoofing checker](https://github.com/Lanchon/sigspoof-checker) app to verify that spoofing works:

`$ wget 'https://f-droid.org/repo/lanchon.sigspoof.checker_2.apk'`

`$ adb install lanchon.sigspoof.checker_2.apk`

Launch the checker app and verify that signature spoofing is in fact disabled. Open up settings, go to developer settings (unlock the item if you need to), scroll to "Allow signature spoofing" (the last option), enable it, go back to the checker app... And profit!
