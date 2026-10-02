package com.yanny.ali.plugin.server;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipNode;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record LootCount(NumberExpr value, List<TooltipNode> conditions) {
    public LootCount {
        conditions = List.copyOf(conditions);
    }

    @NotNull
    public static LootCount of(NumberExpr value) {
        return new LootCount(value, List.of());
    }
}
