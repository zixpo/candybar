package candybar.lib.helpers;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.HashMap;

import candybar.lib.R;
import candybar.lib.applications.CandyBarApplication;

/*
 * CandyBar - Material Dashboard
 *
 * Copyright (c) 2014-2016 Dani Mahardhika
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class LauncherHelper {

    private static final String thirdPartyHelperURL = "https://play.google.com/store/apps/details?id=rk.android.app.shortcutmaker";

    public enum Launcher {
        UNKNOWN,

        ACTION(
                "Action",
                R.drawable.ic_launcher_action,
                new String[]{"com.actionlauncher.playstore", "com.chrislacy.actionlauncher.pro"},
                true),
        ADW(
                "ADW",
                R.drawable.ic_launcher_adw,
                new String[]{"org.adw.launcher", "org.adwfreak.launcher"},
                true),
        APEX(
                "Apex",
                R.drawable.ic_launcher_apex,
                new String[]{"com.anddoes.launcher", "com.anddoes.launcher.pro"},
                true),
        CMTHEME(
                "CM Theme",
                R.drawable.ic_launcher_cm,
                new String[]{"org.cyanogenmod.theme.chooser"},
                true),
        GO(
                "GO EX",
                R.drawable.ic_launcher_go,
                new String[]{"com.gau.go.launcherex"},
                true),
        HOLO(
                "Holo",
                R.drawable.ic_launcher_holo,
                new String[]{"com.mobint.hololauncher"},
                false),
        HOLOHD(
                "Holo HD",
                R.drawable.ic_launcher_holohd,
                new String[]{"com.mobint.hololauncher.hd"},
                false),
        LAWNCHAIR(
                "Lawnchair",
                R.drawable.ic_launcher_lawnchair,
                new String[]{"ch.deletescape.lawnchair.plah", "ch.deletescape.lawnchair.ci", "app.lawnchair"},
                // Lawnchair 12 (app.lawnchair) doesn't support direct apply
                (pkg) -> !pkg.startsWith("app")),
        LGHOME(
                "LG Home",
                R.drawable.ic_launcher_lg,
                new String[]{"com.lge.launcher2", "com.lge.launcher3"},
                false),
        LUCID(
                "Lucid",
                R.drawable.ic_launcher_lucid,
                new String[]{"com.powerpoint45.launcher"},
                true),
        NOUGAT(
                "Nougat",
                R.drawable.ic_launcher_nougat,
                new String[]{"me.craftsapp.nlauncher"},
                true),
        NOVA(
                "Nova",
                R.drawable.ic_launcher_nova,
                new String[]{"com.teslacoilsw.launcher", "com.teslacoilsw.launcher.prime"},
                true),
        PIXEL(
                "Pixel",
                R.drawable.ic_launcher_pixel,
                new String[]{"com.google.android.apps.nexuslauncher"},
                false),
        SMART(
                "Smart",
                R.drawable.ic_launcher_smart,
                new String[]{"ginlemon.flowerfree", "ginlemon.flowerpro", "ginlemon.flowerpro.special"},
                true),
        SOLO(
                "Solo",
                R.drawable.ic_launcher_solo,
                new String[]{"home.solo.launcher.free"},
                true),
        POCO(
                "POCO",
                R.drawable.ic_launcher_poco,
                new String[]{"com.mi.android.globallauncher"},
                false),
        POSIDON(
                "Posidon",
                R.drawable.ic_launcher_posidon,
                new String[]{"posidon.launcher"},
                true),
        MICROSOFT(
                "Microsoft",
                R.drawable.ic_launcher_microsoft,
                new String[]{"com.microsoft.launcher"},
                false),
        BLACKBERRY(
                "BlackBerry",
                R.drawable.ic_launcher_blackberry,
                new String[]{"com.blackberry.blackberrylauncher"},
                false),
        FLICK(
                "Flick",
                R.drawable.ic_launcher_flick,
                new String[]{"com.universallauncher.universallauncher"},
                true),
        SQUARE(
                "Square",
                R.drawable.ic_launcher_square,
                new String[]{"com.ss.squarehome2"},
                true),
        NIAGARA(
                "Niagara",
                R.drawable.ic_launcher_niagara,
                new String[]{"bitpit.launcher"},
                true),
        HYPERION(
                "Hyperion",
                R.drawable.ic_launcher_hyperion,
                new String[]{"projekt.launcher"},
                false),
        NEO(
                "Neo",
                R.drawable.ic_launcher_neo,
                new String[]{"com.saggitt.omega"},
                true),
        KISS(
                "KISS",
                R.drawable.ic_launcher_kiss,
                new String[]{"fr.neamar.kiss"},
                true),
        ONEUI(
                "One UI",
                R.drawable.ic_launcher_one_ui,
                new String[]{"com.sec.android.app.launcher"},
                false),
        ZENUI(
                "ZenUI",
                R.drawable.ic_launcher_zenui,
                new String[]{"com.asus.launcher"},
                true);

        private interface DirectApplyFunc {
            boolean check(String packageName);
        }

        public final String name;
        public final @DrawableRes
        int icon;
        public final String[] packages;
        private final boolean directApply;
        private DirectApplyFunc directApplyFunc = null;

        Launcher() {
            this.name = null;
            this.icon = 0;
            this.packages = null;
            this.directApply = false;
        }

        Launcher(String name, @DrawableRes int icon, String[] packages, boolean directApply) {
            this.name = name;
            this.icon = icon;
            this.packages = packages;
            this.directApply = directApply;
        }

        Launcher(String name, @DrawableRes int icon, String[] packages, DirectApplyFunc directApplyFunc) {
            this.name = name;
            this.icon = icon;
            this.packages = packages;
            this.directApply = true;
            this.directApplyFunc = directApplyFunc;
        }

        public boolean supportsDirectApply(String launcherPackageName) {
            if (directApplyFunc != null) {
                return directApplyFunc.check(launcherPackageName);
            }
            return directApply;
        }
    }

    private static Launcher getLauncher(String packageName) {
        if (packageName == null) return Launcher.UNKNOWN;

        for (Launcher launcher : Launcher.values()) {
            if (launcher.packages == null) continue;
            for (String launcherPackageName : launcher.packages) {
                if (launcherPackageName.contentEquals(packageName)) {
                    return launcher;
                }
            }
        }

        return Launcher.UNKNOWN;
    }

    public static void apply(@NonNull Context context, String packageName, String launcherName) {
        CandyBarApplication.getConfiguration().getAnalyticsHandler().logEvent(
                "click",
                new HashMap<String, Object>() {{
                    put("section", "apply");
                    put("action", "open_dialog");
                    put("launcher", packageName);
                }}
        );
        applyLauncher(context, packageName, launcherName, getLauncher(packageName));
    }

    private static void applyLauncher(@NonNull Context context, String launcherPackage, String launcherName, Launcher launcher) {
        switch (launcher) {
            case ACTION:
                try {
                    final Intent action = context.getPackageManager().getLaunchIntentForPackage(
                            launcherPackage);
                    action.putExtra("apply_icon_pack", context.getPackageName());
                    action.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(action);
                    ((AppCompatActivity) context).finish();
                    CandyBarApplication.getConfiguration().getAnalyticsHandler().logEvent(
                            "click",
                            new HashMap<String, Object>() {{
                                put("section", "apply");
                                put("action", "confirm");
                                put("launcher", launcherPackage);
                            }}
                    );
                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, launcherPackage, launcherName);
                }
                break;
            case ADW:
                try {
                    final Intent adw = new Intent("org.adw.launcher.SET_THEME");
                    adw.putExtra("org.adw.launcher.theme.NAME", context.getPackageName());
                    adw.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(adw);
                    ((AppCompatActivity) context).finish();
                    CandyBarApplication.getConfiguration().getAnalyticsHandler().logEvent(
                            "click",
                            new HashMap<String, Object>() {{
                                put("section", "apply");
                                put("action", "confirm");
                                put("launcher", launcherPackage);
                            }}
                    );
                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, launcherPackage, launcherName);
                }
                break;
            case APEX:
                try {
                    final Intent apex = new Intent("com.anddoes.launcher.SET_THEME");
                    apex.putExtra("com.anddoes.launcher.THEME_PACKAGE_NAME", context.getPackageName());
                    apex.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(apex);
                    ((AppCompatActivity) context).finish();
                    CandyBarApplication.getConfiguration().getAnalyticsHandler().logEvent(
                            "click",
                            new HashMap<String, Object>() {{
                                put("section", "apply");
                                put("action", "confirm");
                                put("launcher", launcherPackage);
                            }}
                    );
                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, launcherPackage, launcherName);
                }
                break;
            case BLACKBERRY:
                applyManual(context, launcherPackage, launcherName, "com.blackberry.blackberrylauncher.MainActivity");
                break;
            case CMTHEME:
                try {
                    final Intent cmtheme = new Intent("android.intent.action.MAIN");
                    cmtheme.setComponent(new ComponentName(launcherPackage,
                            "org.cyanogenmod.theme.chooser.ChooserActivity"));
                    cmtheme.putExtra("pkgName", context.getPackageName());
                    context.startActivity(cmtheme);
                    CandyBarApplication.getConfiguration().getAnalyticsHandler().logEvent(
                            "click",
                            new HashMap<String, Object>() {{
                                put("section", "apply");
                                put("action", "confirm");
                                put("launcher", launcherPackage);
                            }}
                    );
                } catch (ActivityNotFoundException | NullPointerException e) {
                    Toast.makeText(context, R.string.apply_cmtheme_not_available,
                            Toast.LENGTH_LONG).show();
                } catch (SecurityException | IllegalArgumentException e) {
                    Toast.makeText(context, R.string.apply_cmtheme_failed,
                            Toast.LENGTH_LONG).show();
                }
                break;
            case FLICK:
                try {
                    final Intent flick = context.getPackageManager().getLaunchIntentForPackage("com.universallauncher.universallauncher");
                    final Intent flickAction = new Intent("com.universallauncher.universallauncher.FLICK_ICON_PACK_APPLIER");
                    flickAction.putExtra("com.universallauncher.universallauncher.ICON_THEME_PACKAGE", context.getPackageName());
                    flickAction.setComponent(new ComponentName("com.universallauncher.universallauncher", "com.android.launcher3.icon.ApplyIconPack"));
                    flick.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.sendBroadcast(flickAction);
                    context.startActivity(flick);
                    ((AppCompatActivity) context).finish();
                    CandyBarApplication.getConfiguration().getAnalyticsHandler().logEvent(
                            "click",
                            new HashMap<String, Object>() {{
                                put("section", "apply");
                                put("action", "confirm");
                                put("launcher", launcherPackage);
                            }}
                    );
                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, launcherPackage, launcherName);
                }
                break;
            case GO:
                try {
                    final Intent goex = context.getPackageManager().getLaunchIntentForPackage(
                            "com.gau.go.launcherex");
                    final Intent go = new Intent("com.gau.go.launcherex.MyThemes.mythemeaction");
                    go.putExtra("type", 1);
                    go.putExtra("pkgname", context.getPackageName());
                    goex.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.sendBroadcast(go);
                    context.startActivity(goex);
                    ((AppCompatActivity) context).finish();
                    CandyBarApplication.getConfiguration().getAnalyticsHandler().logEvent(
                            "click",
                            new HashMap<String, Object>() {{
                                put("section", "apply");
                                put("action", "confirm");
                                put("launcher", launcherPackage);
                            }}
                    );
                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, launcherPackage, launcherName);
                }
                break;
            case HOLO:
            case HOLOHD:
                applyManual(context, launcherPackage, launcherName, "com.mobint.hololauncher.SettingsActivity");
                break;
            case HYPERION:
                applyManual(context, launcherPackage, launcherName, "projekt.launcher.activities.SettingsActivity");
                break;
            case KISS:
                try {
                    Intent kiss = new Intent(Intent.ACTION_MAIN);
                    kiss.setComponent(new ComponentName("fr.neamar.kiss", "fr.neamar.kiss.SettingsActivity"));
                    //kiss.putExtra("theme.ThemeManager", context.getPackageName());
                    kiss.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(kiss);
                    ((AppCompatActivity) context).finish();
                    CandyBarApplication.getConfiguration().getAnalyticsHandler().logEvent(
                            "click",
                            new HashMap<String, Object>() {{
                                put("section", "apply");
                                put("action", "confirm");
                                put("launcher", launcherPackage);
                            }}
                    );
                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, launcherPackage, launcherName);
                }
                break;
            case LAWNCHAIR:
                if (launcherPackage.startsWith("app")) {
                    // Lawnchair 12 (app.lawnchair) does not support direct apply yet
                    applyManual(context, launcherPackage, launcherName, "app.lawnchair.ui.preferences.PreferenceActivity");
                    break;
                }

                try {
                    final Intent lawnchair = new Intent("ch.deletescape.lawnchair.APPLY_ICONS", null);
                    lawnchair.putExtra("packageName", context.getPackageName());
                    context.startActivity(lawnchair);
                    ((AppCompatActivity) context).finish();
                    CandyBarApplication.getConfiguration().getAnalyticsHandler().logEvent(
                            "click",
                            new HashMap<String, Object>() {{
                                put("section", "apply");
                                put("action", "confirm");
                                put("launcher", launcherPackage);
                            }}
                    );
                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, launcherPackage, launcherName);
                }
                break;
            case LGHOME:
                launcherIncompatible(context, launcherName);
                break;
            case LUCID:
                try {
                    final Intent lucid = new Intent("com.powerpoint45.action.APPLY_THEME", null);
                    lucid.putExtra("icontheme", context.getPackageName());
                    context.startActivity(lucid);
                    ((AppCompatActivity) context).finish();
                    CandyBarApplication.getConfiguration().getAnalyticsHandler().logEvent(
                            "click",
                            new HashMap<String, Object>() {{
                                put("section", "apply");
                                put("action", "confirm");
                                put("launcher", launcherPackage);
                            }}
                    );
                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, launcherPackage, launcherName);
                }
                break;
            case MICROSOFT:
                applyManual(context, launcherPackage, launcherName, null);
                break;
            case NIAGARA:
                try {
                    final Intent niagara = new Intent("bitpit.launcher.APPLY_ICONS");
                    niagara.putExtra("packageName", context.getPackageName());
                    context.startActivity(niagara);
                    CandyBarApplication.getConfiguration().getAnalyticsHandler().logEvent(
                            "click",
                            new HashMap<String, Object>() {{
                                put("section", "apply");
                                put("action", "confirm");
                                put("launcher", launcherPackage);
                            }}
                    );
                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, launcherPackage, launcherName);
                }
                break;
            case NOVA:
                try {
                    final Intent nova = new Intent("com.teslacoilsw.launcher.APPLY_ICON_THEME");
                    nova.setPackage("com.teslacoilsw.launcher");
                    nova.putExtra("com.teslacoilsw.launcher.extra.ICON_THEME_TYPE", "GO");
                    nova.putExtra("com.teslacoilsw.launcher.extra.ICON_THEME_PACKAGE", context.getPackageName());
                    nova.putExtra("com.teslacoilsw.launcher.extra.ICON_THEME_RESHAPE", context.getResources()
                            .getString(R.string.nova_reshape_legacy_icons));
                    nova.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(nova);
                    ((AppCompatActivity) context).finish();
                    CandyBarApplication.getConfiguration().getAnalyticsHandler().logEvent(
                            "click",
                            new HashMap<String, Object>() {{
                                put("section", "apply");
                                put("action", "confirm");
                                put("launcher", launcherPackage);
                            }}
                    );
                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, launcherPackage, launcherName);
                }
                break;
            case PIXEL:
                launcherIncompatible(context, launcherName);
                break;
            case POCO:
                applyManual(context, launcherPackage, launcherName, "com.miui.home.settings.HomeSettingsActivity");
                break;
            case POSIDON:
                try {
                    Intent posidon = new Intent(Intent.ACTION_MAIN);
                    posidon.setComponent(new ComponentName("posidon.launcher", "posidon.launcher.external.ApplyIcons"));
                    posidon.putExtra("iconpack", context.getPackageName());
                    posidon.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(posidon);
                    ((AppCompatActivity) context).finish();
                    CandyBarApplication.getConfiguration().getAnalyticsHandler().logEvent(
                            "click",
                            new HashMap<String, Object>() {{
                                put("section", "apply");
                                put("action", "confirm");
                                put("launcher", launcherPackage);
                            }}
                    );
                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, launcherPackage, launcherName);
                }
                break;
            case ONEUI:
                applyOneUI(context, launcherName);
                break;
            case SMART:
                try {
                    final Intent smart = new Intent("ginlemon.smartlauncher.setGSLTHEME");
                    smart.putExtra("package", context.getPackageName());
                    smart.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(smart);
                    ((AppCompatActivity) context).finish();
                    CandyBarApplication.getConfiguration().getAnalyticsHandler().logEvent(
                            "click",
                            new HashMap<String, Object>() {{
                                put("section", "apply");
                                put("action", "confirm");
                                put("launcher", launcherPackage);
                            }}
                    );
                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, launcherPackage, launcherName);
                }
                break;
            case SOLO:
                try {
                    final Intent solo = context.getPackageManager().getLaunchIntentForPackage(
                            "home.solo.launcher.free");
                    final Intent soloAction = new Intent("home.solo.launcher.free.APPLY_THEME");
                    soloAction.putExtra("EXTRA_THEMENAME", context.getResources().getString(
                            R.string.app_name));
                    soloAction.putExtra("EXTRA_PACKAGENAME", context.getPackageName());
                    solo.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.sendBroadcast(soloAction);
                    context.startActivity(solo);
                    ((AppCompatActivity) context).finish();
                    CandyBarApplication.getConfiguration().getAnalyticsHandler().logEvent(
                            "click",
                            new HashMap<String, Object>() {{
                                put("section", "apply");
                                put("action", "confirm");
                                put("launcher", launcherPackage);
                            }}
                    );
                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, launcherPackage, launcherName);
                }
                break;
            case SQUARE:
                try {
                    final Intent square = new Intent("com.ss.squarehome2.ACTION_APPLY_ICONPACK");
                    square.setComponent(ComponentName.unflattenFromString("com.ss.squarehome2/.ApplyThemeActivity"));
                    square.putExtra("com.ss.squarehome2.EXTRA_ICONPACK", context.getPackageName());
                    context.startActivity(square);
                    CandyBarApplication.getConfiguration().getAnalyticsHandler().logEvent(
                            "click",
                            new HashMap<String, Object>() {{
                                put("section", "apply");
                                put("action", "confirm");
                                put("launcher", launcherPackage);
                            }}
                    );
                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, launcherPackage, launcherName);
                }
                break;
            case NEO:
                try {
                    Intent neo = new Intent(Intent.ACTION_MAIN);
                    neo.setComponent(new ComponentName("com.saggitt.omega", "com.saggitt.omega.preferences.views.PreferencesActivity"));
                    neo.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(neo);
                    ((AppCompatActivity) context).finish();
                    CandyBarApplication.getConfiguration().getAnalyticsHandler().logEvent(
                            "click",
                            new HashMap<String, Object>() {{
                                put("section", "apply");
                                put("action", "confirm");
                                put("launcher", launcherPackage);
                            }}
                    );
                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, launcherPackage, launcherName);
                }
                break;
            case NOUGAT:
                try {
                    /*
                     * Just want to let anyone who is going to copy
                     * It's not easy searching for this
                     * I will be grateful if you take this with a proper credit
                     * Thank you
                     */
                    final Intent nougat = new Intent("me.craftsapp.nlauncher");
                    nougat.setAction("me.craftsapp.nlauncher.SET_THEME");
                    nougat.putExtra("me.craftsapp.nlauncher.theme.NAME", context.getPackageName());
                    nougat.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(nougat);
                    ((AppCompatActivity) context).finish();
                    CandyBarApplication.getConfiguration().getAnalyticsHandler().logEvent(
                            "click",
                            new HashMap<String, Object>() {{
                                put("section", "apply");
                                put("action", "confirm");
                                put("launcher", launcherPackage);
                            }}
                    );
                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, launcherPackage, launcherName);
                }
                break;
            case ZENUI:
                try {
                    final Intent asus = new Intent("com.asus.launcher");
                    asus.setAction("com.asus.launcher.intent.action.APPLY_ICONPACK");
                    asus.addCategory(Intent.CATEGORY_DEFAULT);
                    asus.putExtra("com.asus.launcher.iconpack.PACKAGE_NAME", context.getPackageName());
                    asus.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(asus);
                    ((AppCompatActivity) context).finish();
                    CandyBarApplication.getConfiguration().getAnalyticsHandler().logEvent(
                            "click",
                            new HashMap<String, Object>() {{
                                put("section", "apply");
                                put("action", "confirm");
                                put("launcher", launcherPackage);
                            }}
                    );
                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, launcherPackage, launcherName);
                }
                break;
        }
    }

    @SuppressLint("StringFormatInvalid")
    private static void applyManual(Context context, String launcherPackage, String launcherName, String activity) {
        if (isInstalled(context, launcherPackage)) {
            new MaterialDialog.Builder(context)
                    .typeface(TypefaceHelper.getMedium(context), TypefaceHelper.getRegular(context))
                    .title(launcherName)
                    .content(context.getResources().getString(R.string.apply_manual,
                            launcherName,
                            context.getResources().getString(R.string.app_name)))
                    .positiveText(android.R.string.ok)
                    .onPositive((dialog, which) -> {
                        CandyBarApplication.getConfiguration().getAnalyticsHandler().logEvent(
                                "click",
                                new HashMap<String, Object>() {{
                                    put("section", "apply");
                                    put("action", "manual_open_confirm");
                                    put("launcher", launcherPackage);
                                }}
                        );
                        if (activity == null) return;
                        try {
                            final Intent intent = new Intent(Intent.ACTION_MAIN);
                            intent.setComponent(new ComponentName(launcherPackage,
                                    activity));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                            ((AppCompatActivity) context).finish();
                        } catch (ActivityNotFoundException | NullPointerException e) {
                            openGooglePlay(context, launcherPackage, launcherName);
                        } catch (SecurityException | IllegalArgumentException e) {
                            Toast.makeText(context, context.getResources().getString(
                                            R.string.apply_launch_failed, launcherName),
                                    Toast.LENGTH_LONG).show();
                        }
                    })
                    .negativeText(android.R.string.cancel)
                    .onNegative(((dialog, which) -> {
                        CandyBarApplication.getConfiguration().getAnalyticsHandler().logEvent(
                                "click",
                                new HashMap<String, Object>() {{
                                    put("section", "apply");
                                    put("action", "manual_open_cancel");
                                    put("launcher", launcherPackage);
                                }}
                        );
                    }))
                    .show();
        } else {
            openGooglePlay(context, launcherPackage, launcherName);
        }
    }

    private static boolean isInstalled(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        boolean found = true;
        try {
            packageManager.getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            found = false;
        }
        return found;
    }

    /**
     * Samsung's OneUI launcher version is tightly coupled to the Android OS version.
     * Starting OneUI 3.1.1, icon theming is supported on Android 11, foldable devices only[1]
     * Starting OneUI 4.0, icon theming is supported on all Android 12 devices[2]
     * <p>
     * Sadly it's impossible to detect the OneUI version programmatically[3].
     * <p>
     * Technically it's incorrect to report all Android 11 devices as incompatible but the
     * only other option is to try and list out all device models individually. The below
     * code will display a "please update to Android 12" message to anyone running Android
     * 11 or lower, and display step-by-step theming instructions to everyone else.
     * <p>
     * See:
     * [1] <a href="https://www.androidpolice.com/how-to-use-custom-icon-packs-on-samsung-one-ui-4/"/>
     * [2] <a href="https://en.wikipedia.org/wiki/One_UI#One_UI_4_2"/>
     * [3] <a href="https://github.com/zixpo/candybar/pull/122#issuecomment-1510379686"/>
     */
    private static void applyOneUI(Context context, String launcherName) {
        String incompatibleText = context.getResources().getString(
                R.string.apply_manual_samsung_oneui_too_old,
                launcherName
        );
        String compatibleText =
                "\t• " + context.getResources().getString(
                        R.string.apply_manual_samsung_oneui_step_1,
                        "Samsung Galaxy Store"
                ) + "\n\t• " +
                        context.getResources().getString(
                                R.string.apply_manual_samsung_oneui_step_2,
                                "Theme Park"
                        ) + "\n\t• " +
                        context.getResources().getString(R.string.apply_manual_samsung_oneui_step_3) + "\n\t• " +
                        context.getResources().getString(R.string.apply_manual_samsung_oneui_step_4) + "\n\t• " +
                        context.getResources().getString(R.string.apply_manual_samsung_oneui_step_5) + "\n\t• " +
                        context.getResources().getString(
                                R.string.apply_manual_samsung_oneui_step_6,
                                context.getResources().getString(R.string.app_name)
                        ) + "\n\n" +
                        context.getResources().getString(
                                R.string.apply_manual_samsung_oneui_step_7,
                                context.getResources().getString(R.string.app_name)
                        );
        new MaterialDialog.Builder(context)
                .typeface(TypefaceHelper.getMedium(context), TypefaceHelper.getRegular(context))
                .title(launcherName)
                .content(
                        context.getResources().getString(
                                R.string.apply_manual_samsung_oneui,
                                launcherName,
                                launcherName + " 4.0"
                        )
                                + "\n\n"
                                + (Build.VERSION.SDK_INT > Build.VERSION_CODES.R ? compatibleText : incompatibleText)
                )
                .positiveText(android.R.string.yes)
                .onPositive((dialog, which) -> {
                    CandyBarApplication.getConfiguration().getAnalyticsHandler().logEvent(
                            "click",
                            new HashMap<String, Object>() {{
                                put("section", "apply");
                                put("action", "manual_open_confirm");
                                put("launcher", launcherName);
                            }}
                    );
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
                        String packageName = "com.samsung.android.themedesigner";
                        try {
                            String uri = "samsungapps://ProductDetail/" + packageName;
                            Intent store = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                            context.startActivity(store);
                        } catch (ActivityNotFoundException e) {
                            // The device can't handle Samsung Deep Links
                            // Let us point to the app in a browser instead
                            try {
                                Uri uri = Uri.parse("https://galaxystore.samsung.com/detail/" + packageName);
                                Intent store = new Intent(Intent.ACTION_VIEW, uri);
                                context.startActivity(store);
                            } catch (ActivityNotFoundException ignored) {
                                Toast.makeText(context, context.getResources().getString(
                                        R.string.no_browser), Toast.LENGTH_LONG).show();
                            }
                        }
                    } else {
                        try {
                            // Open software update activity if we can.
                            // Verified to be working on:
                            //   Samsung Galaxy S10 DUOS running Android 10
                            //   Samsung Galaxy S23 Ultra running Android 13
                            Intent intent = new Intent("android.settings.SYSTEM_UPDATE_SETTINGS");
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        } catch (ActivityNotFoundException ignored) {
                        }
                    }
                })
                .negativeText(android.R.string.cancel)
                .onNegative(((dialog, which) -> {
                    CandyBarApplication.getConfiguration().getAnalyticsHandler().logEvent(
                            "click",
                            new HashMap<String, Object>() {{
                                put("section", "apply");
                                put("action", "manual_open_cancel");
                                put("launcher", launcherName);
                            }}
                    );
                }))
                .show();
    }

    private static void launcherIncompatible(Context context, String launcherName) {
        new MaterialDialog.Builder(context)
                .typeface(TypefaceHelper.getMedium(context), TypefaceHelper.getRegular(context))
                .title(launcherName)
                .content(R.string.apply_launcher_incompatible, launcherName, launcherName)
                .positiveText(android.R.string.yes)
                .onPositive((dialog, which) -> {
                    CandyBarApplication.getConfiguration().getAnalyticsHandler().logEvent(
                            "click",
                            new HashMap<String, Object>() {{
                                put("section", "apply");
                                put("action", "incompatible_third_party_open");
                                put("launcher", launcherName);
                            }}
                    );
                    try {
                        Intent store = new Intent(Intent.ACTION_VIEW, Uri.parse(thirdPartyHelperURL));
                        context.startActivity(store);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(context, context.getResources().getString(
                                R.string.no_browser), Toast.LENGTH_LONG).show();
                    }
                })
                .negativeText(android.R.string.cancel)
                .onNegative(((dialog, which) -> {
                    CandyBarApplication.getConfiguration().getAnalyticsHandler().logEvent(
                            "click",
                            new HashMap<String, Object>() {{
                                put("section", "apply");
                                put("action", "incompatible_third_party_cancel");
                                put("launcher", launcherName);
                            }}
                    );
                }))
                .show();
    }

    private static void notInstalledError(Context context, String launcherName) {
        new MaterialDialog.Builder(context)
                .typeface(TypefaceHelper.getMedium(context), TypefaceHelper.getRegular(context))
                .title(launcherName)
                .content(R.string.apply_launcher_not_installable, launcherName)
                .positiveText(context.getResources().getString(R.string.close))
                .show();
    }

    private static void openGooglePlay(Context context, String packageName, String launcherName) {
        new MaterialDialog.Builder(context)
                .typeface(TypefaceHelper.getMedium(context), TypefaceHelper.getRegular(context))
                .title(launcherName)
                .content(R.string.apply_launcher_not_installed, launcherName)
                .positiveText(context.getResources().getString(R.string.install))
                .onPositive((dialog, which) -> {
                    CandyBarApplication.getConfiguration().getAnalyticsHandler().logEvent(
                            "click",
                            new HashMap<String, Object>() {{
                                put("section", "apply");
                                put("action", "not_installed_google_play_open");
                                put("launcher", packageName);
                            }}
                    );
                    try {
                        Intent store = new Intent(Intent.ACTION_VIEW, Uri.parse(
                                "https://play.google.com/store/apps/details?id=" + packageName));
                        context.startActivity(store);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(context, context.getResources().getString(
                                R.string.no_browser), Toast.LENGTH_LONG).show();
                    }
                })
                .negativeText(android.R.string.cancel)
                .onNegative(((dialog, which) -> {
                    CandyBarApplication.getConfiguration().getAnalyticsHandler().logEvent(
                            "click",
                            new HashMap<String, Object>() {{
                                put("section", "apply");
                                put("action", "not_installed_google_play_cancel");
                                put("launcher", packageName);
                            }}
                    );
                }))
                .show();
    }

    public static boolean quickApply(Context context) {
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        String packageName = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY).activityInfo.packageName;
        Launcher launcher = getLauncher(packageName);
        if (launcher.supportsDirectApply(packageName)) {
            applyLauncher(context, packageName, launcher.name, launcher);
            return true;
        }
        return false;
    }
}
