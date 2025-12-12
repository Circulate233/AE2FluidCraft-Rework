package com.glodblock.github.util;

import appeng.api.definitions.IItemDefinition;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.container.ContainerOpenContext;
import appeng.container.implementations.CraftingCPURecord;
import appeng.fluids.helper.DualityFluidInterface;
import appeng.helpers.DualityInterface;
import appeng.me.helpers.AENetworkProxy;
import appeng.recipes.game.DisassembleRecipe;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.inv.ItemSlot;
import appeng.util.inv.filter.IAEItemFilter;
import com.mekeng.github.common.me.duality.impl.DualityGasInterface;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.world.World;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

public class Ae2Reflect {

    private static final MethodHandle mItemSlot_setExtractable;
    private static final MethodHandle fGetDisassembleRecipe_nonCellMappings;
    private static final MethodHandle fGetInventory_container;
    private static final MethodHandle fGetDualInterface_gridProxy;
    private static final MethodHandle fGetDualityFluidInterface_gridProxy;
    private static final MethodHandle fGetAppEngInternalInventory_filter;
    private static final MethodHandle fGetContainerOpenContext_w;
    private static final MethodHandle fGetContainerOpenContext_x;
    private static final MethodHandle fGetContainerOpenContext_y;
    private static final MethodHandle fGetContainerOpenContext_z;
    private static final MethodHandle fGetCraftingCPURecord_cpu;
    private static final MethodHandle fGetDualityGasInterface_gridProxy;

    static {
        try {
            mItemSlot_setExtractable = reflectMethodHandle(ItemSlot.class, "setExtractable", boolean.class);
            fGetInventory_container = reflectFieldGetter(InventoryCrafting.class, "eventHandler", "field_70465_c", "c");
            fGetDisassembleRecipe_nonCellMappings = reflectFieldGetter(DisassembleRecipe.class, "nonCellMappings");
            fGetDualInterface_gridProxy = reflectFieldGetter(DualityInterface.class, "gridProxy");
            fGetDualityFluidInterface_gridProxy = reflectFieldGetter(DualityFluidInterface.class, "gridProxy");
            fGetAppEngInternalInventory_filter = reflectFieldGetter(AppEngInternalInventory.class, "filter");
            fGetContainerOpenContext_w = reflectFieldGetter(ContainerOpenContext.class, "w");
            fGetContainerOpenContext_x = reflectFieldGetter(ContainerOpenContext.class, "x");
            fGetContainerOpenContext_y = reflectFieldGetter(ContainerOpenContext.class, "y");
            fGetContainerOpenContext_z = reflectFieldGetter(ContainerOpenContext.class, "z");
            fGetCraftingCPURecord_cpu = reflectFieldGetter(CraftingCPURecord.class, "cpu");
            if (ModAndClassUtil.GAS) {
                fGetDualityGasInterface_gridProxy = reflectFieldGetter(DualityGasInterface.class, "gridProxy");
            } else {
                fGetDualityGasInterface_gridProxy = null;
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize AE2 reflection hacks!", e);
        }
    }

    public static MethodHandle reflectConstructor(Class<?> owner, Class<?>... paramTypes) throws NoSuchMethodException, IllegalAccessException {
        Constructor<?> constructor = owner.getDeclaredConstructor(paramTypes);
        constructor.setAccessible(true);
        return MethodHandles.lookup().unreflectConstructor(constructor);
    }

    public static MethodHandle reflectMethodHandle(Class<?> owner, String name, Class<?>... paramTypes) throws NoSuchMethodException, IllegalAccessException {
        return reflectMethodHandle(owner, new String[]{name}, paramTypes);
    }

    public static MethodHandle reflectMethodHandle(Class<?> owner, String[] names, Class<?>... paramTypes) throws NoSuchMethodException, IllegalAccessException {
        return MethodHandles.lookup().unreflect(reflectMethod(owner, names, paramTypes));
    }

    public static Method reflectMethod(Class<?> owner, String name, Class<?>... paramTypes) throws NoSuchMethodException {
        return reflectMethod(owner, new String[]{name}, paramTypes);
    }

    @SuppressWarnings("all")
    public static Method reflectMethod(Class<?> owner, String[] names, Class<?>... paramTypes) throws NoSuchMethodException {
        Method m = null;
        for (String name : names) {
            try {
                m = owner.getDeclaredMethod(name, paramTypes);
                if (m != null) break;
            }
            catch (NoSuchMethodException ignore) {
            }
        }
        if (m == null) throw new NoSuchMethodException("Can't find field from " + Arrays.toString(names));
        m.setAccessible(true);
        return m;
    }

    public static MethodHandle reflectFieldGetter(Class<?> owner, String ...names) throws IllegalAccessException, NoSuchFieldException {
        return MethodHandles.lookup().unreflectGetter(reflectField(owner, names));
    }

    public static MethodHandle reflectFieldSetter(Class<?> owner, String ...names) throws IllegalAccessException, NoSuchFieldException {
        return MethodHandles.lookup().unreflectSetter(reflectField(owner, names));
    }

    @SuppressWarnings("all")
    public static Field reflectField(Class<?> owner, String ...names) throws NoSuchFieldException {
        Field f = null;
        for (String name : names) {
            try {
                f = owner.getDeclaredField(name);
                if (f != null) break;
            }
            catch (NoSuchFieldException ignore) {
            }
        }
        if (f == null) throw new NoSuchFieldException("Can't find field from " + Arrays.toString(names));
        f.setAccessible(true);
        return f;
    }

    public static <T> T readField(Object owner, Field field) {
        try {
            return (T)field.get(owner);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read field: " + field);
        }
    }

    public static <T> T readField(Object owner, MethodHandle field) {
        try {
            if (owner == null) {
                return (T)field.invoke();
            } else {
                return (T)field.invoke(owner);
            }
        } catch (Throwable e) {
            throw new IllegalStateException("Failed to read field: " + field);
        }
    }

    public static void writeField(Object owner, Field field, Object value) {
        try {
            field.set(owner, value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write field: " + field);
        }
    }

    public static void writeField(Object owner, MethodHandle field, Object value) {
        try {
            if (owner == null) {
                field.invoke(value);
            } else {
                field.invoke(owner, value);
            }
        } catch (Throwable e) {
            throw new IllegalStateException("Failed to write field: " + field);
        }
    }

    public static Container getCraftContainer(InventoryCrafting inv) {
        return Ae2Reflect.readField(inv, fGetInventory_container);
    }

    public static void setItemSlotExtractable(ItemSlot slot, boolean extractable) {
        try {
            mItemSlot_setExtractable.invoke(slot, extractable);
        } catch (Throwable e) {
            throw new IllegalStateException("Failed to invoke method: " + mItemSlot_setExtractable, e);
        }
    }

    public static Map<IItemDefinition, IItemDefinition> getDisassemblyNonCellMap(DisassembleRecipe recipe) {
        return readField(recipe, fGetDisassembleRecipe_nonCellMappings);
    }

    public static AENetworkProxy getInterfaceProxy(DualityInterface owner) {
        return readField(owner, fGetDualInterface_gridProxy);
    }

    public static AENetworkProxy getInterfaceProxy(DualityFluidInterface owner) {
        return readField(owner, fGetDualityFluidInterface_gridProxy);
    }

    public static IAEItemFilter getInventoryFilter(AppEngInternalInventory owner) {
        return readField(owner, fGetAppEngInternalInventory_filter);
    }

    public static World getContextWorld(ContainerOpenContext owner) {
        return readField(owner, fGetContainerOpenContext_w);
    }

    public static int getContextX(ContainerOpenContext owner) {
        return readField(owner, fGetContainerOpenContext_x);
    }

    public static int getContextY(ContainerOpenContext owner) {
        return readField(owner, fGetContainerOpenContext_y);
    }

    public static int getContextZ(ContainerOpenContext owner) {
        return readField(owner, fGetContainerOpenContext_z);
    }

    public static ICraftingCPU getCraftingCPU(CraftingCPURecord owner) {
        return readField(owner, fGetCraftingCPURecord_cpu);
    }

    public static AENetworkProxy getGasInterfaceGrid(Object owner) {
        return readField(owner, fGetDualityGasInterface_gridProxy);
    }

}