package com.bob.hms.adminmasters.services;

import com.bob.hms.common.Constants;
import com.bob.hms.common.DataBaseUtil;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class MasterServicesDao.
 */

public class MasterServicesDao {

  /** The logger. */
  private static final Logger logger = LoggerFactory.getLogger(MasterServicesDao.class);

  /** The charge query. */
  private static final String CHARGE_QUERY = "SELECT "
      + "  s.serv_dept_id, smc.unit_charge, s.service_tax, s.service_name, s.service_id, "
      + "  s.service_code, sod.item_code,s.service_sub_group_id, "
      + "  s.status, s.conduction_applicable, s.specialization, discount"
      + " ,sod.applicable, sod.code_type,"
      + "  s.conducting_doc_mandatory, s.insurance_category_id,"
      + "  allow_rate_increase,allow_rate_decrease, billing_group_id "
      + " FROM service_master_charges smc "
      + "  JOIN services s ON (s.service_id = smc.service_id) "
      + "  JOIN service_org_details sod ON ( "
      + "    sod.service_id = smc.service_id AND sod.org_id = smc.org_id) "
      + " WHERE smc.service_id=? and smc.bed_type=? and smc.org_id=?";

  /**
   * Gets the service charges bean.
   *
   * @param serviceId
   *          the service id
   * @param bedType
   *          the bed type
   * @param orgId
   *          the org id
   * @return the service charges bean
   * @throws SQLException
   *           the SQL exception
   */
  public BasicDynaBean getServiceChargesBean(String serviceId, String bedType, String orgId)
      throws SQLException {
    return DataBaseUtil.queryToDynaBean(CHARGE_QUERY, new String[] { serviceId, bedType, orgId });
  }

  /**
   * Gets the service charge bean.
   *
   * @param serviceId
   *          the service id
   * @param bedType
   *          the bed type
   * @param orgId
   *          the org id
   * @return the service charge bean
   * @throws SQLException
   *           the SQL exception
   */
  public BasicDynaBean getServiceChargeBean(String serviceId, String bedType, String orgId)
      throws SQLException {
    MasterServicesDao dao = new MasterServicesDao();
    BasicDynaBean servchargebean = dao.getServiceChargesBean(serviceId, bedType, orgId);
    if (servchargebean == null) {
      servchargebean = dao.getServiceChargesBean(serviceId, "GENERAL", "ORG0001");
    }
    return servchargebean;
  }

  /** Rate Plan ID fetch query. **/
  private static final String RP_QUERY = "select org_id from organization_details where org_name=?";

  /**
   * Returns service details of a particular service.
   *
   * @param serviceId
   *          the service id
   * @param bedType
   *          the bed type
   * @param orgId
   *          the org id
   * @return the service
   * @throws SQLException
   *           the SQL exception
   */
  public Service getService(String serviceId, String bedType, String orgId) throws SQLException {
    Service service = null;
    try (Connection con = DataBaseUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(CHARGE_QUERY);) {
      String generalorgid = DataBaseUtil.getStringValueFromDb(RP_QUERY, 
          Constants.getConstantValue("ORG"));
      String generalbedtype = Constants.getConstantValue("BEDTYPE");

      ps.setString(1, serviceId);
      ps.setString(2, bedType);
      ps.setString(3, orgId);
      try (ResultSet rs = ps.executeQuery();) {
        while (rs.next()) {
          service = new Service();
          service.setServiceId(rs.getString("service_id"));
          service.setServiceName(rs.getString("service_name"));
          service.setServiceCode(rs.getString("item_code"));
          service.setStatus(rs.getString("status"));
          service.setConduction_applicable(rs.getBoolean("conduction_applicable"));
          service.setDeptId(rs.getString("dept_name"));
          service.setTax(rs.getDouble("service_tax"));
          service.setServiceCharge(rs.getDouble("unit_charge"));
          service.setDiscount(rs.getDouble("discount"));
        }
      }
      /*
       * incase service did not found for the specified bedtype and orgid combination a general
       * betypa and general org charges will be filled in service
       */
      if (service == null) {
        try (PreparedStatement ps1 = con.prepareStatement(CHARGE_QUERY)) {
          ps1.setString(1, serviceId);
          ps1.setString(2, generalbedtype);
          ps1.setString(3, generalorgid);
          try (ResultSet rs1 = ps.executeQuery();) {
            while (rs1.next()) {
              service = new Service();
              service.setServiceId(rs1.getString("service_id"));
              service.setServiceName(rs1.getString("service_name"));
              service.setServiceCode(rs1.getString("item_code"));
              service.setStatus(rs1.getString("status"));
              service.setConduction_applicable(rs1.getBoolean("conduction_applicable"));
              service.setDeptId(rs1.getString("dept_name"));
              service.setTax(rs1.getDouble("service_tax"));
              service.setServiceCharge(rs1.getDouble("unit_charge"));
              service.setDiscount(rs1.getDouble("discount"));
            }
          }
        }
      }
    }
    return service;
  }

  /**
   * Gets the servicecharges.
   *
   * @param serviceid
   *          the serviceid
   * @param bedtype
   *          the bedtype
   * @param orgid
   *          the orgid
   * @return the servicecharges
   */
  public List getServiceCharges(String serviceid, String bedtype, String orgid) {

    List cl = null;
    try (Connection con = DataBaseUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(CHARGE_QUERY)) {
      String generalorgid = DataBaseUtil.getStringValueFromDb(RP_QUERY, 
          Constants.getConstantValue("ORG"));
      String generalbedtype = Constants.getConstantValue("BEDTYPE");
      ps.setString(1, serviceid);
      ps.setString(2, bedtype);
      ps.setString(3, orgid);
      cl = DataBaseUtil.queryToArrayList(ps);
      logger.debug("{}", cl);
      if (cl.isEmpty()) {
        ps.setString(1, serviceid);
        ps.setString(2, generalbedtype);
        ps.setString(3, generalorgid);
        cl = DataBaseUtil.queryToArrayList(ps);
      }

    } catch (Exception ex) {
      logger.error("Exception occured in getservicecharges method", ex);
    }

    return cl;
  }

  /** The Constant GET_SERVICE_DEPT. */
  private static final String GET_SERVICE_DEPT = 
      "SELECT s.service_id, s.service_name, sd.department AS dept_name, s.units,"
      + " s.service_tax,s.specialization, sc.unit_charge, sod.item_code,sc.discount "
      + "FROM services s JOIN service_master_charges sc using (service_id) "
      + "JOIN services_departments sd ON (sd.serv_dept_id=s.serv_dept_id) "
      + "JOIN service_org_details sod ON (sod.service_id = sc.service_id and"
      + " sod.org_id = sc.org_id) AND  sod.applicable " + "WHERE s.status='A' ";

  /** The Constant GET_SERVICE_DEPT_CHARGES_WITOUT_DIALYSIS. */
  private static final String GET_SERVICE_DEPT_CHARGES_WITOUT_DIALYSIS = GET_SERVICE_DEPT
      + " AND sc.bed_type=? AND sc.org_id=? and s.specialization is null";

  /** The Constant GET_SERVICE_DEPT_CHARGES_WIT_DIALYSIS. */
  private static final String GET_SERVICE_DEPT_CHARGES_WIT_DIALYSIS = GET_SERVICE_DEPT
      + " AND sc.bed_type=? AND sc.org_id=? ";

  /**
   * Gets the service dept charges.
   *
   * @param bedType
   *          the bed type
   * @param orgid
   *          the orgid
   * @param dialysisModule
   *          the dialysis module
   * @return the service dept charges
   * @throws SQLException
   *           the SQL exception
   */
  public List getServiceDeptCharges(String bedType, String orgid, String dialysisModule)
      throws SQLException {
    List list = null;
    String query = (dialysisModule != null && dialysisModule.equals("Y"))
        ? GET_SERVICE_DEPT_CHARGES_WIT_DIALYSIS : GET_SERVICE_DEPT_CHARGES_WITOUT_DIALYSIS; 
    try (Connection con = DataBaseUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(query)) {
      ps.setString(1, bedType);
      ps.setString(2, orgid);
      list = DataBaseUtil.queryToArrayList(ps);
    }
    return list;
  }

  /** The get all service. */
  private static String GET_ALL_SERVICE = 
      "SELECT service_id,service_name FROM  services ORDER BY service_name ";

  /**
   * Gets the all service names.
   *
   * @return the all service names
   * @throws SQLException
   *           the SQL exception
   */
  public ArrayList getAllServiceNames() throws SQLException {
    ArrayList services = new ArrayList();
    try (Connection con = DataBaseUtil.getReadOnlyConnection();
        PreparedStatement ps = con.prepareStatement(GET_ALL_SERVICE)) {
      services = DataBaseUtil.queryToArrayList(ps);
    }
    return services;
  }

}