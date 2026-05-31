package com.yanny.awi.network;

import com.mojang.logging.LogUtils;
import com.yanny.awi.manager.AwiServerRegistry;
import com.yanny.awi.manager.PluginManager;
import com.yanny.awi.plugin.common.nodes.LevelStemNode;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.dimension.LevelStem;
import org.slf4j.Logger;

public abstract class AbstractServer {
    private static final int MAX_CHUNK_SIZE = 32 * 1024; // 32 KB
    private static final Logger LOGGER = LogUtils.getLogger();

    public void readWorldgenInfo(ServerLevel level) {
        AwiServerRegistry serverRegistry = PluginManager.getInstance().serverRegistry;
        RegistryAccess registryAccess = level.registryAccess();
        Registry<LevelStem> levelStemRegistry = registryAccess.registryOrThrow(Registries.LEVEL_STEM);

        for (LevelStem levelStem : levelStemRegistry) {
            new LevelStemNode(serverRegistry, levelStem);
            LOGGER.info("Level: {}", levelStemRegistry.getKey(levelStem));
        }
    }
}
