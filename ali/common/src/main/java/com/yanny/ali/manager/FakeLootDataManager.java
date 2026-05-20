package com.yanny.ali.manager;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.yanny.ali.platform.Services;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class FakeLootDataManager extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String FOLDER = "fake_loot";
    private static final Gson GSON = new Gson();

    private final Map<ResourceLocation, LootTable> fakeTables = new HashMap<>();

    public FakeLootDataManager() {
        super(GSON, FOLDER);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        Map<ResourceLocation, LootTable> map = new HashMap<>();
        HolderLookup.Provider provider = Services.getPlatform().getLookupProvider();
        DynamicOps<JsonElement> dynamicOps = provider != null ? RegistryOps.create(JsonOps.INSTANCE, provider) : JsonOps.INSTANCE;

        this.fakeTables.clear();

        object.forEach((location, json) -> {
            try {
                LootDataType.TABLE.codec().parse(dynamicOps, json)
                        .resultOrPartial((e) -> LOGGER.warn("Failed to parse fake loot table {}: {}", location, e))
                        .ifPresent((lootTable) -> map.put(location, lootTable));
            } catch (Throwable e) {
                LOGGER.warn("Failed to parse fake loot table {}", location, e);
            }
        });

        this.fakeTables.putAll(map);
        LOGGER.info("Loaded {} fake loot tables", this.fakeTables.size());
    }

    public Map<ResourceLocation, LootTable> getLootTables() {
        return fakeTables;
    }

    public void clearLootTables() {
        fakeTables.clear();
    }
}
