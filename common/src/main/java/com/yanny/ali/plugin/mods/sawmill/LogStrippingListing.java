package com.yanny.ali.plugin.mods.sawmill;

import com.mojang.datafixers.util.Either;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.ali.plugin.common.trades.SubTradesNode;
import com.yanny.ali.plugin.mods.BaseAccessor;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.mods.IItemListing;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@ClassAccessor("net.mehvahdjukaar.sawmill.CarpenterTrades$LogStrippingListing")
public class LogStrippingListing extends BaseAccessor<VillagerTrades.ItemListing> implements IItemListing {
    @FieldAccessor
    private ItemStack price;
    @FieldAccessor
    private int amount;
    @FieldAccessor
    private int maxTrades;
    @FieldAccessor
    private int xp;
    @FieldAccessor
    private float priceMult;

    public LogStrippingListing(VillagerTrades.ItemListing parent) {
        super(parent);
    }

    @Override
    public IDataNode getNode(IServerUtils utils, ITooltipNode condition) {
        return new SubTradesNode<>(utils, this, condition) {
            @Override
            public List<IDataNode> getSubTrades(IServerUtils utils, LogStrippingListing listing) {
                List<IDataNode> nodes = new ArrayList<>();

                for (VillagerType type : BuiltInRegistries.VILLAGER_TYPE) {
                    List<WoodType> woodTypes = Utils.WOOD_TYPES.get(type);

                    if (woodTypes != null) {
                        for (WoodType woodType : woodTypes) {
                            Item log = woodType.log.asItem();
                            Item stripped = woodType.getItemOfThis("stripped_log");

                            if (stripped != null) {
                                nodes.add(new ItemsToItemsNode(
                                        utils,
                                        Either.left(log.getDefaultInstance()),
                                        new RangeValue(amount),
                                        Either.left(price),
                                        new RangeValue(price.getCount()),
                                        Either.left(stripped.getDefaultInstance()),
                                        new RangeValue(amount),
                                        maxTrades,
                                        xp,
                                        priceMult,
                                        utils.getValueTooltip(utils, type.toString()).key("ali.property.value.villager_type")
                                ));
                            }
                        }
                    }
                }

                return nodes;
            }
        };
    }

    @Override
    public Pair<List<Item>, List<Item>> collectItems(IServerUtils utils) {
        List<Item> inputs = new ArrayList<>(BuiltInRegistries.VILLAGER_TYPE.stream()
                .map(Utils.WOOD_TYPES::get)
                .filter(Objects::nonNull)
                .flatMap((e) -> e.stream().flatMap((w) -> {
                    Item stripped = w.getItemOfThis("stripped_log");

                    return stripped != null ? Stream.of(w.log.asItem()) : Stream.of();
                }))
                .toList());
        List<Item> outputs = BuiltInRegistries.VILLAGER_TYPE.stream()
                .map(Utils.WOOD_TYPES::get)
                .filter(Objects::nonNull)
                .flatMap((e) -> e.stream().flatMap((w) -> {
                    Item stripped = w.getItemOfThis("stripped_log");

                    return stripped != null ? Stream.of(stripped) : Stream.of();
                }))
                .toList();

        inputs.add(price.getItem());
        return new Pair<>(inputs, outputs);
    }
}
