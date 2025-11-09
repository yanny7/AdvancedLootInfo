package com.yanny.ali.plugin.mods.immersive_engineering.trades;

import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.plugin.common.tooltip.EmptyTooltipNode;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.ali.plugin.common.trades.SubTradesNode;
import com.yanny.ali.plugin.mods.BaseAccessor;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.IItemListing;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import oshi.util.tuples.Pair;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@ClassAccessor("blusunrize.immersiveengineering.common.world.Villages$RerollingItemListing")
public class RerollingItemListing extends BaseAccessor<VillagerTrades.ItemListing> implements IItemListing {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final List<Item> ITEMS = List.of(getItem("gunpart_barrel"), getItem("gunpart_drum"), getItem("gunpart_hammer"));
    private static final Object MAP_INSTANCE;
    private static final Object REVOLVER_INSTANCE;

    static {
        Object mapInstance = null;
        Object revolverInstance = null;

        try {
            Class<?> tradesClass = Class.forName("blusunrize.immersiveengineering.common.world.Villages$OreveinMapForEmeralds");
            Field typeMapField = tradesClass.getDeclaredField("INSTANCE");

            typeMapField.setAccessible(true);
            mapInstance = typeMapField.get(null);
        } catch (Throwable e) {
            LOGGER.warn("Unable to obtain map instance: {}", e.getMessage());
        }

        try {
            Class<?> tradesClass = Class.forName("blusunrize.immersiveengineering.common.world.Villages$RevolverPieceForEmeralds");
            Field typeMapField = tradesClass.getDeclaredField("INSTANCE");

            typeMapField.setAccessible(true);
            revolverInstance = typeMapField.get(null);
        } catch (Throwable e) {
            LOGGER.warn("Unable to obtain revolver instance: {}", e.getMessage());
        }

        MAP_INSTANCE = mapInstance;
        REVOLVER_INSTANCE = revolverInstance;
    }

    public RerollingItemListing(VillagerTrades.ItemListing parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, ITooltipNode condition) {
        return new SubTradesNode<>(utils, this, condition) {
            @Override
            public List<IDataNode> getSubTrades(IServerUtils ignoredUtils, RerollingItemListing ignoredListing) {
                List<IDataNode> nodes = new ArrayList<>();

                if (parent == MAP_INSTANCE) {
                    nodes.add(getMapListing());
                } else if (parent == REVOLVER_INSTANCE) {
                    for (Item item : ITEMS) {
                        nodes.add(getRevolverListing(item));
                    }
                }

                return nodes;
            }

            @NotNull
            private IDataNode getMapListing() {
                ItemStack map = Items.MAP.getDefaultInstance();

                map.set(DataComponents.ITEM_NAME, Component.translatable("item.immersiveengineering.map_orevein"));

                return new ItemsToItemsNode(
                        utils,
                        Either.left(Items.EMERALD.getDefaultInstance()),
                        new RangeValue(8, 16),
                        Either.left(map),
                        new RangeValue(1),
                        30,
                        1,
                        0.5F,
                        EmptyTooltipNode.EMPTY
                );
            }

            @NotNull
            private IDataNode getRevolverListing(Item item) {
                return new ItemsToItemsNode(
                        utils,
                        Either.left(Items.EMERALD.getDefaultInstance()),
                        new RangeValue(5, 64),
                        Either.left(item.getDefaultInstance()),
                        new RangeValue(1),
                        30,
                        1,
                        0.5F,
                        EmptyTooltipNode.EMPTY
                );
            }
        };
    }

    @Override
    public Pair<List<Item>, List<Item>> collectItems(IServerUtils utils) {
        if (parent == MAP_INSTANCE) {
            return new Pair<>(List.of(Items.EMERALD), List.of(Items.MAP));
        } else if (parent == REVOLVER_INSTANCE) {
            return new Pair<>(List.of(Items.EMERALD), ITEMS);
        }

        return new Pair<>(List.of(), List.of());
    }

    @NotNull
    private static Item getItem(String name) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("immersiveengineering", name));
    }
}
