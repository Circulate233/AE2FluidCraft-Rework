package com.glodblock.github.coremod.mixin.jei;

import appeng.api.implementations.IUpgradeableHost;
import appeng.client.gui.implementations.GuiCellWorkbench;
import appeng.client.gui.implementations.GuiUpgradeable;
import appeng.container.implementations.ContainerCellWorkbench;
import appeng.container.slot.SlotFake;
import appeng.items.storage.ItemViewCell;
import appeng.tile.misc.TileCellWorkbench;
import com.glodblock.github.integration.jei.FluidPacketTarget;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mezz.jei.api.gui.IGhostIngredientHandler;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(value = GuiCellWorkbench.class, remap = false)
public abstract class MixinGuiCellWorkbench extends GuiUpgradeable {

    @Shadow
    @Final
    private ContainerCellWorkbench workbench;

    public MixinGuiCellWorkbench(InventoryPlayer inventoryPlayer, IUpgradeableHost te) {
        super(inventoryPlayer, te);
    }

    @Intrinsic
    public List<IGhostIngredientHandler.Target<?>> getPhantomTargets(final Object ingredient) {
        if (((TileCellWorkbench) this.workbench.getTileEntity()).getCell() instanceof ItemViewCell) {
            final List<IGhostIngredientHandler.Target<?>> targets = new ObjectArrayList<>();
            for (final Slot slot : this.inventorySlots.inventorySlots) {
                if (slot instanceof SlotFake) {
                    final IGhostIngredientHandler.Target<?> target = new FluidPacketTarget(getGuiLeft(), getGuiTop(), slot);
                    targets.add(target);
                    mapTargetSlot.putIfAbsent(target, slot);
                }
            }
            return targets;
        } else return super.getPhantomTargets(ingredient);
    }
}
