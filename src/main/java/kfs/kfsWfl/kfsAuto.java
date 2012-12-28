package kfs.kfsWfl;

import java.util.Calendar;
import java.util.Date;
import kfs.kfsDbi.kfsRowData;

/**
 *
 * @author pavedrim
 */
public class kfsAuto {

    private final wflDb db;
    private final Calendar cal;

    public kfsAuto(final wflDb db) {
        this.db = db;
        cal = Calendar.getInstance();
    }

    public void checkTask(Date date, kfsTask task) {
        for (kfsRowData node : task.getActualNodesRowData()) {
            Date sd = db.dbNode.getStartDate(node);
            if (sd != null) {
                Integer lw = db.dbNode.getLimitWarning(node);
                Integer le = db.dbNode.getLimitEnd(node);
                if (lw > 0) {
                    cal.setTime(sd);
                    cal.add(Calendar.DAY_OF_YEAR, lw);
                    if (cal.getTime().after(date)) {
                        // send expire warning
                        //task.get
                    }
                }
                if (le > 0) {
                    cal.setTime(sd);
                    cal.add(Calendar.DAY_OF_YEAR, le);
                    if (cal.getTime().after(date)) {
                        // send expire end
                    }

                }
            }
        }
    }
}
