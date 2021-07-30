package com.insta.hms.outpatient.prescriptions;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The Class PatientPendingPrescriptionsDAO.
 *
 * @author krishna
 */
public class PatientPendingPrescriptionsDAO extends GenericDAO {

  /**
   * Instantiates a new patient pending prescriptions DAO.
   */
  public PatientPendingPrescriptionsDAO() {
    super("patient_prescription");
  }

  /** The Constant FIELDS. */
  private static final String FIELDS = "SELECT patient_id, mr_no,"
      + " max(patient_presc_id) as patient_presc_id ";

  /** The Constant COUNT. */
  private static final String COUNT = "SELECT count(distinct patient_id) ";

  /** The Constant FROM. */
  private static final String FROM = " FROM ( SELECT pp.patient_presc_id,"
      + " pp.consultation_id, cancel_status, dc.status, pp.status as presc_status, "
      + " visit_type, pr.mr_no, dc.doctor_name, doc.doctor_name as doctor_full_name,"
      + " doc.dept_id, date(pp.prescribed_date) as prescribed_date, presc_type,"
      + " pr.patient_id, pr.center_id, case when presc_type = 'Inv.' then  atp.ddept_id"
      + " when presc_type = 'Service' then"
      + " s.serv_dept_id::text when presc_type = 'Doctor' then d.dept_id"
      + " when presc_type='Operation' then om.dept_id end as cond_dept_id,"
      + " case when presc_type = 'Inv.' then  dd.category else '' end as category "
      + " FROM patient_prescription pp "
      + " LEFT JOIN doctor_consultation dc ON (dc.consultation_id=pp.consultation_id) "
      + " LEFT JOIN doctors doc ON (doc.doctor_id=dc.doctor_name) "
      + " LEFT JOIN department dept ON (doc.dept_id=dept.dept_id) "
      + " JOIN patient_registration pr ON (pr.patient_id=coalesce(pp.visit_id, dc.patient_id)"
      + " AND pp.presc_type IN ('Inv.', 'Service', 'Doctor', 'Operation')) "
      + " LEFT JOIN patient_test_prescriptions ptp"
      + " ON (pp.patient_presc_id=ptp.op_test_pres_id) "
      + " LEFT JOIN all_tests_pkgs_view atp ON (atp.test_id = ptp.test_id) "
      + " LEFT JOIN diagnostics_departments dd ON (dd.ddept_id=atp.ddept_id)"
      + " LEFT JOIN patient_service_prescriptions psp"
      + " ON (pp.patient_presc_id=psp.op_service_pres_id)"
      + " LEFT JOIN services s ON (s.service_id = psp.service_id) "
      + " LEFT JOIN patient_consultation_prescriptions pcp"
      + " ON (pp.patient_presc_id=pcp.prescription_id)"
      + " LEFT JOIN doctors d ON (pcp.doctor_id = d.doctor_id) "
      + " LEFT JOIN patient_operation_prescriptions pop"
      + " ON (pp.patient_presc_id=pop.prescription_id) "
      + " LEFT JOIN operation_master om ON (pop.operation_id=om.op_id)  "
      + " WHERE cancel_status is null AND coalesce(dc.status, 'A')!='U' and visit_type='o' "
      + " ) as foo ";

  /** The Constant WHERE. */
  private static final String WHERE = " WHERE presc_status IN (#presc_status#) ";

  /** The Constant GROUP_BY. */
  private static final String GROUP_BY = " mr_no, patient_id ";

  /** The Constant FIELDS_QUERY. */
  private static final String FIELDS_QUERY = "  FROM (SELECT pd.mr_no,"
      + " dc.consultation_id, pr.patient_id, dc.doctor_name, doc.dept_id,"
      + " pp.status as presc_status, pd.patient_name, pd.last_name, pd.patient_phone,"
      + " get_patient_full_name(sm.salutation, pd.patient_name,"
      + " pd.middle_name, pd.last_name) AS patient_full_name,"
      + " get_patient_age(pd.dateofbirth, pd.expected_dob) AS patient_age, "
      + " get_patient_age_in(pd.dateofbirth, pd.expected_dob) AS patient_age_in, "
      + " doc.doctor_name as doctor_full_name, "
      + " presc_type, case when pp.presc_type='Inv.' then atp.test_name "
      + " when pp.presc_type='Service' then s.service_name when pp.presc_type='Doctor'"
      + " then d.doctor_name when pp.presc_type='Operation' then"
      + " om.operation_name end as item_name, "
      + " case when pp.presc_type='Inv.' then 1 "
      + " when pp.presc_type='Service' then coalesce(qty, 1) else 1 end as qty,"
      + " date(pp.prescribed_date) as prescribed_date, "
      + " pp.patient_presc_id, dd.category, case when pp.presc_type='Inv.' then"
      + " atp.prior_auth_required "
      + " when pp.presc_type='Service' then s.prior_auth_required"
      + " when pp.presc_type='Doctor' then 'N' "
      + " when pp.presc_type='Operation'"
      + " then om.prior_auth_required end as prior_auth_required, "
      + " case when presc_type = 'Inv.' then  atp.ddept_id"
      + " when presc_type = 'Service' then s.serv_dept_id::text "
      + " when presc_type = 'Doctor' then d.dept_id when presc_type='Operation'"
      + " then om.dept_id end as cond_dept_id "
      + "   FROM patient_prescription pp "
      + "   LEFT JOIN doctor_consultation dc ON (dc.consultation_id=pp.consultation_id) "
      + "   JOIN patient_registration pr"
      + "  ON (pr.patient_id=coalesce(pp.visit_id, dc.patient_id) AND "
      + "     pp.presc_type IN ('Inv.', 'Service', 'Doctor', 'Operation')) "
      + "   JOIN patient_details pd ON (pr.mr_no=pd.mr_no) "
      + "     JOIN salutation_master sm ON (sm.salutation_id = pd.salutation) "
      + "   LEFT JOIN doctors doc ON (doc.doctor_id = dc.doctor_name) "
      + "   LEFT JOIN department dept ON (doc.dept_id=dept.dept_id) "
      + "   LEFT JOIN patient_test_prescriptions ptp"
      + " ON (pp.patient_presc_id=ptp.op_test_pres_id) "
      + "   LEFT JOIN all_tests_pkgs_view atp ON (atp.test_id = ptp.test_id) "
      + "   LEFT JOIN diagnostics_departments dd ON (dd.ddept_id=atp.ddept_id)"
      + "   LEFT JOIN patient_service_prescriptions psp"
      + " ON (pp.patient_presc_id=psp.op_service_pres_id) "
      + "   LEFT JOIN services s ON (s.service_id = psp.service_id) "
      + "   LEFT JOIN services_departments sd ON (s.serv_dept_id=sd.serv_dept_id) "
      + "   LEFT JOIN patient_consultation_prescriptions pcp"
      + " ON (pp.patient_presc_id=pcp.prescription_id) "
      + "   LEFT JOIN doctors d ON (pcp.doctor_id = d.doctor_id) "
      + "   LEFT JOIN department consdept ON (d.dept_id=consdept.dept_id) "
      + "   LEFT JOIN patient_operation_prescriptions pop"
      + " ON (pp.patient_presc_id=pop.prescription_id) "
      + "   LEFT JOIN operation_master om ON (pop.operation_id=om.op_id)  "
      + "   LEFT JOIN department opdept ON (om.dept_id=opdept.dept_id) "
      + " WHERE cancel_status is null AND coalesce(dc.status, 'A')!='U' and visit_type='o' "
      + " ) foo";

  /**
   * Gets the prescriptions.
   *
   * @param params
   *          the params
   * @param listingParams
   *          the listing params
   * @return the prescriptions
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ParseException
   *           the parse exception
   */
  public PagedList getPrescriptions(Map params, Map<LISTING, Object> listingParams)
      throws SQLException, IOException, ParseException {
    Connection con = DataBaseUtil.getConnection();
    SearchQueryBuilder sqb = null;
    PreparedStatement ps = null;
    try {
      String category = ConversionUtils.getParamValue(params, "category", "");
      String[] prescStatus = (String[]) params.get("exclude_in_qb_presc_status");
      String status = "";
      if (prescStatus != null) {
        for (int i = 0; i < prescStatus.length; i++) {
          if (prescStatus[i] == null || prescStatus[i].equals("")) {
            continue;
          }
          if (i > 0) {
            status += ", ";
          }
          status += "'" + prescStatus[i] + "'";
        }
      }
      // filter not found, fetch both(in-progress, cancelled)
      if (status.equals("")) {
        status = "'P', 'X', 'O'";
      }

      String whereCond = WHERE.replace("#presc_status#", status);

      listingParams.put(LISTING.PAGESIZE, 10);
      sqb = new SearchQueryBuilder(con, FIELDS, COUNT, FROM, whereCond, GROUP_BY, listingParams);
      sqb.addSecondarySort("patient_presc_id");
      sqb.addFilterFromParamMap(params);
      int centerId = RequestContext.getCenterId();
      if (centerId != 0) {
        sqb.addFilter(SearchQueryBuilder.INTEGER, "center_id", "=", centerId);
      }
      sqb.build();

      PagedList list = sqb.getDynaPagedList();

      // empty: return as is
      if ((list.getDtoList().isEmpty()) || (list.getTotalRecords() == 0)) {
        return list;
      }

      sqb.close();

      sqb = new SearchQueryBuilder(con, "SELECT * ", null, FIELDS_QUERY, whereCond,
          (String) listingParams.get(LISTING.SORTCOL),
          (Boolean) listingParams.get(LISTING.SORTASC), 0, 0);
      // get the actual result from fields query for primary key IN values
      sqb.addFilterFromParamMap(params);
      ArrayList visitIds = new ArrayList();
      Iterator itr = list.getDtoList().iterator();
      while (itr.hasNext()) {
        visitIds.add(((BasicDynaBean) itr.next()).get("patient_id"));
      }
      sqb.addFilter(SearchQueryBuilder.STRING, "patient_id", "IN", visitIds);
      sqb.addSecondarySort("consultation_id");
      sqb.addSecondarySort("presc_type");
      sqb.build();

      List presList = DataBaseUtil.queryToDynaList(sqb.getDataStatement());

      return new PagedList(presList, list.getTotalRecords(), list.getPageSize(),
          list.getPageNumber(), list.getCountInfo());
    } finally {
      DataBaseUtil.closeConnections(con, null);
      if (sqb != null) {
        sqb.close();
      }
    }
  }

  /** The Constant PRESCRIPTIONS. */
  private static final String PRESCRIPTIONS = " SELECT pp.*, atp.test_name as item_name,"
      + " test_remarks as item_remarks, "
      + " '' as tooth_unv_number, '' as tooth_fdi_number, 1 as service_qty "
      + " FROM patient_prescription pp "
      + " JOIN patient_test_prescriptions ptp ON (pp.patient_presc_id=ptp.op_test_pres_id) "
      + " JOIN all_tests_pkgs_view atp ON (atp.test_id = ptp.test_id) "
      + " JOIN diagnostics_departments dd ON (dd.ddept_id=atp.ddept_id) "
      + " WHERE pp.status!='X' #presc_type# AND #patid_or_consid# #category#"

      + " UNION ALL "

      + " SELECT pp.*, s.service_name as item_name, service_remarks as item_remarks, "
      + " psp.tooth_unv_number, psp.tooth_fdi_number, qty as service_qty "
      + " FROM patient_prescription pp " + "  JOIN patient_service_prescriptions psp"
      + " ON (pp.patient_presc_id=psp.op_service_pres_id) "
      + " JOIN services s ON (s.service_id = psp.service_id) "
      + " WHERE pp.status!='X' #presc_type# AND #patid_or_consid#"

      + " UNION ALL "

      + " SELECT pp.*, d.doctor_name as item_name, cons_remarks as item_remarks, "
      + " '' as tooth_unv_number, '' as tooth_fdi_number, 1 as service_qty "
      + " FROM patient_prescription pp " + "  JOIN patient_consultation_prescriptions pcp"
      + " ON (pp.patient_presc_id=pcp.prescription_id) "
      + " JOIN doctors d ON (pcp.doctor_id = d.doctor_id) "
      + " WHERE pp.status!='X' #presc_type# AND #patid_or_consid# "

      + " UNION ALL "

      + " SELECT pp.*, om.operation_name as item_name, pop.remarks as item_remarks, "
      + " '' as tooth_unv_number, '' as tooth_fdi_number, 1 as service_qty "
      + " FROM patient_prescription pp " + "  JOIN patient_operation_prescriptions pop"
      + " ON (pp.patient_presc_id=pop.prescription_id) "
      + " JOIN operation_master om ON (pop.operation_id=om.op_id) "
      + " WHERE pp.status!='X' #presc_type# AND #patid_or_consid# ";

  /**
   * Gets the prescriptions.
   *
   * @param consultationId
   *          the consultation id
   * @param patientId
   *          the patient id
   * @param prescType
   *          the presc type
   * @param category
   *          the category
   * @return the prescriptions
   * @throws SQLException
   *           the SQL exception
   */
  public List getPrescriptions(int consultationId, String patientId, String prescType,
      String category) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    prescType = prescType == null ? "" : prescType;
    category = category == null ? "" : category;

    try {
      String filter = " pp.consultation_id=?";
      if (consultationId == 0) {
        filter = " pp.visit_id=? and consultation_id is null";
      }
      String query = PRESCRIPTIONS;
      query = query.replaceAll("#patid_or_consid#", filter);

      if (!prescType.equals("")) {
        query = query.replaceAll("#presc_type#", " AND pp.presc_type=?'");
        
      } else {
        query = query.replaceAll("#presc_type#", "");
      }
      if (!category.equals("")) {
        query = query.replaceAll("#category#"," AND category=?");
      } else {
        query = query.replaceAll("#category#","");
      }
      ps = con.prepareStatement(query);
      if (!prescType.equals("")) {
        ps.setString(1, prescType);
        ps.setString(4, prescType);
        ps.setString(6, prescType);
        ps.setString(8, prescType);
      }
      if (!category.equals("")) {
        ps.setString(3, category);
      }
      if (consultationId == 0) {
        ps.setString(2, patientId);
        ps.setString(5, patientId);
        ps.setString(7, patientId);
        ps.setString(9, patientId);
      } else {
        ps.setInt(2, consultationId);
        ps.setInt(5, consultationId);
        ps.setInt(7, consultationId);
        ps.setInt(9, consultationId);

      }
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }
  
  /** The Constant GET_ITEM_QUERY. */
  private static final String GET_ITEM_QUERY = " SELECT dc.mr_no, dc.consultation_id,"
      + " case when coalesce(pp.visit_id, '')='' then dc.patient_id else"
      + " pp.visit_id end  as patient_id, doc.doctor_name as doctor_full_name, "
      + " presc_type, case when pp.presc_type='Inv.' then atp.test_name "
      + " when pp.presc_type='Service' then s.service_name when pp.presc_type='Doctor'"
      + " then d.doctor_name when pp.presc_type='Operation' then om.operation_name end"
      + " as item_name, case when pp.presc_type='Inv.' then atp.test_id "
      + " when pp.presc_type='Service' then s.service_id when pp.presc_type='Doctor'"
      + " then d.doctor_id when pp.presc_type='Operation' then om.op_id end as item_id,"
      + " case when pp.presc_type='Inv.' then ptp.test_remarks "
      + " when pp.presc_type='Service' then psp.service_remarks when pp.presc_type='Doctor'"
      + " then pcp.cons_remarks when pp.presc_type='Operation'"
      + " then pop.remarks end as item_remarks,"
      + " case when pp.presc_type='Inv.' then ptp.preauth_required "
      + " when pp.presc_type='Service' then psp.preauth_required"
      + " when pp.presc_type='Doctor' then 'N'"
      + " when pp.presc_type='Operation' then pop.preauth_required end as preauth_required, "
      + " case when pp.presc_type='Inv.' then 1 "
      + " when pp.presc_type='Service' then coalesce(qty, 1) else 1 end as qty,"
      + " date(pp.prescribed_date) as prescribed_date, "
      + " pp.patient_presc_id, coalesce(s.tooth_num_required, 'N') as tooth_num_required, "
      + " psp.tooth_unv_number, psp.tooth_fdi_number, dd.ddept_id as dept_id,"
      + " atp.ispkg as ispackage, pp.status as presc_status, pp.no_order_reason,"
      + " pp.presc_type, conducting_personnel, pp.pri_pre_auth_no, pp.pri_pre_auth_mode_id,"
      + " pp.sec_pre_auth_no, pp.sec_pre_auth_mode_id,"
      + " cancelled_by, cancelled_datetime "
      + " FROM patient_prescription pp "
      + " LEFT JOIN doctor_consultation dc ON (dc.consultation_id=pp.consultation_id AND"
      + " pp.presc_type IN ('Inv.', 'Service', 'Doctor', 'Operation') ) "
      + " LEFT JOIN doctors doc ON (doc.doctor_id = dc.doctor_name) "
      + " LEFT JOIN patient_test_prescriptions ptp"
      + " ON (pp.patient_presc_id=ptp.op_test_pres_id) "
      + " LEFT JOIN all_tests_pkgs_view atp ON (atp.test_id = ptp.test_id) "
      + " LEFT JOIN diagnostics_departments dd ON (dd.ddept_id=atp.ddept_id)"
      + " LEFT JOIN patient_service_prescriptions psp"
      + " ON (pp.patient_presc_id=psp.op_service_pres_id) "
      + " LEFT JOIN services s ON (s.service_id = psp.service_id) "
      + " LEFT JOIN patient_consultation_prescriptions pcp"
      + " ON (pp.patient_presc_id=pcp.prescription_id) "
      + " LEFT JOIN doctors d ON (pcp.doctor_id = d.doctor_id) "
      + " LEFT JOIN patient_operation_prescriptions pop"
      + " ON (pp.patient_presc_id=pop.prescription_id) "
      + " LEFT JOIN operation_master om ON (pop.operation_id=om.op_id)  ";

  /**
   * Gets the presc details.
   *
   * @param patientPrescId
   *          the patient presc id
   * @return the presc details
   * @throws SQLException
   *           the SQL exception
   */
  public BasicDynaBean getPrescDetails(int patientPrescId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(GET_ITEM_QUERY 
          + " WHERE pp.patient_presc_id=?");
      ps.setInt(1, patientPrescId);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }


}
