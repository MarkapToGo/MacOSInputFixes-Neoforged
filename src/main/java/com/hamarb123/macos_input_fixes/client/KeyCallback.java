package com.hamarb123.macos_input_fixes.client;

/**
 * Callback interface for key events from native code.
 * The method name MUST be "accept" to match the native JNI signature.
 * Native signature: (IIII)V
 */
@FunctionalInterface
public interface KeyCallback {
    /**
     * Called when a key event occurs.
     * 
     * @param key       GLFW key code
     * @param scancode  Native scancode
     * @param action    Key action (press/release/repeat)
     * @param modifiers Modifier flags
     */
    void accept(int key, int scancode, int action, int modifiers);
}
