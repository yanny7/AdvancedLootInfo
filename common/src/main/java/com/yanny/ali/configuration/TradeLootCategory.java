package com.yanny.ali.configuration;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.regex.Pattern;

public class TradeLootCategory extends LootCategory<ResourceLocation> {
    private final List<Pattern> patterns;

    public TradeLootCategory(ResourceLocation key, Item icon, boolean hide, List<Pattern> patterns) {
        super(key, icon, Type.TRADE, hide);
        this.patterns = patterns;
    }

    public TradeLootCategory(JsonObject object) {
        super(Type.TRADE, object);
        patterns = GsonHelper.getAsJsonArray(object, "pattern").asList().stream().map(JsonElement::getAsString).map(Pattern::compile).toList();
    }

    @Override
    protected void toJson(JsonObject object) {
        JsonArray array = new JsonArray();

        patterns.forEach((p) -> array.add(p.pattern()));
        object.add("pattern", array);
    }

    @Override
    public boolean validate(ResourceLocation path) {
        return patterns.stream().anyMatch((p) -> p.matcher(path.toString()).find());
    }
}
