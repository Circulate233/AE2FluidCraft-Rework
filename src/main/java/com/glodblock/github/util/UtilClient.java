package com.glodblock.github.util;

import appeng.api.storage.data.IAEItemStack;
import appeng.client.me.SlotME;
import appeng.helpers.InventoryAction;
import com.glodblock.github.coremod.mixin.jei.AccessorGhostIngredientDragManager;
import com.glodblock.github.coremod.mixin.jei.AccessorIngredientListOverlay;
import com.glodblock.github.coremod.mixin.jei.AccessorInputHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;

@SideOnly(Side.CLIENT)
public final class UtilClient {

    public static boolean shouldAutoCraft(Slot slot, int mouseButton, ClickType clickType) {
        if (slot instanceof SlotME) {
            IAEItemStack stack;
            InventoryAction action;
            final EntityPlayer player = Minecraft.getMinecraft().player;
            switch (clickType) {
                case PICKUP:
                    action = (mouseButton == 1) ? InventoryAction.SPLIT_OR_PLACE_SINGLE : InventoryAction.PICKUP_OR_SET_DOWN;
                    stack = ((SlotME) slot).getAEStack();
                    if (stack != null && action == InventoryAction.PICKUP_OR_SET_DOWN
                            && (stack.getStackSize() == 0 || GuiScreen.isAltKeyDown())
                            && player.inventory.getItemStack().isEmpty()) {
                        return true;
                    }
                    break;
                case CLONE:
                    stack = ((SlotME) slot).getAEStack();
                    if (stack != null && stack.isCraftable()) {
                        return true;
                    }
                    break;
            }
        }
        return false;
    }

    public static boolean renderPatternSlotTip(GuiScreen gui, int mouseX, int mouseY) {
        var item = getMouseItem();
        if (item.isEmpty()) return false;

        var f = Util.getFluidFromItem(item);
        if (f != null) {
            gui.drawHoveringText(
                Arrays.asList(
                    I18n.format("ae2fc.tooltip.fluid_pattern.tooltip", GameSettings.getKeyDisplayString(-100), f.getLocalizedName()),
                    I18n.format("ae2fc.tooltip.fluid_pattern.tooltip", GameSettings.getKeyDisplayString(-99), item.getDisplayName())
                ),
                mouseX,
                mouseY
            );
            return true;
        }
        if (ModAndClassUtil.GAS) {
            var g = Util.getGasNameFromItem(item);
            if (g != null) {
                gui.drawHoveringText(
                    Arrays.asList(
                        I18n.format("ae2fc.tooltip.fluid_pattern.tooltip", GameSettings.getKeyDisplayString(-100), g),
                        I18n.format("ae2fc.tooltip.fluid_pattern.tooltip", GameSettings.getKeyDisplayString(-99), item.getDisplayName())
                    ),
                    mouseX,
                    mouseY
                );
                return true;
            }
        }
        return false;
    }

    public static boolean renderContainerToolTip(GuiContainer gui, int mouseX, int mouseY) {
        var item = Minecraft.getMinecraft().player.inventory.getItemStack();
        if (item.isEmpty()) return false;

        var f = Util.getFluidFromItem(item);
        if (f != null) {
            final String s = " ： " + I18n.format("gui.appliedenergistics2.security.inject.name") + " " + TextFormatting.RESET;
            gui.drawHoveringText(
                Arrays.asList(
                    TextFormatting.DARK_GRAY + GameSettings.getKeyDisplayString(-100) + s + f.getLocalizedName(),
                    TextFormatting.DARK_GRAY + GameSettings.getKeyDisplayString(-99) + s + item.getDisplayName()
                ),
                mouseX,
                mouseY
            );
            return true;
        }
        if (ModAndClassUtil.GAS) {
            var g = Util.getGasNameFromItem(item);
            if (g != null) {
                final String s = " ： " + I18n.format("gui.appliedenergistics2.security.inject.name") + " " + TextFormatting.RESET;
                gui.drawHoveringText(
                    Arrays.asList(
                        TextFormatting.DARK_GRAY + GameSettings.getKeyDisplayString(-100) + s + g,
                        TextFormatting.DARK_GRAY + GameSettings.getKeyDisplayString(-99) + s + item.getDisplayName()
                    ),
                    mouseX,
                    mouseY
                );
                return true;
            }
        }
        return false;
    }

    public static ItemStack getMouseItem() {
        var i = Minecraft.getMinecraft().player.inventory.getItemStack();
        if (!i.isEmpty()) return i;

        if (ModAndClassUtil.JEI) return getJEIMouseItem();

        return ItemStack.EMPTY;
    }

    @Optional.Method(modid = "jei")
    public static ItemStack getJEIMouseItem() {
        var ii = ((AccessorGhostIngredientDragManager) ((AccessorIngredientListOverlay) ((AccessorInputHandler) Ae2ReflectClient.getInputHandler()).getIngredientListOverlay()).getGhostIngredientDragManager()).getGhostIngredientDrag();
        if (ii != null && ii.getIngredient() instanceof ItemStack stack) return stack;
        return ItemStack.EMPTY;
    }

}
