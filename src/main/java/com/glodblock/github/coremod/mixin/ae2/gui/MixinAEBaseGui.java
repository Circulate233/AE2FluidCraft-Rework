package com.glodblock.github.coremod.mixin.ae2.gui;

import appeng.client.gui.AEBaseGui;
import appeng.core.sync.packets.PacketInventoryAction;
import appeng.helpers.InventoryAction;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AEBaseGui.class)
public class MixinAEBaseGui {

    @WrapOperation(method = "handleMouseClick", at = @At(value = "NEW", target = "(Lappeng/helpers/InventoryAction;IJ)Lappeng/core/sync/packets/PacketInventoryAction;", ordinal = 0, remap = false))
    protected PacketInventoryAction handleMouseClick(InventoryAction action, int slot, long id, Operation<PacketInventoryAction> original) {
        var newid = (long) Mouse.getEventButton();
        if (newid == -1) {
            newid = Mouse.isButtonDown(0) ? 0 : 1;
        }
        return original.call(action, slot, newid);
    }

    @WrapOperation(method = "mouseClickMove", at = @At(value = "NEW", target = "(Lappeng/helpers/InventoryAction;IJ)Lappeng/core/sync/packets/PacketInventoryAction;", ordinal = 0, remap = false))
    protected PacketInventoryAction mouseClickMove(InventoryAction action, int slot, long id, Operation<PacketInventoryAction> original) {
        var newid = (long) Mouse.getEventButton();
        if (newid == -1) {
            newid = Mouse.isButtonDown(0) ? 0 : 1;
        }
        return original.call(action, slot, newid);
    }
}