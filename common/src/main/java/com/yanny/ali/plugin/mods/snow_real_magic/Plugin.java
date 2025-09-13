package com.yanny.ali.plugin.mods.snow_real_magic;

import com.yanny.ali.api.IClientRegistry;
import com.yanny.ali.api.IPlugin;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.plugin.mods.PluginUtils;
import net.minecraft.world.item.Items;

import java.util.List;

//@AliEntrypoint
public class Plugin implements IPlugin {
    @Override
    public String getModId() {
        return "snowrealmagic";
    }

    @Override
    public void registerClient(IClientRegistry registry) {
        registry.registerWidget(NormalizeNode.ID, NormalizeWidget::new);
        registry.registerNode(NormalizeNode.ID, NormalizeNode::new);
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerItemCollector(NormalizeNode.class, (u, c) -> List.of(Items.CAKE));
        PluginUtils.registerEntry(registry, NormalizeLoot.class);
    }
}
