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
    }

    @DexAdd
    private static boolean getPerAppEnable(PackageInfo pi, Context context, PackageParser.Package p, int flags, int userId) {
        // MAYBE FIXME: at least Android 4.1 required (for requestedPermissionsFlags)
        if (pi.requestedPermissions==null || pi.requestedPermissionsFlags==null) {
            return false;
        }
        if (pi.requestedPermissions.length != pi.requestedPermissionsFlags.length) {
            Log.e("GeneratePackageInfoHook", "pi.requestedPermissions.length != pi.requestedPermissionsFlags.length");
            return false;
        }
        for (int i=0; i<pi.requestedPermissions.length; i++) {
            if (pi.requestedPermissions[i].equals(FakeSignatureCore.PERMISSION)) {
                boolean r = ( (pi.requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0 );
                if (!r) {
                    Log.w("GeneratePackageInfoHook", "user didn't grant FAKE_PACKAGE_SIGNATURE permission");
                }
                return r;
            }
        }
        return false;
    }

    @DexAdd
    private static boolean getGlobalEnable(PackageInfo pi, Context context, PackageParser.Package p, int flags, int userId) {
        return true;
    }

    @DexReplace
    static PackageInfo hook(PackageInfo pi, Context context, PackageParser.Package p, int flags, int userId) {
        try {
            if ((flags & PackageManager.GET_SIGNATURES) != 0) {
                if (getGlobalEnable(pi, context, p, flags, userId) &&
                        getPerAppEnable(pi, context, p, flags, userId)) {
                    if (p.mAppMetaData != null) {
                        Object fakeSignatureData = p.mAppMetaData.get(FakeSignatureCore.METADATA);
                        if (fakeSignatureData != null) {
                            if (fakeSignatureData instanceof String) {
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
        } catch (Throwable t) {
            // We should never die because of any failures, this is system code!
            // Eating up instances of Error might seem excessive, but they can be
            // the result of improperly applied patches so we will handle them here.
            Log.e("GeneratePackageInfoHook", "hook exception", t);
        }
        return pi;
    }

}
