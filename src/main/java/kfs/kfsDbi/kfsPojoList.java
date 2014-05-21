package kfs.kfsDbi;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author pavedrim
 * @param <B> DBO object
 * @param <ID> ID type - for unique in list identification
 * @param <T> pojo type - based on <B>
 */
public class kfsPojoList<B extends kfsDbObject, ID, T extends kfsPojoObj<B>> implements kfsADb.loadCB, Collection<T> {

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
        return lst.containsValue((T)o);
    }

    @Override
    public Object[] toArray() {
        return lst.values().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return lst.values().toArray(a);
    }

    @Override
    public boolean remove(Object o) {
        lst.remove(getId.getUniqueId((T)o));
        return true;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!lst.containsValue((T)o))
                return false;
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
       for (T o : c) {
           add(o);
       }
       return true;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        for (Object o : c) {
            remove((T)o);
        }
        return true;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override
    public void clear() {
        lst.clear();
    }

    public static interface getId<B extends kfsDbObject, ID, T extends kfsPojoObj<B>>  {
        ID getUniqueId(T pj);
    }
    
    private final B base;
    private final getId<B,ID,T> getId;

    public kfsPojoList(B base, getId<B,ID,T> getId) {
        this.base = base;
        this.getId = getId;
    }
    private final HashMap<ID, T> lst = new HashMap<ID, T>();

    /**
     *
     * @param rd
     * @return
     */
    @Override
    public boolean add(T rd) {
        lst.put(getId.getUniqueId(rd), rd);
        return true;
    }

    @Override
    public boolean kfsDbAddItem(kfsRowData rd) {
        add((T) base.getPojo(rd));
        return true;
    }

    public T get(ID id) {
        return lst.get(id);
    }

    @Override
    public Iterator<T> iterator() {
        return lst.values().iterator();
    }

    public Collection<T> collection() {
        return lst.values();
    }
    
}
