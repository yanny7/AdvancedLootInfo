package com.yanny.ali.plugin.common.trades;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.api.NumberExpr;
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
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemsToItemsNode extends ListNode implements ITradeNode {
    public static final Identifier ID = Utils.modLoc("items_to_items");
    private static final Logger LOGGER = CommonLogUtils.getLogger(Utils.MOD_ID);

    private final TooltipNode tooltip;
    private final int inputCount;

    public ItemsToItemsNode(IServerUtils utils,
                            Either<ItemStack, TagKey<? extends ItemLike>> input1,
                            NumberExpr input1Count,
                            Either<ItemStack, TagKey<? extends ItemLike>> output,
                            NumberExpr outputCount,
                            int maxUses,
                            int xp,
                            float priceMultiplier,
                            TooltipNode condition) {
        this(utils, input1, input1Count, Either.left(ItemStack.EMPTY), NumberExpr.constant(1), output, outputCount, maxUses, xp, priceMultiplier, condition);
    }

    public ItemsToItemsNode(IServerUtils utils,
                            Either<ItemStack, TagKey<? extends ItemLike>> input1,
                            NumberExpr input1Count,
                            Either<ItemStack, TagKey<? extends ItemLike>> input2,
                            NumberExpr input2Count,
                            Either<ItemStack, TagKey<? extends ItemLike>> output,
                            NumberExpr outputCount,
                            int maxUses,
                            int xp,
                            float priceMultiplier,
                            TooltipNode condition) {
        this(utils, input1, input1Count, TooltipNode.empty(), input2, input2Count, TooltipNode.empty(), output, outputCount, TooltipNode.empty(), maxUses, xp, priceMultiplier, condition);
    }

    public ItemsToItemsNode(IServerUtils utils,
                            Either<ItemStack, TagKey<? extends ItemLike>> input1,
                            NumberExpr input1Count,
                            TooltipNode input1Condition,
                            Either<ItemStack, TagKey<? extends ItemLike>> input2,
                            NumberExpr input2Count,
                            TooltipNode input2Condition,
                            Either<ItemStack, TagKey<? extends ItemLike>> output,
                            NumberExpr outputCount,
                            TooltipNode outputCondition,
                            int maxUses,
                            int xp,
                            float priceMultiplier,
                            TooltipNode condition) {
        this(utils,
                getChildren(input1, input1Count, input1Condition),
                getChildren(input2, input2Count, input2Condition),
                getChildren(output, outputCount, outputCondition),
                maxUses,
                xp,
                priceMultiplier,
                condition);
    }

    public ItemsToItemsNode(IServerUtils utils, IDataNode costA, IDataNode costB, IDataNode result, int maxUses, int xp, float priceMultiplier, TooltipNode condition) {
        this(costA, costB, result, TooltipBuilder.array((b) -> b
                .add(condition)
                .add(utils.getValueTooltip(utils, maxUses).build(Lang.Value.USES))
                .add(utils.getValueTooltip(utils, xp).build(Lang.Value.XP))
                .add(utils.getValueTooltip(utils, priceMultiplier).build(Lang.Value.PRICE_MULTIPLIER))
        ).build());
    }

    public ItemsToItemsNode(IDataNode costA, IDataNode costB, IDataNode result, TooltipNode tooltip) {
        addChildren(costA);
        addChildren(costB);
        addChildren(result);
        inputCount = 2;
        this.tooltip = tooltip;
    }

    public ItemsToItemsNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
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

    @Override
    protected boolean isOrdered() {
        return true;
    }

    @NotNull
    public List<IDataNode> getSlotOptions(int index) {
        List<IDataNode> options = new ArrayList<>();

        if (index < nodes().size()) {
            collectOptions(nodes().get(index), options);
        }

        return options;
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
        buf.writeVarInt(inputCount);
    }

    @NotNull
    private List<ItemStack> collectItems(int fromIndex, int toIndex) {
        List<ItemStack> items = new ArrayList<>();

        for (int i = Math.max(0, fromIndex); i < Math.min(toIndex, nodes().size()); i++) {
            for (IDataNode option : getSlotOptions(i)) {
                items.addAll(((IItemNode) option).getItems());
            }
        }

        return items;
    }

    private static void collectOptions(IDataNode node, List<IDataNode> options) {
        if (node instanceof IItemNode) {
            options.add(node);
        } else if (node instanceof ListNode listNode) {
            for (IDataNode child : listNode.nodes()) {
                collectOptions(child, options);
            }
        }
    }

    private static IDataNode getChildren(Either<ItemStack, TagKey<? extends ItemLike>> item, NumberExpr count, TooltipNode condition) {
        return item.map(
                (i) -> new ItemNode(1, count, i, condition, Collections.emptyList(), Collections.emptyList()),
                (t) -> new ItemNode(1, count, t, condition, Collections.emptyList(), Collections.emptyList())
        );
    }
}
