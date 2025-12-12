package com.glodblock.github.coremod.mixin.ae2;

import appeng.helpers.DualityInterface;
import appeng.util.InventoryAdaptor;
import appeng.util.inv.BlockingInventoryAdaptor;
import com.glodblock.github.interfaces.FCDualityInterface;
import com.glodblock.github.inventory.BlockingFluidInventoryAdaptor;
import com.glodblock.github.inventory.FluidConvertingInventoryAdaptor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DualityInterface.class, remap = false)
public class MixinDualityInterface implements FCDualityInterface {

    @Unique
    public boolean fluidPacket = false;

    @Unique
    public int blockModeEx = 0;

    @Unique
    public boolean allowSplitting = true;

    @Unique
    public boolean isFluidPacket() {
        return fluidPacket;
    }

    @Unique
    public void setFluidPacket(boolean fluidPacket) {
        this.fluidPacket = fluidPacket;
    }

    @Unique
    public int getBlockModeEx() {
        return blockModeEx;
    }

    @Unique
    public void setBlockModeEx(int blockModeEx) {
        this.blockModeEx = blockModeEx;
    }

    @Unique
    public boolean isAllowSplitting() {
        return allowSplitting;
    }

    @Unique
    public void setAllowSplitting(boolean allowSplitting) {
        this.allowSplitting = allowSplitting;
    }

    @Redirect(
        method = {"pushItemsOut(Ljava/util/EnumSet;)V", "pushItemsOut(Lnet/minecraft/util/EnumFacing;)V", "pushPattern", "isBusy"},
        at = @At(
            value = "INVOKE",
            target = "Lappeng/util/InventoryAdaptor;getAdaptor(Lnet/minecraft/tileentity/TileEntity;Lnet/minecraft/util/EnumFacing;)Lappeng/util/InventoryAdaptor;"
        )
    )
    public InventoryAdaptor getAdaptorR(TileEntity te, EnumFacing facing) {
        return te != null ? FluidConvertingInventoryAdaptor.wrap(te, facing) : null;
    }

    @Redirect(
        method = "isCustomInvBlocking",
        at = @At(
            value = "INVOKE",
            target = "Lappeng/util/inv/BlockingInventoryAdaptor;getAdaptor(Lnet/minecraft/tileentity/TileEntity;Lnet/minecraft/util/EnumFacing;)Lappeng/util/inv/BlockingInventoryAdaptor;"
        )
    )
    private BlockingInventoryAdaptor wrapBlockingInventoryCall(TileEntity te, EnumFacing facing) {
        return te != null ? BlockingFluidInventoryAdaptor.getAdaptor(te, facing) : null;
    }

    @Inject(method = "writeToNBT", at = @At("HEAD"))
    private void onWriteToNBTStart(NBTTagCompound compound, CallbackInfo ci) {
        compound.setBoolean("fluidPacket", this.fluidPacket);
        compound.setBoolean("allowSplitting", this.allowSplitting);
        compound.setInteger("blockModeEx", this.blockModeEx);
    }

    @Inject(method = "readFromNBT", at = @At("HEAD"))
    private void onReadFromNBTStart(NBTTagCompound compound, CallbackInfo ci) {
        fluidPacket = compound.getBoolean("fluidPacket");
        allowSplitting = !compound.hasKey("allowSplitting") || compound.getBoolean("allowSplitting");
        blockModeEx = compound.getInteger("blockModeEx");
    }

}