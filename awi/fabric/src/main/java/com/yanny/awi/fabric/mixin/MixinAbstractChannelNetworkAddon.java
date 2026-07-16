package com.yanny.awi.fabric.mixin;

import com.yanny.awi.network.DoneMessage;
import com.yanny.awi.network.StartMessage;
import com.yanny.awi.network.WorldgenDataChunkMessage;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.impl.networking.AbstractChanneledNetworkAddon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AbstractChanneledNetworkAddon.class)
public class MixinAbstractChannelNetworkAddon {
    @Inject(
            method = "handle(Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void ali$handleOnNetworkingThread(CustomPacketPayload payload, CallbackInfoReturnable<Boolean> cir) {
        boolean isLootPacket = payload instanceof StartMessage || payload instanceof WorldgenDataChunkMessage || payload instanceof DoneMessage;

        if (isLootPacket) {
            AbstractChanneledNetworkAddon<?> addon = (AbstractChanneledNetworkAddon<?>)(Object)this;
            Object handler = addon.getHandler(payload.type().id());

            if (handler instanceof ClientPlayNetworking.PlayPayloadHandler playHandler) {
                PacketSender packetSender = addon;

                playHandler.receive(payload, new ClientPlayNetworking.Context() {
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

                cir.setReturnValue(true);
            }
        }
    }
}
