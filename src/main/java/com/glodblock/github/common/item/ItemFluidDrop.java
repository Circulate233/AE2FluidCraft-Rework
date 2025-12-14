package com.glodblock.github.common.item;

import com.glodblock.github.common.item.fake.FakeItemRegister;
import com.glodblock.github.interfaces.HasCustomModel;
import com.glodblock.github.util.NameConst;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;

public class ItemFluidDrop extends Item implements HasCustomModel {

    @Override
    public void getSubItems(@Nonnull final CreativeTabs tab, @Nonnull final NonNullList<ItemStack> items) {

    }

    @Override
    protected boolean isInCreativeTab(final CreativeTabs targetTab) {
        return false;
    }

    @Override
    @Nonnull
    public String getItemStackDisplayName(@Nonnull final ItemStack stack) {
        final FluidStack fluid = FakeItemRegister.getStack(stack);
        // would like to use I18n::format instead of this deprecated function, but that only exists on the client :/
        return fluid != null ? fluid.getLocalizedName() : "???";
    }

    @Override
    public ResourceLocation getCustomModelPath() {
        return NameConst.MODEL_FLUID_DROP;
    }
}