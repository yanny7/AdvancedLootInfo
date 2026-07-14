package com.yanny.ali.manager;

import com.yanny.aci.manager.CoreClientRegistry;
import com.yanny.ali.api.*;
import com.yanny.ali.configuration.AliConfig;
import com.yanny.ali.plugin.client.widget.MissingWidget;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AliClientRegistry extends CoreClientRegistry<AliConfig, AliCommonRegistry, IDataNode, IWidgetUtils, IClientUtils> implements IClientRegistry, IClientUtils, ICommonUtils {
    public AliClientRegistry(AliCommonRegistry utils) {
        super(utils);
    }

    @NotNull
    @Override
    public List<Entity> createEntities(EntityType<?> type, Level level) {
        return commonUtils.createEntities(type, level);
    }

    @NotNull
    @Override
    public IWidgetFactory<IDataNode, IWidgetUtils> getMissingWidgetFactory() {
        return MissingWidget::new;
    }
}
