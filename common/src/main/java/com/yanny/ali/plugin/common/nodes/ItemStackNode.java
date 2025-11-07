package com.yanny.ali.plugin.common.nodes;

import com.mojang.datafixers.util.Either;
import com.yanny.ali.Utils;
import com.yanny.ali.api.*;
import com.yanny.ali.plugin.common.tooltip.EmptyTooltipNode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Collections;
import java.util.List;

public class ItemStackNode implements IDataNode, IItemNode {
    public static final ResourceLocation ID = new ResourceLocation(Utils.MOD_ID, "item_stack");

    private final ITooltipNode tooltip;
    private final ItemStack itemStack;
    private final RangeValue count;

    public ItemStackNode(IServerUtils utils, ItemStack item, RangeValue count) {
        this(utils, item, count, EmptyTooltipNode.EMPTY);
    }

    public ItemStackNode(IServerUtils ignoredUtils, ItemStack item, RangeValue count, ITooltipNode tooltip) {
        itemStack = item;
        this.tooltip = tooltip;
        this.count = count;
    }

    public ItemStackNode(IClientUtils utils, FriendlyByteBuf buf) {
        itemStack = buf.readItem();
        tooltip = ITooltipNode.decodeNode(utils, buf);
        count = new RangeValue(buf);
    }

    @Override
    public Either<ItemStack, TagKey<? extends ItemLike>> getModifiedItem() {
        return Either.left(itemStack);
    }

    @Override
    public List<LootItemCondition> getConditions() {
        return Collections.emptyList();
    }

    @Override
    public List<LootItemFunction> getFunctions() {
        return Collections.emptyList();
    }

    @Override
    public RangeValue getCount() {
        return count;
    }

    @Override
    public float getChance() {
        return 1;
    }

    @Override
    public void encode(IServerUtils utils, FriendlyByteBuf buf) {
        buf.writeItem(itemStack);
        ITooltipNode.encodeNode(utils, tooltip, buf);
        count.encode(buf);
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
