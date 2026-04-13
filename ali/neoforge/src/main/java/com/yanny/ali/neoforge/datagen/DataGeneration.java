package com.yanny.ali.neoforge.datagen;

import com.yanny.ali.datagen.FakeLootProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

public class DataGeneration {
    public static void generate(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeClient(), new LanguageProvider(packOutput, "en_us"));
        generator.addProvider(event.includeServer(), new FakeLootProvider(packOutput, lookupProvider));
    }
}