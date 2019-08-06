package com.candybar.sample.activities;

import android.support.annotation.NonNull;

import com.candybar.sample.services.MuzeiService;

import candybar.lib.activities.CandyBarMuzeiActivity;

public class MuzeiActivity extends CandyBarMuzeiActivity {

    @NonNull
    @Override
    public Class<?> onInit() {
        return MuzeiService.class;
    }
}
