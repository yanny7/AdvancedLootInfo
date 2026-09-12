package com.yanny.alicompat.compat.advancedperipherals;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IItemListing;
import de.srendi.advancedperipherals.common.village.VillagerTrade;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VillagerTradeAccessor extends BaseAccessor<VillagerTrade> implements IItemListing {
    private static final float PRICE_MULTIPLIER = 1.0F;

    @FieldAccessor
    private VillagerTrade.Type type;

    @FieldAccessor
    private int emeraldAmount;

    @FieldAccessor
    private int itemAmount;

    @FieldAccessor
    private int maxUses;

    @FieldAccessor
    private int xp;

    @Nullable
    @FieldAccessor
    private ItemLike item;

    @Nullable
    @FieldAccessor
    private ItemStack itemStack;

    public VillagerTradeAccessor(VillagerTrade parent) {
        super(parent);
    }

    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        ItemStack tradedStack = getTradedStack();
        Either<ItemStack, TagKey<? extends ItemLike>> traded = Either.left(tradedStack);
        Either<ItemStack, TagKey<? extends ItemLike>> emeralds = Either.left(new ItemStack(Items.EMERALD, emeraldAmount));
        RangeValue tradedCount = new RangeValue(tradedStack.getCount());
        RangeValue emeraldCount = new RangeValue(emeraldAmount);

        if (type == VillagerTrade.Type.EMERALD_FOR_ITEM) {
            return new ItemsToItemsNode(utils, traded, tradedCount, emeralds, emeraldCount, maxUses, xp, PRICE_MULTIPLIER, conditions);
        }

        return new ItemsToItemsNode(utils, emeralds, emeraldCount, traded, tradedCount, maxUses, xp, PRICE_MULTIPLIER, conditions);
    }

    @NotNull
    private ItemStack getTradedStack() {
        if (itemStack != null) {
            return itemStack;
        }

        if (item != null) {
            return new ItemStack(item, itemAmount);
        }

        return ItemStack.EMPTY;
    }
}
