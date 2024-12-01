package com.yanny.emi_loot_addon.network.function;

import com.yanny.emi_loot_addon.mixin.MixinExplorationMapFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import org.jetbrains.annotations.NotNull;

public class ExplorationMapFunction extends LootConditionalFunction {
    public final ResourceLocation structure;

    public ExplorationMapFunction(LootContext lootContext, LootItemFunction function) {
        super(lootContext, function);
        structure = ((MixinExplorationMapFunction) function).getDestination().location();
    }

    public ExplorationMapFunction(FunctionType type, @NotNull FriendlyByteBuf buf) {
        super(type, buf);
        structure = buf.readResourceLocation();
    }

    @Override
    public void encode(@NotNull FriendlyByteBuf buf) {
        super.encode(buf);
        buf.writeResourceLocation(structure);
    }
}
