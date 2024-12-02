package com.yanny.emi_loot_addon.network.function;

import com.yanny.emi_loot_addon.mixin.MixinEnchantWithLevelsFunction;
import com.yanny.emi_loot_addon.network.value.RangeValue;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import org.jetbrains.annotations.NotNull;

public class EnchantWithLevelsFunction extends LootConditionalFunction {
    public final RangeValue levels;
    public final boolean treasure;

    public EnchantWithLevelsFunction(LootContext lootContext, LootItemFunction function) {
        super(lootContext, function);
        levels = RangeValue.of(lootContext, ((MixinEnchantWithLevelsFunction) function).getLevels());
        treasure = ((MixinEnchantWithLevelsFunction) function).getTreasure();
    }

    public EnchantWithLevelsFunction(FunctionType type, @NotNull FriendlyByteBuf buf) {
        super(type, buf);
        levels = new RangeValue(buf);
        treasure = buf.readBoolean();
    }

    @Override
    public void encode(@NotNull FriendlyByteBuf buf) {
        super.encode(buf);
        levels.encode(buf);
        buf.writeBoolean(treasure);
    }
}
