package com.yanny.emi_loot_addon.network.condition;

import com.yanny.emi_loot_addon.mixin.MixinLootItemBlockStatePropertyCondition;
import com.yanny.emi_loot_addon.network.LootCondition;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.registries.ForgeRegistries;

public class BlockStatePropertyCondition extends LootCondition {
    public final ResourceLocation block;
    public final StatePropertiesPredicate properties;

    public BlockStatePropertyCondition(LootContext lootContext, LootItemCondition condition) {
        super(ConditionType.of(condition.getType()));
        block = ForgeRegistries.BLOCKS.getKey(((MixinLootItemBlockStatePropertyCondition) condition).getBlock());
        properties = ((MixinLootItemBlockStatePropertyCondition) condition).getProperties();
    }

    public BlockStatePropertyCondition(ConditionType type, FriendlyByteBuf buf) {
        super(type);
        block = buf.readResourceLocation();
        properties = StatePropertiesPredicate.fromJson(buf.readJsonWithCodec(ExtraCodecs.JSON));
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(block);
        buf.writeJsonWithCodec(ExtraCodecs.JSON, properties.serializeToJson());
    }

}
