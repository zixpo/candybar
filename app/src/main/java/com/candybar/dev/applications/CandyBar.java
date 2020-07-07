package com.candybar.dev.applications;

import androidx.annotation.NonNull;

import candybar.lib.applications.CandyBarApplication;

//import com.onesignal.OneSignal

public class CandyBar extends CandyBarApplication {

    // Remove '/*' and '*/' to Enable OneSignal
    /*
    @Override
    public void onCreate() {
        super.onCreate();

        // OneSignal Initialization
        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();
    }
    */

    @NonNull
    @Override
    public Configuration onInit() {
        Configuration configuration = new Configuration();

        configuration.setGenerateAppFilter(true);
        configuration.setGenerateAppMap(true);
        configuration.setGenerateThemeResources(true);
        configuration.setNavigationIcon(NavigationIcon.STYLE_4);

        return configuration;
    }
}
