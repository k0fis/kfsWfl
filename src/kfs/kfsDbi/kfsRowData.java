package kfs.kfsDbi;


/**
 *
 * @author pavedrim
 */
public class kfsRowData  {

    private Object[] data;
    private final kfsDbObject desc;

    public kfsRowData(final kfsDbObject desc) {
        this.desc = desc;
        data = new Object[desc.getColumnCount()];
    }

    public int getCount() {
        return data.length;
    }

    public Object[] getContent() {
        return data;
    }

    public Object getObject(int position) {
        return data[position];
    }

    public void setObject(int position, Object obj) {
        data[position] = obj;
    }

    public kfsDbObject getDesc() {
        return desc;
    }
}
