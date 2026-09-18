package com.yanny.alicompat.compat.repurposedstructures;

import com.telepathicgrunt.repurposedstructures.misc.forge.lootmanager.DetectRSLootTables;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IConditionTooltip;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class DetectRSLootTablesAccessor extends BaseAccessor<DetectRSLootTables> implements IConditionTooltip {
    @FieldAccessor
    private Set<ResourceLocation> blacklistedLootTableIds;

    public DetectRSLootTablesAccessor(DetectRSLootTables parent) {
        super(parent);
    }

    @NotNull
    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, blacklistedLootTableIds).build(RepurposedStructuresLang.Branch.BLACKLISTED_LOOT_TABLES));
            b.showEmpty();
        }, RepurposedStructuresLang.Conditions.DETECT_RS_LOOT_TABLES);
    }
}
