package com.yanny.ali.plugin.condition;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.plugin.ConditionTooltipUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.LinkedList;
import java.util.List;

public class TableBonusAliCondition implements ILootCondition {
    public final Holder<Enchantment> enchantment;
    public final List<Float> values;

    public TableBonusAliCondition(IContext context, LootItemCondition condition) {
        enchantment = ((BonusLevelTableCondition) condition).enchantment();
        values = ((BonusLevelTableCondition) condition).values();
    }

    public TableBonusAliCondition(IContext context, FriendlyByteBuf buf) {
        enchantment = BuiltInRegistries.ENCHANTMENT.getHolderOrThrow(buf.readResourceKey(Registries.ENCHANTMENT));

        int count = buf.readInt();

        values = new LinkedList<>();

        for (int i = 0; i < count; i++) {
            values.add(buf.readFloat());
        }
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        buf.writeResourceKey(enchantment.unwrap().orThrow());
        buf.writeInt(values.size());

        for (float value : values) {
            buf.writeFloat(value);
        }
    }

    @Override
    public List<Component> getTooltip(int pad) {
        return ConditionTooltipUtils.getTableBonusTooltip(pad, enchantment, values);
    }
}
