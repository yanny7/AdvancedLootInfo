package com.yanny.ali.plugin.server;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.api.NumberFunctions;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import net.minecraft.core.Holder;
import net.minecraft.world.level.storage.loot.providers.number.ints.*;
import org.jetbrains.annotations.NotNull;

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
    public static NumberExpr convert(IServerUtils utils, Holder<ContextIntProvider> provider) {
        return utils.convertContextInt(utils, provider);
    }

    @NotNull
    public static NumberExpr convertConstant(IServerUtils utils, ConstantValue provider) {
        return NumberExpr.constant(provider.value());
    }

    @NotNull
    public static NumberExpr convertUniform(IServerUtils utils, UniformGenerator provider) {
        return NumberProviderUtils.range(utils, provider, IntProviderUtils::convert, NumberExpr::uniformInt);
    }

    @NotNull
    public static NumberExpr convertBinomial(IServerUtils utils, BinomialDistributionGenerator provider) {
        return NumberExpr.binomial(convert(utils, provider.n()), FloatProviderUtils.convert(utils, provider.p()));
    }

    @NotNull
    public static NumberExpr convertScore(IServerUtils utils, ScoreboardValue provider) {
        return NumberProviderUtils.withFallback(utils, NumberProviderUtils.score(provider.target(), provider.score()), Lang.Numbers.SCORE_EXISTS, convert(utils, provider.fallback()));
    }

    @NotNull
    public static NumberExpr convertStorage(IServerUtils utils, StorageValue provider) {
        NumberExpr value = NumberExpr.fn(NumberFunctions.TRUNC, NumberProviderUtils.storage(provider.access()));

        return NumberProviderUtils.withFallback(utils, value, Lang.Numbers.STORAGE_VALUE_EXISTS, convert(utils, provider.fallback()));
    }

    @NotNull
    public static NumberExpr convertEnvironmentAttribute(IServerUtils utils, EnvironmentAttributeValue provider) {
        return NumberProviderUtils.environmentAttribute(provider.attribute());
    }

    @NotNull
    public static NumberExpr convertSum(IServerUtils utils, Sum provider) {
        return NumberProviderUtils.aggregate(utils, provider, IntProviderUtils::convert, NumberExpr::add);
    }

    @NotNull
    public static NumberExpr convertProduct(IServerUtils utils, Product provider) {
        return NumberProviderUtils.aggregate(utils, provider, IntProviderUtils::convert, NumberExpr::mul);
    }

    @NotNull
    public static NumberExpr convertAverage(IServerUtils utils, Average provider) {
        return NumberExpr.fn(NumberFunctions.TRUNC, NumberProviderUtils.aggregate(utils, provider, IntProviderUtils::convert, (args) -> NumberExpr.fn(NumberFunctions.AVG, args)));
    }

    @NotNull
    public static NumberExpr convertMinimum(IServerUtils utils, Minimum provider) {
        return NumberProviderUtils.aggregate(utils, provider, IntProviderUtils::convert, NumberExpr::min);
    }

    @NotNull
    public static NumberExpr convertMaximum(IServerUtils utils, Maximum provider) {
        return NumberProviderUtils.aggregate(utils, provider, IntProviderUtils::convert, NumberExpr::max);
    }

    @NotNull
    public static NumberExpr convertDifference(IServerUtils utils, Difference provider) {
        return NumberProviderUtils.binary(utils, provider, IntProviderUtils::convert, NumberExpr::sub);
    }

    @NotNull
    public static NumberExpr convertNegate(IServerUtils utils, Negate provider) {
        return NumberProviderUtils.unary(utils, provider, IntProviderUtils::convert, (value) -> NumberExpr.fn(NumberFunctions.NEG, value));
    }

    @NotNull
    public static NumberExpr convertAbsolute(IServerUtils utils, Absolute provider) {
        return NumberProviderUtils.unary(utils, provider, IntProviderUtils::convert, (value) -> NumberExpr.fn(NumberFunctions.ABS, value));
    }

    @NotNull
    public static NumberExpr convertFromFloat(IServerUtils utils, FromFloat provider) {
        return NumberProviderUtils.unary(utils, provider, FloatProviderUtils::convert, (value) -> NumberExpr.fn(NumberFunctions.TRUNC, value));
    }

    @NotNull
    public static NumberExpr convertConditional(IServerUtils utils, ConditionalValue provider) {
        return NumberProviderUtils.conditional(utils, provider, IntProviderUtils::convert);
    }

    @NotNull
    public static NumberExpr convertDispatcher(IServerUtils utils, NumberDispatcher provider) {
        return NumberProviderUtils.dispatcher(utils, provider, IntProviderUtils::convert);
    }

    @NotNull
    public static NumberExpr convertWeightedList(IServerUtils utils, WeightedListValue provider) {
        return NumberProviderUtils.distribution(utils, provider, IntProviderUtils::convert);
    }

    @NotNull
    public static NumberExpr convertModulus(IServerUtils utils, Modulus provider) {
        return NumberProviderUtils.binary(utils, provider, IntProviderUtils::convert, (a, b) -> NumberExpr.fn(NumberFunctions.MOD, a, b));
    }

    @NotNull
    public static NumberExpr convertFloorModulus(IServerUtils utils, FloorModulus provider) {
        return NumberProviderUtils.binary(utils, provider, IntProviderUtils::convert, (a, b) -> NumberExpr.fn(NumberFunctions.FLOOR_MOD, a, b));
    }

    @NotNull
    public static NumberExpr convertQuotient(IServerUtils utils, Quotient provider) {
        return NumberProviderUtils.binary(utils, provider, IntProviderUtils::convert, (a, b) -> NumberExpr.fn(NumberFunctions.TRUNC, NumberExpr.div(a, b)));
    }

    @NotNull
    public static NumberExpr convertFloorQuotient(IServerUtils utils, FloorQuotient provider) {
        return NumberProviderUtils.binary(utils, provider, IntProviderUtils::convert, (a, b) -> NumberExpr.fn(NumberFunctions.FLOOR_DIV, a, b));
    }

    @NotNull
    public static NumberExpr convertPower(IServerUtils utils, Power provider) {
        return NumberProviderUtils.power(utils, provider, IntProviderUtils::convert);
    }
}
