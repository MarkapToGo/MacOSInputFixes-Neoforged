package com.hamarb123.macos_input_fixes.client;

import java.io.IOException;
import com.hamarb123.macos_input_fixes.Common;
import com.hamarb123.macos_input_fixes.NativeUtils;
import com.hamarb123.macos_input_fixes.MacOSInputFixesMod;

/**
 * Bridge class for JNI native methods.
 * The native library was compiled with method names based on this class path.
 * This MUST stay in package com.hamarb123.macos_input_fixes.client with class
 * name MacOSInputFixesClientMod to match the JNI method signatures in the
 * native .dylib.
 * 
 * The callbacks MUST also be in package com.hamarb123.macos_input_fixes.client
 * with method name "accept" to match what the native code expects:
 * - ScrollCallback.accept(DDDDDD)V - 6 doubles
 * - KeyCallback.accept(IIII)V - 4 ints
 */
public class MacOSInputFixesClientMod {

    // Native method declarations - these match the JNI signatures in the native
    // library
    // The callback classes must be in com.hamarb123.macos_input_fixes.client
    // package
    public static native void registerCallbacks(ScrollCallback scrollCallback, KeyCallback keyCallback, long window);

    public static native void setTrackpadSensitivity(double sensitivity);

    public static native void setMomentumScrolling(boolean option);

    public static native void setInterfaceSmoothScroll(boolean option);

    static {
        MacOSInputFixesMod.LOGGER.info("[MacOSInputFixesClientMod] Static initializer - loading native library...");
        if (Common.IS_SYSTEM_MAC) {
            try {
                NativeUtils.loadLibraryFromJar("/natives/macos_input_fixes.dylib");
                MacOSInputFixesMod.LOGGER.info("[MacOSInputFixesClientMod] Native library loaded successfully!");
            } catch (IOException e) {
                MacOSInputFixesMod.LOGGER.error("[MacOSInputFixesClientMod] FAILED to load native library!", e);
                throw new RuntimeException(e);
            }
        } else {
            MacOSInputFixesMod.LOGGER.info("[MacOSInputFixesClientMod] Not macOS, skipping native library load");
        }
    }

    /**
     * Ensure the class is loaded (which triggers the static initializer to load the
     * native library)
     */
    public static void ensureLoaded() {
        // Just accessing this class loads it
    }
}
