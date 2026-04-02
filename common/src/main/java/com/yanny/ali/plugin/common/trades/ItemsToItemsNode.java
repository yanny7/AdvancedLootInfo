package com.yanny.ali.plugin.common.trades;

import com.mojang.datafixers.util.Either;
import com.yanny.ali.Utils;
import com.yanny.ali.api.*;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import com.yanny.ali.plugin.common.tooltip.ArrayTooltipNode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.Collections;

public class ItemsToItemsNode extends ListNode {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Utils.MOD_ID, "items_to_items");

    private final ITooltipNode tooltip;

    public ItemsToItemsNode(IServerUtils utils,
                            Either<ItemStack, TagKey<? extends ItemLike>> input1,
                            RangeValue input1Count,
                            ITooltipNode input1Condition,
                            Either<ItemStack, TagKey<? extends ItemLike>> input2,
                            RangeValue input2Count,
                            ITooltipNode input2Condition,
                            Either<ItemStack, TagKey<? extends ItemLike>> output,
                            RangeValue outputCount,
                            ITooltipNode outputModifier,
                            RangeValue maxUses,
                            RangeValue xp,
                            ITooltipNode tooltip) {
        addChildren(getChildren(input1, input1Count, input1Condition));
        addChildren(getChildren(input2, input2Count, input2Condition));
        addChildren(getChildren(output, outputCount, outputModifier));
        this.tooltip = ArrayTooltipNode.array()
                .add(utils.getValueTooltip(utils, maxUses.toString()).build("ali.property.value.uses"))
                .add(utils.getValueTooltip(utils, xp.toString()).build("ali.property.value.villager_xp"))
                .add(tooltip)
                .build();
    }

    public ItemsToItemsNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        super(utils, buf);
        tooltip = ITooltipNode.decodeNode(utils, buf);
    }

    @Override
    public ITooltipNode getTooltip() {
        return tooltip;
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public void encodeNode(IServerUtils utils, RegistryFriendlyByteBuf buf) {
        ITooltipNode.encodeNode(utils, tooltip, buf);
    }

    private static IDataNode getChildren(Either<ItemStack, TagKey<? extends ItemLike>> item, RangeValue count, ITooltipNode condition) {
        return item.map(
                (i) -> new ItemNode(1, count, i, condition, Collections.emptyList(), Collections.emptyList()),
                (t) -> new ItemNode(1, count, t, condition, Collections.emptyList(), Collections.emptyList())
        );
    }
}
