package com.yanny.ali.registries;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class GameplayLootCategory extends LootCategory<String> {
    private final List<Pattern> patterns;

    public GameplayLootCategory(ResourceLocation key, ItemStack icon, List<Pattern> patterns) {
        super(key, icon, Type.GAMEPLAY);
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

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), patterns);
    }
}
