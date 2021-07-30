package com.insta.hms.integration.practo.doctorprofile;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class DoctorProfileMappingDao.
 *
 * @author insta
 */
public class DoctorProfileMappingDao {

  /** The Constant DOCTOR_FIELDS. */
  private static final String DOCTOR_FIELDS = "SELECT *";
  
  /** The Constant DOCTOR_COUNT. */
  private static final String DOCTOR_COUNT = "SELECT count(*)";

  /** The Constant DOCTOR_FROM_TABLES. */
  private static final String DOCTOR_FROM_TABLES = "FROM (SELECT distinct d.doctor_name,"
      + " d.doctor_id, d.status , doctor_mobile, doctor_mail_id, doctor_address,"
      + "d.dept_id, dept.dept_name, dcm.doc_center_id as center_id, hcm.center_id as cen_id,"
      + " hcm.center_name as center_name, dcm.status as doc_cen_status, "
      + "hcm.status as center_status FROM doctors d "
      + "JOIN department dept ON (dept.dept_id = d.dept_id)"
      + "JOIN doctor_center_master  dcm ON(d.doctor_id=dcm.doctor_id) "
      + "JOIN hospital_center_master hcm ON((dcm.center_id=hcm.center_id and dcm.center_id!=0)"
      + " or (dcm.center_id=0 and hcm.center_id!=0)))AS foo";

  /**
   * Instantiates a new doctor profile mapping dao.
   */
  public DoctorProfileMappingDao() {
  }

  /**
   * Gets the doc listing.
   *
   * @param requestParams the request params
   * @param pagingParams the paging params
   * @return the doc listing
   * @throws ParseException the parse exception
   * @throws SQLException the SQL exception
   */
  public PagedList getDocListing(Map requestParams, Map<LISTING, Object> pagingParams)
      throws ParseException, SQLException {
    Connection con = null;
    SearchQueryBuilder qb = null;
    List list = new ArrayList();
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      qb = new SearchQueryBuilder(con, DOCTOR_FIELDS, DOCTOR_COUNT, DOCTOR_FROM_TABLES,
          pagingParams);
      qb.addFilterFromParamMap(requestParams);
      /*
       * list.add(RequestContext.getCenterId()); list.add(0); list.add(2); list.add(3);
       * qb.addFilter(qb.INTEGER, "center_id", "IN", list);
       */
      qb.addFilter(qb.STRING, "doc_cen_status", "=", "A");
      qb.addFilter(qb.STRING, "status", "=", "A");
      qb.addSecondarySort("doctor_id");
      qb.build();
      return qb.getDynaPagedList();
    } finally {
      qb.close();
      DataBaseUtil.closeConnections(con, null);
    }
  }
}
