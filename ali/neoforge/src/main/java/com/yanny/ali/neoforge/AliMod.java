package com.yanny.ali.neoforge;

import com.yanny.ali.Utils;
import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.neoforge.datagen.DataGeneration;
import com.yanny.ali.neoforge.network.Client;
import com.yanny.ali.neoforge.network.NetworkUtils;
import com.yanny.ali.neoforge.network.Server;
import com.yanny.ali.neoforge.platform.NeoForgePlatformHelper;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@Mod(Utils.MOD_ID)
public class AliMod {
    public static final Server SERVER = new Server();
    public static final Client CLIENT = new Client();
    private static final String PROTOCOL_VERSION = "2";

    public AliMod(IEventBus modEventBus) {
        modEventBus.addListener(DataGeneration::generate);
        modEventBus.addListener(AliMod::registerCommonEvent);
        modEventBus.addListener(AliMod::registerClientEvent);
        modEventBus.addListener(AliMod::registerPayloadHandler);

        NeoForge.EVENT_BUS.register(this);
    }

    public static void registerCommonEvent(@SuppressWarnings("unused") FMLCommonSetupEvent event) {
        PluginManager.getInstance().registerCommonEvent();
    }

    public static void registerClientEvent(@SuppressWarnings("unused") FMLClientSetupEvent event) {
        PluginManager.getInstance().registerClientEvent();
    }

    public static void registerPayloadHandler(final RegisterPayloadHandlersEvent event) {
        NetworkUtils.registerClient(event.registrar(Utils.MOD_ID).optional().versioned(PROTOCOL_VERSION), CLIENT);
        NetworkUtils.registerCommon(event.registrar(Utils.MOD_ID).optional().versioned(PROTOCOL_VERSION), SERVER);
    }

    @SubscribeEvent
    public void onAddReloadListener(AddReloadListenerEvent event) {
        NeoForgePlatformHelper.PROVIDER = event.getRegistryAccess();
        event.addListener(SERVER.getFakeLootDataManager());
    }
}
