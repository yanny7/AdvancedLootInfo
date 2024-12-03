package com.yanny.emi_loot_addon.datagen;

import com.yanny.emi_loot_addon.EmiLootMod;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

class EmiLanguageProvider extends LanguageProvider {
    public EmiLanguageProvider(PackOutput output, String locale) {
        super(output, EmiLootMod.MOD_ID, locale);
    }

    @Override
    protected void addTranslations() {
        add("emi.category.emi_loot_addon.block_loot", "Block Drops");
        add("emi.category.emi_loot_addon.entity_loot", "Entity Drops");
        add("emi.category.emi_loot_addon.gameplay_loot", "Gameplay Drops");

        add("emi.description.emi_loot_addon.chance", "Chance: %s");
        add("emi.description.emi_loot_addon.chance_rolls", "Chance: [%s] %s");
        add("emi.description.emi_loot_addon.chance_bonus", "    -> %s (%s %s)");
        add("emi.description.emi_loot_addon.count", "Count: %s");
        add("emi.description.emi_loot_addon.count_bonus", "    -> %s (%s %s)");

        add("emi.type.emi_loot_addon.all_of", "All must pass [%s]");
        add("emi.type.emi_loot_addon.any_of", "None must pass [%s]");
        add("emi.type.emi_loot_addon.inverted", "Inverted %s");
        add("emi.type.emi_loot_addon.survives_explosion", "Must survive explosion");

        add("emi.type.emi_loot_addon.explosion_decay", "Explosion decreases amount");

        add("emi.util.emi_loot_addon.two_values", "%s%s");
        add("emi.util.emi_loot_addon.two_values_with_space", "%s %s");
        add("emi.util.emi_loot_addon.list.0", "[]");
        add("emi.util.emi_loot_addon.list.1", "[%s]");
        add("emi.util.emi_loot_addon.list.2", "[%s, %s]");
        add("emi.util.emi_loot_addon.list.3", "[%s, %s, %s]");
        add("emi.util.emi_loot_addon.list.4", "[%s, %s, %s, %s]");
        add("emi.util.emi_loot_addon.list.5", "[%s, %s, %s, %s, %s]");
        add("emi.util.emi_loot_addon.list.6", "[%s, %s, %s, %s, %s, %s]");
        add("emi.util.emi_loot_addon.list.7", "[%s, %s, %s, %s, %s, %s, %s]");
        add("emi.util.emi_loot_addon.list.8", "[%s, %s, %s, %s, %s, %s, %s, %s]");
        add("emi.util.emi_loot_addon.list.9", "[%s, %s, %s, %s, %s, %s, %s, %s, %s]");
    }
}
