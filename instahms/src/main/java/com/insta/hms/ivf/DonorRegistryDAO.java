package com.insta.hms.ivf;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class DonorRegistryDAO.
 */
public class DonorRegistryDAO {
  
  /** The Constant DONOR_REGISTRY_FIELDS. */
  private static final String DONOR_REGISTRY_FIELDS = "SELECT *";
  
  /** The Constant DONOR_REGISTRY_COUNT. */
  private static final String DONOR_REGISTRY_COUNT = "SELECT count(mr_no) ";
  
  /** The Constant DONOR_REGISTRY_TABLE. */
  private static final String DONOR_REGISTRY_TABLE = "FROM ivf_donor_header ";

  /**
   * Gets the donorregistry details.
   *
   * @param filter the filter
   * @param listing the listing
   * @return the donorregistry details
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   */
  public static PagedList getDonorregistryDetails(Map filter, Map listing) throws SQLException,
      ParseException {
    Connection con = DataBaseUtil.getReadOnlyConnection();
    SearchQueryBuilder qb = new SearchQueryBuilder(con, DONOR_REGISTRY_FIELDS,
        DONOR_REGISTRY_COUNT, DONOR_REGISTRY_TABLE, listing);
    qb.addFilterFromParamMap(filter);
    qb.build();
    PagedList list = qb.getMappedPagedList();
    qb.close();
    con.close();
    return list;
  }
}