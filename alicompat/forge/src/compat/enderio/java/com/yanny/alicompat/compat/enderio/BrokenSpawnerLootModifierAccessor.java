package com.yanny.alicompat.compat.enderio;

import com.enderio.base.common.config.BaseConfig;
import com.enderio.base.common.loot.BrokenSpawnerLootModifier;
import com.yanny.aci.api.RangeValue;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.GlmNodeUtils;
import com.yanny.alicompat.accessor.IGlobalLootModifierAccessor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

public class BrokenSpawnerLootModifierAccessor extends BaseAccessor<BrokenSpawnerLootModifier> implements IGlobalLootModifierAccessor {
    private static final ResourceLocation BROKEN_SPAWNER = new ResourceLocation(EnderIoLang.MOD_ID, "broken_spawner");

    @FieldAccessor
    protected LootItemCondition[] conditions;

    public BrokenSpawnerLootModifierAccessor(BrokenSpawnerLootModifier parent) {
        super(parent);
    }

    @Override
    public Optional<ILootModifier<?>> getLootModifier(IServerUtils utils) {
        float dropChance = BaseConfig.COMMON.BLOCKS.BROKEN_SPAWNER_DROP_CHANCE.get().floatValue();

        return GlobalLootModifierUtils.getLootModifier(utils, parent, Arrays.asList(this.conditions),
                (c) -> Collections.singletonList(new IOperation.AddOperation((itemStack) -> true,
                        GlmNodeUtils.addedNode(utils, c, brokenSpawner(), dropChance, new RangeValue(1)))));
    }

    @NotNull
    private static ItemStack brokenSpawner() {
        return BuiltInRegistries.ITEM.get(BROKEN_SPAWNER).getDefaultInstance();
    }
}
