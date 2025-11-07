package com.yanny.ali.plugin.common.trades;

import com.mojang.datafixers.util.Either;
import com.yanny.ali.Utils;
import com.yanny.ali.api.*;
import com.yanny.ali.plugin.common.nodes.ItemStackNode;
import com.yanny.ali.plugin.common.nodes.TagNode;
import com.yanny.ali.plugin.common.tooltip.ArrayTooltipNode;
import com.yanny.ali.plugin.common.tooltip.EmptyTooltipNode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class ItemsToItemsNode extends ListNode {
    public static final ResourceLocation ID = new ResourceLocation(Utils.MOD_ID, "items_to_items");

    private final ITooltipNode tooltip;

    public ItemsToItemsNode(IServerUtils utils,
                            Either<ItemStack, TagKey<? extends ItemLike>> input1,
                            RangeValue input1Count,
                            Either<ItemStack, TagKey<? extends ItemLike>> output,
                            RangeValue outputCount,
                            int maxUses,
                            int xp,
                            float priceMultiplier,
                            ITooltipNode condition) {
        this(utils, input1, input1Count, Either.left(ItemStack.EMPTY), new RangeValue(), output, outputCount, maxUses, xp, priceMultiplier, condition);
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
                            ITooltipNode condition) {
        this(utils, input1, input1Count, EmptyTooltipNode.EMPTY, input2, input2Count, EmptyTooltipNode.EMPTY, output, outputCount, EmptyTooltipNode.EMPTY, maxUses, xp, priceMultiplier, condition);
    }

    public ItemsToItemsNode(IServerUtils utils,
                            Either<ItemStack, TagKey<? extends ItemLike>> input1,
                            RangeValue input1Count,
                            ITooltipNode input1Condition,
                            Either<ItemStack, TagKey<? extends ItemLike>> input2,
                            RangeValue input2Count,
                            ITooltipNode input2Condition,
                            Either<ItemStack, TagKey<? extends ItemLike>> output,
                            RangeValue outputCount,
                            ITooltipNode outputCondition,
                            int maxUses,
                            int xp,
                            float priceMultiplier,
                            ITooltipNode condition) {
        addChildren(getChildren(utils, input1, input1Count, input1Condition));
        addChildren(getChildren(utils, input2, input2Count, input2Condition));
        addChildren(getChildren(utils, output, outputCount, outputCondition));
        tooltip = ArrayTooltipNode.array()
                .add(condition)
                .add(utils.getValueTooltip(utils, maxUses).key("ali.property.value.uses"))
                .add(utils.getValueTooltip(utils, xp).key("ali.property.value.villager_xp"))
                .add(utils.getValueTooltip(utils, priceMultiplier).key("ali.property.value.price_multiplier"));
    }

    public ItemsToItemsNode(IClientUtils utils, FriendlyByteBuf buf) {
        super(utils, buf);
        tooltip = ITooltipNode.decodeNode(utils, buf);
    }

    @Override
    public ITooltipNode getTooltip() {
        return tooltip;
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void encodeNode(IServerUtils utils, FriendlyByteBuf buf) {
        ITooltipNode.encodeNode(utils, tooltip, buf);
    }

    private static IDataNode getChildren(IServerUtils utils, Either<ItemStack, TagKey<? extends ItemLike>> item, RangeValue count, ITooltipNode condition) {
        return item.map(
                (i) -> new ItemStackNode(utils, i, count, condition),
                (t) -> new TagNode(utils, t, count, condition)
        );
    }
}
