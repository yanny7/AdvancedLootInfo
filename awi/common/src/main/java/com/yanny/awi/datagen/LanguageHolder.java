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
        CoreLang.register(Lang.BaseTerrain.class);
        CoreLang.register(Lang.Placement.class);
        CoreLang.register(Lang.PlacementModifier.class);
        CoreLang.register(Lang.TrunkPlacer.class);
        CoreLang.register(Lang.FeatureSize.class);
        CoreLang.register(Lang.BlockStateProvider.class);
        CoreLang.register(Lang.TreeDecorator.class);
        CoreLang.register(Lang.RootPlacer.class);
        CoreLang.register(Lang.FoliagePlacer.class);
        CoreLang.register(Lang.IntProvider.class);
        CoreLang.register(Lang.FloatProvider.class);
        CoreLang.register(Lang.HeightProvider.class);
        CoreLang.register(Lang.BlockPredicate.class);
        CoreLang.register(Lang.GenerationStep.class);
        CoreLang.register(Lang.StructureProcessor.class);
        CoreLang.register(Lang.Kind.class);

        TRANSLATION_MAP = new HashMap<>(CoreLang.TRANSLATION_MAP);
    }
}
