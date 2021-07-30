package com.insta.hms.core.scheduler.resourcelist;

import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.core.patient.PatientDetailsService;
import com.insta.hms.core.patient.registration.PatientInsurancePlansService;
import com.insta.hms.core.patient.registration.RegistrationService;
import com.insta.hms.core.scheduler.AppointmentCategory;
import com.insta.hms.core.scheduler.AppointmentCategoryFactory;
import com.insta.hms.core.scheduler.AppointmentService;
import com.insta.hms.core.scheduler.AppointmentValidator;
import com.insta.hms.core.scheduler.ResourceCategory;
import com.insta.hms.core.scheduler.ResourceService;
import com.insta.hms.core.scheduler.SchedulerResourceTypesRepository;
import com.insta.hms.exception.EntityNotFoundException;
import com.insta.hms.exception.NestableValidationException;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.mdm.appointmentsources.AppointmentSourceService;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.mdm.complainttypes.ComplaintTypesService;
import com.insta.hms.mdm.resourceavailability.ResourceAvailabilityService;
import com.insta.hms.mdm.salutations.SalutationService;
import com.insta.hms.mdm.savedsearches.SavedSearchService;
import com.insta.hms.security.usermanager.UserService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.io.UnsupportedEncodingException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class SchedulerResourceSearchService.
 */
@Service
public class SchedulerResourceSearchService {
  
  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(SchedulerResourceSearchService.class);

  /** The saved search service. */
  @LazyAutowired
  private SavedSearchService savedSearchService;
  
  /** The user service. */
  @LazyAutowired
  private UserService userService;
  
  /** The res service. */
  @LazyAutowired
  private ResourceService resService;
  
  /** The generic preferences service. */
  @LazyAutowired
  private GenericPreferencesService genericPreferencesService;
  
  /** The appointment category factory. */
  @LazyAutowired
  private AppointmentCategoryFactory appointmentCategoryFactory;
  
  /** The resource availability service. */
  @LazyAutowired
  private ResourceAvailabilityService resourceAvailabilityService;
  
  /** The scheduler resource repository. */
  @LazyAutowired
  private SchedulerResourceRepository schedulerResourceRepository;
  
  /** The appointment source service. */
  @LazyAutowired
  private AppointmentSourceService appointmentSourceService;
  
  /** The appointment service. */
  @LazyAutowired
  private AppointmentService appointmentService;
  
  /** The scheduler resource types repository. */
  @LazyAutowired
  private SchedulerResourceTypesRepository schedulerResourceTypesRepository;
  
  /** The appointment validator. */
  @LazyAutowired
  private AppointmentValidator appointmentValidator;
  
  /** The registration service. */
  @LazyAutowired
  private RegistrationService registrationService;
  
  /** The patient insurance plans service. */
  @LazyAutowired
  private PatientInsurancePlansService patientInsurancePlansService;
  
  /** The center service. */
  @LazyAutowired
  private CenterService centerService;
  
  /** The complaint types service. */
  @LazyAutowired
  private ComplaintTypesService complaintTypesService;
  
  /** The message util. */
  @LazyAutowired
  private MessageUtil messageUtil;

  /** The salutation service. */
  @LazyAutowired
  private SalutationService salutationService;
  
  /** The patient details service. */
  @LazyAutowired
  private PatientDetailsService patientDetailsService;
  
  /**
   * Parses the bool.
   *
   * @param value the value
   * @return true, if successful
   */
  private boolean parseBool(String value) {
    return value != null && Arrays.asList("true", "y", "yes", "1").contains(value.toLowerCase());
  }

  /**
   * Gets the secondary resources list.
   *
   * @param params the params
   * @param advanced the advanced
   * @return the secondary resources list
   * @throws UnsupportedEncodingException the unsupported encoding exception
   */
  public Map<String, Object> getSecondaryResourcesList(Map<String, String[]> params,
      boolean advanced) throws UnsupportedEncodingException {
    String searchId = params.get("search_id") != null ? params.get("search_id")[0] : null;
    Map<String, String[]> savedParams = new HashMap<String, String[]>();
    boolean defaultSearch = params.get("use_default_search") != null
        ? parseBool(params.get("use_default_search")[0])
        : false;
    if (searchId == null && defaultSearch) {
      savedParams = savedSearchService.getDefaultSearch("Scheduler");
      if (savedParams == null) {
        throw new EntityNotFoundException(
            new String[] { "exception.entity.not.found", "search_id", "default_search" });
      }
      savedParams.put("is_system_search", new String[] { "true" });
    } else if (searchId != null) {
      savedParams = savedSearchService.splitQuery(Integer.parseInt(searchId));
      if (savedParams == null) {
        throw new EntityNotFoundException(
            new String[] { "exception.entity.not.found", "search_id", searchId });
      }
      Map<String, Object> savedSearch = savedSearchService
          .getSavedSearch(Integer.parseInt(searchId));
      savedParams.put("is_system_search",
          new String[] {
              savedSearch.get("search_type").toString().equalsIgnoreCase("system") ? "true"
                  : "false" });
    }
    Integer centerId = RequestContext.getCenterId();
    Integer centersIncDefault = (Integer) genericPreferencesService.getAllPreferences()
        .get("max_centers_inc_default");
    savedParams.put("center_id", new String[] { centerId.toString() });
    savedParams.putAll(params);
    savedParams.put("max_centers_inc_default", new String[] { centersIncDefault.toString() });
    BasicDynaBean loggedInUser = userService.getLoggedUser();
    String doctor = (String) loggedInUser.get("doctor_id");
    String loginControlsApplicable = (String) loggedInUser.get("login_controls_applicable");
    if (loggedInUser.get("scheduler_dept_id") != null
        && !loggedInUser.get("scheduler_dept_id").equals("")) {
      savedParams.put("departments", new String[]{((String)
          loggedInUser.get("scheduler_dept_id"))});
    }
    if (doctor != null && !doctor.isEmpty()) {
      savedParams.put("doctor", new String[] { doctor });
      savedParams.put("user_is_doctor", new String[] { "Y" });
    }
    if (loginControlsApplicable.equalsIgnoreCase("Y")) {
      savedParams.put("login_controls_applicable", new String[] { "Y" });
    }
    String category = params.get("res_sch_category") != null ? params.get("res_sch_category")[0]
        : ResourceCategory.DOC.name();
    category = category.toUpperCase(Locale.ENGLISH);
    AppointmentCategory appointmentCategory = appointmentCategoryFactory.getInstance(category);
    // Get the resources list
    Map<String, Object> schedulerResources;
    List<Map<String, Object>> resourcesList;
    int pageNum = params.get("page_num") != null ? Integer.parseInt(params.get("page_num")[0]) : 1;
    if (appointmentCategory.getCategory().equals("DOC") && doctor != null && !doctor.isEmpty()
        && !loginControlsApplicable.equalsIgnoreCase("Y") && pageNum == 1) {
      savedParams.put("login_controls_applicable", new String[] { "Y" });
      schedulerResources = schedulerResourceRepository
          .getSchedulerSecondaryResources(appointmentCategory, savedParams, advanced);
      resourcesList = (List<Map<String, Object>>) schedulerResources.get("resources");
      savedParams.put("login_controls_applicable", new String[] { "N" });
      schedulerResources = schedulerResourceRepository
          .getSchedulerSecondaryResources(appointmentCategory, savedParams, advanced);
      resourcesList.addAll((List<Map<String, Object>>) schedulerResources.get("resources"));
    } else {
      schedulerResources = schedulerResourceRepository
          .getSchedulerSecondaryResources(appointmentCategory, savedParams, advanced);
      resourcesList = (List<Map<String, Object>>) schedulerResources.get("resources");
    }
    // Get the visit timings (default availabilities) for fetched resources
    // Key - resourceId, Value - Visit timings for resourceId
    Map<String, Map<Integer, Object>> visitTimingsMap = new HashMap<String, Map<Integer, Object>>();
    for (Map<String, Object> resource : resourcesList) {
      String resourceId = (String) resource.get("resource_id");
      // TODO:: can be optimized to return only the fields we require
      List<BasicDynaBean> visitTimingsList = resourceAvailabilityService
          .getDefaultResourceAvailabilities(resourceId, null, category, null, null);
      if (visitTimingsList.isEmpty()) {
        visitTimingsList = resourceAvailabilityService.getDefaultResourceAvailabilities("*", null,
            category, null, null);
      }
      if (!visitTimingsList.isEmpty() && centersIncDefault != 1) {
        visitTimingsList = appointmentCategory.filterVisitTimingsByCenter(visitTimingsList,
            centerId);
      }
      for (BasicDynaBean visitTiming : visitTimingsList) {
        Map<Integer, Object> visitTimingsForResource = visitTimingsMap.get(resourceId);
        visitTimingsForResource = (visitTimingsForResource == null) ? new HashMap<Integer, Object>()
            : visitTimingsForResource;
        int dayOfWeek = (Integer) visitTiming.get("day_of_week");
        List<Map<String, Object>> visitTimingsListForDayOfWeek = (visitTimingsForResource
            .get(dayOfWeek) != null)
                ? (List<Map<String, Object>>) visitTimingsForResource.get(dayOfWeek)
                : new ArrayList<Map<String, Object>>();
        visitTimingsListForDayOfWeek.add(visitTiming.getMap());
        visitTimingsForResource.put(dayOfWeek, visitTimingsListForDayOfWeek);
        visitTimingsMap.put(resourceId, visitTimingsForResource);
      }
    }
    // Construct the new map with all resource scheduler information
    // including visit timings
    Map<String, Object> resourcesWithApptsAndOverrides = new HashMap<String, Object>();
    List<Map<String, Object>> resourcesWithApptsAndOverridesList =
        new ArrayList<Map<String, Object>>();
    resourcesWithApptsAndOverrides.putAll(schedulerResources);
    for (Map<String, Object> resource : resourcesList) {
      Map<String, Object> newResource = new HashMap<String, Object>();
      newResource.putAll(resource);
      String resourceId = (String) resource.get("resource_id");
      newResource.put("visit_timings", visitTimingsMap.get(resourceId));
      newResource.put("default_additional_resources", 
          getDefaultAdditionalResources(category,resourceId,centersIncDefault,centerId));
      resourcesWithApptsAndOverridesList.add(newResource);
    }
    resourcesWithApptsAndOverrides.put("resources", resourcesWithApptsAndOverridesList);
    boolean additionalResTypesReqd = params.get("additional_restypes_reqd") != null
        ? parseBool(params.get("additional_restypes_reqd")[0])
        : false;
    // return additional resource types
    if (additionalResTypesReqd) {
      resourcesWithApptsAndOverrides.put("additional_resource_types", 
          getAdditionalResourcesTypes(category));
    }
    return resourcesWithApptsAndOverrides;
  }

  /**
   * Gets the default additional resources.
   *
   * @param params
   *          the params
   * @return the default additional resources
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public Map getDefaultAdditionalResources(Map<String, String[]> params) {

    Map resultMap = new HashMap();
    Integer centerId = Integer.parseInt(params.get("center_id")[0]);
    String category = params.get("category")[0];
    String resourceId = params.get("resource_id")[0];
    Integer centersIncDefault = (Integer) genericPreferencesService
        .getAllPreferences().get("max_centers_inc_default");
    resultMap.put("default_additional_resources",
        getDefaultAdditionalResources(category, 
            resourceId, centersIncDefault, centerId));
    return resultMap;
  }

  /**
   * Gets the default additional resources.
   *
   * @param category
   *          the category
   * @param resourceId
   *          the resource id
   * @param centersIncDefault
   *          the centers inc default
   * @param centerId
   *          the center id
   * @return the default additional resources
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public List<Map> getDefaultAdditionalResources(String category, String resourceId,
      Integer centersIncDefault, Integer centerId) {
    List defaultReslist = new ArrayList<Map<String, Object>>();
    List<BasicDynaBean> defaultResourcesList = schedulerResourceTypesRepository
        .getDefaultResourceList(category, resourceId, centersIncDefault, centerId);
    if (defaultResourcesList != null && defaultResourcesList.size() > 0) {
      for (BasicDynaBean defaultresourceType : defaultResourcesList) {
        defaultReslist.add(defaultresourceType.getMap());
      }
    }
    return defaultReslist;
  }
  
  /**
   * Gets the additional resources types.
   *
   * @param category the category
   * @return the additional resources types
   */
  public List<Object> getAdditionalResourcesTypes(String category) {
    List<Object> list = null;
    List<BasicDynaBean> resourceTypesList = schedulerResourceTypesRepository
        .getAdditionalResourceTypes(category);
    if (resourceTypesList != null && resourceTypesList.size() > 0) {
      list = new ArrayList<Object>();
      for (BasicDynaBean resourceType : resourceTypesList) {
        list.add(resourceType.getMap());
      }
    }
    return list;
  }

  /**
   * Gets the metadata.
   *
   * @param params map of request parameters
   * @param userLangCode Current User Interface Language
   * @return the metadata
   */
  public Map<String, Object> getMetadata(Map<String, String[]> params, String userLangCode) {
    String category = ((String[]) params.get("res_sch_category"))[0];
    List<BasicDynaBean> statusList = appointmentService.getAppointmentStatusList(category);
    Map<String, Object> resultMap = new HashMap<String, Object>();
    List<Object> list = new ArrayList<Object>();
    if (statusList != null && statusList.size() > 0) {
      for (BasicDynaBean status : statusList) {
        list.add(status.getMap());
      }
    }
    resultMap.put("appointment_status", list);
    list = new ArrayList<Object>();
    List<BasicDynaBean> apptSourcesList = appointmentSourceService.getActiveAppointmentSources();
    if (apptSourcesList != null && apptSourcesList.size() > 0) {
      for (BasicDynaBean apptSource : apptSourcesList) {
        list.add(apptSource.getMap());
      }
    }
    resultMap.put("appointment_source", list);

    // get the salutation
    resultMap.put("salutations",
        ConversionUtils.listBeanToListMap(salutationService.lookup(true)));

    // get the preferred language
    resultMap.put("preferred_languages", patientDetailsService.getPreferredLanguages(userLangCode));

    Map<String, String> dailyMap = new HashMap<String, String>();
    dailyMap.put("value", "D");
    dailyMap.put("text", "Daily");

    Map<String, String> weeklyMap = new HashMap<String, String>();
    weeklyMap.put("value", "W");
    weeklyMap.put("text", "Weekly");

    Map<String, String> monthlyMap = new HashMap<String, String>();
    monthlyMap.put("value", "M");
    monthlyMap.put("text", "Monthly");

    Map<String, String> yearlyMap = new HashMap<String, String>();
    yearlyMap.put("value", "Y");
    yearlyMap.put("text", "Yearly");

    List<Map<String, String>> repeatsList = new ArrayList<Map<String, String>>();
    repeatsList.add(dailyMap);
    repeatsList.add(weeklyMap);
    repeatsList.add(monthlyMap);
    repeatsList.add(yearlyMap);
    resultMap.put("repeats", repeatsList);

    List<Map<String, String>> yearList = new ArrayList<Map<String, String>>();
    Map<String, String> yearMap = new HashMap<String, String>();
    yearMap.put("text", "1 Year");
    yearMap.put("value", "1");
    yearList.add(yearMap);
    yearMap = new HashMap<String, String>();
    yearMap.put("text", "2 Year");
    yearMap.put("value", "2");
    yearList.add(yearMap);
    yearMap = new HashMap<String, String>();
    yearMap.put("text", "3 Year");
    yearMap.put("value", "3");
    yearList.add(yearMap);
    yearMap = new HashMap<String, String>();
    yearMap.put("text", "4 Year");
    yearMap.put("value", "4");
    yearList.add(yearMap);
    yearMap = new HashMap<String, String>();
    yearMap.put("text", "5 Year");
    yearMap.put("value", "5");
    yearList.add(yearMap);
    
    Map<String, Object> repeatsEveryMap = new HashMap<String, Object>();
    repeatsEveryMap.put("Y", yearList);

    Map<String, String> monthMap = new HashMap<String, String>();
    List<Map<String, String>> monthList = new ArrayList<Map<String, String>>();
    monthMap.put("text", "1 Month");
    monthMap.put("value", "1");
    monthList.add(monthMap);
    monthMap = new HashMap<String, String>();
    monthMap.put("text", "2 Month");
    monthMap.put("value", "2");
    monthList.add(monthMap);
    monthMap = new HashMap<String, String>();
    monthMap.put("text", "3 Month");
    monthMap.put("value", "3");
    monthList.add(monthMap);
    monthMap = new HashMap<String, String>();
    monthMap.put("text", "4 Month");
    monthMap.put("value", "4");
    monthList.add(monthMap);
    monthMap = new HashMap<String, String>();
    monthMap.put("text", "5 Month");
    monthMap.put("value", "5");
    monthList.add(monthMap);
    monthMap = new HashMap<String, String>();
    monthMap.put("text", "6 Month");
    monthMap.put("value", "6");
    monthList.add(monthMap);
    monthMap = new HashMap<String, String>();
    monthMap.put("text", "7 Month");
    monthMap.put("value", "7");
    monthList.add(monthMap);
    monthMap = new HashMap<String, String>();
    monthMap.put("text", "8 Month");
    monthMap.put("value", "8");
    monthList.add(monthMap);
    monthMap = new HashMap<String, String>();
    monthMap.put("text", "9 Month");
    monthMap.put("value", "9");
    monthList.add(monthMap);
    monthMap = new HashMap<String, String>();
    monthMap.put("text", "10 Month");
    monthMap.put("value", "10");
    monthList.add(monthMap);
    monthMap = new HashMap<String, String>();
    monthMap.put("text", "11 Month");
    monthMap.put("value", "11");
    monthList.add(monthMap);
    monthMap = new HashMap<String, String>();
    monthMap.put("text", "12 Month");
    monthMap.put("value", "12");
    monthList.add(monthMap);
    repeatsEveryMap.put("M", monthList);

    Map<String, String> weekMap = new HashMap<String, String>();
    List<Map<String, String>> weekList = new ArrayList<Map<String, String>>();
    weekMap.put("text", "1 Week");
    weekMap.put("value", "1");
    weekList.add(weekMap);
    weekMap = new HashMap<String, String>();
    weekMap.put("text", "2 Week");
    weekMap.put("value", "2");
    weekList.add(weekMap);
    weekMap = new HashMap<String, String>();
    weekMap.put("text", "3 Week");
    weekMap.put("value", "3");
    weekList.add(weekMap);
    weekMap = new HashMap<String, String>();
    weekMap.put("text", "4 Week");
    weekMap.put("value", "4");
    weekList.add(weekMap);
    repeatsEveryMap.put("W", weekList);

    Map<String, String> dayMap = new HashMap<String, String>();
    List<Map<String, String>> dayList = new ArrayList<Map<String, String>>();
    dayMap.put("text", "1 Day");
    dayMap.put("value", "1");
    dayList.add(dayMap);
    dayMap = new HashMap<String, String>();
    dayMap.put("text", "2 Day");
    dayMap.put("value", "2");
    dayList.add(dayMap);
    dayMap = new HashMap<String, String>();
    dayMap.put("text", "3 Day");
    dayMap.put("value", "3");
    dayList.add(dayMap);
    dayMap = new HashMap<String, String>();
    dayMap.put("text", "4 Day");
    dayMap.put("value", "4");
    dayList.add(dayMap);
    dayMap = new HashMap<String, String>();
    dayMap.put("text", "5 Day");
    dayMap.put("value", "5");
    dayList.add(dayMap);
    dayMap = new HashMap<String, String>();
    dayMap.put("text", "6 Day");
    dayMap.put("value", "6");
    dayList.add(dayMap);
    dayMap = new HashMap<String, String>();
    dayMap.put("text", "7 Day");
    dayMap.put("value", "7");
    dayList.add(dayMap);
    dayMap = new HashMap<String, String>();
    dayMap.put("text", "8 Day");
    dayMap.put("value", "8");
    dayList.add(dayMap);
    dayMap = new HashMap<String, String>();
    dayMap.put("text", "9 Day");
    dayMap.put("value", "9");
    dayList.add(dayMap);
    dayMap = new HashMap<String, String>();
    dayMap.put("text", "10 Day");
    dayMap.put("value", "10");
    dayList.add(dayMap);
    dayMap = new HashMap<String, String>();
    dayMap.put("text", "11 Day");
    dayMap.put("value", "11");
    dayList.add(dayMap);
    dayMap = new HashMap<String, String>();
    dayMap.put("text", "12 Day");
    dayMap.put("value", "12");
    dayList.add(dayMap);
    dayMap = new HashMap<String, String>();
    dayMap.put("text", "13 Day");
    dayMap.put("value", "13");
    dayList.add(dayMap);
    dayMap = new HashMap<String, String>();
    dayMap.put("text", "14 Day");
    dayMap.put("value", "14");
    dayList.add(dayMap);
    dayMap = new HashMap<String, String>();
    dayMap.put("text", "15 Day");
    dayMap.put("value", "15");
    dayList.add(dayMap);
    dayMap = new HashMap<String, String>();
    dayMap.put("text", "16 Day");
    dayMap.put("value", "16");
    dayList.add(dayMap);
    dayMap = new HashMap<String, String>();
    dayMap.put("text", "17 Day");
    dayMap.put("value", "17");
    dayList.add(dayMap);
    dayMap = new HashMap<String, String>();
    dayMap.put("text", "18 Day");
    dayMap.put("value", "18");
    dayList.add(dayMap);
    dayMap = new HashMap<String, String>();
    dayMap.put("text", "19 Day");
    dayMap.put("value", "19");
    dayList.add(dayMap);
    dayMap = new HashMap<String, String>();
    dayMap.put("text", "20 Day");
    dayMap.put("value", "20");
    dayList.add(dayMap);
    dayMap = new HashMap<String, String>();
    dayMap.put("text", "21 Day");
    dayMap.put("value", "21");
    dayList.add(dayMap);
    dayMap = new HashMap<String, String>();
    dayMap.put("text", "22 Day");
    dayMap.put("value", "22");
    dayList.add(dayMap);
    dayMap = new HashMap<String, String>();
    dayMap.put("text", "23 Day");
    dayMap.put("value", "23");
    dayList.add(dayMap);
    dayMap = new HashMap<String, String>();
    dayMap.put("text", "24 Day");
    dayMap.put("value", "24");
    dayList.add(dayMap);
    dayMap = new HashMap<String, String>();
    dayMap.put("text", "25 Day");
    dayMap.put("value", "25");
    dayList.add(dayMap);
    dayMap = new HashMap<String, String>();
    dayMap.put("text", "26 Day");
    dayMap.put("value", "26");
    dayList.add(dayMap);
    dayMap = new HashMap<String, String>();
    dayMap.put("text", "27 Day");
    dayMap.put("value", "27");
    dayList.add(dayMap);
    dayMap = new HashMap<String, String>();
    dayMap.put("text", "28 Day");
    dayMap.put("value", "28");
    dayList.add(dayMap);
    dayMap = new HashMap<String, String>();
    dayMap.put("text", "29 Day");
    dayMap.put("value", "29");
    dayList.add(dayMap);
    dayMap = new HashMap<String, String>();
    dayMap.put("text", "30 Day");
    dayMap.put("value", "30");
    dayList.add(dayMap);
    dayMap = new HashMap<String, String>();
    dayMap.put("text", "31 Day");
    dayMap.put("value", "31");
    dayList.add(dayMap);
    repeatsEveryMap.put("D", dayList);

    resultMap.put("repeats_every", repeatsEveryMap);

    // adding center defaults
    Map<String, Object> keyMap = new HashMap<String, Object>();
    Integer centerId = RequestContext.getCenterId();
    keyMap.put("center_id", centerId);
    BasicDynaBean centerBean = centerService.findByPk(keyMap, true);
    if (centerId != 0) {
      keyMap.put("center_id", 0);
      BasicDynaBean defaultCenterBean = centerService.findByPk(keyMap, true);
      if (centerBean.get("country_code") == null) {
        centerBean.set("country_id", defaultCenterBean.get("country_id"));
        centerBean.set("country_code", defaultCenterBean.get("country_code"));
      }
    }
    resultMap.put("center_defaults", centerBean.getMap());

    // visit types
    Map<String, String> visitTypeMap = new HashMap<>();
    visitTypeMap.put("OPE", messageUtil.getMessage("js.registration.patient.patient.surgery"));
    visitTypeMap.put("SNP", messageUtil.getMessage("js.registration.patient.patient.service"));
    visitTypeMap.put("DIA", messageUtil.getMessage("js.registration.patient.patient.test"));
    visitTypeMap.put("DOC", messageUtil.getMessage("js.registration.patient.patient.consultation"));
    resultMap.put("visit_types", visitTypeMap);
    return resultMap;
  }

  /**
   * Gets the sponsor info for last active visit.
   *
   * @param params the params
   * @return the sponsor info for last active visit
   */
  public Map<String, Object> getSponsorInfoForLastActiveVisit(Map<String, String[]> params) {

    String mrNo = params.get("mr_no") != null ? params.get("mr_no")[0] : null;
    Integer centerId = RequestContext.getCenterId();
    // Get the latest active visit bean
    BasicDynaBean visitBean = registrationService.getPatientLatestVisit(mrNo, true, null, centerId);
    // if active visit not there then get the inactive visit bean
    if (visitBean == null) {
      visitBean = registrationService.getPatientLatestVisit(mrNo, false, null, centerId);
    }
    Map<String, Object> resultMap = new HashMap<String, Object>();
    List<Object> list = new ArrayList<Object>();
    if (visitBean != null) {
      String visitId = (String) visitBean.get("patient_id");
      String visitType = (String) visitBean.get("visit_type");
      List<BasicDynaBean> plansList = patientInsurancePlansService.getInsuranceDetails(visitId,
          visitType);
      if (plansList != null && !plansList.isEmpty()) {
        for (BasicDynaBean planDetails : plansList) {
          list.add(planDetails.getMap());
        }
      }
    }
    resultMap.put("insurance_plan_details", list);
    return resultMap;
  }

  /**
   * Gets the additional resources by resource type.
   *
   * @param params the params
   * @return the additional resources by resource type
   */
  public Map<String, Object> getAdditionalResourcesByResourceType(Map<String, String[]> params) {
    logger.debug(
        "starting  getAdditionalResourcesByResourceType method" + DateUtil.getCurrentTimestamp());
    List<Object> list = new ArrayList<Object>();
    ValidationErrorMap validationErrors = new ValidationErrorMap();
    Map<String, Object> nestedException = new HashMap<String, Object>();
    if (!appointmentValidator.validateAdditionalResourcesParams(params, validationErrors)) {
      ValidationException ex = new ValidationException(validationErrors);
      nestedException.put("resources", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    String apptDate = (String) params.get("date")[0];
    String slotTime = (String) params.get("slot_time")[0];
    Integer duration = Integer.parseInt(params.get("duration")[0]);
    String visitMode = params.get("visit_mode") != null
        ? (String)params.get("visit_mode")[0] : null;
    Integer apptId = (params.get("appointment_id") != null
        && (params.get("appointment_id")).length > 0 
        && !params.get("appointment_id")[0].equals(""))
            ? Integer.parseInt(params.get("appointment_id")[0]) : null;
    String apptIdStr = null;
    if (apptId != null && apptId > 0) {
      apptIdStr = apptId.toString();
    }
    String timestampStr = apptDate + " " + slotTime;
    Timestamp apptTime = null;
    try {
      DateUtil dateUtil = new DateUtil();
      apptTime = dateUtil.parseTheTimestamp(timestampStr);
    } catch (ParseException pe) {
      validationErrors.addError("date", "scheduler.validation.error.invalidDataTime");
      throw new ValidationException(validationErrors);
    }
    long appointmentTimeLong = apptTime.getTime();
    appointmentTimeLong = appointmentTimeLong + (duration * 60 * 1000);
    Timestamp apptEndTime = new java.sql.Timestamp(appointmentTimeLong);
    String category = params.get("res_sch_category") != null ? params.get("res_sch_category")[0]
        : ResourceCategory.DOC.name();
    AppointmentCategory appointmentCategory = appointmentCategoryFactory
        .getInstance(category.toUpperCase(Locale.ENGLISH));
    String[] resTypes = ((String[]) params.get("res_types"));
    Integer centerId = params.get("center_id") != null
        ? Integer.parseInt(params.get("center_id")[0])
        : 0;
    List<BasicDynaBean> schedulerResources = schedulerResourceRepository
        .getAdditionalResourcesByResourceType(appointmentCategory, resTypes, centerId);
    if (schedulerResources != null && schedulerResources.size() > 0) {
      for (BasicDynaBean resource : schedulerResources) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.putAll(resource.getMap());
        map.put("overbooked",
            !(appointmentValidator.validateIfSlotOverbooked(appointmentCategory,
                (String) resource.get("resource_id"), (String) resource.get("resource_type"),
                apptTime, apptEndTime, validationErrors, null, apptIdStr)));
        map.put("availability",
            appointmentValidator.validateResourcesAvailability(appointmentCategory, apptTime,
                apptEndTime, apptDate, (String) resource.get("resource_type"),
                (String) resource.get("resource_id"), 
                centerId, validationErrors, null, null,visitMode));
        list.add(map);
      }
    }
    Map<String, Object> resultMap = new HashMap<String, Object>();
    resultMap.put("resources", list);
    logger.debug(
        "ending getAdditionalResourcesByResourceType method" + DateUtil.getCurrentTimestamp());
    return resultMap;
  }

  /**
   * Gets the filter data.
   *
   * @return the filter data
   */
  public Map<String, Object> getFilterData() {
    return null;
  }

  /**
   * Lookup.
   *
   * @param params the params
   * @return the map
   */
  public Map<String, Object> lookup(MultiValueMap<String, String> params) {
    return null;
  }

  /**
   * Gets the complaint types.
   *
   * @param parameters the parameters
   * @return the complaint types
   */
  public Map getComplaintTypes(Map<String, String[]> parameters) {

    Map<String, Object> responseMap = new HashMap<String, Object>();
    String filterText = (null != parameters && parameters.containsKey("filterText"))
        ? parameters.get("filterText")[0]
        : null;
    List<BasicDynaBean> searchSet = (null != filterText)
        ? complaintTypesService.autocomplete(filterText, parameters)
        : complaintTypesService.lookup(true);
    responseMap.put("dtoList", ConversionUtils.listBeanToListMap(searchSet));
    responseMap.put("listSize", searchSet.size());
    return responseMap;
  }

  /**
   * Gets the available resources.
   *
   * @param params the params
   * @return the available resources
   */
  @java.lang.SuppressWarnings({ "rawtypes", "unchecked", "deprecation", "unused" })
  public Map getAvailableResources(Map<String, String[]> params) {

    ValidationErrorMap validationErrors = new ValidationErrorMap();
    Map<String, Object> nestedException = new HashMap<String, Object>();
    Integer centerId = null;
    if (params.get("center_id") != null && params.get("center_id")[0] != null) {
      centerId = Integer.parseInt(params.get("center_id")[0]);
    } else {
      centerId = RequestContext.getCenterId();
    }
    String appointmentDurationStr = (params.get("appointment_duration") != null
        ? params.get("appointment_duration")[0]
        : null);
    Integer appointmentDuration = null;
    if (appointmentDurationStr != null || appointmentDurationStr != "") {
      appointmentDuration = Integer.parseInt(appointmentDurationStr);
    } else {
      nestedException.put("duration", "exception.scheduler.invalid.duration");
      throw new NestableValidationException(nestedException);
    }
    if (appointmentDuration < 1 || appointmentDuration > 999) {
      nestedException.put("date", "exception.scheduler.invalid.duration");
      throw new NestableValidationException(nestedException);
    }
    String date = (params.get("date") != null ? params.get("date")[0] : null);
    String time = (params.get("time") != null ? params.get("time")[0] : null);
    List<Map> finalResponseList = new ArrayList<Map>();

    String endTimeString = "23:59";
    DateUtil dateUtil = new DateUtil();
    Time endTime = null;
    try {
      endTime = dateUtil.parseTheTime(endTimeString);
    } catch (ParseException exception) {
      logger.error("", exception);
    }
    Long endTimeL = endTime.getTime();
    String deptId = (params.get("dept_id") != null ? params.get("dept_id")[0] : null);
    Map filterMap = new HashMap();
    filterMap.put("find_string", new String[] { "" });
    filterMap.put("center_id", new String[] { centerId.toString() });
    filterMap.put("res_sch_category", new String[] { "DOC" });
    filterMap.put("departments", new String[] { deptId });
    filterMap.put("page_size", new String[] { "0" });
    filterMap.put("page_num", new String[] { "0" });
    Integer centersIncDefault = (Integer) genericPreferencesService.getAllPreferences()
        .get("max_centers_inc_default");
    filterMap.put("max_centers_inc_default", new String[] { centersIncDefault.toString() });
    filterMap.put("with_count", new String[] { "true" });
    AppointmentCategory appointmentCategory = appointmentCategoryFactory.getInstance("DOC");
    Map doctorMap = schedulerResourceRepository.getSchedulerSecondaryResources(appointmentCategory,
        filterMap, false);

    Integer numberOfReturnedResource = (params.get("page_size") != null
        && params.get("page_size")[0] != null) ? Integer.parseInt(params.get("page_size")[0]) : 15;
    List<Map> doctorList = (List) doctorMap.get("resources");

    int numberOfRecordsMatch = 0;
    for (Map doctor : doctorList) {

      if (deptId != null && !deptId.equalsIgnoreCase((String) doctor.get("dept_id"))) {
        continue;
      }
      if (numberOfRecordsMatch >= numberOfReturnedResource) {
        break;
      }
      Integer defaultDuration = (Integer) doctor.get("default_duration");
      java.sql.Time apptTime = getNextSlotTime(time, defaultDuration);
      if (apptTime == null) {
        List<Map> errors = new ArrayList<Map>();
        ValidationErrorMap appErrorMap = new ValidationErrorMap();
        appErrorMap.addError("time", "exception.scheduler.invaliddatetime");
        ValidationException ex = new ValidationException(appErrorMap);
        errors.add(ex.getErrors());
        nestedException.put("availableslots", errors);
        throw new NestableValidationException(nestedException);
      }
      String slotTimeStr = apptTime.toString().substring(0, 5);
      String timeStampStr = date + " " + slotTimeStr;
      Timestamp apptTimeStamp = null;
      try {
        apptTimeStamp = dateUtil.parseTheTimestamp(timeStampStr);
      } catch (ParseException exception) {
        nestedException.put("date", "exception.scheduler.appointment.invalid.date");
        throw new NestableValidationException(nestedException);
      }
      long appTimeL = apptTime.getTime();
      if (appTimeL > endTimeL) {
        continue;
      }
      long appointmentTimeLong = apptTimeStamp.getTime();
      appointmentTimeLong = appointmentTimeLong + (appointmentDuration * 60 * 1000);
      Timestamp endTimeStamp = new java.sql.Timestamp(appointmentTimeLong);
      String resId = (String) doctor.get("resource_id");
      Boolean slotOverBooked = appointmentValidator.validateIfSlotOverbooked(appointmentCategory,
          resId, "OPDOC", apptTimeStamp, endTimeStamp, validationErrors, "slot_time", null);
      Boolean resourceAvailability = appointmentValidator.validateResourcesAvailability(
          appointmentCategory, apptTimeStamp, endTimeStamp, date,
          appointmentCategory.getSecondaryResourceType(), resId, centerId, validationErrors,
          "secondary_resource_id", null);
      if (slotOverBooked && resourceAvailability) {
        Map docMap = new HashMap();
        docMap.put("slot", new java.sql.Time(appTimeL));
        docMap.put("resource_id", (String) doctor.get("resource_id"));
        docMap.put("resource_name", (String) doctor.get("resource_name"));
        docMap.put("dept_id", (String) doctor.get("dept_id"));
        docMap.put("dept_name", (String) doctor.get("dept_name"));
        finalResponseList.add(docMap);
        numberOfRecordsMatch++;
        continue;
      }
      long apptTimel = apptTimeStamp.getTime();
      apptTimel = apptTimel + (defaultDuration * 60 * 1000);
      appTimeL = appTimeL + (defaultDuration * 60 * 1000);
      if (appTimeL > endTimeL) {
        continue;
      }
      appointmentTimeLong = appointmentTimeLong + (defaultDuration * 60 * 1000);
      apptTimeStamp = new java.sql.Timestamp(apptTimel);
      endTimeStamp = new java.sql.Timestamp(appointmentTimeLong);
      slotOverBooked = appointmentValidator.validateIfSlotOverbooked(appointmentCategory, resId,
          "OPDOC", apptTimeStamp, endTimeStamp, validationErrors, "slot_time", null);
      resourceAvailability = appointmentValidator.validateResourcesAvailability(appointmentCategory,
          apptTimeStamp, endTimeStamp, date, appointmentCategory.getSecondaryResourceType(), resId,
          centerId, validationErrors, "secondary_resource_id", null);
      if (slotOverBooked && resourceAvailability) {
        Map docMap = new HashMap();
        docMap.put("slot", new java.sql.Time(appTimeL));
        docMap.put("resource_id", (String) doctor.get("resource_id"));
        docMap.put("resource_name", (String) doctor.get("resource_name"));
        docMap.put("dept_id", (String) doctor.get("dept_id"));
        docMap.put("dept_name", (String) doctor.get("dept_name"));
        finalResponseList.add(docMap);
        continue;
      }
      apptTimel = apptTimel + (defaultDuration * 60 * 1000);
      appTimeL = appTimeL + (defaultDuration * 60 * 1000);
      if (appTimeL > endTimeL) {
        continue;
      }
      appointmentTimeLong = appointmentTimeLong + (defaultDuration * 60 * 1000);
      apptTimeStamp = new java.sql.Timestamp(apptTimel);
      endTimeStamp = new java.sql.Timestamp(appointmentTimeLong);
      slotOverBooked = appointmentValidator.validateIfSlotOverbooked(appointmentCategory, resId,
          "OPDOC", apptTimeStamp, endTimeStamp, validationErrors, "slot_time", null);
      resourceAvailability = appointmentValidator.validateResourcesAvailability(appointmentCategory,
          apptTimeStamp, endTimeStamp, date, appointmentCategory.getSecondaryResourceType(), resId,
          centerId, validationErrors, "secondary_resource_id", null);
      if (slotOverBooked && resourceAvailability) {
        Map docMap = new HashMap();
        docMap.put("slot", new java.sql.Time(appTimeL));
        docMap.put("resource_id", (String) doctor.get("resource_id"));
        docMap.put("resource_name", (String) doctor.get("resource_name"));
        docMap.put("dept_id", (String) doctor.get("dept_id"));
        docMap.put("dept_name", (String) doctor.get("dept_name"));
        finalResponseList.add(docMap);
        continue;
      }
    }
    Collections.sort(finalResponseList, new Comparator<Map>() {

      @Override
      public int compare(Map o1, Map o2) {
        Time t1 = (Time) o1.get("slot");
        Time t2 = (Time) o2.get("slot");
        if (t1.before(t2)) {
          return -1;
        } else if (t2.before(t1)) {
          return 1;
        } else {
          return 0;
        }
      }
    });
    Map responseMap = new HashMap();
    if (finalResponseList.size() > numberOfReturnedResource) {
      responseMap.put("doctors", finalResponseList.subList(0, numberOfReturnedResource));
    } else {
      responseMap.put("doctors", finalResponseList);
    }
    return responseMap;
  }

  /**
   * Gets the next slot time.
   *
   * @param time the time
   * @param defaultDuration the default duration
   * @return the next slot time
   */
  public Time getNextSlotTime(String time, Integer defaultDuration) {

    java.sql.Time initialTime = null;
    try {
      DateUtil dateUtil = new DateUtil();
      initialTime = dateUtil.parseTheTime(time);
    } catch (ParseException exception) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("resources_id_list", "exception.scheduler.invalid.time");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("common-slots", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    String startTimeString = "00:00";
    String endTimeString = "23:59";
    java.sql.Time startTime = null;
    java.sql.Time endTime = null;
    try {
      DateUtil dateUtil = new DateUtil();
      startTime = dateUtil.parseTheTime(startTimeString);
      endTime = dateUtil.parseTheTime(endTimeString);
    } catch (ParseException exception) {
      logger.error("", exception);
    }
    Long startTimeL = startTime.getTime();
    Long endTimeL = endTime.getTime();
    Long initialTimeL = initialTime.getTime();
    Long newTimeL = null;
    for (long j = startTimeL; j < endTimeL; j = j + defaultDuration * 60 * 1000) {
      if (initialTimeL >= j && initialTimeL <= j + defaultDuration * 60 * 1000) {
        if (initialTimeL == j) {
          newTimeL = j;
        } else {
          newTimeL = j + defaultDuration * 60 * 1000;
        }
        if (newTimeL > endTimeL) {
          return null;
        } else {
          return new java.sql.Time(newTimeL);
        }
      }
    }
    return null;
  }

  /**
   * Gets the previous appt.
   *
   * @param params the params
   * @return the previous appt
   */
  public Map<String, Object> getPreviousAppt(Map<String, String[]> params) {
    return appointmentService.getPreviousAppt(params);
  }
}
