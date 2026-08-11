package com.yanny.ali;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public class Utils {
    public static final String MOD_ID = "ali";
    public static final String COMMON_CONFIG_NAME = "ali_common.json";

    @NotNull
    public static Identifier modLoc(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    /**
     * Key under which a block's loot node is stored. Blocks declared with {@code noLootTable()} report no loot table at
     * all - keying them by themselves keeps a GLM-only node attached to the one block it belongs to, instead of
     * collapsing every such block into a single entry. Server and client must derive this identically.
     */
    @NotNull
    public static Identifier getLootTableKey(Block block) {
        return block.getLootTable().map(ResourceKey::identifier).orElseGet(() -> BuiltInRegistries.BLOCK.getKey(block));
    }
}
