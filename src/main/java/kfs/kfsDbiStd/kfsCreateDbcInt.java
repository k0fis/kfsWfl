package kfs.kfsDbiStd;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import kfs.kfsDbi.kfsInt;
import kfs.kfsDbi.kfsIntFk;

/**
 *
 * @author pavedrim
 */
public class kfsCreateDbcInt implements kfsICreateDbc<kfsInt, Integer> {

    private final String name;
    
    public kfsCreateDbcInt(String name){
        this.name = name;
    }
    
    @Override
    public void setPsData(PreparedStatement ps, int inx, Integer obj) throws SQLException {
        ps.setInt(inx, obj);
    }

    @Override
    public kfsInt createDbo(int pos) {
        return new kfsIntFk(name, pos, false);
    }

}
