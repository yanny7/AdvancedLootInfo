package com.yanny.ali.plugin.server.bytecode;

import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.TradeLevel;
import com.yanny.ali.api.TradeLevelInfo;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import com.yanny.ali.plugin.common.nodes.MissingNode;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.ali.plugin.common.trades.TradeGroupNode;
import com.yanny.ali.plugin.common.trades.TradeUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

final class Renderer {
    private static final Logger LOGGER = CommonLogUtils.getLogger(Utils.MOD_ID);
    private static final Set<String> FAILED = ConcurrentHashMap.newKeySet();
    private static final boolean LOG_TRADES = Boolean.getBoolean("ali.debug.trades");

    private Renderer() {}

    @Nullable
    static IDataNode listing(IServerUtils utils, String name, Aggregation.Listing listing, TooltipNode condition) {
        String reason = listing.reason();

        try {
            if (!listing.offers().isEmpty()) {
                List<IDataNode> nodes = new ArrayList<>();
                TooltipNode outer = reason == null ? condition : withIncomplete(condition);

                for (Aggregation.Offer offer : listing.offers()) {
                    PathCondition when = offer.when();

                    if (LOG_TRADES) {
                        LOGGER.info("Trade item listing {}: {}{}", name, offer.text(), when == PathCondition.ALWAYS ? "" : " if " + when.text());
                    }

                    TooltipNode tooltip = listing.offers().size() > 1 ? when.tooltip(utils) : combine(outer, when.tooltip(utils), when);

                    nodes.add(node(utils, offer.result(), tooltip));
                }

                if (reason != null && utils.getConfiguration().logMoreStatistics && FAILED.add(name + reason)) {
                    LOGGER.info("Computed only some variants of trade item listing {} from its bytecode: {}", name, reason);
                }

                return nodes.size() == 1 ? nodes.get(0) : new ConditionalTradesNode(utils, nodes, outer);
            }
        } catch (Fail fail) {
            reason = fail.describe();
        } catch (Throwable e) {
            reason = e.toString();
        }

        if (utils.getConfiguration().logMoreStatistics && FAILED.add(name + reason)) {
            LOGGER.info("Could not work out trade item listing {} from its bytecode: {}", name, reason);
        }

        return null;
    }

    @NotNull
    static BytecodeInterpreter.ComputedTrades levels(IServerUtils utils, String name, Aggregation.Trader trader) {
        Int2ObjectMap<TradeLevel> levels = new Int2ObjectOpenHashMap<>();
        Set<Integer> incomplete = new TreeSet<>(trader.incomplete());
        boolean unknownLevel = trader.unknownLevel();
        String reason = trader.reason();

        try {
            for (Map.Entry<Integer, Aggregation.Level> level : trader.levels().entrySet()) {
                TradeLevel tradeLevel = tradeLevel(level.getValue());

                levels.put((int) level.getKey(), tradeLevel);

                if (LOG_TRADES) {
                    logLevel(name, level.getKey(), level.getValue().groups(), tradeLevel);
                }
            }
        } catch (Fail fail) {
            reason = fail.describe();
            unknownLevel = true;
        } catch (Throwable e) {
            reason = e.toString();
            unknownLevel = true;
        }

        if (reason == null) {
            return new BytecodeInterpreter.ComputedTrades(levels, false);
        }

        if (utils.getConfiguration().logMoreStatistics && FAILED.add(name + reason)) {
            LOGGER.info("Could not auto-detect all trades of trading entity {} from its bytecode: {}", name, reason);
        }

        if (unknownLevel) {
            incomplete.addAll(levels.keySet());
        }

        if (LOG_TRADES) {
            if (unknownLevel) {
                LOGGER.info("Trades of trading entity {}: incomplete at an unknown level, {}", name, reason);
            }

            for (int level : incomplete) {
                LOGGER.info("Trades of trading entity {} level {}: incomplete, {}", name, level, reason);
            }
        }

        for (int level : incomplete) {
            levels.put(level, incomplete(levels.get(level), name));
        }

        return new BytecodeInterpreter.ComputedTrades(levels, true);
    }

    static void clearCaches() {
        FAILED.clear();
    }

    private static TooltipNode withIncomplete(TooltipNode condition) {
        return TooltipBuilder.array((b) -> {
            b.add(condition);
            b.add(TooltipBuilder.error(TooltipBuilder.translate(Lang.Description.INCOMPLETE_TRADE.singular())).build());
        }).build();
    }

    @NotNull
    private static IDataNode node(IServerUtils utils, Value result, TooltipNode condition) {
        if (result instanceof Value.Known known && known.value() instanceof MerchantOffer offer) {
            return TradeUtils.getNode(utils, offer, condition);
        }
        if (result instanceof Value.Offer offer) {
            return new ItemsToItemsNode(utils, slot(utils, offer.costA()), slot(utils, offer.costB()), slot(utils, offer.result()),
                    offer.maxUses(), offer.xp(), offer.priceMultiplier(), condition);
        }

        throw new IllegalArgumentException("Unknown offer type " + result);
    }

    private static TooltipNode combine(TooltipNode condition, TooltipNode tooltip, PathCondition when) {
        if (when == PathCondition.ALWAYS) {
            return condition;
        }

        return TooltipBuilder.array((b) -> {
            b.add(condition);
            b.add(tooltip);
        }).build();
    }

    @NotNull
    private static TradeLevel incomplete(@Nullable TradeLevel level, String merchant) {
        if (level instanceof TradeLevel.OfListings listings) {
            return new TradeLevel.OfTrades(listings.levelInfo(), (u) -> {
                List<IDataNode> nodes = new ArrayList<>(Arrays.stream(TradeUtils.distinctListings(listings.listings())).filter(Objects::nonNull).map((l) -> u.getItemListing(u, l, TooltipNode.empty())).toList());

                nodes.add(incompleteNode());
                return nodes;
            });
        }
        if (level instanceof TradeLevel.OfTrades trades) {
            return new TradeLevel.OfTrades(trades.levelInfo(), (u) -> {
                List<IDataNode> nodes = new ArrayList<>(trades.trades().apply(u));

                nodes.add(incompleteNode());
                return nodes;
            });
        }

        return new TradeLevel.OfTrades(new TradeLevelInfo(NumberExpr.opaque(merchant)), (u) -> List.of(incompleteNode()));
    }

    @NotNull
    private static IDataNode incompleteNode() {
        return new MissingNode(TooltipBuilder.error(TooltipBuilder.translate(Lang.Description.INCOMPLETE_TRADES.singular())).build());
    }

    @NotNull
    private static TradeLevel tradeLevel(Aggregation.Level level) {
        List<Aggregation.Group> groups = level.groups();

        if (groups.size() == 1 && groups.get(0).entry() instanceof Value.Offers.Listings listings && groups.get(0).condition() == PathCondition.ALWAYS) {
            return new TradeLevel.OfListings(listings.listings(), new TradeLevelInfo(groups.get(0).count(), level.chance()));
        }

        NumberExpr total = groups.stream().map(Aggregation.Group::count).reduce(NumberExpr.constant(0), NumberExpr::add);

        return new TradeLevel.OfTrades(new TradeLevelInfo(total, level.chance()), (u) -> {
            List<IDataNode> nodes = new ArrayList<>();

            for (Aggregation.Group group : groups) {
                TooltipNode condition = group.condition().tooltip(u);

                if (!(group.entry() instanceof Value.Offers.Single single)) {
                    List<IDataNode> trades;

                    if (group.entry() instanceof Value.Offers.Listings listings) {
                        trades = Arrays.stream(TradeUtils.distinctListings(listings.listings())).filter(Objects::nonNull).map((l) -> u.getItemListing(u, l, condition)).toList();
                    } else {
                        trades = ((Value.Offers.Picked) group.entry()).offers().stream().map((o) -> node(u, new Value.Known(o), condition)).toList();
                    }

                    if (shownAsGroup(groups.size(), group.count(), trades.size())) {
                        nodes.add(new TradeGroupNode(group.count(), trades));
                    } else {
                        nodes.addAll(trades);
                    }
                } else {
                    IDataNode trade = node(u, single.offer(), condition);

                    nodes.add(group.times() > 1 ? TradeGroupNode.repeated(group.count(), trade) : trade);
                }
            }

            return nodes;
        });
    }

    private static boolean shownAsGroup(int entries, NumberExpr count, int trades) {
        return entries > 1 && count.bounds().lo() < trades;
    }

    private static void logLevel(String name, int level, List<Aggregation.Group> groups, TradeLevel tradeLevel) {
        TradeLevelInfo info = tradeLevel instanceof TradeLevel.OfListings listings ? listings.levelInfo() : ((TradeLevel.OfTrades) tradeLevel).levelInfo();

        LOGGER.info("Trades of trading entity {} level {}: {}, chance {}, {} offers", name, level, tradeLevel instanceof TradeLevel.OfListings ? "OfListings" : "OfTrades",
                info.chance(), info.offers());

        for (int i = 0; i < groups.size(); i++) {
            Aggregation.Group group = groups.get(i);
            PathCondition when = group.condition();
            NumberExpr count = group.count();
            String kind;
            String shown;
            List<String> trades;

            if (group.entry() instanceof Value.Offers.Listings listings) {
                trades = Arrays.stream(TradeUtils.distinctListings(listings.listings())).filter(Objects::nonNull).map((l) -> keyText(TradeUtils.listingKey(l))).toList();
                kind = "listings";
                shown = shownAsGroup(groups.size(), count, trades.size()) ? "shown as a group" : "shown as plain trades";
            } else if (group.entry() instanceof Value.Offers.Picked picked) {
                trades = picked.offers().stream().map((o) -> String.valueOf(Aggregation.describeOffer(new Value.Known(o)))).toList();
                kind = "picked offers";
                shown = shownAsGroup(groups.size(), count, trades.size()) ? "shown as a group" : "shown as plain trades";
            } else {
                trades = List.of(String.valueOf(Aggregation.describeOffer(((Value.Offers.Single) group.entry()).offer())));
                kind = "single";
                shown = group.times() > 1 ? "shown repeated" : "shown as a plain trade";
            }

            LOGGER.info("Trades of trading entity {} level {} group {}: {}, {} offers, {}{}: {}", name, level, i, kind, count, shown,
                    when == PathCondition.ALWAYS ? "" : " if " + when.text(), trades.stream().sorted().collect(Collectors.joining("; ")));
        }
    }

    @NotNull
    private static String keyText(@Nullable Object key) {
        if (key instanceof Class<?> type) {
            return className(type);
        }
        if (key instanceof List<?> list) {
            return list.stream().map(Renderer::keyText).collect(Collectors.joining(", ", "[", "]"));
        }
        if (key instanceof Set<?> set) {
            return set.stream().map(Renderer::keyText).sorted().collect(Collectors.joining(", ", "{", "}"));
        }
        if (key instanceof Map<?, ?> map) {
            return map.entrySet().stream().map((e) -> keyText(e.getKey()) + "=" + keyText(e.getValue())).sorted().collect(Collectors.joining(", ", "{", "}"));
        }

        return leafText(key);
    }

    @SuppressWarnings("unchecked")
    @NotNull
    private static String leafText(@Nullable Object value) {
        if (value == null) {
            return "null";
        }

        for (Registry<?> registry : BuiltInRegistries.REGISTRY) {
            Optional<ResourceKey<Object>> key = ((Registry<Object>) registry).getResourceKey(value);

            if (key.isPresent()) {
                return key.get().toString();
            }
        }

        try {
            if (value.getClass().isHidden() || value.getClass().getMethod("toString").getDeclaringClass() == Object.class) {
                return className(value.getClass());
            }
        } catch (NoSuchMethodException ignored) {}

        return String.valueOf(value);
    }

    @NotNull
    private static String className(Class<?> type) {
        int lambda = type.getName().indexOf("$$Lambda");

        return lambda >= 0 ? type.getName().substring(0, lambda) + "$$Lambda" : type.getName();
    }

    private static IDataNode slot(IServerUtils utils, Value value) {
        if (value instanceof Value.Known known && known.value() instanceof ItemStack stack) {
            return new ItemNode(1, NumberExpr.constant(stack.getCount()), stack, TooltipNode.empty(), List.of(), List.of());
        }
        if (value instanceof Value.Stack stack) {
            List<ItemStack> options = stack.templates();
            TooltipNode tooltip = tooltip(utils, stack);

            if (options.size() == 1) {
                return new ItemNode(1, stack.count, options.get(0), tooltip, List.of(), List.of());
            }

            return TradeUtils.getItemSlotNode(options, stack.count, tooltip);
        }
        if (value instanceof Value.Choice choice && choice.options().stream().allMatch(ItemStack.class::isInstance)) {
            return TradeUtils.getSlotNode(choice.options().stream().<IDataNode>map((o) -> {
                ItemStack stack = (ItemStack) o;

                return new ItemNode(1, NumberExpr.constant(stack.getCount()), stack, TooltipNode.empty(), List.of(), List.of());
            }).toList());
        }

        throw new Fail("trade slot");
    }

    private static TooltipNode tooltip(IServerUtils utils, Value.Stack stack) {
        if (stack.destination == null && stack.decoration == null) {
            return stack.randomTag ? TooltipBuilder.keyOnly(Lang.Description.RANDOM_PROPERTIES).build() : TooltipNode.empty();
        }

        return TooltipBuilder.array((b) -> {
            if (stack.randomTag) {
                b.add(TooltipBuilder.keyOnly(Lang.Description.RANDOM_PROPERTIES).build());
            }
            if (stack.destination != null) {
                b.add(utils.getValueTooltip(utils, stack.destination).build(Lang.Value.DESTINATION));
            }
            if (stack.decoration != null) {
                b.add(utils.getValueTooltip(utils, stack.decoration).build(Lang.Value.MAP_DECORATION));
            }
        }).build();
    }
}
