package com.glodblock.github.integration.mek;

import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEItemStack;
import com.glodblock.github.common.item.ItemGasDrop;
import com.glodblock.github.common.item.ItemGasPacket;
import com.glodblock.github.common.item.fake.FakeItemHandler;
import com.glodblock.github.common.item.fake.FakeItemRegister;
import com.mekeng.github.common.me.data.IAEGasStack;
import com.mekeng.github.common.me.data.impl.AEGasStack;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.Objects;

public class FakeGases {

    public static void init() {
        FakeItemRegister.registerHandler(
                ItemGasDrop.class,
                new FakeItemHandler<GasStack, IAEGasStack>() {

                    @Override
                    public GasStack getStack(final ItemStack stack) {
                        if (stack.isEmpty() || stack.getItem() != FCGasItems.GAS_DROP || !stack.hasTagCompound()) {
                            return null;
                        }
                        final NBTTagCompound tag = Objects.requireNonNull(stack.getTagCompound());
                        if (!tag.hasKey("Gas", Constants.NBT.TAG_STRING)) {
                            return null;
                        }
                        final Gas gas = GasRegistry.getGas(tag.getString("Gas"));
                        if (gas == null) {
                            return null;
                        }
                        return new GasStack(gas, stack.getCount());
                    }

                    @Override
                    public GasStack getStack(@Nullable final IAEItemStack stack) {
                        return stack == null ? null : getStack(stack.createItemStack());
                    }

                    @Override
                    public IAEGasStack getAEStack(final ItemStack stack) {
                        return getAEStack(AEItemStack.fromItemStack(stack));
                    }

                    @Override
                    public IAEGasStack getAEStack(@Nullable final IAEItemStack stack) {
                        if (stack == null) {
                            return null;
                        }
                        final GasStack gas = getStack(stack.createItemStack());
                        if (gas == null || gas.getGas() == null) {
                            return null;
                        }
                        final IAEGasStack gasStack = AEGasStack.of(gas);
                        if (gasStack == null) return null;
                        gasStack.setStackSize(stack.getStackSize());
                        return gasStack;
                    }

                    @Override
                    public ItemStack packStack(final GasStack gas) {
                        if (gas == null || gas.amount <= 0) {
                            return ItemStack.EMPTY;
                        }
                        final ItemStack stack = new ItemStack(FCGasItems.GAS_DROP, gas.amount);
                        final NBTTagCompound tag = new NBTTagCompound();
                        tag.setString("Gas", gas.getGas().getName());
                        stack.setTagCompound(tag);
                        return stack;
                    }

                    @Override
                    public ItemStack displayStack(final GasStack target) {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public IAEItemStack packAEStack(final GasStack gas) {
                        if (gas == null || gas.amount <= 0) {
                            return null;
                        }
                        final IAEItemStack stack = AEItemStack.fromItemStack(packStack(gas));
                        if (stack == null) {
                            return null;
                        }
                        stack.setStackSize(gas.amount);
                        return stack;
                    }

                    @Override
                    public IAEItemStack packAEStackLong(final IAEGasStack gas) {
                        if (gas == null || gas.getStackSize() <= 0) {
                            return null;
                        }
                        final IAEItemStack stack = AEItemStack.fromItemStack(packStack(new GasStack(gas.getGas(), 1)));
                        if (stack == null) {
                            return null;
                        }
                        stack.setStackSize(gas.getStackSize());
                        return stack;
                    }
                }
        );
        FakeItemRegister.registerHandler(
                ItemGasPacket.class,
                new FakeItemHandler<GasStack, IAEGasStack>() {
                    @Override
                    public GasStack getStack(final ItemStack stack) {
                        if (stack.isEmpty() || !stack.hasTagCompound()) {
                            return null;
                        }
                        final GasStack gas = GasStack.readFromNBT(Objects.requireNonNull(stack.getTagCompound()).getCompoundTag("GasStack"));
                        return (gas != null && gas.amount > 0) ? gas : null;
                    }

                    @Override
                    public GasStack getStack(@Nullable final IAEItemStack stack) {
                        return stack != null ? getStack(stack.createItemStack()) : null;
                    }

                    @Override
                    public IAEGasStack getAEStack(final ItemStack stack) {
                        return getAEStack(AEItemStack.fromItemStack(stack));
                    }

                    @Override
                    public IAEGasStack getAEStack(@Nullable final IAEItemStack stack) {
                        if (stack == null) {
                            return null;
                        }
                        final GasStack gas = getStack(stack.createItemStack());
                        if (gas == null || gas.getGas() == null) {
                            return null;
                        }
                        return AEGasStack.of(gas);
                    }

                    @Override
                    public ItemStack packStack(final GasStack gas) {
                        if (gas == null || gas.amount == 0) {
                            return ItemStack.EMPTY;
                        }
                        final ItemStack stack = new ItemStack(FCGasItems.GAS_PACKET);
                        final NBTTagCompound tag = new NBTTagCompound();
                        final NBTTagCompound fluidTag = new NBTTagCompound();
                        gas.write(fluidTag);
                        tag.setTag("GasStack", fluidTag);
                        stack.setTagCompound(tag);
                        return stack;
                    }

                    @Override
                    public ItemStack displayStack(final GasStack gas) {
                        if (gas == null) {
                            return ItemStack.EMPTY;
                        }
                        final GasStack copy = gas.copy();
                        copy.amount = 1000;
                        final ItemStack stack = new ItemStack(FCGasItems.GAS_PACKET);
                        final NBTTagCompound tag = new NBTTagCompound();
                        final NBTTagCompound fluidTag = new NBTTagCompound();
                        copy.write(fluidTag);
                        tag.setTag("GasStack", fluidTag);
                        tag.setBoolean("DisplayOnly", true);
                        stack.setTagCompound(tag);
                        return stack;
                    }

                    @Override
                    public IAEItemStack packAEStack(final GasStack target) {
                        return AEItemStack.fromItemStack(packStack(target));
                    }

                    @Override
                    public IAEItemStack packAEStackLong(final IAEGasStack target) {
                        return AEItemStack.fromItemStack(packStack(target.getGasStack()));
                    }
                }
        );
    }

    public static boolean isGasFakeItem(final ItemStack stack) {
        return stack.getItem() == FCGasItems.GAS_DROP || stack.getItem() == FCGasItems.GAS_PACKET;
    }

    public static ItemStack packGas2Drops(@Nullable final GasStack stack) {
        return FakeItemRegister.packStack(stack, FCGasItems.GAS_DROP);
    }

    public static IAEItemStack packGas2AEDrops(@Nullable final GasStack stack) {
        return FakeItemRegister.packAEStack(stack, FCGasItems.GAS_DROP);
    }

    public static IAEItemStack packGas2AEDrops(@Nullable final IAEGasStack stack) {
        return FakeItemRegister.packAEStackLong(stack, FCGasItems.GAS_DROP);
    }

    public static ItemStack packGas2Packet(@Nullable final GasStack stack) {
        return FakeItemRegister.packStack(stack, FCGasItems.GAS_PACKET);
    }

    public static IAEItemStack packGas2AEPacket(@Nullable final GasStack stack) {
        return FakeItemRegister.packAEStack(stack, FCGasItems.GAS_PACKET);
    }

    public static ItemStack displayGas(@Nullable final GasStack stack) {
        return FakeItemRegister.displayStack(stack, FCGasItems.GAS_PACKET);
    }

}
