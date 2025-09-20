package com.yanny.ali.plugin.mods.sawmill;

import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.ali.plugin.common.trades.SubTradesNode;
import com.yanny.ali.plugin.mods.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.trading.ItemCost;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import oshi.util.tuples.Pair;

import java.util.*;
import java.util.stream.Stream;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.getStringTooltip;

@ClassAccessor("net.mehvahdjukaar.sawmill.CarpenterTrades$WoodToItemListing")
public class WoodToItemListing extends BaseAccessor<VillagerTrades.ItemListing> implements IItemListing {
    private static final Logger LOGGER = LogUtils.getLogger();

    @FieldAccessor
    private boolean buys;
    @FieldAccessor
    private String childKey;
    @FieldAccessor
    private int woodPrice;
    @FieldAccessor
    private ItemCost emeralds;
    @FieldAccessor
    private int maxTrades;
    @FieldAccessor
    private int xp;
    @FieldAccessor
    private float priceMult;
    @FieldAccessor
    private boolean typeDependant;

    public WoodToItemListing(VillagerTrades.ItemListing parent) {
        super(parent);
    }

    @Override
    public IDataNode getNode(IServerUtils utils, List<ITooltipNode> conditions) {
        return new SubTradesNode<>(utils, this, conditions) {
            @Override
            public List<IDataNode> getSubTrades(IServerUtils utils, WoodToItemListing listing) {
                List<IDataNode> nodes = new ArrayList<>();

                if (typeDependant) {
                    for (VillagerType type : BuiltInRegistries.VILLAGER_TYPE) {
                        List<WoodType> woodTypes = Utils.WOOD_TYPES.get(type);

                        if (woodTypes != null) {
                            List<ITooltipNode> cond = List.of(getStringTooltip(utils, "ali.property.value.villager_type", type.toString()));

                            nodes.addAll(getNodes(utils, woodTypes, cond));
                        }
                    }
                } else {
                    nodes.addAll(getNodes(utils, getAllWoodTypes(), Collections.emptyList()));
                }

                return nodes;
            }
        };
    }

    @Override
    public Pair<List<Item>, List<Item>> collectItems(IServerUtils utils) {
        List<Item> inputs = new ArrayList<>();
        List<Item> outputs = new ArrayList<>();
        List<Item> typed = BuiltInRegistries.VILLAGER_TYPE.stream()
                .map(Utils.WOOD_TYPES::get)
                .filter(Objects::nonNull)
                .flatMap((e) -> e.stream().flatMap((w) -> {
                    Item child = w.getItemOfThis(childKey);

                    return child != null ? Stream.of(child) : Stream.of();
                }))
                .toList();
        List<Item> allTypes = getAllWoodTypes().stream()
                .flatMap((w) -> {
                    Item child = w.getItemOfThis(childKey);

                    return child != null ? Stream.of(child) : Stream.of();
                })
                .toList();

        if (typeDependant) {
            if (buys) {
                inputs.addAll(typed);
                outputs.add(emeralds.item().value());
            } else {
                inputs.add(emeralds.item().value());
                outputs.addAll(typed);
            }
        } else {
            if (buys) {
                inputs.addAll(allTypes);
                outputs.add(emeralds.item().value());
            } else {
                inputs.add(emeralds.item().value());
                outputs.addAll(allTypes);
            }
        }

        return new Pair<>(inputs, outputs);
    }

    @NotNull
    private List<IDataNode> getNodes(IServerUtils utils, Collection<WoodType> woodTypes, List<ITooltipNode> cond) {
        List<IDataNode> nodes = new ArrayList<>();

        for (WoodType woodType : woodTypes) {
            Item w = woodType.getItemOfThis(childKey);

            if (w != null && !w.getDefaultInstance().isEmpty()) {
                if (buys) {
                    nodes.add(new ItemsToItemsNode(
                            utils,
                            Either.left(w.getDefaultInstance()),
                            new RangeValue(woodPrice),
                            Either.left(emeralds.item().value().getDefaultInstance()),
                            new RangeValue(emeralds.count()),
                            maxTrades,
                            xp,
                            priceMult,
                            cond
                    ));
                } else {
                    nodes.add(new ItemsToItemsNode(
                            utils,
                            Either.left(emeralds.item().value().getDefaultInstance()),
                            new RangeValue(emeralds.count()),
                            Either.left(w.getDefaultInstance()),
                            new RangeValue(woodPrice),
                            maxTrades,
                            xp,
                            priceMult,
                            cond
                    ));
                }
            }
        }

        return nodes;
    }

    private static Collection<WoodType> getAllWoodTypes() {
        try {
            Class<?> registryClass = Class.forName("net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry");

            //noinspection unchecked
            return ((Collection<Object>) registryClass.getMethod("getTypes").invoke(null)).stream().map((e) -> ReflectionUtils.copyClassData(WoodType.class, e)).toList();
        } catch (Throwable e) {
            LOGGER.warn("Unable to obtain all wood types: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
