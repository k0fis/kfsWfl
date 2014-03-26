package kfs.kfsGenDbi;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 *
 * @author pavedrim
 */
public class createFromResultSet extends kfsTableGenerator {

    public createFromResultSet(ResultSet rs, String packageName, String className) throws SQLException, ClassNotFoundException {
        this(rs, null, packageName, className, true, true, false, false);
    }

    public createFromResultSet(ResultSet rs, Class<?> cls, String packageName, String className, //
            boolean useOraPartitioning, boolean useAutoId, boolean createSetters,//
            boolean createList) throws SQLException, ClassNotFoundException {
        super(cls, packageName, className, useOraPartitioning, useAutoId, createSetters, createList);
        
        ResultSetMetaData rsm = rs.getMetaData();
        for (int i = 0; i < rsm.getColumnCount(); i++) {
            addItem(new kfsRowItem(Class.forName(rsm.getColumnClassName(i + 1)), getJavaName(rsm.getColumnName(i + 1))));
        }

        rs.close();
    }

    public static String getJavaName(String dbiName) {
        StringBuilder sb = new StringBuilder();
        boolean b = true;
        for (String s : dbiName.split("_")) {
            if (b) {
                b = false;
                sb.append(s.toLowerCase());
            } else {
                sb.append(Character.toUpperCase(s.charAt(0)))
                        .append(s.substring(1).toLowerCase());
            }
        }
        return sb.toString();
    }
}
