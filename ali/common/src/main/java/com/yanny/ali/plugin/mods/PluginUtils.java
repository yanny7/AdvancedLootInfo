package com.yanny.ali.plugin.mods;

import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.StreamSupport;

public class PluginUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static <T extends BaseAccessor<?> & IEntryItemCollector> void registerEntryItemCollector(IServerRegistry registry, Class<T> clazz) {
        ClassAccessor classAnnotation = clazz.getAnnotation(ClassAccessor.class);

        if (classAnnotation != null) {
            try {
                //noinspection unchecked
                Class<LootPoolEntryContainer> itemCollectorClass = (Class<LootPoolEntryContainer>) Class.forName(classAnnotation.value());
                registry.registerItemCollector(itemCollectorClass, (u, c) -> ReflectionUtils.copyClassData(clazz, c).collectItems(u));
            } catch (Throwable e) {
                LOGGER.warn("Failed to register entry item collector for {} with error {}", classAnnotation.value(), e.getMessage(), e);
            }
        } else {
            throw new IllegalStateException("Missing ClassAccessor annotation for entry item collector " + clazz.getName());
        }
    }

    public static <T extends BaseAccessor<?> & IFunctionItemCollector> void registerFunctionItemCollector(IServerRegistry registry, Class<T> clazz) {
        ClassAccessor classAnnotation = clazz.getAnnotation(ClassAccessor.class);

        if (classAnnotation != null) {
            try {
                //noinspection unchecked
                Class<LootItemFunction> itemCollectorClass = (Class<LootItemFunction>) Class.forName(classAnnotation.value());
                registry.registerItemCollector(itemCollectorClass, (u, i, c) -> ReflectionUtils.copyClassData(clazz, c).collectItems(u, i));
            } catch (Throwable e) {
                LOGGER.warn("Failed to register function item collector for {} with error {}", classAnnotation.value(), e.getMessage(), e);
            }
        } else {
            throw new IllegalStateException("Missing ClassAccessor annotation for function item collector " + clazz.getName());
        }
    }

    public static <T extends BaseAccessor<?> & IEntry> void registerEntry(IServerRegistry registry, Class<T> clazz) {
        ClassAccessor classAnnotation = clazz.getAnnotation(ClassAccessor.class);

        if (classAnnotation != null) {
            try {
                //noinspection unchecked
                Class<LootPoolEntryContainer> entryClass = (Class<LootPoolEntryContainer>) Class.forName(classAnnotation.value());
                registry.registerEntry(entryClass, (u, e, r, w, f, c) -> ReflectionUtils.copyClassData(clazz, e).create(u, r, w, f, c));
            } catch (Throwable e) {
                LOGGER.warn("Failed to register entry for {} with error {}", classAnnotation.value(), e.getMessage(), e);
            }
        } else {
            throw new IllegalStateException("Missing ClassAccessor annotation for entry " + clazz.getName());
        }
    }

    public static <T extends BaseAccessor<?> & IFunctionTooltip> void registerFunctionTooltip(IServerRegistry registry, Class<T> clazz) {
        ClassAccessor classAnnotation = clazz.getAnnotation(ClassAccessor.class);

        if (classAnnotation != null) {
            try {
                //noinspection unchecked
                Class<LootItemFunction> functionClass = (Class<LootItemFunction>) Class.forName(classAnnotation.value());
                registry.registerFunctionTooltip(functionClass, (u, c) -> ReflectionUtils.copyClassData(clazz, c).getTooltip(u));
            } catch (Throwable e) {
                LOGGER.warn("Failed to register function tooltip for {} with error {}", classAnnotation.value(), e.getMessage(), e);
            }
        } else {
            throw new IllegalStateException("Missing ClassAccessor annotation for function tooltip " + clazz.getName());
        }
    }

    public static <T extends BaseAccessor<?> & IConditionTooltip> void registerConditionTooltip(IServerRegistry registry, Class<T> clazz) {
        ClassAccessor classAnnotation = clazz.getAnnotation(ClassAccessor.class);

        if (classAnnotation != null) {
            try {
                //noinspection unchecked
                Class<LootItemCondition> conditionClass = (Class<LootItemCondition>) Class.forName(classAnnotation.value());
                registry.registerConditionTooltip(conditionClass, (u, c) -> ReflectionUtils.copyClassData(clazz, c).getTooltip(u));
            } catch (Throwable e) {
                LOGGER.warn("Failed to register condition tooltip for {} with error {}", classAnnotation.value(), e.getMessage(), e);
            }
        } else {
            throw new IllegalStateException("Missing ClassAccessor annotation for condition tooltip " + clazz.getName());
        }
    }

    public static <T extends BaseAccessor<?> & IIngredientTooltip> void registerIngredientTooltip(IServerRegistry registry, Class<T> clazz) {
        ClassAccessor classAnnotation = clazz.getAnnotation(ClassAccessor.class);

        if (classAnnotation != null) {
            try {
                //noinspection unchecked
                Class<Ingredient> ingredientClass = (Class<Ingredient>) Class.forName(classAnnotation.value());
                registry.registerIngredientTooltip(ingredientClass, (u, c) -> ReflectionUtils.copyClassData(clazz, c).getTooltip(u));
            } catch (Throwable e) {
                LOGGER.warn("Failed to register ingredient tooltip for {} with error {}", classAnnotation.value(), e.getMessage(), e);
            }
        } else {
            throw new IllegalStateException("Missing ClassAccessor annotation for ingredient tooltip " + clazz.getName());
        }
    }

    public static <T extends BaseAccessor<?> & IItemSubPredicateTooltip> void registerItemSubPredicateTooltip(IServerRegistry registry, Class<T> clazz) {
        ClassAccessor classAnnotation = clazz.getAnnotation(ClassAccessor.class);

        if (classAnnotation != null) {
            try {
                //noinspection unchecked
                Class<ItemSubPredicate> predicateClass = (Class<ItemSubPredicate>) Class.forName(classAnnotation.value());
                registry.registerItemSubPredicateTooltip(predicateClass, (u, c) -> ReflectionUtils.copyClassData(clazz, c).getTooltip(u));
            } catch (Throwable e) {
                LOGGER.warn("Failed to register item sub predicate tooltip for {} with error {}", classAnnotation.value(), e.getMessage(), e);
            }
        } else {
            throw new IllegalStateException("Missing ClassAccessor annotation for item sub predicate " + clazz.getName());
        }
    }

    public static <T extends BaseAccessor<?> & IEntitySubPredicateTooltip> void registerEntitySubPredicateTooltip(IServerRegistry registry, Class<T> clazz, MapCodec<T> codec) {
        ClassAccessor classAnnotation = clazz.getAnnotation(ClassAccessor.class);

        if (classAnnotation != null) {
            try {
                //noinspection unchecked
                registry.registerEntitySubPredicateTooltip((MapCodec<? extends EntitySubPredicate>) codec, (u, c) -> ReflectionUtils.copyClassData(clazz, c).getTooltip(u));
            } catch (Throwable e) {
                LOGGER.warn("Failed to register entity sub predicate tooltip for {} with error {}", classAnnotation.value(), e.getMessage(), e);
            }
        } else {
            throw new IllegalStateException("Missing ClassAccessor annotation for entity sub predicate " + clazz.getName());
        }
    }

    public static <T extends BaseAccessor<?> & IDataComponentTypeTooltip> void registerDataComponentTypeTooltip(IServerRegistry registry, Class<T> clazz, DataComponentType<T> type) {
        ClassAccessor classAnnotation = clazz.getAnnotation(ClassAccessor.class);

        if (classAnnotation != null) {
            try {
                registry.registerDataComponentTypeTooltip(type, (u, c) -> ReflectionUtils.copyClassData(clazz, c).getTooltip(u));
            } catch (Throwable e) {
                LOGGER.warn("Failed to register data component type tooltip for {} with error {}", classAnnotation.value(), e.getMessage(), e);
            }
        } else {
            throw new IllegalStateException("Missing ClassAccessor annotation for data component type tooltip " + clazz.getName());
        }
    }

    public static <T extends BaseAccessor<?> & INumberProvider> void registerNumberProvider(IServerRegistry registry, Class<T> clazz) {
        ClassAccessor classAnnotation = clazz.getAnnotation(ClassAccessor.class);

        if (classAnnotation != null) {
            try {
                //noinspection unchecked
                Class<NumberProvider> numberProviderClass = (Class<NumberProvider>) Class.forName(classAnnotation.value());
                registry.registerNumberProvider(numberProviderClass, (u, c) -> ReflectionUtils.copyClassData(clazz, c).convertNumber(u));
            } catch (Throwable e) {
                LOGGER.warn("Failed to register number provider for {} with error {}", classAnnotation.value(), e.getMessage(), e);
            }
        } else {
            throw new IllegalStateException("Missing ClassAccessor annotation for number provider " + clazz.getName());
        }
    }

    public static <T extends BaseAccessor<?> & ICountModifier> void registerCountModifier(IServerRegistry registry, Class<T> clazz) {
        ClassAccessor classAnnotation = clazz.getAnnotation(ClassAccessor.class);

        if (classAnnotation != null) {
            try {
                //noinspection unchecked
                Class<LootItemFunction> functionClass = (Class<LootItemFunction>) Class.forName(classAnnotation.value());
                registry.registerCountModifier(functionClass, (u, c, m) -> ReflectionUtils.copyClassData(clazz, c).applyCountModifier(u, m));
            } catch (Throwable e) {
                LOGGER.warn("Failed to register count modifier for {} with error {}", classAnnotation.value(), e.getMessage(), e);
            }
        } else {
            throw new IllegalStateException("Missing ClassAccessor annotation for count modifier " + clazz.getName());
        }
    }

    public static <T extends BaseAccessor<?> & IChanceModifier> void registerChanceModifier(IServerRegistry registry, Class<T> clazz) {
        ClassAccessor classAnnotation = clazz.getAnnotation(ClassAccessor.class);

        if (classAnnotation != null) {
            try {
                //noinspection unchecked
                Class<LootItemCondition> conditionClass = (Class<LootItemCondition>) Class.forName(classAnnotation.value());
                registry.registerChanceModifier(conditionClass, (u, c, m) -> ReflectionUtils.copyClassData(clazz, c).applyChanceModifier(u, m));
            } catch (Throwable e) {
                LOGGER.warn("Failed to register chance modifier for {} with error {}", classAnnotation.value(), e.getMessage(), e);
            }
        } else {
            throw new IllegalStateException("Missing ClassAccessor annotation for chance modifier " + clazz.getName());
        }
    }

    public static <T extends BaseAccessor<?> & IItemStackModifier> void registerItemStackModifier(IServerRegistry registry, Class<T> clazz) {
        ClassAccessor classAnnotation = clazz.getAnnotation(ClassAccessor.class);

        if (classAnnotation != null) {
            try {
                //noinspection unchecked
                Class<LootItemFunction> functionClass = (Class<LootItemFunction>) Class.forName(classAnnotation.value());
                registry.registerItemStackModifier(functionClass, (u, c, m) -> ReflectionUtils.copyClassData(clazz, c).applyItemStackModifier(u, m));
            } catch (Throwable e) {
                LOGGER.warn("Failed to register item stack modifier for {} with error {}", classAnnotation.value(), e.getMessage(), e);
            }
        } else {
            throw new IllegalStateException("Missing ClassAccessor annotation for item stack modifier " + clazz.getName());
        }
    }

    public static <T extends BaseAccessor<?> & IItemListing> void registerItemListing(IServerRegistry registry, Class<T> clazz) {
        ClassAccessor classAnnotation = clazz.getAnnotation(ClassAccessor.class);

        if (classAnnotation != null) {
            try {
                //noinspection unchecked
                Class<VillagerTrades.ItemListing> conditionClass = (Class<VillagerTrades.ItemListing>) Class.forName(classAnnotation.value());
                registry.registerItemListing(conditionClass, (u, c, t) -> ReflectionUtils.copyClassData(clazz, c).getNode(u, t));
            } catch (Throwable e) {
                LOGGER.warn("Failed to register item listing for {} with error {}", classAnnotation.value(), e.getMessage(), e);
            }
        } else {
            throw new IllegalStateException("Missing ClassAccessor annotation for item listing {}" + clazz.getName());
        }
    }

    public static <T extends BaseAccessor<?> & IItemListing> void registerItemListingCollector(IServerRegistry registry, Class<T> clazz) {
        ClassAccessor classAnnotation = clazz.getAnnotation(ClassAccessor.class);

        if (classAnnotation != null) {
            try {
                //noinspection unchecked
                Class<VillagerTrades.ItemListing> conditionClass = (Class<VillagerTrades.ItemListing>) Class.forName(classAnnotation.value());
                registry.registerItemListingCollector(conditionClass, (u, c) -> ReflectionUtils.copyClassData(clazz, c).collectItems(u));
            } catch (Throwable e) {
                LOGGER.warn("Failed to register item listing collector for {} with error {}", classAnnotation.value(), e.getMessage(), e);
            }
        } else {
            LOGGER.warn("Missing ClassAccessor annotation for item listing collector {}", clazz.getName());
        }
    }

    public static <T extends ItemLike> List<Item> getItems(IServerUtils utils, TagKey<T> tag) {
        ServerLevel level = utils.getServerLevel();

        if (level != null) {
            Registry<T> registry = level.registryAccess().registryOrThrow(tag.registry());

            return StreamSupport.stream(registry.getTagOrEmpty(tag).spliterator(), false).map(Holder::value).map(ItemLike::asItem).toList();
        }

        return Collections.emptyList();
    }

    public static <T extends ItemLike> List<Item> getItems(IServerUtils utils, Either<ItemStack, TagKey<T>> either) {
        ServerLevel level = utils.getServerLevel();

        if (level != null) {
            return either.map(
                    (i) -> List.of(i.getItem()),
                    (t) -> {
                        Registry<T> registry = level.registryAccess().registryOrThrow(t.registry());
                        return StreamSupport.stream(registry.getTagOrEmpty(t).spliterator(), false).map(Holder::value).map(ItemLike::asItem).toList();
                    }
            );
        }

        return Collections.emptyList();
    }

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
