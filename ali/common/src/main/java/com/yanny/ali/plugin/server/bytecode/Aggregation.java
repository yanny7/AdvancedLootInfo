package com.yanny.ali.plugin.server.bytecode;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.api.NumberInterval;
import com.yanny.ali.plugin.common.trades.TradeUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

final class Aggregation {
    private static final String UNRENDERABLE = "offer that cannot be shown";

    private Aggregation() {}

    @NotNull
    static Listing listing(List<Outcome> outcomes) {
        String reason = null;
        Map<String, List<Outcome.Success>> offers = new LinkedHashMap<>();

        for (Outcome outcome : outcomes) {
            if (outcome instanceof Outcome.Failure failure) {
                reason = reason != null ? reason : failure.reason();
            } else if (outcome instanceof Outcome.Success success && isRenderable(success.result())) {
                offers.computeIfAbsent(Objects.requireNonNull(describeOffer(success.result())), (t) -> new ArrayList<>()).add(success);
            } else if (outcome instanceof Outcome.Success success && !isNoOffer(success.result())) {
                reason = reason != null ? reason : UNRENDERABLE;
            }
        }

        absorbRandomProperties(offers);

        if (offers.isEmpty()) {
            if (reason == null) {
                reason = "no offer returned (" + ((Outcome.Success) outcomes.get(outcomes.size() - 1)).result() + ")";
            }

            return new Listing(List.of(), reason);
        }

        List<Offer> result = new ArrayList<>();

        for (Map.Entry<String, List<Outcome.Success>> entry : offers.entrySet()) {
            PathCondition when;

            if (entry.getValue().size() == outcomes.size()) {
                when = PathCondition.ALWAYS;
            } else {
                when = PathCondition.of(entry.getValue().stream().map(Outcome::decisions).toList());
            }

            result.add(new Offer(entry.getKey(), Objects.requireNonNull(entry.getValue().get(0).result()), when));
        }

        return new Listing(result, reason);
    }

    @NotNull
    static Trader trader(List<Outcome> outcomes) {
        Map<Integer, Level> levels = new TreeMap<>();
        Set<Integer> incomplete = new TreeSet<>();
        boolean unknownLevel = false;
        String reason = null;

        try {
            List<Outcome.Success> productive = new ArrayList<>();

            for (Outcome outcome : outcomes) {
                if (outcome instanceof Outcome.Failure failure) {
                    Integer level = pathLevel(failure.decisions());

                    reason = reason != null ? reason : failure.reason();
                    unknownLevel |= level == null;

                    if (level != null) {
                        incomplete.add(level);
                    }
                } else if (outcome instanceof Outcome.Success success && success.result() instanceof Value.Offers offers && !offers.entries.isEmpty()) {
                    productive.add(success);
                }
            }

            Map<Integer, Map<List<Object>, Bucket>> buckets = new TreeMap<>();

            for (Outcome.Success path : productive) {
                Split split;

                try {
                    split = split(path.decisions());
                } catch (Fail fail) {
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
                        identity = List.of(Arrays.stream(listings.listings()).map(TradeUtils::valueKey).toList(), listings.count(), level);
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
                    Bucket bucket = buckets.computeIfAbsent((Integer) key.get(key.size() - 1), (l) -> new LinkedHashMap<>())
                            .computeIfAbsent(identity, (i) -> new Bucket(entry.getValue(), repeated));

                    bucket.worlds.merge(split.world(), probability, Double::sum);

                    if (!single) {
                        bucket.counts.merge(count((Value) key.get(1)), probability, Double::sum);
                    }
                }
            }

            buckets.values().forEach(Aggregation::absorbRandomTrades);

            for (Map.Entry<Integer, Map<List<Object>, Bucket>> level : buckets.entrySet()) {
                List<Bucket> levelBuckets = List.copyOf(level.getValue().values());
                List<Double> chances = levelBuckets.stream().flatMap((b) -> b.worlds.values().stream()).toList();
                double chance = chances.get(0);

                if (chances.stream().anyMatch((c) -> Math.abs(c - chance) > 1e-6) || chance > 1 + 1e-6) {
                    reason = reason != null ? reason : "several sets of trades of one level";
                    incomplete.add(level.getKey());
                    continue;
                }

                List<Group> groups = levelBuckets.stream().map((b) -> new Group(b.entry, b.times, PathCondition.of(List.copyOf(b.worlds.keySet())), count(b))).toList();

                levels.put(level.getKey(), new Level(groups, (float) Math.min(1, chance)));
            }

            if (levels.isEmpty() && reason == null
                    && !(outcomes.size() == 1 && outcomes.get(0).decisions().isEmpty() && outcomes.get(0) instanceof Outcome.Success success && success.result() instanceof Value.Offers)) {
                reason = "no trades";
                unknownLevel = true;
            }
        } catch (Fail fail) {
            reason = fail.describe();
            unknownLevel = true;
        } catch (Throwable e) {
            reason = e.toString();
            unknownLevel = true;
        }

        return new Trader(levels, incomplete, unknownLevel, reason);
    }

    @Nullable
    static BytecodeInterpreter.Stacks stacks(@Nullable Value result) {
        if (result instanceof Value.Known known && known.value() instanceof ItemStack stack) {
            return stacks(List.of(stack), null);
        }
        if (result instanceof Value.Stack stack) {
            return stacks(stack.templates(), stack.count);
        }
        if (result instanceof Value.Choice choice && choice.options().stream().allMatch(ItemStack.class::isInstance)) {
            return stacks(choice.options().stream().map(ItemStack.class::cast).toList(), null);
        }

        return null;
    }

    @Nullable
    static String describeOffer(@Nullable Value result) {
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

    @NotNull
    private static BytecodeInterpreter.Stacks stacks(List<ItemStack> stacks, @Nullable NumberExpr count) {
        List<BytecodeInterpreter.Option> options = new ArrayList<>();

        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) {
                continue;
            }

            NumberExpr stackCount = count != null ? count : NumberExpr.constant(stack.getCount());
            double chance = 1.0 / stacks.size();
            int same = -1;

            for (int i = 0; i < options.size() && same < 0; i++) {
                if (TradeTypes.isSameStack(options.get(i).stack(), stack) && options.get(i).count().equals(stackCount)) {
                    same = i;
                }
            }

            if (same < 0) {
                options.add(new BytecodeInterpreter.Option(stack.copy(), stackCount, chance));
            } else {
                BytecodeInterpreter.Option option = options.get(same);

                options.set(same, new BytecodeInterpreter.Option(option.stack(), option.count(), option.chance() + chance));
            }
        }

        return new BytecodeInterpreter.Stacks(options);
    }

    @Nullable
    private static Integer pathLevel(List<Decision> decisions) {
        try {
            Split split = split(decisions);

            return decisions.stream().anyMatch((d) -> d.holds() && d.kind() == Decision.Kind.EQUALS && d.value() instanceof Integer) ? split.level() : null;
        } catch (Fail fail) {
            return null;
        }
    }

    @NotNull
    private static Split split(List<Decision> decisions) {
        List<Decision> world = decisions.stream().filter((d) -> d.kind() != Decision.Kind.RANDOM || !(d.value() instanceof Double)).toList();
        List<Decision> levels = world.stream().filter((d) -> d.holds() && d.kind() == Decision.Kind.EQUALS && d.value() instanceof Integer).toList();

        if (levels.size() > 1) {
            throw new Fail("trades depending on several levels");
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

    private static NumberExpr count(Bucket bucket) {
        if (bucket.counts.isEmpty()) {
            return NumberExpr.constant(bucket.times);
        }

        return NumberExpr.weighted(bucket.counts.entrySet().stream().map((e) -> new NumberExpr.WeightedEntry(e.getValue(), e.getKey())).toList());
    }

    private static NumberExpr count(Value value) {
        if (value instanceof Value.Known known && known.value() instanceof Number number) {
            return NumberExpr.constant(number.doubleValue());
        }
        if (value instanceof Value.Num num) {
            return num.expr();
        }

        throw new Fail("number of offers");
    }

    private static void absorbRandomProperties(Map<String, List<Outcome.Success>> offers) {
        boolean merged = true;

        while (merged) {
            merged = false;

            for (List<Outcome.Success> random : offers.values()) {
                Set<List<Decision>> known = knownDecisions(random.stream().map(Outcome::decisions).toList());
                String other = offers.entrySet().stream().filter((e) -> e.getValue() != random && absorbs(random.get(0).result(), e.getValue().get(0).result())
                        && known.equals(knownDecisions(e.getValue().stream().map(Outcome::decisions).toList()))).map(Map.Entry::getKey).findFirst().orElse(null);

                if (other != null) {
                    random.addAll(offers.remove(other));
                    merged = true;
                    break;
                }
            }
        }
    }

    private static void absorbRandomTrades(Map<List<Object>, Bucket> buckets) {
        boolean merged = true;

        while (merged) {
            merged = false;

            for (Bucket random : buckets.values()) {
                if (!(random.entry instanceof Value.Offers.Single single)) {
                    continue;
                }

                Set<List<Decision>> known = knownDecisions(random.worlds.keySet());
                List<Object> other = buckets.entrySet().stream().filter((e) -> e.getValue() != random && e.getValue().times == random.times
                        && e.getValue().entry instanceof Value.Offers.Single absorbed && absorbs(single.offer(), absorbed.offer())
                        && known.equals(knownDecisions(e.getValue().worlds.keySet()))).map(Map.Entry::getKey).findFirst().orElse(null);

                if (other != null) {
                    buckets.remove(other).worlds.forEach((world, probability) -> random.worlds.merge(world, probability, Double::sum));
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

    @NotNull
    private static String describe(Value value) {
        if (value instanceof Value.Known known && known.value() instanceof ItemStack stack) {
            return stack.isEmpty() ? "-" : TradeTypes.stackText(stack) + " x" + stack.getCount();
        }
        if (value instanceof Value.Stack stack) {
            String items = stack.options.stream().map(TradeTypes::stackText).collect(Collectors.joining(" | "));

            return (stack.options.size() > 1 ? "[" + items + "]" : items) + " x" + countText(stack.count) + (stack.randomTag ? " (random properties)" : "")
                    + (stack.destination != null ? " (destination " + stack.destination + ", decoration " + stack.decoration + ")" : "")
                    + (stack.tag != null && ItemStackModel.isTag(stack.tag, stack.options) ? " (" + stack.tag + ")" : "");
        }
        if (value instanceof Value.Choice choice) {
            return "[" + choice.options().stream().map((o) -> o instanceof ItemStack stack ? TradeTypes.stackText(stack) + " x" + stack.getCount() : String.valueOf(o))
                    .collect(Collectors.joining(" | ")) + "]";
        }

        return "?";
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

    record Listing(List<Offer> offers, @Nullable String reason) {
    }

    record Offer(String text, Value result, PathCondition when) {
    }

    record Trader(Map<Integer, Level> levels, Set<Integer> incomplete, boolean unknownLevel, @Nullable String reason) {
    }

    record Level(List<Group> groups, float chance) {
    }

    record Group(Value.Offers.Entry entry, int times, PathCondition condition, NumberExpr count) {
    }

    private record Split(int level, List<Decision> world) {
    }

    private static final class Bucket {
        final Value.Offers.Entry entry;
        final int times;
        final Map<List<Decision>, Double> worlds = new LinkedHashMap<>();
        final Map<NumberExpr, Double> counts = new LinkedHashMap<>();

        Bucket(Value.Offers.Entry entry, int times) {
            this.entry = entry;
            this.times = times;
        }
    }
}
