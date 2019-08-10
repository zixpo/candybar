package com.candybar.dev.activities;

import androidx.annotation.NonNull;

import com.candybar.dev.services.MuzeiService;

import candybar.lib.activities.CandyBarMuzeiActivity;

public class MuzeiActivity extends CandyBarMuzeiActivity {

    @NonNull
    @Override
    public Class<?> onInit() {
        return MuzeiService.class;
    }
}
