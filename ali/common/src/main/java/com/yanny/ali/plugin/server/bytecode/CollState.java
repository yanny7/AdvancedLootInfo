package com.yanny.ali.plugin.server.bytecode;

import com.yanny.aci.api.NumberExpr;
import org.jetbrains.annotations.Nullable;

final class CollState {
    int removals;
    @Nullable
    int[] randomIndexSet;
    @Nullable
    Value.Draws shuffled;
    @Nullable
    NumberExpr drawCount;
    boolean pool;
    @Nullable
    Value.Offers offers;
}
