package com.yanny.alicompat.compat.enderio;

import com.enderio.base.common.config.BaseConfig;
import com.enderio.base.common.loot.BrokenSpawnerLootModifier;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.ali.plugin.server.EnchantedRanges;
import com.yanny.ali.plugin.server.TooltipUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IGlobalLootModifierAccessor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
                (c) -> Collections.singletonList(new IOperation.AddOperation((itemStack) -> true, brokenSpawnerNode(utils, c, dropChance))));
    }

    @NotNull
    private static IDataNode brokenSpawnerNode(IServerUtils utils, List<LootItemCondition> conditions, float dropChance) {
        EnchantedRanges chance = NodeUtils.getEnchantedChance(utils, conditions, dropChance);
        TooltipBuilder tooltip = TooltipUtils.getTooltip(utils, LootPoolSingletonContainer.DEFAULT_QUALITY, chance, new EnchantedRanges(new RangeValue(1)),
                Collections.emptyList(), conditions);

        return new ItemNode(dropChance, new RangeValue(1), brokenSpawner(), tooltip.build(), Collections.emptyList(), conditions);
    }

    @NotNull
    private static ItemStack brokenSpawner() {
        return BuiltInRegistries.ITEM.get(BROKEN_SPAWNER).getDefaultInstance();
    }
}
