package com.glodblock.github.interfaces;

import appeng.api.storage.data.IAEFluidStack;
import com.glodblock.github.util.FakeMonitor;

public interface FCNetworkMonitor {

    void init();

    FakeMonitor<IAEFluidStack> getFluidMonitor();

    FakeMonitor<?> getGasMonitor();
}
