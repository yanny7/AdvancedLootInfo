package com.yanny.ali.neoforge;

import com.yanny.ali.Utils;
import com.yanny.ali.compatibility.common.EntityStorage;
import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.network.StartMessage;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.network.connection.ConnectionPhase;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

@EventBusSubscriber(modid = Utils.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class NeoForgeClientBusSubscriber {
    @SubscribeEvent
    public static void onClientLevelUnload(LevelEvent.Unload event) {
        LevelAccessor level = event.getLevel();

        if (level.isClientSide()) {
            EntityStorage.onUnloadLevel();
        }
    }

    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        boolean connected = NetworkRegistry.getInstance().isConnected(event.getConnection(), ConnectionPhase.ANY, StartMessage.ID);
        PluginManager.CLIENT_REGISTRY.loggingIn(connected);
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        PluginManager.CLIENT_REGISTRY.loggingOut();
    }
}
