package com.yanny.ali.plugin.server.bytecode;

import com.yanny.aci.api.NumberExpr;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

sealed interface Value {
    Unknown UNKNOWN = new Unknown(false, null, null, null);

    default boolean isWide() {
        return false;
    }

    record Known(@Nullable Object value) implements Value {
        @Override
        public boolean isWide() {
            return value instanceof Long || value instanceof Double;
        }
    }

    record Num(NumberExpr expr, Kind kind) implements Value {
        @Override
        public boolean isWide() {
            return kind == Kind.LONG || kind == Kind.DOUBLE;
        }
    }

    record Choice(List<Object> options, @Nullable Object draw) implements Value {
        public Choice {
            options = Collections.unmodifiableList(new ArrayList<>(options));
        }

        Choice(List<Object> options) {
            this(options, null);
        }
    }

    record Cmp(NumberExpr left, NumberExpr right) implements Value {
    }

    record Rng(boolean legacy) implements Value {
    }

    record Offer(Value costA, Value costB, Value result, int maxUses, int xp, float priceMultiplier) implements Value {
    }

    record Closure(Class<?> iface, String samName, String samDesc, Handle impl, List<Value> captured, ClassLoader loader) implements Value {
        public Closure {
            captured = List.copyOf(captured);
        }
    }

    record Unknown(boolean wide, @Nullable String type, @Nullable Object found, @Nullable Origin origin) implements Value {
        static Unknown of(Type type, @Nullable Origin origin) {
            return new Unknown(type.getSize() == 2, type.getSort() == Type.OBJECT ? type.getInternalName() : null, null, origin);
        }

        Unknown withType(String type) {
            return new Unknown(wide, type, found, origin);
        }

        @Override
        public boolean isWide() {
            return wide;
        }
    }

    final class Stack implements Value {
        final List<ItemStack> options;
        NumberExpr count;
        boolean randomTag;
        @Nullable
        Object destination;
        @Nullable
        Object decoration;

        Stack(List<ItemStack> options, NumberExpr count, boolean randomTag) {
            this.options = new ArrayList<>(options);
            this.count = count;
            this.randomTag = randomTag;
        }

        Stack copy() {
            Stack copy = new Stack(options.stream().map(ItemStack::copy).toList(), count, randomTag);

            copy.destination = destination;
            copy.decoration = decoration;
            return copy;
        }
    }

    final class Uninit implements Value {
        final String type;

        Uninit(String type) {
            this.type = type;
        }
    }

    enum Kind {
        INT,
        LONG,
        FLOAT,
        DOUBLE,
    }
}
