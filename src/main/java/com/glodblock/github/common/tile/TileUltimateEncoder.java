package com.glodblock.github.common.tile;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.IConfigManager;
import appeng.items.misc.ItemEncodedPattern;
import appeng.tile.AEBaseInvTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.inv.InvOperation;
import com.glodblock.github.util.Util;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.List;

public class TileUltimateEncoder extends AEBaseInvTile implements ITerminalHost {

    private final AppEngInternalInventory pattern = new AppEngInternalInventory(this, 2);
    private final AppEngInternalInventory craft = new AppEngInternalInventory(this, 42);
    private final AppEngInternalInventory output = new AppEngInternalInventory(this, 8);
    public boolean fluidFirst;
    public boolean combine;

    @Nonnull
    @Override
    public IItemHandler getInternalInventory() {
        return this.pattern;
    }

    public AppEngInternalInventory getCraft() {
        return this.craft;
    }

    public AppEngInternalInventory getOutput() {
        return this.output;
    }

    public AppEngInternalInventory getPattern() {
        return this.pattern;
    }

    @Override
    public void readFromNBT(final NBTTagCompound data) {
        super.readFromNBT(data);
        this.craft.readFromNBT(data.getCompoundTag("craft"));
        this.output.readFromNBT(data.getCompoundTag("output"));
        this.fluidFirst = data.getBoolean("fluidFirst");
        this.combine = data.getBoolean("combine");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data = super.writeToNBT(data);
        this.craft.writeToNBT(data, "craft");
        this.output.writeToNBT(data, "output");
        data.setBoolean("fluidFirst", this.fluidFirst);
        data.setBoolean("combine", this.combine);
        return data;
    }

    public void onChangeCrafting(final Int2ObjectMap<ItemStack[]> inputs, final List<ItemStack> outputs, final boolean combine) {
        Util.clearItemInventory(this.craft);
        Util.clearItemInventory(this.output);
        ItemStack[] fuzzyFind = new ItemStack[Util.findMax(inputs.keySet()) + 1];
        for (final int index : inputs.keySet()) {
            Util.fuzzyTransferItems(index, inputs.get(index), fuzzyFind, Util.getItemChannel().createList());
        }
        if (combine) {
            fuzzyFind = Util.compress(fuzzyFind);
        }
        int bound = Math.min(this.craft.getSlots(), fuzzyFind.length);
        for (int x = 0; x < bound; x++) {
            final ItemStack item = fuzzyFind[x];
            this.craft.setStackInSlot(x, item == null ? ItemStack.EMPTY : item);
        }
        bound = Math.min(output.getSlots(), outputs.size());
        for (int x = 0; x < bound; x++) {
            final ItemStack item = outputs.get(x);
            this.output.setStackInSlot(x, item == null ? ItemStack.EMPTY : item);
        }
    }

    @Override
    public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc, final ItemStack removedStack, final ItemStack newStack) {
        if (slot == 1) {
            final ItemStack is = inv.getStackInSlot(1);
            if (!is.isEmpty()) {
                if (is.getItem() instanceof final ItemEncodedPattern patterns) {
                    final ICraftingPatternDetails details = patterns.getPatternForItem(is, this.getWorld());
                    if(details != null) {
                        Util.clearItemInventory(this.craft);
                        Util.clearItemInventory(this.output);
                        for(int x = 0; x < this.craft.getSlots() && x < details.getInputs().length; x++) {
                            final IAEItemStack item = details.getInputs()[x];
                            this.craft.setStackInSlot(x, item == null ? ItemStack.EMPTY : item.createItemStack());
                        }
                        for(int x = 0; x < this.output.getSlots() && x < details.getOutputs().length; x++) {
                            final IAEItemStack item = details.getOutputs()[x];
                            this.output.setStackInSlot(x, item == null ? ItemStack.EMPTY : item.createItemStack());
                        }
                    }
                    this.markDirty();
                }
            }
        }
    }

    @Override
    public <T extends IAEStack<T>> IMEMonitor<T> getInventory(final IStorageChannel<T> iStorageChannel) {
        return null;
    }

    @Override
    public IConfigManager getConfigManager() {
        return null;
    }
}
