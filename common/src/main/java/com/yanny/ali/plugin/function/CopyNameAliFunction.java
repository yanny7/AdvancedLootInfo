package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinCopyNameFunction;
import com.yanny.ali.plugin.FunctionTooltipUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.functions.CopyNameFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.List;

public class CopyNameAliFunction extends LootConditionalAliFunction {
    public final CopyNameFunction.NameSource source;

    public CopyNameAliFunction(IContext context, LootItemFunction function) {
        super(context, function);
        source = ((MixinCopyNameFunction) function).getSource();
    }

    public CopyNameAliFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        source = CopyNameFunction.NameSource.getByName(buf.readUtf());
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeUtf(source.name);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        return FunctionTooltipUtils.getCopyNameTooltip(pad, source);
    }
}
