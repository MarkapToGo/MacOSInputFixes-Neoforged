package com.hamarb123.macos_input_fixes;

/**
 * Callback interface for key events from native code.
 */
@FunctionalInterface
public interface KeyCallback {
    void onKey(int key, int scancode, int action, int modifiers);
}
