package com.yanny.ali.compatibility.jei;

import com.mojang.blaze3d.vertex.PoseStack;
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
    private static final Map<IType, Pair<IWidget, List<Holder>>> widgets = new HashMap<>();

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

        widgets.put(recipe, new Pair<>(new LootTableWidget(utils, recipe.entry(), rect, CATEGORY_WIDTH), slotParams));

        for (int i = 0; i < slotParams.size(); i++) {
            Holder h = slotParams.get(i);
            IRecipeSlotBuilder slotBuilder = builder.addSlot(RecipeIngredientRole.OUTPUT, h.rect.getX() + 1, h.rect.getY() + 1)
                    .setSlotName(String.valueOf(i))
                    .addTooltipCallback((iRecipeSlotView, tooltipBuilder)
                            -> tooltipBuilder.addAll(NodeUtils.toComponents(h.entry().getTooltip(), 0)));
            Optional<ItemStack> left = h.item.left();
            Optional<TagKey<Item>> right = h.item.right();

            left.ifPresent(slotBuilder::addItemStack);
            right.ifPresent((t) -> slotBuilder.addIngredients(Ingredient.of(t)));
        }
    }

    @Override
    public void draw(T recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Pair<IWidget, List<Holder>> pair = widgets.get(recipe);

        if (pair == null) {
            return;
        }

        IWidget widget = pair.getA();
        List<Holder> slotParams = pair.getB();

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
