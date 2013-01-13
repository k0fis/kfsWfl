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
import kfs.kfsDbi.kfsDbObject;
import kfs.kfsDbi.kfsDbServerType;
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
    private final ArrayList<kfsDbObject> lst;

    public wflDb(final String schema, final kfsDbServerType serverType, final Connection conn) {
        this(schema, serverType, conn, null, null, null, null, null, null);
    }

    public wflDb(final String schema, final kfsDbServerType serverType, final Connection conn,
            final wflEdge dbEdge, final wflTask dbTask, final wflNode dbNode,
            final wflNote dbNote, final wflFile dbFile, final wflUser dbUser) {
        super(schema, serverType, conn);
        this.lst = new ArrayList<kfsDbObject>();
        this.dbEdge = (dbEdge != null) ? dbEdge : new wflEdge(serverType);
        this.dbTask = (dbTask != null) ? dbTask : new wflTask(serverType);
        this.dbNode = (dbNode != null) ? dbNode : new wflNode(serverType);
        this.dbNote = (dbNote != null) ? dbNote : new wflNote(serverType);
        this.dbFile = (dbFile != null) ? dbFile : new wflFile(serverType);
        this.dbUser = (dbUser != null) ? dbUser : new wflUser(serverType);
        this.lst.addAll(Arrays.<kfsDbObject>asList(this.dbEdge, this.dbTask, this.dbNode, //
                this.dbNote, this.dbFile, this.dbUser));
        //super.createTables();
    }

    protected void addToDboList(Collection<kfsDbObject> lst) {
        this.lst.addAll(lst);
    }

    @Override
    protected Collection<kfsDbObject> getDbObjects() {
        return lst;
    }

    // EDGE
    public boolean deleteEdge(wflEdge.pojo r) {
        return super.delete(dbEdge, r.kfsGetRow());
    }

    public wflEdge.pojo createEdge(wflTask.pojo item, wflNode.pojo fromNode, wflNode.pojo toNode) {
        kfsRowData ret = dbEdge.create(item.getId(), fromNode.getId(), toNode.getId());
        super.insert(dbEdge, ret);
        return (wflEdge.pojo) dbEdge.getPojo(ret);
    }

    public ArrayList<wflEdge.pojo> loadEdgesByItem(wflTask.pojo item) {
        final ArrayList<wflEdge.pojo> ret = new ArrayList<wflEdge.pojo>();
        try {
            PreparedStatement ps = prepare(dbEdge.sqlSelectByItemId());
            ps.clearParameters();
            dbEdge.psSelectByItemId(ps, item.getId());
            super.loadCust(ps, new loadCB() {

                @Override
                public boolean kfsDbAddItem(kfsRowData rd) {
                    ret.add((wflEdge.pojo) dbEdge.getPojo(rd));
                    return true;
                }
            }, dbEdge);
        } catch (SQLException ex) {
            l.log(Level.SEVERE, "Error in loadEdgesByItem", ex);
        }
        return ret;
    }

    // FILE
    public wflFile.pojo createFile(wflNode.pojo node, String name, wflUser.pojo user, byte[] data) {
        return (wflFile.pojo) dbFile.getPojo(dbFile.create(node.getId(), name, user.getLogin(), data));
    }

    // Task
    public wflTask.pojo createTask() {
        wflTask.pojo ret = (wflTask.pojo) dbTask.getPojo(dbTask.create());
        ret.setLastChange(new Date());
        super.insert(dbTask, ret.kfsGetRow());
        return ret;
    }

    public wflTask.pojo createTaskByTemplate(wflTask.pojo template, wflUser.pojo owner) {
        wflTask.pojo newTask = (wflTask.pojo) dbTask.getPojo(dbTask.create(template.getId(), owner.getLogin()));
        newTask.setLastChange(new Date());
        super.insert(dbTask, newTask.kfsGetRow());
        wflNode.pojo[] tempNodes = loadNodesByItem(template).toArray(new wflNode.pojo[0]);
        wflEdge.pojo[] tempEdges = loadEdgesByItem(template).toArray(new wflEdge.pojo[0]);

        wflNode.pojo[] newNodes = new wflNode.pojo[tempNodes.length];
        wflEdge.pojo[] newEdges = new wflEdge.pojo[tempEdges.length];

        for (int i = 0; i < tempNodes.length; i++) {
            newNodes[i] = createNodeByTemplate(newTask, tempNodes[i]);
        }
        for (int i = 0; i < tempEdges.length; i++) {
            int tempToId = tempEdges[i].getTo();
            int tempFromId = tempEdges[i].getFrom();

            wflNode.pojo fromNode = null, toNode = null;
            for (int j = 0; j < tempNodes.length; j++) {
                int tempId = tempNodes[j].getId();
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
        int ft = template.getFirstNodeId();
        for (int i = 0; i < tempNodes.length; i++) {
            if (tempNodes[i].getId() == ft) {
                newTask.setFirstNodeId(newNodes[i].getId());
            }
        }
        newTask.setName(template.getName() + " (" + newTask.getId() + ")");
        super.update(dbTask, newTask.kfsGetRow());
        return newTask;
    }

    public wflTask.pojo createTask(wflTask.pojo template, String ownerLogin) {
        kfsRowData ret = dbTask.create(template.getId(), ownerLogin);
        super.insert(dbTask, ret);
        return (wflTask.pojo) dbTask.getPojo(ret);
    }

    public int updateTask(wflTask.pojo r) {
        l.log(Level.FINEST, "Update Task id: {0} ", r.getId());
        return update(dbTask, r.kfsGetRow());
    }

    public wflTask.pojo getTaskbyId(int taskId) {
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
            return (wflTask.pojo) dbTask.getPojo(ret.get(0));
        }
        return null;
    }

    public wflTask.pojo getTaskbyName(String taskName) {
        ArrayList<kfsRowData> ret = new ArrayList<kfsRowData>();
        try {
            PreparedStatement ps = prepare(dbTask.sqlGetTaskByName());
            ps.clearParameters();
            dbTask.psGetTaskByName(ps, taskName);
            loadCust(ps, ret, dbTask);
        } catch (SQLException ex) {
            l.log(Level.SEVERE, "Cannot execure getTack by Name: " + taskName, ex);
        }
        if (!ret.isEmpty()) {
            return (wflTask.pojo) dbTask.getPojo(ret.get(0));
        }
        return null;
    }

    // NODE
    public boolean deleteNode(wflNode.pojo r) {
        return super.delete(dbNode, r.kfsGetRow());
    }

    public wflNode.pojo createNodeByTemplate(wflTask.pojo item, wflNode.pojo nodeSrc) {
        wflNode.pojo ret = createNode(item, nodeSrc.getName(), nodeSrc.getUserLogin());
        ret.setLimitEnd(nodeSrc.getLimitEnd());
        ret.setLimitWarning(nodeSrc.getLimitWarning());
        super.insert(dbNode, ret.kfsGetRow());
        return ret;
    }

    public wflNode.pojo createNode(wflTask.pojo item, String name, wflUser.pojo user) {
        return createNode(item, name, user.getLogin());
    }
    
    public wflNode.pojo createNode(wflTask.pojo item, String name, String userLogin) {
        wflNode.pojo ret = (wflNode.pojo) dbNode.getPojo(dbNode.create(item.getId()));
        ret.setName(name);
        ret.setUserLogin(userLogin);
        super.insert(dbNode, ret.kfsGetRow());
        return ret;
    }

    public void updateNode(wflNode.pojo r) {
        super.update(dbNode, r.kfsGetRow());
    }

    public ArrayList<wflNode.pojo> loadNodesByItem(wflTask.pojo item) {
        final ArrayList<wflNode.pojo> ret = new ArrayList<wflNode.pojo>();
        try {
            PreparedStatement ps = prepare(dbNode.sqlSelectByItemId());
            ps.clearParameters();
            dbNode.psSelectByItemId(ps, item.getId());
            super.loadCust(ps, new loadCB() {

                @Override
                public boolean kfsDbAddItem(kfsRowData rd) {
                    ret.add((wflNode.pojo) dbNode.getPojo(rd));
                    return true;
                }
            }, dbNode);
        } catch (SQLException ex) {
            l.log(Level.SEVERE, "Error in loadNodesByItem", ex);
        }
        return ret;
    }

    /// NOTE
    public wflNote.pojo createNote(wflNode.pojo node, String text, wflUser.pojo user) {
        kfsRowData ret = dbNote.create(node.getId(), text, user.getLogin());
        super.insert(dbNote, ret);
        return (wflNote.pojo) dbNote.getPojo(ret);
    }

    public ArrayList<wflNote.pojo> loadNodeNotes(wflNode.pojo node) {
        final ArrayList<wflNote.pojo> ret = new ArrayList<wflNote.pojo>();
        try {
            PreparedStatement ps = prepare(dbNote.sqlSelectByNode());
            dbNote.psSelectByNode(ps, node.getId());
            super.loadCust(ps, new loadCB() {

                @Override
                public boolean kfsDbAddItem(kfsRowData rd) {
                    ret.add((wflNote.pojo) dbNote.getPojo(rd));
                    return true;
                }
            }, dbNote);
        } catch (SQLException ex) {
            l.log(Level.SEVERE, "Cannot proccess loadNodeNotes: " + node.getId(), ex);
        }
        return ret;
    }

    // USER
    public ArrayList<wflUser.pojo> loadAllUsers() {
        final ArrayList<wflUser.pojo> ret = new ArrayList<wflUser.pojo>();
        super.loadAll(new loadCB() {

            @Override
            public boolean kfsDbAddItem(kfsRowData rd) {
                ret.add((wflUser.pojo) dbUser.getPojo(rd));
                return true;
            }
        }, dbUser);
        return ret;
    }

    public wflUser.pojo createUsers(String login) {
        if (this.loadUserByLogin(login) == null) {
            kfsRowData r = this.dbUser.create(login);
            insert(dbUser, r);
            return (wflUser.pojo) dbUser.getPojo(r);
        }
        return null;
    }

    public void updateUser(wflUser.pojo r) {
        super.update(dbUser, r.kfsGetRow());
    }

    public wflUser.pojo loadUserByLogin(String login) {
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
            return (wflUser.pojo) dbUser.getPojo(ret.get(0));
        }

    }
}
