package com.glodblock.github.common.part;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.parts.IPartModel;
import appeng.core.sync.GuiBridge;
import appeng.items.misc.ItemEncodedPattern;
import appeng.items.parts.PartModels;
import appeng.parts.PartModel;
import appeng.parts.reporting.PartPatternTerminal;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.Platform;
import appeng.util.inv.InvOperation;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.item.ItemFluidCraftEncodedPattern;
import com.glodblock.github.common.item.ItemFluidEncodedPattern;
import com.glodblock.github.common.item.ItemLargeEncodedPattern;
import com.glodblock.github.interfaces.FCFluidPatternPart;
import com.glodblock.github.inventory.ExAppEngInternalInventory;
import com.glodblock.github.inventory.GuiType;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.util.FluidCraftingPatternDetails;
import com.glodblock.github.util.Util;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.List;

import static com.glodblock.github.util.Util.putPattern;

@Deprecated
public class PartFluidPatternTerminal extends PartPatternTerminal implements FCFluidPatternPart {

    private boolean combine = false;
    private boolean fluidFirst = false;

    @PartModels
    public static ResourceLocation[] MODELS = new ResourceLocation[]{
        new ResourceLocation(FluidCraft.MODID, "part/f_pattern_term_on"), // 0
        new ResourceLocation(FluidCraft.MODID, "part/f_pattern_term_off"), // 1
    };

    private static final IPartModel MODELS_ON = new PartModel(MODEL_BASE, MODELS[0], MODEL_STATUS_ON);
    private static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, MODELS[1], MODEL_STATUS_OFF);
    private static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, MODELS[0], MODEL_STATUS_HAS_CHANNEL);

    public PartFluidPatternTerminal(final ItemStack is) {
        super(is);
        final ExAppEngInternalInventory exCraft = new ExAppEngInternalInventory((AppEngInternalInventory) getInventoryByName("crafting"));
        final ExAppEngInternalInventory exOutput = new ExAppEngInternalInventory((AppEngInternalInventory) getInventoryByName("output"));
        this.crafting = exCraft;
        this.output = exOutput;
    }

    @Nonnull
    @Override
    public IPartModel getStaticModels() {
        return this.selectModel(MODELS_OFF, MODELS_ON, MODELS_HAS_CHANNEL);
    }

    @Override
    public void readFromNBT(final NBTTagCompound data) {
        super.readFromNBT(data);
        combine = data.getBoolean("combineMode");
        fluidFirst = data.getBoolean("fluidFirst");
    }

    public void setCombineMode(final boolean value) {
        this.combine = value;
    }

    public boolean getCombineMode() {
        return this.combine;
    }

    public void setFluidPlaceMode(final boolean value) {
        this.fluidFirst = value;
    }

    public boolean getFluidPlaceMode() {
        return this.fluidFirst;
    }

    @Override
    public void writeToNBT(final NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("combineMode", combine);
        data.setBoolean("fluidFirst", fluidFirst);
    }

    @Override
    public boolean onPartActivate(final EntityPlayer player, final EnumHand hand, final Vec3d pos) {
        final TileEntity te = this.getTile();
        final BlockPos tePos = te.getPos();
        if (Platform.isWrench(player, player.inventory.getCurrentItem(), tePos)) {
            return super.onPartActivate(player, hand, pos);
        }
        if (Platform.isServer()) {
            if (GuiBridge.GUI_PATTERN_TERMINAL.hasPermissions(te, tePos.getX(), tePos.getY(), tePos.getZ(), getSide(), player)) {
                InventoryHandler.openGui(player, te.getWorld(), tePos, getSide().getFacing(), GuiType.FLUID_PATTERN_TERMINAL);
            } else {
                Platform.openGUI(player, this.getHost().getTile(), this.getSide(), GuiBridge.GUI_ME);
            }
        }
        return true;
    }

    @Override
    public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc, final ItemStack removedStack,
                                  final ItemStack newStack) {
        if (slot == 1) {
            final ItemStack is = inv.getStackInSlot(1);
            if (!is.isEmpty() && (is.getItem() instanceof ItemFluidEncodedPattern || is.getItem() instanceof ItemFluidCraftEncodedPattern || is.getItem() instanceof ItemLargeEncodedPattern)) {
                final ItemEncodedPattern pattern = (ItemEncodedPattern) is.getItem();
                final ICraftingPatternDetails details = pattern.getPatternForItem(is, this.getHost().getTile().getWorld());
                if (details != null) {
                    this.setCraftingRecipe(details.isCraftable());
                    this.setSubstitution(details.canSubstitute());

                    Util.clearItemInventory(this.crafting);
                    Util.clearItemInventory(this.output);

                    if (details instanceof FluidCraftingPatternDetails) {
                        putPattern(this, ((FluidCraftingPatternDetails) details).getOriginInputs(), details.getOutputs());
                        this.setCraftingRecipe(true);
                    } else {
                        putPattern(this, details.getInputs(), details.getOutputs());
                    }
                }
                this.getHost().markForSave();
                return;
            }
        }
        super.onChangeInventory(inv, slot, mc, removedStack, newStack);
    }

    public void onChangeCrafting(final Int2ObjectMap<ItemStack[]> inputs, final List<ItemStack> outputs, final boolean combine) {
        Util.onPatternTerminalChangeCrafting(this, !this.craftingMode, inputs, outputs, combine);
    }

}