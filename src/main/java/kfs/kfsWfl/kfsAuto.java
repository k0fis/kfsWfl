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
        for (wflNode.pojo node : task.getActualNodes()) {
            Date sd = node.getStartDate();
            if (sd != null) {
                Integer lw = node.getLimitWarning();
                Integer le = node.getLimitEnd();
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
