package com.glodblock.github.integration.mek;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.block.BlockGasDiscretizer;
import com.glodblock.github.common.block.BlockTrioInterface;
import com.glodblock.github.handler.RegistryHandler;
import com.glodblock.github.util.NameConst;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class FCGasBlocks {

    @GameRegistry.ObjectHolder(FluidCraft.MODID + ":" + NameConst.BLOCK_GAS_DISCRETIZER)
    public static BlockGasDiscretizer GAS_DISCRETIZER;

    @GameRegistry.ObjectHolder(FluidCraft.MODID + ":" + NameConst.BLOCK_TRIO_INTERFACE)
    public static BlockTrioInterface TRIO_INTERFACE;

    public static void init(final RegistryHandler regHandler) {
        regHandler.block(NameConst.BLOCK_GAS_DISCRETIZER, new BlockGasDiscretizer());
        regHandler.block(NameConst.BLOCK_TRIO_INTERFACE, new BlockTrioInterface());
    }

}
