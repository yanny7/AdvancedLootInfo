package com.yanny.aci.language;

import org.jetbrains.annotations.NotNull;

public record Translation(String sKey, String pKey, String sEng, String pEng) implements IMultiKey {
    public Translation(String key, String eng) {
        this(key, key, eng, eng);
    }

    @NotNull
    @Override
    public String singular() {
        return sKey;
    }

    @NotNull
    @Override
    public String plural() {
        return pKey;
    }
}
