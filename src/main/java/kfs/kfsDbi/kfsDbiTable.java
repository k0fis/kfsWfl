package kfs.kfsDbi;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author pavedrim
 */
public interface kfsDbiTable {
    String getName();
    String getLabel();
    String getInsertInto();
    String getInsertIntoAll();
    String getCreateTable();
    String []getCreateTableAddons();
    String getSelect();
    String getDelete();
    @Deprecated
    String getExistItemSelect();
    String []getAutoGeneratedColumnNames();
    String getUpdate();

    void psInsertSetParameters(PreparedStatement ps, kfsRowData row) throws SQLException;
    void psInsertAllSetParameters(PreparedStatement ps, kfsRowData row) throws SQLException;
    @Deprecated
    void psExistItemSetParameters(PreparedStatement ps, kfsRowData row) throws SQLException;
    void psSelectGetParameters(ResultSet ps, kfsRowData row) throws SQLException;
    void psInsertGetAutoKeys(ResultSet ps, kfsRowData row) throws SQLException;
    void psSetUpdate(PreparedStatement ps, kfsRowData row) throws SQLException;
    void psSetDelete(PreparedStatement ps, kfsRowData row) throws SQLException;
    String sqlMerge();
    void psMerge(PreparedStatement ps, kfsRowData row) throws SQLException;

    boolean hasGenerateAutoKeys();
    void sort(kfsDbiColumnComparator []cols, List<kfsRowData> inp);
    
    void psFullTextSearch(PreparedStatement ps, String fnd) throws SQLException;
    String sqlFullTextSearch();
    String createFullTextIndex();
}
