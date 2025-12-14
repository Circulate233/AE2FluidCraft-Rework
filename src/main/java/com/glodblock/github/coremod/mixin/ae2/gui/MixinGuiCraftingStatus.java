package com.glodblock.github.coremod.mixin.ae2.gui;

import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.implementations.GuiCraftingStatus;
import appeng.container.implementations.CraftingCPUStatus;
import com.glodblock.github.coremod.CoreModHooks;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = GuiCraftingStatus.class, remap = false)
public class MixinGuiCraftingStatus {

    @WrapOperation(method = "drawFG", at = @At(value = "INVOKE", target = "Lappeng/container/implementations/CraftingCPUStatus;getCrafting()Lappeng/api/storage/data/IAEItemStack;"))
    public IAEItemStack getItemDisplayNameR(final CraftingCPUStatus instance, final Operation<IAEItemStack> original) {
        return CoreModHooks.displayAEFluidAmount(original.call(instance));
    }

}