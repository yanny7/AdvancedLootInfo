package com.yanny.ali;

import com.yanny.ali.compatibility.common.EntityStorage;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.level.LevelEvent;

@Mod.EventBusSubscriber(modid = Utils.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class NeoForgeClientBusSubscriber {
    @SubscribeEvent
    public static void onClientLevelUnload(LevelEvent.Unload event) {
        LevelAccessor level = event.getLevel();

        if (level.isClientSide()) {
            EntityStorage.onUnloadLevel();
        }
    }
}
