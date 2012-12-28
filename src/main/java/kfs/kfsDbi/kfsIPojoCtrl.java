package kfs.kfsDbi;

/**
 *
 * @author pavedrim
 */
public interface kfsIPojoCtrl{

    kfsIPojoObj getNewPojo();
    kfsIPojoObj getPojo(kfsRowData row);
}
