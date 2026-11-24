package com.yanny.ali.fabric.datagen;

import com.yanny.ali.datagen.LanguageHolder;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;

class LanguageProvider extends FabricLanguageProvider {
    public LanguageProvider(FabricDataGenerator dataGenerator, String locale) {
        super(dataGenerator, locale);
    }

    @Override
    public void generateTranslations(TranslationBuilder builder) {
        LanguageHolder.TRANSLATION_MAP.forEach(builder::add);
    }
}
