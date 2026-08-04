package com.yanny.awi.neoforge.mixin;

import com.yanny.awi.manager.PluginManager;
import com.yanny.awi.neoforge.AwiMod;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {
    @Inject(method = "reloadResources", at = @At("TAIL"))
    private void endResourceReload(Collection<String> collection, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        cir.getReturnValue().handleAsync((value, throwable) -> {
            if (throwable == null) {
                MinecraftServer server = (MinecraftServer) (Object) this;

                if (server != null) {
                    PluginManager.getInstance().reloadServer();
                    AwiMod.SERVER.readWorldgenInfo(server.overworld());
                }
            }

            return value;
        }, (MinecraftServer) (Object) this);
    }
}
