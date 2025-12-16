package com.glodblock.github.util;

import appeng.api.AEApi;
import appeng.api.features.ILocatable;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.features.IWirelessTermRegistry;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.core.localization.PlayerMessages;
import appeng.fluids.util.AEFluidInventory;
import appeng.fluids.util.AEFluidStack;
import appeng.parts.reporting.AbstractPartEncoder;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import com.glodblock.github.common.item.fake.FakeFluids;
import com.glodblock.github.integration.mek.FakeGases;
import com.glodblock.github.inventory.GuiType;
import com.glodblock.github.inventory.InventoryHandler;
import com.mekeng.github.common.me.data.IAEGasStack;
import com.mekeng.github.common.me.storage.IGasStorageChannel;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.EncoderException;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public final class Util {

    public static final String mekModName;

    static {
        if (ModAndClassUtil.GAS) {
            mekModName = "" + TextFormatting.BLUE + TextFormatting.ITALIC + Loader.instance().getIndexedModList().get("mekanism").getName();
        } else mekModName = "";
    }

    public static IStorageChannel<IAEFluidStack> getFluidChannel() {
        return AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class);
    }

    public static IStorageChannel<IAEItemStack> getItemChannel() {
        return AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
    }

    @Optional.Method(modid = "mekeng")
    public static IStorageChannel<IAEGasStack> getGasChannel() {
        return AEApi.instance().storage().getStorageChannel(IGasStorageChannel.class);
    }

    public static IAEItemStack packAEStackToDrop(final Object s) {
        if (s instanceof final IAEFluidStack f) {
            return FakeFluids.packFluid2AEDrops(f);
        } else if (ModAndClassUtil.GAS && s instanceof final IAEGasStack g) {
            return FakeGases.packGas2AEDrops(g);
        }
        return null;
    }

    public static FluidStack getFluidFromItem(final ItemStack stack) {
        if (!stack.isEmpty() && stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
            if (stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null) != null) {
                final IFluidTankProperties[] tanks = Objects.requireNonNull(stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)).getTankProperties();
                for (final IFluidTankProperties tank : tanks) {
                    if (tank != null && tank.getContents() != null) {
                        return tank.getContents().copy();
                    }
                }
            }
        }
        return null;
    }

    @Optional.Method(modid = "mekeng")
    public static String getGasNameFromItem(final ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() instanceof final IGasItem gi) {
            final var gas = gi.getGas(stack);
            if (gas != null && gas.amount > 0) {
                return gas.getGas().getLocalizedName();
            }
        }
        return null;
    }

    @Optional.Method(modid = "mekeng")
    public static GasStack getGasFromItem(final ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() instanceof final IGasItem gi) {
            final var gas = gi.getGas(stack);
            if (gas != null && gas.amount > 0) {
                return gas;
            }
        }
        return null;
    }

    public static ItemStack getEmptiedContainer(final ItemStack stack) {
        if (!stack.isEmpty() && stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
            final ItemStack dummy = stack.copy();
            final IFluidHandlerItem fh = dummy.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            if (fh != null) {
                fh.drain(Integer.MAX_VALUE, true);
                return fh.getContainer();
            }
        }
        return stack;
    }

    public static void writeFluidInventoryToBuffer(@Nonnull final AEFluidInventory inv, final ByteBuf data) throws IOException {
        int fluidMask = 0;
        for (int i = 0; i < inv.getSlots(); ++i) {
            if (inv.getFluidInSlot(i) != null) {
                fluidMask |= 1 << i;
            }
        }
        data.writeByte(fluidMask);
        for (int i = 0; i < inv.getSlots(); ++i) {
            final IAEFluidStack fluid = inv.getFluidInSlot(i);
            if (fluid != null) {
                fluid.writeToPacket(data);
            }
        }
    }

    public static boolean readFluidInventoryToBuffer(@Nonnull final AEFluidInventory inv, final ByteBuf data) throws IOException {
        boolean changed = false;
        final int fluidMask = data.readByte();
        for (int i = 0; i < inv.getSlots(); ++i) {
            if ((fluidMask & (1 << i)) != 0) {
                final IAEFluidStack fluid = AEFluidStack.fromPacket(data);
                if (fluid != null) { // this shouldn't happen, but better safe than sorry
                    final IAEFluidStack origFluid = inv.getFluidInSlot(i);
                    if (!fluid.equals(origFluid) || fluid.getStackSize() != origFluid.getStackSize()) {
                        inv.setFluidInSlot(i, fluid);
                        changed = true;
                    }
                }
            } else if (inv.getFluidInSlot(i) != null) {
                inv.setFluidInSlot(i, null);
                changed = true;
            }
        }
        return changed;
    }

    public static void fuzzyTransferItems(final int slot, final ItemStack[] inputs, final ItemStack[] des, final IItemList<IAEItemStack> storage) {
        if (slot < des.length && inputs.length > 0) {
            if (storage != null) {
                IAEItemStack select = AEItemStack.fromItemStack(inputs[0]);
                for (final ItemStack item : inputs) {
                    final IAEItemStack result = storage.findPrecise(AEItemStack.fromItemStack(item));
                    if (result != null) {
                        select = AEItemStack.fromItemStack(item);
                        break;
                    }
                }
                if (select != null) {
                    des[slot] = select.createItemStack();
                }
            } else {
                des[slot] = inputs[0];
            }
        }
    }

    public static void clearItemInventory(final IItemHandlerModifiable inv) {
        for (int i = 0; i < inv.getSlots(); ++i) {
            inv.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    public static void putPattern(final AbstractPartEncoder part, final IAEItemStack[] inputs, final IAEItemStack[] outputs) {
        for (int x = 0; x < part.getInventoryByName("crafting").getSlots() && x < inputs.length; x++) {
            final IAEItemStack item = inputs[x];
            ((AppEngInternalInventory) part.getInventoryByName("crafting")).setStackInSlot(x, item == null ? ItemStack.EMPTY : item.createItemStack());
        }

        for (int x = 0; x < part.getInventoryByName("output").getSlots() && x < outputs.length; x++) {
            final IAEItemStack item = outputs[x];
            ((AppEngInternalInventory) part.getInventoryByName("output")).setStackInSlot(x, item == null ? ItemStack.EMPTY : item.createItemStack());
        }
    }

    public static ItemStack[] compress(final ItemStack[] list) {
        final List<ItemStack> comp = new LinkedList<>();
        for (final ItemStack item : list) {
            if (item == null) continue;
            final ItemStack currentStack = item.copy();
            if (currentStack.isEmpty() || currentStack.getCount() == 0) continue;
            boolean find = false;
            for (final ItemStack storedStack : comp) {
                if (storedStack.isEmpty()) continue;
                final boolean areItemStackEqual = storedStack.isItemEqual(currentStack) && ItemStack.areItemStackTagsEqual(storedStack, currentStack);
                if (areItemStackEqual && (storedStack.getCount() + currentStack.getCount()) <= storedStack.getMaxStackSize()) {
                    find = true;
                    storedStack.setCount(storedStack.getCount() + currentStack.getCount());
                }
            }
            if (!find) {
                comp.add(item.copy());
            }
        }
        return comp.stream().filter(Objects::nonNull).toArray(ItemStack[]::new);
    }

    public static int findMax(final Collection<Integer> list) {
        int a = Integer.MIN_VALUE;
        for (final int x : list) {
            a = Math.max(x, a);
        }
        return a;
    }

    public static void writeNBTToBytes(final ByteBuf buf, final NBTTagCompound nbt) {
        final PacketBuffer pb = new PacketBuffer(buf);
        try {
            pb.writeCompoundTag(nbt);
        } catch (final EncoderException ignore) {
        }
    }

    public static NBTTagCompound readNBTFromBytes(final ByteBuf from) {
        final PacketBuffer pb = new PacketBuffer(from);
        try {
            return pb.readCompoundTag();
        } catch (final IOException e) {
            return new NBTTagCompound();
        }
    }

    public static boolean multiplySlotCheck(final Slot[] slots, final int multiple) {
        for (final Slot slot : slots) {
            if (!slot.getStack().isEmpty()) {
                final long amt = slot.getStack().getCount();
                if (amt * multiple > Integer.MAX_VALUE) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void multiplySlot(final Slot[] slots, final int multiple) {
        for (final Slot slot : slots) {
            if (!slot.getStack().isEmpty()) {
                final ItemStack stack = slot.getStack();
                stack.setCount(stack.getCount() * multiple);
            }
        }
    }

    public static boolean divideSlotCheck(final Slot[] slots, final int divide) {
        for (final Slot slot : slots) {
            if (!slot.getStack().isEmpty()) {
                final long amt = slot.getStack().getCount();
                if (amt % divide != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void divideSlot(final Slot[] slots, final int divide) {
        for (final Slot slot : slots) {
            if (!slot.getStack().isEmpty()) {
                final ItemStack stack = slot.getStack();
                stack.setCount(stack.getCount() / divide);
            }
        }
    }

    public static boolean increaseSlotCheck(final Slot[] slots, final int increase) {
        for (final Slot slot : slots) {
            if (!slot.getStack().isEmpty()) {
                final long amt = slot.getStack().getCount();
                if (amt + increase > Integer.MAX_VALUE) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void increaseSlot(final Slot[] slots, final int increase) {
        for (final Slot slot : slots) {
            if (!slot.getStack().isEmpty()) {
                final ItemStack stack = slot.getStack();
                stack.setCount(stack.getCount() + increase);
            }
        }
    }

    public static boolean decreaseSlotCheck(final Slot[] slots, final int decrease) {
        for (final Slot slot : slots) {
            if (!slot.getStack().isEmpty()) {
                final long amt = slot.getStack().getCount();
                if (amt - decrease < 1) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void decreaseSlot(final Slot[] slots, final int decrease) {
        for (final Slot slot : slots) {
            if (!slot.getStack().isEmpty()) {
                final ItemStack stack = slot.getStack();
                stack.setCount(stack.getCount() - decrease);
            }
        }
    }

    public static void openWirelessTerminal(final ItemStack item, final int playerInvSlot, final boolean isBaubleSlot, final World w, final EntityPlayer player, final GuiType gui) {
        final IWirelessTermRegistry registry = AEApi.instance().registries().wireless();
        if (Platform.isClient()) {
            return;
        }
        if (!registry.isWirelessTerminal(item)) {
            player.sendMessage(PlayerMessages.DeviceNotWirelessTerminal.get());
            return;
        }
        final IWirelessTermHandler handler = registry.getWirelessTerminalHandler(item);
        final String unparsedKey = handler.getEncryptionKey(item);
        if (unparsedKey.isEmpty()) {
            player.sendMessage(PlayerMessages.DeviceNotLinked.get());
            return;
        }
        final long parsedKey = Long.parseLong(unparsedKey);
        final ILocatable securityStation = AEApi.instance().registries().locatable().getLocatableBy(parsedKey);
        if (securityStation == null) {
            player.sendMessage(PlayerMessages.StationCanNotBeLocated.get());
            return;
        }
        if (handler.hasPower(player, 0.5, item)) {
            InventoryHandler.openGui(player, w, new BlockPos(playerInvSlot, isBaubleSlot ? 1 : 0, Integer.MIN_VALUE), EnumFacing.DOWN, gui);
        } else {
            player.sendMessage(PlayerMessages.DeviceNotPowered.get());
        }
    }

    public static void onPatternTerminalChangeCrafting(final AbstractPartEncoder part, final boolean noCraftingMode, final Int2ObjectMap<ItemStack[]> inputs, final List<ItemStack> outputs, final boolean combine) {
        final IItemHandler crafting = part.getInventoryByName("crafting");
        final IItemHandler output = part.getInventoryByName("output");
        final IItemList<IAEItemStack> storageList = part.getInventory(getItemChannel()) == null ?
            null : part.getInventory(getItemChannel()).getStorageList();
        if (crafting instanceof AppEngInternalInventory && output instanceof AppEngInternalInventory) {
            clearItemInventory((IItemHandlerModifiable) crafting);
            clearItemInventory((IItemHandlerModifiable) output);
            ItemStack[] fuzzyFind = new ItemStack[findMax(inputs.keySet()) + 1];
            for (final int index : inputs.keySet()) {
                fuzzyTransferItems(index, inputs.get(index), fuzzyFind, storageList);
            }
            if (combine && noCraftingMode) {
                fuzzyFind = compress(fuzzyFind);
            }
            int bound = Math.min(crafting.getSlots(), fuzzyFind.length);
            for (int x = 0; x < bound; ++x) {
                final ItemStack item = fuzzyFind[x];
                ((AppEngInternalInventory) crafting).setStackInSlot(x, item == null ? ItemStack.EMPTY : item);
            }
            bound = Math.min(output.getSlots(), outputs.size());
            for (int x = 0; x < bound; ++x) {
                final ItemStack item = outputs.get(x);
                ((AppEngInternalInventory) output).setStackInSlot(x, item == null ? ItemStack.EMPTY : item);
            }
        }
    }

}