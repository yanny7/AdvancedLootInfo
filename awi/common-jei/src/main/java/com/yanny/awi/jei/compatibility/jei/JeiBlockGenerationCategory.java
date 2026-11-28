package com.yanny.awi.jei.compatibility.jei;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.api.Rect;
import com.yanny.aci.api.RelativeRect;
import com.yanny.awi.compatibility.BlockGeneration;
import com.yanny.awi.compatibility.GenericUtils;
import com.yanny.awi.plugin.client.widget.BlockGenerationWidget;
import com.yanny.awi.plugin.client.widget.LabelWidget;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class JeiBlockGenerationCategory implements IRecipeCategory<BlockGeneration.Page> {
    private static final int HEADER_HEIGHT = 22;
    private static final int SLOT_SIZE = 18;
    private static final String SLOT_PREFIX = "block_";

    private final RecipeType<BlockGeneration.Page> recipeType;
    private final Component title;
    private final IDrawable icon;

    public JeiBlockGenerationCategory(RecipeType<BlockGeneration.Page> recipeType, Component title, IDrawable icon) {
        this.recipeType = recipeType;
        this.title = title;
        this.icon = icon;
    }

    @NotNull
    @Override
    public RecipeType<BlockGeneration.Page> getRecipeType() {
        return recipeType;
    }

    @NotNull
    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return JeiBaseLoot.CATEGORY_WIDTH + JeiScrollWidget.getScrollbarExtraWidth();
    }

    @Override
    public int getHeight() {
        return JeiBaseLoot.MAX_SAFE_HEIGHT;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BlockGeneration.Page page, IFocusGroup focuses) {
        for (int i = 0; i < page.blocks().size(); i++) {
            Block block = page.blocks().get(i);
            boolean model = GenericUtils.rendersAsBlockModel(block);
            IRecipeSlotBuilder slot = builder.addSlot(model ? RecipeIngredientRole.RENDER_ONLY : RecipeIngredientRole.OUTPUT)
                    .setStandardSlotBackground()
                    .setSlotName(SLOT_PREFIX + i)
                    .setPosition(i * SLOT_SIZE, 0);

            if (GenericUtils.rendersAsFluid(block)) {
                slot.addFluidStack(block.defaultBlockState().getFluidState().getType());
            } else if (!model) {
                slot.addItemLike(block);
            }
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, BlockGeneration.Page page, IFocusGroup focuses) {
        BlockGenerationWidget content = new BlockGenerationWidget(new RelativeRect(0, HEADER_HEIGHT, 0, 0), JeiBaseLoot.CATEGORY_WIDTH, page.dimensions());
        int labelX = page.blocks().size() * SLOT_SIZE + 4;
        LabelWidget label = new LabelWidget(new RelativeRect(labelX, 4, 0, 0), page.blocks().get(0).getName(), JeiBaseLoot.CATEGORY_WIDTH - labelX,
                LabelWidget.COLOR, List.of());
        List<IRecipeWidget> widgets = new LinkedList<>();
        List<IRecipeSlotDrawable> slotDrawables = new LinkedList<>();

        for (int i = 0; i < page.blocks().size(); i++) {
            Block block = page.blocks().get(i);
            int x = i * SLOT_SIZE;

            builder.getRecipeSlots().findSlotByName(SLOT_PREFIX + i).ifPresent((slotDrawable) -> {
                if (GenericUtils.rendersAsBlockModel(block)) {
                    widgets.add(new JeiBlockSlotWidget(slotDrawable, block, x, 0));
                } else {
                    widgets.add(new JeiLootSlotWidget(slotDrawable, x, 0, NumberExpr.constant(1)));
                }

                slotDrawables.add(slotDrawable);
            });
        }

        widgets.add(new JeiWidgetWrapper(label));
        widgets.add(new JeiWidgetWrapper(content));

        Rect renderRect = new Rect(0, 0, getWidth(), getHeight());
        JeiScrollWidget scrollWidget = new JeiScrollWidget(renderRect, JeiBaseLoot.CATEGORY_WIDTH, HEADER_HEIGHT + content.getRect().getHeight(), widgets);

        builder.addSlottedWidget(scrollWidget, slotDrawables);
        builder.addInputHandler(scrollWidget);
    }
}
