package com.glodblock.github.handler;

import appeng.api.storage.data.IAEItemStack;
import com.glodblock.github.interfaces.AeStackInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class AeItemStackHandler implements IItemHandler {

    private final AeStackInventory<IAEItemStack> inv;

    public AeItemStackHandler(final AeStackInventory<IAEItemStack> inv) {
        this.inv = inv;
    }

    public AeStackInventory<IAEItemStack> getAeInventory() {
        return inv;
    }

    @Override
    public int getSlots() {
        return inv.getSlotCount();
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(final int slot) {
        final IAEItemStack stack = inv.getStack(slot);
        return stack != null ? stack.createItemStack() : ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(final int slot, @Nonnull final ItemStack stack, final boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(final int slot, final int amount, final boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(final int slot) {
        return 64;
    }

}
