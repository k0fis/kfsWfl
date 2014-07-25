package kfs.kfsDbiStd;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import kfs.kfsDbi.kfsString;

/**
 *
 * @author pavedrim
 */
public class kfsCreateDbcString implements kfsICreateDbc<kfsString, String> {

    private final int keyLen;
    private final String name;
    
    public kfsCreateDbcString(String name, int keyLen) {
        this.keyLen = keyLen;
        this.name = name;
    }
    
    @Override
    public void setPsData(PreparedStatement ps, int inx, String obj) throws SQLException {
        ps.setString(inx, obj);
    }

    @Override
    public kfsString createDbo(int pos) {
        return new kfsString(name, name, keyLen, pos);
    }

}
