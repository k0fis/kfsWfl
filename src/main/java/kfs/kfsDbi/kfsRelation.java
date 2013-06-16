package kfs.kfsDbi;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

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
        int pos = 1;
        id = new kfsIntAutoInc("ID", "Id.", pos++);
        id1 = new kfsInt(id1Name, id1Name, kfsIntAutoInc.idMaxLen, pos++, false);
        id2 = new kfsInt(id2Name, id2Name, kfsIntAutoInc.idMaxLen, pos++, false);
        super.setIdsColumns(id);
        super.setColumns(id, id1, id2);
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
    public kfsIPojoObj getPojo(kfsRowData rd) {
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
            return inx.id2.getData(rd);
        }

        public int getId2() {
            return inx.id2.getData(rd);
        }
    }

    public class lstRelation implements kfsADb.loadCB, Iterable<pjRelation> {

        private final ArrayList<pjRelation> lst = new ArrayList<pjRelation>();

        @Override
        public boolean kfsDbAddItem(kfsRowData rd) {
            lst.add(new pjRelation(rd));
            return true;
        }

        @Override
        public Iterator<pjRelation> iterator() {
            return lst.iterator();
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
}
