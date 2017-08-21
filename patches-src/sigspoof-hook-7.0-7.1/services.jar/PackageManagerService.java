/*
 * Copyright (C) 2016 Lanchon <https://github.com/Lanchon>
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

import com.android.server.pm.PackageSetting;

import lanchon.dexpatcher.annotation.*;

@DexEdit(onlyEditMembers = true)
public class PackageManagerService /* extends IPackageManager.Stub */ {

    @DexIgnore
    /* final */ Context mContext;

    @DexWrap
    private PackageInfo generatePackageInfo(PackageSetting ps, int flags, int userId) {
        PackageInfo pi = generatePackageInfo(ps, flags, userId);
        if (ps != null && pi != null) {
            PackageParser.Package p = ps.pkg;
            if (p != null) pi = GeneratePackageInfoHook.hook(pi, mContext, p, flags, userId);
        }
        return pi;
    }

}
