package com.yanny.alicompat.compat.portinglib;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.glm.Destination;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IConditionTooltip;
import com.yanny.alicompat.accessor.IDestination;
import io.github.fabricators_of_create.porting_lib.loot.LootTableIdCondition;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class LootTableIdConditionAccessor extends BaseAccessor<LootTableIdCondition> implements IConditionTooltip, IDestination {
    @FieldAccessor
    private ResourceLocation targetLootTableId;

    public LootTableIdConditionAccessor(LootTableIdCondition parent) {
        super(parent);
    }

    @NotNull
    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, targetLootTableId)), Lang.Conditions.LOOT_TABLE_ID);
    }

    @NotNull
    @Override
    public Destination getDestination(IServerUtils utils) {
        return new Destination.Table(targetLootTableId::equals, true);
    }
}
