package com.yanny.alicompat.compat.morejs;

import com.almostreliable.morejs.features.villager.TradeItem;
import com.almostreliable.morejs.features.villager.trades.EnchantedItemTrade;
import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IItemListing;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;

public class EnchantedItemTradeAccessor extends BaseAccessor<EnchantedItemTrade> implements IItemListing {
    @FieldAccessor
    private TradeItem firstInput;

    @FieldAccessor
    private TradeItem secondInput;

    @FieldAccessor
    private Item itemToEnchant;

    @FieldAccessor
    private List<Enchantment> enchantments;

    @FieldAccessor
    private int minEnchantmentAmount;

    @FieldAccessor
    private int maxEnchantmentAmount;

    @FieldAccessor
    private int maxUses;

    @FieldAccessor
    private int villagerExperience;

    @FieldAccessor
    private float priceMultiplier;

    public EnchantedItemTradeAccessor(EnchantedItemTrade parent) {
        super(parent);
    }

    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        TradeItemAccessor first = TradeItemAccessor.of(firstInput);
        TradeItemAccessor second = TradeItemAccessor.of(secondInput);
        ItemStack result = new ItemStack(itemToEnchant.equals(Items.BOOK) ? Items.ENCHANTED_BOOK : itemToEnchant);
        TooltipNode tooltip = TooltipBuilder.branch((b) -> {
            b.add(utils.getValueTooltip(utils, new RangeValue(minEnchantmentAmount, maxEnchantmentAmount)).build(Lang.Value.AMOUNT));

            if (enchantments.size() < BuiltInRegistries.ENCHANTMENT.size()) {
                enchantments.forEach((enchantment) -> b.add(utils.getValueTooltip(utils, enchantment).build(Lang.Value.ENCHANTMENT)));
            }
        }).build(Lang.Functions.ENCHANT_RANDOMLY);

        return new ItemsToItemsNode(
                utils,
                Either.left(first.getStack()),
                first.getCount(),
                TooltipNode.empty(),
                Either.left(second.getStack()),
                second.getCount(),
                TooltipNode.empty(),
                Either.left(result),
                new RangeValue(1),
                tooltip,
                maxUses,
                villagerExperience,
                priceMultiplier,
                conditions
        );
    }
}
