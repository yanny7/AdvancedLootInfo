package com.yanny.ali.fabric.datagen;

import com.yanny.ali.datagen.FakeLootProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class DataGeneration implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        generator.addProvider((FabricDataGenerator g) -> new LanguageProvider(g, "en_us"));
        generator.addProvider(FakeLootProvider::new);
    }
}
