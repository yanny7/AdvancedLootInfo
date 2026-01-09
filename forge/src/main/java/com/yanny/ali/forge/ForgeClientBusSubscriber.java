package com.yanny.ali.forge;

import com.yanny.ali.Utils;
import com.yanny.ali.compatibility.common.EntityStorage;
import com.yanny.ali.manager.PluginManager;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Utils.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ForgeClientBusSubscriber {
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
