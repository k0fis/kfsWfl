package kfs.kfsDbi;

import java.sql.*;
import java.util.*;

/**
 *
 * @author pavedrim
 */
public abstract class kfsADb {

    public interface loadCB {

        boolean kfsDbAddItem(kfsRowData rd);
    };
    //protected static final Logger l = Logger.getLogger(kfsADb.class.getName());
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

    }

    protected void initPostgree() throws SQLException {
        prepare("set search_path to '" + getSchema() + "'").execute();
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

    protected PreparedStatement getInsert(kfsDbiTable tab) throws SQLException {
        String sql = tab.getInsertInto();
        if (!closingList.containsKey(sql)) {
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
        }
        return closingList.get(sql);
    }

    protected PreparedStatement getUpdate(kfsDbiTable tab) throws SQLException {
        return prepare(tab.getUpdate());
    }

    protected PreparedStatement getSelect(kfsDbiTable tab) throws SQLException {
        return prepare(tab.getSelect());
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

    public void rollback() throws SQLException {
        conn.rollback();
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
                return "SELECT table_name FROM all_tables WHERE upper(owner)=upper(?) "
                        + "AND upper(table_name)=upper(?)";
            case kfsDbiPostgre:
                return "SELECT tablename FROM pg_catalog.pg_tables WHERE "
                        + "lower(schemaname)=lower(?) AND lower(tablename)=lower(?)";
        }
        return null;
    }

    protected void createTables() throws SQLException {
        createTables(schema_);
    }

    protected void createTables(String schema) throws SQLException {
        String esql = getExist();
        if (esql == null) {
            throw new SQLException("SQL not-exist for server: " + serverType.name());
        }
        //l.log(Level.FINE, "SQL exist: {0} - {1}", new Object[]{esql, serverType});
        PreparedStatement psExistTable = conn.prepareStatement(esql);
        Statement executeStatement = conn.createStatement();
        for (kfsDbiTable ie : getDbObjects()) {
            //l.log(Level.FINE, "Create table {0} begin", ie.getName());

            if (ie == null) {
                throw new RuntimeException("dbObject cannot be null");
            }
            String sql = "";
            try {
                sql = ie.getCreateTable(schema);
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
                        //logInfo("SQL create table: {0}", sql);
                        executeStatement.execute(sql);
                        for (String ss : ie.getCreateTableAddons()) {
                            sql = ss;
                            //logInfo("SQL table addon: {0}", sql);
                            executeStatement.execute(sql);
                        }
                        String ft = ie.createFullTextIndex();
                        if (ft.length() > 0) {
                            sql = ft;
                            //logInfo("SQL table FullText index: {0}", sql);
                            executeStatement.execute(sql);
                        }
                    }
                }
            } catch (SQLException ex) {
                throw new SQLException("Error in " + ie.getName() + ".createTable: " + sql, ex);
            }
            //l.log(Level.FINE, "Create table {0} done", ie.getName());
        }
        executeStatement.close();
        psExistTable.close();
    }

    protected int loadAll(ArrayList<kfsRowData> data, kfsDbObject inx) throws SQLException {
        int ret = data.size();
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
        return data.size() - ret;
    }

    protected int loadAll(loadCB loadCb, kfsDbObject inx) throws SQLException {
        int ret = 0;
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

    public int update(kfsPojoObj<? extends kfsDbObject> pj) throws SQLException {
        return update(pj.kfsGetDbObject(), pj.kfsGetRow());
    }

    protected void delete(kfsDbiTable tab, kfsRowData r) throws SQLException {
        String sql = tab.getDelete();
        if (sql != null) {
            PreparedStatement ps = prepare(sql);
            ps.clearParameters();
            tab.psSetDelete(ps, r);
            ps.executeUpdate();
        } else {
            throw new SQLException("Try call to Delete for DBI: " + tab.getName());
        }
    }

    protected int insert(kfsDbiTable tab, kfsRowData row) throws SQLException {
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

    protected int insertAll(kfsDbiTable tab, kfsRowData row) throws SQLException {
        int ret = 0;
        PreparedStatement ps = prepare(tab.getInsertIntoAll());
        ps.clearParameters();
        tab.psInsertAllSetParameters(ps, row);
        ps.executeUpdate();
        ret++;
        return ret;
    }

    protected int update(kfsDbiTable tab, kfsRowData row) throws SQLException {
        PreparedStatement ps = getUpdate(tab);
        if (ps == null) {
            throw new SQLException("Cannot update " + tab.getName() + ": " + tab.getUpdate());
        } else {
            ps.clearParameters();
            tab.psSetUpdate(ps, row);
            return ps.executeUpdate();
        }
    }

    /**
     * probaly only for oracle
     * 
     * @param tab
     * @param row
     * @return
     * @throws SQLException 
     */
    protected int merge(kfsDbiTable tab, kfsRowData row) throws SQLException {
        int ret = 0;
        PreparedStatement ps = prepare(tab.sqlMerge());
        ps.clearParameters();
        tab.psMerge(ps, row);
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
}
