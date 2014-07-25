package kfs.kfsDbiStd;

import kfs.kfsDbi.*;

/**
 *
 * @author pavedrim
 */
public class kfsRelationString extends kfsRelationGen<kfsString, String, kfsString, String> {

    public kfsRelationString(kfsDbServerType st, String tableName, final String id1Name, //
            final int len1, String id2Name, final int len2) {
        super(st, tableName, new kfsCreateDbcString(id1Name, len1), new kfsCreateDbcString(id2Name, len2));
    }

}
