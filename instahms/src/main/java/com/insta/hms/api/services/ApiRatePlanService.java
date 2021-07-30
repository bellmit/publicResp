package com.insta.hms.api.services;


import com.bob.hms.common.DateUtil;
import com.insta.hms.api.repositories.ApiRatePlanRepository;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.QueryAssembler;
import com.insta.hms.common.QueryBuilder;
import com.insta.hms.common.SearchQueryAssembler;
import com.insta.hms.mdm.MasterService;
import com.insta.hms.mdm.rateplan.RatePlanValidator;


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
public class ApiRatePlanService extends MasterService {

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

  private static final Logger logger = LoggerFactory.getLogger(ApiRatePlanService.class);

  public ApiRatePlanService(ApiRatePlanRepository repo, RatePlanValidator validator) {
    super(repo, validator);
  }

  @Override
  public List<BasicDynaBean> autocomplete(String match, Map<String, String[]> parameters) {
    return autocomplete("rateplan_name", match, false, parameters);
  }

  @Override
  public SearchQueryAssembler getLookupQueryAssembler(
      String lookupQuery, Map<String, String[]> parameters) {
    SearchQueryAssembler qb = null;
    qb =
        new SearchQueryAssembler(
            lookupQuery, null, null, ConversionUtils.getListingParameter(parameters));

    ArrayList<Object> types = new ArrayList<Object>();
    types.add(QueryAssembler.INTEGER);

    if ( null != parameters && parameters.containsKey("status") 
        && !parameters.get("status")[0].trim().isEmpty()) {
      qb.addFilter(QueryBuilder.STRING, "status","=",
            parameters.get("status")[0].trim());
    }
    if ( null != parameters && parameters.containsKey("modified_after") ) {
      qb.addFilter(QueryBuilder.TIMESTAMP, "modified_at",">",
          Timestamp.valueOf(parameters.get("modified_after")[0]));
    }
    qb.addFilter(QueryBuilder.TIMESTAMP, "modified_at","<=",
        Timestamp.valueOf(parameters.get("modified_before")[0]));
    return qb;
  }


  /**
   * Gets Rate Plans list.
   *
   *
   * @param parameters
   *          the filter params
   * @return Map
   */
  public Map<String, Object> getRatePlans(Map<String, String[]> parameters) {
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
    String status = (null != parameters && parameters.containsKey("status"))
            ? parameters.get("status")[0].toUpperCase() : "";
    Map<String,String[]> paramsMap =  new HashMap<>();
    paramsMap.put("search_term", new String[]{searchTerm});
    paramsMap.put("page_size", new String[]{String.valueOf(pageSize)});
    paramsMap.put("page_num", new String[]{String.valueOf(pageNum)});
    paramsMap.put("contains", new String[]{ contains });
    paramsMap.put("status", new String[] { status } );

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
    List<BasicDynaBean> results = autocomplete(searchTerm, paramsMap);
    List searchSet = copyListDynaBeansToMap(results);
    resultMap.put("rateplans",searchSet);
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
   * Gets Rate Plan by Id.
   *
   *
   * @param  ratePlanCode
   *          the ratePlan id
   * @return Map
   */
  public Map<String, Object> getRatePlanById(String ratePlanCode) {
    Map<String,Object> resultMap = new HashMap<>();
    BasicDynaBean bean =
        ((ApiRatePlanRepository) super.getRepository()).getRatePlanById(ratePlanCode);
    Map<String,Object> map =  new HashMap<>();
    if ( bean == null ) {
      resultMap.put("rateplan", map);
      resultMap.put("return_code", RETURN_CODE_NODATA);
      resultMap.put("return_message", RETURN_MSG_NODATA);
    } else {
      try {
        map.put("rateplan_code",bean.get("rateplan_code"));
        map.put("rateplan_name",bean.get("rateplan_name"));
        map.put("valid_from",bean.get("valid_from"));
        map.put("valid_to",bean.get("valid_to"));
        map.put("created_at", DateUtil.convertTimeStampToIso8601(
                bean.get("created_at").toString()));
        map.put("modified_at", DateUtil.convertTimeStampToIso8601(
                bean.get("modified_at").toString()));
        map.put("status",bean.get("status"));
        resultMap.put("rateplan", map);
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
      Map<String,Object> map =  new HashMap<>();
      try {
        map.put("rateplan_code",bean.get("rateplan_code"));
        map.put("rateplan_name",bean.get("rateplan_name"));
        map.put("valid_from",bean.get("valid_from"));
        map.put("valid_to",bean.get("valid_to"));
        map.put("created_at", DateUtil.convertTimeStampToIso8601(
                bean.get("created_at").toString()));
        map.put("modified_at", DateUtil.convertTimeStampToIso8601(
                bean.get("modified_at").toString()));
        map.put("status",bean.get("status"));
        maplist.add(map);
      } catch (ParseException ex) {
        logger.error("",ex);
      }
    }
    return maplist;
  }

}
