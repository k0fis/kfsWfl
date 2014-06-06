package kfs.kfsDbiStd;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import kfs.kfsDbi.*;

/**
 *
 * @author pavedrim
 */
public class kfsRelation extends kfsRelationGen<kfsInt, Integer> {


    public kfsRelation(kfsDbServerType st, String tableName) {
        this(st, tableName, "ID1", "ID2");
    }

    public kfsRelation(kfsDbServerType st, String tableName, String id1Name, String id2Name) {
        super(st, tableName, id1Name, id2Name, new createDbo<kfsInt>() {

            @Override
            public kfsInt createDbo(String name, int pos) {
                return new kfsIntFk(name, pos, false);
            }
        });
        
    }

    @Override
    protected void setPsData(PreparedStatement ps, int inx, Integer obj) throws SQLException {
        ps.setInt(inx, obj);
    }

   
}
