package com.yanny.alicompat.compat.villagertradingplus;

import com.lion.villagertradingplus.VillagerTradingPlus;
import com.lion.villagertradingplus.config.VTPConfig;
import com.lion.villagertradingplus.tradeoffers.ConditionalTradeFactory;
import com.lion.villagertradingplus.tradeoffers.RegistryRebindFactory;
import com.lion.villagertradingplus.tradeoffers.TradeFactory;
import com.lion.villagertradingplus.tradeoffers.TradePools;
import com.lion.villagertradingplus.tradeoffers.TradeSnapshot;
import com.lion.villagertradingplus.tradeoffers.WanderingTraderTradeLoader;
import com.lion.villagertradingplus.tradeoffers.conditions.ParsedConditions;
import com.lion.villagertradingplus.tradeoffers.conditions.TradeCondition;
import com.mojang.datafixers.util.Either;
import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.TradeLevel;
import com.yanny.ali.api.TradeLevelInfo;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.nodes.MissingNode;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.ali.plugin.common.trades.TradeUtils;
import com.yanny.ali.plugin.server.LootCount;
import com.yanny.ali.plugin.server.MissingTooltipUtils;
import com.yanny.ali.plugin.server.TooltipUtils;
import com.yanny.alicompat.Utils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.ClassAccessor;
import com.yanny.alicompat.accessor.ReflectionUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.item.trading.TradeSets;
import net.minecraft.world.item.trading.VillagerTrade;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class VillagerTradingPlusTrades {
    private static final Logger LOGGER = CommonLogUtils.getLogger(Utils.MOD_ID);
    private static final Identifier WANDERING_TRADER = Identifier.withDefaultNamespace("wandering_trader");
    private static final int MAX_LEVEL = 5;
    private static final int VANILLA_OFFERS = 2;
    private static final int WANDERING_BUYING = 0;
    private static final int WANDERING_COMMON = 1;
    private static final int WANDERING_UNCOMMON = 2;
    private static final Map<Class<?>, Function<TradeFactory, ITradeFactoryAccessor>> ACCESSORS = new HashMap<>();

    public static <T extends BaseAccessor<TradeFactory> & ITradeFactoryAccessor> void registerAccessor(Class<T> clazz) {
        ClassAccessor annotation = clazz.getAnnotation(ClassAccessor.class);

        try {
            Class<?> targetClass = Class.forName(annotation.value());

            ReflectionUtils.validate(clazz, targetClass);
            ACCESSORS.put(targetClass, (f) -> ReflectionUtils.copyClassData(clazz, f));
        } catch (Throwable e) {
            LOGGER.warn("Failed to register trade factory {} with error {}", annotation.value(), e.getMessage());
        }
    }

    @Nullable
    public static Int2ObjectMap<TradeLevel> getTradeLevels(IServerUtils utils, Identifier traderId) {
        if (traderId.equals(WANDERING_TRADER)) {
            if (TradePools.drawsWanderingFromPool() || isPricingActive()) {
                return getWanderingTraderLevels(utils);
            }

            return null;
        }

        Optional<Holder.Reference<VillagerProfession>> profession = BuiltInRegistries.VILLAGER_PROFESSION.get(traderId);

        if (profession.isPresent() && (TradePools.drawsFromPool(profession.get()) || isPricingActive())) {
            return getVillagerLevels(utils, profession.get());
        }

        return null;
    }

    @NotNull
    public static IDataNode getNode(IServerUtils utils, TradeFactory factory, TooltipNode conditions) {
        return switch (factory) {
            case ConditionalTradeFactory conditional -> getNode(utils, conditional.delegate(), TooltipBuilder.array((b) -> {
                b.add(conditions);
                b.add(utils.getValueTooltip(utils, conditional.condition()).build(isAnyOf(conditional.condition()) ? Lang.Conditions.ANY_OF : Lang.Conditions.ALL_OF));
            }).build());
            case RegistryRebindFactory rebind -> getNode(utils, rebind.delegate(), conditions);
            default -> {
                Function<TradeFactory, ITradeFactoryAccessor> accessor = ACCESSORS.get(factory.getClass());

                if (accessor == null) {
                    yield new MissingNode(MissingTooltipUtils.getMissingValueTooltip(utils, factory).build());
                }

                yield accessor.apply(factory).getNode(utils, conditions);
            }
        };
    }

    @NotNull
    public static IDataNode getTradeNode(IServerUtils utils,
                                         Either<ItemStack, TagKey<? extends ItemLike>> input,
                                         NumberExpr inputCount,
                                         Either<ItemStack, TagKey<? extends ItemLike>> output,
                                         NumberExpr outputCount,
                                         int maxUses,
                                         int xp,
                                         float multiplier,
                                         TooltipNode conditions) {
        return getTradeNode(utils, input, inputCount, Either.left(ItemStack.EMPTY), NumberExpr.constant(1), output, outputCount, maxUses, xp, multiplier, conditions);
    }

    @NotNull
    public static IDataNode getTradeNode(IServerUtils utils,
                                         Either<ItemStack, TagKey<? extends ItemLike>> input1,
                                         NumberExpr input1Count,
                                         Either<ItemStack, TagKey<? extends ItemLike>> input2,
                                         NumberExpr input2Count,
                                         Either<ItemStack, TagKey<? extends ItemLike>> output,
                                         NumberExpr outputCount,
                                         int maxUses,
                                         int xp,
                                         float multiplier,
                                         TooltipNode conditions) {
        return getTradeNode(utils, input1, input1Count, TooltipNode.empty(), input2, input2Count, TooltipNode.empty(), output, outputCount, TooltipNode.empty(), maxUses, xp, multiplier, conditions);
    }

    @NotNull
    public static IDataNode getTradeNode(IServerUtils utils,
                                         Either<ItemStack, TagKey<? extends ItemLike>> input1,
                                         NumberExpr input1Count,
                                         TooltipNode input1Condition,
                                         Either<ItemStack, TagKey<? extends ItemLike>> input2,
                                         NumberExpr input2Count,
                                         TooltipNode input2Condition,
                                         Either<ItemStack, TagKey<? extends ItemLike>> output,
                                         NumberExpr outputCount,
                                         TooltipNode outputModifier,
                                         int maxUses,
                                         int xp,
                                         float multiplier,
                                         TooltipNode conditions) {
        return new ItemsToItemsNode(
                utils,
                input1,
                input1Count,
                input1Condition,
                input2,
                input2Count,
                input2Condition,
                output,
                outputCount,
                outputModifier,
                NumberExpr.constant(maxUses),
                NumberExpr.constant(xp),
                TooltipBuilder.array((b) -> {
                    b.add(TooltipBuilder.number(NumberExpr.constant(multiplier)).build(VillagerTradingPlusLang.Value.PRICE_MULTIPLIER));
                    b.add(conditions);
                }).build()
        );
    }

    @NotNull
    public static IDataNode getTradeNode(IDataNode costA, IDataNode costB, IDataNode result, int maxUses, int xp, float multiplier, TooltipNode conditions) {
        return new ItemsToItemsNode(costA, costB, result, TooltipBuilder.array((b) -> {
            b.add(TooltipUtils.getNumberTooltip(LootCount.of(NumberExpr.constant(maxUses))).build(Lang.Value.USES));
            b.add(TooltipUtils.getNumberTooltip(LootCount.of(NumberExpr.constant(xp))).build(Lang.Value.VILLAGER_XP));
            b.add(TooltipBuilder.number(NumberExpr.constant(multiplier)).build(VillagerTradingPlusLang.Value.PRICE_MULTIPLIER));
            b.add(conditions);
        }).build());
    }

    @NotNull
    public static TooltipBuilder getParsedConditionsTooltip(IServerUtils utils, ParsedConditions cond) {
        return TooltipBuilder.array((b) -> cond.entries().forEach((e) -> b.add(utils.getValueTooltip(utils, e.description()).build())));
    }

    @NotNull
    private static Int2ObjectMap<TradeLevel> getVillagerLevels(IServerUtils utils, Holder.Reference<VillagerProfession> profession) {
        Int2ObjectMap<TradeLevel> levels = new Int2ObjectOpenHashMap<>();
        TradeSnapshot snapshot = TradePools.villagerSnapshot();
        ResourceKey<VillagerProfession> key = profession.key();
        boolean replacesVanilla = TradePools.replacesVanillaSet(snapshot, key);

        for (int level = 1; level <= MAX_LEVEL; level++) {
            Optional<TradeSet> tradeSet = getTradeSet(utils, profession.value().getTrades(level));
            List<TradeFactory> custom = new ArrayList<>(snapshot.defaultsAt(key, level));
            NumberExpr offers = getVillagerOffers(utils, profession, tradeSet);

            custom.addAll(snapshot.additionsAt(key, level));
            levels.put(level, new TradeLevel.OfTrades(new TradeLevelInfo(offers), (u) -> getTrades(u, replacesVanilla ? Optional.empty() : tradeSet, custom)));
        }

        return levels;
    }

    @NotNull
    private static NumberExpr getVillagerOffers(IServerUtils utils, Holder<VillagerProfession> profession, Optional<TradeSet> tradeSet) {
        if (!TradePools.drawsFromPool(profession)) {
            return getVanillaOffers(utils, tradeSet);
        }

        int configured = getConfig().trade_offers_per_level;

        if (tradeSet.isEmpty()) {
            return NumberExpr.constant(configured);
        }

        return NumberExpr.max(NumberExpr.constant(0), NumberExpr.add(getVanillaOffers(utils, tradeSet), NumberExpr.constant(configured - VANILLA_OFFERS)));
    }

    @NotNull
    private static Int2ObjectMap<TradeLevel> getWanderingTraderLevels(IServerUtils utils) {
        Int2ObjectMap<TradeLevel> levels = new Int2ObjectOpenHashMap<>();
        Optional<TradeSet> common = getTradeSet(utils, TradeSets.WANDERING_TRADER_COMMON);
        NumberExpr commonOffers = TradePools.drawsWanderingFromPool() ? NumberExpr.constant(getConfig().trade_offers_wandering_trader) : getVanillaOffers(utils, common);

        levels.put(1, getWanderingTraderLevel(utils, TradeSets.WANDERING_TRADER_BUYING, WANDERING_BUYING, null));
        levels.put(2, getWanderingTraderLevel(utils, TradeSets.WANDERING_TRADER_UNCOMMON, WANDERING_UNCOMMON, null));
        levels.put(3, getWanderingTraderLevel(utils, TradeSets.WANDERING_TRADER_COMMON, WANDERING_COMMON, commonOffers));
        return levels;
    }

    @NotNull
    private static TradeLevel getWanderingTraderLevel(IServerUtils utils, ResourceKey<TradeSet> key, int tier, @Nullable NumberExpr offers) {
        WanderingTraderTradeLoader.Snapshot snapshot = WanderingTraderTradeLoader.snapshot();
        List<TradeFactory> custom = snapshot.tier(tier);
        Optional<TradeSet> tradeSet = getTradeSet(utils, key);
        Optional<TradeSet> vanilla = !snapshot.replaceVanilla() || custom.isEmpty() ? tradeSet : Optional.empty();

        return new TradeLevel.OfTrades(new TradeLevelInfo(offers != null ? offers : getVanillaOffers(utils, tradeSet)), (u) -> getTrades(u, vanilla, custom));
    }

    @NotNull
    private static List<IDataNode> getTrades(IServerUtils utils, Optional<TradeSet> vanilla, List<TradeFactory> custom) {
        List<IDataNode> nodes = new ArrayList<>();
        TooltipNode pricing = getPricingTooltip(utils);

        vanilla.ifPresent((s) -> s.trades().forEach((t) -> nodes.add(getVanillaNode(utils, t.value(), pricing))));
        custom.forEach((f) -> nodes.add(getNode(utils, f, pricing)));
        return nodes;
    }

    @NotNull
    private static IDataNode getVanillaNode(IServerUtils utils, VillagerTrade trade, TooltipNode pricing) {
        ItemsToItemsNode node = TradeUtils.getNode(utils, trade);

        if (!isPricingActive()) {
            return node;
        }

        List<IDataNode> slots = node.nodes();

        return new ItemsToItemsNode(slots.get(0), slots.get(1), slots.get(2), TooltipBuilder.array((b) -> {
            b.add(node.getTooltip());
            b.add(pricing);
        }).build());
    }

    @NotNull
    private static NumberExpr getVanillaOffers(IServerUtils utils, Optional<TradeSet> tradeSet) {
        return tradeSet.map((s) -> utils.convertContextInt(utils, s.amount(), new ArrayList<>())).orElse(NumberExpr.constant(VANILLA_OFFERS));
    }

    @NotNull
    private static Optional<TradeSet> getTradeSet(IServerUtils utils, @Nullable ResourceKey<TradeSet> key) {
        if (key == null) {
            return Optional.empty();
        }

        return utils.lookupProvider().lookupOrThrow(Registries.TRADE_SET).get(key).map(Holder::value);
    }

    @NotNull
    private static TooltipNode getPricingTooltip(IServerUtils utils) {
        VTPConfig config = getConfig();

        return TooltipBuilder.array((b) -> {
            if (isCostScaled(config)) {
                b.add(utils.getValueTooltip(utils, getCostScale(config)).build(VillagerTradingPlusLang.Value.COST_SCALE));
            }
            if (config.trade_price_multiplier_scale != 1.0f) {
                b.add(TooltipBuilder.number(NumberExpr.constant(config.trade_price_multiplier_scale)).build(VillagerTradingPlusLang.Value.PRICE_MULTIPLIER_SCALE));
            }
        }).build();
    }

    @NotNull
    private static NumberExpr getCostScale(VTPConfig config) {
        if (config.enable_time_of_day_pricing) {
            return NumberExpr.range(config.trade_cost_scale * (1.0f - config.time_of_day_price_variance), config.trade_cost_scale * (1.0f + config.time_of_day_price_variance));
        }

        return NumberExpr.constant(config.trade_cost_scale);
    }

    private static boolean isPricingActive() {
        VTPConfig config = getConfig();

        return isCostScaled(config) || config.trade_price_multiplier_scale != 1.0f;
    }

    private static boolean isCostScaled(VTPConfig config) {
        return config.trade_cost_scale != 1.0f || config.enable_time_of_day_pricing;
    }

    @NotNull
    private static VTPConfig getConfig() {
        return VillagerTradingPlus.CONFIG;
    }

    private static boolean isAnyOf(TradeCondition condition) {
        return condition instanceof ParsedConditions parsed && parsed.orLogic();
    }
}
