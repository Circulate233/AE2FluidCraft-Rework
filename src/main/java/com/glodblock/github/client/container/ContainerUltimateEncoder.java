package com.glodblock.github.client.container;

import appeng.api.AEApi;
import appeng.api.definitions.IDefinitions;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.AEBaseContainer;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.IOptionalSlotHost;
import appeng.container.slot.OptionalSlotFake;
import appeng.container.slot.SlotFakeCraftingMatrix;
import appeng.container.slot.SlotPatternOutputs;
import appeng.container.slot.SlotPlayerHotBar;
import appeng.container.slot.SlotPlayerInv;
import appeng.container.slot.SlotRestrictedInput;
import appeng.helpers.InventoryAction;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import com.glodblock.github.common.item.fake.FakeFluids;
import com.glodblock.github.common.item.fake.FakeItemRegister;
import com.glodblock.github.common.tile.TileUltimateEncoder;
import com.glodblock.github.integration.mek.FakeGases;
import com.glodblock.github.interfaces.FCFluidPatternContainer;
import com.glodblock.github.loader.FCItems;
import com.glodblock.github.util.FluidPatternDetails;
import com.glodblock.github.util.ModAndClassUtil;
import com.glodblock.github.util.Util;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.List;

public class ContainerUltimateEncoder extends AEBaseContainer implements IOptionalSlotHost, FCFluidPatternContainer {

    private final TileUltimateEncoder encoder;
    protected final SlotRestrictedInput patternSlotIN;
    protected final SlotRestrictedInput patternSlotOUT;
    protected final SlotFakeCraftingMatrix[] craftingSlots = new SlotFakeCraftingMatrix[42];
    protected final SlotPatternOutputs[] outputSlots = new SlotPatternOutputs[8];
    @GuiSync(105)
    public boolean combine = false;
    @GuiSync(106)
    public boolean fluidFirst = false;

    public ContainerUltimateEncoder(final InventoryPlayer ipl, final TileUltimateEncoder encoder) {
        super(ipl, encoder);
        this.encoder = encoder;
        this.patternSlotIN = new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.BLANK_PATTERN, this.encoder.getPattern(), 0, 137, 91, this.getInventoryPlayer());
        this.patternSlotOUT = new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.ENCODED_PATTERN, this.encoder.getPattern(), 1, 137, 134, this.getInventoryPlayer());
        for(int y = 0; y < 7; ++y) {
            for(int x = 0; x < 6; ++x) {
                this.addSlotToContainer(this.craftingSlots[x + y * 6] = new SlotFakeCraftingMatrix(this.encoder.getCraft(), x + y * 6, 8 + x * 18, 29 + y * 18));
            }
        }
        for(int y = 0; y < 4; ++y) {
            for(int x = 0; x < 2; ++x) {
                this.addSlotToContainer(this.outputSlots[x + y * 2] = new SlotPatternOutputs(this.encoder.getOutput(), this, x + y * 2, 134 + x * 18, 12 + y * 18, 0, 0, 1));
                this.outputSlots[x + y * 2].setRenderDisabled(false);
                this.outputSlots[x + y * 2].setIIcon(-1);
            }
        }
        this.addSlotToContainer(this.patternSlotIN);
        this.addSlotToContainer(this.patternSlotOUT);
        this.bindPlayerInventory(ipl, 0, 167);
    }

    @Override
    public boolean getCombineMode() {
        return false;
    }

    public void setCombineMode(final boolean val) {
        this.combine = val;
        this.encoder.combine = val;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (Platform.isServer()) {
            this.combine = this.encoder.combine;
            this.fluidFirst = this.encoder.fluidFirst;
        }
    }

    public TileUltimateEncoder getEncoder() {
        return this.encoder;
    }

    @Override
    public ItemStack transferStackInSlot(final EntityPlayer p, final int idx) {
        if (Platform.isClient()) {
            return ItemStack.EMPTY;
        } else {
            if (this.inventorySlots.get(idx) instanceof SlotPlayerInv || this.inventorySlots.get(idx) instanceof SlotPlayerHotBar) {
                final AppEngSlot clickSlot = (AppEngSlot)this.inventorySlots.get(idx);
                final ItemStack itemStack = clickSlot.getStack();
                if (AEApi.instance().definitions().materials().blankPattern().isSameAs(itemStack)) {
                    final IItemHandler patternInv = this.encoder.getPattern();
                    final ItemStack remainder = patternInv.insertItem(0, itemStack, false);
                    clickSlot.putStack(remainder);
                }
            }
            return super.transferStackInSlot(p, idx);
        }
    }

    @Override
    public void onSlotChange(final Slot s) {
        if (s == this.patternSlotOUT && Platform.isServer()) {
            for (final IContainerListener listener : this.listeners) {
                for (final Slot slot : this.inventorySlots) {
                    if (slot instanceof OptionalSlotFake || slot instanceof SlotFakeCraftingMatrix) {
                        listener.sendSlotContents(this, slot.slotNumber, slot.getStack());
                    }
                }
                if (listener instanceof EntityPlayerMP) {
                    ((EntityPlayerMP) listener).isChangingQuantityOnly = false;
                }
            }
            this.detectAndSendChanges();
        }
    }

    private static IAEItemStack[] collectInventory(final Slot[] slots) {
        // see note at top of DensePatternDetails
        final List<IAEItemStack> acc = new ObjectArrayList<>();
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

    public void encodeAndMoveToInventory() {
        encode();
        final ItemStack output = this.patternSlotOUT.getStack();
        if (!output.isEmpty()) {
            if (!getPlayerInv().addItemStackToInventory(output)) {
                getPlayerInv().player.dropItem(output, false);
            }
            this.patternSlotOUT.putStack(ItemStack.EMPTY);
        }
    }

    public void encode() {
        if (collectInventory(this.craftingSlots).length == 0 || collectInventory(this.outputSlots).length == 0) {
            return;
        }
        if (!checkHasFluidPattern()) {
            ItemStack stack = this.patternSlotOUT.getStack();
            if (stack.isEmpty()) {
                stack = this.patternSlotIN.getStack();
                if (stack.isEmpty() || notPattern(stack)) {
                    return;
                }
                if (stack.getCount() == 1) {
                    this.patternSlotIN.putStack(ItemStack.EMPTY);
                } else {
                    stack.shrink(1);
                }
                encodeItem();
            } else if (!notPattern(stack)) {
                encodeItem();
            }
            return;
        }
        ItemStack stack = this.patternSlotOUT.getStack();
        if (stack.isEmpty()) {
            stack = this.patternSlotIN.getStack();
            if (stack.isEmpty() || notPattern(stack)) {
                return;
            }
            if (stack.getCount() == 1) {
                this.patternSlotIN.putStack(ItemStack.EMPTY);
            } else {
                stack.shrink(1);
            }
            encodeFluidPattern();
        } else if (!notPattern(stack)) {
            encodeFluidPattern();
        }
    }

    public void encodeItem() {
        final ItemStack patternStack = new ItemStack(FCItems.LARGE_ITEM_ENCODED_PATTERN);
        final FluidPatternDetails pattern = new FluidPatternDetails(patternStack);
        pattern.setInputs(collectInventory(craftingSlots));
        pattern.setOutputs(collectInventory(outputSlots));
        pattern.setEncoder(this.getInventoryPlayer().player.getGameProfile());
        patternSlotOUT.putStack(pattern.writeToStack());
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

    private boolean notPattern(final ItemStack output) {
        if (output.isEmpty()) {
            return true;
        }
        if (output.getItem() == FCItems.DENSE_ENCODED_PATTERN ||
            output.getItem() == FCItems.LARGE_ITEM_ENCODED_PATTERN) {
            return false;
        }
        final IDefinitions defs = AEApi.instance().definitions();
        return !defs.items().encodedPattern().isSameAs(output) && !defs.materials().blankPattern().isSameAs(output);
    }

    public void multiply(final int multiple) {
        if (Util.multiplySlotCheck(this.craftingSlots, multiple) && Util.multiplySlotCheck(this.outputSlots, multiple)) {
            Util.multiplySlot(this.craftingSlots, multiple);
            Util.multiplySlot(this.outputSlots, multiple);
        }
    }

    public void clear() {
        for (final Slot slot : this.craftingSlots) {
            slot.putStack(ItemStack.EMPTY);
        }
        for (final Slot slot : this.outputSlots) {
            slot.putStack(ItemStack.EMPTY);
        }
    }

    public void divide(final int divide) {
        if (Util.divideSlotCheck(this.craftingSlots, divide) && Util.divideSlotCheck(this.outputSlots, divide)) {
            Util.divideSlot(this.craftingSlots, divide);
            Util.divideSlot(this.outputSlots, divide);
        }
    }

    public void increase(final int increase) {
        if (Util.increaseSlotCheck(this.craftingSlots, increase) && Util.increaseSlotCheck(this.outputSlots, increase)) {
            Util.increaseSlot(this.craftingSlots, increase);
            Util.increaseSlot(this.outputSlots, increase);
        }
    }

    public void decrease(final int decrease) {
        if (Util.decreaseSlotCheck(this.craftingSlots, decrease) && Util.decreaseSlotCheck(this.outputSlots, decrease)) {
            Util.decreaseSlot(this.craftingSlots, decrease);
            Util.decreaseSlot(this.outputSlots, decrease);
        }
    }

    @Override
    public boolean isSlotEnabled(final int i) {
        return true;
    }

    @Override
    public void acceptPattern(final Int2ObjectMap<ItemStack[]> inputs, final List<ItemStack> outputs, final boolean combine) {
        this.encoder.onChangeCrafting(inputs, outputs, combine);
    }

    @Override
    public boolean getFluidPlaceMode() {
        return false;
    }

    public void setFluidPlaceMode(final boolean val) {
        this.fluidFirst = val;
        this.encoder.fluidFirst = val;
    }
}
