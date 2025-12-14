package com.glodblock.github.util;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEItemStack;
import com.mojang.authlib.GameProfile;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FluidPatternDetails implements ICraftingPatternDetails, Comparable<ICraftingPatternDetails> {

    private final ItemStack patternStack;
    private IAEItemStack patternStackAe;
    private IAEItemStack[] inputs = null, inputsCond = null, outputs = null, outputsCond = null;
    private int priority = 0;
    private String encoderName = "";
    private String encoderID = "";

    public FluidPatternDetails(final ItemStack stack) {
        this.patternStack = stack;
        this.patternStackAe = Objects.requireNonNull(AEItemStack.fromItemStack(stack)); // s2g
    }

    @Override
    public ItemStack getPattern() {
        return patternStack;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void setPriority(final int priority) {
        this.priority = priority;
    }

    @Override
    public boolean isCraftable() {
        return false;
    }

    @Override
    public boolean canSubstitute() {
        return false;
    }

    @Override
    public IAEItemStack[] getInputs() {
        return checkInitialized(inputs);
    }

    @Override
    public IAEItemStack[] getCondensedInputs() {
        return checkInitialized(inputsCond);
    }

    public boolean setInputs(final IAEItemStack[] inputs) {
        for (final IAEItemStack stack : inputs) { // see note at top of class
            if (stack == null) {
                return false;
            }
        }
        final IAEItemStack[] condensed = condenseStacks(inputs);
        if (condensed.length == 0) {
            return false;
        }
        this.inputs = inputs;
        this.inputsCond = condensed;
        return true;
    }

    public void setEncoder(final GameProfile profile) {
        this.encoderName = profile.getName();
        this.encoderID = profile.getId().toString();
    }

    @Override
    public IAEItemStack[] getOutputs() {
        return checkInitialized(outputs);
    }

    @Override
    public IAEItemStack[] getCondensedOutputs() {
        return checkInitialized(outputsCond);
    }

    public boolean setOutputs(final IAEItemStack[] outputs) {
        for (final IAEItemStack stack : outputs) { // see note at top of class
            if (stack == null) {
                return false;
            }
        }
        final IAEItemStack[] condensed = condenseStacks(outputs);
        if (condensed.length == 0) {
            return false;
        }
        this.outputs = outputs;
        this.outputsCond = condensed;
        return true;
    }

    public static IAEItemStack[] condenseStacks(final IAEItemStack[] stacks) {
        // AE item stacks are equivalent iff they are of the same item type (not accounting for stack size)
        // thus, it's not the semantically-correct definition of "equal" but it's useful for matching item types
        final Map<IAEItemStack, IAEItemStack> accMap = new HashMap<>();
        for (final IAEItemStack stack : stacks) {
            if (stack != null) {
                final IAEItemStack acc = accMap.get(stack);
                if (acc == null) {
                    accMap.put(stack, stack.copy());
                } else {
                    acc.add(stack);
                }
            }
        }
        return accMap.values().toArray(new IAEItemStack[0]);
    }

    @Override
    public ItemStack getOutput(final InventoryCrafting craftingInv, final World world) {
        throw new IllegalStateException("Not a crafting recipe!");
    }

    @Override
    public boolean isValidItemForSlot(final int slotIndex, final ItemStack itemStack, final World world) {
        throw new IllegalStateException("Not a crafting recipe!");
    }

    private static <T> T checkInitialized(@Nullable final T value) {
        if (value == null) {
            throw new IllegalStateException("Pattern is not initialized!");
        }
        return value;
    }

    @Override
    public int hashCode() {
        return patternStackAe.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        // ae2 null-checks the pattern stack here for some reason, but doesn't null-check in hashCode()
        // this is inconsistent, so i've just decided to assert non-null in the constructor, which is to say that
        // the pattern stack can never be null here
        return obj instanceof FluidPatternDetails && patternStackAe.equals(((FluidPatternDetails)obj).patternStackAe);
    }

    @Override
    public int compareTo(final ICraftingPatternDetails o) {
        return Integer.compare(o.getPriority(), this.priority);
    }

    public ItemStack writeToStack() {
        final NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("Inputs", writeStackArray(checkInitialized(inputs)));
        tag.setTag("Outputs", writeStackArray(checkInitialized(outputs)));
        //horrible shit.
        //I have to keep both to maintain the back capacity.
        tag.setTag("in", writeStackArray(checkInitialized(inputs)));
        tag.setTag("out", writeStackArray(checkInitialized(outputs)));
        //encoder info
        if (!this.encoderName.isEmpty()) {
            tag.setString("encoderName", this.encoderName);
        }
        if (!this.encoderID.isEmpty()) {
            tag.setString("encoderID", this.encoderID);
        }
        patternStack.setTagCompound(tag);
        patternStackAe = Objects.requireNonNull(AEItemStack.fromItemStack(patternStack));
        return patternStack;
    }

    public static NBTTagList writeStackArray(final IAEItemStack[] stacks) {
        final NBTTagList listTag = new NBTTagList();
        for (final IAEItemStack stack : stacks) {
            if (stack != null) {
                // see note at top of class
                final NBTTagCompound stackTag = new NBTTagCompound();
                stack.writeToNBT(stackTag);
                listTag.appendTag(stackTag);
            }
        }
        return listTag;
    }

    public boolean readFromStack() {
        if (!patternStack.hasTagCompound()) {
            return false;
        }
        final NBTTagCompound tag = Objects.requireNonNull(patternStack.getTagCompound());
        this.encoderName = tag.getString("encoderName");
        this.encoderName = tag.getString("encoderID");
        // may be possible to enter a partially-correct state if setInputs succeeds but setOutputs failed
        // but outside code should treat it as completely incorrect and not attempt to make calls
        return setInputs(readStackArray(tag.getTagList("Inputs", Constants.NBT.TAG_COMPOUND), 100))
                && setOutputs(readStackArray(tag.getTagList("Outputs", Constants.NBT.TAG_COMPOUND), 100));
    }

    public static IAEItemStack[] readStackArray(final NBTTagList listTag, final int maxCount) {
        // see note at top of class
        final IAEItemStack[] stacks = new IAEItemStack[Math.min(listTag.tagCount(), maxCount)];
        for (int i = 0; i < stacks.length; i++) {
            stacks[i] = AEItemStack.fromNBT(listTag.getCompoundTagAt(i));
        }
        return stacks;
    }

}