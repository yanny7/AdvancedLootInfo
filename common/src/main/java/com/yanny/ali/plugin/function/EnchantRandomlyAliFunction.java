package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinEnchantRandomlyFunction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class EnchantRandomlyAliFunction extends LootConditionalAliFunction {
    public final List<Enchantment> enchantments;

    public EnchantRandomlyAliFunction(IContext context, LootItemFunction function) {
        super(context, function);
        enchantments = ((MixinEnchantRandomlyFunction) function).getEnchantments();
    }

    public EnchantRandomlyAliFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        int count = buf.readInt();
        enchantments = new LinkedList<>();

        for (int i = 0; i < count; i++) {
            enchantments.add(BuiltInRegistries.ENCHANTMENT.getOptional(buf.readResourceLocation()).orElseThrow());
        }
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeInt(enchantments.size());
        enchantments.stream().map(BuiltInRegistries.ENCHANTMENT::getKey).filter(Objects::nonNull).forEach(buf::writeResourceLocation);
    }
}
