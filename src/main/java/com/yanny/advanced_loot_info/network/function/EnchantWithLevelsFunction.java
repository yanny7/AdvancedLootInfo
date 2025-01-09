package com.yanny.advanced_loot_info.network.function;

import com.yanny.advanced_loot_info.mixin.MixinEnchantWithLevelsFunction;
import com.yanny.advanced_loot_info.network.RangeValue;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.List;

import static com.yanny.advanced_loot_info.compatibility.EmiUtils.pad;
import static com.yanny.advanced_loot_info.compatibility.EmiUtils.translatable;

public class EnchantWithLevelsFunction extends LootConditionalFunction {
    public final RangeValue levels;
    public final boolean treasure;

    public EnchantWithLevelsFunction(LootContext lootContext, LootItemFunction function) {
        super(lootContext, function);
        levels = RangeValue.of(lootContext, ((MixinEnchantWithLevelsFunction) function).getLevels());
        treasure = ((MixinEnchantWithLevelsFunction) function).getTreasure();
    }

    public EnchantWithLevelsFunction(FunctionType type, FriendlyByteBuf buf) {
        super(type, buf);
        levels = new RangeValue(buf);
        treasure = buf.readBoolean();
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        super.encode(buf);
        levels.encode(buf);
        buf.writeBoolean(treasure);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = super.getTooltip(pad);

        components.add(pad(pad + 1, translatable("emi.property.function.enchant_with_levels.levels", levels)));
        components.add(pad(pad + 1, translatable("emi.property.function.enchant_with_levels.treasure", treasure)));

        return components;
    }
}
