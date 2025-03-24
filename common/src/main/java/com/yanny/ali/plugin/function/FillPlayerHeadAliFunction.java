package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinFillPlayerHead;
import com.yanny.ali.mixin.MixinLootContext;
import com.yanny.ali.plugin.FunctionTooltipUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.List;

public class FillPlayerHeadAliFunction extends LootConditionalAliFunction {
    public final LootContext.EntityTarget entityTarget;

    public FillPlayerHeadAliFunction(IContext context, LootItemFunction function) {
        super(context, function);
        entityTarget = ((MixinFillPlayerHead) function).getEntityTarget();
    }

    public FillPlayerHeadAliFunction(IContext context, FriendlyByteBuf buf) {
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
        return FunctionTooltipUtils.getFillPlayerHeadTooltip(pad, entityTarget);
    }
}
