package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.mixin.MixinEnchantWithLevelsFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.ali.plugin.TooltipUtils.pad;
import static com.yanny.ali.plugin.TooltipUtils.translatable;

public class EnchantWithLevelsFunction extends LootConditionalFunction {
    public final RangeValue levels;
    public final boolean treasure;

    public EnchantWithLevelsFunction(IContext context, LootItemFunction function) {
        super(context, function);
        levels = context.utils().convertNumber(context, ((MixinEnchantWithLevelsFunction) function).getLevels());
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

        components.add(pad(pad, translatable("ali.type.function.enchant_with_levels")));
        components.add(pad(pad + 1, translatable("ali.property.function.enchant_with_levels.levels", levels)));
        components.add(pad(pad + 1, translatable("ali.property.function.enchant_with_levels.treasure", treasure)));

        return components;
    }
}
