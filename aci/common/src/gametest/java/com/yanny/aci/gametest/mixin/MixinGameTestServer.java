package com.yanny.aci.gametest.mixin;

import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.gametest.framework.MultipleTestTracker;
import net.minecraft.world.flag.FeatureFlagRegistry;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(GameTestServer.class)
public class MixinGameTestServer {
    @Shadow
    private MultipleTestTracker testTracker;

    @Redirect(method = "create", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/flag/FeatureFlagRegistry;allFlags()Lnet/minecraft/world/flag/FeatureFlagSet;"))
    private static FeatureFlagSet aci$withoutTradeRebalance(FeatureFlagRegistry registry) {
        return registry.allFlags().subtract(FeatureFlagSet.of(FeatureFlags.TRADE_REBALANCE));
    }

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
