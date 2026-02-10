package com.hamarb123.macos_input_fixes.mixin;

import com.hamarb123.macos_input_fixes.Common;
import com.hamarb123.macos_input_fixes.ModOptions;
import net.minecraft.client.MouseHandler;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    /**
     * Fixes the issue where Ctrl + Left Click is interpreted as Right Click on
     * macOS.
     * This intercepts the button argument in onPress and changes it back to 0
     * (Left)
     * if it was converted to 1 (Right) due to Ctrl key being held.
     */
    @ModifyVariable(method = "onPress", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private int fixCtrlLeftClick(int button) {
        // checks:
        // 1. Must be on macOS
        // 2. Feature must be enabled (disableCtrlClickFix == false)
        // 3. Button must be 1 (Right Click) - because macOS converts Ctrl+Left to Right
        if (Common.IS_SYSTEM_MAC && !ModOptions.disableCtrlClickFix && button == 1) {
            // We need to check if the user is ACTUALLY pressing the Left Mouse Button.
            long window = net.minecraft.client.Minecraft.getInstance().getWindow().getWindow();

            boolean isLeftDown = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
            boolean isRightDown = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;

            if (isLeftDown && !isRightDown) {
                // User is physically pressing Left, but we received Right (1).
                // This means macOS converted it.
                // We revert it to Left (0).
                com.hamarb123.macos_input_fixes.MacOSInputFixesMod.LOGGER
                        .info("[MouseHandlerMixin] Reverting Ctrl+Click (Right) to Left Click");
                return 0;
            } else {
                com.hamarb123.macos_input_fixes.MacOSInputFixesMod.LOGGER.info(
                        "[MouseHandlerMixin] Pass through: button={}, isLeft={}, isRight={}", button, isLeftDown,
                        isRightDown);
            }
        }
        return button;
    }
}
