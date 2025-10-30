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
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;

@Mod(Utils.MOD_ID)
public class AliMod {
    public static DistHolder<AbstractClient, AbstractServer> INFO_PROPAGATOR;
    private static final String PROTOCOL_VERSION = "1";

    public AliMod(IEventBus modEventBus) {
        modEventBus.addListener(DataGeneration::generate);
        modEventBus.addListener(AliMod::registerCommonEvent);
        modEventBus.addListener(AliMod::registerClientEvent);
        modEventBus.addListener(AliMod::registerPayloadHandler);
    }

    public static void registerCommonEvent(@SuppressWarnings("unused") FMLCommonSetupEvent event) {
        PluginManager.registerCommonEvent();
    }

    public static void registerClientEvent(@SuppressWarnings("unused") FMLClientSetupEvent event) {
        PluginManager.registerClientEvent();
    }

    public static void registerPayloadHandler(final RegisterPayloadHandlerEvent event) {
        INFO_PROPAGATOR = NetworkUtils.registerLootInfoPropagator(event.registrar(Utils.MOD_ID).versioned(PROTOCOL_VERSION));
    }
}
