package com.yanny.ali.plugin.mods.immersive_engineering.trades;

import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.plugin.common.nodes.MissingNode;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.ali.plugin.mods.*;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import oshi.util.tuples.Pair;

import java.lang.reflect.Field;
import java.util.List;

@ClassAccessor("blusunrize.immersiveengineering.common.world.Villages$TradeListing")
public class TradeListing extends BaseAccessor<VillagerTrades.ItemListing> implements IItemListing {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Object SELL_FOR_ONE_EMERALD;
    private static final Object SELL_FOR_MANY_EMERALDS;
    private static final Object BUY_FOR_ONE_EMERALD;
    private static final Object BUY_FOR_MANY_EMERALDS;

    static {
        Object sellForOneEmerald = null;
        Object sellForManyEmeralds = null;
        Object buyForOneEmerald = null;
        Object buyForManyEmeralds = null;

        try {
            Class<?> tradesClass = Class.forName("blusunrize.immersiveengineering.common.world.Villages");
            Field typeMapField = tradesClass.getDeclaredField("SELL_FOR_ONE_EMERALD");

            typeMapField.setAccessible(true);
            sellForOneEmerald = typeMapField.get(null);
        } catch (Throwable e) {
            LOGGER.warn("Unable to obtain trade outline SELL_FOR_ONE_EMERALD: {}", e.getMessage());
        }

        try {
            Class<?> tradesClass = Class.forName("blusunrize.immersiveengineering.common.world.Villages");
            Field typeMapField = tradesClass.getDeclaredField("SELL_FOR_MANY_EMERALDS");

            typeMapField.setAccessible(true);
            sellForManyEmeralds = typeMapField.get(null);
        } catch (Throwable e) {
            e.printStackTrace();
            LOGGER.warn("Unable to obtain trade outline SELL_FOR_MANY_EMERALDS: {}", e.getMessage());
        }

        try {
            Class<?> tradesClass = Class.forName("blusunrize.immersiveengineering.common.world.Villages");
            Field typeMapField = tradesClass.getDeclaredField("BUY_FOR_ONE_EMERALD");

            typeMapField.setAccessible(true);
            buyForOneEmerald = typeMapField.get(null);
        } catch (Throwable e) {
            e.printStackTrace();
            LOGGER.warn("Unable to obtain trade outline BUY_FOR_ONE_EMERALD: {}", e.getMessage());
        }

        try {
            Class<?> tradesClass = Class.forName("blusunrize.immersiveengineering.common.world.Villages");
            Field typeMapField = tradesClass.getDeclaredField("BUY_FOR_MANY_EMERALDS");

            typeMapField.setAccessible(true);
            buyForOneEmerald = typeMapField.get(null);
        } catch (Throwable e) {
            e.printStackTrace();
            LOGGER.warn("Unable to obtain trade outline BUY_FOR_MANY_EMERALDS: {}", e.getMessage());
        }

        SELL_FOR_ONE_EMERALD = sellForOneEmerald;
        SELL_FOR_MANY_EMERALDS = sellForManyEmeralds;
        BUY_FOR_ONE_EMERALD = buyForOneEmerald;
        BUY_FOR_MANY_EMERALDS = buyForManyEmeralds;
    }

    @FieldAccessor
    private int price;
    @FieldAccessor
    private Object outline;
    @FieldAccessor(clazz = LazyItemStack.class)
    private LazyItemStack lazyItem;
    @FieldAccessor
    private int maxUses;
    @FieldAccessor
    private int xp;
    @FieldAccessor
    private float priceMultiplier;

    public TradeListing(VillagerTrades.ItemListing parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, ITooltipNode condition) {
        Either<ItemStack, TagKey<? extends ItemLike>> item = lazyItem.getItem();

        if (outline == SELL_FOR_ONE_EMERALD) {
            return new ItemsToItemsNode(
                    utils,
                    item,
                    new RangeValue(price),
                    Either.left(Items.EMERALD.getDefaultInstance()),
                    new RangeValue(1),
                    maxUses,
                    xp,
                    priceMultiplier,
                    condition
            );
        } else if (outline == SELL_FOR_MANY_EMERALDS) {
            return new ItemsToItemsNode(
                    utils,
                    item,
                    new RangeValue(1),
                    Either.left(Items.EMERALD.getDefaultInstance()),
                    new RangeValue(price),
                    maxUses,
                    xp,
                    priceMultiplier,
                    condition
            );
        } else if (outline == BUY_FOR_ONE_EMERALD) {
            return new ItemsToItemsNode(
                    utils,
                    Either.left(Items.EMERALD.getDefaultInstance()),
                    new RangeValue(1),
                    item,
                    new RangeValue(price),
                    maxUses,
                    xp,
                    priceMultiplier,
                    conditions
            );
        } else if (outline == BUY_FOR_MANY_EMERALDS) {
            return new ItemsToItemsNode(
                    utils,
                    Either.left(Items.EMERALD.getDefaultInstance()),
                    new RangeValue(price),
                    item,
                    new RangeValue(1),
                    maxUses,
                    xp,
                    priceMultiplier,
                    condition
            );
        }

        return new MissingNode();
    }

    @Override
    public Pair<List<Item>, List<Item>> collectItems(IServerUtils utils) {
        //noinspection unchecked
        List<Item> items = PluginUtils.getItems(utils, (Either<ItemStack, TagKey<ItemLike>>) (Object) lazyItem.getItem());

        if (outline == SELL_FOR_ONE_EMERALD || outline == SELL_FOR_MANY_EMERALDS) {
            return new Pair<>(items, List.of(Items.EMERALD));
        } else if (outline == BUY_FOR_ONE_EMERALD || outline == BUY_FOR_MANY_EMERALDS) {
            return new Pair<>(List.of(Items.EMERALD), items);
        }

        return new Pair<>(List.of(), List.of());
    }
}
