package com.insta.hms.mdm.caserate;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository("caseRateRepository")
public class CaseRateRepository extends MasterRepository<Integer> {

  public CaseRateRepository() {
    super("case_rate_main", "case_rate_id");
  }

  private static final String PAGE_SIZE = "page_size";
  private static final String PAGE_NUMBER = "page_number";

  private static final String BASE_QUERY = "SELECT crm.case_rate_id, crm.insurance_company_id,"
      + " icom.insurance_co_name AS insurance_company_name, network_type_id, icam.category_name AS"
      + " network_type_name, ipm.plan_name AS plan_name, crm.plan_id AS plan_id, code_type, code,"
      + " code_description, case_rate_number, crm.status AS status,"
      + " count(case_rate_id) OVER (PARTITION BY 1), sum(crd.amount) AS amount"
      + " FROM case_rate_main crm"
      + " LEFT JOIN case_rate_detail crd using (case_rate_id)"
      + " LEFT JOIN insurance_plan_main ipm ON (crm.plan_id = ipm.plan_id)"
      + " LEFT JOIN insurance_company_master icom"
      + " ON (crm.insurance_company_id = icom.insurance_co_id)"
      + " LEFT JOIN insurance_category_master icam ON (crm.network_type_id = icam.category_id) %"
      + " GROUP BY crm.case_rate_id, icom.insurance_co_name, icam.category_name, ipm.plan_name"
      + " ORDER BY case_rate_id";

  /**
   * Find case rate by filters.
   *
   * @param params filter map
   * @return case rate response map
   */
  public Map<String, Object> findByFilters(Map<String, String> params) {

    StringBuilder query = new StringBuilder().append(BASE_QUERY);
    List<Object> queryArguments = new ArrayList<>();
    List<String> filters = new ArrayList<>();

    String code = params.get("code");
    if (!StringUtils.isEmpty(code)) {
      filters.add(" code =? ");
      queryArguments.add(code.trim());

    }

    String codeType = params.get("code_type");
    if (!StringUtils.isEmpty(codeType)) {
      filters.add(" code_type =? ");
      queryArguments.add(codeType.trim());

    }

    String caseRateNumber = params.get("case_rate_number");
    if (!StringUtils.isEmpty(caseRateNumber)) {
      filters.add(" case_rate_number =? ");
      queryArguments.add(Integer.parseInt(caseRateNumber.trim()));
    }

    String status = params.get("status");
    if (!StringUtils.isEmpty(status)) {
      filters.add(" crm.status =? ");
      queryArguments.add(status.trim());
    }

    if (!filters.isEmpty()) {
      query = new StringBuilder(query.toString().replaceAll("%",
          "WHERE" + StringUtils.collectionToDelimitedString(filters, " AND")));
    } else {
      query = new StringBuilder(query.toString().replaceAll("%", ""));
    }

    Integer pageSize;
    if (!StringUtils.isEmpty(params.get(PAGE_SIZE))) {
      pageSize = Integer.parseInt(params.get(PAGE_SIZE));
    } else {
      pageSize = 0;
    }
    if (pageSize != 0) {
      query.append(" LIMIT ?");
      queryArguments.add(pageSize);
    }

    Integer pageNum;
    if (!StringUtils.isEmpty(params.get(PAGE_NUMBER))) {
      pageNum = Integer.parseInt(params.get(PAGE_NUMBER));
    } else {
      pageNum = 0;
    }
    if (pageNum != 0) {
      query.append(" OFFSET ?");
      queryArguments.add((pageNum) * pageSize);
    }

    Map<String, Object> resultMap = new HashMap<>();
    List<BasicDynaBean> results = DatabaseHelper.queryToDynaList(query.toString(),
        queryArguments.toArray());
    resultMap.put(PAGE_SIZE, pageSize);
    resultMap.put(PAGE_NUMBER, pageNum);
    resultMap.put("case_rate_main", ConversionUtils.listBeanToListMap(results));
    resultMap.put("total_records", !results.isEmpty() ? results.get(0).get("count") : 0);

    return resultMap;
  }

  private static final String GET_CATEGORY_DETAIL_BY_PLAN_ID = "SELECT DISTINCT"
      + " insurance_category_id,"
      + " insurance_category_name"
      + " FROM item_insurance_categories"
      + " LEFT JOIN insurance_plan_details using (insurance_category_id) WHERE plan_id =?"
      + " AND system_category != 'Y'";

  public List<BasicDynaBean> getCategoryDetailsByPlanId(Integer planId) {
    return DatabaseHelper.queryToDynaList(GET_CATEGORY_DETAIL_BY_PLAN_ID, new Object[] { planId });
  }
}

