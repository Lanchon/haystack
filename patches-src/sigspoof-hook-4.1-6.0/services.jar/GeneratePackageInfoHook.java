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

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageParser;

import lanchon.dexpatcher.annotation.*;

class GeneratePackageInfoHook {

    static PackageInfo hook(PackageInfo pi, Context context, PackageParser.Package p, int flags, int userId) {
        return pi;
    }

    private GeneratePackageInfoHook() {}

    /*>4.1*/ // Patch-time marker for hook implementations that require a valid userdId.
    /*>4.1*/ private boolean multiUserMarker;

}
