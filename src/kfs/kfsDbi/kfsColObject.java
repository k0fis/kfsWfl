package kfs.kfsDbi;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author pavedrim
 */
public abstract class kfsColObject implements kfsDbiColumn {

    private final String name;
    private final String label;
    private final boolean nullable;
    private final int position;
    private int sortDirection;

    protected kfsColObject(final String name, final String label, final int position, final boolean nullable) {
        this.name = name;
        this.label = label;
        this.nullable = nullable;
        this.position = position;
        this.sortDirection = 1;
    }

    @Override
    public String[] getCreateTableAddons(kfsDbServerType serverType, String table_name) {
        return new String[0];
    }

    @Override
    public String getColumnName() {
        return name;
    }

    @Override
    public String getColumnLabel() {
        return label;
    }
    
    @Override
    public boolean isColumnNullable() {
        return nullable;
    }

    @Override
    public Object getObject(kfsRowData row) {
        return row.getObject(position);
    }

    @Override
    public void setObject(Object o, kfsRowData row) {
        row.setObject(position, o);
    }

    @Override
    public void getParam(int inx, ResultSet ps, kfsRowData row) throws SQLException {
    }

    @Override
    public void resetFilterCounter() {
    }


    public int getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(int val) {
        this.sortDirection = val;
    }
}
