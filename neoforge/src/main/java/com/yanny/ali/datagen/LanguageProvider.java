package com.yanny.ali.datagen;

import com.yanny.ali.Utils;
import net.minecraft.data.PackOutput;

class LanguageProvider extends net.neoforged.neoforge.common.data.LanguageProvider {
    public LanguageProvider(PackOutput output, String locale) {
        super(output, Utils.MOD_ID, locale);
    }

    @Override
    protected void addTranslations() {
        LanguageHolder.TRANSLATION_MAP.forEach(this::add);
    }
}
