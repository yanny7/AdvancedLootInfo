package com.yanny.awi.jei.compatibility.jei;

import com.yanny.aci.api.IWidget;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.api.Rect;
import com.yanny.aci.api.RelativeRect;
import com.yanny.aci.tooltip.CoreTooltipUtils;
import com.yanny.aci.tooltip.TooltipNodePalette;
import com.yanny.awi.api.IDataNode;
import com.yanny.awi.api.IWidgetUtils;
import com.yanny.awi.manager.PluginManager;
import com.yanny.awi.plugin.client.ClientUtils;
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
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;

import java.util.LinkedList;
import java.util.List;

public abstract class JeiBaseLoot implements IRecipeCategory<RecipeHolder> {
    static final int CATEGORY_WIDTH = 9 * 18;
    static final int CATEGORY_HEIGHT = 7 * 18;

    protected final IGuiHelper guiHelper;
    private final RecipeType<RecipeHolder> recipeType;
    private final Component title;
    private final IDrawable icon;

    public JeiBaseLoot(IGuiHelper guiHelper, RecipeType<RecipeHolder> recipeType, Component title, IDrawable icon) {
        this.guiHelper = guiHelper;
        this.recipeType = recipeType;
        this.title = title;
        this.icon = icon;
    }

    @NotNull
    @Override
    public final RecipeType<RecipeHolder> getRecipeType() {
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

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder recipe, IFocusGroup iFocusGroup) {
        List<Holder> slotParams = new LinkedList<>();
        IWidgetUtils utils = getJeiUtils(slotParams);
        RelativeRect rect = new RelativeRect(0, getYOffset(), CATEGORY_WIDTH, 0);

        recipe.setWidgetWrapper(new JeiWidgetWrapper(getRootWidget(utils, recipe.getEntry(), rect, CATEGORY_WIDTH)));
        recipe.setHolders(slotParams);
//        recipe.type().inputs().forEach((i) -> builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addItemStack(i));
        recipe.getBlocks().forEach((i) -> builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addItemLike(i));

        for (int i = 0; i < slotParams.size(); i++) {
            Holder h = slotParams.get(i);
            IRecipeSlotBuilder slotBuilder = builder.addSlot(RecipeIngredientRole.RENDER_ONLY)
                    .setStandardSlotBackground()
                    .setSlotName(String.valueOf(i))
                    .setPosition(h.rect.getX(), h.rect.getY())
                    .addRichTooltipCallback((iRecipeSlotView, tooltipBuilder)
                            -> tooltipBuilder.addAll(CoreTooltipUtils.toComponents(h.entry().getTooltip(), 0, Minecraft.getInstance().options.advancedItemTooltips)));
            slotBuilder.addItemLike(h.block);
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, RecipeHolder recipe, IFocusGroup focuses) {
        JeiWidgetWrapper widgetWrapper = recipe.getWidgetWrapper();
        List<Holder> slotParams = recipe.getHolders();

        if (widgetWrapper == null || slotParams == null) {
            return;
        }

        Pair<List<IRecipeWidget>, List<IRecipeSlotDrawable>> additionalWidgets = getWidgets(builder, recipe);
        List<IRecipeWidget> scrollWidgets = new LinkedList<>(additionalWidgets.getA());
        List<IRecipeSlotDrawable> slotDrawables = new LinkedList<>(additionalWidgets.getB());

        scrollWidgets.add(widgetWrapper);

        for (int i = 0; i < slotParams.size(); i++) {
            Holder h = slotParams.get(i);

            builder.getRecipeSlots().findSlotByName(String.valueOf(i)).ifPresent((slotDrawable) -> {
                scrollWidgets.add(new JeiLootSlotWidget(slotDrawable, h.rect.getX(), h.rect.getY(), new RangeValue(1)));
                slotDrawables.add(slotDrawable);
            });
        }

        Rect renderRect = new Rect(0, 0, CATEGORY_WIDTH + JeiScrollWidget.getScrollbarExtraWidth(), CATEGORY_HEIGHT);
        JeiScrollWidget scrollWidget = new JeiScrollWidget(renderRect, widgetWrapper.getRect().height() + getYOffset(), scrollWidgets);

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

    abstract Pair<List<IRecipeWidget>, List<IRecipeSlotDrawable>> getWidgets(IRecipeExtrasBuilder builder, RecipeHolder recipe);

    abstract int getYOffset();

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
            public void addSlotWidget(Block block, IDataNode node, RelativeRect rect) {
                slotParams.add(new Holder(block, node, rect));
            }
        };
    }

    public record Holder(Block block, IDataNode entry, RelativeRect rect) {
    }
}
