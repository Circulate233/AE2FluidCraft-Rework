package com.glodblock.github.client.container;

import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotFake;
import appeng.helpers.InventoryAction;
import appeng.tile.inventory.AppEngInternalAEInventory;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.item.fake.FakeFluids;
import com.glodblock.github.common.tile.TileGeneralLevelMaintainer;
import com.glodblock.github.integration.mek.FakeGases;
import com.glodblock.github.network.SPacketSetGeneralLevel;
import com.glodblock.github.util.ModAndClassUtil;
import com.glodblock.github.util.Util;
import mekanism.api.gas.GasStack;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Optional;

public class ContainerGeneralLevelMaintainer extends AEBaseContainer {

    private final TileGeneralLevelMaintainer tile;

    public ContainerGeneralLevelMaintainer(InventoryPlayer ip, TileGeneralLevelMaintainer tile) {
        super(ip, tile);
        this.tile = tile;
        AppEngInternalAEInventory inv = tile.getInventoryHandler();
        for (int i = 0; i < 5; i++) {
            addSlotToContainer(new SlotFake(inv, i, 17, 28 + i * 20));
        }
        bindPlayerInventory(ip, 0, 141);
    }

    @Optional.Method(modid = "mekeng")
    public static int mek$doAction(Slot slot, Object obj) {
        slot.putStack(FakeGases.packGas2Drops((GasStack) obj));
        return ((GasStack) obj).amount;
    }

    public TileGeneralLevelMaintainer getTile() {
        return tile;
    }

    @Override
    public void doAction(EntityPlayerMP player, InventoryAction action, int slotId, long id) {
        Slot slot = getSlot(slotId);
        if (slot instanceof SlotFake) {
            final ItemStack stack = player.inventory.getItemStack();
            final int size;
            if (id == 0) {
                FluidStack s = Util.getFluidFromItem(stack);
                Object g;
                if (s != null) {
                    slot.putStack(FakeFluids.packFluid2Drops(s));
                    size = s.amount;
                } else if (ModAndClassUtil.GAS && (g = Util.getGasFromItem(stack)) != null) {
                    size = mek$doAction(slot, g);
                } else {
                    slot.putStack(stack);
                    size = stack.getCount();
                }
            } else {
                slot.putStack(stack);
                size = stack.getCount();
            }
            FluidCraft.proxy.netHandler.sendTo(new SPacketSetGeneralLevel(slotId, size), player);
        } else {
            super.doAction(player, action, slotId, id);
        }
    }

}