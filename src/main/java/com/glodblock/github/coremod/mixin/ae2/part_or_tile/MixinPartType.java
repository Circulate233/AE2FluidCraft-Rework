package com.glodblock.github.coremod.mixin.ae2.part_or_tile;

import appeng.api.parts.IPart;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.fluids.parts.PartFluidExportBus;
import appeng.integration.IntegrationType;
import appeng.items.parts.PartType;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Set;

@Mixin(value = PartType.class, remap = false)
public class MixinPartType {

    @Mutable
    @Shadow
    @Final
    private Class<? extends IPart> myPart;

    @Inject(method = "<init>(Ljava/lang/String;IILjava/lang/String;Ljava/util/Set;Ljava/util/Set;Ljava/lang/Class;Lappeng/core/localization/GuiText;Ljava/lang/String;)V", at = @At("TAIL"))
    private void partFluidExportBusI(final String name, final int o, final int baseMetaValue, final String itemModel, final Set<AEFeature> features, final Set<IntegrationType> integrations, final Class<? extends IPart> c, final GuiText par8, final String par9, final CallbackInfo ci) {
        if (c == PartFluidExportBus.class) {
            this.myPart = com.glodblock.github.common.part.PartFluidExportBus.class;
        }
    }

    @WrapOperation(method = "<init>(Ljava/lang/String;IILjava/lang/String;Ljava/util/Set;Ljava/util/Set;Ljava/lang/Class;Lappeng/core/localization/GuiText;Ljava/lang/String;)V", at = @At(value = "INVOKE", target = "Lappeng/items/parts/PartModelsHelper;createModels(Ljava/lang/Class;)Ljava/util/List;"))
    public List<ResourceLocation> partFluidExportBusR(final Class<?> value, final Operation<List<ResourceLocation>> original) {
        if (value == PartFluidExportBus.class) {
            return original.call(com.glodblock.github.common.part.PartFluidExportBus.class);
        }
        return original.call(value);
    }

}