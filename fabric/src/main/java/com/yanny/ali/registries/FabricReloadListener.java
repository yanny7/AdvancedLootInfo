package com.yanny.ali.registries;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.yanny.ali.Utils;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class FabricReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    @NotNull
    public static IdentifiableResourceReloadListener onResourceReload() {
        SimpleJsonResourceReloadListener<JsonElement> listener = LootCategories.getReloadListener(GSON, "loot_categories");
        return new IdentifiableResourceReloadListener() {

            @Override
            public ResourceLocation getFabricId() {
                return Utils.modLoc("loot_categories");
            }

            @NotNull
            @Override
            public CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, Executor executor, Executor executor2) {
                return listener.reload(preparationBarrier, resourceManager, executor, executor2);
            }
        };
    }
}
