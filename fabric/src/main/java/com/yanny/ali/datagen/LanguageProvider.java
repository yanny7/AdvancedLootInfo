package com.yanny.ali.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;

class LanguageProvider extends FabricLanguageProvider {
    public LanguageProvider(FabricDataOutput dataGenerator, String locale) {
        super(dataGenerator, locale);
    }

    @Override
    public void generateTranslations(TranslationBuilder builder) {
        LanguageHolder.TRANSLATION_MAP.forEach(builder::add);
    }
}
