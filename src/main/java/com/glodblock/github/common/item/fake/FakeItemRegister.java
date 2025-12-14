package com.glodblock.github.common.item.fake;

import appeng.api.storage.data.IAEItemStack;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public final class FakeItemRegister {

    private static final Reference2ObjectMap<Class<? extends Item>, FakeItemHandler<?, ?>> HANDLERS = new Reference2ObjectOpenHashMap<>();

    public static void registerHandler(final Class<? extends Item> host, final FakeItemHandler<?, ?> handler) {
        if (host == null) {
            throw new IllegalArgumentException("Null fake item");
        }
        if (HANDLERS.containsKey(host)) {
            throw new IllegalArgumentException("Duplicate item handler.");
        }
        HANDLERS.put(host, handler);
    }

    public static <T> T getStack(final ItemStack stack) {
        final Item item = stack.getItem();
        final FakeItemHandler<T, ?> handler = checkItem(item);
        return handler == null ? null : handler.getStack(stack);
    }

    public static <T> T getStack(final IAEItemStack stack) {
        if (stack == null) {
            return null;
        }
        final Item item = stack.getItem();
        final FakeItemHandler<T, ?> handler = checkItem(item);
        return handler == null ? null : handler.getStack(stack);
    }

    public static <T> T getAEStack(final ItemStack stack) {
        final Item item = stack.getItem();
        final FakeItemHandler<?, T> handler = checkItem(item);
        return handler == null ? null : handler.getAEStack(stack);
    }

    public static <T> T getAEStack(final IAEItemStack stack) {
        if (stack == null) {
            return null;
        }
        final Item item = stack.getItem();
        final FakeItemHandler<?, T> handler = checkItem(item);
        return handler == null ? null : handler.getAEStack(stack);
    }

    public static <T> ItemStack packStack(final T target, final Item host) {
        final FakeItemHandler<T, ?> handler = checkItem(host);
        return handler == null ? null : handler.packStack(target);
    }

    public static <T> ItemStack displayStack(final T target, final Item host) {
        final FakeItemHandler<T, ?> handler = checkItem(host);
        return handler == null ? null : handler.displayStack(target);
    }

    public static <T> IAEItemStack packAEStack(final T target, final Item host) {
        final FakeItemHandler<T, ?> handler = checkItem(host);
        return handler == null ? null : handler.packAEStack(target);
    }

    public static <T> IAEItemStack packAEStackLong(final T target, final Item host) {
        final FakeItemHandler<?, T> handler = checkItem(host);
        return handler == null ? null : handler.packAEStackLong(target);
    }

    public static boolean isFakeItem(final ItemStack stack) {
        return HANDLERS.containsKey(stack.getItem().getClass());
    }

    private static <V, A> FakeItemHandler<V, A> checkItem(final Item host) {
        if (HANDLERS.containsKey(host.getClass())) {
            return (FakeItemHandler<V, A>) HANDLERS.get(host.getClass());
        }
        return null;
    }

}
