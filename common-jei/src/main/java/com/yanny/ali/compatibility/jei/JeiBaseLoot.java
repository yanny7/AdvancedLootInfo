package com.yanny.ali.compatibility.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import com.yanny.ali.api.IWidget;
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
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

import java.util.*;

public abstract class JeiBaseLoot<T extends IType, V> implements IRecipeCategory<T> {
    private static final Map<IType, Pair<IWidget, List<ISlotParams>>> widgets = new HashMap<>();

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
        List<ISlotParams> slotParams = new LinkedList<>();
        IWidgetUtils utils = getJeiUtils(slotParams);

        widgets.put(recipe, new Pair<>(new LootTableWidget(utils, recipe.entry(), 0, getYOffset(recipe)), slotParams));

        for (int i = 0; i < slotParams.size(); i++) {
            ISlotParams p = slotParams.get(i);

            if (p instanceof ItemSlotParams itemSlotParams) {
                builder.addSlot(RecipeIngredientRole.OUTPUT, itemSlotParams.x + 1, itemSlotParams.y + 1)
                        .setSlotName(String.valueOf(i))
                        .addTooltipCallback((iRecipeSlotView, components)
                                -> components.addAll(EntryTooltipUtils.getTooltip(utils, p.entry(), p.chance(), p.count(), p.allFunctions(), p.allConditions())))
                        .addItemStack(itemSlotParams.item);
            } else if (p instanceof TagSlotParams tagSlotParams) {
                builder.addSlot(RecipeIngredientRole.OUTPUT, tagSlotParams.x + 1, tagSlotParams.y + 1)
                        .setSlotName(String.valueOf(i))
                        .addTooltipCallback((iRecipeSlotView, components)
                                -> components.addAll(EntryTooltipUtils.getTooltip(utils, p.entry(), p.chance(), p.count(), p.allFunctions(), p.allConditions())))
                        .addIngredients(Ingredient.of(tagSlotParams.item));
            }
        }
    }

    @Override
    public void draw(T recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Pair<IWidget, List<ISlotParams>> pair = widgets.get(recipe);

        if (pair == null) {
            return;
        }

        IWidget widget = pair.getA();
        List<ISlotParams> slotParams = pair.getB();

        widget.render(guiGraphics, (int) mouseX, (int) mouseY);
        guiGraphics.renderTooltip(Minecraft.getInstance().font, widget.getTooltipComponents((int) mouseX, (int) mouseY), Optional.empty(), (int) mouseX, (int) mouseY);

        for (int i = 0; i < slotParams.size(); i++) {
            ISlotParams p = slotParams.get(i);

            guiHelper.getSlotDrawable().draw(guiGraphics, p.x(), p.y());

            recipeSlotsView.findSlotByName(String.valueOf(i)).ifPresent((slotView) -> {
                RangeValue value = p.count().get(null).get(0);

                if (value.isRange() || value.min() > 1) {
                    Component count = Component.literal(value.toIntString());
                    boolean isRange = value.isRange();
                    Font font = Minecraft.getInstance().font;
                    PoseStack stack = guiGraphics.pose();

                    stack.pushPose();
                    stack.translate(p.x(), p.y(), 0);

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
        return 9 * 18;
    }

    @Override
    public int getHeight() {
        return 1024;
    }

    abstract int getYOffset(T recipe);

    @NotNull
    private IWidgetUtils getJeiUtils(List<ISlotParams> slotParams) {
        return new ClientUtils() {
            @Override
            public Rect addSlotWidget(Item item, LootPoolEntryContainer entry, int x, int y, Map<Holder<Enchantment>, Map<Integer, RangeValue>> chance,
                                      Map<Holder<Enchantment>, Map<Integer, RangeValue>> count, List<LootItemFunction> allFunctions, List<LootItemCondition> allConditions) {
                ItemStack itemStack = TooltipUtils.getItemStack(this, entry, item);
                slotParams.add(new ItemSlotParams(itemStack, entry, x, y, chance, count, allFunctions, allConditions));
                return new Rect(x, y, 18, 18);
            }

            @Override
            public Rect addSlotWidget(TagKey<Item> item, LootPoolEntryContainer entry, int x, int y, Map<Holder<Enchantment>, Map<Integer, RangeValue>> chance,
                                      Map<Holder<Enchantment>, Map<Integer, RangeValue>> count, List<LootItemFunction> allFunctions, List<LootItemCondition> allConditions) {
                slotParams.add(new TagSlotParams(item, entry, x, y, chance, count, allFunctions, allConditions));
                return new Rect(x, y, 18, 18);
            }
        };
    }

    private interface ISlotParams {
        LootPoolEntryContainer entry();
        int x();
        int y();
        Map<Holder<Enchantment>, Map<Integer, RangeValue>> chance();
        Map<Holder<Enchantment>, Map<Integer, RangeValue>> count();
        List<LootItemFunction> allFunctions();
        List<LootItemCondition> allConditions();
    }

    private record ItemSlotParams (
            ItemStack item,
            LootPoolEntryContainer entry,
            int x,
            int y,
            Map<Holder<Enchantment>, Map<Integer, RangeValue>> chance,
            Map<Holder<Enchantment>, Map<Integer, RangeValue>> count,
            List<LootItemFunction> allFunctions,
            List<LootItemCondition> allConditions
    ) implements ISlotParams {}

    private record TagSlotParams (
            TagKey<Item> item,
            LootPoolEntryContainer entry,
            int x,
            int y,
            Map<Holder<Enchantment>, Map<Integer, RangeValue>> chance,
            Map<Holder<Enchantment>, Map<Integer, RangeValue>> count,
            List<LootItemFunction> allFunctions,
            List<LootItemCondition> allConditions
    ) implements ISlotParams {}
}
