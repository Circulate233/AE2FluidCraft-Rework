package com.glodblock.github.common.part;

import appeng.api.parts.IPartModel;
import appeng.items.parts.PartModels;
import appeng.parts.PartModel;
import appeng.util.Platform;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.component.DualityDualInterface;
import com.glodblock.github.common.component.DualityTrioInterface;
import com.glodblock.github.integration.mek.FCGasItems;
import com.glodblock.github.inventory.GuiType;
import com.glodblock.github.inventory.InventoryHandler;
import com.mekeng.github.common.me.duality.IGasInterfaceHost;
import com.mekeng.github.common.me.duality.impl.DualityGasInterface;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nonnull;

public class PartTrioInterface extends PartDualInterface implements IGasInterfaceHost {

    @PartModels
    public static ResourceLocation[] MODELS = new ResourceLocation[] {
            new ResourceLocation(FluidCraft.MODID, "part/trio_interface_base"),
            new ResourceLocation(FluidCraft.MODID, "part/trio_interface_on"),
            new ResourceLocation(FluidCraft.MODID, "part/trio_interface_off"),
            new ResourceLocation(FluidCraft.MODID, "part/trio_interface_has_channel")
    };

    public static final PartModel MODELS_OFF = new PartModel(MODELS[0], MODELS[2]);
    public static final PartModel MODELS_ON = new PartModel(MODELS[0], MODELS[1]);
    public static final PartModel MODELS_HAS_CHANNEL = new PartModel(MODELS[0], MODELS[3]);

    public PartTrioInterface(final ItemStack is) {
        super(is);
    }

    @SuppressWarnings("rawtypes")
    protected DualityDualInterface createDuality() {
        return new DualityTrioInterface<>(getProxy(), this);
    }

    @Override
    public DualityGasInterface getDualityGasInterface() {
        return ((DualityTrioInterface<?>) this.duality).getGasInterface();
    }

    @Override
    public GuiType getGuiType() {
        return GuiType.TRIO_ITEM_INTERFACE;
    }

    @Override
    public ItemStack getItemStackRepresentation() {
        return new ItemStack(FCGasItems.PART_TRIO_INTERFACE);
    }

    @Override
    public boolean onPartActivate(final EntityPlayer p, final EnumHand hand, final Vec3d pos) {
        if (Platform.isServer()) {
            final TileEntity tile = getTileEntity();
            InventoryHandler.openGui(p, tile.getWorld(), tile.getPos(), getSide().getFacing(), GuiType.TRIO_ITEM_INTERFACE);
        }
        return true;
    }

    @Nonnull
    @Override
    public IPartModel getStaticModels() {
        if (this.isActive() && this.isPowered()) {
            return MODELS_HAS_CHANNEL;
        } else if (this.isPowered()) {
            return MODELS_ON;
        } else {
            return MODELS_OFF;
        }
    }

}
