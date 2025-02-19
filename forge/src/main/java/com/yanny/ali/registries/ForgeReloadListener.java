package com.yanny.ali.registries;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraftforge.event.AddReloadListenerEvent;

public class ForgeReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static void onResourceReload(AddReloadListenerEvent event) {
        event.addListener(LootCategories.getReloadListener(GSON, "loot_categories"));
    }
}
