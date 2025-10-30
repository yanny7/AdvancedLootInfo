package com.yanny.ali;

import com.yanny.ali.datagen.DataGeneration;
import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.network.AbstractClient;
import com.yanny.ali.network.AbstractServer;
import com.yanny.ali.network.DistHolder;
import com.yanny.ali.network.NetworkUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.network.NetworkRegistry;
import net.neoforged.neoforge.network.simple.SimpleChannel;

@Mod(Utils.MOD_ID)
public class AliMod {
    public static final DistHolder<AbstractClient, AbstractServer> INFO_PROPAGATOR;
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

    public AliMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(DataGeneration::generate);
        modEventBus.addListener(AliMod::registerCommonEvent);
        modEventBus.addListener(AliMod::registerClientEvent);
    }

    public static void registerCommonEvent(@SuppressWarnings("unused") FMLCommonSetupEvent event) {
        PluginManager.registerCommonEvent();
    }

    public static void registerClientEvent(@SuppressWarnings("unused") FMLClientSetupEvent event) {
        PluginManager.registerClientEvent();
    }
}
