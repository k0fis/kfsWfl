package kfs.kfsDbi;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author pavedrim
 */
public class kfsPojoList<B extends kfsDbObject, ID, T extends kfsPojoObj<B>> implements kfsADb.loadCB, Iterable<T> {

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

    public void add(T rd) {
        lst.put(getId.getUniqueId(rd), rd);
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
