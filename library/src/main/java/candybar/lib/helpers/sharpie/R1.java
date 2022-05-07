package candybar.lib.helpers.sharpie;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import io.michaelrocks.paranoid.Obfuscate;

@Obfuscate
public class R1 {
    public static Map<String, Class<?>> classMap = new HashMap<>();
    public static Map<String, Object> valueMap = new HashMap<>();

    static {
        // IMPORTANT: Include all these classes in Proguard if they are not built-in
        classMap.put("String", String.class);
        classMap.put("Context", Context.class);
        classMap.put("Resources", Resources.class);
        classMap.put("PackageManager", PackageManager.class);
        classMap.put("Toast", Toast.class);
        classMap.put("AppCompatActivity", AppCompatActivity.class);
        classMap.put("int", int.class);
        classMap.put("PackageInfo", PackageInfo.class);
        classMap.put("Bitmap", Bitmap.class);
        classMap.put("Bitmap.Config", Bitmap.Config.class);
        classMap.put("Canvas", Canvas.class);
        classMap.put("float", float.class);
        classMap.put("Paint", Paint.class);
        classMap.put("Paint.Style", Paint.Style.class);
        classMap.put("boolean", boolean.class);
        classMap.put("CharSequence", CharSequence.class);

        valueMap.put("Bitmap.Config.ARGB_8888", Bitmap.Config.ARGB_8888);
        valueMap.put("Paint.Style.FILL", Paint.Style.FILL);
        valueMap.put("Paint.Style.STROKE", Paint.Style.STROKE);
    }

    public static WrappedClass CLAZZ(String name) {
        if (!classMap.containsKey(name)) throw new Error(name + ".class has not been stored");
        return new WrappedClass(classMap.get(name));
    }

    public static Object VALUE(String name) {
        if (!valueMap.containsKey(name)) throw new Error(name + " has not been stored");
        return valueMap.get(name);
    }

    private static Class<?>[] unwrapClass(WrappedClass[] wrappedClasses) {
        Class<?>[] parameterClasses = new Class<?>[wrappedClasses.length];
        for (int i = 0; i < wrappedClasses.length; i++) {
            parameterClasses[i] = wrappedClasses[i].clazz;
        }
        return parameterClasses;
    }

    public static class WrappedClass {
        private final Class<?> clazz;

        private WrappedClass(Class<?> c) {
            clazz = c;
        }

        public WrappedMethod getMethod(String name, WrappedClass... parameterTypes) throws Exception {
            return new WrappedMethod(clazz.getMethod(name, unwrapClass(parameterTypes)));
        }

        public WrappedField getField(String fieldName) throws Exception {
            return new WrappedField(clazz.getField(fieldName));
        }

        public WrappedConstructor getConstructor(WrappedClass... parameterTypes) throws Exception {
            return new WrappedConstructor(clazz.getConstructor(unwrapClass(parameterTypes)));
        }

        public Object newInstance() throws Exception {
            return clazz.newInstance();
        }
    }

    public static class WrappedConstructor {
        private final Object constructor;

        private WrappedConstructor(Object c) {
            constructor = c;
        }

        public Object newInstance(Object o) throws Exception {
            return ((Constructor<?>) constructor).newInstance(o);
        }
    }

    public static class WrappedField {
        private final Object field;

        private WrappedField(Object f) {
            field = f;
        }

        public Object get(Object o) throws Exception {
            return ((Field) field).get(o);
        }
    }

    public static class WrappedMethod {
        private final Object method;

        private WrappedMethod(Object m) {
            method = m;
        }

        public Object invoke(Object object, Object... args) throws Exception {
            return ((Method) method).invoke(object, args);
        }
    }
}
