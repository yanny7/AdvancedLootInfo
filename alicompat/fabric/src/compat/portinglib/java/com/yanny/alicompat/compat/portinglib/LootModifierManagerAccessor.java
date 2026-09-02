package com.yanny.alicompat.compat.portinglib;

import com.yanny.aci.CommonLogUtils;
import com.yanny.alicompat.Utils;
import io.github.fabricators_of_create.porting_lib.loot.IGlobalLootModifier;
import io.github.fabricators_of_create.porting_lib.loot.LootModifierManager;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

public class LootModifierManagerAccessor {
    private static final Logger LOGGER = CommonLogUtils.getLogger(Utils.MOD_ID);
    private static final Field REGISTERED_GLM;

    static {
        Field internalField = null;

        try {
            internalField = LootModifierManager.class.getDeclaredField("registeredLootModifiers");
            internalField.setAccessible(true);
        } catch (Throwable e) {
            LOGGER.warn("Unable to obtain LootModifierManager: {}", e.getMessage(), e);
        }

        REGISTERED_GLM = internalField;
    }

    public static Map<Identifier, IGlobalLootModifier> getAllLootMods() {
        try {
            //noinspection unchecked
            return (Map<Identifier, IGlobalLootModifier>) REGISTERED_GLM.get(LootModifierManager.INSTANCE);
        } catch (Throwable e) {
            LOGGER.warn("Unable to obtain global loot modifiers: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }
}
