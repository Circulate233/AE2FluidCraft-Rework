package com.glodblock.github.coremod.mixin.ae2.part_or_tile;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.items.misc.ItemEncodedPattern;
import appeng.parts.reporting.AbstractPartEncoder;
import appeng.parts.reporting.PartExpandedProcessingPatternTerminal;
import appeng.util.inv.InvOperation;
import com.glodblock.github.common.item.ItemFluidCraftEncodedPattern;
import com.glodblock.github.common.item.ItemFluidEncodedPattern;
import com.glodblock.github.common.item.ItemLargeEncodedPattern;
import com.glodblock.github.interfaces.FCFluidPatternPart;
import com.glodblock.github.util.FluidCraftingPatternDetails;
import com.glodblock.github.util.Util;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

import static com.glodblock.github.util.Util.putPattern;

@Mixin(value = PartExpandedProcessingPatternTerminal.class, remap = false)
public abstract class MixinPartExpandedProcessingPatternTerminal extends AbstractPartEncoder implements FCFluidPatternPart {

    @Unique
    private boolean fc$combine = false;
    @Unique
    private boolean fc$fluidFirst = false;

    public MixinPartExpandedProcessingPatternTerminal(final ItemStack is) {
        super(is);
    }

    public boolean getCombineMode() {
        return this.fc$combine;
    }

    public void setCombineMode(final boolean value) {
        this.fc$combine = value;
    }

    public boolean getFluidPlaceMode() {
        return this.fc$fluidFirst;
    }

    public void setFluidPlaceMode(final boolean value) {
        this.fc$fluidFirst = value;
    }

    public void onChangeCrafting(final Int2ObjectMap<ItemStack[]> inputs, final List<ItemStack> outputs, final boolean combine) {
        Util.onPatternTerminalChangeCrafting(this, true, inputs, outputs, combine);
    }

    @Intrinsic
    public void readFromNBT(final NBTTagCompound data) {
        super.writeToNBT(data);
        fc$combine = data.getBoolean("combineMode");
        fc$fluidFirst = data.getBoolean("fluidFirst");
    }

    @Intrinsic
    public void writeToNBT(final NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("combineMode", fc$combine);
        data.setBoolean("fluidFirst", fc$fluidFirst);
    }

    @Intrinsic
    public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc, final ItemStack removedStack,
                                  final ItemStack newStack) {
        if (slot == 1) {
            final ItemStack is = inv.getStackInSlot(1);
            if (!is.isEmpty() && (is.getItem() instanceof ItemFluidEncodedPattern || is.getItem() instanceof ItemFluidCraftEncodedPattern || is.getItem() instanceof ItemLargeEncodedPattern)) {
                final ItemEncodedPattern pattern = (ItemEncodedPattern) is.getItem();
                final ICraftingPatternDetails details = pattern.getPatternForItem(is, this.getHost().getTile().getWorld());
                if (details != null) {
                    this.setCraftingRecipe(details.isCraftable());
                    this.setSubstitution(details.canSubstitute());

                    Util.clearItemInventory(this.crafting);
                    Util.clearItemInventory(this.output);

                    if (details instanceof FluidCraftingPatternDetails) {
                        putPattern(this, ((FluidCraftingPatternDetails) details).getOriginInputs(), details.getOutputs());
                        this.setCraftingRecipe(true);
                    } else {
                        putPattern(this, details.getInputs(), details.getOutputs());
                    }
                }
                this.getHost().markForSave();
                return;
            }
        }
        super.onChangeInventory(inv, slot, mc, removedStack, newStack);
    }
}