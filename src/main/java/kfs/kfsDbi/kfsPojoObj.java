package kfs.kfsDbi;

/**
 *
 * @author pavedrim
 * @param <T>
 */
public class kfsPojoObj<T extends kfsDbObject> implements kfsIPojoObj {

    protected final kfsRowData rd;
    protected final T inx;
    
    protected kfsPojoObj(final T inx, final kfsRowData rd) {
        this.rd = rd;
        this.inx = inx;
    }
    
    @Override
    public kfsRowData kfsGetRow() {
        return rd;
    }

    @Override
    public kfsDbObject kfsGetDbObject() {
        return inx;
    }

    public String kfsGetCsv() {
        return inx.getCsv(rd);
    }
}
