package com.yanny.ali.forge;

import com.yanny.ali.Utils;
import com.yanny.ali.forge.datagen.DataGeneration;
import com.yanny.ali.network.AbstractServer;
import com.yanny.ali.forge.network.NetworkUtils;
import com.yanny.ali.forge.network.Server;
import com.yanny.ali.manager.PluginManager;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod(Utils.MOD_ID)
public class AliMod {
    public static final AbstractServer SERVER;

    private static final String PROTOCOL_VERSION = "1";

    static {
        SimpleChannel channel = NetworkRegistry.newSimpleChannel(
                Utils.modLoc("network"),
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals
        );

        NetworkUtils.registerClient(channel);
        SERVER = new Server(channel);
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
