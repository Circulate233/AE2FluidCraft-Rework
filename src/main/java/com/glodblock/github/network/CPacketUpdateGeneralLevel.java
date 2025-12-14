package com.glodblock.github.network;

import com.glodblock.github.client.container.ContainerGeneralLevelMaintainer;
import com.glodblock.github.common.tile.TileGeneralLevelMaintainer;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;

public class CPacketUpdateGeneralLevel implements IMessage {

    private int index;
    private int size;

    public CPacketUpdateGeneralLevel() {
        //NO-OP
    }

    public CPacketUpdateGeneralLevel(final int id, final int value) {
        this.index = id;
        this.size = value;
    }

    @Override
    public void fromBytes(final ByteBuf buf) {
        index = buf.readInt();
        size = buf.readInt();
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        buf.writeInt(index);
        buf.writeInt(size);
    }

    public static class Handler implements IMessageHandler<CPacketUpdateGeneralLevel, IMessage> {

        @Nullable
        @Override
        public IMessage onMessage(final CPacketUpdateGeneralLevel message, final MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                if (player.openContainer instanceof ContainerGeneralLevelMaintainer) {
                    final TileGeneralLevelMaintainer te = ((ContainerGeneralLevelMaintainer) player.openContainer).getTile();
                    if (message.index >= 10) {
                        te.setRequest(message.index - 10, message.size);
                    }
                    else {
                        te.setConfig(message.index, message.size);
                    }
                }
            });
            return null;
        }

    }

}
