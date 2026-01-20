package com.yanny.ali.network;

import com.yanny.ali.Utils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public class StartMessage implements CustomPacketPayload {
    public static final Type<ClearMessage> TYPE = new Type<>(Utils.modLoc("loot_table_start"));
    public static final StreamCodec<RegistryFriendlyByteBuf, StartMessage> CODEC = new StreamCodec<>() {
        @NotNull
        @Override
        public StartMessage decode(@NotNull RegistryFriendlyByteBuf buf) {
            return new ClearMessage(buf);
        }

        @Override
        public void encode(@NotNull RegistryFriendlyByteBuf buf, @NotNull StartMessage message) {
            message.write(buf);
        }
    };

    public final int totalMessages;

    public StartMessage(int totalMessages) {
        this.totalMessages = totalMessages;
    }

    public StartMessage(FriendlyByteBuf buf) {
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
