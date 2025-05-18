package com.yanny.ali.compatibility.jei;

import com.yanny.ali.api.IWidgetUtils;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.api.Rect;
import com.yanny.ali.compatibility.common.IType;
import com.yanny.ali.plugin.client.ClientUtils;
import com.yanny.ali.plugin.client.EntryTooltipUtils;
import com.yanny.ali.plugin.client.TooltipUtils;
import com.yanny.ali.plugin.client.widget.LootTableWidget;
import com.yanny.ali.registries.LootCategory;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class JeiBaseLoot<T extends IType, V> implements IRecipeCategory<T> {
    private static final Map<IType, Pair<JeiWidgetWrapper, List<ISlotParams>>> widgets = new HashMap<>();

    protected final IGuiHelper guiHelper;
    private final RecipeType<T> recipeType;
    private final LootCategory<V> lootCategory;
    private final Component title;
    private final IDrawable icon;

    public JeiBaseLoot(IGuiHelper guiHelper, RecipeType<T> recipeType, LootCategory<V> lootCategory, Component title, IDrawable icon) {
        this.guiHelper = guiHelper;
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
        List<ISlotParams> slotParams = new LinkedList<>();
        IWidgetUtils utils = getJeiUtils(slotParams);

        widgets.put(recipe, new Pair<>(new JeiWidgetWrapper(new LootTableWidget(utils, recipe.entry(), 0, getYOffset(recipe))), slotParams));

        for (int i = 0; i < slotParams.size(); i++) {
            ISlotParams p = slotParams.get(i);

            if (p instanceof ItemSlotParams itemSlotParams) {
                builder.addOutputSlot()
                        .setStandardSlotBackground()
                        .setSlotName(String.valueOf(i))
                        .setPosition(itemSlotParams.x, itemSlotParams.y)
                        .addRichTooltipCallback((iRecipeSlotView, tooltipBuilder)
                                -> tooltipBuilder.addAll(EntryTooltipUtils.getTooltip(utils, p.entry(), p.chance(), p.count(), p.allFunctions(), p.allConditions())))
                        .addItemStack(itemSlotParams.item);
            } else if (p instanceof TagSlotParams tagSlotParams) {
                builder.addOutputSlot()
                        .setStandardSlotBackground()
                        .setSlotName(String.valueOf(i))
                        .setPosition(tagSlotParams.x, tagSlotParams.y)
                        .addRichTooltipCallback((iRecipeSlotView, tooltipBuilder)
                                -> tooltipBuilder.addAll(EntryTooltipUtils.getTooltip(utils, p.entry(), p.chance(), p.count(), p.allFunctions(), p.allConditions())))
                        .addIngredients(Ingredient.of(tagSlotParams.item));
            }
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, T recipe, IFocusGroup focuses) {
        Pair<JeiWidgetWrapper, List<ISlotParams>> pair = widgets.remove(recipe);

        if (pair == null) {
            return;
        }

        Pair<List<IRecipeWidget>, List<IRecipeSlotDrawable>> additionalWidgets = getWidgets(builder, recipe);
        JeiWidgetWrapper widgetWrapper = pair.getA();
        List<ISlotParams> slotParams = pair.getB();
        List<IRecipeWidget> scrollWidgets = new LinkedList<>(additionalWidgets.getA());
        List<IRecipeSlotDrawable> slotDrawables = new LinkedList<>(additionalWidgets.getB());

        scrollWidgets.add(widgetWrapper);

        for (int i = 0; i < slotParams.size(); i++) {
            ISlotParams p = slotParams.get(i);

            builder.getRecipeSlots().findSlotByName(String.valueOf(i)).ifPresent((slotDrawable) -> {
                scrollWidgets.add(new JeiLootSlotWidget(slotDrawable, p.x(), p.y(), p.count()));
                slotDrawables.add(slotDrawable);
            });
        }

        Rect renderRect = new Rect(0, 0, 9 * 18 + JeiScrollWidget.getScrollBoxScrollbarExtraWidth(), 7 * 18);
        JeiScrollWidget scrollWidget = new JeiScrollWidget(renderRect, pair.getA().getRect().height() + getYOffset(recipe), scrollWidgets);

        builder.addSlottedWidget(scrollWidget, slotDrawables);
        builder.addInputHandler(scrollWidget);
    }

    @Override
    public int getWidth() {
        return 9 * 18 + JeiScrollWidget.getScrollBoxScrollbarExtraWidth();
    }

    @Override
    public int getHeight() {
        return 7 * 18;
    }

    abstract Pair<List<IRecipeWidget>, List<IRecipeSlotDrawable>> getWidgets(IRecipeExtrasBuilder builder, T recipe);

    abstract int getYOffset(T recipe);

    @NotNull
    protected IRecipeWidget createTextWidget(Component component, int x, int y, boolean centered) {
        return guiHelper.createWidgetFromDrawable(new IDrawable() {
            @Override
            public int getWidth() {
                return 9 * 18;
            }

            @Override
            public int getHeight() {
                return 8;
            }

            @Override
            public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset) {
                if (centered) {
                    int width = Minecraft.getInstance().font.width(component);
                    guiGraphics.drawString(Minecraft.getInstance().font, component, x - width / 2, y, 0, false);
                } else {
                    guiGraphics.drawString(Minecraft.getInstance().font, component, x, y, 0, false);
                }
            }
        }, x, y);
    }

    @NotNull
    private IWidgetUtils getJeiUtils(List<ISlotParams> slotParams) {
        return new ClientUtils() {
            @Override
            public Rect addSlotWidget(Item item, LootPoolEntryContainer entry, int x, int y, Map<Enchantment, Map<Integer, RangeValue>> chance,
                                      Map<Enchantment, Map<Integer, RangeValue>> count, List<LootItemFunction> allFunctions, List<LootItemCondition> allConditions) {
                ItemStack itemStack = TooltipUtils.getItemStack(this, entry, item);
                slotParams.add(new ItemSlotParams(itemStack, entry, x, y, chance, count, allFunctions, allConditions));
                return new Rect(x, y, 18, 18);
            }

            @Override
            public Rect addSlotWidget(TagKey<Item> item, LootPoolEntryContainer entry, int x, int y, Map<Enchantment, Map<Integer, RangeValue>> chance,
                                      Map<Enchantment, Map<Integer, RangeValue>> count, List<LootItemFunction> allFunctions, List<LootItemCondition> allConditions) {
                slotParams.add(new TagSlotParams(item, entry, x, y, chance, count, allFunctions, allConditions));
                return new Rect(x, y, 18, 18);
            }
        };
    }

    private interface ISlotParams {
        LootPoolEntryContainer entry();
        int x();
        int y();
        Map<Enchantment, Map<Integer, RangeValue>> chance();
        Map<Enchantment, Map<Integer, RangeValue>> count();
        List<LootItemFunction> allFunctions();
        List<LootItemCondition> allConditions();
    }

    private record ItemSlotParams (
            ItemStack item,
            LootPoolEntryContainer entry,
            int x,
            int y,
            Map<Enchantment, Map<Integer, RangeValue>> chance,
            Map<Enchantment, Map<Integer, RangeValue>> count,
            List<LootItemFunction> allFunctions,
            List<LootItemCondition> allConditions
    ) implements ISlotParams {}

    private record TagSlotParams (
            TagKey<Item> item,
            LootPoolEntryContainer entry,
            int x,
            int y,
            Map<Enchantment, Map<Integer, RangeValue>> chance,
            Map<Enchantment, Map<Integer, RangeValue>> count,
            List<LootItemFunction> allFunctions,
            List<LootItemCondition> allConditions
    ) implements ISlotParams {}
}
