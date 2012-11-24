package kfs.kfsDbi;

/**
 *
 * @author pavedrim
 */
interface kfsDbiTableNewInstance<T extends kfsDbiTable> {

    T createNew();
}
