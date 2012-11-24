package kfs.kfsWfl;

import java.util.Date;
import kfs.kfsDbi.*;

/**
 *
 * @author pavedrim
 */
public class wflFile extends kfsDbObject {

    private final kfsIntAutoInc id;
    private final kfsInt idNode;
    private final kfsString name;
    private final kfsDate date;
    private final kfsString userLogin;
    private final kfsBlob data;

    public wflFile(final kfsDbServerType serverType) {
        super(serverType, "T_WFL_FILE");
        int pos = 0;
        id = new kfsIntAutoInc("ID", "Id", pos++);
        idNode = new kfsInt("ID_NODE", "Node ID", kfsIntAutoInc.idMaxLen, pos++, false);
        name = new kfsString("FILE_NAME", "Name", 128, pos++);
        date = new kfsDate("FILE_DATE", "Date", pos++);
        userLogin = new kfsString("USER", "Uživatel", wflUser.loginLen, pos++);
        data = new kfsBlob("FILE_DATA", "Data", pos++, serverType);

        super.setColumns(new kfsDbiColumn[]{id, idNode, name, date, userLogin, data});
        super.setIdsColumns(new kfsDbiColumn[]{id});
    }
    
    public kfsRowData create(int idNode, String name, String user, byte [] data) {
        kfsRowData ret = new kfsRowData(this);
        this.idNode.setInt(idNode, ret);
        this.name.setString(name, ret);
        this.date.setDate(new Date(), ret);
        this.userLogin.setString(user, ret);
        this.data.setData(data, ret);
        return ret;
    }
    
    public int getId(kfsRowData r) {
        return id.getInt(r);
    }
    public int getIdNode(kfsRowData r) {
        return idNode.getInt(r);
    }
    public String getName(kfsRowData r) {
        return name.getString(r);
    }
    public Date getDate(kfsRowData r) {
        return date.getDate(r);
    }
    public String getUserLogin(kfsRowData r) {
        return userLogin.getString(r);
    }
    public Object getData(kfsRowData r) {
        return data.getObject(r);
    }
}
