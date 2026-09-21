package com.yanny.ali.plugin.server;

import com.yanny.aci.api.RangeValue;
import com.yanny.ali.api.IServerUtils;
import net.minecraft.core.Holder;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.storage.loot.providers.number.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

public class NumberProviderUtils {
    @NotNull
    public static RangeValue unknown() {
        return new RangeValue(false, true);
    }

    public static float truncate(float value) {
        return (int) value;
    }

    @NotNull
    public static <T> RangeValue range(IServerUtils utils, RangeProvider<?> provider, BiFunction<IServerUtils, Holder<T>, RangeValue> converter) {
        //noinspection unchecked
        RangeValue min = converter.apply(utils, (Holder<T>) provider.min());
        //noinspection unchecked
        RangeValue max = converter.apply(utils, (Holder<T>) provider.max());

        return withFlags(min.min(), max.max(), min, max);
    }

    @NotNull
    public static <T> RangeValue sum(IServerUtils utils, AggregateProvider<?> provider, BiFunction<IServerUtils, Holder<T>, RangeValue> converter) {
        List<RangeValue> values = convertAll(utils, provider, converter);
        RangeValue result = new RangeValue(0);

        for (RangeValue value : values) {
            result = result.add(value);
        }

        return result;
    }

    @NotNull
    public static <T> RangeValue product(IServerUtils utils, AggregateProvider<?> provider, BiFunction<IServerUtils, Holder<T>, RangeValue> converter) {
        List<RangeValue> values = convertAll(utils, provider, converter);
        RangeValue result = new RangeValue(1);

        for (RangeValue value : values) {
            result = result.multiply(value);
        }

        return result;
    }

    @NotNull
    public static <T> RangeValue average(IServerUtils utils, AggregateProvider<?> provider, BiFunction<IServerUtils, Holder<T>, RangeValue> converter) {
        int size = provider.inputs().size();

        return size > 0 ? sum(utils, provider, converter).multiply(1f / size) : unknown();
    }

    @NotNull
    public static <T> RangeValue minimum(IServerUtils utils, AggregateProvider<?> provider, BiFunction<IServerUtils, Holder<T>, RangeValue> converter) {
        List<RangeValue> values = convertAll(utils, provider, converter);

        if (values.isEmpty()) {
            return unknown();
        }

        float min = values.stream().map(RangeValue::min).reduce(Float.MAX_VALUE, Math::min);
        float max = values.stream().map(RangeValue::max).reduce(Float.MAX_VALUE, Math::min);
        return withFlags(min, max, values.toArray(RangeValue[]::new));
    }

    @NotNull
    public static <T> RangeValue maximum(IServerUtils utils, AggregateProvider<?> provider, BiFunction<IServerUtils, Holder<T>, RangeValue> converter) {
        List<RangeValue> values = convertAll(utils, provider, converter);

        if (values.isEmpty()) {
            return unknown();
        }

        float min = values.stream().map(RangeValue::min).reduce(-Float.MAX_VALUE, Math::max);
        float max = values.stream().map(RangeValue::max).reduce(-Float.MAX_VALUE, Math::max);
        return withFlags(min, max, values.toArray(RangeValue[]::new));
    }

    @NotNull
    public static <T> RangeValue difference(IServerUtils utils, BinaryProvider<?> provider, BiFunction<IServerUtils, Holder<T>, RangeValue> converter) {
        //noinspection unchecked
        RangeValue left = converter.apply(utils, (Holder<T>) provider.left());
        //noinspection unchecked
        RangeValue right = converter.apply(utils, (Holder<T>) provider.right());

        return left.add(right.multiply(-1));
    }

    @NotNull
    public static <T> RangeValue negate(IServerUtils utils, UnaryProvider<?> provider, BiFunction<IServerUtils, Holder<T>, RangeValue> converter) {
        //noinspection unchecked
        return converter.apply(utils, (Holder<T>) provider.input()).multiply(-1);
    }

    @NotNull
    public static <T> RangeValue absolute(IServerUtils utils, UnaryProvider<?> provider, BiFunction<IServerUtils, Holder<T>, RangeValue> converter) {
        //noinspection unchecked
        RangeValue value = converter.apply(utils, (Holder<T>) provider.input());

        if (value.min() >= 0) {
            return value;
        } else if (value.max() <= 0) {
            return value.multiply(-1);
        } else {
            return withFlags(0, Math.max(-value.min(), value.max()), value);
        }
    }

    @NotNull
    public static <T> RangeValue monotonic(IServerUtils utils, UnaryProvider<?> provider, BiFunction<IServerUtils, Holder<T>, RangeValue> converter, UnaryOperator<Float> operator) {
        //noinspection unchecked
        RangeValue value = converter.apply(utils, (Holder<T>) provider.input());

        return withFlags(operator.apply(value.min()), operator.apply(value.max()), value);
    }

    @NotNull
    public static <T> RangeValue conditional(IServerUtils utils, ConditionalProvider<?> provider, BiFunction<IServerUtils, Holder<T>, RangeValue> converter) {
        //noinspection unchecked
        RangeValue onTrue = converter.apply(utils, (Holder<T>) provider.onTrue());
        //noinspection unchecked
        return onTrue.union(converter.apply(utils, (Holder<T>) provider.onFalse()));
    }

    @NotNull
    public static <T> RangeValue dispatcher(IServerUtils utils, DispatcherProvider<?> provider, BiFunction<IServerUtils, Holder<T>, RangeValue> converter) {
        //noinspection unchecked
        RangeValue result = converter.apply(utils, (Holder<T>) provider.defaultValue());

        for (DispatcherProvider.Case<?> aCase : provider.cases()) {
            //noinspection unchecked
            result = result.union(converter.apply(utils, (Holder<T>) aCase.value()));
        }

        return result;
    }

    @NotNull
    public static <T> RangeValue distribution(IServerUtils utils, DistributionProvider<?> provider, BiFunction<IServerUtils, Holder<T>, RangeValue> converter) {
        //noinspection unchecked
        List<Weighted<Holder<T>>> entries = ((WeightedList<Holder<T>>) (WeightedList<?>) provider.distribution()).unwrap();

        if (entries.isEmpty()) {
            return unknown();
        }

        RangeValue result = converter.apply(utils, entries.getFirst().value());

        for (Weighted<Holder<T>> entry : entries) {
            result = result.union(converter.apply(utils, entry.value()));
        }

        return result;
    }

    @NotNull
    private static <T> List<RangeValue> convertAll(IServerUtils utils, AggregateProvider<?> provider, BiFunction<IServerUtils, Holder<T>, RangeValue> converter) {
        //noinspection unchecked
        return provider.inputs().stream().map((h) -> converter.apply(utils, (Holder<T>) h)).toList();
    }

    @NotNull
    private static RangeValue withFlags(float min, float max, RangeValue... sources) {
        RangeValue result = new RangeValue(min, max);

        for (RangeValue source : sources) {
            result = result.add(source.multiply(0));
        }

        return result;
    }
}
