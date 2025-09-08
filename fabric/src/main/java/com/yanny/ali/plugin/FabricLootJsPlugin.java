package com.yanny.ali.plugin;

import com.yanny.ali.api.AliEntrypoint;
import com.yanny.ali.api.IPlugin;
import com.yanny.ali.api.IServerRegistry;
import net.fabricmc.fabric.impl.recipe.ingredient.CustomIngredientImpl;

@AliEntrypoint
public class FabricLootJsPlugin implements IPlugin {
    @Override
    public String getModId() {
        return "fabric";
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerIngredientTooltip(CustomIngredientImpl.class, LootJsIngredientTooltipUtils::getCustomIngredientTooltip);
    }
}
