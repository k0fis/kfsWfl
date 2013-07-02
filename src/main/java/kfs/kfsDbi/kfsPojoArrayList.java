package kfs.kfsDbi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author pavedrim
 */
public class kfsPojoArrayList<B extends kfsDbObject, T extends kfsPojoObj<B>> implements kfsADb.loadCB, Collection<T> {

    @Override
    public int size() {
        return lst.size();
    }

    @Override
    public boolean isEmpty() {
        return lst.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return lst.contains(o);
    }

    @Override
    public Object[] toArray() {
        return lst.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return lst.toArray(a);
    }

    @Override
    public boolean remove(Object o) {
        return lst.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return lst.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
       return lst.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return lst.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return lst.removeAll(c);
    }

    @Override
    public void clear() {
        lst.clear();
    }

    
    private final B base;

    public kfsPojoArrayList(B base) {
        this.base = base;
    }
    private final ArrayList<T> lst = new ArrayList<T>();

    /**
     *
     * @param rd
     * @return
     */
    @Override
    public boolean add(T rd) {
        return lst.add(rd);
    }

    @Override
    public boolean kfsDbAddItem(kfsRowData rd) {
        add((T) base.getPojo(rd));
        return true;
    }

    @Override
    public Iterator<T> iterator() {
        return lst.iterator();
    }

}
