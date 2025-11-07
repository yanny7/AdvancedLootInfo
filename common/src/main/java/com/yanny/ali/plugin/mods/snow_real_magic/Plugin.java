package com.yanny.ali.plugin.mods.snow_real_magic;

import com.yanny.ali.api.AliEntrypoint;
import com.yanny.ali.api.IClientRegistry;
import com.yanny.ali.api.IPlugin;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.plugin.mods.PluginUtils;

@AliEntrypoint
public class Plugin implements IPlugin {
    @Override
    public String getModId() {
        return "snowrealmagic";
    }

    @Override
    public void registerClient(IClientRegistry registry) {
        registry.registerWidget(NormalizeNode.ID, NormalizeWidget::new);
        registry.registerDataNode(NormalizeNode.ID, NormalizeNode::new);
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerEntry(registry, NormalizeLoot.class);
        PluginUtils.registerEntryItemCollector(registry, NormalizeLoot.class);
    }
}
