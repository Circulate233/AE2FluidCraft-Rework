package com.glodblock.github.coremod.mixin.packagedauto;

import appeng.api.storage.data.IAEItemStack;
import com.glodblock.github.coremod.CoreModHooks;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import thelm.packagedauto.integration.appeng.recipe.PackageCraftingPatternHelper;

@Mixin(value = PackageCraftingPatternHelper.class, remap = false)
public class MixinPackageCraftingPatternHelper {

    @WrapOperation(method = "<init>", at = @At(value = "FIELD", target = "Lthelm/packagedauto/integration/appeng/recipe/PackageCraftingPatternHelper;inputs:[Lappeng/api/storage/data/IAEItemStack;", opcode = Opcodes.PUTFIELD))
    public void packInputs(final PackageCraftingPatternHelper instance, final IAEItemStack[] value, final Operation<Void> original) {
        original.call(instance, CoreModHooks.flattenFluidPackets(value));
    }

}