package com.yanny.advanced_loot_info.compatibility;

import com.yanny.advanced_loot_info.network.LootGroup;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.common.ForgeSpawnEggItem;

import java.util.List;

public class EmiEntityLoot extends EmiBaseLoot {
    private final Entity entity;

    public EmiEntityLoot(EmiRecipeCategory category, ResourceLocation id, Entity entity, LootGroup message) {
        super(category, id, message);
        this.entity = entity;

        SpawnEggItem spawnEgg = ForgeSpawnEggItem.fromEntityType(entity.getType());

        if (spawnEgg != null) {
            catalysts = List.of(EmiStack.of(spawnEgg));
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        ClientLevel level = Minecraft.getInstance().level;

        if (level != null) {
            widgets.add(new Widget() {
                @Override
                public Bounds getBounds() {
                    return new Bounds(0, 0, widgets.getWidth(), 48);
                }

                @Override
                public void render(GuiGraphics draw, int mouseX, int mouseY, float delta) {
                    if (entity instanceof LivingEntity livingEntity) {
                        //draw.enableScissor(draw.guiWidth() / 2 - 64, 64, draw.guiWidth() / 2 + 64, 256);
                        int length = Minecraft.getInstance().font.width(livingEntity.getName());

                        draw.drawString(Minecraft.getInstance().font, livingEntity.getName(), (widgets.getWidth() - length) / 2, 0, 0, false);
                        InventoryScreen.renderEntityInInventoryFollowsMouse(draw, (int) (4.5 * 18), 48, (int) (30 / EmiEntityLoot.this.entity.getType().getDimensions().height),
                                -mouseX + ((float) widgets.getWidth() / 2), -mouseY + 32 , livingEntity);
                        //draw.disableScissor();
                    }
                }
            });
            addWidgets(widgets, new int[]{0, 72});
        }

        catalysts.forEach((catalyst) -> {
            widgets.addSlot(catalyst, 0, 0);
        });
    }

    @Override
    public int getDisplayHeight() {
        return 72 + (outputs.size() / 9 + 1) * 18;
    }
}
