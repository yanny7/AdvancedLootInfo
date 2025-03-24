package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.mixin.MixinEnchantWithLevelsFunction;
import com.yanny.ali.plugin.FunctionTooltipUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.List;

public class EnchantWithLevelsAliFunction extends LootConditionalAliFunction {
    public final RangeValue levels;
    public final boolean treasure;

    public EnchantWithLevelsAliFunction(IContext context, LootItemFunction function) {
        super(context, function);
        levels = context.utils().convertNumber(context, ((MixinEnchantWithLevelsFunction) function).getLevels());
        treasure = ((MixinEnchantWithLevelsFunction) function).getTreasure();
    }

    public EnchantWithLevelsAliFunction(IContext context, FriendlyByteBuf buf) {
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
        return FunctionTooltipUtils.getEnchantWithLevelsTooltip(pad, levels, treasure);
    }
}
