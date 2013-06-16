package kfs.kfsDbi;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pavedrim
 */
public abstract class kfsADb {

    public interface loadCB {

        boolean kfsDbAddItem(kfsRowData rd);
    };
    protected static final Logger l = Logger.getLogger(kfsADb.class.getName());
    private final HashMap<String, PreparedStatement> closingList;
    private final Connection conn;
    private final kfsDbServerType serverType;
    private final String schema_;
    private List<kfsDbObject> dbObjects;

    protected kfsADb(
            final String schema,///
            final kfsDbServerType serverType, //
            final Connection conn) {
        this.closingList = new HashMap<String, PreparedStatement>();
        this.conn = conn;
        this.serverType = serverType;
        this.schema_ = schema;
        this.dbObjects = Arrays.<kfsDbObject>asList();
        if ((schema != null) && (!schema.isEmpty()) && (serverType == kfsDbServerType.kfsDbiPostgre)) {
            try {
                String sql = "set search_path to '" + schema + "'";
                l.log(Level.INFO, sql);
                prepare(sql).execute();
            } catch (SQLException ex) {
                l.log(Level.SEVERE, "Cannot set search_path", ex);
            }
        }
    }

    protected void setDboObjects(kfsDbObject... objs) {
        dbObjects = Arrays.asList(objs);
    }

    protected Collection<kfsDbObject> getDbObjects() {
        return dbObjects;
    }

    public kfsDbObject getDbObjectByName(String name) {
        for (kfsDbObject dbo : getDbObjects()) {
            if (dbo.getName().equals(name)) {
                return dbo;
            }
        }
        return null;
    }

    protected Collection<kfsDbObject> getFulltextObjects() {
        return Arrays.<kfsDbObject>asList();
    }

    protected kfsDbServerType getServerType() {
        return serverType;
    }

    protected String getSchema() {
        return schema_;
    }

    protected PreparedStatement getInsert(kfsDbiTable tab) {
        String sql = tab.getInsertInto();
        if (!closingList.containsKey(sql)) {
            try {

                PreparedStatement ps;
                if (tab.hasGenerateAutoKeys()) {
                    if (this.serverType == kfsDbServerType.kfsDbiOracle) {
                        ps = conn.prepareStatement(sql, tab.getAutoGeneratedColumnNames());
                    } else {
                        ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    }
                } else {
                    ps = conn.prepareStatement(sql);
                }
                closingList.put(sql, ps);
                return ps;
            } catch (SQLException ex) {
                l.log(Level.SEVERE, "Error in call getInsert for sql: " + sql, ex);
            }
        }
        return closingList.get(sql);
    }

    protected PreparedStatement getUpdate(kfsDbiTable tab) {
        String sql = tab.getUpdate();
        if (sql == null) {
            return null;
        }
        try {
            return prepare(sql);
        } catch (SQLException ex) {
            l.log(Level.SEVERE, "Cannot prepare statement for SQL: " + sql, ex);
        }
        return null;
    }

    protected PreparedStatement getSelect(kfsDbiTable tab) {
        try {
            return prepare(tab.getSelect());
        } catch (SQLException ex) {
            l.log(Level.SEVERE, "Error in call getSelect for dbName: " + tab.getName(), ex);
        }
        return null;
    }

    protected CallableStatement prepareCs(String sql) throws SQLException {
        CallableStatement ps = (CallableStatement) closingList.get(sql);
        if (ps == null) {
            ps = conn.prepareCall(sql);
            closingList.put(sql, ps);
        }
        return ps;
    }

    protected final PreparedStatement prepare(String sql) throws SQLException {
        PreparedStatement ps = closingList.get(sql);
        if (ps == null) {
            ps = conn.prepareStatement(sql);
            closingList.put(sql, ps);
        }
        return ps;
    }

    public void commit() throws SQLException {
        conn.commit();
    }

    public void done(boolean commit, boolean rollback) throws SQLException {
        if (conn == null) {
            return;
        }
        if (conn.isClosed()) {
            return;
        }
        if (commit) {
            conn.commit();
        } else {
            if (rollback) {
                conn.rollback();
            }
        }
        for (PreparedStatement ps : closingList.values()) {
            ps.close();
        }
        conn.close();
    }

    private String getExist() {
        switch (serverType) {
            case kfsDbiSqlite:
                return "SELECT name FROM sqlite_master WHERE type='table' AND name=?";
            case kfsDbiMysql:
                return "SELECT table_name FROM information_schema.tables WHERE "
                        + "table_schema = ? AND table_name = ?";
            case kfsDbiOracle:
                return "SELECT table_name FROM all_tables WHERE owner=? AND table_name =?";
            case kfsDbiPostgre:
                return "SELECT tablename FROM pg_catalog.pg_tables WHERE "
                        + "lower(schemaname)=lower(?) AND lower(tablename)=lower(?)";
        }
        return null;
    }

    @Deprecated
    protected void reCreateTables() {
        for (kfsDbiTable ie : getDbObjects()) {
            if (ie == null) {
                throw new RuntimeException("dbObject cannot be null");
            }
            try {
                PreparedStatement ps = prepare("DROP TABLE " + ie.getName());
                ps.execute();
            } catch (SQLException ex) {
                l.log(Level.SEVERE, "Cannot drop " + ie.getName(), ex);
            }

        }
        createTables();
    }

    protected void createTables() {
        createTables(schema_);
    }

    protected void createTables(String schema) {
        try {
            String esql = getExist();
            if (esql == null) {
                l.log(Level.WARNING, "SQL not-exist for server: {0}", serverType);
                return;
            }
            l.log(Level.FINE, "SQL exist: {0} - {1}", new Object[]{esql, serverType});
            PreparedStatement psExistTable = conn.prepareStatement(esql);
            Statement executeStatement = conn.createStatement();
            for (kfsDbiTable ie : getDbObjects()) {
                l.log(Level.FINE, "Create table {0} begin", ie.getName());

                if (ie == null) {
                    throw new RuntimeException("dbObject cannot be null");
                }
                String sql = "";
                try {
                    sql = ie.getCreateTable();
                    if ((sql != null) && (sql.length() > 0)) {
                        if ((schema != null) && (schema.length() > 0)) {
                            psExistTable.setString(1, schema);
                            psExistTable.setString(2, ie.getName());
                        } else {
                            psExistTable.setString(1, ie.getName());
                        }
                        ResultSet rs = psExistTable.executeQuery();
                        boolean ret = rs.next();
                        rs.close();
                        if (!ret) {
                            if (sql != null) {
                                l.log(Level.INFO, "SQL create table: {0}", sql);
                                executeStatement.execute(sql);
                            }
                            for (String ss : ie.getCreateTableAddons()) {
                                sql = ss;
                                l.log(Level.INFO, "SQL table addon: {0}", sql);
                                executeStatement.execute(sql);
                            }
                            String ft = ie.createFullTextIndex();
                            if (ft.length() > 0) {
                                sql = ft;
                                l.log(Level.INFO, "SQL table FullText index: {0}", sql);
                                executeStatement.execute(sql);
                            }
                        }
                    }
                } catch (Exception ex) {
                    l.log(Level.SEVERE, "Error in " + ie.getName() + ".createTable: " + sql, ex);
                }
                l.log(Level.FINE, "Create table {0} done", ie.getName());

            }
            executeStatement.close();
            psExistTable.close();
        } catch (SQLException ex) {
            l.log(Level.SEVERE, "Error in createTable", ex);
        }

    }

    protected int loadAll(ArrayList<kfsRowData> data, kfsDbObject inx) {
        int ret = data.size();
        try {
            ResultSet rs = null;
            try {
                rs = getSelect(inx).executeQuery();
                while (rs.next()) {
                    kfsRowData r = new kfsRowData(inx);
                    inx.psSelectGetParameters(rs, r);
                    data.add(r);
                }
            } finally {
                if (rs != null) {
                    rs.close();
                }
            }
        } catch (SQLException ex) {
            l.log(Level.SEVERE, "Error in " + inx.getName() + ".loadAll", ex);

        }
        return data.size() - ret;
    }

    protected int loadAll(loadCB loadCb, kfsDbObject inx) {
        int ret = 0;
        try {
            ResultSet rs = null;
            try {
                rs = getSelect(inx).executeQuery();
                while (rs.next()) {
                    kfsRowData r = new kfsRowData(inx);
                    inx.psSelectGetParameters(rs, r);
                    loadCb.kfsDbAddItem(r);
                    ret++;
                }
            } finally {
                if (rs != null) {
                    rs.close();
                }
            }
        } catch (SQLException ex) {
            l.log(Level.SEVERE, "Error in " + inx.getName() + ".loadAll " + getSelect(inx), ex);
        }
        return ret;
    }

    public void sort(kfsDbiColumnComparator[] sColumns, List<kfsRowData> lst, kfsDbObject o) {
        o.sort(sColumns, lst);
    }

    protected int loadCust(PreparedStatement ps, ArrayList<kfsRowData> data, kfsDbObject inx) throws SQLException {
        int ret = data.size();
        ResultSet rs = null;
        try {
            rs = ps.executeQuery();
            while (rs.next()) {
                kfsRowData r = new kfsRowData(inx);
                inx.psSelectGetParameters(rs, r);
                data.add(r);
            }
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
        return data.size() - ret;
    }

    protected int loadCust(CallableStatement ps, int resInx, loadCB loadCb, kfsDbObject inx) throws SQLException {
        int ret = 0;
        ResultSet rs = null;
        try {
            ps.registerOutParameter(resInx, -10); //REF CURSOR OracleTypes.CURSOR
            ps.execute();
            rs = (ResultSet) ps.getObject(resInx);
            while (rs.next()) {
                kfsRowData r = new kfsRowData(inx);
                inx.psSelectGetParameters(rs, r);
                loadCb.kfsDbAddItem(r);
                ret++;
            }
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
        return ret;
    }

    protected int loadCust(PreparedStatement ps, loadCB loadCb, kfsDbObject inx) throws SQLException {
        return loadCust(ps, loadCb, inx, inx);
    }

    protected int loadCust(PreparedStatement ps, loadCB loadCb, kfsDbiTable inx, kfsTableDesc desc) throws SQLException {
        int ret = 0;
        ResultSet rs = null;
        try {
            rs = ps.executeQuery();
            while (rs.next()) {
                kfsRowData r = new kfsRowData(desc);
                inx.psSelectGetParameters(rs, r);
                loadCb.kfsDbAddItem(r);
                ret++;
            }
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
        return ret;
    }

    public int update(kfsPojoObj<? extends kfsDbObject> pj) {
        return update(pj.kfsGetDbObject(), pj.kfsGetRow());
    }

    public kfsReleation.pjRelation createRelation (kfsReleation tab, int id1, int id2) {
        kfsReleation.pjRelation ret = tab.create(id1, id2);
        insert(tab, ret.kfsGetRow());
        return ret;
    }
    
    public int loadRelationsAll(kfsReleation tab, kfsReleation.lstReleation lst) {
        return loadAll(lst, tab);
    }

    public int loadRelationsById(kfsReleation tab, int id, kfsReleation.lstReleation lst) throws SQLException {
        PreparedStatement ps = prepare(tab.sqlSelectById());
        tab.psSelectById1(ps, id);
        return loadCust(ps, lst, tab);
    }

    public int loadRelationsById1(kfsReleation tab, int id1, kfsReleation.lstReleation lst) throws SQLException {
        PreparedStatement ps = prepare(tab.sqlSelectById1());
        tab.psSelectById1(ps, id1);
        return loadCust(ps, lst, tab);
    }

    public int loadRelationsById2(kfsReleation tab, int id2, kfsReleation.lstReleation lst) throws SQLException {
        PreparedStatement ps = prepare(tab.sqlSelectById2());
        tab.psSelectById1(ps, id2);
        return loadCust(ps, lst, tab);
    }

    public boolean deleteRelationsById(kfsReleation tab, int id) throws SQLException {
        PreparedStatement ps = prepare(tab.sqlDeleteById());
        tab.psSelectById1(ps, id);
        return ps.execute();
    }

    public boolean deleteRelationsById1(kfsReleation tab, int id1) throws SQLException {
        PreparedStatement ps = prepare(tab.sqlDeleteById1());
        tab.psSelectById1(ps, id1);
        return ps.execute();
    }

    public boolean deleteRelationsById2(kfsReleation tab, int id2) throws SQLException {
        PreparedStatement ps = prepare(tab.sqlDeleteById2());
        tab.psSelectById1(ps, id2);
        return ps.execute();
    }

    protected boolean delete(kfsDbiTable tab, kfsRowData r) {
        String sql = tab.getDelete();
        if (sql != null) {
            try {
                PreparedStatement ps = prepare(sql);
                ps.clearParameters();
                tab.psSetDelete(ps, r);
                ps.executeUpdate();
                return true;
            } catch (SQLException ex) {
                l.log(Level.SEVERE, "Cannot delete entry: " + sql, ex);
            }
        } else {
            l.log(Level.WARNING, "Try call to Delete for DBI: {0}", tab.getName());
        }
        return false;
    }

    protected int insertExc(kfsDbiTable tab, kfsRowData row) throws SQLException {
        int ret = 0;

        PreparedStatement ps = getInsert(tab);
        ps.clearParameters();
        tab.psInsertSetParameters(ps, row);
        ps.execute();
        ret++;
        if (tab.hasGenerateAutoKeys()) {
            ResultSet rs = getInsert(tab).getGeneratedKeys();
            if (rs.next()) {
                tab.psInsertGetAutoKeys(rs, row);
            }
            rs.close();
        }

        return ret;
    }

    protected int insert(kfsDbiTable tab, kfsRowData row) {
        int ret = 0;
        try {
            PreparedStatement ps = getInsert(tab);
            ps.clearParameters();
            tab.psInsertSetParameters(ps, row);
            ps.execute();
            ret++;
            if (tab.hasGenerateAutoKeys()) {
                l.log(Level.FINE, "has auto keys");
                ResultSet rs = getInsert(tab).getGeneratedKeys();
                if (rs.next()) {
                    tab.psInsertGetAutoKeys(rs, row);
                }
                rs.close();
            }
        } catch (SQLException ex) {
            l.log(Level.SEVERE, "Error in INSERT into " + tab.getName(), ex);
        }
        return ret;
    }

    protected int insertAll(kfsDbiTable tab, kfsRowData row) {
        int ret = 0;
        try {
            PreparedStatement ps = prepare(tab.getInsertIntoAll());
            ps.clearParameters();
            tab.psInsertAllSetParameters(ps, row);
            ps.executeUpdate();
            ret++;
        } catch (SQLException ex) {
            l.log(Level.SEVERE, "Error in INSERT into all " + tab.getName(), ex);
        }
        return ret;
    }

    protected int update(kfsDbiTable tab, kfsRowData row) {
        int ret = 0;
        try {
            PreparedStatement ps = getUpdate(tab);
            if (ps == null) {
                l.log(Level.WARNING, "Cannot update {0}", tab.getName());
            } else {
                ps.clearParameters();
                tab.psSetUpdate(ps, row);
                ret = ps.executeUpdate();
            }
        } catch (SQLException ex) {
            l.log(Level.SEVERE, "Error in Update " + tab.getName(), ex);
        }
        return ret;
    }

    private void copyFrom1(final kfsADb src, final kfsDbObject dt) {
        String sql = "";
        try {
            sql = kfsDbObject.getSelect(dt.getName(), dt.getColumns(), null);
            PreparedStatement ps = src.prepare(sql);
            src.loadCust(ps, new loadCB() {

                @Override
                public boolean kfsDbAddItem(kfsRowData rd) {
                    if (kfsADb.this.insertAll(getDbObjectByName(dt.getName()), rd) <= 0) {
                        throw new RuntimeException("Cannot insert data: " + dt.getName());
                    }
                    return true;
                }
            }, dt);

            if ((serverType == kfsDbServerType.kfsDbiPostgre) && dt.hasGenerateAutoKeys()) {
                for (String s : dt.getAutoGeneratedColumnNames()) {
                    sql = String.format("SELECT setval('%1$s_%2$s_SEQ', (SELECT MAX(%2$s) FROM %1$s))",
                            dt.getName(), s);
                    l.log(Level.INFO, "run sql : {0}", sql);
                    ps = prepare(sql);
                    ps.execute();
                }
            }
        } catch (SQLException ex) {
            l.log(Level.SEVERE, "Error in copy " + dt.getName() + ", sql: " + sql, ex);
        }
    }

    protected void copyFrom(kfsADb src) {
        copyFrom(src, src.getDbObjects());
    }

    protected void copyFrom(kfsADb src, Collection<kfsDbObject> tabs) {
        l.info("Copy data begin");
        for (kfsDbObject dt : tabs) {

            // oracle disable triggrer

            String ct = dt.getCreateTable();
            if ((ct == null) || ct.isEmpty()) {
                l.log(Level.INFO, "Skip {0} it does not have creatye tablr", dt.getName());
            } else {
                l.log(Level.INFO, "Copy table - {0}", dt.getName());
                copyFrom1(src, dt);
            }

            // oracle enable trigger
        }
        l.info("Copy data done");
    }

    protected int merge(kfsDbiTable tab, kfsRowData row) {
        int ret = 0;
        try {
            PreparedStatement ps = prepare(tab.sqlMerge());
            ps.clearParameters();
            tab.psMerge(ps, row);
            ps.execute();
            ret++;
            if (tab.hasGenerateAutoKeys()) {
                l.log(Level.FINE, "has auto keys");
                ResultSet rs = getInsert(tab).getGeneratedKeys();
                if (rs.next()) {
                    tab.psInsertGetAutoKeys(rs, row);
                }
                rs.close();
            }
        } catch (SQLException ex) {
            l.log(Level.SEVERE, "Error in INSERT into " + tab.getName(), ex);
        }
        return ret;
    }
}
