package com.glodblock.github.inventory;

import appeng.core.AELog;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.network.CPacketSwitchGuis;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ConcurrentModificationException;

public class InventoryHandler implements IGuiHandler {

    public static void switchGui(final GuiType guiType) {
        FluidCraft.proxy.netHandler.sendToServer(new CPacketSwitchGuis(guiType));
    }

    public static void openGui(final EntityPlayer player, final World world, final BlockPos pos, final EnumFacing face, final GuiType guiType) {
        try {
            player.openGui(FluidCraft.INSTANCE,
                    (guiType.ordinal() << 3) | face.ordinal(), world, pos.getX(), pos.getY(), pos.getZ());
        } catch (final ConcurrentModificationException e) {
            AELog.warn("catch CME when trying to open %s.", guiType);
        }
    }

    @Nullable
    @Override
    public Object getServerGuiElement(final int id, final EntityPlayer player, final World world, final int x, final int y, final int z) {
        final int faceOrd = id & 0x7;
        if (faceOrd > EnumFacing.VALUES.length) {
            return null;
        }
        final EnumFacing face = EnumFacing.VALUES[faceOrd];
        final GuiType type = GuiType.getByOrdinal(id >>> 3);
        return type != null ? type.getFactory().createServerGui(player, world, x, y, z, face) : null;
    }

    @SideOnly(Side.CLIENT)
    @Nullable
    @Override
    public Object getClientGuiElement(final int id, final EntityPlayer player, final World world, final int x, final int y, final int z) {
        final int faceOrd = id & 0x7;
        if (faceOrd > EnumFacing.VALUES.length) {
            return null;
        }
        final EnumFacing face = EnumFacing.VALUES[faceOrd];
        final GuiType type = GuiType.getByOrdinal(id >>> 3);
        return type != null ? type.getFactory().createClientGui(player, world, x, y, z, face) : null;
    }

}