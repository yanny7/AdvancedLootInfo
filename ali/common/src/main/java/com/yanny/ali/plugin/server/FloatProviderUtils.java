package com.yanny.ali.plugin.server;

import com.yanny.aci.api.RangeValue;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import net.minecraft.core.Holder;
import net.minecraft.world.level.storage.loot.providers.number.floats.*;
import org.jetbrains.annotations.NotNull;

import java.util.function.UnaryOperator;

public class FloatProviderUtils {
    public static void register(IServerRegistry registry) {
        registry.registerFloatProvider(ConstantValue.class, FloatProviderUtils::convertConstant);
        registry.registerFloatProvider(UniformGenerator.class, FloatProviderUtils::convertUniform);
        registry.registerFloatProvider(StorageValue.class, FloatProviderUtils::convertStorage);
        registry.registerFloatProvider(EnvironmentAttributeValue.class, FloatProviderUtils::convertEnvironmentAttribute);
        registry.registerFloatProvider(EnchantmentLevelProvider.class, FloatProviderUtils::convertEnchantmentLevel);
        registry.registerFloatProvider(Sum.class, FloatProviderUtils::convertSum);
        registry.registerFloatProvider(Product.class, FloatProviderUtils::convertProduct);
        registry.registerFloatProvider(Average.class, FloatProviderUtils::convertAverage);
        registry.registerFloatProvider(Minimum.class, FloatProviderUtils::convertMinimum);
        registry.registerFloatProvider(Maximum.class, FloatProviderUtils::convertMaximum);
        registry.registerFloatProvider(Difference.class, FloatProviderUtils::convertDifference);
        registry.registerFloatProvider(Negate.class, FloatProviderUtils::convertNegate);
        registry.registerFloatProvider(Absolute.class, FloatProviderUtils::convertAbsolute);
        registry.registerFloatProvider(FromInt.class, FloatProviderUtils::convertFromInt);
        registry.registerFloatProvider(Floor.class, FloatProviderUtils::convertFloor);
        registry.registerFloatProvider(Ceiling.class, FloatProviderUtils::convertCeiling);
        registry.registerFloatProvider(Round.class, FloatProviderUtils::convertRound);
        registry.registerFloatProvider(Truncate.class, FloatProviderUtils::convertTruncate);
        registry.registerFloatProvider(SquareRoot.class, FloatProviderUtils::convertSquareRoot);
        registry.registerFloatProvider(Sine.class, FloatProviderUtils::convertSine);
        registry.registerFloatProvider(Cosine.class, FloatProviderUtils::convertCosine);
        registry.registerFloatProvider(ConditionalValue.class, FloatProviderUtils::convertConditional);
        registry.registerFloatProvider(NumberDispatcher.class, FloatProviderUtils::convertDispatcher);
        registry.registerFloatProvider(WeightedListValue.class, FloatProviderUtils::convertWeightedList);
        registry.registerFloatProvider(Length.class, FloatProviderUtils::convertLength);
        registry.registerFloatProvider(Modulus.class, FloatProviderUtils::convertModulus);
        registry.registerFloatProvider(Quotient.class, FloatProviderUtils::convertQuotient);
        registry.registerFloatProvider(Power.class, FloatProviderUtils::convertPower);
    }

    @NotNull
    public static RangeValue convert(IServerUtils utils, Holder<ContextFloatProvider> provider) {
        return utils.convertFloat(utils, provider);
    }

    @NotNull
    public static RangeValue convertConstant(IServerUtils utils, ConstantValue provider) {
        return new RangeValue(provider.value());
    }

    @NotNull
    public static RangeValue convertUniform(IServerUtils utils, UniformGenerator provider) {
        return NumberProviderUtils.range(utils, provider, FloatProviderUtils::convert);
    }

    @NotNull
    public static RangeValue convertStorage(IServerUtils utils, StorageValue provider) {
        return NumberProviderUtils.unknown();
    }

    @NotNull
    public static RangeValue convertEnvironmentAttribute(IServerUtils utils, EnvironmentAttributeValue provider) {
        return NumberProviderUtils.unknown();
    }

    @NotNull
    public static RangeValue convertEnchantmentLevel(IServerUtils utils, EnchantmentLevelProvider provider) {
        return NumberProviderUtils.unknown();
    }

    @NotNull
    public static RangeValue convertSum(IServerUtils utils, Sum provider) {
        return NumberProviderUtils.sum(utils, provider, FloatProviderUtils::convert);
    }

    @NotNull
    public static RangeValue convertProduct(IServerUtils utils, Product provider) {
        return NumberProviderUtils.product(utils, provider, FloatProviderUtils::convert);
    }

    @NotNull
    public static RangeValue convertAverage(IServerUtils utils, Average provider) {
        return NumberProviderUtils.average(utils, provider, FloatProviderUtils::convert);
    }

    @NotNull
    public static RangeValue convertMinimum(IServerUtils utils, Minimum provider) {
        return NumberProviderUtils.minimum(utils, provider, FloatProviderUtils::convert);
    }

    @NotNull
    public static RangeValue convertMaximum(IServerUtils utils, Maximum provider) {
        return NumberProviderUtils.maximum(utils, provider, FloatProviderUtils::convert);
    }

    @NotNull
    public static RangeValue convertDifference(IServerUtils utils, Difference provider) {
        return NumberProviderUtils.difference(utils, provider, FloatProviderUtils::convert);
    }

    @NotNull
    public static RangeValue convertNegate(IServerUtils utils, Negate provider) {
        return NumberProviderUtils.negate(utils, provider, FloatProviderUtils::convert);
    }

    @NotNull
    public static RangeValue convertAbsolute(IServerUtils utils, Absolute provider) {
        return NumberProviderUtils.absolute(utils, provider, FloatProviderUtils::convert);
    }

    @NotNull
    public static RangeValue convertFromInt(IServerUtils utils, FromInt provider) {
        return NumberProviderUtils.monotonic(utils, provider, IntProviderUtils::convert, UnaryOperator.identity());
    }

    @NotNull
    public static RangeValue convertFloor(IServerUtils utils, Floor provider) {
        return NumberProviderUtils.monotonic(utils, provider, FloatProviderUtils::convert, FloatProviderUtils::floor);
    }

    @NotNull
    public static RangeValue convertCeiling(IServerUtils utils, Ceiling provider) {
        return NumberProviderUtils.monotonic(utils, provider, FloatProviderUtils::convert, FloatProviderUtils::ceiling);
    }

    @NotNull
    public static RangeValue convertRound(IServerUtils utils, Round provider) {
        return NumberProviderUtils.monotonic(utils, provider, FloatProviderUtils::convert, FloatProviderUtils::round);
    }

    @NotNull
    public static RangeValue convertTruncate(IServerUtils utils, Truncate provider) {
        return NumberProviderUtils.monotonic(utils, provider, FloatProviderUtils::convert, NumberProviderUtils::truncate);
    }

    @NotNull
    public static RangeValue convertSquareRoot(IServerUtils utils, SquareRoot provider) {
        return NumberProviderUtils.monotonic(utils, provider, FloatProviderUtils::convert, FloatProviderUtils::squareRoot);
    }

    @NotNull
    public static RangeValue convertSine(IServerUtils utils, Sine provider) {
        return new RangeValue(-1, 1);
    }

    @NotNull
    public static RangeValue convertCosine(IServerUtils utils, Cosine provider) {
        return new RangeValue(-1, 1);
    }

    @NotNull
    public static RangeValue convertConditional(IServerUtils utils, ConditionalValue provider) {
        return NumberProviderUtils.conditional(utils, provider, FloatProviderUtils::convert);
    }

    @NotNull
    public static RangeValue convertDispatcher(IServerUtils utils, NumberDispatcher provider) {
        return NumberProviderUtils.dispatcher(utils, provider, FloatProviderUtils::convert);
    }

    @NotNull
    public static RangeValue convertWeightedList(IServerUtils utils, WeightedListValue provider) {
        return NumberProviderUtils.distribution(utils, provider, FloatProviderUtils::convert);
    }

    @NotNull
    public static RangeValue convertLength(IServerUtils utils, Length provider) {
        return NumberProviderUtils.unknown();
    }

    @NotNull
    public static RangeValue convertModulus(IServerUtils utils, Modulus provider) {
        return NumberProviderUtils.unknown();
    }

    @NotNull
    public static RangeValue convertQuotient(IServerUtils utils, Quotient provider) {
        return NumberProviderUtils.unknown();
    }

    @NotNull
    public static RangeValue convertPower(IServerUtils utils, Power provider) {
        return NumberProviderUtils.unknown();
    }

    private static float floor(float value) {
        return (float) Math.floor(value);
    }

    private static float ceiling(float value) {
        return (float) Math.ceil(value);
    }

    private static float round(float value) {
        return Math.round(value);
    }

    private static float squareRoot(float value) {
        return (float) Math.sqrt(Math.max(0, value));
    }
}
