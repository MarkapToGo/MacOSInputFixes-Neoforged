package com.hamarb123.macos_input_fixes.mixin;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MouseHandler.class)
public interface MouseInvokerMixin {
    @Invoker("onScroll")
    void invokeOnScroll(long window, double horizontal, double vertical);
}
