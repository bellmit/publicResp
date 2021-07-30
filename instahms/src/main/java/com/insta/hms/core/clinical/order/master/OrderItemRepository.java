package com.insta.hms.core.clinical.order.master;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class OrderItemRepository extends GenericRepository {

  public static final String GET_MVP_ITEM_CONDITION = " AND package_ref IS NOT NULL "
      + " AND pm.multi_visit_package = true ";

  public static final String IGNORE_MVP_ITEM_CONDITION = " AND package_ref IS NULL "
      + " AND (pm.multi_visit_package = false or pm.multi_visit_package IS NULL )";

  public static final String GET_OPERATION_ITEM_CONDITION = " AND operation_ref = ? ";

  public static final String IGNORE_OPERATION_ITEM_CONDITION = " AND operation_ref IS NULL ";

  public OrderItemRepository(String tableName) {
    super(tableName);
  }

  /**
   * Returns the item list using IN clause.
   * 
   * @param query         the query
   * @param entityIdList  the entityIdList
   * @param filterOnField the filterOnField
   * @param otherParams   the otherParams
   * @return list of basic dyna bean
   */
  public List<BasicDynaBean> getItemDetails(String query, List<Object> entityIdList,
      String filterOnField, List<Object> otherParams) {
    return getItemDetails(query, entityIdList, filterOnField, otherParams, true);
  }

  /**
   * Returns the item list using IN clause.
   * 
   * @param query         the query
   * @param entityIdList  the entityIdList
   * @param filterOnField the filterOnField
   * @param otherParams   the otherParams
   * @param appendWhere   the appendWhere
   * @return list of basic dyna bean
   */
  public List<BasicDynaBean> getItemDetails(String query, List<Object> entityIdList,
      String filterOnField, List<Object> otherParams, boolean appendWhere) {

    StringBuffer queryStr = new StringBuffer();
    List<Object> params = new ArrayList<Object>();

    if (otherParams != null && otherParams.size() > 0) {
      params.addAll(otherParams);
    }
    queryStr.append(query);
    if (entityIdList != null && entityIdList.size() > 0) {
      String[] placeholdersArr = new String[entityIdList.size()];
      Arrays.fill(placeholdersArr, "?");
      queryStr.append((appendWhere ? "WHERE " : "AND ") + filterOnField + " IN ( ")
          .append(StringUtils.arrayToCommaDelimitedString(placeholdersArr)).append(")");
      params.addAll(entityIdList);
    }

    if (params.size() > 0) {
      return DatabaseHelper.queryToDynaList(queryStr.toString(), params.toArray());
    }
    return DatabaseHelper.queryToDynaList(queryStr.toString());

  }

  /**
   * Converting List of String to List of Integer.
   * 
   * @param entityList the entityList
   * @return list
   */
  protected List<Object> convertStringToInteger(List<Object> entityList) {
    List<Object> intEntityList = new ArrayList<Object>();
    for (Object entity : entityList) {
      intEntityList.add(Integer.parseInt((String) entity));
    }
    return intEntityList;
  }

  /**
   * Gets the package or operation ref query.
   *
   * @return the package or operation ref query
   */
  public abstract String getPackageRefQuery();

  /**
   * Gets the operation ref query.
   *
   * @return the operation ref query
   */
  public abstract String getOperationRefQuery();

  /**
   * Gets the package ref orders.
   *
   * @param visitId            the visit id
   * @param prescriptionIdList the prescription id list
   * @return the package ref orders
   */
  public List<BasicDynaBean> getPackageRefOrders(String visitId, List<Integer> prescriptionIdList) {
    String refQuery = getPackageRefQuery();
    if (refQuery == null) {
      return Collections.emptyList();
    }

    StringBuilder query = new StringBuilder(refQuery);

    List<Object> params = new ArrayList<>();
    params.add(visitId);

    if (prescriptionIdList != null && !prescriptionIdList.isEmpty()) {
      String[] placeholdersArr = new String[prescriptionIdList.size()];
      Arrays.fill(placeholdersArr, "?");
      query.append(" AND package_ref IN ( ")
          .append(StringUtils.arrayToCommaDelimitedString(placeholdersArr)).append(" )");
      params.addAll(prescriptionIdList);
    }
    return DatabaseHelper.queryToDynaList(query.toString(), params.toArray());
  }

  /**
   * Gets the operation ref orders.
   *
   * @param visitId          the visit id
   * @param prescribedIdList the prescribed id list
   * @return the operation ref orders
   */
  public List<BasicDynaBean> getOperationRefOrders(String visitId, List<Integer> prescribedIdList) {
    String refQuery = getOperationRefQuery();
    if (refQuery == null) {
      return Collections.emptyList();
    }

    StringBuilder query = new StringBuilder(refQuery);

    List<Object> params = new ArrayList<>();
    params.add(visitId);

    if (prescribedIdList != null && !prescribedIdList.isEmpty()) {
      String[] placeholdersArr = new String[prescribedIdList.size()];
      Arrays.fill(placeholdersArr, "?");
      query.append(" AND operation_ref IN ( ")
          .append(StringUtils.arrayToCommaDelimitedString(placeholdersArr)).append(")");
      params.addAll(prescribedIdList);
    }
    return DatabaseHelper.queryToDynaList(query.toString(), params.toArray());
  }

}
