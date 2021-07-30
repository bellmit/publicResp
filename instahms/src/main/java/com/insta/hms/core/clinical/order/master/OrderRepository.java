package com.insta.hms.core.clinical.order.master;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.joda.time.Period;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * OrderRepository.
 * 
 * @author ritolia
 *
 */
@Repository
public class OrderRepository {

  @LazyAutowired
  private GenericPreferencesService genericPreferencesService;

  private static final String GET_ORDERABLE_ITEM = "SELECT oi.* FROM orderable_item oi "
      + "LEFT JOIN mapping_center_id mci using(orderable_item_id) "
      + "LEFT JOIN mapping_tpa_id mti using(orderable_item_id) "
      + "LEFT JOIN mapping_org_id moi using(orderable_item_id) "
      + "LEFT JOIN mapping_plan_id mpi using(orderable_item_id) "
      + "WHERE oi.status = 'A' AND (mti.status ='A' OR mti.status IS NULL) "
      + "AND (moi.status = 'A' OR moi.status IS NULL) AND (mci.status ='A' OR mci.status IS NULL) "
      + "AND (valid_to_date >= current_date OR valid_to_date IS NULL) "
      + "AND (valid_from_date <= current_date OR valid_from_date IS NULL) ";

  private static final String[] nonDirectBillingItems = { "Laboratory", "Radiology", "Service",
      "Operation", "Package", "MultiVisitPackage", "DiagPackage", "Other Charge", "Direct Charge" };
  private static final String[] directBillingItems = { "Doctor", "Meal", "Equipment", "Bed",
      "ICU" };
  private static final String IS_MULTI_VISIT_PACKAGE = " AND (is_multi_visit_package = ?) ";
  private static final String PACKAGE_APPLICABLE = " AND package_applicable = ? ";
  private static final String OPERATION_APPLICABLE = " AND (operation_applicable = ?) ";
  private static final String ENTITY_ID_SEARCH = " AND entity_id = ? ";
  private static final String SEARCH_ITEM = " oi.orderable_item_id IN "
      + "( SELECT orderable_item_id FROM ";
  private static final String TOKEN_SUB_QUERY =
      "SELECT distinct orderable_item_id,token FROM orderable_items_tokens oit "
      + " JOIN orderable_item oi using(orderable_item_id) ";


  private Map<String, String> getClausesMap() {
    Map<String, String> clauseMap = new HashMap<>();
    clauseMap.put("center_id",
        " AND (center_id = ? OR center_id IS NULL OR center_id = '-1' OR center_id = '0') ");
    // clauseMap.put("tpa_id", " AND (tpa_id = ? OR tpa_id = '-1' OR tpa_id IS NULL) ");
    // clauseMap.put("plan_id", " AND (plan_id = ? OR plan_id = '-1' OR plan_id IS NULL) ");
    clauseMap.put("orderable", " AND (orderable = ?) ");
    clauseMap.put("service_group_id", " AND (service_group_id = ?) ");
    // clauseMap.put("service_sub_group_id", " AND (service_sub_group_id = ?) ");
    clauseMap.put("insurance_category_id", " AND (insurance_category_id = ?) ");
    clauseMap.put("visit_type", " AND (visit_type = ? OR visit_type = '*' ) ");
    clauseMap.put("gender_applicability", " AND (gender_applicability = ? "
        + "OR gender_applicability = '*' OR gender_applicability IS NULL) ");
    clauseMap.put("module_id", " AND (module_id = ?) ");
    return clauseMap;
  }

  /**
   * Returns the Orderable items based on filter passed. When packageApplicable is not defined do
   * not show doctor package, previously it was broken(fixing it). center_id 0, -1 both means all
   * center(default center) In packages -1 is used as default center, in doctor 0 is used as default
   * center.
   * 
   * @param paramsMap
   *          the paramsMap
   * @param filterList
   *          the filterList
   * @param ratePlanList
   *          the ratePlanList
   * @return list of basic dyna bean
   */
  public List<BasicDynaBean> getOrderableItems(Map<String, Object> paramsMap, String[] filterList,
      List<String> ratePlanList) {
    StringBuffer query = new StringBuffer();
    List<Object> params = new ArrayList<>();
    Map<String, String> clauseMap = getClausesMap();

    query = query.append(GET_ORDERABLE_ITEM);
    for (Entry<String, String> entry : clauseMap.entrySet()) {
      if (paramsMap.get(entry.getKey()) != null && !paramsMap.get(entry.getKey()).equals("")) {
        query.append(entry.getValue());
        params.add(paramsMap.get(entry.getKey()));
      }
    }

    boolean userIsDoctor = false;
    if (paramsMap.get("user_is_doctor") != null
        && paramsMap.get("user_is_doctor").toString().equals("Y")) {
      userIsDoctor = true;
    }

    boolean loginControlsApplicable = false;
    if (paramsMap.get("login_controls_applicable") != null
        && paramsMap.get("login_controls_applicable").toString().equals("Y")) {
      loginControlsApplicable = true;
    }

    if (userIsDoctor && loginControlsApplicable) {
      String doctorId = paramsMap.get("doctor").toString();
      if (doctorId != null) {
        query.append("AND entity_id = ? ");
        params.add(doctorId);
      }
    }
    
    List<Integer> subGrpIds = (List) paramsMap.get("order_controls_sub_groups");
    List<String> entityIds = (List) paramsMap.get("order_control_items");
    if (!subGrpIds.isEmpty()) {
      String[] placeholdersArr = new String[subGrpIds.size()];
      Arrays.fill(placeholdersArr, "?");
      if (!entityIds.isEmpty()) {
        query.append("AND ( ");
      } else {
        query.append("AND ");
      }
      query.append("service_sub_group_id IN ( ")
          .append(StringUtils.arrayToCommaDelimitedString(placeholdersArr)).append(")");
      for (Integer subGrpId : subGrpIds) {
        params.add(subGrpId);
      }
    } else {
      if (paramsMap.get("service_sub_group_id") != null
          && !paramsMap.get("service_sub_group_id").equals("")) {
        query.append("AND (service_sub_group_id = ?)");
        params.add((int) paramsMap.get("service_sub_group_id"));
      }
    }

    if (!entityIds.isEmpty()) {
      String[] placeholdersArr = new String[entityIds.size()];
      Arrays.fill(placeholdersArr, "?");
      if (!subGrpIds.isEmpty()) {
        query.append("OR entity_id IN ( ")
              .append(StringUtils.arrayToCommaDelimitedString(placeholdersArr)).append("))");
      } else {
        query.append("AND entity_id IN ( ")
              .append(StringUtils.arrayToCommaDelimitedString(placeholdersArr)).append(")");
      }
      for (String entityId : entityIds) {
        params.add(entityId);
      }
    }

    if (filterList != null && filterList.length > 0) {
      String[] placeholdersArr = new String[filterList.length];
      Arrays.fill(placeholdersArr, "?");
      query.append("AND entity IN ( ")
          .append(StringUtils.arrayToCommaDelimitedString(placeholdersArr)).append(")");
      for (String filterItem : filterList) {
        params.add(filterItem);
      }
    }

    if (!ratePlanList.isEmpty()) {
      String[] placeholdersArr = new String[ratePlanList.size()];
      Arrays.fill(placeholdersArr, "?");
      query.append("AND ( org_id IN ( ")
          .append(StringUtils.arrayToCommaDelimitedString(placeholdersArr)).append(")");
      query.append(" OR org_id IS NULL )");
      params.addAll(ratePlanList);
    }

    if (paramsMap.get("tpa_id") != null && !(paramsMap.get("tpa_id").equals(""))) {
      List<String> tpaList = new ArrayList<>();
      tpaList = (List<String>) paramsMap.get("tpa_id");
      String[] placeholdersArr = new String[tpaList.size()];
      Arrays.fill(placeholdersArr, "?");
      query.append(" AND ( tpa_id IN ( ")
          .append(StringUtils.arrayToCommaDelimitedString(placeholdersArr)).append(")");
      params.addAll(tpaList);
      query.append(" OR tpa_id = '-1' OR tpa_id IS NULL) ");
    }

    if (paramsMap.get("plan_id") != null && !(paramsMap.get("plan_id").equals(""))) {
      List<Object> planList = new ArrayList<>();
      planList = (List<Object>) paramsMap.get("plan_id");
      List<Object> intPlanList = new ArrayList<Object>();
      for (Object entity : planList) {
        intPlanList.add(Integer.parseInt((String) entity));
      }
      List<Integer> planIds = new ArrayList<>();
      String[] placeholdersArr = new String[planList.size()];
      Arrays.fill(placeholdersArr, "?");
      query.append(" AND ( plan_id IN ( ")
          .append(StringUtils.arrayToCommaDelimitedString(placeholdersArr)).append(")");
      params.addAll(intPlanList);
      query.append(" OR plan_id = '-1' OR plan_id IS NULL) ");
    }

    // age_text should be in ISO 8601 duration format
    if (org.apache.commons.lang3.StringUtils.isNotEmpty((String) paramsMap.get("age_text"))
        && Period.parse((String) paramsMap.get("age_text")) != null) {
      query.append(" AND("
          + " ( min_age is null OR max_age is null OR age_unit is null)"
          + " OR"
          + " (('P'||min_age||age_unit)::interval <=  ?::interval AND ('P'||max_age||age_unit)"
          + "::interval >=  ?::interval)"
          + " ) ");
      String ageText = paramsMap.get("age_text").toString();
      if (ageText.contains("+")) {
        ageText = ageText.replace("+", "");
      }
      params.add(ageText);
      params.add(ageText);
    }

    // Only Rate plan Independent Items are allowed if direct Billing is set
    // to yes (except Other Charge, Direct Charge).
    BasicDynaBean genericPrefs = genericPreferencesService.getPreferences();
    String opeartionApplicableFor = (String) genericPrefs.get("operation_apllicable_for");
    
    String[] placeHolderArrForTokens = null;
    List<Object> paramsForTokens = new ArrayList<>();
    
    if (paramsMap.get("direct_billing") != null && paramsMap.get("direct_billing").equals("Y")) {
      String[] placeholdersArr = new String[directBillingItems.length];
      Arrays.fill(placeholdersArr, "?");
      query.append("AND entity IN ( ")
          .append(StringUtils.arrayToCommaDelimitedString(placeholdersArr)).append(")");
      params.addAll(Arrays.asList(directBillingItems));
      placeHolderArrForTokens = placeholdersArr;
      paramsForTokens.addAll(Arrays.asList(directBillingItems));
      
    } else if (paramsMap.get("direct_billing") != null
        && paramsMap.get("direct_billing").equals("N")) {
      String[] placeholdersArr;
      params.addAll(Arrays.asList(nonDirectBillingItems));
      paramsForTokens.addAll(Arrays.asList(nonDirectBillingItems));
      
      if (paramsMap.get("visit_type") != null && paramsMap.get("visit_type").equals("o")
          && opeartionApplicableFor.equals("i")) {
        params.remove("Operation");
        paramsForTokens.remove("Operation");
        placeholdersArr = new String[nonDirectBillingItems.length - 1];
      } else {
        placeholdersArr = new String[nonDirectBillingItems.length];
      }

      Arrays.fill(placeholdersArr, "?");
      query.append("AND entity IN ( ")
          .append(StringUtils.arrayToCommaDelimitedString(placeholdersArr)).append(")");
      
      placeHolderArrForTokens = placeholdersArr;
      
    } else if (paramsMap.get("visit_type") != null && paramsMap.get("visit_type").equals("o")
        && opeartionApplicableFor.equals("i")) {
      query.append(" AND (entity != 'Operation') ");
    }

    // In case operationApplicable is both it means Y and N both. So don't
    // add parameter in this case
    if (paramsMap.get("operation_applicable") != null
        && !paramsMap.get("operation_applicable").equals("b")
        && !paramsMap.get("operation_applicable").equals("")) {
      query.append("AND " + OPERATION_APPLICABLE);
      params.add(paramsMap.get("operation_applicable"));
    }

    if (paramsMap.get("package_applicable") != null
        && !paramsMap.get("package_applicable").equals("")) {
      if (paramsMap.get("is_multi_visit_package").equals("Y")) {
        query.append(IS_MULTI_VISIT_PACKAGE);
        params.add(paramsMap.get("is_multi_visit_package"));
      } else {
        query.append(PACKAGE_APPLICABLE);
        params.add(paramsMap.get("package_applicable"));
      }
    } else {   // When package_applicable is not defined, not include doctor package

      // To bring system defined 'Doctor package' applicable only for package master
      if (org.apache.commons.lang3.StringUtils.isBlank((String) paramsMap.get(
          "doctor_package_applicability"))
          || !paramsMap.get("doctor_package_applicability").equals("Y")) {
        query.append(" AND (entity_id != 'Doctor') ");
      }
      if (paramsMap.get("is_multi_visit_package").equals("N")) {
        query.append(" AND (entity != 'MultiVisitPackage') ");
      }
    }

    if (paramsMap.get("search_id") != null && !paramsMap.get("search_id").equals("")) {
      String[] searchTerms = ((String) paramsMap.get("search_id")).trim().split(" ");
      StringBuilder searchTermQuery = new StringBuilder();
      searchTermQuery.append(SEARCH_ITEM).append('(').append(TOKEN_SUB_QUERY);
      boolean searchTermSelected = false;
      Integer searchTermCount = 0;
      for (String searchTerm : searchTerms) {
        String val = searchTerm.trim().toLowerCase();
        if (val.isEmpty() || val.matches("(\\+|-|\\(|\\)|\\[|\\])*")) {
          continue;
        }

        // First time it will be added
        if (!searchTermSelected) {
          searchTermQuery.append(" WHERE ");
        } else {
          searchTermQuery.append(" OR ");
        }

        searchTermSelected = true;
        
        if (null != placeHolderArrForTokens && placeHolderArrForTokens.length > 1 ) {
          searchTermQuery.append(" oi.entity IN (")
            .append(StringUtils.arrayToCommaDelimitedString(placeHolderArrForTokens)).append(")");
          searchTermQuery.append(" AND ");
          params.addAll(paramsForTokens);
        }
        
        searchTermQuery.append(" oit.token like ?");

        // item name words starting with searched item
        params.add(val + "%");
        searchTermCount++;
      }
      if (searchTermSelected) {
        searchTermQuery
            .append(") oit group by orderable_item_id having count(orderable_item_id) >= ?");
        params.add(searchTermCount);
        query.append("AND " + searchTermQuery.toString() + " ) ");
      }
    }

    if (paramsMap.get("item_id") != null && !paramsMap.get("item_id").equals("")) {
      query.append(ENTITY_ID_SEARCH);
      params.add(paramsMap.get("item_id"));
    }

    return DatabaseHelper.queryToDynaList(query.toString(), params.toArray());
  }

  private static final String GET_RATE_PLAN_LIST = "SELECT moi.org_id from mapping_org_id moi"
      + " LEFT JOIN orderable_item oi using(orderable_item_id)"
      + " WHERE oi.entity_id = ? AND moi.status != 'I' ";

  /**
   * get rate plan for item.
   * 
   * @param itemId
   *          the itemId
   * @param entity
   *          the entity
   * @return list of basic dyna bean
   */
  public List<BasicDynaBean> getRatePlanForItem(String itemId, List<String> entity) {
    StringBuilder query = new StringBuilder();
    List<Object> params = new ArrayList<>();

    query = query.append(GET_RATE_PLAN_LIST);
    params.add(itemId);

    if (!entity.isEmpty()) {
      String[] placeholdersArr = new String[entity.size()];
      Arrays.fill(placeholdersArr, "?");
      query.append("AND entity IN ( ")
          .append(StringUtils.arrayToCommaDelimitedString(placeholdersArr)).append(")");
      params.addAll(entity);
    }

    return DatabaseHelper.queryToDynaList(query.toString(), params.toArray());
  }

  private static final String GET_ITEM_NAME_BY_ENTITY_ID = "SELECT entity_id, entity, "
      + " CASE WHEN entity IN ('Other Charge', 'Bed')"
      + " THEN entity_id "
      + " ELSE coalesce(test.test_name, s.service_name, doc.doctor_name, d.dept_name, "
      + " em.equipment_name, om.operation_name, sid.medicine_name, oi.item_name) END "
      + " as item_name "
      + " FROM orderable_item oi "
      + " LEFT JOIN diagnostics test ON (oi.entity_id=test.test_id) "
      + " LEFT JOIN services s ON (oi.entity_id=s.service_id) "
      + " LEFT JOIN doctors doc ON (oi.entity_id=doc.doctor_id) "
      + " LEFT JOIN department d ON (oi.entity_id=d.dept_id) "
      + " LEFT JOIN equipment_master em ON (oi.entity_id=em.eq_id) "
      + " LEFT JOIN operation_master om ON (oi.entity_id=om.op_id) "
      + " LEFT JOIN store_item_details sid ON (oi.entity_id=sid.medicine_id::character varying) "
      + " WHERE entity_id IN (:entityIds) ";

  /**
   * Get item names by entity ids.
   * @param entityIds entity identifiers
   * @return List of BasicDynaBean
   */
  public List<BasicDynaBean> getItemNamesByEntityIds(List<String> entityIds) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("entityIds", entityIds);
    return DatabaseHelper.queryToDynaList(GET_ITEM_NAME_BY_ENTITY_ID, parameters);
  }
}
