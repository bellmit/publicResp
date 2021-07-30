package com.insta.hms.api.services;

import com.bob.hms.common.DateUtil;
import com.insta.hms.api.repositories.ApiReferralDoctorRepository;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.QueryAssembler;
import com.insta.hms.common.QueryBuilder;
import com.insta.hms.common.SearchQueryAssembler;
import com.insta.hms.mdm.MasterService;
import com.insta.hms.mdm.referraldoctors.ReferralDoctorValidator;


import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class ApiReferralDoctorService extends MasterService {

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

  private static final Logger logger = LoggerFactory.getLogger(ApiReferralDoctorService.class);

  public ApiReferralDoctorService(ApiReferralDoctorRepository repo,
      ReferralDoctorValidator validator) {
    super(repo, validator);
  }


  @Override
  public List<BasicDynaBean> autocomplete(String match, Map<String, String[]> parameters) {
    return autocomplete("name", match, false, parameters);
  }

  @Override
  public SearchQueryAssembler getLookupQueryAssembler(
      String lookupQuery, Map<String, String[]> parameters) {
    SearchQueryAssembler qb = null;
    qb =
        new SearchQueryAssembler(
            lookupQuery, null, null, ConversionUtils.getListingParameter(parameters));

    String[] centerIds =
        (null != parameters && parameters.containsKey("center_id"))
            ? parameters.get("center_id")
            : new String[]{"0"};
    ArrayList<Object> valueList = new ArrayList<>();
    if ( centerIds != null ) {
      for (String centerId : centerIds) {
        valueList.add(Integer.parseInt(centerId));
      }
    }
    ArrayList<Object> types = new ArrayList<Object>();
    types.add(QueryAssembler.INTEGER);

    if ( null != parameters && parameters.containsKey("status")
        && !parameters.get("status")[0].trim().isEmpty() ) {
      qb.addFilter(QueryBuilder.STRING, "status","=",
            parameters.get("status")[0].trim());
    }
    if ( null != parameters && parameters.containsKey("modified_after") ) {
      qb.addFilter(QueryBuilder.TIMESTAMP, "modified_at",">",
          Timestamp.valueOf(parameters.get("modified_after")[0]));
    }
    qb.addFilter(QueryBuilder.TIMESTAMP, "modified_at","<=",
        Timestamp.valueOf(parameters.get("modified_before")[0]));
    qb.appendExpression("(? = ANY ( string_to_array(center_ids,','):: integer[]))",
        types,valueList);
    return qb;
  }

  @Override
  public void addFilterForLookUp(
      SearchQueryAssembler qb,
      String likeValue,
      String matchField,
      boolean contains,
      Map<String, String[]> parameters) {
    if (!likeValue.trim().isEmpty()) {
      String filterText = likeValue.trim() + "%";
      if (contains) {
        filterText = "%" + likeValue.trim() + "%";
      }
      ArrayList<Object> types = new ArrayList<Object>();
      types.add(QueryAssembler.STRING);
      types.add(QueryAssembler.STRING);
      ArrayList<String> values = new ArrayList<String>();
      values.add(filterText);
      values.add(filterText);
      qb.appendExpression(
          " ( " + matchField + " ILIKE ? " + "OR  license_no ILIKE ? ) ", types, values);
    }
  }

  /**
   * Gets Referral doctors list.
   *
   *
   * @param parameters
   *          the filter params
   * @return Map
   */
  public Map<String, Object> getReferralsList( Map<String, String[]> parameters) {
    Map<String,Object> resultMap = new HashMap<>();
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
    String centerId = (null != parameters && parameters.containsKey("center_id"))
        ? parameters.get("center_id")[0]
        : "0";
    String status = (null != parameters && parameters.containsKey("status"))
        ? parameters.get("status")[0].toUpperCase()
        : "";
    Map<String,String[]> paramsMap =  new HashMap<>();
    paramsMap.put("search_term", new String[]{searchTerm});
    paramsMap.put("page_size", new String[]{String.valueOf(pageSize)});
    paramsMap.put("page_num", new String[]{String.valueOf(pageNum)});
    paramsMap.put("center_id", new String[]{centerId});
    paramsMap.put("contains", new String[]{ contains });
    paramsMap.put("status", new String[] { status });

    Object modifiedAfterTimestamp = null;
    Object modifiedBeforeTimestamp = null;
    try {
      if (null != parameters && parameters.containsKey("modified_after")) {
        String modifiedAfter = parameters.get("modified_after")[0];
        modifiedAfterTimestamp = DateUtil.parseIso8601Timestamp(modifiedAfter);
        if ( modifiedAfterTimestamp == null ) {
          resultMap.put("return_code", RETURN_CODE_ERROR);
          resultMap.put("return_message", RETURN_MSG_ERROR);
          return resultMap;
        }
        paramsMap.put("modified_after",new String[]{modifiedAfterTimestamp.toString()});
      }
      if ( null != parameters && parameters.containsKey("modified_before") ) {
        String modifiedBefore = parameters.get("modified_before")[0];
        modifiedBeforeTimestamp = DateUtil.parseIso8601Timestamp(modifiedBefore);
        if ( modifiedBeforeTimestamp == null ) {
          resultMap.put("return_code", RETURN_CODE_ERROR);
          resultMap.put("return_message", RETURN_MSG_ERROR);
          return resultMap;
        }
        paramsMap.put("modified_before",new String[]{modifiedBeforeTimestamp.toString()});
      } else {
        modifiedBeforeTimestamp = DateUtil.getCurrentTimestamp();
        paramsMap.put("modified_before",
            new String[]{modifiedBeforeTimestamp.toString()});
      }

    } catch (ParseException ex) {
      resultMap.put("return_code", RETURN_CODE_ERROR);
      resultMap.put("return_message", RETURN_MSG_ERROR);
      return resultMap;
    }
    List searchSet = copyListDynaBeansToMap(autocomplete(searchTerm, paramsMap));
    resultMap.put("referraldoctors",searchSet);
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
   * Gets Referral doctor by Id.
   *
   *
   * @param  referralId
   *          the referral doctor id
   * @return Map
   */
  public Map<String, Object> getReferralById(String referralId) {
    Map<String,Object> resultMap = new HashMap<>();
    BasicDynaBean bean =
        ((ApiReferralDoctorRepository) super.getRepository()).getReferralById(referralId);
    Map<String,Object> map =  new HashMap<>();
    if ( bean == null ) {
      resultMap.put("referraldoctor", map);
      resultMap.put("return_code", RETURN_CODE_NODATA);
      resultMap.put("return_message", RETURN_MSG_NODATA);
    } else {
      String[] centerIdsStr = bean.get("center_ids").toString().split(",");
      ArrayList<Integer> centerIds = new ArrayList<Integer>();
      for (String centerId : centerIdsStr) {
        centerIds.add(Integer.parseInt(centerId));
      }
      try {
        map.put("id", bean.get("id"));
        map.put("name", bean.get("name"));
        map.put("address", bean.get("address"));
        map.put("referral_source", bean.get("referral_source"));
        map.put("mobile", bean.get("mobile"));
        map.put("created_at", DateUtil.convertTimeStampToIso8601(
                bean.get("created_at").toString()));
        map.put("modified_at", DateUtil.convertTimeStampToIso8601(
                bean.get("modified_at").toString()));
        map.put("center_ids", centerIds);
        map.put("area_id", bean.get("area_id"));
        map.put("city_id", bean.get("city_id"));
        map.put("license_no", bean.get("license_no"));
        map.put("phone", bean.get("phone"));
        map.put("email", bean.get("email"));
        map.put("status", bean.get("status"));
        resultMap.put("referraldoctor", map);
        resultMap.put("return_code", RETURN_CODE_SUCCESS);
        resultMap.put("return_message", RETURN_MSG_SUCCESS);
      } catch (ParseException ex) {
        logger.error("",ex);
      }
    }
    return resultMap;
  }

  /**
   * Convert a list of beans to a list of Maps.
   *
   * @param dynabeans the dynabeans
   * @return the list
   */
  private static List copyListDynaBeansToMap(List dynabeans) {
    List maplist = new ArrayList<Map<String, Object>>();
    if (dynabeans == null) {
      return maplist;
    }
    for (Object object : dynabeans) {
      BasicDynaBean bean = (BasicDynaBean) object;
      String[] centerIdsStr = bean.get("center_ids").toString().split(",");
      ArrayList<Integer> centerIds = new  ArrayList<Integer>();
      for (String centerId :centerIdsStr ) {
        centerIds.add(Integer.parseInt(centerId));
      }
      Map<String,Object> map =  new HashMap<>();
      try {
        map.put("id", bean.get("id"));
        map.put("name", bean.get("name"));
        map.put("address", bean.get("address"));
        map.put("referral_source", bean.get("referral_source"));
        map.put("mobile", bean.get("mobile"));
        map.put("created_at", DateUtil.convertTimeStampToIso8601(
                bean.get("created_at").toString()));
        map.put("modified_at", DateUtil.convertTimeStampToIso8601(
                bean.get("modified_at").toString()));
        map.put("center_ids", centerIds);
        map.put("area_id", bean.get("area_id"));
        map.put("city_id", bean.get("city_id"));
        map.put("license_no", bean.get("license_no"));
        map.put("phone", bean.get("phone"));
        map.put("email", bean.get("email"));
        map.put("status", bean.get("status"));
        maplist.add(map);
      } catch (ParseException ex) {
        logger.error("",ex);
      }
    }
    return maplist;
  }
}
