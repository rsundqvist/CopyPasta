package model;

import java.util.ArrayList;
import java.util.Collection;

/** Created by Richard Sundqvist on 20/02/2017. */
public class UniqueArrayList<E> extends ArrayList<E> {

    @Override
    public boolean add(E e) {
        if (super.contains(e)) return false;

        return super.add(e);
    }

    @Override
    public void add(int index, E e) {
        if (!contains(e)) super.add(index, e);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return super.addAll(ensureUnique(c));
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        return super.addAll(index, ensureUnique(c));
    }

    private ArrayList<E> ensureUnique(Collection<? extends E> c) {
        ArrayList<E> uniqueList = new ArrayList<>(c.size());

        for (E e : c) if (!(super.contains(e) || uniqueList.contains(e))) uniqueList.add(e);
        return uniqueList;
    }
}
