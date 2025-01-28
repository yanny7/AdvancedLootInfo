package com.yanny.advanced_loot_info.plugin.function;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.advanced_loot_info.compatibility.EmiUtils.pad;
import static com.yanny.advanced_loot_info.compatibility.EmiUtils.translatable;

public class CopyNbtFunction extends LootConditionalFunction {
    public CopyNbtFunction(LootContext lootContext, LootItemFunction function) {
        super(lootContext, function);
    }

    public CopyNbtFunction(FriendlyByteBuf buf) {
        super(buf);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("emi.type.advanced_loot_info.function.copy_nbt")));

        return components;
    }
}
