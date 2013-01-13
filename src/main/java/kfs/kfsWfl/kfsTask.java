package kfs.kfsWfl;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Create template Create instance Start instance Add notes, files for node
 *
 *
 * @author pavedrim
 */
public class kfsTask {

    private static final Logger l = Logger.getLogger(kfsTask.class.getName());
    private ArrayList<wflNode.pojo> nodes;
    private ArrayList<wflEdge.pojo> edges;
    private final wflTask.pojo r;
    private final wflDb db;

    public kfsTask(final wflDb db, final int taskId) {
        this(db, db.getTaskbyId(taskId));
    }

    public kfsTask(final wflDb db, final wflTask.pojo r) {
        this.db = db;
        this.r = r;
        this.edges = this.db.loadEdgesByItem(r);
        this.nodes = this.db.loadNodesByItem(r);
    }

    public void setChangeFlagNow() {
        r.setLastChange(new Date());
        db.updateTask(r);
    }

    public wflNode.pojo getNodeById(int id) {
        for (wflNode.pojo node : nodes) {
            if (node.getId() == id) {
                return node;
            }
        }
        return null;
    }

    public wflNode.pojo getFirstNode() {
        return getNodeById(r.getFirstNodeId());
    }

    public List<wflNode.pojo> getNextNodes(wflNode.pojo from) {
        ArrayList<wflNode.pojo> ret = new ArrayList<wflNode.pojo>();
        for (wflEdge.pojo e : edges) {
            if (e.getFrom() == from.getId()) {
                ret.add(getNodeById(e.getTo()));
            }
        }
        return ret;
    }

    public List<wflNode.pojo> getActualNodes() {
        ArrayList<wflNode.pojo> ret1 = new ArrayList<wflNode.pojo>();
        ArrayDeque<wflNode.pojo> idNodes = new ArrayDeque<wflNode.pojo>();
        idNodes.add(getNodeById(r.getFirstNodeId()));
        while (!idNodes.isEmpty()) {
            wflNode.pojo ee = idNodes.pop();
            if (ee.getEndDate() == null) {
                ret1.add(ee);
            } else {
                idNodes.addAll(getNextNodes(ee));
            }
        }
        return ret1;
    }

    public String getName() {
        return r.getName();
    }

    public void setName(String name) {
        r.setName(name);
        r.setLastChange(new Date());
        db.updateTask(r);
    }

    public void finishNode(wflNode.pojo node) {
        Date d = new Date();
        for (wflNode.pojo rr : nodes) {
            if (rr.getId() == node.getId()) {
                rr.setEndDate(d);
                db.updateNode(rr);
                r.setLastChange(d);
                db.updateTask(r);
                break;
            }
        }
        List<wflNode.pojo> nnl = getNextNodes(node);
        for (wflNode.pojo ni : nnl) {
            startNode(ni, d);
        }
        /*
        if (nnl.size() <= 0) {
            // end task
        }
        */
    }

    public kfsTask startTask(wflUser.pojo owner) {
        int templateId = r.getTemplateId();
        l.log(Level.INFO, "StartTask, template id: {0}", templateId);
        if (templateId > 0) {
            l.info("start node");
            startNode(getNodeById(r.getFirstNodeId()), new Date());
            return this;
        } else {
            l.info("Create new Instance");
            kfsTask ret = createInstance(owner);
            ret.startTask(owner);
            return ret;
        }
    }

    public void startNode(wflNode.pojo node, Date d) {
        node.setStartDate(d);
        db.updateNode(node);
    }

    public void addNote(wflNode.pojo node, String text, wflUser.pojo user) {
        db.createNote(node, text, user);
        setChangeFlagNow();
    }

    public wflNode.pojo addNode(String name, wflUser.pojo user) {
        wflNode.pojo nn = db.createNode(r, name, user);
        nodes.add(nn);
        return nn;
    }

    public void setStartNode(wflNode.pojo node) {
        r.setFirstNodeId(node.getId());
        db.updateTask(r);
    }

    public wflEdge.pojo addEdge(wflNode.pojo fromNode, wflNode.pojo toNode) {
        wflEdge.pojo ret = db.createEdge(r, fromNode, toNode);
        edges.add(ret);
        return ret;
    }

    public kfsTask createInstance(wflUser.pojo owner) {
        wflTask.pojo rr = r;
        while (rr.getTemplateId() > 0) {
            rr = db.getTaskbyId(rr.getTemplateId());
        }
        return new kfsTask(db, db.createTaskByTemplate(rr, owner));
    }

    public String getOnwerMail() {
        return getUserMail(r.getOwnerLogin());
    }

    public String getNodeOwnerMail(wflNode.pojo node) {
        return getUserMail(node.getUserLogin());
    }

    public String getUserMail(String userMail) {
        if (userMail == null) {
            return "";
        }
        wflUser.pojo user = db.loadUserByLogin(userMail);
        if (user == null) {
            return "";
        }
        return user.getMail();
    }
}
