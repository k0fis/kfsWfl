package kfs.kfsDbiStd;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import kfs.kfsDbi.*;

/**
 *
 * @author pavedrim
 */
public class kfsRelationString extends kfsRelationGen<kfsString, String> {

    public kfsRelationString(kfsDbServerType st, String tableName, final String id1Name, //
            final int len1, String id2Name, final int len2) {
        super(st, tableName, id1Name, id2Name, new createDbo<kfsString>() {

            @Override
            public kfsString createDbo(String name, int pos) {
                if (name.equals(id1Name)) {
                    return new kfsString(name, name, len1, pos);
                }
                return new kfsString(name, name, len2, pos);
            }
        });
    }
    
    public lstRelation get() {
        return null;
    }

    @Override
    protected void setPsData(PreparedStatement ps, int inx, String obj) throws SQLException {
        ps.setString(inx, obj);
    }
}
