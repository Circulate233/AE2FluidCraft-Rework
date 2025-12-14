package com.glodblock.github.client.render;

import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.render.StackSizeRenderer;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.common.item.fake.FakeFluids;
import com.glodblock.github.common.item.fake.FakeItemRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;

public class FluidRenderUtils {

    @Nullable
    public static TextureAtlasSprite prepareRender(@Nullable final FluidStack fluidStack) {
        if (fluidStack == null) {
            return null;
        }
        final Fluid fluid = fluidStack.getFluid();
        final TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks()
                                                   .getAtlasSprite(fluid.getStill(fluidStack).toString());
        final int colour = fluid.getColor(fluidStack);
        GlStateManager.color(
                ((colour >> 16) & 0xFF) / 255F,
                ((colour >> 8) & 0xFF) / 255F,
                (colour & 0xFF) / 255F,
                ((colour >> 24) & 0xFF) / 255F);
        return sprite;
    }

    public static void doRenderFluid(final Tessellator tess, final BufferBuilder buf, final int x, final int y, final int width, final int height,
                                     final TextureAtlasSprite sprite, final double fraction) {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        int fluidHeight = Math.round(height * (float)Math.min(1D, Math.max(0D, fraction)));
        final double x2 = x + width;
        while (fluidHeight > 0) {
            buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            final double y1 = y + height - fluidHeight;
            final double y2 = y1 + Math.min(fluidHeight, width);
            final double u1 = sprite.getMinU();
            final double v1 = sprite.getMinV();
            final double u2 = sprite.getMaxU();
            double v2 = sprite.getMaxV();
            if (fluidHeight < width) {
                v2 = v1 + (v2 - v1) * (fluidHeight / (double)width);
                fluidHeight = 0;
            } else {
                //noinspection SuspiciousNameCombination
                fluidHeight -= width;
            }
            buf.pos(x, y1, 0D).tex(u1, v1).endVertex();
            buf.pos(x, y2, 0D).tex(u1, v2).endVertex();
            buf.pos(x2, y2, 0D).tex(u2, v2).endVertex();
            buf.pos(x2, y1, 0D).tex(u2, v1).endVertex();
            tess.draw();
        }
        GlStateManager.disableBlend();
    }

    public static void renderFluidIntoGui(final Tessellator tess, final BufferBuilder buf, final int x, final int y, final int width, final int height,
                                          @Nullable final IAEFluidStack aeFluidStack, final int capacity) {
        if (aeFluidStack != null) {
            final TextureAtlasSprite sprite = FluidRenderUtils.prepareRender(aeFluidStack.getFluidStack());
            if (sprite != null) {
                doRenderFluid(tess, buf, x, y, width, height, sprite, aeFluidStack.getStackSize() / (double)capacity);
            }
        }
    }

    public static void renderFluidIntoGui(final Tessellator tess, final BufferBuilder buf, final int x, final int y, final int width, final int height,
                                          @Nullable final FluidStack fluidStack, final int capacity) {
        if (fluidStack != null) {
            final TextureAtlasSprite sprite = FluidRenderUtils.prepareRender(fluidStack);
            if (sprite != null) {
                doRenderFluid(tess, buf, x, y, width, height, sprite, fluidStack.amount / (double)capacity);
            }
        }
    }

    public static void renderFluidIntoGuiCleanly(final int x, final int y, final int width, final int height,
                                                 @Nullable final IAEFluidStack aeFluidStack, final int capacity) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        final Tessellator tess = Tessellator.getInstance();
        renderFluidIntoGui(tess, tess.getBuffer(), x, y, width, height, aeFluidStack, capacity);
        GlStateManager.color(1F, 1F, 1F, 1F);
    }

    public static void renderFluidIntoGuiCleanly(final int x, final int y, final int width, final int height,
                                                 @Nullable final FluidStack fluidStack, final int capacity) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        final Tessellator tess = Tessellator.getInstance();
        renderFluidIntoGui(tess, tess.getBuffer(), x, y, width, height, fluidStack, capacity);
        GlStateManager.color(1F, 1F, 1F, 1F);
    }

    public static boolean renderFluidIntoGuiSlot(final Slot slot, @Nullable final FluidStack fluid,
                                                 final StackSizeRenderer stackSizeRenderer, final FontRenderer fontRenderer) {
        if (fluid == null || fluid.amount <= 0) {
            return false;
        }
        renderFluidIntoGuiCleanly(slot.xPos, slot.yPos, 16, 16, fluid, fluid.amount);
        stackSizeRenderer.renderStackSize(fontRenderer, FakeFluids.packFluid2AEDrops(fluid), slot.xPos, slot.yPos);
        return true;
    }

    public static boolean renderFluidPacketIntoGuiSlot(final Slot slot, @Nullable final IAEItemStack stack,
                                                       final StackSizeRenderer stackSizeRenderer, final FontRenderer fontRenderer) {
        return stack != null && stack.getItem() instanceof ItemFluidPacket
                && renderFluidIntoGuiSlot(slot, FakeItemRegister.getStack(stack), stackSizeRenderer, fontRenderer);
    }

    public static boolean renderFluidPacketIntoGuiSlot(final Slot slot, final ItemStack stack,
                                                       final StackSizeRenderer stackSizeRenderer, final FontRenderer fontRenderer) {
        return !stack.isEmpty() && stack.getItem() instanceof ItemFluidPacket
                && renderFluidIntoGuiSlot(slot, FakeItemRegister.getStack(stack), stackSizeRenderer, fontRenderer);
    }

}
