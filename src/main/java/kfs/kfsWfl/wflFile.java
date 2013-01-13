package kfs.kfsWfl;

import java.io.InputStream;
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
        userLogin = new kfsString("USER_NAME", "Uživatel", wflUser.loginLen, pos++);
        data = new kfsBlob("FILE_DATA", "Data", pos++, serverType);

        super.setColumns(new kfsDbiColumn[]{id, idNode, name, date, userLogin, data});
        super.setIdsColumns(new kfsDbiColumn[]{id});
    }

    public kfsRowData create(int idNode, String name, String user, byte[] data) {
        kfsRowData ret = new kfsRowData(this);
        this.idNode.setInt(idNode, ret);
        this.name.setString(name, ret);
        this.date.setDate(new Date(), ret);
        this.userLogin.setString(user, ret);
        this.data.setData(data, ret);
        return ret;
    }

    @Override
    public kfsIPojoObj getPojo(kfsRowData row) {
        return new pojo(row);
    }

    public class pojo extends kfsPojoObj<wflFile> {

        public pojo(kfsRowData row) {
            super(wflFile.this, row);
        }

        public int getId() {
            return inx.id.getData(rd);
        }

        public Integer getIdNode() {
            return inx.idNode.getData(rd);
        }

        public void setIdNode(Integer idNode) {
            inx.idNode.setData(idNode, rd);
        }

        public String getName() {
            return inx.name.getData(rd);
        }

        public void setName(String name) {
            inx.name.setData(name, rd);
        }

        public Date getDate() {
            return inx.date.getData(rd);
        }

        public void setDate(Date date) {
            inx.date.setData(date, rd);
        }

        public String getUserLogin() {
            return inx.userLogin.getData(rd);
        }

        public void setUserLogin(String userLogin) {
            inx.userLogin.setData(userLogin, rd);
        }

        public kfsBlobData getData() {
            return inx.data.getData(rd);
        }

        public void setData(kfsBlobData data) {
            inx.data.setData(data, rd);
        }

        public void setData(byte [] data) {
            inx.data.setData(data, rd);
        }

        public void setData(InputStream data) {
            inx.data.setData(data, rd);
        }
    }
}
