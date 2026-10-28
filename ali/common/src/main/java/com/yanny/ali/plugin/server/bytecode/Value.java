package com.yanny.ali.plugin.server.bytecode;

import com.yanny.aci.api.NumberExpr;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

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
        @Nullable
        TagKey<? extends ItemLike> tag;

        Stack(List<ItemStack> options, NumberExpr count, boolean randomTag) {
            this.options = new ArrayList<>(options);
            this.count = count;
            this.randomTag = randomTag;
        }

        @NotNull
        Stack copy() {
            Stack copy = new Stack(options.stream().map(ItemStack::copy).toList(), count, randomTag);

            copy.destination = destination;
            copy.decoration = decoration;
            copy.tag = tag;
            return copy;
        }

        @NotNull
        List<ItemStack> templates() {
            List<ItemStack> templates = new ArrayList<>();
            int each = count instanceof NumberExpr.Const(double value1) ? (int) value1 : 1;

            for (ItemStack option : options) {
                templates.add(option.copyWithCount(each));
            }

            return templates;
        }
    }

    record Pick(List<VillagerTrades.ItemListing> listings, Object pool, @Nullable Integer key) implements Value {
    }

    final class Draws implements Value {
        final Object pool;
        final List<Object> options;
        int left;

        Draws(Object pool, List<Object> options, int left) {
            this.pool = pool;
            this.options = options;
            this.left = left;
        }
    }

    final class Offers implements Value {
        final List<Entry> entries = new ArrayList<>();
        final Map<Object, Integer> picks = new IdentityHashMap<>();

        sealed interface Entry {
        }

        record Listings(VillagerTrades.ItemListing[] listings, Value count, @Nullable Integer key) implements Entry {
        }

        record Single(Value offer) implements Entry {
        }

        record Picked(List<Object> offers, Value count) implements Entry {
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
