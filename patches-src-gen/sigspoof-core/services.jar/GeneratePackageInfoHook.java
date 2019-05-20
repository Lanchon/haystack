/*
 * Copyright (C) 2015 Marvin W <https://github.com/mar-v-in>
 * Copyright (C) 2016-2019 Lanchon <https://github.com/Lanchon>
 *
 * Based on Marvin's work:
 *
 *      https://gerrit.omnirom.org/#/c/14898/
 *      https://gerrit.omnirom.org/#/c/14899/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.server.pm;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.pm.Signature;
import android.util.Log;

import lanchon.dexpatcher.annotation.*;

@DexEdit
class GeneratePackageInfoHook {

    @DexAdd
    static class FakeSignatureCore {
        static final String PERMISSION = "android.permission.FAKE_PACKAGE_SIGNATURE";
        static final String METADATA = "fake-signature";
        static final List<String> ALLOWED_SIGNATURES = Arrays.asList(
            "308204433082032ba003020102020900c2e08746644a308d300d06092a864886f70d01010405003074310b3009060355040613025553311330110603550408130a43616c69666f726e6961311630140603550407130d4d6f756e7461696e205669657731143012060355040a130b476f6f676c6520496e632e3110300e060355040b1307416e64726f69643110300e06035504031307416e64726f6964301e170d3038303832313233313333345a170d3336303130373233313333345a3074310b3009060355040613025553311330110603550408130a43616c69666f726e6961311630140603550407130d4d6f756e7461696e205669657731143012060355040a130b476f6f676c6520496e632e3110300e060355040b1307416e64726f69643110300e06035504031307416e64726f696430820120300d06092a864886f70d01010105000382010d00308201080282010100ab562e00d83ba208ae0a966f124e29da11f2ab56d08f58e2cca91303e9b754d372f640a71b1dcb130967624e4656a7776a92193db2e5bfb724a91e77188b0e6a47a43b33d9609b77183145ccdf7b2e586674c9e1565b1f4c6a5955bff251a63dabf9c55c27222252e875e4f8154a645f897168c0b1bfc612eabf785769bb34aa7984dc7e2ea2764cae8307d8c17154d7ee5f64a51a44a602c249054157dc02cd5f5c0e55fbef8519fbe327f0b1511692c5a06f19d18385f5c4dbc2d6b93f68cc2979c70e18ab93866b3bd5db8999552a0e3b4c99df58fb918bedc182ba35e003c1b4b10dd244a8ee24fffd333872ab5221985edab0fc0d0b145b6aa192858e79020103a381d93081d6301d0603551d0e04160414c77d8cc2211756259a7fd382df6be398e4d786a53081a60603551d2304819e30819b8014c77d8cc2211756259a7fd382df6be398e4d786a5a178a4763074310b3009060355040613025553311330110603550408130a43616c69666f726e6961311630140603550407130d4d6f756e7461696e205669657731143012060355040a130b476f6f676c6520496e632e3110300e060355040b1307416e64726f69643110300e06035504031307416e64726f6964820900c2e08746644a308d300c0603551d13040530030101ff300d06092a864886f70d010104050003820101006dd252ceef85302c360aaace939bcff2cca904bb5d7a1661f8ae46b2994204d0ff4a68c7ed1a531ec4595a623ce60763b167297a7ae35712c407f208f0cb109429124d7b106219c084ca3eb3f9ad5fb871ef92269a8be28bf16d44c8d9a08e6cb2f005bb3fe2cb96447e868e731076ad45b33f6009ea19c161e62641aa99271dfd5228c5c587875ddb7f452758d661f6cc0cccb7352e424cc4365c523532f7325137593c4ae341f4db41edda0d0b1071a7c440f0fe9ea01cb627ca674369d084bd2fd911ff06cdbf2cfa10dc0f893ae35762919048c7efc64c7144178342f70581c9de573af55b390dd7fdb9418631895d5f759f30112687ff621410c069308a"
        );
        static final List<String> ALLOWED_PACKAGES = Arrays.asList(
            "com.google.android.gms",
            "com.android.vending"
        );
    }

    @DexAdd
    private static boolean getPerAppEnable(PackageInfo pi, Context context, PackageParser.Package p, int flags, int userId) {
        return ((pi.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0 || (pi.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) && FakeSignatureCore.ALLOWED_PACKAGES.contains(pi.packageName);
    }

    @DexAdd
    private static boolean getGlobalEnable(PackageInfo pi, Context context, PackageParser.Package p, int flags, int userId) {
        return true;
    }

    @DexReplace
    static PackageInfo hook(PackageInfo pi, Context context, PackageParser.Package p, int flags, int userId) {
        try {
            if ((flags & PackageManager.GET_SIGNATURES) != 0) {
                if (p.requestedPermissions != null) {
                    if (p.requestedPermissions.contains(FakeSignatureCore.PERMISSION)) {
                        if (getGlobalEnable(pi, context, p, flags, userId) &&
                                getPerAppEnable(pi, context, p, flags, userId)) {
                            if (p.mAppMetaData != null) {
                                Object fakeSignatureData = p.mAppMetaData.get(FakeSignatureCore.METADATA);
                                if (fakeSignatureData != null) {
                                    if (fakeSignatureData instanceof String) {
                                        if(FakeSignatureCore.ALLOWED_SIGNATURES.contains(fakeSignatureData))
                                            pi.signatures = new Signature[] { new Signature((String) fakeSignatureData) };
                                    } else {
                                        Log.e("GeneratePackageInfoHook", "invalid '" + FakeSignatureCore.METADATA + "' metadata");
                                    }
                                } else {
                                    Log.e("GeneratePackageInfoHook", "missing 'fake-signature' metadata");
                                }
                            } else {
                                Log.e("GeneratePackageInfoHook", "null mAppMetaData");
                            }
                        }
                    }
                } else {
                    Log.e("GeneratePackageInfoHook", "null requestedPermissions");
                }
            }
        } catch (Throwable t) {
            // We should never die because of any failures, this is system code!
            // Eating up instances of Error might seem excessive, but they can be
            // the result of improperly applied patches so we will handle them here.
            Log.e("GeneratePackageInfoHook", "hook exception", t);
        }
        return pi;
    }

}
