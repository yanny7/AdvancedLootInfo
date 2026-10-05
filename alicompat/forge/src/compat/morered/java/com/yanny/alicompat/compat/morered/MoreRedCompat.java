package com.yanny.alicompat.compat.morered;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.alicompat.IModCompat;
import commoble.morered.wires.WireCountLootFunction;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MoreRedCompat implements IModCompat {
    private static final NumberExpr WIRE_COUNT = NumberExpr.range(1, 6);

    @NotNull
    @Override
    public String targetModId() {
        return MoreRedLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerFunctionTooltip(WireCountLootFunction.class, MoreRedCompat::wireCountTooltip);
        registry.registerCountModifier(WireCountLootFunction.class, MoreRedCompat::applyWireCount);
    }

    @NotNull
    private static TooltipBuilder wireCountTooltip(IServerUtils ignoredUtils, WireCountLootFunction ignoredFunction) {
        return TooltipBuilder.array(TooltipBuilder::showEmpty, MoreRedLang.Functions.SET_WIRE_COUNT);
    }

    private static NumberExpr applyWireCount(IServerUtils ignoredUtils, WireCountLootFunction ignoredFunction, NumberExpr count, List<TooltipNode> ignoredConditions) {
        return WIRE_COUNT;
    }
}
