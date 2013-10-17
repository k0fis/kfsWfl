package kfs.kfsDbi;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author pavedrim
 */
public class kfsDouble extends kfsColObject implements kfsDbiColumnComparator {

    public kfsDouble(final String name, final String label, final int position, final boolean nullable) {
        super(name, label, position, nullable);
    }

    public void setData(Double data, kfsRowData row) {
        setDouble(data, row);
    }

    public Double getData(kfsRowData row) {
        return getDouble(row);
    }

    public void setDouble(Double data, kfsRowData row) {
        super.setObject(data, row);
    }

    public Double getDouble(kfsRowData row) {
        return (Double) super.getObject(row);
    }

    @Override
    public String getColumnCreateTable(kfsDbServerType serverType) {
        switch (serverType) {
            case kfsDbiSqlite:
                return getColumnName() + " REAL "+(isColumnNullable()?"NULL":"NOT NULL") + " ";            
            case kfsDbiOracle:
                return getColumnName() + " NUMBER " + (isColumnNullable() ? "NULL" : "NOT NULL") + " ";
        }
        return null;
    }

    @Override
    public void setParam(int inx, PreparedStatement ps, kfsRowData row) throws SQLException {
        Double data = (Double) super.getObject(row);
        if (data == null) {
            ps.setNull(inx, java.sql.Types.DOUBLE);
        } else {
            ps.setDouble(inx, data);
        }
    }

    @Override
    public void getParam(int inx, ResultSet ps, kfsRowData row) throws SQLException {
        setDouble(ps.getDouble(inx), row);
    }

    @Override
    public Class<?> getColumnJavaClass() {
        return Double.class;
    }

    @Override
    public int compare(kfsRowData t, kfsRowData t1) {
        return getSortDirection() * getData(t).compareTo(getData(t1));
    }

    @Override
    public String appendOracleControlFile() {
        return "";
    }

    @Override
    public String exportToCsv(kfsRowData row) {
        return Double.toString(getData(row));
    }

}
