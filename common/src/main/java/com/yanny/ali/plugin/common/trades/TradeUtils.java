package com.yanny.ali.plugin.common.trades;

import com.mojang.datafixers.util.Either;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.plugin.common.tooltip.EmptyTooltipNode;
import com.yanny.ali.plugin.server.GenericTooltipUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.VillagerTrade;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.List;

public class TradeUtils {
    @NotNull
    public static ItemsToItemsNode getNode(IServerUtils utils, VillagerTrade trade) {
        return new ItemsToItemsNode(
                utils,
                Either.left(trade.wants.item().value().getDefaultInstance()),
                new RangeValue(utils.convertNumber(utils, trade.wants.count())),
                utils.getValueTooltip(utils, trade.wants.components()).build("ali.property.branch.expected_components"),
                Either.left(trade.additionalWants.map((t) -> t.item().value().getDefaultInstance()).orElse(ItemStack.EMPTY)),
                trade.additionalWants.map((t) -> utils.convertNumber(utils, t.count())).orElse(new RangeValue()),
                trade.additionalWants.map((t) -> utils.getValueTooltip(utils, t.components())).orElse(EmptyTooltipNode.empty()).build("ali.property.branch.expected_components"),
                Either.left(trade.gives.create()),
                new RangeValue(trade.gives.count()),
                GenericTooltipUtils.getFunctionListTooltip(utils, trade.givenItemModifiers),
                utils.convertNumber(utils, trade.maxUses),
                utils.convertNumber(utils, trade.xp),
                utils.getValueTooltip(utils, trade.doubleTradePriceEnchantments).build("ali.property.branch.double_trade_price_enchantments")
        );
    }

    @NotNull
    public static Pair<List<Item>, List<Item>> collectItems(IServerUtils utils, VillagerTrade trade) {
        List<Item> inputs = new ArrayList<>();
        List<Item> outputs = new ArrayList<>();

        inputs.add(trade.wants.item().value());
        trade.additionalWants.ifPresent((tradeCost) -> inputs.add(tradeCost.item().value()));
        outputs.add(trade.gives.item().value());

        return new Pair<>(inputs, outputs);
    }
}
