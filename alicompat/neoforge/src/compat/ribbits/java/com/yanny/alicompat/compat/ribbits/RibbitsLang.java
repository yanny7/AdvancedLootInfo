package com.yanny.alicompat.compat.ribbits;

import com.yanny.alicompat.ICompatTranslations;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class RibbitsLang implements ICompatTranslations {
    static final String MOD_ID = "ribbits";

    public static final Map<String, String> TRANSLATION_MAP = new HashMap<>();

    static {
        TRANSLATION_MAP.put("entity.ribbits.ribbit_nitwit", "Nitwit Ribbit");
        TRANSLATION_MAP.put("entity.ribbits.ribbit_gardener", "Gardener Ribbit");
        TRANSLATION_MAP.put("entity.ribbits.ribbit_sorcerer", "Sorcerer Ribbit");
        TRANSLATION_MAP.put("entity.ribbits.ribbit_fisherman", "Fisherman Ribbit");
        TRANSLATION_MAP.put("entity.ribbits.ribbit_merchant", "Merchant Ribbit");
    }

    @NotNull
    @Override
    public String targetModId() {
        return MOD_ID;
    }

    @NotNull
    @Override
    public Map<String, String> getTranslations() {
        return TRANSLATION_MAP;
    }
}
