package com.yanny.ali.compatibility.jei;

import com.mojang.datafixers.util.Pair;
import com.yanny.ali.api.*;
import com.yanny.ali.compatibility.common.IType;
import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.plugin.GenericTooltipUtils;
import com.yanny.ali.plugin.widget.LootTableWidget;
import com.yanny.ali.registries.LootCategory;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class JeiBaseLoot<T extends IType, V> implements IRecipeCategory<T> {
    private final RecipeType<T> recipeType;
    private final LootCategory<V> lootCategory;
    private final Component title;
    private final IDrawable icon;
    @Nullable
    private IRecipeWidget widget;
    private List<ISlotParams> slotParams;

    public JeiBaseLoot(RecipeType<T> recipeType, LootCategory<V> lootCategory, Component title, IDrawable icon) {
        this.recipeType = recipeType;
        this.lootCategory = lootCategory;
        this.title = title;
        this.icon = icon;
    }

    @NotNull
    @Override
    public final RecipeType<T> getRecipeType() {
        return recipeType;
    }

    @NotNull
    @Override
    public final Component getTitle() {
        return title;
    }

    @Override
    public final IDrawable getIcon() {
        return icon;
    }

    public LootCategory<V> getLootCategory() {
        return lootCategory;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup iFocusGroup) {
        widget = new JeiWidgetWrapper(new LootTableWidget(getJeiUtils(), recipe.entry(), 0, getYOffset(recipe)));

        for (int i = 0; i < slotParams.size(); i++) {
            ISlotParams p = slotParams.get(i);

            if (p instanceof ItemSlotParams itemSlotParams) {
                builder.addOutputSlot()
                        .setStandardSlotBackground()
                        .setSlotName(String.valueOf(i))
                        .setPosition(1, 1)
                        .addRichTooltipCallback((iRecipeSlotView, tooltipBuilder)
                                -> tooltipBuilder.addAll(GenericTooltipUtils.getTooltip(p.entry(), p.chance(), p.bonusChance(), p.count(), p.bonusCount(), p.allFunctions(), p.allConditions())))
                        .addItemLike(itemSlotParams.item);
            } else if (p instanceof TagSlotParams tagSlotParams) {
                builder.addOutputSlot()
                        .setStandardSlotBackground()
                        .setSlotName(String.valueOf(i))
                        .setPosition(1, 1)
                        .addRichTooltipCallback((iRecipeSlotView, tooltipBuilder)
                                -> tooltipBuilder.addAll(GenericTooltipUtils.getTooltip(p.entry(), p.chance(), p.bonusChance(), p.count(), p.bonusCount(), p.allFunctions(), p.allConditions())))
                        .addIngredients(Ingredient.of(tagSlotParams.item));
            }
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, T recipe, IFocusGroup focuses) {
        if (widget != null) {
            builder.addWidget(widget);
        }

        for (int i = 0; i < slotParams.size(); i++) {
            ISlotParams p = slotParams.get(i);

            builder.getRecipeSlots().findSlotByName(String.valueOf(i)).ifPresent(slotDrawable -> builder.addSlottedWidget(
                    new JeiLootSlotWidget(p.entry(), slotDrawable, p.x(), p.y(), p.chance(), p.bonusChance(), p.count(), p.bonusCount(), p.allFunctions(), p.allConditions()),
                    List.of(slotDrawable)
            ));
        }
    }

    @Override
    public int getWidth() {
        return 9 * 18;
    }

    @Override
    public int getHeight() {
        return 1024;
    }

    abstract int getYOffset(T recipe);

    @NotNull
    private IWidgetUtils getJeiUtils() {
        slotParams = new LinkedList<>();

        return new IWidgetUtils() {
            @Override
            public Pair<List<IEntryWidget>, Rect> createWidgets(IWidgetUtils registry, List<ILootEntry> entries, int x, int y, List<ILootFunction> functions, List<ILootCondition> conditions) {
                return PluginManager.CLIENT_REGISTRY.createWidgets(registry, entries, x, y, functions, conditions);
            }

            @Override
            public Rect getBounds(IClientUtils registry, List<ILootEntry> entries, int x, int y) {
                return PluginManager.CLIENT_REGISTRY.getBounds(registry, entries, x, y);
            }

            @Nullable
            @Override
            public WidgetDirection getWidgetDirection(ILootEntry entry) {
                return PluginManager.CLIENT_REGISTRY.getWidgetDirection(entry);
            }

            @Override
            public Rect addSlotWidget(Item item, ILootEntry entry, int x, int y, RangeValue chance, @Nullable Pair<Enchantment, Map<Integer, RangeValue>> bonusChance, RangeValue count,
                                      @Nullable Pair<Enchantment, Map<Integer, RangeValue>> bonusCount, List<ILootFunction> allFunctions, List<ILootCondition> allConditions) {
                slotParams.add(new ItemSlotParams(item, entry, x, y, chance, bonusChance, count, bonusCount, allFunctions, allConditions));
                return new Rect(x, y, 18, 18);
            }

            @Override
            public Rect addSlotWidget(TagKey<Item> item, ILootEntry entry, int x, int y, RangeValue chance, @Nullable Pair<Enchantment, Map<Integer, RangeValue>> bonusChance, RangeValue count,
                                      @Nullable Pair<Enchantment, Map<Integer, RangeValue>> bonusCount, List<ILootFunction> allFunctions, List<ILootCondition> allConditions) {
                slotParams.add(new TagSlotParams(item, entry, x, y, chance, bonusChance, count, bonusCount, allFunctions, allConditions));
                return new Rect(x, y, 18, 18);
            }
        };
    }

    private interface ISlotParams {
        ILootEntry entry();
        int x();
        int y();
        RangeValue chance();
        @Nullable
        Pair<Enchantment, Map<Integer, RangeValue>> bonusChance();
        RangeValue count();
        @Nullable
        Pair<Enchantment, Map<Integer, RangeValue>> bonusCount();
        List<ILootFunction> allFunctions();
        List<ILootCondition> allConditions();
    }

    private record ItemSlotParams (
            Item item,
            ILootEntry entry,
            int x,
            int y,
            RangeValue chance,
            @Nullable Pair<Enchantment, Map<Integer, RangeValue>> bonusChance,
            RangeValue count,
            @Nullable Pair<Enchantment, Map<Integer, RangeValue>> bonusCount,
            List<ILootFunction> allFunctions,
            List<ILootCondition> allConditions
    ) implements ISlotParams {}

    private record TagSlotParams (
            TagKey<Item> item,
            ILootEntry entry,
            int x,
            int y,
            RangeValue chance,
            @Nullable Pair<Enchantment, Map<Integer, RangeValue>> bonusChance,
            RangeValue count,
            @Nullable Pair<Enchantment, Map<Integer, RangeValue>> bonusCount,
            List<ILootFunction> allFunctions,
            List<ILootCondition> allConditions
    ) implements ISlotParams {}
}
