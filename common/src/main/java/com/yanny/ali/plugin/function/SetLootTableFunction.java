package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinSetContainerLootTable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.ali.plugin.TooltipUtils.pad;
import static com.yanny.ali.plugin.TooltipUtils.translatable;

public class SetLootTableFunction extends LootConditionalFunction {
    public final ResourceLocation name;
    public final long seed;
    public final ResourceLocation blockEntityType;

    public SetLootTableFunction(IContext context, LootItemFunction function) {
        super(context, function);
        name = ((MixinSetContainerLootTable) function).getName();
        seed = ((MixinSetContainerLootTable) function).getSeed();
        blockEntityType = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(((MixinSetContainerLootTable) function).getType().value());
    }

    public SetLootTableFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        name = buf.readResourceLocation();
        seed = buf.readLong();
        blockEntityType = buf.readResourceLocation();
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeResourceLocation(name);
        buf.writeLong(seed);
        buf.writeResourceLocation(blockEntityType);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_loot_table")));
        components.add(pad(pad + 1, translatable("ali.property.function.set_loot_table.name", name)));
        components.add(pad(pad + 1, translatable("ali.property.function.set_loot_table.seed", seed)));
        components.add(pad(pad + 1, translatable("ali.property.function.set_loot_table.type", blockEntityType)));

        return components;
    }
}
