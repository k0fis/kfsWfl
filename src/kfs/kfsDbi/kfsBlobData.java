package kfs.kfsDbi;

import java.io.InputStream;

/**
 *
 * @author pavedrim
 */
public class kfsBlobData {
    private InputStream ind;
    private long length;
    
    public kfsBlobData(InputStream ind, long length) {
        this.ind = ind;
        this.length = length;
    }
    
    public long getLength() {
        return length;
    }

    public InputStream getInputStream() {
        return ind;
    }
    
}
