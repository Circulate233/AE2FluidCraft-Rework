package com.glodblock.github.client;

import appeng.client.gui.AEBaseGui;
import appeng.core.localization.GuiText;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.container.ContainerFluidPatternEncoder;
import com.glodblock.github.common.tile.TileFluidPatternEncoder;
import com.glodblock.github.network.CPacketEncodePattern;
import com.glodblock.github.util.MouseRegionManager;
import com.glodblock.github.util.NameConst;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class GuiFluidPatternEncoder extends AEBaseGui {

    private static final ResourceLocation TEX_BG = FluidCraft.resource("textures/gui/fluid_pattern_encoder.png");

    private final ContainerFluidPatternEncoder cont;
    private final MouseRegionManager mouseRegions = new MouseRegionManager(this);

    public GuiFluidPatternEncoder(final InventoryPlayer ipl, final TileFluidPatternEncoder tile) {
        super(new ContainerFluidPatternEncoder(ipl, tile));
        this.cont = (ContainerFluidPatternEncoder)inventorySlots;
        mouseRegions.addRegion(141, 38, 10, 10, new MouseRegionManager.Handler() {
            @Override
            public List<String> getTooltip() {
                return Collections.singletonList(I18n.format(NameConst.TT_ENCODE_PATTERN));
            }

            @Override
            public boolean onClick(final int button) {
                if (button == 0) {
                    if (cont.canEncodePattern()) {
                        FluidCraft.proxy.netHandler.sendToServer(new CPacketEncodePattern());
                    }
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void mouseClicked(final int xCoord, final int yCoord, final int btn) throws IOException {
        if (mouseRegions.onClick(xCoord, yCoord, btn)) {
            super.mouseClicked(xCoord, yCoord, btn);
        }
    }

    @Override
    public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        mc.getTextureManager().bindTexture(TEX_BG);
        drawTexturedModalRect(offsetX, offsetY, 0, 0, 176, ySize);
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        fontRenderer.drawString(getGuiDisplayName(I18n.format(NameConst.GUI_FLUID_PATTERN_ENCODER)), 8, 6, 0x404040);
        fontRenderer.drawString(GuiText.inventory.getLocal(), 8, ySize - 94, 0x404040);
        mouseRegions.render(mouseX, mouseY);
    }

}
