package com.yanny.ali.plugin.server.bytecode;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.manager.ManagedRegistry;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.TradeLevel;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public final class BytecodeInterpreter {
    private BytecodeInterpreter() {}

    @Nullable
    static IDataNode getItemListing(IServerUtils utils, BoundOffer listing, TooltipNode condition) {
        if (!RuntimeNames.AVAILABLE) {
            return null;
        }

        Aggregation.Listing aggregated;

        try {
            aggregated = Aggregation.listing(Explorer.explore(Profile.TRADE, utils.getServerLevel().registryAccess(), (i) -> {
                i.subject = new ListingSubject(listing.receiver().getClass());
                return listing.call(i);
            }));
        } catch (Fail fail) {
            aggregated = new Aggregation.Listing(List.of(), fail.describe());
        } catch (Throwable e) {
            aggregated = new Aggregation.Listing(List.of(), e.toString());
        }

        return Renderer.listing(utils, ManagedRegistry.classKeyName(listing.receiver().getClass()), aggregated, condition);
    }

    @NotNull
    public static ComputedTrades getTradeLevels(IServerUtils utils, Class<?> merchant) {
        if (!RuntimeNames.AVAILABLE) {
            return new ComputedTrades(new Int2ObjectOpenHashMap<>(), true);
        }

        Aggregation.Trader trader;

        try {
            trader = Aggregation.trader(Explorer.trader(utils.getServerLevel().registryAccess(), merchant));
        } catch (Fail fail) {
            trader = new Aggregation.Trader(Map.of(), Set.of(), true, fail.describe());
        } catch (Throwable e) {
            trader = new Aggregation.Trader(Map.of(), Set.of(), true, e.toString());
        }

        return Renderer.levels(utils, ManagedRegistry.classKeyName(merchant), trader);
    }

    @Nullable
    public static Stacks getItemStack(Supplier<?> supplier) {
        if (!RuntimeNames.AVAILABLE) {
            return null;
        }

        try {
            Interpreter interpreter = new Interpreter(Profile.TRADE, Interpreter.Mode.SYMBOLIC, 0);

            interpreter.subject = new ListingSubject(supplier.getClass());
            return Aggregation.stacks(interpreter.call(supplier, Supplier.class, "get", "()Ljava/lang/Object;", List.of()));
        } catch (Throwable ignored) {}

        return null;
    }

    public static void clearCaches() {
        Resolution.clearCaches();
        MixinChanges.clearCaches();
        LambdaResolver.clearCaches();
        Renderer.clearCaches();
        LootTableAnalysis.clearCaches();
        ModJarIndex.clearCaches();
    }

    public record Stacks(List<Option> options) {
    }

    public record Option(ItemStack stack, NumberExpr count, double chance) {
    }

    public record ComputedTrades(Int2ObjectMap<TradeLevel> levels, boolean incomplete) {
    }
}
