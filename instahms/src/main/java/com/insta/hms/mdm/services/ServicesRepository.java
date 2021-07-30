package com.insta.hms.mdm.services;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

/** The Class ServicesRepository. */
@Repository
public class ServicesRepository extends MasterRepository<String> {

  /** Instantiates a new services repository. */
  public ServicesRepository() {
    super("services", "service_id", "service_name");
  }

  /** The charge query. */
  private static final String CHARGE_QUERY =
      "SELECT "
          + "  s.serv_dept_id, smc.unit_charge, s.service_tax, s.service_name, s.service_id, "
          + "  s.service_code, sod.item_code,s.service_sub_group_id, "
          + "  s.status, s.conduction_applicable, s.specialization, discount ,sod.applicable, "
          + "  sod.code_type,"
          + "  s.conducting_doc_mandatory, s.insurance_category_id,allow_rate_increase,"
          + "  allow_rate_decrease, s.allow_zero_claim_amount, billing_group_id "
          + "  FROM service_master_charges smc "
          + "  JOIN services s ON (s.service_id = smc.service_id) "
          + "  JOIN service_org_details sod ON ( "
          + "  sod.service_id = smc.service_id AND sod.org_id = smc.org_id) "
          + "  WHERE smc.service_id=? and smc.bed_type=? and smc.org_id=?";

  /**
   * Gets the service charges bean.
   *
   * @param serviceId the service id
   * @param bedType the bed type
   * @param orgId the org id
   * @return the service charges bean
   */
  public BasicDynaBean getServiceChargesBean(String serviceId, String bedType, String orgId) {
    return DatabaseHelper.queryToDynaBean(CHARGE_QUERY, new Object[] {serviceId, bedType, orgId});
  }

  /**
   * The charge query.
   */
  private static final String ALL_BED_TYPE_CHARGE_QUERY =
      "SELECT "
          + "  unit_charge as charge ,bed_type ,discount  "
          + "  FROM service_master_charges "
          + "  WHERE service_id=? and org_id=?";

  /**
   * Gets the service charges bean.
   *
   * @param serviceId the service id
   * @param orgId     the org id
   * @return the service charges bean
   */
  public List<BasicDynaBean> getAllServiceChargesBean(String serviceId, String orgId) {
    return DatabaseHelper
        .queryToDynaList(ALL_BED_TYPE_CHARGE_QUERY, serviceId, orgId);
  }

  /**
   * Gets the service charge bean.
   *
   * @param serviceId the service id
   * @param bedType the bed type
   * @param orgId the org id
   * @return the service charge bean
   */
  public BasicDynaBean getServiceChargeBean(String serviceId, String bedType, String orgId) {
    BasicDynaBean servchargebean = DatabaseHelper.queryToDynaBean(CHARGE_QUERY,
        serviceId, bedType, orgId);
    if (servchargebean == null) {
      servchargebean = DatabaseHelper.queryToDynaBean(CHARGE_QUERY,
          serviceId, "GENERAL", "ORG0001");
    }
    return servchargebean;
  }

  /** The pres services. */
  private static final String PRES_SERVICES =
      "SELECT s.service_name AS item_name, s.service_code AS order_code,"
          + " s.service_id AS item_id, s.serv_dept_id,sd.department AS serv_dept_name, "
          + " 'Service' AS item_type,"
          + " s.prior_auth_required, s.tooth_num_required, "
          + " COALESCE(cat.insurance_category_id,0) AS insurance_category_id, "
          + " COALESCE(cat.insurance_category_name,'') AS insurance_category_name, "
          + " COALESCE(cat.category_payable,'N') AS category_payable, "
          + " unit_charge as charge, discount, sod.item_code "
          + " AS item_rate_plan_code, sod.applicable "
          + " FROM services s "
          + " JOIN service_master_charges smc ON (s.service_id=smc.service_id "
          + "   AND bed_type=? AND org_id=?) "
          + " JOIN services_departments sd ON (s.serv_dept_id=sd.serv_dept_id) "
          + " JOIN service_org_details sod ON (sod.service_id = smc.service_id AND "
          + " sod.org_id = ?) "
          + " LEFT JOIN LATERAL (SELECT sic.service_id AS service_id, sic.insurance_category_id, "
          + "      iic.insurance_category_name, ipd.category_payable "
          + "      FROM service_insurance_category_mapping sic "
          + "      JOIN item_insurance_categories iic ON "
          + " (sic.insurance_category_id = iic.insurance_category_id) "
          + "      JOIN insurance_plan_details ipd ON "
          + " (ipd.insurance_category_id = iic.insurance_category_id"
          + " AND ipd.patient_type = ?"
          + " AND ipd.plan_id=?) WHERE s.service_id = sic.service_id "
          + "      ORDER BY iic.priority LIMIT 1) as cat ON(cat.service_id = s.service_id) "
          + " WHERE s.status='A' AND (s.service_name ilike ? OR "
          + " s.service_name ilike ? OR s.service_code ilike ?) "
          + " ORDER BY s.service_name "
          + " LIMIT ?";

  /**
   * Gets the services for prescription.
   *
   * @param bedType the bed type
   * @param orgId the org id
   * @param patientType the patient type
   * @param insPlanId the ins plan id
   * @param searchQuery the search query
   * @param itemLimit the item limit
   * @return the services for prescription
   */
  public List<BasicDynaBean> getServicesForPrescription(
      String bedType,
      String orgId,
      String patientType,
      Integer insPlanId,
      String searchQuery,
      Integer itemLimit) {
    return DatabaseHelper.queryToDynaList(
        PRES_SERVICES,
        new Object[] {
          bedType,
          orgId,
          orgId,
          patientType,
          insPlanId,
          searchQuery + "%",
          "% " + searchQuery + "%",
          searchQuery + "%",
          itemLimit
        });
  }

  //services_item_sub_groups
  //item_sub_groups
  //item_sub_groups_tax_details

  /** The Constant GET_SERVICE_ITEM_SUB_GROUP_TAX_DETAILS. */
  private static final String GET_SERVICE_ITEM_SUB_GROUP_TAX_DETAILS =
      "SELECT isg.item_subgroup_id, isg.subgroup_code, isg.item_subgroup_name, ig.group_code "
          + " FROM service_item_sub_groups sisg "
          + " JOIN item_sub_groups isg ON(sisg.item_subgroup_id = isg.item_subgroup_id) "
          + " JOIN item_groups ig ON (ig.item_group_id = isg.item_group_id) "
          + " WHERE sisg.service_id = ? ";

  /**
   * Gets the service item sub group tax details.
   *
   * @param itemId the item id
   * @return the service item sub group tax details
   */
  public List<BasicDynaBean> getServiceItemSubGroupTaxDetails(String itemId) {
    return DatabaseHelper.queryToDynaList(
        GET_SERVICE_ITEM_SUB_GROUP_TAX_DETAILS, new Object[] {itemId});
  }
  
  /** The Constant GET_SERVICE_ITEM_SUB_GROUP_TAX_DETAILS. */
  private static final String GET_SERVICES =
      "SELECT service_id, serv_dept_id "
          + " FROM services"
          + " WHERE service_id IN (:serviceId) ";


  /**
   * Gets the services.
   *
   * @param ids the ids
   * @return the services
   */
  public List<BasicDynaBean> getServices(List<String> ids) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("serviceId", ids);
    return DatabaseHelper.queryToDynaList(GET_SERVICES, parameters);
  }

  /** The special service Code query. */
  private static final String SPECIAL_SERVICE_CODE_QUERY =
      "SELECT service_id,special_service_code,special_service_contract_name"
          + "  FROM service_org_details "
          + "  WHERE service_id=?  and org_id=?";

  /**
   * Gets the special service Code bean.
   *
   * @param serviceId the service id
   * @param orgId the org id
   * @return the special service Code bean
   */
  public BasicDynaBean getSpecialServiceCodeBean(String serviceId, String orgId) {
    return DatabaseHelper.queryToDynaBean(SPECIAL_SERVICE_CODE_QUERY, 
     new Object[] {serviceId, orgId});
  }
}

