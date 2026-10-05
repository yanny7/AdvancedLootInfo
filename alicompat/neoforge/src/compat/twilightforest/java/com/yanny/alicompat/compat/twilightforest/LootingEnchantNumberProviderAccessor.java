package com.yanny.alicompat.compat.twilightforest;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.server.TooltipUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.INumberProvider;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import twilightforest.loot.LootingEnchantNumberProvider;

import java.util.List;

public class LootingEnchantNumberProviderAccessor extends BaseAccessor<LootingEnchantNumberProvider> implements INumberProvider {
    @FieldAccessor
    private Holder<Enchantment> enchantment;

    @FieldAccessor
    private NumberProvider baseValue;

    public LootingEnchantNumberProviderAccessor(LootingEnchantNumberProvider parent) {
        super(parent);
    }

    @Override
    public NumberExpr convertNumber(IServerUtils utils, List<TooltipNode> conditions) {
        return NumberExpr.add(utils.convertNumber(utils, baseValue, conditions), TooltipUtils.level(enchantment));
    }
}
