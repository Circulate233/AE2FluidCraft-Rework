package com.glodblock.github.coremod.mixin.ae2.gui;

import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.AEBaseMEGui;
import appeng.client.gui.implementations.GuiMEMonitorable;
import appeng.client.me.SlotME;
import appeng.util.Platform;
import com.glodblock.github.common.item.fake.FakeItemRegister;
import com.glodblock.github.integration.mek.FCGasItems;
import com.glodblock.github.loader.FCItems;
import com.glodblock.github.util.ModAndClassUtil;
import com.glodblock.github.util.UtilClient;
import com.mekeng.github.common.me.data.IAEGasStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@Mixin(value = GuiMEMonitorable.class,remap = false)
public abstract class MixinGuiMEMonitorable extends AEBaseMEGui {

    public MixinGuiMEMonitorable(Container container) {
        super(container);
    }

    @Intrinsic
    protected void renderHoveredToolTip(int mouseX, int mouseY) {
        Slot slot = this.hoveredSlot;
        if (UtilClient.getMouseItem().isEmpty() && slot instanceof SlotME s && s.isEnabled()) {
            var item = s.getAEStack();
            if (item != null) {
                if (item.getItem() == FCItems.FLUID_DROP) {
                    IAEFluidStack fluidStack = FakeItemRegister.getAEStack(item);
                    if (fluidStack != null) {
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
        }

        super.renderHoveredToolTip(mouseX, mouseY);
    }

    @Unique
    @Optional.Method(modid = "mekeng")
    private boolean rendererGas(IAEItemStack item, int mouseX, int mouseY) {
        IAEGasStack gs = FakeItemRegister.getAEStack(item);
        if (gs != null) {
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
}