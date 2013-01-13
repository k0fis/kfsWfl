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

    public String sqlSelectByItemId() {
        return getSelect(getName(), getColumns(), new kfsDbiColumn[]{itemId});
    }

    public void psSelectByItemId(PreparedStatement ps, int itemId) throws SQLException {
        ps.setInt(1, itemId);
    }

    @Override
    public kfsIPojoObj getPojo(kfsRowData row) {
        return new pojo(row);
    }

    public class pojo extends kfsPojoObj<wflEdge> {

        public pojo(kfsRowData row) {
            super(wflEdge.this, row);
        }

        public int getId() {
            return inx.id.getData(rd);
        }

        public Integer getItemId() {
            return inx.itemId.getData(rd);
        }

        public void setItemId(Integer itemId) {
            inx.itemId.setData(itemId, rd);
        }

        public Integer getFrom() {
            return inx.from.getData(rd);
        }

        public void setFrom(Integer from) {
            inx.from.setData(from, rd);
        }

        public Integer getTo() {
            return inx.to.getData(rd);
        }

        public void setTo(Integer to) {
            inx.to.setData(to, rd);
        }
    }
}
