package com.glodblock.github.client.container;

import appeng.api.AEApi;
import appeng.api.definitions.IDefinitions;
import appeng.api.implementations.IUpgradeableCellContainer;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.guisync.GuiSync;
import appeng.container.implementations.ContainerWirelessPatternTerminal;
import appeng.container.interfaces.IInventorySlotAware;
import appeng.container.slot.SlotFakeCraftingMatrix;
import appeng.container.slot.SlotPatternOutputs;
import appeng.helpers.InventoryAction;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.items.misc.ItemEncodedPattern;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.Platform;
import appeng.util.inv.InvOperation;
import appeng.util.item.AEItemStack;
import com.glodblock.github.common.item.ItemFluidCraftEncodedPattern;
import com.glodblock.github.common.item.ItemFluidEncodedPattern;
import com.glodblock.github.common.item.ItemLargeEncodedPattern;
import com.glodblock.github.common.item.fake.FakeFluids;
import com.glodblock.github.common.item.fake.FakeItemRegister;
import com.glodblock.github.integration.mek.FakeGases;
import com.glodblock.github.interfaces.FCFluidPatternContainer;
import com.glodblock.github.loader.FCItems;
import com.glodblock.github.util.FluidCraftingPatternDetails;
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
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ContainerWirelessFluidPatternTerminal extends ContainerWirelessPatternTerminal implements IUpgradeableCellContainer, IInventorySlotAware, FCFluidPatternContainer {

    private final WirelessTerminalGuiObject wirelessTerminalGUIObject;
    @GuiSync(105)
    public boolean combine = false;
    @GuiSync(106)
    public boolean fluidFirst = false;

    public ContainerWirelessFluidPatternTerminal(final InventoryPlayer ip, final WirelessTerminalGuiObject gui) {
        super(ip, gui);
        this.wirelessTerminalGUIObject = gui;
        this.loadFromNBT();
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
        if (output.getItem() instanceof ItemFluidEncodedPattern
            || output.getItem() instanceof ItemFluidCraftEncodedPattern
            || output.getItem() instanceof ItemLargeEncodedPattern) {
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
        patternSlotOUT.putStack(pattern.writeToStack());
    }

    private boolean checkHasFluidPattern() {
        if (this.craftingMode) {
            return false;
        }
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

    public void encodeFluidCraftPattern() {
        ItemStack output = this.patternSlotOUT.getStack();

        final ItemStack[] in = this.getInputs();
        final ItemStack[] out = this.getOutputs();
        if (in == null || out == null) {
            return;
        }

        if (!output.isEmpty() && !isPattern(output)) {
            return;
        } else if (output.isEmpty()) {
            output = this.patternSlotIN.getStack();
            if (output.isEmpty() || !isPattern(output)) {
                return;
            }
            output.setCount(output.getCount() - 1);
            if (output.getCount() == 0) {
                this.patternSlotIN.putStack(ItemStack.EMPTY);
            }
            final Optional<ItemStack> maybePattern = AEApi.instance().definitions().items().encodedPattern().maybeStack(1);
            if (maybePattern.isPresent()) {
                output = maybePattern.get();
                this.patternSlotOUT.putStack(output);
            }
        }
        final NBTTagCompound encodedValue = new NBTTagCompound();

        final NBTTagList tagIn = new NBTTagList();
        final NBTTagList tagOut = new NBTTagList();

        for (final ItemStack i : in) {
            tagIn.appendTag(this.createItemTag(i));
        }

        for (final ItemStack i : out) {
            tagOut.appendTag(this.createItemTag(i));
        }

        encodedValue.setTag("in", tagIn);
        encodedValue.setTag("out", tagOut);
        encodedValue.setBoolean("crafting", this.isCraftingMode());
        encodedValue.setBoolean("substitute", this.substitute);
        final ItemStack patternStack = new ItemStack(FCItems.DENSE_CRAFT_ENCODED_PATTERN);
        patternStack.setTagCompound(encodedValue);
        final FluidCraftingPatternDetails details = FluidCraftingPatternDetails.GetFluidPattern(patternStack, getNetworkNode().getWorld());
        if (details == null || !details.isNecessary()) {
            encode();
            return;
        }
        patternSlotOUT.putStack(patternStack);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (Platform.isServer()) {
            final NBTTagCompound tag = this.iGuiItemObject.getItemStack().getTagCompound();
            if (tag != null) {
                this.combine = tag.getBoolean("combine");
                this.fluidFirst = tag.getBoolean("fluidFirst");
            } else {
                this.combine = false;
                this.fluidFirst = false;
            }
        }
    }

    @Override
    public void saveChanges() {
        super.saveChanges();
        if (Platform.isServer()) {
            final NBTTagCompound tag = new NBTTagCompound();
            tag.setBoolean("combine", this.combine);
            tag.setBoolean("fluidFirst", this.fluidFirst);
            this.wirelessTerminalGUIObject.saveChanges(tag);
        }
    }

    private void loadFromNBT() {
        final NBTTagCompound data = this.wirelessTerminalGUIObject.getItemStack().getTagCompound();
        if (data != null) {
            this.combine = data.getBoolean("combine");
            this.fluidFirst = data.getBoolean("fluidFirst");
        }
    }

    @Override
    public void doAction(final EntityPlayerMP player, final InventoryAction action, final int slotId, final long id) {
        if (this.isCraftingMode()) {
            super.doAction(player, action, slotId, id);
            return;
        }
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
    public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc, final ItemStack removedStack, final ItemStack newStack) {
        if (slot == 1) {
            final ItemStack is = inv.getStackInSlot(1);
            if (!is.isEmpty() && (is.getItem() instanceof ItemFluidEncodedPattern || is.getItem() instanceof ItemFluidCraftEncodedPattern || is.getItem() instanceof ItemLargeEncodedPattern)) {
                final ItemEncodedPattern pattern = (ItemEncodedPattern) is.getItem();
                final ICraftingPatternDetails details = pattern.getPatternForItem(is, this.getPlayerInv().player.world);
                if (details != null) {
                    this.setCraftingMode(details.isCraftable());
                    this.setSubstitute(details.canSubstitute());

                    Util.clearItemInventory((AppEngInternalInventory) this.crafting);
                    Util.clearItemInventory(this.output);

                    if (details instanceof FluidCraftingPatternDetails) {
                        putPattern(((FluidCraftingPatternDetails) details).getOriginInputs(), details.getOutputs());
                        this.setCraftingMode(true);
                    } else {
                        putPattern(details.getInputs(), details.getOutputs());
                    }
                }
                this.saveChanges();
                return;
            }
        }
        super.onChangeInventory(inv, slot, mc, removedStack, newStack);
    }

    public void putPattern(final IAEItemStack[] inputs, final IAEItemStack[] outputs) {
        for (int x = 0; x < this.getInventoryByName("crafting").getSlots() && x < inputs.length; x++) {
            final IAEItemStack item = inputs[x];
            ((AppEngInternalInventory) this.getInventoryByName("crafting")).setStackInSlot(x, item == null ? ItemStack.EMPTY : item.createItemStack());
        }

        for (int x = 0; x < this.getInventoryByName("output").getSlots() && x < outputs.length; x++) {
            final IAEItemStack item = outputs[x];
            ((AppEngInternalInventory) this.getInventoryByName("output")).setStackInSlot(x, item == null ? ItemStack.EMPTY : item.createItemStack());
        }
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
    public IItemHandler getInventoryByName(final String name) {
        if (name.equals("crafting")) {
            return this.crafting;
        } else {
            return name.equals("output") ? this.output : super.getInventoryByName(name);
        }
    }

    @Override
    public boolean getCombineMode() {
        return combine;
    }

    @Override
    public boolean getFluidPlaceMode() {
        return fluidFirst;
    }

    public void setCombineMode(final boolean value) {
        NBTTagCompound data = this.wirelessTerminalGUIObject.getItemStack().getTagCompound();
        if (data != null) {
            data.setBoolean("combine", value);
        } else {
            data = new NBTTagCompound();
            data.setBoolean("combine", value);
            this.wirelessTerminalGUIObject.getItemStack().setTagCompound(data);
        }
        this.combine = value;
    }

    public void setFluidPlaceMode(final boolean value) {
        NBTTagCompound data = this.wirelessTerminalGUIObject.getItemStack().getTagCompound();
        if (data != null) {
            data.setBoolean("fluidFirst", value);
        } else {
            data = new NBTTagCompound();
            data.setBoolean("fluidFirst", value);
            this.wirelessTerminalGUIObject.getItemStack().setTagCompound(data);
        }
        this.fluidFirst = value;
    }

    @Override
    public void acceptPattern(final Int2ObjectMap<ItemStack[]> inputs, final List<ItemStack> outputs, final boolean combine) {
        final IItemList<IAEItemStack> storageList = this.wirelessTerminalGUIObject.getInventory(Util.getItemChannel()) == null ?
            null : this.wirelessTerminalGUIObject.getInventory(Util.getItemChannel()).getStorageList();
        if (this.crafting instanceof AppEngInternalInventory && this.output != null) {
            Util.clearItemInventory((IItemHandlerModifiable) this.crafting);
            Util.clearItemInventory(this.output);
            ItemStack[] fuzzyFind = new ItemStack[Util.findMax(inputs.keySet()) + 1];
            for (final int index : inputs.keySet()) {
                Util.fuzzyTransferItems(index, inputs.get(index), fuzzyFind, storageList);
            }
            if (combine && !this.craftingMode) {
                fuzzyFind = Util.compress(fuzzyFind);
            }
            int bound = Math.min(this.crafting.getSlots(), fuzzyFind.length);
            for (int x = 0; x < bound; x++) {
                final ItemStack item = fuzzyFind[x];
                ((AppEngInternalInventory) this.crafting).setStackInSlot(x, item == null ? ItemStack.EMPTY : item);
            }
            bound = Math.min(this.output.getSlots(), outputs.size());
            for (int x = 0; x < bound; x++) {
                final ItemStack item = outputs.get(x);
                this.output.setStackInSlot(x, item == null ? ItemStack.EMPTY : item);
            }
        }
    }

    NBTBase createItemTag(final ItemStack i) {
        final NBTTagCompound c = new NBTTagCompound();
        if (!i.isEmpty()) {
            i.writeToNBT(c);
            if (i.getCount() > i.getMaxStackSize()) {
                c.setInteger("stackSize", i.getCount());
            }
        }
        return c;
    }

}