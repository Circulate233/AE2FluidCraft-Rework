package com.glodblock.github.common.item;

import appeng.api.AEApi;
import appeng.api.parts.IPartItem;
import com.glodblock.github.common.part.PartExtendedFluidPatternTerminal;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Deprecated
public class ItemPartExtendedFluidPatternTerminal extends Item implements IPartItem<PartExtendedFluidPatternTerminal> {

    public ItemPartExtendedFluidPatternTerminal() {
        this.setMaxStackSize(64);
    }

    @Nullable
    @Override
    public PartExtendedFluidPatternTerminal createPartFromItemStack(final ItemStack is) {
        return new PartExtendedFluidPatternTerminal(is);
    }

    @Override
    @Nonnull
    public EnumActionResult onItemUse(@Nonnull final EntityPlayer player, @Nonnull final World world, @Nonnull final BlockPos pos, @Nonnull final EnumHand hand, @Nonnull final EnumFacing side,
                                      final float hitX, final float hitY, final float hitZ) {
        return AEApi.instance().partHelper().placeBus(player.getHeldItem(hand), pos, side, player, hand, world);
    }

    @Override
    protected boolean isInCreativeTab(final CreativeTabs targetTab) {
        return false;
    }

    @Override
    public void getSubItems(final CreativeTabs tab, final NonNullList<ItemStack> items) {

    }
}
