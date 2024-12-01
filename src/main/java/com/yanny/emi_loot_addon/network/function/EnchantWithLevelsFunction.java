package com.yanny.emi_loot_addon.network.function;

import com.yanny.emi_loot_addon.mixin.MixinEnchantWithLevelsFunction;
import com.yanny.emi_loot_addon.network.LootUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import org.jetbrains.annotations.NotNull;

public class EnchantWithLevelsFunction extends LootConditionalFunction {
    public final int[] levels;
    public final boolean treasure;

    public EnchantWithLevelsFunction(LootContext lootContext, LootItemFunction function) {
        super(lootContext, function);
        levels = LootUtils.getInt(lootContext, ((MixinEnchantWithLevelsFunction) function).getLevels());
        treasure = ((MixinEnchantWithLevelsFunction) function).getTreasure();
    }

    public EnchantWithLevelsFunction(FunctionType type, @NotNull FriendlyByteBuf buf) {
        super(type, buf);
        levels = buf.readVarIntArray();
        treasure = buf.readBoolean();
    }

    @Override
    public void encode(@NotNull FriendlyByteBuf buf) {
        super.encode(buf);
        buf.writeVarIntArray(levels);
        buf.writeBoolean(treasure);
    }
}
