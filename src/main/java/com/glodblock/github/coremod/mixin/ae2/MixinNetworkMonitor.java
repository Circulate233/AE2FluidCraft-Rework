package com.glodblock.github.coremod.mixin.ae2;

import appeng.api.config.FuzzyMode;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.me.cache.GridStorageCache;
import appeng.me.cache.NetworkMonitor;
import appeng.util.item.AEItemStack;
import com.glodblock.github.common.item.fake.FakeItemRegister;
import com.glodblock.github.integration.mek.FCGasItems;
import com.glodblock.github.interfaces.FCNetworkMonitor;
import com.glodblock.github.loader.FCItems;
import com.glodblock.github.util.FakeMonitor;
import com.glodblock.github.util.ModAndClassUtil;
import com.glodblock.github.util.Util;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nonnull;

@Mixin(value = NetworkMonitor.class, remap = false)
public abstract class MixinNetworkMonitor<T extends IAEStack<T>> implements FCNetworkMonitor<T> {

    @Shadow
    @Final
    @Nonnull
    private IStorageChannel<?> myChannel;

    @Shadow
    @Final
    @Nonnull
    private GridStorageCache myGridCache;

    @Shadow
    protected abstract void postChange(boolean add, Iterable<T> changes, IActionSource src);

    @Unique
    private FakeMonitor<IAEFluidStack> fluidMonitor;

    @Unique
    private FakeMonitor<?> gasMonitor;

    @Override
    public void init() {
        if (this.myChannel == Util.getItemChannel()) {
            fluidMonitor = new FakeMonitor<>(this.myGridCache, Util.getFluidChannel()) {

                @Override
                public IItemList<IAEItemStack> getAvailableItems(final IItemList<IAEItemStack> list) {
                    list.findFuzzy(
                        drop.computeIfAbsent(Util.getFluidChannel(), s -> AEItemStack.fromItemStack(new ItemStack(FCItems.FLUID_DROP, 1))),
                        FuzzyMode.IGNORE_ALL
                    ).forEach(i -> i.setStackSize(0));

                    for (final var t : monitor.getStorageList()) {
                        final var i = cacheMap.computeIfAbsent(t, ti -> FakeItemRegister.packAEStackLong(ti, FCItems.FLUID_DROP));
                        if (i != null) {
                            i.setStackSize(t.getStackSize());
                            list.addStorage(i);
                        }
                    }
                    return list;
                }

            };
            if (ModAndClassUtil.GAS) {
                gasMonitor = new FakeMonitor<>(this.myGridCache, Util.getGasChannel()) {

                    @Override
                    public IItemList<IAEItemStack> getAvailableItems(final IItemList<IAEItemStack> list) {
                        list.findFuzzy(
                            drop.computeIfAbsent(Util.getGasChannel(), s -> AEItemStack.fromItemStack(new ItemStack(FCGasItems.GAS_DROP, 1))),
                            FuzzyMode.IGNORE_ALL
                        ).forEach(i -> i.setStackSize(0));

                        for (final var t : monitor.getStorageList()) {
                            final var i = cacheMap.computeIfAbsent(t, ti -> FakeItemRegister.packAEStackLong(ti, FCGasItems.GAS_DROP));
                            if (i != null) {
                                i.setStackSize(t.getStackSize());
                                list.addStorage(i);
                            }
                        }
                        return list;
                    }

                };
            }
        }
    }

    @Override
    public FakeMonitor<IAEFluidStack> getFluidMonitor() {
        return fluidMonitor;
    }

    @Override
    public FakeMonitor<?> getGasMonitor() {
        return gasMonitor;
    }

    @Override
    public void fc$postChange(final boolean add, final Iterable<T> changes, final IActionSource src) {
        this.postChange(add, changes, src);
    }

    @Inject(method = "getAvailableItems", at = @At("TAIL"))
    public void getAvailableItems(final IItemList<T> out, final CallbackInfoReturnable<IItemList<T>> cir) {
        if (this.myChannel == Util.getItemChannel()) {
            fluidMonitor.getAvailableItems((IItemList<IAEItemStack>) out);
            if (ModAndClassUtil.GAS && gasMonitor != null) {
                gasMonitor.getAvailableItems((IItemList<IAEItemStack>) out);
            }
        }
    }

}