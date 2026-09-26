package com.yanny.alicompat.compat.sawmill;

import com.yanny.alicompat.IModCompat;
import org.jetbrains.annotations.NotNull;

public class SawmillCompat implements IModCompat {
    static final String MOD_ID = "sawmill";

    @NotNull
    @Override
    public String targetModId() {
        return MOD_ID;
    }
}
