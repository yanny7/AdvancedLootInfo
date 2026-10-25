package com.yanny.ali.plugin.server.bytecode;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.manager.ManagedRegistry;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.TradeLevel;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public final class BytecodeInterpreter {
    private BytecodeInterpreter() {}

    @Nullable
    public static IDataNode getItemListing(IServerUtils utils, VillagerTrades.ItemListing listing, TooltipNode condition) {
        if (!RuntimeNames.AVAILABLE) {
            return null;
        }

        Aggregation.Listing aggregated;

        try {
            aggregated = Aggregation.listing(Explorer.explore(utils.getServerLevel().registryAccess(), (i) -> getOffer(i, listing)));
        } catch (Fail fail) {
            aggregated = new Aggregation.Listing(List.of(), fail.describe());
        } catch (Throwable e) {
            aggregated = new Aggregation.Listing(List.of(), e.toString());
        }

        return Renderer.listing(utils, ManagedRegistry.classKeyName(listingSource(listing).getClass()), aggregated, condition);
    }

    @Nullable
    private static Value getOffer(Interpreter interpreter, VillagerTrades.ItemListing listing) {
        if (listing instanceof BoundOffer bound) {
            return bound.call(interpreter);
        }

        return interpreter.call(listing, VillagerTrades.ItemListing.class, RuntimeNames.GET_OFFER.name(), RuntimeNames.GET_OFFER.desc(),
                List.of(Value.Unknown.of(Type.getType(Entity.class), Origin.TRADER), new Value.Rng(false)));
    }

    public static Object listingSource(VillagerTrades.ItemListing listing) {
        return listing instanceof BoundOffer bound ? bound.receiver() : listing;
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
            return Aggregation.stacks(new Interpreter(Interpreter.Mode.SYMBOLIC, 0).call(supplier, Supplier.class, "get", "()Ljava/lang/Object;", List.of()));
        } catch (Throwable ignored) {}

        return null;
    }

    public static void clearCaches() {
        Resolution.clearCaches();
        MixinChanges.clearCaches();
        LambdaResolver.clearCaches();
        Renderer.clearCaches();
    }

    public record Stacks(List<Option> options) {
    }

    public record Option(ItemStack stack, NumberExpr count, double chance) {
    }

    public record ComputedTrades(Int2ObjectMap<TradeLevel> levels, boolean incomplete) {
    }
}
