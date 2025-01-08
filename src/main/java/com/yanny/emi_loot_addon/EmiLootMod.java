package com.yanny.emi_loot_addon;

import com.yanny.emi_loot_addon.configuration.Config;
import com.yanny.emi_loot_addon.datagen.DataGeneration;
import com.yanny.emi_loot_addon.network.NetworkUtils;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.commons.lang3.tuple.Pair;

@Mod(EmiLootMod.MOD_ID)
public class EmiLootMod {
    public static final String MOD_ID = "emi_loot_addon";
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

    public EmiLootMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CONFIGURATION_SPEC);

        modEventBus.addListener(DataGeneration::generate);
    }
}
