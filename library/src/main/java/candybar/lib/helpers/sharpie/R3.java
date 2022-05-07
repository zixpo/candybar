package candybar.lib.helpers.sharpie;

import static candybar.lib.helpers.sharpie.R1.CLAZZ;
import static candybar.lib.helpers.sharpie.R1.WrappedMethod;

import android.content.Context;
import android.util.Log;

import com.danimahardhika.android.helpers.core.utils.LogUtil;

import io.michaelrocks.paranoid.Obfuscate;

@Obfuscate
public class R3 {
    public static Object getResVal(Context context, String name, String type) {
        try {
            final WrappedMethod contentEquals = CLAZZ("String").getMethod("contentEquals", CLAZZ("CharSequence"));

            WrappedMethod method = CLAZZ("Context").getMethod("getPackageName");
            final Object packageName = method.invoke(context);

            method = CLAZZ("Context").getMethod("getResources");
            final Object res = method.invoke(context);

            method = CLAZZ("Resources").getMethod("getIdentifier", CLAZZ("String"), CLAZZ("String"), CLAZZ("String"));
            final int resId = (int) method.invoke(res, name, type, packageName);

            if ((boolean) contentEquals.invoke(type, "string")) {
                method = CLAZZ("Resources").getMethod("getString", CLAZZ("int"));
            } else if ((boolean) contentEquals.invoke(type, "bool")) {
                method = CLAZZ("Resources").getMethod("getBoolean", CLAZZ("int"));
            }
            return method.invoke(res, resId);
        } catch (Exception e) {
            LogUtil.e(Log.getStackTraceString(e));
            return 0;
        }
    }
}
