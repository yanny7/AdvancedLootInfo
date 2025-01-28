package com.yanny.advanced_loot_info.plugin.function;

import com.yanny.advanced_loot_info.mixin.MixinSetItemDamageFunction;
import com.yanny.advanced_loot_info.network.RangeValue;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.advanced_loot_info.compatibility.EmiUtils.pad;
import static com.yanny.advanced_loot_info.compatibility.EmiUtils.translatable;

public class SetDamageFunction extends LootConditionalFunction {
    public final RangeValue damage;
    public final boolean add;

    public SetDamageFunction(LootContext lootContext, LootItemFunction function) {
        super(lootContext, function);
        damage = RangeValue.of(lootContext, ((MixinSetItemDamageFunction) function).getDamage());
        add = ((MixinSetItemDamageFunction) function).getAdd();
    }

    public SetDamageFunction(FriendlyByteBuf buf) {
        super(buf);
        damage = new RangeValue(buf);
        add = buf.readBoolean();
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        super.encode(buf);
        damage.encode(buf);
        buf.writeBoolean(add);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("emi.type.advanced_loot_info.function.set_damage")));
        components.add(pad(pad + 1, translatable("emi.property.function.set_damage.damage", damage)));
        components.add(pad(pad + 1, translatable("emi.property.function.set_damage.add", add)));

        return components;
    }
}
