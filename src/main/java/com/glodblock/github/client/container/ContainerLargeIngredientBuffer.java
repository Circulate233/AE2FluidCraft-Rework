package com.glodblock.github.client.container;

import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotNormal;
import com.glodblock.github.common.tile.TileLargeIngredientBuffer;
import com.glodblock.github.interfaces.TankDumpable;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.items.IItemHandler;

public class ContainerLargeIngredientBuffer extends AEBaseContainer implements TankDumpable {

    private final TileLargeIngredientBuffer tile;

    public ContainerLargeIngredientBuffer(final InventoryPlayer ipl, final TileLargeIngredientBuffer tile) {
        super(ipl, tile);
        this.tile = tile;
        final IItemHandler inv = tile.getInternalInventory();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlotToContainer(new SlotNormal(inv, i * 9 + j, 8 + 18 * j, 72 + 18 * i));
            }
        }
        bindPlayerInventory(ipl, 0, 140);
    }

    public TileLargeIngredientBuffer getTile() {
        return tile;
    }

    @Override
    public boolean canDumpTank(final int index) {
        return tile.getFluidInventory().getFluidInSlot(index) != null;
    }

    @Override
    public void dumpTank(final int index) {
        if (index >= 0 && index < tile.getFluidInventory().getSlots()) {
            tile.getFluidInventory().setFluidInSlot(index, null);
        }
    }

}