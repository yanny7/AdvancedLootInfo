package com.yanny.alicompat.compat.artifacts;

import artifacts.forge.loot.SmeltOresWithPickaxeHeaterModifier;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IItemNode;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import com.yanny.ali.plugin.common.nodes.ModifiedNode;
import com.yanny.ali.plugin.server.EnchantedRanges;
import com.yanny.ali.plugin.server.TooltipUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IGlobalLootModifierAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class SmeltOresWithPickaxeHeaterModifierAccessor extends BaseAccessor<SmeltOresWithPickaxeHeaterModifier> implements IGlobalLootModifierAccessor {
    @FieldAccessor
    protected LootItemCondition[] conditions;

    public SmeltOresWithPickaxeHeaterModifierAccessor(SmeltOresWithPickaxeHeaterModifier parent) {
        super(parent);
    }

    @Override
    public Optional<ILootModifier<?>> getLootModifier(IServerUtils utils) {
        List<LootItemCondition> conditionList = Arrays.asList(this.conditions);

        return Optional.of(new ILootModifier<Block>() {
            @Override
            public boolean predicate(Block value) {
                return value.defaultBlockState().is(Tags.Blocks.ORES);
            }

            @NotNull
            @Override
            public List<IOperation> getOperations() {
                return List.of(new IOperation.ReplaceOperation(SmeltOresWithPickaxeHeaterModifierAccessor::isRawMaterial, (src) -> smeltNode(utils, conditionList, src)));
            }

            @NotNull
            @Override
            public IType<Block> getType() {
                return IType.BLOCK;
            }
        });
    }

    private static boolean isRawMaterial(ItemStack itemStack) {
        return itemStack.is(Tags.Items.RAW_MATERIALS);
    }

    @NotNull
    private static List<IDataNode> smeltNode(IServerUtils utils, List<LootItemCondition> conditions, IDataNode src) {
        IItemNode node = (IItemNode) src;
        Optional<ItemStack> result = node.getItem().left().filter(SmeltOresWithPickaxeHeaterModifierAccessor::isRawMaterial).flatMap((stack) -> smelt(utils, stack));

        if (result.isEmpty()) {
            return List.of(src);
        }

        ItemStack smelted = result.get();
        List<LootItemCondition> allConditions = Stream.concat(conditions.stream(), node.getConditions().stream()).toList();
        RangeValue count = new RangeValue(node.getCount()).multiply(smelted.getCount());
        EnchantedRanges chance = NodeUtils.getEnchantedChance(utils, allConditions, node.getChance());
        TooltipBuilder tooltip = TooltipUtils.getTooltip(utils, LootPoolSingletonContainer.DEFAULT_QUALITY, chance, new EnchantedRanges(new RangeValue(count)),
                node.getFunctions(), allConditions);
        ItemNode smeltedNode = new ItemNode(node.getChance(), count, smelted, tooltip.build(), node.getFunctions(), allConditions);

        return List.of(new ModifiedNode(utils, src, smeltedNode));
    }

    @NotNull
    private static Optional<ItemStack> smelt(IServerUtils utils, ItemStack itemStack) {
        ServerLevel level = utils.getServerLevel();

        return level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SimpleContainer(itemStack), level)
                .map((recipe) -> recipe.getResultItem(level.registryAccess()))
                .filter((stack) -> !stack.isEmpty());
    }
}
