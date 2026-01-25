package com.yanny.ali.jei.compatibility.jei;

import com.mojang.datafixers.util.Either;
import com.yanny.ali.api.*;
import com.yanny.ali.compatibility.common.IType;
import com.yanny.ali.configuration.LootCategory;
import com.yanny.ali.plugin.client.ClientUtils;
import com.yanny.ali.plugin.common.NodeUtils;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class JeiBaseLoot<T extends IType, V> implements IRecipeCategory<RecipeHolder<T>> {
    static final int CATEGORY_WIDTH = 9 * 18;
    static final int CATEGORY_HEIGHT = 7 * 18;

    protected final IGuiHelper guiHelper;
    private final IRecipeType<RecipeHolder<T>> recipeType;
    private final LootCategory<V> lootCategory;
    private final Component title;
    private final IDrawable icon;

    public JeiBaseLoot(IGuiHelper guiHelper, IRecipeType<RecipeHolder<T>> recipeType, LootCategory<V> lootCategory, Component title, IDrawable icon) {
        this.guiHelper = guiHelper;
        this.recipeType = recipeType;
        this.lootCategory = lootCategory;
        this.title = title;
        this.icon = icon;
    }

    @NotNull
    @Override
    public final IRecipeType<RecipeHolder<T>> getRecipeType() {
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
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<T> recipe, IFocusGroup iFocusGroup) {
        List<Holder> slotParams = new LinkedList<>();
        IWidgetUtils utils = getJeiUtils(slotParams);
        RelativeRect rect = new RelativeRect(0, getYOffset(recipe.type()), CATEGORY_WIDTH, 0);

        recipe.setWidgetWrapper(new JeiWidgetWrapper(getRootWidget(utils, recipe.type().entry(), rect, CATEGORY_WIDTH)));
        recipe.setHolders(slotParams);
        recipe.type().inputs().forEach((i) -> builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).add(i));
        recipe.type().outputs().forEach((i) -> builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).add(i));

        for (int i = 0; i < slotParams.size(); i++) {
            Holder h = slotParams.get(i);
            IRecipeSlotBuilder slotBuilder = builder.addSlot(RecipeIngredientRole.RENDER_ONLY)
                    .setStandardSlotBackground()
                    .setSlotName(String.valueOf(i))
                    .setPosition(h.rect.getX(), h.rect.getY())
                    .addRichTooltipCallback((iRecipeSlotView, tooltipBuilder)
                            -> tooltipBuilder.addAll(NodeUtils.toComponents(h.entry().getTooltip(), 0, Minecraft.getInstance().options.advancedItemTooltips)));
            Optional<ItemStack> left = h.item.left();
            Optional<TagKey<? extends ItemLike>> right = h.item.right();

            left.ifPresent(slotBuilder::add);
            right.ifPresent((t) -> slotBuilder.add(Ingredient.of(BuiltInRegistries.ITEM.get((TagKey<Item>) t).map((f) -> f.stream().map(net.minecraft.core.Holder::value)).orElse(Stream.of()))));
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, RecipeHolder<T> recipe, IFocusGroup focuses) {
        JeiWidgetWrapper widgetWrapper = recipe.getWidgetWrapper();
        List<Holder> slotParams = recipe.getHolders();

        if (widgetWrapper == null || slotParams == null) {
            return;
        }

        Pair<List<IRecipeWidget>, List<IRecipeSlotDrawable>> additionalWidgets = getWidgets(builder, recipe.type());
        List<IRecipeWidget> scrollWidgets = new LinkedList<>(additionalWidgets.getA());
        List<IRecipeSlotDrawable> slotDrawables = new LinkedList<>(additionalWidgets.getB());

        scrollWidgets.add(widgetWrapper);

        for (int i = 0; i < slotParams.size(); i++) {
            Holder h = slotParams.get(i);

            builder.getRecipeSlots().findSlotByName(String.valueOf(i)).ifPresent((slotDrawable) -> {
                scrollWidgets.add(new JeiLootSlotWidget(slotDrawable, h.rect.getX(), h.rect.getY(), ((IItemNode) h.entry).getCount()));
                slotDrawables.add(slotDrawable);
            });
        }

        Rect renderRect = new Rect(0, 0, CATEGORY_WIDTH + JeiScrollWidget.getScrollbarExtraWidth(), CATEGORY_HEIGHT);
        JeiScrollWidget scrollWidget = new JeiScrollWidget(renderRect, widgetWrapper.getRect().height() + getYOffset(recipe.type()), scrollWidgets);

        builder.addSlottedWidget(scrollWidget, slotDrawables);
        builder.addInputHandler(scrollWidget);
    }

    @Override
    public int getWidth() {
        return CATEGORY_WIDTH + JeiScrollWidget.getScrollbarExtraWidth();
    }

    @Override
    public int getHeight() {
        return CATEGORY_HEIGHT;
    }

    abstract Pair<List<IRecipeWidget>, List<IRecipeSlotDrawable>> getWidgets(IRecipeExtrasBuilder builder, T recipe);

    abstract int getYOffset(T recipe);

    abstract IWidget getRootWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth);

    @NotNull
    protected IRecipeWidget createTextWidget(Component component, int x, int y, boolean centered) {
        return new IRecipeWidget() {
            private final ScreenPosition position = new ScreenPosition(x, y);

            @NotNull
            @Override
            public ScreenPosition getPosition() {
                return position;
            }

            @Override
            public void drawWidget(GuiGraphics guiGraphics, double mouseX, double mouseY) {
                if (centered) {
                    int width = Minecraft.getInstance().font.width(component);
                    guiGraphics.drawString(Minecraft.getInstance().font, component, x - width / 2, 0, 0, false);
                } else {
                    guiGraphics.drawString(Minecraft.getInstance().font, component, x, 0, 0, false);
                }
            }
        };
    }

    @NotNull
    private IWidgetUtils getJeiUtils(List<Holder> slotParams) {
        return new ClientUtils() {
            @Override
            public void addSlotWidget(Either<ItemStack, TagKey<? extends ItemLike>> item, IDataNode entry, RelativeRect rect) {
                slotParams.add(new Holder(item, entry, rect));
            }
        };
    }

    public record Holder(Either<ItemStack, TagKey<? extends ItemLike>> item, IDataNode entry, RelativeRect rect) {
    }
}
