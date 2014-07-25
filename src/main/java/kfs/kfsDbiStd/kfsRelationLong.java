package kfs.kfsDbiStd;

import kfs.kfsDbi.*;

/**
 *
 * @author pavedrim
 */
public class kfsRelationLong extends kfsRelationGen<kfsLong, Long, kfsLong, Long> {

    public kfsRelationLong(kfsDbServerType st, String tableName) {
        this(st, tableName, "ID1", "ID2");
    }

    public kfsRelationLong(kfsDbServerType st, String tableName, String id1Name, String id2Name) {
        super(st, tableName, new kfsCreateDbcLong(id1Name), new kfsCreateDbcLong(id2Name));
    }

}
