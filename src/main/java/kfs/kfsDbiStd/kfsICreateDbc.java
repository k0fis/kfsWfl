package kfs.kfsDbiStd;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import kfs.kfsDbi.kfsColObject;

/**
 *
 * @author pavedrim
 * @param <DBI> column type
 * @param <T>
 */
public interface kfsICreateDbc<DBI extends kfsColObject, T> {

    DBI createDbo(int pos);
    void setPsData(PreparedStatement ps, int inx, T obj) throws SQLException;

}
