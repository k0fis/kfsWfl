package kfs.kfsDbi;

/**
 *
 * @author pavedrim
 */
public class kfsPhone extends kfsString {

    public kfsPhone(String name, String label, int pos) {
        super(name, label, 22, pos, defaultCharSet);
    }

    public boolean isValid(kfsRowData rd) {
        return true;
    }

    @Override
    public void setString(String s, kfsRowData rd) {
        super.setString(convertPhone(s), rd);
    }

    public static String convertPhone(String s) {
        if ((s == null) || (s.isEmpty())) {
            return "";
        }
        s = s.replaceAll("\\D", "");
        /*
        if (s.startsWith("+")) {
            s = s.substring(1);
        }
        if (s.startsWith(".")) {
            s = s.substring(1);
        }
        if (s.startsWith("00")) {
            s = s.substring(2);
        }
        if (s.startsWith("0")) {
            s = s.substring(1);
        }
        if (s.startsWith("Tel.")) {
            s = s.substring(4);
        }
        */
        if (s.isEmpty()) {
            return s;
        }

        if (s.startsWith("420")) {
            return s;
        } else if (isCzCellPhone2(s)) {
            return "420" + s;
        }
        return s;
    }

    public static boolean isCzCellPhone(String s) {
        if (s.startsWith("420")) {
            return isCzCellPhone2(s.substring(3));
        }
        return isCzCellPhone2(s);
    }

    public static boolean isCzCellPhone2(String s) {
        return (s.length() == 9) && (
                s.startsWith("77") || //
                s.startsWith("72") || //
                s.startsWith("73") || //
                s.startsWith("601") || //
                s.startsWith("602") || //
                s.startsWith("603") || //
                s.startsWith("604") || //
                s.startsWith("605") || //
                s.startsWith("606") || //
                s.startsWith("607") || //
                s.startsWith("608"));
    }
}
