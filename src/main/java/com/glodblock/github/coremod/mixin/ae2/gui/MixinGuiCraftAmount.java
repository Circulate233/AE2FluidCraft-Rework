package com.glodblock.github.coremod.mixin.ae2.gui;

import appeng.api.storage.data.IAEFluidStack;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.implementations.GuiCraftAmount;
import com.glodblock.github.common.item.fake.FakeItemRegister;
import com.glodblock.github.integration.mek.FCGasItems;
import com.glodblock.github.loader.FCItems;
import com.glodblock.github.util.ModAndClassUtil;
import com.glodblock.github.util.UtilClient;
import mekanism.api.gas.GasStack;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Optional;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Collections;

@Mixin(GuiCraftAmount.class)
public abstract class MixinGuiCraftAmount extends AEBaseGui {
    public MixinGuiCraftAmount(final Container container) {
        super(container);
    }

    @Intrinsic
    protected void renderHoveredToolTip(final int mouseX, final int mouseY) {
        final Slot slot = this.getSlotUnderMouse();
        if (UtilClient.getMouseItem().isEmpty() && slot != null) {
            if (slot.getHasStack()) {
                final var item = slot.getStack();
                if (item.getItem() == FCItems.FLUID_DROP) {
                    final IAEFluidStack fluidStack = FakeItemRegister.getAEStack(item);
                    if (fluidStack != null) {
                        this.drawHoveringText(Collections.singletonList(fluidStack.getFluidStack().getLocalizedName()), mouseX, mouseY);
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
    private boolean rendererGas(final ItemStack item, final int mouseX, final int mouseY) {
        final GasStack gs = FakeItemRegister.getStack(item);
        if (gs != null) {
            this.drawHoveringText(Collections.singletonList(gs.getGas().getLocalizedName()), mouseX, mouseY);
            return true;
        }
        return false;
    }
}