package com.yanny.advanced_loot_info;

import com.yanny.advanced_loot_info.configuration.Config;
import com.yanny.advanced_loot_info.datagen.DataGeneration;
import com.yanny.advanced_loot_info.manager.PluginManager;
import com.yanny.advanced_loot_info.network.NetworkUtils;
import com.yanny.advanced_loot_info.registries.LootCategories;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.commons.lang3.tuple.Pair;

@Mod(AdvancedLootInfoMod.MOD_ID)
public class AdvancedLootInfoMod {
    public static final String MOD_ID = "advanced_loot_info";
    public static final Config CONFIGURATION;
    public static final NetworkUtils.DistHolder<NetworkUtils.Client, NetworkUtils.Server> INFO_PROPAGATOR;

    private static final String PROTOCOL_VERSION = "1";
    private static final ForgeConfigSpec CONFIGURATION_SPEC;

    static {
        Pair<Config, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(Config::new);
        SimpleChannel channel = NetworkRegistry.newSimpleChannel(
                Utils.modLoc("network"),
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals
        );

        CONFIGURATION = pair.getKey();
        CONFIGURATION_SPEC = pair.getValue();
        INFO_PROPAGATOR = NetworkUtils.registerLootInfoPropagator(channel);
    }

    public AdvancedLootInfoMod(IEventBus modEventBus) {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CONFIGURATION_SPEC);

        modEventBus.addListener(DataGeneration::generate);
        modEventBus.addListener(PluginManager::registerCommonEvent);
        modEventBus.addListener(PluginManager::registerClientEvent);
        MinecraftForge.EVENT_BUS.addListener(LootCategories::onResourceReload);
    }
}
