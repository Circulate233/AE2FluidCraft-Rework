package com.glodblock.github.common.item;

import appeng.items.tools.powered.ToolWirelessTerminal;
import com.glodblock.github.inventory.GuiType;
import com.glodblock.github.loader.FCItems;
import com.glodblock.github.util.Util;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

@Deprecated
public class ItemWirelessFluidPatternTerminal extends ToolWirelessTerminal {

    private static final int OFFHAND_SLOT = 40;

    @Override
    public ActionResult<ItemStack> onItemRightClick(final World w, final EntityPlayer player, final EnumHand hand) {
        Util.openWirelessTerminal(player.getHeldItem(hand), hand == EnumHand.MAIN_HAND ? player.inventory.currentItem : OFFHAND_SLOT, false, w, player, GuiType.WIRELESS_FLUID_PATTERN_TERMINAL);
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    @Override
    public boolean canHandle(final ItemStack is) {
        return is.getItem() == FCItems.WIRELESS_FLUID_PATTERN_TERMINAL;
    }

    @Override
    protected boolean isInCreativeTab(final CreativeTabs targetTab) {
        return false;
    }

    @Override
    public void getCheckedSubItems(@Nonnull final CreativeTabs tab, @Nonnull final NonNullList<ItemStack> items) {

    }
}
