package com.yanny.ali.mixin;

import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ForgeHooks.class, remap=false)
public class MixinForgeHooks {
    @Inject(method = "readPoolName", at = @At("HEAD"), cancellable = true)
    private static void readPoolNameALI(JsonObject json, CallbackInfoReturnable<String> cir) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            if (json.has("name")) {
                cir.setReturnValue(GsonHelper.getAsString(json, "name"));
            } else {
                cir.setReturnValue("");
            }
        }
    }
}
