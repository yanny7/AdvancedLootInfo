package com.yanny.ali.plugin;

import com.mojang.logging.LogUtils;
import com.yanny.ali.api.IKeyTooltipNode;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.mixin.MixinLootModifier;
import com.yanny.ali.mixin.MixinLootTableIdCondition;
import com.yanny.ali.plugin.common.nodes.GlobalLootModifierNode;
import com.yanny.ali.plugin.common.tooltip.ArrayTooltipNode;
import com.yanny.ali.plugin.mods.BaseAccessor;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.ReflectionUtils;
import com.yanny.ali.plugin.server.GenericTooltipUtils;
import com.yanny.ali.plugin.server.TooltipUtils;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.common.loot.LootTableIdCondition;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class GlobalLootModifierUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static Optional<ILootModifier<?>> getLootModifier(List<LootItemCondition> conditions, Function<List<LootItemCondition>, List<IOperation>> operationSupplier) {
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
        } else if (GlobalLootModifierUtils.tablePredicate(conditions)) {
            return Optional.of(new ILootModifier<ResourceLocation>() {
                @Override
                public boolean predicate(ResourceLocation value) {
                    return GlobalLootModifierUtils.tablePredicate(conditions, value);
                }

                @Override
                public List<IOperation> getOperations() {
                    return operationSupplier.apply(conditions.stream().filter((c) -> !tablePredicate(c)).toList());
                }

                @Override
                public IType<ResourceLocation> getType() {
                    return IType.LOOT_TABLE;
                }
            });
        }

        return Optional.empty();
    }

    public static boolean entityPredicate(LootItemCondition c) {
        if (c instanceof LootItemEntityPropertyCondition condition && condition.entityTarget == LootContext.EntityTarget.THIS) {
            return true;
        } else {
            return c instanceof AnyOfCondition condition && entityPredicate(Arrays.asList(condition.terms));
        }
    }

    public static boolean entityPredicate(List<LootItemCondition> conditions) {
        return conditions.stream().anyMatch(GlobalLootModifierUtils::entityPredicate);
    }

    public static boolean entityPredicate(List<LootItemCondition> conditions, Entity entity) {
        return conditions.stream().anyMatch((c) -> {
            if (c instanceof LootItemEntityPropertyCondition condition
                    && condition.entityTarget == LootContext.EntityTarget.THIS
                    && condition.predicate != EntityPredicate.ANY
                    && condition.predicate.entityType != EntityTypePredicate.ANY
                    && condition.predicate.entityType.matches(entity.getType())) {
                return true;
            } else {
                return c instanceof AnyOfCondition condition && entityPredicate(Arrays.asList(condition.terms), entity);
            }
        });
    }

    public static boolean blockPredicate(LootItemCondition c) {
        if (c instanceof LootItemBlockStatePropertyCondition) {
            return true;
        } else {
            return c instanceof AnyOfCondition condition && blockPredicate(Arrays.asList(condition.terms));
        }
    }

    public static boolean blockPredicate(List<LootItemCondition> conditions) {
        return conditions.stream().anyMatch(GlobalLootModifierUtils::blockPredicate);
    }

    public static boolean blockPredicate(List<LootItemCondition> conditions, Block block) {
        return conditions.stream().anyMatch((c) -> {
            if (c instanceof LootItemBlockStatePropertyCondition condition && condition.block.equals(block)) {
                return true;
            } else {
                return c instanceof AnyOfCondition condition && blockPredicate(Arrays.asList(condition.terms), block);
            }
        });
    }

    public static boolean tablePredicate(LootItemCondition c) {
        if (c instanceof LootTableIdCondition) {
            return true;
        } else {
            return c instanceof AnyOfCondition condition && tablePredicate(Arrays.asList(condition.terms));
        }
    }

    public static boolean tablePredicate(List<LootItemCondition> conditions) {
        return conditions.stream().anyMatch(GlobalLootModifierUtils::tablePredicate);
    }

    public static boolean tablePredicate(List<LootItemCondition> conditions, ResourceLocation location) {
        return  conditions.stream().anyMatch((c) -> {
            if (c instanceof LootTableIdCondition condition && ((MixinLootTableIdCondition) condition).getTargetLootTableId().equals(location)) {
                return true;
            } else {
                return c instanceof AnyOfCondition condition && tablePredicate(Arrays.asList(condition.terms), location);
            }
        });
    }

    public static <T extends BaseAccessor<?> & IForgeLootModifier> void registerGlobalLootModifier(IForgePlugin.IRegistry registry, Class<T> clazz) {
        ClassAccessor classAnnotation = clazz.getAnnotation(ClassAccessor.class);

        if (classAnnotation != null) {
            try {
                //noinspection unchecked
                Class<IGlobalLootModifier> functionClass = (Class<IGlobalLootModifier>) Class.forName(classAnnotation.value());
                registry.registerGlobalLootModifier(functionClass, (u, c) -> ReflectionUtils.copyClassData(clazz, c).getLootModifier(u));
            } catch (Throwable e) {
                LOGGER.warn("Failed to register GLM for {} with error {}", classAnnotation.value(), e.getMessage());
                e.printStackTrace();
            }
        } else {
            throw new IllegalStateException("Missing ClassAccessor annotation for GLM " + clazz.getName());
        }
    }

    public static Optional<ILootModifier<?>> getMissingGlobalLootModifier(IServerUtils utils, IGlobalLootModifier modifier) {
        if (modifier instanceof LootModifier lootModifier) {
            return getLootModifier(Arrays.asList(((MixinLootModifier) lootModifier).getConditions()), (conditions) -> {
                ArrayTooltipNode.Builder tooltip = ArrayTooltipNode.array();
                IKeyTooltipNode fieldsTooltip = utils.getValueTooltip(utils, getName(modifier));

                TooltipUtils.addObjectFields(utils, fieldsTooltip, lootModifier, LootModifier.class);
                tooltip.add(fieldsTooltip.build("ali.util.advanced_loot_info.auto_detected"));
                tooltip.add(GenericTooltipUtils.getConditionsTooltip(utils, conditions));
                return List.of(new IOperation.AddOperation((i) -> true, new GlobalLootModifierNode(utils, tooltip.build())));
            });
        }

        return Optional.empty();
    }

    public static ResourceLocation getName(IGlobalLootModifier modifier) {
        return ForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS.get().getKey(modifier.codec());
    }
}
