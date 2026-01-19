package com.yanny.ali.forge;

import com.yanny.ali.Utils;
import com.yanny.ali.forge.datagen.DataGeneration;
import com.yanny.ali.forge.network.NetworkUtils;
import com.yanny.ali.forge.network.Server;
import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.network.AbstractServer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.SimpleChannel;

@Mod(Utils.MOD_ID)
public class AliMod {
    public static final AbstractServer SERVER;
    public static final SimpleChannel CHANNEL;

    private static final int PROTOCOL_VERSION = 2;

    static {
        CHANNEL = ChannelBuilder.named(Utils.modLoc("network"))
                .networkProtocolVersion(PROTOCOL_VERSION)
                .clientAcceptedVersions(NetworkRegistry.acceptMissingOr(PROTOCOL_VERSION))
                .serverAcceptedVersions(NetworkRegistry.acceptMissingOr(PROTOCOL_VERSION))
                .simpleChannel();

        NetworkUtils.registerClient(CHANNEL);
        SERVER = new Server(CHANNEL);
    }

    public AliMod() {
        FMLJavaModLoadingContext context = FMLJavaModLoadingContext.get();
        IEventBus modEventBus = context.getModEventBus();

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
