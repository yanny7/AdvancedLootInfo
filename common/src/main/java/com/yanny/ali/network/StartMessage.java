package com.yanny.ali.network;

import com.yanny.ali.Utils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public class StartMessage implements CustomPacketPayload {
    public static final Type<StartMessage> TYPE = new Type<>(Utils.modLoc("loot_table_start"));
    public static final StreamCodec<RegistryFriendlyByteBuf, StartMessage> CODEC = new StreamCodec<>() {
        @NotNull
        @Override
        public StartMessage decode(RegistryFriendlyByteBuf buf) {
            return new StartMessage(buf);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, StartMessage message) {
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
