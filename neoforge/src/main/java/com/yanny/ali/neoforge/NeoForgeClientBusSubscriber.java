package com.yanny.ali.neoforge;

import com.yanny.ali.Utils;
import com.yanny.ali.compatibility.common.EntityStorage;
import com.yanny.ali.manager.PluginManager;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

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
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        PluginManager.CLIENT_REGISTRY.logOut();
    }
}
