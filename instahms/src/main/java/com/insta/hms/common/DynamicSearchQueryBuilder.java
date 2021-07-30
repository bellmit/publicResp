package com.insta.hms.common;

import com.insta.hms.diagnosticmodule.laboratory.LaboratoryDAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extends {@link SearchQueryBuilder}.
 * <p>Constructor takes two extra arguments besides super constructor args</p>
 * 
 * <p> 1. filterParamReplacementMap - useful when converting a view into query and fields</p>
 * in where/select part need to be replaced with aliased names.
 * <p>Usage: SELECT id from table_name tn...., changes to SELECT tn.id from table_name tn....</p>
 * <p>Key: Non-aliased field name</p>
 * <p>Value: Aliased field name</p>
 * 
 * <p> 2. aliasTablesMap - useful for appending joins using addJoin method </p>
 * <p>Key: table alias</p>
 * <p>Value: table name</p>
 * 
 * <p>When both args set to null, class behaves like superclass. </p>
 * Ref: {@link LaboratoryDAO} for usage.
 * 
 * @author pranaysahota
 *
 */
public class DynamicSearchQueryBuilder extends SearchQueryBuilder {

  /** The filter param replacement map. */
  private Map<String, String> filterParamReplacementMap;
  
  /** The alias tables map. */
  private Map<String, String> aliasTablesMap;
  
  /** The join statements. */
  private List<String> joinStatements = new ArrayList<>();

  /**
   * The Enum JOIN_TYPES.
   */
  public enum JoinTypes {
    
    /** The left join. */
    LEFT_JOIN {
      public String toString() {
        return " LEFT JOIN ";
      }
    },
    
    /** The right join. */
    RIGHT_JOIN {
      public String toString() {
        return " RIGHT JOIN ";
      }
    },
    
    /** The join. */
    JOIN
  }

  /**
   * Instantiates a new dynamic search query builder.
   *
   * @param con the con
   * @param selectFields the select fields
   * @param selectCount the select count
   * @param fromTables the from tables
   * @param initWhere the init where
   * @param groupBy the group by
   * @param listing the listing
   * @param filterParamReplacementMap the filter param replacement map
   * @param aliasTablesMap the alias tables map
   */
  public DynamicSearchQueryBuilder(Connection con, String selectFields, String selectCount,
      String fromTables, String initWhere, String groupBy, Map listing,
      Map<String, String> filterParamReplacementMap, Map<String, String> aliasTablesMap) {
    super(con, selectFields, selectCount, fromTables, initWhere, groupBy, listing);
    this.filterParamReplacementMap = filterParamReplacementMap;
    this.aliasTablesMap = aliasTablesMap;
  }



  /**
   * Instantiates a new dynamic search query builder.
   *
   * @param con the con
   * @param selectFields the select fields
   * @param selectCount the select count
   * @param fromTables the from tables
   * @param initWhere the init where
   * @param groupByFields the group by fields
   * @param sortField the sort field
   * @param sortReverse the sort reverse
   * @param pageSize the page size
   * @param pageNum the page num
   * @param filterParamReplacementMap the filter param replacement map
   * @param aliasTablesMap the alias tables map
   */
  public DynamicSearchQueryBuilder(Connection con, String selectFields, String selectCount,
      String fromTables, String initWhere, String groupByFields, String sortField,
      boolean sortReverse, int pageSize, int pageNum, Map<String, String> filterParamReplacementMap,
      Map<String, String> aliasTablesMap) {
    super(con, selectFields, selectCount, fromTables, initWhere, groupByFields, sortField,
        sortReverse, pageSize, pageNum);
    this.filterParamReplacementMap = filterParamReplacementMap;
    this.aliasTablesMap = aliasTablesMap;
  }



  /**
   * Builds the Query.
   *
   * @throws SQLException the SQL exception
   */
  @Override
  public void build() throws SQLException {
    if (built) {
      return;
    }
    String selectFieldsUpdated = replaceKeys(filterParamReplacementMap, selectFields);
    dataQuery.append(selectFieldsUpdated);
    if (null != fromTables) {
      dataQuery.append(fromTables);
      if (!joinStatements.isEmpty()) {
        for (String s : joinStatements) {
          dataQuery.append(s);
        }
      }
    }

    if (initWhere != null) {
      String initWhereUpdated = replaceKeys(filterParamReplacementMap, initWhere);
      dataQuery.append(" " + initWhereUpdated + " ");
    }

    if (null != whereClause) {
      String whereClauseUpdated = replaceKeys(filterParamReplacementMap, whereClause.toString());
      dataQuery.append(whereClauseUpdated);
    }

    if (groupByFields != null) {
      String groupByFieldsUpdated = replaceKeys(filterParamReplacementMap, groupByFields);
      dataQuery.append(" GROUP BY ").append(groupByFieldsUpdated);
    }

    appendSort(dataQuery);

    if (pageSize != 0) {
      dataQuery.append(" LIMIT ?");
    }

    if (pageNum != 0) {
      dataQuery.append(" OFFSET ?");
    }

    /*
     * Build the query to get the count
     */
    if (countQuery != null) {
      String selectCountUpdated = replaceKeys(filterParamReplacementMap, selectCount);
      countQuery.append(selectCountUpdated);
      if (null != fromTables) {
        countQuery.append(fromTables);
        if (!joinStatements.isEmpty()) {
          for (String s : joinStatements) {
            countQuery.append(s);
          }
        }

      }
      if (initWhere != null) {
        String initWhereUpdated = replaceKeys(filterParamReplacementMap, initWhere);
        countQuery.append(initWhereUpdated);
      }
      String whereClauseUpdated = replaceKeys(filterParamReplacementMap, whereClause.toString());
      countQuery.append(whereClauseUpdated);
      // count query should not append the group by
    }

    dataStmt = con.prepareStatement(dataQuery.toString());
    logger.debug("Data query: " + dataQuery.toString());
    if (null != countQuery) {
      countStmt = con.prepareStatement(countQuery.toString());
      logger.debug("Count query: " + countQuery.toString());
    } else {
      logger.debug("Count query is null");
    }

    // now add the values to the statement
    int stmtIndex = 1;
    int numValues = fieldTypes.size();
    for (int i = 0; i < numValues; i++) {

      int type = (Integer) fieldTypes.get(i);
      Object value = fieldValues.get(i);
      if (value == null) {
        continue;
      }

      setTypeInStatement(dataStmt, stmtIndex, type, value);
      if (countStmt != null) {
        setTypeInStatement(countStmt, stmtIndex, type, value);
      }
      stmtIndex++;
    }

    if (pageSize != 0) {
      dataStmt.setInt(stmtIndex++, pageSize);
    }

    if (pageNum != 0) {
      dataStmt.setInt(stmtIndex++, (pageNum - 1) * pageSize);
    }

    built = true;
  }

  /**
   * Replaces variables in part of a query. Useful in a case where fields need to be replaced with
   * query specific variables
   *
   * @param filterParamReplacementMap the key replacement map
   * @param keyString the key string of a query
   * @return the original input string if map is null.
   */
  private String replaceKeys(Map filterParamReplacementMap, String keyString) {
    if (filterParamReplacementMap != null && keyString != null) {
      String keyStr = null;
      Iterator it = filterParamReplacementMap.entrySet().iterator();
      while (it.hasNext()) {
        Map.Entry entry = (Map.Entry) it.next();
        String regex = "\\b" + entry.getKey().toString() + "\\b";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(keyString);
        keyString = matcher.replaceAll(entry.getValue().toString());
      }
      keyStr = keyString;
      return keyStr;
    }
    return keyString;
  }

  /**
   * Adds the join to dataStatement.
   *
   * @param joinType the join type
   * @param fromTableAlias the from table alias
   * @param joinTableAlias the join table alias
   * @param joinClause the join clause
   */
  public void addJoin(String joinType, String fromTableAlias, String joinTableAlias,
      String joinClause) {
    StringBuilder sb = new StringBuilder();
    if (aliasTablesMap.containsKey(fromTableAlias)) {
      if (aliasTablesMap.containsKey(joinTableAlias)) {
        sb.append(joinType + " ").append(aliasTablesMap.get(joinTableAlias) + " " + joinTableAlias)
            .append(" ON ").append(" (" + joinClause + ") ");
        aliasTablesMap.remove(joinTableAlias);
      }
    }
    joinStatements.add(sb.toString());
  }
}
