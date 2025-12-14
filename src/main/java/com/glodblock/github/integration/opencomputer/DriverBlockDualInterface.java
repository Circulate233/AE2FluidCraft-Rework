package com.glodblock.github.integration.opencomputer;

import appeng.api.util.AEPartLocation;
import com.glodblock.github.common.tile.TileDualInterface;
import com.glodblock.github.loader.FCBlocks;
import li.cil.oc.api.driver.EnvironmentProvider;
import li.cil.oc.api.driver.NamedBlock;
import li.cil.oc.api.internal.Database;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Component;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.prefab.DriverSidedTileEntity;
import li.cil.oc.integration.ManagedTileEntityEnvironment;
import li.cil.oc.integration.appeng.NetworkControl;
import li.cil.oc.integration.appeng.NetworkControl$class;
import li.cil.oc.util.ExtendedArguments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

public class DriverBlockDualInterface extends DriverSidedTileEntity {

    @Override
    public Class<?> getTileEntityClass() {
        return TileDualInterface.class;
    }

    @Override
    public ManagedEnvironment createEnvironment(final World world, final BlockPos blockPos, final EnumFacing enumFacing) {
        return new Environment((TileDualInterface) world.getTileEntity(blockPos));
    }

    public static class Environment extends ManagedTileEntityEnvironment<TileDualInterface> implements NamedBlock, NetworkControl<TileDualInterface> {

        static final String name = "me_dual_interface";

        public Environment(final TileDualInterface tile) {
            super(tile, name);
        }

        @Callback(doc = "function([slot:number]):table -- Get the configuration of the dual interface.")
        public Object[] getInterfaceConfiguration(final Context context, final Arguments args) {
            final ExtendedArguments.ExtendedArguments exArg = ExtendedArguments.extendedArguments(args);
            final IItemHandler config = tileEntity.getInventoryByName("config");
            final int slot = exArg.optSlot(config, 0, 0);
            final ItemStack stack = config.getStackInSlot(slot);
            return new Object[] {stack};
        }

        @Callback(doc = "function([slot:number][, database:address, entry:number[, size:number]]):boolean -- Configure the dual interface.")
        public Object[] setInterfaceConfiguration(final Context context, final Arguments args) {
            final ExtendedArguments.ExtendedArguments exArg = ExtendedArguments.extendedArguments(args);
            final IItemHandler config = tileEntity.getInventoryByName("config");
            final int slot = args.isString(0) ? 0 : exArg.optSlot(config, 0, 0);
            ItemStack stack = ItemStack.EMPTY;
            if (args.count() > 1) {
                final int offset = args.isString(0) ? 0 : 1;
                final String address = args.checkString(offset);
                final int entry = args.checkInteger(1 + offset);
                final int size = args.optInteger(2 + offset, 1);
                final Node n = this.node().network().node(address);
                if (!(n instanceof Component)) {
                    throw new IllegalArgumentException("no such component");
                }
                if (!(n instanceof final Database database)) {
                    throw new IllegalArgumentException("not a database");
                }
                ItemStack dbStack = database.getStackInSlot(entry - 1);
                if (dbStack == null || size < 1 || dbStack.isEmpty())  {
                    dbStack = ItemStack.EMPTY;
                }
                else {
                    dbStack.setCount(Math.min(size, dbStack.getMaxStackSize()));
                }
                stack = dbStack;
            }
            config.extractItem(slot, config.getStackInSlot(slot).getCount(), false);
            config.insertItem(slot, stack, false);
            context.pause(0.5);
            return new Object[] {true};
        }

        @Override
        @Callback(doc = "function():table -- Get a list of tables representing the available CPUs in the network.")
        public Object[] getCpus(final Context context, final Arguments arguments) {
            return NetworkControl$class.getCpus(this, context, arguments);
        }

        @Override
        @Callback(doc = "function([filter:table]):table -- Get a list of known item recipes. These can be used to issue crafting requests.")
        public Object[] getCraftables(final Context context, final Arguments arguments) {
            return NetworkControl$class.getCraftables(this, context, arguments);
        }

        @Override
        @Callback(doc = "function([filter:table]):table -- Get a list of the stored items in the network.")
        public Object[] getItemsInNetwork(final Context context, final Arguments arguments) {
            return NetworkControl$class.getItemsInNetwork(this, context, arguments);
        }

        @Override
        @Callback(doc = "function([filter:table, dbAddress:string, startSlot:number, count:number]): bool -- Store items in the network matching the specified filter in the database with the specified address.")
        public Object[] store(final Context context, final Arguments arguments) {
            return NetworkControl$class.store(this, context, arguments);
        }

        @Override
        @Callback(doc = "function():table -- Get a list of the stored fluids in the network.")
        public Object[] getFluidsInNetwork(final Context context, final Arguments arguments) {
            return NetworkControl$class.getFluidsInNetwork(this, context, arguments);
        }

        @Override
        @Callback(doc = "function():number -- Get the average power injection into the network.")
        public Object[] getAvgPowerInjection(final Context context, final Arguments arguments) {
            return NetworkControl$class.getAvgPowerInjection(this, context, arguments);
        }

        @Override
        @Callback(doc = "function():number -- Get the average power usage of the network.")
        public Object[] getAvgPowerUsage(final Context context, final Arguments arguments) {
            return NetworkControl$class.getAvgPowerUsage(this, context, arguments);
        }

        @Override
        @Callback(doc = "function():number -- Get the idle power usage of the network.")
        public Object[] getIdlePowerUsage(final Context context, final Arguments arguments) {
            return NetworkControl$class.getIdlePowerUsage(this, context, arguments);
        }

        @Override
        @Callback(doc = "function():number -- Get the maximum stored power in the network.")
        public Object[] getMaxStoredPower(final Context context, final Arguments arguments) {
            return NetworkControl$class.getMaxStoredPower(this, context, arguments);
        }

        @Override
        @Callback(doc = "function():number -- Get the stored power in the network. ")
        public Object[] getStoredPower(final Context context, final Arguments arguments) {
            return NetworkControl$class.getStoredPower(this, context, arguments);
        }

        @Override
        @Callback(doc = "function():boolean -- True if the AE network is considered online")
        public Object[] isNetworkPowered(final Context context, final Arguments arguments) {
            return NetworkControl$class.isNetworkPowered(this, context, arguments);
        }

        @Override
        @Callback(doc = "function():number -- Returns the energy demand on the AE network")
        public Object[] getEnergyDemand(final Context context, final Arguments arguments) {
            return NetworkControl$class.getEnergyDemand(this, context, arguments);
        }

        @Override
        public String preferredName() {
            return name;
        }

        @Override
        public int priority() {
            return 5;
        }

        @Override
        public TileDualInterface tile() {
            return this.tileEntity;
        }

        @Override
        public AEPartLocation pos() {
            return AEPartLocation.INTERNAL;
        }
    }

    public static class Provider implements EnvironmentProvider {

        @Override
        public Class<?> getEnvironment(final ItemStack stack) {
            if (stack.isItemEqual(new ItemStack(FCBlocks.DUAL_INTERFACE))) {
                return Environment.class;
            }
            return null;
        }
    }

}
