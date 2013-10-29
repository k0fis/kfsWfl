package kfs.kfsGenDbi;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import kfs.kfsDbi.kfsNames;
import kfs.kfsDbi.kfsSb;

/**
 *
 * @author pavedrim
 */
public class generator {
    public static String getJavaName(String name) {
        return new kfsNames().add(name, "_").getJavoizeName();
    }

    enum dbt {

        num, str, date, nil, numAi;

        public static dbt getDbt(String typeName) {
            if ("VARCHAR2".equals(typeName) || "varchar".equals(typeName)) {
                return str;
            } else if ("int identity".equals(typeName)) {
                return numAi;
            } else if ("NUMBER".equals(typeName) || "tinyint".equals(typeName) || "unsigned bigint".equals(typeName) || "unsigned int".equals(typeName) || "smallint".equals(typeName) || "int".equals(typeName) ) {
                return num;
            } else if ("DATE".equals(typeName) || "TIMESTAMP".equals(typeName) || "datetime".equals(typeName)) {
                return date;
            }
            System.err.println("getDbt: "+typeName);
            return nil;
        }

        public static dbt getDbt2(String typeName) {
            if (String.class.getName().equals(typeName)) {
                return str;
            } else if (BigDecimal.class.getName().equals(typeName) || Long.class.getName().equals(typeName)) {
                return num;
            } else if (java.sql.Timestamp.class.getName().equals(typeName) || "oracle.sql.TIMESTAMP".equals(typeName)) {
                return date;
            } else if (Integer.class.getName().equals(typeName)) {
                return num;
            }
            System.err.println("getDbt2: " +typeName);
            //throw new RuntimeException(typeName);
            return nil;
        }
    }

    public static String getKfsType(String typeName) {
        switch (dbt.getDbt(typeName)) {
            case numAi: 
                return "kfsIntAutoInc";
            case num:
                return "kfsLong";
            case str:
                return "kfsString";
            case date:
                return "kfsDate";
        }
        return "kfs" + typeName;
    }
    private static PreparedStatement pps = null;

    public static String getColumnLabel(Connection con, String owner, String table, String columnName, String retLab) throws SQLException {
        try {
        if (pps == null) {
            pps = con.prepareStatement("select comments from all_col_comments where owner = ? and table_name = ? and column_name = ?");
        }
        pps.setString(1, owner);
        pps.setString(2, table);
        pps.setString(3, columnName);
        ResultSet rs = pps.executeQuery();
        String r = "";
        if (rs.next()) {
            r = rs.getString(1);
        }
        rs.close();
        if (r == null) {
            return "";
        }
        if (r.length() > 0) {
            return r;
        }
        return retLab;
        } catch (SQLException ex) {
            return columnName;
        }
    }

    public static String getClassString(Connection con, String owner, String table, ResultSetMetaData rsm, String pkg, String objName, String tblName) throws SQLException {
        kfsSb s = new kfsSb("");
        s//
                .anl("package", " ", pkg, ";")//
                .nl()//
                .anl("import java.util.Date;")//
                .anl("import kfs.kfsDbi.*;")//
                .nl()//
                .anl("public class ", objName, " extends kfsDbObject {")//
                .nl();
        for (int i = 0; i < rsm.getColumnCount(); i++) {
            s.anl("  private final ", getKfsType(rsm.getColumnTypeName(i + 1)), " ", getJavaName(rsm.getColumnName(i + 1)), "; //" + rsm.getColumnClassName(i+1));
        }
        s//
                .anl("  public ", objName, "(kfsDbServerType dbType) {")//
                .anl("    super(dbType, \"", tblName, "\");")//
                .anl("    int pos = 0;");
        for (int i = 0; i < rsm.getColumnCount(); i++) {
            s//
                    .a("    ", getJavaName(rsm.getColumnName(i + 1)), " = new ")//
                    .a(getKfsType(rsm.getColumnTypeName(i + 1)), "(");//
            String colName = rsm.getColumnName(i + 1);
            String label = getColumnLabel(con, owner, table, colName, rsm.getColumnLabel(i + 1));
            switch (dbt.getDbt2(rsm.getColumnClassName(i + 1))) {
                case date:
                    s.a("\"", colName, "\", \"", label, "\", pos++");
                    break;
                case str:
                    s.a("\"", colName, "\", \"", label, "\", ", rsm.getPrecision(i + 1), ", pos++");
                    break;
                case num:
                    s.a("\"", colName, "\", \"", label, "\", ", rsm.getPrecision(i + 1), ", pos++, ", rsm.isNullable(i + 1) == ResultSetMetaData.columnNullable);
                    break;
            }
            s//
                    .anl(");");
        }
        s//
                .anl("    super.setColumns(new kfsDbiColumn[]{");
        for (int i = 0; i < rsm.getColumnCount(); i++) {
            s.anl("      ", getJavaName(rsm.getColumnName(i + 1)), ",");
        }
        s//
                .anl("    });")//
                .anl("    super.setIdsColumns(new kfsDbiColumn[0]);")//
                .anl("  }");

        s//
                .anl("  public kfsRowData copy(", objName, " src, kfsRowData f) {")//
                .anl("     kfsRowData r = new kfsRowData(this);");
        for (int i = 0; i < rsm.getColumnCount(); i++) {
            String name = getJavaName(rsm.getColumnName(i + 1));
            s.anl("     ", name, ".setData(src.", name, ".getData(f), r);");
        }
        s//
                .anl("     return r;")//
                .anl("  }");

        s//
                .anl("  public kfsRowData create() {")//
                .anl("     kfsRowData r = new kfsRowData(this);")//
                .anl("     return r;")//
                .anl("  }");

        s//
                .nl()//
                .anl("}");
        return s.toString();
    }

    static Connection getConCorprd(String schema) throws Exception {
        Connection con;
            Class.forName("oracle.jdbc.OracleDriver");
            con = DriverManager.getConnection("jdbc:oracle:thin:@(DESCRIPTION = "
                    + "(ADDRESS = (PROTOCOL = TCP) (HOST = l5race23)(PORT = 1545)) "
                    + "(ADDRESS = (PROTOCOL = TCP) (HOST = l5race22)(PORT = 1545)) "
                    + "(ADDRESS = (PROTOCOL = TCP) (HOST = l5race21)(PORT = 1545)) "
                    + "(CONNECT_DATA = (SERVER = DEDICATED)"
                    + " (SERVICE_NAME = CORPRD_TAF.CESKYMOBIL.CZ)))",
                    schema, schema + "123");
            con.setAutoCommit(false);
        return con;
    }
/*
        public static void main(String[] aa) throws Exception {
        Connection con =  kfs.kfsUtils.kfsDbTools.getConnCorTp();
        / *
        Statement ps = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = ps.executeQuery("select * from stat_table_name");
        ResultSetMetaData rsm = rs.getMetaData();
            for (int i = 0; i < rsm.getColumnCount(); i++) {
                System.out.print(rsm.getColumnName(i+1)+", ");
            }
            System.out.println();
        while (rs.next()) {
            for (int i = 0; i < rsm.getColumnCount(); i++) {
                System.out.print(rs.getObject(i+1)+", ");
            }
            System.out.println();
        }
        * /
        String tn = "vfcz_handset_provisioning";
        ResultSet rs = con.createStatement().executeQuery("SELECT * FROM " + tn);
        System.out.print(getClassString(con, "TOUCHPOINT_OWN", tn, rs.getMetaData(),
                "kfs.kfsAllot.base.db", "dbHandsetDoc",
                tn));
        rs.close();
        con.close();
    }
    */
}
