package com.yanny.alicompat.compat.twilightforest;

import com.yanny.aci.api.RangeValue;
import com.yanny.ali.api.IServerUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.INumberProvider;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import twilightforest.loot.LootingEnchantNumberProvider;

public class LootingEnchantNumberProviderAccessor extends BaseAccessor<LootingEnchantNumberProvider> implements INumberProvider {
    @FieldAccessor
    private Holder<Enchantment> enchantment;

    @FieldAccessor
    private NumberProvider baseValue;

    public LootingEnchantNumberProviderAccessor(LootingEnchantNumberProvider parent) {
        super(parent);
    }

    @Override
    public RangeValue convertNumber(IServerUtils utils) {
        return utils.convertNumber(utils, baseValue).addMax(enchantment.value().getMaxLevel());
    }
}
