package com.yanny.aci.configuration;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.yanny.aci.CommonLogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class ConfigCodecs {
    public static <A> MapCodec<A> field(String modId, Codec<A> codec, String name, Supplier<? extends A> fallback) {
        return new MapCodec<>() {
            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return Stream.of(ops.createString(name));
            }

            @Override
            public <T> DataResult<A> decode(DynamicOps<T> ops, MapLike<T> input) {
                T value = input.get(name);

                if (value == null || value.equals(ops.empty())) {
                    return DataResult.success(fallback.get());
                }

                DataResult<A> result = codec.parse(ops, value);

                return DataResult.success(result.result().orElseGet(() -> {
                    String error = result.error().map(DataResult.Error::message).orElse("");

                    CommonLogUtils.getLogger(modId).warn("Invalid value of '{}', using default: {}", name, error);
                    return fallback.get();
                }));
            }

            @Override
            public <T> RecordBuilder<T> encode(A input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                return prefix.add(name, codec.encodeStart(ops, input));
            }

            @Override
            public String toString() {
                return "ConfigField[" + name + ": " + codec + "]";
            }
        };
    }

    public static <E> Codec<List<E>> lenientList(String modId, Codec<E> element, String name) {
        return new Codec<>() {
            @Override
            public <T> DataResult<Pair<List<E>, T>> decode(DynamicOps<T> ops, T input) {
                return ops.getList(input).map((stream) -> {
                    List<E> list = new ArrayList<>();

                    stream.accept((value) -> {
                        DataResult<E> result = element.parse(ops, value);

                        result.result().ifPresentOrElse(list::add, () -> {
                            String error = result.error().map(DataResult.Error::message).orElse("");

                            CommonLogUtils.getLogger(modId).warn("Ignoring invalid entry in '{}': {}", name, error);
                        });
                    });
                    return Pair.of(list, input);
                });
            }

            @Override
            public <T> DataResult<T> encode(List<E> input, DynamicOps<T> ops, T prefix) {
                return element.listOf().encode(input, ops, prefix);
            }

            @Override
            public String toString() {
                return "LenientList[" + name + ": " + element + "]";
            }
        };
    }

    private ConfigCodecs() {}
}
