package com.yanny.awi.plugin;

import com.yanny.aci.tooltip.CoreTooltipUtils;
import com.yanny.awi.Utils;
import com.yanny.awi.plugin.server.summary.Kind;
import net.minecraft.core.Direction;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

public class EnumTypes {
    public static final Map<Class<? extends Enum<?>>, String> TRANSLATED_ENUMS = new LinkedHashMap<>();

    static {
        TRANSLATED_ENUMS.put(Direction.class, "direction");
        TRANSLATED_ENUMS.put(Heightmap.Types.class, "heightmap_type");
        TRANSLATED_ENUMS.put(CaveSurface.class, "cave_surface");
        TRANSLATED_ENUMS.put(GenerationStep.Carving.class, "carving_step");
        TRANSLATED_ENUMS.put(GenerationStep.Decoration.class, "decoration_step");
        TRANSLATED_ENUMS.put(Kind.class, "kind");
    }

    @NotNull
    public static String key(Enum<?> value) {
        String owner = TRANSLATED_ENUMS.get(value.getDeclaringClass());

        if (owner == null) {
            throw new IllegalStateException("Enum " + value.getDeclaringClass().getTypeName() + " is missing from EnumTypes.TRANSLATED_ENUMS");
        }

        return CoreTooltipUtils.enumKey(Utils.MOD_ID, owner, value.name());
    }
}
