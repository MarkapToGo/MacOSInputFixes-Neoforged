package com.hamarb123.macos_input_fixes.client;

/**
 * Callback interface for scroll events from native code.
 * The method name MUST be "accept" to match the native JNI signature.
 * Native signature: (DDDDDD)V
 */
@FunctionalInterface
public interface ScrollCallback {
    /**
     * Called when a scroll event occurs.
     * 
     * @param x             Grouped horizontal scroll
     * @param y             Grouped vertical scroll
     * @param xWithMomentum Horizontal with momentum
     * @param yWithMomentum Vertical with momentum
     * @param ungroupedX    Raw horizontal scroll
     * @param ungroupedY    Raw vertical scroll
     */
    void accept(double x, double y, double xWithMomentum, double yWithMomentum, double ungroupedX, double ungroupedY);
}
