package com.yanny.ali;

import com.yanny.ali.compatibility.common.EntityStorage;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Utils.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeClientBusSubscriber {
    @SubscribeEvent
    public static void onClientLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ClientLevel) {
            EntityStorage.onUnloadLevel();
        }
    }
}
