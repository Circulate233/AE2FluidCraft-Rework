package com.glodblock.github.network;

import appeng.container.ContainerOpenContext;
import appeng.container.slot.SlotFake;
import appeng.core.sync.GuiBridge;
import appeng.util.Platform;
import com.glodblock.github.client.container.ContainerItemAmountChange;
import com.glodblock.github.interfaces.FCFluidPatternContainer;
import com.glodblock.github.inventory.GuiType;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.util.Ae2Reflect;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class CPacketPatternValueSet implements IMessage {

    private Enum<?> originGui;
    private int amount;
    private int valueIndex;
    private boolean isFC;

    public CPacketPatternValueSet() {
        //NO-OP
    }

    public CPacketPatternValueSet(final Enum<?> originalGui, final int amount, final int valueIndex, final boolean isFC) {
        this.originGui = originalGui;
        this.amount = amount;
        this.valueIndex = valueIndex;
        this.isFC = isFC;
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        buf.writeBoolean(isFC);
        buf.writeInt(originGui.ordinal());
        buf.writeInt(amount);
        buf.writeInt(valueIndex);
    }

    @Override
    public void fromBytes(final ByteBuf buf) {
        this.isFC = buf.readBoolean();
        this.originGui = isFC ? GuiType.getByOrdinal(buf.readInt()) : GuiBridge.values()[buf.readInt()];
        this.amount = buf.readInt();
        this.valueIndex = buf.readInt();
    }

    public static class Handler implements IMessageHandler<CPacketPatternValueSet, IMessage> {

        @Override
        public IMessage onMessage(final CPacketPatternValueSet message, final MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                if (player.openContainer instanceof final ContainerItemAmountChange cpv) {
                    final ContainerOpenContext context = cpv.getOpenContext();
                    if (context != null) {
                        if (message.isFC) {
                            InventoryHandler.openGui(
                                player,
                                player.world,
                                new BlockPos(Ae2Reflect.getContextX(context), Ae2Reflect.getContextY(context), Ae2Reflect.getContextZ(context)),
                                context.getSide().getFacing(),
                                (GuiType) message.originGui
                            );
                        } else {
                            Platform.openGUI(
                                player,
                                context.getTile(),
                                context.getSide(),
                                (GuiBridge) message.originGui
                            );
                        }
                        if (player.openContainer instanceof FCFluidPatternContainer) {
                            final Slot slot = player.openContainer.getSlot(message.valueIndex);
                            if (slot instanceof SlotFake) {
                                if (slot.getHasStack()) {
                                    final ItemStack stack = slot.getStack().copy();
                                    stack.setCount(message.amount);
                                    slot.putStack(stack);
                                }
                            }
                        }
                    }
                }
            });
            return null;
        }

    }
}