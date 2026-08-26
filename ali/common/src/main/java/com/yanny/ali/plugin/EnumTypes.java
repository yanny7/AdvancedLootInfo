package com.yanny.ali.plugin;

import com.yanny.aci.tooltip.CoreTooltipUtils;
import com.yanny.ali.Utils;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.CopyNameFunction;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

public class EnumTypes {
    public static final Map<Class<? extends Enum<?>>, String> TRANSLATED_ENUMS = new LinkedHashMap<>();

    static {
        TRANSLATED_ENUMS.put(EquipmentSlot.class, "equipment_slot");
        TRANSLATED_ENUMS.put(DyeColor.class, "dye_color");
        TRANSLATED_ENUMS.put(GameType.class, "game_type");
        TRANSLATED_ENUMS.put(MapDecoration.Type.class, "map_decoration_type");
        TRANSLATED_ENUMS.put(CopyNbtFunction.MergeStrategy.class, "merge_strategy");
        TRANSLATED_ENUMS.put(AttributeModifier.Operation.class, "attribute_operation");
        TRANSLATED_ENUMS.put(LootContext.EntityTarget.class, "entity_target");
        TRANSLATED_ENUMS.put(CopyNameFunction.NameSource.class, "name_source");
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
