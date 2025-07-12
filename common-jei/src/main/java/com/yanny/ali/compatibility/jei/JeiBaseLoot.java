package com.yanny.ali.compatibility.jei;

import com.mojang.datafixers.util.Either;
import com.yanny.ali.api.*;
import com.yanny.ali.compatibility.common.IType;
import com.yanny.ali.plugin.client.ClientUtils;
import com.yanny.ali.plugin.client.widget.LootTableWidget;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.registries.LootCategory;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

import java.util.*;

public abstract class JeiBaseLoot<T extends IType, V> implements IRecipeCategory<T> {
    static final int CATEGORY_WIDTH = 9 * 18;
    static final int CATEGORY_HEIGHT = 7 * 18;
    private static final Map<IType, Pair<JeiWidgetWrapper, List<Holder>>> widgets = new HashMap<>();

    private final RecipeType<T> recipeType;
    private final LootCategory<V> lootCategory;
    private final Component title;
    private final IDrawable icon;
    protected final IGuiHelper guiHelper;

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

    @NotNull
    @Override
    public final IDrawable getIcon() {
        return icon;
    }

    public LootCategory<V> getLootCategory() {
        return lootCategory;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup iFocusGroup) {
        List<Holder> slotParams = new LinkedList<>();
        IWidgetUtils utils = getJeiUtils(slotParams);
        RelativeRect rect = new RelativeRect(0, getYOffset(recipe), CATEGORY_WIDTH, 0);

        widgets.put(recipe, new Pair<>(new JeiWidgetWrapper(new LootTableWidget(utils, recipe.entry(), rect, CATEGORY_WIDTH)), slotParams));

        for (int i = 0; i < slotParams.size(); i++) {
            Holder h = slotParams.get(i);
            IRecipeSlotBuilder slotBuilder = builder.addOutputSlot()
                    .setStandardSlotBackground()
                    .setSlotName(String.valueOf(i))
                    .setPosition(h.rect.getX(), h.rect.getY())
                    .addRichTooltipCallback((iRecipeSlotView, tooltipBuilder)
                            -> tooltipBuilder.addAll(NodeUtils.toComponents(h.entry().getTooltip(), 0)));
            Optional<ItemStack> left = h.item.left();
            Optional<TagKey<Item>> right = h.item.right();

            left.ifPresent(slotBuilder::addItemStack);
            right.ifPresent((t) -> slotBuilder.addIngredients(Ingredient.of(t)));
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, T recipe, IFocusGroup focuses) {
        Pair<JeiWidgetWrapper, List<Holder>> pair = widgets.remove(recipe);

        if (pair == null) {
            return;
        }

        Pair<List<IRecipeWidget>, List<IRecipeSlotDrawable>> additionalWidgets = getWidgets(builder, recipe);
        JeiWidgetWrapper widgetWrapper = pair.getA();
        List<Holder> slotParams = pair.getB();
        List<IRecipeWidget> scrollWidgets = new LinkedList<>(additionalWidgets.getA());
        List<IRecipeSlotDrawable> slotDrawables = new LinkedList<>(additionalWidgets.getB());

        widget.render(guiGraphics, (int) mouseX, (int) mouseY);
        guiGraphics.renderTooltip(Minecraft.getInstance().font, widget.getTooltipComponents((int) mouseX, (int) mouseY), Optional.empty(), (int) mouseX, (int) mouseY);

        for (int i = 0; i < slotParams.size(); i++) {
            Holder h = slotParams.get(i);

            builder.getRecipeSlots().findSlotByName(String.valueOf(i)).ifPresent((slotDrawable) -> {
                scrollWidgets.add(new JeiLootSlotWidget(slotDrawable, h.rect.getX(), h.rect.getY(), ((IItemNode) h.entry).getCount()));
                slotDrawables.add(slotDrawable);
            });
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
    private IWidgetUtils getJeiUtils(List<Holder> slotParams) {
        return new ClientUtils() {
            @Override
            public void addSlotWidget(Either<ItemStack, TagKey<Item>> item, IDataNode entry, RelativeRect rect) {
                slotParams.add(new Holder(item, entry, rect));
            }
        };
    }

    private record Holder(Either<ItemStack, TagKey<Item>> item, IDataNode entry, RelativeRect rect) {}
}
