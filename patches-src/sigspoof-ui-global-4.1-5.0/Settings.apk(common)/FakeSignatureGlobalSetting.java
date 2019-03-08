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

import android.app.Activity;
import android.provider.Settings;

abstract class FakeSignatureGlobalSetting {

    private static final String SECURE_SETTING_KEY = "allow_fake_signature_global";

    static boolean read(Activity activity) {
        return Settings.Secure.getInt(activity.getContentResolver(), SECURE_SETTING_KEY, 0) != 0;
    }

    static void write(Activity activity, boolean newValue) {
        Settings.Secure.putInt(activity.getContentResolver(), SECURE_SETTING_KEY, newValue ? 1 : 0);
    }

}
