package com.glodblock.github.coremod.mixin.ae2.gui;

import appeng.client.gui.AEBaseMEGui;
import appeng.client.gui.implementations.GuiMEMonitorable;
import appeng.client.me.SlotME;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.item.fake.FakeItemRegister;
import com.glodblock.github.integration.mek.FCGasItems;
import com.glodblock.github.loader.FCItems;
import com.glodblock.github.network.CpacketMEMonitorableAction;
import com.glodblock.github.util.ModAndClassUtil;
import com.glodblock.github.util.Util;
import com.glodblock.github.util.UtilClient;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasItem;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.Optional;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = GuiMEMonitorable.class,remap = false)
public abstract class MixinGuiMEMonitorable extends AEBaseMEGui {

    public MixinGuiMEMonitorable(final Container container) {
        super(container);
    }

    @Intrinsic
    protected void renderHoveredToolTip(final int mouseX, final int mouseY) {
        final Slot slot = this.getSlotUnderMouse();
        if (slot instanceof final SlotME s && s.isEnabled()) {
            if (UtilClient.getMouseItem().isEmpty()) {
                final var item = s.getAEStack();
                if (item != null) {
                    if (item.getItem() == FCItems.FLUID_DROP) {
                        if (UtilClient.rendererFluid(this, item, mouseX, mouseY, true)) return;
                    }
                    if (ModAndClassUtil.GAS && item.getItem() == FCGasItems.GAS_DROP) {
                        if (UtilClient.rendererGas(this, item, mouseX, mouseY, true)) return;
                    }
                }
            } else if (UtilClient.renderContainerToolTip(this, mouseX, mouseY)) return;
        }
        super.renderHoveredToolTip(mouseX, mouseY);
    }

    @Intrinsic
    protected void handleMouseClick(final Slot slot, final int slotIdx, final int mouseButton, final ClickType clickType) {
        if (slot instanceof final SlotME s) {
            if (!UtilClient.getMouseItem().isEmpty()) {
                if (mouseButton == 0) {
                    final var h = this.mc.player.inventory.getItemStack();
                    if (!h.isEmpty()) {
                        final boolean f;
                        if (s.getAEStack() != null) {
                            f = s.getAEStack().getItem() == FCItems.FLUID_DROP;
                        } else f = false;
                        if (h.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)
                            && (f || Util.getFluidFromItem(h) != null)) {
                            final FluidStack fluid = f ? FakeItemRegister.getStack(s.getAEStack()) : null;
                            FluidCraft.proxy.netHandler.sendToServer(new CpacketMEMonitorableAction
                                (CpacketMEMonitorableAction.FLUID, fluid != null ? fluid.writeToNBT(new NBTTagCompound()) : new NBTTagCompound()));
                            return;
                        }
                        if (ModAndClassUtil.GAS) {
                            if (mek$handleMouseClick(s, h)) return;
                        }
                    }
                }
            }
            if (s.getAEStack() != null) {
                if (s.getAEStack().getItem() == FCItems.FLUID_DROP
                    || (ModAndClassUtil.GAS && s.getAEStack().getItem() == FCGasItems.GAS_DROP)) {
                    if (mouseButton != 2
                        && !(mouseButton == 0
                        && (s.getAEStack().getStackSize() == 0 || isAltKeyDown()))
                    ) {
                        if (s.getAEStack().getItem() == FCItems.FLUID_DROP) {
                            final var shift = s.getAEStack().getDefinition().writeToNBT(new NBTTagCompound());
                            shift.setBoolean("shift", isShiftKeyDown());
                            FluidCraft.proxy.netHandler.sendToServer(new CpacketMEMonitorableAction
                                (CpacketMEMonitorableAction.FLUID_OPERATE, shift));
                        }
                        return;
                    }
                }
            }
        }
        super.handleMouseClick(slot, slotIdx, mouseButton, clickType);
    }

    @Unique
    @Optional.Method(modid = "mekeng")
    protected boolean mek$handleMouseClick(final SlotME s, final ItemStack h) {
        final boolean g;
        if (s.getAEStack() != null) {
            g = s.getAEStack().getItem() == FCGasItems.GAS_DROP;
        } else g = false;
        if (h.getItem() instanceof IGasItem && (g || Util.getGasFromItem(h) != null)) {
            final GasStack gas = g ? FakeItemRegister.getStack(s.getAEStack()) : null;
            FluidCraft.proxy.netHandler.sendToServer(new CpacketMEMonitorableAction
                (CpacketMEMonitorableAction.GAS, gas != null ? gas.write(new NBTTagCompound()) : new NBTTagCompound()));
            return true;
        }
        return false;
    }
}