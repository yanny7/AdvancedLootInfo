package com.yanny.ali;

import com.yanny.ali.datagen.DataGeneration;
import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.network.NetworkUtils;
import com.yanny.ali.registries.LootCategories;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod(Utils.MOD_ID)
public class AdvancedLootInfoMod {
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

    public AdvancedLootInfoMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(DataGeneration::generate);
        modEventBus.addListener(PluginManager::registerCommonEvent);
        modEventBus.addListener(PluginManager::registerClientEvent);
        MinecraftForge.EVENT_BUS.addListener(LootCategories::onResourceReload);
    }
}
