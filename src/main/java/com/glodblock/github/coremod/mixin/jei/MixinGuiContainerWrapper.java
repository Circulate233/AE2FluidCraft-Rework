package com.glodblock.github.coremod.mixin.jei;

import com.glodblock.github.coremod.CoreModHooks;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import mezz.jei.input.ClickedIngredient;
import mezz.jei.input.GuiContainerWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.awt.Rectangle;

@Mixin(value = GuiContainerWrapper.class, remap = false)
public class MixinGuiContainerWrapper {

    @WrapOperation(method = "getIngredientUnderMouse", at = @At(value = "INVOKE", target = "Lmezz/jei/input/ClickedIngredient;create(Ljava/lang/Object;Ljava/awt/Rectangle;)Lmezz/jei/input/ClickedIngredient;"))
    private ClickedIngredient<Object> wrapFluidPacket(final Object stack, final Rectangle area, final Operation<ClickedIngredient<Object>> original) {
        return original.call(CoreModHooks.wrapFluidPacket(stack), area);
    }
}