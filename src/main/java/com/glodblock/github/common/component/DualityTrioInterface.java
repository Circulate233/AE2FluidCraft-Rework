package com.glodblock.github.common.component;

import appeng.api.config.Upgrades;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.fluids.helper.IFluidInterfaceHost;
import appeng.helpers.IInterfaceHost;
import appeng.me.helpers.AENetworkProxy;
import appeng.util.SettingsFrom;
import com.mekeng.github.common.me.duality.IGasInterfaceHost;
import com.mekeng.github.common.me.duality.impl.DualityGasInterface;
import com.mekeng.github.common.me.inventory.IGasInventory;
import com.mekeng.github.common.me.inventory.impl.GasInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class DualityTrioInterface<H extends IInterfaceHost & IFluidInterfaceHost & IGasInterfaceHost> extends DualityDualInterface<H> {

    private final DualityGasInterface gasDuality;

    public DualityTrioInterface(final AENetworkProxy networkProxy, final H host) {
        super(networkProxy, host);
        this.gasDuality = new DualityGasInterface(networkProxy, host);
    }

    public DualityGasInterface getGasInterface() {
        return this.gasDuality;
    }

    @Override
    public int getInstalledUpgrades(final Upgrades u) {
        return this.gasDuality.getInstalledUpgrades(u) + super.getInstalledUpgrades(u);
    }

    @Override
    public void setPriority(final int newValue) {
        super.setPriority(newValue);
        this.gasDuality.setPriority(newValue);
    }

    @Override
    public boolean hasCapability(@Nonnull final Capability<?> capability, @Nullable final EnumFacing facing) {
        return this.gasDuality.hasCapability(capability, facing) || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull final Capability<T> capability, @Nullable final EnumFacing facing) {
        final T capInst = super.getCapability(capability, facing);
        return capInst != null ? capInst : this.gasDuality.getCapability(capability, facing);
    }

    @Override
    public TickingRequest getTickingRequest(final IGridNode node) {
        final TickingRequest dual = super.getTickingRequest(node);
        final TickingRequest gas = this.gasDuality.getTickingRequest(node);
        return new TickingRequest(
                Math.min(dual.minTickRate, gas.minTickRate),
                Math.max(dual.maxTickRate, gas.maxTickRate),
                dual.isSleeping && gas.isSleeping, // might cause some unnecessary ticking, but oh well
                true);
    }

    @Override
    public TickRateModulation onTick(final IGridNode node, final int ticksSinceLastCall) {
        final TickRateModulation dual = super.onTick(node, ticksSinceLastCall);
        final TickRateModulation gas = this.gasDuality.tickingRequest(node, ticksSinceLastCall);
        if (dual.ordinal() >= gas.ordinal()) { // return whichever is most urgent
            return dual;
        } else {
            return gas;
        }
    }

    @Override
    public void onChannelStateChange(final MENetworkChannelsChanged c) {
        super.onChannelStateChange(c);
        this.gasDuality.notifyNeighbors();
    }

    @Override
    public void onPowerStateChange(final MENetworkPowerStatusChange c) {
        super.onPowerStateChange(c);
        this.gasDuality.notifyNeighbors();
    }

    @Override
    public void onGridChanged() {
        super.onGridChanged();
        this.gasDuality.gridChanged();
    }

    @Override
    public void addDrops(final List<ItemStack> drops) {
        super.addDrops(drops);
        this.gasDuality.addDrops(drops);
    }

    @Override
    public IItemHandler getItemInventoryByName(final String name) {
        if (name.startsWith("gas_")) {
            return this.gasDuality.getInventoryByName(name.replace("gas_", ""));
        }
        return super.getItemInventoryByName(name);
    }

    @Override
    public void writeToNBT(final NBTTagCompound data) {
        super.writeToNBT(data);
        final NBTTagCompound gasIfaceTag = new NBTTagCompound();
        this.gasDuality.writeToNBT(gasIfaceTag);
        data.setTag("gasDuality", gasIfaceTag);
    }

    @Override
    public void readFromNBT(final NBTTagCompound data) {
        super.readFromNBT(data);
        this.gasDuality.readFromNBT(data.getCompoundTag("gasDuality"));
    }

    @Override
    public NBTTagCompound downloadSettings(final SettingsFrom from) {
        final NBTTagCompound tag = super.downloadSettings(from);
        if (from == SettingsFrom.MEMORY_CARD) {
            final IGasInventory gasInv = this.gasDuality.getGasInventoryByName("config");
            if (gasInv instanceof GasInventory) {
                tag.setTag("gas_config", ((GasInventory) gasInv).save());
            }
        }
        return tag;
    }

    @Override
    public void uploadSettings(final NBTTagCompound compound, final EntityPlayer player) {
        super.uploadSettings(compound, player);
        final IGasInventory gasInv = this.gasDuality.getGasInventoryByName("config");
        if (gasInv instanceof final GasInventory target) {
            final GasInventory tmp = new GasInventory(target.size());
            final NBTTagCompound data = compound.getCompoundTag("gas_config");
            if (!data.isEmpty()) {
                tmp.load(data);
            }
            for (int x = 0; x < tmp.size(); x++) {
                target.setGas(x, tmp.getGasStack(x));
            }
        }
    }

}
