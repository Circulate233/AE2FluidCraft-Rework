package com.glodblock.github.loader;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.network.CPacketDumpTank;
import com.glodblock.github.network.CPacketEncodePattern;
import com.glodblock.github.network.CPacketFluidPatternTermBtns;
import com.glodblock.github.network.CPacketInventoryAction;
import com.glodblock.github.network.CPacketLoadPattern;
import com.glodblock.github.network.CPacketPatternValueSet;
import com.glodblock.github.network.CPacketSwitchGuis;
import com.glodblock.github.network.CPacketTransposeFluid;
import com.glodblock.github.network.CPacketUpdateGeneralLevel;
import com.glodblock.github.network.CPacketUseKeybind;
import com.glodblock.github.network.CpacketMEMonitorableAction;
import com.glodblock.github.network.SPacketSetGeneralLevel;
import com.glodblock.github.network.SPacketSetItemAmount;
import net.minecraftforge.fml.relauncher.Side;

public class ChannelLoader implements Runnable {

    @Override
    @SuppressWarnings("all")
    public void run() {
        int id = 0;
        FluidCraft.proxy.netHandler.registerMessage(new CPacketSwitchGuis.Handler(), CPacketSwitchGuis.class, id ++, Side.SERVER);
        FluidCraft.proxy.netHandler.registerMessage(new CPacketDumpTank.Handler(), CPacketDumpTank.class, id ++, Side.SERVER);
        FluidCraft.proxy.netHandler.registerMessage(new CPacketTransposeFluid.Handler(), CPacketTransposeFluid.class, id ++, Side.SERVER);
        FluidCraft.proxy.netHandler.registerMessage(new CPacketEncodePattern.Handler(), CPacketEncodePattern.class, id ++, Side.SERVER);
        FluidCraft.proxy.netHandler.registerMessage(new CPacketLoadPattern.Handler(), CPacketLoadPattern.class, id ++, Side.SERVER);
        FluidCraft.proxy.netHandler.registerMessage(new CPacketUpdateGeneralLevel.Handler(), CPacketUpdateGeneralLevel.class, id ++, Side.SERVER);
        FluidCraft.proxy.netHandler.registerMessage(new CPacketFluidPatternTermBtns.Handler(), CPacketFluidPatternTermBtns.class, id ++, Side.SERVER);
        FluidCraft.proxy.netHandler.registerMessage(new SPacketSetGeneralLevel.Handler(), SPacketSetGeneralLevel.class, id++, Side.CLIENT);
        FluidCraft.proxy.netHandler.registerMessage(new CPacketPatternValueSet.Handler(), CPacketPatternValueSet.class, id ++, Side.SERVER);
        FluidCraft.proxy.netHandler.registerMessage(new CPacketInventoryAction.Handler(), CPacketInventoryAction.class, id ++, Side.SERVER);
        FluidCraft.proxy.netHandler.registerMessage(new SPacketSetItemAmount.Handler(), SPacketSetItemAmount.class, id ++, Side.CLIENT);
        FluidCraft.proxy.netHandler.registerMessage(new CPacketUseKeybind.Handler(), CPacketUseKeybind.class, id ++, Side.SERVER);
        FluidCraft.proxy.netHandler.registerMessage(new CpacketMEMonitorableAction.Handler(), CpacketMEMonitorableAction.class, id++, Side.SERVER);
    }

}
