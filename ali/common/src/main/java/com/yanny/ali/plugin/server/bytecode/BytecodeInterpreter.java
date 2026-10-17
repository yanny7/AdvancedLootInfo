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
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.ali.plugin.common.trades.TradeUtils;
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
            List<Path> paths = paths(listing);
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

    private static List<Path> paths(VillagerTrades.ItemListing listing) {
        List<Path> results = new ArrayList<>();
        Deque<List<Boolean>> scripts = new ArrayDeque<>();

        scripts.add(List.of());

        while (!scripts.isEmpty()) {
            if (results.size() >= MAX_PATHS) {
                throw new Interpreter.Fail("too many branches");
            }

            List<Boolean> script = scripts.poll();
            Interpreter interpreter = new Interpreter(Interpreter.Mode.SYMBOLIC, 0, script);

            Value result = interpreter.call(listing, VillagerTrades.ItemListing.class, RuntimeNames.GET_OFFER.name(), RuntimeNames.GET_OFFER.desc(),
                    List.of(Value.Unknown.of(Type.getType(Entity.class), Origin.TRADER), new Value.Rng(false)));
            List<Boolean> taken = interpreter.decisions().stream().map(Decision::holds).toList();

            results.add(new Path(result, interpreter.decisions()));

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

    private record Path(@Nullable Value result, List<Decision> decisions) {
    }
}
