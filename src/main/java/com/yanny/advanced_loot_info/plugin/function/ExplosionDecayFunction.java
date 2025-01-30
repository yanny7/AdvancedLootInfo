package com.yanny.advanced_loot_info.plugin.function;

import com.yanny.advanced_loot_info.api.IContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.advanced_loot_info.compatibility.EmiUtils.pad;
import static com.yanny.advanced_loot_info.compatibility.EmiUtils.translatable;

public class ExplosionDecayFunction extends LootConditionalFunction {
    public ExplosionDecayFunction(IContext context, LootItemFunction function) {
        super(context, function);
    }

    public ExplosionDecayFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("emi.type.advanced_loot_info.function.exploration_map")));

        return components;
    }
}
