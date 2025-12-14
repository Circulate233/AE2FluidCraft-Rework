package com.glodblock.github.integration.jei;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.container.ContainerFluidPatternEncoder;
import com.glodblock.github.network.CPacketLoadPattern;
import com.glodblock.github.util.NameConst;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.transfer.RecipeTransferErrorTooltip;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FluidPatternEncoderRecipeTransferHandler implements IRecipeTransferHandler<ContainerFluidPatternEncoder> {
    @Override
    @Nonnull
    public Class<ContainerFluidPatternEncoder> getContainerClass() {
        return ContainerFluidPatternEncoder.class;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(@Nonnull final ContainerFluidPatternEncoder container, final IRecipeLayout recipeLayout,
                                               @Nonnull final EntityPlayer player, final boolean maxTransfer, final boolean doTransfer) {
        if (recipeLayout.getRecipeCategory().getUid().equals(VanillaRecipeCategoryUid.CRAFTING)) {
            return new RecipeTransferErrorTooltip(I18n.format(NameConst.TT_PROCESSING_RECIPE_ONLY));
        }
        if (doTransfer) {
            final RecipeTransferBuilder transfer = new RecipeTransferBuilder(
                    recipeLayout)
                    .clearEmptySlot(true)
                    .putFluidFirst(false)
                    .build();
            FluidCraft.proxy.netHandler.sendToServer(new CPacketLoadPattern(transfer.getInput(), transfer.getOutput(), false));
        }
        return null;
    }

}
