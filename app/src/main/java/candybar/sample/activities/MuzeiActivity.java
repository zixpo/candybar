package candybar.sample.activities;

import android.support.annotation.NonNull;

import candybar.lib.activities.CandyBarMuzeiActivity;
import candybar.sample.services.MuzeiService;

public class MuzeiActivity extends CandyBarMuzeiActivity {

    @NonNull
    @Override
    public Class<?> onInit() {
        return MuzeiService.class;
    }
}
