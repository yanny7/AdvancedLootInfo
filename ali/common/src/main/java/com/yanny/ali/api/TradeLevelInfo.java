package com.yanny.ali.api;

import com.yanny.aci.api.RangeValue;

public record TradeLevelInfo(RangeValue offers, float chance) {
    public TradeLevelInfo(RangeValue offers) {
        this(offers, 1.0f);
    }
}
