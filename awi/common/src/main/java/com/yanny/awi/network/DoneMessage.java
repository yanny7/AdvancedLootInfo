package com.yanny.awi.network;

import com.yanny.awi.Utils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public class DoneMessage implements CustomPacketPayload {
    public static final Type<DoneMessage> TYPE = new Type<>(Utils.modLoc("done_worldgen_info"));
    public static final StreamCodec<RegistryFriendlyByteBuf, DoneMessage> CODEC = new StreamCodec<>() {
        @NotNull
        @Override
        public DoneMessage decode(RegistryFriendlyByteBuf buf) {
            return new DoneMessage(buf);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, DoneMessage message) {
            message.write(buf);
        }
    };

    public DoneMessage() {
    }

    public DoneMessage(FriendlyByteBuf buf) {
    }

    public void write(FriendlyByteBuf buf) {
    }

    @NotNull
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
