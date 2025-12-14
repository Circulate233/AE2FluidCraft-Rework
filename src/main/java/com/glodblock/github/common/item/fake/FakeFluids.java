package com.glodblock.github.common.item.fake;

import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.fluids.util.AEFluidStack;
import appeng.util.item.AEItemStack;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.loader.FCItems;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.Objects;

public final class FakeFluids {

    public static void init() {
        FakeItemRegister.registerHandler(
                ItemFluidDrop.class,
                new FakeItemHandler<FluidStack, IAEFluidStack>() {

                    @Override
                    public FluidStack getStack(final ItemStack stack) {
                        if (stack.isEmpty() || stack.getItem() != FCItems.FLUID_DROP || !stack.hasTagCompound()) {
                            return null;
                        }
                        final NBTTagCompound tag = Objects.requireNonNull(stack.getTagCompound());
                        if (!tag.hasKey("Fluid", Constants.NBT.TAG_STRING)) {
                            return null;
                        }
                        final Fluid fluid = FluidRegistry.getFluid(tag.getString("Fluid"));
                        if (fluid == null) {
                            return null;
                        }
                        final FluidStack fluidStack = new FluidStack(fluid, stack.getCount());
                        if (tag.hasKey("FluidTag", Constants.NBT.TAG_COMPOUND)) {
                            fluidStack.tag = tag.getCompoundTag("FluidTag");
                        }
                        return fluidStack;
                    }

                    @Override
                    public FluidStack getStack(@Nullable final IAEItemStack stack) {
                        return stack == null ? null : getStack(stack.createItemStack());
                    }

                    @Override
                    public IAEFluidStack getAEStack(final ItemStack stack) {
                        if (stack.isEmpty()) {
                            return null;
                        }
                        final IAEFluidStack fluidStack = AEFluidStack.fromFluidStack(getStack(stack));
                        if (fluidStack == null) {
                            return null;
                        }
                        fluidStack.setStackSize(stack.getCount());
                        return fluidStack;
                    }

                    @Override
                    public IAEFluidStack getAEStack(@Nullable final IAEItemStack stack) {
                        if (stack == null) {
                            return null;
                        }
                        final IAEFluidStack fluidStack = AEFluidStack.fromFluidStack(getStack(stack.createItemStack()));
                        if (fluidStack == null) {
                            return null;
                        }
                        fluidStack.setStackSize(stack.getStackSize());
                        return fluidStack;
                    }

                    @Override
                    public ItemStack packStack(final FluidStack fluid) {
                        if (fluid == null || fluid.amount <= 0) {
                            return ItemStack.EMPTY;
                        }
                        final ItemStack stack = new ItemStack(FCItems.FLUID_DROP, fluid.amount);
                        final NBTTagCompound tag = new NBTTagCompound();
                        tag.setString("Fluid", fluid.getFluid().getName());
                        if (fluid.tag != null) {
                            tag.setTag("FluidTag", fluid.tag);
                        }
                        stack.setTagCompound(tag);
                        return stack;
                    }

                    @Override
                    public ItemStack displayStack(final FluidStack target) {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public IAEItemStack packAEStack(final FluidStack fluid) {
                        if (fluid == null || fluid.amount <= 0) {
                            return null;
                        }
                        final IAEItemStack stack = DropLookup.lookup(fluid, f -> AEItemStack.fromItemStack(packStack(f)));
                        if (stack == null) {
                            return null;
                        }
                        stack.setStackSize(fluid.amount);
                        return stack;
                    }

                    @Override
                    public IAEItemStack packAEStackLong(final IAEFluidStack fluid) {
                        if (fluid == null || fluid.getStackSize() <= 0) {
                            return null;
                        }
                        final IAEItemStack stack = DropLookup.lookup(fluid.getFluidStack(), f -> AEItemStack.fromItemStack(packStack(f)));
                        if (stack == null) {
                            return null;
                        }
                        stack.setStackSize(fluid.getStackSize());
                        return stack;
                    }
                }
        );
        FakeItemRegister.registerHandler(
                ItemFluidPacket.class,
                new FakeItemHandler<FluidStack, IAEFluidStack>() {

                    @Override
                    public FluidStack getStack(final ItemStack stack) {
                        if (stack.isEmpty() || !stack.hasTagCompound()) {
                            return null;
                        }
                        final FluidStack fluid = FluidStack.loadFluidStackFromNBT(Objects.requireNonNull(stack.getTagCompound()).getCompoundTag("FluidStack"));
                        return (fluid != null && fluid.amount > 0) ? fluid : null;
                    }

                    @Override
                    public FluidStack getStack(@Nullable final IAEItemStack stack) {
                        return stack != null ? getStack(stack.createItemStack()) : null;
                    }

                    @Override
                    public IAEFluidStack getAEStack(final ItemStack stack) {
                        return getAEStack(AEItemStack.fromItemStack(stack));
                    }

                    @Override
                    public IAEFluidStack getAEStack(@Nullable final IAEItemStack stack) {
                        if (stack == null) {
                            return null;
                        }
                        return AEFluidStack.fromFluidStack(getStack(stack.createItemStack()));
                    }

                    @Override
                    public ItemStack packStack(final FluidStack fluid) {
                        if (fluid == null || fluid.amount == 0) {
                            return ItemStack.EMPTY;
                        }
                        final ItemStack stack = new ItemStack(FCItems.FLUID_PACKET);
                        final NBTTagCompound tag = new NBTTagCompound();
                        final NBTTagCompound fluidTag = new NBTTagCompound();
                        fluid.writeToNBT(fluidTag);
                        tag.setTag("FluidStack", fluidTag);
                        stack.setTagCompound(tag);
                        return stack;
                    }

                    @Override
                    public ItemStack displayStack(final FluidStack fluid) {
                        if (fluid == null) {
                            return ItemStack.EMPTY;
                        }
                        final FluidStack copy = fluid.copy();
                        copy.amount = 1000;
                        final ItemStack stack = new ItemStack(FCItems.FLUID_PACKET);
                        final NBTTagCompound tag = new NBTTagCompound();
                        final NBTTagCompound fluidTag = new NBTTagCompound();
                        copy.writeToNBT(fluidTag);
                        tag.setTag("FluidStack", fluidTag);
                        tag.setBoolean("DisplayOnly", true);
                        stack.setTagCompound(tag);
                        return stack;
                    }

                    @Override
                    public IAEItemStack packAEStack(final FluidStack target) {
                        return AEItemStack.fromItemStack(packStack(target));
                    }

                    @Override
                    public IAEItemStack packAEStackLong(final IAEFluidStack target) {
                        return AEItemStack.fromItemStack(packStack(target.getFluidStack()));
                    }
                }
        );
    }

    public static boolean isFluidFakeItem(final ItemStack stack) {
        return stack.getItem() == FCItems.FLUID_PACKET || stack.getItem() == FCItems.FLUID_DROP;
    }

    public static ItemStack packFluid2Drops(@Nullable final FluidStack stack) {
        return FakeItemRegister.packStack(stack, FCItems.FLUID_DROP);
    }

    public static IAEItemStack packFluid2AEDrops(@Nullable final FluidStack stack) {
        return FakeItemRegister.packAEStack(stack, FCItems.FLUID_DROP);
    }

    public static IAEItemStack packFluid2AEDrops(@Nullable final IAEFluidStack stack) {
        return FakeItemRegister.packAEStackLong(stack, FCItems.FLUID_DROP);
    }

    public static ItemStack packFluid2Packet(@Nullable final FluidStack stack) {
        return FakeItemRegister.packStack(stack, FCItems.FLUID_PACKET);
    }

    public static IAEItemStack packFluid2AEPacket(@Nullable final FluidStack stack) {
        return FakeItemRegister.packAEStack(stack, FCItems.FLUID_PACKET);
    }

    public static ItemStack displayFluid(@Nullable final FluidStack stack) {
        return FakeItemRegister.displayStack(stack, FCItems.FLUID_PACKET);
    }

}
