package com.glodblock.github.coremod.mixin.ae2;

import appeng.api.storage.data.IAEItemStack;
import appeng.crafting.CraftingTreeNode;
import com.glodblock.github.coremod.CoreModHooks;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = CraftingTreeNode.class, remap = false)
public class MixinCraftingTreeNode {

    @Shadow
    private int bytes;

    @Shadow
    @Final
    private IAEItemStack what;

    @WrapOperation(method = "request", at = {
        @At(value = "FIELD", target = "Lappeng/crafting/CraftingTreeNode;bytes:I", opcode = Opcodes.PUTFIELD, ordinal = 0),
        @At(value = "FIELD", target = "Lappeng/crafting/CraftingTreeNode;bytes:I", opcode = Opcodes.PUTFIELD, ordinal = 1),
        @At(value = "FIELD", target = "Lappeng/crafting/CraftingTreeNode;bytes:I", opcode = Opcodes.PUTFIELD, ordinal = 3),
        @At(value = "FIELD", target = "Lappeng/crafting/CraftingTreeNode;bytes:I", opcode = Opcodes.PUTFIELD, ordinal = 4)
    })
    public void changeFluidByte(final CraftingTreeNode instance, final int value, final Operation<Void> original, @Local(name = "available") final IAEItemStack available) {
        original.call(instance, (int) ((long) this.bytes + CoreModHooks.getCraftingByteCost(available)));
    }

    @WrapOperation(method = "request", at = @At(value = "FIELD", target = "Lappeng/crafting/CraftingTreeNode;bytes:I", opcode = Opcodes.PUTFIELD, ordinal = 2))
    public void changeFluidByteT(final CraftingTreeNode instance, final int value, final Operation<Void> original, @Local(name = "wat") final IAEItemStack available) {
        original.call(instance, (int) ((long) this.bytes + CoreModHooks.getCraftingByteCost(available)));
    }

    @WrapOperation(method = "request", at = @At(value = "FIELD", target = "Lappeng/crafting/CraftingTreeNode;bytes:I", opcode = Opcodes.PUTFIELD, ordinal = 5))
    public void changeFluidByte(final CraftingTreeNode instance, final int value, final Operation<Void> original, @Local(name = "l") final long l) {
        original.call(instance, (int) CoreModHooks.getCraftingByteCost(this.bytes, l, this.what));
    }
}