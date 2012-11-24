package kfs.kfsDbi;

/**
 *
 * @author pavedrim
 */
public interface kfsTableDesc {

    Class<?> getColumnJavaClass(int column);
    String getColumnLabel(int column);
    int getColumnCount();
    Object getObject(kfsRowData row, int column);
}
