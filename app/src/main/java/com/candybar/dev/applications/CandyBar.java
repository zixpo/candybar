package com.candybar.dev.applications;

import androidx.annotation.NonNull;

import com.candybar.dev.R;

import candybar.lib.applications.CandyBarApplication;

// TODO: Remove `//` below to enable OneSignal
//import com.onesignal.OneSignal;

public class CandyBar extends CandyBarApplication {

    // TODO: Remove `/*` and `*/` below to Enable OneSignal
    /*
    @Override
    public void onCreate() {
        super.onCreate();

        // OneSignal Initialization
        OneSignal.initWithContext(this);
        OneSignal.setAppId("YOUR_ONESIGNAL_APP_ID_HERE");
    }
    */

    @NonNull
    @Override
    public Class<?> getDrawableClass() {
        return R.drawable.class;
    }

    @NonNull
    @Override
    public Configuration onInit() {
        Configuration configuration = new Configuration();

        configuration.setGenerateAppFilter(true);
        configuration.setGenerateAppMap(true);
        configuration.setGenerateThemeResources(true);
        configuration.setNavigationIcon(NavigationIcon.STYLE_4);
        configuration.setOtherApps(new OtherApp[]{
                new OtherApp(
                        "icon_1",
                        "App 1",
                        "Another app #1",
                        "https://play.google.com/store/apps/details?id=app.1"),
                new OtherApp(
                        "icon_2",
                        "App 2",
                        "Another app #2",
                        "https://play.google.com/store/apps/details?id=app.2")
        });

        /*configuration.setDonationLinks(new DonationLink[]{
                new DonationLink(
                        "icon_52",
                        "Donation Link 1",
                        "Donate me!",
                        "https://example.com"
                ),
                new DonationLink(
                        "icon_65",
                        "Donation Link 2",
                        "Donate me from this if the other one doesn't work",
                        "https://example.com"
                )
        });*/

        // configuration.setFilterRequestHandler((request) -> !request.getPackageName().startsWith("org.chromium.webapk"));

        return configuration;
    }
}
