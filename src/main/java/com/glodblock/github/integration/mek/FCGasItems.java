package com.glodblock.github.integration.mek;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.item.ItemGasDrop;
import com.glodblock.github.common.item.ItemGasPacket;
import com.glodblock.github.common.item.ItemPartTrioInterface;
import com.glodblock.github.handler.RegistryHandler;
import com.glodblock.github.util.NameConst;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class FCGasItems {

    @GameRegistry.ObjectHolder(FluidCraft.MODID + ":" + NameConst.ITEM_GAS_DROP)
    public static ItemGasDrop GAS_DROP;
    @GameRegistry.ObjectHolder(FluidCraft.MODID + ":" + NameConst.ITEM_GAS_PACKET)
    public static ItemGasPacket GAS_PACKET;
    @GameRegistry.ObjectHolder(FluidCraft.MODID + ":" + NameConst.ITEM_PART_TRIO_INTERFACE)
    public static ItemPartTrioInterface PART_TRIO_INTERFACE;

    public static void init(final RegistryHandler regHandler) {
        regHandler.item(NameConst.ITEM_GAS_DROP, new ItemGasDrop());
        regHandler.item(NameConst.ITEM_GAS_PACKET, new ItemGasPacket());
        regHandler.item(NameConst.ITEM_PART_TRIO_INTERFACE, new ItemPartTrioInterface());
    }

}
