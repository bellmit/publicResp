package com.insta.hms.bedview;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.usermanager.User;
import com.insta.hms.usermanager.UserDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BedViewDao {
  private static final String BED_VIEW_LIST_QUERY_FIELDS = " SELECT "
      + "(CASE WHEN foo.status in('A','C','R')  THEN 'Occupied' "
      + " WHEN foo.status = 'B' THEN 'Blocked' "
      + " ELSE (CASE WHEN bed_status = 'C' THEN 'Cleaning' "
      + " WHEN bed_status = 'M' THEN 'Maintainance' "
      + " ELSE 'Ready to Occupy' END) END ) AS bed_avbl_status,*  ";

  private static final String BED_VIEW_LIST_QUERY_COUNT = "SELECT COUNT(mr_no) ";

  private static final String BED_VIEW_LIST = " FROM (SELECT * FROM  bed_status_report bsr "
      + "LEFT JOIN adt_bill_and_discharge_status_view adtv ON (adtv.visit_id = bsr.patient_id) "
      + " ORDER BY ward_name,s_bed_name::integer)  AS foo ";

  private static final String OCCUPIED_BED_LIST = "SELECT BD.BED_TYPE, BD.BED_NAME, BD.OCCUPANCY, "
      + "BD.WARD_NO, WN.WARD_NAME FROM BED_NAMES BD, WARD_NAMES WN WHERE OCCUPANCY IN('N','BN') "
      + " AND BD.WARD_NO = WN.WARD_NO ORDER BY BED_NAME, WARD_NO, WARD_NAME, BED_TYPE";

  /**
   * Gets the bed view.
   *
   * @param filter the filter
   * @param listing the listing
   * @param userName the user name
   * @param centerId the center id
   * @param multicentered the multicentered
   * @return the bed view
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   */
  public PagedList getBedView(Map filter, Map listing, String userName, int centerId,
      boolean multicentered) throws SQLException, ParseException {

    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      SearchQueryBuilder qb = new SearchQueryBuilder(con, BED_VIEW_LIST_QUERY_FIELDS,
          BED_VIEW_LIST_QUERY_COUNT, BED_VIEW_LIST, listing);
      setWardName(filter, new UserDAO(con).getUser(userName));
      qb.addFilterFromParamMap(filter);
      if (multicentered && centerId != 0) {
        qb.addFilter(SearchQueryBuilder.INTEGER, "center_id", "=", centerId);
      }
      qb.build();

      qb.build();

      PagedList list = qb.getMappedPagedList();

      return list;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /**
   * Sets the ward name.
   *
   * @param filter the filter
   * @param userBean the user bean
   * @return the map
   * @throws SQLException the SQL exception
   */
  public Map setWardName(Map filter, User userBean) throws SQLException {
    String[] wardName = ((String[]) filter.get("ward_no"));
    String[] wardNames = new String[1];
    if (wardName == null || (wardName[0] == null && (String[]) filter.get("all_wards") == null)) {
      if (userBean != null) {
        wardNames[0] = userBean.getBedViewDefaultWard();
      } else {
        wardNames[0] = getDefaultWardForAdmin();
      }

      filter.put("ward_no", wardNames);

    }
    if (((String[]) filter.get("all_wards")) != null) {
      wardName[0] = null;
      filter.remove("all_wards");
    }

    return filter;
  }

  /**
   * Gets the default ward for admin.
   *
   * @return the default ward for admin
   * @throws SQLException the SQL exception
   */
  public static String getDefaultWardForAdmin() throws SQLException {
    String defaultQuery = "SELECT min(ward_no) as ward_no FROM ward_names";
    Connection con = null;
    PreparedStatement ps = null;
    String defaultWard = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(defaultQuery);
      BasicDynaBean wardBean = DataBaseUtil.queryToDynaBean(ps);
      defaultWard = (String) wardBean.get("ward_no");
      return defaultWard;
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

  }

  /**
   * Modify bed status.
   *
   * @param bedDetails the bed details
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public boolean modifyBedStatus(BasicDynaBean bedDetails) throws SQLException, IOException {
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      Map keys = new HashMap<String, Integer>();
      keys.put("bed_id", bedDetails.get("bed_id"));
      return new GenericDAO("bed_names").update(con, bedDetails.getMap(), keys) > 0;
    } finally {
      if (con != null) {
        con.close();
      }
    }
  }

  public ArrayList getOccupiedBeds() {
    return DataBaseUtil.queryToArrayList(OCCUPIED_BED_LIST);
  }

}
