package com.glodblock.github.coremod.mixin.ae2;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.crafting.MECraftingInventory;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.helpers.MachineSource;
import com.glodblock.github.common.item.fake.FakeItemRegister;
import com.glodblock.github.coremod.CoreModHooks;
import com.glodblock.github.integration.mek.FCGasItems;
import com.glodblock.github.loader.FCItems;
import com.glodblock.github.util.ModAndClassUtil;
import com.glodblock.github.util.Util;
import com.google.common.base.Preconditions;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = CraftingCPUCluster.class, remap = false)
public abstract class MixinCraftingCPUCluster {

    @Shadow
    private MECraftingInventory inventory;

    @Shadow
    private MachineSource machineSrc;
    @Shadow
    private boolean isComplete;

    @Shadow
    protected abstract void postChange(IAEItemStack diff, IActionSource src);

    @Shadow
    protected abstract void markDirty();

    @Shadow
    protected abstract IGrid getGrid();

    @Redirect(method = "executeCrafting", at = @At(value = "INVOKE", target = "Lappeng/api/storage/data/IAEItemStack;getStackSize()J", ordinal = 0))
    private long getFluidSize(IAEItemStack instance) {
        if (instance.getDefinition() != null && !instance.getDefinition().isEmpty()) {
            if (instance.getDefinition().getItem() == FCItems.FLUID_DROP) {
                return (long) Math.max(instance.getStackSize() / 1000D, 1);
            } else if (ModAndClassUtil.GAS && instance.getDefinition().getItem() == FCGasItems.GAS_DROP) {
                return (long) Math.max(instance.getStackSize() / 4000D, 1);
            }
        }
        return instance.getStackSize();
    }

    @WrapOperation(
        method = "executeCrafting",
        at = @At(
            value = "NEW",
            target = "net/minecraft/inventory/InventoryCrafting",
            remap = true
        )
    )
    private InventoryCrafting wrapInventoryCrafting(Container container, int i, int ii, Operation<InventoryCrafting> original) {
        return CoreModHooks.wrapCraftingBuffer(container, i, ii);
    }

    @Redirect(
        method = "executeCrafting",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/inventory/InventoryCrafting;getStackInSlot(I)Lnet/minecraft/item/ItemStack;",
            remap = true
        )
    )
    private ItemStack redirectGetStackInSlot(InventoryCrafting inventory, int slot) {
        return CoreModHooks.removeFluidPackets(inventory, slot);
    }

    @WrapOperation(
        method = "executeCrafting",
        at = @At(
            value = "INVOKE",
            target = "Lappeng/crafting/MECraftingInventory;injectItems(Lappeng/api/storage/data/IAEItemStack;Lappeng/api/config/Actionable;Lappeng/api/networking/security/IActionSource;)Lappeng/api/storage/data/IAEItemStack;"
        )
    )
    private IAEItemStack wrapFromItemStack(MECraftingInventory instance, IAEItemStack input, Actionable mode, IActionSource src, Operation<IAEItemStack> original) {
        return original.call(instance, CoreModHooks.wrapFluidPacketStack(input), mode, src);
    }

    @Redirect(
        method = {"cancel", "updateCraftingLogic"},
        at = @At(
            value = "INVOKE",
            target = "Lappeng/me/cluster/implementations/CraftingCPUCluster;storeItems()V"
        )
    )
    private void redirectStoreItems(CraftingCPUCluster instance) {
        Preconditions.checkState(this.isComplete, "CPU should be complete to prevent re-insertion when dumping items");
        final IGrid g = this.getGrid();

        if (g == null) {
            return;
        }

        final IStorageGrid sg = g.getCache(IStorageGrid.class);
        final IMEInventory<IAEItemStack> ii = sg.getInventory(Util.getItemChannel());
        final IMEInventory<IAEFluidStack> jj = sg.getInventory(Util.getFluidChannel());
        final IMEInventory kk;
        final MECraftingInventory inventory = this.inventory;
        if (ModAndClassUtil.GAS) {
            kk = sg.getInventory(Util.getGasChannel());
        } else {
            kk = null;
        }

        for (IAEItemStack is : inventory.getItemList()) {
            this.postChange(is, this.machineSrc);

            if (is.getItem() == FCItems.FLUID_DROP) {
                IAEFluidStack drop = FakeItemRegister.getAEStack(is);
                IAEFluidStack fluidRemainder = jj.injectItems(drop, Actionable.MODULATE, this.machineSrc);
                if (fluidRemainder != null) {
                    is.setStackSize(fluidRemainder.getStackSize());
                } else {
                    is.reset();
                }
            } else if (ModAndClassUtil.GAS && is.getItem() == FCGasItems.GAS_DROP && kk != null) {
                IAEStack drop = FakeItemRegister.getAEStack(is);
                IAEStack gasRemainder = kk.injectItems(drop, Actionable.MODULATE, this.machineSrc);
                if (gasRemainder != null) {
                    is.setStackSize(gasRemainder.getStackSize());
                } else {
                    is.reset();
                }
            } else {
                IAEItemStack remainder = ii.injectItems(is.copy(), Actionable.MODULATE, this.machineSrc);
                if (remainder != null) {
                    is.setStackSize(remainder.getStackSize());
                } else {
                    is.reset();
                }
            }
        }

        if (inventory.getItemList().isEmpty()) {
            this.inventory = new MECraftingInventory();
        }

        this.markDirty();
    }
}