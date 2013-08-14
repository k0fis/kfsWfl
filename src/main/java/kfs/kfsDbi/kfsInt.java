package kfs.kfsDbi;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author pavedrim
 */
public class kfsInt extends kfsColObject implements kfsDbiColumnComparator {

    private final int maxLength;

    public kfsInt(final String name, final String label, final int maxLength, final int position, final boolean nullable) {
        super(name, label, position, nullable);
        this.maxLength = maxLength;
    }
    
    public void setData(Integer data, kfsRowData row) {
        setInt(data, row);
    }
    public Integer getData(kfsRowData row) {
        return getInt(row);
    }

    public void setInt(Integer data, kfsRowData row) {
        super.setObject(data, row);
    }

    public Integer getInt(kfsRowData row) {
        Object o = super.getObject(row);
        if (o == null) return null;
        return (Integer)o;
    }

    @Override
    public String getColumnCreateTable(kfsDbServerType serverType) {
        switch (serverType) {
            case kfsDbiOracle:
                return getColumnName() + " NUMBER("+maxLength+") "+(isColumnNullable()?"NULL":"NOT NULL") + " ";
            case kfsDbiMysql:
                return getColumnName() + " BIGINT "+(isColumnNullable()?"NULL":"NOT NULL") + " ";
            case kfsDbiPostgre:
                return getColumnName() + " NUMERIC("+maxLength+") "+(isColumnNullable()?"NULL":"NOT NULL") + " ";
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
            ps.setInt(inx, (Integer)data);
        }
    }

    @Override
    public void getParam(int inx, ResultSet ps, kfsRowData row) throws SQLException {
        if (isColumnNullable()) {
            Object o = ps.getObject(inx);
            if (o == null) {
                setInt(null, row);
                return;
            }
        }
        setInt(ps.getInt(inx), row);
    }

    @Override
    public Class<?> getColumnJavaClass() {
        return Integer.class;
    }

    @Override
    public int compare(kfsRowData t, kfsRowData t1) {
        return getSortDirection()*getData(t).compareTo(getData(t1));
    }

}
