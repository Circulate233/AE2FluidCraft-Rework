package com.glodblock.github.coremod.mixin.ae2;

import appeng.container.implementations.ContainerExpandedProcessingPatternTerm;
import appeng.container.implementations.ContainerInterface;
import appeng.container.implementations.ContainerPatternTerm;
import appeng.container.implementations.ContainerWirelessPatternTerminal;
import appeng.core.sync.GuiBridge;
import com.glodblock.github.client.container.ContainerExtendedFluidPatternTerminal;
import com.glodblock.github.client.container.ContainerFluidPatternTerminal;
import com.glodblock.github.client.container.ContainerWirelessFluidPatternTerminal;
import com.glodblock.github.client.container.ContainerWrapInterface;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = GuiBridge.class, remap = false)
public class MixinGuiBridge {

    @Mutable
    @Shadow
    @Final
    private Class<?> containerClass;

    @WrapOperation(method = "<init>(Ljava/lang/String;ILjava/lang/Class;Ljava/lang/Class;Lappeng/core/sync/GuiHostType;Lappeng/api/config/SecurityPermissions;)V", at = @At(value = "INVOKE", target = "Lappeng/core/sync/GuiBridge;getGui()V"))
    private void containerInterfaceR(final GuiBridge instance, final Operation<Void> original, @Local(name = "containerClass") final Class<?> containerClass) {
        if (containerClass == ContainerInterface.class) {
            this.containerClass = ContainerWrapInterface.class;
        }
        if (containerClass == ContainerPatternTerm.class) {
            this.containerClass = ContainerFluidPatternTerminal.class;
        }
        if (containerClass == ContainerWirelessPatternTerminal.class) {
            this.containerClass = ContainerWirelessFluidPatternTerminal.class;
        }
        if (containerClass == ContainerExpandedProcessingPatternTerm.class) {
            this.containerClass = ContainerExtendedFluidPatternTerminal.class;
        }
        original.call(instance);
    }
}