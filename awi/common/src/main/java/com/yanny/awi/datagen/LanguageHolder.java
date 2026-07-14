package com.yanny.awi.datagen;

import com.yanny.aci.language.CoreLang;
import com.yanny.awi.language.Lang;

import java.util.HashMap;
import java.util.Map;

public class LanguageHolder {
    public static final Map<String, String> TRANSLATION_MAP;

    static {
        CoreLang.register(Lang.FeatureConfiguration.class);
        CoreLang.register(Lang.RuleTest.class);
        CoreLang.register(Lang.Value.class);
        CoreLang.register(Lang.Branch.class);
        CoreLang.register(Lang.GenerationStep.class);

        TRANSLATION_MAP = new HashMap<>(CoreLang.TRANSLATION_MAP);
    }
}
