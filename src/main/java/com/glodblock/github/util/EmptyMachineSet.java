package com.glodblock.github.util;

import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IMachineSet;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class EmptyMachineSet implements IMachineSet {

    private final Class<? extends IGridHost> type;

    public static EmptyMachineSet create(final Class<? extends IGridHost> type) {
        return new EmptyMachineSet(type);
    }

    private EmptyMachineSet(final Class<? extends IGridHost> clazz) {
        this.type = clazz;
    }

    @Nonnull
    @Override
    public Class<? extends IGridHost> getMachineClass() {
        return this.type;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public boolean contains(final Object o) {
        return false;
    }

    @Nonnull
    @Override
    public Iterator<IGridNode> iterator() {
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public IGridNode next() {
                throw new NoSuchElementException();
            }
        };
    }

}
