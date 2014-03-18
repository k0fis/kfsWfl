package kfs.kfsGenDbi;

import java.util.ArrayList;

/**
 *
 * @author pavedrim
 */
public class kfsTableGenerator {

    private final Class<?> cls;
    private final boolean useOraPartitioning;
    private final boolean useAutoId;
    private final boolean createSetters;
    private final boolean createList;
    private final String packageName;
    private final String className;
    private final ArrayList<kfsRowItem> items;

    public kfsTableGenerator(Class<?> cls, String packageName, String className, //
            boolean useOraPartitioning, boolean useAutoId, boolean createSetters,//
            boolean createList) {
        this.cls = cls;
        this.useOraPartitioning = useOraPartitioning;
        this.useAutoId = useAutoId;
        this.createSetters = createSetters;
        this.packageName = packageName;
        this.className = className;
        this.createList = createList;
        items = new ArrayList<kfsRowItem>();
    }

    protected void addItem(kfsRowItem item) {
        items.add(item);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(packageName).append(";\n\n");
        if (useOraPartitioning) {
            sb.append("import java.text.SimpleDateFormat;\nimport java.util.Date;\n");
        }
        if (cls != null) {
            if (!cls.getPackage().getName().equals(packageName)) {
                sb.append("import ").append(cls.getName()).append(";\n");
            }
        }
        sb.append("import kfs.kfsDbi.*;\n\n");
        sb.append("public class ").append(className).append(" extends kfsDbObject {\n\n");
        if (useAutoId) {
            sb.append("    private final kfsLongAutoInc id;\n");
        }
        for (kfsRowItem i : items) {
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
        for (kfsRowItem i : items) {
            sb.append("        ").append(i.getJavaName()).append(" = ").append(i.getDbiNew()).append(";\n");
        }
        if (useOraPartitioning) {
            sb.append("        cDayNo = new kfsInt(\"C_DAY_NO\", \"C_DAY_NO\", 8, pos++, false);\n");
        }
        sb.append("        super.setColumns(\n");
        if (useAutoId) {
            sb.append("                id,\n");
        }
        for (kfsRowItem i : items) {
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
        if (cls != null) {
            sb.append("    public kfsRowData create(").append(cls.getSimpleName()).append(" xdr) {\n"
                    + "        kfsRowData r = new kfsRowData(this);\n");
            for (kfsRowItem i : items) {
                sb.append("        ").append(i.getJavaName()).append(".setData(xdr.").append(i.getGetterJavaName()).append("(), r);\n");
            }

            if (useOraPartitioning) {
                sb.append("        cDayNo.setData(Integer.parseInt(sdf.format(new Date())), r);\n");
            }
            sb.append("        return r;\n    }\n\n");
        } else {
            sb.append("    public kfsRowData create() {\n"
                    + "        kfsRowData r = new kfsRowData(this);\n");
            for (kfsRowItem i : items) {
                sb.append("        ").append(i.getJavaName()).append(".setData(xdr.").append(i.getGetterJavaName()).append("(), r);\n");
            }

            if (useOraPartitioning) {
                sb.append("        cDayNo.setData(Integer.parseInt(sdf.format(new Date())), r);\n");
            }
            sb.append("        return r;\n    }\n\n");

        }
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
        for (kfsRowItem i : items) {
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
