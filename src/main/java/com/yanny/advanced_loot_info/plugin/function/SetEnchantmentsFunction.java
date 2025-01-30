package com.yanny.advanced_loot_info.plugin.function;

import com.yanny.advanced_loot_info.api.IContext;
import com.yanny.advanced_loot_info.api.RangeValue;
import com.yanny.advanced_loot_info.mixin.MixinSetEnchantmentsFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.yanny.advanced_loot_info.compatibility.EmiUtils.pad;
import static com.yanny.advanced_loot_info.compatibility.EmiUtils.translatable;

public class SetEnchantmentsFunction extends LootConditionalFunction {
    public final Map<ResourceLocation, RangeValue> enchantments;
    public final boolean add;

    public SetEnchantmentsFunction(IContext context, LootItemFunction function) {
        super(context, function);
        enchantments = ((MixinSetEnchantmentsFunction) function).getEnchantments().entrySet().stream().collect(Collectors.toMap(
                (e) -> ForgeRegistries.ENCHANTMENTS.getKey(e.getKey()),
                (e) -> context.registry().convertNumber(context, e.getValue())
        ));
        add = ((MixinSetEnchantmentsFunction) function).getAdd();
    }

    public SetEnchantmentsFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        int count = buf.readInt();

        enchantments = new HashMap<>();

        for (int i = 0; i < count; i++) {
            enchantments.put(buf.readResourceLocation(), new RangeValue(buf));
        }

        add = buf.readBoolean();
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeInt(enchantments.size());
        enchantments.forEach((location, levels) -> {
            buf.writeResourceLocation(location);
            levels.encode(buf);
        });
        buf.writeBoolean(add);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("emi.type.advanced_loot_info.function.set_enchantments")));
        components.add(pad(pad + 1, translatable("emi.property.function.set_enchantments.enchantments")));
        enchantments.forEach((enchantment, level) -> components.add(pad(pad + 2, translatable(
                "emi.property.function.set_enchantments.enchantment",
                translatable(ForgeRegistries.ENCHANTMENTS.getValue(enchantment).getDescriptionId()),
                level
        ))));
        components.add(pad(pad + 1, translatable("emi.property.function.set_enchantments.add", add)));

        return components;
    }
}
