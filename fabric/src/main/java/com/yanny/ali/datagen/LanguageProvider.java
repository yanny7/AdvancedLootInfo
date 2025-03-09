package com.yanny.ali.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

class LanguageProvider extends FabricLanguageProvider {
    public LanguageProvider(FabricDataOutput dataGenerator, CompletableFuture<HolderLookup.Provider> registryLookup, String locale) {
        super(dataGenerator, locale, registryLookup);
    }

    @Override
    public void generateTranslations(HolderLookup.Provider provider, TranslationBuilder builder) {
        LanguageHolder.TRANSLATION_MAP.forEach(builder::add);
    }
}
