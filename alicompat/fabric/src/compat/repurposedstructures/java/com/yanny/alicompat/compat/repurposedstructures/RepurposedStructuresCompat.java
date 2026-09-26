package com.yanny.alicompat.compat.repurposedstructures;

import com.yanny.alicompat.IModCompat;
import org.jetbrains.annotations.NotNull;

public class RepurposedStructuresCompat implements IModCompat {
    static final String MOD_ID = "repurposed_structures";

    @NotNull
    @Override
    public String targetModId() {
        return MOD_ID;
    }
}
