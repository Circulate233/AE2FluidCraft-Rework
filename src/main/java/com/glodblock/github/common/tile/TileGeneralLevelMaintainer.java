package com.glodblock.github.common.tile;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.GridFlags;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.events.MENetworkStorageEvent;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.AELog;
import appeng.helpers.Reflected;
import appeng.me.GridAccessException;
import appeng.me.helpers.MachineSource;
import appeng.tile.grid.AENetworkTile;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;
import com.glodblock.github.util.DummyInvAdaptor;
import com.glodblock.github.util.MultiCraftingTracker;
import com.glodblock.github.util.Util;
import com.google.common.collect.ImmutableSet;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.io.IOException;

public class TileGeneralLevelMaintainer extends AENetworkTile implements ICraftingRequester, IAEAppEngInventory, ITickable {

    public EnumFacing facing;

    public static final int MAX_FLUID = 5;
    private int tick = 0;

    public boolean forceNextTick = false;

    private final AppEngInternalAEInventory config = new AppEngInternalAEInventory(this, MAX_FLUID) {
        @Override
        public int getSlotLimit(final int slot) {
            return Integer.MAX_VALUE;
        }

        @Override
        protected int getStackLimit(final int slot, @Nonnull final ItemStack stack) {
            return Integer.MAX_VALUE;
        }
    };
    private final MultiCraftingTracker craftingTracker = new MultiCraftingTracker(this, MAX_FLUID);
    private final long[] request = new long[MAX_FLUID];
    private final IActionSource source;

    @Reflected
    public TileGeneralLevelMaintainer() {
        getProxy().setIdlePowerUsage(2D);
        getProxy().setFlags(GridFlags.REQUIRE_CHANNEL);
        this.source = new MachineSource(this);
    }

    public void setConfig(final int id, final int size) {
        if (id < 0 || id >= MAX_FLUID || this.config.getStackInSlot(id).isEmpty()) {
            return;
        }
        final ItemStack drop = this.config.getStackInSlot(id).copy();
        drop.setCount(size);
        this.config.setStackInSlot(id, drop);
        doWork();
    }

    public void setRequest(final int id, final long amount) {
        if (id < 0 || id >= MAX_FLUID || amount < 0) {
            return;
        }
        this.request[id] = amount;
        markForUpdate();
        doWork();
    }

    public AppEngInternalAEInventory getInventoryHandler() {
        return this.config;
    }

    public long[] getRequest() {
        return this.request;
    }

    @MENetworkEventSubscribe
    public void onStorageUpdate(final MENetworkStorageEvent event) {
        if (event.channel.equals(Util.getFluidChannel())) {
            doWork();
        }
    }

    @MENetworkEventSubscribe
    public void onPowerUpdate(final MENetworkPowerStatusChange event) {
        doWork();
    }

    @MENetworkEventSubscribe
    public void onChannelUpdate(final MENetworkChannelsChanged event) {
        doWork();
    }

    public void doWork() {
        if (!getProxy().isActive()) {
            return;
        }
        final var storage = getItemMonitor();
        try {
            for (int i = 0; i < MAX_FLUID; i++) {
                final IAEItemStack drop = this.config.getAEStackInSlot(i);
                if (drop != null && drop.getStackSize() > 0) {
                    final IAEItemStack remain = storage.getStorageList().findPrecise(drop);
                    if (remain == null || remain.getStackSize() < drop.getStackSize()) {
                        this.craftingTracker.handleCrafting(i, request[i], drop.copy().setStackSize(request[i]), DummyInvAdaptor.INSTANCE, getWorld(), getProxy().getGrid(), getProxy().getCrafting(), this.source);
                    }
                }
            }
        } catch (final GridAccessException e) {
            //Ignore
        }
    }

    @Override
    protected boolean readFromStream(final ByteBuf data) throws IOException {
        boolean changed = super.readFromStream(data);
        for (int i = 0; i < config.getSlots(); i++) {
            final ItemStack stack = ByteBufUtils.readItemStack(data);
            if (!ItemStack.areItemStacksEqual(stack, config.getStackInSlot(i))) {
                config.setStackInSlot(i, stack);
                changed = true;
            }
        }
        for (int i = 0; i < MAX_FLUID; i++) {
            request[i] = data.readLong();
        }

        facing = EnumFacing.byHorizontalIndex(data.readInt());
        return changed;
    }

    @Override
    protected void writeToStream(final ByteBuf data) throws IOException {
        super.writeToStream(data);
        for (int i = 0; i < config.getSlots(); i++) {
            ByteBufUtils.writeItemStack(data, config.getStackInSlot(i));
        }
        for (int i = 0; i < MAX_FLUID; i++) {
            data.writeLong(request[i]);
        }

        if (facing != null) {
            data.writeInt(facing.getHorizontalIndex());
        }
    }

    @Override
    public void readFromNBT(final NBTTagCompound data) {
        super.readFromNBT(data);
        for (int i = 0; i < MAX_FLUID; i++) {
            request[i] = data.getLong("reqX" + i);
        }
        config.readFromNBT(data, "configX");
        craftingTracker.readFromNBT(data);
        if (data.hasKey("facing")) {
            facing = EnumFacing.byHorizontalIndex(data.getInteger("facing"));
        } else {
            facing = EnumFacing.NORTH;
        }

    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound data) {
        super.writeToNBT(data);
        for (int i = 0; i < MAX_FLUID; i++) {
            data.setLong("reqX" + i, request[i]);
        }
        config.writeToNBT(data, "configX");
        craftingTracker.writeToNBT(data);
        data.setInteger("facing", facing.getHorizontalIndex());
        return data;
    }

    private IMEMonitor<IAEItemStack> getItemMonitor() {
        return getProxy().getNode().getGrid().<IStorageGrid>getCache(IStorageGrid.class)
                         .getInventory(Util.getItemChannel());
    }

    @Override
    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        return this.craftingTracker.getRequestedJobs();
    }

    @Override
    public IAEItemStack injectCraftedItems(final ICraftingLink link, final IAEItemStack items, final Actionable mode) {
        final var monitor = getItemMonitor();

        try {
            if (monitor != null && this.getProxy().isActive()) {
                final IEnergyGrid energy = this.getProxy().getEnergy();
                final double power = items.getStackSize() / 1000D;

                if (energy.extractAEPower(power, mode, PowerMultiplier.CONFIG) > power - 0.01) {
                    final ItemStack inputStack = items.getCachedItemStack(items.getStackSize());

                    if (mode == Actionable.SIMULATE) {
                        monitor.injectItems(items, Actionable.SIMULATE, source);
                        items.setCachedItemStack(inputStack);
                    } else {
                        final IAEItemStack remaining = monitor.injectItems(items, Actionable.MODULATE, source);
                        if (remaining == null || remaining.getStackSize() <= 0) {
                            final ItemStack tmp = items.getDefinition();
                            items.setCachedItemStack(tmp);
                        }
                    }

                    return items;
                }
            }

        } catch (final GridAccessException e) {
            AELog.debug(e);
        }

        return items;
    }

    @Override
    public void jobStateChange(final ICraftingLink link) {
        this.craftingTracker.jobStateChange(link);
    }

    @Override
    public void onChangeInventory(final IItemHandler iItemHandler, final int i, final InvOperation invOperation, final ItemStack itemStack, final ItemStack itemStack1) {
        markForUpdate();
    }

    //Sometimes it may get fucked, let's force update it every 60s
    @Override
    public void update() {
        if (!getWorld().isRemote) {
            tick++;
            if (forceNextTick) {
                forceNextTick = false;
                doWork();
            } else if (tick > 1200) {
                tick = 0;
                doWork();
            }
        }
    }
}