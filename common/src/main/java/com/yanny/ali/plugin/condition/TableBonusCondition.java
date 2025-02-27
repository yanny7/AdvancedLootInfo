package com.yanny.ali.plugin.condition;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.LinkedList;
import java.util.List;

public class TableBonusCondition implements ILootCondition {
    public final ResourceLocation location;
    public final List<Float> values;

    public TableBonusCondition(IContext context, LootItemCondition condition) {
        location = BuiltInRegistries.ENCHANTMENT.getKey(((BonusLevelTableCondition) condition).enchantment().value());
        values = ((BonusLevelTableCondition) condition).values();
    }

    public TableBonusCondition(IContext context, FriendlyByteBuf buf) {
        location = buf.readResourceLocation();

        int count = buf.readInt();

        values = new LinkedList<>();

        for (int i = 0; i < count; i++) {
            values.add(buf.readFloat());
        }
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        buf.writeResourceLocation(location);
        buf.writeInt(values.size());

        for (float value : values) {
            buf.writeFloat(value);
        }
    }

    @Override
    public List<Component> getTooltip(int pad) {
        return List.of();
    }
}
