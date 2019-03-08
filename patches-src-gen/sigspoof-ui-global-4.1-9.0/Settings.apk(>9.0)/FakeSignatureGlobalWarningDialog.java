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

import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;

import com.android.settingslib.core.lifecycle.ObservableDialogFragment;

public class FakeSignatureGlobalWarningDialog extends ObservableDialogFragment
        implements DialogInterface.OnClickListener, DialogInterface.OnDismissListener {

    private static final String TAG = "FakeSignatureGlobalWarningDialog";

    static void show(Fragment fragment) {
        FragmentManager manager = fragment.getActivity().getFragmentManager();
        if (manager.findFragmentByTag(TAG) == null) {
            FakeSignatureGlobalWarningDialog dialog = new FakeSignatureGlobalWarningDialog();
            dialog.setTargetFragment(fragment, /* requestCode: */ 0);
            dialog.show(manager, TAG);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle icicle) {
        return FakeSignatureGlobalUI.createWarningDialog(getActivity(), this);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            Fragment fragment = getTargetFragment();
            if (fragment != null) {
                ((DevelopmentSettingsDashboardFragment) fragment).onFakeSignatureGlobalDialogConfirmed();
            }
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        Fragment fragment = getTargetFragment();
        if (fragment != null) {
            ((DevelopmentSettingsDashboardFragment) fragment).onFakeSignatureGlobalDialogDismissed();
        }
    }

}
