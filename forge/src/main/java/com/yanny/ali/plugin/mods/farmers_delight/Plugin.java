package com.yanny.ali.plugin.mods.farmers_delight;

import com.yanny.ali.api.AliEntrypoint;
import com.yanny.ali.api.IClientRegistry;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.plugin.GlobalLootModifierUtils;
import com.yanny.ali.plugin.IForgePlugin;
import com.yanny.ali.plugin.client.widget.ModifiedWidget;
import com.yanny.ali.plugin.common.nodes.ModifiedNode;
import com.yanny.ali.plugin.mods.PluginUtils;

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
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerFunctionTooltip(registry, CopySkilletFunction.class);
        PluginUtils.registerFunctionTooltip(registry, CopyMealFunction.class);
    }

    @Override
    public void registerGLM(IRegistry registry) {
        GlobalLootModifierUtils.registerGlobalLootModifier(registry, AddItemModifier.class);
        GlobalLootModifierUtils.registerGlobalLootModifier(registry, AddLootTableModifier.class);
        GlobalLootModifierUtils.registerGlobalLootModifier(registry, PastrySlicingModifier.class);
        GlobalLootModifierUtils.registerGlobalLootModifier(registry, ReplaceItemModifier.class);
    }
}
