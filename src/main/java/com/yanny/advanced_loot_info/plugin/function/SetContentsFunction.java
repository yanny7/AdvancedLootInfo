package com.yanny.advanced_loot_info.plugin.function;

import com.yanny.advanced_loot_info.api.IContext;
import com.yanny.advanced_loot_info.mixin.MixinSetContainerContents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.advanced_loot_info.compatibility.emi.EmiUtils.pad;
import static com.yanny.advanced_loot_info.compatibility.emi.EmiUtils.translatable;

public class SetContentsFunction extends LootConditionalFunction {
    public final ResourceLocation blockEntityType;

    public SetContentsFunction(IContext context, LootItemFunction function) {
        super(context, function);
        blockEntityType = ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(((MixinSetContainerContents) function).getType());
    }

    public SetContentsFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        blockEntityType = buf.readResourceLocation();
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
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
