package com.yanny.ali.jei.compatibility.jei;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.IWidget;
import com.yanny.aci.api.Rect;
import com.yanny.aci.api.RelativeRect;
import com.yanny.aci.compatibility.ScrollableTooltip;
import com.yanny.aci.tooltip.CoreTooltipUtils;
import com.yanny.aci.tooltip.TooltipLine;
import com.yanny.aci.tooltip.TooltipNodePalette;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IItemNode;
import com.yanny.ali.api.IWidgetUtils;
import com.yanny.ali.compatibility.common.IType;
import com.yanny.ali.configuration.LootCategory;
import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.plugin.client.ClientUtils;
import com.yanny.ali.plugin.client.TooltipUtils;
import com.yanny.ali.plugin.client.WidgetUtils;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;

import java.util.LinkedList;
import java.util.List;

public abstract class JeiBaseLoot<T extends IType, V> implements IRecipeCategory<RecipeHolder<T>> {
    static final int CATEGORY_WIDTH = 9 * 18;
    /**
     * JEI reports the height per <i>category</i>, not per recipe, and does not scissor recipe drawing - anything
     * taller than the recipe area is drawn over the JEI window frame. So this is the largest height JEI is
     * guaranteed to be able to give us: the smallest allowed {@code appearance.RecipeGuiHeight} (175) minus the
     * header/padding JEI reserves around the recipe area (40) minus the per-recipe border (8) = 127.
     * Verified against JEI 15.20.0.105 - re-check {@code RecipesGui}/{@code RecipeGuiLogic} on other branches.
     */
    static final int MAX_SAFE_HEIGHT = 7 * 18;

    protected final IGuiHelper guiHelper;
    private final RecipeType<RecipeHolder<T>> recipeType;
    private final LootCategory<V> lootCategory;
    private final Component title;
    private final IDrawable icon;

    public JeiBaseLoot(IGuiHelper guiHelper, RecipeType<RecipeHolder<T>> recipeType, LootCategory<V> lootCategory, Component title, IDrawable icon) {
        this.guiHelper = guiHelper;
        this.recipeType = recipeType;
        this.lootCategory = lootCategory;
        this.title = title;
        this.icon = icon;
    }

    @NotNull
    @Override
    public final RecipeType<RecipeHolder<T>> getRecipeType() {
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
        List<Holder> slotParams = layout(recipe).slots();

        recipe.type().inputs().forEach((i) -> builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addItemStack(i));
        recipe.type().outputs().forEach((i) -> builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addItemStack(i));

        for (int i = 0; i < slotParams.size(); i++) {
            Holder h = slotParams.get(i);
            IRecipeSlotBuilder slotBuilder = builder.addSlot(RecipeIngredientRole.RENDER_ONLY)
                    .setBackground(getSlotBackground(h.options.stream().anyMatch((o) -> ((IItemNode) o).hasPredicates())), -1, -1)
                    .setSlotName(String.valueOf(i))
                    .setPosition(h.rect.getX(), h.rect.getY())
                    .addRichTooltipCallback((iRecipeSlotView, tooltipBuilder) -> {
                        IDataNode option = TooltipUtils.getDisplayedOption(h.options, iRecipeSlotView.getDisplayedItemStack().orElse(ItemStack.EMPTY));
                        List<TooltipLine> lines = CoreTooltipUtils.toLines(option.getTooltip(), 0, Minecraft.getInstance().options.advancedItemTooltips, TooltipUtils.getStyle(), TooltipUtils.getNumberOptions());

                        if (!lines.isEmpty()) {
                            tooltipBuilder.add(new ScrollableTooltip(lines));
                        }
                    });

            for (IDataNode option : h.options) {
                Either<ItemStack, TagKey<? extends ItemLike>> item = ((IItemNode) option).getItem();

                item.left().ifPresent(slotBuilder::addItemStack);
                //noinspection unchecked
                item.right().ifPresent((t) -> slotBuilder.addIngredients(Ingredient.of((TagKey<Item>) t)));
            }
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, RecipeHolder<T> recipe, IFocusGroup focuses) {
        Layout layout = layout(recipe);
        JeiWidgetWrapper widgetWrapper = layout.widget();
        List<Holder> slotParams = layout.slots();
        Pair<List<IRecipeWidget>, List<IRecipeSlotDrawable>> additionalWidgets = getWidgets(builder, recipe.type());
        List<IRecipeWidget> scrollWidgets = new LinkedList<>(additionalWidgets.getA());
        List<IRecipeSlotDrawable> slotDrawables = new LinkedList<>(additionalWidgets.getB());

        scrollWidgets.add(widgetWrapper);

        for (int i = 0; i < slotParams.size(); i++) {
            Holder h = slotParams.get(i);

            builder.getRecipeSlots().findSlotByName(String.valueOf(i)).ifPresent((slotDrawable) -> {
                scrollWidgets.add(new JeiLootSlotWidget(slotDrawable, h.rect.getX(), h.rect.getY(), (s) -> TooltipUtils.getSlotCount((IItemNode) TooltipUtils.getDisplayedOption(h.options, s))));
                slotDrawables.add(slotDrawable);
            });
        }

        Rect renderRect = new Rect(0, 0, CATEGORY_WIDTH + JeiScrollWidget.getScrollbarExtraWidth(), MAX_SAFE_HEIGHT);
        JeiScrollWidget scrollWidget = new JeiScrollWidget(renderRect, widgetWrapper.getRect().width(), Math.max(widgetWrapper.getRect().height() + getYOffset(recipe.type()), getMinContentHeight(recipe.type())), scrollWidgets);

        builder.addSlottedWidget(scrollWidget, slotDrawables);
        builder.addInputHandler(scrollWidget);
    }

    @Override
    public int getWidth() {
        return CATEGORY_WIDTH + JeiScrollWidget.getScrollbarExtraWidth();
    }

    @Override
    public int getHeight() {
        return MAX_SAFE_HEIGHT;
    }

    abstract Pair<List<IRecipeWidget>, List<IRecipeSlotDrawable>> getWidgets(IRecipeExtrasBuilder builder, T recipe);

    abstract int getYOffset(T recipe);

    int getMinContentHeight(T recipe) {
        return 0;
    }

    abstract IWidget getRootWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth);

    @NotNull
    protected IRecipeWidget createTextWidget(Component component, int x, int y, boolean centered) {
        return guiHelper.createWidgetFromDrawable(new IDrawable() {
            @Override
            public int getWidth() {
                return CATEGORY_WIDTH;
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
        }, x, 0);
    }

    @NotNull
    private IDrawable getSlotBackground(boolean hasPredicates) {
        // deliberately two separate returns - a ternary/joined branch makes the architectury transformer
        // resolve the common supertype of both, which fails when JEI is not on the classpath (EMI/REI runs)
        if (hasPredicates) {
            return new PredicatesSlotBackground(guiHelper.getSlotDrawable());
        }

        return guiHelper.getSlotDrawable();
    }

    @NotNull
    private Layout layout(RecipeHolder<T> recipe) {
        List<Holder> slotParams = new LinkedList<>();
        RelativeRect rect = new RelativeRect(0, getYOffset(recipe.type()), CATEGORY_WIDTH, 0);
        JeiWidgetWrapper widget = new JeiWidgetWrapper(getRootWidget(getJeiUtils(slotParams), recipe.type().entry(), rect, CATEGORY_WIDTH));

        return new Layout(widget, slotParams);
    }

    @NotNull
    private IWidgetUtils getJeiUtils(List<Holder> slotParams) {
        return new ClientUtils() {
            @Nullable
            @Override
            public String getTranslationKey(int index) {
                return null;
            }

            @NotNull
            @Override
            public TooltipNodePalette getTooltipCache() {
                return PluginManager.getInstance().clientRegistry.getTooltipCache();
            }

            @Override
            public void addSlotWidget(List<IDataNode> options, RelativeRect rect) {
                slotParams.add(new Holder(options, rect));
            }
        };
    }

    public record Holder(List<IDataNode> options, RelativeRect rect) {
    }

    private record Layout(JeiWidgetWrapper widget, List<Holder> slots) {}

    /** Standard slot background tinted to mark an entry gated by predicates - drawn as background, so it stays below the item. */
    private record PredicatesSlotBackground(IDrawable slot) implements IDrawable {
        @Override
        public int getWidth() {
            return slot.getWidth();
        }

        @Override
        public int getHeight() {
            return slot.getHeight();
        }

        @Override
        public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset) {
            slot.draw(guiGraphics, xOffset, yOffset);
            guiGraphics.fill(xOffset + 1, yOffset + 1, xOffset + getWidth() - 1, yOffset + getHeight() - 1, WidgetUtils.PREDICATES_COLOR);
        }
    }
}
