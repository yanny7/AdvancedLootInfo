package com.yanny.ali.network;

import com.mojang.logging.LogUtils;
import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.plugin.server.ItemCollectorUtils;
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
        messages.clear();
        manager.get().lookup(Registries.LOOT_TABLE).ifPresent((lookup) -> {
            lookup.listElements().forEach((reference) -> PluginManager.SERVER_REGISTRY.addLootTable(reference.key(), reference.value()));
            lookup.listElements().forEach((reference) -> {
                ResourceKey<LootTable> location = reference.key();
                LootTable table = reference.value();

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
        });

        LOGGER.info("Prepared {} loot tables", messages.size());
    }

    public final void syncLootTables(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            sendClearMessage(serverPlayer, new ClearMessage());

            for (InfoSyncLootTableMessage message : messages) {
                sendSyncMessage(serverPlayer, message);
            }
        }
    }

    protected abstract void sendClearMessage(ServerPlayer serverPlayer, ClearMessage message);

    protected abstract void sendSyncMessage(ServerPlayer serverPlayer, InfoSyncLootTableMessage message);
}
