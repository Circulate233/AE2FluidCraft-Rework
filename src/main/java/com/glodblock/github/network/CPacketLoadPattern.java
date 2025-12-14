package com.glodblock.github.network;

import appeng.helpers.ItemStackHelper;
import com.glodblock.github.interfaces.PatternConsumer;
import com.glodblock.github.util.Util;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class CPacketLoadPattern implements IMessage {

    private List<ItemStack> output;
    private Int2ObjectMap<ItemStack[]> crafting;
    private boolean compress;
    private static final int SLOT_SIZE = 80;

    public CPacketLoadPattern(final Int2ObjectMap<ItemStack[]> crafting, final List<ItemStack> output, final boolean compress) {
        this.crafting = crafting;
        this.output = output;
        this.compress = compress;
    }

    public CPacketLoadPattern() {
        // NO-OP
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        buf.writeBoolean(compress);
        final NBTTagCompound msg = new NBTTagCompound();
        for (final int index : crafting.keySet()) {
            writeItemArray(msg, crafting.get(index), index + "#");
        }
        writeItemArray(msg, output.toArray(new ItemStack[0]), "o");
        Util.writeNBTToBytes(buf, msg);
    }

    @Override
    public void fromBytes(final ByteBuf buf) {
        crafting = new Int2ObjectArrayMap<>();
        compress = buf.readBoolean();
        final NBTTagCompound msg = Util.readNBTFromBytes(buf);
        for (int i = 0; i < SLOT_SIZE; i ++) {
            if (msg.hasKey(i + "#")) {
                crafting.put(i, readItemArray(msg, i + "#"));
            }
        }
        output = Arrays.asList(readItemArray(msg, "o"));
    }

    private void writeItemArray(final NBTTagCompound nbt, final ItemStack[] itemList, final String key) {
        final NBTTagCompound dict = new NBTTagCompound();
        dict.setShort("l", (short) (itemList == null ? 0 : itemList.length));
        if (itemList != null) {
            int cnt = 0;
            for (final ItemStack item : itemList) {
                if (item != null) {
                    dict.setTag(cnt + "#", ItemStackHelper.stackToNBT(item));
                    ++cnt;
                }
            }
            dict.setShort("l", (short) cnt);
        }
        nbt.setTag(key, dict);
    }

    private ItemStack[] readItemArray(final NBTTagCompound nbt, final String key) {
        final NBTTagCompound dict = nbt.getCompoundTag(key);
        final short len = dict.getShort("l");
        if (len == 0) {
            return new ItemStack[0];
        } else {
            final ItemStack[] itemList = new ItemStack[len];
            for (int i = 0; i < len; ++i) {
                itemList[i] = ItemStackHelper.stackFromNBT(dict.getCompoundTag(i + "#"));
            }
            return itemList;
        }
    }

    public static class Handler implements IMessageHandler<CPacketLoadPattern, IMessage> {

        @Nullable
        @Override
        public IMessage onMessage(final CPacketLoadPattern message, final MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                if (player.openContainer instanceof final PatternConsumer c) {
                    c.acceptPattern(message.crafting, message.output, message.compress);
                }
            });
            return null;
        }

    }

}
