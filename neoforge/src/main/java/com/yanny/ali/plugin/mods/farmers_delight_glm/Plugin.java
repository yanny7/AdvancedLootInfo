package com.yanny.ali.plugin.mods.farmers_delight_glm;

import com.yanny.ali.api.AliEntrypoint;
import com.yanny.ali.api.IClientRegistry;
import com.yanny.ali.plugin.GlobalLootModifierUtils;
import com.yanny.ali.plugin.IForgePlugin;

@AliEntrypoint
public class Plugin implements IForgePlugin {
    @Override
    public String getModId() {
        return "farmersdelight";
    }

    @Override
    public void registerClient(IClientRegistry registry) {
        registry.registerWidget(ModifiedNode.ID, ModifiedWidget::new);
        registry.registerDataNode(ModifiedNode.ID, ModifiedNode::new);
    }

    @Override
    public void registerGLM(IRegistry registry) {
        GlobalLootModifierUtils.registerGlobalLootModifier(registry, AddItemModifier.class);
        GlobalLootModifierUtils.registerGlobalLootModifier(registry, FDAddTableLootModifier.class);
        GlobalLootModifierUtils.registerGlobalLootModifier(registry, PastrySlicingModifier.class);
        GlobalLootModifierUtils.registerGlobalLootModifier(registry, ReplaceItemModifier.class);
    }
}
