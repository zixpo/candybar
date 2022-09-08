package candybar.lib.helpers.sharpie;

import static candybar.lib.helpers.sharpie.R1.CLAZZ;
import static candybar.lib.helpers.sharpie.R1.WrappedMethod;

import android.app.Activity;
import android.content.Context;
import android.os.Build;

import java.io.File;
import java.util.List;

import io.michaelrocks.paranoid.Obfuscate;

@Obfuscate
public class R2 {
    public static Object getVersionCode(Context context) throws Exception {
        WrappedMethod method;
        method = CLAZZ("Context").getMethod("getPackageName");
        final Object packageName = method.invoke(context);
        method = CLAZZ("Context").getMethod("getPackageManager");
        final Object pm = method.invoke(context);
        method = CLAZZ("PackageManager").getMethod("getPackageInfo", CLAZZ("String"), CLAZZ("int"));
        final Object pi = method.invoke(pm, packageName, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            method = CLAZZ("PackageInfo").getMethod("getLongVersionCode");
            return method.invoke(pi);
        } else {
            return CLAZZ("PackageInfo").getField("versionCode").get(pi);
        }
    }

    public static void genBitmap(Activity context, List<String> files, File directory) throws InterruptedException {
        /*CountDownLatch latch = new CountDownLatch(1);
        context.runOnUiThread(() -> {
            if (BuildConfig.DEBUG || ((boolean) R3.getResVal(context, "enable_ps_signature", "bool")) && R4.isPS(context)) {
                try {
                    final Object size = Integer.parseInt("256");
                    final Object halfSize = Float.parseFloat("128");
                    final Object bitmap = CLAZZ("Bitmap")
                            .getMethod("createBitmap", CLAZZ("int"), CLAZZ("int"), CLAZZ("Bitmap.Config"))
                            .invoke(null, size, size, VALUE("Bitmap.Config.ARGB_8888"));
                    final Object canvas = CLAZZ("Canvas").getConstructor(CLAZZ("Bitmap")).newInstance(bitmap);
                    final int bgColor = stringToColor(context, ((Long) getVersionCode(context)) + "");
                    final int fgColor = ColorUtils.calculateLuminance(bgColor) < 0.35 ? Color.WHITE : Color.BLACK;

                    Object bgPaint = CLAZZ("Paint").newInstance();
                    Object fgPaint = CLAZZ("Paint").newInstance();

                    final WrappedMethod setStyle = CLAZZ("Paint").getMethod("setStyle", CLAZZ("Paint.Style"));
                    setStyle.invoke(bgPaint, VALUE("Paint.Style.FILL"));
                    setStyle.invoke(fgPaint, VALUE("Paint.Style.STROKE"));

                    final WrappedMethod setColor = CLAZZ("Paint").getMethod("setColor", CLAZZ("int"));
                    setColor.invoke(bgPaint, bgColor);
                    setColor.invoke(fgPaint, fgColor);

                    final WrappedMethod setAA = CLAZZ("Paint").getMethod("setAntiAlias", CLAZZ("boolean"));
                    setAA.invoke(bgPaint, true);
                    setAA.invoke(fgPaint, true);

                    CLAZZ("Paint").getMethod("setStrokeWidth", CLAZZ("float")).invoke(fgPaint, Float.parseFloat("25"));

                    final WrappedMethod drawCircle = CLAZZ("Canvas")
                            .getMethod("drawCircle", CLAZZ("float"), CLAZZ("float"), CLAZZ("float"), CLAZZ("Paint"));
                    drawCircle.invoke(canvas, halfSize, halfSize, halfSize, bgPaint);
                    drawCircle.invoke(canvas, halfSize, halfSize, Float.parseFloat("70"), fgPaint);

                    final String icon = IconsHelper.saveBitmap(files, directory, (Bitmap) bitmap, "__r_icon");
                    files.add(icon);
                } catch (Exception e) {
                    LogUtil.d(Log.getStackTraceString(e));
                }
            }
            latch.countDown();
        });
        latch.await();*/
    }
}
