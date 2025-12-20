package com.glodblock.github.coremod.mixin.ae2.container;

import appeng.api.implementations.IUpgradeableHost;
import appeng.container.implementations.ContainerCellWorkbench;
import appeng.container.implementations.ContainerUpgradeable;
import appeng.container.slot.SlotFake;
import appeng.helpers.InventoryAction;
import appeng.items.storage.ItemViewCell;
import appeng.tile.misc.TileCellWorkbench;
import com.glodblock.github.common.item.fake.FakeFluids;
import com.glodblock.github.common.item.fake.FakeItemRegister;
import com.glodblock.github.integration.mek.FakeGases;
import com.glodblock.github.util.ModAndClassUtil;
import com.glodblock.github.util.Util;
import mekanism.api.gas.GasStack;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.items.ItemHandlerHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = ContainerCellWorkbench.class, remap = false)
public abstract class MixinContainerCellWorkbench extends ContainerUpgradeable {

    @Shadow
    @Final
    private TileCellWorkbench workBench;

    public MixinContainerCellWorkbench(final InventoryPlayer ip, final IUpgradeableHost te) {
        super(ip, te);
    }

    @Intrinsic
    public void doAction(final EntityPlayerMP player, final InventoryAction action, final int slotId, final long id) {
        if (id != 0 || slotId < 0 || slotId >= this.inventorySlots.size()
            || !(this.workBench.getCell() instanceof ItemViewCell)) {
            super.doAction(player, action, slotId, id);
            return;
        }
        final Slot slot = getSlot(slotId);
        final ItemStack stack = player.inventory.getItemStack();
        if (slot instanceof SlotFake && !stack.isEmpty()
            && stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null) && Util.getFluidFromItem(stack) != null) {
            FluidStack fluid = null;
            switch (action) {
                case PICKUP_OR_SET_DOWN -> {
                    fluid = Util.getFluidFromItem(stack);
                    slot.putStack(FakeFluids.packFluid2Drops(fluid));
                }
                case SPLIT_OR_PLACE_SINGLE -> {
                    fluid = Util.getFluidFromItem(ItemHandlerHelper.copyStackWithSize(stack, 1));
                    final FluidStack origin = FakeItemRegister.getStack(slot.getStack());
                    if (fluid != null && fluid.equals(origin)) {
                        fluid.amount += origin.amount;
                        if (fluid.amount <= 0) fluid = null;
                    }
                    slot.putStack(FakeFluids.packFluid2Drops(fluid));
                }
            }
            if (fluid == null) {
                super.doAction(player, action, slotId, id);
                return;
            }
            return;
        }
        if (ModAndClassUtil.GAS && slot instanceof SlotFake && !stack.isEmpty() && Util.getGasFromItem(stack) != null) {
            mek$doAction(player, action, slotId, id, slot, stack);
            return;
        }
        super.doAction(player, action, slotId, id);
    }

    @Unique
    @Optional.Method(modid = "mekeng")
    private void mek$doAction(EntityPlayerMP player, InventoryAction action, int slotId, long id, Slot slot, ItemStack stack) {
        GasStack gas = null;
        switch (action) {
            case PICKUP_OR_SET_DOWN -> {
                gas = Util.getGasFromItem(stack);
                slot.putStack(FakeGases.packGas2Drops(gas));
            }
            case SPLIT_OR_PLACE_SINGLE -> {
                gas = Util.getGasFromItem(ItemHandlerHelper.copyStackWithSize(stack, 1));
                final GasStack origin = FakeItemRegister.getStack(slot.getStack());
                if (gas != null && gas.equals(origin)) {
                    gas.amount += origin.amount;
                    if (gas.amount <= 0) gas = null;
                }
                slot.putStack(FakeGases.packGas2Drops(gas));
            }
        }
        if (gas == null) {
            super.doAction(player, action, slotId, id);
        }
    }
}