package com.yanny.alicompat.compat.supplementaries;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IFunctionTooltip;
import net.mehvahdjukaar.supplementaries.common.items.loot.RandomEnchantFunction;
import net.minecraft.core.HolderSet;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.NotNull;

public class RandomEnchantFunctionAccessor extends BaseAccessor<RandomEnchantFunction> implements IFunctionTooltip {
    @FieldAccessor
    private double chance;
    @FieldAccessor
    private HolderSet<Enchantment> curses;

    public RandomEnchantFunctionAccessor(RandomEnchantFunction parent) {
        super(parent);
    }

    @NotNull
    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, chance).build(Lang.Value.CHANCE));
            b.add(utils.getValueTooltip(utils, curses).build(Lang.Branch.ENCHANTMENTS));
            b.add(utils.getValueTooltip(utils, parent.predicates).build(Lang.Branch.PREDICATES));
        }, SupplementariesLang.Functions.CURSE_LOOT);
    }
}
