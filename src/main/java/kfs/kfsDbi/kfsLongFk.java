package kfs.kfsDbi;

/**
 * forein key
 *
 * @author pavedrim
 */
public class kfsLongFk extends kfsLong {

    public kfsLongFk(String name, int pos, boolean allowNull) {
        super(name, name, kfsLongAutoInc.idMaxLen, pos, allowNull);
    }
}
