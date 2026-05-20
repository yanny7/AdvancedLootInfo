package com.yanny.ali.neoforge;

import com.yanny.ali.Utils;
import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.neoforge.datagen.DataGeneration;
import com.yanny.ali.neoforge.network.NetworkUtils;
import com.yanny.ali.neoforge.network.Server;
import com.yanny.ali.neoforge.platform.NeoForgePlatformHelper;
import com.yanny.ali.network.AbstractServer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@Mod(Utils.MOD_ID)
public class AliMod {
    public static final AbstractServer SERVER = new Server();
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
        NetworkUtils.registerClient(event.registrar(Utils.MOD_ID).optional().versioned(PROTOCOL_VERSION));
    }

    @SubscribeEvent
    public void onAddReloadListener(AddServerReloadListenersEvent event) {
        NeoForgePlatformHelper.PROVIDER = event.getRegistryAccess();
        event.addListener(ResourceLocation.fromNamespaceAndPath(Utils.MOD_ID, "fake_loot_manager"), SERVER.getFakeLootDataManager(event.getRegistryAccess()));
    }
}
