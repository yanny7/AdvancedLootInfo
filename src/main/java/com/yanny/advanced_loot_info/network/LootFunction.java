package com.yanny.advanced_loot_info.network;

import com.yanny.advanced_loot_info.network.function.FunctionType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.advanced_loot_info.compatibility.EmiUtils.pad;
import static com.yanny.advanced_loot_info.compatibility.EmiUtils.translatableType;

public abstract class LootFunction {

    public final FunctionType type;

    public LootFunction(FunctionType type) {
        this.type = type;
    }

    public abstract void encode(FriendlyByteBuf buf);

    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatableType("emi.type.advanced_loot_info.function", type)));

        return components;
    }

    @NotNull
    public static List<LootFunction> of(LootContext lootContext, LootItemFunction[] functions) {
        List<LootFunction> list = new LinkedList<>();

        for (LootItemFunction function : functions) {
            list.add(LootUtils.FUNCTION_MAP.getOrDefault(FunctionType.of(function.getType()), LootUtils.FUNCTION_MAP.get(FunctionType.UNKNOWN)).apply(lootContext, function));
        }

        return list;
    }

    @NotNull
    public static List<LootFunction> decode(FriendlyByteBuf buf) {
        int count = buf.readInt();
        List<LootFunction> list = new LinkedList<>();

        for (int i = 0; i < count; i++) {
            FunctionType type = buf.readEnum(FunctionType.class);
            list.add(LootUtils.FUNCTION_DECODE_MAP.get(type).apply(type, buf));
        }

        return list;
    }

    public static void encode(FriendlyByteBuf buf, List<LootFunction> list) {
        buf.writeInt(list.size());
        list.forEach((f) -> {
            buf.writeEnum(f.type);
            f.encode(buf);
        });
    }
}
