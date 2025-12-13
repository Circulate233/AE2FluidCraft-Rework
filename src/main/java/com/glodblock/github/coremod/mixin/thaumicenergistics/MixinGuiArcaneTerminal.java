package com.glodblock.github.coremod.mixin.thaumicenergistics;

import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.util.Platform;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.item.fake.FakeItemRegister;
import com.glodblock.github.integration.mek.FCGasItems;
import com.glodblock.github.loader.FCItems;
import com.glodblock.github.network.CpacketMEMonitorableAction;
import com.glodblock.github.util.ModAndClassUtil;
import com.glodblock.github.util.Util;
import com.glodblock.github.util.UtilClient;
import com.mekeng.github.common.me.data.IAEGasStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasItem;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.Loader;
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

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

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
                        IAEFluidStack fluidStack = FakeItemRegister.getAEStack(item.copy().setStackSize(1));
                        if (fluidStack != null) {
                            fluidStack.setStackSize(item.getStackSize());
                            String formattedAmount = NumberFormat.getNumberInstance(Locale.US).format((double) fluidStack.getStackSize() / (double) 1000.0F) + " B";
                            String modName = TextFormatting.BLUE.toString() + TextFormatting.ITALIC + Loader.instance().getIndexedModList().get(Platform.getModId(fluidStack)).getName();
                            List<String> list = new ObjectArrayList<>();
                            list.add(fluidStack.getFluidStack().getLocalizedName());
                            list.add(modName);
                            list.add(TextFormatting.DARK_GRAY + I18n.format("gui.appliedenergistics2.StoredFluids") + " ： " + formattedAmount);
                            if (item.isCraftable())
                                list.add(TextFormatting.GRAY + I18n.format("gui.tooltips.appliedenergistics2.ItemsCraftable"));
                            this.drawHoveringText(list, mouseX, mouseY);
                            return;
                        }
                    }
                    if (ModAndClassUtil.GAS && item.getItem() == FCGasItems.GAS_DROP) {
                        if (rendererGas(item, mouseX, mouseY)) return;
                    }
                }
            } else if (UtilClient.renderContainerToolTip(this, mouseX, mouseY)) return;
        }
        super.renderHoveredToolTip(mouseX, mouseY);
    }

    @Unique
    @Optional.Method(modid = "mekeng")
    private boolean rendererGas(IAEItemStack item, int mouseX, int mouseY) {
        IAEGasStack gs = FakeItemRegister.getAEStack(item.copy().setStackSize(1));
        if (gs != null) {
            gs.setStackSize(item.getStackSize());
            String formattedAmount = NumberFormat.getNumberInstance(Locale.US).format((double) gs.getStackSize() / (double) 1000.0F) + " B";
            String modName = "" + TextFormatting.BLUE + TextFormatting.ITALIC + Loader.instance().getIndexedModList().get("mekanism").getName();
            List<String> list = new ObjectArrayList<>();
            list.add(gs.getGas().getLocalizedName());
            list.add(modName);
            list.add(TextFormatting.DARK_GRAY + I18n.format("tooltip.stored") + " ： " + formattedAmount);
            if (item.isCraftable())
                list.add(TextFormatting.GRAY + I18n.format("gui.tooltips.appliedenergistics2.ItemsCraftable"));
            this.drawHoveringText(list, mouseX, mouseY);
            return true;
        }
        return false;
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
                            final boolean g;
                            if (s.getAEStack() != null) {
                                if (s.getAEStack() instanceof IAEItemStack stack) {
                                    g = stack.getItem() == FCGasItems.GAS_DROP;
                                } else {
                                    ci.cancel();
                                    return;
                                }
                            } else g = false;
                            if (h.getItem() instanceof IGasItem && (g || Util.getGasFromItem(h) != null)) {
                                GasStack gas = g ? FakeItemRegister.getStack((IAEItemStack) s.getAEStack()) : null;
                                FluidCraft.proxy.netHandler.sendToServer(new CpacketMEMonitorableAction
                                    (CpacketMEMonitorableAction.GAS, gas != null ? gas.write(new NBTTagCompound()) : new NBTTagCompound()));
                                ci.cancel();
                                return;
                            }
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
}