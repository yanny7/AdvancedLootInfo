package com.yanny.ali.plugin.mods.sawmill;

import com.mojang.logging.LogUtils;
import com.yanny.ali.plugin.mods.ReflectionUtils;
import net.minecraft.world.entity.npc.villager.VillagerType;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Utils {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final Map<VillagerType, List<WoodType>> WOOD_TYPES = new HashMap<>();

    static {
        try {
            Class<?> tradesClass = Class.forName("net.mehvahdjukaar.sawmill.CarpenterTrades");
            Field typeMapField = tradesClass.getDeclaredField("TYPE_MAP");

            typeMapField.setAccessible(true);
            //noinspection unchecked
            WOOD_TYPES.putAll(((Supplier<Map<VillagerType, List<Object>>>) typeMapField.get(null))
                    .get()
                    .entrySet()
                    .stream()
                    .map((e) -> Map.entry(e.getKey(), e.getValue().stream().map((w) -> ReflectionUtils.copyClassData(WoodType.class, w)).toList()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        } catch (Throwable e) {
            e.printStackTrace();
            LOGGER.warn("Unable to obtain wood type mapping: {}", e.getMessage());
        }
    }
}
