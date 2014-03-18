
package kfs.kfsGenDbi;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

/**
 *
 * @author pavedrim
 */
public class kfsRowItem {

    private final Class<?> cls;
    private final String javaName;
    
    
    public kfsRowItem(Class<?> cls, String javaName) {
        this.cls = cls;
        this.javaName = javaName;
    }

    Class<?> getJavaType() {
        return cls;
    }
    
    String getJavaName() {
        return javaName;
    }

    String getGetterJavaName() {
        String s = getJavaName();
        return "get"+ Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    String getSetterJavaName() {
        String s = getJavaName();
        return "set"+ Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }


    Class<?> getJavaClass() {
        Class<?> c = getJavaType();
        if (Timestamp.class.equals(c)) {
            c = Date.class;
        } else if (int.class.equals(c)) {
            c = Integer.class;
        } else if (long.class.equals(c)) {
            c = Long.class;
        } else if (BigDecimal.class.equals(c)) {
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

    boolean isFk() {
        if (!getKfsDbiName().endsWith("Id")) {
            return false;
        }
        String s = getJavaClassName();
        return s.equals("Integer") | s.equals("Long");
    }

    String getKfsDbiClass() {
        String s = getJavaClassName();
        if (s.equals("Integer")) {
            s = "Int";
        }
        if (isFk()) {
            s += "Fk";
        }
        return "kfs" + s;
    }

    String getDbiNew() {
        boolean r = false;
        StringBuilder sb = new StringBuilder();
        sb.append("new ").append(getKfsDbiClass()).append("(\"").append(getKfsDbiName());
        if (Date.class.equals(getJavaClass())) {
            sb.append("\", \"").append(getHumanName()).append("\"").append(", pos++");
            r = true;
        } else if (String.class.equals(getJavaClass())) {
            sb.append("\", \"").append(getHumanName()).append("\"").append(", 50, pos++");
            r = true;
        } else if (Integer.class.equals(getJavaClass())) {
            if (isFk()) {
                sb.append(", pos++, true");
            } else {
                sb.append("\", \"").append(getHumanName()).append("\"").append(", 10, pos++, true");
            }
            r = true;
        } else if (Long.class.equals(getJavaClass())) {
            if (isFk()) {
                sb.append(", pos++, true");
            } else {
                sb.append("\", \"").append(getHumanName()).append("\"").append(", 18, pos++, true");
            }
            r = true;
        }
        if (!r) {
            return "null";
        }
        sb.append(")");
        return sb.toString();
    }
    
}
