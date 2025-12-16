package com.glodblock.github.coremod.mixin.ae2ctl;

import com.glodblock.github.common.item.fake.FakeItemRegister;
import com.glodblock.github.integration.mek.FCGasItems;
import com.glodblock.github.loader.FCItems;
import com.glodblock.github.util.ModAndClassUtil;
import com.glodblock.github.util.Util;
import github.kasuminova.ae2ctl.client.gui.widget.impl.craftingtree.TreeNode;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import mekanism.api.gas.GasStack;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(value = TreeNode.class, remap = false)
public class MixinTreeNode {

    @Redirect(method = "getHoverTooltips", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;getItemToolTip(Lnet/minecraft/item/ItemStack;)Ljava/util/List;"))
    private List<String> getFluidTooltips(final GuiScreen instance, ItemStack stack) {
        if (stack.getItem() == FCItems.FLUID_DROP) {
            stack = stack.copy();
            stack.setCount(1);
            final FluidStack fluidStack = FakeItemRegister.getStack(stack);
            if (fluidStack != null) {
                final String modName = TextFormatting.BLUE.toString() + TextFormatting.ITALIC + Loader.instance().getIndexedModList().get(FluidRegistry.getModId(fluidStack)).getName();
                final List<String> list = new ObjectArrayList<>(4);
                list.add(fluidStack.getLocalizedName());
                list.add(modName);
                return list;
            }
        } else if (ModAndClassUtil.GAS && stack.getItem() == FCGasItems.GAS_DROP) {
            final var list = fc$getGasTooltips(stack);
            if (!list.isEmpty()) return list;
        }
        return instance.getItemToolTip(stack);
    }

    @Unique
    @Optional.Method(modid = "mekeng")
    private List<String> fc$getGasTooltips(ItemStack stack) {
        if (stack.getItem() == FCGasItems.GAS_DROP) {
            stack = stack.copy();
            stack.setCount(1);
            final GasStack gasStack = FakeItemRegister.getStack(stack);
            if (gasStack != null) {
                final List<String> list = new ObjectArrayList<>(4);
                list.add(gasStack.getGas().getLocalizedName());
                list.add(Util.mekModName);
                return list;
            }
        }
        return ObjectLists.emptyList();
    }

}
