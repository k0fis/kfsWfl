package kfs.kfsDbiStd;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import kfs.kfsDbi.*;

/**
 *
 * @author pavedrim
 */
public class kfsRelationLong extends kfsRelationGen<kfsLong, Long> {

    public kfsRelationLong(kfsDbServerType st, String tableName) {
        this(st, tableName, "ID1", "ID2");
    }

    public kfsRelationLong(kfsDbServerType st, String tableName, String id1Name, String id2Name) {
        super(st, tableName, id1Name, id2Name, new createDbo<kfsLong>() {

            @Override
            public kfsLong createDbo(String name, int pos) {
                return new kfsLongFk(name, pos, false);
            }
        });
    }

    @Override
    protected void setPsData(PreparedStatement ps, int inx, Long obj) throws SQLException {
        ps.setLong(inx, obj);
    }
}
