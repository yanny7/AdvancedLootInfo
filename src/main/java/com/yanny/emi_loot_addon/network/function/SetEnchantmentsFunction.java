package com.yanny.emi_loot_addon.network.function;

import com.yanny.emi_loot_addon.mixin.MixinSetEnchantmentsFunction;
import com.yanny.emi_loot_addon.network.RangeValue;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class SetEnchantmentsFunction extends LootConditionalFunction {
    public final Map<ResourceLocation, RangeValue> enchantments;
    public final boolean add;

    public SetEnchantmentsFunction(LootContext lootContext, LootItemFunction function) {
        super(lootContext, function);
        enchantments = ((MixinSetEnchantmentsFunction) function).getEnchantments().entrySet().stream().collect(Collectors.toMap(
                (e) -> ForgeRegistries.ENCHANTMENTS.getKey(e.getKey()),
                (e) -> RangeValue.of(lootContext, e.getValue())
        ));
        add = ((MixinSetEnchantmentsFunction) function).getAdd();
    }

    public SetEnchantmentsFunction(FunctionType type, FriendlyByteBuf buf) {
        super(type, buf);
        int count = buf.readInt();

        enchantments = new HashMap<>();

        for (int i = 0; i < count; i++) {
            enchantments.put(buf.readResourceLocation(), new RangeValue(buf));
        }

        add = buf.readBoolean();
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        super.encode(buf);
        buf.writeInt(enchantments.size());
        enchantments.forEach((location, levels) -> {
            buf.writeResourceLocation(location);
            levels.encode(buf);
        });
        buf.writeBoolean(add);
    }
}
