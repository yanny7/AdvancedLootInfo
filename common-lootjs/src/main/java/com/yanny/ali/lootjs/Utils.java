package com.yanny.ali.lootjs;

import com.almostreliable.lootjs.core.LootType;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.List;
import java.util.function.Predicate;

public class Utils {
    private static final Logger LOGGER = LogUtils.getLogger();

    @NotNull
    public static <T> List<T> getCapturedInstances(Predicate<?> predicate, Class<T> requiredType) {
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

    @NotNull
    public static Predicate<ResourceLocation> typePredicate(LootType type) {
        return switch (type) {
            case UNKNOWN, VAULT, ADVANCEMENT_REWARD, ADVANCEMENT_ENTITY, ADVANCEMENT_LOCATION,
                 COMMAND, SELECTOR -> (r) -> false;
            case BLOCK, BLOCK_USE -> (r) -> r.getPath().startsWith("blocks/");
            case CHEST -> (r) -> r.getPath().startsWith("chests/");
            case FISHING -> (r) -> r.getPath().startsWith("gameplay/fishing");
            case ENTITY -> (r) -> r.getPath().startsWith("entities/");
            case EQUIPMENT -> (r) -> r.getPath().startsWith("equipment/");
            case ARCHAEOLOGY -> (r) -> r.getPath().startsWith("archaeology/");
            case GIFT -> (r) -> r.getPath().endsWith("_gift");
            case PIGLIN_BARTER -> (r) -> r.getPath().endsWith("_bartering");
            case SHEARING -> (r) -> r.getPath().startsWith("shearing/");
            case GENERIC -> (r) -> true;
        };
    }
}
