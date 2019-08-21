package candybar.lib.helpers;

import android.content.Context;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import candybar.lib.items.IconShape;
import candybar.lib.preferences.Preferences;
import sarsamurmu.adaptiveicon.AdaptiveIcon;

public class IconShapeHelper {

    @Nullable
    public static String getCurrentShape(Context context) {
        int currentShape = Preferences.get(context).getIconShape();
        switch (currentShape) {
            case AdaptiveIcon.PATH_CIRCLE:
                return "Circle";
            case AdaptiveIcon.PATH_SQUARE:
                return "Square";
            case AdaptiveIcon.PATH_ROUNDED_SQUARE:
                return "Rounded Square";
            case AdaptiveIcon.PATH_SQUIRCLE:
                return "Squircle";
            case AdaptiveIcon.PATH_TEARDROP:
                return "Teardrop";
        }
        return null;
    }

    public static List<IconShape> getShapes() {
        List<IconShape> shapes = new ArrayList<>();
        shapes.add(new IconShape("Circle", AdaptiveIcon.PATH_CIRCLE));
        shapes.add(new IconShape("Square", AdaptiveIcon.PATH_SQUARE));
        shapes.add(new IconShape("Rounded Square", AdaptiveIcon.PATH_ROUNDED_SQUARE));
        shapes.add(new IconShape("Squircle", AdaptiveIcon.PATH_SQUIRCLE));
        shapes.add(new IconShape("Teardrop", AdaptiveIcon.PATH_TEARDROP));
        return shapes;
    }
}
