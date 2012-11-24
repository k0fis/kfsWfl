package kfs.kfsWfl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import kfs.kfsDbi.*;

/**
 *
 * @author pavedrim
 */
public class wflRole extends kfsDbObject {
    
    private final kfsIntAutoInc id;
    private final kfsString name;

    public wflRole(final kfsDbServerType serverType) {
        super(serverType, "WFL_ROLE");
        int pos = 0;
        id = new kfsIntAutoInc("ID", "Id", pos++);
        name = new kfsString("NAME", "Name", 128, pos++);
        super.setColumns(new kfsDbiColumn[]{id, name});
        super.setUpdateColumns(new kfsDbiColumn[]{name});
        super.setIdsColumns(new kfsDbiColumn[]{id});
    }

    public kfsRowData create(String name) {
        kfsRowData r = new kfsRowData(this);
        this.name.setString(name, r);
        return r;
    }
    
    @Override
    public String getExistItemSelect() {
        return getSelect(getName(), new kfsDbiColumn[]{name}, new kfsDbiColumn[]{name});
    }

    @Override
    public void psExistItemSetParameters(PreparedStatement ps, kfsRowData row) throws SQLException {
        name.setParam(1, ps, row);
    }
    
    public int getId(kfsRowData r) {
        return id.getInt(r);
    }
    
    public String getName(kfsRowData r) {
        return name.getString(r);
    }
    
    public void setName(kfsRowData r, String name) {
        this.name.setString(name, r);
    }
    
    public String sqlSelectById() {
        return getSelect(getName(), super.getColumns(), new kfsDbiColumn[] {id});
    }
    public void psSelectById(PreparedStatement ps, int id) throws SQLException {
        ps.setInt(1, id);
    }

}
