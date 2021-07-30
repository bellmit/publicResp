package com.insta.hms.core.clinical.order.serviceitems;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.clinical.consultation.prescriptions.PendingPrescriptionsRepository;
import com.insta.hms.core.clinical.consultation.prescriptions.PendingPrescriptionsService;
import com.insta.hms.core.clinical.order.master.OrderItemRepository;
import com.insta.hms.core.clinical.preauth.PreAuthItemsService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * The Class ServiceOrderItemRepository.
 */
@Repository
public class ServiceOrderItemRepository extends OrderItemRepository {
  
  /** The pending prescriptions repository. */
  @LazyAutowired
  private PendingPrescriptionsRepository pendingPrescriptionsRepository;
  
  /** The service order item service. */
  @LazyAutowired
  private ServiceOrderItemService serviceOrderItemService;

  /** The pre auth items service. */
  @LazyAutowired
  private PreAuthItemsService preAuthItemsService;

  /** The Constant GENERATE_NEXT_SEQUENCE_QUERY. */
  private static final String GENERATE_NEXT_SEQUENCE_QUERY = " SELECT nextval(?)";

  /**
   * Instantiates a new service order item repository.
   */
  public ServiceOrderItemRepository() {
    super("services_prescribed");
  }

  /** The Constant GET_SERVICE_LIST. */
  private static final String GET_SERVICE_LIST = " SELECT s.service_name, s.service_id"
      + " FROM services_prescribed sp JOIN services s USING (service_id)"
      + " WHERE sp.patient_id=?";

  /**
   * Gets the prescribed service list.
   *
   * @param patientId the patient id
   * @return the prescribed service list
   */
  public List<BasicDynaBean> getPrescribedServiceList(String patientId) {

    return DatabaseHelper.queryToDynaList(GET_SERVICE_LIST, patientId);
  }

  /** The Constant CONDUCTED_SERVICE_DETAILS. */
  private static final String CONDUCTED_SERVICE_DETAILS =
      "SELECT sp.*, s.service_name, sd.department, s.units, "
      + " coalesce(dr.doctor_name, ht.tech_name) as conducting_doctor, sdoc.doc_id, "
      + " (CASE WHEN dat.doc_format IS NULL THEN 'doc_fileupload' ELSE dat.doc_format END)"
      + " AS doc_format, " + " sdoc.doc_name, dat.template_id, s.conducting_doc_mandatory "
      + " FROM services_prescribed sp " + " JOIN services s USING (service_id) "
      + " JOIN services_departments sd using (serv_dept_id) "
      + " LEFT JOIN service_documents sdoc ON (sp.prescription_id=sdoc.prescription_id)"
      + " LEFT JOIN patient_documents pd on sdoc.doc_id=pd.doc_id "
      + " LEFT OUTER JOIN doc_all_templates_view dat ON (pd.template_id=dat.template_id"
      + " AND pd.doc_format=dat.doc_format) "
      + " LEFT JOIN doc_type dt ON (dt.doc_type_id = pd.doc_type) "
      + " LEFT JOIN doctors dr on sp.conductedby = dr.doctor_id "
      + " LEFT JOIN hospital_technical ht on (sp.conductedby = ht.tech_id)"
      + " WHERE sp.patient_id=? and sp.conducted IN ('P', 'C')";

  /**
   * Gets the conducted service details.
   *
   * @param patientId the patient id
   * @return the conducted service details
   */
  public List<BasicDynaBean> getConductedServiceDetails(String patientId) {

    return DatabaseHelper.queryToDynaList(CONDUCTED_SERVICE_DETAILS, patientId);
  }

  /**
   * Gets the next sequence.
   *
   * @return the next sequence
   */
  public Integer getNextSequence() {
    return DatabaseHelper.getInteger(GENERATE_NEXT_SEQUENCE_QUERY, "service_prescribed");
  }

  /** The Constant SERVICES_QUERY. */
  private static final String SERVICES_QUERY =
      " SELECT 'Service' AS type, s.service_id AS id, s.service_name AS name,"
      + " s.service_code AS code, "
      + "  sd.department AS department, sd.serv_dept_id AS department_id,"
      + "  sg.service_sub_group_id as subGrpId, "
      + "  sg.service_group_id as groupid,s.prior_auth_required,"
      + "  s.insurance_category_id,conduction_applicable,"
      + "  (CASE WHEN s.conducting_doc_mandatory = 'O' THEN true ELSE false END)"
      + "  as  conducting_doc_mandatory, "
      + "  false as results_entry_applicable,s.tooth_num_required,false"
      + "  as multi_visit_package, -1 as center_id, '-1' as tpa_id, "
      + "  'N' as mandate_additional_info,'' as additional_info_reqts " + " FROM services s "
      + "  JOIN services_departments sd ON (s.serv_dept_id = sd.serv_dept_id) "
      + "  JOIN service_sub_groups sg using(service_sub_group_id) #SCHEDULABLE_FILTER# ";

  private static final String SERVICE_SCHEDULABLE =
      " where ( scheduleable_by = ? OR scheduleable_by IS NULL) ";

  private static final String SERVICE_SCHEDULABLE_BY_MULTIPLE =
      " where ( scheduleable_by in (?, ?) OR scheduleable_by IS NULL) ";

  /**
   * Gets the item details.
   *
   * @param entityIdList the entity id list
   * @return the item details
   */
  public List<BasicDynaBean> getItemDetails(List<Object> entityIdList, List<String> schedulable) {
    List<Object> otherParams = new ArrayList<>();
    String query = SERVICES_QUERY;
    if (schedulable == null || schedulable.size() == 0) {
      query = query.replace("#SCHEDULABLE_FILTER#", " ");
    } else if (schedulable != null && schedulable.size() > 1) {
      query = query.replace("#SCHEDULABLE_FILTER#", SERVICE_SCHEDULABLE_BY_MULTIPLE);
    } else if (schedulable != null && schedulable.size() == 1) {
      query = query.replace("#SCHEDULABLE_FILTER#", SERVICE_SCHEDULABLE);
    }
    otherParams.addAll(schedulable);
    return super.getItemDetails(query, entityIdList, "s.service_id", otherParams, false);
  }

  /** The Constant ORDERED_ITEMS. */
  private static final String ORDERED_ITEMS =
      "SELECT sp.patient_id, sp.prescription_id as order_id, sp.common_order_id,"
      + " 'Service' as type,"
      + " '' as sub_type, sd.department as sub_type_name, 'SER' as activity_code,"
      + " sp.service_id as item_id," + " s.service_name as item_name, s.service_code as item_code,"
      + " sp.doctor_id as pres_doctor_id, pd.doctor_name as pres_doctor_name,"
      + " sp.presc_date as pres_timestamp, sp.remarks as remarks,"
      + " TO_CHAR(sp.presc_date,'dd-mm-yyyy') as pres_date,"
      + " COALESCE(bc.posted_date, sp.presc_date) as posted_date, "
      + " sp.quantity as quantity, null::timestamp as from_timestamp,"
      + " null::timestamp as to_timestamp, "
      + " (sp.quantity::integer)::text || ' No(s)' AS details, operation_ref,"
      + " package_ref, b.bill_no, b.is_tpa, b.bill_type, b.status as bill_status, bc.amount,"
      + " bc.tax_amt, b.is_primary_bill, bc.insurance_claim_amount, bc.sponsor_tax_amt,"
      + " (CASE WHEN conducted = 'X' THEN 'X' WHEN NOT s.conduction_applicable"
      + " THEN 'U' ELSE sp.conducted END) as status, "
      + " 'N' as sample_collected, 'U' as finalization_status,"
      + " bc.prior_auth_id, bc.prior_auth_mode_id,bc.first_of_category,"
      + " d.doctor_name as cond_doctor_name,"
      + " d.doctor_id as cond_doctor_id, null as labno, true as canclebill,"
      + " sp.specialization AS isdialysis, ds.completion_status, "
      + " coalesce(ds.status, 'O') AS dialysis_status,'' as urgent,"
      + " (case when coalesce(sp.tooth_unv_number,'')='' then sp.tooth_fdi_number"
      + " else sp.tooth_unv_number end) as tooth_number,"
      + " bc.insurance_category_id,bc.charge_id, null AS outsource_dest_prescribed_id,"
      + " 'N' as mandate_additional_info, "
      + " '' as additional_info_reqts, COALESCE(pm.multi_visit_package, false)"
      + " AS multi_visit_package, pm.package_id, pp.pat_package_id,"
      + " b.bill_rate_plan_id as org_id,  "
      + " bc.item_excluded_from_doctor, bc.item_excluded_from_doctor_remarks,"
      + " bc.charge_head,bc.charge_group, bc.preauth_act_id, ppc.content_id_ref, "
      + " ppc.patient_package_content_id, ppa.preauth_required AS send_for_prior_auth, "
      + " ppa.preauth_act_status "
      + " FROM services_prescribed sp JOIN services s ON (s.service_id = sp.service_id)"
      + " JOIN services_departments sd ON (s.serv_dept_id = sd.serv_dept_id)"
      + " LEFT JOIN doctors pd on pd.doctor_id = sp.doctor_id"
      + " LEFT JOIN bill_activity_charge bac ON bac.activity_id=sp.prescription_id::text"
      + " AND bac.activity_code='SER' LEFT JOIN bill_charge bc USING (charge_id)"
      + " LEFT JOIN doctors d on d.doctor_id = bc.payee_doctor_id"
      + " LEFT JOIN bill b USING (bill_no)"
      + " LEFT JOIN dialysis_session ds ON (ds.order_id = sp.prescription_id)"
      + " LEFT JOIN package_prescribed pp ON(pp.prescription_id = sp.package_ref) "
      + " LEFT JOIN patient_package_content_consumed ppcc "
      + " ON (ppcc.prescription_id = sp.prescription_id AND "
      + "     ppcc.item_type IN ('Service')) "
      + " LEFT JOIN patient_package_contents ppc "
      + " ON (ppcc.patient_package_content_id = ppc.patient_package_content_id) "
      + " LEFT JOIN preauth_prescription_activities ppa "
      + "  ON bc.preauth_act_id = ppa.preauth_act_id"
      + " LEFT JOIN packages pm ON CASE WHEN package_ref IS NULL "
      + "   THEN (pm.package_id::text = sp.service_id) "
      + "   ELSE (pm.package_id = pp.package_id) END WHERE sp.patient_id = ? ";

  /**
   * Gets the ordered items.
   *
   * @param visitId      the visit id
   * @param operationRef the operation ref
   * @param packageRef   the package ref
   * @return the ordered items
   */
  public List<BasicDynaBean> getOrderedItems(String visitId, Integer operationRef,
      Boolean packageRef) {
    Object[] values;
    String operationRefCondition;
    String packageRefCondition;
    if (packageRef != null && packageRef) {
      packageRefCondition = GET_MVP_ITEM_CONDITION;
    } else {
      packageRefCondition = IGNORE_MVP_ITEM_CONDITION;
    }
    if (operationRef == null) {
      operationRefCondition = IGNORE_OPERATION_ITEM_CONDITION;
      values = new Object[] { visitId };
    } else {
      operationRefCondition = GET_OPERATION_ITEM_CONDITION;
      values = new Object[] { visitId, operationRef };
    }

    return DatabaseHelper
        .queryToDynaList(ORDERED_ITEMS + operationRefCondition + packageRefCondition, values);
  }

  /** The Constant FOR_PATIENT_SERVICE. */
  private static final String FOR_PATIENT_SERVICE =
      "UPDATE patient_prescription pp SET username=?, status = "
      + "CASE WHEN (SELECT (SELECT COUNT(ppp.status) FROM patient_pending_prescriptions ppp WHERE "
      + "  ppp.patient_presc_id = ? AND status = 'O') > 0) THEN 'PA' ELSE 'P' "
      + " END WHERE pp.patient_presc_id = ? ";


  /**
   * Update cancel status to patient.
   *
   * @param items the items
   * @param userName the user name
   * @return the int[]
   */
  public int[] updateCancelStatusToPatient(List<BasicDynaBean> items, String userName, 
      Map<String, Object> itemInfoMap) {
    List<Object[]> paramsList = new ArrayList<>();
    Map<Integer, Map<String, Object>> chargePriorAuth = 
        serviceOrderItemService.getChargePriorAuthDetails(itemInfoMap);
    for (BasicDynaBean item : items) {
      Object prescId = item.get("prescription_id");
      BasicDynaBean bean = findByKey("prescription_id", prescId);
      Object docPrescId = bean == null ? null : bean.get("doc_presc_id");
      if (docPrescId != null && !"".equals(docPrescId.toString())) {
        paramsList.add(new Object[] { userName, docPrescId, docPrescId });
        String preauthActStatus = null;
        if (chargePriorAuth.containsKey(prescId)) {
          preauthActStatus = preAuthItemsService.determinePreauthStatusForPrescribedItem(
              chargePriorAuth.get(prescId));          
        }
        if (null == preauthActStatus) {
          pendingPrescriptionsRepository.updatePendingPrescriptionStatus((Integer)docPrescId, 1, 
              "P");          
        } else {
          pendingPrescriptionsRepository.updatePendingPrescriptionStatus((Integer)docPrescId, 1, 
              preauthActStatus, "P"); 
        }
      }
    }
    return DatabaseHelper.batchUpdate(FOR_PATIENT_SERVICE, paramsList);
  }

  /** The Constant GET_PACKAGE_OPERATION_REF. */
  private static final String GET_PACKAGE_OPERATION_REF = "SELECT prescription_id as order_id, "
      + " 'Service' as type FROM services_prescribed WHERE patient_id = ? ";

  /**
   * Gets the package ref query.
   *
   * @return the package ref query
   */
  @Override
  public String getPackageRefQuery() {
    return GET_PACKAGE_OPERATION_REF;
  }

  /**
   * Gets the operation ref query.
   *
   * @return the operation ref query
   */
  @Override
  public String getOperationRefQuery() {
    return GET_PACKAGE_OPERATION_REF;
  }

  /** The Constant GET_MR_NO_FOR_SERVICE_PRESCRIBED_ID. */
  private static final String GET_MR_NO_FOR_SERVICE_PRESCRIBED_ID = "SELECT mr_no "
      + " from services_prescribed JOIN patient_details using(mr_no) "
      + " WHERE prescription_id in (:prescribedid)";

  /** The Constant CHECK_ISR_PATIENT_FOR_SERVICE_PRESCRIBED_ID. */
  private static final String CHECK_ISR_PATIENT_FOR_SERVICE_PRESCRIBED_ID = "SELECT * "
      + " FROM services_prescribed sp"
      + " JOIN incoming_sample_registration isr ON(sp.patient_id = isr.incoming_visit_id) "
      + " WHERE prescription_id in (:prescribedid)";

  /**
   * Gets the mr no for service prescribed id.
   *
   * @param prescribedId the prescribed id
   * @return the mr no for service prescribed id
   */
  public List<String> getMrNoForServicePrescribedId(List<String> prescribedId) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("prescribedid", prescribedId, java.sql.Types.INTEGER);
    List<BasicDynaBean> listBeanMrNo = DatabaseHelper
        .queryToDynaList(GET_MR_NO_FOR_SERVICE_PRESCRIBED_ID, parameters);
    List<String> mrNos = null;
    if (listBeanMrNo.isEmpty()) {
      List<BasicDynaBean> listBeanIsr = DatabaseHelper
          .queryToDynaList(CHECK_ISR_PATIENT_FOR_SERVICE_PRESCRIBED_ID, parameters);
      if (!listBeanIsr.isEmpty()) {
        mrNos = Arrays.asList("ISR");
      }
    } else {
      mrNos = new ArrayList<>(listBeanMrNo.size());
      for (BasicDynaBean bean : listBeanMrNo) {
        String thisMrNo = (String) bean.get("mr_no");
        mrNos.add(thisMrNo);
      }
    }
    return mrNos;
  }

  /** The Constant GET_ORDERED_QUANTITY. */
  private static final String GET_ORDERED_QUANTITY = 
      "SELECT SUM(quantity) AS quantity FROM services_prescribed WHERE doc_presc_id = ?";

  /**
   * Gets already ordered quantity for a prescription.
   *
   * @param doctorPrescriptionId the doctor prescription id
   * @return the ordered quantity
   */
  public BigDecimal getOrderedQuantity(Integer doctorPrescriptionId) {
    BigDecimal quantity = (BigDecimal) DatabaseHelper
        .queryToDynaBean(GET_ORDERED_QUANTITY, new Object[] { doctorPrescriptionId })
        .get("quantity");
    if (quantity != null) {
      return quantity;
    }
    return BigDecimal.ZERO;
  }
}
