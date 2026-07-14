package com.yanny.awi.plugin.client.widget;

import com.yanny.aci.api.IWidget;
import com.yanny.aci.api.RelativeRect;
import com.yanny.aci.api.WidgetDirection;
import com.yanny.aci.tooltip.CoreTooltipUtils;
import com.yanny.aci.tooltip.TooltipNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class TextureWidget implements IWidget {
    protected final ResourceLocation texture;
    protected final RelativeRect rect;
    protected final int u;
    protected final int v;
    protected final int regionWidth;
    protected final int regionHeight;
    protected final int textureWidth;
    protected final int textureHeight;
    private final List<Component> components = new LinkedList<>();

    public TextureWidget(ResourceLocation texture, RelativeRect rect, int u, int v, int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
        this.texture = texture;
        this.rect = rect;
        this.u = u;
        this.v = v;
        this.regionWidth = regionWidth;
        this.regionHeight = regionHeight;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        rect.setDimensions(rect.getWidth(), rect.getHeight());
    }

    public TextureWidget(ResourceLocation texture, RelativeRect rect, int u, int v) {
        this(texture, rect, u, v, rect.getWidth(), rect.getHeight(), 256, 256);
    }

    @NotNull
    @Override
    public RelativeRect getRect() {
        return rect;
    }

    @NotNull
    @Override
    public WidgetDirection getDirection() {
        return WidgetDirection.VERTICAL;
    }

    public void tooltipText(TooltipNode tooltip) {
        this.components.addAll(CoreTooltipUtils.toComponents(tooltip, 0, Minecraft.getInstance().options.advancedItemTooltips));
    }

    @NotNull
    @Override
    public List<Component> getTooltipComponents(int mouseX, int mouseY) {
        return components;
    }

    @Override
    public void render(GuiGraphics draw, int mouseX, int mouseY) {
        draw.blit(texture, rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), u, v, regionWidth, regionHeight, textureWidth, textureHeight);
    }
}
