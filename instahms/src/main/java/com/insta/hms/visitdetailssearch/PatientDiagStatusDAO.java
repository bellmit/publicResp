package com.insta.hms.visitdetailssearch;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class PatientDiagStatusDAO.
 */
public class PatientDiagStatusDAO {

  /** The patient diag visit fields. */
  private static final String PATIENT_DIAG_VISIT_FIELDS = " SELECT mr_no, patient_id,"
      + " center_id,reg_date ";

  /** The patient diag visit count. */
  private static final String PATIENT_DIAG_VISIT_COUNT = " SELECT count(distinct patient_id) ";

  /** The patient diag visit tables. */
  private static final String PATIENT_DIAG_VISIT_TABLES = " FROM "
      + " (SELECT COALESCE(pr.mr_no, isr.mr_no) "
      + " as mr_no, tp.pat_id as patient_id, "
      + " COALESCE(pr.center_id, isr.center_id) as center_id,"
      + " CASE WHEN (dom.outsource_dest_type = 'C' AND tpc.conducted IN ('RAS','RBS')) "
      + " THEN tp.conducted ELSE coalesce(tpc.conducted, tp.conducted) END as conducted, "
      + " COALESCE(pr.reg_date, isr.date) as reg_date, d.ddept_id, "
      // use to get current sample status throughout the centers
      + " CASE WHEN (d.sample_needed = 'y' AND sc.sample_status IS NULL) THEN 'NC' "
      + " WHEN (dom.outsource_dest_type = 'C' AND sc.sample_transfer_status = 'P') "
      + " THEN sc.sample_status "
      + " WHEN (dom.outsource_dest_type = 'C' AND scc.sample_receive_status = 'P') "
      + " THEN sc.sample_transfer_status "
      + " WHEN (dom.outsource_dest_type = 'C' AND scc.sample_receive_status = 'R' "
      + " AND scc.sample_status = 'A') THEN scc.sample_status "
      + " WHEN (dom.outsource_dest_type = 'C' AND scc.sample_receive_status = 'R') THEN 'RC' "
      + " WHEN (scol.sample_transfer_status = 'T' AND sc.sample_receive_status = 'P') "
      + " THEN scol.sample_transfer_status "
      + " WHEN (scol.sample_transfer_status = 'T' AND sc.sample_receive_status = 'R' "
      + " AND sc.sample_status != 'A') THEN 'RC' "
      + " WHEN ((dom.outsource_dest_type = 'O' OR dom.outsource_dest_type = 'IO') "
      + " AND sc.sample_transfer_status IS NOT NULL) THEN sc.sample_transfer_status "
      // " WHEN (scol.sample_transfer_status IS NULL AND sc.sample_receive_status = 'R' AND
      // sc.sample_status != 'A') THEN 'RC' " +
      + " WHEN (d.sample_needed = 'n' AND sc.sample_status IS NULL) THEN 'NA' "
      + " ELSE sc.sample_status END AS sample_status, "
      + " COALESCE(tpcol.sample_no, tp.sample_no) as sample_no, tp.external_report_ready "
      + " FROM tests_prescribed tp "
      + " JOIN diagnostics d ON (d.test_id = tp.test_id) AND (tp.mr_no = #)"
      + " LEFT JOIN diag_outsource_master dom ON (dom.outsource_dest_id = tp.outsource_dest_id)"
      + " LEFT JOIN patient_registration pr ON (pr.patient_id = tp.pat_id) "
      + " LEFT JOIN incoming_sample_registration isr ON (tp.pat_id = isr.incoming_visit_id) "
      + " LEFT JOIN incoming_sample_registration_details isrd "
      + " ON (isrd.prescribed_id = tp.prescribed_id "
      + " AND isr.incoming_visit_id = isrd.incoming_visit_id)"
      + " LEFT JOIN sample_collection sc ON (sc.sample_collection_id = tp.sample_collection_id)"
      // use to get current sample status throughout the centers
      + " LEFT JOIN tests_prescribed tpc ON (tpc.prescribed_id = tp.curr_location_presc_id)"
      + " LEFT JOIN sample_collection scc ON (scc.sample_collection_id = tpc.sample_collection_id)"
      // to get current location details from logged in center
      + " LEFT JOIN tests_prescribed tpcol ON (tpcol.prescribed_id = tp.coll_prescribed_id) "
      + " LEFT JOIN sample_collection scol "
      + " ON (scol.sample_collection_id = tpcol.sample_collection_id) "
      + " WHERE tp.conducted IN('P','N','C','V','S','RC','RV','RP','MA',"
      + " 'TS','CC','CR','X','RAS','RBS','NRN','CRN')) "
      + " as foo ";

  /** The patient diag visit group. */
  private static final String PATIENT_DIAG_VISIT_GROUP = " mr_no, patient_id, center_id, reg_date ";

  /**
   * Gets the patient diag visits.
   *
   * @param filterParams
   *          the filter params
   * @param listing
   *          the listing
   * @return the patient diag visits
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  public static PagedList getPatientDiagVisits(Map filterParams, Map listing)
      throws SQLException, ParseException {
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      SearchQueryBuilder qb = null;
      String localPatientDiagVisitTables = PATIENT_DIAG_VISIT_TABLES;
      localPatientDiagVisitTables = localPatientDiagVisitTables.replace("#",
          "'" + ((String[]) filterParams.get("mr_no"))[0] + "'");
      qb = new SearchQueryBuilder(con, PATIENT_DIAG_VISIT_FIELDS, PATIENT_DIAG_VISIT_COUNT,
          localPatientDiagVisitTables, null, PATIENT_DIAG_VISIT_GROUP, listing);

      qb.addFilterFromParamMap(filterParams);
      qb.addSecondarySort("patient_id");
      qb.build();

      PagedList list = qb.getMappedPagedList();
      qb.close();
      return list;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /** The patient diag details fields. */
  private static final String PATIENT_DIAG_DETAILS_FIELDS = "SELECT * ";

  /** The patient diag details tables. */
  private static final String PATIENT_DIAG_DETAILS_TABLES = " FROM (SELECT tp.mr_no, tp.pat_id as "
      + " patient_id, tp.prescribed_id, tp.labno, "
      + " tp.pres_date, tp.common_order_id, CASE WHEN tp.external_report_ready  = 'Y' "
      + " THEN -1 ELSE tvr.report_id END AS report_id,"
      + " d.ddept_id, tvr.report_name, tvr.report_date, tvr.signed_off, d.test_name, "
      + " get_patient_full_name(sm.salutation, pd.patient_name, "
      + " pd.middle_name, pd.last_name) AS patient_full_name,"
      + " isr.patient_name as inc_patient_name, "
      + " CASE WHEN isr.incoming_visit_id IS NOT NULL THEN 't' "
      + " ELSE pr.visit_type END as visit_type, d.sample_needed, "
      + " CASE WHEN (dom.outsource_dest_type IS NULL "
      + " OR dom.outsource_dest_type = 'C') THEN hcmc.center_name ELSE "
      + " COALESCE(osn.outsource_name, hcmc.center_name) END as outsource_name, "
      + " coalesce(pr.center_id, isr.center_id) as center_id, "
      + " COALESCE(sc.coll_sample_no, sc.sample_sno) as sample_no, "
      + " CASE WHEN (dom.outsource_dest_type = 'C' AND tpc.conducted IN ('RAS','RBS')) "
      + " THEN tp.conducted ELSE coalesce(tpc.conducted, tp.conducted) END as conducted, "
      + " COALESCE(pr.reg_date, isr.date) as reg_date, "
      + " COALESCE (pd.patient_gender , isr.patient_gender) AS patient_gender, "
      + " COALESCE (get_patient_age(pd.dateofbirth, pd.expected_dob), isr.patient_age) AS age, "
      + " COALESCE (isr.age_unit, get_patient_age_in(pd.dateofbirth, pd.expected_dob)) AS agein, "
      // use to get current sample status throughout the centers
      + " CASE WHEN (d.sample_needed = 'y' AND sc.sample_status IS NULL) THEN 'NC' "
      + " WHEN (dom.outsource_dest_type = 'C' AND "
      + " sc.sample_transfer_status = 'P') THEN sc.sample_status "
      + " WHEN (dom.outsource_dest_type = 'C' "
      + " AND scc.sample_receive_status = 'P') THEN sc.sample_transfer_status "
      + " WHEN (dom.outsource_dest_type = 'C' "
      + " AND scc.sample_receive_status = 'R' AND scc.sample_status = 'A') THEN scc.sample_status "
      + " WHEN (dom.outsource_dest_type = 'C' AND scc.sample_receive_status = 'R') THEN 'RC' "
      + " WHEN (scol.sample_transfer_status = 'T' AND sc.sample_receive_status = 'P') "
      + " THEN scol.sample_transfer_status "
      + " WHEN (scol.sample_transfer_status = 'T' AND sc.sample_receive_status = 'R' "
      + " AND sc.sample_status != 'A') THEN 'RC' "
      + " WHEN ((dom.outsource_dest_type = 'O' OR dom.outsource_dest_type = 'IO') "
      + " AND sc.sample_transfer_status = 'P') THEN sc.sample_status "
      + " WHEN ((dom.outsource_dest_type = 'O' OR dom.outsource_dest_type = 'IO') "
      + " AND sc.sample_transfer_status IS NOT NULL) THEN sc.sample_transfer_status "
      // +" WHEN (scol.sample_transfer_status IS NULL AND sc.sample_receive_status = 'R' AND
      // sc.sample_status != 'A') THEN 'RC' "
      + " WHEN (d.sample_needed = 'n' AND sc.sample_status IS NULL) THEN 'NA' "
      + " ELSE sc.sample_status END AS sample_status, "
      + " case when coalesce(tpa.sponsor_type, tpacol.sponsor_type, 'R')='R' then "
      + " 'R' else 'S' end as patient_sponsor_type, "
      + " COALESCE('(' ||pmcol.package_name|| ')','(' ||pm.package_name|| ')') as package_name, "
      + " hcmcol.center_name as coll_center_name, "
      + " coalesce((select patient_due_amnt from diag_report_sharing_on_bill_payment "
      + " where report_id = tvr.report_id limit 1), 0) as partial_patient_due, "
      + " tp.external_report_ready, coalesce(tpcol.pat_id,tp.pat_id) as org_patient_id "
      + " FROM tests_prescribed tp"
      // + " LEFT JOIN test_visit_reports tvr USING (report_id)"
      + " JOIN diagnostics d USING (test_id)"
      + " LEFT JOIN sample_collection sc ON (sc.sample_collection_id = tp.sample_collection_id) "
      + " LEFT JOIN patient_registration pr ON (pr.patient_id = tp.pat_id) "
      + " LEFT JOIN patient_details pd ON (pd.mr_no = pr.mr_no) "
      + " LEFT JOIN salutation_master sm ON (sm.salutation_id = pd.salutation) "
      + " LEFT JOIN incoming_sample_registration isr ON (tp.pat_id = isr.incoming_visit_id) "
      + " LEFT JOIN diag_outsource_master dom ON (dom.outsource_dest_id = tp.outsource_dest_id) "
      + " LEFT JOIN tpa_master tpa ON (pr.primary_sponsor_id=tpa.tpa_id) "
      + " LEFT JOIN hospital_center_master hcm "
      + " ON (hcm.center_id = COALESCE(pr.center_id, isr.source_center_id)) "
      + " LEFT JOIN package_prescribed PP ON (pp.prescription_id = tp.package_ref) "
      + " LEFT JOIN packages pm ON (pm.package_id = pp.package_id) "
      // to get collection center details from logged in center
      + " LEFT JOIN tests_prescribed tpcol ON (tpcol.prescribed_id = tp.coll_prescribed_id) "
      + " LEFT JOIN sample_collection scol "
      + " ON (scol.sample_collection_id = tpcol.sample_collection_id) "
      + " LEFT JOIN patient_registration prcol ON (prcol.patient_id = tpcol.pat_id) "
      + " LEFT JOIN tpa_master tpacol ON (tpacol.tpa_id = prcol.primary_sponsor_id) "
      + " LEFT JOIN package_prescribed ppcol ON (ppcol.prescription_id = tpcol.package_ref) "
      + " LEFT JOIN packages pmcol ON (pmcol.package_id = ppcol.package_id) "
      + " LEFT JOIN hospital_center_master hcmcol "
      + " ON (hcmcol.center_id = COALESCE(prcol.center_id, pr.center_id)) "
      // to get current location details from logged in center
      + " LEFT JOIN tests_prescribed tpc ON (tpc.prescribed_id = tp.curr_location_presc_id) "
      + " LEFT JOIN sample_collection scc "
      + " ON (scc.sample_collection_id = tpc.sample_collection_id) "
      + " LEFT JOIN incoming_sample_registration isrc ON (tpc.pat_id = isrc.incoming_visit_id) "
      + " LEFT JOIN hospital_center_master hcmc "
      + " ON (hcmc.center_id = COALESCE(isrc.center_id, pr.center_id)) "
      + " LEFT JOIN outsource_names osn ON (osn.outsource_dest_id = dom.outsource_dest_id) "
      + " LEFT JOIN test_visit_reports tvr ON (tvr.report_id = tp.report_id) "
      + " WHERE tp.conducted IN ('P','N','C','V','S','RC','RV','RP',"
      + "'MA','TS','CC','CR','X','RAS','RBS','NRN','CRN')) as foo  ";

  /**
   * Gets the patient diag details.
   *
   * @param filterParams
   *          the filter params
   * @param visitIds
   *          the visit ids
   * @return the patient diag details
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  public static List<BasicDynaBean> getPatientDiagDetails(Map filterParams, List<String> visitIds)
      throws SQLException, ParseException {
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      SearchQueryBuilder qb = null;
      qb = new SearchQueryBuilder(con, PATIENT_DIAG_DETAILS_FIELDS, null,
          PATIENT_DIAG_DETAILS_TABLES, null, null, false, 0, 0);
      qb.addFilterFromParamMap(filterParams);
      qb.addFilter(SearchQueryBuilder.STRING, "patient_id", "IN", visitIds);
      qb.addSecondarySort("report_id");

      qb.build();
      List list = DataBaseUtil.queryToDynaList(qb.getDataStatement());
      qb.close();
      return list;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

}
