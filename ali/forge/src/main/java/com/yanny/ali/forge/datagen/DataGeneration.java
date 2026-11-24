package com.yanny.ali.forge.datagen;

import com.yanny.ali.datagen.FakeLootProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.data.event.GatherDataEvent;

public class DataGeneration {
    public static void generate(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();

        generator.addProvider(event.includeClient(), new LanguageProvider(generator, "en_us"));
        generator.addProvider(event.includeServer(), new FakeLootProvider(generator));
    }
}