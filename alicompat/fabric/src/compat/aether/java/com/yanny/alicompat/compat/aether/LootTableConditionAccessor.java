package com.yanny.alicompat.compat.aether;

import com.aetherteam.aether.loot.modifiers.AetherLootTableModifications;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IConditionTooltip;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class LootTableConditionAccessor extends BaseAccessor<AetherLootTableModifications.LootTableCondition> implements IConditionTooltip {
    @FieldAccessor
    private ResourceLocation lootTableId;

    public LootTableConditionAccessor(AetherLootTableModifications.LootTableCondition parent) {
        super(parent);
    }

    @NotNull
    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, lootTableId)), Lang.Conditions.LOOT_TABLE_ID);
    }
}
