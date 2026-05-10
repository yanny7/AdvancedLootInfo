package com.yanny.ali.plugin.common.trades;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ListNode;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class ItemsToItemsNode extends ListNode {
    public static final ResourceLocation ID = Utils.modLoc("items_to_items");

    private final TooltipNode tooltip;

    public ItemsToItemsNode(IServerUtils utils,
                            Either<ItemStack, TagKey<? extends ItemLike>> input1,
                            RangeValue input1Count,
                            Either<ItemStack, TagKey<? extends ItemLike>> output,
                            RangeValue outputCount,
                            int maxUses,
                            int xp,
                            float priceMultiplier,
                            TooltipNode condition) {
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
                            TooltipNode condition) {
        this(utils, input1, input1Count, TooltipNode.EMPTY_INSTANCE, input2, input2Count, TooltipNode.EMPTY_INSTANCE, output, outputCount, TooltipNode.EMPTY_INSTANCE, maxUses, xp, priceMultiplier, condition);
    }

    public ItemsToItemsNode(IServerUtils utils,
                            Either<ItemStack, TagKey<? extends ItemLike>> input1,
                            RangeValue input1Count,
                            TooltipNode input1Condition,
                            Either<ItemStack, TagKey<? extends ItemLike>> input2,
                            RangeValue input2Count,
                            TooltipNode input2Condition,
                            Either<ItemStack, TagKey<? extends ItemLike>> output,
                            RangeValue outputCount,
                            TooltipNode outputCondition,
                            int maxUses,
                            int xp,
                            float priceMultiplier,
                            TooltipNode condition) {
        addChildren(getChildren(input1, input1Count, input1Condition));
        addChildren(getChildren(input2, input2Count, input2Condition));
        addChildren(getChildren(output, outputCount, outputCondition));
        tooltip = TooltipBuilder.array((b) -> b
                .add(condition)
                .add(utils.getValueTooltip(utils, maxUses).build("ali.property.value.uses"))
                .add(utils.getValueTooltip(utils, xp).build("ali.property.value.villager_xp"))
                .add(utils.getValueTooltip(utils, priceMultiplier).build("ali.property.value.price_multiplier"))
        ).build();
    }

    public ItemsToItemsNode(IClientUtils utils, FriendlyByteBuf buf) {
        super(utils, buf);
        tooltip = TooltipNode.decode(utils, buf);
    }

    @NotNull
    @Override
    public TooltipNode getTooltip() {
        return tooltip;
    }

    @NotNull
    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void encodeNode(IServerUtils utils, FriendlyByteBuf buf) {
        tooltip.encode(utils, buf);
    }

    private static IDataNode getChildren(Either<ItemStack, TagKey<? extends ItemLike>> item, RangeValue count, TooltipNode condition) {
        return item.map(
                (i) -> new ItemNode(1, count, i, condition, Collections.emptyList(), Collections.emptyList()),
                (t) -> new ItemNode(1, count, t, condition, Collections.emptyList(), Collections.emptyList())
        );
    }
}
