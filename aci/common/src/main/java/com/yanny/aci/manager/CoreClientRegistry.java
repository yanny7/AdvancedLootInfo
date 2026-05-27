package com.yanny.aci.manager;

import com.yanny.aci.api.*;
import com.yanny.aci.tooltip.TooltipNodePalette;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

public abstract class CoreClientRegistry<
        TConfig,
        TCommonUtils extends CoreCommonRegistry<TConfig>,
        TDataNode    extends ICoreDataNode<?>,
        TWidgetUtils extends ICoreWidgetUtils<?>,
        TClientUtils extends ICoreClientUtils<?, ?, ?>
        >
        extends
        BaseRegistry
        implements
        ICoreClientUtils<TDataNode, TWidgetUtils, TClientUtils>,
        ICoreCommonUtils<TConfig>,
        ICoreClientRegistry<TDataNode, TWidgetUtils, TClientUtils> {

    private final ManagedRegistry<ResourceLocation, IWidgetFactory<TDataNode, TWidgetUtils>> widgetMap = register("node widgets", false, HashMap::new, ResourceLocation::toString, null);
    private final ManagedRegistry<ResourceLocation, BiFunction<TClientUtils, FriendlyByteBuf, TDataNode>> dataNodeFactoryMap = register("data node factories", false, HashMap::new, ResourceLocation::toString, null);

    protected final TCommonUtils commonUtils;

    private final TooltipNodePalette tooltipNodeCache = new TooltipNodePalette();

    public CoreClientRegistry(TCommonUtils registry) {
        commonUtils = registry;
    }

    @NotNull
    public abstract IWidgetFactory<TDataNode, TWidgetUtils> getMissingWidgetFactory();

    @Override
    public final void registerWidget(ResourceLocation id, IWidgetFactory<TDataNode, TWidgetUtils> factory) {
        widgetMap.put(id, factory);
    }

    @Override
    public final void registerDataNode(ResourceLocation id, BiFunction<TClientUtils, FriendlyByteBuf, TDataNode> dataFactory) {
        dataNodeFactoryMap.put(id, dataFactory);
    }

    @NotNull
    @Override
    public final List<IWidget> createWidgets(TWidgetUtils utils, List<TDataNode> entries, RelativeRect parent, int maxWidth) {
        int posX = 0, posY = 0;
        List<IWidget> widgets = new ArrayList<>(entries.size());
        WidgetDirection lastDirection = null;

        for (TDataNode entry : entries) {
            IWidgetFactory<TDataNode, TWidgetUtils> widgetFactory = widgetMap.get(entry.getId()).orElse(getMissingWidgetFactory());
            IWidget widget = widgetFactory.create(utils, entry, new RelativeRect(0, 0, maxWidth, 0, parent), maxWidth);

            widgets.add(widget);
        }

        int w = 0;
        int h = 0;

        for (int i = 0; i < widgets.size(); i++) {
            IWidget widget = widgets.get(i);
            WidgetDirection direction = widget.getDirection();
            RelativeRect bounds = widget.getRect();

            if (lastDirection != null) {
                if (lastDirection == WidgetDirection.HORIZONTAL && direction == WidgetDirection.HORIZONTAL) {
                    if (parent.getX() + posX + bounds.getWidth() <= maxWidth) {
                        bounds.setOffset(posX, posY);
                        posX += bounds.getWidth();
                    } else {
                        posX = bounds.getWidth();
                        posY += widgets.get(i - 1).getRect().getHeight();
                        bounds.setOffset(0, posY);
                    }
                } else {
                    posX = 0;

                    if (direction != lastDirection) {
                        if (lastDirection == WidgetDirection.HORIZONTAL) {
                            posY += widgets.get(i - 1).getRect().getHeight() + IWidget.PADDING;
                        }

                        bounds.setOffset(posX, posY);
                    } else {
                        bounds.setOffset(posX, posY);
                    }

                    if (direction != WidgetDirection.HORIZONTAL) {
                        posY += bounds.getHeight() + IWidget.PADDING;
                    } else {
                        posX += bounds.getWidth();
                    }
                }
            } else {
                bounds.setOffset(posX, posY);

                if (direction == WidgetDirection.HORIZONTAL) {
                    posX += bounds.getWidth();
                } else {
                    posY += bounds.getHeight() + IWidget.PADDING;
                }
            }

            w = Math.max(w, bounds.getOffsetX() + bounds.getWidth());
            h = Math.max(h, bounds.getOffsetY() + bounds.getHeight());
            lastDirection = direction;
        }

        parent.setDimensions(w, h);
        return widgets;
    }

    @NotNull
    @Override
    public final BiFunction<TClientUtils, FriendlyByteBuf, TDataNode> getDataNodeFactory(ResourceLocation id) {
        Optional<BiFunction<TClientUtils, FriendlyByteBuf, TDataNode>> dataFactory = dataNodeFactoryMap.get(id);

        return dataFactory.orElseThrow(() -> new IllegalStateException(String.format("Failed to construct data node - node {%s} was not registered!", id)));
    }

    @NotNull
    @Override
    public final TConfig getConfiguration() {
        return commonUtils.getConfiguration();
    }

    @NotNull
    @Override
    public TooltipNodePalette getTooltipCache() {
        return tooltipNodeCache;
    }

    @Nullable
    @Override
    public String getTranslationKey(int index) {
        return commonUtils.getDictionary().inverse().get(index);
    }
}
