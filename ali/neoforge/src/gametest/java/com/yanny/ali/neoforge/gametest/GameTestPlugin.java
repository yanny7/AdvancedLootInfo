package com.yanny.ali.neoforge.gametest;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.Utils;
import com.yanny.ali.api.AliEntrypoint;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.ali.plugin.server.LootCount;
import com.yanny.ali.plugin.server.TooltipUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@AliEntrypoint
public class GameTestPlugin implements IGlobalLootModifierPlugin {
    @NotNull
    @Override
    public String getModId() {
        return Utils.MOD_ID;
    }

    @Override
    public void registerGlobalLootModifier(IRegistry registry) {
        registry.registerGlobalLootModifier(GameTestLootModifiers.AddItem.class, (utils, modifier) -> Optional.of(GlobalLootModifierUtils.getLootModifier(
                utils, modifier, Arrays.asList(modifier.getConditions()),
                (page, conditions) -> List.of(new IOperation.AddOperation((stack) -> true, addedNode(utils, conditions, new ItemStack(modifier.item), modifier.count))))));
        registry.registerGlobalLootModifier(GameTestLootModifiers.RemoveItem.class, (utils, modifier) -> Optional.of(GlobalLootModifierUtils.getLootModifier(
                utils, modifier, Arrays.asList(modifier.getConditions()),
                (page, conditions) -> List.of(new IOperation.RemoveOperation((stack) -> stack.is(modifier.item), (node) -> null)))));
    }

    @NotNull
    private static IDataNode addedNode(IServerUtils utils, List<LootItemCondition> conditions, ItemStack item, int count) {
        LootCount chance = NodeUtils.getChance(utils, conditions, 1);
        TooltipBuilder tooltip = TooltipUtils.getTooltip(utils, LootPoolSingletonContainer.DEFAULT_QUALITY, chance, LootCount.of(NumberExpr.constant(count)),
                NodeUtils.getCountLimit(item), Collections.emptyList(), conditions);

        return new ItemNode(1, NumberExpr.constant(count), item, tooltip.build(), Collections.emptyList(), conditions);
    }
}
