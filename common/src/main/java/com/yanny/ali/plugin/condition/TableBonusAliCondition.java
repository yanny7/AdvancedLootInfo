package com.yanny.ali.plugin.condition;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.mixin.MixinBonusLevelTableCondition;
import com.yanny.ali.plugin.ConditionTooltipUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;
import java.util.Objects;

public class TableBonusAliCondition implements ILootCondition {
    public final Enchantment enchantment;
    public final float[] values;

    public TableBonusAliCondition(IContext context, LootItemCondition condition) {
        enchantment = ((MixinBonusLevelTableCondition) condition).getEnchantment();
        values = ((MixinBonusLevelTableCondition) condition).getValues();
    }

    public TableBonusAliCondition(IContext context, FriendlyByteBuf buf) {
        enchantment = BuiltInRegistries.ENCHANTMENT.get(buf.readResourceLocation());

        int count = buf.readInt();

        values = new float[count];

        for (int i = 0; i < count; i++) {
            values[i] = buf.readFloat();
        }
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        buf.writeResourceLocation(Objects.requireNonNull(BuiltInRegistries.ENCHANTMENT.getKey(enchantment)));
        buf.writeInt(values.length);

        for (float value : values) {
            buf.writeFloat(value);
        }
    }

    @Override
    public List<Component> getTooltip(int pad) {
        return ConditionTooltipUtils.getTableBonusTooltip(pad, enchantment, values);
    }
}
