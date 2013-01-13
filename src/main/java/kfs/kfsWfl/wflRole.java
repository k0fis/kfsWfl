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
        
    public String sqlSelectById() {
        return getSelect(getName(), super.getColumns(), new kfsDbiColumn[] {id});
    }
    public void psSelectById(PreparedStatement ps, int id) throws SQLException {
        ps.setInt(1, id);
    }

    @Override
    public kfsIPojoObj getPojo(kfsRowData row) {
        return new pojo(row);
    }

    public class pojo extends kfsPojoObj<wflRole> {
        pojo(kfsRowData row) {
            super(wflRole.this, row);
        }
        
        public int getId() {
            return inx.id.getData(rd);
        }
        
        public String getName() {
            return inx.name.getData(rd);
        }
        
        public void setName(String name) {
            inx.name.setData(name, rd);
        }
    }
    
}
