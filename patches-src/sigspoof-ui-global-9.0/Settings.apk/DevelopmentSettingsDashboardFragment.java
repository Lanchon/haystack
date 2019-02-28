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

import java.util.List;

import android.app.Activity;
import android.content.Context;

import com.android.settings.dashboard.RestrictedDashboardFragment;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;

import android.annotation.XmlRes;
import lanchon.dexpatcher.annotation.*;

@DexEdit(contentOnly = true)
public abstract class DevelopmentSettingsDashboardFragment extends RestrictedDashboardFragment {

    @DexIgnore public DevelopmentSettingsDashboardFragment() { super(null); throw null; }

    @DexIgnore <T extends AbstractPreferenceController> T getDevelopmentOptionsController(Class<T> clazz) { throw null; }

    @DexAdd
    @Override
    public void addPreferencesFromResource(@XmlRes int preferencesResId) {
        super.addPreferencesFromResource(preferencesResId);
        FakeSignatureGlobalPreferenceController.addPreferences(this);
    }

    @DexWrap
    private static List<AbstractPreferenceController> buildPreferenceControllers(Context context, Activity activity, Lifecycle lifecycle,
            DevelopmentSettingsDashboardFragment fragment, BluetoothA2dpConfigStore bluetoothA2dpConfigStore) {
        List<AbstractPreferenceController> controllers = buildPreferenceControllers(context, activity, lifecycle, fragment, bluetoothA2dpConfigStore);
        controllers.add(new FakeSignatureGlobalPreferenceController(context, fragment));
        return controllers;
    }

    @DexAdd
    void onFakeSignatureGlobalDialogConfirmed() {
        getDevelopmentOptionsController(FakeSignatureGlobalPreferenceController.class).onFakeSignatureGlobalDialogConfirmed();
    }

    @DexAdd
    void onFakeSignatureGlobalDialogDismissed() {
        getDevelopmentOptionsController(FakeSignatureGlobalPreferenceController.class).onFakeSignatureGlobalDialogDismissed();
    }

}
