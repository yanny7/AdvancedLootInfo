package com.yanny.alicompat.compat.refinedstorage;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.alicompat.accessor.ConditionalFunction;
import com.yanny.alicompat.accessor.IFunctionTooltip;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import org.jetbrains.annotations.NotNull;

public class ControllerLootFunctionAccessor extends ConditionalFunction implements IFunctionTooltip {
    public ControllerLootFunctionAccessor(LootItemConditionalFunction conditionalFunction) {
        super(conditionalFunction);
    }

    @NotNull
    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, predicates).build(Lang.Branch.PREDICATES));
            b.showEmpty();
        }, RefinedStorageLang.Functions.COPY_STORED_ENERGY);
    }
}
