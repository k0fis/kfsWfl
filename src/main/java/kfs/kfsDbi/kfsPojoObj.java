package kfs.kfsDbi;

import java.lang.reflect.Field;

/**
 *
 * @author pavedrim
 * @param <T>
 */
public class kfsPojoObj<T extends kfsDbObject> implements kfsIPojoObj {

    protected final kfsRowData rd;
    protected final T inx;

    protected kfsPojoObj(final T inx, final kfsRowData rd) {
        this.rd = rd;
        this.inx = inx;
    }

    @Override
    public kfsRowData kfsGetRow() {
        return rd;
    }

    @Override
    public kfsDbObject kfsGetDbObject() {
        return inx;
    }

    public String kfsGetCsv() {
        return inx.getCsv(rd);
    }

    public String kfsStringRepre() {
        StringBuilder result = new StringBuilder();
        String newLine = "\n";

        result.append(this.getClass().getName());
        result.append(" ").append(this.getClass().getName()).append(" {");
        result.append(newLine);

        //determine fields declared in this class only (no fields of superclass)
        Field[] fields = this.getClass().getDeclaredFields();

        //print field names paired with their values
        for (Field field : fields) {
            result.append("    ");
            try {
                result.append(field.getName());
                result.append(": ");
                //requires access to private field:
                Object o = field.get(this);
                if (o instanceof int[]) {
                    for (int i : (int[]) o) {
                        result.append(i).append(", ");
                    }
                } else if (o instanceof Integer) {
                    Integer i = (Integer) o;
                    result.append(i);
                } else {
                    result.append(o);
                }
            } catch (IllegalAccessException ex) {
                System.out.println(ex);
            }
            result.append(newLine);
        }
        result.append("  }");

        return result.toString();
    }

}
