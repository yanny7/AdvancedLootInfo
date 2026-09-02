package com.yanny.alicompat.accessor;

import com.mojang.serialization.MapCodec;
import com.yanny.aci.CommonLogUtils;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.alicompat.Utils;
import net.minecraft.advancements.criterion.EntitySubPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.slf4j.Logger;

public class PluginUtils {
    private static final Logger LOGGER = CommonLogUtils.getLogger(Utils.MOD_ID);

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

    public static <T extends BaseAccessor<?> & IDataComponentPredicateTooltip> void registerDataComponentPredicateTooltip(IServerRegistry registry, Class<T> clazz) {
        ClassAccessor classAnnotation = clazz.getAnnotation(ClassAccessor.class);

        if (classAnnotation != null) {
            try {
                //noinspection unchecked
                Class<DataComponentPredicate> predicateClass = (Class<DataComponentPredicate>) Class.forName(classAnnotation.value());
                registry.registerDataComponentPredicateTooltip(predicateClass, (u, c) -> ReflectionUtils.copyClassData(clazz, c).getTooltip(u));
            } catch (Throwable e) {
                LOGGER.warn("Failed to register data component predicate tooltip for {} with error {}", classAnnotation.value(), e.getMessage(), e);
            }
        } else {
            throw new IllegalStateException("Missing ClassAccessor annotation for data component predicate tooltip " + clazz.getName());
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

    public static <T extends BaseAccessor<?> & IConsumeEffectTooltip> void registerConsumeEffectTooltip(IServerRegistry registry, Class<T> clazz) {
        ClassAccessor classAnnotation = clazz.getAnnotation(ClassAccessor.class);

        if (classAnnotation != null) {
            try {
                //noinspection unchecked
                Class<ConsumeEffect> ingredientClass = (Class<ConsumeEffect>) Class.forName(classAnnotation.value());
                registry.registerConsumeEffectTooltip(ingredientClass, (u, c) -> ReflectionUtils.copyClassData(clazz, c).getTooltip(u));
            } catch (Throwable e) {
                LOGGER.warn("Failed to register consume effect tooltip for {} with error {}", classAnnotation.value(), e.getMessage(), e);
            }
        } else {
            throw new IllegalStateException("Missing ClassAccessor annotation for consume effect tooltip " + clazz.getName());
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
}
