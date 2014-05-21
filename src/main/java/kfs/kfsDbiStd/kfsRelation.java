package kfs.kfsDbiStd;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import kfs.kfsDbi.kfsADb;
import kfs.kfsDbi.kfsDbObject;
import kfs.kfsDbi.kfsDbServerType;
import kfs.kfsDbi.kfsDbiColumn;
import kfs.kfsDbi.kfsIPojoObj;
import kfs.kfsDbi.kfsInt;
import kfs.kfsDbi.kfsIntAutoInc;
import kfs.kfsDbi.kfsPojoObj;
import kfs.kfsDbi.kfsRowData;

/**
 *
 * @author pavedrim
 */
public class kfsRelation extends kfsDbObject {

    private final kfsIntAutoInc id;
    private final kfsInt id1;
    private final kfsInt id2;

    public kfsRelation(kfsDbServerType st, String tableName) {
        this(st, tableName, "ID1", "ID2");
    }

    public kfsRelation(kfsDbServerType st, String tableName, String id1Name, String id2Name) {
        super(st, tableName);
        int pos = 0;
        id = new kfsIntAutoInc("ID", "Id.", pos++);
        id1 = new kfsInt(id1Name, id1Name, kfsIntAutoInc.idMaxLen, pos++, false);
        id2 = new kfsInt(id2Name, id2Name, kfsIntAutoInc.idMaxLen, pos++, false);
        super.setIdsColumns(id);
        super.setColumns(id, id1, id2);
    }

    public String getSelect1By2() {
        return getSelect(getName(), new kfsDbiColumn[]{id1}, new kfsDbiColumn[]{id2});
    }

    public String getSelect2By1() {
        return getSelect(getName(), new kfsDbiColumn[]{id2}, new kfsDbiColumn[]{id1});
    }

    public pjRelation create(int id1, int id2) {
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

    public void psSelectById(PreparedStatement ps, int id) throws SQLException {
        ps.setInt(1, id);
    }

    public void psSelectById1(PreparedStatement ps, int id1) throws SQLException {
        ps.setInt(1, id1);
    }

    public void psSelectById2(PreparedStatement ps, int id2) throws SQLException {
        ps.setInt(1, id2);
    }

    public String sqlDeleteById1Id2() {
        return getDelete(id1, id2);
    }

    public void psDeleteById1Id2(PreparedStatement ps, int id1i, int id2i) throws SQLException {
        ps.setInt(1, id1i);
        ps.setInt(2, id2i);
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

    public void psDeleteById(PreparedStatement ps, int id) throws SQLException {
        ps.setInt(1, id);
    }

    public void psDeleteById1(PreparedStatement ps, int id1) throws SQLException {
        ps.setInt(1, id1);
    }

    public void psDeleteById2(PreparedStatement ps, int id2) throws SQLException {
        ps.setInt(1, id2);
    }

    @Override
    public pjRelation getPojo(kfsRowData rd) {
        return new pjRelation(rd);
    }

    public class pjRelation extends kfsPojoObj<kfsRelation> {

        pjRelation(kfsRowData rd) {
            super(kfsRelation.this, rd);
        }

        public int getId() {
            return inx.id.getData(rd);
        }

        public int getId1() {
            return inx.id1.getData(rd);
        }

        public int getId2() {
            return inx.id2.getData(rd);
        }

        @Override
        public String toString() {
            return "ID: " + Integer.toString(getId()) + ", ID1: " + Integer.toString(getId1()) + ", ID2: " + Integer.toString(getId2());
        }
    }

    public class lstRelation implements kfsADb.loadCB, Iterable<pjRelation> {

        private final ArrayList<pjRelation> lst = new ArrayList<pjRelation>();

        public pjRelation find(int id1, int id2) {
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

        public Iterator<pjRelation> getById1Iterator(final int id1) {
            return new IteratorId(lst.iterator(), id1, null);
        }

        public Iterable<pjRelation> getById1(final int id1) {
            return new Iterable<pjRelation>() {
                @Override
                public Iterator<pjRelation> iterator() {
                    return new IteratorId(lst.iterator(), id1, null);
                }
            };
        }

        public Iterable<pjRelation> getById2(final int id2) {
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
        private final Integer id1;
        private final Integer id2;
        private pjRelation next;

        IteratorId(Iterator<pjRelation> lst, Integer id1, Integer id2) {
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
