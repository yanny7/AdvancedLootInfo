package com.yanny.alicompat.compat.apotheosis;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IIngredientTooltip;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.util.GemIngredient;
import dev.shadowsoffire.placebo.reload.DynamicHolder;

public class GemIngredientAccessor extends BaseAccessor<GemIngredient> implements IIngredientTooltip {
    @FieldAccessor
    private DynamicHolder<LootRarity> rarity;

    public GemIngredientAccessor(GemIngredient parent) {
        super(parent);
    }

    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, rarity).build(ApotheosisLang.Value.RARITY)), ApotheosisLang.Ingredient.GEM);
    }
}
