package com.yanny.emi_loot_addon.compatibility;

import com.yanny.emi_loot_addon.Utils;
import com.yanny.emi_loot_addon.network.LootGroup;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class EmiEntityLoot extends EmiBaseLoot {
    public static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(Utils.modLoc("entity_loot"), EmiStack.of(Items.ZOMBIE_HEAD));

    private final EntityType<?> entityType;

    public EmiEntityLoot(ResourceLocation id, EntityType<?> entityType, LootGroup message) {
        super(CATEGORY, id, message);
        this.entityType = entityType;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.add(new Widget() {
            @Override
            public Bounds getBounds() {
                return new Bounds(0, 0, 72, 36);
            }

            @Override
            public void render(GuiGraphics draw, int mouseX, int mouseY, float delta) {
                Entity entity = entityType.create(Minecraft.getInstance().level);

                if (entity instanceof LivingEntity livingEntity) {
                    InventoryScreen.renderEntityInInventoryFollowsMouse(draw, (int)(4.5 * 18), 64, 20, mouseX, mouseY, livingEntity);
                }
            }
        });
        addWidgets(widgets, new int[]{0, 72});
    }
}
