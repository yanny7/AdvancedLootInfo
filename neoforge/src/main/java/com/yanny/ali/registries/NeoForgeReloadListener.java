package com.yanny.ali.registries;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;

public class NeoForgeReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static void onResourceReload(AddClientReloadListenersEvent event) {
        event.addListener(ResourceLocation.fromNamespaceAndPath("ali", "loot_categories"), LootCategories.getReloadListener(GSON, "loot_categories"));
    }
}
