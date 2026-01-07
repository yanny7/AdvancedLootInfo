package com.yanny.ali.jei.compatibility.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import com.yanny.ali.api.*;
import com.yanny.ali.compatibility.common.IType;
import com.yanny.ali.configuration.LootCategory;
import com.yanny.ali.plugin.client.ClientUtils;
import com.yanny.ali.plugin.common.NodeUtils;
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
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public abstract class JeiBaseLoot<T extends IType, V> implements IRecipeCategory<RecipeHolder<T>> {
    static final int CATEGORY_WIDTH = 9 * 18;
    static final int CATEGORY_HEIGHT = 7 * 18;

    private final RecipeType<RecipeHolder<T>> recipeType;
    private final LootCategory<V> lootCategory;
    private final Component title;
    private final IDrawable icon;
    protected final IGuiHelper guiHelper;

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

    @NotNull
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

        recipe.setWidget(getRootWidget(utils, recipe.type().entry(), rect, CATEGORY_WIDTH));
        recipe.setHolders(slotParams);
        recipe.type().inputs().forEach((i) -> builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addItemStack(i));
        recipe.type().outputs().forEach((i) -> builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addItemStack(i));

        for (int i = 0; i < slotParams.size(); i++) {
            Holder h = slotParams.get(i);
            IRecipeSlotBuilder slotBuilder = builder.addSlot(RecipeIngredientRole.RENDER_ONLY, h.rect.getX() + 1, h.rect.getY() + 1)
                    .setSlotName(String.valueOf(i))
                    .addTooltipCallback((iRecipeSlotView, tooltipBuilder)
                            -> tooltipBuilder.addAll(NodeUtils.toComponents(h.entry().getTooltip(), 0, Minecraft.getInstance().options.advancedItemTooltips)));
            Optional<ItemStack> left = h.item.left();
            Optional<TagKey<? extends ItemLike>> right = h.item.right();

            left.ifPresent(slotBuilder::addItemStack);
            right.ifPresent((t) -> slotBuilder.addIngredients(Ingredient.of((TagKey<Item>) t)));
        }
    }

    @Override
    public void draw(RecipeHolder<T> recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        IWidget widget = recipe.getWidget();
        List<Holder> slotParams = recipe.getHolders();

        if (widget == null || slotParams == null) {
            return;
        }

        widget.render(guiGraphics, (int) mouseX, (int) mouseY);
        guiGraphics.renderTooltip(Minecraft.getInstance().font, widget.getTooltipComponents((int) mouseX, (int) mouseY), Optional.empty(), (int) mouseX, (int) mouseY);

        for (int i = 0; i < slotParams.size(); i++) {
            Holder h = slotParams.get(i);

            guiHelper.getSlotDrawable().draw(guiGraphics, h.rect.getX(), h.rect.getY());

            recipeSlotsView.findSlotByName(String.valueOf(i)).ifPresent((slotView) -> {
                RangeValue value = ((IItemNode) h.entry).getCount();

                if (value.isRange() || value.min() > 1) {
                    Component count = Component.literal(value.toIntString());
                    boolean isRange = value.isRange();
                    Font font = Minecraft.getInstance().font;
                    PoseStack stack = guiGraphics.pose();

                    stack.pushPose();
                    stack.translate(h.rect.getX(), h.rect.getY(), 0);

                    if (isRange) {
                        stack.translate(17, 13, 200);
                        stack.pushPose();
                        stack.scale(0.5f, 0.5f, 0.5f);
                        //draw.fill(-font.width(count) - 2, -2, 2, 10, 255<<24 | 0);
                        guiGraphics.drawString(font, count, -font.width(count), 0, 16777215, false);
                        stack.popPose();
                    } else {
                        stack.translate(18, 10, 200);
                        guiGraphics.drawString(font, count, -font.width(count), 0, 16777215, true);
                    }

                    stack.popPose();
                }
            });
        }
    }

    @Override
    public int getWidth() {
        return CATEGORY_WIDTH;
    }

    @Override
    public int getHeight() {
        return 1024;
    }

    abstract int getYOffset(T recipe);

    abstract IWidget getRootWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth);

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
