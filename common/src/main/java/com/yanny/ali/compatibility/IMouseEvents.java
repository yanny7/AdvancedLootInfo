<<<<<<<< HEAD:common/src/main/java/com/yanny/ali/compatibility/IMouseEvents.java
package com.yanny.ali.compatibility;
========
package com.yanny.ali.emi.compatibility.emi;
>>>>>>>> origin/1.20.1:common-emi/src/main/java/com/yanny/ali/emi/compatibility/emi/IMouseEvents.java

public interface IMouseEvents {
    boolean onMouseScrolled(double mouseX, double mouseY, double scrollDeltaY);
    boolean onMouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY);
}
