package com.yanny.ali.mixin;

import com.yanny.ali.AliMod;
import com.yanny.ali.manager.PluginManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
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
                    PluginManager.reloadServer();
                    AliMod.INFO_PROPAGATOR.server().readLootTables(server.reloadableRegistries(), server.overworld());

                    for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                        AliMod.INFO_PROPAGATOR.server().syncLootTables(player);
                    }
                }
            }

            return value;
        }, (MinecraftServer) (Object) this);
    }
}
