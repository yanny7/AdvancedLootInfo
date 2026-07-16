package com.yanny.awi.network;

import com.yanny.awi.Utils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public class RequestWorldgenDataMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RequestWorldgenDataMessage> TYPE = new CustomPacketPayload.Type<>(Utils.modLoc("request_worldgen_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestWorldgenDataMessage> CODEC = new StreamCodec<>() {
        @NotNull
        @Override
        public RequestWorldgenDataMessage decode(RegistryFriendlyByteBuf buf) {
            return new RequestWorldgenDataMessage(buf);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, RequestWorldgenDataMessage message) {
            message.write(buf);
        }
    };

    public RequestWorldgenDataMessage() {
    }

    public RequestWorldgenDataMessage(FriendlyByteBuf buf) {
    }

    public void write(FriendlyByteBuf buf) {
    }

    @NotNull
    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
