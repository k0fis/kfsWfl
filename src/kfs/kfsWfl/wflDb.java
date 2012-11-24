package kfs.kfsWfl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;
import kfs.kfsDbi.kfsADb;
import kfs.kfsDbi.kfsDbServerType;
import kfs.kfsDbi.kfsDbiTable;
import kfs.kfsDbi.kfsRowData;

/**
 *
 * wflItem - wflNode - wflEdge - wflNote - wflFile
 *
 * @author pavedrim
 */
public class wflDb extends kfsADb {

    public final wflEdge dbEdge;
    public final wflFile dbFile;
    public final wflTask dbTask;
    public final wflNode dbNode;
    public final wflNote dbNote;
    public final wflUser dbUser;
    private final Collection<kfsDbiTable> lst;

    public wflDb(final String schema, final kfsDbServerType serverType, final Connection conn) {
        this(schema, serverType, conn, null, null, null, null, null, null);
    }

    public wflDb(final String schema, final kfsDbServerType serverType, final Connection conn,
            final wflEdge dbEdge, final wflTask dbTask, final wflNode dbNode,
            final wflNote dbNote, final wflFile dbFile, final wflUser dbUser) {
        super(schema, serverType, conn);
        this.dbEdge = (dbEdge != null) ? dbEdge : new wflEdge(serverType);
        this.dbTask = (dbTask != null) ? dbTask : new wflTask(serverType);
        this.dbNode = (dbNode != null) ? dbNode : new wflNode(serverType);
        this.dbNote = (dbNote != null) ? dbNote : new wflNote(serverType);
        this.dbFile = (dbFile != null) ? dbFile : new wflFile(serverType);
        this.dbUser = (dbUser != null) ? dbUser : new wflUser(serverType);
        this.lst = Arrays.<kfsDbiTable>asList(this.dbEdge, this.dbTask, this.dbNode, this.dbNote, this.dbFile, this.dbUser);
        super.reCreateTables();
    }

    @Override
    protected Collection<kfsDbiTable> getDbObjects() {
        return lst;
    }

    // EDGE
    public boolean deleteEdge(kfsRowData r) {
        return super.delete(dbEdge, r);
    }

    public kfsRowData createEdge(int itemId, int fromNodeId, int toNodeId) {
        kfsRowData ret = dbEdge.create(itemId, fromNodeId, toNodeId);
        super.insert(dbEdge, ret);
        return ret;
    }

    public kfsRowData createEdge(kfsRowData item, kfsRowData fromNode, kfsRowData toNode) {
        return createEdge(dbTask.getId(item), dbNode.getId(fromNode), dbNode.getId(toNode));
    }

    public ArrayList<kfsRowData> loadEdgesByItem(kfsRowData item) {
        return loadEdgesByItem(dbTask.getId(item));
    }

    public ArrayList<kfsRowData> loadEdgesByItem(int itemId) {
        ArrayList<kfsRowData> ret = new ArrayList<kfsRowData>();
        try {
            PreparedStatement ps = prepare(dbEdge.sqlSelectByItemId());
            ps.clearParameters();
            dbEdge.psSelectByItemId(ps, itemId);
            super.loadCust(ps, ret, dbEdge);
        } catch (SQLException ex) {
            l.log(Level.SEVERE, "Error in loadEdgesByItem", ex);
        }
        return ret;
    }

    // FILE
    public kfsRowData createFile(int idNode, String name, String user, byte [] data) {
        return dbFile.create(idNode, name, user, data);
    }

    // Task
    public kfsRowData createTask() {
        kfsRowData ret = dbTask.create();
        dbTask.setLastChange(ret, new Date());
        super.insert(dbTask, ret);
        return ret;
    }

    public kfsRowData createTaskByTemplate(kfsRowData templateTask, String ownerLogin) {
        kfsRowData newTask = dbTask.create(dbTask.getId(templateTask), ownerLogin);
        dbTask.setLastChange(newTask, new Date());
        super.insert(dbTask, newTask);
        kfsRowData[] tempNodes = loadNodesByItem(templateTask).toArray(new kfsRowData[0]);
        kfsRowData[] tempEdges = loadEdgesByItem(templateTask).toArray(new kfsRowData[0]);

        kfsRowData[] newNodes = new kfsRowData[tempNodes.length];
        kfsRowData[] newEdges = new kfsRowData[tempEdges.length];

        for (int i = 0; i < tempNodes.length; i++) {
            newNodes[i] = createNodeByTemplate(newTask, tempNodes[i]);
        }
        for (int i = 0; i < tempEdges.length; i++) {
            int tempToId = dbEdge.getToId(tempEdges[i]);
            int tempFromId = dbEdge.getFromId(tempEdges[i]);

            kfsRowData fromNode = null, toNode = null;
            for (int j = 0; j < tempNodes.length; j++) {
                int tempId = dbNode.getId(tempNodes[j]);
                if (tempToId == tempId) {
                    toNode = newNodes[j];
                }
                if (tempFromId == tempId) {
                    fromNode = newNodes[j];
                }
                if ((fromNode != null) && (toNode != null)) {
                    break;
                }
            }
            if ((fromNode == null) || (toNode == null)) {
                throw new RuntimeException("Cannot copy TemplateItem, inconsistence");
            }
            newEdges[i] = createEdge(newTask, fromNode, toNode);
        }
        // set first node
        int ft = dbTask.getFirstNodeId(templateTask);
        for (int i = 0; i < tempNodes.length; i++) {
            if (dbNode.getId(tempNodes[i]) == ft) {
                dbTask.setFirstNodeId(newTask, dbNode.getId(newNodes[i]));
            }
        }
        dbTask.setName(newTask, dbTask.getName(templateTask) + " (" + dbTask.getId(newTask) + ")");
        super.update(dbTask, newTask);
        return newTask;
    }

    public kfsRowData createTask(int templateId, String ownerLogin) {
        kfsRowData ret = dbTask.create(templateId, ownerLogin);
        super.insert(dbTask, ret);
        return ret;
    }

    public int updateTask(kfsRowData r) {
        l.log(Level.FINEST, "Update Task id: {0} ", dbTask.getId(r));
        return update(dbTask, r);
    }

    public kfsRowData getTaskbyId(int taskId) {
        ArrayList<kfsRowData> ret = new ArrayList<kfsRowData>();
        try {
            PreparedStatement ps = prepare(dbTask.sqlGetTaskById());
            ps.clearParameters();
            dbTask.psGetTaskById(ps, taskId);
            loadCust(ps, ret, dbTask);
        } catch (SQLException ex) {
            l.log(Level.SEVERE, "Cannot execure getTack by Id: " + taskId, ex);
        }
        if (!ret.isEmpty()) {
            return ret.get(0);
        }
        return null;
    }

    // NODE
    public boolean deleteNode(kfsRowData r) {
        return super.delete(dbNode, r);
    }

    public kfsRowData createNodeByTemplate(kfsRowData item, kfsRowData nodeTemp) {
        return createNodeByTemplate(dbTask.getId(item), nodeTemp);
    }

    public kfsRowData createNodeByTemplate(int idItem, kfsRowData nodeTemp) {
        kfsRowData ret = dbNode.create(idItem);
        dbNode.setName(ret, dbNode.getName(nodeTemp));
        dbNode.setLimitEnd(ret, dbNode.getLimitEnd(nodeTemp));
        dbNode.setLimitWarning(ret, dbNode.getLimitWarning(nodeTemp));
        super.insert(dbNode, ret);
        return ret;
    }

    public kfsRowData createNode(int idItem, String name, String user) {
        kfsRowData ret = dbNode.create(idItem);
        dbNode.setName(ret, name);
        dbNode.setUserLogin(ret, user);
        super.insert(dbNode, ret);
        return ret;
    }

    public void updateNode(kfsRowData r) {
        super.update(dbNode, r);
    }

    public ArrayList<kfsRowData> loadNodesByItem(kfsRowData item) {
        return loadNodesByItem(dbTask.getId(item));
    }

    public ArrayList<kfsRowData> loadNodesByItem(int itemId) {
        ArrayList<kfsRowData> ret = new ArrayList<kfsRowData>();
        try {
            PreparedStatement ps = prepare(dbNode.sqlSelectByItemId());
            ps.clearParameters();
            dbNode.psSelectByItemId(ps, itemId);
            super.loadCust(ps, ret, dbNode);
        } catch (SQLException ex) {
            l.log(Level.SEVERE, "Error in loadNodesByItem", ex);
        }
        return ret;
    }

    /// NOTE
    public kfsRowData createNote(int idNode, String text, String user) {
        kfsRowData ret = dbNote.create(idNode, text, user);
        super.insert(dbNote, ret);
        return ret;
    }

    public ArrayList<kfsRowData> loadNodeNotes(int idNode) {
        ArrayList<kfsRowData> ret = new ArrayList<kfsRowData>();
        try {
            PreparedStatement ps = prepare(dbNote.sqlSelectByNode());
            dbNote.psSelectByNode(ps, idNode);
            super.loadCust(ps, ret, dbNote);
        } catch (SQLException ex) {
            l.log(Level.SEVERE, "Cannot proccess loadNodeNotes: " + idNode, ex);
        }
        return ret;
    }

    // USER
    public ArrayList<kfsRowData> loadAllUsers() {
        ArrayList<kfsRowData> ret = new ArrayList<kfsRowData>();
        super.loadAll(ret, dbUser);
        return ret;
    }

    public kfsRowData createUsers(String login) {
        kfsRowData r = this.dbUser.create(login);
        if (this.exist(dbUser, r) == Boolean.TRUE) {
            return null;
        } else {
            insert(dbTask, r);
            return r;
        }
    }

    public void updateUser(kfsRowData r) {
        super.update(dbUser, r);
    }

    public kfsRowData loadUserByLogin(String login) {
        ArrayList<kfsRowData> ret = new ArrayList<kfsRowData>();
        try {
            PreparedStatement ps = prepare(dbUser.sqlSelectByLogin());
            ps.clearParameters();
            dbUser.psSelectByLogin(ps, login);
            super.loadCust(ps, ret, dbUser);
        } catch (SQLException ex) {
            l.log(Level.SEVERE, "Error in getUSerByLogin", ex);
        }
        if (ret.size() <= 0) {
            return null;
        } else {
            return ret.get(0);
        }

    }
}
