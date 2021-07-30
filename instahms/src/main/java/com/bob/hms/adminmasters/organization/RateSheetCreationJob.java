package com.bob.hms.adminmasters.organization;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.bob.hms.otmasters.theamaster.TheatreMasterDAO;
import com.insta.hms.adminmaster.packagemaster.PackageDAO;
import com.insta.hms.adminmaster.packagemaster.PackageItemChargesDAO;
import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;
import com.insta.hms.billing.ClaimGenerationJob;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.diagnosticsmasters.addtest.AddTestDAOImpl;
import com.insta.hms.jobs.GenericJob;
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

import org.apache.commons.lang3.StringUtils;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * The Class RateSheetCreationJob.
 */
public class RateSheetCreationJob extends GenericJob {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(ClaimGenerationJob.class);

  /** The dto. */
  private Organization dto;

  /** The variance type. */
  private String varianceType;

  /** The variance value. */
  private Double varianceValue;

  /** The variance by. */
  private Double varianceBy;

  /** The use value. */
  private boolean useValue;

  /** The base org id. */
  private String baseOrgId;

  /** The nearest round off value. */
  private Double nearestRoundOffValue;

  /** The user name. */
  private String userName;

  /** The org id. */
  private String orgId;

  /** The center id. */
  private String centerId;

  /** The org name. */
  private String orgName;

  /** The scheduler seq id. */
  private Integer schedulerSeqId;

  /*
   * (non-Javadoc)
   * 
   * @see org.springframework.scheduling.quartz.QuartzJobBean
   * #executeInternal(org.quartz.JobExecutionContext)
   */
  /*
   * Creates RateSheet as a background job.
   * 
   */
  @Override
  public void executeInternal(JobExecutionContext jobContext) throws JobExecutionException {
    createRateSheet(jobContext);
  }

  /**
   * Creates the rate sheet.
   *
   * @param jobContext the job context
   */
  private static synchronized void createRateSheet(JobExecutionContext jobContext) {
    Connection con = null;
    boolean status = true;
    RateSheetCreationJob job = new RateSheetCreationJob();
    StringBuilder errorMessageBuilder = new StringBuilder();
    OrgMasterDao orgMasterDao = new OrgMasterDao();
    try {
      job.setRateSheetDetails(jobContext, orgMasterDao);
      RequestContext.setConnectionDetails(
          new String[] { null, null, job.getSchema(), "_system", job.centerId });
      ;
      con = DataBaseUtil.getConnection(300);
      con.setAutoCommit(false);
      if (job.dto.getOrgName() != null && orgMasterDao.isDuplicateOrgName(job.dto.getOrgName())) {
        status = false;
        errorMessageBuilder.append("Duplicate ratesheet name.");
      }
      status = status && orgMasterDao.saveNewOrganization(con, job.dto);
      status = status && job.updateChargesAndExclusions(con);
    } catch (Exception exception) {
      status = false;
      errorMessageBuilder
          .append("Exception while creating ratesheet due to " + exception.getMessage());
      logger.error("Exception while creating rate sheet in background job ", exception);
    } finally {
      try {
        status = status && job.enableAuditTriggers(con, errorMessageBuilder);
        DataBaseUtil.commitClose(con, status);
      } catch (Exception exception) {
        status = false;
        errorMessageBuilder
            .append("Exception while committing ratesheet " + exception.getMessage());
        logger.error("Exception while committing rate sheet in background job ", exception);
      } finally {
        job.updateJobDetails(status, errorMessageBuilder);
      }
    }
  }

  /**
   * Sets the rate sheet details.
   *
   * @param jobContext   the job context
   * @param orgMasterDao the org master dao
   * @throws SQLException the SQL exception
   */
  private void setRateSheetDetails(JobExecutionContext jobContext, OrgMasterDao orgMasterDao)
      throws SQLException {
    this.orgId = orgMasterDao.getNextOrgId();
    if (jobContext != null && jobContext.getJobDetail() != null
        && jobContext.getJobDetail().getJobDataMap() != null) {
      JobDataMap jobDatamap = jobContext.getJobDetail().getJobDataMap();
      dto = (Organization) jobDatamap.get("dto");
      varianceType = jobDatamap.getString("varianceType");
      varianceBy = jobDatamap.getDouble("varianceBy");
      useValue = jobDatamap.getBooleanValue("useValue");
      baseOrgId = jobDatamap.getString("baseOrgId");
      nearestRoundOffValue = jobDatamap.getDoubleValue("nearestRoundOffValue");
      userName = jobDatamap.getString("userName");
      setSchema(jobDatamap.getString("schema"));
      centerId = jobDatamap.getString("centerId");
      schedulerSeqId = jobDatamap.getIntValue("schedulerSeqId");
    }
    if (dto != null) {
      orgName = dto.getOrgName();
    }
    if (StringUtils.isEmpty(baseOrgId)) {
      baseOrgId = "ORG0001";
    }
    dto.setOrgId(orgId);
  }

  /**
   * Update job details.
   *
   * @param status              the status
   * @param errorMessageBuilder the error message builder
   */
  private void updateJobDetails(boolean status, StringBuilder errorMessageBuilder) {
    RateSheetSchedulerDetailsDAO schedulerDetailsDAO = new RateSheetSchedulerDetailsDAO();
    schedulerDetailsDAO.updateScheduledRatesheetCreationJob(schedulerSeqId, status,
        errorMessageBuilder.toString(), orgId);
  }

  /**
   * Enable audit triggers.
   *
   * @param con                 the con
   * @param errorMessageBuilder the error message builder
   * @return true, if successful
   */
  private boolean enableAuditTriggers(Connection con, StringBuilder errorMessageBuilder) {
    boolean success = true;
    try {
      GenericDAO.alterTrigger(con, "ENABLE", "operation_charges",
          "z_operation_charges_audit_trigger");
      GenericDAO.alterTrigger(con, "ENABLE", "diagnostic_charges",
          "z_diagnostictest_charges_audit_trigger");
      GenericDAO.alterTrigger(con, "ENABLE", "service_master_charges",
          "z_services_charges_audit_trigger");
      GenericDAO.alterTrigger(con, "ENABLE", "dyna_package_charges",
          "z_dyna_package_charges_audit_trigger");
      GenericDAO.alterTrigger(con, "ENABLE", "dyna_package_category_limits",
          "z_dyna_package_category_limits_audit_trigger");
    } catch (Exception exception) {
      success = false;
      errorMessageBuilder
          .append("Exception while altering triggers. Please contact customer support.");
      logger.error("Exception while altering triggers rate sheet in background job ", exception);
    }
    return success;
  }

  /**
   * Update charges and exclusions.
   *
   * @param con the con
   * @return true, if successful
   * @throws Exception the exception
   */
  private boolean updateChargesAndExclusions(Connection con) throws Exception {

    RegistrationChargesDAO.addOrgForRegistration(con, orgId, varianceType, varianceValue,
        varianceBy, useValue, baseOrgId, nearestRoundOffValue, true);

    DoctorChargeDAO.addOrgFordoctors(con, orgId, varianceType, varianceValue, varianceBy, useValue,
        baseOrgId, nearestRoundOffValue, true);

    DoctorChargeDAO.addOrgForOpCharges(con, orgId, varianceType, varianceValue, varianceBy,
        useValue, baseOrgId, nearestRoundOffValue, true);

    BedMasterDAO.addOrgForBedTypes(con, orgId, varianceType, varianceValue, varianceBy, useValue,
        baseOrgId, nearestRoundOffValue, true);

    BedMasterDAO.addOrgForIcuBedTypes(con, orgId, varianceType, varianceValue, varianceBy, useValue,
        baseOrgId, true);

    OperationChargeDAO.addOrgForOperations(con, orgId, varianceType, varianceValue, varianceBy,
        useValue, baseOrgId, nearestRoundOffValue, userName, orgName, true);

    TheatreMasterDAO.addOrgForTheatres(con, orgId, varianceType, varianceBy, baseOrgId,
        nearestRoundOffValue, true);

    EquipmentChargeDAO.addOrgForEquipments(con, orgId, varianceType, varianceValue, varianceBy,
        useValue, baseOrgId, nearestRoundOffValue, true);

    AnaesthesiaTypeChargesDAO.addOrgForAnesthesiaType(con, orgId, varianceType, varianceValue,
        varianceBy, useValue, baseOrgId, nearestRoundOffValue, true);

    ServiceChargeDAO.addOrgForServices(con, orgId, varianceType, varianceValue, varianceBy,
        useValue, baseOrgId, nearestRoundOffValue, userName, orgName, true);

    DynaPackageChargesDAO.addOrgForDynaPackages(con, orgId, varianceType, varianceValue, varianceBy,
        useValue, baseOrgId, nearestRoundOffValue, userName, orgName);

    DynaPackageCategoryLimitsDAO.addOrgForDynaPackages(con, orgId, varianceType, varianceValue,
        varianceBy, useValue, baseOrgId, nearestRoundOffValue, userName, orgName);

    DietChargesDAO.addOrgForDietary(con, orgId, varianceType, varianceValue, varianceBy, useValue,
        baseOrgId, nearestRoundOffValue, true);

    AddTestDAOImpl.addOrgForTests(con, orgId, varianceType, varianceValue, varianceBy, useValue,
        baseOrgId, nearestRoundOffValue, userName, orgName, true);

    ConsultationChargesDAO.addOrgForConsultations(con, orgId, varianceType, varianceValue,
        varianceBy, useValue, baseOrgId, nearestRoundOffValue, userName, orgName, true);

    AddTestDAOImpl.addOrgCodesForTests(con, orgId, varianceType, varianceValue, varianceBy,
        useValue, baseOrgId, nearestRoundOffValue, userName);

    ServiceChargeDAO.addOrgCodesForServices(con, orgId, varianceType, varianceValue, varianceBy,
        useValue, baseOrgId, nearestRoundOffValue);

    DynaPackageChargesDAO.addOrgCodesForDynaPackages(con, orgId, varianceType, varianceValue,
        varianceBy, useValue, baseOrgId, nearestRoundOffValue);

    OperationChargeDAO.addOrgCodesForOperations(con, orgId, varianceType, varianceValue, varianceBy,
        useValue, baseOrgId, nearestRoundOffValue);

    PackageDAO.addOrgForPackages(con, orgId, varianceType, varianceValue, varianceBy, useValue,
        baseOrgId, nearestRoundOffValue, true);

    PackageDAO.addOrgCodesForPackages(con, orgId, varianceType, varianceValue, varianceBy, useValue,
        baseOrgId, nearestRoundOffValue);

    PackageItemChargesDAO.addOrgForPackageItems(con, orgId, varianceType, varianceValue, varianceBy,
        useValue, baseOrgId, nearestRoundOffValue, userName, orgName);

    ConsultationChargesDAO.addOrgCodesForConsultations(con, orgId, varianceType, varianceValue,
        varianceBy, useValue, baseOrgId, nearestRoundOffValue, userName);

    AnaesthesiaTypeChargesDAO.addOrgCodesForAnesthesia(con, orgId, varianceType, varianceValue,
        varianceBy, useValue, baseOrgId, nearestRoundOffValue, userName);

    DoctorChargeDAO.addOrgCodesForDoctors(con, orgId, varianceType, varianceValue, varianceBy,
        useValue, baseOrgId, nearestRoundOffValue, userName);

    EquipmentChargeDAO.addOrgCodesForEquipment(con, orgId, varianceType, varianceValue, varianceBy,
        useValue, baseOrgId, nearestRoundOffValue, userName);

    TheatreMasterDAO.addOrgCodesForTheatre(con, orgId, varianceType, varianceValue, varianceBy,
        useValue, baseOrgId, nearestRoundOffValue, userName);

    return true;
  }
}
