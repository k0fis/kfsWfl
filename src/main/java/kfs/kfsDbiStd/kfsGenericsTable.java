package kfs.kfsDbiStd;

import kfs.kfsDbi.kfsColObject;
import kfs.kfsDbi.kfsDbObject;
import kfs.kfsDbi.kfsDbServerType;

/**
 *
 * @author pavedrim
 */
public class kfsGenericsTable extends kfsDbObject {

    //private kfsICreateDbc []cols;
    protected final kfsColObject[] columns;

    public kfsGenericsTable(kfsDbServerType st, String tableName, kfsICreateDbc... cols) {
        super(st, tableName);
        //this.cols = cols;
        columns = new kfsColObject[cols.length];
        for (int i = 0; i < cols.length; i++) {
            columns[i] = cols[i].createDbo(i);
        }
        super.setColumns(columns);
    }

}
