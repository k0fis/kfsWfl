package kfs.kfsWfl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import kfs.kfsDbi.*;

/**
 *
 * @author pavedrim
 */
public class wflNote extends kfsDbObject {

    private final kfsIntAutoInc id;
    private final kfsInt idNode;
    private final kfsString text;
    private final kfsDate date;
    private final kfsString userLogin;

    public wflNote(final kfsDbServerType serverType) {
        super(serverType, "T_WFL_NOTE");
        int pos = 0;
        id = new kfsIntAutoInc("ID", "Id", pos++);
        idNode = new kfsInt("ID_NODE", "Node ID", kfsIntAutoInc.idMaxLen, pos++, false);
        text = new kfsString("NOTE_TEXT", "Text", 2048, pos++);
        date = new kfsDate("NOTE_DATE", "Date", pos++);
        userLogin = new kfsString("USER_NAME", "U6ivatel", 30, pos++);

        super.setColumns(new kfsDbiColumn[]{id, idNode, text, date, userLogin,});
        super.setIdsColumns(new kfsDbiColumn[]{id});
    }

    public kfsRowData create(int idNode, String text, String user) {
        kfsRowData ret = new kfsRowData(this);
        this.idNode.setInt(idNode, ret);
        this.text.setString(text, ret);
        this.date.setDate(new Date(), ret);
        this.userLogin.setString(user, ret);
        return ret;
    }

    public int getId(kfsRowData r) {
        return id.getInt(r);
    }

    public int getIdNode(kfsRowData r) {
        return idNode.getInt(r);
    }

    public String getText(kfsRowData r) {
        return text.getString(r);
    }

    public Date getDate(kfsRowData r) {
        return date.getDate(r);
    }

    public String getUserLogin(kfsRowData r) {
        return userLogin.getString(r);
    }
    
    public void psSelectByNode(PreparedStatement ps, int nodeId) throws SQLException {
        ps.setInt(1, nodeId);
    }
    
    public String sqlSelectByNode() {
        return getSelect(getName(), getColumns(), new kfsDbiColumn[] {idNode});
    }
}
