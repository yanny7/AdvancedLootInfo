package com.yanny.advanced_loot_info.plugin.function;

import com.yanny.advanced_loot_info.mixin.MixinSetContainerContents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.advanced_loot_info.compatibility.EmiUtils.pad;
import static com.yanny.advanced_loot_info.compatibility.EmiUtils.translatable;

public class SetContentsFunction extends LootConditionalFunction {
    public final ResourceLocation blockEntityType;

    public SetContentsFunction(LootContext lootContext, LootItemFunction function) {
        super(lootContext, function);
        blockEntityType = ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(((MixinSetContainerContents) function).getType());
    }

    public SetContentsFunction(FriendlyByteBuf buf) {
        super(buf);
        blockEntityType = buf.readResourceLocation();
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        super.encode(buf);
        buf.writeResourceLocation(blockEntityType);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("emi.type.advanced_loot_info.function.set_contents")));
        components.add(pad(pad + 1, translatable("emi.property.function.set_contents.type", blockEntityType)));

        return components;
    }
}
