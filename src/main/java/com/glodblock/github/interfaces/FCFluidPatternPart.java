package com.glodblock.github.interfaces;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.item.ItemStack;

import java.util.List;

public interface FCFluidPatternPart {

    boolean getCombineMode();

    void setCombineMode(boolean mode);

    boolean getFluidPlaceMode();

    void setFluidPlaceMode(boolean mode);

    default void onChangeCrafting(final Int2ObjectMap<ItemStack[]> inputs, final List<ItemStack> outputs, final boolean combine) {

    }
}
