package com.yanny.ali.mixin;

import com.yanny.ali.network.ClearMessage;
import com.yanny.ali.network.DoneMessage;
import com.yanny.ali.network.LootDataChunkMessage;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.impl.networking.client.ClientPlayNetworkAddon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientPlayNetworkAddon.class)
public class MixinClientPlayNetworkAddon {
    @Inject(
            method = "Lnet/fabricmc/fabric/impl/networking/client/ClientPlayNetworkAddon;receive(Lnet/fabricmc/fabric/api/client/networking/v1/ClientPlayNetworking$PlayPayloadHandler;Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void ali$reload(ClientPlayNetworking.PlayPayloadHandler<?> handler, CustomPacketPayload payload, CallbackInfo ci) {
        PacketSender packetSender = (PacketSender) this;

        if (payload instanceof ClearMessage || payload instanceof LootDataChunkMessage || payload instanceof DoneMessage) {
            // execute on networking thread to avoid deadlock
            ((ClientPlayNetworking.PlayPayloadHandler) handler).receive(payload, new ClientPlayNetworking.Context() {
                @Override
                public Minecraft client() {
                    return Minecraft.getInstance();
                }

                @Override
                public LocalPlayer player() {
                    return Minecraft.getInstance().player;
                }

                @Override
                public PacketSender responseSender() {
                    return packetSender;
                }
            });
            ci.cancel();
        }
    }
}
