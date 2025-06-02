package candybar.lib.helpers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.util.Pair;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import com.afollestad.materialdialogs.MaterialDialog;

import java.lang.reflect.Method;
import java.util.Arrays;
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

    private static final String NO_SETTINGS_ACTIVITY = null;
    private static final LauncherType.DirectApply DIRECT_APPLY_NOT_SUPPORTED = null;
    private static final LauncherType.ManualApply MANUAL_APPLY_NOT_SUPPORTED = null;
    private static final LauncherType.ApplyCallback DEFAULT_CALLBACK = context -> {
        if (context instanceof Activity) { ((Activity) context).finish(); }
    };

    public enum LauncherType {
        UNKNOWN,

        ACTION(
                "Action",
                R.drawable.ic_launcher_action,
                new String[]{"com.actionlauncher.playstore", "com.chrislacy.actionlauncher.pro"},
                NO_SETTINGS_ACTIVITY,
                (context, launcherPackageName) -> context.getPackageManager().getLaunchIntentForPackage(launcherPackageName)
                    .putExtra("apply_icon_pack", context.getPackageName())
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                MANUAL_APPLY_NOT_SUPPORTED
        ),
        ADW(
                "ADW",
                R.drawable.ic_launcher_adw,
                new String[]{"org.adw.launcher", "org.adwfreak.launcher"},
                NO_SETTINGS_ACTIVITY,
                (context, launcherPackageName) -> new Intent("org.adw.launcher.SET_THEME")
                        .putExtra("org.adw.launcher.theme.NAME", context.getPackageName())
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                MANUAL_APPLY_NOT_SUPPORTED
        ),
        APEX(
                "Apex",
                R.drawable.ic_launcher_apex,
                new String[]{"com.anddoes.launcher", "com.anddoes.launcher.pro"},
                NO_SETTINGS_ACTIVITY,
                (context, launcherPackageName) -> new Intent("com.anddoes.launcher.SET_THEME")
                        .putExtra("com.anddoes.launcher.THEME_PACKAGE_NAME", context.getPackageName())
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                MANUAL_APPLY_NOT_SUPPORTED
        ),
        BEFORE(
                "Before",
                R.drawable.ic_launcher_before,
                new String[]{"com.beforesoft.launcher"},
                NO_SETTINGS_ACTIVITY,
                (context, launcherPackageName) -> new Intent("com.beforesoftware.launcher.APPLY_ICONS")
                        .putExtra("packageName", context.getPackageName())
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                new ManualApply() {
                    @Override
                    public String getCompatibilityMessage(Context context, String launcherName) {
                        return context.getResources().getString(R.string.apply_manual_before);
                    }
                    @Override
                    public String[] getInstructionSteps(Context context, String launcherName) {
                        return new String[]{
                                context.getResources().getString(R.string.apply_manual_before_step_1),
                                context.getResources().getString(R.string.apply_manual_before_step_2),
                                context.getResources().getString(R.string.apply_manual_before_step_3),
                                context.getResources().getString(R.string.apply_manual_before_step_4),
                                context.getResources().getString(
                                        R.string.apply_manual_before_step_5,
                                        context.getResources().getString(R.string.app_name)
                                )
                        };
                    }
                }
        ),
        BLACKBERRY(
                "BlackBerry",
                R.drawable.ic_launcher_blackberry,
                new String[]{"com.blackberry.blackberrylauncher"},
                "com.blackberry.blackberrylauncher.MainActivity",
                DIRECT_APPLY_NOT_SUPPORTED,
                (context, launcherName) -> new String[]{} // FIXME: Opens app without instructions
        ),
        CMTHEME(
                "CM Theme",
                R.drawable.ic_launcher_cm,
                new String[]{"org.cyanogenmod.theme.chooser"},
                NO_SETTINGS_ACTIVITY,
                new DirectApply() {
                    @Override
                    public Intent getActivity(Context context, String launcherPackageName) {
                        return new Intent("android.intent.action.MAIN")
                                .setComponent(new ComponentName(launcherPackageName, "org.cyanogenmod.theme.chooser.ChooserActivity"))
                                .putExtra("pkgName", context.getPackageName());
                    }

                    @Override
                    public void run (Context context, String launcherPackageName, ApplyCallback callback) {
                        try {
                            DirectApply.super.run(context, launcherPackageName, callback);
                        } catch (ActivityNotFoundException | NullPointerException e) {
                            Toast.makeText(context, R.string.apply_cmtheme_not_available, Toast.LENGTH_LONG).show();
                        } catch (SecurityException | IllegalArgumentException e) {
                            Toast.makeText(context, R.string.apply_cmtheme_failed, Toast.LENGTH_LONG).show();
                        }
                    }
                },
                MANUAL_APPLY_NOT_SUPPORTED
        ),
        COLOR_OS(
                "ColorOS",
                R.drawable.ic_launcher_color_os,
                new String[]{"com.oppo.launcher"},
                NO_SETTINGS_ACTIVITY,
                DIRECT_APPLY_NOT_SUPPORTED,
                new ManualApply() { // FIXME:
                    @SuppressLint("AnnotateVersionCheck")
                    @Override
                    public boolean isSupported(String launcherPackageName) {
                        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R;
                    }

                    @Override
                    public String[] getInstructionSteps(Context context, String launcherName) {
                        return new String[]{
                                context.getResources().getString(R.string.apply_manual_color_os_step_1),
                                context.getResources().getString(R.string.apply_manual_color_os_step_2),
                                context.getResources().getString(R.string.apply_manual_color_os_step_3),
                                context.getResources().getString(
                                        R.string.apply_manual_color_os_step_4,
                                        context.getResources().getString(R.string.app_name)
                                )
                        };
                    }

                    @Override
                    public void run(Context context, String launcherPackageName, ApplyCallback callback) {
                        if (isSupported(launcherPackageName)) {
                            ManualApply.super.run(context, launcherPackageName, callback);
                        } else {
                            launcherIncompatibleCustomMessage(
                                    context,
                                    "ColorOS",
                                    context.getResources().getString(
                                            R.string.apply_launcher_incompatible_depending_on_version, "ColorOS", 10
                                    )
                            );
                        }
                    }
                }),
        FLICK(
                "Flick",
                R.drawable.ic_launcher_flick,
                new String[]{"com.universallauncher.universallauncher"},
                NO_SETTINGS_ACTIVITY,
                new DirectApply() {
                    @Override
                    public Intent getActivity(Context context, String launcherPackageName) {
                        return context.getPackageManager().getLaunchIntentForPackage("com.universallauncher.universallauncher")
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    }
                    @Override
                    public Intent getBroadcast(Context context) {
                        return new Intent("com.universallauncher.universallauncher.FLICK_ICON_PACK_APPLIER")
                                .putExtra("com.universallauncher.universallauncher.ICON_THEME_PACKAGE", context.getPackageName())
                                .setComponent(new ComponentName("com.universallauncher.universallauncher", "com.android.launcher3.icon.ApplyIconPack"));
                    }
                }, MANUAL_APPLY_NOT_SUPPORTED
        ),
        GO(
                "GO EX",
                R.drawable.ic_launcher_go,
                new String[]{"com.gau.go.launcherex"},
                NO_SETTINGS_ACTIVITY,
                new DirectApply() {
                    @Override
                    public Intent getActivity(Context context, String launcherPackageName) {
                        return context.getPackageManager().getLaunchIntentForPackage("com.gau.go.launcherex")
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    }

                    @Override
                    public Intent getBroadcast(Context context) {
                        return new Intent("com.gau.go.launcherex.MyThemes.mythemeaction")
                                .putExtra("type", 1)
                                .putExtra("pkgname", context.getPackageName());
                    }
                },
                MANUAL_APPLY_NOT_SUPPORTED
        ),
        HIOS(
                "HiOS",
                R.drawable.ic_launcher_hios,
                new String[]{"com.transsion.hilauncher"},
                NO_SETTINGS_ACTIVITY,
                DIRECT_APPLY_NOT_SUPPORTED,
                (context, launcherName) -> new String[]{ // FIXME: R.string.apply_manual_hios should be deleted, it has no extra info compared to the standard message
                        context.getResources().getString(R.string.apply_manual_hios_step_1),
                        context.getResources().getString(R.string.apply_manual_hios_step_2),
                        context.getResources().getString(R.string.apply_manual_hios_step_3),
                        context.getResources().getString(R.string.apply_manual_hios_step_4),
                        context.getResources().getString(
                                R.string.apply_manual_hios_step_5,
                                context.getResources().getString(R.string.app_name)
                        )
                }
        ),
        HOLO(
                "Holo",
                R.drawable.ic_launcher_holo,
                new String[]{"com.mobint.hololauncher"},
                "com.mobint.hololauncher.SettingsActivity",
                DIRECT_APPLY_NOT_SUPPORTED,
                (context, launcherName) -> new String[]{} // FIXME: Opens app without instructions
        ),
        HOLOHD(
                "Holo HD",
                R.drawable.ic_launcher_holohd,
                new String[]{"com.mobint.hololauncher.hd"},
                "com.mobint.hololauncher.SettingsActivity", // TODO: Is this package actually correct?
                DIRECT_APPLY_NOT_SUPPORTED,
                (context, launcherName) -> new String[]{} // FIXME: Opens app without instructions
        ),
        HYPERION(
                "Hyperion",
                R.drawable.ic_launcher_hyperion,
                new String[]{"projekt.launcher"},
                "projekt.launcher.activities.SettingsActivity",
                DIRECT_APPLY_NOT_SUPPORTED,
                (context, launcherName) -> new String[]{} // FIXME: Opens app without instructions
        ),
        ION_LAUNCHER(
                "Ion Launcher",
                R.drawable.ic_launcher_ion,
                new String[]{"one.zagura.IonLauncher"},
                NO_SETTINGS_ACTIVITY,
                (context, launcherPackageName) -> new Intent("one.zagura.IonLauncher.ui.settings.iconPackPicker.IconPackPickerActivity")
                        .putExtra("pkgname", context.getPackageName())
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                MANUAL_APPLY_NOT_SUPPORTED // TODO: Add instructions
        ),
        KISS(
                "KISS",
                R.drawable.ic_launcher_kiss,
                new String[]{"fr.neamar.kiss"},
                NO_SETTINGS_ACTIVITY,
                DIRECT_APPLY_NOT_SUPPORTED,
                (context, launcherName) -> new String[]{ // FIXME: R.string.apply_manual_kiss should be deleted, it has no extra info compared to the standard message
                        context.getResources().getString(R.string.apply_manual_kiss_step_1),
                        context.getResources().getString(R.string.apply_manual_kiss_step_2),
                        context.getResources().getString(R.string.apply_manual_kiss_step_3),
                        context.getResources().getString(
                                R.string.apply_manual_kiss_step_4,
                                context.getResources().getString(R.string.app_name)
                        )
                }
        ),
        KVAESITSO(
                "Kvaesitso",
                R.drawable.ic_launcher_kvaesitso,
                new String[]{"de.mm20.launcher2.release"},
                NO_SETTINGS_ACTIVITY,
                DIRECT_APPLY_NOT_SUPPORTED,
                (context, launcherName) -> new String[]{ // FIXME: R.string.apply_manual_kvaesitso should be deleted, it has no extra info compared to the standard message
                        context.getResources().getString(R.string.apply_manual_kvaesitso_step_1),
                        context.getResources().getString(R.string.apply_manual_kvaesitso_step_2),
                        context.getResources().getString(R.string.apply_manual_kvaesitso_step_3),
                        context.getResources().getString(
                                R.string.apply_manual_kvaesitso_step_4,
                                context.getResources().getString(R.string.app_name)
                        ),
                        context.getResources().getString(R.string.apply_manual_kvaesitso_step_5),
                }
        ),
        LAWNCHAIR_LEGACY(
                "Lawnchair Legacy",
                R.drawable.ic_launcher_lawnchair,
                new String[]{"ch.deletescape.lawnchair.plah", "ch.deletescape.lawnchair.ci"},
                NO_SETTINGS_ACTIVITY,
                (context, launcherName) -> new Intent("ch.deletescape.lawnchair.APPLY_ICONS")
                        .putExtra("packageName", context.getPackageName()),
                (context, launcherName) -> new String[]{} // FIXME: Opens app without instructions
        ),
        LAWNCHAIR(
                "Lawnchair",
                R.drawable.ic_launcher_lawnchair,
                new String[]{"app.lawnchair", "app.lawnchair.play"},
                "app.lawnchair.ui.preferences.PreferenceActivity",
                DIRECT_APPLY_NOT_SUPPORTED,
                (context, launcherName) -> new String[]{} // FIXME: Opens app without instructions
        ),
        LGHOME( /* INCOMPATIBLE */
                "LG Home",
                R.drawable.ic_launcher_lg,
                new String[]{"com.lge.launcher2", "com.lge.launcher3"},
                NO_SETTINGS_ACTIVITY,
                DIRECT_APPLY_NOT_SUPPORTED,
                MANUAL_APPLY_NOT_SUPPORTED
        ),
        LUCID(
                "Lucid",
                R.drawable.ic_launcher_lucid,
                new String[]{"com.powerpoint45.launcher"},
                NO_SETTINGS_ACTIVITY,
                (context, launcherPackageName) -> new Intent("com.powerpoint45.action.APPLY_THEME", null)
                        .putExtra("icontheme", context.getPackageName()),
                MANUAL_APPLY_NOT_SUPPORTED
        ),
        MOTO(
                "Moto Launcher",
                R.drawable.ic_launcher_moto,
                new String[]{"com.motorola.launcher3"},
                "com.motorola.personalize.app.IconPacksActivity",
                DIRECT_APPLY_NOT_SUPPORTED,
                (context, launcherName) -> new String[]{} // FIXME: Opens app without instructions
        ),
        MICROSOFT(
                "Microsoft",
                R.drawable.ic_launcher_microsoft,
                new String[]{"com.microsoft.launcher"},
                NO_SETTINGS_ACTIVITY,
                DIRECT_APPLY_NOT_SUPPORTED,
                (context, launcherName) -> new String[]{} // FIXME: There are no instructions!
        ),
        NIAGARA(
                "Niagara",
                R.drawable.ic_launcher_niagara,
                new String[]{"bitpit.launcher"},
                NO_SETTINGS_ACTIVITY,
                (context, launcherPackageName) -> new Intent("bitpit.launcher.APPLY_ICONS")
                        .putExtra("packageName", context.getPackageName()),
                MANUAL_APPLY_NOT_SUPPORTED
        ),
        NOTHING(
                "Nothing",
                R.drawable.ic_launcher_nothing,
                new String[]{"com.nothing.launcher"},
                NO_SETTINGS_ACTIVITY,
                DIRECT_APPLY_NOT_SUPPORTED,
                (context, launcherName) -> new String[]{
                        context.getResources().getString(R.string.apply_manual_nothing_step_1),
                        context.getResources().getString(R.string.apply_manual_nothing_step_2),
                        context.getResources().getString(R.string.apply_manual_nothing_step_3),
                        context.getResources().getString(
                                R.string.apply_manual_nothing_step_4,
                                context.getResources().getString(R.string.app_name)
                        ),
                }
        ),
        NOUGAT(
                "Nougat",
                R.drawable.ic_launcher_nougat,
                new String[]{"me.craftsapp.nlauncher"},
                NO_SETTINGS_ACTIVITY,
                (context, launcherPackageName) -> new Intent("me.craftsapp.nlauncher")
                    .setAction("me.craftsapp.nlauncher.SET_THEME")
                    .putExtra("me.craftsapp.nlauncher.theme.NAME", context.getPackageName())
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                MANUAL_APPLY_NOT_SUPPORTED
        ),
        NOVA(
                "Nova",
                R.drawable.ic_launcher_nova,
                new String[]{"com.teslacoilsw.launcher", "com.teslacoilsw.launcher.prime"},
                NO_SETTINGS_ACTIVITY,
                (context, launcherPackageName) -> {
                    final Intent nova = new Intent("com.teslacoilsw.launcher.APPLY_ICON_THEME");
                    nova.setPackage("com.teslacoilsw.launcher");
                    nova.putExtra("com.teslacoilsw.launcher.extra.ICON_THEME_TYPE", "GO");
                    nova.putExtra("com.teslacoilsw.launcher.extra.ICON_THEME_PACKAGE", context.getPackageName());
                    String reshapeSetting = context.getResources().getString(R.string.nova_reshape_legacy_icons);
                    if (!reshapeSetting.equals("KEEP")) {
                        // Allowed values are ON, OFF and AUTO
                        nova.putExtra("com.teslacoilsw.launcher.extra.ICON_THEME_RESHAPE", reshapeSetting);
                    }
                    nova.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    return nova;
                },
                MANUAL_APPLY_NOT_SUPPORTED
        ),
        ONEUI(
                "Samsung One UI",
                R.drawable.ic_launcher_one_ui,
                new String[]{"com.sec.android.app.launcher"},
                NO_SETTINGS_ACTIVITY, // TODO: Could link to Theme Park if available
                DIRECT_APPLY_NOT_SUPPORTED,
                new ManualApply() {
                    @Override
                    public boolean isSupported(String launcherPackageName) {
                        return Build.VERSION.SDK_INT > Build.VERSION_CODES.R;
                    }

                    @Override
                    public String getCompatibilityMessage(Context context, String launcherName) {
                        return context.getResources().getString(
                                R.string.apply_manual_samsung_oneui,
                                launcherName,
                                launcherName + " 4.0"
                        );
                    }

                    @Override
                    public String[] getInstructionSteps(Context context, String launcherName) {
                        return new String[] {
                                context.getResources().getString(
                                        R.string.apply_manual_samsung_oneui_step_1,
                                        "Samsung Galaxy Store"
                                ),
                                context.getResources().getString(
                                        R.string.apply_manual_samsung_oneui_step_2,
                                        "Theme Park"
                                ),
                                context.getResources().getString(R.string.apply_manual_samsung_oneui_step_3),
                                context.getResources().getString(R.string.apply_manual_samsung_oneui_step_4),
                                context.getResources().getString(R.string.apply_manual_samsung_oneui_step_5),
                                context.getResources().getString(
                                        R.string.apply_manual_samsung_oneui_step_6,
                                        context.getResources().getString(R.string.app_name)
                                ),
                                context.getResources().getString(
                                        R.string.apply_manual_samsung_oneui_step_7,
                                        context.getResources().getString(R.string.app_name)
                                )
                        };
                    }

                    @Override
                    public void run(Context context, String launcherPackageName, ApplyCallback callback) {
                        applyOneUI(context, launcherPackageName, callback);
                    }
                }
        ),
        OXYGEN_OS(
                "OxygenOS",
                R.drawable.ic_launcher_oxygen_os,
                new String[]{"net.oneplus.launcher"},
                NO_SETTINGS_ACTIVITY,
                DIRECT_APPLY_NOT_SUPPORTED,
                new ManualApply() {
                    @SuppressLint("AnnotateVersionCheck")
                    @Override
                    public boolean isSupported(String launcherPackageName) {
                        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P;
                    }

                    @Override
                    public String[] getInstructionSteps(Context context, String launcherName) {
                        return new String[]{
                                context.getResources().getString(R.string.apply_manual_oxygen_os_step_1),
                                context.getResources().getString(R.string.apply_manual_oxygen_os_step_2),
                                context.getResources().getString(R.string.apply_manual_oxygen_os_step_3),
                                context.getResources().getString(
                                        R.string.apply_manual_oxygen_os_step_4,
                                        context.getResources().getString(R.string.app_name)
                                )
                        };
                    }

                    @Override
                    public void run(Context context, String launcherPackageName, ApplyCallback callback) {
                        if (isSupported(launcherPackageName)) {
                            ManualApply.super.run(context, launcherPackageName, callback);
                        } else {
                            launcherIncompatibleCustomMessage(
                                    context,
                                    "OxygenOS",
                                    context.getResources().getString(
                                            R.string.apply_launcher_incompatible_depending_on_version, "OxygenOS", 8
                                    )
                            );
                        }
                    }
                }
        ),
        PIXEL( /* INCOMPATIBLE */
                "Pixel",
                R.drawable.ic_launcher_pixel,
                new String[]{"com.google.android.apps.nexuslauncher"},
                NO_SETTINGS_ACTIVITY,
                DIRECT_APPLY_NOT_SUPPORTED,
                MANUAL_APPLY_NOT_SUPPORTED
        ),
        POCO(
                "POCO",
                R.drawable.ic_launcher_poco,
                new String[]{"com.mi.android.globallauncher"},
                "com.miui.home.settings.HomeSettingsActivity",
                DIRECT_APPLY_NOT_SUPPORTED,
                (context, launcherName) -> new String[]{} // FIXME: Opens app without instructions
        ),
        PROJECTIVY(
                "Projectivy",
                R.drawable.ic_launcher_projectivy,
                new String[]{"com.spocky.projengmenu"},
                NO_SETTINGS_ACTIVITY,
                (context, launcherPackageName) -> new Intent("com.spocky.projengmenu.APPLY_ICONPACK")
                        .setPackage("com.spocky.projengmenu")
                        .putExtra("com.spocky.projengmenu.extra.ICONPACK_PACKAGENAME", context.getPackageName())
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                MANUAL_APPLY_NOT_SUPPORTED
        ),
        SMART(
                "Smart",
                R.drawable.ic_launcher_smart,
                new String[]{"ginlemon.flowerfree", "ginlemon.flowerpro", "ginlemon.flowerpro.special"},
                NO_SETTINGS_ACTIVITY,
                (context, launcherPackageName) -> new Intent("ginlemon.smartlauncher.setGSLTHEME")
                        .putExtra("package", context.getPackageName())
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                MANUAL_APPLY_NOT_SUPPORTED
        ),
        SOLO(
                "Solo",
                R.drawable.ic_launcher_solo,
                new String[]{"home.solo.launcher.free"},
                NO_SETTINGS_ACTIVITY,
                new DirectApply() {
                    @Override
                    public Intent getActivity(Context context, String launcherPackageName) {
                        return context.getPackageManager().getLaunchIntentForPackage("home.solo.launcher.free")
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    }

                    @Override
                    public Intent getBroadcast(Context context) {
                        return new Intent("home.solo.launcher.free.APPLY_THEME")
                                .putExtra("EXTRA_THEMENAME", context.getResources().getString(R.string.app_name))
                                .putExtra("EXTRA_PACKAGENAME", context.getPackageName());
                    }
                },
                MANUAL_APPLY_NOT_SUPPORTED
        ),
        SQUARE(
                "Square",
                R.drawable.ic_launcher_square,
                new String[]{"com.ss.squarehome2"},
                NO_SETTINGS_ACTIVITY,
                (context, launcherPackageName) -> new Intent("com.ss.squarehome2.ACTION_APPLY_ICONPACK")
                        .setComponent(ComponentName.unflattenFromString("com.ss.squarehome2/.ApplyThemeActivity"))
                        .putExtra("com.ss.squarehome2.EXTRA_ICONPACK", context.getPackageName()),
                MANUAL_APPLY_NOT_SUPPORTED
        ),
        STOCK_LEGACY(
                /*
                 * Historically, ColorOS, OxygenOS and realme UI were standalone launcher variants
                 * but as of Android 12 they're slowly being merged into a single launcher that no
                 * longer reports as e.g. "com.oppo.launcher" but now as "com.android.launcher".
                 */
                isColorOS() ? "ColorOS" : isRealmeUI() ? "realme UI" : "Stock Launcher",
                isColorOS() ? R.drawable.ic_launcher_color_os : isRealmeUI() ? R.drawable.ic_launcher_realme_ui : R.drawable.ic_launcher_android,
                new String[]{"com.android.launcher"},
                NO_SETTINGS_ACTIVITY,
                DIRECT_APPLY_NOT_SUPPORTED,
                (context, launcherName) -> new String[]{
                        context.getResources().getString(R.string.apply_manual_color_os_step_1),
                        context.getResources().getString(R.string.apply_manual_color_os_step_2),
                        context.getResources().getString(R.string.apply_manual_color_os_step_3),
                        context.getResources().getString(
                                R.string.apply_manual_color_os_step_4,
                                context.getResources().getString(R.string.app_name)
                        ),
                }
        ),
        TINYBIT(
                "TinyBit",
                R.drawable.ic_launcher_tinybit,
                new String[]{"rocks.tbog.tblauncher"},
                "rocks.tbog.tblauncher.SettingsActivity",
                DIRECT_APPLY_NOT_SUPPORTED,
                (context, launcherName) -> new String[]{} // FIXME: Opens app without instructions
        ),
        ZENUI(
                "ZenUI",
                R.drawable.ic_launcher_zenui,
                new String[]{"com.asus.launcher"},
                NO_SETTINGS_ACTIVITY,
                (context, launcherPackageName) -> new Intent("com.asus.launcher")
                        .setAction("com.asus.launcher.intent.action.APPLY_ICONPACK")
                        .addCategory(Intent.CATEGORY_DEFAULT)
                        .putExtra("com.asus.launcher.iconpack.PACKAGE_NAME", context.getPackageName())
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                MANUAL_APPLY_NOT_SUPPORTED
        );

        /**
         * Interface for launchers to implement when they support applying icons directly, without
         * the need to open the icon pack app. The {@code run()} method should be self-contained
         * and make sure to finish the activity after applying the icon pack.
         */
        private interface DirectApply {
            default boolean isSupported(String packageName) { return true; }

            Intent getActivity(Context context, String launcherPackageName);
            default Intent getBroadcast(Context context) { return null; }

            default void run(Context context, String launcherPackageName, ApplyCallback callback) throws ActivityNotFoundException, NullPointerException {
                final Intent activityIntent = getActivity(context, launcherPackageName);
                final Intent broadcastIntent = getBroadcast(context);
                if (broadcastIntent != null) {
                    context.sendBroadcast(broadcastIntent);
                }
                context.startActivity(activityIntent);
                if (callback != null) callback.onSuccess(context);
            }
        }

        /**
         * Interface for launchers to implement when they support icon packs but not direct apply.
         * They should provide an overall compatibility description and a list of instructions for
         * users to follow step by step on their device. The {@code run()} method should be
         * self-contained and either launch a deep link into the launcher's settings where the icon
         * pack can be applied, or simply display a dialog with the instructions.
         *
         * @see LauncherType#showManualApplyDialog(Context, String, ApplyCallback)
         */
        private interface ManualApply {
            default boolean isSupported(String launcherPackageName) { return true; }

            default String getSettingsActivity(Context context, String launcherPackageName) { return null; }

            default String getCompatibilityMessage(Context context, String launcherName) {
                return context.getResources().getString(
                        R.string.apply_manual,
                        launcherName,
                        context.getResources().getString(R.string.app_name)
                );
            }

            String[] getInstructionSteps(Context context, String launcherName);

            default void run(Context context, String launcherPackageName, ApplyCallback callback) throws ActivityNotFoundException, NullPointerException {
                showManualApplyDialog(context, launcherPackageName, callback);
            }
        }

        /**
         * Interface for callbacks to be used when applying icon packs directly or manually.
         */
        public interface ApplyCallback {
            void onSuccess(Context context);
            //void onError(Exception error);
        }

        public final String name;
        public final @DrawableRes
        int icon;
        public final String[] packages;
        public final String settingsActivityName;

        private DirectApply directApplyFunc = null;
        private ManualApply manualApplyFunc = null;

        LauncherType() {
            this.name = null;
            this.icon = 0;
            this.packages = null;
            this.settingsActivityName = null;
        }

        LauncherType(String name, @DrawableRes int icon, String[] packages, String settingsActivityName, DirectApply directApplyFunc, ManualApply manualApplyFunc) {
            this.name = name;
            this.icon = icon;
            this.packages = packages;
            this.settingsActivityName = settingsActivityName;
            this.directApplyFunc = directApplyFunc;
            this.manualApplyFunc = manualApplyFunc;
        }

        /**
         * Check if this launcher type is installed on the device. All known packages are checked
         * so in the case of multiple matches, you won't know which package is installed.
         *
         * @param context
         * @return true if the launcher is installed, false otherwise.
         */
        public boolean isInstalled(Context context) {
            PackageManager packageManager = context.getPackageManager();
            if (this.packages == null) return false;

            for (String packageName : this.packages) {
                try {
                    packageManager.getPackageInfo(packageName, 0);
                    return true;
                } catch (PackageManager.NameNotFoundException e) {
                    /* keep searching */
                }
            }
            return false;
        }
    }

    public static class Launcher {
        public final LauncherType type;
        public final String installedPackage;

        public Launcher(@NonNull LauncherType type, @NonNull String installedPackage) {
            this.type = type;
            this.installedPackage = installedPackage;
        }

        /**
         * Check if the launcher supports direct apply of icon packs. Not all launchers do, and it's
         * on the launcher developers to provide the necessary interfaces to allow this. Note that
         * when you use `applyDirectly` it's still possible for it to throw an exception (see
         * exception `LauncherDirectApplyFailed`) because newer versions or OS-specific variants of
         * the launcher might not support it.
         * Consider the return value of this method as a hint, not a guarantee.
         * @return true if the launcher supports direct apply, false otherwise.
         */
        public boolean supportsDirectApply() {
            if (this.type.directApplyFunc != null) {
                return this.type.directApplyFunc.isSupported(this.installedPackage);
            }
            return false;
        }

        /**
         * Check if the launcher supports applying icon packs manually.
         * @return true if the launcher supports manual apply, false otherwise
         */
        public boolean supportsManualApply() {
            if (this.type.manualApplyFunc != null) {
                return this.type.manualApplyFunc.isSupported(this.installedPackage);
            }
            return false;
        }

        /**
         * Check if the launcher supports icon packs. Not all launchers do specifically not those
         * that want to stay close to Vanilla Android.
         * @return true if the launcher supports icon packs, false otherwise
         */
        public boolean supportsIconPacks() {
            return supportsDirectApply() || supportsManualApply();
        }

        /**
         * Get the instruction steps for applying the icon pack manually. Make sure to call
         * {@code supportsManualApply} before calling this or otherwise the result might be empty
         *
         * @return An array of strings containing the steps to apply the icon pack manually.
         */
        public String[] getManualApplyInstructions(Context context) {
            if (this.type.manualApplyFunc != null) {
                return this.type.manualApplyFunc.getInstructionSteps(context, this.type.name);
            }
            return new String[]{};
        }

        /**
         * Get the settings activity name for the launcher. This is used to launch the settings
         * activity of the launcher where the icon pack can be applied. Make sure to call
         * {@code supportsManualApply} before calling this or otherwise the result might be null.
         *
         * @return The settings activity name for the launcher, or null if not available.
         */
        public String getSettingsActivity(Context context) {
            if (this.type.manualApplyFunc != null) {
                return this.type.manualApplyFunc.getSettingsActivity(context, this.installedPackage);
            }
            return null;
        }

        /**
         * Get the direct apply activity for the launcher. This is used to apply the icon pack
         * without leaving the app. Make sure to call {@code supportsDirectApply} before calling this
         * or otherwise the result might be null.
         *
         * @return A pair of intents, the first one is the activity intent to apply the icon pack,
         * and the second one is a broadcast intent to notify the launcher about the change. Either
         * of these can be null.
         */
        public Pair<Intent, Intent> getDirectApplyIntents(Context context) {
            if (this.type.directApplyFunc != null) {
                return new Pair<>(
                        this.type.directApplyFunc.getActivity(context, this.installedPackage),
                        this.type.directApplyFunc.getBroadcast(context)
                );
            }
            return null;
        }

        /**
         * Tries to apply the icon pack directly. Before calling this, you can ask the launcher with
         * {@code supportsDirectApply} if it supports this method. Note that this is just a hint,
         * not a guarantee, so make sure to catch the exceptions thrown by this method and handle
         * them gracefully.
         *
         * <p>
         *  <sup>
         *      <b>Credit where credit is due ♥</b><br>
         *
         *     The instructions, logic and fallback behind this simple method are the
         *     collective work of dozens of open source developers and translators carried
         *     out over many years. If you use this method outside of the CandyBar dashboard,
         *     please credit the contributors.<br>
         *     • <b>Contributors:</b> com/candybar/lib/src/main/res/xml/dashboard_contributors.xml<br>
         *     • <b>Translators:</b> com/candybar/lib/src/main/res/xml/dashboard_translator.xml
         *  </sup>
         * </p>
         *
         * @throws Launcher.LauncherNotInstalledException If the launcher isn't installed on the device.
         * @throws Launcher.LauncherDirectApplyNotSupported If the launcher doesn't support applying icon packs directly.
         * @throws Launcher.LauncherDirectApplyFailed If the icon pack couldn't be applied to the launcher directly. This is never an expected case. If it happens, it might indicate that the launcher interface changed.
         *
         * @see Launcher#supportsDirectApply()
         */
        public void applyDirectly(Context context, LauncherType.ApplyCallback callback) throws ActivityNotFoundException, NullPointerException {
            if (!isInstalled(context)) throw new Launcher.LauncherNotInstalledException(new ActivityNotFoundException());
            if (this.type.directApplyFunc == null) throw new Launcher.LauncherDirectApplyNotSupported(new ActivityNotFoundException());
            if (!this.type.directApplyFunc.isSupported(this.installedPackage)) throw new Launcher.LauncherDirectApplyNotSupported(new ActivityNotFoundException());
            try {
                this.type.directApplyFunc.run(context, this.installedPackage, callback);
                logLauncherDirectApply(this.installedPackage);
            } catch (Exception e) {
                throw new Launcher.LauncherDirectApplyFailed(e);
            }
        }
        public void applyDirectly(Context context) throws ActivityNotFoundException, NullPointerException {
            applyDirectly(context, DEFAULT_CALLBACK);
        }

        /**
         * Show manual instructions to the user on how to apply the icon pack to the launcher. In
         * case the launcher offers a dedicated settings activity, it will be called after the user
         * confirms the dialog. (If the user cancels the dialog, nothing happens.)
         *
         * <p>
         *  <sup>
         *      <b>Credit where credit is due ♥</b><br>
         *
         *     The instructions, logic and fallback behind this simple method are the
         *     collective work of dozens of open source developers and translators carried
         *     out over many years. If you use this method outside of the CandyBar dashboard,
         *     please credit the contributors.<br>
         *     • <b>Contributors:</b> com/candybar/lib/src/main/res/xml/dashboard_contributors.xml<br>
         *     • <b>Translators:</b> com/candybar/lib/src/main/res/xml/dashboard_translator.xml
         *  </sup>
         * </p>
         *
         * @param context The context to use for launching the settings activity or showing the dialog.
         * @param callback The success callback to be called when the user closes the dialog.
         *
         * @throws Launcher.LauncherNotInstalledException If the launcher isn't installed on the device.
         * @throws Launcher.LauncherManualApplyNotSupported If the launcher doesn't support applying icon packs manually.
         * @throws Launcher.LauncherManualApplyFailed If an associated settings activity could not be launched. This is never an expected case. If it happens, it might indicate that the launcher interface changed.
         *
         * @see Launcher#supportsManualApply()
         */
        public void applyManually(Context context, LauncherType.ApplyCallback callback) throws ActivityNotFoundException, NullPointerException {
            //if (!isInstalled(context, launcherPackageName)) throw new LauncherNotInstalledException(new ActivityNotFoundException());
            if (this.type.manualApplyFunc == null) throw new Launcher.LauncherManualApplyNotSupported(new ActivityNotFoundException());
            if (!this.type.manualApplyFunc.isSupported(this.installedPackage)) throw new Launcher.LauncherManualApplyNotSupported(new ActivityNotFoundException());

            try {
                this.type.manualApplyFunc.run(context, this.installedPackage, callback);
                logLauncherManualApply(this.installedPackage, "confirm");
            } catch (Exception e) {
                throw new Launcher.LauncherManualApplyFailed(e);
            }
        }

        /**
         * Show manual instructions to the user on how to apply the icon pack to the launcher. In
         * case the launcher offers a dedicated settings activity, it will be called after the user
         * confirms the dialog. (If the user cancels the dialog, nothing happens.)
         *
         * <p>
         *  <sup>
         *      <b>Credit where credit is due ♥</b><br>
         *
         *     The instructions, logic and fallback behind this simple method are the
         *     collective work of dozens of open source developers and translators carried
         *     out over many years. If you use this method outside of the CandyBar dashboard,
         *     please credit the contributors.<br>
         *     • <b>Contributors:</b> com/candybar/lib/src/main/res/xml/dashboard_contributors.xml<br>
         *     • <b>Translators:</b> com/candybar/lib/src/main/res/xml/dashboard_translator.xml
         *  </sup>
         * </p>
         *
         * @param context The context to use for launching the settings activity or showing the dialog.
         *
         * @throws Launcher.LauncherNotInstalledException If the launcher isn't installed on the device.
         * @throws Launcher.LauncherManualApplyNotSupported If the launcher doesn't support applying icon packs manually.
         * @throws Launcher.LauncherManualApplyFailed If an associated settings activity could not be launched. This is never an expected case. If it happens, it might indicate that the launcher interface changed.
         *
         * @see Launcher#supportsManualApply()
         */
        public void applyManually(Context context) throws ActivityNotFoundException, NullPointerException {
            applyManually(context, DEFAULT_CALLBACK);
        }

        /**
         * Check if the launcher is actually installed on the device. Only the installedPackage
         * parameter is used to determine this, all other known packages in the LauncherType are
         * ignored and not checked for. To check for all packages, use {@link LauncherType#isInstalled(Context)} instead.
         *
         * @param context
         * @return true if the launcher is installed, false otherwise.
         */
        public boolean isInstalled(Context context) {
            PackageManager packageManager = context.getPackageManager();
            boolean found = true;
            try {
                packageManager.getPackageInfo(this.installedPackage, 0);
            } catch (PackageManager.NameNotFoundException e) {
                found = false;
            }
            return found;
        }

        /**
         * Apply the icon pack to the launcher. This method follows the CandyBar standard flow:
         * If the launcher supports direct apply, it will try to apply the icon pack directly.
         * If that fails or isn't supported, it will try to show manual instructions to the user.
         * If the launcher doesn't support icon packs, an incompatibility message will be shown.
         *
         * @param context
         */
        @SuppressLint("StringFormatInvalid")
        public void apply(@NonNull Context context) {
            String packageName = this.installedPackage;
            String launcherName = this.type.name;

            CandyBarApplication.getConfiguration().getAnalyticsHandler().logEvent(
                    "click",
                    new HashMap<>() {{
                        put("section", "apply");
                        put("action", "open_dialog");
                        put("launcher", packageName);
                    }}
            );

            if (!this.supportsIconPacks()) {
                launcherIncompatible(context, launcherName);
                return;
            }

            if (!isInstalled(context) && !this.supportsManualApply()) {
                // FIXME: Not every launcher is on the Play Store
                showInstallPrompt(context, packageName);
                return;
            }

            // Try direct apply first
            if (this.supportsDirectApply()) {
                try {
                    this.applyDirectly(context);
                    return;
                } catch (ActivityNotFoundException | NullPointerException e) { /* No-op */ }
            }

            // Fall back to showing instructions if direct apply failed or isn't supported
            if(this.supportsManualApply()) {
                try {
                    this.applyManually(context);
                    return;
                } catch (ActivityNotFoundException | NullPointerException e) { /* No-op */ }
            }

            // If we're here, it means neither direct nor manual apply worked or are not supported
            launcherIncompatible(context, launcherName);
        }

        /**
         * Exception thrown when the launcher isn't installed on the device. Catch this when you
         * want to provide user-friendly feedback to the user or fall back on other methods such
         * as opening Google Play.
         *
         * @see LauncherType#openGooglePlay(Context, String)
         */
        public static class LauncherNotInstalledException extends ActivityNotFoundException {
            public LauncherNotInstalledException(Throwable cause) {
                super("The launcher is not installed on the device");
                initCause(cause); // preserves the original exceptions information
            }
        }

        /**
         * Exception thrown when the launcher doesn't support applying icon packs directly but if
         * the method `applyDirectly` is called anyway. CandyBar handles this gracefully in-app by
         * showing instructions for how to apply the pack manually. If you see this exception, it
         * means you forgot to respect `supportsDirectApply` before calling `applyDirectly`.
         *
         * @see Launcher#supportsDirectApply()
         */
        public static class LauncherDirectApplyNotSupported extends ActivityNotFoundException {
            public LauncherDirectApplyNotSupported(Throwable cause) {
                super("The launcher does not support direct apply");
                initCause(cause); // preserves the original exceptions information
            }
        }

        /**
         * Exception thrown when the launcher doesn't support applying icon packs manually but if
         * the method `applyManually` is called anyway. CandyBar handles this gracefully in-app by
         * showing a generic launcher incompatibility message. If you see this exception, it
         * means you forgot to respect `supportsManualApply` before calling `applyManually`.
         *
         * @see Launcher#supportsManualApply()
         */
        public static class LauncherManualApplyNotSupported extends ActivityNotFoundException {
            public LauncherManualApplyNotSupported(Throwable cause) {
                super("The launcher does not support manual apply");
                initCause(cause); // preserves the original exceptions information
            }
        }

        /**
         * Exception thrown when the icon pack couldn't be applied to the launcher directly. Catch
         * this when you want to show a user-friendly message to the user or offer the user to send
         * a bug report. In the wild, this exception could indicate that the launcher has been
         * updated by the developers and its interface for applying icon packs has changed.
         * For cases when the launcher isn't installed, use `LauncherNotInstalledException`.
         *
         * @see LauncherNotInstalledException
         */
        public static class LauncherDirectApplyFailed extends ActivityNotFoundException {
            public LauncherDirectApplyFailed(Throwable cause) {
                super("The launcher supports direct apply but applying the icon pack failed");
                initCause(cause); // preserves the original exceptions information
            }
        }

        /**
         * Exception thrown when the launcher's activity couldn't be run. Catch this when you want
         * to show a user-friendly message to the user or offer the user to send a bug report. In
         * the wild, this exception could indicate that the launcher has been updated by the
         * developers and its activity names have changed.
         * For cases when the launcher isn't installed, use `LauncherNotInstalledException`.
         *
         * @see LauncherNotInstalledException
         */
        public static class LauncherManualApplyFailed extends ActivityNotFoundException {
            public LauncherManualApplyFailed(Throwable cause) {
                super("The launcher supports manual apply but launching the activity failed");
                initCause(cause); // preserves the original exceptions information
            }
        }
    }

    /**
     * Get the launcher object for a given package name. If the package name is null or not
     * recognized, it returns Launcher.UNKNOWN. Even though Launcher.UNKNOWN satisfies the
     * interface and responds to all methods, it's wiser to check for it explicitly and then
     * suggest the user to 1) install a different launcher or 2) submit this launcher to you
     * so it can be added to this library.
     *
     * @param packageName The package name of the launcher to look up.
     * @return The corresponding Launcher enum value or Launcher.UNKNOWN if not found.
     */
    public static Launcher getLauncher(String packageName) {
        if (packageName == null) return new Launcher(LauncherType.UNKNOWN, packageName);

        for (LauncherType launcher : LauncherType.values()) {
            if (launcher.packages == null) continue;
            for (String launcherPackageName : launcher.packages) {
                if (launcherPackageName.contentEquals(packageName)) {
                    return new Launcher(launcher, packageName);
                }
            }
        }

        return new Launcher(LauncherType.UNKNOWN, packageName);
    }

    private static void logLauncherDirectApply(String launcherPackage) {
        CandyBarApplication.getConfiguration().getAnalyticsHandler().logEvent(
                "click",
                new HashMap<>() {{
                    put("section", "apply");
                    put("action", "confirm");
                    put("launcher", launcherPackage);
                }}
        );
    }

    private static void logLauncherManualApply(String launcherPackage, String action) {
        CandyBarApplication.getConfiguration().getAnalyticsHandler().logEvent(
                "click",
                new HashMap<>() {{
                    put("section", "apply");
                    put("action", "manual_open_" + action);
                    put("launcher", launcherPackage);
                }}
        );
    }

    @SuppressLint("StringFormatInvalid")
    private static void showManualApplyDialog(Context context, String launcherPackageName, LauncherType.ApplyCallback callback) {
        Launcher launcher = getLauncher(launcherPackageName);
        boolean isInstalled = launcher.isInstalled(context);

        int positiveButton = isInstalled ? android.R.string.ok : R.string.install;
        int negativeButton = android.R.string.cancel;

        String installPrompt = context.getResources().getString(R.string.apply_launcher_not_installed, launcher.type.name);
        String activityLaunchFailed = context.getResources().getString(R.string.apply_launch_failed, launcher.type.name);

        String description = (launcher.type.manualApplyFunc == MANUAL_APPLY_NOT_SUPPORTED) ? null : launcher.type.manualApplyFunc.getCompatibilityMessage(context, launcher.type.name);
        String[] steps = (launcher.type.manualApplyFunc == MANUAL_APPLY_NOT_SUPPORTED) ? new String[]{} : launcher.type.manualApplyFunc.getInstructionSteps(context, launcher.type.name);
        String content = ((description == null) ? "" : (description + "\n\n"))
                + ((steps.length > 0) ? "\t• " : "")
                + String.join("\n\t• ", steps) // bullet point list of instructions
                + ((!isInstalled && (steps.length > 0)) ? "\n\n" : "")
                + (isInstalled ? "" : installPrompt); // prompt to install the launcher

        new MaterialDialog.Builder(context)
                .typeface(TypefaceHelper.getMedium(context), TypefaceHelper.getRegular(context))
                .title(launcher.type.name)
                .content(content)
                .positiveText(positiveButton)
                .onPositive((dialog, which) -> {
                    if (isInstalled) {
                        logLauncherManualApply(launcherPackageName, "confirm");
                        if (launcher.type.settingsActivityName == null) return;
                        try {
                            String settingsActivity = launcher.type.manualApplyFunc.getSettingsActivity(context, launcherPackageName);
                            final Intent intent = new Intent(Intent.ACTION_MAIN);
                            intent.setComponent(new ComponentName(launcherPackageName, settingsActivity));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                            if (callback != null) callback.onSuccess(context);
                        } catch (ActivityNotFoundException | NullPointerException e) {
                            openGooglePlay(context, launcherPackageName);
                        } catch (SecurityException | IllegalArgumentException e) {
                            Toast.makeText(context, activityLaunchFailed, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        openGooglePlay(context, launcherPackageName);
                    }
                })
                .negativeText(negativeButton)
                .onNegative(((dialog, which) -> {
                    logLauncherManualApply(launcherPackageName, "cancel");
                }))
                .show();
    }

    private static void showInstallPrompt(Context context, String launcherPackageName) {
        showManualApplyDialog(context, launcherPackageName, LauncherHelper.DEFAULT_CALLBACK);
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
     * [1] <a href="https://www.androidpolice.com/how-to-use-custom-icon-packs-on-samsung-one-ui-4/">How to use custom icon packs on Samsung One UI 4</a>
     * [2] <a href="https://en.wikipedia.org/wiki/One_UI#One_UI_4">One UI 4.1.1</a>
     * [3] <a href="https://github.com/zixpo/candybar/pull/122#issuecomment-1510379686">Samsung OneUI Support in CandyBar</a>
     */
    private static void applyOneUI(Context context, String launcherPackage, LauncherType.ApplyCallback callback) {
        Launcher launcher = getLauncher(launcherPackage);
        String launcherName = launcher.type.name;
        String[] instructions = launcher.type.manualApplyFunc.getInstructionSteps(context, launcherName);

        String incompatibleText = context.getResources().getString(
                R.string.apply_manual_samsung_oneui_too_old,
                launcherName
        );
        String compatibleText =
                "\t• " + instructions[0]
                + "\n\t• " + String.join(
                        "\n\t• ",
                        Arrays.copyOfRange(instructions, 1, instructions.length - 2)
                )
                + "\n\n" + instructions[instructions.length - 1];
        new MaterialDialog.Builder(context)
                .typeface(TypefaceHelper.getMedium(context), TypefaceHelper.getRegular(context))
                .title(launcherName)
                .content(
                        launcher.type.manualApplyFunc.getCompatibilityMessage(context, launcherName)
                                + "\n\n"
                                + (launcher.type.manualApplyFunc.isSupported(launcherPackage) ? compatibleText : incompatibleText)
                )
                .positiveText(android.R.string.yes)
                .onPositive((dialog, which) -> {
                    logLauncherManualApply(launcherPackage, "confirm");
                    if (launcher.type.manualApplyFunc.isSupported(launcherPackage)) {
                        String packageName = "com.samsung.android.themedesigner";
                        try {
                            String uri = "samsungapps://ProductDetail/" + packageName;
                            Intent store = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                            context.startActivity(store);
                            if (callback != null) callback.onSuccess(context);
                        } catch (ActivityNotFoundException e) {
                            // The device can't handle Samsung Deep Links
                            // Let us point to the app in a browser instead
                            try {
                                Uri uri = Uri.parse("https://galaxystore.samsung.com/detail/" + packageName);
                                Intent store = new Intent(Intent.ACTION_VIEW, uri);
                                context.startActivity(store);
                                if (callback != null) callback.onSuccess(context);
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
                    logLauncherManualApply(launcherPackage, "cancel");
                }))
                .show();
    }

    private static void launcherIncompatible(Context context, String launcherName) {
        launcherIncompatibleCustomMessage(
                context,
                launcherName,
                context.getResources().getString(
                    R.string.apply_launcher_incompatible, launcherName, launcherName
                )
        );
    }

    private static void launcherIncompatibleCustomMessage(Context context, String launcherName, String message) {
        new MaterialDialog.Builder(context)
                .typeface(TypefaceHelper.getMedium(context), TypefaceHelper.getRegular(context))
                .title(launcherName)
                .content(message)
                .positiveText(android.R.string.yes)
                .onPositive((dialog, which) -> {
                    CandyBarApplication.getConfiguration().getAnalyticsHandler().logEvent(
                            "click",
                            new HashMap<>() {{
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
                            new HashMap<>() {{
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

    public static void openGooglePlay(Context context, String packageName) {
        try {
            Intent store = new Intent(Intent.ACTION_VIEW, Uri.parse(
                    "https://play.google.com/store/apps/details?id=" + packageName));
            context.startActivity(store);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, context.getResources().getString(
                    R.string.no_browser), Toast.LENGTH_LONG).show();
        }
    }

    public static boolean quickApply(Context context) {
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        String packageName = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY).activityInfo.packageName;
        Launcher launcher = getLauncher(packageName);
        try {
            if (launcher.supportsDirectApply()) {
                launcher.applyDirectly(context, DEFAULT_CALLBACK);
                return true;
            }
        } catch (ActivityNotFoundException | NullPointerException e) {
            return false;
        }
        return false;
    }

    public static boolean isColorOS() {
        String version = getSystemProperty("ro.build.version.opporom");
        boolean isLegacy = (version != null && !version.isEmpty());
        boolean isHybrid = false;

        /* Starting Android 12 and later, ColorOS is slowly being merged with OxygenOS and
         * no longer reporting as "com.oppo.launcher" but instead as "com.android.launcher".
         * Going forward this will likely be the standard launcher for OnePlus, realme
         * and OPPO devices but as of August 2023 this is not certain.
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (Build.MANUFACTURER.equalsIgnoreCase("OnePlus") || Build.MANUFACTURER.equalsIgnoreCase("OPPO")) {
                isHybrid = true;
            } else isHybrid = Build.MANUFACTURER.equalsIgnoreCase("realme") && isLegacy;
        }

        return isLegacy || isHybrid;
    }

    public static boolean isRealmeUI() {
        if (Build.MANUFACTURER.equalsIgnoreCase("realme")) {
            String version = getSystemProperty("ro.build.version.realmeui");
            return (version != null && !version.isEmpty());
        } else {
            return false;
        }
    }

    /**
     * Relying on SystemProperties is a brittle approach. ROMs vary widely
     * and properties present on one device might be missing on another.
     * We use this specifically for customised ROMs of e.g. OPPO, OnePlus
     * and realme to detect their flavoured launcher versions. If a property
     * with the expected value is present, this is a strong signal. If the
     * property is missing, however, this tells us nothing.
     */
    public static String getSystemProperty(String property) {
        String value = "";
        try {
            Class<?> systemProperties = Class.forName("android.os.SystemProperties");
            Method get = systemProperties.getMethod("get", String.class);
            value = (String) get.invoke(systemProperties, property);

        } catch (Exception ignored) {
        }
        return value;
    }
}
