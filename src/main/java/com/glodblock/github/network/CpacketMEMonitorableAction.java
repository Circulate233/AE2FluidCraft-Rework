package com.glodblock.github.network;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.implementations.ContainerMEMonitorable;
import appeng.core.AELog;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketInventoryAction;
import appeng.fluids.util.AEFluidStack;
import appeng.helpers.InventoryAction;
import appeng.me.helpers.PlayerSource;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import com.glodblock.github.common.item.fake.FakeItemRegister;
import com.glodblock.github.util.ModAndClassUtil;
import com.glodblock.github.util.Util;
import com.mekeng.github.common.me.data.impl.AEGasStack;
import io.netty.buffer.ByteBuf;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.spongepowered.asm.mixin.Unique;
import thaumicenergistics.container.ContainerBaseTerminal;

import java.io.IOException;

public class CpacketMEMonitorableAction implements IMessage {

    public static final byte FLUID_OPERATE = 2;
    public static final byte FLUID = 0;
    public static final byte GAS = 1;
    private static IAEItemStack bucket;

    private byte type;
    private NBTTagCompound obj;

    public CpacketMEMonitorableAction() {
    }

    public CpacketMEMonitorableAction(byte b, NBTTagCompound s) {
        type = b;
        obj = s;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        type = buf.readByte();
        obj = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(type);
        ByteBufUtils.writeTag(buf, obj);
    }

    public static class Handler implements IMessageHandler<CpacketMEMonitorableAction, IMessage> {

        @Override
        public IMessage onMessage(CpacketMEMonitorableAction message, MessageContext ctx) {
            final var player = ctx.getServerHandler().player;
            final var c = player.openContainer;
            final IStorageGrid grid;
            final IActionSource source;
            if (c instanceof ContainerMEMonitorable cme) {
                grid = cme.getNetworkNode().getGrid().getCache(IStorageGrid.class);
                source = new PlayerSource(player, (IActionHost) cme.getTarget());
            } else if (ModAndClassUtil.TCE && c instanceof ContainerBaseTerminal cbt) {
                grid = cbt.getPart().getGridNode().getGrid().getCache(IStorageGrid.class);
                source = new PlayerSource(player, cbt.getPart());
            } else return null;

            boolean drain = false;
            var h = player.inventory.getItemStack();
            if (!h.isEmpty()) {
                ItemStack ch = h.copy();
                ch.setCount(1);
                if (message.type == FLUID) {
                    final IFluidHandlerItem fh = FluidUtil.getFluidHandler(ch);
                    if (fh == null) return null;
                    final var allFluid = fh.drain(Integer.MAX_VALUE, false);
                    FluidStack fluid = null;
                    if (!message.obj.isEmpty()) {
                        fluid = FluidStack.loadFluidStackFromNBT(message.obj);
                        if (fluid != null) {
                            if (allFluid != null && allFluid.amount > 0) {
                                if (!allFluid.isFluidEqual(fluid)) drain = true;
                            }
                        } else drain = true;
                    } else drain = true;
                    final var fluidStorage = grid.getInventory(Util.getFluidChannel());
                    final AEFluidStack allAEFluid;
                    if (drain) {
                        allAEFluid = AEFluidStack.fromFluidStack(allFluid);
                        if (allAEFluid == null) return null;
                        final var a = fluidStorage.injectItems(allAEFluid, Actionable.SIMULATE, source);
                        final var size = allAEFluid.getStackSize() - (a == null ? 0 : a.getStackSize());
                        fluidStorage.injectItems(allAEFluid.setStackSize(size), Actionable.MODULATE, source);
                        fh.drain((int) size, true);
                    } else {
                        allAEFluid = AEFluidStack.fromFluidStack(fluid);
                        final var a = fluidStorage.extractItems(allAEFluid, Actionable.SIMULATE, source);
                        if (a == null) return null;
                        final var size = fh.fill(a.getFluidStack(), false);
                        fluidStorage.extractItems(allAEFluid.setStackSize(size), Actionable.MODULATE, source);
                        fh.fill(a.getFluidStack(), true);
                    }
                    if (h.getCount() > 1) {
                        h.shrink(1);
                        final var cc = fh.getContainer();
                        cc.setCount(1);
                        player.inventory.placeItemBackInInventory(player.world, cc);
                    } else player.inventory.setItemStack(fh.getContainer());
                    updateHeld(player);
                } else if (ModAndClassUtil.GAS && message.type == GAS && h.getItem() instanceof IGasItem ig) {
                    mek$Work(message, ig, ch, grid, source, h, player);
                }
            } else if (message.type == FLUID_OPERATE) {
                if (bucket == null) {
                    bucket = AEItemStack.fromItemStack(new ItemStack(Items.BUCKET));
                }
                final FluidStack fluid;
                if (!message.obj.isEmpty()) {
                    var i = new ItemStack(message.obj);
                    fluid = FakeItemRegister.getStack(i);
                    if (fluid == null) return null;
                    fluid.amount = 1000;
                } else return null;
                boolean shift = message.obj.getBoolean("shift");
                final var itemStorage = grid.getInventory(Util.getItemChannel());
                final var fluidStorage = grid.getInventory(Util.getFluidChannel());
                var b = itemStorage.extractItems(bucket, Actionable.SIMULATE, source);
                if (b == null) return null;
                final var aeFluid = fluidStorage.extractItems(AEFluidStack.fromFluidStack(fluid), Actionable.SIMULATE, source);
                if (aeFluid == null || aeFluid.getStackSize() < 1000) return null;
                final IFluidHandlerItem fh = FluidUtil.getFluidHandler(b.createItemStack());
                if (fh == null) return null;
                var s = fh.fill(aeFluid.getFluidStack(), true);
                if (s != 1000) return null;
                var out = fh.getContainer();
                if (shift) {
                    var slot = player.inventory.getFirstEmptyStack();
                    if (slot == -1) return null;
                    player.inventory.setInventorySlotContents(slot, out);
                } else {
                    player.inventory.setItemStack(out);
                    updateHeld(player);
                }
                itemStorage.extractItems(bucket, Actionable.MODULATE, source);
                fluidStorage.extractItems(aeFluid, Actionable.MODULATE, source);
            }
            return null;
        }

        @Unique
        @Optional.Method(modid = "mekeng")
        private void mek$Work(CpacketMEMonitorableAction message, IGasItem ig, ItemStack ch, IStorageGrid grid, IActionSource source, ItemStack h, EntityPlayerMP player) {
            boolean drain = false;
            final var allGas = ig.getGas(ch);
            final var allAmount = allGas == null ? 0 : allGas.amount;
            GasStack gas = null;
            if (!message.obj.isEmpty()) {
                gas = GasStack.readFromNBT(message.obj);
                if (gas != null) {
                    if (allGas != null && allGas.amount > 0) {
                        if (!allGas.isGasEqual(gas)) drain = true;
                    }
                } else drain = true;
            } else drain = true;
            final var gasStorage = grid.getInventory(Util.getGasChannel());
            AEGasStack allAEGas;
            if (drain) {
                allAEGas = AEGasStack.of(allGas);
                if (allAEGas == null) return;
                final var a = gasStorage.injectItems(allAEGas, Actionable.SIMULATE, source);
                final var size = allAEGas.getStackSize() - (a == null ? 0 : a.getStackSize());
                gasStorage.injectItems(allAEGas.setStackSize(size), Actionable.MODULATE, source);
                allGas.amount -= (int) size;
                ig.setGas(ch, allGas);
            } else {
                allAEGas = AEGasStack.of(gas);
                if (allAEGas == null) return;
                final var a = gasStorage.extractItems(allAEGas, Actionable.SIMULATE, source);
                if (a == null) return;
                final var size = Math.min(ig.getMaxGas(ch) - allAmount, (int) a.getStackSize());
                gasStorage.extractItems(allAEGas.setStackSize(size), Actionable.MODULATE, source);
                gas.amount = size + allAmount;
                ig.setGas(ch, gas);
            }
            if (h.getCount() > 1) {
                h.shrink(1);
                player.inventory.placeItemBackInInventory(player.world, ch);
            } else player.inventory.setItemStack(ch);
            updateHeld(player);
        }

        private void updateHeld(EntityPlayerMP p) {
            if (Platform.isServer()) {
                try {
                    NetworkHandler.instance().sendTo(new PacketInventoryAction(InventoryAction.UPDATE_HAND, 0, AEItemStack.fromItemStack(p.inventory.getItemStack())), p);
                } catch (IOException e) {
                    AELog.debug(e);
                }
            }
        }
    }
}