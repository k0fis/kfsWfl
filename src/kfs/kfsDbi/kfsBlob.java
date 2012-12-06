package kfs.kfsDbi;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author pavedrim
 */
public class kfsBlob extends kfsColObject {

    private final kfsDbServerType serverType;

    public kfsBlob(final String name, final String label, final int position, final kfsDbServerType serverType) {
        super(name, label, position, true);
        this.serverType = serverType;
    }

    @Override
    public String getColumnCreateTable(kfsDbServerType serverType) {
        if (serverType == kfsDbServerType.kfsDbiMysql) {
            return getColumnName() + " MEDIUMBLOB";
        }
        if (serverType == kfsDbServerType.kfsDbiPostgre) {
            return getColumnName() + " bytea ";
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Class<?> getColumnJavaClass() {
        return kfsBlobData.class;
    }

    @Override
    public Object getObject(kfsRowData row) {
        return (kfsBlobData) super.getObject(row);
    }

    public kfsBlobData getData(kfsRowData row) {
        return (kfsBlobData) super.getObject(row);
    }

    public void setData(byte[] d, kfsRowData r) {
        super.setObject(new kfsBlobData(new ByteArrayInputStream(d), d.length), r);
    }

    public void setData(kfsBlobData d, kfsRowData r) {
        super.setObject(d, r);
    }
    
    public void setData(InputStream value, int size, kfsRowData r) { 
        this.setObject(new kfsBlobData(value, size), r);
    }

    @Override
    public void setParam(int inx, PreparedStatement ps, kfsRowData row) throws SQLException {
        kfsBlobData data = getData(row);
        if (data.getInputStream() != null) {
            ps.setBinaryStream(inx, data.getInputStream());
        } else {
            ps.setNull(inx, java.sql.Types.BLOB);
        }
    }

    @Override
    public void getParam(int inx, ResultSet ps, kfsRowData row) throws SQLException {
        if (serverType == kfsDbServerType.kfsDbiOracle) {
            super.setObject(new kfsBlobData(ps.getBinaryStream(inx), -1), row);
        } else {
            Blob bl = ps.getBlob(inx);
            super.setObject(new kfsBlobData(bl.getBinaryStream(), bl.length()), row);
        }
    }


    /*
     * public void setData(Object value) { if (value instanceof String) { setData((String) value); }
     * else if (value instanceof InputStream) { setData((InputStream) value); } else { throw new
     * UnsupportedOperationException("Not supported yet. SetData(" + // value.getClass().getName() +
     * ")"); } }
     *
     * public void setData(String value) { this.ind = new ByteArrayInputStream(value.getBytes());
     * this.length = value.length(); }
     *
     * public String getStreamAsString(String encoding) throws IOException { if (this.ind != null) {
     * StringWriter writer = new StringWriter(); IOUtils.copy(this.ind, writer, encoding); return
     * writer.toString(); } return ""; }
     *
     * public byte[] getStreamAsByteArr() throws IOException { if (this.ind != null) { return
     * IOUtils.toByteArray(this.ind); } return new byte[0]; }
     *
     * public void toOutputStream(OutputStream out) throws IOException { if (this.ind != null) {
     * IOUtils.copy(this.ind, out); } }
     */
}
