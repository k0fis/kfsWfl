package kfs.kfsDbiStd;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import kfs.kfsDbi.kfsADb;
import kfs.kfsDbi.kfsDbObject;
import kfs.kfsDbi.kfsDbServerType;
import kfs.kfsDbi.kfsLong;
import kfs.kfsDbi.kfsLongAutoInc;
import kfs.kfsDbi.kfsPojoObj;
import kfs.kfsDbi.kfsRowData;

/**
 *
 * @author pavedrim
 */
public class kfsRelationLong extends kfsDbObject {

    private final kfsLongAutoInc id;
    private final kfsLong id1;
    private final kfsLong id2;

    public kfsRelationLong(kfsDbServerType st, String tableName) {
        this(st, tableName, "ID1", "ID2");
    }

    public kfsRelationLong(kfsDbServerType st, String tableName, String id1Name, String id2Name) {
        super(st, tableName);
        int pos = 0;
        id = new kfsLongAutoInc("ID", pos++);
        id1 = new kfsLong(id1Name, id1Name, kfsLongAutoInc.idMaxLen, pos++, false);
        id2 = new kfsLong(id2Name, id2Name, kfsLongAutoInc.idMaxLen, pos++, false);
        super.setIdsColumns(id);
        super.setColumns(id, id1, id2);
    }

    public pjRelation create(long id1, long id2) {
        kfsRowData rd = new kfsRowData(this);
        this.id1.setData(id1, rd);
        this.id2.setData(id2, rd);
        return new pjRelation(rd);
    }

    public String sqlSelectById() {
        return getSelect(getName(), getColumns(), id);
    }

    public String sqlSelectById1() {
        return getSelect(getName(), getColumns(), id1);
    }

    public String sqlSelectById2() {
        return getSelect(getName(), getColumns(), id2);
    }

    public void psSelectById(PreparedStatement ps, long id) throws SQLException {
        ps.setLong(1, id);
    }

    public void psSelectById1(PreparedStatement ps, long id1) throws SQLException {
        ps.setLong(1, id1);
    }

    public void psSelectById2(PreparedStatement ps, long id2) throws SQLException {
        ps.setLong(1, id2);
    }

    public String sqlDeleteById() {
        return getDelete(id);
    }

    public String sqlDeleteById1() {
        return getDelete(id1);
    }

    public String sqlDeleteById2() {
        return getDelete(id2);
    }

    public void psDeleteById(PreparedStatement ps, long id) throws SQLException {
        ps.setLong(1, id);
    }

    public void psDeleteById1(PreparedStatement ps, long id1) throws SQLException {
        ps.setLong(1, id1);
    }

    public void psDeleteById2(PreparedStatement ps, long id2) throws SQLException {
        ps.setLong(1, id2);
    }

    @Override
    public pjRelation getPojo(kfsRowData rd) {
        return new pjRelation(rd);
    }

    public class pjRelation extends kfsPojoObj<kfsRelationLong> {

        pjRelation(kfsRowData rd) {
            super(kfsRelationLong.this, rd);
        }

        public long getId() {
            return inx.id.getData(rd);
        }

        public long getId1() {
            return inx.id1.getData(rd);
        }

        public long getId2() {
            return inx.id2.getData(rd);
        }

        @Override
        public String toString() {
            return "ID: " + Long.toString(getId()) + ", ID1: " + Long.toString(getId1()) + ", ID2: " + Long.toString(getId2());
        }
    }

    public class lstRelation implements kfsADb.loadCB, Iterable<pjRelation> {

        private final ArrayList<pjRelation> lst = new ArrayList<pjRelation>();

        public pjRelation find(long id1, long id2) {
            for (pjRelation r : lst) {
                if ((r.getId1() == id1) && (r.getId2() == id2)) {
                    return r;
                }
            }
            return null;
        }

        public boolean add(pjRelation pj) {
            return lst.add(pj);
        }

        public boolean remove(pjRelation pj) {
            return lst.remove(pj);
        }

        @Override
        public boolean kfsDbAddItem(kfsRowData rd) {
            lst.add(new pjRelation(rd));
            return true;
        }

        @Override
        public Iterator<pjRelation> iterator() {
            return lst.iterator();
        }

        public Iterator<pjRelation> getById1Iterator(final long id1) {
            return new IteratorId(lst.iterator(), id1, null);
        }

        public Iterable<pjRelation> getById1(final long id1) {
            return new Iterable<pjRelation>() {
                @Override
                public Iterator<pjRelation> iterator() {
                    return new IteratorId(lst.iterator(), id1, null);
                }
            };
        }

        public Iterable<pjRelation> getById2(final long id2) {
            return new Iterable<pjRelation>() {
                @Override
                public Iterator<pjRelation> iterator() {
                    return new IteratorId(lst.iterator(), null, id2);
                }
            };
        }
    }

    private class IteratorId implements Iterator<pjRelation> {

        private final Iterator<pjRelation> lst;
        private final Long id1;
        private final Long id2;
        private pjRelation next;

        IteratorId(Iterator<pjRelation> lst, Long id1, Long id2) {
            this.lst = lst;
            this.id1 = id1;
            this.id2 = id2;
            this.next = null;
        }

        @Override
        public boolean hasNext() {
            while (true) {
                if (!lst.hasNext()) {
                    next = null;
                    return false;
                }
                next = lst.next();
                if ((id1 != null) && (id1 == next.getId1())) {
                    return true;
                }
                if ((id2 != null) && (id2 == next.getId2())) {
                    return true;
                }
            }
        }

        @Override
        public pjRelation next() {
            return next;
        }

        @Override
        public void remove() {
        }
    }

    public static abstract class relIterator<T> implements Iterator<T> {

        private Iterator<pjRelation> it;

        public relIterator(Iterator<pjRelation> it) {
            this.it = it;
        }

        protected abstract T getType(pjRelation rel);

        @Override
        public boolean hasNext() {
            return it.hasNext();
        }

        @Override
        public T next() {
            return getType(it.next());
        }

        @Override
        public void remove() {
        }
    }
}
