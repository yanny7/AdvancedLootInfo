package com.yanny.awi.datagen;

import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.language.CoreLang;
import com.yanny.awi.Utils;
import com.yanny.awi.language.Lang;
import com.yanny.awi.plugin.EnumTypes;
import com.yanny.awi.plugin.server.summary.Kind;
import net.minecraft.core.Direction;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LanguageHolder {
    private static final Logger LOGGER = CommonLogUtils.getLogger(Utils.MOD_ID);

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

        put(Direction.DOWN, "Down");
        put(Direction.UP, "Up");
        put(Direction.NORTH, "North");
        put(Direction.SOUTH, "South");
        put(Direction.WEST, "West");
        put(Direction.EAST, "East");

        put(Heightmap.Types.WORLD_SURFACE_WG, "Highest Block, Plants Included");
        put(Heightmap.Types.WORLD_SURFACE, "Highest Block, Plants Included");
        put(Heightmap.Types.OCEAN_FLOOR_WG, "Solid Ground, Ignores Water");
        put(Heightmap.Types.OCEAN_FLOOR, "Solid Ground, Ignores Water");
        put(Heightmap.Types.MOTION_BLOCKING, "Ground or Water Surface");
        put(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, "Ground or Water Surface, Below Leaves");

        put(CaveSurface.CEILING, "Ceiling");
        put(CaveSurface.FLOOR, "Floor");

        put(GenerationStep.Carving.AIR, "Air");
        put(GenerationStep.Carving.LIQUID, "Liquid");

        put(GenerationStep.Decoration.RAW_GENERATION, "Raw Generation");
        put(GenerationStep.Decoration.LAKES, "Lakes");
        put(GenerationStep.Decoration.LOCAL_MODIFICATIONS, "Local Modifications");
        put(GenerationStep.Decoration.UNDERGROUND_STRUCTURES, "Underground Structures");
        put(GenerationStep.Decoration.SURFACE_STRUCTURES, "Surface Structures");
        put(GenerationStep.Decoration.STRONGHOLDS, "Strongholds");
        put(GenerationStep.Decoration.UNDERGROUND_ORES, "Underground Ores");
        put(GenerationStep.Decoration.UNDERGROUND_DECORATION, "Underground Decoration");
        put(GenerationStep.Decoration.FLUID_SPRINGS, "Fluid Springs");
        put(GenerationStep.Decoration.VEGETAL_DECORATION, "Vegetation Decoration");
        put(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, "Top Layer Modifications");

        put(Kind.CONSTANT, "Constant");
        put(Kind.UNIFORM, "Uniform");
        put(Kind.BIASED_TO_BOTTOM, "Biased To Bottom");
        put(Kind.VERY_BIASED_TO_BOTTOM, "Very Biased To Bottom");
        put(Kind.TRAPEZOID, "Trapezoid");
        put(Kind.CLAMPED, "Clamped");
        put(Kind.CLAMPED_NORMAL, "Clamped Normal");
        put(Kind.WEIGHTED, "Weighted");
        put(Kind.RELATIVE_TO_HEIGHTMAP, "Relative To Heightmap");
        put(Kind.UNKNOWN, "Unknown");

        verifyEnumTranslations();
    }

    private static void put(Enum<?> value, String english) {
        TRANSLATION_MAP.put(EnumTypes.key(value), english);
    }

    private static void verifyEnumTranslations() {
        List<String> missing = new ArrayList<>();

        EnumTypes.TRANSLATED_ENUMS.forEach((type, owner) -> {
            for (Enum<?> value : type.getEnumConstants()) {
                if (!TRANSLATION_MAP.containsKey(EnumTypes.key(value))) {
                    missing.add(type.getSimpleName() + "." + value.name());
                }
            }
        });

        if (!missing.isEmpty()) {
            LOGGER.warn("Missing enum translations: {}", String.join(", ", missing));
        }
    }
}
