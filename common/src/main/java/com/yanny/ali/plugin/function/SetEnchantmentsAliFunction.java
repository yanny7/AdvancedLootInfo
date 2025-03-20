package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.mixin.MixinSetEnchantmentsFunction;
import com.yanny.ali.plugin.FunctionTooltipUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class SetEnchantmentsAliFunction extends LootConditionalAliFunction {
    public final Map<Enchantment, RangeValue> enchantments;
    public final boolean add;

    public SetEnchantmentsAliFunction(IContext context, LootItemFunction function) {
        super(context, function);
        enchantments = ((MixinSetEnchantmentsFunction) function).getEnchantments().entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                (e) -> context.utils().convertNumber(context, e.getValue())
        ));
        add = ((MixinSetEnchantmentsFunction) function).getAdd();
    }

    public SetEnchantmentsAliFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        int count = buf.readInt();

        enchantments = new HashMap<>();

        for (int i = 0; i < count; i++) {
            enchantments.put(BuiltInRegistries.ENCHANTMENT.get(buf.readResourceLocation()), new RangeValue(buf));
        }

        add = buf.readBoolean();
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeInt(enchantments.size());
        enchantments.forEach((location, levels) -> {
            buf.writeResourceLocation(Objects.requireNonNull(BuiltInRegistries.ENCHANTMENT.getKey(location)));
            levels.encode(buf);
        });
        buf.writeBoolean(add);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        return FunctionTooltipUtils.getSetEnchantmentsTooltip(pad, enchantments, add);
    }
}
