package com.yanny.aci.gametest.mixin;

import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.gametest.framework.MultipleTestTracker;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(GameTestServer.class)
public class MixinGameTestServer {
    @Shadow
    private MultipleTestTracker testTracker;

    @Inject(method = "tickServer", at = @At("TAIL"))
    private void aci$exitWhenDone(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        if (testTracker != null && testTracker.isDone()) {
            LogManager.shutdown();
            System.out.flush();
            System.err.flush();
            Runtime.getRuntime().halt(testTracker.getFailedRequiredCount());
        }
    }
}
