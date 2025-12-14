package com.glodblock.github.coremod.mixin.wct;

import appeng.api.storage.data.IAEItemStack;
import com.glodblock.github.coremod.CoreModHooks;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import p455w0rd.wct.client.gui.GuiCraftConfirm;
import p455w0rd.wct.client.gui.GuiCraftingCPU;

@Mixin(value = {GuiCraftingCPU.class, GuiCraftConfirm.class}, remap = false)
public class MixinGuiCraft {

    @WrapOperation(method = "drawFG", at = @At(value = "INVOKE", target = "Lappeng/api/storage/data/IAEItemStack;createItemStack()Lnet/minecraft/item/ItemStack;"))
    public ItemStack packItemStack(final IAEItemStack instance, final Operation<ItemStack> original) {
        return CoreModHooks.displayFluid(original.call(instance));
    }

}