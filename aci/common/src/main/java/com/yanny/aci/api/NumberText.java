package com.yanny.aci.api;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public sealed interface NumberText {
    record Key(String key, List<NumberText> args) implements NumberText {
        public Key {
            args = List.copyOf(args);
        }
    }

    record Num(double value) implements NumberText {
    }

    record Str(String text) implements NumberText {
    }

    record Seq(List<NumberText> parts) implements NumberText {
        public Seq {
            parts = List.copyOf(parts);
        }
    }

    @NotNull
    static NumberText key(String key, NumberText... args) {
        return new Key(key, List.of(args));
    }

    @NotNull
    static NumberText num(double value) {
        return new Num(value);
    }

    @NotNull
    static NumberText str(String text) {
        return new Str(text);
    }

    @NotNull
    static NumberText seq(NumberText... parts) {
        return new Seq(List.of(parts));
    }

    @NotNull
    static NumberText seq(List<NumberText> parts) {
        return parts.size() == 1 ? parts.get(0) : new Seq(parts);
    }

    @NotNull
    static String formatNumber(double value, Locale locale) {
        if (Double.isInfinite(value)) {
            return value > 0 ? "∞" : "−∞";
        }

        DecimalFormat format = new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(locale));

        format.setRoundingMode(RoundingMode.HALF_UP);

        String text = format.format(value);

        if (text.equals("-0")) {
            return "0";
        }

        return text.startsWith("-") ? "−" + text.substring(1) : text;
    }

    default void encode(FriendlyByteBuf buf) {
        if (this instanceof Key k) {
            buf.writeByte(0);
            buf.writeUtf(k.key);
            encodeList(buf, k.args);
        } else if (this instanceof Num n) {
            buf.writeByte(1);
            buf.writeDouble(n.value);
        } else if (this instanceof Str s) {
            buf.writeByte(2);
            buf.writeUtf(s.text);
        } else if (this instanceof Seq s) {
            buf.writeByte(3);
            encodeList(buf, s.parts);
        }
    }

    @NotNull
    default MutableComponent toComponent(Locale locale) {
        if (this instanceof Key k) {
            return Component.translatable(k.key, k.args.stream().map((a) -> a.toComponent(locale)).toArray());
        } else if (this instanceof Num n) {
            return Component.literal(formatNumber(n.value, locale));
        } else if (this instanceof Str s) {
            return Component.literal(s.text);
        } else if (this instanceof Seq s) {
            MutableComponent component = Component.empty();

            s.parts.forEach((p) -> component.append(p.toComponent(locale)));
            return component;
        }

        throw new IllegalStateException("Unhandled number text " + this);
    }

    @NotNull
    static NumberText decode(FriendlyByteBuf buf) {
        byte tag = buf.readByte();

        return switch (tag) {
            case 0 -> new Key(buf.readUtf(), decodeList(buf));
            case 1 -> new Num(buf.readDouble());
            case 2 -> new Str(buf.readUtf());
            case 3 -> new Seq(decodeList(buf));
            default -> throw new IllegalStateException("Unknown number text tag " + tag);
        };
    }

    static void encodeList(FriendlyByteBuf buf, List<NumberText> list) {
        buf.writeVarInt(list.size());
        list.forEach((t) -> t.encode(buf));
    }

    @NotNull
    static List<NumberText> decodeList(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<NumberText> list = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            list.add(decode(buf));
        }

        return list;
    }
}
