package kfs.kfsWfl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import kfs.kfsDbi.*;

/**
 *
 * @author pavedrim
 */
public class wflUser extends kfsDbObject {

    public static final int loginLen = 30;
    private final kfsString login;
    private final kfsString name;
    private final kfsString mail;

    public wflUser(final kfsDbServerType serverType) {
        super(serverType, "WFL_USER");
        int pos = 0;
        login = new kfsString("LOGIN", "Login", loginLen, pos++);
        name = new kfsString("NAME", "Name", 128, pos++);
        mail = new kfsString("MAIL", "Mail", 128, pos++);
        super.setColumns(new kfsDbiColumn[]{login, name, mail});
        super.setUpdateColumns(new kfsDbiColumn[]{name, mail});
        super.setIdsColumns(new kfsDbiColumn[] {login});
    }

    public kfsRowData create(String login) {
        kfsRowData r = new kfsRowData(this);
        this.login.setString(login, r);
        return r;
    }


    public String getLogin(kfsRowData r) {
        return login.getString(r);
    }
    
    public String getName(kfsRowData r) {
        return name.getString(r);
    }
    
    public void setName(kfsRowData r, String name) {
        this.name.setString(name, r);
    }
    
    public String getMail(kfsRowData r) {
        return this.mail.getString(r);
    }
    
    public void setMail(kfsRowData r, String mail) {
        this.mail.setString(mail, r);
    }
    
    public String sqlSelectByLogin() {
        return getSelect(getName(), super.getColumns(), new kfsDbiColumn[] {login});
    }
    public void psSelectByLogin(PreparedStatement ps, String login) throws SQLException {
        ps.setString(1, login);
    }
}
