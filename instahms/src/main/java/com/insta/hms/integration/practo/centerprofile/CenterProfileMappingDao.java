package com.insta.hms.integration.practo.centerprofile;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Map;

/**
 * The Class CenterProfileMappingDao.
 */
public class CenterProfileMappingDao {
  
  /** The Constant CENTERS_FILEDS. */
  private static final String CENTERS_FILEDS = "SELECT * ";
  
  /** The Constant CENTERS_FROM. */
  private static final String CENTERS_FROM = " FROM hospital_center_master ";
  
  /** The Constant COUNT. */
  private static final String COUNT = " SELECT count(center_id) ";
  
  /**
   * Search centers.
   *
   * @param params the params
   * @param listingParams the listing params
   * @return the paged list
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   */
  public PagedList searchCenters(Map params, Map<LISTING, Object> listingParams)
      throws SQLException, ParseException {
    Connection con = DataBaseUtil.getConnection();
    SearchQueryBuilder qb = null;
    try {
      qb = new SearchQueryBuilder(con, CENTERS_FILEDS, COUNT, CENTERS_FROM, listingParams);
      qb.addFilterFromParamMap(params);
      qb.addFilter(qb.STRING, "status", "=", "A");
      qb.build();

      return qb.getDynaPagedList();
    } finally {
      DataBaseUtil.closeConnections(con, null);
      if (qb != null) {
        qb.close();
      }
    }
  }

  /** The Constant hospitalGroupID. */
  private static final String hospitalGroupID = "SELECT group_id FROM system_data";

  /**
   * Gets the hospital group id.
   *
   * @return the hospital group id
   * @throws SQLException the SQL exception
   */
  public String getHospitalGroupId() throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    String grpId = null;
    ResultSet rs = null;
    try {
      ps = con.prepareStatement(hospitalGroupID);
      rs = ps.executeQuery();
      if (rs.next()) {
        grpId = rs.getString("group_id");
      }
      return grpId;
    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }
  }

}
