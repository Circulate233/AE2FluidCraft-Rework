package com.glodblock.github.common.tile;

import appeng.fluids.util.AEFluidInventory;
import appeng.fluids.util.IAEFluidInventory;
import appeng.fluids.util.IAEFluidTank;
import appeng.tile.AEBaseInvTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.inv.InvOperation;
import com.glodblock.github.util.Util;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

public class TileLargeIngredientBuffer extends AEBaseInvTile implements IAEFluidInventory {

    private final AppEngInternalInventory invItems = new AppEngInternalInventory(this, 27);
    private final AEFluidInventory invFluids = new AEFluidInventory(this, 7, 16000);

    @Nonnull
    @Override
    public IItemHandler getInternalInventory() {
        return invItems;
    }

    public IAEFluidTank getFluidInventory() {
        return invFluids;
    }

    @Override
    public boolean canBeRotated() {
        return false;
    }

    @Override
    public boolean hasCapability(final Capability<?> capability, final EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
                || capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
    }

    @Nullable
    @Override
    public <T> T getCapability(final Capability<T> capability, @Nullable final EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return (T)invItems;
        } else if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return (T)invFluids;
        }
        return null;
    }

    @Override
    public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc, final ItemStack removed, final ItemStack added) {
        markForUpdate();
    }

    @Override
    public void onFluidInventoryChanged(final IAEFluidTank inv, final int slot) {
        saveChanges();
        markForUpdate();
    }

    @Override
    public void onFluidInventoryChanged(final IAEFluidTank inv, final int slot, final InvOperation operation, final FluidStack added, final FluidStack removed) {
        this.onFluidInventoryChanged(inv, slot);
    }

    @Override
    protected void writeToStream(final ByteBuf data) throws IOException {
        super.writeToStream(data);
        for (int i = 0; i < invItems.getSlots(); i++) {
            ByteBufUtils.writeItemStack(data, invItems.getStackInSlot(i));
        }
        Util.writeFluidInventoryToBuffer(invFluids, data);
    }

    @Override
    protected boolean readFromStream(final ByteBuf data) throws IOException {
        boolean changed = super.readFromStream(data);
        for (int i = 0; i < invItems.getSlots(); i++) {
            final ItemStack stack = ByteBufUtils.readItemStack(data);
            if (!ItemStack.areItemStacksEqual(stack, invItems.getStackInSlot(i))) {
                invItems.setStackInSlot(i, stack);
                changed = true;
            }
        }
        changed |= Util.readFluidInventoryToBuffer(invFluids, data);
        return changed;
    }

    @Override
    public void readFromNBT(final NBTTagCompound data) {
        super.readFromNBT(data);
        invItems.readFromNBT(data, "ItemInv");
        invFluids.readFromNBT(data, "FluidInv");
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound data) {
        super.writeToNBT(data);
        invItems.writeToNBT(data, "ItemInv");
        invFluids.writeToNBT(data, "FluidInv");
        return data;
    }

}
