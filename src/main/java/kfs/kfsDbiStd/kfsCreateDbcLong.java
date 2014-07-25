package kfs.kfsDbiStd;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import kfs.kfsDbi.kfsLong;
import kfs.kfsDbi.kfsLongFk;

/**
 *
 * @author pavedrim
 */
public class kfsCreateDbcLong implements kfsICreateDbc<kfsLong, Long> {

    private final String name;

    public kfsCreateDbcLong(String name) {
        this.name = name;
    }

    @Override
    public void setPsData(PreparedStatement ps, int inx, Long obj) throws SQLException {
        ps.setLong(inx, obj);
    }

    @Override
    public kfsLong createDbo(int pos) {
        return new kfsLongFk(name, pos, false);
    }

}
