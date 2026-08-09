package com.yanny.ali;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.jetbrains.annotations.NotNull;

public class Utils {
    public static final String MOD_ID = "ali";
    public static final String COMMON_CONFIG_NAME = "ali_common.json";

    @NotNull
    public static ResourceLocation modLoc(String path) {
        return  new ResourceLocation(MOD_ID, path);
    }

    /**
     * Key under which a block's loot node is stored. Blocks declared with {@code noLootTable()} all report
     * {@link BuiltInLootTables#EMPTY}, which is a shared sentinel and not a registered table - keying them by
     * themselves keeps a GLM-only node attached to the one block it belongs to, instead of collapsing every such
     * block into a single entry. Server and client must derive this identically.
     */
    @NotNull
    public static ResourceLocation getLootTableKey(Block block) {
        ResourceLocation location = block.getLootTable();

        return location.equals(BuiltInLootTables.EMPTY) ? BuiltInRegistries.BLOCK.getKey(block) : location;
    }
}
