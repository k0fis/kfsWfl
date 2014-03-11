package kfs.kfsGenDbi;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author pavedrim
 */
public class createFromClass {

    static class item {

        final Method meth;

        item(Method meth) {
            this.meth = meth;
        }

        String getGetterJavaName() {
            return meth.getName();
        }

        String getSetterJavaName() {
            return 's' + getGetterJavaName().substring(1);
        }

        String getJavaName() {
            String s = meth.getName();
            return Character.toLowerCase(s.charAt(3)) + s.substring(4);
        }

        Class<?> getJavaClass() {
            Class<?> c = meth.getReturnType();
            if (Timestamp.class.equals(c)) {
                c = Date.class;
            } else if (int.class.equals(c)) {
                c = Integer.class;
            } else if (long.class.equals(c)) {
                c = Long.class;
            }
            return c;
        }

        String getJavaClassName() {
            return getJavaClass().getSimpleName();
        }

        String getKfsDbiName() {
            StringBuilder sb = new StringBuilder();
            String[] r = getJavaName().split("(?=\\p{Lu})");
            boolean f = true;
            for (String s : r) {
                if (f) {
                    f = false;
                } else {
                    sb.append("_");
                }
                sb.append(s.toUpperCase());
            }
            return sb.toString();
        }

        String getHumanName() {
            StringBuilder sb = new StringBuilder();
            String[] r = getJavaName().split("(?=\\p{Lu})");
            boolean f = true;
            for (String s : r) {
                if (f) {
                    f = false;
                } else {
                    sb.append(" ");
                }
                sb.append(s.substring(0, 1).toUpperCase()).append(s.substring(1));
            }
            return sb.toString();
        }

        String getKfsDbiClass() {
            String s = getJavaClassName();
            if (s.equals("Integer")) {
                s = "Int";
            }
            return "kfs" + s;
        }

        String getDbiNew() {
            boolean r = false;
            StringBuilder sb = new StringBuilder();
            sb.append("new ").append(getKfsDbiClass()).append("(\"").append(getKfsDbiName())
                    .append("\", \"").append(getHumanName()).append("\"");
            if (Date.class.equals(getJavaClass())) {
                sb.append(", pos++");
                r = true;
            } else if (String.class.equals(getJavaClass())) {
                sb.append(", 50, pos++");
                r = true;
            } else if (Integer.class.equals(getJavaClass())) {
                sb.append(", 10, pos++, true");
                r = true;
            } else if (Long.class.equals(getJavaClass())) {
                sb.append(", 18, pos++, true");
                r = true;
            }
            if (!r) {
                return "null";
            }
            sb.append(")");
            return sb.toString();
        }
    }

    private final Class<?> cls;
    private final boolean useOraPartitioning;
    private final boolean useAutoId;
    private final boolean createSetters;
    private final boolean createList;
    private final String packageName;
    private final String className;
    private final item[] items;

    public createFromClass(Class<?> cls, String packageName, String className) throws IntrospectionException {
        this(cls, packageName, className, true, true, false, false);
    }

    public createFromClass(Class<?> cls, String packageName, String className, //
            boolean useOraPartitioning, boolean useAutoId, boolean createSetters,//
            boolean createList) throws IntrospectionException {
        this.cls = cls;
        this.useOraPartitioning = useOraPartitioning;
        this.useAutoId = useAutoId;
        this.createSetters = createSetters;
        this.packageName = packageName;
        this.className = className;
        this.createList = createList;
        ArrayList<item> il = new ArrayList<item>();
        for (PropertyDescriptor propertyDescriptor
                : Introspector.getBeanInfo(cls, Object.class).getPropertyDescriptors()) {
            Method meth = propertyDescriptor.getReadMethod();
            if (meth != null) {
                il.add(new item(meth));
            }
        }
        items = il.toArray(new item[il.size()]);
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
        for (item i : items) {
            sb.append("    private final ").append(i.getKfsDbiClass()).append(" ").append(i.getJavaName()).append(";\n");
        }
        if (useOraPartitioning) {
            sb.append("    private final kfsInt cDayNo;\n");
        }
        sb.append("\n    public ").append(className).append("(kfsDbServerType dbType, String tableName) {\n")
                .append("        super(dbType, tableName);\n")
                .append("        int pos = 0;\n");
        if (useAutoId) {
            sb.append("        id = new kfsLongAutoInc(\"ID\", pos++).setsequenceCycle(true);\n");
        }
        for (item i : items) {
            sb.append("        ").append(i.getJavaName()).append(" = ").append(i.getDbiNew()).append(";\n");
        }
        if (useOraPartitioning) {
            sb.append("        cDayNo = new kfsInt(\"C_DAY_NO\", \"C_DAY_NO\", 8, pos++, false);\n");
        }
        sb.append("        super.setColumns(\n");
        if (useAutoId) {
            sb.append("                id,\n");
        }
        for (item i : items) {
            sb.append("                ").append(i.getJavaName()).append(",\n");
        }
        if (useOraPartitioning) {
            sb.append("                cDayNo");
        }
        sb.append(" );\n");
        if (useAutoId) {
            sb.append("        super.setIdsColumns(id);\n");
        }
        sb.append("    }\n\n");

        if (useAutoId) {
            sb.append("    public String sqlNewId() {\n        return \"SELECT \" + id.getOraSequenceName(getName())+\".nextval FROM DUAL\";\n    }\n\n");
        }

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
        for (item i : items) {
            sb.append("        ").append(i.getJavaName()).append(".setData(xdr.").append(i.getGetterJavaName()).append("(), r);\n");
        }

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
            sb
                    .append("        public Long getId() {\n"
                            + "            return inx.id.getData(rd);\n"
                            + "        }\n\n")
                    .append("        public void setId(Long newId) {\n"
                            + "            inx.id.setData(newId, rd);\n"
                            + "        }\n\n");
        }
        for (item i : items) {
            sb.append("        public ").append(i.getJavaClassName()).append(" ").append(i.getGetterJavaName())//
                    .append("() {\n").append("            return inx.").append(i.getJavaName())
                    .append(".getData(rd);\n")
                    .append("        }\n\n");
            if (createSetters) {
                sb.append("        public void ").append(i.getSetterJavaName())
                        .append("(").append(i.getJavaClassName())
                        .append(" ").append(i.getJavaName())
                        .append(") {\n" + "            inx.")
                        .append(i.getJavaName()).append(".setData(")
                        .append(i.getJavaName()).append(", rd);\n        }\n\n");
            }
        }

        if (useOraPartitioning) {
            sb.append("        public Integer getDayNo() {\n"
                    + "            return inx.cDayNo.getData(rd);\n"
                    + "        }\n");
        }
        sb.append("    }\n");
        if (createList) {
            sb.append("\n    public class ").append(className)//
                    .append("List extends kfsPojoArrayList<").append(className)//
                    .append(", pojo> {\n" + "\n" + "        public ")//
                    .append(className).append("List() {\n" + "            super(")//
                    .append(className).append(".this);\n"
                            + "        }\n"
                            + "    }\n\n"
                    );
        }

        return sb.append("}\n").toString();
    }

}
