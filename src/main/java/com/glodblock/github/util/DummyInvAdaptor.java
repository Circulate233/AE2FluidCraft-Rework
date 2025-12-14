package com.glodblock.github.util;

import appeng.api.config.FuzzyMode;
import appeng.util.InventoryAdaptor;
import appeng.util.inv.IInventoryDestination;
import appeng.util.inv.ItemSlot;
import net.minecraft.item.ItemStack;

import java.util.Iterator;

/**
 * This a dummy InventoryAdaptor, it has infinity capacity and infinity any items.
 */
public class DummyInvAdaptor extends InventoryAdaptor {

    public static final DummyInvAdaptor INSTANCE = new DummyInvAdaptor();

    @Override
    public ItemStack removeItems(final int i, final ItemStack itemStack, final IInventoryDestination iInventoryDestination) {
        return itemStack;
    }

    @Override
    public ItemStack simulateRemove(final int i, final ItemStack itemStack, final IInventoryDestination iInventoryDestination) {
        return itemStack;
    }

    @Override
    public ItemStack removeSimilarItems(final int i, final ItemStack itemStack, final FuzzyMode fuzzyMode, final IInventoryDestination iInventoryDestination) {
        return itemStack;
    }

    @Override
    public ItemStack simulateSimilarRemove(final int i, final ItemStack itemStack, final FuzzyMode fuzzyMode, final IInventoryDestination iInventoryDestination) {
        return itemStack;
    }

    @Override
    public ItemStack addItems(final ItemStack itemStack) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack simulateAdd(final ItemStack itemStack) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean containsItems() {
        return true;
    }

    @Override
    public boolean hasSlots() {
        return false;
    }

    @Override
    public Iterator<ItemSlot> iterator() {
        return null;
    }
}
