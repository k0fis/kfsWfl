package kfs.kfsGenDbi;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

/**
 *
 * @author pavedrim
 */
public class createFromClass extends kfsTableGenerator {

    public createFromClass(Class<?> cls, String packageName, String className) throws IntrospectionException {
        this(cls, packageName, className, true, true, false, false);
    }

    public createFromClass(Class<?> cls, String packageName, String className, //
            boolean useOraPartitioning, boolean useAutoId, boolean createSetters,//
            boolean createList) throws IntrospectionException {
        super(cls, packageName, className, useOraPartitioning, useAutoId, createSetters, createList);
        for (PropertyDescriptor propertyDescriptor
                : Introspector.getBeanInfo(cls, Object.class).getPropertyDescriptors()) {
            Method meth = propertyDescriptor.getReadMethod();
            if (meth != null) {
                addItem(new kfsRowItem(meth.getReturnType(), getJavaName(meth.getName())));
            }
        }
    }

    private static String getJavaName(String getterName) {
        return Character.toLowerCase(getterName.charAt(3)) + getterName.substring(4);
    }    

}
