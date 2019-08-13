package candybar.lib.helpers;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.appcompat.app.AppCompatActivity;

import com.afollestad.materialdialogs.MaterialDialog;

import candybar.lib.R;

public class PlaystoreCheckHelper {

    public Context mContext;

    public PlaystoreCheckHelper(Context context) {
        mContext = context;
    }

    public void run() {
        if (mContext.getResources().getBoolean(R.bool.playstore_check_enabled)) {
            PackageManager pm = mContext.getPackageManager();
            String installerPackage = pm.getInstallerPackageName(mContext.getPackageName());

            if (installerPackage == null || !installerPackage.contentEquals("com.android.vending")) {
                new MaterialDialog.Builder(mContext)
                        .typeface(
                                TypefaceHelper.getMedium(mContext),
                                TypefaceHelper.getRegular(mContext))
                        .title(R.string.playstore_check)
                        .content(R.string.playstore_check_failed)
                        .positiveText(R.string.close)
                        .cancelable(false)
                        .canceledOnTouchOutside(false)
                        .onPositive((dialog, which) -> ((AppCompatActivity) mContext).finish())
                        .show();
            } else {
                new MaterialDialog.Builder(mContext)
                        .typeface(
                                TypefaceHelper.getMedium(mContext),
                                TypefaceHelper.getRegular(mContext))
                        .title(R.string.playstore_check)
                        .content(R.string.playstore_check_success)
                        .positiveText(R.string.close)
                        .cancelable(false)
                        .canceledOnTouchOutside(false)
                        .show();
            }
        }
    }
}
