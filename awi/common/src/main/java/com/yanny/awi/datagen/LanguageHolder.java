package com.yanny.awi.datagen;

import com.yanny.aci.language.CoreLang;
import com.yanny.awi.language.Lang;

import java.util.HashMap;
import java.util.Map;

public class LanguageHolder {
    public static final Map<String, String> TRANSLATION_MAP = new HashMap<>();

    static {
        CoreLang.register(TRANSLATION_MAP, Lang.FeatureConfiguration.class);
        CoreLang.register(TRANSLATION_MAP, Lang.RuleTest.class);
        CoreLang.register(TRANSLATION_MAP, Lang.Value.class);
        CoreLang.register(TRANSLATION_MAP, Lang.Branch.class);
        CoreLang.register(TRANSLATION_MAP, Lang.BaseTerrain.class);
        CoreLang.register(TRANSLATION_MAP, Lang.Placement.class);
        CoreLang.register(TRANSLATION_MAP, Lang.PlacementModifier.class);
        CoreLang.register(TRANSLATION_MAP, Lang.TrunkPlacer.class);
        CoreLang.register(TRANSLATION_MAP, Lang.FeatureSize.class);
        CoreLang.register(TRANSLATION_MAP, Lang.BlockStateProvider.class);
        CoreLang.register(TRANSLATION_MAP, Lang.TreeDecorator.class);
        CoreLang.register(TRANSLATION_MAP, Lang.RootPlacer.class);
        CoreLang.register(TRANSLATION_MAP, Lang.FoliagePlacer.class);
        CoreLang.register(TRANSLATION_MAP, Lang.IntProvider.class);
        CoreLang.register(TRANSLATION_MAP, Lang.FloatProvider.class);
        CoreLang.register(TRANSLATION_MAP, Lang.HeightProvider.class);
        CoreLang.register(TRANSLATION_MAP, Lang.BlockPredicate.class);
        CoreLang.register(TRANSLATION_MAP, Lang.GenerationStep.class);
        CoreLang.register(TRANSLATION_MAP, Lang.StructureProcessor.class);
        CoreLang.register(TRANSLATION_MAP, Lang.Kind.class);
    }
}
