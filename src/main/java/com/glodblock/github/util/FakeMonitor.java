package com.glodblock.github.util;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.me.cache.GridStorageCache;
import appeng.me.cache.NetworkMonitor;
import com.glodblock.github.common.item.fake.FakeItemRegister;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.Nonnull;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Optional;

public abstract class FakeMonitor<T extends IAEStack<T>> implements IMEMonitor<IAEItemStack> {

    protected static final Map<IStorageChannel<?>, IAEItemStack> drop = new Reference2ObjectOpenHashMap<>();
    protected final Map<T, IAEItemStack> cacheMap = new Object2ObjectOpenHashMap<>();
    protected final NetworkMonitor<T> monitor;
    private final GridStorageCache storage;
    private final IStorageChannel<IAEItemStack> channel = Util.getItemChannel();

    public FakeMonitor(final GridStorageCache grid, final IStorageChannel<T> channel) {
        monitor = (NetworkMonitor<T>) grid.getInventory(channel);
        storage = grid;
    }

    @Override
    public IAEItemStack injectItems(final IAEItemStack stack, final Actionable actionable, final IActionSource source) {
        final T i = FakeItemRegister.getAEStack(stack);
        if (i == null) return null;
        final FakeMonitorSource fakeSource = FakeMonitorSource.release(source);
        final T s = monitor.injectItems(i, actionable, fakeSource);
        fakeSource.recycle();
        if (actionable == Actionable.MODULATE) {
            storage.postAlterationOfStoredItems(
                channel,
                ObjectLists.singleton(
                    stack.copy().setStackSize(
                        stack.getStackSize() - (s == null ? 0 : s.getStackSize())
                    )
                ),
                source
            );
        }
        if (s == null) {
            return null;
        } else {
            return FakeItemRegister.packAEStackLong(s, stack.getItem());
        }
    }

    @Override
    public IAEItemStack extractItems(final IAEItemStack stack, final Actionable actionable, final IActionSource source) {
        final T i = FakeItemRegister.getAEStack(stack);
        if (i == null) return null;
        final FakeMonitorSource fakeSource = FakeMonitorSource.release(source);
        final T s = monitor.extractItems(i, actionable, fakeSource);
        fakeSource.recycle();
        if (s == null) {
            return null;
        } else {
            final var o = FakeItemRegister.packAEStackLong(s, stack.getItem());
            if (actionable == Actionable.MODULATE && o != null) {
                storage.postAlterationOfStoredItems(
                    channel,
                    ObjectLists.singleton(
                        stack.copy().setStackSize(
                            -o.getStackSize()
                        )
                    ),
                    source
                );
            }
            return o;
        }
    }

    @Override
    public abstract IItemList<IAEItemStack> getAvailableItems(IItemList<IAEItemStack> list) ;

    @Override
    public IStorageChannel<IAEItemStack> getChannel() {
        return channel;
    }

    @Override
    public IItemList<IAEItemStack> getStorageList() {
        return null;
    }

    @Override
    public void addListener(final IMEMonitorHandlerReceiver<IAEItemStack> imeMonitorHandlerReceiver, final Object o) {

    }

    @Override
    public void removeListener(final IMEMonitorHandlerReceiver<IAEItemStack> imeMonitorHandlerReceiver) {

    }

    @Override
    public AccessRestriction getAccess() {
        return AccessRestriction.READ_WRITE;
    }

    @Override
    public boolean isPrioritized(final IAEItemStack stack) {
        return true;
    }

    @Override
    public boolean canAccept(final IAEItemStack stack) {
        return true;
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getSlot() {
        return monitor.getSlot();
    }

    @Override
    public boolean validForPass(final int i) {
        return i == 2;
    }

    public static final class FakeMonitorSource implements IActionSource {

        private static final Deque<FakeMonitorSource> POOL = new ArrayDeque<>(100);
        private IActionSource source;

        private FakeMonitorSource(final IActionSource source) {
            this.source = source;
        }

        public static FakeMonitorSource release(final IActionSource source) {
            synchronized (POOL) {
                if (!POOL.isEmpty()) {
                    return POOL.peek().setSource(source);
                }
            }
            return new FakeMonitorSource(source);
        }

        public FakeMonitorSource setSource(final IActionSource source) {
            this.source = source;
            return this;
        }

        public void recycle() {
            synchronized (POOL) {
                if (POOL.size() < 100) POOL.add(this);
            }
            this.setSource(null);
        }

        @Nonnull
        @Override
        public Optional<EntityPlayer> player() {
            return source.player();
        }

        @Nonnull
        @Override
        public Optional<IActionHost> machine() {
            return source.machine();
        }

        @Nonnull
        @Override
        public <T> Optional<T> context(@Nonnull final Class<T> aClass) {
            return source.context(aClass);
        }
    }

}