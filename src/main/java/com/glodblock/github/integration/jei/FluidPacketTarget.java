package com.glodblock.github.integration.jei;

import appeng.api.storage.data.IAEItemStack;
import appeng.container.slot.SlotFake;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketInventoryAction;
import appeng.helpers.InventoryAction;
import appeng.util.item.AEItemStack;
import com.glodblock.github.common.item.fake.FakeFluids;
import com.glodblock.github.integration.mek.FakeGases;
import com.glodblock.github.util.ModAndClassUtil;
import com.glodblock.github.util.Util;
import mekanism.api.gas.GasStack;
import mezz.jei.api.gui.IGhostIngredientHandler;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.input.Mouse;

import javax.annotation.Nonnull;
import java.awt.Rectangle;
import java.io.IOException;

public class FluidPacketTarget implements IGhostIngredientHandler.Target<Object> {

    private final int left;
    private final int top;
    private final Slot slot;

    public FluidPacketTarget(final int guiLeft, final int guiTop, final Slot slot) {
        this.left = guiLeft;
        this.top = guiTop;
        this.slot = slot;
    }

    @Nonnull
    @Override
    public Rectangle getArea() {
        return new Rectangle(left + slot.xPos, top + slot.yPos, 16, 16);
    }

    @Override
    public void accept(@Nonnull final Object ingredient) {
        IAEItemStack packet = null;
        if (Mouse.getEventButton() == 0 || !(ingredient instanceof ItemStack)) {
            final FluidStack fluid = covertFluid(ingredient);
            final Object gas = covertGas(ingredient);
            if (fluid != null || gas != null) {
                if (fluid != null) {
                    packet = FakeFluids.packFluid2AEDrops(fluid);
                } else {
                    packet = FakeGases.packGas2AEDrops((GasStack) gas);
                }
            }
        }
        if (ingredient instanceof final ItemStack s) {
            if (packet == null) {
                packet = AEItemStack.fromItemStack(s);
            }
        }

        if (packet == null) return;
        try {
            NetworkHandler.instance().sendToServer(new PacketInventoryAction(InventoryAction.PLACE_JEI_GHOST_ITEM, (SlotFake) slot, packet));
        } catch (final IOException ignored) {

        }
    }

    public static FluidStack covertFluid(final Object ingredient) {
        if (ingredient instanceof FluidStack) {
            return (FluidStack) ingredient;
        } else if (ingredient instanceof ItemStack) {
            return Util.getFluidFromItem((ItemStack) ingredient);
        }
        return null;
    }

    public static Object covertGas(final Object ingredient) {
        if (ModAndClassUtil.GAS) {
            if (ingredient instanceof GasStack) {
                return ingredient;
            } else if (ingredient instanceof ItemStack) {
                return Util.getGasFromItem((ItemStack) ingredient);
            }
        }
        return null;
    }

}