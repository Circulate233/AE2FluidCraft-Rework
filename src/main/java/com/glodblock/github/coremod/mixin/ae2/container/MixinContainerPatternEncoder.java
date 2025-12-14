package com.glodblock.github.coremod.mixin.ae2.container;

import appeng.container.implementations.ContainerPatternEncoder;
import com.glodblock.github.coremod.CoreModHooks;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ContainerPatternEncoder.class, remap = false)
public class MixinContainerPatternEncoder {

    @WrapOperation(method = "encode", at = @At(value = "INVOKE", target = "Lappeng/container/implementations/ContainerPatternEncoder;isPattern(Lnet/minecraft/item/ItemStack;)Z"))
    public boolean chanceOutput(final ContainerPatternEncoder instance, final ItemStack output, final Operation<Boolean> original) {
        return original.call(instance, CoreModHooks.transformPattern(instance, output));
    }
}
