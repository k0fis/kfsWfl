package kfs.kfsWfl;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import kfs.kfsDbi.kfsRowData;

/**
 * Create template Create instance Start instance Add notes, files for node
 *
 *
 * @author pavedrim
 */
public class kfsTask {

    private static final Logger l = Logger.getLogger(kfsTask.class.getName());
    
    private ArrayList<kfsRowData> nodes;
    private ArrayList<kfsRowData> edges;
    private final kfsRowData r;
    private final wflDb db;

    public kfsTask(final wflDb db, final int taskId) {
        this(db, db.getTaskbyId(taskId));
    }
    public kfsTask(final wflDb db, final kfsRowData r) {
        this.db = db;
        this.r = r;
        this.edges = this.db.loadEdgesByItem(r);
        this.nodes = this.db.loadNodesByItem(r);
    }

    public void setChangeFlagNow() {
        db.dbTask.setLastChange(r, new Date());
        db.updateTask(r);
    }

    public kfsRowData getNodeById(int id) {
        for (kfsRowData node : nodes) {
            if (db.dbNode.getId(node) == id) {
                return node;
            }
        }
        return null;
    }

    public kfsRowData getFirstNode() {
        return getNodeById(getFirstNodeId());
    }

    public int getFirstNodeId() {
        return db.dbTask.getFirstNodeId(r);
    }

    public List<Integer> getNextNodeIdByNodeId(int fromId) {
        ArrayList<Integer> ret = new ArrayList<Integer>();
        for (kfsRowData e : edges) {
            if (db.dbEdge.getFromId(e) == fromId) {
                ret.add(db.dbEdge.getToId(e));
            }
        }
        return ret;
    }

    public List<Integer> getActualNodeIds() {
        ArrayList<Integer> ret1 = new ArrayList<Integer>();
        ArrayDeque<Integer> idNodes = new ArrayDeque<Integer>();
        idNodes.add(db.dbTask.getFirstNodeId(r));
        while (!idNodes.isEmpty()) {
            int id = idNodes.pop();
            if (db.dbNode.getEndDate(getNodeById(id)) == null) {
                ret1.add(id);
            } else {
                idNodes.addAll(getNextNodeIdByNodeId(id));
            }
        }
        return ret1;
    }

    public List<kfsRowData> getActualNodesRowData() {
        List<Integer> ret1 = getActualNodeIds();
        ArrayList<kfsRowData> ret = new ArrayList<kfsRowData>(ret1.size());
        for (kfsRowData n : nodes) {
            if (ret1.contains(db.dbNode.getId(n))) {
                ret.add(n);
            }
        }
        return ret;
    }

    public String getName() {
        return db.dbTask.getName(r);
    }
    
    public void setName(String name) {
        db.dbTask.setName(r, name);
        db.updateTask(r);
    }
    
    public void finishNode(kfsRowData node) {
        finishNode(db.dbNode.getId(node));
    }

    public void finishNode(int nodeId) {
        Date d = new Date();
        for (kfsRowData rr : nodes) {
            if (db.dbNode.getId(rr) == nodeId) {
                db.dbNode.setEndDate(r, d);
                db.updateNode(rr);
                db.dbTask.setLastChange(r, d);
                db.updateTask(r);
                break;
            }
        }
        for (Integer ni : getNextNodeIdByNodeId(nodeId)) {
            startnode(ni, d);
        }
    }

    public void startTask(String ownerLogin) {
        int templateId = db.dbTask.getTemplateId(r);
        l.log(Level.INFO, "StartTask, template id: {0}", templateId);
        if (templateId > 0) {
            l.info("start node");
            startnode(db.dbTask.getFirstNodeId(r), new Date());
        } else {
            l.info("Create new Instance");
            createInstance(ownerLogin).startTask(ownerLogin);
        }
    }

    public void startnode(Integer id, Date d) {
        kfsRowData node = getNodeById(id);
        db.dbNode.setStartDate(node, d);
        db.updateNode(node);
    }
    
    public void addNote(int idNode, String text, String user) {
        db.createNote(idNode, text, user);
        setChangeFlagNow();
    }
    
    /***
     * 
     * @return new node ID 
     */
    public int addNode(String name, String user) {
        kfsRowData nn = db.createNode(db.dbTask.getId(r), name, user);
        nodes.add(nn);
        return db.dbNode.getId(nn);
    }
    
    public void setStartNode(int nodeId){
        db.dbTask.setFirstNodeId(r, nodeId);
        db.updateTask(r);
    }
    
    public void addEdge(int fromNodeId, int toNodeId) {
        db.createEdge(db.dbTask.getId(r), fromNodeId, toNodeId);
    }
    
    public kfsTask createInstance(String ownerLogin) {
        kfsRowData rr = r;
        while(db.dbTask.getTemplateId(rr) > 0) {
            rr = db.getTaskbyId(db.dbTask.getTemplateId(rr));
        }
        return new kfsTask(db, db.createTaskByTemplate(rr, ownerLogin));
    }
    
    public String getOnwerMail() {
        return getUserMail(db.dbTask.getOwnerLogin(r));
    }
    
    public String getNodeOwnerMail(kfsRowData node) {
        return getUserMail(db.dbNode.getUserLogin(node));
    }
    
    public String getUserMail(String userMail) {
        if (userMail == null) {
            return "";
        }
        kfsRowData user = db.loadUserByLogin(userMail);
        if (user == null) {
            return "";
        }
        return db.dbUser.getMail(user);
    }
}
