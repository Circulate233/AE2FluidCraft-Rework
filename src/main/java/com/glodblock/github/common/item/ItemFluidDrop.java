package com.glodblock.github.common.item;

import com.glodblock.github.common.item.fake.FakeItemRegister;
import com.glodblock.github.util.NameConst;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemFluidDrop extends Item {

    @Override
    public void getSubItems(@Nonnull CreativeTabs tab,@Nonnull NonNullList<ItemStack> items) {

    }

    @Override
    protected boolean isInCreativeTab(CreativeTabs targetTab) {
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    @Nonnull
    public String getItemStackDisplayName(@Nonnull ItemStack stack) {
        FluidStack fluid = FakeItemRegister.getStack(stack);
        // would like to use I18n::format instead of this deprecated function, but that only exists on the client :/
        return I18n.translateToLocalFormatted(getTranslationKey(stack) + ".name", fluid != null ? fluid.getLocalizedName() : "???");
    }

    @SuppressWarnings("deprecation")
    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World world, @Nonnull List<String> tooltip, @Nonnull ITooltipFlag flags) {
        FluidStack fluid = FakeItemRegister.getStack(stack);
        if (fluid != null) {
            tooltip.add(String.format(TextFormatting.GRAY + "%s, 1 mB", fluid.getLocalizedName()));
        } else {
            tooltip.add(TextFormatting.RED + I18n.translateToLocal(NameConst.TT_INVALID_FLUID));
        }
    }

}