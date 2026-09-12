package com.yanny.alicompat.compat.appliedcooking;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.alicompat.accessor.ConditionalFunction;
import com.yanny.alicompat.accessor.IFunctionTooltip;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import org.jetbrains.annotations.NotNull;

public class KitchenStationBlockLootFunctionAccessor extends ConditionalFunction implements IFunctionTooltip {
    public KitchenStationBlockLootFunctionAccessor(LootItemConditionalFunction conditionalFunction) {
        super(conditionalFunction);
    }

    @NotNull
    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, predicates).build(Lang.Branch.PREDICATES));
            b.showEmpty();
        }, AppliedCookingLang.Functions.KITCHEN_STATION);
    }
}
