package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinFillPlayerHead;
import com.yanny.ali.mixin.MixinLootContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.ali.plugin.TooltipUtils.pad;
import static com.yanny.ali.plugin.TooltipUtils.translatable;

public class FillPlayerHeadFunction extends LootConditionalFunction {
    public final LootContext.EntityTarget entityTarget;

    public FillPlayerHeadFunction(IContext context, LootItemFunction function) {
        super(context, function);
        entityTarget = ((MixinFillPlayerHead) function).getEntityTarget();
    }

    public FillPlayerHeadFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        entityTarget = LootContext.EntityTarget.getByName(buf.readUtf());
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeUtf(((MixinLootContext.EntityTarget) ((Object) entityTarget)).getName());
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.fill_player_head")));
        components.add(pad(pad + 1, translatable("ali.property.function.fill_player_head.target", entityTarget)));

        return components;
    }
}
