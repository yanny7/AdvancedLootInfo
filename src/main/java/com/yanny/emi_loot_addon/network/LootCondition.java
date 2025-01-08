package com.yanny.emi_loot_addon.network;

import com.yanny.emi_loot_addon.network.condition.ConditionType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public abstract class LootCondition {

    public final ConditionType type;

    public LootCondition(ConditionType type) {
        this.type = type;
    }

    public abstract void encode(FriendlyByteBuf buf);

    @NotNull
    public static List<LootCondition> of(LootContext lootContext, LootItemCondition[] conditions) {
        List<LootCondition> list = new LinkedList<>();

        for (LootItemCondition condition : conditions) {
            list.add(LootUtils.CONDITION_MAP.getOrDefault(ConditionType.of(condition.getType()), LootUtils.CONDITION_MAP.get(ConditionType.UNKNOWN)).apply(lootContext, condition));
        }

        return list;
    }

    @NotNull
    public static List<LootCondition> decode(FriendlyByteBuf buf) {
        int count = buf.readInt();
        List<LootCondition> list = new LinkedList<>();

        for (int i = 0; i < count; i++) {
            ConditionType type = buf.readEnum(ConditionType.class);
            list.add(LootUtils.CONDITION_DECODE_MAP.get(type).apply(type, buf));
        }

        return list;
    }

    public static void encode(FriendlyByteBuf buf, List<LootCondition> list) {
        buf.writeInt(list.size());
        list.forEach((f) -> {
            buf.writeEnum(f.type);
            f.encode(buf);
        });
    }
}
