package kfs.kfsDbi;

import java.util.Comparator;

/**
 *
 * @author pavedrim
 */
public interface kfsDbiColumnComparator extends Comparator<kfsRowData> {

    int getSortDirection();

    void setSortDirection(int val);
}
