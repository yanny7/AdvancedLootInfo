package com.yanny.alicompat.neoforge.datagen;

import com.yanny.alicompat.ModCompatManager;
import com.yanny.alicompat.Utils;
import net.minecraft.data.PackOutput;

class LanguageProvider extends net.neoforged.neoforge.common.data.LanguageProvider {
    public LanguageProvider(PackOutput output, String locale) {
        super(output, Utils.MOD_ID, locale);
    }

    @Override
    protected void addTranslations() {
        ModCompatManager.collectTranslations(false).forEach(this::add);
    }
}
