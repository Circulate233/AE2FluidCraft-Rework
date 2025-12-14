package com.glodblock.github.coremod;

import appeng.api.AEApi;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IMachineSet;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.security.IActionHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.ContainerOpenContext;
import appeng.container.implementations.ContainerCraftConfirm;
import appeng.container.implementations.ContainerPatternEncoder;
import appeng.container.implementations.CraftingCPURecord;
import appeng.fluids.parts.PartFluidInterface;
import appeng.fluids.tile.TileFluidInterface;
import appeng.helpers.DualityInterface;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.parts.misc.PartInterface;
import appeng.tile.misc.TileInterface;
import appeng.util.InventoryAdaptor;
import appeng.util.inv.BlockingInventoryAdaptor;
import appeng.util.item.AEItemStack;
import com.glodblock.github.client.container.ContainerFCCraftConfirm;
import com.glodblock.github.common.item.ItemFluidCraftEncodedPattern;
import com.glodblock.github.common.item.ItemFluidEncodedPattern;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.common.item.ItemLargeEncodedPattern;
import com.glodblock.github.common.item.fake.FakeFluids;
import com.glodblock.github.common.item.fake.FakeItemRegister;
import com.glodblock.github.common.part.PartDualInterface;
import com.glodblock.github.common.part.PartExtendedFluidPatternTerminal;
import com.glodblock.github.common.part.PartFluidPatternTerminal;
import com.glodblock.github.common.tile.TileDualInterface;
import com.glodblock.github.handler.FluidConvertingItemHandler;
import com.glodblock.github.integration.mek.FCGasItems;
import com.glodblock.github.integration.mek.FakeGases;
import com.glodblock.github.integration.mek.GasInterfaceUtil;
import com.glodblock.github.interfaces.FCDualityInterface;
import com.glodblock.github.inventory.BlockingFluidInventoryAdaptor;
import com.glodblock.github.inventory.FluidConvertingInventoryAdaptor;
import com.glodblock.github.inventory.FluidConvertingInventoryCrafting;
import com.glodblock.github.inventory.GuiType;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.loader.FCItems;
import com.glodblock.github.util.Ae2Reflect;
import com.glodblock.github.util.ModAndClassUtil;
import com.glodblock.github.util.SetBackedMachineSet;
import mekanism.api.gas.GasStack;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Optional;

@SuppressWarnings("unused")
public class CoreModHooks {

    public static InventoryCrafting wrapCraftingBuffer(final InventoryCrafting inv) {
        final int s = inv.getWidth() > 3 ? 10 : 3;
        return new FluidConvertingInventoryCrafting(Ae2Reflect.getCraftContainer(inv), s, s);
    }

    public static InventoryCrafting wrapCraftingBuffer(final Container container, final int width, final int height) {
        return new FluidConvertingInventoryCrafting(container, width, height);
    }

    public static IAEItemStack wrapFluidPacketStack(final IAEItemStack stack) {
        if (stack.getItem() == FCItems.FLUID_PACKET) {
            final IAEItemStack dropStack = FakeFluids.packFluid2AEDrops((FluidStack) FakeItemRegister.getStack(stack));
            if (dropStack != null) {
                return dropStack;
            }
        }
        if (ModAndClassUtil.GAS && stack.getItem() == FCGasItems.GAS_PACKET) {
            final IAEItemStack dropStack = FakeGases.packGas2AEDrops((GasStack) FakeItemRegister.getStack(stack));
            if (dropStack != null) {
                return dropStack;
            }
        }
        return stack;
    }

    @Nullable
    public static InventoryAdaptor wrapInventory(@Nullable final TileEntity tile, final EnumFacing face) {
        return tile != null ? FluidConvertingInventoryAdaptor.wrap(tile, face) : null;
    }

    @Nullable
    public static BlockingInventoryAdaptor wrapBlockInventory(@Nullable final TileEntity tile, final EnumFacing face) {
        return tile != null ? BlockingFluidInventoryAdaptor.getAdaptor(tile, face) : null;
    }

    public static void writeExtraNBTInterface(final DualityInterface dual, final NBTTagCompound nbt) {
        nbt.setBoolean("fluidPacket", ((FCDualityInterface) dual).isFluidPacket());
        nbt.setBoolean("allowSplitting", ((FCDualityInterface) dual).isAllowSplitting());
        nbt.setInteger("blockModeEx", ((FCDualityInterface) dual).getBlockModeEx());
    }

    public static void readExtraNBTInterface(final DualityInterface dual, final NBTTagCompound nbt) {
        final boolean value = nbt.getBoolean("fluidPacket");
        ((FCDualityInterface) dual).setFluidPacket(value);
        final boolean value1 = !nbt.hasKey("allowSplitting") || nbt.getBoolean("allowSplitting");
        ((FCDualityInterface) dual).setAllowSplitting(value1);
        final int value2 = nbt.getInteger("blockModeEx");
        ((FCDualityInterface) dual).setBlockModeEx(value2);
    }

    public static ItemStack removeFluidPackets(final InventoryCrafting inv, final int index) {
        final ItemStack stack = inv.getStackInSlot(index);
        if (!stack.isEmpty() && stack.getItem() == FCItems.FLUID_PACKET) {
            final FluidStack fluid = FakeItemRegister.getStack(stack);
            return FakeFluids.packFluid2Drops(fluid);
        }
        if (ModAndClassUtil.GAS && !stack.isEmpty() && stack.getItem() == FCGasItems.GAS_PACKET) {
            final GasStack gas = FakeItemRegister.getStack(stack);
            return FakeGases.packGas2Drops(gas);
        } else {
            return stack;
        }
    }

    public static long getCraftingByteCost(final IAEItemStack stack) {
        if (stack.getItem() == FCItems.FLUID_DROP) {
            return (long) Math.ceil(stack.getStackSize() / 1000D);
        } else if (ModAndClassUtil.GAS && stack.getItem() == FCGasItems.GAS_DROP) {
            return (long) Math.ceil(stack.getStackSize() / 4000D);
        }
        return stack.getStackSize();
    }

    public static long getCraftingByteCost(final long originBytes, final long missingBytes, final IAEItemStack stack) {
        if (stack != null && stack.getItem() == FCItems.FLUID_DROP) {
            return (long) Math.ceil(missingBytes / 1000D) + originBytes;
        } else if (ModAndClassUtil.GAS && stack != null && stack.getItem() == FCGasItems.GAS_DROP) {
            return (long) Math.ceil(missingBytes / 4000D) + originBytes;
        }
        return missingBytes + originBytes;
    }

    public static boolean checkForItemHandler(final ICapabilityProvider capProvider, final Capability<?> capability, final EnumFacing side) {
        return capProvider.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side)
            || capProvider.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side);
    }

    public static IItemHandler wrapItemHandler(final ICapabilityProvider capProvider, final Capability<?> capability, final EnumFacing side) {
        return FluidConvertingItemHandler.wrap(capProvider, side);
    }

    public static IAEItemStack[] flattenFluidPackets(final IAEItemStack[] stacks) {
        for (int i = 0; i < stacks.length; i++) {
            if (stacks[i] != null && stacks[i].getItem() instanceof ItemFluidPacket) {
                stacks[i] = FakeFluids.packFluid2AEDrops((FluidStack) FakeItemRegister.getStack(stacks[i]));
            }
        }
        return stacks;
    }

    public static IMachineSet getMachines(final IGrid grid, final Class<? extends IGridHost> c) {
        if (c == TileInterface.class || c == TileFluidInterface.class) {
            if (ModAndClassUtil.GAS) {
                return unionMachineSets(grid.getMachines(c), grid.getMachines(TileDualInterface.class), GasInterfaceUtil.getGasInterface(grid));
            } else {
                return unionMachineSets(grid.getMachines(c), grid.getMachines(TileDualInterface.class));
            }
        } else if (c == PartInterface.class || c == PartFluidInterface.class) {
            if (ModAndClassUtil.GAS) {
                return unionMachineSets(grid.getMachines(c), grid.getMachines(PartDualInterface.class), GasInterfaceUtil.getGasPartInterface(grid));
            } else {
                return unionMachineSets(grid.getMachines(c), grid.getMachines(PartDualInterface.class));
            }
        } else if (ModAndClassUtil.GAS && GasInterfaceUtil.isGasInterfaceTile(c)) {
            return unionMachineSets(grid.getMachines(c), GasInterfaceUtil.getGasInterface(grid));
        } else if (ModAndClassUtil.GAS && GasInterfaceUtil.isGasInterfacePart(c)) {
            return unionMachineSets(grid.getMachines(c), GasInterfaceUtil.getGasPartInterface(grid));
        }
        return grid.getMachines(c);
    }

    public static Object wrapFluidPacket(final Object obj) {
        if (obj instanceof final ItemStack stack) {
            if (FakeFluids.isFluidFakeItem(stack)) {
                return FakeItemRegister.getStack(stack);
            }
            if (ModAndClassUtil.GAS && FakeGases.isGasFakeItem(stack)) {
                return FakeItemRegister.getStack(stack);
            }
        }
        return obj;
    }

    private static IMachineSet unionMachineSets(final IMachineSet... sets) {
        return SetBackedMachineSet.combine(TileInterface.class, sets);
    }

    public static ItemStack displayFluid(final ItemStack drop) {
        if (!drop.isEmpty() && drop.getItem() == FCItems.FLUID_DROP) {
            final FluidStack fluid = FakeItemRegister.getStack(drop);
            return FakeFluids.displayFluid(fluid);
        } else if (!drop.isEmpty() && ModAndClassUtil.GAS && drop.getItem() == FCGasItems.GAS_DROP) {
            final GasStack gas = FakeItemRegister.getStack(drop);
            return FakeGases.displayGas(gas);
        } else return drop;
    }

    public static IAEItemStack displayAEFluid(final IAEItemStack drop) {
        if (!drop.getDefinition().isEmpty() && drop.getItem() == FCItems.FLUID_DROP) {
            final FluidStack fluid = FakeItemRegister.getStack(drop);
            return AEItemStack.fromItemStack(FakeFluids.displayFluid(fluid));
        } else if (!drop.getDefinition().isEmpty() && ModAndClassUtil.GAS && drop.getItem() == FCGasItems.GAS_DROP) {
            final GasStack gas = FakeItemRegister.getStack(drop);
            return AEItemStack.fromItemStack(FakeGases.displayGas(gas));
        } else return drop;
    }

    public static IAEItemStack displayAEFluidAmount(final IAEItemStack drop) {
        if (drop != null && !drop.getDefinition().isEmpty()) {
            if (drop.getItem() == FCItems.FLUID_DROP) {
                final FluidStack fluid = FakeItemRegister.getStack(drop);
                final AEItemStack stack = AEItemStack.fromItemStack(FakeFluids.displayFluid(fluid));
                return stack == null ? null : stack.setStackSize(drop.getStackSize());
            }
            if (ModAndClassUtil.GAS && drop.getItem() == FCGasItems.GAS_DROP) {
                final GasStack gas = FakeItemRegister.getStack(drop);
                final AEItemStack stack = AEItemStack.fromItemStack(FakeGases.displayGas(gas));
                return stack == null ? null : stack.setStackSize(drop.getStackSize());
            }
        }
        return drop;
    }

    public static ItemStack transformPattern(final ContainerPatternEncoder container, final ItemStack output) {
        if (output.getItem() instanceof ItemFluidEncodedPattern || output.getItem() instanceof ItemFluidCraftEncodedPattern || output.getItem() instanceof ItemLargeEncodedPattern) {
            final Optional<ItemStack> maybePattern = AEApi.instance().definitions().items().encodedPattern().maybeStack(1);
            if (maybePattern.isPresent()) {
                return maybePattern.get();
            }
        }
        return output;
    }

    public static boolean startJob(final ContainerCraftConfirm ccc, final ArrayList<CraftingCPURecord> cpus, final ICraftingJob result) {
        GuiType originalGui = null;
        if (!(ccc instanceof final ContainerFCCraftConfirm container)) {
            return false;
        }
        final IActionHost ah = container.getActionHost();

        if (ah instanceof WirelessTerminalGuiObject) {
            final ItemStack tool = ((WirelessTerminalGuiObject) ah).getItemStack();
            if (tool.getItem() == FCItems.WIRELESS_FLUID_PATTERN_TERMINAL) {
                originalGui = GuiType.WIRELESS_FLUID_PATTERN_TERMINAL;
            }
        }

        if (ah instanceof PartFluidPatternTerminal) {
            originalGui = GuiType.FLUID_PATTERN_TERMINAL;
        }

        if (ah instanceof PartExtendedFluidPatternTerminal) {
            originalGui = GuiType.FLUID_EXTENDED_PATTERN_TERMINAL;
        }

        if (originalGui == null) {
            return false;
        }

        final IActionHost h = (IActionHost) container.getTarget();
        if (h != null) {
            final IGridNode node = h.getActionableNode();
            final IGrid grid = node.getGrid();
            if (result != null && !container.isSimulation()) {
                final ICraftingGrid cc = grid.getCache(ICraftingGrid.class);
                final ICraftingLink g = cc.submitJob(result, null, container.getSelectedCpu() == -1 ? null : Ae2Reflect.getCraftingCPU(cpus.get(container.getSelectedCpu())), true, container.getActionSrc());
                container.setAutoStart(false);
                if (g == null) {
                    container.setJob(cc.beginCraftingJob(container.getWorld(), grid, container.getActionSrc(), result.getOutput(), null));
                } else if (container.getOpenContext() != null) {
                    final ContainerOpenContext context = container.getOpenContext();
                    InventoryHandler.openGui(
                        container.getInventoryPlayer().player,
                        container.getInventoryPlayer().player.world,
                        new BlockPos(Ae2Reflect.getContextX(context), Ae2Reflect.getContextY(context), Ae2Reflect.getContextZ(context)),
                        container.getOpenContext().getSide().getFacing(),
                        originalGui
                    );
                }
            }
        }
        return true;
    }

}