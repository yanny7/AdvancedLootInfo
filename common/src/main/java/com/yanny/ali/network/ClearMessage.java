package com.yanny.ali.network;

import com.yanny.ali.Utils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public class ClearMessage implements CustomPacketPayload {
    public static final Type<ClearMessage> TYPE = new Type<>(Utils.modLoc("loot_table_clear"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClearMessage> CODEC = new StreamCodec<>() {
        @NotNull
        @Override
        public ClearMessage decode(@NotNull RegistryFriendlyByteBuf buf) {
            return new ClearMessage(buf);
        }

        @Override
        public void encode(@NotNull RegistryFriendlyByteBuf buf, @NotNull ClearMessage message) {
            message.write(buf);
        }
    };

    public final int totalMessages;

    public ClearMessage(int totalMessages) {
        this.totalMessages = totalMessages;
    }

    public ClearMessage(FriendlyByteBuf buf) {
        totalMessages = buf.readInt();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(totalMessages);
    }

    @NotNull
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
