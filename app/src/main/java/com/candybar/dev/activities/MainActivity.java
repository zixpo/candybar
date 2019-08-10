package com.candybar.dev.activities;

import androidx.annotation.NonNull;

import com.candybar.dev.licenses.License;

import candybar.lib.activities.CandyBarMainActivity;
import candybar.lib.activities.configurations.ActivityConfiguration;

public class MainActivity extends CandyBarMainActivity {

    @NonNull
    @Override
    public ActivityConfiguration onInit() {
        return new ActivityConfiguration()
                .setLicenseCheckerEnabled(License.isLicenseCheckerEnabled())
                .setLicenseKey(License.getLicenseKey())
                .setRandomString(License.getRandomString())
                .setDonationProductsId(License.getDonationProductsId())
                .setPremiumRequestProducts(License.getPremiumRequestProductsId(), License.getPremiumRequestProductsCount());
    }
}
