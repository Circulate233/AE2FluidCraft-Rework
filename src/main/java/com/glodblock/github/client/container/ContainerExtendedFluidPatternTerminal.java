package com.glodblock.github.client.container;

import appeng.api.AEApi;
import appeng.api.definitions.IDefinitions;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.guisync.GuiSync;
import appeng.container.implementations.ContainerExpandedProcessingPatternTerm;
import appeng.container.slot.SlotFakeCraftingMatrix;
import appeng.container.slot.SlotPatternOutputs;
import appeng.helpers.InventoryAction;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import com.glodblock.github.common.item.ItemFluidEncodedPattern;
import com.glodblock.github.common.item.ItemLargeEncodedPattern;
import com.glodblock.github.common.item.fake.FakeFluids;
import com.glodblock.github.common.item.fake.FakeItemRegister;
import com.glodblock.github.integration.mek.FakeGases;
import com.glodblock.github.interfaces.FCFluidPatternContainer;
import com.glodblock.github.interfaces.FCFluidPatternPart;
import com.glodblock.github.loader.FCItems;
import com.glodblock.github.util.FluidPatternDetails;
import com.glodblock.github.util.ModAndClassUtil;
import com.glodblock.github.util.Util;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;

public class ContainerExtendedFluidPatternTerminal extends ContainerExpandedProcessingPatternTerm implements FCFluidPatternContainer {
    public final FCFluidPatternPart part;

    @GuiSync(105)
    public boolean combine = false;
    @GuiSync(106)
    public boolean fluidFirst = false;

    public ContainerExtendedFluidPatternTerminal(final InventoryPlayer ip, final ITerminalHost monitorable) {
        super(ip, monitorable);
        part = (FCFluidPatternPart) monitorable;
        this.craftingMode = false;
    }

    @Override
    public void encode() {
        ItemStack stack = this.patternSlotOUT.getStack();
        if (!checkHasFluidPattern()) {
            if (!stack.isEmpty()) {
                if (this.patternSlotIN.getStack().isEmpty()) {
                    this.patternSlotIN.putStack(AEApi.instance().definitions().materials().blankPattern().maybeStack(1).orElse(ItemStack.EMPTY));
                    this.patternSlotIN.putStack(ItemStack.EMPTY);
                } else {
                    this.patternSlotIN.getStack().grow(1);
                    this.patternSlotIN.putStack(ItemStack.EMPTY);
                }
            }
            super.encode();
            return;
        }
        if (stack.isEmpty()) {
            stack = this.patternSlotIN.getStack();
            if (stack.isEmpty() || !isPattern(stack)) {
                return;
            }
            if (stack.getCount() == 1) {
                this.patternSlotIN.putStack(ItemStack.EMPTY);
            } else {
                stack.shrink(1);
            }
            encodeFluidPattern();
        } else if (isPattern(stack)) {
            encodeFluidPattern();
        }
    }

    private static boolean isPattern(final ItemStack output) {
        if (output.isEmpty()) {
            return false;
        }
        if (output.getItem() instanceof ItemFluidEncodedPattern) {
            return true;
        }
        if (output.getItem() instanceof ItemLargeEncodedPattern) {
            return true;
        }
        final IDefinitions defs = AEApi.instance().definitions();
        return defs.items().encodedPattern().isSameAs(output) || defs.materials().blankPattern().isSameAs(output);
    }

    private static IAEItemStack[] collectInventory(final Slot[] slots) {
        // see note at top of DensePatternDetails
        final List<IAEItemStack> acc = new ArrayList<>();
        for (final Slot slot : slots) {
            final ItemStack stack = slot.getStack();
            if (stack.isEmpty()) {
                continue;
            }
            final IAEItemStack aeStack = AEItemStack.fromItemStack(stack);
            if (aeStack == null) {
                continue;
            }
            acc.add(aeStack);
        }
        return acc.toArray(new IAEItemStack[0]);
    }

    private void encodeFluidPattern() {
        final ItemStack patternStack = new ItemStack(FCItems.DENSE_ENCODED_PATTERN);
        final FluidPatternDetails pattern = new FluidPatternDetails(patternStack);
        pattern.setInputs(collectInventory(craftingSlots));
        pattern.setOutputs(collectInventory(outputSlots));
        pattern.setEncoder(this.getInventoryPlayer().player.getGameProfile());
        patternSlotOUT.putStack(pattern.writeToStack());
    }

    private boolean checkHasFluidPattern() {
        boolean hasFluid = false, search = false;
        for (final Slot craftingSlot : this.craftingSlots) {
            final ItemStack crafting = craftingSlot.getStack();
            if (crafting.isEmpty()) {
                continue;
            }
            search = true;
            if (FakeFluids.isFluidFakeItem(crafting)) {
                hasFluid = true;
                break;
            }
            if (ModAndClassUtil.GAS && FakeGases.isGasFakeItem(crafting)) {
                hasFluid = true;
                break;
            }
        }
        if (!search) { // search=false -> inputs were empty
            return false;
        }
        // `search` should be true at this point
        for (final Slot outputSlot : this.outputSlots) {
            final ItemStack out = outputSlot.getStack();
            if (out.isEmpty()) {
                continue;
            }
            search = false;
            if (hasFluid) {
                break;
            } else if (FakeFluids.isFluidFakeItem(out)) {
                hasFluid = true;
                break;
            } else if (ModAndClassUtil.GAS && FakeGases.isGasFakeItem(out)) {
                hasFluid = true;
                break;
            }
        }
        return hasFluid && !search; // search=true -> outputs were empty
    }

    @Override
    public void doAction(final EntityPlayerMP player, final InventoryAction action, final int slotId, final long id) {
        if (id != 0 || slotId < 0 || slotId >= this.inventorySlots.size()) {
            super.doAction(player, action, slotId, id);
            return;
        }
        final Slot slot = getSlot(slotId);
        final ItemStack stack = player.inventory.getItemStack();
        if ((slot instanceof SlotFakeCraftingMatrix || slot instanceof SlotPatternOutputs) && !stack.isEmpty()
                && stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null) && Util.getFluidFromItem(stack) != null) {
            FluidStack fluid = null;
            switch (action) {
                case PICKUP_OR_SET_DOWN:
                    fluid = Util.getFluidFromItem(stack);
                    slot.putStack(FakeFluids.packFluid2Drops(fluid));
                    break;
                case SPLIT_OR_PLACE_SINGLE:
                    fluid = Util.getFluidFromItem(ItemHandlerHelper.copyStackWithSize(stack, 1));
                    final FluidStack origin = FakeItemRegister.getStack(slot.getStack());
                    if (fluid != null && fluid.equals(origin)) {
                        fluid.amount += origin.amount;
                        if (fluid.amount <= 0) fluid = null;
                    }
                    slot.putStack(FakeFluids.packFluid2Drops(fluid));
                    break;
            }
            if (fluid == null) {
                super.doAction(player, action, slotId, id);
                return;
            }
            return;
        }
        if (ModAndClassUtil.GAS && (slot instanceof SlotFakeCraftingMatrix || slot instanceof SlotPatternOutputs) && !stack.isEmpty()
            && stack.getItem() instanceof IGasItem && Util.getGasFromItem(stack) != null) {
            GasStack gas = null;
            switch (action) {
                case PICKUP_OR_SET_DOWN:
                    gas = Util.getGasFromItem(stack);
                    slot.putStack(FakeGases.packGas2Drops(gas));
                    break;
                case SPLIT_OR_PLACE_SINGLE:
                    gas = Util.getGasFromItem(ItemHandlerHelper.copyStackWithSize(stack, 1));
                    final GasStack origin = FakeItemRegister.getStack(slot.getStack());
                    if (gas != null && gas.equals(origin)) {
                        gas.amount += origin.amount;
                        if (gas.amount <= 0) gas = null;
                    }
                    slot.putStack(FakeGases.packGas2Drops(gas));
                    break;
            }
            if (gas == null) {
                super.doAction(player, action, slotId, id);
                return;
            }
            return;
        }
        super.doAction(player, action, slotId, id);
    }

    @Override
    public void multiply(final int multiple) {
        if (Util.multiplySlotCheck(this.craftingSlots, multiple) && Util.multiplySlotCheck(this.outputSlots, multiple)) {
            Util.multiplySlot(this.craftingSlots, multiple);
            Util.multiplySlot(this.outputSlots, multiple);
        }
    }

    @Override
    public void divide(final int divide) {
        if (Util.divideSlotCheck(this.craftingSlots, divide) && Util.divideSlotCheck(this.outputSlots, divide)) {
            Util.divideSlot(this.craftingSlots, divide);
            Util.divideSlot(this.outputSlots, divide);
        }
    }

    @Override
    public void increase(final int increase) {
        if (Util.increaseSlotCheck(this.craftingSlots, increase) && Util.increaseSlotCheck(this.outputSlots, increase)) {
            Util.increaseSlot(this.craftingSlots, increase);
            Util.increaseSlot(this.outputSlots, increase);
        }
    }

    @Override
    public void decrease(final int decrease) {
        if (Util.decreaseSlotCheck(this.craftingSlots, decrease) && Util.decreaseSlotCheck(this.outputSlots, decrease)) {
            Util.decreaseSlot(this.craftingSlots, decrease);
            Util.decreaseSlot(this.outputSlots, decrease);
        }
    }

    @Override
    public void acceptPattern(final Int2ObjectMap<ItemStack[]> inputs, final List<ItemStack> outputs, final boolean combine) {
        this.part.onChangeCrafting(inputs, outputs, combine);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (Platform.isServer()) {
            this.combine = this.part.getCombineMode();
            this.fluidFirst = this.part.getFluidPlaceMode();
        }
    }

    @Override
    public boolean getCombineMode() {
        return part.getCombineMode();
    }

    @Override
    public void setCombineMode(final boolean mode) {
        part.setCombineMode(mode);
    }

    @Override
    public boolean getFluidPlaceMode() {
        return part.getFluidPlaceMode();
    }

    @Override
    public void setFluidPlaceMode(final boolean mode) {
        part.setFluidPlaceMode(mode);
    }
}
