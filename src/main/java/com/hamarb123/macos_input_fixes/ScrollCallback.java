package com.hamarb123.macos_input_fixes;

/**
 * Callback interface for scroll events from native code.
 */
@FunctionalInterface
public interface ScrollCallback {
    void onScroll(double horizontal, double vertical);
}
