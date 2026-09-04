package com.yanny.alicompat.compat.ribbits;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IItemListing;
import com.yungnickyoung.minecraft.ribbits.entity.trade.PotionForAmethyst;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.Nullable;

public class PotionForAmethystAccessor extends BaseAccessor<PotionForAmethyst> implements VillagerTrades.ItemListing, IItemListing {
    @FieldAccessor
    private ItemStack toItem;

    @FieldAccessor
    private int count;

    @FieldAccessor
    private int amethystCostMin;

    @FieldAccessor
    private int amethystCostMax;

    @FieldAccessor
    private int maxUses;

    @FieldAccessor
    private float priceMultiplier;

    @Nullable
    @FieldAccessor
    private Potion potionType;

    public PotionForAmethystAccessor(PotionForAmethyst parent) {
        super(parent);
    }

    @Nullable
    @Override
    public MerchantOffer getOffer(Entity trader, RandomSource random) {
        return parent.getOffer(trader, random);
    }

    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        ItemStack result = new ItemStack(toItem.getItem(), count);
        TooltipNode resultTooltip = TooltipNode.empty();

        if (potionType != null) {
            result = PotionUtils.setPotion(result, potionType);
        } else {
            resultTooltip = TooltipBuilder.keyOnly(RibbitsLang.Functions.RANDOM_POTION).build();
        }

        return new ItemsToItemsNode(
                utils,
                Either.left(Items.AMETHYST_SHARD.getDefaultInstance()),
                new RangeValue(amethystCostMin, amethystCostMax),
                TooltipNode.empty(),
                Either.left(ItemStack.EMPTY),
                new RangeValue(1),
                TooltipNode.empty(),
                Either.left(result),
                new RangeValue(count),
                resultTooltip,
                maxUses,
                0,
                priceMultiplier,
                conditions
        );
    }
}
