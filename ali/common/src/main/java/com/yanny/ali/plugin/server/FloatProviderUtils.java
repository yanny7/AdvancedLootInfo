package com.yanny.ali.plugin.server;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.api.NumberFunctions;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.providers.number.UnaryProvider;
import net.minecraft.world.level.storage.loot.providers.number.floats.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

public class FloatProviderUtils {
    public static void register(IServerRegistry registry) {
        registry.registerContextFloatProvider(ConstantValue.class, FloatProviderUtils::convertConstant);
        registry.registerContextFloatProvider(UniformGenerator.class, FloatProviderUtils::convertUniform);
        registry.registerContextFloatProvider(StorageValue.class, FloatProviderUtils::convertStorage);
        registry.registerContextFloatProvider(EnvironmentAttributeValue.class, FloatProviderUtils::convertEnvironmentAttribute);
        registry.registerContextFloatProvider(EnchantmentLevelProvider.class, FloatProviderUtils::convertEnchantmentLevel);
        registry.registerContextFloatProvider(Sum.class, FloatProviderUtils::convertSum);
        registry.registerContextFloatProvider(Product.class, FloatProviderUtils::convertProduct);
        registry.registerContextFloatProvider(Average.class, FloatProviderUtils::convertAverage);
        registry.registerContextFloatProvider(Minimum.class, FloatProviderUtils::convertMinimum);
        registry.registerContextFloatProvider(Maximum.class, FloatProviderUtils::convertMaximum);
        registry.registerContextFloatProvider(Difference.class, FloatProviderUtils::convertDifference);
        registry.registerContextFloatProvider(Negate.class, FloatProviderUtils::convertNegate);
        registry.registerContextFloatProvider(Absolute.class, FloatProviderUtils::convertAbsolute);
        registry.registerContextFloatProvider(FromInt.class, FloatProviderUtils::convertFromInt);
        registry.registerContextFloatProvider(Floor.class, FloatProviderUtils::convertFloor);
        registry.registerContextFloatProvider(Ceiling.class, FloatProviderUtils::convertCeiling);
        registry.registerContextFloatProvider(Round.class, FloatProviderUtils::convertRound);
        registry.registerContextFloatProvider(Truncate.class, FloatProviderUtils::convertTruncate);
        registry.registerContextFloatProvider(SquareRoot.class, FloatProviderUtils::convertSquareRoot);
        registry.registerContextFloatProvider(Sine.class, FloatProviderUtils::convertSine);
        registry.registerContextFloatProvider(Cosine.class, FloatProviderUtils::convertCosine);
        registry.registerContextFloatProvider(ConditionalValue.class, FloatProviderUtils::convertConditional);
        registry.registerContextFloatProvider(NumberDispatcher.class, FloatProviderUtils::convertDispatcher);
        registry.registerContextFloatProvider(WeightedListValue.class, FloatProviderUtils::convertWeightedList);
        registry.registerContextFloatProvider(Length.class, FloatProviderUtils::convertLength);
        registry.registerContextFloatProvider(Modulus.class, FloatProviderUtils::convertModulus);
        registry.registerContextFloatProvider(Quotient.class, FloatProviderUtils::convertQuotient);
        registry.registerContextFloatProvider(Power.class, FloatProviderUtils::convertPower);
    }

    @NotNull
    public static NumberExpr convert(IServerUtils utils, Holder<ContextFloatProvider> provider, List<TooltipNode> conditions) {
        return utils.convertContextFloat(utils, provider, conditions);
    }

    @NotNull
    public static NumberExpr convertConstant(IServerUtils utils, ConstantValue provider, List<TooltipNode> conditions) {
        return NumberExpr.constant(provider.value());
    }

    @NotNull
    public static NumberExpr convertUniform(IServerUtils utils, UniformGenerator provider, List<TooltipNode> conditions) {
        return NumberProviderUtils.range(utils, provider, conditions, FloatProviderUtils::convert, (min, max) -> NumberExpr.fn(NumberFunctions.UNIFORM_FLOAT, min, max));
    }

    @NotNull
    public static NumberExpr convertStorage(IServerUtils utils, StorageValue provider, List<TooltipNode> conditions) {
        return NumberProviderUtils.withFallback(NumberProviderUtils.storage(provider.access()), Lang.Numbers.STORAGE_VALUE_EXISTS, convert(utils, provider.fallback(), conditions), conditions);
    }

    @NotNull
    public static NumberExpr convertEnvironmentAttribute(IServerUtils utils, EnvironmentAttributeValue provider, List<TooltipNode> conditions) {
        return NumberProviderUtils.environmentAttribute(provider.attribute());
    }

    @NotNull
    public static NumberExpr convertEnchantmentLevel(IServerUtils utils, EnchantmentLevelProvider provider, List<TooltipNode> conditions) {
        return utils.convertLevelBasedValue(utils, provider.amount(), TooltipUtils.anyEnchantmentLevel(utils));
    }

    @NotNull
    public static NumberExpr convertSum(IServerUtils utils, Sum provider, List<TooltipNode> conditions) {
        return NumberProviderUtils.aggregate(utils, provider, conditions, FloatProviderUtils::convert, NumberExpr::add);
    }

    @NotNull
    public static NumberExpr convertProduct(IServerUtils utils, Product provider, List<TooltipNode> conditions) {
        return NumberProviderUtils.aggregate(utils, provider, conditions, FloatProviderUtils::convert, NumberExpr::mul);
    }

    @NotNull
    public static NumberExpr convertAverage(IServerUtils utils, Average provider, List<TooltipNode> conditions) {
        return NumberProviderUtils.aggregate(utils, provider, conditions, FloatProviderUtils::convert, (args) -> NumberExpr.fn(NumberFunctions.AVG, args));
    }

    @NotNull
    public static NumberExpr convertMinimum(IServerUtils utils, Minimum provider, List<TooltipNode> conditions) {
        return NumberProviderUtils.aggregate(utils, provider, conditions, FloatProviderUtils::convert, NumberExpr::min);
    }

    @NotNull
    public static NumberExpr convertMaximum(IServerUtils utils, Maximum provider, List<TooltipNode> conditions) {
        return NumberProviderUtils.aggregate(utils, provider, conditions, FloatProviderUtils::convert, NumberExpr::max);
    }

    @NotNull
    public static NumberExpr convertDifference(IServerUtils utils, Difference provider, List<TooltipNode> conditions) {
        return NumberProviderUtils.binary(utils, provider, conditions, FloatProviderUtils::convert, NumberExpr::sub);
    }

    @NotNull
    public static NumberExpr convertNegate(IServerUtils utils, Negate provider, List<TooltipNode> conditions) {
        return unary(utils, provider, conditions, NumberFunctions.NEG);
    }

    @NotNull
    public static NumberExpr convertAbsolute(IServerUtils utils, Absolute provider, List<TooltipNode> conditions) {
        return unary(utils, provider, conditions, NumberFunctions.ABS);
    }

    @NotNull
    public static NumberExpr convertFromInt(IServerUtils utils, FromInt provider, List<TooltipNode> conditions) {
        return NumberProviderUtils.unary(utils, provider, conditions, IntProviderUtils::convert, Function.identity());
    }

    @NotNull
    public static NumberExpr convertFloor(IServerUtils utils, Floor provider, List<TooltipNode> conditions) {
        return unary(utils, provider, conditions, NumberFunctions.FLOOR);
    }

    @NotNull
    public static NumberExpr convertCeiling(IServerUtils utils, Ceiling provider, List<TooltipNode> conditions) {
        return unary(utils, provider, conditions, NumberFunctions.CEIL);
    }

    @NotNull
    public static NumberExpr convertRound(IServerUtils utils, Round provider, List<TooltipNode> conditions) {
        return unary(utils, provider, conditions, NumberFunctions.ROUND);
    }

    @NotNull
    public static NumberExpr convertTruncate(IServerUtils utils, Truncate provider, List<TooltipNode> conditions) {
        return unary(utils, provider, conditions, NumberFunctions.TRUNC);
    }

    @NotNull
    public static NumberExpr convertSquareRoot(IServerUtils utils, SquareRoot provider, List<TooltipNode> conditions) {
        return unary(utils, provider, conditions, NumberFunctions.SQRT);
    }

    @NotNull
    public static NumberExpr convertSine(IServerUtils utils, Sine provider, List<TooltipNode> conditions) {
        return unary(utils, provider, conditions, NumberFunctions.SIN);
    }

    @NotNull
    public static NumberExpr convertCosine(IServerUtils utils, Cosine provider, List<TooltipNode> conditions) {
        return unary(utils, provider, conditions, NumberFunctions.COS);
    }

    @NotNull
    public static NumberExpr convertConditional(IServerUtils utils, ConditionalValue provider, List<TooltipNode> conditions) {
        return NumberProviderUtils.conditional(utils, provider, conditions, FloatProviderUtils::convert);
    }

    @NotNull
    public static NumberExpr convertDispatcher(IServerUtils utils, NumberDispatcher provider, List<TooltipNode> conditions) {
        return NumberProviderUtils.dispatcher(utils, provider, conditions, FloatProviderUtils::convert);
    }

    @NotNull
    public static NumberExpr convertWeightedList(IServerUtils utils, WeightedListValue provider, List<TooltipNode> conditions) {
        return NumberProviderUtils.distribution(utils, provider, conditions, FloatProviderUtils::convert);
    }

    @NotNull
    public static NumberExpr convertLength(IServerUtils utils, Length provider, List<TooltipNode> conditions) {
        return NumberProviderUtils.aggregate(utils, provider, conditions, FloatProviderUtils::convert, (args) -> NumberExpr.fn(NumberFunctions.LENGTH, args));
    }

    @NotNull
    public static NumberExpr convertModulus(IServerUtils utils, Modulus provider, List<TooltipNode> conditions) {
        return NumberProviderUtils.binary(utils, provider, conditions, FloatProviderUtils::convert, (a, b) -> NumberExpr.fn(NumberFunctions.MOD, a, b));
    }

    @NotNull
    public static NumberExpr convertQuotient(IServerUtils utils, Quotient provider, List<TooltipNode> conditions) {
        return NumberProviderUtils.binary(utils, provider, conditions, FloatProviderUtils::convert, NumberExpr::div);
    }

    @NotNull
    public static NumberExpr convertPower(IServerUtils utils, Power provider, List<TooltipNode> conditions) {
        return NumberProviderUtils.power(utils, provider, conditions, FloatProviderUtils::convert);
    }

    @NotNull
    private static NumberExpr unary(IServerUtils utils, UnaryProvider<ContextFloatProvider> provider, List<TooltipNode> conditions, Identifier function) {
        return NumberProviderUtils.unary(utils, provider, conditions, FloatProviderUtils::convert, (value) -> NumberExpr.fn(function, value));
    }
}
