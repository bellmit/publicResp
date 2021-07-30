package com.insta.hms.core.clinical.mar;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.BusinessService;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.clinical.consultation.prescriptions.PrescriptionsService;
import com.insta.hms.core.clinical.patientactivities.PatientActivitiesHibernateRepository;
import com.insta.hms.core.clinical.patientactivities.PatientActivitiesModel;
import com.insta.hms.core.clinical.patientactivities.PatientActivitiesModel.ActivityStatus;
import com.insta.hms.core.clinical.patientactivities.PatientActivitiesRepository;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class MarSetupService.
 */
@Service
public class MarSetupService extends BusinessService {

  protected static final String INSERT = "insert";

  protected static final String UPDATE = "update";
  
  protected static final String DELETE = "deletedActivity";
  
  private Logger logger = LoggerFactory.getLogger(this.getClass());

  /** The repo. */
  @LazyAutowired
  private MarSetupRepository repo;

  @LazyAutowired
  private PrescriptionsService prescriptionService;

  @LazyAutowired
  private PatientActivitiesRepository patientActivitiesRepository;

  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;

  @LazyAutowired
  private PatientActivitiesHibernateRepository patientActivitiesHibernateRepository;

  @LazyAutowired
  private MarSetupRepository marSetupRepository;

  @LazyAutowired
  private MarSetupHibernateRepository marSetupHibernateRepository;

  /**
   * Insert.
   *
   * @param remarks            the remarks
   * @param servingFrequencyId the serving frequency id
   * @param prescriptionId     the prescription id
   * @param username           the username
   * @return the basic dyna bean
   */
  public BasicDynaBean insert(String remarks, Integer servingFrequencyId, Integer prescriptionId,
      String username) {
    BasicDynaBean bean = repo.getBean();
    bean.set("setup_id", repo.getNextSequence());
    bean.set("remarks", remarks);
    bean.set("serving_frequency_id", servingFrequencyId);
    bean.set("prescription_id", prescriptionId);
    bean.set("username", username);
    return repo.insert(bean) == 0 ? null : bean;
  }

  /**
   * Get list of setup remarks.
   * @param prescriptionId Prescription identifier
   * @return List of setup remarks
   */
  public List listSetupRemarks(Integer prescriptionId) {
    List<String> columns = new ArrayList<>();
    columns.add("remarks");
    columns.add("mod_time");
    columns.add("username");
    List<BasicDynaBean> remarksList = repo.listAll(columns, "prescription_id", prescriptionId,
        "mod_time");
    return ConversionUtils.copyListDynaBeansToMap(remarksList);
  }

  /**
   * Save MAR Setup.
   * @param patientId visit identifier
   * @param prescriptionId prescription identifier
   * @param requestBody request body
   * @return Map of updated mar
   * @throws ParseException may throw parse exception
   */
  public Map<String, Object> saveMarSetup(String patientId, Integer prescriptionId,
      ModelMap requestBody) throws ParseException {
    ValidationErrorMap errMap = new ValidationErrorMap();
    BasicDynaBean prescriptionBean = prescriptionService.findById(prescriptionId);
    if (prescriptionBean == null || !patientId.equals(prescriptionBean.get("visit_id"))) {
      errMap.addError("prescription_id", "exception.patient.id.prescription.id.miss.match");
      throw new ValidationException(errMap);
    }
    if (requestBody.get("remarks") == null || requestBody.get("remarks").equals("")) {
      errMap.addError("remarks", "exception.mar.setup.remarks.mandatory");
      throw new ValidationException(errMap);
    }
    Integer activityNum = patientActivitiesRepository
        .getMaxActivityNum((Integer) prescriptionBean.get("patient_presc_id"));
    List<PatientActivitiesModel> insertPatientActivities = new ArrayList<PatientActivitiesModel>();
    List<PatientActivitiesModel> updatePatientActivities = new ArrayList<PatientActivitiesModel>();
    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
    String userName = (String) sessionAttributes.get("userId");
    Integer setupId = marSetupRepository.getNextSequence();

    // To remove activities setup beyond the setup due date
    SimpleDateFormat simpleDateformatter = new SimpleDateFormat("dd-MM-yyyy");
    String lastSetupDateString = (String) requestBody.get("lastSetupDate");
    try {
      Date lastSetupDate = simpleDateformatter.parse(lastSetupDateString);
      Timestamp lastSetupDateTime = DateUtil
          .parseTimestamp(DataBaseUtil.dateFormatter.format(lastSetupDate).toString(), "23:59");
      if (patientActivitiesRepository.checkIfExisitingActivitiesBeyondSetupDueDate(prescriptionId,
          lastSetupDateTime) > 0) {
        if (patientActivitiesRepository.deleteActivitiesBeyondSetupDueDate(prescriptionId,
            lastSetupDateTime)) {
          logger.info("Delete Successful for activities beyond " + lastSetupDate);
        }
      }
    } catch (Exception exe) {
      logger.error("Last Setup Due Date parse exception occured ", exe);
      throw new ParseException("Unparseable Due Date: " + lastSetupDateString, 0);
    }
    
    if (requestBody.get(INSERT) != null) {
      List<Map<String, Object>> insertActivities = (List<Map<String, Object>>) requestBody
          .get(INSERT);
      for (Map<String, Object> patientActivity : insertActivities) {
        PatientActivitiesModel activity = new PatientActivitiesModel();
        activity.setActivityId(patientActivitiesRepository.getNextSequence());
        activity.setPatientId(patientId);
        activity.setActivityType('P');
        activity.setPrescriptionType('M');
        activity.setPrescriptionId((Integer) prescriptionBean.get("patient_presc_id"));
        activity.setPrescDoctorId((String) prescriptionBean.get("doctor_id"));
        activity.setSetupId(setupId);
        try {
          activity.setDueDate((String) patientActivity.get("due_date"));
        } catch (ParseException exe) {
          logger.error("Due Date parse exception occured "
              + exe);
        }
        activity.setActivityStatus(ActivityStatus.S);
        activity.setAddedBy(userName);
        activity.setUsername(userName);
        activity.setActivityNum(++activityNum);

        insertPatientActivities.add(activity);

      }
    }

    if (requestBody.get(UPDATE) != null) {
      List<Map<String, Object>> updateActivities = (List<Map<String, Object>>) requestBody
          .get(UPDATE);
      for (Map<String, Object> updateActivity : updateActivities) {
        PatientActivitiesModel activity = patientActivitiesHibernateRepository
            .get((Integer) updateActivity.get("activity_id"));
        try {
          activity.setDueDate((String) updateActivity.get("due_date"));
        } catch (ParseException exe) {
          logger.error("Due Date parse exception occured "
              + exe);
        }
        activity.setUsername(userName);
        activity.setSetupId(setupId);
        updatePatientActivities.add(activity);
      }

    }
    
    if (requestBody.get(DELETE) != null) {
      List<Object> activityIds = new ArrayList<>();
      List<Map<String, Object>> deletedActivities =
          (List<Map<String, Object>>) requestBody.get(DELETE);
      for (Map<String, Object> deleteActivity : deletedActivities) {
        PatientActivitiesModel activity = patientActivitiesHibernateRepository
            .get((Integer) deleteActivity.get("activity_id"));
        if (activity != null) {
          activityIds.add(activity.getActivityId());
        }
      }
      patientActivitiesRepository.batchDelete("activity_id", activityIds);
    }

    MarSetupModel setupModel = new MarSetupModel();
    setupModel.setSetupId(setupId);
    setupModel.setPrescriptionId(prescriptionId);
    setupModel.setPackageUom((String) requestBody.get("package_uom"));
    setupModel.setRemarks((String) requestBody.get("remarks"));
    setupModel.setServingDosage((String) requestBody.get("serving_dosage"));
    setupModel.setUsername(userName);
    if (requestBody.get("serving_frequency_id") != null
        && !requestBody.get("serving_frequency_id").equals("")) {
      setupModel.setServingFrequencyId((Integer) requestBody.get("serving_frequency_id"));
    }
    try {
      if (!insertPatientActivities.isEmpty()) {
        patientActivitiesHibernateRepository.batchInsert(insertPatientActivities);
      }

      if (!updatePatientActivities.isEmpty()) {
        for (PatientActivitiesModel modal : updatePatientActivities) {
          patientActivitiesHibernateRepository.save(modal);
          patientActivitiesHibernateRepository.flush();
        }
      }

      marSetupHibernateRepository.persist(setupModel);
      marSetupHibernateRepository.flush();

    } catch (Exception exe) {
      logger.error("Exception Occured While Saving MarSetup "
          + exe);

    }

    return new HashMap<String, Object>();
  }

  public BasicDynaBean getLastSavedSetup(Integer prescriptionId) {

    return marSetupRepository.getLastSavedSetup(prescriptionId);
  }

}
