package kfs.kfsDbiStd;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import kfs.kfsDbi.*;

/**
 *
 * @author pavedrim
 * @param <DBI1>
 * @param <T1>
 * @param <DBI2>
 * @param <T2>
 */
public class kfsRelationGen<DBI1 extends kfsColObject, T1, DBI2 extends kfsColObject, T2> extends kfsDbObject {

    private final kfsLongAutoInc id;
    private final DBI1 id1;
    private final DBI2 id2;
    private final kfsICreateDbc<DBI1, T1> dboc1;
    private final kfsICreateDbc<DBI2, T2> dboc2;

    public kfsRelationGen(kfsDbServerType st, String tableName, 
            kfsICreateDbc<DBI1, T1> dboc1, kfsICreateDbc<DBI2, T2> dboc2) {
        super(st, tableName);
        int pos = 0;
        this.dboc1 = dboc1;
        this.dboc2 = dboc2;
        super.setColumns((id = new kfsLongAutoInc("ID", pos++)),
                (id1 = dboc1.createDbo(pos++)),
                (id2 = dboc2.createDbo(pos++)));
        super.setIdsColumns(id);
    }

    public String getSelect1By2() {
        return getSelect(getName(), new kfsDbiColumn[]{id1}, new kfsDbiColumn[]{id2});
    }

    public String getSelect2By1() {
        return getSelect(getName(), new kfsDbiColumn[]{id2}, new kfsDbiColumn[]{id1});
    }

    public String getSelect1By2(String innerSql) {
        return getSelect(getName(), new kfsDbiColumn[]{id1}) + " WHERE " + id2.getColumnName()
                + " IN ( " + innerSql + " ) ";
    }

    public String getSelect2By1(String innerSql) {
        return getSelect(getName(), new kfsDbiColumn[]{id2}) + " WHERE " + id1.getColumnName()
                + " IN ( " + innerSql + " ) ";
    }

    public pjRelation create(T1 id1, T2 id2) {
        kfsRowData rd = new kfsRowData(this);
        this.id1.setObject(id1, rd);
        this.id2.setObject(id2, rd);
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

    public void psSelectById1(PreparedStatement ps, T1 id1) throws SQLException {
        dboc1.setPsData(ps, 1, id1);
    }

    public void psSelectById2(PreparedStatement ps, T2 id2) throws SQLException {
        dboc2.setPsData(ps, 1, id2);
    }

    public String sqlDeleteById1Id2() {
        return getDelete(id1, id2);
    }

    public void psDeleteById1Id2(PreparedStatement ps, T1 id1i, T2 id2i) throws SQLException {
        dboc1.setPsData(ps, 1, id1i);
        dboc2.setPsData(ps, 2, id2i);
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

    public void psDeleteById1(PreparedStatement ps, T1 id1) throws SQLException {
        dboc1.setPsData(ps, 1, id1);
    }

    public void psDeleteById2(PreparedStatement ps, T2 id2) throws SQLException {
        dboc2.setPsData(ps, 1, id2);
    }

    @Override
    public pjRelation getPojo(kfsRowData rd) {
        return new pjRelation(rd);
    }

    public class pjRelation extends kfsPojoObj<kfsRelationGen> {

        pjRelation(kfsRowData rd) {
            super(kfsRelationGen.this, rd);
        }

        public long getId() {
            return inx.id.getData(rd);
        }

        public T1 getId1() {
            return (T1) inx.id1.getObject(rd);
        }

        public T2 getId2() {
            return (T2) inx.id2.getObject(rd);
        }

        @Override
        public String toString() {
            return "ID: " + Long.toString(getId()) + ", ID1: " + getId1() + ", ID2: " + getId2();
        }
    }

    public class lstRelation implements kfsADb.loadCB, Iterable<pjRelation> {

        private final ArrayList<pjRelation> lst = new ArrayList<pjRelation>();

        public pjRelation find(T1 id1, T2 id2) {
            for (pjRelation r : lst) {
                if (r.getId1().equals(id1) && r.getId2().equals(id2)) {
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

        public Iterator<pjRelation> getById1Iterator(final T1 id1) {
            return new IteratorId(lst.iterator(), id1, null);
        }

        public Iterable<pjRelation> getById1(final T1 id1) {
            return new Iterable<pjRelation>() {
                @Override
                public Iterator<pjRelation> iterator() {
                    return new IteratorId(lst.iterator(), id1, null);
                }
            };
        }

        public Iterable<pjRelation> getById2(final T2 id2) {
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
        private final T1 id1;
        private final T2 id2;
        private pjRelation next;

        IteratorId(Iterator<pjRelation> lst, T1 id1, T2 id2) {
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

    public abstract class relIterator<TT> implements Iterator<TT> {

        private final Iterator<pjRelation> it;

        public relIterator(Iterator<pjRelation> it) {
            this.it = it;
        }

        protected abstract TT getType(pjRelation rel);

        @Override
        public boolean hasNext() {
            return it.hasNext();
        }

        @Override
        public TT next() {
            return getType(it.next());
        }

        @Override
        public void remove() {
        }
    }

}
