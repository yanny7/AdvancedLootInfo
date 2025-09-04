package com.yanny.ali.plugin.common.trades;

import com.mojang.datafixers.util.Either;
import com.yanny.ali.Utils;
import com.yanny.ali.api.*;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.ItemStackNode;
import com.yanny.ali.plugin.common.nodes.TagNode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.getFloatTooltip;
import static com.yanny.ali.plugin.server.GenericTooltipUtils.getIntegerTooltip;

public class ItemsToItemsNode extends ListNode {
    public static final ResourceLocation ID = new ResourceLocation(Utils.MOD_ID, "items_to_items");

    private final List<ITooltipNode> tooltip;

    public ItemsToItemsNode(IServerUtils utils,
                            Either<ItemStack, TagKey<? extends ItemLike>> input1,
                            RangeValue input1Count,
                            Either<ItemStack, TagKey<? extends ItemLike>> output,
                            RangeValue outputCount,
                            int maxUses,
                            int xp,
                            float priceMultiplier,
                            List<ITooltipNode> conditions) {
        this(utils, input1, input1Count, Either.left(ItemStack.EMPTY), new RangeValue(), output, outputCount, maxUses, xp, priceMultiplier, conditions);
    }

    public ItemsToItemsNode(IServerUtils utils,
                            Either<ItemStack, TagKey<? extends ItemLike>> input1,
                            RangeValue input1Count,
                            Either<ItemStack, TagKey<? extends ItemLike>> input2,
                            RangeValue input2Count,
                            Either<ItemStack, TagKey<? extends ItemLike>> output,
                            RangeValue outputCount,
                            int maxUses,
                            int xp,
                            float priceMultiplier,
                            List<ITooltipNode> conditions) {
        this(utils, input1, input1Count, Collections.emptyList(), input2, input2Count, Collections.emptyList(), output, outputCount, Collections.emptyList(), maxUses, xp, priceMultiplier, conditions);
    }

    public ItemsToItemsNode(IServerUtils utils,
                            Either<ItemStack, TagKey<? extends ItemLike>> input1,
                            RangeValue input1Count,
                            List<ITooltipNode> input1Conditions,
                            Either<ItemStack, TagKey<? extends ItemLike>> input2,
                            RangeValue input2Count,
                            List<ITooltipNode> input2Conditions,
                            Either<ItemStack, TagKey<? extends ItemLike>> output,
                            RangeValue outputCount,
                            List<ITooltipNode> outputConditions,
                            int maxUses,
                            int xp,
                            float priceMultiplier,
                            List<ITooltipNode> conditions) {
        addChildren(getChildren(utils, input1, input1Count, input1Conditions));
        addChildren(getChildren(utils, input2, input2Count, input2Conditions));
        addChildren(getChildren(utils, output, outputCount, outputConditions));
        tooltip = new ArrayList<>(conditions);
        tooltip.add(getIntegerTooltip(utils, "ali.property.value.uses", maxUses));
        tooltip.add(getIntegerTooltip(utils, "ali.property.value.villager_xp", xp));
        tooltip.add(getFloatTooltip(utils, "ali.property.value.price_multiplier", priceMultiplier));
    }

    public ItemsToItemsNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        super(utils, buf);
        tooltip = NodeUtils.decodeTooltipNodes(utils, buf);
    }

    @Override
    public List<ITooltipNode> getTooltip() {
        return tooltip;
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void encodeNode(IServerUtils utils, RegistryFriendlyByteBuf buf) {
        NodeUtils.encodeTooltipNodes(utils, buf, tooltip);
    }

    private static IDataNode getChildren(IServerUtils utils, Either<ItemStack, TagKey<? extends ItemLike>> item, RangeValue count, List<ITooltipNode> conditions) {
        return item.map(
                (i) -> new ItemStackNode(utils, i, count, conditions),
                (t) -> new TagNode(utils, t, count, conditions)
        );
    }
}
