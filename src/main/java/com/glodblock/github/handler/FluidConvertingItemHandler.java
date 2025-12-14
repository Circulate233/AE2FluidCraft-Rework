package com.glodblock.github.handler;

import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.common.item.fake.FakeFluids;
import com.glodblock.github.common.item.fake.FakeItemRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class FluidConvertingItemHandler  implements IItemHandler {

    public static FluidConvertingItemHandler wrap(final ICapabilityProvider capProvider, final EnumFacing face) {
        // sometimes i wish i had the monadic version from 1.15
        return new FluidConvertingItemHandler(
                capProvider.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, face)
                        ? Objects.requireNonNull(capProvider.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, face))
                        : null,
                capProvider.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, face)
                        ? Objects.requireNonNull(capProvider.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, face))
                        : null);
    }

    @Nullable
    private final IItemHandler invItems;
    @Nullable
    private final IFluidHandler invFluids;

    private FluidConvertingItemHandler(@Nullable final IItemHandler invItems, @Nullable final IFluidHandler invFluids) {
        this.invItems = invItems;
        this.invFluids = invFluids;
    }

    @Override
    public int getSlots() {
        int slots = 0;
        if (invItems != null) {
            slots += invItems.getSlots();
        }
        if (invFluids != null) {
            slots += invFluids.getTankProperties().length;
        }
        return slots;
    }

    @Override
    @Nonnull
    public ItemStack getStackInSlot(final int slot) {
        return slotOp(slot, IItemHandler::getStackInSlot,
                (fh, i) -> FakeFluids.packFluid2Drops(fh.getTankProperties()[i].getContents()));
    }

    @Override
    @Nonnull
    public ItemStack insertItem(final int slot, @Nonnull final ItemStack stack, final boolean simulate) {
        return slotOp(slot,
                (ih, i) -> (stack.getItem() instanceof ItemFluidDrop || stack.getItem() instanceof ItemFluidPacket)
                        ? stack : ih.insertItem(i, stack, simulate),
                (fh, i) -> {
                    if (stack.getItem() instanceof ItemFluidDrop) {
                        final FluidStack toInsert = FakeItemRegister.getStack(stack);
                        if (toInsert != null && toInsert.amount > 0) {
                            final FluidStack contained = fh.getTankProperties()[i].getContents();
                            if (contained == null || contained.amount == 0 || contained.isFluidEqual(toInsert)) {
                                toInsert.amount -= fh.fill(toInsert, !simulate);
                                return FakeFluids.packFluid2Drops(toInsert);
                            }
                        }
                    } else if (stack.getItem() instanceof ItemFluidPacket) {
                        final FluidStack toInsert = FakeItemRegister.getStack(stack);
                        if (toInsert != null && toInsert.amount > 0) {
                            final FluidStack contained = fh.getTankProperties()[i].getContents();
                            if (contained == null || contained.amount == 0 || contained.isFluidEqual(toInsert)) {
                                final int insertable = fh.fill(toInsert, false); // only insert if the entire packet fits
                                if (insertable >= toInsert.amount) {
                                    if (!simulate) {
                                        fh.fill(toInsert, true);
                                    }
                                    return ItemStack.EMPTY;
                                }
                            }
                        }
                    }
                    return stack;
                });
    }

    @Override
    @Nonnull
    public ItemStack extractItem(final int slot, final int amount, final boolean simulate) {
        return slotOp(slot, (ih, i) -> ih.extractItem(i, slot, simulate), (fh, i) -> {
            final FluidStack contained = fh.getTankProperties()[i].getContents();
            if (contained != null && contained.amount > 0) {
                return FakeFluids.packFluid2Drops(fh.drain(contained, !simulate));
            }
            return ItemStack.EMPTY;
        });
    }

    @Override
    public int getSlotLimit(final int slot) {
        return slotOp(slot, IItemHandler::getSlotLimit, (fh, i) -> fh.getTankProperties()[i].getCapacity());
    }

    @Override
    public boolean isItemValid(final int slot, @Nonnull final ItemStack stack) {
        return slotOp(slot, (ih, i) -> ih.isItemValid(i, stack),
                (fh, i) -> stack.getItem() instanceof ItemFluidDrop || stack.getItem() instanceof ItemFluidPacket);
    }

    private <T> T slotOp(final int slot, final Op<IItemHandler, T> itemConsumer, final Op<IFluidHandler, T> fluidConsumer) {
        if (slot >= 0) {
            int fluidSlot = slot;
            if (invItems != null) {
                if (slot < invItems.getSlots()) {
                    return itemConsumer.apply(invItems, slot);
                } else {
                    fluidSlot -= invItems.getSlots();
                }
            }
            if (invFluids != null) {
                final IFluidTankProperties[] tanks = invFluids.getTankProperties();
                if (fluidSlot < tanks.length) {
                    return fluidConsumer.apply(invFluids, fluidSlot);
                }
            }
        }
        throw new IndexOutOfBoundsException(String.format("Slot index %d out of bounds! |items| = %d, |fluids| = %d", slot,
                invItems != null ? invItems.getSlots() : 0, invFluids != null ? invFluids.getTankProperties().length : 0));
    }

    @FunctionalInterface
    private interface Op<C, T> {

        T apply(C collection, int index);

    }

}
