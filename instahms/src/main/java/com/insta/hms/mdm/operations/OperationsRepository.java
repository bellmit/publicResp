package com.insta.hms.mdm.operations;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class OperationsRepository.
 */
@Repository
public class OperationsRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new operations repository.
   */
  public OperationsRepository() {
    super("operation_master", "op_id");
  }

  /** The Constant PRES_OPERATIONS. */
  private static final String PRES_OPERATIONS = " SELECT om.operation_name AS item_name,"
      + " om.operation_code AS order_code, om.op_id AS item_id, 'Operation' AS item_type,"
      + " om.prior_auth_required, "
      + " COALESCE(cat.insurance_category_id,0) AS insurance_category_id, "
      + " COALESCE(cat.insurance_category_name,'') AS insurance_category_name, "
      + " COALESCE(cat.category_payable,'N') AS category_payable, "
      + " surg_asstance_charge as charge, surg_asst_discount as discount,"
      + " ood.item_code AS item_rate_plan_code, ood.applicable "
      + "FROM operation_master om "
      + " JOIN operation_charges oc ON (om.op_id=oc.op_id AND bed_type=? AND org_id=?) "
      + " JOIN operation_org_details ood ON (ood.operation_id=om.op_id AND ood.org_id=?) "
      + " LEFT JOIN LATERAL (SELECT oic.operation_id AS operation_id, oic.insurance_category_id, "
      + "      iic.insurance_category_name, ipd.category_payable "
      + "      FROM operation_insurance_category_mapping oic "
      + "      JOIN item_insurance_categories iic "
      + " ON (oic.insurance_category_id = iic.insurance_category_id) JOIN insurance_plan_details"
      + " ipd ON(ipd.insurance_category_id = iic.insurance_category_id "
      + "        AND ipd.patient_type = ? AND ipd.plan_id=?) "
      + "      WHERE om.op_id = oic.operation_id "
      + "      ORDER BY iic.priority LIMIT 1) as cat ON(cat.operation_id = om.op_id) "
      + " WHERE om.status='A' AND "
      + "    (om.operation_name ilike ? OR om.operation_name ilike ? OR om.operation_code ilike ?) "
      + " ORDER BY om.operation_name " + "LIMIT ?";

  /**
   * Gets the operations for prescription.
   *
   * @param bedType
   *          the bed type
   * @param orgId
   *          the org id
   * @param patientType
   *          the patient type
   * @param insPlanId
   *          the ins plan id
   * @param searchQuery
   *          the search query
   * @param itemLimit
   *          the item limit
   * @return the operations for prescription
   */
  public List<BasicDynaBean> getOperationsForPrescription(String bedType, String orgId,
      String patientType, Integer insPlanId, String searchQuery, Integer itemLimit) {
    return DatabaseHelper.queryToDynaList(PRES_OPERATIONS,
        new Object[] { bedType, orgId, orgId, patientType, insPlanId, searchQuery + "%",
            "% " + searchQuery + "%", searchQuery + "%", itemLimit });
  }

  /** The Constant GET_OPERATION_ITEM_SUB_GROUP_TAX_DETAILS. */
  private static final String GET_OPERATION_ITEM_SUB_GROUP_TAX_DETAILS = "SELECT "
      + " isg.item_subgroup_id, isg.subgroup_code, isg.item_subgroup_name, ig.group_code "
      + " FROM operation_item_sub_groups oisg "
      + " JOIN item_sub_groups isg ON(oisg.item_subgroup_id = isg.item_subgroup_id) "
      + " JOIN item_groups ig ON (ig.item_group_id = isg.item_group_id) "
      + " WHERE oisg.op_id = ? ";

  /**
   * Gets the operation item sub group tax details.
   *
   * @param actDescriptionId
   *          the act description id
   * @return the operation item sub group tax details
   */
  public List<BasicDynaBean> getOperationItemSubGroupTaxDetails(String actDescriptionId) {
    return DatabaseHelper.queryToDynaList(GET_OPERATION_ITEM_SUB_GROUP_TAX_DETAILS,
        new Object[] { actDescriptionId });
  }

  /** The Constant OP_CHARGE_QUERY. */
  private static final String OP_CHARGE_QUERY = "SELECT "
      + "  surg_asstance_charge, surgeon_charge, anesthetist_charge, "
      + "  surg_asst_discount, surg_discount, anest_discount, "
      + "  om.operation_code, ood.item_code, om.op_id, "
      + "  om.operation_name, om.dept_id, om.conduction_applicable,ood.applicable, "
      + "  om.service_sub_group_id, ood.code_type, om.insurance_category_id,"
      + "  allow_rate_increase,allow_rate_decrease, om.allow_zero_claim_amount,  "
      + "  om.allow_zero_claim_amount, om.billing_group_id "
      + "  FROM operation_charges oc " 
      + "  JOIN operation_master om ON (om.op_id = oc.op_id) "
      + "  JOIN operation_org_details ood "
      + "    ON (oc.op_id = ood.operation_id and oc.org_id = ood.org_id) "
      + " WHERE oc.op_id=? and oc.bed_type=? and oc.org_id=? ";

  public BasicDynaBean getSurgeryCharge(String opId, String bedType, String orgId) {
    return DatabaseHelper.queryToDynaBean(OP_CHARGE_QUERY, new Object[] { opId, bedType, orgId });
  }

  /**
   * The Constant OP_CHARGE_QUERY.
   */
  private static final String ALL_OP_CHARGE_QUERY = "SELECT "
      + "  surg_asstance_charge, surgeon_charge, anesthetist_charge, "
      + "  surg_asst_discount, surg_discount, anest_discount, bed_type "
      + "  FROM operation_charges"
      + " WHERE op_id=? and org_id=? ";

  public List<BasicDynaBean> getAllSurgeryCharge(String opId, String orgId) {
    return DatabaseHelper.queryToDynaList(ALL_OP_CHARGE_QUERY, opId, orgId);
  }

  /**
   * Gets the operation charge.
   *
   * @param opId the op id
   * @param bedType the bed type
   * @param orgId the org id
   * @return the operation charge
   */
  public BasicDynaBean getOperationCharge(String opId, String bedType, String orgId) {
    BasicDynaBean opechargebean = getSurgeryCharge(opId, bedType, orgId);
    if (opechargebean == null) {
      opechargebean = getSurgeryCharge(opId, "GENERAL", "ORG0001"); 
    }
    return opechargebean;
  }

  /**
   * Gets the operation charge.
   *
   * @param opId    the op id
   * @param orgId   the org id
   * @return the operation charge
   */
  public List<BasicDynaBean> getAllOperationCharge(String opId, String orgId) {
    return getAllSurgeryCharge(opId, orgId);
  }

}
