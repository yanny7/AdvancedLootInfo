package com.yanny.alicompat.compat.goblintraders;

import com.mrcrayfish.goblintraders.loot_functions.IncreaseDurabilityFunction;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.TradeLevel;
import com.yanny.alicompat.IModCompat;
import com.yanny.alicompat.accessor.PluginUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.trading.TradeSet;
import org.jetbrains.annotations.NotNull;

public class GoblinTradersCompat implements IModCompat {
    private static final String MOD_ID = "goblintraders";
    private static final String[] RARITIES = {"common", "uncommon", "rare", "epic", "legendary"};

    @NotNull
    @Override
    public String targetModId() {
        return MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerFunctionTooltip(registry, IncreaseDurabilityFunction.class, IncreaseDurabilityFunctionAccessor.class);

        registerTrader(registry, "goblin_trader");
        registerTrader(registry, "vein_goblin_trader");
    }

    private static void registerTrader(IServerRegistry registry, String name) {
        Identifier traderId = id(name);

        registry.registerTrades(traderId, BuiltInRegistries.ENTITY_TYPE.getValue(traderId), () -> TradeLevel.ofSets(tradeSets(name)));
    }

    // a goblin adds every rarity set instead of picking one - keyed as levels here
    @NotNull
    private static Int2ObjectMap<ResourceKey<TradeSet>> tradeSets(String name) {
        Int2ObjectMap<ResourceKey<TradeSet>> tradeSets = new Int2ObjectOpenHashMap<>();

        for (int i = 0; i < RARITIES.length; i++) {
            tradeSets.put(i + 1, ResourceKey.create(Registries.TRADE_SET, id(name + "/" + RARITIES[i])));
        }

        return tradeSets;
    }

    @NotNull
    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
