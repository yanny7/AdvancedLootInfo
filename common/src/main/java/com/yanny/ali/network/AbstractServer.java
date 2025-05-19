package com.yanny.ali.network;

import com.mojang.logging.LogUtils;
import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.plugin.server.ItemCollectorUtils;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootTable;
import org.slf4j.Logger;

import java.util.LinkedList;
import java.util.List;

public abstract class AbstractServer {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final List<InfoSyncLootTableMessage> messages = new LinkedList<>();

    public final void readLootTables(ReloadableServerRegistries.Holder manager, ServerLevel level) {
        Registry<LootTable> registry = (Registry<LootTable>)manager.lookup().lookup(Registries.LOOT_TABLE).orElseThrow();

        messages.clear();
        registry.entrySet().forEach((e) -> PluginManager.SERVER_REGISTRY.addLootTable(e.getKey(), e.getValue()));
        registry.entrySet().forEach((reference) -> {
            ResourceKey<LootTable> location = reference.getKey();
            LootTable table = reference.getValue();

            if (table != LootTable.EMPTY) {
                List<Item> items = ItemCollectorUtils.collectLootTable(PluginManager.SERVER_REGISTRY, table);

                if (!items.isEmpty()) {
                    messages.add(new InfoSyncLootTableMessage(location, table, items));
                } else {
                    LOGGER.info("LootTable {} has no items", location);
                }
            } else {
                LOGGER.warn("Ignoring {} LootTable, because it's empty or null", location);
            }
        });

        LOGGER.info("Prepared {} loot tables", messages.size());
    }

    public final void syncLootTables(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            sendClearMessage(serverPlayer, new ClearMessage());

            for (InfoSyncLootTableMessage message : messages) {
                try {
                    sendSyncMessage(serverPlayer, message);
                } catch (Throwable e) {
                    LOGGER.warn("Failed to send message for loot table {} with error: {}", message.location().location(), e.getMessage());
                }
            }
        }
    }

    protected abstract void sendClearMessage(ServerPlayer serverPlayer, ClearMessage message);

    protected abstract void sendSyncMessage(ServerPlayer serverPlayer, InfoSyncLootTableMessage message);
}
