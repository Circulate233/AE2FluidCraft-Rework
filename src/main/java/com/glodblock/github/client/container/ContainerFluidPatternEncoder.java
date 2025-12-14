package com.glodblock.github.client.container;

import appeng.api.AEApi;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotFake;
import appeng.container.slot.SlotRestrictedInput;
import appeng.helpers.InventoryAction;
import appeng.util.item.AEItemStack;
import com.glodblock.github.common.item.ItemFluidEncodedPattern;
import com.glodblock.github.common.item.fake.FakeFluids;
import com.glodblock.github.common.tile.TileFluidPatternEncoder;
import com.glodblock.github.handler.AeItemStackHandler;
import com.glodblock.github.integration.mek.FakeGases;
import com.glodblock.github.interfaces.AeStackInventory;
import com.glodblock.github.interfaces.PatternConsumer;
import com.glodblock.github.interfaces.SlotFluid;
import com.glodblock.github.loader.FCItems;
import com.glodblock.github.util.FluidPatternDetails;
import com.glodblock.github.util.ModAndClassUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import mekanism.api.gas.GasTankInfo;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ContainerFluidPatternEncoder extends AEBaseContainer implements PatternConsumer {

    private final TileFluidPatternEncoder tile;

    public ContainerFluidPatternEncoder(final InventoryPlayer ipl, final TileFluidPatternEncoder tile) {
        super(ipl, tile);
        this.tile = tile;
        final AeItemStackHandler crafting = new AeItemStackHandler(tile.getCraftingSlots());
        final AeItemStackHandler output = new AeItemStackHandler(tile.getOutputSlots());
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                addSlotToContainer(new SlotFluidConvertingFake(crafting, y * 3 + x, 23 + x * 18, 17 + y * 18));
            }
            addSlotToContainer(new SlotFluidConvertingFake(output, y, 113, 17 + y * 18));
        }
        addSlotToContainer(new SlotRestrictedInput(
            SlotRestrictedInput.PlacableItemType.BLANK_PATTERN, tile.getInventory(), 0, 138, 20, ipl));
        addSlotToContainer(new SlotRestrictedInput(
            SlotRestrictedInput.PlacableItemType.ENCODED_PATTERN, tile.getInventory(), 1, 138, 50, ipl));
        bindPlayerInventory(ipl, 0, 84);
    }

    public TileFluidPatternEncoder getTile() {
        return tile;
    }

    public boolean canEncodePattern() {
        if (isNotPattern(tile.getInventory().getStackInSlot(0)) && isNotPattern(tile.getInventory().getStackInSlot(1))) {
            return false;
        }
        find_input:
        {
            for (final IAEItemStack stack : tile.getCraftingSlots()) {
                if (stack != null && stack.getStackSize() > 0) {
                    break find_input;
                }
            }
            return false;
        }
        for (final IAEItemStack stack : tile.getOutputSlots()) {
            if (stack != null && stack.getStackSize() > 0) {
                return true;
            }
        }
        return false;
    }

    private static boolean isNotPattern(final ItemStack stack) {
        return stack.isEmpty() || !(AEApi.instance().definitions().materials().blankPattern().isSameAs(stack)
            || (stack.getItem() instanceof ItemFluidEncodedPattern));
    }

    public void encodePattern() {
        if (canEncodePattern()) {
            // if there is an encoded pattern, overwrite it; otherwise, consume a blank
            if (tile.getInventory().getStackInSlot(1).isEmpty()) {
                tile.getInventory().extractItem(0, 1, false); // this better work
            }
            final ItemStack patternStack = new ItemStack(FCItems.DENSE_ENCODED_PATTERN);
            final FluidPatternDetails pattern = new FluidPatternDetails(patternStack);
            pattern.setInputs(collectAeInventory(tile.getCraftingSlots()));
            pattern.setOutputs(collectAeInventory(tile.getOutputSlots()));
            pattern.setEncoder(this.getInventoryPlayer().player.getGameProfile());
            tile.getInventory().setStackInSlot(1, pattern.writeToStack());
        }
    }

    private static IAEItemStack[] collectAeInventory(final AeStackInventory<IAEItemStack> inv) {
        // see note at top of DensePatternDetails
        final List<IAEItemStack> acc = new ArrayList<>();
        for (final IAEItemStack stack : inv) {
            if (stack != null) {
                acc.add(stack);
            }
        }
        return acc.toArray(new IAEItemStack[0]);
    }

    // adapted from ae2's AEBaseContainer#doAction
    @Override
    public void doAction(final EntityPlayerMP player, final InventoryAction action, final int slotId, final long id) {
        final Slot slot = getSlot(slotId);
        if (slot instanceof SlotFluidConvertingFake) {
            final ItemStack stack = player.inventory.getItemStack();
            switch (action) {
                case PICKUP_OR_SET_DOWN:
                    if (stack.isEmpty()) {
                        slot.putStack(ItemStack.EMPTY);
                    } else {
                        ((SlotFluidConvertingFake) slot).putConvertedStack(stack.copy());
                    }
                    break;
                case PLACE_SINGLE:
                    if (!stack.isEmpty()) {
                        ((SlotFluidConvertingFake) slot).putConvertedStack(ItemHandlerHelper.copyStackWithSize(stack, 1));
                    }
                    break;
                case SPLIT_OR_PLACE_SINGLE:
                    final ItemStack inSlot = slot.getStack();
                    if (!inSlot.isEmpty()) {
                        if (stack.isEmpty()) {
                            slot.putStack(ItemHandlerHelper.copyStackWithSize(inSlot, Math.max(1, inSlot.getCount() - 1)));
                        } else if (stack.isItemEqual(inSlot)) {
                            slot.putStack(ItemHandlerHelper.copyStackWithSize(inSlot,
                                Math.min(inSlot.getMaxStackSize(), inSlot.getCount() + 1)));
                        } else {
                            ((SlotFluidConvertingFake) slot).putConvertedStack(ItemHandlerHelper.copyStackWithSize(stack, 1));
                        }
                    } else if (!stack.isEmpty()) {
                        ((SlotFluidConvertingFake) slot).putConvertedStack(ItemHandlerHelper.copyStackWithSize(stack, 1));
                    }
                    break;
            }
        } else {
            super.doAction(player, action, slotId, id);
        }
    }

    @Override
    public void acceptPattern(final Int2ObjectMap<ItemStack[]> inputs, final List<ItemStack> outputs, final boolean combine) {
        final AeStackInventory<IAEItemStack> craftingSlot = tile.getCraftingSlots();
        final AeStackInventory<IAEItemStack> outputSlot = tile.getOutputSlots();
        for (final int index : inputs.keySet()) {
            final ItemStack[] items = inputs.get(index);
            if (index < craftingSlot.getSlotCount() && items.length > 0) {
                craftingSlot.setStack(index, AEItemStack.fromItemStack(items[0]));
            }
        }
        final int bound = Math.min(outputSlot.getSlotCount(), outputs.size());
        for (int index = 0; index < bound; index++) {
            outputSlot.setStack(index, AEItemStack.fromItemStack(outputs.get(index)));
        }
    }

    private static class SlotFluidConvertingFake extends SlotFake implements SlotFluid {

        private final AeStackInventory<IAEItemStack> inv;

        public SlotFluidConvertingFake(final AeItemStackHandler inv, final int idx, final int x, final int y) {
            super(inv, idx, x, y);
            this.inv = inv.getAeInventory();
        }

        @Override
        public void putStack(final ItemStack stack) {
            inv.setStack(getSlotIndex(), AEItemStack.fromItemStack(stack));
        }

        @Override
        public void setAeStack(@Nullable final IAEItemStack stack, final boolean sync) {
            inv.setStack(getSlotIndex(), stack);
        }

        public void putConvertedStack(final ItemStack stack) {
            if (stack.isEmpty()) {
                setAeStack(null, false);
                return;
            } else if (stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
                final IFluidTankProperties[] tanks = Objects.requireNonNull(
                                                          stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null))
                                                            .getTankProperties();
                for (final IFluidTankProperties tank : tanks) {
                    final IAEItemStack aeStack = FakeFluids.packFluid2AEDrops(tank.getContents());
                    if (aeStack != null) {
                        setAeStack(aeStack, false);
                        return;
                    }
                }
            } else if (ModAndClassUtil.GAS && stack.hasCapability(Capabilities.GAS_HANDLER_CAPABILITY, null)) {
                final GasTankInfo[] tanks = Objects.requireNonNull(
                                                 stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY, null))
                                                   .getTankInfo();
                for (final GasTankInfo tank : tanks) {
                    final IAEItemStack aeStack = FakeGases.packGas2AEDrops(tank.getGas());
                    if (aeStack != null) {
                        setAeStack(aeStack, false);
                        return;
                    }
                }
            }
            putStack(stack);
        }

        @Nullable
        @Override
        public IAEItemStack getAeStack() {
            return inv.getStack(getSlotIndex());
        }

    }

}