package com.glodblock.github.common.item;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.items.misc.ItemEncodedPattern;
import com.glodblock.github.interfaces.HasCustomModel;
import com.glodblock.github.util.FluidPatternDetails;
import com.glodblock.github.util.NameConst;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemLargeEncodedPattern extends ItemEncodedPattern implements HasCustomModel {

    @Override
    protected void getCheckedSubItems(final CreativeTabs creativeTab, final NonNullList<ItemStack> itemStacks) {
        // NO-OP
    }

    @Nullable
    @Override
    public ICraftingPatternDetails getPatternForItem(final ItemStack is, final World w) {
        final FluidPatternDetails pattern = new FluidPatternDetails(is);
        return pattern.readFromStack() ? pattern : null;
    }

    @Override
    public ResourceLocation getCustomModelPath() {
        return NameConst.MODEL_LARGE_ITEM_ENCODED_PATTERN;
    }

    @Override
    public void addCheckedInformation(final ItemStack stack, final World world, final List<String> lines, final ITooltipFlag advancedTooltips) {
        super.addCheckedInformation(stack, world, lines, advancedTooltips);
        final NBTTagCompound tag = stack.getTagCompound();
        if (tag != null && tag.hasKey("encoderName")) {
            lines.add(I18n.format("ae2fc.tooltip.pattern_encoder.name", tag.getString("encoderName")));
        }
    }

}