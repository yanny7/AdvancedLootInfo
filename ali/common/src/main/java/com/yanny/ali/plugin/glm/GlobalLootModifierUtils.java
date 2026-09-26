package com.yanny.ali.plugin.glm;

import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.language.CoreLang;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.nodes.GlobalLootModifierNode;
import com.yanny.ali.plugin.server.TooltipUtils;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.AllOfCondition;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.DamageSourceCondition;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class GlobalLootModifierUtils {
    private static final Logger LOGGER = CommonLogUtils.getLogger(Utils.MOD_ID);

    @NotNull
    public static IPageLootModifier getLootModifier(IServerUtils utils, @Nullable Object modifier, List<LootItemCondition> conditions,
                                                    BiFunction<LootPage, List<LootItemCondition>, List<IOperation>> operationSupplier) {
        List<Object> terms = Stream.concat(Stream.ofNullable(modifier), conditions.stream()).toList();
        int conditionOffset = terms.size() - conditions.size();
        // evaluation order is mutated by every test, so a modifier must not be tested from several threads at once
        int[] order = IntStream.range(0, terms.size()).toArray();

        return new IPageLootModifier() {
            @NotNull
            @Override
            public PageMatch test(LootPage page) {
                boolean[] explained = new boolean[conditions.size()];
                int explainedCount = 0;
                Match result = Match.UNKNOWN;

                for (int i = 0; i < order.length; i++) {
                    int index = order[i];
                    Verdict verdict = utils.testPage(utils, terms.get(index), page);

                    if (verdict.match() == Match.NO) {
                        System.arraycopy(order, 0, order, 1, i);
                        order[0] = index;
                        return PageMatch.NO;
                    } else if (verdict.match() == Match.YES) {
                        result = Match.YES;
                    }

                    if (index >= conditionOffset && verdict.explained()) {
                        explained[index - conditionOffset] = true;
                        explainedCount++;
                    }
                }

                return new PageMatch(result, getUnexplained(conditions, explained, explainedCount));
            }

            @NotNull
            @Override
            public List<IOperation> getOperations(LootPage page, PageMatch match) {
                return operationSupplier.apply(page, match.unexplained());
            }
        };
    }

    public static Optional<IPageLootModifier> getMissingGlobalLootModifier(IServerUtils utils, IGlobalLootModifierWrapper modifier) {
        if (modifier.isLootModifier()) {
            return Optional.of(getLootModifier(utils, modifier.getLootModifier(), modifier.getConditions(), (page, conditions) -> {

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

                        TooltipUtils.addObjectFields(utils, fieldsTooltip, modifier.getLootModifier(), modifier.getLootModifierClass());
                        b.add(fieldsTooltip.build(CoreLang.Utils.AUTO_DETECTED));
                        b.add(utils.getValueTooltip(utils, conditions));
                    });
                    return List.of(new IOperation.AddOperation((i) -> true, new GlobalLootModifierNode(tooltip.build())));
                }
            }));
        }

        return Optional.empty();
    }

    @NotNull
    public static Verdict testPage(IServerUtils utils, Object value, LootPage page, @Nullable IPageResolver<Object> resolver,
                                   List<ILootContextPreparer> preparers, HolderGetter.Provider lootData) {
        if (resolver != null) {
            Verdict verdict = resolver.test(utils, value, page);

            if (verdict != null) {
                return verdict;
            }
        }

        if (value instanceof AllOfCondition allOf) {
            return testAllOf(utils, allOf.terms, page);
        } else if (value instanceof AnyOfCondition anyOf) {
            return testAnyOf(utils, anyOf.terms, page);
        } else if (value instanceof InvertedLootItemCondition inverted) {
            return invert(utils.testPage(utils, inverted.term(), page));
        } else if (value instanceof LootItemCondition condition) {
            return LootContextProbe.probe(utils, condition, page, preparers, lootData);
        }

        return Verdict.UNKNOWN;
    }

    @NotNull
    public static ParamState getParamState(LootPage page, ContextKey<?> param) {
        ContextKeySet paramSet = page.paramSet();

        if (paramSet == LootContextParamSets.ALL_PARAMS) {
            return ParamState.GENERIC;
        } else if (!paramSet.allowed().contains(param)) {
            return ParamState.DISALLOWED;
        }

        return paramSet.required().contains(param) ? ParamState.REQUIRED : ParamState.OPTIONAL;
    }

    @NotNull
    public static Verdict testBlocks(LootPage page, Predicate<Block> matcher, boolean explained) {
        long matching = page.blocks().stream().filter(matcher).count();

        if (matching == 0) {
            return Verdict.NO;
        }

        return Verdict.yes(explained && matching == page.blocks().size());
    }

    @NotNull
    public static Verdict testEntityTypes(LootPage page, Predicate<EntityType<?>> matcher, boolean explained) {
        long matching = page.entityTypes().stream().filter(matcher).count();

        if (matching == 0) {
            return Verdict.NO;
        }

        return Verdict.yes(explained && matching == page.entityTypes().size());
    }

    @NotNull
    public static Verdict testTable(LootPage page, Predicate<Identifier> matcher, boolean explained) {
        return matcher.test(page.tableId()) ? Verdict.yes(explained) : Verdict.NO;
    }

    @Nullable
    public static Verdict testBlockStateProperty(IServerUtils ignoredUtils, LootItemBlockStatePropertyCondition condition, LootPage page) {
        if (page.blocks().isEmpty()) {
            return null;
        }

        return testBlocks(page, (b) -> condition.block().value().equals(b), condition.properties().map((p) -> p.properties().isEmpty()).orElse(true));
    }

    @NotNull
    public static Verdict testEntityProperty(IServerUtils utils, LootItemEntityPropertyCondition condition, LootPage page) {
        if (condition.predicate().isEmpty()) {
            return Verdict.yes(true);
        } else if (utils.getParamState(page, condition.entityTarget().contextParam()) == ParamState.DISALLOWED) {
            return Verdict.NO;
        } else if (condition.entityTarget() != LootContext.EntityTarget.THIS || page.entityTypes().isEmpty()) {
            return Verdict.UNKNOWN;
        }

        EntityPredicate predicate = condition.predicate().get();
        List<Verdict> parts = new ArrayList<>();

        predicate.entityType().ifPresent((t) -> parts.add(testEntityTypes(page, (e) -> t.matches(e.builtInRegistryHolder()), true)));
        predicate.subPredicate().ifPresent((s) -> parts.add(Objects.requireNonNullElse(utils.testEntitySubPredicate(utils, s, page), Verdict.UNKNOWN)));

        if (hasUntestableParts(predicate)) {
            parts.add(Verdict.UNKNOWN);
        }

        return combineParts(parts);
    }

    @Nullable
    public static Verdict testDamageSource(IServerUtils utils, DamageSourceCondition ignoredCondition, LootPage page) {
        return utils.getParamState(page, LootContextParams.DAMAGE_SOURCE) == ParamState.DISALLOWED ? Verdict.NO : null;
    }

    @NotNull
    private static List<LootItemCondition> getUnexplained(List<LootItemCondition> conditions, boolean[] explained, int explainedCount) {
        if (explainedCount == 0) {
            return conditions;
        } else if (explainedCount == conditions.size()) {
            return List.of();
        }

        List<LootItemCondition> unexplained = new ArrayList<>(conditions.size() - explainedCount);

        for (int i = 0; i < conditions.size(); i++) {
            if (!explained[i]) {
                unexplained.add(conditions.get(i));
            }
        }

        return unexplained;
    }

    @NotNull
    private static Verdict testAllOf(IServerUtils utils, List<LootItemCondition> terms, LootPage page) {
        boolean allYes = true;
        boolean explained = true;

        for (LootItemCondition term : terms) {
            Verdict verdict = utils.testPage(utils, term, page);

            if (verdict.match() == Match.NO) {
                return Verdict.NO;
            }

            allYes &= verdict.match() == Match.YES;
            explained &= verdict.explained();
        }

        return allYes ? Verdict.yes(explained) : Verdict.UNKNOWN;
    }

    @NotNull
    private static Verdict testAnyOf(IServerUtils utils, List<LootItemCondition> terms, LootPage page) {
        boolean anyYes = false;
        boolean allNo = true;
        boolean explained = true;

        for (LootItemCondition term : terms) {
            Verdict verdict = utils.testPage(utils, term, page);

            anyYes |= verdict.match() == Match.YES;
            allNo &= verdict.match() == Match.NO;
            explained &= verdict.explained();
        }

        if (anyYes) {
            return Verdict.yes(explained);
        }

        return allNo ? Verdict.NO : Verdict.UNKNOWN;
    }

    @NotNull
    private static Verdict invert(Verdict verdict) {
        return switch (verdict.match()) {
            case YES -> verdict.explained() ? Verdict.NO : Verdict.UNKNOWN;
            case NO -> Verdict.yes(false);
            case UNKNOWN -> Verdict.UNKNOWN;
        };
    }

    @NotNull
    private static Verdict combineParts(List<Verdict> parts) {
        boolean anyYes = false;
        boolean explained = true;

        for (Verdict part : parts) {
            if (part.match() == Match.NO) {
                return Verdict.NO;
            }

            anyYes |= part.match() == Match.YES;
            explained &= part.explained();
        }

        if (anyYes || parts.isEmpty()) {
            return Verdict.yes(explained);
        }

        return Verdict.UNKNOWN;
    }

    private static boolean hasUntestableParts(EntityPredicate predicate) {
        return predicate.distanceToPlayer().isPresent()
                || predicate.movement().isPresent()
                || predicate.location().located().isPresent()
                || predicate.location().steppingOn().isPresent()
                || predicate.location().affectsMovement().isPresent()
                || predicate.effects().isPresent()
                || predicate.nbt().isPresent()
                || predicate.flags().isPresent()
                || predicate.equipment().isPresent()
                || predicate.periodicTick().isPresent()
                || predicate.vehicle().isPresent()
                || predicate.passenger().isPresent()
                || predicate.targetedEntity().isPresent()
                || predicate.team().isPresent()
                || predicate.slots().isPresent()
                || !predicate.components().isEmpty();
    }
}
