package com.glodblock.github.coremod.mixin.ae2.part_or_tile;

import appeng.helpers.IInterfaceHost;
import appeng.parts.AEBasePart;
import appeng.tile.AEBaseTile;
import appeng.util.SettingsFrom;
import com.glodblock.github.interfaces.FCDualityInterface;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = {AEBasePart.class, AEBaseTile.class}, remap = false)
public class MixinAEBaseTileAndPart {

    @Inject(method = "uploadSettings", at = @At("HEAD"))
    public void onUploadSettings(SettingsFrom from, NBTTagCompound compound, EntityPlayer player, CallbackInfo ci) {
        if (this instanceof IInterfaceHost && compound != null && compound.hasKey("extraNBTData")) {
            FCDualityInterface dual = (FCDualityInterface) ((IInterfaceHost) this).getInterfaceDuality();
            NBTTagCompound extra = compound.getCompoundTag("extraNBTData");
            dual.setAllowSplitting(extra.getBoolean("allowSplitting"));
            dual.setBlockModeEx(extra.getInteger("blockModeEx"));
            dual.setFluidPacket(extra.getBoolean("fluidPacket"));
        }
    }

    @Inject(method = "downloadSettings", at = @At(value = "RETURN"))
    public void onDownloadSettings(SettingsFrom from, CallbackInfoReturnable<NBTTagCompound> cir, @Local(name = "output") NBTTagCompound output) {
        if (this instanceof IInterfaceHost) {
            FCDualityInterface dual = (FCDualityInterface) ((IInterfaceHost) this).getInterfaceDuality();
            NBTTagCompound extra = new NBTTagCompound();
            extra.setBoolean("fluidPacket", dual.isFluidPacket());
            extra.setBoolean("allowSplitting", dual.isAllowSplitting());
            extra.setInteger("blockModeEx", dual.getBlockModeEx());
            output.setTag("extraNBTData", extra);
        }
    }

}
