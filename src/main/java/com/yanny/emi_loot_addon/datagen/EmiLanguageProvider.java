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

        add("emi.description.emi_loot_addon.unit", "%s%s");
        add("emi.description.emi_loot_addon.chance", "Chance: %s");
        add("emi.description.emi_loot_addon.chance_rolls", "Chance: [%s] %s");
        add("emi.description.emi_loot_addon.chance_bonus", "    -> %s (%s %s)");
        add("emi.description.emi_loot_addon.count", "Count: %s");
        add("emi.description.emi_loot_addon.count_bonus", "    -> %s (%s %s)");
    }
}
