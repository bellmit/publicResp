package com.insta.hms.outpatient;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * The Class OutPatientDAO.
 */
public class OutPatientDAO extends GenericDAO {

  /**
   * Instantiates a new out patient DAO.
   */
  public OutPatientDAO() {
    super("outpatient_docs");
  }

  /** The Constant getAllOutpatientDocs_fields. */
  private static final String getAllOutpatientDocs_fields = "SELECT dat.template_name,"
      + " dat.template_id, " + " dat.title, (CASE WHEN dat.doc_format IS NULL THEN 'doc_fileupload'"
      + " ELSE dat.doc_format END) AS doc_format," + " pd.doc_status, d.doctor_name as doctor,"
      + " od.doc_id, dc.consultation_id, pr.mr_no, od.username, od.doc_name, "
      + " od.doc_date, dat.status, dat.specialized, dc.patient_id,"
      + " pd.content_type,dat.access_rights, pr.status as visit_status,pr.reg_date";

  /** The Constant getAllOutpatientDocs_tables. */
  private static final String getAllOutpatientDocs_tables = " FROM outpatient_docs od "
      + " JOIN patient_documents pd on od.doc_id=pd.doc_id "
      + " JOIN doctor_consultation dc on od.consultation_id=dc.consultation_id "
      + " JOIN doctors d on d.doctor_id=dc.doctor_name"
      + " JOIN patient_registration pr on dc.patient_id=pr.patient_id "
      + " LEFT OUTER JOIN doc_all_templates_view dat on pd.template_id=dat.template_id "
      + " and pd.doc_format=dat.doc_format ";

  /** The Constant OUT_PATIENT_DOCS_COUNT. */
  private static final String OUT_PATIENT_DOCS_COUNT = "SELECT count(od.doc_id) ";

  /**
   * Gets the outpatient docs.
   *
   * @param listingParams the listing params
   * @param extraParams   the extra params
   * @param specialized   the specialized
   * @return the outpatient docs
   * @throws SQLException the SQL exception
   */
  public static PagedList getOutpatientDocs(Map listingParams, Map extraParams, Boolean specialized)
      throws SQLException {

    SearchQueryBuilder qb = null;
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      qb = new SearchQueryBuilder(con, getAllOutpatientDocs_fields, OUT_PATIENT_DOCS_COUNT,
          getAllOutpatientDocs_tables, null, listingParams);
      qb.addFilter(SearchQueryBuilder.INTEGER, "od.consultation_id", "=",
          Integer.parseInt((String) extraParams.get("consultation_id")));
      qb.build();
      return qb.getDynaPagedList();
    } finally {
      if (qb != null) {
        qb.close();
      }
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /**
   * Gets the all visit documents.
   *
   * @param patientId the patient id
   * @return the all visit documents
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getAllVisitDocuments(String patientId) throws SQLException {

    SearchQueryBuilder qb = null;
    Connection con = null;
    int pageSize = 0;
    int pageNum = 0;
    List list = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      qb = new SearchQueryBuilder(con, getAllOutpatientDocs_fields, null,
          getAllOutpatientDocs_tables, null, null, null, false, pageSize, pageNum);
      qb.addFilter(SearchQueryBuilder.STRING, "dc.patient_id", "=", patientId);
      qb.build();
      PreparedStatement psData = qb.getDataStatement();
      list = DataBaseUtil.queryToDynaList(psData);
    } finally {
      if (qb != null) {
        qb.close();
      }
      DataBaseUtil.closeConnections(con, null);
    }

    return list;

  }

  /**
   * Gets the all visits documents for mr no.
   *
   * @param mrNo the mr no
   * @return the all visits documents for mr no
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getAllVisitsDocumentsForMrNo(String mrNo) throws SQLException {

    if (mrNo == null || mrNo.equals("")) {
      return null;
    }
    SearchQueryBuilder qb = null;
    Connection con = null;
    int pageSize = 0;
    int pageNum = 0;
    List list = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      qb = new SearchQueryBuilder(con, getAllOutpatientDocs_fields, null,
          getAllOutpatientDocs_tables, null, null, null, false, pageSize, pageNum);
      qb.addFilter(SearchQueryBuilder.STRING, "dc.mr_no", "=", mrNo);
      qb.build();
      PreparedStatement psData = qb.getDataStatement();
      list = DataBaseUtil.queryToDynaList(psData);
    } finally {
      if (qb != null) {
        qb.close();
      }
      DataBaseUtil.closeConnections(con, null);
    }

    return list;

  }

  /** The Constant PHARMA_MEDICINE_PRESC. */
  private static final String PHARMA_MEDICINE_PRESC = "SELECT pmp.op_medicine_pres_id,"
      + " sid.medicine_id as item_id,pmp.strength, g.generic_name, "
      + " sid.medicine_name as item_name,frequency as medicine_dosage, "
      + " mr.route_id, mr.route_name, pmp.medicine_remarks as remarks, pmp.item_strength,"
      + " (select sum(qty) from store_stock_details ssd "
      + " JOIN stores s USING(dept_id)  where ssd.medicine_id = sid.medicine_id"
      + " AND s.auto_fill_prescriptions = 't')as "
      + " qtyavl, 'Medicine' as item_type, medicine_quantity, cum.consumption_uom,"
      + " 'N' as tooth_num_required, 'item_master' as master,"
      + " if.item_form_name, sid.prior_auth_required, frequency, g.generic_code,"
      + " pmp.item_form_id, false as non_hosp_medicine, duration, duration_units,"
      + " pmp.item_strength_units, su.unit_name, cum.consumption_uom, "
      + " pmp.admin_strength, if.granular_units, sic.item_code as drug_code, pp.special_instr,"
      + " iic.insurance_category_id, iic.insurance_category_name "

      + " FROM doctor_consultation dc " + " JOIN patient_prescription pp USING (consultation_id) "
      + "   JOIN patient_medicine_prescriptions pmp"
      + " ON(pp.patient_presc_id=pmp.op_medicine_pres_id)"
      + " LEFT JOIN consumption_uom_master cum ON (cum.cons_uom_id=pmp.cons_uom_id)"
      + "   LEFT JOIN store_item_details sid ON(pmp.medicine_id = sid.medicine_id)"
      + " LEFT JOIN item_insurance_categories iic"
      + " ON (iic.insurance_category_id=sid.insurance_category_id)"
      + "   LEFT JOIN item_form_master if ON (pmp.item_form_id=if.item_form_id) "
      + "   LEFT JOIN generic_name g ON (pmp.generic_code = g.generic_code)"
      + "   LEFT JOIN medicine_route mr ON (mr.route_id=pmp.route_of_admin)"
      + "   LEFT JOIN strength_units su ON (su.unit_id=pmp.item_strength_units) "
      // drug code
      + "   LEFT JOIN ha_item_code_type hict"
      + " ON (hict.medicine_id = sid.medicine_id AND hict.health_authority= ? ) "
      + " LEFT JOIN store_item_codes sic"
      + " ON (sic.medicine_id = hict.medicine_id AND sic.code_type = hict.code_type) "

      + " WHERE dc.consultation_id = ?";

  /** The Constant OP_MEDICINE_PRESC. */
  private static final String OP_MEDICINE_PRESC = " SELECT "
      + " pomp.prescription_id AS op_medicine_pres_id, pomp.medicine_name as item_name,"
      + " '' as item_id, medicine_quantity,"
      + " frequency as medicine_dosage, frequency, strength, medicine_remarks as remarks,"
      + " pomp.consumption_uom, g.generic_name, "
      + " g.generic_code, 'op' as master, 'Medicine' as item_type, false as ispackage, "
      + " mr.route_id, mr.route_name ,'' AS category_name, "
      + " '' as prior_auth_required, coalesce(pomp.item_form_id, 0) as item_form_id, "
      + " pomp.item_strength, if.item_form_name, 0 as charge, 0 as discount, "
      + " 'N' as tooth_num_required, 0 as qtyavl, false as non_hosp_medicine, duration,"
      + " duration_units, pomp.item_strength_units, su.unit_name, pomp.consumption_uom,"
      + " pomp.admin_strength, if.granular_units, pp.special_instr, "
      + " 0 as insurance_category_id, '' as insurance_category_name"

      + " FROM doctor_consultation dc JOIN patient_prescription pp USING (consultation_id) "
      + "   JOIN patient_other_medicine_prescriptions pomp"
      + " ON(pp.patient_presc_id=pomp.prescription_id)"
      + "   LEFT JOIN prescribed_medicines_master pms"
      + " ON (pomp.medicine_name=pms.medicine_name)"
      + "   LEFT JOIN item_form_master if ON (pomp.item_form_id=if.item_form_id) "
      + "   LEFT JOIN generic_name g ON (pms.generic_name=g.generic_code)"
      + "   LEFT JOIN medicine_route mr ON (mr.route_id=pomp.route_of_admin)"
      + "   LEFT JOIN strength_units su ON (su.unit_id=pomp.item_strength_units) "
      + " WHERE dc.consultation_id = ?";

  /** The Constant ALL_PRESCRIPTIONS. */
  private static final String ALL_PRESCRIPTIONS = " SELECT d.test_name as item_name,"
      + " d.test_id as item_id, 0 as medicine_quantity, op_test_pres_id, "
      + " '' as medicine_dosage, '' as frequency, '' as strength,"
      + " test_remarks as item_remarks, pp.status as added_to_bill, '' as consumption_uom,"
      + " '' as generic_name, '' as generic_code, 'item_master' as master,"
      + " 'Inv.' as item_type, false as ispackage, activity_due_date,"
      + " mod_time, -1 as route_id, '' as route_name,'' AS category_name,"
      + " '' as manf_name, '' as manf_mnemonic, 0 as lblcount,0 as issue_base_unit,"
      + " d.prior_auth_required, 0 as item_form_id, "
      + " '' as item_strength, '' as item_form_name, coalesce(dc.charge, 0) as charge,"
      + " coalesce(dc.discount, 0) as discount, "
      + " dd.category as test_category, 'N' as tooth_num_required, '' as tooth_unv_number,"
      + " '' as tooth_fdi_number, 0 as service_qty, "
      + " '' as service_code, false as non_hosp_medicine, 0 as duration, '' as duration_units,"
      + "  0 as item_strength_units, '' as unit_name, "
      + " ptp.admin_strength, '' as granular_units, pp.special_instr, iic.insurance_category_id,"
      + " iic.insurance_category_name FROM doctor_consultation dcons "
      + " JOIN patient_prescription pp USING (consultation_id)"
      + " JOIN patient_test_prescriptions ptp ON (pp.patient_presc_id=ptp.op_test_pres_id) "
      + " JOIN diagnostics d ON (d.test_id=ptp.test_id) JOIN item_insurance_categories iic"
      + " ON (iic.insurance_category_id=d.insurance_category_id) "
      + " LEFT JOIN diagnostics_departments dd ON (dd.ddept_id=d.ddept_id) "
      + " LEFT JOIN diagnostic_charges dc"
      + " ON (dc.test_id=d.test_id and dc.org_name=? and dc.bed_type=?) "
      + " WHERE  dcons.consultation_id=? " + " UNION ALL "
      + " SELECT pm.package_name as item_name, ptp.test_id as item_id, 0 as medicine_quantity,"
      + " op_test_pres_id, '' as medicine_dosage, '' as frequency, '' as strength,"
      + " test_remarks as item_remarks, pp.status as added_to_bill, '' as consumption_uom,"
      + " '' as generic_name, '' as generic_code, 'item_master' as master, "
      + " 'Inv.' as item_type, true as ispackage, activity_due_date, mod_time,"
      + " -1 as route_id, '' as route_name,'' AS category_name, "
      + " '' as manf_name, '' as manf_mnemonic, 0 as lblcount,0 as issue_base_unit,"
      + " pm.prior_auth_required, 0 as item_form_id, "
      + " '' as item_strength, '' as item_form_name, coalesce(pc.charge, 0) as charge,"
      + " coalesce(pc.discount, 0) as discount, "
      + " null as test_category, 'N' as tooth_num_required, '' as tooth_unv_number,"
      + " '' as tooth_fdi_number, 0 as service_qty, "
      + " '' as service_code, false as non_hosp_medicine, 0 as duration, '' as duration_units,"
      + "  0 as item_strength_units, '' as unit_name, "
      + " ptp.admin_strength, '' as granular_units, pp.special_instr, iic.insurance_category_id,"
      + " iic.insurance_category_name " + " FROM doctor_consultation dcons "
      + " JOIN patient_prescription pp USING (consultation_id) "
      + " JOIN patient_test_prescriptions ptp ON (pp.patient_presc_id=ptp.op_test_pres_id) "
      + " JOIN packages pm ON (ptp.ispackage=true AND pm.package_id = ptp.test_id::integer) "
      + " JOIN item_insurance_categories iic"
      + " ON (iic.insurance_category_id=pm.insurance_category_id) "
      + " LEFT JOIN package_charges pc"
      + " ON (pc.package_id=pm.package_id and org_id=? and pc.bed_type=?) "
      + " JOIN center_package_applicability pcm ON (pcm.package_id=pm.package_id and pcm.status='A'"
      + " AND (pcm.center_id=? or pcm.center_id=-1)) "
      + " JOIN package_sponsor_master psm ON (psm.pack_id=pm.package_id AND psm.status='A'"
      + " AND (psm.tpa_id=? or psm.tpa_id='-1')) "
      + " WHERE pm.approval_status='A' AND dcons.consultation_id=? " + " UNION ALL "
      + " SELECT s.service_name as item_name, s.service_id as item_id, 0 as medicine_quantity,"
      + " op_service_pres_id, '' as medicine_dosage, '' as frequency,  '' as strength, "
      + " service_remarks as item_remarks, pp.status as added_to_bill, '' as consumption_uom,"
      + " '' as generic_name, '' as generic_code, 'item_master' as master, 'Service' as item_type,"
      + " false as ispackage, activity_due_date, mod_time, -1 as  route_id, '' as route_name,"
      + " '' AS category_name,'' as manf_name,  '' as manf_mnemonic, 0 as lblcount,"
      + " 0 as issue_base_unit, s.prior_auth_required, 0 as item_form_id, "
      + " '' as item_strength, '' as item_form_name, smc.unit_charge as charge,"
      + " smc.discount as discount, '' as test_category, s.tooth_num_required,"
      + " psp.tooth_unv_number, psp.tooth_fdi_number, qty as service_qty, service_code, "
      + " false as non_hosp_medicine, 0 as duration, '' as duration_units,"
      + "  0 as item_strength_units, '' as unit_name,"
      + "   psp.admin_strength, '' as granular_units, pp.special_instr,"
      + " iic.insurance_category_id, iic.insurance_category_name "

      + " FROM doctor_consultation dc" + " JOIN patient_prescription pp USING (consultation_id)"
      + " JOIN patient_service_prescriptions psp"
      + " ON (pp.patient_presc_id=psp.op_service_pres_id) "
      + " JOIN services s ON (s.service_id = psp.service_id) "
      + " JOIN item_insurance_categories iic"
      + " ON (iic.insurance_category_id=s.insurance_category_id) "
      + "   JOIN service_master_charges smc"
      + " ON (smc.service_id=s.service_id and org_id=? and bed_type=?)"
      + " WHERE dc.consultation_id=? "

      + " UNION ALL " + " SELECT d.doctor_name as item_name, d.doctor_id, 0 as medicine_quantity,"
      + " pcp.prescription_id, '' as medicine_dosage, '' as frequency,  '' as strength,"
      + " cons_remarks as item_remarks, pp.status as added_to_bill, '' as consumption_uom,"
      + " '', '', 'item_master' as master, 'Doctor' as item_type, false as ispackage, "
      + " activity_due_date, pcp.mod_time, -1 as route_id, '' as route_name,"
      + "'' AS category_name,'' as manf_name, '' as manf_mnemonic, "
      + " 0 as lblcount,0 as issue_base_unit, '' as prior_auth_required,"
      + " 0 as item_form_id, '' as item_strength, '' as item_form_name, "
      + " cc.charge + docc.op_charge AS charge, cc.discount + docc.op_charge_discount AS discount,"
      + " '' as test_category, 'N' as tooth_num_required, '' as tooth_unv_number, "
      + " '' as tooth_fdi_number, 0 as service_qty, '' as service_code,"
      + " false as non_hosp_medicine, 0 as duration, '' as duration_units, "
      + " 0 as item_strength_units, '' as unit_name, pcp.admin_strength,"
      + " '' as granular_units, pp.special_instr, "
      + " iic.insurance_category_id, iic.insurance_category_name "

      + " FROM doctor_consultation dc " + " JOIN patient_prescription pp USING (consultation_id) "
      + " JOIN patient_consultation_prescriptions pcp"
      + " ON(pp.patient_presc_id=pcp.prescription_id)"
      + "   JOIN doctors d ON (pcp.doctor_id = d.doctor_id) " + " JOIN consultation_charges cc"
      + " ON (cc.org_id=? AND cc.bed_type = ? AND cc.consultation_type_id = -1) "
      + " JOIN doctor_op_consultation_charge docc"
      + " ON (docc.doctor_id=d.doctor_id AND docc.org_id=cc.org_id) "
      + " JOIN consultation_types ct ON (ct.consultation_type_id=-1) "
      + " JOIN item_insurance_categories iic"
      + " ON (iic.insurance_category_id=ct.insurance_category_id) " + " WHERE dc.consultation_id=? "
      + " UNION ALL SELECT item_name, '' as item_id, medicine_quantity, prescription_id,"
      + " frequency as medicine_dosage, frequency, strength,  "
      + " item_remarks as remarks, 'P' as issued, pop.consumption_uom, '' as generic_name,"
      + " '' as generic_code, 'op' as master, "
      + " CASE WHEN non_hosp_medicine THEN 'Medicine' ELSE 'NonHospital' END as item_type,"
      + " false as ispackage, activity_due_date, pop.mod_time, -1 as route_id,"
      + " '' as route_name,'' AS category_name,'' as manf_name, '' as manf_mnemonic , "
      + " 0 as lblcount,0 as issue_base_unit, '' as prior_auth_required, pop.item_form_id,"
      + " pop.item_strength, if.item_form_name, 0 as charge, 0 as discount,"
      + "  '' as test_category, 'N' as tooth_num_required, '' as tooth_unv_number, "
      + " '' as tooth_fdi_number, 0 as service_qty, '' as service_code, non_hosp_medicine,"
      + " duration, duration_units, pop.item_strength_units, '' as unit_name,"
      + " pop.admin_strength, if.granular_units, pp.special_instr, "
      + " null as insurance_category_id, '' as insurance_category_name "

      + " FROM doctor_consultation dc  JOIN patient_prescription pp USING (consultation_id) "
      + " JOIN patient_other_prescriptions pop" + " ON (pp.patient_presc_id=pop.prescription_id) "
      + " LEFT JOIN item_form_master if ON (pop.item_form_id=if.item_form_id)"
      + " WHERE dc.consultation_id=? "

      + " UNION ALL "
      + " SELECT om.operation_name as item_name, om.op_id as item_id, 0 as medicine_quantity,"
      + " prescription_id, '' as medicine_dosage, '' as frequency,  '' as strength, "
      + " pop.remarks as item_remarks, pp.status as added_to_bill, '' as consumption_uom,"
      + " '' as generic_name, '' as generic_code, 'item_master' as master,"
      + " 'Operation' as item_type, false as ispackage, null::date as activity_due_date,"
      + " mod_time, -1 as route_id, '' as route_name, '' AS category_name,'' as manf_name,"
      + " '' as manf_mnemonic,0 as lblcount,0 as issue_base_unit, om.prior_auth_required, "
      + " 0 as item_form_id, '' as item_strength, '' as item_form_name,"
      + " surg_asstance_charge as charge, surg_asst_discount as discount, '' as test_category,"
      + " 'N' as tooth_num_required, '' as tooth_unv_number, '' as tooth_fdi_number,"
      + " 0 as service_qty, '' as service_code, false as non_hosp_medicine, 0 as duration,"
      + " '' as duration_units, 0 as item_strength_units, '' as unit_name, pop.admin_strength,"
      + " '' as granular_units, pp.special_instr, "
      + " iic.insurance_category_id, iic.insurance_category_name " + " FROM doctor_consultation dc "
      + " JOIN patient_prescription pp USING (consultation_id)"
      + " JOIN patient_operation_prescriptions pop"
      + " ON (pp.patient_presc_id=pop.prescription_id) "
      + " JOIN operation_master om ON (pop.operation_id=om.op_id) "
      + " JOIN item_insurance_categories iic"
      + " ON (iic.insurance_category_id=om.insurance_category_id)"
      + " JOIN operation_charges oc ON (oc.op_id=pop.operation_id and org_id=? and bed_type=?)"
      + " WHERE dc.consultation_id=? "

      + " ORDER BY item_type ";

  /**
   * Gets the patient previous prescriptions list.
   *
   * @param mrNo                  the mr no
   * @param consid                the consid
   * @param doctorId              the doctor id
   * @param patientId             the patient id
   * @param patientHealthAutority the patient health autority
   * @return the patient previous prescriptions list
   * @throws SQLException   the SQL exception
   * @throws ParseException the parse exception
   */
  public static List<BasicDynaBean> getPatientPreviousPrescriptionsList(String mrNo, int consid,
      String doctorId, String patientId, String patientHealthAutority)
      throws SQLException, ParseException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement pstmt = null;
    PreparedStatement pstmt2 = null;
    SearchQueryBuilder qb = null;
    try {
      BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
      String useStoreItems = (String) genericPrefs.get("prescription_uses_stores");
      pstmt = con
          .prepareStatement(useStoreItems.equals("Y") ? PHARMA_MEDICINE_PRESC : OP_MEDICINE_PRESC);
      int index = 1;
      if (useStoreItems.equals("Y")) {
        pstmt.setString(index++, patientHealthAutority);
      }
      pstmt.setInt(index++, consid);

      pstmt2 = con.prepareStatement(ALL_PRESCRIPTIONS);
      Map patDetMap = VisitDetailsDAO.getPatientVisitDetailsBean(patientId).getMap();

      String orgId = (String) patDetMap.get("org_id");
      orgId = orgId == null || orgId.equals("") ? "GENERAL" : orgId;
      String tpaId = (String) patDetMap.get("primary_sponsor_id");
      tpaId = tpaId == null || tpaId.equals("") ? "-1" : tpaId;
      int centerId = (Integer) patDetMap.get("center_id");
      if (centerId == 0) {
        centerId = -1;
      }
      String bedType = (String) patDetMap.get("alloc_bed_type");
      bedType = bedType == null || bedType.equals("") ? (String) patDetMap.get("bill_bed_type")
          : bedType;
      bedType = bedType == null || bedType.equals("") ? "GENERAL" : bedType;

      // test related place holders
      int idx = 1;
      pstmt2.setString(idx++, orgId);
      pstmt2.setString(idx++, bedType);
      pstmt2.setInt(idx++, consid);
      pstmt2.setString(idx++, orgId);
      pstmt2.setString(idx++, bedType);
      pstmt2.setInt(idx++, centerId);
      pstmt2.setString(idx++, tpaId);
      pstmt2.setInt(idx++, consid);

      // service related place holders
      pstmt2.setString(idx++, orgId);
      pstmt2.setString(idx++, bedType);
      pstmt2.setInt(idx++, consid);

      pstmt2.setString(idx++, orgId);
      pstmt2.setString(idx++, bedType);
      pstmt2.setInt(idx++, consid);

      pstmt2.setInt(idx++, consid);

      pstmt2.setString(idx++, orgId);
      pstmt2.setString(idx++, bedType);
      pstmt2.setInt(idx++, consid);

      List<BasicDynaBean> list = DataBaseUtil.queryToDynaList(pstmt);
      List<BasicDynaBean> allPrescList = DataBaseUtil.queryToDynaList(pstmt2);
      list.addAll(allPrescList);

      return list;

    } finally {
      if (pstmt != null) {
        pstmt.close();
      }
      if (pstmt2 != null) {
        pstmt2.close();
      }
      DataBaseUtil.closeConnections(con, null);
      if (qb != null) {
        qb.close();
      }
    }

  }

  /** The Constant CONSULTATION_IDS. */
  private static final String CONSULTATION_IDS = "SELECT * FROM ("
      + " SELECT consultation_id, CASE WHEN cancel_status IS null"
      + " THEN '' ELSE cancel_status END AS cancel_state"
      + " FROM doctor_consultation WHERE mr_no = ? AND consultation_id != ?  ) "
      + " as foo WHERE cancel_state != 'C' ORDER BY consultation_id DESC";

  /**
   * Gets the consultaion ids.
   *
   * @param mrNo   the mr no
   * @param consId the cons id
   * @return the consultaion ids
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getConsultaionIds(String mrNo, int consId) throws SQLException {
    Connection con = null;
    PreparedStatement pstmt = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      pstmt = con.prepareStatement(CONSULTATION_IDS);
      pstmt.setString(1, mrNo);
      pstmt.setInt(2, consId);
      return DataBaseUtil.queryToDynaList(pstmt);
    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }
  }

  /** The Constant DOCTOR_CONSULTATION_DETAILS. */
  private static final String DOCTOR_CONSULTATION_DETAILS = "SELECT d.doctor_name,"
      + " to_char(dc.visited_date::date, 'dd-MM-yyyy')AS consultation_date, "
      + " d.doctor_name, md.description as diagnosis" + " FROM doctor_consultation dc"
      + " JOIN doctors d ON(d.doctor_id = dc.doctor_name)"
      + " LEFT JOIN mrd_diagnosis md ON(dc.patient_id=md.visit_id and diag_type='P')"
      + " WHERE dc.consultation_id = ?";

  /**
   * Gets the doctor consultation map.
   *
   * @param consultationId the consultation id
   * @return the doctor consultation map
   * @throws SQLException the SQL exception
   */
  public static Map getDoctorConsultationMap(int consultationId) throws SQLException {
    Connection con = null;
    PreparedStatement pstmt = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      pstmt = con.prepareStatement(DOCTOR_CONSULTATION_DETAILS);
      pstmt.setInt(1, consultationId);
      return (Map) ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(pstmt))
          .get(0);
    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }
  }

}
