package com.yanny.advanced_loot_info.plugin.function;

import com.yanny.advanced_loot_info.api.IContext;
import com.yanny.advanced_loot_info.api.RangeValue;
import com.yanny.advanced_loot_info.mixin.MixinEnchantWithLevelsFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.advanced_loot_info.compatibility.EmiUtils.pad;
import static com.yanny.advanced_loot_info.compatibility.EmiUtils.translatable;

public class EnchantWithLevelsFunction extends LootConditionalFunction {
    public final RangeValue levels;
    public final boolean treasure;

    public EnchantWithLevelsFunction(IContext context, LootItemFunction function) {
        super(context, function);
        levels = RangeValue.convertNumber(context, ((MixinEnchantWithLevelsFunction) function).getLevels());
        treasure = ((MixinEnchantWithLevelsFunction) function).getTreasure();
    }

    public EnchantWithLevelsFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        levels = new RangeValue(buf);
        treasure = buf.readBoolean();
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        levels.encode(buf);
        buf.writeBoolean(treasure);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("emi.type.advanced_loot_info.function.enchant_with_levels")));
        components.add(pad(pad + 1, translatable("emi.property.function.enchant_with_levels.levels", levels)));
        components.add(pad(pad + 1, translatable("emi.property.function.enchant_with_levels.treasure", treasure)));

        return components;
    }
}
