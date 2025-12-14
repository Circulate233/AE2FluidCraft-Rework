package com.glodblock.github.client.container;

import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotNormal;
import com.glodblock.github.common.tile.TileIngredientBuffer;
import com.glodblock.github.interfaces.TankDumpable;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.items.IItemHandler;

public class ContainerIngredientBuffer extends AEBaseContainer implements TankDumpable {

    private final TileIngredientBuffer tile;

    public ContainerIngredientBuffer(final InventoryPlayer ipl, final TileIngredientBuffer tile) {
        super(ipl, tile);
        this.tile = tile;
        final IItemHandler inv = tile.getInternalInventory();
        for (int i = 0; i < 9; i++) {
            addSlotToContainer(new SlotNormal(inv, i, 8 + 18 * i, 108));
        }
        bindPlayerInventory(ipl, 0, 140);
    }

    public TileIngredientBuffer getTile() {
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
