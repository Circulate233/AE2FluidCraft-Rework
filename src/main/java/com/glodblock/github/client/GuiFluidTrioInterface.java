package com.glodblock.github.client;

import appeng.client.gui.widgets.GuiTabButton;
import appeng.fluids.helper.IFluidInterfaceHost;
import com.glodblock.github.inventory.GuiType;
import com.glodblock.github.inventory.InventoryHandler;
import com.mekeng.github.common.ItemAndBlocks;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import java.io.IOException;

public class GuiFluidTrioInterface extends GuiFluidDualInterface {

    private GuiTabButton switchGasInterface;

    public GuiFluidTrioInterface(final InventoryPlayer ip, final IFluidInterfaceHost te) {
        super(ip, te);
    }

    @Override
    public void initGui() {
        super.initGui();
        final ItemStack icon = new ItemStack(ItemAndBlocks.GAS_INTERFACE);
        this.switchGasInterface = new GuiTabButton(guiLeft + 112, guiTop, icon, icon.getDisplayName(), itemRender);
        this.buttonList.add(this.switchGasInterface);
    }

    @Override
    protected void actionPerformed(final GuiButton btn) throws IOException {
        if (btn == this.switchGasInterface) {
            InventoryHandler.switchGui(GuiType.TRIO_GAS_INTERFACE);
        } else if (btn == this.switchInterface) {
            InventoryHandler.switchGui(GuiType.TRIO_ITEM_INTERFACE);
        } else  {
            super.actionPerformed(btn);
        }
    }

}
