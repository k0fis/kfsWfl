package kfs.kfsDbi;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author pavedrim
 */
public interface kfsDbiColumn {

    String getColumnCreateTable(kfsDbServerType serverType);
    String []getCreateTableAddons(kfsDbServerType serverType, String table_name);
    
    String getColumnName();
    String getColumnLabel();
    boolean isColumnNullable();
    Class<?> getColumnJavaClass();

    Object getObject(kfsRowData row);
    void setObject(Object o, kfsRowData row);

    void setParam(int inx, PreparedStatement ps, kfsRowData row) throws SQLException;
    void getParam(int inx, ResultSet ps, kfsRowData row) throws SQLException;
    void resetFilterCounter();
    String appendOracleControlFile();
    String exportToCsv(kfsRowData row);
}
