package com.yanny.ali.plugin.server.bytecode;

import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.api.NumberInterval;
import com.yanny.aci.manager.ManagedRegistry;
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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class BytecodeInterpreter {
    private static final Logger LOGGER = CommonLogUtils.getLogger(Utils.MOD_ID);
    private static final Set<String> FAILED = ConcurrentHashMap.newKeySet();
    private static final boolean LOG_TRADES = Boolean.getBoolean("ali.debug.trades");
    private static final int MAX_PATHS = 32;

    private BytecodeInterpreter() {}

    @Nullable
    public static IDataNode getItemListing(IServerUtils utils, VillagerTrades.ItemListing listing, TooltipNode condition) {
        if (!RuntimeNames.AVAILABLE) {
            return null;
        }

        String reason;

        try {
            List<Path> paths = paths((i) -> i.call(listing, VillagerTrades.ItemListing.class, RuntimeNames.GET_OFFER.name(), RuntimeNames.GET_OFFER.desc(),
                    List.of(Value.Unknown.of(Type.getType(Entity.class), Origin.TRADER), new Value.Rng(false))), false);
            Map<String, List<Path>> offers = new LinkedHashMap<>();

            for (Path path : paths) {
                String text = describeOffer(path.result());

                if (text != null) {
                    offers.computeIfAbsent(text, (t) -> new ArrayList<>()).add(path);
                }
            }

            if (!offers.isEmpty()) {
                List<IDataNode> nodes = new ArrayList<>();

                for (Map.Entry<String, List<Path>> entry : offers.entrySet()) {
                    PathCondition when = entry.getValue().size() == paths.size() ? PathCondition.ALWAYS
                            : PathCondition.of(entry.getValue().stream().map(Path::decisions).toList());

                    if (LOG_TRADES) {
                        LOGGER.info("Trade item listing {}: {}{}", ManagedRegistry.classKeyName(listing.getClass()), entry.getKey(),
                                when == PathCondition.ALWAYS ? "" : " if " + when.text());
                    }

                    TooltipNode tooltip = offers.size() > 1 ? when.tooltip(utils) : combine(condition, when.tooltip(utils), when);

                    nodes.add(node(utils, entry.getValue().get(0).result(), tooltip));
                }

                return nodes.size() == 1 ? nodes.get(0) : new ConditionalTradesNode(utils, nodes, condition);
            }

            reason = "no offer returned (" + paths.get(paths.size() - 1).result() + ")";
        } catch (Interpreter.Fail fail) {
            reason = fail.describe();
        } catch (Throwable e) {
            reason = e.toString();
        }

        String name = ManagedRegistry.classKeyName(listing.getClass());

        if (utils.getConfiguration().logMoreStatistics && FAILED.add(name + reason)) {
            LOGGER.info("Could not work out trade item listing {} from its bytecode: {}", name, reason);
        }

        return null;
    }

    private static IDataNode node(IServerUtils utils, @Nullable Value result, TooltipNode condition) {
        if (result instanceof Value.Known known && known.value() instanceof MerchantOffer offer) {
            return TradeUtils.getNode(utils, offer, condition);
        }

        Value.Offer offer = (Value.Offer) Objects.requireNonNull(result);

        return new ItemsToItemsNode(utils, slot(utils, offer.costA()), slot(utils, offer.costB()), slot(utils, offer.result()),
                offer.maxUses(), offer.xp(), offer.priceMultiplier(), condition);
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

    @Nullable
    public static ComputedTrades getTradeLevels(IServerUtils utils, Class<?> merchant) {
        if (!RuntimeNames.AVAILABLE) {
            return null;
        }

        Int2ObjectMap<TradeLevel> levels = new Int2ObjectOpenHashMap<>();
        Set<Integer> incomplete = new TreeSet<>();
        boolean unknownLevel = false;
        String reason = null;

        try {
            List<Path> paths = paths((i) -> i.trades(merchant), true);

            if (productive(paths).isEmpty() && paths.stream().allMatch((p) -> p.failure() == null) && Interpreter.writesOffers(merchant)) {
                paths = paths((i) -> i.writtenTrades(merchant), true);
            }

            List<Path> productive = new ArrayList<>();

            for (Path path : paths) {
                Integer level = path.failure() == null ? null : pathLevel(path.decisions());

                if (path.failure() != null) {
                    reason = reason != null ? reason : path.failure();
                    unknownLevel |= level == null;

                    if (level != null) {
                        incomplete.add(level);
                    }
                } else if (path.result() instanceof Value.Offers offers && !offers.entries.isEmpty()) {
                    productive.add(path);
                }
            }

            Map<Integer, Map<List<Object>, Group>> groups = new TreeMap<>();

            for (Path path : productive) {
                Split split;

                try {
                    split = split(path.decisions());
                } catch (Interpreter.Fail fail) {
                    reason = reason != null ? reason : fail.describe();
                    unknownLevel = true;
                    continue;
                }

                double probability = probability(path.decisions());
                Map<List<Object>, Value.Offers.Entry> entries = new LinkedHashMap<>();
                Map<List<Object>, Integer> times = new HashMap<>();
                boolean twice = false;

                for (Value.Offers.Entry entry : ((Value.Offers) Objects.requireNonNull(path.result())).entries) {
                    int level = entry instanceof Value.Offers.Listings listings && listings.key() != null ? listings.key() : split.level();
                    List<Object> identity = entry instanceof Value.Offers.Listings listings ? List.of(Arrays.asList(listings.listings()), listings.count(), level)
                            : List.of(String.valueOf(describeOffer(((Value.Offers.Single) entry).offer())), level);

                    if (entries.putIfAbsent(identity, entry) != null && entry instanceof Value.Offers.Listings) {
                        reason = reason != null ? reason : "the same trades added twice";
                        incomplete.add(level);
                        twice = true;
                    }

                    times.merge(identity, 1, Integer::sum);
                }

                if (twice) {
                    continue;
                }

                for (Map.Entry<List<Object>, Value.Offers.Entry> entry : entries.entrySet()) {
                    int repeated = times.get(entry.getKey());
                    List<Object> identity = List.of(entry.getKey(), repeated);

                    groups.computeIfAbsent((Integer) entry.getKey().get(entry.getKey().size() - 1), (l) -> new LinkedHashMap<>())
                            .computeIfAbsent(identity, (i) -> new Group(entry.getValue(), repeated)).worlds.merge(split.world(), probability, Double::sum);
                }
            }

            for (Map.Entry<Integer, Map<List<Object>, Group>> level : groups.entrySet()) {
                List<Group> levelGroups = List.copyOf(level.getValue().values());
                List<Double> chances = levelGroups.stream().flatMap((g) -> g.worlds.values().stream()).toList();
                double chance = chances.get(0);

                if (chances.stream().anyMatch((c) -> Math.abs(c - chance) > 1e-6) || chance > 1 + 1e-6) {
                    reason = reason != null ? reason : "several sets of trades of one level";
                    incomplete.add(level.getKey());
                    continue;
                }

                levels.put((int) level.getKey(), tradeLevel(levelGroups, (float) Math.min(1, chance)));
            }

            if (levels.isEmpty() && reason == null && !(paths.size() == 1 && paths.get(0).decisions().isEmpty() && paths.get(0).result() instanceof Value.Offers)) {
                reason = "no trades";
                unknownLevel = true;
            }
        } catch (Interpreter.Fail fail) {
            reason = fail.describe();
            unknownLevel = true;
        } catch (Throwable e) {
            reason = e.toString();
            unknownLevel = true;
        }

        if (reason == null) {
            return new ComputedTrades(levels, false);
        }

        String name = ManagedRegistry.classKeyName(merchant);

        if (utils.getConfiguration().logMoreStatistics && FAILED.add(name + reason)) {
            LOGGER.info("Could not auto-detect all trades of trading entity {} from its bytecode: {}", name, reason);
        }

        if (unknownLevel) {
            incomplete.addAll(levels.keySet());
        }

        for (int level : incomplete) {
            levels.put(level, incomplete(levels.get(level), name));
        }

        return new ComputedTrades(levels, true);
    }

    private static TradeLevel incomplete(@Nullable TradeLevel level, String merchant) {
        if (level instanceof TradeLevel.OfListings listings) {
            return new TradeLevel.OfTrades(listings.levelInfo(), (u) -> {
                List<IDataNode> nodes = new ArrayList<>(Arrays.stream(listings.listings()).filter(Objects::nonNull).map((l) -> u.getItemListing(u, l, TooltipNode.empty())).toList());

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

    private static IDataNode incompleteNode() {
        return new MissingNode(TooltipBuilder.keyOnly(Lang.Description.INCOMPLETE_TRADES).build());
    }

    @Nullable
    private static Integer pathLevel(List<Decision> decisions) {
        try {
            Split split = split(decisions);

            return decisions.stream().anyMatch((d) -> d.holds() && d.kind() == Decision.Kind.EQUALS && d.value() instanceof Integer) ? split.level() : null;
        } catch (Interpreter.Fail fail) {
            return null;
        }
    }

    private static List<Path> productive(List<Path> paths) {
        return paths.stream().filter((p) -> p.result() instanceof Value.Offers offers && !offers.entries.isEmpty()).toList();
    }

    private static Split split(List<Decision> decisions) {
        List<Decision> world = decisions.stream().filter((d) -> d.kind() != Decision.Kind.RANDOM || !(d.value() instanceof Double)).toList();
        List<Decision> levels = world.stream().filter((d) -> d.holds() && d.kind() == Decision.Kind.EQUALS && d.value() instanceof Integer).toList();

        if (levels.size() > 1) {
            throw new Interpreter.Fail("trades depending on several levels");
        }
        if (levels.isEmpty()) {
            return new Split(1, world);
        }

        Decision level = levels.get(0);

        return new Split((Integer) Objects.requireNonNull(level.value()),
                world.stream().filter((d) -> d.kind() != Decision.Kind.EQUALS || !Objects.equals(d.subject(), level.subject())).toList());
    }

    private static double probability(List<Decision> decisions) {
        double probability = 1;

        for (Decision decision : decisions) {
            if (decision.kind() == Decision.Kind.RANDOM && decision.value() instanceof Double chance) {
                probability *= decision.holds() ? chance : 1 - chance;
            }
        }

        return probability;
    }

    private static TradeLevel tradeLevel(List<Group> groups, float chance) {
        List<Value.Offers.Entry> entries = groups.stream().map((g) -> g.entry).toList();
        List<PathCondition> conditions = groups.stream().map((g) -> PathCondition.of(List.copyOf(g.worlds.keySet()))).toList();
        List<NumberExpr> counts = groups.stream().map((g) -> g.entry instanceof Value.Offers.Listings listings ? count(listings.count()) : NumberExpr.constant(g.times)).toList();

        if (entries.size() == 1 && entries.get(0) instanceof Value.Offers.Listings listings && conditions.get(0) == PathCondition.ALWAYS) {
            return new TradeLevel.OfListings(listings.listings(), new TradeLevelInfo(counts.get(0), chance));
        }

        NumberExpr total = counts.stream().reduce(NumberExpr.constant(0), NumberExpr::add);

        return new TradeLevel.OfTrades(new TradeLevelInfo(total, chance), (u) -> {
            List<IDataNode> nodes = new ArrayList<>();

            for (int i = 0; i < entries.size(); i++) {
                TooltipNode condition = conditions.get(i).tooltip(u);

                if (entries.get(i) instanceof Value.Offers.Listings listings) {
                    List<IDataNode> trades = Arrays.stream(listings.listings()).filter(Objects::nonNull).map((l) -> u.getItemListing(u, l, condition)).toList();

                    if (entries.size() == 1 || counts.get(i).bounds().lo() >= trades.size()) {
                        nodes.addAll(trades);
                    } else {
                        nodes.add(new TradeGroupNode(counts.get(i), trades));
                    }
                } else {
                    IDataNode trade = node(u, ((Value.Offers.Single) entries.get(i)).offer(), condition);

                    nodes.add(groups.get(i).times > 1 ? TradeGroupNode.repeated(counts.get(i), trade) : trade);
                }
            }

            return nodes;
        });
    }

    private static NumberExpr count(Value value) {
        if (value instanceof Value.Known known && known.value() instanceof Number number) {
            return NumberExpr.constant(number.doubleValue());
        }
        if (value instanceof Value.Num num) {
            return num.expr();
        }

        throw new Interpreter.Fail("number of offers");
    }

    private static List<Path> paths(Function<Interpreter, Value> run, boolean partial) {
        List<Path> results = new ArrayList<>();
        Deque<List<Boolean>> scripts = new ArrayDeque<>();

        scripts.add(List.of());

        while (!scripts.isEmpty()) {
            if (results.size() >= MAX_PATHS) {
                if (!partial) {
                    throw new Interpreter.Fail("too many branches");
                }

                results.add(new Path(null, List.of(), "too many branches"));
                break;
            }

            List<Boolean> script = scripts.poll();
            Interpreter interpreter = new Interpreter(Interpreter.Mode.SYMBOLIC, 0, script);
            Value result = null;
            String failure = null;

            try {
                result = run.apply(interpreter);
            } catch (Interpreter.Fail fail) {
                if (!partial) {
                    throw fail;
                }

                failure = fail.describe();
            }

            List<Boolean> taken = interpreter.decisions().stream().map(Decision::holds).toList();

            results.add(new Path(result, interpreter.decisions(), failure));

            for (int i = script.size(); i < taken.size(); i++) {
                List<Boolean> other = new ArrayList<>(taken.subList(0, i));

                other.add(!taken.get(i));
                scripts.add(other);
            }
        }

        return results;
    }

    @Nullable
    private static String describeOffer(@Nullable Value result) {
        if (result instanceof Value.Offer offer) {
            return String.format("%s + %s -> %s, uses %d, xp %d, price multiplier %s", describe(offer.costA()), describe(offer.costB()), describe(offer.result()),
                    offer.maxUses(), offer.xp(), offer.priceMultiplier());
        }
        if (result instanceof Value.Known known && known.value() instanceof MerchantOffer offer) {
            return String.format("%s + %s -> %s, uses %d, xp %d, price multiplier %s", describe(new Value.Known(offer.getBaseCostA())),
                    describe(new Value.Known(offer.getCostB())), describe(new Value.Known(offer.getResult())), offer.getMaxUses(), offer.getXp(),
                    offer.getPriceMultiplier());
        }

        return null;
    }

    @Nullable
    public static Stacks getItemStack(Supplier<?> supplier) {
        if (!RuntimeNames.AVAILABLE) {
            return null;
        }

        try {
            Value result = new Interpreter(Interpreter.Mode.SYMBOLIC, 0).call(supplier, Supplier.class, "get", "()Ljava/lang/Object;", List.of());

            if (result instanceof Value.Known known && known.value() instanceof ItemStack stack) {
                return stack.isEmpty() ? new Stacks(List.of(), NumberExpr.constant(0)) : new Stacks(List.of(stack.copy()), NumberExpr.constant(stack.getCount()));
            }
            if (result instanceof Value.Stack stack) {
                return new Stacks(templates(stack).stream().filter((s) -> !s.isEmpty()).toList(), stack.count);
            }
            if (result instanceof Value.Choice choice && choice.options().stream().allMatch(ItemStack.class::isInstance)) {
                List<ItemStack> options = choice.options().stream().map((o) -> ((ItemStack) o).copy()).filter((s) -> !s.isEmpty()).toList();

                if (options.stream().mapToInt(ItemStack::getCount).distinct().count() <= 1) {
                    return new Stacks(options, NumberExpr.constant(options.isEmpty() ? 0 : options.get(0).getCount()));
                }
            }
        } catch (Throwable ignored) {}

        return null;
    }

    public static void clearCaches() {
        Interpreter.clearCaches();
        MixinChanges.clearCaches();
        LambdaResolver.clearCaches();
        FAILED.clear();
    }

    private static IDataNode slot(IServerUtils utils, Value value) {
        if (value instanceof Value.Known known && known.value() instanceof ItemStack stack) {
            return new ItemNode(1, NumberExpr.constant(stack.getCount()), stack, TooltipNode.empty(), List.of(), List.of());
        }
        if (value instanceof Value.Stack stack) {
            List<ItemStack> options = templates(stack);
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

        throw new Interpreter.Fail("trade slot");
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

    private static String describe(Value value) {
        if (value instanceof Value.Known known && known.value() instanceof ItemStack stack) {
            return stack.isEmpty() ? "-" : stackText(stack) + " x" + stack.getCount();
        }
        if (value instanceof Value.Stack stack) {
            String items = stack.options.stream().map(BytecodeInterpreter::stackText).collect(Collectors.joining(" | "));

            return (stack.options.size() > 1 ? "[" + items + "]" : items) + " x" + countText(stack.count) + (stack.randomTag ? " (random properties)" : "")
                    + (stack.destination != null ? " (destination " + stack.destination + ", decoration " + stack.decoration + ")" : "");
        }
        if (value instanceof Value.Choice choice) {
            return "[" + choice.options().stream().map((o) -> o instanceof ItemStack stack ? stackText(stack) + " x" + stack.getCount() : String.valueOf(o))
                    .collect(Collectors.joining(" | ")) + "]";
        }

        return "?";
    }

    private static String stackText(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()) + (stack.hasTag() ? " " + stack.getTag() : "");
    }

    private static String countText(NumberExpr count) {
        NumberInterval bounds = count.bounds();

        if (bounds.isUnknown()) {
            return "?";
        }

        return bounds.isPoint() ? numberText(bounds.lo()) : numberText(bounds.lo()) + "-" + numberText(bounds.hi());
    }

    private static String numberText(double value) {
        return value == Math.rint(value) ? String.valueOf((long) value) : String.valueOf(value);
    }

    private static List<ItemStack> templates(Value.Stack stack) {
        List<ItemStack> options = new ArrayList<>();
        int count = stack.count instanceof NumberExpr.Const c ? (int) c.value() : 1;

        for (ItemStack option : stack.options) {
            options.add(option.copyWithCount(count));
        }

        return options;
    }

    public record Stacks(List<ItemStack> options, NumberExpr count) {
    }

    private record Path(@Nullable Value result, List<Decision> decisions, @Nullable String failure) {
    }

    public record ComputedTrades(Int2ObjectMap<TradeLevel> levels, boolean incomplete) {
    }

    private record Split(int level, List<Decision> world) {
    }

    private static final class Group {
        final Value.Offers.Entry entry;
        final int times;
        final Map<List<Decision>, Double> worlds = new LinkedHashMap<>();

        Group(Value.Offers.Entry entry, int times) {
            this.entry = entry;
            this.times = times;
        }
    }
}
