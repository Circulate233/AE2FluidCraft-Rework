package com.glodblock.github.coremod.mixin.ae2.container;

import appeng.api.networking.crafting.ICraftingJob;
import appeng.container.implementations.ContainerCraftConfirm;
import appeng.container.implementations.CraftingCPURecord;
import com.glodblock.github.coremod.CoreModHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(value = ContainerCraftConfirm.class, remap = false)
public class MixinContainerCraftConfirm {

    @Shadow
    private ICraftingJob result;

    @Shadow
    @Final
    private ArrayList<CraftingCPURecord> cpus;

    @Inject(method = "startJob", at = @At("HEAD"), cancellable = true)
    public void startJob(final CallbackInfo ci) {
        if (CoreModHooks.startJob((ContainerCraftConfirm) (Object) this, this.cpus, this.result)) {
            ci.cancel();
        }
    }
}