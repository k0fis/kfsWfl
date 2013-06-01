package kfs.kfsDbi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pavedrim
 */
public class kfsOracleUtils {

    private static final String sqlExistPartitionInTable = "SELECT "//
            + "partition_name FROM all_tab_partitions WHERE "//
            + "table_owner=? AND table_name=? AND partition_name=?";
    private static final String existTable = "SELECT 1 FROM all_tables " //
            + "WHERE table_name=? AND OWNER=?";

    public static boolean existTable(Connection conn, String schema,//
            String tableName) throws SQLException {
        boolean res = false;
        PreparedStatement stmt = null;
        ResultSet r = null;
        try {
            stmt = conn.prepareStatement(existTable);
            stmt.setString(1, tableName.toUpperCase());
            stmt.setString(2, schema.toUpperCase());
            r = stmt.executeQuery();
            res = r.next();
        } catch (SQLException ex) {
            Logger.getLogger(kfsOracleUtils.class.getName()).log(Level.SEVERE,//
                    "Error in existTable " + schema + "." +//
                    tableName + " ::" + ex.getMessage(), ex);
        } finally {
            if (r != null) {
                r.close();
            }
            if (stmt != null) {
                stmt.close();
            }
        }
        return res;
    }

    public static boolean existPartitionInTable(Connection conn, String owner,//
            String tableName, String partName) throws SQLException {
        boolean res = false;
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(sqlExistPartitionInTable);
            stmt.setString(1, owner.toUpperCase());
            stmt.setString(2, tableName.toUpperCase());
            stmt.setString(3, partName.toUpperCase());
            String p = getString(conn, stmt);
            res = partName.equalsIgnoreCase(p);
        } catch (SQLException ex) {
            Logger.getLogger(kfsOracleUtils.class.getName()).log(Level.SEVERE,//
                    "Error in existPartitionInTable " + owner + "." +//
                    tableName + " ::" + ex.getMessage(), ex);
            throw ex;
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
        return res;
    }

    public static String createPartitionList(String schema, String tableName, //
            String partName, String[] values) {
        StringBuilder sb = new StringBuilder();
        sb.append("ALTER TABLE ").append(schema).append(".").append(tableName)//
                .append(" ADD PARTITION ").append(partName).append(" VALUES (");
        boolean f = true;
        for (String v : values) {
            if (f) {
                f = false;
            } else {
                sb.append(", ");
            }
            sb.append(v);
        }
        sb.append(")");
        return sb.toString();
    }

    public static void createPartitionList(Connection conn, String owner, //
            String tableName, String partName, String... values) //
            throws SQLException {
        PreparedStatement stmt = null;
        String sql = createPartitionList(owner, tableName, partName, values);
        try {
            stmt = conn.prepareStatement(sql);
            stmt.execute();
        } catch (SQLException ex) {
            Logger.getLogger(kfsOracleUtils.class.getName()).log(Level.SEVERE,//
                    "Error in existPartitionInTable " + owner + "." +//
                    tableName + " ::" + ex.getMessage(), ex);
            throw ex;
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
    }

    public static String getString(Connection conn, PreparedStatement stm)
            throws SQLException {
        String res = null;
        ResultSet r = null;
        try {
            r = stm.executeQuery();
            if (r.next()) {
                res = r.getString(1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(kfsOracleUtils.class.getName()).log(Level.SEVERE,//
                    "Error in kfsOracleUtls.getString :" + ex.getMessage(), ex);
        } finally {
            if (r != null) {
                r.close();
            }
        }
        return res;
    }
    public static final String sql_updateTableStatistics = "{ call dbms_stats.gather_table_stats(ownname => '%1$s', tabname => '%2$s', estimate_percent => DBMS_STATS.AUTO_SAMPLE_SIZE, cascade=>TRUE, method_opt=>'FOR ALL COLUMNS SIZE AUTO') }";
    public static final String sql_updateIndexStatistics = "{ call dbms_stats.gather_index_stats(ownname => '%1$s', indname => '%2$s', estimate_percent => DBMS_STATS.AUTO_SAMPLE_SIZE) }";

    public static void updateTableStatistics(Connection con, String schema, String table) throws SQLException {
        con.prepareCall(String.format(sql_updateTableStatistics, schema, table)).execute();
    }

    public static void updateIndexStatistics(Connection con, String schema, String index) throws SQLException {
        con.prepareCall(String.format(sql_updateIndexStatistics, schema, index)).execute();
    }

    public static void updateTableIndexiesStatistics(Connection con, String schema, String table) throws SQLException {
        for (String inxName : getTableIndexies(con, schema, schema, table)) {
            updateIndexStatistics(con, schema, inxName);
        }
    }
    private static final String sql_getTableIndexies = "select index_name from all_indexes where owner=? and table_owner=? and table_name=?";

    public static ArrayList<String> getTableIndexies(Connection con, String owner, String tableName) throws SQLException {
        return getTableIndexies(con, owner, owner, tableName);
    }

    public static ArrayList<String> getTableIndexies(Connection con, String indexOwner, String tableOwner, String tableName) throws SQLException {
        ArrayList<String> al = new ArrayList<String>();
        PreparedStatement ps = con.prepareStatement(sql_getTableIndexies);
        ps.setString(1, indexOwner);
        ps.setString(2, tableOwner);
        ps.setString(3, tableName);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            al.add(rs.getString(1));
        }
        rs.close();
        ps.close();
        return al;
    }
}
