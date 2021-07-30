package com.insta.hms.integration.backload;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryAssembler;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class DataBackloadAuditRepository extends MasterRepository<Integer>  {

  public DataBackloadAuditRepository() {
    super("backload_audit_table", "backload_audit_id");
  }
  
  private static final String GET_BACKLOADJOB_QUERY = 
      "Select but.backload_audit_id as Backload_Id,"
      + " but.records_found,"
      + " but.records_processed,"
      + " but.record_start_date,"
      + " but.record_end_date,"
      + " but.job_submitted_time,"
      + " but.center_id,"
      + " but.status,"
      + " but.created_by ";
  
  private static final String SELECT_JOBLOG_FIELDS = ", jl.job_start_time, jl.job_end_time ";
  
  private static final String SELECT_INTERFACE_FIELDS = ", icm.interface_name ";
  
  private static final String FROM_BACKLOADAUDIT = " FROM backload_audit_table but ";
  
  private static final String FROM_JOBLOG_JOIN = " left join job_log jl "
      + " on jl.job_group = split_part(but.job_key,'.',1) "
      + " and jl.job_name = split_part(but.job_key,'.',2) ";
  
  private static final String FROM_ICM_JOIN = " left join interface_config_master icm "
      + " on icm.interface_id = but.interface_id ";

  private static final String BACKLOADJOB_QUERY_WHERE = "WHERE but.backload_audit_id = ?";

  /**
   * Get backload audit details.
   * 
   * @param backloadId the backload id
   * @return basic dyna bean
   */
  public BasicDynaBean getBackloadAuditDetails(int backloadId) {
    return DatabaseHelper.queryToDynaBean(GET_BACKLOADJOB_QUERY + SELECT_INTERFACE_FIELDS
      + FROM_BACKLOADAUDIT + FROM_ICM_JOIN + BACKLOADJOB_QUERY_WHERE, new Object[] {backloadId});
  }

  private static final String COUNT = " SELECT count(distinct but.backload_audit_id) ";
  
  /**
   * Get the interface list with details.
   *
   * @param paramMap the param map
   * @return the note types details
   */
  public PagedList getBackloadJobDetails( Map<String, String[]> paramMap) {
    Map<LISTING, Object> listingParams = ConversionUtils.getListingParameter(paramMap);
    int pageNum = ((Integer) listingParams.get(LISTING.PAGENUM)).intValue();
    int pageSize = ((Integer) listingParams.get(LISTING.PAGESIZE)).intValue();
    String centerId = " AND but.center_id =" + RequestContext.getCenterId();
    String status = " but.status ='" + paramMap.get("status")[0] + "'";
    SearchQueryAssembler qb = 
        new SearchQueryAssembler(GET_BACKLOADJOB_QUERY + SELECT_JOBLOG_FIELDS
        + SELECT_INTERFACE_FIELDS, COUNT, FROM_BACKLOADAUDIT + FROM_JOBLOG_JOIN + FROM_ICM_JOIN,
        "WHERE " + status + centerId, "but.created_at", true, pageSize, pageNum );
    qb.build();
    return qb.getMappedPagedList();
  }

  private static final String JOB_ALREADY_INITIATED = " WHERE but.status = 'INITIATED' "
      + "AND but.center_id = ? ";

  /**
   * To check if backload started for center.
   * 
   * @return basic dyna bean
   */
  public BasicDynaBean checkIfBackloadInitiatedForCenter() {
    return DatabaseHelper.queryToDynaBean(GET_BACKLOADJOB_QUERY + FROM_BACKLOADAUDIT
      + JOB_ALREADY_INITIATED, new Object[] {RequestContext.getCenterId()});
  }
}