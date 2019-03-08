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

//<8.0// package com.android.settings;
/*>8.0*/ package com.android.settings.development;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v14.preference.PreferenceFragment;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.PreferenceGroup;
import android.util.Log;

abstract class FakeSignatureGlobalUI {

    private static final String PREFERENCE_CATEGORY_KEY = "debug_applications_category";
    private static final String PREFERENCE_KEY = "allow_fake_signature_global";

    private static final String PREFERENCE_TITLE = "Signature spoofing";
    private static final String PREFERENCE_SUMMARY =
            //"Allow apps to bypass security systems by pretending to be a different app";
            //"Allow apps to spoof information about their publisher";
            "Allow apps to impersonate other apps";

    private static final String WARNING_TITLE = "Allow signature spoofing?";
    private static final String WARNING_MESSAGE =
            //"Allowing apps to bypass security systems can lead to serious security and privacy problems! " +
            //"Check that only benign apps use the corresponding permission when this is active.";
            "Allowing apps to impersonate other apps has security and privacy implications. " +
            "Malicious apps can use this functionality to access the private data of other apps.\n\n" +
            "Make sure that you trust all installed apps that use the 'FAKE_PACKAGE_SIGNATURE' " +
            "permission before enabling this setting.";

    static String getPreferenceKey() {
        return PREFERENCE_KEY;
    }

    static SwitchPreference addPreference(PreferenceFragment fragment) {
        PreferenceGroup pg = (PreferenceGroup) fragment.findPreference(PREFERENCE_CATEGORY_KEY);
        if (pg != null) {
            SwitchPreference p = new SwitchPreference(pg.getContext());
            p.setKey(PREFERENCE_KEY);
            p.setTitle(PREFERENCE_TITLE);
            p.setSummary(PREFERENCE_SUMMARY);
            p.setPersistent(false);
            pg.addPreference(p);
            return p;
        } else {
            Log.e("FakeSignatureGlobalUI", "cannot find '" + PREFERENCE_CATEGORY_KEY +"' preference category");
            return null;
        }
    }

    static AlertDialog createWarningDialog(Activity activity, DialogInterface.OnClickListener buttonListener) {
        return new AlertDialog.Builder(activity)
                .setTitle(FakeSignatureGlobalUI.WARNING_TITLE)
                .setMessage(FakeSignatureGlobalUI.WARNING_MESSAGE)
                //.setIconAttribute(android.R.attr.alertDialogIcon)
                .setPositiveButton(android.R.string.yes, buttonListener)
                .setNegativeButton(android.R.string.no, buttonListener)
                .create();
    }

}
