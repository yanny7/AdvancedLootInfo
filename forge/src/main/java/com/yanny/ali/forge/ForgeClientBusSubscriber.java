package com.yanny.ali.forge;

import com.yanny.ali.Utils;
import com.yanny.ali.compatibility.common.EntityStorage;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Utils.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeClientBusSubscriber {
    @SubscribeEvent
    public static void onClientLevelUnload(LevelEvent.Unload event) {
        LevelAccessor level = event.getLevel();

        if (level.isClientSide()) {
            EntityStorage.onUnloadLevel();
        }
    }
}
