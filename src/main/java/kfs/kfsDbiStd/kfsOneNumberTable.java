package kfs.kfsDbiStd;

import kfs.kfsDbi.*;

/**
 *
 * @author pavedrim
 */
public class kfsOneNumberTable extends kfsDbObject {

    private final kfsDouble data;

    public kfsOneNumberTable(kfsDbServerType st, String tableName, String colName, int maxLen) {
        super(st, tableName);
        int pos = 0;
        data = new kfsDouble(colName, colName, pos++, true);
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
    public pjOneNumber getPojo(kfsRowData rd) {
        return new pjOneNumber(rd);
    }

    public class pjOneNumber extends kfsPojoObj<kfsOneNumberTable> {

        public pjOneNumber(kfsRowData row) {
            super(kfsOneNumberTable.this, row);
        }

        public Double getData() {
            return inx.data.getData(rd);
        }

        public void setData(Double newVal) {
            inx.data.setData(newVal, rd);
        }

        @Override
        public String toString() {
            return getData() != null ? getData().toString() : "";
        }
    }

    public class pjOneNumberList extends kfsPojoArrayList<kfsOneNumberTable, pjOneNumber> {

        public pjOneNumberList() {
            super(kfsOneNumberTable.this);
        }
    }
}
