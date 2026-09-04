package com.yanny.alicompat.compat.ribbits;

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
import com.yungnickyoung.minecraft.ribbits.entity.trade.EnchantedItemForAmethyst;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.jetbrains.annotations.Nullable;

public class EnchantedItemForAmethystAccessor extends BaseAccessor<EnchantedItemForAmethyst> implements VillagerTrades.ItemListing, IItemListing {
    @FieldAccessor
    private ItemStack itemStack;

    @FieldAccessor
    private int amethystCostMin;

    @FieldAccessor
    private int amethystCostMax;

    @FieldAccessor
    private int maxUses;

    @FieldAccessor
    private float priceMultiplier;

    public EnchantedItemForAmethystAccessor(EnchantedItemForAmethyst parent) {
        super(parent);
    }

    @Nullable
    @Override
    public MerchantOffer getOffer(Entity trader, RandomSource random) {
        return parent.getOffer(trader, random);
    }

    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        TooltipNode tooltip = TooltipBuilder.branch((b) -> b
                        .add(utils.getValueTooltip(utils, UniformGenerator.between(5, 19)).build(Lang.Value.LEVELS))
                        .add(utils.getValueTooltip(utils, false).build(Lang.Value.TREASURE))
                )
                .build(Lang.Functions.ENCHANT_WITH_LEVELS);

        return new ItemsToItemsNode(
                utils,
                Either.left(Items.AMETHYST_SHARD.getDefaultInstance()),
                new RangeValue(amethystCostMin, amethystCostMax),
                TooltipNode.empty(),
                Either.left(ItemStack.EMPTY),
                new RangeValue(1),
                TooltipNode.empty(),
                Either.left(itemStack.getItem().getDefaultInstance()),
                new RangeValue(1),
                tooltip,
                maxUses,
                0,
                priceMultiplier,
                conditions
        );
    }
}
