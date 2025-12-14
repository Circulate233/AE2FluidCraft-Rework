package com.glodblock.github.network;

import baubles.api.BaublesApi;
import com.glodblock.github.inventory.GuiType;
import com.glodblock.github.loader.FCItems;
import com.glodblock.github.util.Util;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class CPacketUseKeybind implements IMessage {

    @Override
    public void fromBytes(final ByteBuf buf) {
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        // empty for now, can be changed to allow differentiating keybinds if more are added in the future
    }

    public static class Handler implements IMessageHandler<CPacketUseKeybind, IMessage> {

        @Override
        public IMessage onMessage(final CPacketUseKeybind message, final MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                    final ItemStack stackInSlot = player.inventory.getStackInSlot(i);
                    if (stackInSlot.getItem() == FCItems.WIRELESS_FLUID_PATTERN_TERMINAL) {
                        Util.openWirelessTerminal(stackInSlot, i, false, player.world, player, GuiType.WIRELESS_FLUID_PATTERN_TERMINAL);
                        return;
                    }
                }
                if (Loader.isModLoaded("baubles")) {
                    tryOpenBauble(player);
                }
            });
            return null;
        }

        @Optional.Method(modid = "baubles")
        private static void tryOpenBauble(final EntityPlayer player) {
            for (int i = 0; i < BaublesApi.getBaublesHandler(player).getSlots(); i++) {
                final ItemStack stackInSlot = BaublesApi.getBaublesHandler(player).getStackInSlot(i);
                if (stackInSlot.getItem() == FCItems.WIRELESS_FLUID_PATTERN_TERMINAL) {
                    Util.openWirelessTerminal(stackInSlot, i, true, player.world, player, GuiType.WIRELESS_FLUID_PATTERN_TERMINAL);
                    return;
                }
            }
        }
    }

}
