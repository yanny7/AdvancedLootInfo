package com.yanny.ali.plugin;

import com.yanny.ali.api.IClientRegistry;
import com.yanny.ali.api.WidgetDirection;
import com.yanny.ali.plugin.entry.*;
import com.yanny.ali.plugin.widget.*;

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
