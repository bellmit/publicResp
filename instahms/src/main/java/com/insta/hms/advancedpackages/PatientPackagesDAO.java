package com.insta.hms.advancedpackages;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class PatientPackagesDAO.
 */
public class PatientPackagesDAO {

  /**
   * Gets the next sequence.
   *
   * @return the next sequence
   * @throws SQLException the SQL exception
   */
  public int getNextSequence() throws SQLException {
    String query = "SELECT nextval('patient_package_seq')";
    Connection con = DataBaseUtil.getReadOnlyConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(query);
      return DataBaseUtil.getIntValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant patient_packages. */
  private static final String patient_packages = "SELECT * ";

  /** The Constant patient_packages_count. */
  private static final String patient_packages_count = " SELECT count(*)  ";

  /** The Constant patient_packages_tables. */
  private static final String patient_packages_tables = " FROM (SELECT pp.mr_no, pp.patient_id, "
      + "  coalesce(get_patient_full_name(s.salutation, pd.patient_name,"
      + " pd.middle_name, pd.last_name),isr.patient_name) as patient_name, "
      + "  package_name, pp.package_id, pp.prescription_id,"
      + "  presc_date as presc_date,pr.reg_date," + "  (CASE "
      + "        WHEN pp.handed_over = 'Y' THEN 'Handed Over'"
      + " WHEN ppv.status = 'C' THEN 'Done' "
      + "        WHEN ppv.status = 'P' THEN 'In Progress'"
      + "  ELSE 'Not Conducted' END ) as status_text,pr.primary_sponsor_id,"
      + " pp.completion_time as completion_date,"
      + "  ppv.status,pp.handed_over,coalesce(pr.center_id,isr.center_id) as center_id,"
      + " pm.handover_to,pp.status as package_status"
      + "  ,pp.handover_time,pp.handover_to as package_handover_to,"
      + " date(pp.handover_time) as handover_date,"
      + " coalesce(pd.patient_group,0) as patient_group " + " FROM package_prescribed pp"
      + "  JOIN patient_package_status_view ppv ON(ppv.prescription_id = pp.prescription_id )"
      + "  JOIN packages pm USING(package_id) "
      + "  LEFT JOIN patient_registration pr ON (pr.patient_id = pp.patient_id) "
      + "  LEFT JOIN patient_details pd ON (pd.mr_no = pr.mr_no) "
      + "  LEFT JOIN salutation_master s ON (s.salutation_id = pd.salutation)"
      + "  LEFT JOIN incoming_sample_registration isr ON (pp.patient_id = isr.incoming_visit_id) "
      + " WHERE ppv.status != 'X' #completed#) as packages";

  /**
   * Gets the patient packages.
   *
   * @param filter the filter
   * @param listing the listing
   * @param centerId the center id
   * @return the patient packages
   * @throws ParseException the parse exception
   * @throws SQLException the SQL exception
   */
  public PagedList getPatientPackages(Map filter, Map listing, int centerId)
      throws ParseException, SQLException {
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();

      SearchQueryBuilder qb = new SearchQueryBuilder(con, patient_packages, patient_packages_count,
          patient_packages_tables.replace("#completed#",
              " AND (pp.status != 'C' OR pp.status IS NULL)"),
          listing);

      qb.addFilterFromParamMap(filter);
      if (centerId != 0) {
        qb.addFilter(qb.INTEGER, "center_id", "=", centerId);
      }
      qb.addFilter(qb.STRING, "handed_over", "=", "N");
      qb.appendToQuery(" patient_confidentiality_check(packages.patient_group,packages.mr_no) ");

      qb.addSecondarySort("presc_date");
      qb.build();

      PagedList lis = qb.getMappedPagedList();
      qb.close();
      return lis;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /**
   * Gets the patient packages.
   *
   * @param filter the filter
   * @param listing the listing
   * @param centerId the center id
   * @param handedover the handedover
   * @param sponsor the sponsor
   * @return the patient packages
   * @throws ParseException the parse exception
   * @throws SQLException the SQL exception
   */
  public PagedList getPatientPackages(Map filter, Map listing, int centerId, String handedover,
      String sponsor) throws ParseException, SQLException {
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();

      SearchQueryBuilder qb = new SearchQueryBuilder(con, patient_packages, patient_packages_count,
          patient_packages_tables.replace("#completed#", " AND pp.status = 'C' "), listing);

      qb.addFilterFromParamMap(filter);
      if (centerId != 0) {
        qb.addFilter(qb.INTEGER, "center_id", "=", centerId);
      }
      if (sponsor != null) {
        qb.addFilter(qb.STRING, "primary_sponsor_id", "=", sponsor);
      }
      qb.appendToQuery(" patient_confidentiality_check(packages.patient_group,packages.mr_no) ");

      qb.addSecondarySort("presc_date");
      qb.build();

      PagedList lis = qb.getMappedPagedList();
      qb.close();
      return lis;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /** The package details. */
  private String packageDetails = " SELECT * FROM package_prescribed "
      + " JOIN packages USING(package_id) WHERE prescription_id = ? ";

  /**
   * Gets the package details.
   *
   * @param prescritpionId the prescritpion id
   * @return the package details
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getPackageDetails(int prescritpionId) throws SQLException {
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      return DataBaseUtil.queryToDynaBean(packageDetails, prescritpionId);
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  private static final String INV_ISSUED_MULTI_PACKAGES = "SELECT pp.package_id,"
      + " pm.package_name, pp.pat_package_id  FROM patient_packages pp  JOIN packages pm ON "
      + " (pp.package_id = pm.package_id ) JOIN patient_package_contents ppc ON("
      + " pp.pat_package_id = ppc.patient_package_id) JOIN "
      + " patient_package_content_consumed ppcc ON(ppc.patient_package_content_id ="
      + " ppcc.patient_package_content_id) WHERE pp.mr_no = ? AND pp.status IN('P','C') AND "
      + " pm.multi_visit_package = true AND ppc.charge_head='INVITE' GROUP BY pp.package_id,"
      + " pm.package_name, pp.pat_package_id;";

  public List<BasicDynaBean> getInvMultiPackagesIssued(String mrNo) throws SQLException {
    return DataBaseUtil.queryToDynaList(INV_ISSUED_MULTI_PACKAGES, mrNo);
  }

  private static final String GET_CONSUMED_PACKAGE_CONTENT =
      "SELECT ppcc.patient_package_content_id, ppc.activity_qty as content_qty,"
      + "  SUM(ppcc.quantity) as consumed_qty FROM patient_package_content_consumed ppcc "
      + " JOIN patient_package_contents ppc ON("
      + "ppc.patient_package_content_id = ppcc.patient_package_content_id) "
      + " WHERE ppc.patient_package_id=? GROUP BY 1,2";

  public List<BasicDynaBean> getPatPkgContentsConsumed(Connection con, Integer patPkgId)
      throws SQLException {
    return DataBaseUtil.queryToDynaList(con, GET_CONSUMED_PACKAGE_CONTENT, patPkgId);
  }
}
