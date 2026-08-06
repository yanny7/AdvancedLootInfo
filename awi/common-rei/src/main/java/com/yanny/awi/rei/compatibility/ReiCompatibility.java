package com.yanny.awi.rei.compatibility;

import com.mojang.logging.LogUtils;
import com.yanny.awi.api.IDataNode;
import com.yanny.awi.compatibility.GenericUtils;
import com.yanny.awi.manager.AwiClientRegistry;
import com.yanny.awi.manager.PluginManager;
import com.yanny.awi.plugin.common.nodes.BiomeNode;
import com.yanny.awi.plugin.common.nodes.LevelStemNode;
import com.yanny.awi.rei.compatibility.rei.RecipeHolder;
import com.yanny.awi.rei.compatibility.rei.ReiBaseCategory;
import com.yanny.awi.rei.compatibility.rei.ReiBiomeCategory;
import com.yanny.awi.rei.compatibility.rei.ReiBiomeDisplay;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.config.entry.EntryStackProvider;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.display.reason.DisplayAdditionReasons;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

@SuppressWarnings("UnstableApiUsage")
public class ReiCompatibility implements REIClientPlugin {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Map<Holder, List<RecipeHolder>> dimensions = new LinkedHashMap<>();

    @Override
    public void registerCategories(CategoryRegistry registry) {
        dimensions.clear();

        GenericUtils.register(registry, this::registerData);
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registerFiller(registry, dimensions, ReiCompatibility::biomePredicate);
    }

    private void registerData(CategoryRegistry registry, byte[] fullCompressedData) {
        AwiClientRegistry clientRegistry = PluginManager.getInstance().clientRegistry;
        ClientLevel level = Minecraft.getInstance().level;

        LOGGER.info("Adding loot information to REI");

        if (level != null) {
            Map<ResourceLocation, LevelStemNode> worldgenData = GenericUtils.decompressWorldgenData(clientRegistry, fullCompressedData, level.registryAccess());
            LongSet hiddenStacks = collectHiddenStacks();

            GenericUtils.pruneHiddenBlocks(worldgenData, (block) -> isVisible(hiddenStacks, block));

            worldgenData.forEach((key, levelNode) -> {
                WorldCategory category = new WorldCategory(key);
                Holder holder = createCategory(category, ReiBiomeDisplay::new, ReiBiomeCategory::new);
                List<RecipeHolder> recipes = new ArrayList<>();

                for (IDataNode biomeNode : levelNode.nodes()) {
                    List<Block> blocks = GenericUtils.collectBlocks(biomeNode);
                    recipes.add(new RecipeHolder(biomeNode, ((BiomeNode) biomeNode).getBiomeId(), blocks));
                }

                dimensions.put(holder, recipes);
                registry.add(holder.category);
            });
        } else {
            LOGGER.warn("REI integration was not loaded! Level is null!");
        }
    }

    /**
     * REI has no {@code isDisabled}/{@code isHidden} pair like EMI - the entries the player hid live in the config as
     * filtered stack providers. {@code EntryRegistry#getPreFilteredList} would also cover rule-based filtering, but it
     * is not guaranteed to be populated while categories are still being registered, so the config is the safe source.
     */
    @NotNull
    private static LongSet collectHiddenStacks() {
        LongSet hidden = new LongOpenHashSet();

        for (EntryStackProvider<?> provider : ConfigObject.getInstance().getFilteredStackProviders()) {
            if (provider.isValid()) {
                EntryStack<?> stack = provider.provide();

                if (!stack.isEmpty()) {
                    hidden.add(EntryStacks.hashFuzzy(stack));
                }
            }
        }

        return hidden;
    }

    /**
     * Blocks with no item form (fire, {@code *_plant}, ...) yield an empty entry that can never be hidden - those are
     * kept, the biome tree draws them as a block model.
     */
    private static boolean isVisible(LongSet hiddenStacks, Block block) {
        EntryStack<ItemStack> entry = EntryStacks.of(block);
        return entry.isEmpty() || !hiddenStacks.contains(EntryStacks.hashFuzzy(entry));
    }

    @NotNull
    private static Holder createCategory(WorldCategory lootCategory,
                                         BiFunction<RecipeHolder, CategoryIdentifier<ReiBiomeDisplay>, ReiBiomeDisplay> displayFactory,
                                         BiFunction<CategoryIdentifier<ReiBiomeDisplay>, Component, ReiBaseCategory<ReiBiomeDisplay>> categoryFactory) {
        CategoryIdentifier<ReiBiomeDisplay> identifier = CategoryIdentifier.of(lootCategory.id);
        Component title = GenericUtils.getFormattedCategoryTitle(lootCategory.id);
        BiFunction<RecipeHolder, DisplayAdditionReasons, ReiBiomeDisplay> filler = (type, r) -> displayFactory.apply(type, identifier);
        return new Holder(identifier, categoryFactory.apply(identifier, title), filler);
    }

    @NotNull
    private static BiPredicate<Object, DisplayAdditionReasons> biomePredicate(List<RecipeHolder> lootTypes) {
        return (o, r) -> {
            if (o != null) {
                if (o instanceof RecipeHolder type) {
                    return lootTypes.contains(type);
                }
            }

            return false;
        };
    }

    private static void registerFiller(DisplayRegistry registry, Map<Holder, List<RecipeHolder>> categories, Function<List<RecipeHolder>, BiPredicate<Object, DisplayAdditionReasons>> predicate) {
        for (Map.Entry<Holder, List<RecipeHolder>> entry : categories.entrySet()) {
            registry.registerFillerWithReason(predicate.apply(entry.getValue()), entry.getKey().filler());
            entry.getValue().forEach(registry::add);
        }
    }

    private record Holder(CategoryIdentifier<ReiBiomeDisplay> identifier, ReiBaseCategory<ReiBiomeDisplay> category, BiFunction<RecipeHolder, DisplayAdditionReasons, ReiBiomeDisplay> filler) {}

    private record WorldCategory(ResourceLocation id) {}
}