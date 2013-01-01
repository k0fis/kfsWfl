package kfs.kfsWfl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import kfs.kfsDbi.*;

/**
 *
 * @author pavedrim
 */
public class wflNode extends kfsDbObject {

    private final kfsIntAutoInc id;
    private final kfsInt idItem;
    private final kfsString name;
    private final kfsDate startDate;
    private final kfsDate endDate;
    private final kfsInt limitEnd;
    private final kfsInt limitWarning;
    private final kfsInt roleId;
    private final kfsString userLogin;

    public wflNode(final kfsDbServerType serverType) {
        super(serverType, "T_WFL_NODE");
        int pos = 0;
        id = new kfsIntAutoInc("ID", "Id", pos++);
        idItem = new kfsInt("ID_ITEM", "Item ID", kfsIntAutoInc.idMaxLen, pos++, false);
        name = new kfsString("NAME", "Name", 48, pos++);
        startDate = new kfsDate("START_DATE", "Start", pos++);
        endDate = new kfsDate("END_DATE", "End", pos++);
        limitEnd = new kfsInt("LIMIT_END", "Limit to End", 4, pos++, true);
        limitWarning = new kfsInt("LIMIT_WAR", "Limit to Warning", 4, pos++, true);
        roleId = new kfsInt("ROLE_ID", "RoleId", kfsIntAutoInc.idMaxLen, pos++, true);
        userLogin = new kfsString("USER_NAME", "Řešitel", wflUser.loginLen, pos++);

        super.setColumns(new kfsDbiColumn[]{id, idItem, name, startDate, endDate, limitEnd, limitWarning, roleId, userLogin});
        super.setUpdateColumns(new kfsDbiColumn[]{name, startDate, endDate, limitEnd, limitWarning, roleId, userLogin});
        super.setIdsColumns(new kfsDbiColumn[]{id});
    }
    
    public kfsRowData create(int idItem) {
        kfsRowData ret = new kfsRowData(this);
        this.idItem.setInt(idItem, ret);
        return ret;
    }

    public int getId(kfsRowData r) {
        return id.getInt(r);
    }

    public int getIdItem(kfsRowData r) {
        return idItem.getInt(r);
    }

    public String getName(kfsRowData r) {
        return name.getString(r);
    }

    public void setName(kfsRowData r, String newName) {
        name.setString(newName, r);
    }

    public Date getStartDate(kfsRowData r) {
        return startDate.getDate(r);
    }

    public void setStartDate(kfsRowData r, Date d) {
        startDate.setDate(d, r);
    }

    public Date getEndDate(kfsRowData r) {
        return endDate.getDate(r);
    }

    public void setEndDate(kfsRowData r, Date d) {
        endDate.setDate(d, r);
    }

    public int getLimitEnd(kfsRowData r) {
        return limitEnd.getInt(r);
    }

    public void setLimitEnd(kfsRowData r, int limit) {
        limitEnd.setInt(limit, r);
    }

    public int getLimitWarning(kfsRowData r) {
        return limitWarning.getInt(r);
    }

    public void setLimitWarning(kfsRowData r, int limit) {
        limitWarning.setInt(limit, r);
    }
    
    public int getRoleId(kfsRowData r) {
        return roleId.getInt(r);
    } 
    
    public void setRoleId(kfsRowData r, int rolId) {
        this.roleId.setInt(rolId, r);
    }

    public String getUserLogin(kfsRowData r) {
        return userLogin.getString(r);
    }

    public void setUserLogin(kfsRowData r, String newLogin) {
        userLogin.setString(newLogin, r);
    }
    
    public String sqlSelectByItemId() {
        return getSelect(getName(), getColumns(), new kfsDbiColumn[]{ idItem});
    }
    
    public void psSelectByItemId(PreparedStatement ps, int itemId) throws SQLException {
        ps.setInt(1, itemId);
    }
}
