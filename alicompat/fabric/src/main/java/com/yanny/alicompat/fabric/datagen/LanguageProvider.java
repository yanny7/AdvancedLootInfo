package com.yanny.alicompat.fabric.datagen;

import com.yanny.alicompat.ModCompatManager;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;

class LanguageProvider extends FabricLanguageProvider {
    public LanguageProvider(FabricDataOutput dataGenerator, String locale) {
        super(dataGenerator, locale);
    }

    @Override
    public void generateTranslations(TranslationBuilder builder) {
        ModCompatManager.collectTranslations(false).forEach(builder::add);
    }
}
