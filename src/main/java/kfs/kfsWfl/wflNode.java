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

    public String sqlSelectByItemId() {
        return getSelect(getName(), getColumns(), new kfsDbiColumn[]{idItem});
    }

    public void psSelectByItemId(PreparedStatement ps, int itemId) throws SQLException {
        ps.setInt(1, itemId);
    }

    @Override
    public kfsIPojoObj getPojo(kfsRowData row) {
        return new pojo(row);
    }

    public class pojo extends kfsPojoObj<wflNode> {

        public pojo(kfsRowData row) {
            super(wflNode.this, row);
        }

        public int getId() {
            return inx.id.getData(rd);
        }

        public Integer getIdItem() {
            return inx.idItem.getData(rd);
        }

        public String getName() {
            return inx.name.getData(rd);
        }

        public void setName(String name) {
            inx.name.setData(name, rd);
        }

        public Date getStartDate() {
            return inx.startDate.getData(rd);
        }

        public void setStartDate(Date startDate) {
            inx.startDate.setData(startDate, rd);
        }

        public Date getEndDate() {
            return inx.endDate.getData(rd);
        }

        public void setEndDate(Date endDate) {
            inx.endDate.setData(endDate, rd);
        }

        public Integer getLimitEnd() {
            return inx.limitEnd.getData(rd);
        }

        public void setLimitEnd(Integer limitEnd) {
            inx.limitEnd.setData(limitEnd, rd);
        }

        public Integer getLimitWarning() {
            return inx.limitWarning.getData(rd);
        }

        public void setLimitWarning(Integer limitWarning) {
            inx.limitWarning.setData(limitWarning, rd);
        }

        public Integer getRoleId() {
            return inx.roleId.getData(rd);
        }

        public void setRoleId(Integer roleId) {
            inx.roleId.setData(roleId, rd);
        }

        public String getUserLogin() {
            return inx.userLogin.getData(rd);
        }

        public void setUserLogin(String userLogin) {
            inx.userLogin.setData(userLogin, rd);
        }
    }
}
