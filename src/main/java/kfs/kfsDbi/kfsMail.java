package kfs.kfsDbi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author pavedrim
 */
public class kfsMail extends kfsString {

    public kfsMail(String name, String label, int posistion) {
        super(name, label, 256, posistion, defaultCharSet);
    }
    
    public boolean isValid(kfsRowData rd) {
        return isValidMail(getString(rd));
    }
        
    public static final String mailValidExpression = "^[\\w\\-]+(\\.[\\w\\-]+)*@([A-Za-z0-9-]+\\.)+[A-Za-z]{2,4}$";
    private static final Pattern pattern = Pattern.compile(mailValidExpression, Pattern.CASE_INSENSITIVE);

    public static boolean isValidMail(String s) {
        boolean isValid = false;
        Matcher matcher = pattern.matcher(s);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }
}
