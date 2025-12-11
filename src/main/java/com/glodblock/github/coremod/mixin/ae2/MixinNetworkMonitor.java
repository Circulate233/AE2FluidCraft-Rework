package com.glodblock.github.coremod.mixin.ae2;

import appeng.api.config.FuzzyMode;
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

@SuppressWarnings("unchecked")
@Mixin(value = NetworkMonitor.class, remap = false)
public class MixinNetworkMonitor<T extends IAEStack<T>> implements FCNetworkMonitor {

    @Shadow
    @Final
    @Nonnull
    private IStorageChannel<?> myChannel;
    @Shadow
    @Final
    @Nonnull
    private GridStorageCache myGridCache;

    @Unique
    private FakeMonitor<IAEFluidStack> fluidMonitor;

    @Unique
    private FakeMonitor<?> gasMonitor;

    @Override
    public void init() {
        if (this.myChannel == Util.getItemChannel()) {
            fluidMonitor = new FakeMonitor<>(this.myGridCache, Util.getFluidChannel()) {

                @Override
                public IItemList<IAEItemStack> getAvailableItems(IItemList<IAEItemStack> list) {
                    list.findFuzzy(
                        AEItemStack.fromItemStack(new ItemStack(FCItems.FLUID_DROP,1)),
                        FuzzyMode.IGNORE_ALL
                    ).forEach(IAEItemStack::reset);

                    for (var t : monitor.getStorageList()) {
                        var i = cacheMap.computeIfAbsent(t, ti -> FakeItemRegister.packAEStackLong(ti, FCItems.FLUID_DROP));
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
                    public IItemList<IAEItemStack> getAvailableItems(IItemList<IAEItemStack> list) {
                        list.findFuzzy(
                            AEItemStack.fromItemStack(new ItemStack(FCGasItems.GAS_DROP,1)),
                            FuzzyMode.IGNORE_ALL
                        ).forEach(IAEItemStack::reset);

                        for (var t : monitor.getStorageList()) {
                            var i = cacheMap.computeIfAbsent(t, ti -> FakeItemRegister.packAEStackLong(ti, FCGasItems.GAS_DROP));
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

    @Inject(method = "getAvailableItems", at = @At("TAIL"))
    public void getAvailableItems(IItemList<T> out, CallbackInfoReturnable<IItemList<T>> cir) {
        if (this.myChannel == Util.getItemChannel()) {
            fluidMonitor.getAvailableItems((IItemList<IAEItemStack>) out);
            if (ModAndClassUtil.GAS && gasMonitor != null) {
                gasMonitor.getAvailableItems((IItemList<IAEItemStack>) out);
            }
        }
    }

}