package com.glodblock.github.coremod.mixin.jei;

import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.input.InputHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = InputHandler.class, remap = false)
public interface AccessorInputHandler {

    @Accessor
    IngredientListOverlay getIngredientListOverlay();
}
