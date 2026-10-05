package com.yanny.ali.api;

import com.yanny.aci.api.NumberExpr;

public record TradeLevelInfo(NumberExpr offers, float chance) {
    public TradeLevelInfo(NumberExpr offers) {
        this(offers, 1.0f);
    }
}
