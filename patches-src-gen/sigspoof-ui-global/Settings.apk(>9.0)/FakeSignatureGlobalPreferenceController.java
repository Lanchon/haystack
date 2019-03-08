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

package com.android.settings.development;

import android.content.Context;
import android.os.UserManager;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;

import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;

class FakeSignatureGlobalPreferenceController extends DeveloperOptionsPreferenceController
        implements Preference.OnPreferenceChangeListener, PreferenceControllerMixin {

    private static final boolean SHOW_WARNING_DIALOG = true;

    private static final boolean HIDE_IF_NON_ADMIN_USER = false;
    private static final boolean DISABLE_IF_NON_ADMIN_USER = true;

    private static final boolean RESET_IF_DEVELOPER_OPTIONS_DISABLED = false;

    private final DevelopmentSettingsDashboardFragment mFragment;

    FakeSignatureGlobalPreferenceController(Context context, DevelopmentSettingsDashboardFragment fragment) {
        super(context);
        mFragment = fragment;
    }

    @Override
    public String getPreferenceKey() {
        return FakeSignatureGlobalUI.getPreferenceKey();
    }

    @Override
    public void updateState(Preference preference) {
        updatePreference();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        writeSettingWithWarningDialog((Boolean) newValue);
        return true;
    }

    // Show warning dialog if needed

    private void writeSettingWithWarningDialog(boolean newValue) {
        if (SHOW_WARNING_DIALOG && newValue) {
            FakeSignatureGlobalWarningDialog.show(mFragment);
        } else {
            writeSetting(newValue);
        }
    }

    void onFakeSignatureGlobalDialogConfirmed() {
        writeSetting(true);
    }

    void onFakeSignatureGlobalDialogDismissed() {
        if (mFragment.getActivity() != null) {
            updatePreference();
        }
    }

    // Hide if non-admin user

    @Override
    public boolean isAvailable() {
        return !(HIDE_IF_NON_ADMIN_USER && !isAdminUser());
    }

    // Disable if non-admin user

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        if (DISABLE_IF_NON_ADMIN_USER && !isAdminUser()) {
            mPreference.setEnabled(false);
        }
    }

    @Override
    protected void onDeveloperOptionsSwitchEnabled() {
        if (!(DISABLE_IF_NON_ADMIN_USER && !isAdminUser())) {
            mPreference.setEnabled(true);
        }
    }

    // Reset if developer options disabled

    @Override
    protected void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        if (RESET_IF_DEVELOPER_OPTIONS_DISABLED && !((HIDE_IF_NON_ADMIN_USER || DISABLE_IF_NON_ADMIN_USER) && !isAdminUser())) {
            writeSetting(false);
            updatePreference();
        }
    }

    // Convenience methods

    private void writeSetting(boolean newValue) {
        FakeSignatureGlobalSetting.write(mFragment.getActivity(), newValue);
    }

    private void updatePreference() {
        ((SwitchPreference) mPreference).setChecked(FakeSignatureGlobalSetting.read(mFragment.getActivity()));
    }

    private boolean isAdminUser() {
        // UserManager.isAdminUser() triggers a javac bug:

        // An exception has occurred in the compiler (1.8.0_191). Please file a bug against the Java compiler via the Java bug
        // reporting page (http://bugreport.java.com) after checking the Bug Database (http://bugs.java.com) for duplicates.
        // Include your program and the following diagnostic in your report. Thank you.
        // java.lang.NullPointerException
        //         at com.sun.tools.javac.code.Symbol$ClassSymbol.isSubClass(Symbol.java:1020)

        UserManager userManager = (UserManager) mContext.getSystemService(Context.USER_SERVICE);
        //return userManager.isAdminUser();
        try {
            return (Boolean) userManager.getClass().getMethod("isAdminUser").invoke(userManager);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

}
