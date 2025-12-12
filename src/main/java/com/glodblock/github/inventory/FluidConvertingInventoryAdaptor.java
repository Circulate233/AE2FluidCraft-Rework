package com.glodblock.github.inventory;

import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.parts.IPart;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.fluids.helper.DualityFluidInterface;
import appeng.fluids.helper.IFluidInterfaceHost;
import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.tile.misc.TileInterface;
import appeng.tile.networking.TileCableBus;
import appeng.util.InventoryAdaptor;
import appeng.util.inv.AdaptorItemHandler;
import appeng.util.inv.IInventoryDestination;
import appeng.util.inv.ItemSlot;
import com.glodblock.github.common.item.fake.FakeFluids;
import com.glodblock.github.common.item.fake.FakeItemRegister;
import com.glodblock.github.common.tile.TileDualInterface;
import com.glodblock.github.integration.mek.FakeGases;
import com.glodblock.github.interfaces.FCDualityInterface;
import com.glodblock.github.util.Ae2Reflect;
import com.glodblock.github.util.ModAndClassUtil;
import com.glodblock.github.util.Util;
import com.mekeng.github.common.me.data.IAEGasStack;
import com.mekeng.github.common.me.duality.IGasInterfaceHost;
import com.mekeng.github.common.me.duality.impl.DualityGasInterface;
import com.mekeng.github.common.me.storage.IGasStorageChannel;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTankInfo;
import mekanism.api.gas.IGasHandler;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;

public class FluidConvertingInventoryAdaptor extends InventoryAdaptor {

    public static InventoryAdaptor wrap(ICapabilityProvider capProvider, EnumFacing face) {
        TileEntity cap = (TileEntity) capProvider;
        TileEntity inter = cap.getWorld().getTileEntity(cap.getPos().add(face.getDirectionVec()));
        DualityInterface dualInterface = getInterfaceTE(inter, face) == null ?
                null : Objects.requireNonNull(getInterfaceTE(inter, face)).getInterfaceDuality();
        boolean onmi = false;
        if (inter instanceof TileInterface) {
            onmi = ((TileInterface) inter).getTargets().size() > 1;
        } else if (inter instanceof TileDualInterface) {
            onmi = ((TileDualInterface) inter).getTargets().size() > 1;
        }

        if (dualInterface == null || !((FCDualityInterface) dualInterface).isFluidPacket()) {
            return new FluidConvertingInventoryAdaptor(
                    capProvider,
                    inter,
                    onmi,
                    dualInterface,
                    face.getOpposite());
        }
        return InventoryAdaptor.getAdaptor(cap, face);
    }

    @Nullable
    private final InventoryAdaptor invItems;
    @Nullable
    private final IFluidHandler invFluids;
    @Nullable
    /*mek my beloved*/
    private final Object invGases;
    private final ICapabilityProvider facingTE;
    private final boolean onmi;
    private final EnumFacing facing;
    @Nullable
    private final TileEntity posInterface;
    @Nullable
    private final DualityInterface self;

    public FluidConvertingInventoryAdaptor(ICapabilityProvider facingTE, @Nullable TileEntity pos, boolean isOnmi, @Nullable DualityInterface interSelf, EnumFacing facing) {
        this.invItems = facingTE.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite())
                ? new AdaptorItemHandler(Objects.requireNonNull(facingTE.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite())))
                : null;
        this.invFluids = facingTE.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite());
        this.invGases = ModAndClassUtil.GAS ? facingTE.getCapability(Capabilities.GAS_HANDLER_CAPABILITY, facing.getOpposite()) : null;
        this.facingTE = facingTE;
        this.posInterface = pos;
        this.onmi = isOnmi;
        this.self = interSelf;
        this.facing = facing;
    }

    @Override
    public ItemStack addItems(ItemStack toBeAdded) {
        if (FakeFluids.isFluidFakeItem(toBeAdded)) {
            if (onmi) {
                FluidStack fluid = FakeItemRegister.getStack(toBeAdded);
                // First try to output to the same side
                if (invFluids != null) {
                    if (fluid != null) {
                        int filled = invFluids.fill(fluid, true);
                        if (filled > 0) {
                            fluid.amount -= filled;
                            return FakeFluids.packFluid2Packet(fluid);
                        }
                    }
                }

                if (self != null && fluid != null && posInterface != null && ((FCDualityInterface) self).isAllowSplitting()) {
                    for (EnumFacing dir : EnumFacing.values()) {
                        TileEntity te = posInterface.getWorld().getTileEntity(posInterface.getPos().add(dir.getDirectionVec()));
                        if (te != null) {
                            IFluidInterfaceHost interFTE = getFluidInterfaceTE(te, dir);
                            if (interFTE != null && isSameGrid(interFTE)) {
                                continue;
                            }
                            IFluidHandler fh = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, dir.getOpposite());
                            if (fh != null) {
                                int filled = fh.fill(fluid, false);
                                if (filled == fluid.amount) {
                                    fh.fill(fluid, true);
                                    return ItemStack.EMPTY;
                                }
                            }
                        }
                    }
                }
                return FakeFluids.packFluid2Packet(fluid);
            }
            if (invFluids != null) {
                FluidStack fluid = FakeItemRegister.getStack(toBeAdded);
                if (fluid != null) {
                    int filled = invFluids.fill(fluid, true);
                    if (filled > 0) {
                        fluid.amount -= filled;
                        return FakeFluids.packFluid2Packet(fluid);
                    }
                }
            }
            return toBeAdded;
        }
        if (ModAndClassUtil.GAS && FakeGases.isGasFakeItem(toBeAdded)) {
            if (onmi) {
                GasStack gas = FakeItemRegister.getStack(toBeAdded);
                if (invGases != null && posInterface != null) {
                    TileEntity te = (TileEntity) facingTE;
                    AENetworkProxy node = getGasInterfaceGrid(te, facing);
                    IGasHandler gasHandler = (IGasHandler) invGases;
                    if (!isSameGrid(node)) {
                        if (gas != null && gas.getGas() != null) {
                            int filled = gasHandler.receiveGas(facing.getOpposite(), gas, true);
                            if (filled > 0) {
                                gas.amount -= filled;
                                return FakeGases.packGas2Packet(gas);
                            }
                        }
                    }
                }

                if (self != null && gas != null && gas.getGas() != null && posInterface != null && ((FCDualityInterface) self).isAllowSplitting()) {
                    for (EnumFacing dir : EnumFacing.values()) {
                        TileEntity te = posInterface.getWorld().getTileEntity(posInterface.getPos().add(dir.getDirectionVec()));
                        if (te != null) {
                            AENetworkProxy node = getGasInterfaceGrid(te, dir);
                            if (node != null && isSameGrid(node)) {
                                continue;
                            }
                            IGasHandler gh = te.getCapability(Capabilities.GAS_HANDLER_CAPABILITY, dir.getOpposite());
                            if (gh != null) {
                                int filled = gh.receiveGas(dir.getOpposite(), gas, false);
                                if (filled == gas.amount) {
                                    gh.receiveGas(dir.getOpposite(), gas, true);
                                    return ItemStack.EMPTY;
                                }
                            }
                        }
                    }
                }
                return FakeGases.packGas2Packet(gas);
            }
            if (invGases != null && posInterface != null) {
                GasStack gas = FakeItemRegister.getStack(toBeAdded);
                TileEntity te = (TileEntity) facingTE;
                AENetworkProxy node = getGasInterfaceGrid(te, facing);
                IGasHandler gasHandler = (IGasHandler) invGases;
                if (!isSameGrid(node)) {
                    if (gas != null && gas.getGas() != null) {
                        int filled = gasHandler.receiveGas(facing.getOpposite(), gas, true);
                        if (filled > 0) {
                            gas.amount -= filled;
                            return FakeGases.packGas2Packet(gas);
                        }
                    }
                }
            }
            return toBeAdded;
        }
        return invItems != null ? invItems.addItems(toBeAdded) : toBeAdded;
    }

    @Override
    public ItemStack simulateAdd(ItemStack toBeSimulated) {
        if (FakeFluids.isFluidFakeItem(toBeSimulated)) {
            if (onmi) {
                boolean sus = false;
                FluidStack fluid = FakeItemRegister.getStack(toBeSimulated);

                // First try to output to the same side
                if (invFluids != null) {
                    if (fluid != null) {
                        int filled = invFluids.fill(fluid, false);
                        if (filled > 0) {
                            fluid.amount -= filled;
                            return FakeFluids.packFluid2Packet(fluid);
                        }
                    }
                }

                if (fluid != null && posInterface != null) {
                    if (self != null && ((FCDualityInterface) self).isAllowSplitting()) {
                        for (EnumFacing dir : EnumFacing.values()) {
                            TileEntity te = posInterface.getWorld().getTileEntity(posInterface.getPos().add(dir.getDirectionVec()));
                            if (te != null) {
                                IInterfaceHost interTE = getInterfaceTE(te, dir);
                                if (interTE != null && isSameGrid(interTE)) {
                                    continue;
                                }
                                IFluidInterfaceHost interFTE = getFluidInterfaceTE(te, dir);
                                if (interFTE != null && isSameGrid(interFTE)) {
                                    continue;
                                }
                                IFluidHandler fh = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, dir.getOpposite());
                                if (fh != null) {
                                    int filled = fh.fill(fluid, false);
                                    if (filled == fluid.amount) {
                                        sus = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                } else {
                    sus = true;
                }
                return sus ? ItemStack.EMPTY : toBeSimulated;
            }
            if (invFluids != null) {
                FluidStack fluid = FakeItemRegister.getStack(toBeSimulated);
                if (fluid != null) {
                    int filled = invFluids.fill(fluid, false);
                    if (filled > 0) {
                        fluid.amount -= filled;
                        return FakeFluids.packFluid2Packet(fluid);
                    }
                }
            }
            return toBeSimulated;
        }
        if (ModAndClassUtil.GAS && FakeGases.isGasFakeItem(toBeSimulated)) {
            if (onmi) {
                boolean sus = false;
                GasStack gas = FakeItemRegister.getStack(toBeSimulated);
                if (invGases != null && posInterface != null) {
                    TileEntity te = (TileEntity) facingTE;
                    AENetworkProxy node = getGasInterfaceGrid(te, facing);
                    IGasHandler gasHandler = (IGasHandler) invGases;
                    if (!isSameGrid(node)) {
                        if (gas != null && gas.getGas() != null) {
                            int filled = gasHandler.receiveGas(facing.getOpposite(), gas, false);
                            if (filled > 0) {
                                gas.amount -= filled;
                                return FakeGases.packGas2Packet(gas);
                            }
                        }
                    }
                }

                if (gas != null && posInterface != null) {
                    if (self != null && ((FCDualityInterface) self).isAllowSplitting()) {
                        for (EnumFacing dir : EnumFacing.values()) {
                            TileEntity te = posInterface.getWorld().getTileEntity(posInterface.getPos().add(dir.getDirectionVec()));
                            if (te != null) {
                                IInterfaceHost interTE = getInterfaceTE(te, dir);
                                if (interTE != null && isSameGrid(interTE)) {
                                    continue;
                                }
                                IFluidInterfaceHost interFTE = getFluidInterfaceTE(te, dir);
                                if (interFTE != null && isSameGrid(interFTE)) {
                                    continue;
                                }
                                AENetworkProxy node = getGasInterfaceGrid(te, dir);
                                if (isSameGrid(node)) {
                                    continue;
                                }
                                IGasHandler gh = te.getCapability(Capabilities.GAS_HANDLER_CAPABILITY, dir.getOpposite());
                                if (gh != null) {
                                    int filled = gh.receiveGas(dir.getOpposite(), gas, false);
                                    if (filled == gas.amount) {
                                        sus = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                } else {
                    sus = true;
                }
                return sus ? ItemStack.EMPTY : toBeSimulated;
            }
            if (invGases != null && posInterface != null) {
                GasStack gas = FakeItemRegister.getStack(toBeSimulated);
                TileEntity te = (TileEntity) facingTE;
                AENetworkProxy node = getGasInterfaceGrid(te, facing);
                IGasHandler gasHandler = (IGasHandler) invGases;
                if (!isSameGrid(node)) {
                    if (gas != null) {
                        int filled = gasHandler.receiveGas(facing.getOpposite(), gas, false);
                        if (filled > 0) {
                            gas.amount -= filled;
                            return FakeGases.packGas2Packet(gas);
                        }
                    }
                }
            }
            return toBeSimulated;
        }
        return invItems != null ? invItems.simulateAdd(toBeSimulated) : toBeSimulated;
    }

    @Override
    public ItemStack removeItems(int amount, ItemStack filter, IInventoryDestination destination) {
        return invItems != null ? invItems.removeItems(amount, filter, destination) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack simulateRemove(int amount, ItemStack filter, IInventoryDestination destination) {
        return invItems != null ? invItems.simulateRemove(amount, filter, destination) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeSimilarItems(int amount, ItemStack filter, FuzzyMode fuzzyMode, IInventoryDestination destination) {
        return invItems != null ? invItems.removeSimilarItems(amount, filter, fuzzyMode, destination) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack simulateSimilarRemove(int amount, ItemStack filter, FuzzyMode fuzzyMode, IInventoryDestination destination) {
        return invItems != null ? invItems.simulateSimilarRemove(amount, filter, fuzzyMode, destination) : ItemStack.EMPTY;
    }

    @Override
    public boolean containsItems() {
        if (invItems == null && invFluids == null && invGases == null) {
            return true;
        }
        int blockMode = 0;
        if (this.self != null) {
            blockMode = ((FCDualityInterface) this.self).getBlockModeEx();
        }
        boolean checkFluid = blockMode != 1;
        boolean checkItem = blockMode != 2;

        IFluidInterfaceHost fluidHost = getFluidInterfaceTE((TileEntity) facingTE, facing);
        if (fluidHost != null && !isSameGrid(fluidHost) && checkFluid) {
            DualityFluidInterface inter = fluidHost.getDualityFluidInterface();
            IMEMonitor<IAEFluidStack> monitor = inter.getInventory(Util.getFluidChannel());
            if (monitor != null && !monitor.getStorageList().isEmpty()) {
                return true;
            }
        } else if (invFluids != null && checkFluid) {
            for (IFluidTankProperties tank : invFluids.getTankProperties()) {
                FluidStack fluid = tank.getContents();
                if (fluid != null && fluid.amount > 0) {
                    return true;
                }
            }
        }

        /*yeah, gas is fluid*/
        if (ModAndClassUtil.GAS) {
            Object gasHost = getGasInterfaceTE((TileEntity) facingTE, facing);
            if (gasHost != null && !isSameGrid(((IGasInterfaceHost) gasHost).getProxy()) && checkFluid) {
                DualityGasInterface inter = ((IGasInterfaceHost) gasHost).getDualityGasInterface();
                IMEMonitor<IAEGasStack> monitor = inter.getInventory(AEApi.instance().storage().getStorageChannel(IGasStorageChannel.class));
                if (monitor != null && !monitor.getStorageList().isEmpty()) {
                    return true;
                }
            } else if (invGases != null && checkFluid) {
                IGasHandler gasHandler = (IGasHandler) invGases;
                for (GasTankInfo tank : gasHandler.getTankInfo()) {
                    GasStack gas = tank.getGas();
                    if (gas != null && gas.getGas() != null && gas.amount > 0) {
                        return true;
                    }
                }
            }
        }

        IInterfaceHost itemHost = getInterfaceTE((TileEntity) facingTE, facing);
        if (itemHost != null && !isSameGrid(itemHost) && checkItem) {
            DualityInterface inter = itemHost.getInterfaceDuality();
            IMEMonitor<IAEItemStack> monitor = inter.getInventory(Util.getItemChannel());
            return monitor != null && !monitor.getStorageList().isEmpty();
        } else if (invItems != null && checkItem) {
            return invItems.containsItems();
        }
        return false;
    }

    @Override
    public boolean hasSlots() {
        return (invFluids != null && invFluids.getTankProperties().length > 0)
                || (invItems != null && invItems.hasSlots());
    }

    @Nullable
    protected static IInterfaceHost getInterfaceTE(TileEntity te, EnumFacing face) {
        if (te instanceof IInterfaceHost) {
            return (IInterfaceHost) te;
        } else if (te instanceof TileCableBus) {
            IPart part = ((TileCableBus) te).getPart(face.getOpposite());
            if (part instanceof IInterfaceHost) {
                return (IInterfaceHost) part;
            }
        }
        return null;
    }

    @Nullable
    protected static IFluidInterfaceHost getFluidInterfaceTE(TileEntity te, EnumFacing face) {
        if (te instanceof IFluidInterfaceHost) {
            return (IFluidInterfaceHost) te;
        } else if (te instanceof TileCableBus) {
            IPart part = ((TileCableBus) te).getPart(face.getOpposite());
            if (part instanceof IFluidInterfaceHost) {
                return (IFluidInterfaceHost) part;
            }
        }
        return null;
    }

    @Nullable
    protected static Object getGasInterfaceTE(TileEntity te, EnumFacing face) {
        if (te instanceof IGasInterfaceHost) {
            return te;
        } else if (te instanceof TileCableBus) {
            IPart part = ((TileCableBus) te).getPart(face.getOpposite());
            if (part instanceof IGasInterfaceHost) {
                return part;
            }
        }
        return null;
    }

    protected static AENetworkProxy getGasInterfaceGrid(@Nullable TileEntity te, EnumFacing face) {
        if (te instanceof IGasInterfaceHost) {
            return Ae2Reflect.getGasInterfaceGrid(((IGasInterfaceHost) te).getDualityGasInterface());
        } else if (te instanceof TileCableBus) {
            IPart part = ((TileCableBus) te).getPart(face.getOpposite());
            if (part instanceof IGasInterfaceHost) {
                return Ae2Reflect.getGasInterfaceGrid(((IGasInterfaceHost) part).getDualityGasInterface());
            }
        }
        return null;
    }

    private boolean isSameGrid(IInterfaceHost target) {
        if (this.self != null && target != null) {
            DualityInterface other = target.getInterfaceDuality();
            try {
                AENetworkProxy proxy1 = Ae2Reflect.getInterfaceProxy(other);
                AENetworkProxy proxy2 = Ae2Reflect.getInterfaceProxy(this.self);
                if (proxy1.getGrid() == proxy2.getGrid()) {
                    return true;
                }
            } catch (GridAccessException e) {
                return false;
            }
        }
        return false;
    }

    private boolean isSameGrid(IFluidInterfaceHost target) {
        if (this.self != null && target != null) {
            DualityFluidInterface other = target.getDualityFluidInterface();
            try {
                AENetworkProxy proxy1 = Ae2Reflect.getInterfaceProxy(other);
                AENetworkProxy proxy2 = Ae2Reflect.getInterfaceProxy(this.self);
                if (proxy1.getGrid() == proxy2.getGrid()) {
                    return true;
                }
            } catch (GridAccessException e) {
                return false;
            }
        }
        return false;
    }

    private boolean isSameGrid(AENetworkProxy target) {
        if (this.self != null && target != null) {
            try {
                AENetworkProxy proxy = Ae2Reflect.getInterfaceProxy(this.self);
                if (proxy.getGrid() == target.getGrid()) {
                    return true;
                }
            } catch (GridAccessException e) {
                return false;
            }
        }
        return false;
    }

    @Override
    @Nonnull
    public Iterator<ItemSlot> iterator() {
        return new SlotIterator(
                invFluids != null ? invFluids.getTankProperties() : new IFluidTankProperties[0],
                invItems != null ? invItems.iterator() : Collections.emptyIterator());
    }

    private static class SlotIterator implements Iterator<ItemSlot> {

        private final IFluidTankProperties[] tanks;
        private final Iterator<ItemSlot> itemSlots;
        private int nextSlotIndex = 0;

        SlotIterator(IFluidTankProperties[] tanks, Iterator<ItemSlot> itemSlots) {
            this.tanks = tanks;
            this.itemSlots = itemSlots;
        }

        @Override
        public boolean hasNext() {
            return nextSlotIndex < tanks.length || itemSlots.hasNext();
        }

        @Override
        public ItemSlot next() {
            if (nextSlotIndex < tanks.length) {
                FluidStack fluid = tanks[nextSlotIndex].getContents();
                ItemSlot slot = new ItemSlot();
                slot.setSlot(nextSlotIndex++);
                slot.setItemStack(fluid != null ? FakeFluids.packFluid2Packet(fluid) : ItemStack.EMPTY);
                Ae2Reflect.setItemSlotExtractable(slot, false);
                return slot;
            } else {
                ItemSlot slot = itemSlots.next();
                slot.setSlot(nextSlotIndex++);
                return slot;
            }
        }

    }

}
