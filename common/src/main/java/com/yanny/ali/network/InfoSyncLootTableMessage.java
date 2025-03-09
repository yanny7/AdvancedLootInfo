package com.yanny.ali.network;

import com.yanny.ali.Utils;
import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.plugin.entry.LootTableEntry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class InfoSyncLootTableMessage implements CustomPacketPayload {
    public static final Type<InfoSyncLootTableMessage> TYPE = new Type<>(Utils.modLoc("loot_table_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, InfoSyncLootTableMessage> CODEC = new StreamCodec<>() {
        @NotNull
        @Override
        public InfoSyncLootTableMessage decode(@NotNull RegistryFriendlyByteBuf buf) {
            return new InfoSyncLootTableMessage(buf);
        }

        @Override
        public void encode(@NotNull RegistryFriendlyByteBuf buf, @NotNull InfoSyncLootTableMessage message) {
            message.write(buf);
        }
    };

    public final ResourceLocation location;
    public final LootTableEntry value;

    public InfoSyncLootTableMessage(ResourceLocation location, LootTableEntry value) {
        this.location = location;
        this.value = value;
    }

    public InfoSyncLootTableMessage(FriendlyByteBuf buf) {
        location = buf.readResourceLocation();
        value = new LootTableEntry(new AliContext(null, PluginManager.COMMON_REGISTRY, null), buf);
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(location);
        value.encode(new AliContext(null, PluginManager.COMMON_REGISTRY, null), buf);
    }

    @NotNull
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
