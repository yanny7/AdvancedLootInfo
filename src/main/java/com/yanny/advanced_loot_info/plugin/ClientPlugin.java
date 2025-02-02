package com.yanny.advanced_loot_info.plugin;

import com.yanny.advanced_loot_info.api.IClientRegistry;
import com.yanny.advanced_loot_info.api.WidgetDirection;
import com.yanny.advanced_loot_info.plugin.entry.*;
import com.yanny.advanced_loot_info.plugin.widget.*;

public class ClientPlugin {
    public static void initialize(IClientRegistry registry) {
        registry.registerWidget(ItemEntry.class, WidgetDirection.HORIZONTAL, ItemWidget::new, ItemWidget::getBounds);
        registry.registerWidget(EmptyEntry.class, WidgetDirection.HORIZONTAL, EmptyWidget::new, EmptyWidget::getBounds);
        registry.registerWidget(ReferenceEntry.class, WidgetDirection.VERTICAL, ReferenceWidget::new, ReferenceWidget::getBounds);
        registry.registerWidget(DynamicEntry.class, WidgetDirection.VERTICAL, DynamicWidget::new, DynamicWidget::getBounds);
        registry.registerWidget(TagEntry.class, WidgetDirection.HORIZONTAL, TagWidget::new, TagWidget::getBounds);
        registry.registerWidget(AlternativesEntry.class, WidgetDirection.VERTICAL, AlternativesWidget::new, CompositeWidget::getBounds);
        registry.registerWidget(SequentialEntry.class, WidgetDirection.VERTICAL, SequentialWidget::new, CompositeWidget::getBounds);
        registry.registerWidget(GroupEntry.class, WidgetDirection.VERTICAL, GroupWidget::new, CompositeWidget::getBounds);
        registry.registerWidget(UnknownEntry.class, WidgetDirection.VERTICAL, UnknownWidget::new, UnknownWidget::getBounds);
    }
}
