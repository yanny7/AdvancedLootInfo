package com.yanny.advanced_loot_info.plugin.condition;

import com.yanny.advanced_loot_info.api.ILootCondition;
import com.yanny.advanced_loot_info.mixin.MixinBonusLevelTableCondition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class TableBonusCondition implements ILootCondition {
    public final ResourceLocation location;
    public final float[] values;

    public TableBonusCondition(LootContext lootContext, LootItemCondition condition) {
        location = ForgeRegistries.ENCHANTMENTS.getKey(((MixinBonusLevelTableCondition) condition).getEnchantment());
        values = ((MixinBonusLevelTableCondition) condition).getValues();
    }

    public TableBonusCondition(FriendlyByteBuf buf) {
        location = buf.readResourceLocation();

        int count = buf.readInt();

        values = new float[count];

        for (int i = 0; i < count; i++) {
            values[i] = buf.readFloat();
        }
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(location);
        buf.writeInt(values.length);
        for (float value : values) {
            buf.writeFloat(value);
        }
    }

    @Override
    public List<Component> getTooltip(int pad) {
        return List.of();
    }
}
