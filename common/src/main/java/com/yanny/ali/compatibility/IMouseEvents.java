package com.yanny.ali.compatibility;

public interface IMouseEvents {
    boolean onMouseScrolled(double mouseX, double mouseY, double scrollDeltaY);
    boolean onMouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY);
}
