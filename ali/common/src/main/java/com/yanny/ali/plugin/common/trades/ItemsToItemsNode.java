package com.yanny.ali.plugin.common.trades;

import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IItemNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITradeNode;
import com.yanny.ali.api.ListNode;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemsToItemsNode extends ListNode implements ITradeNode {
    public static final ResourceLocation ID = Utils.modLoc("items_to_items");
    private static final Logger LOGGER = LogUtils.getLogger();

    private final TooltipNode tooltip;
    /**
     * How many of the leading children are the trade's costs - the rest is its result. The children are added in that
     * order below and stay in it through the wire: every one of them carries chance 1, and {@code CoreListNode}'s
     * decode sorts them by chance with a stable sort, so equal keys cannot reorder.
     */
    private final int inputCount;

    public ItemsToItemsNode(IServerUtils utils,
                            Either<ItemStack, TagKey<? extends ItemLike>> input1,
                            RangeValue input1Count,
                            Either<ItemStack, TagKey<? extends ItemLike>> output,
                            RangeValue outputCount,
                            int maxUses,
                            int xp,
                            float priceMultiplier,
                            TooltipNode condition) {
        this(utils, input1, input1Count, Either.left(ItemStack.EMPTY), new RangeValue(1), output, outputCount, maxUses, xp, priceMultiplier, condition);
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
        this(utils, input1, input1Count, TooltipNode.empty(), input2, input2Count, TooltipNode.empty(), output, outputCount, TooltipNode.empty(), maxUses, xp, priceMultiplier, condition);
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
        inputCount = 2;
        tooltip = TooltipBuilder.array((b) -> b
                .add(condition)
                .add(utils.getValueTooltip(utils, maxUses).build(Lang.Value.USES))
                .add(utils.getValueTooltip(utils, xp).build(Lang.Value.VILLAGER_XP))
                .add(utils.getValueTooltip(utils, priceMultiplier).build(Lang.Value.PRICE_MULTIPLIER))
        ).build();
    }

    public ItemsToItemsNode(IClientUtils utils, FriendlyByteBuf buf) {
        super(utils, buf);
        tooltip = utils.getTooltipCache().getNodeById(buf.readVarInt());

        int encodedInputCount = buf.readVarInt();

        if (encodedInputCount >= nodes().size()) {
            LOGGER.warn("Trade declares {} cost(s) but decoded {} child node(s), treating the last one as its result",
                    encodedInputCount, nodes().size());
            inputCount = Math.max(0, nodes().size() - 1);
        } else {
            inputCount = encodedInputCount;
        }
    }

    @NotNull
    @Override
    public List<ItemStack> getInputItems() {
        return collectItems(0, inputCount);
    }

    @NotNull
    @Override
    public List<ItemStack> getOutputItems() {
        return collectItems(inputCount, nodes().size());
    }

    /** A trade missing one of its inputs or its result is not a cheaper trade - it is a wrong one. */
    @Override
    protected boolean requiresAllChildren() {
        return true;
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
        buf.writeVarInt(utils.getTooltipCache().getNodeId(tooltip));
        buf.writeVarInt(inputCount);
    }

    @NotNull
    private List<ItemStack> collectItems(int fromIndex, int toIndex) {
        List<IDataNode> nodes = nodes();
        List<ItemStack> items = new ArrayList<>();

        for (int i = Math.max(0, fromIndex); i < Math.min(toIndex, nodes.size()); i++) {
            if (nodes.get(i) instanceof IItemNode itemNode) {
                items.addAll(itemNode.getItems());
            }
        }

        return items;
    }

    private static IDataNode getChildren(Either<ItemStack, TagKey<? extends ItemLike>> item, RangeValue count, TooltipNode condition) {
        return item.map(
                (i) -> new ItemNode(1, count, i, condition, Collections.emptyList(), Collections.emptyList()),
                (t) -> new ItemNode(1, count, t, condition, Collections.emptyList(), Collections.emptyList())
        );
    }
}
