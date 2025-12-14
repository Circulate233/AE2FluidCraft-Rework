package com.glodblock.github.util;

import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IMachineSet;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class SetBackedMachineSet implements IMachineSet {

    private final Class<? extends IGridHost> machineClass;
    private final List<IMachineSet> backingSet = new ArrayList<>();
    private int count;

    public static IMachineSet combine(final Class<? extends IGridHost> machineClass, final IMachineSet... backingSet) {
        final SetBackedMachineSet packed = new SetBackedMachineSet(machineClass, backingSet);
        if (packed.backingSet.isEmpty()) {
            return EmptyMachineSet.create(machineClass);
        }
        if (packed.backingSet.size() == 1) {
            return packed.backingSet.get(0);
        }
        return packed;
    }

    private SetBackedMachineSet(final Class<? extends IGridHost> machineClass, final IMachineSet... backingSet) {
        this.machineClass = machineClass;
        this.count = 0;
        for (final IMachineSet set : backingSet) {
            if (!set.isEmpty()) {
                this.count += set.size();
                this.backingSet.add(set);
            }
        }
    }

    @Nonnull
    @Override
    public Class<? extends IGridHost> getMachineClass() {
        return machineClass;
    }

    @Override
    public int size() {
        return this.count;
    }

    @Override
    public boolean isEmpty() {
        for (final IMachineSet set : this.backingSet) {
            if (!set.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean contains(final Object o) {
        for (final IMachineSet set : this.backingSet) {
            if (set.contains(o)) {
                return true;
            }
        }
        return false;
    }

    @Override
    @Nonnull
    public Iterator<IGridNode> iterator() {
        return new CombinedIterator();
    }

    @Override
    public void forEach(final Consumer<? super IGridNode> action) {
        for (final IMachineSet set : this.backingSet) {
            set.forEach(action);
        }
    }

    private class CombinedIterator implements Iterator<IGridNode> {
        Iterator<IGridNode> currentIter;
        final Iterator<Iterator<IGridNode>> iterators;

        CombinedIterator() {
            final List<Iterator<IGridNode>> list = new ArrayList<>();
            for (final IMachineSet set : SetBackedMachineSet.this.backingSet) {
                list.add(set.iterator());
            }
            this.iterators = list.iterator();
            this.nextSet();
        }

        private void nextSet() {
            currentIter = iterators.next();
        }

        @Override
        public boolean hasNext() {
            if (currentIter.hasNext()) {
                return true;
            }
            if (iterators.hasNext()) {
                this.nextSet();
                return this.hasNext();
            }
            return false;
        }

        @Override
        public IGridNode next() {
            return currentIter.next();
        }

    }

}