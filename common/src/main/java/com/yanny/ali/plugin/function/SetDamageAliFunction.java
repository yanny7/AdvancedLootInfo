package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.mixin.MixinSetItemDamageFunction;
import com.yanny.ali.plugin.FunctionTooltipUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.List;

public class SetDamageAliFunction extends LootConditionalAliFunction {
    public final RangeValue damage;
    public final boolean add;

    public SetDamageAliFunction(IContext context, LootItemFunction function) {
        super(context, function);
        damage = context.utils().convertNumber(context, ((MixinSetItemDamageFunction) function).getDamage());
        add = ((MixinSetItemDamageFunction) function).getAdd();
    }

    public SetDamageAliFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        damage = new RangeValue(buf);
        add = buf.readBoolean();
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        damage.encode(buf);
        buf.writeBoolean(add);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        return FunctionTooltipUtils.getSetDamageTooltip(pad, damage, add);
    }
}
