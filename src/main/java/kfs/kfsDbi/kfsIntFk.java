package kfs.kfsDbi;

/**
 * forein key
 *
 * @author pavedrim
 */
public class kfsIntFk extends kfsInt {

    public kfsIntFk(String name, int pos, boolean allowNull) {
        super(name, name, kfsIntAutoInc.idMaxLen, pos, allowNull);
    }
}
