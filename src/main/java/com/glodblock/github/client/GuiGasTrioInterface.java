package com.glodblock.github.client;

import appeng.api.AEApi;
import appeng.client.gui.widgets.GuiTabButton;
import com.glodblock.github.inventory.GuiType;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.util.Ae2ReflectClient;
import com.mekeng.github.client.gui.GuiGasInterface;
import com.mekeng.github.common.me.duality.IGasInterfaceHost;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import java.io.IOException;

public class GuiGasTrioInterface extends GuiGasInterface {

    private GuiTabButton switchItemInterface;
    private GuiTabButton switchFluidInterface;
    private GuiTabButton priorityBtn;

    public GuiGasTrioInterface(final InventoryPlayer ip, final IGasInterfaceHost te) {
        super(ip, te);
    }

    @Override
    public void initGui() {
        super.initGui();
        final ItemStack iconItem = AEApi.instance().definitions().blocks().iface().maybeStack(1).orElse(ItemStack.EMPTY);
        switchItemInterface = new GuiTabButton(guiLeft + 133, guiTop, iconItem, iconItem.getDisplayName(), itemRender);
        buttonList.add(switchItemInterface);
        final ItemStack iconFluid = AEApi.instance().definitions().blocks().fluidIface().maybeStack(1).orElse(ItemStack.EMPTY);
        switchFluidInterface = new GuiTabButton(guiLeft + 112, guiTop, iconFluid, iconFluid.getDisplayName(), itemRender);
        buttonList.add(switchFluidInterface);
        priorityBtn = Ae2ReflectClient.getPriorityButtonGas(this);
    }

    @Override
    protected void actionPerformed(final GuiButton btn) throws IOException {
        if (btn == switchItemInterface) {
            InventoryHandler.switchGui(GuiType.TRIO_ITEM_INTERFACE);
        } else if (btn == switchFluidInterface) {
            InventoryHandler.switchGui(GuiType.TRIO_FLUID_INTERFACE);
        } else if (btn == priorityBtn) {
            InventoryHandler.switchGui(GuiType.PRIORITY);
        } else {
            super.actionPerformed(btn);
        }
    }

}
