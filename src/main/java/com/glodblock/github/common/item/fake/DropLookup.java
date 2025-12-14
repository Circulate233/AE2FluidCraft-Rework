package com.glodblock.github.common.item.fake;

import appeng.api.storage.data.IAEItemStack;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraftforge.fluids.FluidStack;

import java.util.Map;
import java.util.function.Function;

public final class DropLookup {

    private final static Map<FluidStack, IAEItemStack> FLUID_CACHE_A = new Object2ObjectLinkedOpenHashMap<>();

    public static IAEItemStack lookup(final FluidStack fluid, final Function<FluidStack, IAEItemStack> fallback) {
        IAEItemStack cache = FLUID_CACHE_A.get(fluid);
        if (cache == null) {
            cache = fallback.apply(fluid);
            FLUID_CACHE_A.put(fluid, cache);
        }
        return cache == null ? null : cache.copy();
    }

}
