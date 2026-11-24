package com.yanny.ali.forge.datagen;

import com.yanny.ali.Utils;
import com.yanny.ali.datagen.LanguageHolder;
import net.minecraft.data.DataGenerator;

class LanguageProvider extends net.minecraftforge.common.data.LanguageProvider {
    public LanguageProvider(DataGenerator output, String locale) {
        super(output, Utils.MOD_ID, locale);
    }

    @Override
    protected void addTranslations() {
        LanguageHolder.TRANSLATION_MAP.forEach(this::add);
    }
}
