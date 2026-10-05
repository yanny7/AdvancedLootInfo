package com.yanny.alicompat.compat.undergarden;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.TradeLevel;
import com.yanny.ali.api.TradeLevelInfo;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.alicompat.IModCompat;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import quek.undergarden.UGRegistries;
import quek.undergarden.component.predicate.InfectionConsumeEffectPredicate;
import quek.undergarden.entity.monster.stoneborn.trading.StonebornTrade;
import quek.undergarden.entity.monster.stoneborn.trading.StonebornTradeSet;
import quek.undergarden.item.consumeeffects.ModifyUthericInfectionConsumeEffect;
import quek.undergarden.registry.custom.UGStonebornTradeSets;

import java.util.ArrayList;

public class UndergardenCompat implements IModCompat {
    private static final Identifier STONEBORN = Identifier.fromNamespaceAndPath(UndergardenLang.MOD_ID, "stoneborn");

    @NotNull
    @Override
    public String targetModId() {
        return UndergardenLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerConsumeEffectTooltip(ModifyUthericInfectionConsumeEffect.class, UndergardenCompat::getModifyUthericInfectionTooltip);
        registry.registerDataComponentPredicateTooltip(InfectionConsumeEffectPredicate.class, UndergardenCompat::getInfectionConsumeEffectPredicateTooltip);
        registry.registerTrades(STONEBORN, BuiltInRegistries.ENTITY_TYPE.getValue(STONEBORN), UndergardenCompat::getStonebornLevels);
    }

    @NotNull
    private static TooltipBuilder getModifyUthericInfectionTooltip(IServerUtils utils, ModifyUthericInfectionConsumeEffect effect) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, effect.value()).build(Lang.Value.VALUE));
            b.add(utils.getValueTooltip(utils, effect.addToExisting()).build(Lang.Value.ADD));
        }, UndergardenLang.ConsumeEffects.MODIFY_UTHERIC_INFECTION);
    }

    @NotNull
    private static TooltipBuilder getInfectionConsumeEffectPredicateTooltip(IServerUtils utils, InfectionConsumeEffectPredicate predicate) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, predicate.amount()).build(Lang.Value.AMOUNT)), UndergardenLang.ItemSubPredicates.INFECTION_CONSUME_EFFECT);
    }

    @NotNull
    private static Int2ObjectMap<TradeLevel> getStonebornLevels(IServerUtils utils) {
        Int2ObjectMap<TradeLevel> levels = new Int2ObjectOpenHashMap<>();

        utils.lookupProvider().lookup(UGRegistries.Keys.STONEBORN_TRADE_SET)
                .flatMap((lookup) -> lookup.get(UGStonebornTradeSets.VAGABOND))
                .ifPresent((tradeSet) -> levels.put(1, getLevel(utils, tradeSet.value())));
        return levels;
    }

    @NotNull
    private static TradeLevel getLevel(IServerUtils utils, StonebornTradeSet tradeSet) {
        TradeLevelInfo levelInfo = new TradeLevelInfo(utils.convertIntNumber(utils, tradeSet.amount(), new ArrayList<>()));

        return new TradeLevel.OfTrades(levelInfo, (u) -> tradeSet.trades().stream().<IDataNode>map((trade) -> getNode(u, trade.value())).toList());
    }

    @NotNull
    private static ItemsToItemsNode getNode(IServerUtils utils, StonebornTrade trade) {
        return new ItemsToItemsNode(
                utils,
                Either.left(trade.wants().item().value().getDefaultInstance()),
                utils.convertNumber(utils, trade.wants().count(), new ArrayList<>()),
                utils.getValueTooltip(utils, trade.wants().components()).build(Lang.Branch.EXPECTED_COMPONENTS),
                Either.left(trade.additionalWants().map((t) -> t.item().value().getDefaultInstance()).orElse(ItemStack.EMPTY)),
                trade.additionalWants().map((t) -> utils.convertNumber(utils, t.count(), new ArrayList<>())).orElse(NumberExpr.constant(1)),
                trade.additionalWants().map((t) -> utils.getValueTooltip(utils, t.components())).orElse(TooltipBuilder.empty()).build(Lang.Branch.EXPECTED_COMPONENTS),
                Either.left(trade.gives().create()),
                NumberExpr.constant(trade.gives().count()),
                utils.getValueTooltip(utils, trade.givenItemModifiers()).build(),
                utils.convertNumber(utils, trade.maxUses(), new ArrayList<>()),
                utils.convertNumber(utils, trade.xp(), new ArrayList<>()),
                utils.getValueTooltip(utils, trade.merchantPredicate()).build(Lang.Branch.PREDICATES)
        );
    }
}
