package com.yanny.ali.plugin.server;

import com.yanny.aci.api.NumberConverter;
import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.api.NumberFunctions;
import com.yanny.aci.api.NumberText;
import com.yanny.aci.language.CoreLang;
import com.yanny.aci.language.ITooltipKey;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IServerUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.*;
import net.minecraft.world.level.storage.loot.providers.number.floats.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.score.ContextScoreboardNameProvider;
import net.minecraft.world.level.storage.loot.providers.score.FixedScoreboardNameProvider;
import net.minecraft.world.level.storage.loot.providers.score.ScoreboardNameProvider;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class NumberProviderUtils {
    @NotNull
    public static <T> NumberExpr range(IServerUtils utils, RangeProvider<?> provider, List<TooltipNode> conditions, NumberConverter<IServerUtils, Holder<T>> converter,
                                       BiFunction<NumberExpr, NumberExpr, NumberExpr> distribution) {
        return distribution.apply(convert(utils, provider.min(), converter, conditions), convert(utils, provider.max(), converter, conditions));
    }

    @NotNull
    public static <T> NumberExpr aggregate(IServerUtils utils, AggregateProvider<?> provider, List<TooltipNode> conditions, NumberConverter<IServerUtils, Holder<T>> converter,
                                           Function<NumberExpr[], NumberExpr> operation) {
        List<NumberExpr> values = new ArrayList<>();

        for (Holder<?> input : provider.inputs()) {
            values.add(convert(utils, input, converter, conditions));
        }

        return operation.apply(values.toArray(NumberExpr[]::new));
    }

    @NotNull
    public static <T> NumberExpr binary(IServerUtils utils, BinaryProvider<?> provider, List<TooltipNode> conditions, NumberConverter<IServerUtils, Holder<T>> converter,
                                        BiFunction<NumberExpr, NumberExpr, NumberExpr> operation) {
        return operation.apply(convert(utils, provider.left(), converter, conditions), convert(utils, provider.right(), converter, conditions));
    }

    @NotNull
    public static <T> NumberExpr power(IServerUtils utils, PowerProvider<?> provider, List<TooltipNode> conditions, NumberConverter<IServerUtils, Holder<T>> converter) {
        return NumberExpr.fn(NumberFunctions.POW, convert(utils, provider.base(), converter, conditions), convert(utils, provider.exponent(), converter, conditions));
    }

    @NotNull
    public static <T> NumberExpr unary(IServerUtils utils, UnaryProvider<?> provider, List<TooltipNode> conditions, NumberConverter<IServerUtils, Holder<T>> converter,
                                       Function<NumberExpr, NumberExpr> operation) {
        return operation.apply(convert(utils, provider.input(), converter, conditions));
    }

    @NotNull
    public static <T> NumberExpr conditional(IServerUtils utils, ConditionalProvider<?> provider, List<TooltipNode> conditions, NumberConverter<IServerUtils, Holder<T>> converter) {
        NumberExpr onTrue = convert(utils, provider.onTrue(), converter, conditions);
        NumberExpr onFalse = convert(utils, provider.onFalse(), converter, conditions);

        if (provider.condition().isBound() && provider.condition().value() instanceof LootItemRandomChanceCondition(Holder<?> chance)
                && chance.isBound() && chance.value() instanceof ConstantValue(float probability)) {
            if (probability >= 1) {
                return onTrue;
            } else if (probability <= 0) {
                return onFalse;
            }

            return NumberExpr.weighted(List.of(new NumberExpr.WeightedEntry(probability, onTrue), new NumberExpr.WeightedEntry(1 - probability, onFalse)));
        }

        return NumberExpr.cond(List.of(branch(utils, provider.condition(), onTrue, conditions)), onFalse);
    }

    @NotNull
    public static <T> NumberExpr dispatcher(IServerUtils utils, DispatcherProvider<?> provider, List<TooltipNode> conditions, NumberConverter<IServerUtils, Holder<T>> converter) {
        List<NumberExpr.Branch> branches = new ArrayList<>();

        for (DispatcherProvider.Case<?> aCase : provider.cases()) {
            branches.add(branch(utils, aCase.condition(), convert(utils, aCase.value(), converter, conditions), conditions));
        }

        return NumberExpr.cond(branches, convert(utils, provider.defaultValue(), converter, conditions));
    }

    @NotNull
    public static <T> NumberExpr distribution(IServerUtils utils, DistributionProvider<?> provider, List<TooltipNode> conditions, NumberConverter<IServerUtils, Holder<T>> converter) {
        List<NumberExpr.WeightedEntry> entries = new ArrayList<>();

        for (Weighted<? extends Holder<?>> entry : ((WeightedList<? extends Holder<?>>) provider.distribution()).unwrap()) {
            entries.add(new NumberExpr.WeightedEntry(entry.weight(), convert(utils, entry.value(), converter, conditions)));
        }

        return NumberExpr.weighted(entries);
    }

    @NotNull
    public static NumberExpr score(ScoreboardNameProvider targetProvider, String score) {
        NumberText target;

        if (targetProvider instanceof ContextScoreboardNameProvider provider) {
            target = NumberText.key(getTargetKey(provider.target()).singular());
        } else if (targetProvider instanceof FixedScoreboardNameProvider provider) {
            target = NumberText.str(provider.name());
        } else {
            target = NumberText.key(CoreLang.Numbers.UNKNOWN.singular());
        }

        return NumberExpr.score(target, score);
    }

    @NotNull
    public static NumberExpr storage(StoredNumberAccess access) {
        return new NumberExpr.Var(TooltipUtils.STORAGE, List.of(NumberText.str(access.storage().toString()), NumberText.str(access.path().toString())),
                Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    @NotNull
    public static NumberExpr withFallback(NumberExpr value, ITooltipKey existsKey, NumberExpr fallback, List<TooltipNode> conditions) {
        conditions.add(TooltipBuilder.keyOnly(existsKey).build());
        return NumberExpr.cond(List.of(new NumberExpr.Branch(conditions.size() - 1, value)), fallback);
    }

    @NotNull
    public static NumberExpr environmentAttribute(EnvironmentAttribute<?> attribute) {
        return new NumberExpr.Var(TooltipUtils.ENVIRONMENT_ATTRIBUTE, List.of(NumberText.str(String.valueOf(BuiltInRegistries.ENVIRONMENT_ATTRIBUTE.getKey(attribute)))),
                Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    @NotNull
    private static NumberExpr.Branch branch(IServerUtils utils, Holder<LootItemCondition> condition, NumberExpr value, List<TooltipNode> conditions) {
        TooltipNode tooltip = condition.isBound() ? utils.getConditionTooltip(utils, condition.value()).build() : TooltipNode.empty();

        if (tooltip == TooltipNode.empty()) {
            return new NumberExpr.Branch(-1, value);
        }

        conditions.add(tooltip);
        return new NumberExpr.Branch(conditions.size() - 1, value);
    }

    @NotNull
    private static <T> NumberExpr convert(IServerUtils utils, Holder<?> holder, NumberConverter<IServerUtils, Holder<T>> converter, List<TooltipNode> conditions) {
        //noinspection unchecked
        return converter.convert(utils, (Holder<T>) holder, conditions);
    }

    @NotNull
    private static CoreLang.Numbers getTargetKey(LootContext.EntityTarget target) {
        return switch (target) {
            case THIS -> CoreLang.Numbers.TARGET_THIS;
            case ATTACKER -> CoreLang.Numbers.TARGET_KILLER;
            case DIRECT_ATTACKER -> CoreLang.Numbers.TARGET_DIRECT_KILLER;
            case ATTACKING_PLAYER -> CoreLang.Numbers.TARGET_KILLER_PLAYER;
            case TARGET_ENTITY -> CoreLang.Numbers.TARGET_TARGET_ENTITY;
            case INTERACTING_ENTITY -> CoreLang.Numbers.TARGET_INTERACTING_ENTITY;
        };
    }
}
