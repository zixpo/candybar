package candybar.lib.helpers.sharpie;

import static candybar.lib.helpers.sharpie.R1.CLAZZ;
import static candybar.lib.helpers.sharpie.R1.WrappedMethod;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.danimahardhika.android.helpers.core.utils.LogUtil;

import candybar.lib.helpers.RequestHelper;
import io.michaelrocks.paranoid.Obfuscate;

@Obfuscate
public class R4 {
    public static boolean isPS(Context context) {
        WrappedMethod method;
        try {
            final String psPackageName = RequestHelper.reverseString(TextUtils.join("",
                    new String[]{"g", "n", "i", "d", "n", "e", "v", ".", "d", "i", "o", "r", "d", "n", "a", ".", "m", "o", "c"}));
            method = CLAZZ("Context").getMethod("getPackageManager");
            final Object pm = method.invoke(context);
            method = CLAZZ("Context").getMethod("getPackageName");
            final Object packageName = method.invoke(context);
            method = CLAZZ("PackageManager").getMethod("getInstallerPackageName", CLAZZ("String"));
            final Object installerPackage = method.invoke(pm, packageName);
            method = CLAZZ("String").getMethod("contentEquals", CLAZZ("CharSequence"));
            return installerPackage != null && (boolean) method.invoke(installerPackage, psPackageName);
        } catch (Exception e) {
            LogUtil.e(Log.getStackTraceString(e));
            return false;
        }
    }
}
