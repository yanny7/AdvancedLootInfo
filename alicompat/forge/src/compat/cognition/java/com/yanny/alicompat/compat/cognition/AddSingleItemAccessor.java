package com.yanny.alicompat.compat.cognition;

import com.cyanogen.experienceobelisk.loot_modifiers.AddSingleItem;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import com.yanny.ali.plugin.server.EnchantedRanges;
import com.yanny.ali.plugin.server.TooltipUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IGlobalLootModifierAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class AddSingleItemAccessor extends BaseAccessor<AddSingleItem> implements IGlobalLootModifierAccessor {
    @FieldAccessor
    protected LootItemCondition[] conditions;

    public AddSingleItemAccessor(AddSingleItem parent) {
        super(parent);
    }

    @Override
    public Optional<ILootModifier<?>> getLootModifier(IServerUtils utils) {
        List<LootItemCondition> conditionList = Arrays.asList(this.conditions);
        String path = parent.path;
        Item item = parent.item;
        float appearChance = parent.appearChance;
        RangeValue count = new RangeValue(parent.min, parent.max);

        return Optional.of(new ILootModifier<ResourceLocation>() {
            @Override
            public boolean predicate(ResourceLocation value) {
                return value.getPath().contains(path);
            }

            @NotNull
            @Override
            public List<IOperation> getOperations() {
                return List.of(new IOperation.AddOperation((itemStack) -> true, addedNode(utils, conditionList, item, appearChance, count)));
            }

            @NotNull
            @Override
            public IType<ResourceLocation> getType() {
                return IType.LOOT_TABLE;
            }
        });
    }

    @NotNull
    private static IDataNode addedNode(IServerUtils utils, List<LootItemCondition> conditions, Item item, float appearChance, RangeValue count) {
        EnchantedRanges chance = NodeUtils.getEnchantedChance(utils, conditions, appearChance);
        TooltipBuilder tooltip = TooltipUtils.getTooltip(utils, LootPoolSingletonContainer.DEFAULT_QUALITY, chance, new EnchantedRanges(new RangeValue(count)),
                Collections.emptyList(), conditions);

        return new ItemNode(appearChance, new RangeValue(count), item.getDefaultInstance(), tooltip.build(), Collections.emptyList(), conditions);
    }
}
