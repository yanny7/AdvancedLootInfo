package com.yanny.awi.neoforge;

import com.yanny.awi.Utils;
import com.yanny.awi.manager.PluginManager;
import com.yanny.awi.neoforge.datagen.DataGeneration;
import com.yanny.awi.neoforge.network.Client;
import com.yanny.awi.neoforge.network.NetworkUtils;
import com.yanny.awi.neoforge.network.Server;
import com.yanny.awi.pip.BlockPictureInPictureRenderer;
import com.yanny.awi.pip.BlockRenderState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@Mod(Utils.MOD_ID)
public class AwiMod {
    public static final Server SERVER = new Server();
    public static final Client CLIENT = new Client();
    private static final String PROTOCOL_VERSION = "1";

    public AwiMod(IEventBus modEventBus) {
        modEventBus.addListener(DataGeneration::generate);
        modEventBus.addListener(AwiMod::registerCommonEvent);
        modEventBus.addListener(AwiMod::registerClientEvent);
        modEventBus.addListener(AwiMod::registerPayloadHandler);
        modEventBus.addListener(AwiMod::registerPipRenderer);
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

    public static void registerPipRenderer(final RegisterPictureInPictureRenderersEvent event) {
        event.register(BlockRenderState.class, BlockPictureInPictureRenderer::new);
    }
}
