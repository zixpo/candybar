package candybar.lib.helpers;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.appcompat.app.AppCompatActivity;

import com.afollestad.materialdialogs.MaterialDialog;
import com.danimahardhika.android.helpers.core.FileHelper;

import java.io.File;

import candybar.lib.R;
import candybar.lib.fragments.dialog.ChangelogFragment;
import candybar.lib.preferences.Preferences;

public class PlaystoreCheckHelper {

    public Context mContext;
    private static String contentString;

    /*private boolean isTest = true, testPass = true;*/

    public PlaystoreCheckHelper(Context context) {
        mContext = context;
    }

    public static Boolean fromPlaystore(Context context) {
        PackageManager pm = context.getPackageManager();
        String installerPackage = pm.getInstallerPackageName(context.getPackageName());

        return installerPackage != null && installerPackage.contentEquals("com.android.vending");
    }

    public void run() {
        /*if (isTest) {
            if (testPass) {
                contentString = mContext.getResources().getString(R.string.playstore_check_success);
                checkPassed = true;
            } else {
                contentString = mContext.getResources().getString(R.string.playstore_check_failed);
                checkPassed = false;
            }
            onPlaystoreChecked(checkPassed);
            return;
        }*/
        if (mContext.getResources().getBoolean(R.bool.playstore_check_enabled)) {
            boolean checkPassed;
            if (fromPlaystore(mContext)) {
                contentString = mContext.getResources().getString(R.string.playstore_check_success);
                checkPassed = true;
            } else {
                contentString = mContext.getResources().getString(R.string.playstore_check_failed);
                checkPassed = false;
            }

            onPlaystoreChecked(checkPassed);
        }
    }

    private void doIfNewVersion() {
        if (Preferences.get(mContext).isNewVersion()) {
            ChangelogFragment.showChangelog(((AppCompatActivity) mContext).getSupportFragmentManager());
            File cache = mContext.getCacheDir();
            FileHelper.clearDirectory(cache);
        }
    }

    private void onPlaystoreChecked(boolean success) {
        MaterialDialog.Builder dialog = new MaterialDialog.Builder(mContext)
                .typeface(
                        TypefaceHelper.getMedium(mContext),
                        TypefaceHelper.getRegular(mContext))
                .title(R.string.playstore_check)
                .content(contentString)
                .positiveText(R.string.close)
                .onPositive((dial, which) -> {
                    Preferences.get(mContext).setFirstRun(false);
                    doIfNewVersion();
                })
                .cancelable(false)
                .canceledOnTouchOutside(false);

        if (success) {
            if (Preferences.get(mContext).isFirstRun()) {
                dialog.show();
            } else {
                doIfNewVersion();
            }
        } else {
            dialog.onPositive((dial, which) -> ((AppCompatActivity) mContext).finish()).show();
        }
    }
}
