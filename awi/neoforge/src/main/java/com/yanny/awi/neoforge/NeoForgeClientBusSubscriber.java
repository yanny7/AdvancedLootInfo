package com.yanny.awi.neoforge;

import com.yanny.awi.Utils;
import com.yanny.awi.manager.PluginManager;
import com.yanny.awi.network.RequestWorldgenDataMessage;
import net.minecraft.network.ConnectionProtocol;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

@EventBusSubscriber(modid = Utils.MOD_ID)
public class NeoForgeClientBusSubscriber {
    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        PluginManager.getInstance().clientRegistry.loggingIn(NetworkRegistry.hasChannel(event.getConnection(), ConnectionProtocol.PLAY, RequestWorldgenDataMessage.TYPE.id()));
    }

    @SubscribeEvent
    public static void onLoggingOut(@SuppressWarnings("unused") ClientPlayerNetworkEvent.LoggingOut event) {
        PluginManager.getInstance().clientRegistry.loggingOut();
    }
}
