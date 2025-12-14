package com.glodblock.github.common.block;

import appeng.block.AEBaseTileBlock;
import com.glodblock.github.common.tile.TileGeneralLevelMaintainer;
import com.glodblock.github.inventory.GuiType;
import com.glodblock.github.inventory.InventoryHandler;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public class BlockGeneralLevelMaintainer extends AEBaseTileBlock {

    public static final PropertyDirection facingProperty = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

    public BlockGeneralLevelMaintainer() {
        super(Material.IRON);
        setTileEntity(TileGeneralLevelMaintainer.class);
    }

    @Override
    public boolean onBlockActivated(final World world, final BlockPos pos, final IBlockState state, final EntityPlayer player, final EnumHand hand,
                                    final EnumFacing facing, final float hitX, final float hitY, final float hitZ) {
        if (player.isSneaking()) {
            return super.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ);
        }
        final TileGeneralLevelMaintainer tile = getTileEntity(world, pos);
        if (tile != null) {
            if (!world.isRemote) {
                InventoryHandler.openGui(player, world, pos, facing, GuiType.GENERAL_LEVEL_MAINTAINER);
            }
            return true;
        }

        return super.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new IProperty[]{facingProperty}, new IUnlistedProperty[]{});
    }

    @Override
    public IBlockState getActualState(final IBlockState state, final IBlockAccess worldIn, final BlockPos pos) {
        final TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof TileGeneralLevelMaintainer) {
            if (((TileGeneralLevelMaintainer) tileEntity).facing != null) {
                return state.withProperty(facingProperty, ((TileGeneralLevelMaintainer) tileEntity).facing);
            }
        }
        return state;
    }

    @Override
    public void onBlockPlacedBy(final World w, final BlockPos pos, final IBlockState state, final EntityLivingBase placer, final ItemStack is) {
        super.onBlockPlacedBy(w, pos, state, placer, is);
        final TileEntity tileEntity = w.getTileEntity(pos);
        if (tileEntity instanceof TileGeneralLevelMaintainer) {
            ((TileGeneralLevelMaintainer) tileEntity).facing = placer.getHorizontalFacing().getOpposite();
        }
    }

    @Override
    public boolean rotateBlock(final World w, final BlockPos pos, final EnumFacing axis) {
//        FluidCraft.log.log(Level.INFO,axis.getOpposite());
        final TileEntity tileEntity = w.getTileEntity(pos);
        if (tileEntity instanceof TileGeneralLevelMaintainer) {
            final EnumFacing facing = ((TileGeneralLevelMaintainer) tileEntity).facing;
            ((TileGeneralLevelMaintainer) tileEntity).facing = facing.rotateY();
            w.setBlockState(pos, this.blockState.getBaseState().withProperty(facingProperty, facing.rotateY()));
            return true;
        }
        return super.rotateBlock(w, pos, axis);
    }
}