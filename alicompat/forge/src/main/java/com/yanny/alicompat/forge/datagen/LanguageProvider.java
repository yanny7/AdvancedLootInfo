package com.yanny.alicompat.forge.datagen;

import com.yanny.alicompat.ModCompatManager;
import com.yanny.alicompat.Utils;
import net.minecraft.data.DataGenerator;

class LanguageProvider extends net.minecraftforge.common.data.LanguageProvider {
    public LanguageProvider(DataGenerator output, String locale) {
        super(output, Utils.MOD_ID, locale);
    }

    @Override
    protected void addTranslations() {
        ModCompatManager.collectTranslations(false).forEach(this::add);
    }
}
