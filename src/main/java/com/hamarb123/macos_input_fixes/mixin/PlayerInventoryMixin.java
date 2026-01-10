package com.hamarb123.macos_input_fixes.mixin;

import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.hamarb123.macos_input_fixes.ModOptions;

@Mixin(Inventory.class)
public class PlayerInventoryMixin {
    
    @ModifyVariable(method = "swapPaint(D)V", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private double maybeReverseHotbarScroll(double value) {
        // If the reverse hotbar scrolling option is enabled, reverse the scroll value
        return ModOptions.reverseHotbarScrolling ? -value : value;
    }
}
