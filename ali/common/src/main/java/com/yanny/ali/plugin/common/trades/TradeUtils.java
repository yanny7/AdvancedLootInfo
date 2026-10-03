package com.yanny.ali.plugin.common.trades;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.server.LootCount;
import com.yanny.ali.plugin.server.TooltipUtils;
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
                TooltipUtils.collectConditions((c) -> utils.convertContextInt(utils, trade.wants.count(), c)),
                utils.getValueTooltip(utils, trade.wants.components()).build(Lang.Branch.EXPECTED_COMPONENTS),
                Either.left(trade.additionalWants.map((t) -> t.item().value().getDefaultInstance()).orElse(ItemStack.EMPTY)),
                trade.additionalWants.map((t) -> TooltipUtils.collectConditions((c) -> utils.convertContextInt(utils, t.count(), c))).orElse(LootCount.of(NumberExpr.constant(1))),
                trade.additionalWants.map((t) -> utils.getValueTooltip(utils, t.components())).orElse(TooltipBuilder.empty()).build(Lang.Branch.EXPECTED_COMPONENTS),
                Either.left(trade.gives.create()),
                NumberExpr.constant(trade.gives.count()),
                utils.getValueTooltip(utils, trade.givenItemModifier).build(),
                TooltipUtils.collectConditions((c) -> utils.convertContextInt(utils, trade.maxUses, c)),
                TooltipUtils.collectConditions((c) -> utils.convertContextInt(utils, trade.xp, c)),
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
