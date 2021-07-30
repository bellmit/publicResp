package com.insta.hms.api.services;

import com.insta.hms.api.repositories.ApiInsurancePlanRepository;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.QueryBuilder;
import com.insta.hms.common.SearchQueryAssembler;
import com.insta.hms.mdm.MasterService;
import com.insta.hms.mdm.insuranceplans.InsurancePlanValidator;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ApiInsurancePlanService extends MasterService {

  /** The Constant RETURN_CODE_SUCCESS. */
  private static final String RETURN_CODE_SUCCESS = "2001";

  /** The Constant RETURN_MSG_SUCCESS. */
  private static final String RETURN_MSG_SUCCESS = "Success";

  /** The Constant RETURN_CODE_SUCCESS. */
  private static final String RETURN_CODE_ERROR = "1021";

  /** The Constant RETURN_MSG_SUCCESS. */
  private static final String RETURN_MSG_ERROR = "Invalid input parameters supplied";

  /** The Constant RETURN_CODE_NODATA. */
  private static final String RETURN_CODE_NODATA = "2004";

  /** The Constant RETURN_MSG_NODATA. */
  private static final String RETURN_MSG_NODATA = "No matching records found";

  private static final Logger logger = LoggerFactory.getLogger(ApiInsurancePlanService.class);

  public ApiInsurancePlanService(
      ApiInsurancePlanRepository repo,
      InsurancePlanValidator validator) {
    super(repo, validator);
  }

  @Override
  public List<BasicDynaBean> autocomplete(String match, Map<String, String[]> parameters) {
    return autocomplete("insurance_plan_name", match, false, parameters);
  }

  @Override
  public SearchQueryAssembler getLookupQueryAssembler(
      String lookupQuery, Map<String, String[]> parameters) {
    SearchQueryAssembler qb = null;
    qb =
        new SearchQueryAssembler(
            lookupQuery, null, null, ConversionUtils.getListingParameter(parameters));

    if ( null != parameters && parameters.containsKey("status")
        && !parameters.get("status")[0].trim().isEmpty()) {
      qb.addFilter(QueryBuilder.STRING, "status","=",
          parameters.get("status")[0].trim());
    }
    return qb;
  }

  /**
   * Gets Insurance Plans list.
   *
   *
   * @param parameters
   *          the filter params
   * @return Map
   */
  public Map<String, Object> getInsurancePlans(Map<String, String[]> parameters) {
    Integer pageSize = (parameters.get("page_size") != null
        && NumberUtils.isDigits((parameters.get("page_size")[0])))
        ? Integer.parseInt(parameters.get("page_size")[0]) : 100;

    Integer pageNum = ( parameters.get("page") != null
        && NumberUtils.isDigits((parameters.get("page")[0])))
        ? Integer.parseInt(parameters.get("page")[0]) : 1;

    pageNum = pageNum < 0 ? 1 : pageNum;
    pageSize = pageSize < 0 ? 100 : pageSize;
    pageSize = pageSize > 500 ? 500 : pageSize;
    String searchTerm =
        (null != parameters && parameters.containsKey("search_term"))
            ? parameters.get("search_term")[0]
            : "";
    String contains = parameters.get("contains") != null
        && parameters.get("contains")[0].equals("false")
        ? "false" : "true";
    String  withSponsors = parameters.get("with_sponsors") != null
        && parameters.get("with_sponsors")[0].trim().equals("true")
        ? "true" : "false";
    String  withPlanType = parameters.get("with_plan_types") != null
        && parameters.get("with_plan_types")[0].trim().equals("true")
        ? "true" : "false";
    String status = (null != parameters && parameters.containsKey("status"))
        ? parameters.get("status")[0].toUpperCase() : "";
    Map<String,String[]> paramsMap =  new HashMap<>();
    paramsMap.put("search_term", new String[]{searchTerm});
    paramsMap.put("page_size", new String[]{String.valueOf(pageSize)});
    paramsMap.put("page_num", new String[]{String.valueOf(pageNum)});
    paramsMap.put("contains", new String[]{ contains });
    paramsMap.put("with_sponsors", new String[]{ withSponsors });
    paramsMap.put("with_plan_types", new String[]{ withPlanType });
    paramsMap.put("status", new String[] { status } );
    Map<String,Object> resultMap = new HashMap<>();
    List<BasicDynaBean> results = autocomplete(searchTerm, paramsMap);
    List searchSet = copyListDynaBeansToMap(results,paramsMap);
    resultMap.put("insurance_plans",searchSet);
    resultMap.put("page",pageNum);
    resultMap.put("page_size",searchSet.size());
    if ( searchSet != null && searchSet.size() == 0 ) {
      resultMap.put("return_code", RETURN_CODE_NODATA);
      resultMap.put("return_message", RETURN_MSG_NODATA);
    } else {
      resultMap.put("return_code", RETURN_CODE_SUCCESS);
      resultMap.put("return_message", RETURN_MSG_SUCCESS);
    }
    return resultMap;
  }


  /**
   * Gets Insurance Plan using planId.
   *
   * @param planId
   *          the planId
   * @param parameters
   *          the filter params
   * @return Map
   */
  public Map<String, Object> getInsurancePlanByPlanId(String planId,
      Map<String, String[]> parameters) {
    Map<String,Object> resultMap = new HashMap<>();
    if (planId == null || !NumberUtils.isDigits(planId)) {
      resultMap.put("return_code", RETURN_CODE_ERROR);
      resultMap.put("return_message", RETURN_MSG_ERROR);
      return resultMap;
    }
    Map<String,String[]> paramsMap =  new HashMap<>();
    String  withSponsors = parameters.get("with_sponsors") != null
        && parameters.get("with_sponsors")[0].trim().equals("true")
        ? "true" : "false";
    String  withPlanType = parameters.get("with_plan_types") != null
        && parameters.get("with_plan_types")[0].trim().equals("true")
        ? "true" : "false";
    paramsMap.put("with_sponsors", new String[]{ withSponsors });
    paramsMap.put("with_plan_types", new String[]{ withPlanType });
    BasicDynaBean bean =
        ((ApiInsurancePlanRepository) super.getRepository())
            .getInsurancePlanByPlanId(Integer.parseInt(planId));
    Map<String,Object> map = new HashMap<>();
    if ( bean == null ) {
      resultMap.put("return_code", RETURN_CODE_NODATA);
      resultMap.put("return_message", RETURN_MSG_NODATA);
    } else {
      List<BasicDynaBean> beans = new ArrayList<>();
      beans.add(bean);
      List listOfMap = copyListDynaBeansToMap(beans,paramsMap);
      map = (Map<String, Object>) listOfMap.get(0);
      resultMap.put("return_code", RETURN_CODE_SUCCESS);
      resultMap.put("return_message", RETURN_MSG_SUCCESS);
    }
    resultMap.put("insurance_plans", map);
    return resultMap;
  }

  /**
   * Gets Insurance Plan using planCode.
   *
   * @param planCode
   *          the planId
   * @param parameters
   *          the filter params
   * @return Map
   */
  public Map<String, Object> getInsurancePlanByPlanCode(String planCode,
      Map<String, String[]> parameters) {
    Map<String,Object> resultMap = new HashMap<>();
    BasicDynaBean bean =
        ((ApiInsurancePlanRepository) super.getRepository()).getInsurancePlanByPlanCode(planCode);
    Map<String,String[]> paramsMap =  new HashMap<>();
    String  withSponsors = parameters.get("with_sponsors") != null
        && parameters.get("with_sponsors")[0].trim().equals("true")
        ? "true" : "false";
    String  withPlanType = parameters.get("with_plan_types") != null
        && parameters.get("with_plan_types")[0].trim().equals("true")
        ? "true" : "false";
    paramsMap.put("with_sponsors", new String[]{ withSponsors });
    paramsMap.put("with_plan_types", new String[]{ withPlanType });
    Map<String,Object> map = new HashMap<>();
    if ( bean == null ) {
      resultMap.put("return_code", RETURN_CODE_NODATA);
      resultMap.put("return_message", RETURN_MSG_NODATA);
    } else {
      List<BasicDynaBean> beans = new ArrayList<>();
      beans.add(bean);
      List listOfMap = copyListDynaBeansToMap(beans,paramsMap);
      map = (Map<String, Object>) listOfMap.get(0);
      resultMap.put("return_code", RETURN_CODE_SUCCESS);
      resultMap.put("return_message", RETURN_MSG_SUCCESS);
    }
    resultMap.put("insurance_plans", map);
    return resultMap;
  }

  /**
   * Convert a list of beans to a list of Maps.
   *
   * @param dynabeans the dynabeans
   * @param paramMap the param Map
   * @return the list
   */
  private  List copyListDynaBeansToMap(List dynabeans, Map<String, String[]> paramMap) {
    List maplist = new ArrayList<Map<String, Object>>();
    if (dynabeans == null) {
      return maplist;
    }
    Map<Integer,List<BasicDynaBean>> sponsorListMap1 = null;
    Map<Integer,List<BasicDynaBean>> sponsorListMap2 = null;
    Map<Integer,List<BasicDynaBean>> planTypeListMap1 = null;
    Map<Integer,List<BasicDynaBean>> planTypeListMap2 = null;
    if ( paramMap.get("with_sponsors") != null
        && paramMap.get("with_sponsors")[0].equals("true")) {
      String [] planIds = getFilterPlanIdsString(dynabeans,"sponsor_id");
      List<BasicDynaBean> sponsorList = null;
      if (!planIds[0].equals("")) {
        sponsorList =
            ((ApiInsurancePlanRepository) super.getRepository()).getSponsorDetails(planIds[0]);
        sponsorListMap1 =
            ConversionUtils.listBeanToMapListBean(sponsorList, "insurance_plan_id");
      }
      if (!planIds[1].equals("")) {
        sponsorList =
           ((ApiInsurancePlanRepository) super.getRepository())
               .getSponsorDetailsFromTpaMaster(planIds[1]);
        sponsorListMap2 =
           ConversionUtils.listBeanToMapListBean(sponsorList,"insurance_plan_id");
      }
    }
    if ( paramMap.get("with_plan_types") != null
        && paramMap.get("with_plan_types")[0].equals("true")) {
      String [] planIds = getFilterPlanIdsString(dynabeans,"category_id");
      List<BasicDynaBean> planTypeList = null;
      if (!planIds[0].equals("")) {
        planTypeList =
            ((ApiInsurancePlanRepository) super.getRepository()).getPlanTypeDetails(planIds[0]);
        planTypeListMap1 =
            ConversionUtils.listBeanToMapListBean(planTypeList, "insurance_plan_id");
      }
      if (!planIds[1].equals("")) {
        planTypeList =
            ((ApiInsurancePlanRepository) super.getRepository())
                .getPlanTypeDetailsJoinUsingCategoryId(planIds[1]);
        planTypeListMap2 =
            ConversionUtils.listBeanToMapListBean(planTypeList, "insurance_plan_id");
      }
    }
    for (Object object : dynabeans) {
      BasicDynaBean bean = (BasicDynaBean) object;
      Map<String,Object> map =  new HashMap<>();
      map.put("insurance_plan_id",bean.get("insurance_plan_id"));
      map.put("insurance_plan_name",bean.get("insurance_plan_name"));
      map.put("insurance_co_id",bean.get("insurance_co_id"));
      map.put("insurance_co_name",bean.get("insurance_co_name"));
      map.put("insurance_plan_code",bean.get("insurance_plan_code"));
      map.put("insurance_interface_code", bean.get("insurance_interface_code"));
      map.put("default_rate_plan_code", bean.get("default_rate_plan_code"));
      map.put("ip_applicable", bean.get("ip_applicable"));
      map.put("op_applicable", bean.get("op_applicable"));
      map.put("valid_from", bean.get("valid_from"));
      map.put("valid_to", bean.get("valid_to"));
      map.put("status",bean.get("status"));
      if ( sponsorListMap1 != null || sponsorListMap2 != null) {
        List<BasicDynaBean> sponsors;
        if (bean.get("sponsor_id") == null) {
          sponsors = sponsorListMap1.get(bean.get("insurance_plan_id"));
        } else {
          sponsors = sponsorListMap2.get(bean.get("insurance_plan_id"));
        }
        List sponsorsList = new ArrayList<Map<String, Object>>();
        if (sponsors != null) {
          for (BasicDynaBean sponsor : sponsors) {
            Map<String, Object> tempMap = new HashMap<>();
            tempMap.put("sponsor_id", sponsor.get("sponsor_id"));
            tempMap.put("sponsor_name", sponsor.get("sponsor_name"));
            sponsorsList.add(tempMap);
          }
        }
        map.put("sponsors", sponsorsList);
      }
      if ( planTypeListMap1 != null || planTypeListMap2 != null ) {
        List<BasicDynaBean> planTypes;
        if (bean.get("category_id") == null) {
          planTypes = planTypeListMap1.get(bean.get("insurance_plan_id"));
        } else {
          planTypes = planTypeListMap2.get(bean.get("insurance_plan_id"));
        }
        List planTypesList = new ArrayList<Map<String, Object>>();
        if (planTypes != null ) {
          for (BasicDynaBean planType : planTypes) {
            Map<String, Object> tempMap = new HashMap<>();
            tempMap.put("plan_type_id", planType.get("plan_type_id"));
            tempMap.put("plan_type_name", planType.get("plan_type_name"));
            planTypesList.add(tempMap);
          }
        }
        map.put("plan_types",planTypesList);
      }
      maplist.add(map);
    }
    return maplist;
  }


  /**
   * Get planIds based on filter.
   *
   * @param beans
   *        the beans
   * @param filter
   *          the filter
   * @return String[]
   */
  private static String[] getFilterPlanIdsString(List<BasicDynaBean> beans,String filter) {
    StringBuilder planIdsWhenValueIsNull = new StringBuilder();
    StringBuilder planIdsWhenValueIsNotNull = new StringBuilder();
    String prefixWhenValueIsNull = "";
    String prefixWhenValueIsNotNull = "";
    for (BasicDynaBean bean : beans) {
      if (bean.get(filter) != null) {
        planIdsWhenValueIsNotNull.append(prefixWhenValueIsNotNull);
        planIdsWhenValueIsNotNull.append(bean.get("insurance_plan_id"));
        prefixWhenValueIsNotNull = ",";
      } else {
        planIdsWhenValueIsNull.append(prefixWhenValueIsNull);
        planIdsWhenValueIsNull.append(bean.get("insurance_plan_id"));
        prefixWhenValueIsNull = ",";
      }
    }
    return new String[]{planIdsWhenValueIsNull.toString(),planIdsWhenValueIsNotNull.toString()};
  }

}
