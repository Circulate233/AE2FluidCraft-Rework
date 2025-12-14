package com.glodblock.github.integration.mek;

import appeng.api.storage.data.IAEItemStack;
import appeng.client.render.StackSizeRenderer;
import com.glodblock.github.client.render.FluidRenderUtils;
import com.glodblock.github.common.item.ItemGasPacket;
import com.glodblock.github.common.item.fake.FakeItemRegister;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class GasRenderUtil {

    private static TextureAtlasSprite getStillGasSprite(final Gas gas) {
        final TextureMap textureMapBlocks = Minecraft.getMinecraft().getTextureMapBlocks();
        final ResourceLocation gasStill = gas.getIcon();
        TextureAtlasSprite gasStillSprite = null;
        if (gasStill != null) {
            gasStillSprite = textureMapBlocks.getTextureExtry(gasStill.toString());
        }
        if (gasStillSprite == null) {
            gasStillSprite = textureMapBlocks.getMissingSprite();
        }
        return gasStillSprite;
    }

    public static void renderGasIntoGuiCleanly(final int x, final int y, final int width, final int height, @Nullable final GasStack gasStack, final int capacity) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        if (gasStack != null && gasStack.getGas() != null) {
            final TextureAtlasSprite sprite = getStillGasSprite(gasStack.getGas());
            final Tessellator tess = Tessellator.getInstance();
            final BufferBuilder buf = tess.getBuffer();
            MekanismRenderer.color(gasStack.getGas());
            FluidRenderUtils.doRenderFluid(tess, buf, x, y, width, height, sprite, gasStack.amount / (double)capacity);
        }
        GlStateManager.color(1F, 1F, 1F, 1F);
    }

    public static boolean renderGasIntoGuiSlot(final Slot slot, @Nullable final GasStack gas,
                                               final StackSizeRenderer stackSizeRenderer, final FontRenderer fontRenderer) {
        if (gas == null || gas.amount <= 0) {
            return false;
        }
        renderGasIntoGuiCleanly(slot.xPos, slot.yPos, 16, 16, gas, gas.amount);
        stackSizeRenderer.renderStackSize(fontRenderer, FakeGases.packGas2AEDrops(gas), slot.xPos, slot.yPos);
        return true;
    }

    public static boolean renderGasPacketIntoGuiSlot(final Slot slot, @Nullable final IAEItemStack stack,
                                                     final StackSizeRenderer stackSizeRenderer, final FontRenderer fontRenderer) {
        return stack != null && stack.getItem() instanceof ItemGasPacket
                && renderGasIntoGuiSlot(slot, FakeItemRegister.getStack(stack), stackSizeRenderer, fontRenderer);
    }

    public static boolean renderGasPacketIntoGuiSlot(final Slot slot, final ItemStack stack,
                                                     final StackSizeRenderer stackSizeRenderer, final FontRenderer fontRenderer) {
        return !stack.isEmpty() && stack.getItem() instanceof ItemGasPacket
                && renderGasIntoGuiSlot(slot, FakeItemRegister.getStack(stack), stackSizeRenderer, fontRenderer);
    }

}
