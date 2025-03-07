package com.yanny.ali.plugin.condition;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.mixin.MixinBonusLevelTableCondition;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static com.yanny.ali.plugin.TooltipUtils.pad;
import static com.yanny.ali.plugin.TooltipUtils.translatable;

public class TableBonusCondition implements ILootCondition {
    public final ResourceLocation location;
    public final float[] values;

    public TableBonusCondition(IContext context, LootItemCondition condition) {
        location = BuiltInRegistries.ENCHANTMENT.getKey(((MixinBonusLevelTableCondition) condition).getEnchantment());
        values = ((MixinBonusLevelTableCondition) condition).getValues();
    }

    public TableBonusCondition(IContext context, FriendlyByteBuf buf) {
        location = buf.readResourceLocation();

        int count = buf.readInt();

        values = new float[count];

        for (int i = 0; i < count; i++) {
            values[i] = buf.readFloat();
        }
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        buf.writeResourceLocation(location);
        buf.writeInt(values.length);
        for (float value : values) {
            buf.writeFloat(value);
        }
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.table_bonus")));
        components.add(pad(pad + 1, translatable("ali.property.condition.table_bonus.location", location)));
        components.add(pad(pad + 1, translatable("ali.property.condition.table_bonus.values", Arrays.toString(values))));

        return components;
    }
}
