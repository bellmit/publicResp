package com.bob.hms.adminmasters.organization;

import static com.insta.hms.jobs.common.QuartzJobHelper.buildJob;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.bob.hms.otmasters.theamaster.TheatreMasterDAO;
import com.insta.hms.adminmaster.packagemaster.PackageChargeDAO;
import com.insta.hms.adminmaster.packagemaster.PackageDAO;
import com.insta.hms.adminmaster.packagemaster.PackageItemChargesDAO;
import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.JobSchedulingService;
import com.insta.hms.diagnosticsmasters.addtest.AddTestDAOImpl;
import com.insta.hms.diagnosticsmasters.addtest.TestChargesDAO;
import com.insta.hms.jobs.JobService;
import com.insta.hms.master.AnaesthesiaTypeMaster.AnaesthesiaTypeChargesDAO;
import com.insta.hms.master.ConsultationCharges.ConsultationChargesDAO;
import com.insta.hms.master.DietaryMaster.DietChargesDAO;
import com.insta.hms.master.DoctorMaster.DoctorChargeDAO;
import com.insta.hms.master.DynaPackage.DynaPackageCategoryLimitsDAO;
import com.insta.hms.master.DynaPackage.DynaPackageChargesDAO;
import com.insta.hms.master.EquipmentMaster.EquipmentChargeDAO;
import com.insta.hms.master.OperationMaster.OperationChargeDAO;
import com.insta.hms.master.RegistrationCharges.RegistrationChargesDAO;
import com.insta.hms.master.ServiceMaster.ServiceChargeDAO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * The Class OrgMasterBo.
 */
public class OrgMasterBo {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(OrgMasterBo.class);

  private static final long SCHEDULER_TRIGGER_DELAY_IN_MINUTES = 15;

  /**
   * Save new organization.
   *
   * @param dto                  the dto
   * @param varianceType         the variance type
   * @param varianceValue        the variance value
   * @param varianceBy           the variance by
   * @param useValue             the use value
   * @param baseOrgId            the base org id
   * @param nearestRoundOffValue the nearest round off value
   * @param userName             the user name
   * @return the string
   * @throws Exception the exception
   */
  public String saveNewOrganization(Organization dto, String varianceType, Double varianceValue,
      Double varianceBy, boolean useValue, String baseOrgId, Double nearestRoundOffValue,
      String userName) throws Exception {
    insertRateSheetCreationDetails(null, dto.getOrgName(), "P", null);
    scheduleRateSheetCreation(dto, varianceType, varianceValue, varianceBy, useValue, baseOrgId,
        nearestRoundOffValue, userName, null);

    return null;

  }

  /**
   * Insert rate sheet creation details.
   *
   * @param orgId   the org id
   * @param orgName the org name
   * @return true, if successful
   */
  private static boolean insertRateSheetCreationDetails(String orgId, String orgName, String status,
      String errorMessage) {
    RateSheetSchedulerDetailsDAO schedulerDetailsDAO = new RateSheetSchedulerDetailsDAO();
    return schedulerDetailsDAO.insertScheduledJobDetails(orgId, orgName, status, errorMessage);
  }

  /**
   * Schedule rate sheet creation.
   *
   * @param dto                  the dto
   * @param varianceType         the variance type
   * @param varianceValue        the variance value
   * @param varianceBy           the variance by
   * @param useValue             the use value
   * @param baseOrgId            the base org id
   * @param nearestRoundOffValue the nearest round off value
   * @param userName             the user name
   * @param orgId                the org id
   * @throws SQLException the SQL exception
   */
  private static void scheduleRateSheetCreation(Organization dto, String varianceType,
      Double varianceValue, Double varianceBy, boolean useValue, String baseOrgId,
      Double nearestRoundOffValue, String userName, String orgId) throws SQLException {
    Map<String, Object> jobData = new HashMap<>();
    jobData.put("dto", dto);
    jobData.put("varianceType", varianceType);
    jobData.put("varianceValue", varianceValue);
    jobData.put("varianceBy", varianceBy);
    jobData.put("useValue", useValue);
    jobData.put("baseOrgId", baseOrgId);
    jobData.put("nearestRoundOffValue", nearestRoundOffValue);
    jobData.put("userName", userName);
    jobData.put("centerId", RequestContext.getCenterId().toString());
    jobData.put("schema", RequestContext.getSchema());
    jobData.put("schedulerSeqId", getSchedulerSequenceId());
    JobService jobService = JobSchedulingService.getJobService();
    String jobName = "RateSheetCreationJob" + System.currentTimeMillis();
    Date jobTriggerDate = new Date(System.currentTimeMillis()
        + TimeUnit.MINUTES.toMillis(SCHEDULER_TRIGGER_DELAY_IN_MINUTES));
    jobService.scheduleAt(buildJob(jobName, RateSheetCreationJob.class, jobData),
        jobTriggerDate);
  }

  private static Integer getSchedulerSequenceId() throws SQLException {
    RateSheetSchedulerDetailsDAO schedulerDetailsDAO = new RateSheetSchedulerDetailsDAO();
    return schedulerDetailsDAO.getCurrentScheduledJobSeqId();
  }

  // Rate Plan Changes - Begin

  /**
   * Update rate plan.
   *
   * @param con          the con
   * @param orgId        the org id
   * @param varianceType the variance type
   * @param varianceBy   the variance by
   * @param useValue     the use value
   * @param baseOrgId    the base org id
   * @param roundOff     the round off
   * @param userName     the user name
   * @param orgName      the org name
   * @return true, if successful
   * @throws Exception the exception
   */
  public boolean updateRatePlan(Connection con, String orgId, String varianceType,
      Double varianceBy, boolean useValue, String baseOrgId, Double roundOff, String userName,
      String orgName) throws Exception {

    boolean success = false;

    // DoctorChargeDAO ddao = new DoctorChargeDAO();
    OperationChargeDAO odao = new OperationChargeDAO();
    TheatreMasterDAO thdao = new TheatreMasterDAO();
    EquipmentChargeDAO edao = new EquipmentChargeDAO();
    AnaesthesiaTypeChargesDAO adao = new AnaesthesiaTypeChargesDAO();
    ServiceChargeDAO sdao = new ServiceChargeDAO();
    TestChargesDAO tdao = new TestChargesDAO();
    ConsultationChargesDAO cdao = new ConsultationChargesDAO();
    PackageChargeDAO pdao = new PackageChargeDAO();
    PackageItemChargesDAO picdao = new PackageItemChargesDAO();
    DynaPackageChargesDAO dpdao = new DynaPackageChargesDAO();
    DynaPackageCategoryLimitsDAO dpcldao = new DynaPackageCategoryLimitsDAO();

    logger.info("Start procesing rate sheet " + baseOrgId + " : " + new Date());
    success = odao.updateRatePlan(con, orgId, baseOrgId, varianceType, varianceBy, roundOff,
        userName, orgName);
    if (success) {
      success = thdao.updateRatePlan(con, orgId, baseOrgId, varianceType, varianceBy, roundOff,
          userName, orgName);
    }
    if (success) {
      success = edao.updateRatePlan(con, orgId, baseOrgId, varianceType, varianceBy, roundOff,
          userName, orgName);
    }
    if (success) {
      success = adao.updateRatePlan(con, orgId, baseOrgId, varianceType, varianceBy, roundOff,
          userName, orgName);
    }
    if (success) {
      success = sdao.updateRatePlan(con, orgId, baseOrgId, varianceType, varianceBy, roundOff,
          userName, orgName);
    }
    if (success) {
      success = tdao.updateRatePlan(con, orgId, baseOrgId, varianceType, varianceBy, roundOff,
          userName, orgName);
    }
    if (success) {
      success = cdao.updateRatePlan(con, orgId, baseOrgId, varianceType, varianceBy, roundOff,
          userName, orgName);
    }
    if (success) {
      success = pdao.updateRatePlan(con, orgId, baseOrgId, varianceType, varianceBy, roundOff,
          userName, orgName);
    }
    if (success) {
      success = picdao.updateRatePlan(con, orgId, baseOrgId, varianceType, varianceBy, roundOff,
          userName, orgName);
    }
    if (success) {
      success = dpdao.updateRatePlan(con, orgId, baseOrgId, varianceType, varianceBy, roundOff,
          userName, orgName);
    }
    if (success) {
      success = dpcldao.updateRatePlan(con, orgId, baseOrgId, varianceType, varianceBy, roundOff,
          userName, orgName);
    }
    logger.info("End procesing rate sheet " + baseOrgId + " : " + new Date());

    return success;
  }

  /**
   * Inits the rate plan.
   *
   * @param con          the con
   * @param orgId        the org id
   * @param varianceType the variance type
   * @param varianceBy   the variance by
   * @param baseOrgId    the base org id
   * @param roundOff     the round off
   * @param userName     the user name
   * @param orgName      the org name
   * @return true, if successful
   * @throws Exception the exception
   */
  public boolean initRatePlan(Connection con, String orgId, String varianceType, Double varianceBy,
      String baseOrgId, Double roundOff, String userName, String orgName) throws Exception {

    boolean success = false;

    RegistrationChargesDAO rdao = new RegistrationChargesDAO();
    DoctorChargeDAO ddao = new DoctorChargeDAO();
    OperationChargeDAO odao = new OperationChargeDAO();
    TheatreMasterDAO thdao = new TheatreMasterDAO();
    EquipmentChargeDAO edao = new EquipmentChargeDAO();
    AnaesthesiaTypeChargesDAO adao = new AnaesthesiaTypeChargesDAO();
    ServiceChargeDAO sdao = new ServiceChargeDAO();
    TestChargesDAO tdao = new TestChargesDAO();
    ConsultationChargesDAO cdao = new ConsultationChargesDAO();
    PackageChargeDAO pdao = new PackageChargeDAO();
    PackageItemChargesDAO picdao = new PackageItemChargesDAO();
    DynaPackageChargesDAO dpdao = new DynaPackageChargesDAO();
    DynaPackageCategoryLimitsDAO dpcldao = new DynaPackageCategoryLimitsDAO();
    DietChargesDAO dtdao = new DietChargesDAO();
    BedMasterDAO bdao = new BedMasterDAO();

    logger.info("Start procesing rate plan " + orgId + " : " + new Date());
    logger.info("Start procesing rate sheet " + baseOrgId + " : " + new Date());

    success = rdao.initRatePlan(con, orgId, varianceType, varianceBy, baseOrgId, roundOff, userName,
        orgName);
    if (success) {
      success = bdao.initRatePlan(con, orgId, varianceType, varianceBy, baseOrgId, roundOff,
          userName, orgName);
    }
    if (success) {
      success = ddao.initRatePlan(con, orgId, varianceType, varianceBy, baseOrgId, roundOff,
          userName, orgName);
    }
    if (success) {
      success = odao.initRatePlan(con, orgId, varianceType, varianceBy, baseOrgId, roundOff,
          userName, orgName);
    }
    if (success) {
      success = thdao.initRatePlan(con, orgId, varianceType, varianceBy, baseOrgId, roundOff,
          userName, orgName);
    }
    if (success) {
      success = edao.initRatePlan(con, orgId, varianceType, varianceBy, baseOrgId, roundOff,
          userName, orgName);
    }
    if (success) {
      success = adao.initRatePlan(con, orgId, varianceType, varianceBy, baseOrgId, roundOff,
          userName, orgName);
    }
    if (success) {
      success = sdao.initRatePlan(con, orgId, varianceType, varianceBy, baseOrgId, roundOff,
          userName, orgName);
    }
    if (success) {
      success = tdao.initRatePlan(con, orgId, varianceType, varianceBy, baseOrgId, roundOff,
          userName, orgName);
    }
    if (success) {
      success = cdao.initRatePlan(con, orgId, varianceType, varianceBy, baseOrgId, roundOff,
          userName, orgName);
    }
    if (success) {
      success = pdao.initRatePlan(con, orgId, varianceType, varianceBy, baseOrgId, roundOff,
          userName, orgName);
    }
    if (success) {
      success = picdao.initRatePlan(con, orgId, varianceType, varianceBy, baseOrgId, roundOff,
          userName, orgName);
    }
    if (success) {
      success = dpdao.initRatePlan(con, orgId, varianceType, varianceBy, baseOrgId, roundOff,
          userName, orgName);
    }
    if (success) {
      success = dpcldao.initRatePlan(con, orgId, varianceType, varianceBy, baseOrgId, roundOff,
          userName, orgName);
    }
    if (success) {
      success = dtdao.initRatePlan(con, orgId, varianceType, varianceBy, baseOrgId, roundOff,
          userName, orgName);
    }
    logger.info("End procesing rate sheet " + baseOrgId + " : " + new Date());

    return success;
  }

  /**
   * Switch triggers.
   *
   * @param con   the con
   * @param state the state
   * @return true, if successful
   * @throws Exception the exception
   */
  public boolean switchTriggers(Connection con, String state) throws Exception {
    boolean status = true;
    try {
      GenericDAO.alterTrigger(state, "operation_charges", "z_operation_charges_audit_trigger");
      GenericDAO.alterTrigger(state, "diagnostic_charges",
          "z_diagnostictest_charges_audit_trigger");
      GenericDAO.alterTrigger(state, "service_master_charges", "z_services_charges_audit_trigger");
      GenericDAO.alterTrigger(state, "dyna_package_charges",
          "z_dyna_package_charges_audit_trigger");
      GenericDAO.alterTrigger(state, "dyna_package_category_limits",
          "z_dyna_package_category_limits_audit_trigger");

      GenericDAO.alterTrigger(state, "operation_org_details", "operation_org_update_timestamp");
      GenericDAO.alterTrigger(state, "service_org_details", "service_org_update_timestamp");
      GenericDAO.alterTrigger(state, "test_org_details", "test_org_update_timestamp");
      GenericDAO.alterTrigger(state, "pack_org_details", "pack_org_details_timestamp");
      GenericDAO.alterTrigger(state, "dyna_package_org_details",
          "dyna_package_org_update_timestamp");
    } catch (Exception ex) {
      status = false;
    }
    return status;
  }

  /**
   * Reinit rate plan.
   *
   * @param con          the con
   * @param orgId        the org id
   * @param varianceType the variance type
   * @param varianceBy   the variance by
   * @param baseOrgId    the base org id
   * @param roundOff     the round off
   * @param userName     the user name
   * @param orgName      the org name
   * @return true, if successful
   * @throws Exception the exception
   */
  public boolean reinitRatePlan(Connection con, String orgId, String varianceType,
      Double varianceBy, String baseOrgId, Double roundOff, String userName, String orgName)
      throws Exception {
    boolean success = false;

    RegistrationChargesDAO rdao = new RegistrationChargesDAO();
    DoctorChargeDAO ddao = new DoctorChargeDAO();
    OperationChargeDAO odao = new OperationChargeDAO();
    TheatreMasterDAO thdao = new TheatreMasterDAO();
    EquipmentChargeDAO edao = new EquipmentChargeDAO();
    AnaesthesiaTypeChargesDAO adao = new AnaesthesiaTypeChargesDAO();
    ServiceChargeDAO sdao = new ServiceChargeDAO();
    TestChargesDAO tdao = new TestChargesDAO();
    ConsultationChargesDAO cdao = new ConsultationChargesDAO();
    PackageChargeDAO pdao = new PackageChargeDAO();
    PackageItemChargesDAO picdao = new PackageItemChargesDAO();
    DynaPackageChargesDAO dpdao = new DynaPackageChargesDAO();
    DynaPackageCategoryLimitsDAO dpcldao = new DynaPackageCategoryLimitsDAO();
    DietChargesDAO dtdao = new DietChargesDAO();
    BedMasterDAO bdao = new BedMasterDAO();

    logger.info("Start procesing rate plan " + orgId + " : " + new Date());
    logger.info("Start procesing rate sheet " + baseOrgId + " : " + new Date());

    success = rdao.reinitRatePlan(con, orgId, varianceType, varianceBy, baseOrgId, roundOff,
        userName, orgName);
    if (success) {
      success = bdao.reinitRatePlan(con, orgId, varianceType, varianceBy, baseOrgId, roundOff,
          userName, orgName);
    }
    if (success) {
      success = ddao.reinitRatePlan(con, orgId, varianceType, varianceBy, baseOrgId, roundOff,
          userName, orgName);
    }
    if (success) {
      success = odao.reinitRatePlan(con, orgId, varianceType, varianceBy, baseOrgId, roundOff,
          userName, orgName);
    }
    if (success) {
      success = thdao.reinitRatePlan(con, orgId, varianceType, varianceBy, baseOrgId, roundOff,
          userName, orgName);
    }
    if (success) {
      success = edao.reinitRatePlan(con, orgId, varianceType, varianceBy, baseOrgId, roundOff,
          userName, orgName);
    }
    if (success) {
      success = adao.reinitRatePlan(con, orgId, varianceType, varianceBy, baseOrgId, roundOff,
          userName, orgName);
    }
    if (success) {
      success = sdao.reinitRatePlan(con, orgId, varianceType, varianceBy, baseOrgId, roundOff,
          userName, orgName);
    }
    if (success) {
      success = tdao.reinitRatePlan(con, orgId, varianceType, varianceBy, baseOrgId, roundOff,
          userName, orgName);
    }
    if (success) {
      success = cdao.reinitRatePlan(con, orgId, varianceType, varianceBy, baseOrgId, roundOff,
          userName, orgName);
    }
    if (success) {
      success = pdao.reinitRatePlan(con, orgId, varianceType, varianceBy, baseOrgId, roundOff,
          userName, orgName);
    }
    if (success) {
      success = picdao.reinitRatePlan(con, orgId, varianceType, varianceBy, baseOrgId, roundOff,
          userName, orgName);
    }
    if (success) {
      success = dpdao.reinitRatePlan(con, orgId, varianceType, varianceBy, baseOrgId, roundOff,
          userName, orgName);
    }
    if (success) {
      success = dpcldao.reinitRatePlan(con, orgId, varianceType, varianceBy, baseOrgId, roundOff,
          userName, orgName);
    }
    if (success) {
      success = dtdao.reinitRatePlan(con, orgId, varianceType, varianceBy, baseOrgId, roundOff,
          userName, orgName);
    }

    logger.info("End procesing rate sheet " + baseOrgId + " : " + new Date());

    return success;
  }

  // Rate Plan Changes - End

  /**
   * Update exist organization.
   *
   * @param dto                the dto
   * @param varianceType       the variance type
   * @param existOrgId         the exist org id
   * @param varianceValue      the variance value
   * @param varianceBy         the variance by
   * @param useValue           the use value
   * @param baseOrgId          the base org id
   * @param nearstRoundOfValue the nearst round of value
   * @param userName           the user name
   * @return true, if successful
   * @throws Exception the exception
   */
  // Rate Plan TODO : Unused method - remove
  private boolean updateExistOrganization(Organization dto, String varianceType, String existOrgId,
      Double varianceValue, Double varianceBy, boolean useValue, String baseOrgId,
      Double nearstRoundOfValue, String userName) throws Exception {

    boolean status = false;
    String orgId = existOrgId;
    String orgName = dto.getOrgName();

    if (baseOrgId == null || baseOrgId.equals("")) {
      baseOrgId = "ORG0001";
    }

    if (varianceValue == 0.0 && varianceBy == 0.0) {
      return true;
    }

    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);
    try {

      TheatreMasterDAO.updateOrgForTheatres(con, orgId, varianceType, varianceValue, varianceBy,
          useValue, baseOrgId, nearstRoundOfValue);

      PackageDAO.updateOrgForPackages(con, orgId, varianceType, varianceValue, varianceBy, useValue,
          baseOrgId, nearstRoundOfValue);

      BedMasterDAO.updateOrgForBeddetailscharge(con, orgId, varianceType, varianceValue, varianceBy,
          useValue, baseOrgId, nearstRoundOfValue);

      BedMasterDAO.updateOrgForIcuBedcharge(con, orgId, varianceType, varianceValue, varianceBy,
          useValue, baseOrgId, nearstRoundOfValue);

      AddTestDAOImpl.updateOrgForTests(con, orgId, varianceType, varianceValue, varianceBy,
          useValue, baseOrgId, nearstRoundOfValue, userName, orgName);

      ConsultationChargesDAO.updateOrgForConsultations(con, orgId, varianceType, varianceValue,
          varianceBy, useValue, baseOrgId, nearstRoundOfValue);

      DietChargesDAO.updateOrgForDietCharges(con, orgId, varianceType, varianceValue, varianceBy,
          useValue, baseOrgId, nearstRoundOfValue);

      DoctorChargeDAO.updateOrgForDrConstCharge(con, orgId, varianceType, varianceValue, varianceBy,
          useValue, baseOrgId, nearstRoundOfValue);

      DoctorChargeDAO.updateOrgForDrOPConscharge(con, orgId, varianceType, varianceValue,
          varianceBy, useValue, baseOrgId, nearstRoundOfValue);

      DynaPackageChargesDAO.updateOrgForDynapkgs(con, orgId, varianceType, varianceValue,
          varianceBy, useValue, baseOrgId, nearstRoundOfValue, userName, orgName);

      EquipmentChargeDAO.updateOrgForEquipments(con, orgId, varianceType, varianceValue, varianceBy,
          useValue, baseOrgId, nearstRoundOfValue);

      OperationChargeDAO.updateOrgForOperations(con, orgId, varianceType, varianceValue, varianceBy,
          useValue, baseOrgId, nearstRoundOfValue, userName, orgName);

      RegistrationChargesDAO.updateOrgForRegCharges(con, orgId, varianceType, varianceValue,
          varianceBy, useValue, baseOrgId, nearstRoundOfValue);

      ServiceChargeDAO.updateOrgForServices(con, orgId, varianceType, varianceValue, varianceBy,
          useValue, baseOrgId, nearstRoundOfValue, userName, orgName);

      AnaesthesiaTypeChargesDAO.updateOrgForAnaesthesia(con, orgId, varianceType, varianceValue,
          varianceBy, useValue, baseOrgId, nearstRoundOfValue);

      status = true;

    } finally {

      DataBaseUtil.commitClose(con, status);

      GenericDAO.alterTrigger("ENABLE", "operation_charges", "z_operation_charges_audit_trigger");
      GenericDAO.alterTrigger("ENABLE", "diagnostic_charges",
          "z_diagnostictest_charges_audit_trigger");
      GenericDAO.alterTrigger("ENABLE", "service_master_charges",
          "z_services_charges_audit_trigger");
      GenericDAO.alterTrigger("ENABLE", "dyna_packages", "z_dyna_package_master_audit_trigger");
    }

    return status;
  }

  /**
   * Update O rg details.
   *
   * @param dto the dto
   * @return true, if successful
   * @throws Exception the exception
   */
  public boolean updateORgDetails(Organization dto) throws Exception {

    boolean status = true;
    Connection con = DataBaseUtil.getConnection(300);
    con.setAutoCommit(false);

    OrgMasterDao dao = new OrgMasterDao();
    status = dao.updateORgDetails(con, dto);

    DataBaseUtil.commitClose(con, status);

    return status;
  }
}
