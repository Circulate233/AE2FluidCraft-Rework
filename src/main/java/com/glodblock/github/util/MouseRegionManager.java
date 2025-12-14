package com.glodblock.github.util;

import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.init.SoundEvents;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MouseRegionManager {

    private final GuiContainer gui;
    private final List<Region> regions = new ArrayList<>();

    public MouseRegionManager(final GuiContainer gui) {
        this.gui = gui;
    }

    public void addRegion(final int x, final int y, final int width, final int height, final Handler handler) {
        regions.add(new Region(x, y, width, height, handler));
    }

    public boolean onClick(int mX, int mY, final int button) {
        mX -= gui.getGuiLeft();
        mY -= gui.getGuiTop();
        for (final Region region : regions) {
            if (region.containsMouse(mX, mY) && region.handler.onClick(button)) {
                gui.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1F));
                return false;
            }
        }
        return true;
    }

    public void render(int mX, int mY) {
        mX -= gui.getGuiLeft();
        mY -= gui.getGuiTop();
        for (final Region region : regions) {
            if (region.containsMouse(mX, mY)) {
                final List<String> tooltip = region.handler.getTooltip();
                if (tooltip != null) {
                    gui.drawHoveringText(tooltip, mX, mY);
                    return;
                }
            }
        }
    }

    private static class Region {

        private final int x, y, width, height;
        private final Handler handler;

        Region(final int x, final int y, final int width, final int height, final Handler handler) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.handler = handler;
        }

        boolean containsMouse(final int mX, final int mY) {
            return mX >= x && mX < x + width && mY >= y && mY < y + height;
        }

    }

    public interface Handler {

        @Nullable
        default List<String> getTooltip() {
            return null;
        }

        default boolean onClick(final int button) {
            return false;
        }

    }

}
