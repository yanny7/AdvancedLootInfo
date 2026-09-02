package com.yanny.alicompat.fabric.datagen;

import com.yanny.alicompat.ModCompatManager;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

class LanguageProvider extends FabricLanguageProvider {
    public LanguageProvider(FabricPackOutput dataGenerator, CompletableFuture<HolderLookup.Provider> registryLookup, String locale) {
        super(dataGenerator, locale, registryLookup);
    }

    @Override
    public void generateTranslations(HolderLookup.Provider provider, TranslationBuilder builder) {
        ModCompatManager.collectTranslations(false).forEach(builder::add);
    }
}
