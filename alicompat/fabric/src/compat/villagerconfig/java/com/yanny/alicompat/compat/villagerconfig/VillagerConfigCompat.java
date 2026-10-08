package com.yanny.alicompat.compat.villagerconfig;

import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.TradeLevel;
import com.yanny.alicompat.IModCompat;
import com.yanny.alicompat.accessor.PluginUtils;
import com.yanny.alicompat.accessor.ReflectionUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import me.drex.villagerconfig.common.VillagerConfig;
import me.drex.villagerconfig.common.data.TradeTable;
import me.drex.villagerconfig.common.util.TradeProvider;
import me.drex.villagerconfig.common.util.loot.function.EnchantRandomlyLootFunction;
import me.drex.villagerconfig.common.util.loot.function.SetDyeFunction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VillagerConfigCompat implements IModCompat {
    private static final Identifier WANDERING_TRADER = Identifier.withDefaultNamespace("wandering_trader");

    @NotNull
    @Override
    public String targetModId() {
        return VillagerConfigLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerFunctionTooltip(registry, EnchantRandomlyLootFunction.class, EnchantRandomlyLootFunctionAccessor.class);
        PluginUtils.registerItemStackModifier(registry, EnchantRandomlyLootFunction.class, EnchantRandomlyLootFunctionAccessor.class);
        PluginUtils.registerFunctionTooltip(registry, SetDyeFunction.class, SetDyeFunctionAccessor.class);

        registry.registerTradeOverride(VillagerConfigCompat::getTradeLevels);
    }

    @Nullable
    private static Int2ObjectMap<TradeLevel> getTradeLevels(IServerUtils utils, Identifier traderId) {
        TradeTable tradeTable = getTradeTable(traderId);

        if (tradeTable == null) {
            return null;
        }

        return ReflectionUtils.copyClassData(TradeTableAccessor.class, tradeTable, TradeTable.class).getLevels(utils);
    }

    @Nullable
    private static TradeTable getTradeTable(Identifier traderId) {
        if (traderId.equals(WANDERING_TRADER)) {
            return VillagerConfig.TRADE_MANAGER.getTrade(TradeProvider.WANDERING_TRADER_ID);
        }
        if (BuiltInRegistries.VILLAGER_PROFESSION.containsKey(traderId)) {
            return VillagerConfig.TRADE_MANAGER.getTrade(traderId);
        }

        return null;
    }
}
