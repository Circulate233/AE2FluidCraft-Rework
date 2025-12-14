package com.glodblock.github.client.render;

import com.glodblock.github.util.ModAndClassUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

public class DropColourHandler {

    private final Map<String, Integer> colourCache = new HashMap<>();

    @SubscribeEvent
    public void onTextureMapStitch(final TextureStitchEvent event) {
        if (event.getMap() == Minecraft.getMinecraft().getTextureMapBlocks()) {
            colourCache.clear();
        }
    }

    public int getColour(final FluidStack fluidStack) {
        final Fluid fluid = fluidStack.getFluid();
        final int colour = fluid.getColor(fluidStack);
        if (ModAndClassUtil.GT && colour == 0xFFFFFFFF)
            return runBidAidFix(fluidStack);
        return colour != 0xFFFFFFFF ? colour : getColour(fluid);
    }

    public int getColour(final Fluid fluid) {
        final Integer cached = colourCache.get(fluid.getName());
        if (cached != null) {
            return cached;
        }
        int colour = fluid.getColor();
        if (colour == 0xFFFFFFFF) {
            final TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks()
                                                       .getTextureExtry(fluid.getStill().toString());
            if (sprite != null && sprite.getFrameCount() > 0) {
                final int[][] image = sprite.getFrameTextureData(0);
                int r = 0, g = 0, b = 0, count = 0;
                for (final int[] row : image) {
                    for (final int pixel : row) {
                        if (((pixel >> 24) & 0xFF) > 127) { // is alpha above 50%?
                            r += (pixel >> 16) & 0xFF;
                            g += (pixel >> 8) & 0xFF;
                            b += pixel & 0xFF;
                            ++count;
                        }
                    }
                }
                if (count > 0) {
                    // probably shouldn't need to mask each component by 0xFF
                    colour = ((r / count) << 16) | ((g / count) << 8) | (b / count);
                }
            }
        }
        colourCache.put(fluid.getName(), colour);
        return colour;
    }

    //Need to find a better way to replace this.
    private int runBidAidFix(final FluidStack fluidStack) {
        if (fluidStack.isFluidEqual(FluidRegistry.getFluidStack("helium", 1))) {
            return 0xFFFCFF90;
        }
        return getColour(fluidStack.getFluid());
    }

}
