package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.ali.plugin.TooltipUtils.pad;
import static com.yanny.ali.plugin.TooltipUtils.translatable;

public class FurnaceSmeltFunction extends LootConditionalFunction {
    public FurnaceSmeltFunction(IContext context, LootItemFunction function) {
        super(context, function);
    }

    public FurnaceSmeltFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.furnace_smelt")));

        return components;
    }
}
