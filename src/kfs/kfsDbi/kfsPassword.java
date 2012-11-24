package kfs.kfsDbi;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pavedrim
 */
public class kfsPassword extends kfsString {

    public kfsPassword(String name, String label, int pos) {
        super(name, label, 50, pos, defaultCharSet);
    }

    public void encode(String input, kfsRowData rd) {
        setString(encode0(input), rd);
    }

    public boolean equalsPass(String input, kfsRowData rd) {
        return getString(rd).equals(encode0(input));
    }
    private static MessageDigest md = null;

    public static String encode0(String input) {
        if (input == null) {
            return "";
        }
        if (md == null) {
            try {
                md = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(kfsPassword.class.getName()).log(Level.SEVERE, "Canot find MD5", ex);
            }
        }
        md.update(input.getBytes());
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", (int) (b & 0xff)));
        }
        return sb.toString();
    }
}
