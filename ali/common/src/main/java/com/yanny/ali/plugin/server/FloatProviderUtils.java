package com.yanny.ali.plugin.server;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.api.NumberFunctions;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.providers.number.UnaryProvider;
import net.minecraft.world.level.storage.loot.providers.number.floats.*;
import org.jetbrains.annotations.NotNull;

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
    public static NumberExpr convert(IServerUtils utils, Holder<ContextFloatProvider> provider) {
        return utils.convertContextFloat(utils, provider);
    }

    @NotNull
    public static NumberExpr convertConstant(IServerUtils utils, ConstantValue provider) {
        return NumberExpr.constant(provider.value());
    }

    @NotNull
    public static NumberExpr convertUniform(IServerUtils utils, UniformGenerator provider) {
        return NumberProviderUtils.range(utils, provider, FloatProviderUtils::convert, (min, max) -> NumberExpr.fn(NumberFunctions.UNIFORM_FLOAT, min, max));
    }

    @NotNull
    public static NumberExpr convertStorage(IServerUtils utils, StorageValue provider) {
        return NumberProviderUtils.withFallback(utils, NumberProviderUtils.storage(provider.access()), Lang.Numbers.STORAGE_VALUE_EXISTS, convert(utils, provider.fallback()));
    }

    @NotNull
    public static NumberExpr convertEnvironmentAttribute(IServerUtils utils, EnvironmentAttributeValue provider) {
        return NumberProviderUtils.environmentAttribute(provider.attribute());
    }

    @NotNull
    public static NumberExpr convertEnchantmentLevel(IServerUtils utils, EnchantmentLevelProvider provider) {
        return utils.convertLevelBasedValue(utils, provider.amount(), TooltipUtils.anyEnchantmentLevel(utils));
    }

    @NotNull
    public static NumberExpr convertSum(IServerUtils utils, Sum provider) {
        return NumberProviderUtils.aggregate(utils, provider, FloatProviderUtils::convert, NumberExpr::add);
    }

    @NotNull
    public static NumberExpr convertProduct(IServerUtils utils, Product provider) {
        return NumberProviderUtils.aggregate(utils, provider, FloatProviderUtils::convert, NumberExpr::mul);
    }

    @NotNull
    public static NumberExpr convertAverage(IServerUtils utils, Average provider) {
        return NumberProviderUtils.aggregate(utils, provider, FloatProviderUtils::convert, (args) -> NumberExpr.fn(NumberFunctions.AVG, args));
    }

    @NotNull
    public static NumberExpr convertMinimum(IServerUtils utils, Minimum provider) {
        return NumberProviderUtils.aggregate(utils, provider, FloatProviderUtils::convert, NumberExpr::min);
    }

    @NotNull
    public static NumberExpr convertMaximum(IServerUtils utils, Maximum provider) {
        return NumberProviderUtils.aggregate(utils, provider, FloatProviderUtils::convert, NumberExpr::max);
    }

    @NotNull
    public static NumberExpr convertDifference(IServerUtils utils, Difference provider) {
        return NumberProviderUtils.binary(utils, provider, FloatProviderUtils::convert, NumberExpr::sub);
    }

    @NotNull
    public static NumberExpr convertNegate(IServerUtils utils, Negate provider) {
        return unary(utils, provider, NumberFunctions.NEG);
    }

    @NotNull
    public static NumberExpr convertAbsolute(IServerUtils utils, Absolute provider) {
        return unary(utils, provider, NumberFunctions.ABS);
    }

    @NotNull
    public static NumberExpr convertFromInt(IServerUtils utils, FromInt provider) {
        return NumberProviderUtils.unary(utils, provider, IntProviderUtils::convert, Function.identity());
    }

    @NotNull
    public static NumberExpr convertFloor(IServerUtils utils, Floor provider) {
        return unary(utils, provider, NumberFunctions.FLOOR);
    }

    @NotNull
    public static NumberExpr convertCeiling(IServerUtils utils, Ceiling provider) {
        return unary(utils, provider, NumberFunctions.CEIL);
    }

    @NotNull
    public static NumberExpr convertRound(IServerUtils utils, Round provider) {
        return unary(utils, provider, NumberFunctions.ROUND);
    }

    @NotNull
    public static NumberExpr convertTruncate(IServerUtils utils, Truncate provider) {
        return unary(utils, provider, NumberFunctions.TRUNC);
    }

    @NotNull
    public static NumberExpr convertSquareRoot(IServerUtils utils, SquareRoot provider) {
        return unary(utils, provider, NumberFunctions.SQRT);
    }

    @NotNull
    public static NumberExpr convertSine(IServerUtils utils, Sine provider) {
        return unary(utils, provider, NumberFunctions.SIN);
    }

    @NotNull
    public static NumberExpr convertCosine(IServerUtils utils, Cosine provider) {
        return unary(utils, provider, NumberFunctions.COS);
    }

    @NotNull
    public static NumberExpr convertConditional(IServerUtils utils, ConditionalValue provider) {
        return NumberProviderUtils.conditional(utils, provider, FloatProviderUtils::convert);
    }

    @NotNull
    public static NumberExpr convertDispatcher(IServerUtils utils, NumberDispatcher provider) {
        return NumberProviderUtils.dispatcher(utils, provider, FloatProviderUtils::convert);
    }

    @NotNull
    public static NumberExpr convertWeightedList(IServerUtils utils, WeightedListValue provider) {
        return NumberProviderUtils.distribution(utils, provider, FloatProviderUtils::convert);
    }

    @NotNull
    public static NumberExpr convertLength(IServerUtils utils, Length provider) {
        return NumberProviderUtils.aggregate(utils, provider, FloatProviderUtils::convert, (args) -> NumberExpr.fn(NumberFunctions.LENGTH, args));
    }

    @NotNull
    public static NumberExpr convertModulus(IServerUtils utils, Modulus provider) {
        return NumberProviderUtils.binary(utils, provider, FloatProviderUtils::convert, (a, b) -> NumberExpr.fn(NumberFunctions.MOD, a, b));
    }

    @NotNull
    public static NumberExpr convertQuotient(IServerUtils utils, Quotient provider) {
        return NumberProviderUtils.binary(utils, provider, FloatProviderUtils::convert, NumberExpr::div);
    }

    @NotNull
    public static NumberExpr convertPower(IServerUtils utils, Power provider) {
        return NumberProviderUtils.power(utils, provider, FloatProviderUtils::convert);
    }

    @NotNull
    private static NumberExpr unary(IServerUtils utils, UnaryProvider<ContextFloatProvider> provider, Identifier function) {
        return NumberProviderUtils.unary(utils, provider, FloatProviderUtils::convert, (value) -> NumberExpr.fn(function, value));
    }
}
