package com.yanny.ali.registries;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.regex.Pattern;

public class TradeLootCategory extends LootCategory<String> {
    private final List<Pattern> patterns;

    public TradeLootCategory(ResourceLocation key, ItemStack icon, boolean hide, List<Pattern> patterns) {
        super(key, icon, Type.TRADE, hide);
        this.patterns = patterns;
    }

    @Override
    protected void toJson(JsonObject object) {
        JsonArray array = new JsonArray();

        patterns.forEach((p) -> array.add(p.pattern()));
        object.add("pattern", array);
    }

    @Override
    public boolean validate(String path) {
        return patterns.stream().anyMatch((p) -> p.matcher(path).find());
    }
}
