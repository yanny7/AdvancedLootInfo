package com.yanny.ali.fabric.plugin.mods.porting_lib.loot;


import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

public class LootModifierManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Object INSTANCE;
    private static final Field REGISTERED_GLM;

    static {
        Object internalInstance = null;
        Field internalField = null;

        try {
            Class<?> managerClass = Class.forName("io.github.fabricators_of_create.porting_lib.loot.LootModifierManager");
            Field instanceField = managerClass.getDeclaredField("INSTANCE");

            instanceField.setAccessible(true);
            internalInstance = instanceField.get(null);
            internalField = managerClass.getDeclaredField("registeredLootModifiers");
            internalField.setAccessible(true);
        } catch (Throwable e) {
            LOGGER.warn("Unable to obtain LootModifierManager: {}", e.getMessage(), e);
        }

        INSTANCE = internalInstance;
        REGISTERED_GLM = internalField;
    }

    public static Map<Identifier, Object> getAllLootMods() {
        try {
            //noinspection unchecked
            return (Map<Identifier, Object>) REGISTERED_GLM.get(INSTANCE);
        } catch (IllegalAccessException e) {
            LOGGER.warn("Unable to obtain global loot modifiers: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }
}
