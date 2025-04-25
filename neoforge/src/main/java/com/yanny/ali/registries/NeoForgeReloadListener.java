package com.yanny.ali.registries;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

public class NeoForgeReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static void onResourceReload(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(LootCategories.getReloadListener(GSON, "loot_categories"));
    }
}
