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
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class ItemsToItemsNode extends ListNode {
    public static final Identifier ID = Utils.modLoc("items_to_items");

    private final TooltipNode tooltip;

    public ItemsToItemsNode(IServerUtils utils,
                            Either<ItemStack, TagKey<? extends ItemLike>> input1,
                            RangeValue input1Count,
                            TooltipNode input1Condition,
                            Either<ItemStack, TagKey<? extends ItemLike>> input2,
                            RangeValue input2Count,
                            TooltipNode input2Condition,
                            Either<ItemStack, TagKey<? extends ItemLike>> output,
                            RangeValue outputCount,
                            TooltipNode outputModifier,
                            RangeValue maxUses,
                            RangeValue xp,
                            TooltipNode tooltip) {
        addChildren(getChildren(input1, input1Count, input1Condition));
        addChildren(getChildren(input2, input2Count, input2Condition));
        addChildren(getChildren(output, outputCount, outputModifier));
        this.tooltip = TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, maxUses.toString()).build(Lang.Value.USES))
                .add(utils.getValueTooltip(utils, xp.toString()).build(Lang.Value.VILLAGER_XP))
                .add(tooltip)
        ).build();
    }

    public ItemsToItemsNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        super(utils, buf);
        tooltip = utils.getTooltipCache().getNodeById(buf.readVarInt());
    }

    @NotNull
    @Override
    public TooltipNode getTooltip() {
        return tooltip;
    }

    @NotNull
    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public void encodeNode(IServerUtils utils, RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(utils.getTooltipCache().getNodeId(tooltip));
    }

    private static IDataNode getChildren(Either<ItemStack, TagKey<? extends ItemLike>> item, RangeValue count, TooltipNode condition) {
        return item.map(
                (i) -> new ItemNode(1, count, i, condition, Collections.emptyList(), Collections.emptyList()),
                (t) -> new ItemNode(1, count, t, condition, Collections.emptyList(), Collections.emptyList())
        );
    }
}
