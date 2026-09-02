package com.yanny.alicompat.accessor;

import com.yanny.aci.CommonLogUtils;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.ali.plugin.glm.ILootTableIdConditionPredicate;
import com.yanny.alicompat.Utils;
import org.slf4j.Logger;

public class GlmAccessorUtils {
    private static final Logger LOGGER = CommonLogUtils.getLogger(Utils.MOD_ID);

    public static <M, T extends BaseAccessor<?> & IGlobalLootModifierAccessor> void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry, Class<M> targetClass, Class<T> clazz, ILootTableIdConditionPredicate predicate) {
        try {
            registry.registerGlobalLootModifier(targetClass, (u, c) -> ReflectionUtils.copyClassData(clazz, c, targetClass).getLootModifier(u, predicate));
        } catch (Throwable e) {
            LOGGER.warn("Failed to register GLM for {} with error {}", targetClass.getName(), e.getMessage(), e);
        }
    }

    public static <T extends BaseAccessor<?> & IGlobalLootModifierAccessor> void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry, Class<T> clazz, ILootTableIdConditionPredicate predicate) {
        ClassAccessor classAnnotation = clazz.getAnnotation(ClassAccessor.class);

        if (classAnnotation != null) {
            try {
                Class<?> functionClass = Class.forName(classAnnotation.value());
                registry.registerGlobalLootModifier(functionClass, (u, c) -> ReflectionUtils.copyClassData(clazz, c).getLootModifier(u, predicate));
            } catch (Throwable e) {
                LOGGER.warn("Failed to register GLM for {} with error {}", classAnnotation.value(), e.getMessage(), e);
            }
        } else {
            throw new IllegalStateException("Missing ClassAccessor annotation for GLM " + clazz.getName());
        }
    }
}
