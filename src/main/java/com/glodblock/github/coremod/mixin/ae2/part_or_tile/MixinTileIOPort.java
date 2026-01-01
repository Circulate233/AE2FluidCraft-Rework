package com.glodblock.github.coremod.mixin.ae2.part_or_tile;

import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.tile.storage.TileIOPort;
import com.glodblock.github.integration.mek.FCGasItems;
import com.glodblock.github.loader.FCItems;
import com.glodblock.github.util.ModAndClassUtil;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = TileIOPort.class, remap = false)
public class MixinTileIOPort {

    @Redirect(method = "transferContents", at = @At(value = "INVOKE", target = "Lappeng/api/storage/data/IAEStack;getStackSize()J", ordinal = 0))
    private long removeDropSize(final IAEStack<?> instance, @Local(name = "src") final IMEInventory<?> src) {
        if (src instanceof IMEMonitor<?>) {
            if (instance instanceof IAEItemStack i) {
                var item = i.getItem();
                if (item == FCItems.FLUID_DROP || (ModAndClassUtil.GAS && item == FCGasItems.GAS_DROP)) {
                    return 0;
                }
            }
        }
        return instance.getStackSize();
    }
}