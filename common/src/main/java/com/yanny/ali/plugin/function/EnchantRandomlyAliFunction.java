package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinEnchantRandomlyFunction;
import com.yanny.ali.plugin.FunctionTooltipUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class EnchantRandomlyAliFunction extends LootConditionalAliFunction {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public final Optional<HolderSet<Enchantment>> enchantments;

    public EnchantRandomlyAliFunction(IContext context, LootItemFunction function) {
        super(context, function);
        enchantments = ((MixinEnchantRandomlyFunction) function).getEnchantments();
    }

    public EnchantRandomlyAliFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        enchantments = buf.readOptional((b) -> {
            int count = buf.readInt();
            List<Holder<Enchantment>> holders = new LinkedList<>();

            for (int i = 0; i < count; i++) {
                holders.add(BuiltInRegistries.ENCHANTMENT.getHolderOrThrow(buf.readResourceKey(Registries.ENCHANTMENT)));
            }

            return HolderSet.direct(holders);
        });
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeOptional(enchantments, (b, holderSet) -> {
            b.writeInt(holderSet.size());
            holderSet.forEach((holder) -> b.writeResourceKey(holder.unwrap().orThrow()));
        });
    }

    @Override
    public List<Component> getTooltip(int pad) {
        return FunctionTooltipUtils.getEnchantRandomlyTooltip(pad, enchantments);
    }
}
