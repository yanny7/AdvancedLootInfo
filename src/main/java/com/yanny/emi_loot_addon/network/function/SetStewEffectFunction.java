package com.yanny.emi_loot_addon.network.function;

import com.yanny.emi_loot_addon.mixin.MixinSetStewEffectFunction;
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

public class SetStewEffectFunction extends LootConditionalFunction {
    public final Map<ResourceLocation, int[]> effectMap;

    public SetStewEffectFunction(LootContext lootContext, LootItemFunction function) {
        super(lootContext, function);
        effectMap = ((MixinSetStewEffectFunction) function).getEffectDurationMap().entrySet().stream().collect(Collectors.toMap(
                (e) -> ForgeRegistries.MOB_EFFECTS.getKey(e.getKey()),
                (e) -> LootUtils.getInt(lootContext, e.getValue())
        ));
    }

    public SetStewEffectFunction(FunctionType type, @NotNull FriendlyByteBuf buf) {
        super(type, buf);
        int count = buf.readInt();

        effectMap = new HashMap<>();

        for (int i = 0; i < count; i++) {
            effectMap.put(buf.readResourceLocation(), buf.readVarIntArray());
        }
    }

    @Override
    public void encode(@NotNull FriendlyByteBuf buf) {
        super.encode(buf);
        buf.writeInt(effectMap.size());
        effectMap.forEach((location, level) -> {
            buf.writeResourceLocation(location);
            buf.writeVarIntArray(level);
        });
    }
}
