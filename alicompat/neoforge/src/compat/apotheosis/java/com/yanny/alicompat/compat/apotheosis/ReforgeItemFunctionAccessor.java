package com.yanny.alicompat.compat.apotheosis;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IFunctionTooltip;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.functions.ReforgeItemFunction;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class ReforgeItemFunctionAccessor extends BaseAccessor<ReforgeItemFunction> implements IFunctionTooltip {
    @FieldAccessor
    private Set<DynamicHolder<LootRarity>> rarities;

    public ReforgeItemFunctionAccessor(ReforgeItemFunction parent) {
        super(parent);
    }

    @NotNull
    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, rarities).build(ApotheosisLang.Branch.RARITY));
            b.add(utils.getValueTooltip(utils, parent.predicates).build(Lang.Branch.PREDICATES));
        }, ApotheosisLang.Functions.REFORGE_ITEM);
    }
}
