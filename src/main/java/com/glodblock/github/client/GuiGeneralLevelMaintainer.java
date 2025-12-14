package com.glodblock.github.client;

import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiNumberBox;
import appeng.container.interfaces.IJEIGhostIngredients;
import appeng.container.slot.SlotFake;
import appeng.core.localization.GuiText;
import appeng.util.item.AEItemStack;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.container.ContainerGeneralLevelMaintainer;
import com.glodblock.github.common.tile.TileGeneralLevelMaintainer;
import com.glodblock.github.integration.jei.FluidPacketTarget;
import com.glodblock.github.integration.mek.FCGasItems;
import com.glodblock.github.loader.FCItems;
import com.glodblock.github.network.CPacketUpdateGeneralLevel;
import com.glodblock.github.util.ModAndClassUtil;
import com.glodblock.github.util.NameConst;
import com.glodblock.github.util.UtilClient;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mezz.jei.api.gui.IGhostIngredientHandler;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class GuiGeneralLevelMaintainer extends AEBaseGui implements IJEIGhostIngredients {

    private static final ResourceLocation TEX_BG = FluidCraft.resource("textures/gui/general_level_maintainer.png");

    private final ContainerGeneralLevelMaintainer cont;
    private final GuiNumberBox[] maintain = new GuiNumberBox[TileGeneralLevelMaintainer.MAX_FLUID];
    private final GuiNumberBox[] request = new GuiNumberBox[TileGeneralLevelMaintainer.MAX_FLUID];
    private final Map<IGhostIngredientHandler.Target<?>, Object> mapTargetSlot = new Object2ObjectOpenHashMap<>();

    public GuiGeneralLevelMaintainer(final InventoryPlayer ipl, final TileGeneralLevelMaintainer tile) {
        super(new ContainerGeneralLevelMaintainer(ipl, tile));
        this.cont = (ContainerGeneralLevelMaintainer) inventorySlots;
        this.ySize = 223;
    }

    public void setMaintainNumber(final int id, final int size) {
        if (id < 0 || id >= TileGeneralLevelMaintainer.MAX_FLUID || size < 0)
            return;
        this.maintain[id].setText(String.valueOf(size));
    }

    @Override
    protected void renderHoveredToolTip(final int mouseX, final int mouseY) {
        final Slot slot = this.getSlotUnderMouse();
        if (slot instanceof final SlotFake s && s.isEnabled()) {
            if (UtilClient.getMouseItem().isEmpty()) {
                final var item = s.getStack();
                if (!item.isEmpty()) {
                    if (item.getItem() == FCItems.FLUID_DROP) {
                        if (UtilClient.rendererFluid(this, AEItemStack.fromItemStack(item), mouseX, mouseY, false))
                            return;
                    }
                    if (ModAndClassUtil.GAS && item.getItem() == FCGasItems.GAS_DROP) {
                        if (UtilClient.rendererGas(this, AEItemStack.fromItemStack(item), mouseX, mouseY, false))
                            return;
                    }
                }
            } else if (UtilClient.renderPatternSlotTip(this, mouseX, mouseY)) return;
        }
        super.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void keyTyped(final char character, final int key) throws IOException {
        if (!this.checkHotbarKeys(key)) {
            GuiNumberBox focus = null;
            int id = 0;
            for (int i = 0; i < TileGeneralLevelMaintainer.MAX_FLUID; i++) {
                if (maintain[i].isFocused()) {
                    focus = maintain[i];
                    id = i;
                }
                if (request[i].isFocused()) {
                    focus = request[i];
                    id = i + 10;
                }
            }
            if (focus != null && key != 1) {
                if ((key == 211 || key == 205 || key == 203 || key == 14 || character == '-' || Character.isDigit(character)) && focus.textboxKeyTyped(character, key)) {
                    try {
                        String out = focus.getText();

                        boolean fixed;
                        for (fixed = false; out.startsWith("0") && out.length() > 1; fixed = true) {
                            out = out.substring(1);
                        }

                        if (fixed) {
                            focus.setText(out);
                        }

                        if (out.isEmpty()) {
                            out = "0";
                        }

                        long result = Long.parseLong(out);
                        if (result < 0L) {
                            focus.setText("1");
                            result = 1;
                        }

                        if (result > Integer.MAX_VALUE) {
                            result = Integer.MAX_VALUE;
                            focus.setText(String.valueOf(result));
                        }

                        if (id >= 10 || result != 0)
                            FluidCraft.proxy.netHandler.sendToServer(new CPacketUpdateGeneralLevel(id, (int) result));

                    } catch (final NumberFormatException ignored) {
                    }
                }
            } else {
                super.keyTyped(character, key);
            }
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        for (int i = 0; i < TileGeneralLevelMaintainer.MAX_FLUID; i++) {
            maintain[i] = new GuiNumberBox(this.fontRenderer, this.guiLeft + 39, this.guiTop + 33 + i * 20, 51, 10, Integer.class);
            request[i] = new GuiNumberBox(this.fontRenderer, this.guiLeft + 102, this.guiTop + 33 + i * 20, 51, 10, Integer.class);
            maintain[i].setTextColor(16777215);
            request[i].setTextColor(16777215);
            maintain[i].setEnableBackgroundDrawing(false);
            request[i].setEnableBackgroundDrawing(false);
            maintain[i].setMaxStringLength(10);
            request[i].setMaxStringLength(10);
        }
        final TileGeneralLevelMaintainer tile = this.cont.getTile();
        final IItemHandler inv = tile.getInventoryHandler();
        for (int i = 0; i < inv.getSlots(); i++) {
            if (!inv.getStackInSlot(i).isEmpty()) {
                this.maintain[i].setText(Integer.toString(inv.getStackInSlot(i).getCount()));
            } else {
                this.maintain[i].setText("0");
            }
            this.request[i].setText(String.valueOf(tile.getRequest()[i]));
        }
    }

    @Override
    protected void mouseClicked(final int xCoord, final int yCoord, final int btn) throws IOException {
        for (int i = 0; i < TileGeneralLevelMaintainer.MAX_FLUID; i++) {
            this.configNumberBar(request[i], xCoord, yCoord, btn);
            this.configNumberBar(maintain[i], xCoord, yCoord, btn);
        }
        super.mouseClicked(xCoord, yCoord, btn);
    }

    private void configNumberBar(final GuiNumberBox bar, final int xCoord, final int yCoord, final int btn) {
        bar.mouseClicked(xCoord, yCoord, btn);
        if (btn == 1 && bar.isFocused()) {
            bar.setText("");
        }
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        fontRenderer.drawString(getGuiDisplayName(I18n.format(NameConst.GUI_FLUID_LEVEL_MAINTAINER)), 8, 6, 0x404040);
        fontRenderer.drawString(GuiText.inventory.getLocal(), 8, ySize - 94, 0x404040);
        fontRenderer.drawString(I18n.format(NameConst.MISC_THRESHOLD), 39, 19, 0x404040);
        fontRenderer.drawString(I18n.format(NameConst.MISC_REQ), 102, 19, 0x404040);
    }

    @Override
    public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        mc.getTextureManager().bindTexture(TEX_BG);
        drawTexturedModalRect(offsetX, offsetY, 0, 0, 176, ySize);
        for (int i = 0; i < TileGeneralLevelMaintainer.MAX_FLUID; i++) {
            this.maintain[i].drawTextBox();
            this.request[i].drawTextBox();
        }
    }

    @Override
    public List<IGhostIngredientHandler.Target<?>> getPhantomTargets(final Object ingredient) {
        this.mapTargetSlot.clear();
        final List<IGhostIngredientHandler.Target<?>> list = new ObjectArrayList<>();
        if (!this.inventorySlots.inventorySlots.isEmpty()) {
            for (final Slot slots : this.inventorySlots.inventorySlots) {
                if (slots instanceof final SlotFake slot) {
                    final IGhostIngredientHandler.Target<Object> targetItem
                        = new FluidPacketTarget(getGuiLeft(), getGuiTop(), slot);
                    list.add(targetItem);
                    this.mapTargetSlot.put(targetItem, slot);
                }
            }
        }
        return list;
    }

    @Override
    public Map<IGhostIngredientHandler.Target<?>, Object> getFakeSlotTargetMap() {
        return mapTargetSlot;
    }

}