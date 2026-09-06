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
import net.minecraft.core.HolderSet;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;

public class EnchantedItemTradeAccessor extends BaseAccessor<EnchantedItemTrade> implements IItemListing {
    @FieldAccessor
    private TradeItem firstInput;

    @FieldAccessor
    private TradeItem secondInput;

    @FieldAccessor
    private ItemStack itemToEnchant;

    @FieldAccessor
    private IntProvider enchantLevels;

    @FieldAccessor
    private Either<TagKey<Enchantment>, HolderSet<Enchantment>> tradeableEnchantments;

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
        ItemStack result = itemToEnchant.is(Items.BOOK) ? Items.ENCHANTED_BOOK.getDefaultInstance() : itemToEnchant.copy();
        TooltipNode tooltip = TooltipBuilder.branch((b) -> {
            b.add(utils.getValueTooltip(utils, new RangeValue(enchantLevels.getMinValue(), enchantLevels.getMaxValue())).build(Lang.Value.LEVELS));
            tradeableEnchantments.ifLeft((tag) -> b.add(utils.getValueTooltip(utils, tag).build(Lang.Branch.ENCHANTMENTS)));
            tradeableEnchantments.ifRight((enchantments) -> b.add(utils.getValueTooltip(utils, enchantments).build(Lang.Branch.ENCHANTMENTS)));
        }).build(Lang.Functions.ENCHANT_WITH_LEVELS);

        return new ItemsToItemsNode(
                utils,
                Either.left(first.getStack()),
                first.getCount(),
                TooltipNode.empty(),
                Either.left(second.getStack()),
                second.getCount(),
                TooltipNode.empty(),
                Either.left(result),
                new RangeValue(result.getCount()),
                tooltip,
                maxUses,
                villagerExperience,
                priceMultiplier,
                conditions
        );
    }
}
