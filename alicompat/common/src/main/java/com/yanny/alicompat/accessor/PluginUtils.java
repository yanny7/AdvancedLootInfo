package com.yanny.alicompat.accessor;

import com.mojang.serialization.MapCodec;
import com.yanny.aci.CommonLogUtils;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.alicompat.Utils;
import net.minecraft.advancements.criterion.EntitySubPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.slot.SlotSource;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.slf4j.Logger;

import java.util.function.Function;

public class PluginUtils {
    private static final Logger LOGGER = CommonLogUtils.getLogger(Utils.MOD_ID);

    public static <U extends LootPoolEntryContainer, T extends BaseAccessor<?> & IEntry> void registerEntry(IServerRegistry registry, Class<U> targetClass, Class<T> clazz) {
        registry.registerEntry(targetClass, (u, e, r, w, f, c) -> ReflectionUtils.copyClassData(clazz, e, targetClass).create(u, r, w, f, c));
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

    public static <U extends LootPoolEntryContainer, T extends IEntry> void registerEntry(IServerRegistry registry, Class<U> targetClass, Function<U, T> factory) {
        registry.registerEntry(targetClass, (u, e, r, w, f, c) -> factory.apply(e).create(u, r, w, f, c));
    }

    public static <U extends LootPoolEntryContainer, T extends BaseAccessor<?> & IEntryTooltip> void registerEntryTooltip(IServerRegistry registry, Class<U> targetClass, Class<T> clazz) {
        registry.registerEntryTooltip(targetClass, (u, c) -> ReflectionUtils.copyClassData(clazz, c, targetClass).getTooltip(u));
    }

    public static <T extends BaseAccessor<?> & IEntryTooltip> void registerEntryTooltip(IServerRegistry registry, Class<T> clazz) {
        ClassAccessor classAnnotation = clazz.getAnnotation(ClassAccessor.class);

        if (classAnnotation != null) {
            try {
                //noinspection unchecked
                Class<LootPoolEntryContainer> entryClass = (Class<LootPoolEntryContainer>) Class.forName(classAnnotation.value());
                registry.registerEntryTooltip(entryClass, (u, c) -> ReflectionUtils.copyClassData(clazz, c).getTooltip(u));
            } catch (Throwable e) {
                LOGGER.warn("Failed to register entry tooltip for {} with error {}", classAnnotation.value(), e.getMessage(), e);
            }
        } else {
            throw new IllegalStateException("Missing ClassAccessor annotation for entry tooltip " + clazz.getName());
        }
    }

    public static <U extends LootPoolEntryContainer, T extends IEntryTooltip> void registerEntryTooltip(IServerRegistry registry, Class<U> targetClass, Function<U, T> factory) {
        registry.registerEntryTooltip(targetClass, (u, c) -> factory.apply(c).getTooltip(u));
    }

    public static <U extends LootItemFunction, T extends BaseAccessor<?> & IFunctionTooltip> void registerFunctionTooltip(IServerRegistry registry, Class<U> targetClass, Class<T> clazz) {
        registry.registerFunctionTooltip(targetClass, (u, c) -> ReflectionUtils.copyClassData(clazz, c, targetClass).getTooltip(u));
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

    public static <U extends LootItemFunction, T extends IFunctionTooltip> void registerFunctionTooltip(IServerRegistry registry, Class<U> targetClass, Function<U, T> factory) {
        registry.registerFunctionTooltip(targetClass, (u, c) -> factory.apply(c).getTooltip(u));
    }

    public static <U extends LootItemCondition, T extends BaseAccessor<?> & IConditionTooltip> void registerConditionTooltip(IServerRegistry registry, Class<U> targetClass, Class<T> clazz) {
        registry.registerConditionTooltip(targetClass, (u, c) -> ReflectionUtils.copyClassData(clazz, c, targetClass).getTooltip(u));
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

    public static <U extends LootItemCondition, T extends IConditionTooltip> void registerConditionTooltip(IServerRegistry registry, Class<U> targetClass, Function<U, T> factory) {
        registry.registerConditionTooltip(targetClass, (u, c) -> factory.apply(c).getTooltip(u));
    }

    public static <U extends Ingredient, T extends BaseAccessor<?> & IIngredientTooltip> void registerIngredientTooltip(IServerRegistry registry, Class<U> targetClass, Class<T> clazz) {
        registry.registerIngredientTooltip(targetClass, (u, c) -> ReflectionUtils.copyClassData(clazz, c, targetClass).getTooltip(u));
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

    public static <U extends Ingredient, T extends IIngredientTooltip> void registerIngredientTooltip(IServerRegistry registry, Class<U> targetClass, Function<U, T> factory) {
        registry.registerIngredientTooltip(targetClass, (u, c) -> factory.apply(c).getTooltip(u));
    }

    public static <U extends DataComponentPredicate, T extends BaseAccessor<?> & IDataComponentPredicateTooltip> void registerDataComponentPredicateTooltip(IServerRegistry registry, Class<U> targetClass, Class<T> clazz) {
        registry.registerDataComponentPredicateTooltip(targetClass, (u, c) -> ReflectionUtils.copyClassData(clazz, c, targetClass).getTooltip(u));
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

    public static <U extends DataComponentPredicate, T extends IDataComponentPredicateTooltip> void registerDataComponentPredicateTooltip(IServerRegistry registry, Class<U> targetClass, Function<U, T> factory) {
        registry.registerDataComponentPredicateTooltip(targetClass, (u, c) -> factory.apply(c).getTooltip(u));
    }

    public static <U extends EntitySubPredicate, T extends BaseAccessor<?> & IEntitySubPredicateTooltip> void registerEntitySubPredicateTooltip(IServerRegistry registry, MapCodec<U> codec, Class<U> targetClass, Class<T> clazz) {
        registry.registerEntitySubPredicateTooltip(codec, (u, c) -> ReflectionUtils.copyClassData(clazz, c, targetClass).getTooltip(u));
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

    public static <U extends EntitySubPredicate, T extends IEntitySubPredicateTooltip> void registerEntitySubPredicateTooltip(IServerRegistry registry, MapCodec<U> codec, Function<U, T> factory) {
        registry.registerEntitySubPredicateTooltip(codec, (u, c) -> factory.apply(c).getTooltip(u));
    }

    public static <U, T extends BaseAccessor<?> & IDataComponentTypeTooltip> void registerDataComponentTypeTooltip(IServerRegistry registry, DataComponentType<U> type, Class<U> targetClass, Class<T> clazz) {
        registry.registerDataComponentTypeTooltip(type, (u, c) -> ReflectionUtils.copyClassData(clazz, c, targetClass).getTooltip(u));
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

    public static <U, T extends IDataComponentTypeTooltip> void registerDataComponentTypeTooltip(IServerRegistry registry, DataComponentType<U> type, Function<U, T> factory) {
        registry.registerDataComponentTypeTooltip(type, (u, c) -> factory.apply(c).getTooltip(u));
    }

    public static <U extends ConsumeEffect, T extends BaseAccessor<?> & IConsumeEffectTooltip> void registerConsumeEffectTooltip(IServerRegistry registry, Class<U> targetClass, Class<T> clazz) {
        registry.registerConsumeEffectTooltip(targetClass, (u, c) -> ReflectionUtils.copyClassData(clazz, c, targetClass).getTooltip(u));
    }

    public static <T extends BaseAccessor<?> & IConsumeEffectTooltip> void registerConsumeEffectTooltip(IServerRegistry registry, Class<T> clazz) {
        ClassAccessor classAnnotation = clazz.getAnnotation(ClassAccessor.class);

        if (classAnnotation != null) {
            try {
                //noinspection unchecked
                Class<ConsumeEffect> consumeEffectClass = (Class<ConsumeEffect>) Class.forName(classAnnotation.value());
                registry.registerConsumeEffectTooltip(consumeEffectClass, (u, c) -> ReflectionUtils.copyClassData(clazz, c).getTooltip(u));
            } catch (Throwable e) {
                LOGGER.warn("Failed to register consume effect tooltip for {} with error {}", classAnnotation.value(), e.getMessage(), e);
            }
        } else {
            throw new IllegalStateException("Missing ClassAccessor annotation for consume effect tooltip " + clazz.getName());
        }
    }

    public static <U extends ConsumeEffect, T extends IConsumeEffectTooltip> void registerConsumeEffectTooltip(IServerRegistry registry, Class<U> targetClass, Function<U, T> factory) {
        registry.registerConsumeEffectTooltip(targetClass, (u, c) -> factory.apply(c).getTooltip(u));
    }

    public static <U extends SlotSource, T extends BaseAccessor<?> & ISlotSourceTooltip> void registerSlotSourceTooltip(IServerRegistry registry, Class<U> targetClass, Class<T> clazz) {
        registry.registerSlotSourceTooltip(targetClass, (u, c) -> ReflectionUtils.copyClassData(clazz, c, targetClass).getTooltip(u));
    }

    public static <T extends BaseAccessor<?> & ISlotSourceTooltip> void registerSlotSourceTooltip(IServerRegistry registry, Class<T> clazz) {
        ClassAccessor classAnnotation = clazz.getAnnotation(ClassAccessor.class);

        if (classAnnotation != null) {
            try {
                //noinspection unchecked
                Class<SlotSource> slotSourceClass = (Class<SlotSource>) Class.forName(classAnnotation.value());
                registry.registerSlotSourceTooltip(slotSourceClass, (u, c) -> ReflectionUtils.copyClassData(clazz, c).getTooltip(u));
            } catch (Throwable e) {
                LOGGER.warn("Failed to register slot source tooltip for {} with error {}", classAnnotation.value(), e.getMessage(), e);
            }
        } else {
            throw new IllegalStateException("Missing ClassAccessor annotation for slot source tooltip " + clazz.getName());
        }
    }

    public static <U extends SlotSource, T extends ISlotSourceTooltip> void registerSlotSourceTooltip(IServerRegistry registry, Class<U> targetClass, Function<U, T> factory) {
        registry.registerSlotSourceTooltip(targetClass, (u, c) -> factory.apply(c).getTooltip(u));
    }

    public static <U extends NumberProvider, T extends BaseAccessor<?> & INumberProvider> void registerNumberProvider(IServerRegistry registry, Class<U> targetClass, Class<T> clazz) {
        registry.registerNumberProvider(targetClass, (u, c) -> ReflectionUtils.copyClassData(clazz, c, targetClass).convertNumber(u));
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

    public static <U extends NumberProvider, T extends INumberProvider> void registerNumberProvider(IServerRegistry registry, Class<U> targetClass, Function<U, T> factory) {
        registry.registerNumberProvider(targetClass, (u, c) -> factory.apply(c).convertNumber(u));
    }

    public static <U extends LootItemFunction, T extends BaseAccessor<?> & ICountModifier> void registerCountModifier(IServerRegistry registry, Class<U> targetClass, Class<T> clazz) {
        registry.registerCountModifier(targetClass, (u, c, m) -> ReflectionUtils.copyClassData(clazz, c, targetClass).applyCountModifier(u, m));
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

    public static <U extends LootItemFunction, T extends ICountModifier> void registerCountModifier(IServerRegistry registry, Class<U> targetClass, Function<U, T> factory) {
        registry.registerCountModifier(targetClass, (u, c, m) -> factory.apply(c).applyCountModifier(u, m));
    }

    public static <U extends LootItemCondition, T extends BaseAccessor<?> & IChanceModifier> void registerChanceModifier(IServerRegistry registry, Class<U> targetClass, Class<T> clazz) {
        registry.registerChanceModifier(targetClass, (u, c, m) -> ReflectionUtils.copyClassData(clazz, c, targetClass).applyChanceModifier(u, m));
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

    public static <U extends LootItemCondition, T extends IChanceModifier> void registerChanceModifier(IServerRegistry registry, Class<U> targetClass, Function<U, T> factory) {
        registry.registerChanceModifier(targetClass, (u, c, m) -> factory.apply(c).applyChanceModifier(u, m));
    }

    public static <U extends LootItemFunction, T extends BaseAccessor<?> & IItemStackModifier> void registerItemStackModifier(IServerRegistry registry, Class<U> targetClass, Class<T> clazz) {
        registry.registerItemStackModifier(targetClass, (u, c, m) -> ReflectionUtils.copyClassData(clazz, c, targetClass).applyItemStackModifier(u, m));
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

    public static <U extends LootItemFunction, T extends IItemStackModifier> void registerItemStackModifier(IServerRegistry registry, Class<U> targetClass, Function<U, T> factory) {
        registry.registerItemStackModifier(targetClass, (u, c, m) -> factory.apply(c).applyItemStackModifier(u, m));
    }

    public static <U extends LootItemCondition, T extends BaseAccessor<?> & IDestination> void registerDestination(IServerRegistry registry, Class<U> targetClass, Class<T> clazz) {
        registry.registerDestination(targetClass, (u, c) -> ReflectionUtils.copyClassData(clazz, c, targetClass).getDestination(u));
    }

    public static <T extends BaseAccessor<?> & IDestination> void registerDestination(IServerRegistry registry, Class<T> clazz) {
        ClassAccessor classAnnotation = clazz.getAnnotation(ClassAccessor.class);

        if (classAnnotation != null) {
            try {
                //noinspection unchecked
                Class<LootItemCondition> conditionClass = (Class<LootItemCondition>) Class.forName(classAnnotation.value());
                registry.registerDestination(conditionClass, (u, c) -> ReflectionUtils.copyClassData(clazz, c).getDestination(u));
            } catch (Throwable e) {
                LOGGER.warn("Failed to register destination for {} with error {}", classAnnotation.value(), e.getMessage(), e);
            }
        } else {
            throw new IllegalStateException("Missing ClassAccessor annotation for destination " + clazz.getName());
        }
    }

    public static <U extends LootItemCondition, T extends IDestination> void registerDestination(IServerRegistry registry, Class<U> targetClass, Function<U, T> factory) {
        registry.registerDestination(targetClass, (u, c) -> factory.apply(c).getDestination(u));
    }

    public static <U, T extends BaseAccessor<?> & IValueTooltip> void registerValueTooltip(IServerRegistry registry, Class<U> targetClass, Class<T> clazz) {
        registry.registerValueTooltip(targetClass, (u, c) -> ReflectionUtils.copyClassData(clazz, c, targetClass).getTooltip(u));
    }

    public static <T extends BaseAccessor<?> & IValueTooltip> void registerValueTooltip(IServerRegistry registry, Class<T> clazz) {
        ClassAccessor classAnnotation = clazz.getAnnotation(ClassAccessor.class);

        if (classAnnotation != null) {
            try {
                Class<?> valueClass = Class.forName(classAnnotation.value());
                registry.registerValueTooltip(valueClass, (u, c) -> ReflectionUtils.copyClassData(clazz, c).getTooltip(u));
            } catch (Throwable e) {
                LOGGER.warn("Failed to register value tooltip for {} with error {}", classAnnotation.value(), e.getMessage(), e);
            }
        } else {
            throw new IllegalStateException("Missing ClassAccessor annotation for value tooltip " + clazz.getName());
        }
    }

    public static <U, T extends IValueTooltip> void registerValueTooltip(IServerRegistry registry, Class<U> targetClass, Function<U, T> factory) {
        registry.registerValueTooltip(targetClass, (u, c) -> factory.apply(c).getTooltip(u));
    }
}
