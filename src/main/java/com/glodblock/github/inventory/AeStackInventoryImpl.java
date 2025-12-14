package com.glodblock.github.inventory;

import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.util.inv.IAEAppEngInventory;
import com.glodblock.github.interfaces.AeStackInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import org.apache.logging.log4j.core.util.ObjectArrayIterator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Stream;

public class AeStackInventoryImpl <T extends IAEStack<T>> implements AeStackInventory<T> {

    private final IStorageChannel<T> channel;
    private final T[] inv;
    @Nullable
    private final IAEAppEngInventory owner;

    public AeStackInventoryImpl(final IStorageChannel<T> channel, final int slotCount, @Nullable final IAEAppEngInventory owner) {
        this.channel = channel;
        this.inv = (T[])new IAEStack[slotCount];
        this.owner = owner;
    }

    public AeStackInventoryImpl(final IStorageChannel<T> channel, final int slotCount) {
        this(channel, slotCount, null);
    }

    @Override
    public int getSlotCount() {
        return inv.length;
    }

    @Override
    @Nullable
    public T getStack(final int slot) {
        return inv[slot];
    }

    @Override
    public void setStack(final int slot, @Nullable final T stack) {
        inv[slot] = stack;
        if (owner != null) {
            owner.saveChanges();
        }
    }

    @Override
    @Nonnull
    public Iterator<T> iterator() {
        return new ObjectArrayIterator<>(inv);
    }

    @Override
    public Stream<T> stream() {
        return Arrays.stream(inv);
    }

    public void writeToNbt(final NBTTagCompound tag) {
        final NBTTagList stacksTag = new NBTTagList();
        for (final T stack : inv) {
            if (stack == null) {
                stacksTag.appendTag(new NBTTagCompound());
            } else {
                final NBTTagCompound stackTag = new NBTTagCompound();
                stack.writeToNBT(stackTag);
                stacksTag.appendTag(stackTag);
            }
        }
        tag.setTag("Contents", stacksTag);
    }

    public void writeToNbt(final NBTTagCompound parentTag, final String key) {
        final NBTTagCompound tag = new NBTTagCompound();
        writeToNbt(tag);
        parentTag.setTag(key, tag);
    }

    public void readFromNbt(final NBTTagCompound tag) {
        final NBTTagList stacksTag = tag.getTagList("Contents", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < inv.length; i++) {
            final NBTTagCompound stackTag = stacksTag.getCompoundTagAt(i);
            inv[i] = stackTag.isEmpty() ? null : channel.createFromNBT(stackTag);
        }
    }

    public void readFromNbt(final NBTTagCompound parentTag, final String key) {
        readFromNbt(parentTag.getCompoundTag(key));
    }

}