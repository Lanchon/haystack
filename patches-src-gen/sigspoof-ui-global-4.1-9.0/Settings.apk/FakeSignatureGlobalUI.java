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

abstract class FakeSignatureGlobalUI {

    static final String SECURE_SETTING_KEY = "allow_fake_signature_global";

    static final String PREFERENCE_CATEGORY_KEY = "debug_applications_category";
    static final String PREFERENCE_KEY = SECURE_SETTING_KEY;

    static final String PREFERENCE_TITLE = "Signature spoofing";
    static final String PREFERENCE_SUMMARY =
            //"Allow apps to bypass security systems by pretending to be a different app";
            //"Allow apps to spoof information about their publisher";
            "Allow apps to impersonate other apps";

    static final String WARNING_TITLE = "Allow signature spoofing?";
    static final String WARNING_MESSAGE =
            //"Allowing apps to bypass security systems can lead to serious security and privacy problems! " +
            //"Check that only benign apps use the corresponding permission when this is active.";
            "Allowing apps to impersonate other apps has security and privacy implications. " +
            "Malicious apps can use this functionality to access the private data of other apps.\n\n" +
            "Make sure that you trust all installed apps that use the 'FAKE_PACKAGE_SIGNATURE' " +
            "permission before enabling this setting.";

}

