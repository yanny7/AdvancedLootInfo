package com.yanny.alicompat.compat.immersiveengineering;

import blusunrize.immersiveengineering.common.world.Villages;
import com.mojang.datafixers.util.Either;
import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.nodes.MissingNode;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.ali.plugin.server.MissingTooltipUtils;
import com.yanny.alicompat.Utils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.ClassAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IItemListing;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.lang.reflect.Field;

@ClassAccessor("blusunrize.immersiveengineering.common.world.Villages$TradeListing")
public class TradeListingAccessor extends BaseAccessor<VillagerTrades.ItemListing> implements IItemListing {
    private static final Logger LOGGER = CommonLogUtils.getLogger(Utils.MOD_ID);
    private static final Object SELL_FOR_ONE_EMERALD = getOutline("SELL_FOR_ONE_EMERALD");
    private static final Object SELL_FOR_MANY_EMERALDS = getOutline("SELL_FOR_MANY_EMERALDS");
    private static final Object BUY_FOR_ONE_EMERALD = getOutline("BUY_FOR_ONE_EMERALD");
    private static final Object BUY_FOR_MANY_EMERALDS = getOutline("BUY_FOR_MANY_EMERALDS");

    @FieldAccessor
    private Object outline;

    @FieldAccessor(clazz = LazyItemStackAccessor.class)
    private LazyItemStackAccessor lazyItem;

    @FieldAccessor
    private int price;

    @FieldAccessor
    private int maxUses;

    @FieldAccessor
    private int xp;

    @FieldAccessor
    private float priceMultiplier;

    public TradeListingAccessor(VillagerTrades.ItemListing parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        Either<ItemStack, TagKey<? extends ItemLike>> item = lazyItem.getItem();

        if (isOutline(SELL_FOR_ONE_EMERALD)) {
            return getNode(utils, item, new RangeValue(price), emerald(), new RangeValue(1), conditions);
        }

        if (isOutline(SELL_FOR_MANY_EMERALDS)) {
            return getNode(utils, item, new RangeValue(1), emerald(), new RangeValue(price), conditions);
        }

        if (isOutline(BUY_FOR_ONE_EMERALD)) {
            return getNode(utils, emerald(), new RangeValue(1), item, new RangeValue(price), conditions);
        }

        if (isOutline(BUY_FOR_MANY_EMERALDS)) {
            return getNode(utils, emerald(), new RangeValue(price), item, item.map((s) -> new RangeValue(Math.max(1, s.getCount())), (t) -> new RangeValue(1)), conditions);
        }

        return new MissingNode(MissingTooltipUtils.getMissingItemListingTooltip(utils, parent).build());
    }

    @NotNull
    private IDataNode getNode(IServerUtils utils,
                              Either<ItemStack, TagKey<? extends ItemLike>> input,
                              RangeValue inputCount,
                              Either<ItemStack, TagKey<? extends ItemLike>> output,
                              RangeValue outputCount,
                              TooltipNode conditions) {
        return new ItemsToItemsNode(utils, input, inputCount, output, outputCount, maxUses, xp, priceMultiplier, conditions);
    }

    private boolean isOutline(@Nullable Object candidate) {
        return candidate != null && candidate == outline;
    }

    @NotNull
    private static Either<ItemStack, TagKey<? extends ItemLike>> emerald() {
        return Either.left(Items.EMERALD.getDefaultInstance());
    }

    @Nullable
    private static Object getOutline(String name) {
        try {
            Field field = Villages.class.getDeclaredField(name);

            field.setAccessible(true);
            return field.get(null);
        } catch (Throwable e) {
            LOGGER.warn("Failed to read trade outline {} with error {}", name, e.getMessage(), e);
            return null;
        }
    }
}
