package com.yanny.ali.network;

import com.yanny.ali.Utils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public class RequestLootDataMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RequestLootDataMessage> TYPE = new CustomPacketPayload.Type<>(Utils.modLoc("request_loot_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestLootDataMessage> CODEC = new StreamCodec<>() {
        @NotNull
        @Override
        public RequestLootDataMessage decode(RegistryFriendlyByteBuf buf) {
            return new RequestLootDataMessage(buf);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, RequestLootDataMessage message) {
            message.write(buf);
        }
    };

    public RequestLootDataMessage() {
    }

    public RequestLootDataMessage(FriendlyByteBuf buf) {
    }

    public void write(FriendlyByteBuf buf) {
    }

    @NotNull
    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
