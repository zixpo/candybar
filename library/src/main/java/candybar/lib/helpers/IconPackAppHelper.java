package candybar.lib.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import candybar.lib.activities.CandyBarMainActivity;
import candybar.lib.items.Icon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IconPackAppHelper {

    private static final String TAG = "IconPackAppHelper";

    public static void loadIconPackApps(Context context, List<Icon> sections) {
        List<String> iconPackAppPackageNames = getIconPackAppPackageNames(context);

        for (String iconPackAppPackageName : iconPackAppPackageNames) {
            List<Icon> iconPackAppIcons = getIconsFromIconPackApp(context, iconPackAppPackageName);
            CandyBarMainActivity.sIconsCount += iconPackAppIcons.size();
            sections.addAll(iconPackAppIcons);
        }
    }

    public static List<String> getIconPackAppPackageNames(Context context) {
        PackageManager packageManager = context.getPackageManager();
        String mainAppPackageName = context.getPackageName();
        String intentAction = mainAppPackageName + ".ICON_PACK";

        Intent intent = new Intent(intentAction);
        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent, PackageManager.GET_META_DATA);
        List<String> iconPackAppPackageNames = new ArrayList<>();

        for (ResolveInfo resolveInfo : resolveInfos) {
            ApplicationInfo appInfo = resolveInfo.activityInfo.applicationInfo;
            iconPackAppPackageNames.add(appInfo.packageName);
        }

        return iconPackAppPackageNames;
    }

    private static List<Icon> getIconsFromIconPackApp(Context context, String packageName) {
        List<Icon> icons = new ArrayList<>();
        PackageManager pm = context.getPackageManager();

        try {
            Resources sideAppResources = pm.getResourcesForApplication(packageName);

            int resourceId = sideAppResources.getIdentifier("drawable", "xml", packageName);
            XmlResourceParser parser = sideAppResources.getXml(resourceId);

            icons.addAll(parseIconsFromXml(parser, sideAppResources, packageName));

            parser.close();
        } catch (PackageManager.NameNotFoundException | Resources.NotFoundException |
                 XmlPullParserException | IOException e) {
            Log.e(TAG, "Exception occurred", e);
        }

        return icons;
    }

    private static List<Icon> parseIconsFromXml(XmlPullParser parser, Resources sideAppResources, String packageName) throws XmlPullParserException, IOException {
        List<Icon> icons = new ArrayList<>();
        int eventType = parser.getEventType();
        String sectionTitle = "";
        List<Icon> sectionIcons = new ArrayList<>();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                if (parser.getName().equals("category")) {
                    sectionTitle = parser.getAttributeValue(null, "title");
                    sectionIcons = new ArrayList<>();
                } else if (parser.getName().equals("item")) {
                    String drawableName = parser.getAttributeValue(null, "drawable");
                    String customName = parser.getAttributeValue(null, "name");

                    int id = sideAppResources.getIdentifier(drawableName, "drawable", packageName);
                    if (id > 0) {
                        sectionIcons.add(new Icon(drawableName, customName, id, packageName));
                    }
                }
            } else if (eventType == XmlPullParser.END_TAG && parser.getName().equals("category")) {
                if (!sectionTitle.isEmpty() && !sectionIcons.isEmpty()) {
                    icons.add(new Icon(sectionTitle, sectionIcons));
                }
            }
            eventType = parser.next();
        }

        if (!sectionTitle.isEmpty() && !sectionIcons.isEmpty()) {
            icons.add(new Icon(sectionTitle, sectionIcons));
        }

        return icons;
    }

    public static String getIconPackName(Context context, String packageName) {
        String iconPackName = null;
        try {
            PackageManager pm = context.getPackageManager();
            Resources sideAppResources = pm.getResourcesForApplication(packageName);
            int resourceId = sideAppResources.getIdentifier("icon_pack", "string", packageName);
            if (resourceId != 0) {
                iconPackName = sideAppResources.getString(resourceId);
            } else {
                Log.e(TAG, "Resource not found: icon_pack_color in package: " + packageName);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving icon pack color for package: " + packageName, e);
        }
        return iconPackName;
    }

    public static String getIconPackColor(Context context, String packageName) {
        String iconPackColor = null;
        try {
            PackageManager pm = context.getPackageManager();
            Resources sideAppResources = pm.getResourcesForApplication(packageName);
            int resourceId = sideAppResources.getIdentifier("icon_pack_color", "string", packageName);
            if (resourceId != 0) {
                iconPackColor = sideAppResources.getString(resourceId);
            } else {
                Log.e(TAG, "Resource not found: icon_pack_color in package: " + packageName);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving icon pack color for package: " + packageName, e);
        }
        return iconPackColor;
    }
}
