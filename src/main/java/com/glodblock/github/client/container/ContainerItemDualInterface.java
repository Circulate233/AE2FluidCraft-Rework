package com.glodblock.github.client.container;

import appeng.container.guisync.GuiSync;
import appeng.container.implementations.ContainerInterface;
import appeng.container.slot.SlotRestrictedInput;
import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import appeng.util.Platform;
import com.glodblock.github.interfaces.FCDualityInterface;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.items.IItemHandler;

public class ContainerItemDualInterface extends ContainerInterface {

    @GuiSync(95)
    public boolean fluidPacket = false;
    @GuiSync(96)
    public boolean allowSplitting = true;
    @GuiSync(97)
    public int blockModeEx = 0;
    private final DualityInterface dualityInterfaceCopy;

    public ContainerItemDualInterface(final InventoryPlayer ip, final IInterfaceHost te) {
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

    public void setFluidPacketInTile(final boolean value) {
        this.fluidPacket = value;
        ((FCDualityInterface) dualityInterfaceCopy).setFluidPacket(value);
    }

    public void setAllowSplittingInTile(final boolean value) {
        this.allowSplitting = value;
        ((FCDualityInterface) dualityInterfaceCopy).setAllowSplitting(value);
    }

    public void setExtendedBlockMode(final int value) {
        this.blockModeEx = value;
        ((FCDualityInterface) dualityInterfaceCopy).setBlockModeEx(value);
    }

    @Override
    protected void setupUpgrades() {
        final IItemHandler upgrades = this.getUpgradeable().getInventoryByName("item_upgrades");
        if (this.availableUpgrades() > 0) {
            this.addSlotToContainer((new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 0, 187, 8, this.getInventoryPlayer())).setNotDraggable());
        }

        if (this.availableUpgrades() > 1) {
            this.addSlotToContainer((new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 1, 187, 26, this.getInventoryPlayer())).setNotDraggable());
        }

        if (this.availableUpgrades() > 2) {
            this.addSlotToContainer((new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 2, 187, 44, this.getInventoryPlayer())).setNotDraggable());
        }

        if (this.availableUpgrades() > 3) {
            this.addSlotToContainer((new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 3, 187, 62, this.getInventoryPlayer())).setNotDraggable());
        }
    }

}
