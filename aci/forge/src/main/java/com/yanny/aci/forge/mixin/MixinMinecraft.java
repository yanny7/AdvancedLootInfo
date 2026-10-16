package com.yanny.aci.forge.mixin;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Inject(method = "buildInitialScreens", at = @At("HEAD"))
    private void startEventBusBeforeQuickPlay(CallbackInfoReturnable<Runnable> cir) {
        if (ModLoader.isLoadingStateValid()) {
            MinecraftForge.EVENT_BUS.start();
        }
    }
}
