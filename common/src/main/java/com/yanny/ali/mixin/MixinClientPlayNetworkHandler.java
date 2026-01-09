package com.yanny.ali.mixin;

import com.yanny.ali.manager.PluginManager;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundUpdateTagsPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class MixinClientPlayNetworkHandler {
    // This is called on reload
    @Inject(method = "handleUpdateTags", at = @At("TAIL"))
    public void handleInvokeTags(ClientboundUpdateTagsPacket clientboundUpdateTagsPacket, CallbackInfo ci) {
        PluginManager.CLIENT_REGISTRY.reloadLootData();
    }
}
