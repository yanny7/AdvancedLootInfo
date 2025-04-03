package com.yanny.ali.network;

import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;
import org.slf4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public abstract class AbstractServer {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final List<InfoSyncLootTableMessage> messages = new LinkedList<>();

    public final void readLootTables(LootDataManager manager, ServerLevel level) {
        messages.clear();
        manager.getKeys(LootDataType.TABLE).forEach((location) -> {
            LootTable table = manager.getLootTable(location);

            if (table != LootTable.EMPTY) {
//                List<Item> items = TooltipUtils.collectItems(PluginManager.CLIENT_REGISTRY, table);
//
//                if (!items.isEmpty()) {
//                        LootModifierManager man = ForgeInternalHandler.getLootModifierManager();
//                        ObjectArrayList<ItemStack> generatedLoot = new ObjectArrayList<>(items.stream().map(Item::getDefaultInstance).toList());
//
//                        for (IGlobalLootModifier mod : man.getAllLootMods()) {
//                            generatedLoot = mod.apply(generatedLoot, context);
//                        }

                    messages.add(new InfoSyncLootTableMessage(location, table));
//                } else {
//                    LOGGER.info("LootTable {} has no items", location);
//                }
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
                sendSyncMessage(serverPlayer, message);
            }
        }
    }

    protected abstract void sendClearMessage(ServerPlayer serverPlayer, ClearMessage message);

    protected abstract void sendSyncMessage(ServerPlayer serverPlayer, InfoSyncLootTableMessage message);
}
