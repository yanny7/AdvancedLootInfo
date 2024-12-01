package com.yanny.emi_loot_addon.network.function;

import com.yanny.emi_loot_addon.mixin.MixinSetItemCountFunction;
import com.yanny.emi_loot_addon.network.LootUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import org.jetbrains.annotations.NotNull;

public class SetCountFunction extends LootConditionalFunction {
    public final int[] count;
    public final boolean add;

    public SetCountFunction(LootContext lootContext, LootItemFunction function) {
        super(lootContext, function);
        count = LootUtils.getInt(lootContext, ((MixinSetItemCountFunction) function).getValue());
        add = ((MixinSetItemCountFunction) function).getAdd();
    }

    public SetCountFunction(FunctionType type, @NotNull FriendlyByteBuf buf) {
        super(type, buf);
        count = buf.readVarIntArray();
        add = buf.readBoolean();
    }

    @Override
    public void encode(@NotNull FriendlyByteBuf buf) {
        super.encode(buf);
        buf.writeVarIntArray(count);
        buf.writeBoolean(add);
    }
}
