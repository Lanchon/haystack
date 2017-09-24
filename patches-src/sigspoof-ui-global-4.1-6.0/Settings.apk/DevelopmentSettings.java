/*
 * Copyright (C) 2015 Marvin W <https://github.com/mar-v-in>
 * Copyright (C) 2016 Lanchon <https://github.com/Lanchon>
 *
 * This is Marvin's work converted to DexPatcher patches by Lanchon.
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

package com.android.settings;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.preference.TwoStatePreference;
import android.provider.Settings;
import android.util.Log;

import lanchon.dexpatcher.annotation.*;

@DexEdit(onlyEditMembers = true)
public class DevelopmentSettings extends PreferenceFragment {

    // SwitchPreference Hooks

    @DexIgnore private final ArrayList<SwitchPreference> mResetSwitchPrefs = new ArrayList<SwitchPreference>();
    @DexIgnore void updateSwitchPreference(SwitchPreference switchPreference, boolean value) { throw null; }

    // CheckBoxPreference Hooks

    @DexIgnore private final ArrayList<CheckBoxPreference> mResetCbPrefs = new ArrayList<CheckBoxPreference>();
    @DexIgnore void updateCheckBox(CheckBoxPreference checkBox, boolean value) { throw null; }

    // TwoStatePreference Interface

    @DexAdd
    private boolean useSwitchPreference() {
        return Build.VERSION.SDK_INT >= 22;     // Build.VERSION_CODES.LOLLIPOP_MR1
    }

    @DexAdd
    private TwoStatePreference createTwoStatePreference(Context context) {
        if (useSwitchPreference()) {
            return new SwitchPreference(context);
        } else {
            return new CheckBoxPreference(context);
        }
    }

    @DexAdd
    private boolean mResetTwoStatePrefsAdd(TwoStatePreference preference) {
        if (useSwitchPreference()) {
            return mResetSwitchPrefs.add((SwitchPreference) preference);
        } else {
            return mResetCbPrefs.add((CheckBoxPreference) preference);
        }
    }

    @DexAdd
    private void updateTwoStatePreference(TwoStatePreference preference, boolean value) {
        if (useSwitchPreference()) {
            updateSwitchPreference((SwitchPreference) preference, value);
        } else {
            updateCheckBox((CheckBoxPreference) preference, value);
        }
    }

    @DexAdd
    static class FakeSignatureGlobalUI {

        static final String DEBUG_APPLICATIONS_CATEGORY_KEY = "debug_applications_category";

        static final String SETTING_SECURE_KEY = "allow_fake_signature_global";

        static final String SETTING_TITLE =
                "Allow signature spoofing";
        static final String SETTING_SUMMARY =
                //"Allow apps to bypass security systems by pretending to be a different app";
                //"Allow apps to spoof information about their publisher";
                "Allow apps to impersonate other apps";
        static final String SETTING_WARNING =
                //"Allowing apps to bypass security systems can lead to serious security and privacy problems! " +
                //"Check that only benign apps use the corresponding permission when this is active.";
                "Allowing apps to impersonate other apps has security and privacy implications. " +
                "Malicious apps can use this functionality to access the private data of other apps.\n\n" +
                "Make sure that you trust all installed apps that use the 'FAKE_PACKAGE_SIGNATURE' " +
                "permission before enabling this setting.";

    }

    @DexIgnore private final ArrayList<Preference> mAllPrefs = new ArrayList<Preference>();

    @DexAdd private TwoStatePreference mAllowFakeSignatureGlobal;
    @DexAdd /* private */ Dialog mAllowFakeSignatureGlobalDialog;

    @DexIgnore
    private DevelopmentSettings() { throw null; }

    @DexAppend
    @Override
    public void onCreate(Bundle icicle) {
        try {
            PreferenceGroup pg = (PreferenceGroup) findPreference(
                    FakeSignatureGlobalUI.DEBUG_APPLICATIONS_CATEGORY_KEY);
            if (pg != null) {
                TwoStatePreference p = createTwoStatePreference(pg.getContext());
                p.setKey(FakeSignatureGlobalUI.SETTING_SECURE_KEY);
                p.setTitle(FakeSignatureGlobalUI.SETTING_TITLE);
                p.setSummary(FakeSignatureGlobalUI.SETTING_SUMMARY);
                p.setPersistent(false);
                mResetTwoStatePrefsAdd(p);
                mAllPrefs.add(p);
                //pg.setOrderingAsAdded(true);
                pg.addPreference(p);
                mAllowFakeSignatureGlobal = p;
            } else {
                Log.e("DevelopmentSettings_FakeSignatureGlobalUI", "cannot find 'applications' preference category");
            }
        } catch (Throwable t) {
            Log.e("DevelopmentSettings_FakeSignatureGlobalUI", "onCreate exception", t);
        }
    }

    @DexAppend
    private void updateAllOptions() {
        if (mAllowFakeSignatureGlobal != null) {
            updateAllowFakeSignatureGlobalOption();
        }
    }

    @DexAdd
    /* private */ void updateAllowFakeSignatureGlobalOption() {
        boolean value = Settings.Secure.getInt(getActivity().getContentResolver(),
                FakeSignatureGlobalUI.SETTING_SECURE_KEY, 0) != 0;
        updateTwoStatePreference(mAllowFakeSignatureGlobal, value);
    }

    @DexWrap
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (Utils.isMonkeyRunning()) {
            return false;
        }
        if (mAllowFakeSignatureGlobal != null) {
            if (preference == mAllowFakeSignatureGlobal) {
                writeAllowFakeSignatureGlobalOption();
                return false;
            }
        }
        return onPreferenceTreeClick(preferenceScreen, preference);
    }

    @DexAdd
    private void writeAllowFakeSignatureGlobalOption() {
        if (mAllowFakeSignatureGlobal.isChecked()) {
            if (mAllowFakeSignatureGlobalDialog != null) {
                dismissDialogs();
            }
            @DexAdd
            class Listener implements DialogInterface.OnClickListener, DialogInterface.OnDismissListener {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        Settings.Secure.putInt(getActivity().getContentResolver(),
                                FakeSignatureGlobalUI.SETTING_SECURE_KEY, 1);
                    }
                    //updateAllowFakeSignatureGlobalOption();   // handled by onDismiss(...)
                }
                @Override
                public void onDismiss(DialogInterface dialog) {
                    updateAllowFakeSignatureGlobalOption();
                    mAllowFakeSignatureGlobalDialog = null;
                }
            }
            Listener listener = new Listener();
            mAllowFakeSignatureGlobalDialog = new AlertDialog.Builder(getActivity())
                    .setMessage(FakeSignatureGlobalUI.SETTING_WARNING)
                    .setTitle(FakeSignatureGlobalUI.SETTING_TITLE)
                    .setIconAttribute(android.R.attr.alertDialogIcon)
                    .setPositiveButton(android.R.string.yes, listener)
                    .setNegativeButton(android.R.string.no, listener)
                    .create();
            mAllowFakeSignatureGlobalDialog.setOnDismissListener(listener);
            mAllowFakeSignatureGlobalDialog.show();
        } else {
            Settings.Secure.putInt(getActivity().getContentResolver(),
                    FakeSignatureGlobalUI.SETTING_SECURE_KEY, 0);
            updateAllowFakeSignatureGlobalOption();
        }
    }

    @DexAppend
    private void dismissDialogs() {
        if (mAllowFakeSignatureGlobalDialog != null) {
            mAllowFakeSignatureGlobalDialog.dismiss();
            mAllowFakeSignatureGlobalDialog = null;
        }
    }

}
