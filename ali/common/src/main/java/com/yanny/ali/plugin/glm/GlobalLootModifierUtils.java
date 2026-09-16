package com.yanny.ali.plugin.glm;

import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.language.CoreLang;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.nodes.GlobalLootModifierNode;
import com.yanny.ali.plugin.server.TooltipUtils;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootDataResolver;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
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
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class GlobalLootModifierUtils {
    private static final Logger LOGGER = CommonLogUtils.getLogger(Utils.MOD_ID);

    @NotNull
    public static IPageLootModifier getLootModifier(IServerUtils utils, @Nullable Object modifier, List<LootItemCondition> conditions,
                                                    Function<List<LootItemCondition>, List<IOperation>> operationSupplier) {
        List<Object> terms = Stream.concat(Stream.ofNullable(modifier), conditions.stream()).toList();

        return new IPageLootModifier() {
            @NotNull
            @Override
            public Match test(LootPage page) {
                Match result = Match.UNKNOWN;

                for (Object term : terms) {
                    Match match = utils.testPage(utils, term, page).match();

                    if (match == Match.NO) {
                        return Match.NO;
                    } else if (match == Match.YES) {
                        result = Match.YES;
                    }
                }

                return result;
            }

            @NotNull
            @Override
            public List<IOperation> getOperations(LootPage page) {
                return operationSupplier.apply(conditions.stream().filter((c) -> !utils.testPage(utils, c, page).explained()).toList());
            }
        };
    }

    public static Optional<IPageLootModifier> getMissingGlobalLootModifier(IServerUtils utils, IGlobalLootModifierWrapper modifier) {
        if (modifier.isLootModifier()) {
            return Optional.of(getLootModifier(utils, modifier.getLootModifier(), modifier.getConditions(), (conditions) -> {

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
                                   List<ILootContextPreparer> preparers, LootDataResolver lootData) {
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
            return invert(utils.testPage(utils, inverted.term, page));
        } else if (value instanceof LootItemCondition condition) {
            return LootContextProbe.probe(utils, condition, page, preparers, lootData);
        }

        return Verdict.UNKNOWN;
    }

    @NotNull
    public static ParamState getParamState(LootPage page, LootContextParam<?> param) {
        LootContextParamSet paramSet = page.paramSet();

        if (paramSet == LootContextParamSets.ALL_PARAMS) {
            return ParamState.GENERIC;
        } else if (!paramSet.isAllowed(param)) {
            return ParamState.DISALLOWED;
        }

        return paramSet.getRequired().contains(param) ? ParamState.REQUIRED : ParamState.OPTIONAL;
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
    public static Verdict testTable(LootPage page, Predicate<ResourceLocation> matcher, boolean explained) {
        return matcher.test(page.tableId()) ? Verdict.yes(explained) : Verdict.NO;
    }

    @Nullable
    public static Verdict testBlockStateProperty(IServerUtils ignoredUtils, LootItemBlockStatePropertyCondition condition, LootPage page) {
        if (page.blocks().isEmpty()) {
            return null;
        }

        return testBlocks(page, condition.block::equals, condition.properties.properties.isEmpty());
    }

    @NotNull
    public static Verdict testEntityProperty(IServerUtils utils, LootItemEntityPropertyCondition condition, LootPage page) {
        EntityPredicate predicate = condition.predicate;

        if (predicate == EntityPredicate.ANY) {
            return Verdict.yes(true);
        } else if (utils.getParamState(page, condition.entityTarget.getParam()) == ParamState.DISALLOWED) {
            return Verdict.NO;
        } else if (condition.entityTarget != LootContext.EntityTarget.THIS || page.entityTypes().isEmpty()) {
            return Verdict.UNKNOWN;
        }

        List<Verdict> parts = new ArrayList<>();

        if (predicate.entityType != EntityTypePredicate.ANY) {
            parts.add(testEntityTypes(page, predicate.entityType::matches, true));
        }

        if (predicate.subPredicate != EntitySubPredicate.ANY) {
            parts.add(Objects.requireNonNullElse(utils.testEntitySubPredicate(utils, predicate.subPredicate, page), Verdict.UNKNOWN));
        }

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
    private static Verdict testAllOf(IServerUtils utils, LootItemCondition[] terms, LootPage page) {
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
    private static Verdict testAnyOf(IServerUtils utils, LootItemCondition[] terms, LootPage page) {
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
        return predicate.distanceToPlayer != DistancePredicate.ANY
                || predicate.location != LocationPredicate.ANY
                || predicate.steppingOnLocation != LocationPredicate.ANY
                || predicate.effects != MobEffectsPredicate.ANY
                || predicate.nbt != NbtPredicate.ANY
                || predicate.flags != EntityFlagsPredicate.ANY
                || predicate.equipment != EntityEquipmentPredicate.ANY
                || predicate.vehicle != EntityPredicate.ANY
                || predicate.passenger != EntityPredicate.ANY
                || predicate.targetedEntity != EntityPredicate.ANY
                || predicate.team != null;
    }
}
