package com.yanny.ali.fabric.mixin;

import com.yanny.ali.fabric.platform.FabricPlatformHelper;
import net.minecraft.commands.Commands;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.flag.FeatureFlagSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(ReloadableServerResources.class)
public class ReloadableServerResourceMixin {
    @Inject(method = "loadResources", at = @At("HEAD"))
    private static void captureRegistries(ResourceManager resourceManager, LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess, FeatureFlagSet featureFlagSet,
                                          Commands.CommandSelection commandSelection, int i, Executor executor, Executor executor2,
                                          CallbackInfoReturnable<CompletableFuture<ReloadableServerResources>> cir) {
        FabricPlatformHelper.PROVIDER = layeredRegistryAccess.compositeAccess();
    }
}
