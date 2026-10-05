package com.yanny.alicompat.accessor;

import com.yanny.aci.api.NumberExpr;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IItemNode;
import com.yanny.ali.api.IServerUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SmeltingUtils {
    @Nullable
    private static Map<Item, List<SmeltingRecipe>> recipesByItem;

    @NotNull
    public static Optional<ItemStack> smelt(IServerUtils utils, ItemStack itemStack) {
        ServerLevel level = utils.getServerLevel();
        List<SmeltingRecipe> recipes = getRecipesByItem(level).getOrDefault(itemStack.getItem(), List.of());

        if (recipes.isEmpty()) {
            return Optional.empty();
        }

        SimpleContainer container = new SimpleContainer(itemStack);

        return recipes.stream()
                .filter((recipe) -> recipe.matches(container, level))
                .findFirst()
                .map((recipe) -> recipe.getResultItem(level.registryAccess()))
                .filter((stack) -> !stack.isEmpty());
    }

    public static void clearCache() {
        recipesByItem = null;
    }

    @NotNull
    public static List<IDataNode> smeltedNode(IServerUtils utils, List<LootItemCondition> conditions, IDataNode src) {
        IItemNode node = (IItemNode) src;
        Optional<ItemStack> result = node.getItem().left().flatMap((stack) -> smelt(utils, stack));

        if (result.isEmpty()) {
            return List.of(src);
        }

        ItemStack smelted = result.get();

        return GlmNodeUtils.replacedNode(utils, conditions, src, smelted, NumberExpr.mul(node.getCount(), NumberExpr.constant(smelted.getCount())));
    }

    @NotNull
    private static Map<Item, List<SmeltingRecipe>> getRecipesByItem(ServerLevel level) {
        if (recipesByItem == null) {
            Map<Item, List<SmeltingRecipe>> index = new HashMap<>();

            for (SmeltingRecipe recipe : level.getRecipeManager().getAllRecipesFor(RecipeType.SMELTING)) {
                recipe.getIngredients().stream()
                        .flatMap((ingredient) -> Arrays.stream(ingredient.getItems()))
                        .map(ItemStack::getItem)
                        .distinct()
                        .forEach((item) -> index.computeIfAbsent(item, (i) -> new ArrayList<>()).add(recipe));
            }

            recipesByItem = index;
        }

        return recipesByItem;
    }
}
