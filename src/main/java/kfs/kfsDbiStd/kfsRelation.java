package kfs.kfsDbiStd;

import kfs.kfsDbi.*;

/**
 *
 * @author pavedrim
 */
public class kfsRelation extends kfsRelationGen<kfsInt, Integer, kfsInt, Integer> {

    public kfsRelation(kfsDbServerType st, String tableName) {
        this(st, tableName, "ID1", "ID2");
    }

    public kfsRelation(kfsDbServerType st, String tableName, String id1Name, String id2Name) {
        super(st, tableName, new kfsCreateDbcInt(id1Name), new kfsCreateDbcInt(id2Name));
    }

}
