package com.yanny.ali.plugin.glm;

import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.language.CoreLang;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.Utils;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.nodes.GlobalLootModifierNode;
import com.yanny.ali.plugin.server.TooltipUtils;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.predicates.entity.EntityTypePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.CompositeLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class GlobalLootModifierUtils {
    private static final Logger LOGGER = CommonLogUtils.getLogger(Utils.MOD_ID);

    public static Optional<ILootModifier<?>> getLootModifier(IServerUtils utils, List<LootItemCondition> conditions,
                                                            Function<List<LootItemCondition>, List<IOperation>> operationSupplier) {
        List<Destination.Entities> entities = collect(utils, conditions, Destination.Entities.class);

        if (!entities.isEmpty()) {
            return Optional.of(new ILootModifier<Entity>() {
                @Override
                public boolean predicate(Entity value) {
                    return entities.stream().anyMatch((d) -> d.type().matches(value.typeHolder()));
                }

                @NotNull
                @Override
                public List<IOperation> getOperations() {
                    return operationSupplier.apply(retain(utils, conditions, Destination.Entities.class));
                }

                @NotNull
                @Override
                public IType<Entity> getType() {
                    return IType.ENTITY;
                }
            });
        }

        List<Destination.Blocks> blocks = collect(utils, conditions, Destination.Blocks.class);

        if (!blocks.isEmpty()) {
            return Optional.of(new ILootModifier<Block>() {
                @Override
                public boolean predicate(Block value) {
                    return blocks.stream().anyMatch((d) -> d.blocks().contains(value));
                }

                @NotNull
                @Override
                public List<IOperation> getOperations() {
                    return operationSupplier.apply(retain(utils, conditions, Destination.Blocks.class));
                }

                @NotNull
                @Override
                public IType<Block> getType() {
                    return IType.BLOCK;
                }
            });
        }

        List<Destination.Table> tables = collect(utils, conditions, Destination.Table.class);

        if (!tables.isEmpty()) {
            return Optional.of(new ILootModifier<Identifier>() {
                @Override
                public boolean predicate(Identifier value) {
                    return tables.stream().anyMatch((d) -> d.id().equals(value));
                }

                @NotNull
                @Override
                public List<IOperation> getOperations() {
                    return operationSupplier.apply(retain(utils, conditions, Destination.Table.class));
                }

                @NotNull
                @Override
                public IType<Identifier> getType() {
                    return IType.LOOT_TABLE;
                }
            });
        }

        if (utils.getConfiguration().showUnboundedGlobalLootModifiers) {
            return Optional.of(new ILootModifier<>() {
                @Override
                public boolean predicate(Object value) {
                    return true;
                }

                @NotNull
                @Override
                public List<IOperation> getOperations() {
                    return operationSupplier.apply(conditions);
                }

                @NotNull
                @Override
                public IType<Object> getType() {
                    return IType.UNBOUNDED;
                }
            });
        }

        return Optional.empty();
    }

    @NotNull
    public static Destination getBlockStateDestination(IServerUtils ignoredUtils, LootItemBlockStatePropertyCondition condition) {
        return new Destination.Blocks(List.of(condition.block().value()), condition.properties().map((p) -> p.properties().isEmpty()).orElse(true));
    }

    @Nullable
    public static Destination getEntityPropertyDestination(IServerUtils ignoredUtils, LootItemEntityPropertyCondition condition) {
        if (condition.entityTarget() != LootContext.EntityTarget.THIS || condition.predicate().isEmpty() || condition.predicate().get().entityType().isEmpty()) {
            return null;
        }

        EntityPredicate predicate = condition.predicate().get();

        return new Destination.Entities(predicate.entityType().get(), carriesOnlyEntityType(predicate));
    }

    public static Optional<ILootModifier<?>> getMissingGlobalLootModifier(IServerUtils utils, IGlobalLootModifierWrapper modifier) {
        if (modifier.isLootModifier()) {
            return getLootModifier(utils, modifier.getConditions(), (conditions) -> {

                try {
                    TooltipBuilder tooltip = utils.getValueTooltip(utils, modifier.getName());

                    tooltip.add(TooltipUtils.getJsonTooltip(utils, modifier.serialize()));
                    return List.of(new IOperation.AddOperation((i) -> true, new GlobalLootModifierNode(tooltip.build(CoreLang.Utils.AUTO_DETECTED))));
                } catch (Throwable e) {
                    if (utils.getConfiguration().logMoreStatistics) {
                        LOGGER.warn("Failed to get GLM info from serialized data for {}", modifier.getName(), e);
                    }

                    TooltipBuilder tooltip = TooltipBuilder.array((b) -> {
                        TooltipBuilder fieldsTooltip = utils.getValueTooltip(utils, modifier.getName());

                        TooltipUtils.addObjectFields(utils, fieldsTooltip, modifier, modifier.getLootModifierClass());
                        b.add(fieldsTooltip.build(CoreLang.Utils.AUTO_DETECTED));
                        b.add(utils.getValueTooltip(utils, conditions));
                    });
                    return List.of(new IOperation.AddOperation((i) -> true, new GlobalLootModifierNode(tooltip.build())));
                }
            });
        }

        return Optional.empty();
    }

    @NotNull
    private static <T extends Destination> List<T> collect(IServerUtils utils, List<LootItemCondition> conditions, Class<T> kind) {
        List<T> destinations = new ArrayList<>();

        conditions.forEach((c) -> collect(utils, c, kind, destinations));
        return destinations;
    }

    private static <T extends Destination> void collect(IServerUtils utils, LootItemCondition condition, Class<T> kind, List<T> destinations) {
        Destination destination = utils.getDestination(utils, condition);

        if (destination != null) {
            if (kind.isInstance(destination)) {
                destinations.add(kind.cast(destination));
            }
        } else if (condition instanceof CompositeLootItemCondition composite) {
            for (LootItemCondition term : composite.terms) {
                collect(utils, term, kind, destinations);
            }
        }
    }

    @NotNull
    private static List<LootItemCondition> retain(IServerUtils utils, List<LootItemCondition> conditions, Class<? extends Destination> kind) {
        return conditions.stream().filter((c) -> !fullyExplained(utils, c, kind)).toList();
    }

    private static boolean fullyExplained(IServerUtils utils, LootItemCondition condition, Class<? extends Destination> kind) {
        Destination destination = utils.getDestination(utils, condition);

        if (destination != null) {
            return kind.isInstance(destination) && destination.fullyExplained();
        }

        return condition instanceof CompositeLootItemCondition composite
                && !composite.terms.isEmpty()
                && composite.terms.stream().allMatch((t) -> fullyExplained(utils, t, kind));
    }

    private static boolean carriesOnlyEntityType(EntityPredicate predicate) {
        return predicate.distanceToPlayer().isEmpty()
                && predicate.movement().isEmpty()
                && predicate.location().located().isEmpty()
                && predicate.location().steppingOn().isEmpty()
                && predicate.location().affectsMovement().isEmpty()
                && predicate.effects().isEmpty()
                && predicate.nbt().isEmpty()
                && predicate.flags().isEmpty()
                && predicate.equipment().isEmpty()
                && predicate.subPredicate().isEmpty()
                && predicate.periodicTick().isEmpty()
                && predicate.vehicle().isEmpty()
                && predicate.passenger().isEmpty()
                && predicate.targetedEntity().isEmpty()
                && predicate.team().isEmpty()
                && predicate.slots().isEmpty();
    }
}
