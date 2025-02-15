package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.mixin.MixinSetItemDamageFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.ali.plugin.TooltipUtils.pad;
import static com.yanny.ali.plugin.TooltipUtils.translatable;

public class SetDamageFunction extends LootConditionalFunction {
    public final RangeValue damage;
    public final boolean add;

    public SetDamageFunction(IContext context, LootItemFunction function) {
        super(context, function);
        damage = context.utils().convertNumber(context, ((MixinSetItemDamageFunction) function).getDamage());
        add = ((MixinSetItemDamageFunction) function).getAdd();
    }

    public SetDamageFunction(IContext context, FriendlyByteBuf buf) {
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
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_damage")));
        components.add(pad(pad + 1, translatable("ali.property.function.set_damage.damage", damage)));
        components.add(pad(pad + 1, translatable("ali.property.function.set_damage.add", add)));

        return components;
    }
}
