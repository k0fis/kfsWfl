package kfs.kfsWfl;

import kfs.kfsDbi.*;

/**
 *
 * @author pavedrim
 */
public class wflUserRole extends kfsDbObject {

    private final kfsInt roleId;
    private final kfsString userLogin;

    public wflUserRole(final kfsDbServerType serverType) {
        super(serverType, "WFL_USER_ROLE");
        int pos = 0;
        roleId = new kfsInt("ROLE_ID", "RoleId", kfsIntAutoInc.idMaxLen, pos++, false);
        userLogin = new kfsString("USER_LOGIN", "UserLogin", wflUser.loginLen, pos++);
        super.setColumns(new kfsDbiColumn[]{roleId, userLogin});
        super.setIdsColumns(new kfsDbiColumn[]{roleId, userLogin});
    }
    
    public kfsRowData create(int roleId, String userLogin) {
        kfsRowData r = new kfsRowData(this);
        this.roleId.setInt(roleId, r);
        this.userLogin.setString(userLogin, r);
        return r;
    }

    @Override
    public kfsIPojoObj getPojo(kfsRowData row) {
        return new pojo(row);
    }
    
    public class pojo extends kfsPojoObj<wflUserRole> {
        
        pojo(kfsRowData row) {
            super(wflUserRole.this, row);
        }
        
        public int getId() {
            return inx.roleId.getData(rd);
        }
        
        public String getUserLogin() {
            return inx.userLogin.getData(rd);
        }
        
    }    
}
