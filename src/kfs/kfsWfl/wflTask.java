package kfs.kfsWfl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import kfs.kfsDbi.*;

/**
 *
 * @author pavedrim
 */
public class wflTask extends kfsDbObject {

    private final kfsIntAutoInc id;
    private final kfsInt idTemplate;
    private final kfsString name;
    private final kfsString ownerLogin;
    private final kfsInt firstNodeId;
    private final kfsDate lastChange;
    private final kfsDate archivedDate;

    public wflTask(final kfsDbServerType serverType) {
        super(serverType, "T_WFL_TASK");
        int pos = 0;
        id = new kfsIntAutoInc("ID", "Id", pos++);
        idTemplate = new kfsInt("ID_TEMPLATE", "Template ID", kfsIntAutoInc.idMaxLen, pos++, true);
        name = new kfsString("NAME", "Name", 48, pos++);
        ownerLogin = new kfsString("OWNER_LOGIN", "Owner Login", wflUser.loginLen, pos++);
        firstNodeId = new kfsInt("FIRST_NODE_ID", "First Node Id", kfsIntAutoInc.idMaxLen, pos++, true);
        lastChange = new kfsDate("LAST_CHNG", "Last Change", pos++);
        archivedDate = new kfsDate("ARCHIVED_DATE", "Archived Date", pos++);

        super.setColumns(new kfsDbiColumn[]{id, idTemplate, ownerLogin, name, firstNodeId, lastChange, archivedDate});
        super.setUpdateColumns(new kfsDbiColumn[]{ownerLogin, name, firstNodeId, lastChange, archivedDate});
        super.setIdsColumns(new kfsDbiColumn[]{id});
    }

    public kfsRowData create(int templId, String ownerLogin) {
        kfsRowData ret = new kfsRowData(this);
        this.idTemplate.setInt(templId, ret);
        this.ownerLogin.setString(ownerLogin, ret);
        return ret;
    }

    public kfsRowData create() {
        return new kfsRowData(this);
    }

    public int getId(kfsRowData r) {
        return id.getInt(r);
    }

    public Integer getTemplateId(kfsRowData r) {
        return idTemplate.getInt(r);
    }

    public String getName(kfsRowData r) {
        return name.getString(r);
    }

    public void setName(kfsRowData r, String newName) {
        name.setString(newName, r);
    }

    public int getFirstNodeId(kfsRowData r) {
        return firstNodeId.getInt(r);
    }

    public void setFirstNodeId(kfsRowData r, int fist) {
        firstNodeId.setInt(fist, r);
    }

    public Date getLastChange(kfsRowData r) {
        return lastChange.getDate(r);
    }

    public void setLastChange(kfsRowData r, Date lch) {
        lastChange.setDate(lch, r);
    }

    public Date getArchivedDate(kfsRowData r) {
        return archivedDate.getDate(r);
    }

    public void setArchivedDate(kfsRowData r, Date lch) {
        archivedDate.setDate(lch, r);
    }
    
    public String sqlGetTaskById() {
        return getSelect(getName(), getColumns(), new kfsDbiColumn[] {id});
    }
    
    public void psGetTaskById(PreparedStatement ps, int taskId) throws SQLException {
        ps.setInt(1, taskId);
    }
    
    public String getOwnerLogin(kfsRowData r) {
        return this.ownerLogin.getString(r);
    }
    
    public void setOwnerLogin(kfsRowData r, String ownerLogin) {
        this.ownerLogin.setString(ownerLogin, r);
    }
}
