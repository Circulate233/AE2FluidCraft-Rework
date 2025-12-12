package com.glodblock.github.client.container;

import appeng.container.guisync.GuiSync;
import appeng.container.implementations.ContainerInterface;
import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import appeng.util.Platform;
import com.glodblock.github.interfaces.FCDualityInterface;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerWrapInterface extends ContainerInterface {

    @GuiSync(95)
    public boolean fluidPacket = false;
    @GuiSync(96)
    public boolean allowSplitting = true;
    @GuiSync(97)
    public int blockModeEx = 0;
    private final DualityInterface dualityInterfaceCopy;

    public ContainerWrapInterface(InventoryPlayer ip, IInterfaceHost te) {
        super(ip, te);
        this.dualityInterfaceCopy = te.getInterfaceDuality();
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (Platform.isServer()) {
            fluidPacket = ((FCDualityInterface) dualityInterfaceCopy).isFluidPacket();
            allowSplitting = ((FCDualityInterface) dualityInterfaceCopy).isAllowSplitting();
            blockModeEx = ((FCDualityInterface) dualityInterfaceCopy).getBlockModeEx();
        }
    }

    public void setFluidPacketInTile(boolean value) {
        this.fluidPacket = value;
        ((FCDualityInterface) dualityInterfaceCopy).setFluidPacket(value);
    }

    public void setAllowSplittingInTile(boolean value) {
        this.allowSplitting = value;
        ((FCDualityInterface) dualityInterfaceCopy).setAllowSplitting(value);
    }

    public void setExtendedBlockMode(int value) {
        this.blockModeEx = value;
        ((FCDualityInterface) dualityInterfaceCopy).setBlockModeEx(value);
    }

}
