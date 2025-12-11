package com.glodblock.github.coremod.mixin.ae2;

import appeng.api.networking.IGrid;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.me.cache.GridStorageCache;
import appeng.me.cache.NetworkMonitor;
import appeng.me.storage.NetworkInventoryHandler;
import com.glodblock.github.interfaces.FCNetworkInventoryHandler;
import com.glodblock.github.interfaces.FCNetworkMonitor;
import com.glodblock.github.util.Util;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(value = GridStorageCache.class, remap = false)
public abstract class MixinGridStorageCache {

    @Shadow
    @Final
    private Map<IStorageChannel<? extends IAEStack<?>>, NetworkMonitor<?>> storageMonitors;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(IGrid g, CallbackInfo ci) {
        ((FCNetworkMonitor) this.storageMonitors.get(Util.getItemChannel())).init();
    }

    @Inject(method = "buildNetworkStorage", at = @At("RETURN"))
    public void onBuild(IStorageChannel<?> chan, CallbackInfoReturnable<NetworkInventoryHandler<?>> cir) {
        var m = ((FCNetworkMonitor) this.storageMonitors.get(Util.getItemChannel()));
        ((FCNetworkInventoryHandler) cir.getReturnValue()).init(m);
    }
}