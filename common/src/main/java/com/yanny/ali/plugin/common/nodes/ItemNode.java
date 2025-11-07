package com.yanny.ali.plugin.common.nodes;

import com.mojang.datafixers.util.Either;
import com.yanny.ali.Utils;
import com.yanny.ali.api.*;
import com.yanny.ali.plugin.common.tooltip.EmptyTooltipNode;
import com.yanny.ali.plugin.server.EntryTooltipUtils;
import com.yanny.ali.plugin.server.TooltipUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class ItemNode implements IDataNode, IItemNode {
    public static final ResourceLocation ID = new ResourceLocation(Utils.MOD_ID, "item");

    private final ITooltipNode tooltip;
    private final List<LootItemCondition> conditions;
    private final List<LootItemFunction> functions;
    private final ItemStack itemStack;
    private final RangeValue count;
    private final float chance;

    public ItemNode(IServerUtils utils, LootItem entry, float chance, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        this.conditions = Stream.concat(conditions.stream(), Arrays.stream(entry.conditions)).toList();
        this.functions = Stream.concat(functions.stream(), Arrays.stream(entry.functions)).toList();
        this.chance = chance * entry.weight / sumWeight;
        itemStack = TooltipUtils.getItemStack(utils, entry.item.getDefaultInstance(), this.functions);
        tooltip = EntryTooltipUtils.getSingletonTooltip(utils, entry, chance, sumWeight, functions, conditions);
        count = TooltipUtils.getCount(utils, this.functions).get(null).get(0);
    }

    public ItemNode(IServerUtils utils, Item item, RangeValue count) {
        this(utils, item, count, EmptyTooltipNode.EMPTY);
    }

    public ItemNode(IServerUtils ignoredUtils, Item item, RangeValue count, ITooltipNode tooltip) {
        this.conditions = Collections.emptyList();
        this.functions = Collections.emptyList();
        chance = 1;
        itemStack = item.getDefaultInstance();
        this.tooltip = tooltip;
        this.count = count;
    }

    public ItemNode(IClientUtils utils, FriendlyByteBuf buf) {
        itemStack = buf.readItem();
        tooltip = ITooltipNode.decodeNode(utils, buf);
        count = new RangeValue(buf);
        chance = buf.readFloat();

        conditions = Collections.emptyList();
        functions = Collections.emptyList();
    }

    @Override
    public Either<ItemStack, TagKey<? extends ItemLike>> getModifiedItem() {
        return Either.left(itemStack);
    }

    @Override
    public List<LootItemCondition> getConditions() {
        return conditions;
    }

    @Override
    public List<LootItemFunction> getFunctions() {
        return functions;
    }

    @Override
    public RangeValue getCount() {
        return count;
    }

    @Override
    public float getChance() {
        return chance;
    }

    @Override
    public void encode(IServerUtils utils, FriendlyByteBuf buf) {
        buf.writeItem(itemStack);
        ITooltipNode.encodeNode(utils, tooltip, buf);
        count.encode(buf);
        buf.writeFloat(chance);
    }

    @Override
    public ITooltipNode getTooltip() {
        return tooltip;
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }
}
