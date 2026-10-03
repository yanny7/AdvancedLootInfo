package com.yanny.ali.plugin.server;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.api.NumberFunctions;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import net.minecraft.core.Holder;
import net.minecraft.world.level.storage.loot.providers.number.ints.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class IntProviderUtils {
    public static void register(IServerRegistry registry) {
        registry.registerContextIntProvider(ConstantValue.class, IntProviderUtils::convertConstant);
        registry.registerContextIntProvider(UniformGenerator.class, IntProviderUtils::convertUniform);
        registry.registerContextIntProvider(BinomialDistributionGenerator.class, IntProviderUtils::convertBinomial);
        registry.registerContextIntProvider(ScoreboardValue.class, IntProviderUtils::convertScore);
        registry.registerContextIntProvider(StorageValue.class, IntProviderUtils::convertStorage);
        registry.registerContextIntProvider(EnvironmentAttributeValue.class, IntProviderUtils::convertEnvironmentAttribute);
        registry.registerContextIntProvider(Sum.class, IntProviderUtils::convertSum);
        registry.registerContextIntProvider(Product.class, IntProviderUtils::convertProduct);
        registry.registerContextIntProvider(Average.class, IntProviderUtils::convertAverage);
        registry.registerContextIntProvider(Minimum.class, IntProviderUtils::convertMinimum);
        registry.registerContextIntProvider(Maximum.class, IntProviderUtils::convertMaximum);
        registry.registerContextIntProvider(Difference.class, IntProviderUtils::convertDifference);
        registry.registerContextIntProvider(Negate.class, IntProviderUtils::convertNegate);
        registry.registerContextIntProvider(Absolute.class, IntProviderUtils::convertAbsolute);
        registry.registerContextIntProvider(FromFloat.class, IntProviderUtils::convertFromFloat);
        registry.registerContextIntProvider(ConditionalValue.class, IntProviderUtils::convertConditional);
        registry.registerContextIntProvider(NumberDispatcher.class, IntProviderUtils::convertDispatcher);
        registry.registerContextIntProvider(WeightedListValue.class, IntProviderUtils::convertWeightedList);
        registry.registerContextIntProvider(Modulus.class, IntProviderUtils::convertModulus);
        registry.registerContextIntProvider(FloorModulus.class, IntProviderUtils::convertFloorModulus);
        registry.registerContextIntProvider(Quotient.class, IntProviderUtils::convertQuotient);
        registry.registerContextIntProvider(FloorQuotient.class, IntProviderUtils::convertFloorQuotient);
        registry.registerContextIntProvider(Power.class, IntProviderUtils::convertPower);
    }

    @NotNull
    public static NumberExpr convert(IServerUtils utils, Holder<ContextIntProvider> provider, List<TooltipNode> conditions) {
        return utils.convertContextInt(utils, provider, conditions);
    }

    @NotNull
    public static NumberExpr convertConstant(IServerUtils utils, ConstantValue provider, List<TooltipNode> conditions) {
        return NumberExpr.constant(provider.value());
    }

    @NotNull
    public static NumberExpr convertUniform(IServerUtils utils, UniformGenerator provider, List<TooltipNode> conditions) {
        return NumberProviderUtils.range(utils, provider, conditions, IntProviderUtils::convert, NumberExpr::uniformInt);
    }

    @NotNull
    public static NumberExpr convertBinomial(IServerUtils utils, BinomialDistributionGenerator provider, List<TooltipNode> conditions) {
        return NumberExpr.binomial(convert(utils, provider.n(), conditions), FloatProviderUtils.convert(utils, provider.p(), conditions));
    }

    @NotNull
    public static NumberExpr convertScore(IServerUtils utils, ScoreboardValue provider, List<TooltipNode> conditions) {
        return NumberProviderUtils.withFallback(NumberProviderUtils.score(provider.target(), provider.score()), Lang.Numbers.SCORE_EXISTS, convert(utils, provider.fallback(), conditions), conditions);
    }

    @NotNull
    public static NumberExpr convertStorage(IServerUtils utils, StorageValue provider, List<TooltipNode> conditions) {
        NumberExpr value = NumberExpr.fn(NumberFunctions.TRUNC, NumberProviderUtils.storage(provider.access()));

        return NumberProviderUtils.withFallback(value, Lang.Numbers.STORAGE_VALUE_EXISTS, convert(utils, provider.fallback(), conditions), conditions);
    }

    @NotNull
    public static NumberExpr convertEnvironmentAttribute(IServerUtils utils, EnvironmentAttributeValue provider, List<TooltipNode> conditions) {
        return NumberProviderUtils.environmentAttribute(provider.attribute());
    }

    @NotNull
    public static NumberExpr convertSum(IServerUtils utils, Sum provider, List<TooltipNode> conditions) {
        return NumberProviderUtils.aggregate(utils, provider, conditions, IntProviderUtils::convert, NumberExpr::add);
    }

    @NotNull
    public static NumberExpr convertProduct(IServerUtils utils, Product provider, List<TooltipNode> conditions) {
        return NumberProviderUtils.aggregate(utils, provider, conditions, IntProviderUtils::convert, NumberExpr::mul);
    }

    @NotNull
    public static NumberExpr convertAverage(IServerUtils utils, Average provider, List<TooltipNode> conditions) {
        return NumberExpr.fn(NumberFunctions.TRUNC, NumberProviderUtils.aggregate(utils, provider, conditions, IntProviderUtils::convert, (args) -> NumberExpr.fn(NumberFunctions.AVG, args)));
    }

    @NotNull
    public static NumberExpr convertMinimum(IServerUtils utils, Minimum provider, List<TooltipNode> conditions) {
        return NumberProviderUtils.aggregate(utils, provider, conditions, IntProviderUtils::convert, NumberExpr::min);
    }

    @NotNull
    public static NumberExpr convertMaximum(IServerUtils utils, Maximum provider, List<TooltipNode> conditions) {
        return NumberProviderUtils.aggregate(utils, provider, conditions, IntProviderUtils::convert, NumberExpr::max);
    }

    @NotNull
    public static NumberExpr convertDifference(IServerUtils utils, Difference provider, List<TooltipNode> conditions) {
        return NumberProviderUtils.binary(utils, provider, conditions, IntProviderUtils::convert, NumberExpr::sub);
    }

    @NotNull
    public static NumberExpr convertNegate(IServerUtils utils, Negate provider, List<TooltipNode> conditions) {
        return NumberProviderUtils.unary(utils, provider, conditions, IntProviderUtils::convert, (value) -> NumberExpr.fn(NumberFunctions.NEG, value));
    }

    @NotNull
    public static NumberExpr convertAbsolute(IServerUtils utils, Absolute provider, List<TooltipNode> conditions) {
        return NumberProviderUtils.unary(utils, provider, conditions, IntProviderUtils::convert, (value) -> NumberExpr.fn(NumberFunctions.ABS, value));
    }

    @NotNull
    public static NumberExpr convertFromFloat(IServerUtils utils, FromFloat provider, List<TooltipNode> conditions) {
        return NumberProviderUtils.unary(utils, provider, conditions, FloatProviderUtils::convert, (value) -> NumberExpr.fn(NumberFunctions.TRUNC, value));
    }

    @NotNull
    public static NumberExpr convertConditional(IServerUtils utils, ConditionalValue provider, List<TooltipNode> conditions) {
        return NumberProviderUtils.conditional(utils, provider, conditions, IntProviderUtils::convert);
    }

    @NotNull
    public static NumberExpr convertDispatcher(IServerUtils utils, NumberDispatcher provider, List<TooltipNode> conditions) {
        return NumberProviderUtils.dispatcher(utils, provider, conditions, IntProviderUtils::convert);
    }

    @NotNull
    public static NumberExpr convertWeightedList(IServerUtils utils, WeightedListValue provider, List<TooltipNode> conditions) {
        return NumberProviderUtils.distribution(utils, provider, conditions, IntProviderUtils::convert);
    }

    @NotNull
    public static NumberExpr convertModulus(IServerUtils utils, Modulus provider, List<TooltipNode> conditions) {
        return NumberProviderUtils.binary(utils, provider, conditions, IntProviderUtils::convert, (a, b) -> NumberExpr.fn(NumberFunctions.MOD, a, b));
    }

    @NotNull
    public static NumberExpr convertFloorModulus(IServerUtils utils, FloorModulus provider, List<TooltipNode> conditions) {
        return NumberProviderUtils.binary(utils, provider, conditions, IntProviderUtils::convert, (a, b) -> NumberExpr.fn(NumberFunctions.FLOOR_MOD, a, b));
    }

    @NotNull
    public static NumberExpr convertQuotient(IServerUtils utils, Quotient provider, List<TooltipNode> conditions) {
        return NumberProviderUtils.binary(utils, provider, conditions, IntProviderUtils::convert, (a, b) -> NumberExpr.fn(NumberFunctions.TRUNC, NumberExpr.div(a, b)));
    }

    @NotNull
    public static NumberExpr convertFloorQuotient(IServerUtils utils, FloorQuotient provider, List<TooltipNode> conditions) {
        return NumberProviderUtils.binary(utils, provider, conditions, IntProviderUtils::convert, (a, b) -> NumberExpr.fn(NumberFunctions.FLOOR_DIV, a, b));
    }

    @NotNull
    public static NumberExpr convertPower(IServerUtils utils, Power provider, List<TooltipNode> conditions) {
        return NumberProviderUtils.power(utils, provider, conditions, IntProviderUtils::convert);
    }
}
