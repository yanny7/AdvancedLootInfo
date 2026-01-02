package com.yanny.ali.forge.plugin.mods.mantle.trades;

import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.ali.plugin.mods.BaseAccessor;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.IItemListing;
import com.yanny.ali.plugin.mods.PluginUtils;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import oshi.util.tuples.Pair;

import java.lang.reflect.Field;
import java.util.List;

@ClassAccessor("slimeknights.tconstruct.world.logic.AncientToolItemListing")
public class AncientToolItemListing extends BaseAccessor<VillagerTrades.ItemListing> implements IItemListing {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final TagKey<Item> TRADER_TOOLS;

    static {
        TagKey<Item> tagKey = null;
        try {
            Class<?> tradesClass = Class.forName("slimeknights.tconstruct.common.TinkerTags$Items");
            Field typeMapField = tradesClass.getDeclaredField("TRADER_TOOLS");

            typeMapField.setAccessible(true);
            //noinspection unchecked
            tagKey = (TagKey<Item>) typeMapField.get(null);
        } catch (Throwable e) {
            e.printStackTrace();
            LOGGER.warn("Unable to obtain item TRADER_TOOLS: {}", e.getMessage());
        }

        TRADER_TOOLS = tagKey;
    }

    public AncientToolItemListing(VillagerTrades.ItemListing parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, ITooltipNode condition) {
        return new ItemsToItemsNode(
                utils,
                Either.left(Items.EMERALD.getDefaultInstance()),
                new RangeValue(12, 64),
                Either.right(TRADER_TOOLS),
                new RangeValue(),
                15,
                1,
                1.0F,
                condition
        );
    }

    @Override
    public Pair<List<Item>, List<Item>> collectItems(IServerUtils utils) {
        return new Pair<>(List.of(Items.EMERALD), PluginUtils.getItems(utils, TRADER_TOOLS));
    }
}
