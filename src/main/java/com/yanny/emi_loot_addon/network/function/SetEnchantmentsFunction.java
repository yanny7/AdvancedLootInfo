package com.yanny.emi_loot_addon.network.function;

import com.yanny.emi_loot_addon.mixin.MixinSetEnchantmentsFunction;
import com.yanny.emi_loot_addon.network.LootUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class SetEnchantmentsFunction extends LootConditionalFunction {
    public final Map<ResourceLocation, int[]> enchantments;
    public final boolean add;

    public SetEnchantmentsFunction(LootContext lootContext, LootItemFunction function) {
        super(lootContext, function);
        enchantments = ((MixinSetEnchantmentsFunction) function).getEnchantments().entrySet().stream().collect(Collectors.toMap(
                (e) -> ForgeRegistries.ENCHANTMENTS.getKey(e.getKey()),
                (e) -> LootUtils.getInt(lootContext, e.getValue())
        ));
        add = ((MixinSetEnchantmentsFunction) function).getAdd();
    }

    public SetEnchantmentsFunction(FunctionType type, @NotNull FriendlyByteBuf buf) {
        super(type, buf);
        int count = buf.readInt();

        enchantments = new HashMap<>();

        for (int i = 0; i < count; i++) {
            enchantments.put(buf.readResourceLocation(), buf.readVarIntArray());
        }

        add = buf.readBoolean();
    }

    @Override
    public void encode(@NotNull FriendlyByteBuf buf) {
        super.encode(buf);
        buf.writeInt(enchantments.size());
        enchantments.forEach((location, levels) -> {
            buf.writeResourceLocation(location);
            buf.writeVarIntArray(levels);
        });
        buf.writeBoolean(add);
    }
}
