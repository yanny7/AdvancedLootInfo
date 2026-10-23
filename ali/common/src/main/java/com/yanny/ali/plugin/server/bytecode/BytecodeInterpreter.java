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
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
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
    private static final int MAX_PATHS = 128;
    private static final int MAX_RESTARTS = 8;
    private static final String UNRENDERABLE = "offer that cannot be shown";

    private BytecodeInterpreter() {}

    @Nullable
    public static IDataNode getItemListing(IServerUtils utils, VillagerTrades.ItemListing listing, TooltipNode condition) {
        if (!RuntimeNames.AVAILABLE) {
            return null;
        }

        String name = ManagedRegistry.classKeyName(listingSource(listing).getClass());
        String reason = null;

        try {
            List<Path> paths = paths(utils, (i) -> listing instanceof BoundOffer bound ? bound.call(i) : i.call(listing, VillagerTrades.ItemListing.class,
                    RuntimeNames.GET_OFFER.name(), RuntimeNames.GET_OFFER.desc(), List.of(Value.Unknown.of(Type.getType(Entity.class), Origin.TRADER), new Value.Rng(false))));
            Map<String, List<Path>> offers = new LinkedHashMap<>();

            for (Path path : paths) {
                if (path.failure() != null) {
                    reason = reason != null ? reason : path.failure();
                } else if (isRenderable(path.result())) {
                    offers.computeIfAbsent(Objects.requireNonNull(describeOffer(path.result())), (t) -> new ArrayList<>()).add(path);
                } else if (!isNoOffer(path.result())) {
                    reason = reason != null ? reason : UNRENDERABLE;
                }
            }

            absorbRandomProperties(offers);

            if (!offers.isEmpty()) {
                List<IDataNode> nodes = new ArrayList<>();
                TooltipNode outer = reason == null ? condition : withIncomplete(condition);

                for (Map.Entry<String, List<Path>> entry : offers.entrySet()) {
                    PathCondition when = entry.getValue().size() == paths.size() ? PathCondition.ALWAYS
                            : PathCondition.of(entry.getValue().stream().map(Path::decisions).toList());

                    if (LOG_TRADES) {
                        LOGGER.info("Trade item listing {}: {}{}", name, entry.getKey(), when == PathCondition.ALWAYS ? "" : " if " + when.text());
                    }

                    TooltipNode tooltip = offers.size() > 1 ? when.tooltip(utils) : combine(outer, when.tooltip(utils), when);

                    nodes.add(node(utils, entry.getValue().get(0).result(), tooltip));
                }

                if (reason != null && utils.getConfiguration().logMoreStatistics && FAILED.add(name + reason)) {
                    LOGGER.info("Computed only some variants of trade item listing {} from its bytecode: {}", name, reason);
                }

                return nodes.size() == 1 ? nodes.get(0) : new ConditionalTradesNode(utils, nodes, outer);
            }

            reason = reason != null ? reason : "no offer returned (" + paths.get(paths.size() - 1).result() + ")";
        } catch (Interpreter.Fail fail) {
            reason = fail.describe();
        } catch (Throwable e) {
            reason = e.toString();
        }

        if (utils.getConfiguration().logMoreStatistics && FAILED.add(name + reason)) {
            LOGGER.info("Could not work out trade item listing {} from its bytecode: {}", name, reason);
        }

        return null;
    }

    public static Object listingSource(VillagerTrades.ItemListing listing) {
        return listing instanceof BoundOffer bound ? bound.receiver() : listing;
    }

    private static TooltipNode withIncomplete(TooltipNode condition) {
        return TooltipBuilder.array((b) -> {
            b.add(condition);
            b.add(TooltipBuilder.error(TooltipBuilder.translate(Lang.Description.INCOMPLETE_TRADE.singular())).build());
        }).build();
    }

    @NotNull
    private static IDataNode node(IServerUtils utils, @Nullable Value result, TooltipNode condition) {
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
    public static ComputedTrades getTradeLevels(IServerUtils utils, Class<?> merchant) {
        if (!RuntimeNames.AVAILABLE) {
            return new ComputedTrades(new Int2ObjectOpenHashMap<>(), true);
        }

        Int2ObjectMap<TradeLevel> levels = new Int2ObjectOpenHashMap<>();
        Set<Integer> incomplete = new TreeSet<>();
        boolean unknownLevel = false;
        String reason = null;

        try {
            List<Path> paths = paths(utils, (i) -> i.trades(merchant));

            if (productive(paths).isEmpty() && paths.stream().allMatch((p) -> p.failure() == null) && Interpreter.writesOffers(merchant)) {
                paths = paths(utils, (i) -> i.writtenTrades(merchant));
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

                    if (entry instanceof Value.Offers.Single single && !isRenderable(single.offer())) {
                        reason = reason != null ? reason : UNRENDERABLE;
                        incomplete.add(level);
                        continue;
                    }

                    List<Object> identity;

                    if (entry instanceof Value.Offers.Listings listings) {
                        identity = List.of(Arrays.stream(listings.listings()).map(TradeUtils::listingKey).toList(), listings.count(), level);
                    } else if (entry instanceof Value.Offers.Picked picked) {
                        identity = List.of(picked.offers().stream().map((o) -> String.valueOf(describeOffer(new Value.Known(o)))).toList(), picked.count(), level);
                    } else {
                        identity = List.of(String.valueOf(describeOffer(((Value.Offers.Single) entry).offer())), level);
                    }

                    if (entries.putIfAbsent(identity, entry) != null && !(entry instanceof Value.Offers.Single)) {
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
                    List<Object> key = entry.getKey();
                    boolean single = entry.getValue() instanceof Value.Offers.Single;
                    List<Object> identity = List.of(single || !split.world().isEmpty() ? key : List.of(key.get(0), key.get(2)), repeated);
                    Group group = groups.computeIfAbsent((Integer) key.get(key.size() - 1), (l) -> new LinkedHashMap<>())
                            .computeIfAbsent(identity, (i) -> new Group(entry.getValue(), repeated));

                    group.worlds.merge(split.world(), probability, Double::sum);

                    if (!single) {
                        group.counts.merge(count((Value) key.get(1)), probability, Double::sum);
                    }
                }
            }

            groups.values().forEach(BytecodeInterpreter::absorbRandomTrades);

            for (Map.Entry<Integer, Map<List<Object>, Group>> level : groups.entrySet()) {
                List<Group> levelGroups = List.copyOf(level.getValue().values());
                List<Double> chances = levelGroups.stream().flatMap((g) -> g.worlds.values().stream()).toList();
                double chance = chances.get(0);

                if (chances.stream().anyMatch((c) -> Math.abs(c - chance) > 1e-6) || chance > 1 + 1e-6) {
                    reason = reason != null ? reason : "several sets of trades of one level";
                    incomplete.add(level.getKey());
                    continue;
                }

                TradeLevel tradeLevel = tradeLevel(levelGroups, (float) Math.min(1, chance));

                levels.put((int) level.getKey(), tradeLevel);

                if (LOG_TRADES) {
                    logLevel(ManagedRegistry.classKeyName(merchant), level.getKey(), levelGroups, tradeLevel);
                }
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

        return new ComputedTrades(levels, true);
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

    @Nullable
    private static Integer pathLevel(List<Decision> decisions) {
        try {
            Split split = split(decisions);

            return decisions.stream().anyMatch((d) -> d.holds() && d.kind() == Decision.Kind.EQUALS && d.value() instanceof Integer) ? split.level() : null;
        } catch (Interpreter.Fail fail) {
            return null;
        }
    }

    @Unmodifiable
    @NotNull
    private static List<Path> productive(List<Path> paths) {
        return paths.stream().filter((p) -> p.result() instanceof Value.Offers offers && !offers.entries.isEmpty()).toList();
    }

    @NotNull
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

    @NotNull
    private static TradeLevel tradeLevel(List<Group> groups, float chance) {
        List<Value.Offers.Entry> entries = groups.stream().map((g) -> g.entry).toList();
        List<PathCondition> conditions = groups.stream().map((g) -> PathCondition.of(List.copyOf(g.worlds.keySet()))).toList();
        List<NumberExpr> counts = groups.stream().map(BytecodeInterpreter::count).toList();

        if (entries.size() == 1 && entries.get(0) instanceof Value.Offers.Listings listings && conditions.get(0) == PathCondition.ALWAYS) {
            return new TradeLevel.OfListings(listings.listings(), new TradeLevelInfo(counts.get(0), chance));
        }

        NumberExpr total = counts.stream().reduce(NumberExpr.constant(0), NumberExpr::add);

        return new TradeLevel.OfTrades(new TradeLevelInfo(total, chance), (u) -> {
            List<IDataNode> nodes = new ArrayList<>();

            for (int i = 0; i < entries.size(); i++) {
                TooltipNode condition = conditions.get(i).tooltip(u);

                if (!(entries.get(i) instanceof Value.Offers.Single single)) {
                    List<IDataNode> trades;

                    if (entries.get(i) instanceof Value.Offers.Listings listings) {
                        trades = Arrays.stream(TradeUtils.distinctListings(listings.listings())).filter(Objects::nonNull).map((l) -> u.getItemListing(u, l, condition)).toList();
                    } else {
                        trades = ((Value.Offers.Picked) entries.get(i)).offers().stream().map((o) -> node(u, new Value.Known(o), condition)).toList();
                    }

                    if (shownAsGroup(entries.size(), counts.get(i), trades.size())) {
                        nodes.add(new TradeGroupNode(counts.get(i), trades));
                    } else {
                        nodes.addAll(trades);
                    }
                } else {
                    IDataNode trade = node(u, single.offer(), condition);

                    nodes.add(groups.get(i).times > 1 ? TradeGroupNode.repeated(counts.get(i), trade) : trade);
                }
            }

            return nodes;
        });
    }

    private static NumberExpr count(Group group) {
        if (group.counts.isEmpty()) {
            return NumberExpr.constant(group.times);
        }

        return NumberExpr.weighted(group.counts.entrySet().stream().map((e) -> new NumberExpr.WeightedEntry(e.getValue(), e.getKey())).toList());
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

    private static boolean shownAsGroup(int entries, NumberExpr count, int trades) {
        return entries > 1 && count.bounds().lo() < trades;
    }

    private static void logLevel(String name, int level, List<Group> groups, TradeLevel tradeLevel) {
        TradeLevelInfo info = tradeLevel instanceof TradeLevel.OfListings listings ? listings.levelInfo() : ((TradeLevel.OfTrades) tradeLevel).levelInfo();

        LOGGER.info("Trades of trading entity {} level {}: {}, chance {}, {} offers", name, level, tradeLevel instanceof TradeLevel.OfListings ? "OfListings" : "OfTrades",
                info.chance(), info.offers());

        for (int i = 0; i < groups.size(); i++) {
            Group group = groups.get(i);
            PathCondition when = PathCondition.of(List.copyOf(group.worlds.keySet()));
            NumberExpr count = count(group);
            String kind;
            String shown;
            List<String> trades;

            if (group.entry instanceof Value.Offers.Listings listings) {
                trades = Arrays.stream(TradeUtils.distinctListings(listings.listings())).filter(Objects::nonNull).map((l) -> keyText(TradeUtils.listingKey(l))).toList();
                kind = "listings";
                shown = shownAsGroup(groups.size(), count, trades.size()) ? "shown as a group" : "shown as plain trades";
            } else if (group.entry instanceof Value.Offers.Picked picked) {
                trades = picked.offers().stream().map((o) -> String.valueOf(describeOffer(new Value.Known(o)))).toList();
                kind = "picked offers";
                shown = shownAsGroup(groups.size(), count, trades.size()) ? "shown as a group" : "shown as plain trades";
            } else {
                trades = List.of(String.valueOf(describeOffer(((Value.Offers.Single) group.entry).offer())));
                kind = "single";
                shown = group.times > 1 ? "shown repeated" : "shown as a plain trade";
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
            return list.stream().map(BytecodeInterpreter::keyText).collect(Collectors.joining(", ", "[", "]"));
        }
        if (key instanceof Set<?> set) {
            return set.stream().map(BytecodeInterpreter::keyText).sorted().collect(Collectors.joining(", ", "{", "}"));
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

    @NotNull
    private static List<Path> paths(IServerUtils utils, Function<Interpreter, Value> run) {
        ServerLevel level = utils.getServerLevel();
        RegistryAccess registries = level.registryAccess();
        Set<String> opaque = new HashSet<>();
        int restarts = 0;
        List<Path> results = new ArrayList<>();
        Deque<List<Boolean>> scripts = new ArrayDeque<>();

        scripts.add(List.of());

        while (!scripts.isEmpty()) {
            if (results.size() >= MAX_PATHS) {
                results.add(new Path(null, List.of(), "too many branches"));
                break;
            }

            List<Boolean> script = scripts.poll();
            Interpreter interpreter = new Interpreter(Interpreter.Mode.SYMBOLIC, 0, script, registries, opaque);
            Value result = null;
            String failure = null;

            try {
                result = run.apply(interpreter);
            } catch (Interpreter.Fail fail) {
                failure = fail.describe();
            }
            if (interpreter.retry() && ++restarts < MAX_RESTARTS) {
                results.clear();
                scripts.clear();
                scripts.add(List.of());
                continue;
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

    private static void absorbRandomProperties(Map<String, List<Path>> offers) {
        boolean merged = true;

        while (merged) {
            merged = false;

            for (List<Path> random : offers.values()) {
                Set<List<Decision>> known = knownDecisions(random.stream().map(Path::decisions).toList());
                String other = offers.entrySet().stream().filter((e) -> e.getValue() != random && absorbs(random.get(0).result(), e.getValue().get(0).result())
                        && known.equals(knownDecisions(e.getValue().stream().map(Path::decisions).toList()))).map(Map.Entry::getKey).findFirst().orElse(null);

                if (other != null) {
                    random.addAll(offers.remove(other));
                    merged = true;
                    break;
                }
            }
        }
    }

    private static void absorbRandomTrades(Map<List<Object>, Group> groups) {
        boolean merged = true;

        while (merged) {
            merged = false;

            for (Group random : groups.values()) {
                if (!(random.entry instanceof Value.Offers.Single single)) {
                    continue;
                }

                Set<List<Decision>> known = knownDecisions(random.worlds.keySet());
                List<Object> other = groups.entrySet().stream().filter((e) -> e.getValue() != random && e.getValue().times == random.times
                        && e.getValue().entry instanceof Value.Offers.Single absorbed && absorbs(single.offer(), absorbed.offer())
                        && known.equals(knownDecisions(e.getValue().worlds.keySet()))).map(Map.Entry::getKey).findFirst().orElse(null);

                if (other != null) {
                    groups.remove(other).worlds.forEach((world, probability) -> random.worlds.merge(world, probability, Double::sum));
                    merged = true;
                    break;
                }
            }
        }
    }

    private static Set<List<Decision>> knownDecisions(Collection<List<Decision>> paths) {
        return paths.stream().map((p) -> p.stream().filter((d) -> d.kind() != Decision.Kind.UNKNOWN).toList()).collect(Collectors.toSet());
    }

    private static boolean absorbs(@Nullable Value random, @Nullable Value other) {
        Value.Offer offer = random instanceof Value.Offer o ? o : null;
        Value.Offer absorbed = offer(other);

        if (offer == null || absorbed == null || offer.maxUses() != absorbed.maxUses() || offer.xp() != absorbed.xp()
                || offer.priceMultiplier() != absorbed.priceMultiplier()) {
            return false;
        }

        return absorbsSlot(offer.costA(), absorbed.costA()) && absorbsSlot(offer.costB(), absorbed.costB()) && absorbsSlot(offer.result(), absorbed.result());
    }

    @Nullable
    private static Value.Offer offer(@Nullable Value value) {
        if (value instanceof Value.Offer offer) {
            return offer;
        }
        if (value instanceof Value.Known known && known.value() instanceof MerchantOffer offer) {
            return new Value.Offer(new Value.Known(offer.getBaseCostA()), new Value.Known(offer.getCostB()), new Value.Known(offer.getResult()), offer.getMaxUses(),
                    offer.getXp(), offer.getPriceMultiplier());
        }

        return null;
    }

    private static boolean absorbsSlot(Value random, Value other) {
        if (describe(random).equals(describe(other))) {
            return true;
        }
        if (!(random instanceof Value.Stack stack) || !stack.randomTag) {
            return false;
        }

        List<Item> items = stack.options.stream().map(ItemStack::getItem).toList();

        if (other instanceof Value.Known known && known.value() instanceof ItemStack real) {
            return items.equals(List.of(real.getItem())) && stack.count.equals(NumberExpr.constant(real.getCount())) && stack.destination == null && stack.decoration == null;
        }

        return other instanceof Value.Stack absorbed && items.equals(absorbed.options.stream().map(ItemStack::getItem).toList()) && stack.count.equals(absorbed.count)
                && Objects.equals(stack.destination, absorbed.destination) && Objects.equals(stack.decoration, absorbed.decoration);
    }

    private static boolean isRenderable(@Nullable Value result) {
        if (result instanceof Value.Known known && known.value() instanceof MerchantOffer) {
            return true;
        }

        return result instanceof Value.Offer offer && isSlot(offer.costA()) && isSlot(offer.costB()) && isSlot(offer.result());
    }

    private static boolean isSlot(Value value) {
        return value instanceof Value.Known known && known.value() instanceof ItemStack || value instanceof Value.Stack
                || value instanceof Value.Choice choice && choice.options().stream().allMatch(ItemStack.class::isInstance);
    }

    private static boolean isNoOffer(@Nullable Value result) {
        return result == null || result instanceof Value.Known known && known.value() == null;
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
                return stacks(List.of(stack), null);
            }
            if (result instanceof Value.Stack stack) {
                return stacks(templates(stack), stack.count);
            }
            if (result instanceof Value.Choice choice && choice.options().stream().allMatch(ItemStack.class::isInstance)) {
                return stacks(choice.options().stream().map(ItemStack.class::cast).toList(), null);
            }
        } catch (Throwable ignored) {}

        return null;
    }

    @NotNull
    private static Stacks stacks(List<ItemStack> stacks, @Nullable NumberExpr count) {
        List<Option> options = new ArrayList<>();

        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) {
                continue;
            }

            NumberExpr stackCount = count != null ? count : NumberExpr.constant(stack.getCount());
            double chance = 1.0 / stacks.size();
            int same = -1;

            for (int i = 0; i < options.size() && same < 0; i++) {
                if (ItemStack.isSameItemSameTags(options.get(i).stack(), stack) && options.get(i).count().equals(stackCount)) {
                    same = i;
                }
            }

            if (same < 0) {
                options.add(new Option(stack.copy(), stackCount, chance));
            } else {
                Option option = options.get(same);

                options.set(same, new Option(option.stack(), option.count(), option.chance() + chance));
            }
        }

        return new Stacks(options);
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

    @NotNull
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

    @NotNull
    private static String stackText(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()) + (stack.hasTag() ? " " + stack.getTag() : "");
    }

    @NotNull
    private static String countText(NumberExpr count) {
        NumberInterval bounds = count.bounds();

        if (bounds.isUnknown()) {
            return "?";
        }

        return bounds.isPoint() ? numberText(bounds.lo()) : numberText(bounds.lo()) + "-" + numberText(bounds.hi());
    }

    @NotNull
    private static String numberText(double value) {
        return value == Math.rint(value) ? String.valueOf((long) value) : String.valueOf(value);
    }

    @NotNull
    private static List<ItemStack> templates(Value.Stack stack) {
        List<ItemStack> options = new ArrayList<>();
        int count = stack.count instanceof NumberExpr.Const c ? (int) c.value() : 1;

        for (ItemStack option : stack.options) {
            options.add(option.copyWithCount(count));
        }

        return options;
    }

    public record Stacks(List<Option> options) {
    }

    public record Option(ItemStack stack, NumberExpr count, double chance) {
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
        final Map<NumberExpr, Double> counts = new LinkedHashMap<>();

        Group(Value.Offers.Entry entry, int times) {
            this.entry = entry;
            this.times = times;
        }
    }
}
