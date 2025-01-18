package com.yanny.advanced_loot_info.network;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class NetworkUtils {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static int messageId = 0;

    public static DistHolder<Client, Server> registerLootInfoPropagator(SimpleChannel channel) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            return registerClientLootInfoPropagator(channel);
        } else {
            return registerServerLootInfoPropagator(channel);
        }
    }

    @NotNull
    private static DistHolder<Client, Server> registerClientLootInfoPropagator(SimpleChannel channel) {
        Client client = new Client();
        Server server = new Server(channel);

        channel.registerMessage(getMessageId(), InfoSyncLootTableMessage.class, InfoSyncLootTableMessage::encode, InfoSyncLootTableMessage::new, client::onLootInfo);
        channel.registerMessage(getMessageId(), ClearMessage.class, ClearMessage::encode, ClearMessage::new, client::onClear);
        return new DistHolder<>(client, server);
    }

    @NotNull
    private static DistHolder<Client, Server> registerServerLootInfoPropagator(SimpleChannel channel) {
        Server server = new Server(channel);

        channel.registerMessage(getMessageId(), InfoSyncLootTableMessage.class, InfoSyncLootTableMessage::encode, InfoSyncLootTableMessage::new, (m, c) -> {});
        channel.registerMessage(getMessageId(), ClearMessage.class, ClearMessage::encode, ClearMessage::new, (m, c) -> {});
        return new DistHolder<>(null, server);
    }

    public static class Client {
        public List<InfoSyncLootTableMessage> lootEntries = new LinkedList<>();

        public void onLootInfo(InfoSyncLootTableMessage msg, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> lootEntries.add(msg));
            context.setPacketHandled(true);
        }

        public void onClear(ClearMessage msg, Supplier<NetworkEvent.Context> contextSupplier) {
            lootEntries.clear();
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> lootEntries.clear());
            context.setPacketHandled(true);
        }
    }

    public static class Server {
        private final SimpleChannel channel;
        private LootDataManager manager;

        public Server(SimpleChannel channel) {
            this.channel = channel;
        }

        public void syncLootTables(Player player) {
            if (player instanceof ServerPlayer serverPlayer) {
                channel.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new ClearMessage());
                manager.getKeys(LootDataType.TABLE).forEach((location) -> {
                    LootTable table = manager.getElement(LootDataType.TABLE, location);

                    if (table != null && table != LootTable.EMPTY && (player.level() instanceof ServerLevel serverLevel)) {
                        LootParams lootParams = (new LootParams.Builder(serverLevel)).create(LootContextParamSets.EMPTY);
                        LootContext lootContext = new LootContext.Builder(lootParams).create(null);
                        ObjectArrayList<Item> items = new ObjectArrayList<>();
                        LootTableEntry lootTableEntry = LootUtils.parseLoot(table, manager, lootContext, items, 1f, 0, 0);

                        if (!items.isEmpty()) {
                            channel.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new InfoSyncLootTableMessage(location, lootTableEntry));
                        } else {
                            LOGGER.warn("LootTable {} has no items!", location);
                        }
                    } else {
                        LOGGER.warn("Ignoring {} LootTable, because it's empty or null", location);
                    }
                });
            }
        }

        public void setLootDataManager(LootDataManager manager) {
            this.manager = manager;
        }
    }

    public static class ClearMessage {
        public ClearMessage() {
        }

        public ClearMessage(FriendlyByteBuf buf) {
        }

        public void encode(FriendlyByteBuf buf) {
        }
    }

    public static class InfoSyncLootTableMessage {
        public final ResourceLocation location;
        public final LootTableEntry value;

        public InfoSyncLootTableMessage(ResourceLocation location, LootTableEntry value) {
            this.location = location;
            this.value = value;
        }

        public InfoSyncLootTableMessage(FriendlyByteBuf buf) {
            location = buf.readResourceLocation();
            value = new LootTableEntry(buf);
        }

        public void encode(FriendlyByteBuf buf) {
            buf.writeResourceLocation(location);
            value.encode(buf);
        }
    }

    public record DistHolder<Client, Server>(
            @Nullable Client client,
            Server server
    ) {}

    private static int getMessageId() {
        return ++messageId;
    }
}
