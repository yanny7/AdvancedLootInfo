package com.yanny.ali.plugin.common;

import com.yanny.aci.CommonLogUtils;
import com.yanny.ali.Utils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ReflectionUtils {
    private static final Logger LOGGER = CommonLogUtils.getLogger(Utils.MOD_ID);

    @NotNull
    public static <T> List<T> getCapturedInstances(Object predicate, Class<T> requiredType) {
        List<T> instances = new ArrayList<>();

        try {
            Field[] fields = predicate.getClass().getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);
                Object entry = field.get(predicate);

                if (requiredType.isInstance(entry)) {
                    instances.add(requiredType.cast(entry));
                }
            }
        } catch (IllegalAccessException e) {
            LOGGER.warn("Error while accessing field: {}", e.getMessage(), e);
        } catch (SecurityException e) {
            LOGGER.warn("Security error while accessing field: {}", e.getMessage(), e);
        }

        return instances;
    }
}
