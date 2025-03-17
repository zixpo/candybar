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

import java.lang.reflect.Method;
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
    private static final Launcher.DirectApply DIRECT_APPLY_NOT_SUPPORTED = null;
    private static final Launcher.ManualApply MANUAL_APPLY_NOT_SUPPORTED = null;

    public enum Launcher {
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
                    public void run (Context context, String launcherPackageName) {
                        try {
                            DirectApply.super.run(context, launcherPackageName);
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
                    public void run(Context context, String launcherPackageName) {
                        if (isSupported(launcherPackageName)) {
                            ManualApply.super.run(context, launcherPackageName);
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
        LAWNCHAIR(
                "Lawnchair",
                R.drawable.ic_launcher_lawnchair,
                new String[]{"ch.deletescape.lawnchair.plah", "ch.deletescape.lawnchair.ci", "app.lawnchair"},
                "app.lawnchair.ui.preferences.PreferenceActivity", // FIXME: Lawnchair 12 should really be its own launcher even if it looks the same
                new DirectApply() {
                    @Override
                    public boolean isSupported(String packageName) {
                        // Lawnchair 12 (app.lawnchair) doesn't support direct apply
                        return !packageName.startsWith("app");
                    }

                    @Override
                    public Intent getActivity(Context context, String launcherName) {
                        return new Intent("ch.deletescape.lawnchair.APPLY_ICONS", null)
                                .putExtra("packageName", context.getPackageName());
                    }
                },
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
                    public void run(Context context, String launcherPackageName) {
                        if (isSupported(launcherPackageName)) {
                            ManualApply.super.run(context, launcherPackageName);
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
        POCO(
                "POCO",
                R.drawable.ic_launcher_poco,
                new String[]{"com.mi.android.globallauncher"},
                "com.miui.home.settings.HomeSettingsActivity",
                DIRECT_APPLY_NOT_SUPPORTED,
                (context, launcherName) -> new String[]{} // FIXME: Opens app without instructions
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
        BLACKBERRY(
                "BlackBerry",
                R.drawable.ic_launcher_blackberry,
                new String[]{"com.blackberry.blackberrylauncher"},
                "com.blackberry.blackberrylauncher.MainActivity",
                DIRECT_APPLY_NOT_SUPPORTED,
                (context, launcherName) -> new String[]{} // FIXME: Opens app without instructions
        ),
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
        NIAGARA(
                "Niagara",
                R.drawable.ic_launcher_niagara,
                new String[]{"bitpit.launcher"},
                NO_SETTINGS_ACTIVITY,
                (context, launcherPackageName) -> new Intent("bitpit.launcher.APPLY_ICONS")
                        .putExtra("packageName", context.getPackageName()),
                MANUAL_APPLY_NOT_SUPPORTED
        ),
        HYPERION(
                "Hyperion",
                R.drawable.ic_launcher_hyperion,
                new String[]{"projekt.launcher"},
                "projekt.launcher.activities.SettingsActivity",
                DIRECT_APPLY_NOT_SUPPORTED,
                (context, launcherName) -> new String[]{} // FIXME: Opens app without instructions
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
        Kvaesitso(
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
        ONEUI(
                "Samsung One UI",
                R.drawable.ic_launcher_one_ui,
                new String[]{"com.sec.android.app.launcher"},
                NO_SETTINGS_ACTIVITY, // TODO: Could link to Theme Park if available
                DIRECT_APPLY_NOT_SUPPORTED,
                new ManualApply() {
                    @Override
                    public String[] getInstructionSteps(Context context, String launcherName) {
                        return null; // Can't use the standard flow
                    }

                    @Override
                    public void run(Context context, String launcherPackageName) {
                        applyOneUI(context, "Samsung One UI");
                    }
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

            default void run(Context context, String launcherPackageName) throws ActivityNotFoundException, NullPointerException {
                final Intent activity = getActivity(context, launcherPackageName);
                final Intent broadcast = getBroadcast(context);
                if (getBroadcast(context) != null) {
                    context.sendBroadcast(broadcast);
                }
                context.startActivity(activity);
                ((AppCompatActivity) context).finish();
            }
        }

        /**
         * Interface for launchers to implement when they support icon packs but not direct apply.
         * They should provide an overall compatibility description and a list of instructions for
         * users to follow step by step on their device. The {@code run()} method should be
         * self-contained and either launch a deep link into the launcher's settings where the icon
         * pack can be applied, or simply display a dialog with the instructions.
         *
         * @see Launcher#showManualApplyDialog(Context, String, String, String, String[], String)
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

            default void run(Context context, String launcherPackageName) throws ActivityNotFoundException, NullPointerException {
                Launcher launcher = getLauncher(launcherPackageName);
                showManualApplyDialog(
                        context,
                        launcherPackageName,
                        launcher.name,
                        getCompatibilityMessage(context, launcher.name),
                        getInstructionSteps(context, launcher.name),
                        getSettingsActivity(context, launcherPackageName)
                );
            }
        }

        /**
         * Exception thrown when the launcher isn't installed on the device. Catch this when you
         * want to provide user-friendly feedback to the user or fall back on other methods such
         * as opening Google Play.
         *
         * @see Launcher#openGooglePlay(Context, String)
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
         * @see Launcher#supportsDirectApply(String)
         */
        public static class LauncherDirectApplyNotSupported extends ActivityNotFoundException {
            public LauncherDirectApplyNotSupported(Throwable cause) {
                super("The launcher does not support direct apply");
                initCause(cause); // preserves the original exceptions information
            }
        }

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

        public final String name;
        public final @DrawableRes
        int icon;
        public final String[] packages;
        public final String settingsActivityName;

        private DirectApply directApplyFunc = null;
        private ManualApply manualApplyFunc = null;

        Launcher() {
            this.name = null;
            this.icon = 0;
            this.packages = null;
            this.settingsActivityName = null;
        }

        Launcher(String name, @DrawableRes int icon, String[] packages, String settingsActivityName, DirectApply directApplyFunc, ManualApply manualApplyFunc) {
            this.name = name;
            this.icon = icon;
            this.packages = packages;
            this.settingsActivityName = settingsActivityName;
            this.directApplyFunc = directApplyFunc;
            this.manualApplyFunc = manualApplyFunc;
        }

        /**
         * Check if the launcher supports direct apply of icon packs. Not all launchers do, and it's
         * on the launcher developers to provide the necessary interfaces to allow this. Note that
         * when you use `applyDirectly` it's still possible for it to throw an exception (see
         * exception `LauncherDirectApplyFailed`) because newer versions or OS-specific variants of
         * the launcher might not support it.
         * Consider the return value of this method as a hint, not a guarantee.
         * @param launcherPackageName The package name of the launcher to check.
         * @return true if the launcher supports direct apply, false otherwise.
         */
        public boolean supportsDirectApply(String launcherPackageName) {
            if (directApplyFunc != null) {
                return directApplyFunc.isSupported(launcherPackageName);
            }
            return false;
        }

        /**
         * Check if the launcher supports applying icon packs manually.
         * @param launcherPackageName The package name of the launcher to check
         * @return true if the launcher supports manual apply, false otherwise
         */
        public boolean supportsManualApply(String launcherPackageName) {
            if (manualApplyFunc != null) {
                return manualApplyFunc.isSupported(launcherPackageName);
            }
            return false;
        }

        /**
         * Check if the launcher supports icon packs. Not all launchers do specifically not those
         * that want to stay close to Vanilla Android.
         * @param launcherPackageName The package name of the launcher to check
         * @return true if the launcher supports icon packs, false otherwise
         */
        public boolean supportsIconPacks(String launcherPackageName) {
            return supportsDirectApply(launcherPackageName) || supportsManualApply(launcherPackageName);
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
         * @param launcherPackageName The package name of the launcher to apply the icon pack to.
         * @throws LauncherNotInstalledException If the launcher isn't installed on the device.
         * @throws LauncherDirectApplyNotSupported If the launcher doesn't support applying icon packs directly.
         * @throws LauncherDirectApplyFailed If the icon pack couldn't be applied to the launcher directly. This is never an expected case. If it happens, it might indicate that the launcher interface changed.
         *
         * @see Launcher#supportsDirectApply(String)
         */
        public void applyDirectly(Context context, String launcherPackageName) throws ActivityNotFoundException, NullPointerException {
            if (!isInstalled(context, launcherPackageName)) throw new LauncherNotInstalledException(new ActivityNotFoundException());
            if (directApplyFunc == null) throw new LauncherDirectApplyNotSupported(new ActivityNotFoundException());
            if (!directApplyFunc.isSupported(launcherPackageName)) throw new LauncherDirectApplyNotSupported(new ActivityNotFoundException());
            try {
                directApplyFunc.run(context, launcherPackageName);
                logLauncherDirectApply(launcherPackageName);
            } catch (Exception e) {
                throw new LauncherDirectApplyFailed(e);
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
         * @param launcherPackageName The package name of the launcher to apply the icon pack to.
         * @param launcherName The name of the launcher to display in the dialog.
         * @throws LauncherNotInstalledException If the launcher isn't installed on the device.
         * @throws LauncherManualApplyNotSupported If the launcher doesn't support applying icon packs manually.
         * @throws LauncherManualApplyFailed If an associated settings activity could not be launched. This is never an expected case. If it happens, it might indicate that the launcher interface changed.
         *
         */
        public void applyManually(Context context, String launcherPackageName, String launcherName) throws ActivityNotFoundException, NullPointerException {
            //if (!isInstalled(context, launcherPackageName)) throw new LauncherNotInstalledException(new ActivityNotFoundException());
            if (manualApplyFunc == null) throw new LauncherManualApplyNotSupported(new ActivityNotFoundException());
            if (!manualApplyFunc.isSupported(launcherPackageName)) throw new LauncherManualApplyNotSupported(new ActivityNotFoundException());

            try {
                manualApplyFunc.run(context, launcherPackageName);
                //logLauncherManualApply(launcherPackageName);
            } catch (Exception e) {
                throw new LauncherManualApplyFailed(e);
            }
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

    @SuppressLint("StringFormatInvalid")
    public static void apply(@NonNull Context context, String packageName, String launcherName) {
        CandyBarApplication.getConfiguration().getAnalyticsHandler().logEvent(
                "click",
                new HashMap<>() {{
                    put("section", "apply");
                    put("action", "open_dialog");
                    put("launcher", packageName);
                }}
        );
        Launcher launcher = getLauncher(packageName);

        if (!launcher.supportsIconPacks(packageName)) {
            launcherIncompatible(context, launcherName);
            return;
        }

        if (!isInstalled(context, packageName) && !launcher.supportsManualApply(packageName)) {
            // FIXME: Not every launcher is on the Play Store
            showInstallPrompt(context, packageName, launcherName);
            return;
        }

        // Try direct apply first
        if (launcher.supportsDirectApply(packageName)) {
            try {
                launcher.applyDirectly(context, packageName);
                return;
            } catch (ActivityNotFoundException | NullPointerException e) { /* No-op */ }
        }

        // Fall back to showing instructions if direct apply failed or isn't supported
        if(launcher.supportsManualApply(packageName)) {
            try {
                launcher.applyManually(context, packageName, launcherName);
                return;
            } catch (ActivityNotFoundException | NullPointerException e) { /* No-op */ }
        }

        // If we're here, it means neither direct nor manual apply worked or are not supported
        launcherIncompatible(context, launcherName);
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

    @SuppressLint("StringFormatInvalid")
    private static void showManualApplyDialog(Context context, String launcherPackageName, String launcherName, String description, String[] steps, String settingsActivityName) {
        boolean isInstalled = isInstalled(context, launcherPackageName);

        int positiveButton = isInstalled ? android.R.string.ok : R.string.install;
        int negativeButton = android.R.string.cancel;

        String installPrompt = context.getResources().getString(R.string.apply_launcher_not_installed, launcherName);
        String activityLaunchFailed = context.getResources().getString(R.string.apply_launch_failed, launcherName);

        String content = ((description == null) ? "" : (description + "\n\n"))
                + ((steps.length > 0) ? "\t• " : "")
                + String.join("\n\t• ", steps) // bullet point list of instructions
                + ((!isInstalled && (steps.length > 0)) ? "\n\n" : "")
                + (isInstalled ? "" : installPrompt); // prompt to install the launcher

        new MaterialDialog.Builder(context)
                .typeface(TypefaceHelper.getMedium(context), TypefaceHelper.getRegular(context))
                .title(launcherName)
                .content(content)
                .positiveText(positiveButton)
                .onPositive((dialog, which) -> {
                    if (isInstalled) {
                        CandyBarApplication.getConfiguration().getAnalyticsHandler().logEvent(
                                "click",
                                new HashMap<>() {{
                                    put("section", "apply");
                                    put("action", "manual_open_confirm");
                                    put("launcher", launcherName);
                                }}
                        );
                        if (settingsActivityName == null) return;
                        try {
                            final Intent intent = new Intent(Intent.ACTION_MAIN);
                            intent.setComponent(new ComponentName(launcherPackageName, settingsActivityName));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                            ((AppCompatActivity) context).finish();
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
                    CandyBarApplication.getConfiguration().getAnalyticsHandler().logEvent(
                            "click",
                            new HashMap<>() {{
                                put("section", "apply");
                                put("action", "manual_open_cancel");
                                put("launcher", launcherName);
                            }}
                    );
                }))
                .show();
    }

    private static void showInstallPrompt(Context context, String launcherPackageName, String launcherName) {
        showManualApplyDialog(
                context,
                launcherPackageName,
                launcherName,
                null,
                new String[]{},
                null
        );
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

    private static void openGooglePlay(Context context, String packageName) {
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
            if (launcher.supportsDirectApply(packageName)) {
                launcher.applyDirectly(context, packageName);
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
