package kfs.kfsDbi;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 *
 * @author pavedrim
 */
public class kfsDbObject implements kfsDbiTable, kfsTableDesc, Comparator<kfsRowData>, kfsIPojoCtrl {

    private final String t_name;
    private final String t_label;
    protected final kfsDbServerType serverType;
    protected kfsDbiColumn[] allCols = null;
    private kfsDbiColumn[] updUpdSet = null;
    private kfsDbiColumn[] updIds = null;
    private kfsDbiColumn[] ftCols = null;
    private kfsDbiColumn[] ftColsWhat = null;
    private kfsDbiColumnComparator[] sortCols = null;

    protected kfsDbObject(final kfsDbServerType serverType, final String t_name) {
        this(serverType, t_name, (new kfsNames().add(t_name, "_").getCapitalizeName(" ")));
    }

    protected kfsDbObject(final kfsDbServerType serverType, final String t_name, final String label) {
        this.t_name = t_name;
        this.t_label = label;
        this.serverType = serverType;
    }

    public kfsDbiColumn[] getColumns() {
        return this.allCols;
    }

    protected void setFullTextColumns(final kfsDbiColumn[] what, final kfsDbiColumn[] cols) {
        ftCols = cols;
        ftColsWhat = what;
    }

    protected void setColumns(final kfsDbiColumn[] allCols) {
        this.allCols = allCols;
    }

    protected void setIdsColumns(final kfsDbiColumn[] updIds) {
        this.updIds = updIds;
    }

    protected void setUpdateColumns(final kfsDbiColumn[] updSet) {
        this.updUpdSet = updSet;
    }

    public List<kfsDbiColumn> getAllColumnComparators() {
        ArrayList<kfsDbiColumn> ret = new ArrayList<kfsDbiColumn>(allCols.length);
        for (kfsDbiColumn o : allCols) {
            if (o instanceof kfsDbiColumn) {
                ret.add(o);
            }
        }
        return ret;
    }

    @Override
    public String getLabel() {
        return t_label;
    }

    @Override
    public String getName() {
        return t_name;
    }

    @Override
    public String getInsertInto() {
        String s = "INSERT INTO " + getName() + " ( ";
        String d = "";
        boolean f = true;
        for (kfsDbiColumn di : allCols) {
            if (di instanceof kfsIntAutoInc) {
                continue;
            }
            if (f) {
                f = false;
            } else {
                s += ", ";
                d += ", ";
            }
            s += di.getColumnName();
            d += "?";
        }
        return s + ") VALUES ( " + d + ")";
    }

    @Override
    public String getInsertIntoAll() {
        String s = "INSERT INTO " + getName() + " ( ";
        String d = "";
        boolean f = true;
        for (kfsDbiColumn di : allCols) {
            if (f) {
                f = false;
            } else {
                s += ", ";
                d += ", ";
            }
            s += di.getColumnName();
            d += "?";
        }
        return s + ") VALUES ( " + d + ")";
    }

    @Override
    public String[] getInsertIntoAllAddon() {
        ArrayList<String> str = new ArrayList<String>();
        if (serverType == kfsDbServerType.kfsDbiPostgre) {
            for (kfsDbiColumn dc : allCols) {
                if (dc instanceof kfsIntAutoInc) {
                    str.add("select nextval('" + getName() + "_" + dc.getColumnName() + "_seq')");
                }
            }
        } else if (serverType == kfsDbServerType.kfsDbiOracle) {
            for (kfsDbiColumn dc : allCols) {
                if (dc instanceof kfsIntAutoInc) {
                    str.add("select " + getName() + "_" + dc.getColumnName() + "_seq.nextval from dual");
                }
            }
        }
        return str.toArray(new String[0]);
    }

    @Override
    public String getCreateTable() {
        String s = "CREATE TABLE " + getName() + " ( ";
        boolean f = true;
        for (kfsDbiColumn di : allCols) {
            if (f) {
                f = false;
            } else {
                s += ", ";
            }
            s += di.getColumnCreateTable(serverType);
        }
        s += ")";
        return s;
    }

    @Override
    public String[] getCreateTableAddons() {
        ArrayList<String> ret = new ArrayList<String>();
        for (kfsDbiColumn dc : allCols) {
            String[] aa = dc.getCreateTableAddons(serverType, t_name);
            if ((aa != null) && (aa.length > 0)) {
                ret.addAll(Arrays.asList(aa));
            }
        }
        return ret.toArray(new String[0]);
    }

    @Override
    public boolean hasGenerateAutoKeys() {
        for (kfsDbiColumn dc : allCols) {
            if (dc instanceof kfsIntAutoInc) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getSelect() {
        return getSelect(getName(), allCols, null);
    }

    protected static String getSelect(String tableName, kfsDbiColumn[] allCols, kfsDbiColumn[] where) {
        return getSelect(tableName, allCols, where, false);
    }

    protected static String getSelect(String tableName, kfsDbiColumn[] allCols, kfsDbiColumn[] where, boolean dist) {
        StringBuilder s = new StringBuilder();
        s.append("SELECT ");
        if (dist) {
            s.append("DISTINCT ");
        }
        boolean f = true;
        for (kfsDbiColumn di : allCols) {
            if (f) {
                f = false;
            } else {
                s.append(", ");
            }
            s.append(di.getColumnName());
        }
        s.append(" FROM ").append(tableName);
        if ((where != null) && (where.length > 0)) {
            s.append(" WHERE ");
            f = true;
            for (kfsDbiColumn di : where) {
                if (f) {
                    f = false;
                } else {
                    s.append(" AND ");
                }
                s.append(di.getColumnName()).append("=?");
            }
        }
        return s.toString();
    }

    @Override
    public String getDelete() {
        StringBuilder r = new StringBuilder();
        r.append("DELETE FROM ").append(getName()).append(" WHERE ");
        for (int i = 0; i < updIds.length; i++) {
            if (i > 0) {
                r.append(" AND ");
            }
            r.append(updIds[i].getColumnName()).append("=? ");
        }
        return r.toString();
    }

    @Override
    public void psSetDelete(PreparedStatement ps, kfsRowData row) throws SQLException {
        for (int i = 0; i < updIds.length; i++) {
            updIds[i].setParam(i + 1, ps, row);
        }
    }

    @Deprecated
    @Override
    public String getExistItemSelect() {
        return null;
    }

    @Override
    public void psInsertSetParameters(PreparedStatement ps, kfsRowData row) throws SQLException {
        int y = 1;
        for (int i = 0; i < allCols.length; i++) {
            if (allCols[i] instanceof kfsIntAutoInc) {
                continue;
            }
            allCols[i].setParam(y++, ps, row);
        }
    }

    @Override
    public void psInsertAllSetParameters(PreparedStatement ps, kfsRowData row) throws SQLException {
        int y = 1;
        for (int i = 0; i < allCols.length; i++) {
            allCols[i].setParam(y++, ps, row);
        }
    }

    @Deprecated
    @Override
    public void psExistItemSetParameters(PreparedStatement ps, kfsRowData row) throws SQLException {
    }

    public void resetFilterCounter() {
        for (kfsDbiColumn fc : allCols) {
            fc.resetFilterCounter();
        }
    }

    @Override
    public void psSelectGetParameters(ResultSet ps, kfsRowData row) throws SQLException {
        resetFilterCounter();
        for (int i = 0; i < allCols.length; i++) {
            try {
                allCols[i].getParam(i + 1, ps, row);
            } catch (SQLException ex) {
                System.err.println(i);
                throw ex;
            }
        }
    }

    @Override
    public Class<?> getColumnJavaClass(int column) {
        return allCols[column].getColumnJavaClass();
    }

    @Override
    public String getColumnLabel(int column) {
        return allCols[column].getColumnLabel();
    }

    @Override
    public int getColumnCount() {
        return allCols.length;
    }

    @Override
    public Object getObject(kfsRowData row, int column) {
        return row.getObject(column);
    }

    protected void setObject(kfsRowData row, int column, Object obj) {
        row.setObject(column, obj);
    }

    @Override
    public void psInsertGetAutoKeys(ResultSet ps, kfsRowData row) throws SQLException {
        for (int i = 0; i < updIds.length; i++) {
            updIds[i].getParam(i + 1, ps, row);
        }
    }

    @Override
    public String[] getAutoGeneratedColumnNames() {
        ArrayList<String> ret = new ArrayList<String>();
        for (kfsDbiColumn dc : allCols) {
            if (dc instanceof kfsIntAutoInc) {
                ret.add(dc.getColumnName());
            }
        }
        return ret.toArray(new String[0]);
    }

    protected static String getUpdate(String tname, kfsDbiColumn[] updUpdSet, kfsDbiColumn[] updIds) {
        if (updUpdSet == null) {
            return null;
        }
        String s = "UPDATE " + tname + " SET ";
        boolean f = true;
        for (kfsDbiColumn di : updUpdSet) {
            if (f) {
                f = false;
            } else {
                s += ", ";
            }
            s += di.getColumnName() + "=?";
        }
        s += " WHERE ";
        f = true;
        for (kfsDbiColumn di : updIds) {
            if (f) {
                f = false;
            } else {
                s += " AND ";
            }
            s += di.getColumnName() + "=?";
        }
        return s;
    }

    @Override
    public String getUpdate() {
        return getUpdate(getName(), updUpdSet, updIds);
    }

    @Override
    public void psSetUpdate(PreparedStatement ps, kfsRowData row) throws SQLException {
        if (updUpdSet == null) {
            return;
        }

        for (int i = 0; i < updUpdSet.length; i++) {
            try {
                updUpdSet[i].setParam(i + 1, ps, row);
            } catch (SQLException ex) {
                System.err.println(i);
                throw ex;
            }
        }
        for (int i = 0; i < updIds.length; i++) {
            try {
                updIds[i].setParam(i + updUpdSet.length + 1, ps, row);
            } catch (SQLException ex) {
                System.err.println(i);
                throw ex;
            }
        }

    }

    public void setSortColumns(kfsDbiColumnComparator[] cols) {
        this.sortCols = cols;
    }

    @Override
    public void sort(kfsDbiColumnComparator[] cols, List<kfsRowData> inp) {
        this.sortCols = cols;
        Collections.sort(inp, this);
    }

    @Override
    public int compare(kfsRowData t, kfsRowData t1) {
        if (sortCols != null) {
            for (kfsDbiColumnComparator cc : sortCols) {
                int cmp = cc.compare(t, t1);
                if (cmp != 0) {
                    return cmp;
                }
            }
        }
        return 0;

    }

    @Override
    public kfsIPojoObj getNewPojo() {
        return getPojo(new kfsRowData(this));
    }

    @Override
    public kfsIPojoObj getPojo(kfsRowData row) {
        return null;
    }

    @Override
    public void psFullTextSearch(PreparedStatement ps, String fnd) throws SQLException {
        ps.setString(1, fnd);
    }

    @Override
    public String sqlFullTextSearch() {
        if ((ftCols == null) || (ftCols.length <= 0)) {
            return "";
        }
        if (serverType == kfsDbServerType.kfsDbiMssql) {
            String r = "SELECT ";
            boolean f = true;
            for (kfsDbiColumn s : ftColsWhat) {
                if (f) {
                    f = false;
                } else {
                    r += ", ";
                }
                r += s.getColumnName();
            }
            r += " FROM " + getName() + " WHERE MATCH (";
            f = true;
            for (kfsDbiColumn s : ftCols) {
                if (f) {
                    f = false;
                } else {
                    r += ", ";
                }
                r += s.getColumnName();
            }
            return r + ") AGAINST (?)";
        } 
        if (serverType == kfsDbServerType.kfsDbiPostgre) {
            String r = "SELECT ";
            boolean f = true;
            for (kfsDbiColumn s : ftColsWhat) {
                if (f) {
                    f = false;
                } else {
                    r += ", ";
                }
                r += s.getColumnName();
            }
            r += " FROM " + getName() + " WHERE to_tsvector (";
            f = true;
            for (kfsDbiColumn s : ftCols) {
                if (f) {
                    f = false;
                } else {
                    r += "|| ' ' || ";
                }
                r += s.getColumnName();
            }
            return r + ") @@ (?)";
        }
        return null;
    }
    
    protected String getPgFullTextFunction() {
        return "english";
    }

    @Override
    public String createFullTextIndex() {
        if ((ftCols == null) || (ftCols.length <= 0)) {
            return "";
        }
        if (serverType == kfsDbServerType.kfsDbiMysql) {
            String s = "CREATE FULLTEXT INDEX FT_" + getName() + " ON " + getName() + "( ";
            boolean f = true;
            for (kfsDbiColumn i : ftCols) {
                if (f) {
                    f = false;
                } else {
                    s += ", ";
                }
                s += i.getColumnName();
            }
            return s + ")";
        }
        if (serverType == kfsDbServerType.kfsDbiPostgre) {
            String s = "CREATE INDEX FT_" + getName() + " ON " + getName() +//
                    " USING gin(to_tsvector('"+getPgFullTextFunction()+"', ";
            boolean f = true;
            for (kfsDbiColumn i : ftCols) {
                if (f) {
                    f = false;
                } else {
                    s += "|| ' ' || ";
                }
                s += i.getColumnName();
            }
            return s + "))";
        }
        return "";
    }
}
