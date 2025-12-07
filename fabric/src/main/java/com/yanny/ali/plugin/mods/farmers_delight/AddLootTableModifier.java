package com.yanny.ali.plugin.mods.farmers_delight;

import com.yanny.ali.api.*;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.tooltip.ArrayTooltipNode;
import com.yanny.ali.plugin.common.tooltip.LiteralTooltipNode;
import com.yanny.ali.plugin.mods.BaseAccessor;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.mods.porting_lib.loot.GlobalLootModifierUtils;
import com.yanny.ali.plugin.mods.porting_lib.loot.IGlobalLootModifier;
import com.yanny.ali.plugin.server.GenericTooltipUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ClassAccessor("vectorwing.farmersdelight.common.loot.modifier.AddLootTableModifier")
public class AddLootTableModifier extends BaseAccessor<Object> implements IGlobalLootModifier {
    @FieldAccessor
    private ResourceLocation lootTable;
    @FieldAccessor
    protected LootItemCondition[] conditions;

    public AddLootTableModifier(Object parent) {
        super(parent);
    }

    public Optional<ILootModifier<?>> getLootModifier(IServerUtils utils) {
        List<LootItemCondition> conditionList = Arrays.asList(this.conditions);

        return GlobalLootModifierUtils.getLootModifier(conditionList, (c) -> {
            ITooltipNode tooltip = ArrayTooltipNode.array()
                    .add(LiteralTooltipNode.translatable("ali.enum.group_type.all"))
                    .add(GenericTooltipUtils.getConditionsTooltip(utils, c))
                    .build();
            IDataNode node = NodeUtils.getReferenceNode(utils, lootTable, c, tooltip);
            return Collections.singletonList(new IOperation.AddOperation((itemStack) -> true, node));
        });
    }
}
