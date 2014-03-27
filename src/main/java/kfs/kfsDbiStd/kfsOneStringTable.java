package kfs.kfsDbiStd;

import kfs.kfsDbi.*;

/**
 *
 * @author pavedrim
 */
public class kfsOneStringTable extends kfsDbObject {

    private final kfsString data;

    public kfsOneStringTable(kfsDbServerType st, String tableName, String colName, int maxLen) {
        super(st, tableName);
        int pos = 0;
        data = new kfsString(colName, colName, maxLen, pos++);
        super.setColumns(data);
    }

    @Override
    public String getCreateTable() {
        if (getName() == null) {
            return null;
        } else {
            return super.getCreateTable();
        }
    }

    @Override
    public pjOneString getPojo(kfsRowData rd) {
        return new pjOneString(rd);
    }

    public class pjOneString extends kfsPojoObj<kfsOneStringTable> {

        public pjOneString(kfsRowData row) {
            super(kfsOneStringTable.this, row);
        }

        public String getData() {
            return inx.data.getData(rd);
        }

        public void setData(String newVal) {
            inx.data.setData(newVal, rd);
        }
    }
    
    public class pjOneStringList extends kfsPojoArrayList<kfsOneStringTable, pjOneString> {

        public pjOneStringList() {
            super(kfsOneStringTable.this);
        }
    }    
}
