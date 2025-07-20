package com.yanny.ali.network;

import com.yanny.ali.Utils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public class DoneMessage implements CustomPacketPayload {
    public static final Type<DoneMessage> TYPE = new Type<>(Utils.modLoc("loot_table_done"));
    public static final StreamCodec<RegistryFriendlyByteBuf, DoneMessage> CODEC = new StreamCodec<>() {
        @NotNull
        @Override
        public DoneMessage decode(@NotNull RegistryFriendlyByteBuf buf) {
            return new DoneMessage(buf);
        }

        @Override
        public void encode(@NotNull RegistryFriendlyByteBuf buf, @NotNull DoneMessage message) {
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
