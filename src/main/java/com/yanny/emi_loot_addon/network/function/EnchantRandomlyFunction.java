package com.yanny.emi_loot_addon.network.function;

import com.yanny.emi_loot_addon.mixin.MixinEnchantRandomlyFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static com.yanny.emi_loot_addon.compatibility.EmiUtils.pad;
import static com.yanny.emi_loot_addon.compatibility.EmiUtils.translatable;

public class EnchantRandomlyFunction extends LootConditionalFunction {
    public final List<ResourceLocation> enchantments;

    public EnchantRandomlyFunction(LootContext lootContext, LootItemFunction function) {
        super(lootContext, function);
        enchantments = ((MixinEnchantRandomlyFunction) function).getEnchantments().stream().map(ForgeRegistries.ENCHANTMENTS::getKey).collect(Collectors.toList());
    }

    public EnchantRandomlyFunction(FunctionType type, FriendlyByteBuf buf) {
        super(type, buf);
        int count = buf.readInt();
        enchantments = new LinkedList<>();

        for (int i = 0; i < count; i++) {
            enchantments.add(buf.readResourceLocation());
        }
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        super.encode(buf);
        buf.writeInt(enchantments.size());
        enchantments.forEach(buf::writeResourceLocation);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = super.getTooltip(pad);

        if (!enchantments.isEmpty()) {
            components.add(pad(pad + 1, translatable("emi.property.function.enchant_randomly.enchantments")));

            enchantments.forEach((enchantment) -> {
                components.add(pad(pad + 2, translatable(ForgeRegistries.ENCHANTMENTS.getValue(enchantment).getDescriptionId())));
            });
        }

        return components;
    }
}
