package com.yanny.awi.forge;

import com.yanny.awi.Utils;
import com.yanny.awi.forge.datagen.DataGeneration;
import com.yanny.awi.forge.network.NetworkUtils;
import com.yanny.awi.forge.network.Server;
import com.yanny.awi.manager.PluginManager;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.SimpleChannel;

@Mod(Utils.MOD_ID)
public class AwiMod {
    public static final Server SERVER;
    public static final SimpleChannel CHANNEL;

    private static final int PROTOCOL_VERSION = 1;

    static {
        CHANNEL = ChannelBuilder.named(Utils.modLoc("network"))
                .networkProtocolVersion(PROTOCOL_VERSION)
                .optional()
                .simpleChannel();

        SERVER = new Server(CHANNEL);
        NetworkUtils.registerClient(CHANNEL);
        NetworkUtils.registerCommon(CHANNEL, SERVER);
    }

    public AwiMod() {
        FMLJavaModLoadingContext context = FMLJavaModLoadingContext.get();
        IEventBus modEventBus = context.getModEventBus();

        modEventBus.addListener(DataGeneration::generate);
        modEventBus.addListener(AwiMod::registerCommonEvent);
        modEventBus.addListener(AwiMod::registerClientEvent);
    }

    public static void registerCommonEvent(@SuppressWarnings("unused") FMLCommonSetupEvent event) {
        PluginManager.getInstance().registerCommonEvent();
    }

    public static void registerClientEvent(@SuppressWarnings("unused") FMLClientSetupEvent event) {
        PluginManager.getInstance().registerClientEvent();
    }
}
