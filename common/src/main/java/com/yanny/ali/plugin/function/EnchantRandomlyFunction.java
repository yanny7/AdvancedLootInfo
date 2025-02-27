package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinEnchantRandomlyFunction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static com.yanny.ali.plugin.TooltipUtils.pad;
import static com.yanny.ali.plugin.TooltipUtils.translatable;

public class EnchantRandomlyFunction extends LootConditionalFunction {
    public final List<ResourceLocation> enchantments;

    public EnchantRandomlyFunction(IContext context, LootItemFunction function) {
        super(context, function);
        enchantments = ((MixinEnchantRandomlyFunction) function).getEnchantments().stream().flatMap((t) -> t.stream().map((e) -> BuiltInRegistries.ENCHANTMENT.getKey(e.value()))).collect(Collectors.toList());
    }

    public EnchantRandomlyFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        int count = buf.readInt();
        enchantments = new LinkedList<>();

        for (int i = 0; i < count; i++) {
            enchantments.add(buf.readResourceLocation());
        }
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeInt(enchantments.size());
        enchantments.forEach(buf::writeResourceLocation);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.enchant_randomly")));

        if (!enchantments.isEmpty()) {
            components.add(pad(pad + 1, translatable("ali.property.function.enchant_randomly.enchantments")));

            enchantments.forEach((enchantment) -> {
                components.add(pad(pad + 2, translatable(BuiltInRegistries.ENCHANTMENT.get(enchantment).getDescriptionId())));
            });
        }

        return components;
    }
}
