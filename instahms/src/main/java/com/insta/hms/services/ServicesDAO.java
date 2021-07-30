package com.insta.hms.services;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.billing.BillActivityCharge;
import com.insta.hms.billing.BillActivityChargeDAO;
import com.insta.hms.billing.ChargeBO;
import com.insta.hms.billing.ChargeDAO;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.common.SplitSearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class ServicesDAO.
 *
 * @author krishna.t
 */
public class ServicesDAO extends GenericDAO {

  /**
   * Instantiates a new services DAO.
   */
  public ServicesDAO() {
    super("services_prescribed");
  }

  /** The Constant SERVICES_SEARCH. */
  // Search query only on filterable/sortable fields
  private static final String SERVICES_SEARCH = " SELECT sp.mr_no, sp.patient_id, "
      + "  sp.presc_date, s.serv_dept_id, s.service_name, "
      + "  sp.conducted, pr.visit_type, pr.center_id, "
      + "  coalesce(sd.signed_off, false) as signed_off, " + "  sp.prescription_id, "
      + "sp.conducteddate "
      + " FROM services_prescribed sp " + "  JOIN services s on sp.service_id = s.service_id "
      + "  LEFT JOIN service_documents sd ON (sp.prescription_id=sd.prescription_id) "
      + "  JOIN patient_registration pr ON (pr.patient_id = sp.patient_id) ";

  /** The Constant CONDUCTING_DOCTOR. */
  private static final String CONDUCTING_DOCTOR = " SELECT sp.conductedby "
      + " FROM services_prescribed sp " + " LEFT JOIN doctors d on d.doctor_id=sp.conductedby "
      + " where sp.prescription_id=?";

  /**
   * Gets the conducting doctor id.
   *
   * @param prescriptionId
   *          the prescription id
   * @return the conducting doctor id
   * @throws SQLException
   *           the SQL exception
   */
  public static BasicDynaBean getConductingDoctorId(int prescriptionId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(CONDUCTING_DOCTOR);
      ps.setInt(1, prescriptionId);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /*
   * Fields query for all displayable fields. Since this does a join on bill_activity_charge, we
   * don't want to use this as the search query (takes too long).
   */

  /** The Constant SERVICES_FIELDS. */
  private static final String SERVICES_FIELDS = " SELECT sp.mr_no, sp.patient_id, "
      + " sp.service_id, sp.remarks, sp.prescription_id, "
      + " get_patient_full_name(sm.salutation, pd.patient_name, pd.middle_name, pd.last_name) "
      + " as patient_full_name, pd.patient_name, pd.last_name, sm.salutation, "
      + " s.conduction_applicable, "
      + " b.bill_type, b.payment_status, b.visit_type, pr.op_type, otn.op_type_name, "
      + " bc.charge_group, sp.presc_date as pres_date, s.service_name, sp.conducted, "
      + " pr.reg_date, bac.payment_charge_head as charge_head, sd.doc_name as report_name, "
      + " sd.username as report_generated_user, sd.doc_id, "
      + " coalesce(sd.signed_off, false) as signed_off, "
      + " sdept.serv_dept_id as service_department, sp.doctor_id as pres_doctor_id, "
      + " sp.conductedby as cond_doctor_id, "
      + " coalesce(cd.doctor_name, cht.tech_name) as cond_doctor_name, "
      + " coalesce(presdoc.doctor_name, pht.tech_name) as pres_doctor_name, "
      + " sd.username as report_user_name, sp.conducteddate, pr.center_id, "
      + " sp.tooth_unv_number, sp.tooth_fdi_number, sp.conducteddate "
          + " FROM services_prescribed sp "
      + " LEFT JOIN bill_activity_charge bac on (bac.activity_id=sp.prescription_id::varchar) "
      + " AND bac.activity_code = 'SER' "
      + " LEFT JOIN bill_charge bc on  bc.charge_id=bac.charge_id "
      + " LEFT JOIN bill b on b.bill_no=bc.bill_no "
      + " LEFT JOIN service_documents sd USING (prescription_id) "
      + " LEFT JOIN doctors cd on (sp.conductedby=cd.doctor_id) "
      + " LEFT JOIN hospital_technical cht on (sp.conductedby=cht.tech_id) "
      + " LEFT JOIN doctors presdoc on (sp.doctor_id=presdoc.doctor_id) "
      + " LEFT JOIN hospital_technical pht on (sp.doctor_id=pht.tech_id) "
      + " JOIN services s on sp.service_id = s.service_id "
      + " JOIN services_departments sdept ON (sdept.serv_dept_id=s.serv_dept_id) "
      + " JOIN patient_registration pr  on (sp.patient_id=pr.patient_id) "
      + " LEFT JOIN op_type_names otn ON (otn.op_type = pr.op_type) "
      + " JOIN patient_details pd  on (sp.mr_no=pd.mr_no AND "
      + " ( patient_confidentiality_check(COALESCE(pd.patient_group, 0),pd.mr_no) )) "
      + " JOIN salutation_master sm on (sm.salutation_id = pd.salutation) ";

  /**
   * Search pending services.: true retrieves the pending services false retrieves the conduction
   * completed
   *
   * @param reqParams the req params
   * @param listingParams the listing params
   * @param pendingList the pending list
   * @return the paged list
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   */
  public static PagedList searchPendingServices(Map reqParams, Map listingParams,
      boolean pendingList) throws SQLException, ParseException {
    Connection con = DataBaseUtil.getConnection();
    SplitSearchQueryBuilder sb = null;
    try {
      String conductionStatus = "";
      List<String> signedOffList = new ArrayList<>();
      if (reqParams.get("signed_off") != null) {
        signedOffList = Arrays.asList((String[]) reqParams.get("signed_off"));
      }
      if (pendingList) {
        conductionStatus =
            "  WHERE conducted in ('N','P','R') and " + " coalesce(signed_off, false)=false ";
      } else {
        conductionStatus = " WHERE conducted in ('C') ";
        if (!signedOffList.isEmpty()) {
          conductionStatus = null;
          if (signedOffList.contains("")) {
            conductionStatus = " WHERE (conducted = 'C' OR signed_off = true) ";
            reqParams.remove("signed_off");
          } else {
            if (signedOffList.contains("C")) {
              conductionStatus = " WHERE conducted = 'C' ";
            }
            String[] signedOffValues = new String[1];
            signedOffValues[0] = "false";
            if (signedOffList.contains("S")) {
              signedOffValues[0] = "true";
            }
            reqParams.remove("signed_off");
            reqParams.put("signed_off", signedOffValues);
          }
        }
      }
      sb = new SplitSearchQueryBuilder(con, SERVICES_SEARCH, SERVICES_FIELDS, conductionStatus,
          "prescription_id", listingParams);
      sb.addFilterFromParamMap(reqParams);
      int centerId = RequestContext.getCenterId();
      if (centerId != 0) {
        sb.addFilter(SearchQueryBuilder.INTEGER, "center_id", "=", centerId);
      }
      sb.addSecondarySort("prescription_id");
      sb.build();
      return sb.getMappedPagedList();

    } finally {
      if (sb != null) {
        sb.close();
      }
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /** The Constant SIGNED_OFF_SERVICE_REPORTS. */
  public static final String SIGNED_OFF_SERVICE_REPORTS = " SELECT sd.doc_id, "
      + " sd.doc_name as report_name, coalesce(presdoc.doctor_name, "
      + " pht.tech_name) as pres_doctor_name, pdoc.doc_location, "
      + " dat.access_rights, sd.username as report_user_name, "
      + " (CASE WHEN pdoc.doc_format='doc_link' THEN 'doc_link' "
      + " WHEN dat.doc_format IS NULL THEN 'doc_fileupload' "
      + " ELSE dat.doc_format END) AS doc_format, "
      + " content_type, sd.doc_date, sp.patient_id, sp.mr_no, "
      + " coalesce(sd.signed_off, false) as signed_off, pdoc.content_type,pr.reg_date "
      + " FROM services_prescribed sp "
      + " JOIN patient_registration pr ON(sp.patient_id = pr.patient_id) "
      + " JOIN service_documents sd ON (sp.prescription_id=sd.prescription_id) "
      + " JOIN patient_documents pdoc on sd.doc_id=pdoc.doc_id "
      + " LEFT OUTER JOIN doc_all_templates_view dat ON "
      + " (pdoc.template_id=dat.template_id AND pdoc.doc_format=dat.doc_format) "
      + " LEFT JOIN doctors presdoc on (sp.doctor_id=presdoc.doctor_id) "
      + " LEFT JOIN hospital_technical pht on (sp.doctor_id=pht.tech_id) "
      + " WHERE signed_off=true ";

  /**
   * Gets the signed off service reports.
   *
   * @param patientId
   *          the patient id
   * @param mrNo
   *          the mr no
   * @param allVisitsDocs
   *          the all visits docs
   * @return the signed off service reports
   * @throws SQLException
   *           the SQL exception
   */
  public static List getSignedOffServiceReports(String patientId, String mrNo,
      boolean allVisitsDocs) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      if (allVisitsDocs) {
        ps = con.prepareStatement(SIGNED_OFF_SERVICE_REPORTS + " AND pr.mr_no=?");
        ps.setString(1, mrNo);
      } else {
        ps = con.prepareStatement(SIGNED_OFF_SERVICE_REPORTS + " AND pr.patient_id=?");
        ps.setString(1, patientId);
      }
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant COMPLETED_SERVICE_REPORTS. */
  public static final String COMPLETED_SERVICE_REPORTS = " SELECT sp.prescription_id, "
      + " coalesce(presdoc.doctor_name, pht.tech_name) as pres_doctor_name ,"
      + " sp.conducteddate ::date as conducted_date,sp.patient_id, sp.mr_no , "
      + " sp.user_name, s.service_name,pr.reg_date "
      + " FROM services_prescribed  sp " 
      + " JOIN services s on (sp.service_id=s.service_id) "
      + " JOIN patient_registration pr ON(sp.patient_id = pr.patient_id)"
      + " LEFT JOIN doctors presdoc on(sp.doctor_id=presdoc.doctor_id) "
      + " LEFT JOIN hospital_technical pht on(sp.doctor_id=pht.tech_id) "
      + " where sp.conducted='C' ";

  /**
   * Gets the completed service reports.
   *
   * @param patientId
   *          the patient id
   * @param mrNo
   *          the mr no
   * @param allVisitsDocs
   *          the all visits docs
   * @return the completed service reports
   * @throws SQLException
   *           the SQL exception
   */
  public static List getCompletedServiceReports(String patientId, String mrNo,
      boolean allVisitsDocs) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      if (allVisitsDocs) {
        ps = con.prepareStatement(COMPLETED_SERVICE_REPORTS + " AND pr.mr_no=?");
        ps.setString(1, mrNo);
      } else {
        ps = con.prepareStatement(COMPLETED_SERVICE_REPORTS + " AND pr.patient_id=?");
        ps.setString(1, patientId);
      }
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant SERVICE_FIELDS. */
  private static final String SERVICE_FIELDS = "SELECT sp.mr_no, sp.patient_id, sp.service_id,"
      + " sp.presc_date, sp.doctor_id, sp.conducted, "
      + " COALESCE(sp.conductedby, (case when bc.charge_group='PKG' "
      + " then null else bc.payee_doctor_id end))AS conductedby,"
      + " sp.conducteddate, sp.conducted_end_date, sp.quantity, sp.comments, sp.prescription_id, "
      + " sp. user_name, sp.remarks, sp.operation_ref,"
      + " sp.service_type, sp.report_id, sp.docid, sp.doc_content, sp.cancelled_by, "
      + " sp.cancel_date, sp.common_order_id, "
      + " sp.stock_reduced, sp.specialization, sp.package_ref, sp.appointment_id, "
      + " sp.doc_presc_id, sp.tooth_unv_number," + " sp.tooth_fdi_number,"
      + " s.service_name,s.serv_dept_id, sd.department, s.units, srm.store_id::text,"
      + " coalesce(dr.doctor_name, ht.tech_name,"
      + " sp.conductedby, bcd.doctor_name) as conducting_doctor, sdoc.doc_id, "
      + " (CASE WHEN dat.doc_format IS NULL THEN 'doc_fileupload' "
      + " ELSE dat.doc_format END) AS doc_format, "
      + " sdoc.doc_name, dat.template_id, s.conducting_doc_mandatory,"
      + " pdr.doctor_name as prescribing_doctor,s.conducting_role_id  "
      + " FROM services_prescribed sp " + "   JOIN services s USING (service_id) "
      + " JOIN services_departments sd using (serv_dept_id) "
      + " LEFT JOIN service_documents sdoc ON (sp.prescription_id=sdoc.prescription_id)"
      + " LEFT JOIN store_reagent_usage_main srm ON (srm.ref_no = sp.prescription_id "
      + " and s.service_id=srm.consumer_id)"
      + " LEFT JOIN patient_documents pd on sdoc.doc_id=pd.doc_id "
      + " LEFT OUTER JOIN doc_all_templates_view dat ON  "
      + " (pd.template_id=dat.template_id AND pd.doc_format=dat.doc_format) "
      + " LEFT JOIN doc_type dt ON (dt.doc_type_id = pd.doc_type) "
      + " LEFT JOIN doctors dr on sp.conductedby = dr.doctor_id "
      + " LEFT JOIN hospital_technical ht on (sp.conductedby = ht.tech_id)"
      + " LEFT JOIN doctors pdr on sp.doctor_id = pdr.doctor_id "
      + " LEFT JOIN bill_activity_charge bac ON "
      + " (bac.activity_id = sp.prescription_id::varchar AND bac.activity_code = 'SER')"
      + " LEFT JOIN bill_charge bc ON (bac.charge_id = bc.charge_id)"
      + " LEFT JOIN doctors bcd ON (bcd.doctor_id = bac.doctor_id)" + " WHERE sp.prescription_id=?";

  /**
   * Gets the service details.
   *
   * @param prescribedId
   *          the prescribed id
   * @return the service details
   * @throws SQLException
   *           the SQL exception
   */
  public static BasicDynaBean getServiceDetails(int prescribedId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(SERVICE_FIELDS);
      ps.setInt(1, prescribedId);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant SERVICE_CONDUCTED_FIELDS. */
  private static final String SERVICE_CONDUCTED_FIELDS =
      "SELECT sp.*, s.service_name, sp.conducted_end_date, "
      + " sd.department, s.units, "
      + " coalesce(dr.doctor_name, ht.tech_name) as conducting_doctor, sdoc.doc_id, "
      + " (CASE WHEN dat.doc_format IS NULL THEN 'doc_fileupload' "
      + " ELSE dat.doc_format END) AS doc_format, "
      + " sdoc.doc_name, dat.template_id, s.conducting_doc_mandatory "
      + " FROM services_prescribed sp " + " JOIN services s USING (service_id) "
      + " JOIN services_departments sd using (serv_dept_id) "
      + " LEFT JOIN service_documents sdoc ON (sp.prescription_id=sdoc.prescription_id)"
      + " LEFT JOIN patient_documents pd on sdoc.doc_id=pd.doc_id "
      + " LEFT OUTER JOIN doc_all_templates_view dat ON (pd.template_id=dat.template_id "
      + " AND pd.doc_format=dat.doc_format) "
      + " LEFT JOIN doc_type dt ON (dt.doc_type_id = pd.doc_type) "
      + " LEFT JOIN doctors dr on sp.conductedby = dr.doctor_id "
      + " LEFT JOIN hospital_technical ht on (sp.conductedby = ht.tech_id)"
      + " WHERE sp.patient_id=? and sp.conducted IN ('P','R','C')";

  /**
   * Gets the service details.
   *
   * @param patientId
   *          the patient id
   * @return the service details
   * @throws SQLException
   *           the SQL exception
   */
  public static List getServiceDetails(String patientId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(SERVICE_CONDUCTED_FIELDS);
      ps.setString(1, patientId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant ALL_DOCTORS_AND_TECHNICIANS_LIST. */
  private static final String ALL_DOCTORS_AND_TECHNICIANS_LIST = " SELECT "
      + " d.doctor_name as doctor_name, d.doctor_id FROM doctors d "
      + " JOIN doctor_center_master dcm ON(d.doctor_id=dcm.doctor_id)"
      + " WHERE d.status='A' AND dcm.status='A' " + " UNION ALL "
      + " SELECT h.tech_name as name, h.tech_id FROM hospital_technical h";

  /** The Constant ALL_DOCTORS_AND_TECHNICIANS_LIST_CENTERWISE. */
  private static final String ALL_DOCTORS_AND_TECHNICIANS_LIST_CENTERWISE = " SELECT "
      + " d.doctor_name " + " as doctor_name, d.doctor_id FROM doctors d "
      + " JOIN doctor_center_master dcm ON(d.doctor_id=dcm.doctor_id)"
      + " WHERE d.status='A' AND dcm.status='A' AND (dcm.center_id=0 OR dcm.center_id=?) "
      + " UNION ALL " + " SELECT h.tech_name as name, h.tech_id FROM hospital_technical h";

  /**
   * Gets the doctors and techs list.
   *
   * @return the doctors and techs list
   * @throws SQLException
   *           the SQL exception
   */
  public static List getDoctorsAndTechsList() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    int centerID = RequestContext.getCenterId();
    try {
      con = DataBaseUtil.getConnection();
      if (centerID == 0) {
        ps = con.prepareStatement(ALL_DOCTORS_AND_TECHNICIANS_LIST);
      } else {
        ps = con.prepareStatement(ALL_DOCTORS_AND_TECHNICIANS_LIST_CENTERWISE);
        ps.setInt(1, centerID);
      }
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      if (ps != null) {
        ps.close();
      }
      if (con != null) {
        con.close();
      }
    }
  }

  /** The Constant SERVICE_CONSUMABLES. */
  public static final String SERVICE_CONSUMABLES = " SELECT s.service_name,"
      + " sc.service_id, sc.consumable_id, "
      + " (case when sc.status='A' then 'true' else 'false' end ) " + " as status,i.medicine_name, "
      + " sc.quantity_needed as qty, 0 as usage_no, 0 as ref_no, '' as store_id, "
      + " COALESCE(i.issue_units,'') AS issue_units, i.package_type "
      + " FROM  service_consumables sc" + " JOIN services s using(service_id)"
      + " JOIN store_item_details i on(sc.consumable_id = i.medicine_id) "
      + " WHERE  sc.service_id = ? order by i.medicine_name";

  /**
   * Gets the service consumables.
   *
   * @param serviceId
   *          the service id
   * @return the service consumables
   * @throws SQLException
   *           the SQL exception
   */
  public static List<BasicDynaBean> getServiceConsumables(String serviceId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(SERVICE_CONSUMABLES);
      ps.setString(1, serviceId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant SERVCIE_CONSUMABLES_USED. */
  private static final String SERVCIE_CONSUMABLES_USED = " SELECT "
      + " consumable_id, prescription_id,service_id, usage_no,qty, i.medicine_name, "
      + " 'true' as status, "
      + " s.service_name, scu.store_id::text, COALESCE(i.issue_units,'') AS issue_units, "
      + " i.package_type " + " FROM service_consumable_usage  scu "
      + " JOIN store_item_details i ON(i.medicine_id = scu.consumable_id) "
      + " JOIN services s using (service_id)"
      + " WHERE prescription_id = ? order by i.medicine_name";

  /**
   * Gets the service consumables used.
   *
   * @param prescriptionId
   *          the prescription id
   * @return the service consumables used
   * @throws SQLException
   *           the SQL exception
   */
  public static List<BasicDynaBean> getServiceConsumablesUsed(int prescriptionId)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(SERVCIE_CONSUMABLES_USED);
      ps.setInt(1, prescriptionId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant SERVCIE_CONSUMABLES_REAGENTS_USED. */
  private static final String SERVCIE_CONSUMABLES_REAGENTS_USED = " SELECT sru.* "
      + " FROM store_reagent_usage_details sru "
      + " JOIN store_reagent_usage_main srm using(reagent_usage_seq) "
      + " WHERE srm.ref_no = ?  and reagent_type = 'S' ";

  /**
   * Gets the service consumable reagents used.
   *
   * @param prescriptionId
   *          the prescription id
   * @return the service consumable reagents used
   * @throws SQLException
   *           the SQL exception
   */
  public static List<BasicDynaBean> getServiceConsumableReagentsUsed(int prescriptionId)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(SERVCIE_CONSUMABLES_REAGENTS_USED);
      ps.setInt(1, prescriptionId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Cancle service with out bill refund.
   *
   * @param con
   *          the con
   * @param prescriptionId
   *          the prescription id
   * @param chargeId
   *          the charge id
   * @return true, if successful
   * @throws Exception
   *           the exception
   */
  public boolean cancelServiceWithOutBillRefund(Connection con, int prescriptionId, String chargeId)
      throws Exception {
    boolean status = true;
    BasicDynaBean chargeDTO = null;
    if (new ServicesDAO().cancelServicesPrescribed(con, prescriptionId)) {
      status = new ChargeDAO(con).updateHasActivityStatus(chargeId, false);
      BillActivityCharge billActivityDto = new BillActivityCharge();
      billActivityDto.setActivityId(prescriptionId);
      billActivityDto.setActivityCode("SER");
      billActivityDto.setChargeId(chargeId);
      if (status) {
        status = new BillActivityChargeDAO(con).deleteActivity(billActivityDto);
      }
      // canceling service tax
      List<BasicDynaBean> chargeRefs = new GenericDAO("bill_charge").listAll(null, "charge_ref",
          chargeId);
      for (int i = 0; i < chargeRefs.size(); i++) {
        chargeDTO = chargeRefs.get(i);
        if (new ChargeBO().isCancellable((String) chargeDTO.get("charge_id"))) {
          status = new ChargeDAO(con).updateHasActivityStatus((String) chargeDTO.get("charge_id"),
              false);
        }
      }
    }
    return status;
  }

  /** The Constant serviceCancellation. */
  public static final String serviceCancellation = "UPDATE SERVICES_PRESCRIBED "
      + " SET CONDUCTED='X' WHERE PRESCRIPTION_ID=?";

  /**
   * Cancel services prescribed.
   *
   * @param con
   *          the con
   * @param id
   *          the id
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean cancelServicesPrescribed(Connection con, int id) throws SQLException {
    boolean servicesstatus = true;
    PreparedStatement ps = con.prepareStatement(serviceCancellation);
    ps.setInt(1, id);
    if (ps.executeUpdate() <= 0) {
      servicesstatus = false;
    }
    ps.close();
    return servicesstatus;
  }

  /** The Constant UPDATE_SERVICE_VISIT. */
  /*
   * Update the visit ID of a service prescribed, restricted to the bill number which is being moved
   * from one visit to another. Used in OP IP conversion.
   */
  public static final String UPDATE_SERVICE_VISIT = "UPDATE services_prescribed "
      + " s SET patient_id=? " + " FROM bill_activity_charge bac, bill_charge bc "
      + " WHERE (bac.activity_id = s.prescription_id::text AND activity_code = 'SER') "
      + " AND (bac.charge_id = bc.charge_id) " + "  AND bill_no=?";

  /**
   * Update visit id.
   *
   * @param con
   *          the con
   * @param billNo
   *          the bill no
   * @param newVisit
   *          the new visit
   * @throws SQLException
   *           the SQL exception
   */
  public void updateVisitId(Connection con, String billNo, String newVisit) throws SQLException {
    PreparedStatement ps = con.prepareStatement(UPDATE_SERVICE_VISIT);
    ps.setString(1, newVisit);
    ps.setString(2, billNo);
    ps.executeUpdate();
    ps.close();
  }

  /** The Constant GET_SERVICE_ITEM_SUB_GROUP_CODES_DETAILS. */
  private static final String GET_SERVICE_ITEM_SUB_GROUP_CODES_DETAILS = " SELECT "
      + " isg.item_subgroup_id, isg.subgroup_code, isg.item_subgroup_name, ig.group_code "
      + " FROM service_item_sub_groups sisg "
      + " JOIN item_sub_groups isg ON(sisg.item_subgroup_id = isg.item_subgroup_id) "
      + " JOIN item_groups ig ON (ig.item_group_id = isg.item_group_id) "
      + " WHERE sisg.service_id = ? ";

  /**
   * Gets the service item sub group tax details.
   *
   * @param itemId
   *          the item id
   * @return the service item sub group tax details
   * @throws SQLException
   *           the SQL exception
   */
  public List<BasicDynaBean> getServiceItemSubGroupTaxDetails(String itemId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    List<BasicDynaBean> itemSubGroups = new ArrayList<BasicDynaBean>();

    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_SERVICE_ITEM_SUB_GROUP_CODES_DETAILS);
      ps.setString(1, itemId);
      itemSubGroups = DataBaseUtil.queryToDynaList(ps);

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return itemSubGroups;
  }
}
