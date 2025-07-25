package com.yanny.ali.plugin.client.widget;

import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.IWidget;
import com.yanny.ali.api.RelativeRect;
import com.yanny.ali.api.WidgetDirection;
import com.yanny.ali.plugin.common.NodeUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedList;
import java.util.List;

public class TextureWidget extends IWidget {
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
        super(ResourceLocation.withDefaultNamespace("texture_widget"));
        this.texture = texture;
        this.rect = rect;
        this.u = u;
        this.v = v;
        this.regionWidth = regionWidth;
        this.regionHeight = regionHeight;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        rect.setDimensions(rect.width, rect.height);
    }

    public TextureWidget(ResourceLocation texture, RelativeRect rect, int u, int v) {
        this(texture, rect, u, v, rect.width, rect.height, 256, 256);
    }

    @Override
    public RelativeRect getRect() {
        return rect;
    }

    @Override
    public WidgetDirection getDirection() {
        return WidgetDirection.VERTICAL;
    }

    public void tooltipText(List<ITooltipNode> tooltip) {
        this.components.addAll(NodeUtils.toComponents(tooltip, 0));
    }

    @Override
    public List<Component> getTooltipComponents(int mouseX, int mouseY) {
        return components;
    }

    @Override
    public void render(GuiGraphics draw, int mouseX, int mouseY) {
        draw.blit(RenderPipelines.GUI_TEXTURED, texture, rect.getX(), rect.getY(), u, v, rect.width, rect.height, textureWidth, textureHeight);
    }
}
