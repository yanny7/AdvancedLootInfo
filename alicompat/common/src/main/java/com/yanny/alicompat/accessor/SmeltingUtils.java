package com.yanny.alicompat.accessor;

import com.yanny.aci.api.RangeValue;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IItemNode;
import com.yanny.ali.api.IServerUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class SmeltingUtils {
    @NotNull
    public static Optional<ItemStack> smelt(IServerUtils utils, ItemStack itemStack) {
        ServerLevel level = utils.getServerLevel();

        return level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(itemStack), level)
                .map((recipe) -> recipe.value().getResultItem(level.registryAccess()))
                .filter((stack) -> !stack.isEmpty());
    }

    @NotNull
    public static List<IDataNode> smeltedNode(IServerUtils utils, List<LootItemCondition> conditions, IDataNode src) {
        IItemNode node = (IItemNode) src;
        Optional<ItemStack> result = node.getItem().left().flatMap((stack) -> smelt(utils, stack));

        if (result.isEmpty()) {
            return List.of(src);
        }

        ItemStack smelted = result.get();

        return GlmNodeUtils.replacedNode(utils, conditions, src, smelted, new RangeValue(node.getCount()).multiply(smelted.getCount()));
    }
}
