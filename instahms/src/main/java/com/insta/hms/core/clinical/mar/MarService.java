package com.insta.hms.core.clinical.mar;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.clinicalpreferences.ClinicalPreferencesService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.clinical.consultation.prescriptions.PrescriptionsRepository;
import com.insta.hms.core.clinical.consultation.prescriptions.PrescriptionsService;
import com.insta.hms.core.clinical.forms.FormParameter;
import com.insta.hms.core.clinical.forms.SystemSectionService;
import com.insta.hms.core.clinical.ivadministrationdetails.PatientIVAdminstrationDetailsRepository;
import com.insta.hms.core.clinical.ivadministrationdetails.PatientIvAdminstrationModel;
import com.insta.hms.core.clinical.mar.MarSetupService;
import com.insta.hms.core.clinical.mar.MarValidator;
import com.insta.hms.core.clinical.patientactivities.PatientActivitiesHibernateRepository;
import com.insta.hms.core.clinical.patientactivities.PatientActivitiesModel;
import com.insta.hms.core.clinical.patientactivities.PatientActivitiesModel.ActivityStatus;
import com.insta.hms.core.clinical.patientactivities.PatientActivitiesRepository;
import com.insta.hms.core.clinical.patientactivities.PatientActivitiesService;
import com.insta.hms.core.clinical.prescriptions.PatientMedicinePrescriptionsService;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.mdm.formcomponents.FormComponentsService;
import com.insta.hms.mdm.packageuom.PackageUomService;
import com.insta.hms.mdm.servingfrequency.ServingFrequencyService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class MarService.
 */
@Service
public class MarService extends SystemSectionService {

  /** The prescriptions service. */
  @LazyAutowired
  private PrescriptionsService prescriptionsService;

  /** The patient activities service. */
  @LazyAutowired
  private PatientActivitiesService patientActivitiesService;

  /** The clinical preferences service. */
  @LazyAutowired
  private ClinicalPreferencesService clinicalPreferencesService;

  /** The serving frequency service. */
  @LazyAutowired
  private ServingFrequencyService servingFrequencyService;

  /** The patient activities repository. */
  @LazyAutowired
  private PatientActivitiesRepository patientActivitiesRepository;

  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;

  /** The mar setup service. */
  @LazyAutowired
  private MarSetupService marSetupService;

  @LazyAutowired
  private PatientActivitiesHibernateRepository patientActivitiesHibernateRepository;

  @LazyAutowired
  private PackageUomService packageUomService;

  @LazyAutowired
  private PatientMedicinePrescriptionsService patientMedicinePrescriptionsService;

  @LazyAutowired
  private PrescriptionsRepository prescriptionsRepository;

  @LazyAutowired
  private MarValidator marValidator;

  @LazyAutowired
  private PatientIvAdminstrationModel patientIvAdminstationModel;

  @LazyAutowired
  private PatientIVAdminstrationDetailsRepository patientIVAdminstrationDetailsRepository;

  private static final String SYSTEM_SETUP_MESSAGE = "System Generated Setup";

  private Logger logger = LoggerFactory.getLogger(this.getClass());

  /**
   * Instantiates a new mar service.
   */
  public MarService() {
    this.sectionId = -19;
  }

  @Override
  public Map<String, Object> saveSection(Map<String, Object> requestBody, BasicDynaBean sdbean,
      FormParameter parameter, Map<String, Object> errorMap) {
    return null;
  }

  @Override
  public Boolean deleteSection(Integer sectiondetailId, FormParameter parameter,
      Map<String, Object> errorMap) {
    return true;
  }

  @Override
  public Map<String, Object> getSectionDetailsFromLastSavedForm(FormParameter parameter) {
    if (FormComponentsService.FormType.Form_IP.name().equals(parameter.getFormType())
            && prescriptionsRepository.isAdmReqPresentInPresc(parameter.getMrNo(),
                parameter.getPatientId())) {
      return getSectionDetailsFromCurrentForm(parameter);
    }
    Map<String, Object> data = new HashMap<>();
    data.put("records", new ArrayList<Map<String, Object>>());
    return data;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Map<String, Object> getSectionDetailsFromCurrentForm(FormParameter parameter) {
    Calendar calenderInstance = Calendar.getInstance();
    calenderInstance.setTime(new Date());
    calenderInstance.add(Calendar.HOUR, -8);
    Timestamp from = new Timestamp(calenderInstance.getTimeInMillis());
    calenderInstance.add(Calendar.HOUR, 16);
    Timestamp to = new Timestamp(calenderInstance.getTimeInMillis());
    Map<String, Object> activities = get(parameter.getPatientId(), from, to, null);
    List<BasicDynaBean> medications = prescriptionsService.getMedications(parameter.getPatientId());
    List<Map<String, Object>> responsemedicationList = new ArrayList<>();
    for (int index = 0; index < medications.size(); index++) {
      Map<String, Object> medication = new HashMap<>();
      medication.putAll(medications.get(index).getMap());
      Integer prescriptionId = (Integer) medication.get("item_prescribed_id");
      if (activities.get(prescriptionId.toString()) != null) {
        medication.put("activities", activities.get(prescriptionId.toString()));
      } else {
        medication.put("activities", new ArrayList<>());
      }
      medication.put("remarks_history", marSetupService.listSetupRemarks(prescriptionId));
      responsemedicationList.add(medication);
    }
    Map<String, Object> response = new HashMap<>();
    response.put("records", responsemedicationList);
    return response;
  }

  /**
   * Gets the MAR.
   *
   * @param patientId the patient id
   * @param from the from
   * @param to the to
   * @param prescriptionId prescription Id
   * @return the map
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> get(String patientId, Timestamp from, Timestamp to,
      Integer prescriptionId) {
    List<BasicDynaBean> activities =
        patientActivitiesRepository.getMedicationsActivities(patientId, from, to, prescriptionId);
    Map<String, Object> response = new HashMap<>();
    if (activities.isEmpty()) {
      return response;
    }
    List<Integer> activityIds = new ArrayList<>();
    for (BasicDynaBean bean : activities) {
      activityIds.add((Integer) bean.get("activity_id"));
    }
    Map<Integer, Object> ivAdministerDetails = ConversionUtils.listBeanToMapListMap(
        patientActivitiesRepository.getIvAdministerDetails(activityIds), "activity_id");
    List<Map> activitiesM = new ArrayList<>();
    for (BasicDynaBean bean : activities) {
      if (ivAdministerDetails.get((Integer) bean.get("activity_id")) != null) {
        Map<String, Object> temp = new HashMap<>();
        temp.putAll(bean.getMap());
        temp.put("iv_administred_details",
            ivAdministerDetails.get((Integer) bean.get("activity_id")));
        activitiesM.add(temp);
      } else {
        activitiesM.add(bean.getMap());
      }
    }
    Map<Integer, Object> result =
        ConversionUtils.listMapToMapListMap(activitiesM, "prescription_id");
    for (Map.Entry<Integer, Object> entry : result.entrySet()) {
      response.put(entry.getKey().toString(), entry.getValue());
    }
    return response;
  }

  /**
   * Mar setup.
   *
   * @param prescription the prescription
   * @param parameter the parameter
   */
  public Boolean marSetup(BasicDynaBean prescription, FormParameter parameter) {
    Date currentDate = new Date();
    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
    String userName = (String) sessionAttributes.get("userId");

    BasicDynaBean clinicalPreference = clinicalPreferencesService.getClinicalPreferences();
    Calendar setupEndDateTimeCalender = Calendar.getInstance();
    setupEndDateTimeCalender.setTime(currentDate);
    setupEndDateTimeCalender.add(Calendar.DATE,
        (Integer) clinicalPreference.get("advance_setup_period"));

    Timestamp prescriptionStartDateTime = (Timestamp) prescription.get("start_datetime");
    Timestamp prescriptionEndDateTime = (Timestamp) prescription.get("end_datetime");
    Timestamp setupEndDateTime = new Timestamp(setupEndDateTimeCalender.getTimeInMillis());

    Calendar dueDateTimeCalender = Calendar.getInstance();
    dueDateTimeCalender
        .setTimeInMillis(Math.max(prescriptionStartDateTime.getTime(), currentDate.getTime()));
    List<BasicDynaBean> setupActivities = new ArrayList<>();

    Integer prescriptionId = (Integer) prescription.get("patient_presc_id");
    BasicDynaBean activitiesInfo =
        patientActivitiesRepository.getMedicationActivitiysInfo(prescriptionId);
    Timestamp lastActivityDueDate =
        activitiesInfo == null ? null : (Timestamp) activitiesInfo.get("due_date");
    Integer exsitingActivitiesCount =
        activitiesInfo == null ? 0 : ((Long) activitiesInfo.get("activitiy_count")).intValue();
    Integer currentDayActivityCount = activitiesInfo == null ? 0
        : ((Long) activitiesInfo.get("current_day_activity_count")).intValue();
    Integer newActivitiesCount = 0;
    Integer prescriptionOccurences = (Integer) prescription.get("no_of_occurrences");
    Integer recurrenceDailyId = (Integer) prescription.get("recurrence_daily_id");
    Integer maxDoses = (Integer) prescription.get("max_doses");
    BasicDynaBean bean = null;
    String freqType = (String) prescription.get("freq_type");
    if (recurrenceDailyId != null && recurrenceDailyId == -2) {
      dueDateTimeCalender.setTimeInMillis(currentDate.getTime());
      dueDateTimeCalender.set(Calendar.HOUR_OF_DAY, 0);
      dueDateTimeCalender.set(Calendar.MINUTE, 0);
      if (maxDoses != null && maxDoses <= currentDayActivityCount) {
        dueDateTimeCalender.add(Calendar.DATE, 1);
      }
      if (!checkEndCondition(prescriptionEndDateTime, setupEndDateTime,
          new Timestamp(dueDateTimeCalender.getTimeInMillis()), prescriptionOccurences,
          exsitingActivitiesCount, newActivitiesCount)) {
        setupActivities.add(getActivityBean(parameter, prescription,
            new Timestamp(dueDateTimeCalender.getTimeInMillis()), userName,
            exsitingActivitiesCount + 1));
      }
    } else if ("F".equals(freqType) && recurrenceDailyId == -1) {
      if (exsitingActivitiesCount != 0) {
        return false;
      }
      setupActivities.add(getActivityBean(parameter, prescription,
          new Timestamp(dueDateTimeCalender.getTimeInMillis()), userName, 1));
    } else if ("F".equals(freqType)) {
      // To check for late serving window
      SimpleDateFormat dateHourFormatter = new SimpleDateFormat("yyyy-MM-dd HH:00:00");
      Timestamp effectiveCurrentDateHour =
          Timestamp.valueOf(dateHourFormatter.format(dueDateTimeCalender.getTime()));

      BasicDynaBean servingFrequencyBean = servingFrequencyService
          .findByRecurrenceDailyId((Integer) prescription.get("recurrence_daily_id"));
      if (servingFrequencyBean == null) {
        ValidationErrorMap errMap = new ValidationErrorMap();
        errMap.addError("others", "exception.mar.setup.serving.ferquency.notexists");
        throw new ValidationException(errMap);
      }
      String[] timings = ((String) servingFrequencyBean.get("timings")).split(",");
      Integer timingsSize = timings.length;
      int index = 0;
      while (true) {
        if (index >= timingsSize) {
          index = 0;
          dueDateTimeCalender.add(Calendar.DATE, 1);
        }
        String time = timings[index];
        dueDateTimeCalender.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time.split(":")[0].trim()));
        dueDateTimeCalender.set(Calendar.MINUTE, Integer.parseInt(time.split(":")[1].trim()));
        Timestamp dueDate = new Timestamp(dueDateTimeCalender.getTimeInMillis());
        Date effectiveDueDateSlot = DateUtils.addHours(dueDateTimeCalender.getTime(),
            (Integer) clinicalPreference.get("late_serving_period"));
        if ((lastActivityDueDate != null && dueDate.before(lastActivityDueDate))
            || effectiveDueDateSlot.before(effectiveCurrentDateHour)) {
          index++;
          continue;
        }
        if (checkEndCondition(prescriptionEndDateTime, setupEndDateTime, dueDate,
            prescriptionOccurences, exsitingActivitiesCount, newActivitiesCount)) {
          break;
        }
        setupActivities.add(getActivityBean(parameter, prescription, dueDate, userName,
            exsitingActivitiesCount + ++newActivitiesCount));
        index++;
      }
    } else if ("R".equals(freqType)) {
      recurrenceDailyId = null;
      Integer interval = (Integer) prescription.get("repeat_interval");
      String intervalUnits = (String) prescription.get("repeat_interval_units");
      if (lastActivityDueDate == null) { // For Creating first activity
        Timestamp dueDate = new Timestamp(dueDateTimeCalender.getTimeInMillis());
        if (!checkEndCondition(prescriptionEndDateTime, setupEndDateTime, dueDate,
            prescriptionOccurences, exsitingActivitiesCount, newActivitiesCount)) {
          setupActivities.add(getActivityBean(parameter, prescription, dueDate, userName,
              exsitingActivitiesCount + ++newActivitiesCount));
        }
      } else {
        dueDateTimeCalender.setTimeInMillis(lastActivityDueDate.getTime());
      }
      while (true) {
        if (interval != null && interval != 0 && intervalUnits != null
            && !intervalUnits.equals("")) {
          if (intervalUnits.equals("M")) {
            dueDateTimeCalender.add(Calendar.MINUTE, interval);
          } else if (intervalUnits.equals("H")) {
            dueDateTimeCalender.add(Calendar.HOUR, interval);
          } else if (intervalUnits.equals("D")) {
            dueDateTimeCalender.add(Calendar.DATE, interval);
          }
        } else {
          break;
        }
        Timestamp dueDate = new Timestamp(dueDateTimeCalender.getTimeInMillis());
        if (dueDate.before(currentDate)) {
          continue;
        }
        if (checkEndCondition(prescriptionEndDateTime, setupEndDateTime, dueDate,
            prescriptionOccurences, exsitingActivitiesCount, newActivitiesCount)) {
          break;
        }
        setupActivities.add(getActivityBean(parameter, prescription, dueDate, userName,
            exsitingActivitiesCount + ++newActivitiesCount));
      }
    }
    if (!setupActivities.isEmpty()) {
      bean =
          marSetupService.insert(SYSTEM_SETUP_MESSAGE, recurrenceDailyId, prescriptionId, userName);
      Integer setupId = (Integer) bean.get("setup_id");
      for (int index = 0; index < setupActivities.size(); index++) {
        setupActivities.get(index).set("setup_id", setupId);
      }
      return patientActivitiesRepository
          .isBatchSuccess(patientActivitiesRepository.batchInsert(setupActivities));
    }
    return false;
  }

  private Boolean checkEndCondition(Timestamp endDate, Timestamp setupEndDate, Timestamp dueDate,
      Integer occurences, Integer noOfExsistingactivities, Integer newActivities) {
    if (occurences != null && (occurences <= (noOfExsistingactivities + newActivities)
        || noOfExsistingactivities > occurences)) {
      return true;
    }
    if (endDate != null && dueDate.after(endDate)) {
      return true;
    }
    if (setupEndDate != null && newActivities > 0 && dueDate.after(setupEndDate)) {
      return true;
    }
    return false;
  }

  private BasicDynaBean getActivityBean(FormParameter parameter, BasicDynaBean prescriptionBean,
      Timestamp duedate, String userName, Integer activityNumber) {
    BasicDynaBean activityBean = patientActivitiesRepository.getBean();
    activityBean.set("activity_id", patientActivitiesRepository.getNextSequence());
    activityBean.set("patient_id", parameter.getPatientId());
    activityBean.set("activity_type", "P");
    activityBean.set("prescription_type", "M");
    activityBean.set("prescription_id", prescriptionBean.get("patient_presc_id"));
    activityBean.set("presc_doctor_id", prescriptionBean.get("doctor_id"));
    activityBean.set("due_date", duedate);
    activityBean.set("activity_status",
        "Y".equals(prescriptionBean.get("discontinued")) ? "X" : "S");
    activityBean.set("added_by", userName);
    activityBean.set("username", userName);
    activityBean.set("activity_num", activityNumber);
    return activityBean;
  }

  /**
   * Save.
   *
   * @param parameter the parameter
   * @param entities the entities
   * @return the list
   */
  public List<PatientActivitiesModel> save(FormParameter parameter,
      List<PatientActivitiesModel> entities) {
    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
    String username = (String) sessionAttributes.get("userId");
    List<PatientActivitiesModel> response = new ArrayList<>();
    for (PatientActivitiesModel entity : entities) {
      if (entity.getActivityId() == 0) {
        Integer activityNum =
            patientActivitiesRepository.getMaxActivityNum(entity.getPrescriptionId());
        entity.setActivityNum(++activityNum);
        entity.setPatientId(parameter.getPatientId());
        entity.setActivityType('P');
        entity.setActivityId(patientActivitiesRepository.getNextSequence());
        patientActivitiesHibernateRepository.persist(entity);
        patientActivitiesHibernateRepository.flush();
        if (entity.getActivityStatus().equals(ActivityStatus.P)
            && entity.getIvStatus().equals('S')) {
          // patient_iv_adminstration_details repository insert
          PatientIvAdminstrationModel patientIvAdminstationModel =
              new PatientIvAdminstrationModel();
          patientIvAdminstationModel.setActivityId(entity.getActivityId());
          patientIvAdminstationModel.setState(entity.getIvStatus());
          patientIvAdminstationModel.setModTime(new Date());
          patientIvAdminstationModel.setRemarks(entity.getActivityRemarks());
          patientIvAdminstationModel.setUsername(username);
          patientIVAdminstrationDetailsRepository.save(patientIvAdminstationModel);
        }
        response.add(entity);
      } else {
        PatientActivitiesModel modal =
            patientActivitiesHibernateRepository.get(entity.getActivityId());
        entity.copyforUpdate(modal);
        modal.setUsername(username);
        if (modal.getActivityStatus().equals(ActivityStatus.D)) {
          modal.setCompletedBy(username);
        }
        patientActivitiesHibernateRepository.save(modal);
        patientActivitiesHibernateRepository.flush();
        BasicDynaBean bean =
            patientActivitiesRepository.getMedicationActivitiysInfo(modal.getPrescriptionId());
        if (modal.getActivityNum().equals(((Long) bean.get("activitiy_count")).intValue())
            && modal.getActivityStatus().equals(ActivityStatus.D)) {
          BasicDynaBean prescriptionBean = prescriptionsService.findById(modal.getPrescriptionId());
          if ((Integer) prescriptionBean.get("recurrence_daily_id") != -3) {
            marSetup(prescriptionBean, parameter);
          }
        }
        response.add(modal);
      }
    }
    return response;
  }

  /**
   * Get MAR Setup details.
   * 
   * @param prescription Bean for prescription
   * @param parameter parameters
   * @return Map containing setup details
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> marSetupDetails(BasicDynaBean prescription, FormParameter parameter) {
    Date currentDate = new Date();
    BasicDynaBean clinicalPreference = clinicalPreferencesService.getClinicalPreferences();
    Integer advanceSetupPeriod = (Integer) clinicalPreference.get("advance_setup_period");
    Integer maximumMedicationSetupDays =
        (Integer) clinicalPreference.get("maximum_medication_setup_days");
    if (maximumMedicationSetupDays > 0) {
      maximumMedicationSetupDays = maximumMedicationSetupDays - 1;
    }
    Calendar setupEndDateTimeCalender = Calendar.getInstance();
    setupEndDateTimeCalender.setTime(currentDate);
    setupEndDateTimeCalender.add(Calendar.DATE, advanceSetupPeriod);
    List<Map<String, Object>> setupActivities = new ArrayList<>();

    ValidationErrorMap errMap = new ValidationErrorMap();
    boolean isValid = marValidator.isMarSetupApplicable(prescription, errMap);
    if (!isValid) {
      throw new ValidationException(errMap);
    }

    Integer prescriptionId = (Integer) prescription.get("patient_presc_id");
    BasicDynaBean activitiesInfo =
        patientActivitiesRepository.getMedicationActivitiysInfo(prescriptionId);
    Timestamp lastActivityDueDate =
        activitiesInfo == null ? null : (Timestamp) activitiesInfo.get("due_date");
    Integer exsitingActivitiesCount =
        activitiesInfo == null ? 0 : ((Long) activitiesInfo.get("activitiy_count")).intValue();
    Integer currentDayActivityCount = activitiesInfo == null ? 0
        : ((Long) activitiesInfo.get("current_day_activity_count")).intValue();

    Map<String, Object> setupdays = getSetupDaysDetails(prescription, parameter,
        lastActivityDueDate, exsitingActivitiesCount, currentDayActivityCount, advanceSetupPeriod,
        maximumMedicationSetupDays, setupEndDateTimeCalender);
    Map<String, Object> responseMap = new HashMap<>();
    responseMap.putAll(setupdays);

    Timestamp prescriptionStartDateTime = (Timestamp) prescription.get("start_datetime");
    Timestamp prescriptionEndDateTime = (Timestamp) prescription.get("end_datetime");
    Timestamp setupEndDateTime = new Timestamp(setupEndDateTimeCalender.getTimeInMillis());

    setupEndDateTime =
        setupEndDateTime.getTime() > ((Timestamp) setupdays.get("finalSetupEndDay")).getTime()
            ? setupEndDateTime
            : (Timestamp) setupdays.get("finalSetupEndDay");
    Calendar dueDateTimeCalender = Calendar.getInstance();
    dueDateTimeCalender.setTimeInMillis(prescriptionStartDateTime.getTime() > currentDate.getTime()
        ? prescriptionStartDateTime.getTime()
        : currentDate.getTime());
    Integer newActivitiesCount = 0;
    Integer prescriptionOccurences = (Integer) prescription.get("no_of_occurrences");
    String freqType = (String) prescription.get("freq_type");
    Integer recurrenceDailyId = (Integer) prescription.get("recurrence_daily_id");
    SimpleDateFormat dateHourFormatter = new SimpleDateFormat("yyyy-MM-dd HH:00:00");

    if ("F".equals(freqType) && (recurrenceDailyId == -1 || recurrenceDailyId == -3)) {
      if (exsitingActivitiesCount == 0) {
        setupActivities
            .add(getActivityMap(new Timestamp(dueDateTimeCalender.getTimeInMillis()), 1));
      }
    } else if ("F".equals(freqType)) {
      BasicDynaBean servingFrequencyBean = servingFrequencyService
          .findByRecurrenceDailyId((Integer) prescription.get("recurrence_daily_id"));
      SimpleDateFormat simpleDateformatter = new SimpleDateFormat("yyyy-MM-dd");

      // To check for late serving window
      Timestamp effectiveCurrentDateHour = Timestamp.valueOf(dateHourFormatter.format(
          dueDateTimeCalender.getTime()));

      String[] timings = ((String) servingFrequencyBean.get("timings")).split(",");
      Integer timingsSize = timings.length;
      int index = 0;
      while (true) {
        if (index >= timingsSize) {
          index = 0;
          dueDateTimeCalender.add(Calendar.DATE, 1);
        }
        String time = timings[index];
        dueDateTimeCalender.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time.split(":")[0].trim()));
        dueDateTimeCalender.set(Calendar.MINUTE, Integer.parseInt(time.split(":")[1].trim()));
        dueDateTimeCalender.set(Calendar.SECOND, 0);
        dueDateTimeCalender.set(Calendar.MILLISECOND, 0);
        Timestamp dueDate = new Timestamp(dueDateTimeCalender.getTimeInMillis());
        String dueDateString = simpleDateformatter.format(DateUtil.getDatePart(dueDate));
        Date effectiveDueDateSlot = DateUtils.addHours(dueDateTimeCalender.getTime(),
            (Integer) clinicalPreference.get("late_serving_period"));
        if ((lastActivityDueDate != null
            && (!dueDate.after(lastActivityDueDate) || simpleDateformatter
                .format(DateUtil.getDatePart(lastActivityDueDate)).equals(dueDateString)))
            || effectiveDueDateSlot.before(effectiveCurrentDateHour)) {
          index++;
          continue;
        }
        if (checkEndCondition(prescriptionEndDateTime, setupEndDateTime, dueDate,
            prescriptionOccurences, exsitingActivitiesCount, newActivitiesCount)) {
          break;
        }
        setupActivities
            .add(getActivityMap(dueDate, exsitingActivitiesCount + ++newActivitiesCount));
        index++;
      }
    } else if ("R".equals(freqType)) {
      recurrenceDailyId = null;
      Integer interval = (Integer) prescription.get("repeat_interval");
      String intervalUnits = (String) prescription.get("repeat_interval_units");
      Timestamp effectiveCurrentDateHour = Timestamp.valueOf(dateHourFormatter.format(
          currentDate));
      if (lastActivityDueDate == null) { // For Creating first activity
        Timestamp dueDate = prescriptionStartDateTime;
        dueDateTimeCalender.setTimeInMillis(dueDate.getTime());
       
        Date effectiveDueDateSlot = DateUtils.addHours(dueDateTimeCalender.getTime(),
            (Integer) clinicalPreference.get("late_serving_period"));

        if (!checkEndCondition(prescriptionEndDateTime, setupEndDateTime, dueDate,
            prescriptionOccurences, exsitingActivitiesCount, newActivitiesCount)
            && !effectiveDueDateSlot.before(effectiveCurrentDateHour)) {
          setupActivities
              .add(getActivityMap(dueDate, exsitingActivitiesCount + ++newActivitiesCount));
        }
      } else {
        dueDateTimeCalender.setTimeInMillis(lastActivityDueDate.getTime());
      }
      while (true) {
        if (interval != null && interval != 0 && intervalUnits != null
            && !intervalUnits.equals("")) {
          if (intervalUnits.equals("M")) {
            dueDateTimeCalender.add(Calendar.MINUTE, interval);
          } else if (intervalUnits.equals("H")) {
            dueDateTimeCalender.add(Calendar.HOUR, interval);
          } else if (intervalUnits.equals("D")) {
            dueDateTimeCalender.add(Calendar.DATE, interval);
          }
        } else {
          break;
        }
        Timestamp dueDate = new Timestamp(dueDateTimeCalender.getTimeInMillis());
        Date effectiveDueDateSlot = DateUtils.addHours(dueDateTimeCalender.getTime(),
            (Integer) clinicalPreference.get("late_serving_period"));
        if (dueDate.before(currentDate) && (effectiveDueDateSlot.before(
            effectiveCurrentDateHour))) {
          continue;
        }
        if (checkEndCondition(prescriptionEndDateTime, setupEndDateTime, dueDate,
            prescriptionOccurences, exsitingActivitiesCount, newActivitiesCount)) {
          break;
        }
        setupActivities
            .add(getActivityMap(dueDate, exsitingActivitiesCount + ++newActivitiesCount));
      }
    }
    BasicDynaBean lastSavedSetupDueDate =
        patientActivitiesRepository.getLastSavedSetupDueDate(prescriptionId);
    Timestamp lastSavedSetupDueDateTime =
        (Timestamp) lastSavedSetupDueDate.get("last_setup_due_date");

    Timestamp setupStartDay = (Timestamp) setupdays.get("setupStartDay");
    Date setupstartdate = new Date(setupStartDay.getTime());
    Timestamp setupEndDay = (Timestamp) setupdays.get("setupEndDay");
    Date setupEnddate = new Date(setupEndDay.getTime());
    Timestamp startdayfrom;
    Timestamp endDayto;
    try {
      startdayfrom = DateUtil
          .parseTimestamp(DataBaseUtil.dateFormatter.format(setupstartdate).toString(), "00:00");

      endDayto = DateUtil.parseTimestamp(DataBaseUtil.dateFormatter.format(setupEnddate).toString(),
          "23:59");

      List<BasicDynaBean> activities = patientActivitiesRepository.getMedicationsActivities(
          parameter.getPatientId(), startdayfrom, endDayto, prescriptionId);

      setupActivities.addAll(ConversionUtils.listBeanToListMap(activities));
      if (lastSavedSetupDueDateTime != null && !lastSavedSetupDueDateTime.before(startdayfrom)) {
        responseMap.put("lastSavedDueDate", lastSavedSetupDueDate.getMap());
      }
    } catch (ParseException ex) {
      ex.printStackTrace();
    }
    responseMap.put("activities", setupActivities);
    BasicDynaBean lastSavedSetup = marSetupService.getLastSavedSetup(prescriptionId);
    if (lastSavedSetup != null) {
      responseMap.put("lastSavedData", lastSavedSetup.getMap());
    }
    return responseMap;
  }

  /**
   * Get setup days details.
   * 
   * @param prescription prescription bean
   * @param parameter parameters
   * @param lastActivityDueDate last activity due date
   * @param exsitingActivitiesCount existing activity count
   * @param currentDayActivityCount current day activity count
   * @param advanceSetupPeriod advance setup period
   * @param maximumMedicationSetupDays maximum medication setup days
   * @return map of details
   */
  private Map<String, Object> getSetupDaysDetails(BasicDynaBean prescription,
      FormParameter parameter, Timestamp lastActivityDueDate, Integer exsitingActivitiesCount,
      Integer currentDayActivityCount, Integer advanceSetupPeriod,
      Integer maximumMedicationSetupDays, Calendar setupDayEndDateTimeCalender) {
    Timestamp prescriptionStartDateTime = (Timestamp) prescription.get("start_datetime");
    Timestamp prescriptionEndDateTime = (Timestamp) prescription.get("end_datetime");
    Date currentDate = new Date();

    Calendar setupEndDayTimeCalender = Calendar.getInstance();

    Integer newActivitiesCount = 0;
    Integer prescriptionOccurences = (Integer) prescription.get("no_of_occurrences");
    Integer recurrenceDailyId = (Integer) prescription.get("recurrence_daily_id");
    String freqType = (String) prescription.get("freq_type");
    String endOnDiscontinue = (String) prescription.get("end_on_discontinue");

    Calendar dueDateTimeCalender = Calendar.getInstance();
    dueDateTimeCalender.setTimeInMillis(prescriptionStartDateTime.getTime() > currentDate.getTime()
        ? prescriptionStartDateTime.getTime()
        : currentDate.getTime());

    Calendar dueDateTimeDaysCalender = dueDateTimeCalender;

    Timestamp setupStartDaysTime =
        (Timestamp) (prescriptionStartDateTime.getTime() > currentDate.getTime()
            ? prescriptionStartDateTime
            : DateUtil.getCurrentTimestamp());

    boolean isEndOnDiscontinueType = endOnDiscontinue != null && endOnDiscontinue.equals("Y");
    if ("F".equals(freqType) && recurrenceDailyId == -3) {
      // continous flow for IV
      if (exsitingActivitiesCount == 0) {
        setupEndDayTimeCalender = dueDateTimeDaysCalender;
      } else {
        if (prescriptionStartDateTime.getTime() > currentDate.getTime()) {
          setupEndDayTimeCalender.setTimeInMillis(lastActivityDueDate.getTime());
        }
      }
    } else if (prescriptionStartDateTime != null && prescriptionEndDateTime != null) {
      setupEndDayTimeCalender.setTimeInMillis(prescriptionEndDateTime.getTime());
    } else if (prescriptionOccurences != null) {
      if ("F".equals(freqType) && recurrenceDailyId == -1) {
        if (exsitingActivitiesCount == 0) {
          setupEndDayTimeCalender = dueDateTimeDaysCalender;
        } else {
          setupEndDayTimeCalender.setTimeInMillis(lastActivityDueDate.getTime());
        }
      } else if ("F".equals(freqType)) {
        BasicDynaBean servingFrequencyBean =
            servingFrequencyService.findByRecurrenceDailyId(recurrenceDailyId);
        if (servingFrequencyBean == null) {
          ValidationErrorMap errMap = new ValidationErrorMap();
          errMap.addError("others", "exception.mar.setup.serving.ferquency.notexists");
          throw new ValidationException(errMap);
        }
        String[] timings = ((String) servingFrequencyBean.get("timings")).split(",");
        Integer timingsSize = timings.length;
        int index = 0;
        while (true) {
          if (index >= timingsSize) {
            index = 0;
            dueDateTimeDaysCalender.add(Calendar.DATE, 1);
          }
          String time = timings[index];
          dueDateTimeDaysCalender.set(Calendar.HOUR_OF_DAY,
              Integer.parseInt(time.split(":")[0].trim()));
          dueDateTimeDaysCalender.set(Calendar.MINUTE, Integer.parseInt(time.split(":")[1].trim()));
          dueDateTimeDaysCalender.set(Calendar.SECOND, 0);
          dueDateTimeDaysCalender.set(Calendar.MILLISECOND, 0);
          Timestamp dayDueDate = new Timestamp(dueDateTimeDaysCalender.getTimeInMillis());
          if ((lastActivityDueDate != null && !dayDueDate.after(lastActivityDueDate))
              || dayDueDate.before(currentDate)) {
            index++;
            continue;
          }
          if (checkEndCondition(prescriptionEndDateTime, null, dayDueDate, prescriptionOccurences,
              exsitingActivitiesCount, newActivitiesCount)) {
            if (prescriptionOccurences <= exsitingActivitiesCount) {
              setupEndDayTimeCalender.setTimeInMillis(lastActivityDueDate.getTime());
            }
            break;
          }
          newActivitiesCount++;
          index++;
          setupEndDayTimeCalender.setTimeInMillis(dueDateTimeDaysCalender.getTimeInMillis());
        }
      } else if ("R".equals(freqType)) {
        recurrenceDailyId = null;
        Integer interval = (Integer) prescription.get("repeat_interval");
        String intervalUnits = (String) prescription.get("repeat_interval_units");
        if (lastActivityDueDate == null) { // For Creating first activity
          Timestamp dueDate = prescriptionStartDateTime;
          dueDateTimeCalender.setTimeInMillis(dueDate.getTime());
          if (!checkEndCondition(prescriptionEndDateTime, null, dueDate, prescriptionOccurences,
              exsitingActivitiesCount, newActivitiesCount)) {
            newActivitiesCount = exsitingActivitiesCount + ++newActivitiesCount;
          }
        } else {
          dueDateTimeDaysCalender.setTimeInMillis(lastActivityDueDate.getTime());
        }
        setupEndDayTimeCalender.setTimeInMillis(dueDateTimeDaysCalender.getTimeInMillis());
        while (true) {
          if (interval != null && interval != 0 && intervalUnits != null
              && !intervalUnits.equals("")) {
            if (intervalUnits.equals("M")) {
              dueDateTimeDaysCalender.add(Calendar.MINUTE, interval);
            } else if (intervalUnits.equals("H")) {
              dueDateTimeDaysCalender.add(Calendar.HOUR, interval);
            } else if (intervalUnits.equals("D")) {
              dueDateTimeDaysCalender.add(Calendar.DATE, interval);
            }
          } else {
            break;
          }
          Timestamp dueDate = new Timestamp(dueDateTimeDaysCalender.getTimeInMillis());
          if (dueDate.before(currentDate)) {
            continue;
          }
          if (checkEndCondition(prescriptionEndDateTime, null, dueDate, prescriptionOccurences,
              exsitingActivitiesCount, newActivitiesCount)) {
            if (prescriptionOccurences.equals(exsitingActivitiesCount)) {
              setupEndDayTimeCalender.setTimeInMillis(lastActivityDueDate.getTime());
            }
            break;
          }
          newActivitiesCount++;
          setupEndDayTimeCalender.setTimeInMillis(dueDateTimeDaysCalender.getTimeInMillis());
        }
      }
    } else if (isEndOnDiscontinueType) {
      setupEndDayTimeCalender.setTime(setupStartDaysTime);
      setupEndDayTimeCalender.add(Calendar.DATE, maximumMedicationSetupDays);
      setupEndDayTimeCalender.set(Calendar.HOUR_OF_DAY, 23);
      setupEndDayTimeCalender.set(Calendar.MINUTE, 59);
    }

    Map<String, Object> setupdays = new HashMap<>();

    setupdays.put("setupStartDay", setupStartDaysTime);

    Calendar maxMedicationCalender = Calendar.getInstance();
    maxMedicationCalender.setTime(setupStartDaysTime);
    maxMedicationCalender.add(Calendar.DATE, maximumMedicationSetupDays);

    if (isEndOnDiscontinueType) {
      setupdays.put("setupEndDay", new Timestamp(setupEndDayTimeCalender.getTimeInMillis()));
    } else {
      setupdays.put("setupEndDay",
          new Timestamp(
              setupEndDayTimeCalender.getTimeInMillis() > maxMedicationCalender.getTimeInMillis()
                  ? maxMedicationCalender.getTimeInMillis()
                  : setupEndDayTimeCalender.getTimeInMillis()));
    }

    setupdays.put("finalSetupEndDay", new Timestamp(setupEndDayTimeCalender.getTimeInMillis()));
    return setupdays;
  }

  private Map<String, Object> getActivityMap(Timestamp dueDate, int activityNumber) {
    Map<String, Object> activityMap = new HashMap<>();
    activityMap.put("due_date", dueDate);
    return activityMap;
  }

  /**
   * Stop a medication that is part of MAR.
   * 
   * @param patientId visit identifier
   * @param prescriptionId prescription identifier
   * @param requestBody request body
   * @return map containing response
   */
  public Map<String, Object> stopMedication(String patientId, Integer prescriptionId,
      ModelMap requestBody) {
    ValidationErrorMap errMap = new ValidationErrorMap();
    BasicDynaBean prescriptionBean = prescriptionsService.findById(prescriptionId);
    if (prescriptionBean == null || !patientId.equals(prescriptionBean.get("visit_id"))) {
      errMap.addError("prescription_id", "exception.patient.id.prescription.id.miss.match");
      throw new ValidationException(errMap);
    }
    if (requestBody.get("stopped_reason") == null || requestBody.get("stopped_reason").equals("")) {
      errMap.addError("stopped_reason", "exception.mar.stopped.medication.reason.mandatory");
      throw new ValidationException(errMap);
    }
    Timestamp stoppedDate = null;
    if (requestBody.get("stopped_date") != null && !requestBody.get("stopped_date").equals("")) {
      try {
        stoppedDate = new Timestamp(new DateUtil().getTimeStampFormatter()
            .parse((String) requestBody.get("stopped_date")).getTime());
      } catch (ParseException exe) {
        logger.error("Exception Occured While Saving MarSetup " + exe);
      }
    }
    prescriptionBean.set("discontinued", "Y");

    Map<String, Object> presUpdateKeys = new HashMap<String, Object>();
    presUpdateKeys.put("patient_presc_id", prescriptionId);
    prescriptionsRepository.update(prescriptionBean, presUpdateKeys);
    patientActivitiesService.cancelActivity(prescriptionId, "M");
    BasicDynaBean medicinePrescBean = patientMedicinePrescriptionsService.getBean();
    ConversionUtils.copyToDynaBean(requestBody, medicinePrescBean);
    medicinePrescBean.set("stopped_date", stoppedDate);
    Map<String, Object> updateKeyMap = new HashMap<String, Object>();
    updateKeyMap.put("op_medicine_pres_id", prescriptionId);
    patientMedicinePrescriptionsService.update(medicinePrescBean, updateKeyMap);
    Map<String, Object> response = new HashMap<>();
    return response;
  }

}
