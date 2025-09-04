package com.yanny.ali.plugin.common.nodes;

import com.mojang.datafixers.util.Either;
import com.yanny.ali.Utils;
import com.yanny.ali.api.*;
import com.yanny.ali.plugin.common.NodeUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
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

    private final List<ITooltipNode> tooltip;
    private final ItemStack itemStack;
    private final RangeValue count;

    public ItemStackNode(IServerUtils utils, ItemStack item, RangeValue count) {
        this(utils, item, count, Collections.emptyList());
    }

    public ItemStackNode(IServerUtils ignoredUtils, ItemStack item, RangeValue count, List<ITooltipNode> tooltip) {
        itemStack = item;
        this.tooltip = tooltip;
        this.count = count;
    }

    public ItemStackNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        itemStack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
        tooltip = NodeUtils.decodeTooltipNodes(utils, buf);
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
    public void encode(IServerUtils utils, RegistryFriendlyByteBuf buf) {
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, itemStack);
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
