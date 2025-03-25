package com.yanny.ali.compatibility.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.yanny.ali.api.*;
import com.yanny.ali.compatibility.common.IType;
import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.plugin.GenericTooltipUtils;
import com.yanny.ali.plugin.widget.LootTableWidget;
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
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class JeiBaseLoot<T extends IType, V> implements IRecipeCategory<T> {
    private final RecipeType<T> recipeType;
    private final LootCategory<V> lootCategory;
    private final Component title;
    private final IDrawable icon;
    protected final IGuiHelper guiHelper;
    @Nullable
    private IWidget widget;
    private List<ISlotParams> slotParams;

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
        widget = new LootTableWidget(getJeiUtils(), recipe.entry(), 0, getYOffset(recipe));

        for (int i = 0; i < slotParams.size(); i++) {
            ISlotParams p = slotParams.get(i);

            if (p instanceof ItemSlotParams itemSlotParams) {
                builder.addSlot(RecipeIngredientRole.OUTPUT, itemSlotParams.x + 1, itemSlotParams.y + 1)
                        .setSlotName(String.valueOf(i))
                        .addTooltipCallback((iRecipeSlotView, components)
                                -> components.addAll(GenericTooltipUtils.getTooltip(p.entry(), p.chance(), p.bonusChance(), p.count(), p.bonusCount(), p.allFunctions(), p.allConditions())))
                        .addItemStack(itemSlotParams.item.getDefaultInstance());
            } else if (p instanceof TagSlotParams tagSlotParams) {
                builder.addSlot(RecipeIngredientRole.OUTPUT, tagSlotParams.x + 1, tagSlotParams.y + 1)
                        .setSlotName(String.valueOf(i))
                        .addTooltipCallback((iRecipeSlotView, components)
                                -> components.addAll(GenericTooltipUtils.getTooltip(p.entry(), p.chance(), p.bonusChance(), p.count(), p.bonusCount(), p.allFunctions(), p.allConditions())))
                        .addIngredients(Ingredient.of(tagSlotParams.item));
            }
        }
    }

    @Override
    public void draw(T recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (widget != null) {
            widget.render(guiGraphics, (int) mouseX, (int) mouseY);
            guiGraphics.renderTooltip(Minecraft.getInstance().font, widget.getTooltipComponents((int) mouseX, (int) mouseY), Optional.empty(), (int) mouseX, (int) mouseY);
        }

        for (int i = 0; i < slotParams.size(); i++) {
            ISlotParams p = slotParams.get(i);

            guiHelper.getSlotDrawable().draw(guiGraphics, p.x(), p.y());

            recipeSlotsView.findSlotByName(String.valueOf(i)).ifPresent((slotView) -> {
                if (p.count().isRange() || p.count().min() > 1) {
                    Component count = Component.literal(p.count().toIntString());
                    boolean isRange = p.count().isRange();
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
    private IWidgetUtils getJeiUtils() {
        slotParams = new LinkedList<>();

        return new IWidgetUtils() {
            @Override
            public Pair<List<IEntryWidget>, Rect> createWidgets(IWidgetUtils registry, List<ILootEntry> entries, int x, int y, List<ILootFunction> functions, List<ILootCondition> conditions) {
                return PluginManager.CLIENT_REGISTRY.createWidgets(registry, entries, x, y, functions, conditions);
            }

            @Override
            public Rect getBounds(IClientUtils registry, List<ILootEntry> entries, int x, int y) {
                return PluginManager.CLIENT_REGISTRY.getBounds(registry, entries, x, y);
            }

            @Nullable
            @Override
            public WidgetDirection getWidgetDirection(ILootEntry entry) {
                return PluginManager.CLIENT_REGISTRY.getWidgetDirection(entry);
            }

            @Override
            public Rect addSlotWidget(Item item, ILootEntry entry, int x, int y, RangeValue chance, Optional<Pair<Holder<Enchantment>, Map<Integer, RangeValue>>> bonusChance, RangeValue count,
                                      Optional<Pair<Holder<Enchantment>, Map<Integer, RangeValue>>> bonusCount, List<ILootFunction> allFunctions, List<ILootCondition> allConditions) {
                slotParams.add(new ItemSlotParams(item, entry, x, y, chance, bonusChance, count, bonusCount, allFunctions, allConditions));
                return new Rect(x, y, 18, 18);
            }

            @Override
            public Rect addSlotWidget(TagKey<Item> item, ILootEntry entry, int x, int y, RangeValue chance, Optional<Pair<Holder<Enchantment>, Map<Integer, RangeValue>>> bonusChance, RangeValue count,
                                      Optional<Pair<Holder<Enchantment>, Map<Integer, RangeValue>>> bonusCount, List<ILootFunction> allFunctions, List<ILootCondition> allConditions) {
                slotParams.add(new TagSlotParams(item, entry, x, y, chance, bonusChance, count, bonusCount, allFunctions, allConditions));
                return new Rect(x, y, 18, 18);
            }
        };
    }

    private interface ISlotParams {
        ILootEntry entry();
        int x();
        int y();
        RangeValue chance();
        Optional<Pair<Holder<Enchantment>, Map<Integer, RangeValue>>> bonusChance();
        RangeValue count();
        Optional<Pair<Holder<Enchantment>, Map<Integer, RangeValue>>> bonusCount();
        List<ILootFunction> allFunctions();
        List<ILootCondition> allConditions();
    }

    private record ItemSlotParams (
            Item item,
            ILootEntry entry,
            int x,
            int y,
            RangeValue chance,
            Optional<Pair<Holder<Enchantment>, Map<Integer, RangeValue>>> bonusChance,
            RangeValue count,
            Optional<Pair<Holder<Enchantment>, Map<Integer, RangeValue>>> bonusCount,
            List<ILootFunction> allFunctions,
            List<ILootCondition> allConditions
    ) implements ISlotParams {}

    private record TagSlotParams (
            TagKey<Item> item,
            ILootEntry entry,
            int x,
            int y,
            RangeValue chance,
            Optional<Pair<Holder<Enchantment>, Map<Integer, RangeValue>>> bonusChance,
            RangeValue count,
            Optional<Pair<Holder<Enchantment>, Map<Integer, RangeValue>>> bonusCount,
            List<ILootFunction> allFunctions,
            List<ILootCondition> allConditions
    ) implements ISlotParams {}
}
