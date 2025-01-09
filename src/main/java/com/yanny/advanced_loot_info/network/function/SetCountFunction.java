package com.yanny.advanced_loot_info.network.function;

import com.yanny.advanced_loot_info.EmiLootMod;
import com.yanny.advanced_loot_info.mixin.MixinSetItemCountFunction;
import com.yanny.advanced_loot_info.network.RangeValue;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.advanced_loot_info.compatibility.EmiUtils.pad;
import static com.yanny.advanced_loot_info.compatibility.EmiUtils.translatable;

public class SetCountFunction extends LootConditionalFunction {
    public final RangeValue count;
    public final boolean add;

    public SetCountFunction(LootContext lootContext, LootItemFunction function) {
        super(lootContext, function);
        count = RangeValue.of(lootContext, ((MixinSetItemCountFunction) function).getValue());
        add = ((MixinSetItemCountFunction) function).getAdd();
    }

    public SetCountFunction(FunctionType type, FriendlyByteBuf buf) {
        super(type, buf);
        count = new RangeValue(buf);
        add = buf.readBoolean();
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        super.encode(buf);
        count.encode(buf);
        buf.writeBoolean(add);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        if (EmiLootMod.CONFIGURATION.isDebug()) {
            components.add(pad(pad, translatable("emi.debug.set_count", count, add)));
        }

        return components;
    }
}
