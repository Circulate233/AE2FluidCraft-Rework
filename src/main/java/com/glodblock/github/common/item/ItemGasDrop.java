package com.glodblock.github.common.item;

import com.glodblock.github.common.item.fake.FakeItemRegister;
import com.glodblock.github.interfaces.HasCustomModel;
import com.glodblock.github.util.NameConst;
import mekanism.api.gas.GasStack;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class ItemGasDrop extends Item implements HasCustomModel {

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
        final GasStack gas = FakeItemRegister.getStack(stack);
        return gas != null ? gas.getGas().getLocalizedName() : "???";
    }

    @Override
    public ResourceLocation getCustomModelPath() {
        return NameConst.MODEL_GAS_DROP;
    }

}