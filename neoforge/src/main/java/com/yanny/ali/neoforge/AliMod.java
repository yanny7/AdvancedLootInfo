package com.yanny.ali.neoforge;

import com.yanny.ali.Utils;
import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.neoforge.datagen.DataGeneration;
import com.yanny.ali.neoforge.network.NetworkUtils;
import com.yanny.ali.neoforge.network.Server;
import com.yanny.ali.network.AbstractServer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.network.NetworkRegistry;
import net.neoforged.neoforge.network.simple.SimpleChannel;

@Mod(Utils.MOD_ID)
public class AliMod {
    public static final AbstractServer SERVER;
    public static final SimpleChannel CHANNEL;
    private static final String PROTOCOL_VERSION = "2";

    static {
        CHANNEL = NetworkRegistry.ChannelBuilder.named(Utils.modLoc("network"))
                .networkProtocolVersion(() -> PROTOCOL_VERSION)
                .clientAcceptedVersions(NetworkRegistry.acceptMissingOr(PROTOCOL_VERSION))
                .serverAcceptedVersions(NetworkRegistry.acceptMissingOr(PROTOCOL_VERSION))
                .simpleChannel();

        NetworkUtils.registerClient(CHANNEL);
        SERVER = new Server(CHANNEL);
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
