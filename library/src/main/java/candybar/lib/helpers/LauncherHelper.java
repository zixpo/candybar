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

    public enum Launcher {
        UNKNOWN,

        ACTION(
                "Action",
                R.drawable.ic_launcher_action,
                new String[]{"com.actionlauncher.playstore", "com.chrislacy.actionlauncher.pro"},
                (context, launcherPackageName) -> context.getPackageManager().getLaunchIntentForPackage(launcherPackageName)
                    .putExtra("apply_icon_pack", context.getPackageName())
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                null
        ),
        ADW(
                "ADW",
                R.drawable.ic_launcher_adw,
                new String[]{"org.adw.launcher", "org.adwfreak.launcher"},
                (context, launcherPackageName) -> new Intent("org.adw.launcher.SET_THEME")
                        .putExtra("org.adw.launcher.theme.NAME", context.getPackageName())
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                null
        ),
        APEX(
                "Apex",
                R.drawable.ic_launcher_apex,
                new String[]{"com.anddoes.launcher", "com.anddoes.launcher.pro"},
                (context, launcherPackageName) -> new Intent("com.anddoes.launcher.SET_THEME")
                        .putExtra("com.anddoes.launcher.THEME_PACKAGE_NAME", context.getPackageName())
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                null
        ),
        BEFORE(
                "Before",
                R.drawable.ic_launcher_before,
                new String[]{"com.beforesoft.launcher"},
                (context, launcherPackageName) -> new Intent("com.beforesoftware.launcher.APPLY_ICONS")
                        .putExtra("packageName", context.getPackageName())
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                null
        ),
        CMTHEME(
                "CM Theme",
                R.drawable.ic_launcher_cm,
                new String[]{"org.cyanogenmod.theme.chooser"},
                (context, launcherPackageName) -> new Intent("android.intent.action.MAIN")
                    .setComponent(new ComponentName(launcherPackageName, "org.cyanogenmod.theme.chooser.ChooserActivity"))
                    .putExtra("pkgName", context.getPackageName()),
                null
        ),
        COLOR_OS(
                "ColorOS",
                R.drawable.ic_launcher_color_os,
                new String[]{"com.oppo.launcher"}),
        GO(
                "GO EX",
                R.drawable.ic_launcher_go,
                new String[]{"com.gau.go.launcherex"},
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
                null
        ),
        HIOS(
                "HiOS",
                R.drawable.ic_launcher_hios,
                new String[]{"com.transsion.hilauncher"}),
        HOLO(
                "Holo",
                R.drawable.ic_launcher_holo,
                new String[]{"com.mobint.hololauncher"}),
        HOLOHD(
                "Holo HD",
                R.drawable.ic_launcher_holohd,
                new String[]{"com.mobint.hololauncher.hd"}),
        LAWNCHAIR(
                "Lawnchair",
                R.drawable.ic_launcher_lawnchair,
                new String[]{"ch.deletescape.lawnchair.plah", "ch.deletescape.lawnchair.ci", "app.lawnchair"},
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
                null
        ),
        LGHOME(
                "LG Home",
                R.drawable.ic_launcher_lg,
                new String[]{"com.lge.launcher2", "com.lge.launcher3"}),
        LUCID(
                "Lucid",
                R.drawable.ic_launcher_lucid,
                new String[]{"com.powerpoint45.launcher"},
                (context, launcherPackageName) -> new Intent("com.powerpoint45.action.APPLY_THEME", null)
                        .putExtra("icontheme", context.getPackageName()),
                null
        ),
        NOTHING(
                "Nothing",
                R.drawable.ic_launcher_nothing,
                new String[]{"com.nothing.launcher"}),
        NOUGAT(
                "Nougat",
                R.drawable.ic_launcher_nougat,
                new String[]{"me.craftsapp.nlauncher"},
                (context, launcherPackageName) -> new Intent("me.craftsapp.nlauncher")
                    .setAction("me.craftsapp.nlauncher.SET_THEME")
                    .putExtra("me.craftsapp.nlauncher.theme.NAME", context.getPackageName())
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                null
        ),
        NOVA(
                "Nova",
                R.drawable.ic_launcher_nova,
                new String[]{"com.teslacoilsw.launcher", "com.teslacoilsw.launcher.prime"},
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
                null
        ),
        OXYGEN_OS(
                "OxygenOS",
                R.drawable.ic_launcher_oxygen_os,
                new String[]{"net.oneplus.launcher"}),
        PIXEL(
                "Pixel",
                R.drawable.ic_launcher_pixel,
                new String[]{"com.google.android.apps.nexuslauncher"}),
        PROJECTIVY(
                "Projectivy",
                R.drawable.ic_launcher_projectivy,
                new String[]{"com.spocky.projengmenu"},
                (context, launcherPackageName) -> new Intent("com.spocky.projengmenu.APPLY_ICONPACK")
                        .setPackage("com.spocky.projengmenu")
                        .putExtra("com.spocky.projengmenu.extra.ICONPACK_PACKAGENAME", context.getPackageName())
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                null
        ),
        SMART(
                "Smart",
                R.drawable.ic_launcher_smart,
                new String[]{"ginlemon.flowerfree", "ginlemon.flowerpro", "ginlemon.flowerpro.special"},
                (context, launcherPackageName) -> new Intent("ginlemon.smartlauncher.setGSLTHEME")
                        .putExtra("package", context.getPackageName())
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                null
        ),
        SOLO(
                "Solo",
                R.drawable.ic_launcher_solo,
                new String[]{"home.solo.launcher.free"},
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
                null
        ),
        STOCK_LEGACY(
                /*
                 * Historically, ColorOS, OxygenOS and realme UI were standalone launcher variants
                 * but as of Android 12 they're slowly being merged into a single launcher that no
                 * longer reports as e.g. "com.oppo.launcher" but now as "com.android.launcher".
                 */
                isColorOS() ? "ColorOS" : isRealmeUI() ? "realme UI" : "Stock Launcher",
                isColorOS() ? R.drawable.ic_launcher_color_os : isRealmeUI() ? R.drawable.ic_launcher_realme_ui : R.drawable.ic_launcher_android,
                new String[]{"com.android.launcher"}),
        POCO(
                "POCO",
                R.drawable.ic_launcher_poco,
                new String[]{"com.mi.android.globallauncher"}),
        MOTO(
                "Moto Launcher",
                R.drawable.ic_launcher_moto,
                new String[]{"com.motorola.launcher3"}),
        MICROSOFT(
                "Microsoft",
                R.drawable.ic_launcher_microsoft,
                new String[]{"com.microsoft.launcher"}),
        BLACKBERRY(
                "BlackBerry",
                R.drawable.ic_launcher_blackberry,
                new String[]{"com.blackberry.blackberrylauncher"}),
        FLICK(
                "Flick",
                R.drawable.ic_launcher_flick,
                new String[]{"com.universallauncher.universallauncher"},
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
                }, null
        ),
        SQUARE(
                "Square",
                R.drawable.ic_launcher_square,
                new String[]{"com.ss.squarehome2"},
                (context, launcherPackageName) -> new Intent("com.ss.squarehome2.ACTION_APPLY_ICONPACK")
                        .setComponent(ComponentName.unflattenFromString("com.ss.squarehome2/.ApplyThemeActivity"))
                        .putExtra("com.ss.squarehome2.EXTRA_ICONPACK", context.getPackageName()),
                null
        ),
        NIAGARA(
                "Niagara",
                R.drawable.ic_launcher_niagara,
                new String[]{"bitpit.launcher"},
                (context, launcherPackageName) -> new Intent("bitpit.launcher.APPLY_ICONS")
                        .putExtra("packageName", context.getPackageName()),
                null
        ),
        HYPERION(
                "Hyperion",
                R.drawable.ic_launcher_hyperion,
                new String[]{"projekt.launcher"}),
        KISS(
                "KISS",
                R.drawable.ic_launcher_kiss,
                new String[]{"fr.neamar.kiss"}),
        Kvaesitso(
                "Kvaesitso",
                R.drawable.ic_launcher_kvaesitso,
                new String[]{"de.mm20.launcher2.release"}),
        ONEUI(
                "Samsung One UI",
                R.drawable.ic_launcher_one_ui,
                new String[]{"com.sec.android.app.launcher"}),
        TINYBIT(
                "TinyBit",
                R.drawable.ic_launcher_tinybit,
                new String[]{"rocks.tbog.tblauncher"}),
        ZENUI(
                "ZenUI",
                R.drawable.ic_launcher_zenui,
                new String[]{"com.asus.launcher"},
                (context, launcherPackageName) -> new Intent("com.asus.launcher")
                        .setAction("com.asus.launcher.intent.action.APPLY_ICONPACK")
                        .addCategory(Intent.CATEGORY_DEFAULT)
                        .putExtra("com.asus.launcher.iconpack.PACKAGE_NAME", context.getPackageName())
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                null
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
         * @see Launcher#applyWithInstructions(Context, String, String, String[])
         */
        private interface ManualApply {
            default boolean isSupported(String packageName) { return true; }

            default Intent getSettingsActivity(Context context, String launcherPackageName) { return null; }

            default String getCompatibilityMessage(Context context, String launcherName) {
                return context.getResources().getString(
                        R.string.apply_manual,
                        launcherName,
                        context.getResources().getString(R.string.app_name)
                );
            }

            String[] getInstructionSteps(Context context, String launcherName);

            default void run(Context context, String launcherPackageName, String launcherName) throws ActivityNotFoundException, NullPointerException {
                applyWithInstructions(
                        context,
                        launcherName,
                        getCompatibilityMessage(context, launcherName),
                        getInstructionSteps(context, launcherName),
                        getSettingsActivity(context, launcherPackageName)
                );
            }
        }

        /**
         * Exception thrown when the launcher isn't installed on the device. Catch this when you
         * want to provide user-friendly feedback to the user or fall back on other methods such
         * as opening Google Play. If you want to open Google Play as a default, take note of the
         * overloaded method `applyDirectly` that accepts a boolean parameter for opening Google
         * Play upon error.
         *
         * @see Launcher#applyDirectly(Context, String, boolean)
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
        private DirectApply directApplyFunc = null;
        private ManualApply manualApplyFunc = null;

        Launcher() {
            this.name = null;
            this.icon = 0;
            this.packages = null;
        }

        Launcher(String name, @DrawableRes int icon, String[] packages) {
            this.name = name;
            this.icon = icon;
            this.packages = packages;
        }

        Launcher(String name, @DrawableRes int icon, String[] packages, DirectApply directApplyFunc, ManualApply manualApplyFunc) {
            this.name = name;
            this.icon = icon;
            this.packages = packages;
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
            return !supportsDirectApply(launcherPackageName) && !supportsManualApply(launcherPackageName);
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
         * Try to apply the icon pack directly. In case of any errors, open launcher in Google Play.
         * This is a convenience method to preserve backwards compatibility in CandyBar. If you
         * rather handle exceptions yourself, use `applyDirectly` without the boolean parameter
         * and catch exceptions `LauncherNotInstalledException`, `LauncherDirectApplyFailed` and
         * `LauncherDirectApplyNotSupported`.
         *
         * @param launcherPackageName The package name of the launcher to apply the icon pack to.
         * @param openGooglePlayUponError If true, open Google Play if the launcher isn't installed.
         */
        public void applyDirectly(Context context, String launcherPackageName, boolean openGooglePlayUponError) throws ActivityNotFoundException, NullPointerException {
            try {
                applyDirectly(context, launcherPackageName);
            } catch (ActivityNotFoundException | NullPointerException e) {
                if (openGooglePlayUponError) {
                    openGooglePlay(context, launcherPackageName, name);
                } else {
                    throw e;
                }
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
            if (!isInstalled(context, launcherPackageName)) throw new LauncherNotInstalledException(new ActivityNotFoundException());
            if (manualApplyFunc == null) throw new LauncherManualApplyNotSupported(new ActivityNotFoundException());
            if (!manualApplyFunc.isSupported(launcherPackageName)) throw new LauncherManualApplyNotSupported(new ActivityNotFoundException());

            try {
                manualApplyFunc.run(context, launcherPackageName, launcherName);
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

    public static void apply(@NonNull Context context, String packageName, String launcherName) {
        CandyBarApplication.getConfiguration().getAnalyticsHandler().logEvent(
                "click",
                new HashMap<>() {{
                    put("section", "apply");
                    put("action", "open_dialog");
                    put("launcher", packageName);
                }}
        );
        applyLauncher(context, packageName, launcherName, getLauncher(packageName));
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

    private static void applyLauncher(@NonNull Context context, String launcherPackage, String launcherName, Launcher launcher) {
        switch (launcher) {
            case ACTION:
                launcher.applyDirectly(context, launcherPackage, true);
                break;
            case ADW:
                launcher.applyDirectly(context, launcherPackage, true);
                break;
            case APEX:
                launcher.applyDirectly(context, launcherPackage, true);
                break;
            case BEFORE:
                try {
                    launcher.applyDirectly(context, launcherPackage, false);
                } catch (ActivityNotFoundException | NullPointerException e) {
                    applyWithInstructions(
                            context,
                            launcherName,
                            context.getResources().getString(R.string.apply_manual_before),
                            new String[]{
                                    context.getResources().getString(R.string.apply_manual_before_step_1),
                                    context.getResources().getString(R.string.apply_manual_before_step_2),
                                    context.getResources().getString(R.string.apply_manual_before_step_3),
                                    context.getResources().getString(R.string.apply_manual_before_step_4),
                                    context.getResources().getString(
                                            R.string.apply_manual_before_step_5,
                                            context.getResources().getString(R.string.app_name)
                                    )
                            }
                    );
                }
                break;
            case BLACKBERRY:
                applyManualInApp(context, launcherPackage, launcherName, "com.blackberry.blackberrylauncher.MainActivity");
                break;
            case CMTHEME:
                try {
                    launcher.applyDirectly(context, launcherPackage, false);
                } catch (ActivityNotFoundException | NullPointerException e) {
                    Toast.makeText(context, R.string.apply_cmtheme_not_available,
                            Toast.LENGTH_LONG).show();
                } catch (SecurityException | IllegalArgumentException e) {
                    Toast.makeText(context, R.string.apply_cmtheme_failed,
                            Toast.LENGTH_LONG).show();
                }
                break;
            case COLOR_OS:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    applyWithInstructions(
                            context,
                            launcherName,
                            context.getResources().getString(
                                    R.string.apply_manual,
                                    launcherName,
                                    context.getResources().getString(R.string.app_name)
                            ),
                            new String[]{
                                    context.getResources().getString(R.string.apply_manual_color_os_step_1),
                                    context.getResources().getString(R.string.apply_manual_color_os_step_2),
                                    context.getResources().getString(R.string.apply_manual_color_os_step_3),
                                    context.getResources().getString(
                                            R.string.apply_manual_color_os_step_4,
                                            context.getResources().getString(R.string.app_name)
                                    ),
                            }
                    );
                } else {
                    launcherIncompatibleCustomMessage(
                            context,
                            launcherName,
                            context.getResources().getString(
                                    R.string.apply_launcher_incompatible_depending_on_version, launcherName, 10
                            )
                    );
                }
                break;
            case OXYGEN_OS:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    applyWithInstructions(
                            context,
                            launcherName,
                            context.getResources().getString(
                                    R.string.apply_manual,
                                    launcherName,
                                    context.getResources().getString(R.string.app_name)
                            ),
                            new String[]{
                                    context.getResources().getString(R.string.apply_manual_oxygen_os_step_1),
                                    context.getResources().getString(R.string.apply_manual_oxygen_os_step_2),
                                    context.getResources().getString(R.string.apply_manual_oxygen_os_step_3),
                                    context.getResources().getString(
                                            R.string.apply_manual_oxygen_os_step_4,
                                            context.getResources().getString(R.string.app_name)
                                    ),
                            }
                    );
                } else {
                    launcherIncompatibleCustomMessage(
                            context,
                            launcherName,
                            context.getResources().getString(
                                    R.string.apply_launcher_incompatible_depending_on_version, launcherName, 8
                            )
                    );
                }
                break;
            case FLICK:
                launcher.applyDirectly(context, launcherPackage, true);
                break;
            case GO:
                launcher.applyDirectly(context, launcherPackage, true);
                break;
            case HIOS:
                applyWithInstructions(
                        context,
                        launcherName,
                        context.getResources().getString(R.string.apply_manual_hios),
                        new String[]{
                                context.getResources().getString(R.string.apply_manual_hios_step_1),
                                context.getResources().getString(R.string.apply_manual_hios_step_2),
                                context.getResources().getString(R.string.apply_manual_hios_step_3),
                                context.getResources().getString(R.string.apply_manual_hios_step_4),
                                context.getResources().getString(
                                        R.string.apply_manual_hios_step_5,
                                        context.getResources().getString(R.string.app_name)
                                )
                        }
                );
                break;
            case HOLO:
            case HOLOHD:
                applyManualInApp(context, launcherPackage, launcherName, "com.mobint.hololauncher.SettingsActivity");
                break;
            case HYPERION:
                applyManualInApp(context, launcherPackage, launcherName, "projekt.launcher.activities.SettingsActivity");
                break;
            case KISS:
                applyWithInstructions(
                        context,
                        launcherName,
                        context.getResources().getString(R.string.apply_manual_kiss),
                        new String[]{
                                context.getResources().getString(R.string.apply_manual_kiss_step_1),
                                context.getResources().getString(R.string.apply_manual_kiss_step_2),
                                context.getResources().getString(R.string.apply_manual_kiss_step_3),
                                context.getResources().getString(
                                        R.string.apply_manual_kiss_step_4,
                                        context.getResources().getString(R.string.app_name)
                                ),
                        }
                );
                break;
            case Kvaesitso:
                applyWithInstructions(
                        context,
                        launcherName,
                        context.getResources().getString(R.string.apply_manual_kvaesitso),
                        new String[]{
                            context.getResources().getString(R.string.apply_manual_kvaesitso_step_1),
                            context.getResources().getString(R.string.apply_manual_kvaesitso_step_2),
                            context.getResources().getString(R.string.apply_manual_kvaesitso_step_3),
                            context.getResources().getString(
                                    R.string.apply_manual_kvaesitso_step_4,
                                    context.getResources().getString(R.string.app_name)
                            ),
                            context.getResources().getString(R.string.apply_manual_kvaesitso_step_5),
                        }
                );
                break;
            case LAWNCHAIR:
                if (launcher.supportsDirectApply(launcherPackage)) {
                    launcher.applyDirectly(context, launcherPackage, true);
                } else {
                    applyManualInApp(context, launcherPackage, launcherName, "app.lawnchair.ui.preferences.PreferenceActivity");
                }
                break;
            case LGHOME:
                launcherIncompatible(context, launcherName);
                break;
            case LUCID:
                launcher.applyDirectly(context, launcherPackage, true);
                break;
            case MICROSOFT:
                applyManualInApp(context, launcherPackage, launcherName, null);
                break;
            case NIAGARA:
                launcher.applyDirectly(context, launcherPackage, true);
                break;
            case NOTHING:
                applyWithInstructions(
                        context,
                        launcherName,
                        context.getResources().getString(
                                R.string.apply_manual,
                                launcherName,
                                context.getResources().getString(R.string.app_name)
                        ),
                        new String[]{
                                context.getResources().getString(R.string.apply_manual_nothing_step_1),
                                context.getResources().getString(R.string.apply_manual_nothing_step_2),
                                context.getResources().getString(R.string.apply_manual_nothing_step_3),
                                context.getResources().getString(
                                        R.string.apply_manual_nothing_step_4,
                                        context.getResources().getString(R.string.app_name)
                                ),
                        }
                );
                break;
            case NOVA:
                launcher.applyDirectly(context, launcherPackage, true);
                break;
            case PIXEL:
                launcherIncompatible(context, launcherName);
                break;
            case POCO:
                applyManualInApp(context, launcherPackage, launcherName, "com.miui.home.settings.HomeSettingsActivity");
                break;
            case PROJECTIVY:
                launcher.applyDirectly(context, launcherPackage, true);
                break;
            case ONEUI:
                applyOneUI(context, launcherName);
                break;
            case TINYBIT:
                applyManualInApp(context, launcherPackage, launcherName, "rocks.tbog.tblauncher.SettingsActivity");
                break;
            case MOTO:
                applyManualInApp(context, launcherPackage, launcherName, "com.motorola.personalize.app.IconPacksActivity");
                break;
            case SMART:
                launcher.applyDirectly(context, launcherPackage, true);
                break;
            case SOLO:
                launcher.applyDirectly(context, launcherPackage, true);
                break;
            case SQUARE:
                launcher.applyDirectly(context, launcherPackage, true);
                break;
            case STOCK_LEGACY:
                applyWithInstructions(
                        context,
                        launcherName,
                        context.getResources().getString(
                                R.string.apply_manual,
                                launcherName,
                                context.getResources().getString(R.string.app_name)
                        ),
                        new String[]{
                                context.getResources().getString(R.string.apply_manual_color_os_step_1),
                                context.getResources().getString(R.string.apply_manual_color_os_step_2),
                                context.getResources().getString(R.string.apply_manual_color_os_step_3),
                                context.getResources().getString(
                                        R.string.apply_manual_color_os_step_4,
                                        context.getResources().getString(R.string.app_name)
                                ),
                        }
                );
                break;
            case NOUGAT:
                launcher.applyDirectly(context, launcherPackage, true);
                break;
            case ZENUI:
                launcher.applyDirectly(context, launcherPackage, true);
                break;
        }
    }

    @SuppressLint("StringFormatInvalid")
    private static void applyManualInApp(Context context, String launcherPackage, String launcherName, String activity) {
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
                                new HashMap<>() {{
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
                                new HashMap<>() {{
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

    private static void applyWithInstructions(Context context, String launcherName, String description, String[] steps) {
        new MaterialDialog.Builder(context)
                .typeface(TypefaceHelper.getMedium(context), TypefaceHelper.getRegular(context))
                .title(launcherName)
                .content(description + ((steps.length > 0) ? "\n\n\t• " : "") + String.join("\n\t• ", steps))
                .positiveText(android.R.string.ok)
                .onPositive((dialog, which) -> {
                    CandyBarApplication.getConfiguration().getAnalyticsHandler().logEvent(
                            "click",
                            new HashMap<>() {{
                                put("section", "apply");
                                put("action", "manual_open_confirm");
                                put("launcher", launcherName);
                            }}
                    );
                })
                .negativeText(android.R.string.cancel)
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
