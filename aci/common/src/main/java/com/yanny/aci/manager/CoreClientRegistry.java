package com.yanny.aci.manager;

import com.yanny.aci.api.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

public abstract class CoreClientRegistry<
        TConfig,
        TCommonUtils extends ICoreCommonUtils<TConfig>,
        TTooltipNode extends ICoreTooltipNode<?>,
        TDataNode    extends ICoreDataNode<?, ?>,
        TWidgetUtils extends ICoreWidgetUtils<?>,
        TClientUtils extends ICoreClientUtils<?, ?, ?, ?>
        >
        extends
        BaseRegistry
        implements
        ICoreClientUtils<TTooltipNode, TDataNode, TWidgetUtils, TClientUtils>,
        ICoreCommonUtils<TConfig>,
        ICoreClientRegistry<TTooltipNode, TDataNode, TWidgetUtils, TClientUtils> {

    private final ManagedRegistry<Identifier, IWidgetFactory<TDataNode, TWidgetUtils>> widgetMap = register("node widgets", false, HashMap::new, Identifier::toString, null);
    private final ManagedRegistry<Identifier, BiFunction<TClientUtils, RegistryFriendlyByteBuf, TDataNode>> dataNodeFactoryMap = register("data node factories", false, HashMap::new, Identifier::toString, null);
    private final ManagedRegistry<Identifier, BiFunction<TClientUtils, RegistryFriendlyByteBuf, TTooltipNode>> tooltipNodeFactoryMap = register("tooltip node factories", false, HashMap::new, Identifier::toString, null);

    protected final TCommonUtils commonUtils;

    public CoreClientRegistry(TCommonUtils registry) {
        commonUtils = registry;
    }

    @NotNull
    public abstract IWidgetFactory<TDataNode, TWidgetUtils> getMissingWidgetFactory();

    @Override
    public final void registerWidget(Identifier id, IWidgetFactory<TDataNode, TWidgetUtils> factory) {
        widgetMap.put(id, factory);
    }

    @Override
    public final void registerDataNode(Identifier id, BiFunction<TClientUtils, RegistryFriendlyByteBuf, TDataNode> dataFactory) {
        dataNodeFactoryMap.put(id, dataFactory);
    }

    @Override
    public final void registerTooltipNode(Identifier id, BiFunction<TClientUtils, RegistryFriendlyByteBuf, TTooltipNode> tooltipFactory) {
        tooltipNodeFactoryMap.put(id, tooltipFactory);
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
    public final BiFunction<TClientUtils, RegistryFriendlyByteBuf, TDataNode> getDataNodeFactory(Identifier id) {
        Optional<BiFunction<TClientUtils, RegistryFriendlyByteBuf, TDataNode>> dataFactory = dataNodeFactoryMap.get(id);

        return dataFactory.orElseThrow(() -> new IllegalStateException(String.format("Failed to construct data node - node {%s} was not registered!", id)));
    }

    @NotNull
    @Override
    public final BiFunction<TClientUtils, RegistryFriendlyByteBuf, TTooltipNode> getTooltipNodeFactory(Identifier id) {
        Optional<BiFunction<TClientUtils, RegistryFriendlyByteBuf, TTooltipNode>> tooltipFactory = tooltipNodeFactoryMap.get(id);

        return tooltipFactory.orElseThrow(() -> new IllegalStateException(String.format("Failed to construct tooltip node - node {%s} was not registered!", id)));
    }

    @NotNull
    @Override
    public final TConfig getConfiguration() {
        return commonUtils.getConfiguration();
    }
}
