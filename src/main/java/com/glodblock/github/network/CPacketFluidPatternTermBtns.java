package com.glodblock.github.network;

import com.glodblock.github.client.container.ContainerItemDualInterface;
import com.glodblock.github.client.container.ContainerUltimateEncoder;
import com.glodblock.github.client.container.ContainerWrapInterface;
import com.glodblock.github.interfaces.FCFluidPatternContainer;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class CPacketFluidPatternTermBtns implements IMessage {

    private String Name = "";
    private String Value = "";

    public CPacketFluidPatternTermBtns(final String name, final String value) {
        Name = name;
        Value = value;
    }

    public CPacketFluidPatternTermBtns() {
        // NO-OP
    }

    @Override
    public void fromBytes(final ByteBuf buf) {
        final int leName = buf.readInt();
        final int leVal = buf.readInt();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < leName; i++) {
            sb.append(buf.readChar());
        }
        Name = sb.toString();
        sb = new StringBuilder();
        for (int i = 0; i < leVal; i++) {
            sb.append(buf.readChar());
        }
        Value = sb.toString();
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        buf.writeInt(Name.length());
        buf.writeInt(Value.length());
        for (int i = 0; i < Name.length(); i++) {
            buf.writeChar(Name.charAt(i));
        }
        for (int i = 0; i < Value.length(); i++) {
            buf.writeChar(Value.charAt(i));
        }
    }

    public static class Handler implements IMessageHandler<CPacketFluidPatternTermBtns, IMessage> {

        @Override
        public IMessage onMessage(final CPacketFluidPatternTermBtns message, final MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                final String Name = message.Name;
                final String Value = message.Value;
                final Container c = player.openContainer;
                if (c instanceof final FCFluidPatternContainer cpt) {
                    if (c instanceof final ContainerUltimateEncoder cue) {
                        switch (Name) {
                            case "UltimateEncoder.Encode" -> {
                                if (Value.equals("0"))
                                    cue.encode();
                                else cue.encodeAndMoveToInventory();
                            }
                            case "UltimateEncoder.Clear" -> cue.clear();
                            case "UltimateEncoder.MultiplyByTwo" -> cue.multiply(2);
                            case "UltimateEncoder.MultiplyByThree" -> cue.multiply(3);
                            case "UltimateEncoder.DivideByTwo" -> cue.divide(2);
                            case "UltimateEncoder.DivideByThree" -> cue.divide(3);
                            case "UltimateEncoder.IncreaseByOne" -> cue.increase(1);
                            case "UltimateEncoder.DecreaseByOne" -> cue.decrease(1);
                            case "UltimateEncoder.Combine" -> cue.setCombineMode(Value.equals("1"));
                            case "UltimateEncoder.Fluid" -> cue.setFluidPlaceMode(Value.equals("1"));
                        }
                    } else {
                        switch (Name) {
                            case "PatternTerminal.Combine" -> cpt.setCombineMode(Value.equals("1"));
                            case "PatternTerminal.Fluid" -> cpt.setFluidPlaceMode(Value.equals("1"));
                            case "PatternTerminal.Craft" -> cpt.encodeFluidCraftPattern();
                        }
                    }
                } else if (c instanceof final ContainerItemDualInterface cdi) {
                    switch (Name) {
                        case "DualInterface.FluidPacket" -> cdi.setFluidPacketInTile(Value.equals("1"));
                        case "DualInterface.AllowSplitting" -> cdi.setAllowSplittingInTile(Value.equals("1"));
                        case "DualInterface.ExtendedBlockMode" -> cdi.setExtendedBlockMode(Integer.parseInt(Value));
                    }
                } else if (c instanceof final ContainerWrapInterface cdi) {
                    switch (Name) {
                        case "WrapDualInterface.FluidPacket" -> cdi.setFluidPacketInTile(Value.equals("1"));
                        case "WrapDualInterface.AllowSplitting" -> cdi.setAllowSplittingInTile(Value.equals("1"));
                        case "WrapDualInterface.ExtendedBlockMode" -> cdi.setExtendedBlockMode(Integer.parseInt(Value));
                    }
                }
            });
            return null;
        }
    }

}