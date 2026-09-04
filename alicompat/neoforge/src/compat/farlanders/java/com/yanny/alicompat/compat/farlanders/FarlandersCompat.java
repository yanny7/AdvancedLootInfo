package com.yanny.alicompat.compat.farlanders;

import com.yanny.ali.api.IServerRegistry;
import com.yanny.alicompat.IModCompat;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.trading.TradeSet;
import org.jetbrains.annotations.NotNull;

public class FarlandersCompat implements IModCompat {
    private static final String MOD_ID = "farlanders";

    @NotNull
    @Override
    public String targetModId() {
        return MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerTrades(id("farlander"), () -> tradeSets("farlander", 5));
        registry.registerTrades(id("elder_farlander"), () -> tradeSets("elder", 5));
        registry.registerTrades(id("wanderer"), () -> tradeSets("wanderer", 1));
    }

    @NotNull
    private static Int2ObjectMap<ResourceKey<TradeSet>> tradeSets(String name, int levels) {
        Int2ObjectMap<ResourceKey<TradeSet>> tradeSets = new Int2ObjectOpenHashMap<>();

        for (int level = 1; level <= levels; level++) {
            tradeSets.put(level, ResourceKey.create(Registries.TRADE_SET, id(name + "/level_" + level)));
        }

        return tradeSets;
    }

    @NotNull
    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
