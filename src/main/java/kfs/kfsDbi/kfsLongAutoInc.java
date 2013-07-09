package kfs.kfsDbi;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 *
 * @author pavedrim
 */
public class kfsLongAutoInc extends kfsLong {

    public static final int idMaxLen = 18;
    private boolean sequenceCycle = false;
    
    public kfsLongAutoInc(final String name, final int position) {
        super(name, name, idMaxLen, position, false);
    }
    
    @Override
    public String getColumnCreateTable(kfsDbServerType serverType) {
        switch (serverType) {
            case kfsDbiOracle:
                return super.getColumnName() + " NUMBER("+super.getColumnMaxLength()+") PRIMARY KEY ENABLE ";
            case kfsDbiMysql:
                return super.getColumnName() + " BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY ";
            case kfsDbiPostgre:
                return super.getColumnName() + " BIGSERIAL UNIQUE ";
            case kfsDbiSqlite:
                return super.getColumnName() + " INTEGER PRIMARY KEY AUTOINCREMENT ";
        }
        return super.getColumnCreateTable(serverType);
    }    

    public boolean isSequenceCycle() {
        return sequenceCycle;
    }
    
    public kfsLongAutoInc setsequenceCycle(boolean newVal) {
        this.sequenceCycle = newVal;
        return this;
    }
    
    @Override
    public String []getCreateTableAddons(kfsDbServerType serverType, String table_name) {
        if (serverType == kfsDbServerType.kfsDbiOracle) {
        return new String[] {
            "create sequence "+ table_name + "_"+ this.getColumnName() +"_seq start with 1 increment by 1 maxvalue 999999999999999999 " + (sequenceCycle?"CYCLE":"NOCYCLE"),
            "create trigger "+ table_name + "_"+ this.getColumnName() +"_triger before insert on "+table_name+" for each row begin select "+ table_name + "_"+ this.getColumnName() +"_seq.nextval into :new."+ this.getColumnName() +" from dual; end;"
        };
        }
         return null;
    }

    @Override
    public void setParam(int inx, PreparedStatement ps, kfsRowData row) throws SQLException {
        Long d = getLong(row);
        if ((d == null) || (d < 0)) {
            ps.setNull(inx, java.sql.Types.INTEGER);
        }
        super.setParam(inx, ps, row);
    }

}
