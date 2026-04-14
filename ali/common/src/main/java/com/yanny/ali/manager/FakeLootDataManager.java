package com.yanny.ali.manager;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class FakeLootDataManager extends SimpleJsonResourceReloadListener<LootTable> {
    private static final String FOLDER = "fake_loot";

    private final Map<Identifier, LootTable> fakeTables = new HashMap<>();

    public FakeLootDataManager(HolderLookup.Provider provider) {
        super(provider, LootDataType.TABLE.codec(), ResourceKey.createRegistryKey(Identifier.withDefaultNamespace(FOLDER)));
    }

    @Override
    protected void apply(Map<Identifier, LootTable> object, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        this.fakeTables.putAll(object);
    }

    public Map<Identifier, LootTable> getLootTables() {
        return fakeTables;
    }

    @NotNull
    @Override
    public String getName() {
        return "fake_loot_manager";
    }
}
