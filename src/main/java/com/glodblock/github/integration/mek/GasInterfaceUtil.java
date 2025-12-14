package com.glodblock.github.integration.mek;

import appeng.api.config.Upgrades;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IMachineSet;
import com.glodblock.github.common.part.PartTrioInterface;
import com.glodblock.github.common.tile.TileTrioInterface;
import com.mekeng.github.common.part.PartGasInterface;
import com.mekeng.github.common.tile.TileGasInterface;
import net.minecraft.item.ItemStack;

public class GasInterfaceUtil {

    public static void addUpgrade() {
        Upgrades.PATTERN_EXPANSION.registerItem(new ItemStack(FCGasBlocks.TRIO_INTERFACE), 3);
        Upgrades.CRAFTING.registerItem(new ItemStack(FCGasBlocks.TRIO_INTERFACE), 1);
        Upgrades.CAPACITY.registerItem(new ItemStack(FCGasBlocks.TRIO_INTERFACE), 4);
        Upgrades.PATTERN_EXPANSION.registerItem(new ItemStack(FCGasItems.PART_TRIO_INTERFACE), 3);
        Upgrades.CRAFTING.registerItem(new ItemStack(FCGasItems.PART_TRIO_INTERFACE), 1);
        Upgrades.CAPACITY.registerItem(new ItemStack(FCGasItems.PART_TRIO_INTERFACE), 4);
    }

    public static IMachineSet getGasInterface(final IGrid grid) {
        return grid.getMachines(TileTrioInterface.class);
    }

    public static IMachineSet getGasPartInterface(final IGrid grid) {
        return grid.getMachines(PartTrioInterface.class);
    }

    public static boolean isGasInterfaceTile(final Class<? extends IGridHost> clazz) {
        return clazz == TileGasInterface.class;
    }

    public static boolean isGasInterfacePart(final Class<? extends IGridHost> clazz) {
        return clazz == PartGasInterface.class;
    }

}
