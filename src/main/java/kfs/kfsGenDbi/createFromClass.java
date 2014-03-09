package kfs.kfsGenDbi;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

/**
 *
 * @author pavedrim
 */
public class createFromClass {

    private final Class<?> cls;
    private final boolean useOraPartitioning;
    private final boolean useAutoId;
    private final String packageName;
    private final String className;

    public createFromClass(Class<?> cls, String packageName, String className) {
        this.cls = cls;
        this.useOraPartitioning = true;
        this.useAutoId = true;
        this.packageName = packageName;
        this.className = className;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(packageName).append(";\n\n");
        if (useOraPartitioning) {
            sb.append("import java.text.SimpleDateFormat;\nimport java.util.Date;\n");
        }
        if (!cls.getPackage().getName().equals(packageName)) {
            sb.append("import ").append(cls.getName()).append(";\n");
        }
        sb.append("import kfs.kfsDbi.*;\n\n");
        sb.append("public class ").append(className).append(" extends kfsDbObject {\n\n");
        if (useAutoId) {
            sb.append("    private final kfsLongAutoInc id;\n");
        }
        /// defs
        ///
        ///
        if (useOraPartitioning) {
            sb.append("    private final kfsInt cDayNo;\n");
        }
        sb.append("\n    public ").append(className).append("(kfsDbServerType dbType, String tableName) {\n")
                .append("        super(dbType, tableName);\n")
                .append("        int pos = 0;\n");
        if (useAutoId) {
            sb.append("        id = new kfsLongAutoInc(\"ID\", pos++).setsequenceCycle(true);\n");
        }
        ///
        ///
        if (useOraPartitioning) {
            sb.append("        cDayNo = new kfsInt(\"C_DAY_NO\", \"C_DAY_NO\", 8, pos++, false);\n");
        }
        sb.append("        super.setColumns(\n");
        if (useAutoId) {
            sb.append("                id,\n");
        }
        if (useOraPartitioning) {
            sb.append("                cDayNo");
        }
        sb.append(" );\n");
        if (useAutoId) {
            sb.append("        super.setIdsColumns(id);\n");
        }
        sb.append("    }\n\n");

        if (useOraPartitioning) {
            sb.append("    @Override\n"
                    + "    public String getCreateTable() {\n"
                    + "        return super.getCreateTable() + \n"
                    + "                \" PARTITION BY LIST(\" + cDayNo.getColumnName() +\n"
                    + "                \") ( PARTITION P_20110904 VALUES ( 20110904 ) )\";\n"
                    + "    }\n\n");
            sb.append("    private static final SimpleDateFormat sdf = new SimpleDateFormat(\"yyyyMMdd\");\n\n");
        }
        sb.append("    public kfsRowData create(").append(cls.getSimpleName()).append(" xdr) {\n"
                + "        kfsRowData r = new kfsRowData(this);\n");
        ///
        ///
        if (useOraPartitioning) {
            sb.append("        cDayNo.setData(Integer.parseInt(sdf.format(new Date())), r);\n");
        }
        sb.append("        return r;\n    }\n\n");
        sb.append("    @Override\n"
                + "    public pojo getPojo(kfsRowData row) {\n"
                + "        return new pojo(row);\n"
                + "    }\n\n");
        sb.append("    public class pojo extends kfsPojoObj<").append(className).append("> {\n\n"
                + "        public pojo(kfsRowData row) {\n"
                + "            super(").append(className).append(".this, row);\n"
                        + "        }\n\n");
        if (useAutoId) {
            sb.append("        public Long getId() {\n"
                    + "            return inx.id.getData(rd);\n"
                    + "        }\n\n");
        }
        ///
        ///

        if (useOraPartitioning) {
            sb.append("        public Integer getDayNo() {\n"
                    + "            return inx.cDayNo.getData(rd);\n"
                    + "        }\n");
        }
        sb.append("    }\n");
        return sb.append("}\n").toString();
    }

    public void pako() throws IntrospectionException {
        for (PropertyDescriptor propertyDescriptor
                : Introspector.getBeanInfo(cls, Object.class).getPropertyDescriptors()) {
            Method meth = propertyDescriptor.getReadMethod();
            if (meth != null) {
                System.out.println(meth.getReturnType().getSimpleName() + " " + meth.getName());
            }
        }
    }
}
