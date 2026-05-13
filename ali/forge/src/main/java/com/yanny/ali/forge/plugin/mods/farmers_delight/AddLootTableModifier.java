package com.yanny.ali.forge.plugin.mods.farmers_delight;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.forge.plugin.GlobalLootModifier;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.ali.plugin.glm.IGlobalLootModifierAccessor;
import com.yanny.ali.plugin.glm.ILootTableIdConditionPredicate;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.server.GenericTooltipUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.LootModifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ClassAccessor("vectorwing.farmersdelight.common.loot.modifier.AddLootTableModifier")
public class AddLootTableModifier extends GlobalLootModifier implements IGlobalLootModifierAccessor {
    @FieldAccessor
    private ResourceLocation lootTable;

    public AddLootTableModifier(LootModifier parent) {
        super(parent);
    }

    public Optional<ILootModifier<?>> getLootModifier(IServerUtils utils, ILootTableIdConditionPredicate predicate) {
        List<LootItemCondition> conditionList = Arrays.asList(this.conditions);

        return GlobalLootModifierUtils.getLootModifier(conditionList, (c) -> {
            TooltipNode tooltip = TooltipBuilder.array((b) -> b
                            .add(TooltipBuilder.keyOnly(Lang.Group.ALL))
                            .add(GenericTooltipUtils.getConditionsSectionTooltip(utils, c))
                    )
                    .build();
            IDataNode node = NodeUtils.getReferenceNode(utils, lootTable, c, tooltip);
            return Collections.singletonList(new IOperation.AddOperation((itemStack) -> true, node));
        }, predicate);
    }
}
