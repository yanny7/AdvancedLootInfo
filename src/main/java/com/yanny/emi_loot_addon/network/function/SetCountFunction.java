package com.yanny.emi_loot_addon.network.function;

import com.yanny.emi_loot_addon.mixin.MixinSetItemCountFunction;
import com.yanny.emi_loot_addon.network.value.RangeValue;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import org.jetbrains.annotations.NotNull;

public class SetCountFunction extends LootConditionalFunction {
    public final RangeValue count;
    public final boolean add;

    public SetCountFunction(LootContext lootContext, LootItemFunction function) {
        super(lootContext, function);
        count = RangeValue.of(lootContext, ((MixinSetItemCountFunction) function).getValue());
        add = ((MixinSetItemCountFunction) function).getAdd();
    }

    public SetCountFunction(FunctionType type, @NotNull FriendlyByteBuf buf) {
        super(type, buf);
        count = new RangeValue(buf);
        add = buf.readBoolean();
    }

    @Override
    public void encode(@NotNull FriendlyByteBuf buf) {
        super.encode(buf);
        count.encode(buf);
        buf.writeBoolean(add);
    }
}
