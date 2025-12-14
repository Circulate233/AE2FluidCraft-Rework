package com.glodblock.github.coremod.mixin.packagedauto;

import appeng.api.storage.data.IAEItemStack;
import com.glodblock.github.coremod.CoreModHooks;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import thelm.packagedauto.integration.appeng.recipe.RecipeCraftingPatternHelper;

@Mixin(value = RecipeCraftingPatternHelper.class, remap = false)
public class MixinRecipeCraftingPatternHelper {

    @WrapOperation(method = "<init>", at = @At(value = "FIELD", target = "Lthelm/packagedauto/integration/appeng/recipe/RecipeCraftingPatternHelper;outputs:[Lappeng/api/storage/data/IAEItemStack;", opcode = Opcodes.PUTFIELD))
    public void packInputs(final RecipeCraftingPatternHelper instance, final IAEItemStack[] value, final Operation<Void> original) {
        original.call(instance, CoreModHooks.flattenFluidPackets(value));
    }

}
