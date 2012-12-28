package kfs.kfsDbi;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pavedrim
 */
public class kfsDate extends kfsColObject implements kfsDbiColumnComparator {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private boolean oraTimestamp = false;

    public kfsDate(final String name, final String label, final int position) {
        super(name, label, position, true);
    }

    public void setData(Date data, kfsRowData row) {
        setDate(data, row);
    }

    public Date getData(kfsRowData row) {
        return getDate(row);
    }

    public void setDate(Date d, kfsRowData row) {
        super.setObject(d, row);
    }

    public Date getDate(kfsRowData row) {
        return (Date) super.getObject(row);
    }

    public void now(kfsRowData row) {
        setDate(new Date(), row);
    }

    @Override
    public String getColumnCreateTable(kfsDbServerType serverType) {
        switch (serverType) {
            case kfsDbiOracle:
                return getColumnName() + (oraTimestamp?" TIMESTAMP ":" DATE ");
            case kfsDbiMysql:
            case kfsDbiSybase:
                return getColumnName() + " DATETIME ";
            case kfsDbiPostgre:
                return getColumnName() + " timestamp ";
            case kfsDbiSqlite:
                return getColumnName() + " datetime ";                
        }
        return null;
    }

    @Override
    public void setParam(int inx, PreparedStatement ps, kfsRowData row) throws SQLException {
        Date data = (Date) super.getObject(row);
        if (data == null) {
            ps.setNull(inx, java.sql.Types.DATE);
        } else {
            if (super.getObject(row) instanceof Timestamp) {
                ps.setTimestamp(inx, (Timestamp)super.getObject(row));
            } else {
                ps.setTimestamp(inx, new Timestamp(data.getTime()));
            }
        }
    }

    @Override
    public void getParam(int inx, ResultSet ps, kfsRowData row) throws SQLException {
        super.setObject(ps.getTimestamp(inx), row);
    }

    public void setString(String date, kfsRowData row) {
        try {
            setDate(sdf.parse(date), row);
        } catch (ParseException ex) {
            Logger.getLogger(kfsDate.class.getName()).log(Level.SEVERE, //
                    "Error in parsing input date:" + date, ex);
        }
    }

    public String getString(kfsRowData row) {
        Date data = getDate(row);
        return (data == null) ? "" : sdf.format(data);
    }

    @Override
    public Class<?> getColumnJavaClass() {
        return Date.class;
    }

    @Override
    public int compare(kfsRowData t, kfsRowData t1) {
        return getSortDirection()*getDate(t).compareTo(getDate(t1));
    }

    /**
     * @return the oraTimestamp
     */
    public boolean isOraTimestamp() {
        return oraTimestamp;
    }

    /**
     * @param oraTimestamp the oraTimestamp to set
     */
    public kfsDate setOraTimestamp(boolean oraTimestamp) {
        this.oraTimestamp = oraTimestamp;
        return this;
    }
}
