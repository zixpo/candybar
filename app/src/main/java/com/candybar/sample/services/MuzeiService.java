package com.candybar.sample.services;

import candybar.lib.services.CandyBarMuzeiService;

public class MuzeiService extends CandyBarMuzeiService {

    private static final String SOURCE_NAME = "CandyBar:MuzeiArtSource";

    public MuzeiService() {
        super(SOURCE_NAME);
    }
}
