package candybar.sample.activities;

import android.support.annotation.NonNull;

import candybar.lib.activities.CandyBarSplashActivity;
import candybar.lib.activities.configurations.SplashScreenConfiguration;
import candybar.sample.R;

public class SplashActivity extends CandyBarSplashActivity {

    @NonNull
    @Override
    public SplashScreenConfiguration onInit() {
        return new SplashScreenConfiguration(MainActivity.class)
                .setBottomText(getString(R.string.splash_screen_title));
    }
}
