package com.yanny.ali.mixin;

import com.yanny.ali.compatibility.common.EntityStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Shadow
    public ClientLevel level;

    @Inject(method = "setLevel", at = @At("HEAD"))
    public void setLevelALI(ClientLevel clientLevel, ReceivingLevelScreen.Reason reason, CallbackInfo ci) {
        if (this.level != null) {
            EntityStorage.onUnloadLevel();
        }
    }
}
