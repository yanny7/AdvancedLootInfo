package com.yanny.ali.plugin.server;

import com.yanny.aci.api.RangeValue;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import net.minecraft.core.Holder;
import net.minecraft.world.level.storage.loot.providers.number.ints.*;
import org.jetbrains.annotations.NotNull;

public class IntProviderUtils {
    public static void register(IServerRegistry registry) {
        registry.registerIntProvider(ConstantValue.class, IntProviderUtils::convertConstant);
        registry.registerIntProvider(UniformGenerator.class, IntProviderUtils::convertUniform);
        registry.registerIntProvider(BinomialDistributionGenerator.class, IntProviderUtils::convertBinomial);
        registry.registerIntProvider(ScoreboardValue.class, IntProviderUtils::convertScore);
        registry.registerIntProvider(StorageValue.class, IntProviderUtils::convertStorage);
        registry.registerIntProvider(EnvironmentAttributeValue.class, IntProviderUtils::convertEnvironmentAttribute);
        registry.registerIntProvider(Sum.class, IntProviderUtils::convertSum);
        registry.registerIntProvider(Product.class, IntProviderUtils::convertProduct);
        registry.registerIntProvider(Average.class, IntProviderUtils::convertAverage);
        registry.registerIntProvider(Minimum.class, IntProviderUtils::convertMinimum);
        registry.registerIntProvider(Maximum.class, IntProviderUtils::convertMaximum);
        registry.registerIntProvider(Difference.class, IntProviderUtils::convertDifference);
        registry.registerIntProvider(Negate.class, IntProviderUtils::convertNegate);
        registry.registerIntProvider(Absolute.class, IntProviderUtils::convertAbsolute);
        registry.registerIntProvider(FromFloat.class, IntProviderUtils::convertFromFloat);
        registry.registerIntProvider(ConditionalValue.class, IntProviderUtils::convertConditional);
        registry.registerIntProvider(NumberDispatcher.class, IntProviderUtils::convertDispatcher);
        registry.registerIntProvider(WeightedListValue.class, IntProviderUtils::convertWeightedList);
        registry.registerIntProvider(Modulus.class, IntProviderUtils::convertModulus);
        registry.registerIntProvider(FloorModulus.class, IntProviderUtils::convertFloorModulus);
        registry.registerIntProvider(Quotient.class, IntProviderUtils::convertQuotient);
        registry.registerIntProvider(FloorQuotient.class, IntProviderUtils::convertFloorQuotient);
        registry.registerIntProvider(Power.class, IntProviderUtils::convertPower);
    }

    @NotNull
    public static RangeValue convert(IServerUtils utils, Holder<ContextIntProvider> provider) {
        return utils.convertInt(utils, provider);
    }

    @NotNull
    public static RangeValue convertConstant(IServerUtils utils, ConstantValue provider) {
        return new RangeValue(provider.value());
    }

    @NotNull
    public static RangeValue convertUniform(IServerUtils utils, UniformGenerator provider) {
        return NumberProviderUtils.range(utils, provider, IntProviderUtils::convert);
    }

    @NotNull
    public static RangeValue convertBinomial(IServerUtils utils, BinomialDistributionGenerator provider) {
        return new RangeValue(0, utils.convertInt(utils, provider.n()).max());
    }

    @NotNull
    public static RangeValue convertScore(IServerUtils utils, ScoreboardValue provider) {
        return new RangeValue(true, false);
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
    public static RangeValue convertSum(IServerUtils utils, Sum provider) {
        return NumberProviderUtils.sum(utils, provider, IntProviderUtils::convert);
    }

    @NotNull
    public static RangeValue convertProduct(IServerUtils utils, Product provider) {
        return NumberProviderUtils.product(utils, provider, IntProviderUtils::convert);
    }

    @NotNull
    public static RangeValue convertAverage(IServerUtils utils, Average provider) {
        return NumberProviderUtils.average(utils, provider, IntProviderUtils::convert);
    }

    @NotNull
    public static RangeValue convertMinimum(IServerUtils utils, Minimum provider) {
        return NumberProviderUtils.minimum(utils, provider, IntProviderUtils::convert);
    }

    @NotNull
    public static RangeValue convertMaximum(IServerUtils utils, Maximum provider) {
        return NumberProviderUtils.maximum(utils, provider, IntProviderUtils::convert);
    }

    @NotNull
    public static RangeValue convertDifference(IServerUtils utils, Difference provider) {
        return NumberProviderUtils.difference(utils, provider, IntProviderUtils::convert);
    }

    @NotNull
    public static RangeValue convertNegate(IServerUtils utils, Negate provider) {
        return NumberProviderUtils.negate(utils, provider, IntProviderUtils::convert);
    }

    @NotNull
    public static RangeValue convertAbsolute(IServerUtils utils, Absolute provider) {
        return NumberProviderUtils.absolute(utils, provider, IntProviderUtils::convert);
    }

    @NotNull
    public static RangeValue convertFromFloat(IServerUtils utils, FromFloat provider) {
        return NumberProviderUtils.monotonic(utils, provider, FloatProviderUtils::convert, NumberProviderUtils::truncate);
    }

    @NotNull
    public static RangeValue convertConditional(IServerUtils utils, ConditionalValue provider) {
        return NumberProviderUtils.conditional(utils, provider, IntProviderUtils::convert);
    }

    @NotNull
    public static RangeValue convertDispatcher(IServerUtils utils, NumberDispatcher provider) {
        return NumberProviderUtils.dispatcher(utils, provider, IntProviderUtils::convert);
    }

    @NotNull
    public static RangeValue convertWeightedList(IServerUtils utils, WeightedListValue provider) {
        return NumberProviderUtils.distribution(utils, provider, IntProviderUtils::convert);
    }

    @NotNull
    public static RangeValue convertModulus(IServerUtils utils, Modulus provider) {
        return NumberProviderUtils.unknown();
    }

    @NotNull
    public static RangeValue convertFloorModulus(IServerUtils utils, FloorModulus provider) {
        return NumberProviderUtils.unknown();
    }

    @NotNull
    public static RangeValue convertQuotient(IServerUtils utils, Quotient provider) {
        return NumberProviderUtils.unknown();
    }

    @NotNull
    public static RangeValue convertFloorQuotient(IServerUtils utils, FloorQuotient provider) {
        return NumberProviderUtils.unknown();
    }

    @NotNull
    public static RangeValue convertPower(IServerUtils utils, Power provider) {
        return NumberProviderUtils.unknown();
    }
}
