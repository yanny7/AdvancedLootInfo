package com.yanny.awi.jei.compatibility.jei;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.IWidget;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.api.Rect;
import com.yanny.aci.api.RelativeRect;
import com.yanny.aci.tooltip.CoreTooltipUtils;
import com.yanny.aci.tooltip.TooltipNodePalette;
import com.yanny.awi.api.IBlockNode;
import com.yanny.awi.api.IDataNode;
import com.yanny.awi.api.IWidgetUtils;
import com.yanny.awi.compatibility.GenericUtils;
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
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;

import java.util.LinkedList;
import java.util.List;

public abstract class JeiBaseLoot implements IRecipeCategory<RecipeHolder> {
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
    private final IRecipeType<RecipeHolder> recipeType;
    private final Component title;
    private final IDrawable icon;

    public JeiBaseLoot(IGuiHelper guiHelper, IRecipeType<RecipeHolder> recipeType, Component title, IDrawable icon) {
        this.guiHelper = guiHelper;
        this.recipeType = recipeType;
        this.title = title;
        this.icon = icon;
    }

    @NotNull
    @Override
    public final IRecipeType<RecipeHolder> getRecipeType() {
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
//        recipe.type().inputs().forEach((i) -> builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).add(i));
        recipe.getBlocks().forEach((i) -> builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).add(i));

        for (int i = 0; i < slotParams.size(); i++) {
            Holder h = slotParams.get(i);
            IRecipeSlotBuilder slotBuilder = builder.addSlot(RecipeIngredientRole.RENDER_ONLY)
                    .setStandardSlotBackground()
                    .setSlotName(String.valueOf(i))
                    .setPosition(h.rect.getX(), h.rect.getY())
                    .addRichTooltipCallback((iRecipeSlotView, tooltipBuilder)
                            -> tooltipBuilder.addAll(CoreTooltipUtils.toComponents(h.entry().getTooltip(), 0, Minecraft.getInstance().options.advancedItemTooltips)));


            // A tag adds every member to the same slot, which is what makes JEI cycle through them. Item-less blocks
            // with a model of their own are drawn by JeiBlockSlotWidget below and contribute no ingredient.
            for (Block block : ingredientBlocks(h)) {
                if (GenericUtils.rendersAsFluid(block)) {
                    slotBuilder.addFluidStack(block.defaultBlockState().getFluidState().getType());
                } else {
                    slotBuilder.addItemLike(block);
                }
            }
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
                List<Block> models = modelBlocks(h);

                if (!models.isEmpty() && ingredientBlocks(h).isEmpty()) {
                    scrollWidgets.add(new JeiBlockSlotWidget(slotDrawable, models.get(0), h.rect.getX(), h.rect.getY()));
                } else {
                    scrollWidgets.add(new JeiLootSlotWidget(slotDrawable, h.rect.getX(), h.rect.getY(), new RangeValue(1)));
                }

                slotDrawables.add(slotDrawable);
            });
        }

        Rect renderRect = new Rect(0, 0, CATEGORY_WIDTH + JeiScrollWidget.getScrollbarExtraWidth(), MAX_SAFE_HEIGHT);
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
        return MAX_SAFE_HEIGHT;
    }

    /**
     * A block with no item form and no fluid state can only be shown as a 3D block model
     * (see {@link JeiBlockSlotWidget}) rather than as a JEI item/fluid ingredient.
     */
    static boolean isBlockModel(Block block) {
        return GenericUtils.rendersAsBlockModel(block);
    }

    abstract Pair<List<IRecipeWidget>, List<IRecipeSlotDrawable>> getWidgets(IRecipeExtrasBuilder builder, RecipeHolder recipe);

    abstract int getYOffset();

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
                    guiGraphics.drawString(Minecraft.getInstance().font, component, x - width / 2, y, 0, false);
                } else {
                    guiGraphics.drawString(Minecraft.getInstance().font, component, x, y, 0, false);
                }
            }
        };
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
            public void addSlotWidget(Either<Block, TagKey<Block>> block, IDataNode node, RelativeRect rect) {
                slotParams.add(new Holder(block, node, rect));
            }
        };
    }

    /** Members JEI can hold as an ingredient - everything except item-less blocks that only have a model. */
    @NotNull
    private static List<Block> ingredientBlocks(Holder holder) {
        return ((IBlockNode) holder.entry).getBlocks().stream().filter((b) -> !isBlockModel(b)).toList();
    }

    @NotNull
    private static List<Block> modelBlocks(Holder holder) {
        return ((IBlockNode) holder.entry).getBlocks().stream().filter(JeiBaseLoot::isBlockModel).toList();
    }

    public record Holder(Either<Block, TagKey<Block>> block, IDataNode entry, RelativeRect rect) {
    }
}
