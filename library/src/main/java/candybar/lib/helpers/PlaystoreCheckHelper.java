package candybar.lib.helpers;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.appcompat.app.AppCompatActivity;

import com.afollestad.materialdialogs.MaterialDialog;

import candybar.lib.R;
import candybar.lib.fragments.dialog.ChangelogFragment;
import candybar.lib.preferences.Preferences;

public class PlaystoreCheckHelper {

    public Context mContext;
    private static String contentString;
    private static boolean checkPassed;

    public PlaystoreCheckHelper(Context context) {
        mContext = context;
    }

    public void run() {
        if (mContext.getResources().getBoolean(R.bool.playstore_check_enabled)) {
            PackageManager pm = mContext.getPackageManager();
            String installerPackage = pm.getInstallerPackageName(mContext.getPackageName());

            if (installerPackage == null || !installerPackage.contentEquals("com.android.vending")) {
                ComponentName compName = new ComponentName(mContext.getPackageName(), mContext.getPackageName() + ".alias.Intent");
                pm.setComponentEnabledSetting(
                        compName,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);
                contentString = mContext.getResources().getString(R.string.playstore_check_failed);
                checkPassed = false;
            } else {
                contentString = mContext.getResources().getString(R.string.playstore_check_success);
                checkPassed = true;
            }

            if (!checkPassed) {
                showDialog(mContext);
            } else if (Preferences.get(mContext).isFirstRun()) {
                showDialog(mContext);
            }
        }
    }

    private void showDialog(Context context) {
        new MaterialDialog.Builder(mContext)
                .typeface(
                        TypefaceHelper.getMedium(mContext),
                        TypefaceHelper.getRegular(mContext))
                .title(R.string.playstore_check)
                .content(contentString)
                .positiveText(R.string.close)
                .cancelable(false)
                .canceledOnTouchOutside(false)
                .onPositive((dialog, which) -> onPlaystoreChecked(checkPassed))
                .show();
    }

    private void onPlaystoreChecked(boolean success) {
        if (success) {
            Preferences.get(mContext).setFirstRun(false);
            Preferences.get(mContext).setLicensed(true);
            if (Preferences.get(mContext).isNewVersion())
                ChangelogFragment.showChangelog(((AppCompatActivity) mContext).getSupportFragmentManager());
        } else {
            Preferences.get(mContext).setLicensed(false);
            ((AppCompatActivity) mContext).finish();
        }
    }
}
