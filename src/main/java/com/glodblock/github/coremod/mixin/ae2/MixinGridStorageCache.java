package com.glodblock.github.coremod.mixin.ae2;

import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.me.cache.GridStorageCache;
import appeng.me.cache.NetworkMonitor;
import appeng.me.storage.NetworkInventoryHandler;
import com.glodblock.github.interfaces.FCNetworkInventoryHandler;
import com.glodblock.github.interfaces.FCNetworkMonitor;
import com.glodblock.github.util.ModAndClassUtil;
import com.glodblock.github.util.Util;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
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
    private void onInit(final IGrid g, final CallbackInfo ci) {
        ((FCNetworkMonitor) this.storageMonitors.get(Util.getItemChannel())).init();
    }

    @Inject(method = "buildNetworkStorage", at = @At("RETURN"))
    public void onBuild(final IStorageChannel<?> chan, final CallbackInfoReturnable<NetworkInventoryHandler<?>> cir) {
        final var m = ((FCNetworkMonitor) this.storageMonitors.get(Util.getItemChannel()));
        ((FCNetworkInventoryHandler) cir.getReturnValue()).init(m);
    }

    @Inject(method = "postAlterationOfStoredItems", at = @At("TAIL"))
    public void postAlterationOfStoredItems(final IStorageChannel<?> chan, final Iterable<? extends IAEStack<?>> input, final IActionSource src, final CallbackInfo ci) {
        if (chan == Util.getFluidChannel() || (ModAndClassUtil.GAS && chan == Util.getGasChannel())) {
            final var list = new ObjectArrayList<IAEItemStack>();
            for (final IAEStack<?> i : input) {
                final var drop = Util.packAEStackToDrop(i);
                if (drop != null) list.add(drop);
            }
            ((FCNetworkMonitor) this.storageMonitors.get(Util.getItemChannel())).fc$postChange(true, list, src);
        }
    }
}