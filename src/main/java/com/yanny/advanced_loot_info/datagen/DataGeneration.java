package com.yanny.advanced_loot_info.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.data.event.GatherDataEvent;

public class DataGeneration {
    public static void generate(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();

        generator.addProvider(event.includeClient(), new LanguageProvider(packOutput, "en_us"));
        generator.addProvider(event.includeClient(), new LootCategoryProvider(packOutput));
    }
}