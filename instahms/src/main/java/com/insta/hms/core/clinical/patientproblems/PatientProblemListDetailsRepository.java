package com.insta.hms.core.clinical.patientproblems;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryAssembler;
import com.insta.hms.mdm.SearchQuery;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Patient Problem List Details Repository.
 * 
 * @author VinayKumarJavalkar
 *
 */
@Repository
public class PatientProblemListDetailsRepository extends GenericRepository {

  public PatientProblemListDetailsRepository() {
    super("patient_problem_list_details");
  }

  private static final String PATIENT_PROBLEM_HISTORY = "SELECT"
      + " ppld.visit_id, ppld.problem_status, ppld.last_status_date,"
      + " ppld.created_by AS modified_by, ppld.created_at AS last_status_modified_at,"
      + " pr.reg_date AS visit_date"
      + " FROM patient_problem_list_details ppld"
      + " LEFT JOIN patient_registration pr ON ppld.visit_id = pr.patient_id";

  /**
   * Get Last Updated Problem Details.
   * 
   * @param pplId the pplid
   * @param sectionDetailId the section detail id
   * @param startDateTime the datetime
   * @return bean
   */
  public BasicDynaBean getLastUpdatedProblemDetails(int pplId, int sectionDetailId,
      String startDateTime) {
    StringBuilder query = new StringBuilder(PATIENT_PROBLEM_HISTORY);
    MapSqlParameterSource parameter = new MapSqlParameterSource();
    query.append(" WHERE ppl_id=:pplId AND (ppld.section_detail_id = :sectionDetailId");
    query.append(" OR ppld.created_at <= :consVisitStartDate::TIMESTAMP)");
    parameter.addValue("pplId", pplId);
    parameter.addValue("sectionDetailId", sectionDetailId);
    if (sectionDetailId != 0) {
      parameter.addValue("consVisitStartDate", startDateTime);
    } else {
      parameter.addValue("consVisitStartDate", "now()");
    }
    query.append(" ORDER BY ppld.created_at DESC LIMIT 1");
    return DatabaseHelper.queryToDynaBean(query.toString(), parameter);
  }
  
  /**
   * Gets Patient Problem History.
   * 
   * @param parameters the param
   * @return paged list
   */
  @SuppressWarnings("unchecked")
  public PagedList getPatientProblemHistory(Map<String, String[]> parameters) {
    Map<String, Object> filterMap = new HashMap<>();
    Map<String, Object> flattenRequestMap = ConversionUtils.flatten(parameters);
    filterMap.put("pplId", Integer.parseInt(flattenRequestMap.get("ppl_id").toString()));
    filterMap.put("page_num", Integer.parseInt(flattenRequestMap.get("page_num").toString()));
    filterMap.put("page_size", Integer.parseInt(flattenRequestMap.get("page_size").toString()));
    
    List<Object> paramList = new ArrayList<>();
    paramList.add(filterMap.get("pplId"));
    
    SearchQuery query =
        new SearchQuery("FROM (" + PATIENT_PROBLEM_HISTORY + " WHERE ppl_id=? " + ") AS foo");
    SearchQueryAssembler qb = new SearchQueryAssembler(query.getFieldList(), query.getCountQuery(),
        query.getSelectTables(), null, ConversionUtils.getListingParameter(parameters));
    qb.setfieldValues((ArrayList<Object>) paramList);
    qb.addSecondarySort("last_status_modified_at", true);
    qb.build();
    return qb.getMappedPagedList();
  }
}
