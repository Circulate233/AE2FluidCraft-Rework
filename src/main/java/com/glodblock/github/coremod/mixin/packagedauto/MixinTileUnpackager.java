package com.glodblock.github.coremod.mixin.packagedauto;

import com.glodblock.github.coremod.CoreModHooks;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import thelm.packagedauto.tile.TileBase;
import thelm.packagedauto.tile.TileUnpackager;

@Mixin(value = TileUnpackager.class, remap = false)
public abstract class MixinTileUnpackager extends TileBase {

    @WrapOperation(method = "emptyTrackers()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntity;hasCapability(Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/util/EnumFacing;)Z"))
    public boolean packHasCapability(final TileEntity instance, final Capability<?> capability, final EnumFacing facing, final Operation<Boolean> original) {
        if (CapabilityItemHandler.ITEM_HANDLER_CAPABILITY == capability) {
            return CoreModHooks.checkForItemHandler(this, capability, facing);
        }
        return original.call(instance, capability, facing);
    }

    @WrapOperation(method = "emptyTrackers()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntity;getCapability(Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/util/EnumFacing;)Ljava/lang/Object;"))
    public Object packGetCapability(final TileEntity instance, final Capability<?> capability, final EnumFacing facing, final Operation<Boolean> original) {
        if (CapabilityItemHandler.ITEM_HANDLER_CAPABILITY == capability) {
            return CoreModHooks.wrapItemHandler(this, capability, facing);
        }
        return original.call(instance, capability, facing);
    }
}