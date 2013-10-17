package kfs.kfsDbi;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author pavedrim
 */
public class kfsLong extends kfsColObject implements kfsDbiColumnComparator{

    private final int maxLength;

    public kfsLong(String name, String label, int maxLength, int position, boolean nullable) {
        super(name, label, position, nullable);
        this.maxLength = maxLength;
    }
    
    public void setData(Long data, kfsRowData row) {
        setLong(data, row);
    }
    public Long getData(kfsRowData row) {
        return getLong(row);
    }

    public void setLong(Long data, kfsRowData row) {
        super.setObject(data, row);
    }

    public Long getLong(kfsRowData row) {
        Object o = super.getObject(row);
        if (o == null) return null;
        return (Long)o;
    }

    @Override
    public String getColumnCreateTable(kfsDbServerType serverType) {
        switch (serverType) {
            case kfsDbiOracle:
                return getColumnName() + " NUMBER("+maxLength+") "+(isColumnNullable()?"NULL":"NOT NULL") + " ";
            case kfsDbiMysql:
                return getColumnName() + " BIGINT "+(isColumnNullable()?"NULL":"NOT NULL") + " ";
            case kfsDbiPostgre:
            case kfsDbiSqlite:
                return getColumnName() + " NUMERIC("+maxLength+") "+(isColumnNullable()?"NULL":"NOT NULL") + " ";
        }
        return null;
    }

    public int getColumnMaxLength() {
        return maxLength;
    }

    @Override
    public void setParam(int inx, PreparedStatement ps, kfsRowData row) throws SQLException {
        Object data = super.getObject(row);
        if (data == null) {
            ps.setNull(inx, java.sql.Types.INTEGER);
        } else {
            ps.setLong(inx, (Long)data);
        }
    }

    @Override
    public void getParam(int inx, ResultSet ps, kfsRowData row) throws SQLException {
        setLong(ps.getLong(inx), row);
    }

    @Override
    public Class<?> getColumnJavaClass() {
        return Long.class;
    }

    @Override
    public int compare(kfsRowData t, kfsRowData t1) {
        return getSortDirection()*getData(t).compareTo(getData(t1));
    }

    @Override
    public String appendOracleControlFile() {
        return "";
    }

    @Override
    public String exportToCsv(kfsRowData row) {
        return Long.toString(getData(row));
    }
    
}
