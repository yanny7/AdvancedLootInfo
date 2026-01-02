package com.yanny.ali.neoforge.mixin;

import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.neoforge.AliMod;
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
                    AliMod.SERVER.readLootTables(server.getLootData(), server.overworld());

                    for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                        AliMod.SERVER.syncLootTables(player);
                    }
                }
            }

            return value;
        }, (MinecraftServer) (Object) this);
    }
}
