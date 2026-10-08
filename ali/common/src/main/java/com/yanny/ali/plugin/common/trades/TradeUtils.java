package com.yanny.ali.plugin.common.trades;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.GroupNode;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.VillagerTrade;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.List;

public class TradeUtils {
    @NotNull
    public static IDataNode getSlotNode(IServerUtils utils, LootPoolEntryContainer entry) {
        List<TooltipNode> conditions = new ArrayList<>();
        NumberExpr sumWeight = NodeUtils.getTotalWeight(utils, List.of(entry), conditions);

        return NodeUtils.getChildren(utils, List.of(entry), NumberExpr.constant(1), sumWeight, conditions, List.of(), List.of()).get(0);
    }

    @NotNull
    public static IDataNode getSlotNode(List<IDataNode> options) {
        return options.size() == 1 ? options.get(0) : new GroupNode(options, TooltipNode.empty());
    }

    @NotNull
    public static IDataNode getItemSlotNode(List<ItemStack> stacks, NumberExpr count, TooltipNode tooltip) {
        return getSlotNode(stacks.stream().<IDataNode>map((s) -> new ItemNode(1, count, s, tooltip, List.of(), List.of())).toList());
    }

    @NotNull
    public static IDataNode getEmptySlotNode() {
        return new ItemNode(1, NumberExpr.constant(1), ItemStack.EMPTY, TooltipNode.empty(), List.of(), List.of());
    }

    @NotNull
    public static ItemsToItemsNode getNode(IServerUtils utils, VillagerTrade trade) {
        return new ItemsToItemsNode(
                utils,
                Either.left(trade.wants.item().value().getDefaultInstance()),
                utils.convertNumber(utils, trade.wants.count(), new ArrayList<>()),
                utils.getValueTooltip(utils, trade.wants.components()).build(Lang.Branch.EXPECTED_COMPONENTS),
                Either.left(trade.additionalWants.map((t) -> t.item().value().getDefaultInstance()).orElse(ItemStack.EMPTY)),
                trade.additionalWants.map((t) -> utils.convertNumber(utils, t.count(), new ArrayList<>())).orElse(NumberExpr.constant(1)),
                trade.additionalWants.map((t) -> utils.getValueTooltip(utils, t.components())).orElse(TooltipBuilder.empty()).build(Lang.Branch.EXPECTED_COMPONENTS),
                Either.left(trade.gives.create()),
                NumberExpr.constant(trade.gives.count()),
                utils.getValueTooltip(utils, trade.givenItemModifiers).build(),
                utils.convertNumber(utils, trade.maxUses, new ArrayList<>()),
                utils.convertNumber(utils, trade.xp, new ArrayList<>()),
                utils.getValueTooltip(utils, trade.doubleTradePriceEnchantments).build(Lang.Branch.DOUBLE_TRADE_PRICE_ENCHANTMENTS)
        );
    }

    @NotNull
    public static Pair<List<Item>, List<Item>> collectItems(IServerUtils ignoredUtils, VillagerTrade trade) {
        List<Item> inputs = new ArrayList<>();
        List<Item> outputs = new ArrayList<>();

        inputs.add(trade.wants.item().value());
        trade.additionalWants.ifPresent((tradeCost) -> inputs.add(tradeCost.item().value()));
        outputs.add(trade.gives.item().value());

        return new Pair<>(inputs, outputs);
    }
}
