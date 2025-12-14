package com.glodblock.github.coremod.mixin.ae2.container;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IMachineSet;
import appeng.container.implementations.ContainerFluidInterfaceConfigurationTerminal;
import appeng.container.implementations.ContainerInterfaceConfigurationTerminal;
import appeng.container.implementations.ContainerInterfaceTerminal;
import com.glodblock.github.coremod.CoreModHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(
    value = {
        ContainerInterfaceTerminal.class,
        ContainerInterfaceConfigurationTerminal.class,
        ContainerFluidInterfaceConfigurationTerminal.class
    },
    remap = false
)
public class MixinContainerInterfaceTerminal {

    @SuppressWarnings("MixinAnnotationTarget")
    @Redirect(method = "*", at = @At(value = "INVOKE", target = "Lappeng/api/networking/IGrid;getMachines(Ljava/lang/Class;)Lappeng/api/networking/IMachineSet;"))
    public IMachineSet packGetMachines(final IGrid instance, final Class<? extends IGridHost> aClass) {
        return CoreModHooks.getMachines(instance, aClass);
    }
}
