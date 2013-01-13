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

    public String sqlGetTaskById() {
        return getSelect(getName(), getColumns(), new kfsDbiColumn[]{id});
    }
    public void psGetTaskById(PreparedStatement ps, int taskId) throws SQLException {
        ps.setInt(1, taskId);
    }
    
    public String sqlGetTaskByName() {
        return getSelect(getName(), getColumns(), new kfsDbiColumn[]{name});
    }

    public void psGetTaskByName(PreparedStatement ps, String taskName) throws SQLException {
        ps.setString(1, taskName);
    }

    public String sqlGetAllTemplates() {
        return getSelect(getName(), getColumns(), new kfsDbiColumn[]{idTemplate});
    }
    public void psGetAllTemplates(PreparedStatement ps) throws SQLException {
        ps.setNull(1, java.sql.Types.INTEGER);
    }
    
    @Override
    public kfsIPojoObj getPojo(kfsRowData row) {
        return new pojo(row);
    }

    
    public class pojo extends kfsPojoObj<wflTask> {

        public pojo(kfsRowData rd) {
            super(wflTask.this, rd);
        }

        public int getId() {
            return id.getInt(rd);
        }

        public Integer getTemplateId() {
            return idTemplate.getInt(rd);
        }

        public String getName() {
            return name.getString(rd);
        }

        public void setName(String newName) {
            name.setString(newName, rd);
        }

        public int getFirstNodeId() {
            return firstNodeId.getInt(rd);
        }

        public void setFirstNodeId(int fist) {
            firstNodeId.setInt(fist, rd);
        }

        public Date getLastChange() {
            return lastChange.getDate(rd);
        }

        public void setLastChange( Date lch) {
            lastChange.setDate(lch, rd);
        }

        public Date getArchivedDate() {
            return archivedDate.getDate(rd);
        }

        public void setArchivedDate(Date lch) {
            archivedDate.setDate(lch, rd);
        }

        public String getOwnerLogin() {
            return ownerLogin.getString(rd);
        }

        public void setOwnerLogin(String owner) {
            ownerLogin.setString(owner, rd);
        }
    }
}
