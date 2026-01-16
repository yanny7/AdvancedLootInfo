package com.yanny.ali.plugin.glm;

import com.mojang.logging.LogUtils;
import com.yanny.ali.api.IKeyTooltipNode;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.tooltip.ArrayTooltipNode;
import com.yanny.ali.plugin.mods.BaseAccessor;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.ReflectionUtils;
import com.yanny.ali.plugin.server.GenericTooltipUtils;
import com.yanny.ali.plugin.server.GlobalLootModifierNode;
import com.yanny.ali.plugin.server.TooltipUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class GlobalLootModifierUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static Optional<ILootModifier<?>> getLootModifier(List<LootItemCondition> conditions, Function<List<LootItemCondition>, List<IOperation>> operationSupplier, ILootTableIdConditionPredicate predicate) {
        if (GlobalLootModifierUtils.entityPredicate(conditions)) {
            return Optional.of(new ILootModifier<Entity>() {
                @Override
                public boolean predicate(Entity value) {
                    return GlobalLootModifierUtils.entityPredicate(conditions, value);
                }

                @Override
                public List<IOperation> getOperations() {
                    return operationSupplier.apply(conditions.stream().filter((c) -> !entityPredicate(c)).toList());
                }

                @Override
                public IType<Entity> getType() {
                    return IType.ENTITY;
                }
            });
        } else if (GlobalLootModifierUtils.blockPredicate(conditions)) {
            return Optional.of(new ILootModifier<Block>() {
                @Override
                public boolean predicate(Block value) {
                    return GlobalLootModifierUtils.blockPredicate(conditions, value);
                }

                @Override
                public List<IOperation> getOperations() {
                    return operationSupplier.apply(conditions.stream().filter((c) -> !blockPredicate(c)).toList());
                }

                @Override
                public IType<Block> getType() {
                    return IType.BLOCK;
                }
            });
        } else if (GlobalLootModifierUtils.tablePredicate(conditions, predicate)) {
            return Optional.of(new ILootModifier<Identifier>() {
                @Override
                public boolean predicate(Identifier value) {
                    return GlobalLootModifierUtils.tablePredicate(conditions, value, predicate);
                }

                @Override
                public List<IOperation> getOperations() {
                    return operationSupplier.apply(conditions.stream().filter((c) -> !tablePredicate(c, predicate)).toList());
                }

                @Override
                public IType<Identifier> getType() {
                    return IType.LOOT_TABLE;
                }
            });
        }

        return Optional.empty();
    }

    public static boolean entityPredicate(LootItemCondition c) {
        if (c instanceof LootItemEntityPropertyCondition condition
                && condition.entityTarget() == LootContext.EntityTarget.THIS
                && condition.predicate().isPresent()
                && condition.predicate().get().entityType().isPresent()) {
            return true;
        } else {
            return c instanceof AnyOfCondition condition && entityPredicate(condition.terms);
        }
    }

    public static boolean entityPredicate(List<LootItemCondition> conditions) {
        return conditions.stream().anyMatch(GlobalLootModifierUtils::entityPredicate);
    }

    public static boolean entityPredicate(List<LootItemCondition> conditions, Entity entity) {
        return conditions.stream().anyMatch((c) -> {
            if (c instanceof LootItemEntityPropertyCondition condition
                    && condition.entityTarget() == LootContext.EntityTarget.THIS
                    && condition.predicate().isPresent()
                    && condition.predicate().get().entityType().isPresent()
                    && condition.predicate().get().entityType().get().matches(entity.getType())) {
                return true;
            } else {
                return c instanceof AnyOfCondition condition && entityPredicate(condition.terms, entity);
            }
        });
    }

    public static boolean blockPredicate(LootItemCondition c) {
        if (c instanceof LootItemBlockStatePropertyCondition) {
            return true;
        } else {
            return c instanceof AnyOfCondition condition && blockPredicate(condition.terms);
        }
    }

    public static boolean blockPredicate(List<LootItemCondition> conditions) {
        return conditions.stream().anyMatch(GlobalLootModifierUtils::blockPredicate);
    }

    public static boolean blockPredicate(List<LootItemCondition> conditions, Block block) {
        return conditions.stream().anyMatch((c) -> {
            if (c instanceof LootItemBlockStatePropertyCondition condition && condition.block().equals(block)) {
                return true;
            } else {
                return c instanceof AnyOfCondition condition && blockPredicate(condition.terms, block);
            }
        });
    }

    public static boolean tablePredicate(LootItemCondition c, ILootTableIdConditionPredicate predicate) {
        if (predicate.isLootTableIdCondition(c)) {
            return true;
        } else {
            return c instanceof AnyOfCondition condition && tablePredicate(condition.terms, predicate);
        }
    }

    public static boolean tablePredicate(List<LootItemCondition> conditions, ILootTableIdConditionPredicate predicate) {
        return conditions.stream().anyMatch((c) -> tablePredicate(c, predicate));
    }

    public static boolean tablePredicate(List<LootItemCondition> conditions, Identifier location, ILootTableIdConditionPredicate predicate) {
        return  conditions.stream().anyMatch((c) -> {
            if (predicate.isLootTableIdCondition(c) && predicate.getTargetLootTableId(c).equals(location)) {
                return true;
            } else {
                return c instanceof AnyOfCondition condition && tablePredicate(condition.terms, location, predicate);
            }
        });
    }

    public static <T extends BaseAccessor<?> & IGlobalLootModifierAccessor> void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry, Class<T> clazz, ILootTableIdConditionPredicate predicate) {
        ClassAccessor classAnnotation = clazz.getAnnotation(ClassAccessor.class);

        if (classAnnotation != null) {
            try {
                Class<?> functionClass = Class.forName(classAnnotation.value());
                registry.registerGlobalLootModifier(functionClass, (u, c) -> ReflectionUtils.copyClassData(clazz, c).getLootModifier(u, predicate));
            } catch (Throwable e) {
                LOGGER.warn("Failed to register GLM for {} with error {}", classAnnotation.value(), e.getMessage());
                e.printStackTrace();
            }
        } else {
            throw new IllegalStateException("Missing ClassAccessor annotation for GLM " + clazz.getName());
        }
    }

    public static Optional<ILootModifier<?>> getMissingGlobalLootModifier(IServerUtils utils, IGlobalLootModifierWrapper modifier, ILootTableIdConditionPredicate predicate) {
        if (modifier.isLootModifier()) {
            return getLootModifier(modifier.getConditions(), (conditions) -> {
                ArrayTooltipNode.Builder tooltip = ArrayTooltipNode.array();
                IKeyTooltipNode fieldsTooltip = utils.getValueTooltip(utils, modifier.getName());

                TooltipUtils.addObjectFields(utils, fieldsTooltip, modifier, modifier.getLootModifierClass());
                tooltip.add(fieldsTooltip.build("ali.util.advanced_loot_info.auto_detected"));
                tooltip.add(GenericTooltipUtils.getConditionsTooltip(utils, conditions));
                return List.of(new IOperation.AddOperation((i) -> true, new GlobalLootModifierNode(utils, tooltip.build())));
            }, predicate);
        }

        return Optional.empty();
    }
}
