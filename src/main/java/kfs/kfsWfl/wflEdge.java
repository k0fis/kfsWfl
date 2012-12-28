package kfs.kfsWfl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import kfs.kfsDbi.*;

/**
 *
 * @author pavedrim
 */
public class wflEdge extends kfsDbObject {

    private final kfsIntAutoInc id;
    private final kfsInt itemId;
    private final kfsInt from;
    private final kfsInt to;

    public wflEdge(final kfsDbServerType serverType) {
        super(serverType, "T_WFL_EDGE");
        int pos = 0;
        id = new kfsIntAutoInc("ID", "Id", pos++);
        itemId = new kfsInt("ITEM_ID", "Item Id", kfsIntAutoInc.idMaxLen, pos++, false);
        from = new kfsInt("E_FROM", "From Id", kfsIntAutoInc.idMaxLen, pos++, false);
        to = new kfsInt("E_TO", "To Id", kfsIntAutoInc.idMaxLen, pos++, false);
        super.setColumns(new kfsDbiColumn[]{id, itemId, from, to});
        super.setIdsColumns(new kfsDbiColumn[]{id});
    }

    public kfsRowData create(int itemId, int fromId, int toId) {
        kfsRowData ret = new kfsRowData(this);
        this.itemId.setInt(itemId, ret);
        to.setInt(toId, ret);
        from.setInt(fromId, ret);
        return ret;
    }

    public int getId(kfsRowData r) {
        return id.getInt(r);
    }

    public int getItemId(kfsRowData r) {
        return itemId.getInt(r);
    }

    public int getToId(kfsRowData r) {
        return to.getInt(r);
    }

    public int getFromId(kfsRowData r) {
        return from.getInt(r);
    }

    public String sqlSelectByItemId() {
        return getSelect(getName(), getColumns(), new kfsDbiColumn[]{itemId});
    }

    public void psSelectByItemId(PreparedStatement ps, int itemId) throws SQLException {
        ps.setInt(1, itemId);
    }
}
