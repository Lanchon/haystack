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

/*<8.0*/ package com.android.settings;
//>8.0// package com.android.settings.development;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
/*<7.0*/ import android.preference.Preference;
//>7.0// import android.support.v7.preference.Preference;
/*<7.0*/ import android.preference.PreferenceFragment;
//>7.0// import android.support.v14.preference.PreferenceFragment;
/*<7.0*/ import android.preference.PreferenceScreen;
//<5.1// import android.preference.CheckBoxPreference;
/*>5.1*/ /*<7.0*/ import android.preference.SwitchPreference;
//>7.0// import android.support.v14.preference.SwitchPreference;

//>8.0// import com.android.settings.Utils;

import lanchon.dexpatcher.annotation.*;

@DexEdit(contentOnly = true)
public class DevelopmentSettings extends PreferenceFragment {

    @DexAdd private static final boolean SHOW_WARNING_DIALOG = true;

    @DexAdd private static final boolean RESET_IF_DEVELOPER_OPTIONS_DISABLED = false;

    @DexIgnore private /* final */ ArrayList<Preference> mAllPrefs;
    //<5.1// @DexIgnore private /* final */ ArrayList<CheckBoxPreference> mResetCbPrefs;
    /*>5.1*/ @DexIgnore private /* final */ ArrayList<SwitchPreference> mResetSwitchPrefs;

    //<5.1// @DexAdd private CheckBoxPreference mFakeSignatureGlobalPreference;
    /*>5.1*/ @DexAdd private SwitchPreference mFakeSignatureGlobalPreference;
    @DexAdd /* private */ AlertDialog mFakeSignatureGlobalDialog;

    @DexIgnore private DevelopmentSettings() { throw null; }

    //>7.0// @DexIgnore @Override public void onCreatePreferences(Bundle savedInstanceState, String rootKey) { throw null; }
    /*>4.2*/ @DexIgnore private void disableForUser(Preference pref) { throw null; }
    //<5.1// @DexIgnore void updateCheckBox(CheckBoxPreference checkBox, boolean value) { throw null; }
    /*>5.1*/ @DexIgnore void updateSwitchPreference(SwitchPreference switchPreference, boolean value) { throw null; }

    @DexAppend
    @Override
    public void onCreate(Bundle icicle) {
        //<5.1// CheckBoxPreference p = FakeSignatureGlobalUI.addPreference(this);
        /*>5.1*/ SwitchPreference p = FakeSignatureGlobalUI.addPreference(this);
        if (p != null) {
            mFakeSignatureGlobalPreference = p;
            mAllPrefs.add(p);
            /*>4.2*/ /*<7.0*/ if (!android.os.Process.myUserHandle().equals(android.os.UserHandle.OWNER)) disableForUser(p); else
            //>7.0// if (!((android.os.UserManager) getActivity().getSystemService(android.content.Context.USER_SERVICE)).isAdminUser()) disableForUser(p); else
            //<5.1// if (RESET_IF_DEVELOPER_OPTIONS_DISABLED) mResetCbPrefs.add(p);
            /*>5.1*/ if (RESET_IF_DEVELOPER_OPTIONS_DISABLED) mResetSwitchPrefs.add(p);
        }
    }

    @DexAppend
    private void updateAllOptions() {
        if (mFakeSignatureGlobalPreference != null) {
            updateFakeSignatureGlobalPreference();
        }
    }

    @DexAppend
    private void dismissDialogs() {
        if (mFakeSignatureGlobalDialog != null) {
            mFakeSignatureGlobalDialog.dismiss();
            mFakeSignatureGlobalDialog = null;
        }
    }

    @DexWrap
    @Override
    /*<7.0*/ public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
    //>7.0// public boolean onPreferenceTreeClick(Preference preference) {
        if (Utils.isMonkeyRunning()) {
            return false;
        }
        if (mFakeSignatureGlobalPreference != null) {
            if (preference == mFakeSignatureGlobalPreference) {
                writeFakeSignatureGlobalSettingWithWarningDialog(mFakeSignatureGlobalPreference.isChecked());
                return false;
            }
        }
        /*<7.0*/ return onPreferenceTreeClick(preferenceScreen, preference);
        //>7.0// return onPreferenceTreeClick(preference);
    }

    @DexAdd
    private void writeFakeSignatureGlobalSettingWithWarningDialog(boolean newValue) {
        if (SHOW_WARNING_DIALOG && newValue) {
            if (mFakeSignatureGlobalDialog != null) {
                dismissDialogs();
            }
            FakeSignatureGlobalDialogListener listener = new FakeSignatureGlobalDialogListener();
            mFakeSignatureGlobalDialog = FakeSignatureGlobalUI.createWarningDialog(getActivity(), listener);
            mFakeSignatureGlobalDialog.setOnDismissListener(listener);
            mFakeSignatureGlobalDialog.show();
        } else {
            writeFakeSignatureGlobalSetting(newValue);
            updateFakeSignatureGlobalPreference();
        }
    }

    @DexAdd
    class FakeSignatureGlobalDialogListener
            implements DialogInterface.OnClickListener, DialogInterface.OnDismissListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                writeFakeSignatureGlobalSetting(true);
                //updateFakeSignatureGlobalPreference();   // handled by onDismiss(...)
            }
        }
        @Override
        public void onDismiss(DialogInterface dialog) {
            if (DevelopmentSettings.this.getActivity() != null) {
                updateFakeSignatureGlobalPreference();
            }
            mFakeSignatureGlobalDialog = null;
        }
    }

    // Convenience methods

    @DexAdd
    /* private */ void writeFakeSignatureGlobalSetting(boolean newValue) {
        FakeSignatureGlobalSetting.write(getActivity(), newValue);
    }

    @DexAdd
    /* private */ void updateFakeSignatureGlobalPreference() {
        //<5.1// updateCheckBox(mFakeSignatureGlobalPreference, FakeSignatureGlobalSetting.read(getActivity()));
        /*>5.1*/ updateSwitchPreference(mFakeSignatureGlobalPreference, FakeSignatureGlobalSetting.read(getActivity()));
    }

}
