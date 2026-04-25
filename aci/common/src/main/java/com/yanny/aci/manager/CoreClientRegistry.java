package com.yanny.aci.manager;

import com.yanny.aci.api.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

public abstract class CoreClientRegistry
        <
                CN,
                BU extends ICoreCommonUtils<CN>,
                SU extends ICoreServerUtils,
                TN extends ICoreTooltipNode<SU>,
                DN extends ICoreDataNode<SU, TN>,
                WU extends ICoreWidgetUtils<SU, TN, DN>,
                CU extends ICoreClientUtils<SU, TN, DN, CU, WU>
        >
        extends BaseRegistry
        implements ICoreClientUtils<SU, TN, DN, CU, WU>, ICoreCommonUtils<CN>, ICoreClientRegistry<SU, TN, DN, CU, WU> {

    private final ManagedRegistry<ResourceLocation, IWidgetFactory<SU, TN, DN, WU>> widgetMap = register("node widgets", false, HashMap::new, ResourceLocation::toString, null);
    private final ManagedRegistry<ResourceLocation, BiFunction<CU, RegistryFriendlyByteBuf, DN>> dataNodeFactoryMap = register("data node factories", false, HashMap::new, ResourceLocation::toString, null);
    private final ManagedRegistry<ResourceLocation, BiFunction<CU, RegistryFriendlyByteBuf, TN>> tooltipNodeFactoryMap = register("tooltip node factories", false, HashMap::new, ResourceLocation::toString, null);

    protected final BU commonUtils;

    public CoreClientRegistry(BU registry) {
        commonUtils = registry;
    }

    @NotNull
    public abstract IWidgetFactory<SU, TN, DN, WU> getMissingWidgetFactory();

    @Override
    public final void registerWidget(ResourceLocation id, IWidgetFactory<SU, TN, DN, WU> factory) {
        widgetMap.put(id, factory);
    }

    @Override
    public final void registerDataNode(ResourceLocation id, BiFunction<CU, RegistryFriendlyByteBuf, DN> dataFactory) {
        dataNodeFactoryMap.put(id, dataFactory);
    }

    @Override
    public final void registerTooltipNode(ResourceLocation id, BiFunction<CU, RegistryFriendlyByteBuf, TN> tooltipFactory) {
        tooltipNodeFactoryMap.put(id, tooltipFactory);
    }

    @NotNull
    @Override
    public final List<IWidget> createWidgets(WU utils, List<DN> entries, RelativeRect parent, int maxWidth) {
        int posX = 0, posY = 0;
        List<IWidget> widgets = new ArrayList<>(entries.size());
        WidgetDirection lastDirection = null;

        for (DN entry : entries) {
            IWidgetFactory<SU, TN, DN, WU> widgetFactory = widgetMap.get(entry.getId()).orElse(getMissingWidgetFactory());
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
    public final BiFunction<CU, RegistryFriendlyByteBuf, DN> getDataNodeFactory(ResourceLocation id) {
        Optional<BiFunction<CU, RegistryFriendlyByteBuf, DN>> dataFactory = dataNodeFactoryMap.get(id);

        return dataFactory.orElseThrow(() -> new IllegalStateException(String.format("Failed to construct data node - node {%s} was not registered!", id)));
    }

    @NotNull
    @Override
    public final BiFunction<CU, RegistryFriendlyByteBuf, TN> getTooltipNodeFactory(ResourceLocation id) {
        Optional<BiFunction<CU, RegistryFriendlyByteBuf, TN>> tooltipFactory = tooltipNodeFactoryMap.get(id);

        return tooltipFactory.orElseThrow(() -> new IllegalStateException(String.format("Failed to construct tooltip node - node {%s} was not registered!", id)));
    }

    @NotNull
    @Override
    public final CN getConfiguration() {
        return commonUtils.getConfiguration();
    }
}
