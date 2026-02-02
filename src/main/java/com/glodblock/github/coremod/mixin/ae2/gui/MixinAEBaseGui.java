package com.glodblock.github.coremod.mixin.ae2.gui;

import appeng.client.gui.AEBaseGui;
import appeng.core.sync.packets.PacketInventoryAction;
import appeng.helpers.InventoryAction;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AEBaseGui.class)
public class MixinAEBaseGui {

    @WrapOperation(method = {"mouseClickMove", "handleMouseClick"}, at = @At(value = "NEW", target = "(Lappeng/helpers/InventoryAction;IJ)Lappeng/core/sync/packets/PacketInventoryAction;",ordinal = 0, remap = false))
    protected PacketInventoryAction writeMouseButton(final InventoryAction action, final int slot, final long id, final Operation<PacketInventoryAction> original) {
        return original.call(action, slot, action == InventoryAction.PICKUP_OR_SET_DOWN ? 0L : 1L);
    }
}