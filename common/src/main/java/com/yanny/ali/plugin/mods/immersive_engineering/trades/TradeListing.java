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
import com.yanny.ali.plugin.server.GenericTooltipUtils;
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

    private static final Object EMERALD_FOR_ITEM;
    private static final Object ONE_ITEM_FOR_EMERALDS;
    private static final Object ITEMS_FOR_ONE_EMERALD;

    static {
        Object emeraldForItem = null;
        Object oneItemForEmeralds = null;
        Object itemsForOneEmerald = null;

        try {
            Class<?> tradesClass = Class.forName("blusunrize.immersiveengineering.common.world.Villages");
            Field typeMapField = tradesClass.getDeclaredField("EMERALD_FOR_ITEM");

            typeMapField.setAccessible(true);
            emeraldForItem = typeMapField.get(null);
        } catch (Throwable e) {
            LOGGER.warn("Unable to obtain trade outline EMERALD_FOR_ITEM: {}", e.getMessage(), e);
        }

        try {
            Class<?> tradesClass = Class.forName("blusunrize.immersiveengineering.common.world.Villages");
            Field typeMapField = tradesClass.getDeclaredField("ONE_ITEM_FOR_EMERALDS");

            typeMapField.setAccessible(true);
            oneItemForEmeralds = typeMapField.get(null);
        } catch (Throwable e) {
            LOGGER.warn("Unable to obtain trade outline ONE_ITEM_FOR_EMERALDS: {}", e.getMessage(), e);
        }

        try {
            Class<?> tradesClass = Class.forName("blusunrize.immersiveengineering.common.world.Villages");
            Field typeMapField = tradesClass.getDeclaredField("ITEMS_FOR_ONE_EMERALD");

            typeMapField.setAccessible(true);
            itemsForOneEmerald = typeMapField.get(null);
        } catch (Throwable e) {
            LOGGER.warn("Unable to obtain trade outline ITEMS_FOR_ONE_EMERALD: {}", e.getMessage(), e);
        }

        EMERALD_FOR_ITEM = emeraldForItem;
        ONE_ITEM_FOR_EMERALDS = oneItemForEmeralds;
        ITEMS_FOR_ONE_EMERALD = itemsForOneEmerald;
    }

    @FieldAccessor(clazz = PriceInterval.class)
    private PriceInterval priceInfo;
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

        if (outline == EMERALD_FOR_ITEM) {
            return new ItemsToItemsNode(
                    utils,
                    item,
                    new RangeValue(priceInfo.min, priceInfo.max),
                    Either.left(Items.EMERALD.getDefaultInstance()),
                    new RangeValue(1),
                    maxUses,
                    xp,
                    priceMultiplier,
                    condition
            );
        } else if (outline == ONE_ITEM_FOR_EMERALDS) {
            return new ItemsToItemsNode(
                    utils,
                    Either.left(Items.EMERALD.getDefaultInstance()),
                    new RangeValue(priceInfo.min, priceInfo.max),
                    item,
                    new RangeValue(1),
                    maxUses,
                    xp,
                    priceMultiplier,
                    condition
            );
        } else if (outline == ITEMS_FOR_ONE_EMERALD) {
            return new ItemsToItemsNode(
                    utils,
                    Either.left(Items.EMERALD.getDefaultInstance()),
                    new RangeValue(1),
                    item,
                    new RangeValue(priceInfo.min, priceInfo.max),
                    maxUses,
                    xp,
                    priceMultiplier,
                    condition
            );
        }

        return new MissingNode(GenericTooltipUtils.getMissingItemListingTooltip(utils, parent));
    }

    @Override
    public Pair<List<Item>, List<Item>> collectItems(IServerUtils utils) {
        //noinspection unchecked
        List<Item> items = PluginUtils.getItems(utils, (Either<ItemStack, TagKey<ItemLike>>) (Object) lazyItem.getItem());

        if (outline == EMERALD_FOR_ITEM) {
            return new Pair<>(items, List.of(Items.EMERALD));
        } else if (outline == ONE_ITEM_FOR_EMERALDS || outline == ITEMS_FOR_ONE_EMERALD) {
            return new Pair<>(List.of(Items.EMERALD), items);
        }

        return new Pair<>(List.of(), List.of());
    }
}
