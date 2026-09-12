package com.yanny.alicompat.compat.artifacts;

import artifacts.loot.ReplaceWithLootTableFunction;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IFunctionTooltip;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class ReplaceWithLootTableFunctionAccessor extends BaseAccessor<ReplaceWithLootTableFunction> implements IFunctionTooltip {
    @FieldAccessor
    private ResourceLocation lootTable;

    public ReplaceWithLootTableFunctionAccessor(ReplaceWithLootTableFunction parent) {
        super(parent);
    }

    @NotNull
    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, lootTable).build(Lang.Value.LOOT_TABLE));
            b.add(utils.getValueTooltip(utils, parent.predicates).build(Lang.Branch.PREDICATES));
        }, ArtifactsLang.Functions.REPLACE_WITH_LOOT_TABLE);
    }
}
