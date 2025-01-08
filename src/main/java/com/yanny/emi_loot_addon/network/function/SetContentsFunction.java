package com.yanny.emi_loot_addon.network.function;

import com.yanny.emi_loot_addon.mixin.MixinSetContainerContents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraftforge.registries.ForgeRegistries;

public class SetContentsFunction extends LootConditionalFunction {
    public final ResourceLocation blockEntityType;

    public SetContentsFunction(LootContext lootContext, LootItemFunction function) {
        super(lootContext, function);
        blockEntityType = ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(((MixinSetContainerContents) function).getType());
    }

    public SetContentsFunction(FunctionType type, FriendlyByteBuf buf) {
        super(type, buf);
        blockEntityType = buf.readResourceLocation();
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        super.encode(buf);
        buf.writeResourceLocation(blockEntityType);
    }
}
