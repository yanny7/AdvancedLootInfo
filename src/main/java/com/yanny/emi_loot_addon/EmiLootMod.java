package com.yanny.emi_loot_addon;

import com.yanny.emi_loot_addon.datagen.DataGeneration;
import com.yanny.emi_loot_addon.network.NetworkUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod(EmiLootMod.MOD_ID)
public class EmiLootMod {
    public static final String MOD_ID = "emi_loot_addon";
    public static final NetworkUtils.DistHolder<NetworkUtils.Client, NetworkUtils.Server> INFO_PROPAGATOR;

    private static final String PROTOCOL_VERSION = "1";

    static {
        SimpleChannel channel = NetworkRegistry.newSimpleChannel(
                Utils.modLoc("network"),
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals
        );

        INFO_PROPAGATOR = NetworkUtils.registerLootInfoPropagator(channel);
    }

    public EmiLootMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(DataGeneration::generate);
    }
}
