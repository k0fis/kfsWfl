package kfs.kfsGenDbi;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author pavedrim
 */
public class createFromSql extends kfsTableGenerator {

    public createFromSql(Connection con, String sql, String packageName, String className) throws SQLException, ClassNotFoundException {
        this(con, sql, null, packageName, className, true, true, false, false);
    }

    public createFromSql(Connection con, String sql, Class<?> cls, String packageName, String className, //
            boolean useOraPartitioning, boolean useAutoId, boolean createSetters,//
            boolean createList) throws SQLException, ClassNotFoundException {
        super(cls, packageName, className, useOraPartitioning, useAutoId, createSetters, createList);

        Statement ps = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = ps.executeQuery(sql);

        ResultSetMetaData rsm = rs.getMetaData();
        for (int i = 0; i < rsm.getColumnCount(); i++) {
            addItem(new kfsRowItem(Class.forName(rsm.getColumnClassName(i + 1)),
                    createFromResultSet.getJavaName(rsm.getColumnName(i + 1))));
        }

        rs.close();
        ps.close();
    }

}
