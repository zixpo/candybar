package candybar.lib.helpers.sharpie;

import android.content.Context;

import io.michaelrocks.paranoid.Obfuscate;

@Obfuscate
public class R5 {
    public static void thing(Context context) {
        /*try {
            final boolean isEnabled = (boolean) getResVal(context, "playstore_check_enabled", "bool");

            if (BuildConfig.DEBUG) return;

            if (isEnabled && !isPS(context)) {
                final WrappedMethod makeText = CLAZZ("Toast").getMethod("makeText", CLAZZ("Context"), CLAZZ("CharSequence"), CLAZZ("int"));
                final Object toast = makeText.invoke(null, context, getResVal(context, "playstore_check_failed", "string"), Toast.LENGTH_LONG);
                final WrappedMethod show = CLAZZ("Toast").getMethod("show");
                show.invoke(toast);
                final WrappedMethod finish = CLAZZ("AppCompatActivity").getMethod("finish");
                finish.invoke(context);
            }
        } catch (Exception e) {
            LogUtil.e(Log.getStackTraceString(e));
        }*/
    }
}
