package com.glodblock.github.coremod.mixin.thaumicenergistics;

import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
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
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.Optional;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thaumicenergistics.client.gui.part.GuiAbstractTerminal;
import thaumicenergistics.client.gui.part.GuiArcaneTerminal;
import thaumicenergistics.container.ContainerBaseTerminal;
import thaumicenergistics.container.slot.SlotME;

@Mixin(value = GuiArcaneTerminal.class, remap = false)
public abstract class MixinGuiArcaneTerminal extends GuiAbstractTerminal<IAEItemStack, IItemStorageChannel> {

    public MixinGuiArcaneTerminal(ContainerBaseTerminal container) {
        super(container);
    }

    @Intrinsic
    protected void renderHoveredToolTip(int mouseX, int mouseY) {
        Slot slot = this.hoveredSlot;
        if (slot instanceof SlotME<?> s && s.isEnabled()) {
            if (UtilClient.getMouseItem().isEmpty()) {
                var aItem = s.getAEStack();
                if (aItem instanceof IAEItemStack item) {
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

    @Inject(method = "handleMouseClick", at = @At("HEAD"), cancellable = true)
    private void fc$handleMouseClick(Slot slot, int slotId, int mouseButton, ClickType type, CallbackInfo ci) {
        boolean success = false;
        if (slot instanceof SlotME<?> s) {
            if (!UtilClient.getMouseItem().isEmpty()) {
                if (mouseButton == 0) {
                    var h = this.mc.player.inventory.getItemStack();
                    if (!h.isEmpty()) {
                        final boolean f;
                        if (s.getAEStack() != null) {
                            if (s.getAEStack() instanceof IAEItemStack stack) {
                                f = stack.getItem() == FCItems.FLUID_DROP;
                            } else {
                                ci.cancel();
                                return;
                            }
                        } else f = false;
                        if (h.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)
                            && (f || Util.getFluidFromItem(h) != null)) {
                            FluidStack fluid = f ? FakeItemRegister.getStack((IAEItemStack) s.getAEStack()) : null;
                            FluidCraft.proxy.netHandler.sendToServer(new CpacketMEMonitorableAction
                                (CpacketMEMonitorableAction.FLUID, fluid != null ? fluid.writeToNBT(new NBTTagCompound()) : new NBTTagCompound()));
                            ci.cancel();
                            return;
                        }
                        if (ModAndClassUtil.GAS) {
                            if (mek$handleMouseClick(s, h, ci)) return;
                        }
                    }
                }
            }
            if (s.getAEStack() instanceof IAEItemStack stack) {
                if (stack.getItem() == FCItems.FLUID_DROP
                    || (ModAndClassUtil.GAS && stack.getItem() == FCGasItems.GAS_DROP)) {
                    if (mouseButton != 2 && !(mouseButton == 0 && stack.getStackSize() == 0)) {
                        if (stack.getItem() == FCItems.FLUID_DROP) {
                            var shift = stack.getDefinition().writeToNBT(new NBTTagCompound());
                            shift.setBoolean("shift", isShiftKeyDown());
                            FluidCraft.proxy.netHandler.sendToServer(new CpacketMEMonitorableAction
                                (CpacketMEMonitorableAction.FLUID_OPERATE, shift));
                            ci.cancel();
                        }
                    }
                }
            }
        }
    }

    @Unique
    @Optional.Method(modid = "mekeng")
    protected boolean mek$handleMouseClick(SlotME s, ItemStack h, CallbackInfo ci) {
        final boolean g;
        if (s.getAEStack() != null) {
            if (s.getAEStack() instanceof IAEItemStack stack) {
                g = stack.getItem() == FCGasItems.GAS_DROP;
            } else {
                ci.cancel();
                return true;
            }
        } else g = false;
        if (h.getItem() instanceof IGasItem && (g || Util.getGasFromItem(h) != null)) {
            GasStack gas = g ? FakeItemRegister.getStack((IAEItemStack) s.getAEStack()) : null;
            FluidCraft.proxy.netHandler.sendToServer(new CpacketMEMonitorableAction
                (CpacketMEMonitorableAction.GAS, gas != null ? gas.write(new NBTTagCompound()) : new NBTTagCompound()));
            ci.cancel();
            return true;
        }
        return false;
    }
}