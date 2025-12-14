package com.glodblock.github.network;

import com.glodblock.github.client.GuiGeneralLevelMaintainer;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;

public class SPacketSetGeneralLevel implements IMessage {

    private int index;
    private int size;

    public SPacketSetGeneralLevel() {
        //NO-OP
    }

    public SPacketSetGeneralLevel(final int id, final int value) {
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

    public static class Handler implements IMessageHandler<SPacketSetGeneralLevel, IMessage> {

        @Nullable
        @Override
        public IMessage onMessage(final SPacketSetGeneralLevel message, final MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                final GuiScreen gs = Minecraft.getMinecraft().currentScreen;
                if (gs instanceof GuiGeneralLevelMaintainer) {
                    ((GuiGeneralLevelMaintainer) gs).setMaintainNumber(message.index, message.size);
                }
            });
            return null;
        }

    }

}
