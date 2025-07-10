package com.yanny.ali.plugin.common.nodes;

import com.mojang.datafixers.util.Either;
import com.yanny.ali.Utils;
import com.yanny.ali.api.*;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.server.EntryTooltipUtils;
import com.yanny.ali.plugin.server.TooltipUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class ItemNode implements IDataNode, IItemNode {
    public static final ResourceLocation ID = new ResourceLocation(Utils.MOD_ID, "item");

    private final List<ITooltipNode> tooltip;
    private final List<LootItemCondition> conditions;
    private final ItemStack itemStack;
    private final RangeValue count;

    public ItemNode(IServerUtils utils, LootItem entry, float chance, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<LootItemFunction> allFunctions = Stream.concat(functions.stream(), Arrays.stream(entry.functions)).toList();

        this.conditions = Stream.concat(conditions.stream(), Arrays.stream(entry.conditions)).toList();
        itemStack = TooltipUtils.getItemStack(utils, entry.item, allFunctions);
        tooltip = EntryTooltipUtils.getSingletonTooltip(utils, entry, chance, sumWeight, functions, conditions);
        count = TooltipUtils.getCount(utils, allFunctions).get(null).get(0);
    }

    public ItemNode(IClientUtils utils, FriendlyByteBuf buf) {
        itemStack = buf.readItem();
        tooltip = NodeUtils.decodeTooltipNodes(utils, buf);
        conditions = Collections.emptyList();
        count = new RangeValue(buf);
    }

    @Override
    public Either<ItemStack, TagKey<Item>> getModifiedItem() {
        return Either.left(itemStack);
    }

    @Override
    public List<LootItemCondition> getConditions() {
        return conditions;
    }

    @Override
    public RangeValue getCount() {
        return count;
    }

    @Override
    public void encode(IServerUtils utils, FriendlyByteBuf buf) {
        buf.writeItem(itemStack);
        NodeUtils.encodeTooltipNodes(utils, buf, tooltip);
        count.encode(buf);
    }

    @Override
    public List<ITooltipNode> getTooltip() {
        return tooltip;
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }
}
