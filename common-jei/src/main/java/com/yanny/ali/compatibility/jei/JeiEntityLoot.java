package com.yanny.ali.compatibility.jei;

import com.yanny.ali.api.*;
import com.yanny.ali.compatibility.common.EntityLootType;
import com.yanny.ali.compatibility.common.EntityStorage;
import com.yanny.ali.compatibility.common.GenericUtils;
import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.plugin.client.widget.LootTableWidget;
import com.yanny.ali.registries.LootCategory;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

import java.util.LinkedList;
import java.util.List;

public class JeiEntityLoot extends JeiBaseLoot<EntityLootType, EntityType<?>> {
    public JeiEntityLoot(IGuiHelper guiHelper, RecipeType<RecipeHolder<EntityLootType>> recipeType, LootCategory<EntityType<?>> lootCategory, Component title, IDrawable icon) {
        super(guiHelper, recipeType, lootCategory, title, icon);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<EntityLootType> recipe, IFocusGroup iFocusGroup) {
        super.setRecipe(builder, recipe, iFocusGroup);

        SpawnEggItem spawnEgg = SpawnEggItem.byId(recipe.type().entityType());

        if (spawnEgg != null) {
            builder.addSlot(RecipeIngredientRole.CATALYST).setPosition(1, 1).setStandardSlotBackground().setSlotName("spawn_egg").addItemLike(spawnEgg);
        }
    }

    @Override
    Pair<List<IRecipeWidget>, List<IRecipeSlotDrawable>> getWidgets(IRecipeExtrasBuilder builder, EntityLootType recipe) {
        List<IRecipeWidget> widgets = new LinkedList<>();
        List<IRecipeSlotDrawable> slotDrawables = new LinkedList<>();

        builder.getRecipeSlots().findSlotByName("spawn_egg").ifPresent((slotDrawable -> {
            widgets.add(new JeiLootSlotWidget(slotDrawable, CATEGORY_WIDTH / 2 - 9, 0, new RangeValue(1)));
            slotDrawables.add(slotDrawable);
        }));
        widgets.add(createTextWidget(recipe.entity().getDisplayName(), CATEGORY_WIDTH / 2, true));
        widgets.add(new IRecipeWidget() {
            private static final int WIDGET_SIZE = 36;
            final Rect rect = new Rect((CATEGORY_WIDTH - WIDGET_SIZE) / 2, 10, WIDGET_SIZE, WIDGET_SIZE);
            final ScreenPosition position = new ScreenPosition(0, 0);

            @Override
            public void drawWidget(GuiGraphics guiGraphics, double mouseX, double mouseY) {
                Level level = Minecraft.getInstance().level;

                if (level != null) {
                    Entity entity = EntityStorage.getEntity(PluginManager.COMMON_REGISTRY, recipe.type().entityType(), level, recipe.type().variant());
                    GenericUtils.renderEntity(entity, new Rect((9 * 18 - WIDGET_SIZE) / 2, 10, WIDGET_SIZE, WIDGET_SIZE), 9 * 18, guiGraphics, (int) mouseX, (int) mouseY);
                }
            }

            @NotNull
            @Override
            public ScreenPosition getPosition() {
                return position;
            }
        });

        return new Pair<>(widgets, slotDrawables);
    }

    @Override
    int getYOffset(EntityLootType recipe) {
        return 48;
    }

    @Override
    IWidget getRootWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth) {
        return new LootTableWidget(utils, entry, rect, maxWidth);
    }
}
