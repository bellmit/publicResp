package com.insta.hms.mdm.resourceoverride;

import static com.insta.hms.jobs.common.QuartzJobHelper.buildJob;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.batchjob.builders.AppointmentRescheduleJob;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.PushService;
import com.insta.hms.common.SearchQueryAssembler;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.security.SecurityService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.scheduler.AppointmentCategory;
import com.insta.hms.core.scheduler.AppointmentCategoryFactory;
import com.insta.hms.core.scheduler.AppointmentService;
import com.insta.hms.core.scheduler.ResourceRepository;
import com.insta.hms.exception.HMSException;
import com.insta.hms.exception.NestableValidationException;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.jobs.JobService;
import com.insta.hms.mdm.MasterService;
import com.insta.hms.mdm.centers.CenterRepository;
import com.insta.hms.mdm.resourceavailability.ResourceAvailabilityRepository;
import com.insta.hms.mdm.resourceavailability.ResourceAvailabilityService;
import com.insta.hms.resourcescheduler.PractoBookHelper;

import flexjson.JSONSerializer;
import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class ResourceOverrideService.
 */

@Service
public class ResourceOverrideService extends MasterService {
  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(ResourceOverrideService.class);

  /** The category repository. */
  @LazyAutowired
  private ResourceAvailabilityRepository categoryRepository;

  /** The resource override repository. */
  @LazyAutowired
  private ResourceOverrideRepository resourceOverrideRepository;

  /** The resource repo. */
  @LazyAutowired
  private ResourceRepository resourceRepo;

  /** The resource details repository. */
  @LazyAutowired
  private ResourceOverrideDetailsRepository resourceDetailsRepository;

  /** The center repository. */
  @LazyAutowired
  private CenterRepository centerRepository;

  /** The category service. */
  @LazyAutowired
  private ResourceAvailabilityService categoryService;

  /** The pref service. */
  @LazyAutowired
  private GenericPreferencesService prefService;

  /** The appointment service. */
  @LazyAutowired
  private AppointmentService appointmentService;

  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;

  /** The job service. */
  @LazyAutowired
  private JobService jobService;

  /** The appointment category factory. */
  @LazyAutowired
  private AppointmentCategoryFactory appointmentCategoryFactory;

  /** The push service. */
  @LazyAutowired
  private PushService pushService;

  /** The security service. */
  @LazyAutowired
  private SecurityService securityService;

  /** The Constant WEBSOCKET_PUSH_CHANNEL. */
  private static final String WEBSOCKET_PUSH_CHANNEL = "/topic/overrides";

  /** Default value for visit mode. */
  private static final String DEFAULT_VISIT_MODE = "I";
  
  /**
   * Instantiates a new resource override service.
   *
   * @param respository
   *          the respository
   * @param validator
   *          the validator
   */
  public ResourceOverrideService(ResourceOverrideRepository respository,
      ResourceOverrideValidator validator) {
    super(respository, validator);
  }

  /**
   * Gets the search query assembler.
   *
   * @param params the params
   * @param listingParams the listing params
   * @return the search query assembler
   */
  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.mdm.MasterService#getSearchQueryAssembler(java.util.Map, java.util.Map)
   */
  @SuppressWarnings("rawtypes")
  @Override
  protected SearchQueryAssembler getSearchQueryAssembler(Map params,
      Map<LISTING, Object> listingParams) {
    Map<String, Object> parameterMap = new HashMap<String, Object>(params);
    if (parameterMap.get("login_center_id") != null
        && parameterMap.get("login_center_name") != null) {
      parameterMap.remove("login_center_id");
      parameterMap.remove("login_center_name");
    }
    SearchQueryAssembler qa = super.getSearchQueryAssembler(parameterMap, listingParams);
    qa.addSecondarySort("availability_date", true);
    return qa;
  }

  /**
   * Gets the list page data.
   *
   * @param params
   *          the params
   * @return the list page data
   */
  public Map<String, List<BasicDynaBean>> getListPageData(Map params) {
    String resourceName = null;
    String resourceType = null;
    if (params != null) {
      if (params.get("res_sch_type") != null) {
        resourceType = ((String[]) params.get("res_sch_type"))[0];
      }
      if (params.get("res_sch_name") != null) {
        resourceName = ((String[]) params.get("res_sch_name"))[0];
      }
    }
    Map<String, List<BasicDynaBean>> beanMap = new HashMap<String, List<BasicDynaBean>>();
    beanMap.put("allResourcesList", categoryRepository.getAllResources());
    beanMap.put("centersJSON", centerRepository.getAllCentersDetails());
    int loggedCenterId = RequestContext.getCenterId();
    beanMap.put("DoctorsJSON", categoryRepository.getResourceMasterList("DOC", loggedCenterId));
    List<BasicDynaBean> resourceBean = categoryRepository.getCategoryDescription(true);
    if (resourceBean != null) {
      beanMap.put("categoryDescripton", resourceBean);
    }
    return beanMap;
  }

  /**
   * Gets the adds the page data.
   *
   * @param params
   *          the params
   * @return the adds the page data
   */
  @SuppressWarnings("unchecked")
  public Map<String, List<BasicDynaBean>> getAddPageData(Map params) {
    Map<String, List<BasicDynaBean>> refData = new HashMap<String, List<BasicDynaBean>>();
    int loggedCenterId = RequestContext.getCenterId();
    if (params.containsKey("center_id") && params.get("center_id") != null
        && ((String[]) params.get("center_id"))[0] != null
        && !((String[]) params.get("center_id"))[0].equals("")) {
      loggedCenterId = Integer.parseInt(((String[]) params.get("center_id"))[0]);
    }
    refData.put("allResourcesList", categoryRepository.getAllResources(loggedCenterId));
    refData.put("centersJSON", centerRepository.getAllCentersDetails());
    refData.put("DoctorsJSON", categoryRepository.getResourceMasterList("DOC", loggedCenterId));
    List<BasicDynaBean> resourceBean = categoryRepository.getCategoryDescription(true);
    if (resourceBean != null) {
      refData.put("categoryDescripton", resourceBean);
    }
    return refData;
  }

  /**
   * Adds the override details.
   *
   * @param overrideDetails
   *          the override details
   * @param resAvailId
   *          the res avail id
   * @return the map
   */
  // public Map addOverrideDetails(Map params) {
  // Map resourcesMap = new HashMap();
  // Map appointmentDetails = new HashMap();
  // List errors = new ArrayList();
  // Date fromDate = null;
  // Date toDate = null;
  // String resourceType = ((String[]) params.get("res_sch_type"))[0];
  // try {
  // fromDate = new Date(DateUtil.parseDate(((String[]) params.get("from_date"))[0]).getTime());
  // toDate = new Date(DateUtil.parseDate(((String[]) params.get("to_date"))[0]).getTime());
  // } catch(ParseException e) {
  // logger.debug("Failed to parse the date details" + e.getMessage());
  // }
  // Calendar cal1 = Calendar.getInstance();
  // Calendar cal2 = Calendar.getInstance();
  // cal1.setTime(fromDate);
  // cal2.setTime(toDate);
  // int resAvailId = 0;
  // boolean success = true;
  // BasicDynaBean bean = resourceOverrideRepository.getBean();
  // Date availabilityDate = null;
  // boolean recordExistsWithinGivenDateRange = true;
  // String warningMessage = null;
  // resourcesMap.put("availabilityDate", availabilityDate);
  // resourcesMap.put("fromDate", fromDate);
  // resourcesMap.put("toDate", toDate);
  // bean = resourceOverrideRepository.getBean();
  // ConversionUtils.copyToDynaBean(params, bean, errors);
  // if (errors.isEmpty()) {
  // List<BasicDynaBean> existingAvailabiltyRecords = new ArrayList<BasicDynaBean>();
  // existingAvailabiltyRecords = resourceOverrideRepository
  // .getResources((String) bean.get("res_sch_type"),
  // (String) bean.get("res_sch_name"));
  // if (existingAvailabiltyRecords != null) {
  // for (BasicDynaBean existingAvailabiltyRecord : existingAvailabiltyRecords) {
  // availabilityDate = (java.util.Date) existingAvailabiltyRecord.get("availability_date");
  // if (availabilityDate.equals(fromDate) || availabilityDate.equals(toDate)) {
  // recordExistsWithinGivenDateRange = false;
  // break;
  // }
  // }
  // }
  // if (recordExistsWithinGivenDateRange) {
  // if (cal1.compareTo(cal2) == 0) {
  // bean = resourceOverrideRepository.getBean();
  // ConversionUtils.copyToDynaBean(params, bean, errors);
  // resAvailId = (Integer) resourceOverrideRepository.getNextId();
  // bean.set("res_avail_id", resAvailId);
  // bean.set("availability_date", new java.sql.Date(cal1.getTime().getTime()));
  // success = resourceOverrideRepository.insert(bean) > 0;
  // if (saveResourceOverride(params, resAvailId) == null)
  // success = true;
  // else
  // success = false;
  //
  // } else {
  // if (success) {
  // while (cal1.before(cal2)) {
  // bean = resourceOverrideRepository.getBean();
  // ConversionUtils.copyToDynaBean(params, bean, errors);
  // resAvailId = (Integer) resourceOverrideRepository.getNextId();
  // bean.set("res_avail_id", resAvailId);
  // bean.set("availability_date", new java.sql.Date(cal1.getTime().getTime()));
  // success = resourceOverrideRepository.insert(bean) > 0;
  // if (saveResourceOverride(params, resAvailId) == null)
  // success = true;
  // else {
  // success = false;
  // break;
  // }
  // cal1.add(Calendar.DATE, 1);
  // }
  // }
  // if (success) {
  // bean = resourceOverrideRepository.getBean();
  // ConversionUtils.copyToDynaBean(params, bean, errors);
  // resAvailId = (Integer) resourceOverrideRepository.getNextId();
  // bean.set("res_avail_id", resAvailId);
  // bean.set("availability_date", new java.sql.Date(cal2.getTime().getTime()));
  // success = resourceOverrideRepository.insert(bean) > 0;
  // if (saveResourceOverride(params, resAvailId) == null)
  // success = true;
  // else
  // success = false;
  // }
  // }
  // if (success) {
  // warningMessage = getResourceAppointments(((String[]) params.get("res_sch_name"))[0],
  // resourceType,null, new java.sql.Date(fromDate.getTime()),
  // new java.sql.Date(toDate.getTime()));
  // success = true;
  // }
  // if (warningMessage != null)
  // resourcesMap.put("warningMesssage", warningMessage);
  // } else {
  // success = false;
  // }
  // resourcesMap.put("redirectStatus", success);
  // resourcesMap.put("resourceBean", bean);
  // if (success && PractoBookHelper.isPractoAdvantageEnabled()) {
  // try {
  // PractoBookHelper.addUpdateOverridesToPracto((String) bean.get("res_sch_name"),
  // new java.sql.Date(fromDate.getTime()), new java.sql.Date(toDate.getTime()), null);
  // } catch (Exception e) {
  // logger.debug("Failed to parse the date details" + e.getMessage());
  // }
  // }
  // }
  // return resourcesMap;
  //
  // }

  /*
   * This insertion method is common for all types of requests 1.Getting request from Resource
   * Override screen 2.Getting request from API
   */
  /**
   * save resource override.
   *
   * @param overrideDetails
   *          the override details
   * @param resAvailId
   *          the res avail id
   * @return the string
   */
  public String saveResourceOverride(List<Map> overrideDetails, int resAvailId) {
    BasicDynaBean schDefaultAvailBean = null;
    boolean success = true;

    /* Iterating list of availability_details in the form of map */
    for (Map overrideResource : overrideDetails) {
      String rdelete = (String) overrideResource.get("r_delete");
      Time fromTime = (Time) overrideResource.get("start_time");
      Time toTime = (Time) overrideResource.get("end_time");
      String defaultVal = (String) overrideResource.get("default_value");
      String remarks = (String) overrideResource.get("remarks");
      String availabilityStatus = (String) overrideResource.get("availability_status");
      String visitMode = (String) overrideResource.get("visit_mode");
      
      if (defaultVal != null) {
        if (rdelete != null && !rdelete.equals("") && rdelete.equals("false")) {
          if (fromTime != null && !fromTime.equals("") && toTime != null && !toTime.equals("")) {
            schDefaultAvailBean = resourceDetailsRepository.getBean();
            int resAvailDdetailsId = (Integer) resourceDetailsRepository.getNextId();
            schDefaultAvailBean.set("res_avail_details_id", resAvailDdetailsId);
            schDefaultAvailBean.set("res_avail_id", resAvailId);
            schDefaultAvailBean.set("remarks", remarks);
            schDefaultAvailBean.set("availability_status", availabilityStatus);
            if (overrideResource.get("center_id") != null
                && !overrideResource.get("center_id").equals("")) {
              schDefaultAvailBean.set("center_id", overrideResource.get("center_id"));
            } else {
              schDefaultAvailBean.set("center_id", null);
            }
            if (visitMode != null && !visitMode.isEmpty()) {
              schDefaultAvailBean.set("visit_mode", visitMode);
            } else {
              schDefaultAvailBean.set("visit_mode",DEFAULT_VISIT_MODE);
            }
            schDefaultAvailBean.set("from_time", fromTime);
            schDefaultAvailBean.set("to_time", toTime);

            success = resourceDetailsRepository.insert(schDefaultAvailBean) > 0;
            if (!success) {
              success = false;
              break;
            }
          }
        }
      }
    }

    if (success) {
      return null;
    } else {
      return "error";
    }
  }

  /**
   * Update resource override.
   *
   * @param params
   *          the params
   * @param resAvailId
   *          the res avail id
   * @param resAvailDetId
   *          the res avail det id
   * @return the string
   */
  public String updateResourceOverride(Map params, int resAvailId, String[] resAvailDetId) {
    List errors = new ArrayList();
    BasicDynaBean bean = null;
    List availDetIds = new ArrayList();

    BasicDynaBean schDefaultAvailBean = null;
    SimpleDateFormat timeFormatterSecs = new SimpleDateFormat("HH:mm");
    boolean success = true;

    String[] rdelete = (String[]) params.get("r_delete");
    String[] fromTime = (String[]) params.get("from_time");
    String[] toTime = (String[]) params.get("to_time");
    String[] defaultVal = (String[]) params.get("default_value");
    String[] remarks = (String[]) params.get("remarks");
    String[] availabilityStatus = (String[]) params.get("availability_status");
    String[] centerIds = (String[]) params.get("center_id");
    String[] resType = (String[]) params.get("res_sch_type");
    String[] loginCenterId = (String[]) params.get("login_center_id");
    String[] visitMode = (String[]) params.get("visit_mode");
    Integer centersIncDefault = (Integer) prefService.getAllPreferences()
        .get("max_centers_inc_default");

    if (resAvailDetId != null) {
      for (int j = 0; j < resAvailDetId.length; j++) {
        if (!resAvailDetId[j].equals("")) {
          if (rdelete[j].equals("false") && !fromTime[j].equals("") && !toTime[j].equals("")) {
            bean = resourceDetailsRepository.getBean();
            ConversionUtils.copyIndexToDynaBean(params, j, bean, errors);
            Map<String, Integer> keys = new HashMap<String, Integer>();
            keys.put("res_avail_details_id", Integer.parseInt(resAvailDetId[j]));
            bean.set("res_avail_id", resAvailId);
            success = resourceDetailsRepository.update(bean, keys) > 0;
            availDetIds.add(bean.get("res_avail_details_id"));

            if (!success) {
              break;
            }
          } else {
            success = resourceDetailsRepository.delete("res_avail_details_id",
                Integer.parseInt(resAvailDetId[j])) > 0;
            if (!success) {
              break;
            }
          }
        } else {
          if (!fromTime[j].equals("") && !toTime[j].equals("")) {
            bean = resourceDetailsRepository.getBean();
            ConversionUtils.copyIndexToDynaBean(params, j, bean, errors);
            bean.set("res_avail_details_id", resourceDetailsRepository.getNextSequence());
            bean.set("res_avail_id", resAvailId);
            try {
              bean.set("from_time", new java.sql.Time(timeFormatterSecs.parse(fromTime[j])
                  .getTime()));
              bean.set("to_time", new java.sql.Time(timeFormatterSecs.parse(toTime[j]).getTime()));
            } catch (ParseException exe) {
              // TODO Auto-generated catch block
              logger.debug("Failed to parse the time details" + exe.getMessage());
            }
            bean.set("availability_status", availabilityStatus[j]);
            bean.set("remarks", remarks[j]);
            
            if (resType != null && resType[0].equalsIgnoreCase("DOC")) {
              if (centersIncDefault > 1) {
                if (centerIds[j] != null && !centerIds[j].equals("")) {
                  bean.set("center_id", Integer.parseInt(centerIds[j]));
                } else {
                  bean.set("center_id", null);
                }
              } else {
                bean.set("center_id",
                    availabilityStatus[j].equalsIgnoreCase("N") ? null : 0);
              }
              if (visitMode != null && visitMode[j] != null && !visitMode[j].isEmpty()) {
                bean.set("visit_mode", visitMode[j]);
              } else {
                bean.set("visit_mode",DEFAULT_VISIT_MODE);
              }
            } else {
              bean.set("center_id",availabilityStatus[j]
                  .equalsIgnoreCase("N") ? null 
                      : (loginCenterId != null 
                      ? Integer.parseInt(loginCenterId[0]) : 0));
              bean.set("visit_mode",DEFAULT_VISIT_MODE);
            }
            
            success = resourceDetailsRepository.insert(bean) > 0;
            availDetIds.add(bean.get("res_avail_details_id"));
            if (!success) {
              break;
            }
          }
        }
      }
      if (success) {
        success = resourceOverrideRepository.deleteResourceTimings(resAvailId, availDetIds);
      }
    }
    if (success) {
      return null;
    } else {
      return "error";
    }
  }

  /**
   * Gets the next resource id.
   * 
   * @return the next resource id
   */
  public Integer getNextResourceId() {
    return (Integer) resourceOverrideRepository.getNextId();
  }

  /**
   * Update resource override details.
   *
   * @param params
   *          the params
   * @return the map
   */
  public Map updateResourceOverrideDetails(Map params) {
    Map resourceDetailsMap = new HashMap();
    Map appointmentDetails = new HashMap();
    boolean success = false;
    String warningMessage = null;
    String resAvailId = ((String[]) params.get("res_avail_id"))[0];
    String availabilityDate = ((String[]) params.get("availability_date"))[0];
    String[] resAvailDetId = ((String[]) params.get("res_avail_details_id"));

    try {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      java.sql.Date availDate = new java.sql.Date(sdf.parse(availabilityDate).getTime());
      updateResourceOverride(params, Integer.parseInt(resAvailId), resAvailDetId);
      warningMessage = getResourceAppointments(((String[]) params.get("res_sch_name"))[0],
          ((String[]) params.get("res_sch_type"))[0], availDate, null, null);
    } catch (ParseException exception) {
      logger.debug("Failed to parse the date details" + exception.getMessage());
    }
    success = true;

    resourceDetailsMap.put("successStatus", success);
    if (warningMessage != null) {
      resourceDetailsMap.put("warningMessage", warningMessage);
    }
    String resourceType = ((String[]) params.get("res_sch_type"))[0];
    if (PractoBookHelper.isPractoAdvantageEnabled() && resourceType.equals("DOC")) {
      try {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        java.sql.Date date = new java.sql.Date(sdf.parse(availabilityDate).getTime());
        PractoBookHelper.addUpdateOverridesToPracto(((String[]) params.get("res_sch_name"))[0],
            date, date, RequestContext.getCenterId() == 0 ? null : RequestContext.getCenterId());
      } catch (Exception exception) {
        logger.debug("Failed to parse the date details" + exception.getMessage());
      }
    }
    return resourceDetailsMap;
  }

  /**
   * Delete selected rows.
   *
   * @param resourceMap
   *          the resource map
   * @return the string
   * @throws Exception
   *           the exception
   */
  public String deleteSelectedRows(Map<String, String> resourceMap) throws Exception {

    boolean success = true;
    String responseContent = "Deleted";
    String resAvailId = resourceMap.get("resAvailId");
    String resourceType = resourceMap.get("res_sch_type");
    String[] resAvailIds = null;
    if (resAvailId.contains(",")) {
      String[] arrayElements = resAvailId.split(",");
      resAvailIds = new String[arrayElements.length];
      resAvailIds = arrayElements;
    } else {
      resAvailIds = new String[1];
      resAvailIds[0] = resAvailId;
    }
    String doctorId = null;
    List<java.sql.Date> dates = new ArrayList<java.sql.Date>();
    for (int i = 0; i < resAvailIds.length; i++) {
      if (PractoBookHelper.isPractoAdvantageEnabled() && resourceType.equals("DOC")) {
        BasicDynaBean bean = getResourceDetails(Integer.parseInt(resAvailIds[i]), "DOC");
        dates.add((java.sql.Date) bean.get("availability_date"));
        doctorId = (String) bean.get("res_sch_name");
      }
      success = resourceOverrideRepository
          .delete("res_avail_id", Integer.parseInt(resAvailIds[i])) > 0;
      if (success) {
        success = resourceDetailsRepository
            .delete("res_avail_id", Integer.parseInt(resAvailIds[i])) > 0;
      }
    }
    if (success && PractoBookHelper.isPractoAdvantageEnabled() && dates != null && dates.size() > 0
        && resourceType.equals("DOC")) {
      for (java.sql.Date date : dates) {
        PractoBookHelper.deleteOverridesOnPracto(doctorId, date);
      }
    }
    if (!success) {
      responseContent = "";
    }
    return responseContent;
  }

  /**
   * Delete resource override.
   *
   * @param resourceMap
   *          the resource map
   * @return true, if successful
   * @throws Exception
   *           the exception
   */
  public boolean deleteResourceOverride(Map resourceMap) throws Exception {
    boolean success = true;
    String doctorId = null;
    java.sql.Date date = null;
    String resAvailId = (String) resourceMap.get("resAvailId");
    String resourceType = (String) resourceMap.get("resourceType");

    if (PractoBookHelper.isPractoAdvantageEnabled() && resourceType.equals("DOC")) {
      BasicDynaBean bean = (BasicDynaBean) resourceOverrideRepository.getResourceDetails(
          Integer.parseInt(resAvailId), "DOC");
      date = (java.sql.Date) bean.get("availability_date");
      doctorId = (String) bean.get("res_sch_name");
    }
    success = resourceOverrideRepository.delete("res_avail_id", resAvailId) > 0;
    if (success) {
      success = resourceDetailsRepository.delete("res_avail_id", resAvailId) > 0;
    }
    if (success && PractoBookHelper.isPractoAdvantageEnabled() && resourceType.equals("DOC")) {
      PractoBookHelper.deleteOverridesOnPracto(doctorId, date);
    }
    return success;
  }

  /**
   * Show default timings.
   *
   * @param requestMap
   *          the request map
   * @return true, if successful
   */
  public boolean showDefaultTimings(Map requestMap) {
    String resourceName = (String) requestMap.get("resourceName");
    String resourceType = (String) requestMap.get("resourceType");
    BasicDynaBean bean = null;

    if (resourceName != null && resourceType != null) {
      bean = categoryService.getResourceDetails(resourceType, resourceName);
    }

    if (bean == null) {
      bean = resourceOverrideRepository.getDefaultCategory(resourceType);
    }
    requestMap.put("bean", bean);
    return false;
  }

  /**
   * Gets the default category.
   *
   * @param resourceType
   *          the resource type
   * @return the default category
   */
  public BasicDynaBean getDefaultCategory(String resourceType) {
    return ((ResourceOverrideRepository) getRepository()).getDefaultCategory(resourceType);
  }

  /**
   * Gets the resource details.
   *
   * @param resAvailId
   *          the res avail id
   * @param resourceType
   *          the resource type
   * @return the resource details
   */
  public BasicDynaBean getResourceDetails(int resAvailId, String resourceType) {
    return ((ResourceOverrideRepository) getRepository()).getResourceDetails(resAvailId,
        resourceType);
  }

  /**
   * Gets the resource details by date.
   *
   * @param resSchType
   *          the res sch type
   * @param resSchName
   *          the res sch name
   * @return the resource details by date
   */
  public BasicDynaBean getResourceDetailsByDate(String resSchType, String resSchName) {
    return ((ResourceOverrideRepository) getRepository()).getResourceDetailsByDate(resSchType,
        resSchName);
  }

  /**
   * Gets the resources list.
   *
   * @param resourceType
   *          the resource type
   * @param resourceName
   *          the resource name
   * @return the resources list
   */
  public List<BasicDynaBean> getResourcesList(String resourceType, String resourceName) {
    return ((ResourceOverrideRepository) getRepository()).getResourcesList(resourceType,
        resourceName);
  }

  /**
   * Filter all resources availability.
   *
   * @param userCenter
   *          the user center
   * @param resourceAvailabilityList
   *          the resource availability list
   * @param centerId
   *          the center id
   * @return the list
   */
  // filter doctor resource availability time based on centerwise
  public static List<BasicDynaBean> filterAllResourcesAvailability(Integer userCenter,
      List<BasicDynaBean> resourceAvailabilityList, Integer centerId) {
    List<BasicDynaBean> filteredResourceList = new ArrayList<BasicDynaBean>();
    if (resourceAvailabilityList != null && resourceAvailabilityList.size() > 0) {
      for (BasicDynaBean resBean : resourceAvailabilityList) {
        // checking not available slots and adding to list
        if (centerId != null) {
          if ((userCenter.intValue() == 0 && centerId.intValue() == 0)) {
            filteredResourceList.add(resBean);
          } else if ((userCenter.intValue() == 0 && centerId.intValue() != 0)) {
            if (resBean.get("availability_status").equals("N")) {
              resBean.set("availability_status", "N");
              filteredResourceList.add(resBean);
            } else {
              if (resBean.get("center_id").equals(0)) {
                filteredResourceList.add(resBean);
              } else if (centerId.intValue() != (Integer) resBean.get("center_id")) {
                resBean.set("availability_status", "N");
                filteredResourceList.add(resBean);
              } else {
                filteredResourceList.add(resBean);
              }
            }
          } else {
            if (resBean.get("availability_status").equals("N")) {
              resBean.set("availability_status", "N");
              filteredResourceList.add(resBean);
            } else {
              if (resBean.get("center_id").equals(0)) {
                filteredResourceList.add(resBean);
              } else if (userCenter.intValue() != (Integer) resBean.get("center_id")) {
                resBean.set("availability_status", "N");
                filteredResourceList.add(resBean);
              } else {
                filteredResourceList.add(resBean);
              }
            }
          }
        } // else return null;
      }
    }
    return filteredResourceList;
  }

  /**
   * Gets the scheduled timeslots.
   *
   * @param resourceMap
   *          the resource map
   * @return the scheduled timeslots
   * @throws ParseException
   *           the parse exception
   */
  public List getScheduledTimeslots(Map resourceMap) throws ParseException {
    List timingsList = new ArrayList();
    int loggedIncenter = (Integer) resourceMap.get("loggedIncenter");
    String resourceName = (String) resourceMap.get("availableDoctor");
    String category = (String) resourceMap.get("category");
    String dateStr = (String) resourceMap.get("date");
    String availAllCenter = (String) resourceMap.get("_dialog_center");
    String availBelongCenter = (String) resourceMap.get("dialog_center");
    int availResourceCenter;
    if (availAllCenter != null && !availAllCenter.equals("")) {
      availResourceCenter = loggedIncenter == 0 ? Integer.parseInt(availAllCenter) : loggedIncenter;
    } else {
      availResourceCenter = loggedIncenter == 0 ? Integer.parseInt(availBelongCenter)
          : loggedIncenter;
    }

    String resCenterId = (String) resourceMap.get("centerId");
    Integer centerId = resCenterId.equals("") ? 0 : Integer.parseInt(resCenterId);
    Time slotFromTime = DataBaseUtil.parseTime((String) resourceMap.get("avFirstSlotFromTime"));
    Time slotToTime = DataBaseUtil.parseTime((String) resourceMap.get("avFirstSlotToTime"));
    List<BasicDynaBean> resourceAvailNonAvailList = null;
    Time sfromTime = null;
    Time stoTime = null;
    String errorMsg = null;
    java.sql.Date date = DataBaseUtil.parseDate(dateStr);
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    int weekDayNo = (cal.get(Calendar.DAY_OF_WEEK) - 1);
    boolean success = false;
    Integer maxCentersDefault = (Integer) prefService.getPreferences().get(
        "max_centers_inc_default");
    String resourceType = null;
    int index = 0;
    String status = null;
    if (category.equals("DOC")) {
      resourceType = category;
    }

    if (slotFromTime != null && slotToTime != null) {
      resourceAvailNonAvailList = resourceOverrideRepository.getResourceOverrides(resourceType,
          date, null, Arrays.asList(resourceName), status, centerId, maxCentersDefault);
      if (resourceAvailNonAvailList != null && resourceAvailNonAvailList.size() > 0) {
        for (BasicDynaBean resourceAvailBean : resourceAvailNonAvailList) {
          boolean flag = false;
          Time fromTime = (Time) resourceAvailBean.get("from_time");
          Time toTime = (Time) resourceAvailBean.get("to_time");

          if (slotFromTime.equals(toTime) || slotToTime.equals(fromTime)
              || slotFromTime.equals(fromTime) || slotToTime.equals(toTime)) {
            flag = true;
            if (flag) {
              timingsList.add(fromTime);
              timingsList.add(toTime);
            }
          }

          if ((slotFromTime.equals(fromTime) || slotFromTime.after(fromTime) || slotFromTime
              .before(toTime))
              && ((slotToTime.equals(toTime) || slotToTime.before(toTime)) || slotFromTime
                  .before(toTime))) {
            if (index == 0) {
              index++;
              timingsList.add(fromTime);
              timingsList.add(toTime);
            } else {
              timingsList.add(fromTime);
              timingsList.add(toTime);
            }

          } else if (slotFromTime.equals(fromTime) || slotFromTime.after(fromTime)
              && slotToTime.equals(toTime) || slotToTime.before(toTime)) {
            timingsList.add(fromTime);
            timingsList.add(toTime);
          }
        }
      }
      if (resourceAvailNonAvailList != null && resourceAvailNonAvailList.size() < 1) {
        resourceAvailNonAvailList = resourceOverrideRepository.getResourceOverrides(resourceType,
            date, null, Arrays.asList(resourceName), status, centerId, maxCentersDefault);
        BasicDynaBean resourceAvailBean = resourceAvailNonAvailList.get(0);
        Time fromTime = (Time) resourceAvailBean.get("from_time");
        Time toTime = (Time) resourceAvailBean.get("to_time");
        if (slotFromTime.equals(fromTime) || slotFromTime.after(fromTime)
            && slotToTime.equals(toTime) || slotToTime.before(toTime)) {
          timingsList.add(fromTime);
          timingsList.add(toTime);
        }
      }

      if (resourceAvailNonAvailList != null && resourceAvailNonAvailList.size() < 1) {
        resourceAvailNonAvailList = categoryRepository.getDefaultResourceAvailabilities("*",
            weekDayNo, resourceName, status, centerId, maxCentersDefault);
        BasicDynaBean resourceAvailBean = resourceAvailNonAvailList.get(0);
        Time fromTime = (Time) resourceAvailBean.get("from_time");
        Time toTime = (Time) resourceAvailBean.get("to_time");
        if (slotFromTime.equals(fromTime) || slotFromTime.after(fromTime)
            && slotToTime.equals(toTime) || slotToTime.before(toTime)) {
          timingsList.add(fromTime);
          timingsList.add(toTime);
        }
      }
    }
    return timingsList;
  }

  /**
   * Override exists.
   *
   * @param detailsMap
   *          the details map
   * @param existingAvailabiltyRecords
   *          the existing availabilty records
   * @return true, if successful
   */
  public boolean overrideExists(Map detailsMap, List<BasicDynaBean> existingAvailabiltyRecords) {
    boolean recordExistsWithinGivenDateRange = false;
    String resType = (String) detailsMap.get("resType");
    String resName = (String) detailsMap.get("resName");
    java.sql.Date fromDate = (java.sql.Date) detailsMap.get("fromDate");
    java.sql.Date toDate = (java.sql.Date) detailsMap.get("toDate");
    Date availabilityDate = null;
    List<BasicDynaBean> existingRecords = new ArrayList<BasicDynaBean>();
    existingRecords = resourceOverrideRepository.getResources(resType, resName);
    if (existingRecords != null) {
      for (BasicDynaBean existingAvailabiltyRecord : existingRecords) {
        availabilityDate = (java.util.Date) existingAvailabiltyRecord.get("availability_date");
        if (availabilityDate.equals(fromDate) || availabilityDate.equals(toDate)) {
          recordExistsWithinGivenDateRange = true;
          break;
        }
      }
    }
    return recordExistsWithinGivenDateRange;
  }

  /**
   * Gets the resource overrides.
   *
   * @param resourceId
   *          the resource id
   * @param resourceType
   *          the resource type
   * @param fromDate
   *          the from date
   * @param endDate
   *          the end date
   * @param availabilityStatus
   *          the availability status
   * @param centerId
   *          the center id
   * @return the resource overrides
   */
  public List<BasicDynaBean> getResourceOverrides(String resourceId, String resourceType,
      java.sql.Date fromDate, java.sql.Date endDate, String availabilityStatus, Integer centerId) {
    return getResourceOverrides(Arrays.asList(resourceId), resourceType, fromDate, endDate,
        availabilityStatus, centerId);
  }


  /**
   * Gets the resource overrides.
   *
   * @param resourceIds
   *          the resource ids
   * @param resourceType
   *          the resource type
   * @param fromDate
   *          the from date
   * @param endDate
   *          the end date
   * @param availabilityStatus
   *          the availability status
   * @param centerId
   *          the center id
   * @return the resource overrides
   */
  
  public List<BasicDynaBean> getResourceOverrides(List<String> resourceIds, String resourceType,
      java.sql.Date fromDate, java.sql.Date endDate, String availabilityStatus, Integer centerId) {
    int centersIncDefault = (Integer) prefService.getAllPreferences()
        .get("max_centers_inc_default");
    return resourceOverrideRepository.getResourceOverrides(resourceType, fromDate, endDate,
        resourceIds, availabilityStatus, centerId, centersIncDefault);
  }

  /**
   * Gets the resource by avail date.
   *
   * @param resourceType
   *          the resource type
   * @param resourceName
   *          the resource name
   * @param date
   *          the date
   * @param fromDate
   *          the from date
   * @param toDate
   *          the to date
   * @return the resource by avail date
   */
  public List<BasicDynaBean> getResourceByAvailDate(String resourceType, String resourceName,
      java.sql.Date date, java.sql.Date fromDate, java.sql.Date toDate) {
    return resourceOverrideRepository.getResourceByAvailDate(resourceType, resourceName, date,
        fromDate, toDate);
  }

  /**
   * List all.
   *
   * @param columns
   *          the columns
   * @param filterMap
   *          the filter map
   * @param sortColumn
   *          the sort column
   * @return the list
   */
  public List<BasicDynaBean> listAll(List<String> columns, Map<String, Object> filterMap,
      String sortColumn) {
    return resourceDetailsRepository.listAll(columns, filterMap, sortColumn);
  }

  /**
   * Send warning message.
   *
   * @param appointmentIds
   *          the appointment ids
   * @param appointmentStatus
   *          the appointment status
   */
  private void sendWarningMessage(List appointmentIds, List<String> appointmentStatus) {
    String userName = (String) sessionService.getSessionAttributes().get("userId");
    Map<String, Object> jobData = new HashMap<String, Object>();
    jobData.put("userName", userName);
    jobData.put("eventData", appointmentIds);
    jobData.put("status", appointmentStatus);
    jobData.put("schema", RequestContext.getSchema());
    String uniqueName = DateUtil.getCurrentIso8601Timestamp() + "" + RequestContext.getUserName();
    jobService.scheduleImmediate(buildJob("AppointmentRescheduleJob" + uniqueName,
        AppointmentRescheduleJob.class, jobData));
  }

  /**
   * Gets the resource appointments.
   *
   * @param resourceId
   *          the resource id
   * @param resourceType
   *          the resource type
   * @param availDate
   *          the avail date
   * @param fromDate
   *          the from date
   * @param toDate
   *          the to date
   * @return the resource appointments
   */
  public String getResourceAppointments(String resourceId, String resourceType,
      java.sql.Date availDate, java.sql.Date fromDate, java.sql.Date toDate) {
    Map appointmentDetails = new HashMap();
    String message = null;
    DateUtil du = new DateUtil();

    if (resourceType != null
        && (resourceType.equals("DOC") || resourceType.equals("EQID")
            || resourceType.equals("THID") || resourceType.equals("SRID"))) {
      String schCategory = null;
      if (resourceType != null) {
        if (resourceType.equals("DOC")) {
          schCategory = "DOC";
        }
        if (resourceType.equals("EQID")) {
          schCategory = "DIA";
        }
        if (resourceType.equals("THID")) {
          schCategory = "OPE";
        }
        if (resourceType.equals("SRID")) {
          schCategory = "SNP";
        }
      }

      List<BasicDynaBean> resourceUnavailableDates = null;
      boolean flag = true;
      List appointmentIds = new ArrayList();
      List<String> appointmentStatus = new ArrayList<String>();

      resourceUnavailableDates = resourceOverrideRepository.getResourceByAvailDate(resourceType,
          resourceId, availDate, fromDate, toDate);

      if (resourceUnavailableDates != null && resourceUnavailableDates.size() > 0) {
        for (BasicDynaBean resourceUnavailableDateBean : resourceUnavailableDates) {
          Date availabilityDate = (Date) resourceUnavailableDateBean.get("availability_date");
          Integer resAvailId = (Integer) resourceUnavailableDateBean.get("res_avail_id");
          Map<String, Object> filterMap = new HashMap<String, Object>();
          filterMap.put("res_avail_id", resAvailId);
          filterMap.put("availability_status", "N");
          List<BasicDynaBean> resourceUnavailableDetails = resourceDetailsRepository.listAll(null,
              filterMap, "res_avail_details_id");
          if (resourceUnavailableDetails != null && resourceUnavailableDetails.size() > 0) {
            for (BasicDynaBean resourceUnavialBean : resourceUnavailableDetails) {
              Time fromTime = (Time) resourceUnavialBean.get("from_time");
              Time endTime = (Time) resourceUnavialBean.get("to_time");
              Timestamp apptStartTime = null;
              Timestamp apptEndTime = null;
              try {
                DateUtil dateUtil = new DateUtil();
                apptStartTime = dateUtil.parseTheTimestamp(du.getDateFormatter().format(
                    availabilityDate)
                    + " " + fromTime.toString());
                apptEndTime = dateUtil.parseTheTimestamp(du.getDateFormatter().format(
                    availabilityDate)
                    + " " + endTime.toString());
              } catch (ParseException exception) {
                logger.debug("Failed to parse the date details" + exception.getMessage());
              }
              List<BasicDynaBean> resourceAppointmentList = appointmentService
                  .getResourceAppointments(resourceId, schCategory, apptStartTime, apptEndTime);
              for (BasicDynaBean resourceAppointment : resourceAppointmentList) {
                String appointStatus = (String) resourceAppointment.get("appointment_status");
                Integer appointmentId = (Integer) resourceAppointment.get("appointment_id");
                message = "There are appointments existing for these "
                    + "timeslots marked non available.Please reschedule.";
                if (resourceType.equals("DOC")) {
                  if (appointStatus.equalsIgnoreCase("Booked")
                      || appointStatus.equalsIgnoreCase("Confirmed")
                      && !(appointmentService
                          .getAppointmentSource(appointmentId) != null 
                      && appointmentService
                          .getAppointmentSource(appointmentId).equalsIgnoreCase("practo"))) {
                    appointmentIds.add(appointmentId);
                    appointmentStatus.add(appointStatus);
                  }
                }
              }
            }
          }
        }
      }
      if (appointmentIds != null && appointmentIds.size() > 0 && appointmentStatus != null
          && appointmentStatus.size() > 0) {
        sendWarningMessage(appointmentIds, appointmentStatus);
      }
    }
    return message;
  }

  /**
   * Gets the resource avail date.
   *
   * @param resourceMap
   *          the resource map
   * @return the resource avail date
   */
  public Integer getResourceAvailDate(Map resourceMap) {
    String resourceType = (String) resourceMap.get("resourceType");
    String resourceName = (String) resourceMap.get("resourceName");
    java.sql.Date colDate = null;
    try {
      SimpleDateFormat sdf1 = new SimpleDateFormat("dd-mm-yyyy");
      java.util.Date date = sdf1.parse((String) resourceMap.get("colDate"));
      colDate = new java.sql.Date(date.getTime());
    } catch (ParseException exception) {
      logger.debug("Failed to parse the date details" + exception.getMessage());
    }
    java.sql.Date fromDate = null;
    java.sql.Date toDate = null;
    Integer availId = null;
    List<BasicDynaBean> resources = resourceOverrideRepository.getResourceByAvailDate(resourceType,
        resourceName, colDate, fromDate, toDate);
    if (resources != null && resources.size() > 0) {
      return availId = (Integer) resources.get(0).get("res_avail_id");
    } else {
      return 0;
    }
  }

  /**
   * Save bulk override details.
   *
   * @param params
   *          the map
   * @return map list
   */
  @Transactional(rollbackFor = Exception.class)
  public List<Map> saveBulkOverrideDetails(Map params) {
    
    String rightToOverride = (String) ((Map) RequestContext.getSession()
        .getAttribute("urlRightsMap"))
        .get("res_availability");
    Map<String, String> actionRightsMap = (Map<String, String>) securityService
          .getSecurityAttributes().get("actionRightsMap");
    if (!rightToOverride.equalsIgnoreCase("A")
        && !(params.get("screen") != null && params.get("screen").equals("blockCalendar"))) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("override_for", "exception.scheduler.error.rights.override");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("timings", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    Map<String, Object> timingsInfo = (Map<String, Object>) params.get("timings");
    java.sql.Date startDate = null;
    java.sql.Date endDate = null;
    Integer centerId = null;
    Time slotFromTime = null;
    Time slotToTime = null;
    List<Map> mapList = new ArrayList();
    Map originalTimingsInfoMap = new HashMap();
    originalTimingsInfoMap.putAll(timingsInfo);
    List<Map> originalAvailabilityList = new ArrayList();
    String startDateStr = (String) timingsInfo.get("start_date");
    ValidationErrorMap errorMap = new ValidationErrorMap();
    if (timingsInfo.get("resource_id") == null || timingsInfo.get("resource_id").equals("")) {
      errorMap.addError("resource_id", "exception.scheduler.override.invalid.resourcedetails");
      throwValidationException(errorMap);
    }
    if (timingsInfo.get("resource_type") == null || timingsInfo.get("resource_id").equals("")) {
      errorMap.addError("resource_type", "exception.scheduler.override.invalid.resourcedetails");
      throwValidationException(errorMap);
    }
    if (timingsInfo.get("start_date") == null || timingsInfo.get("end_date") == null) {
      errorMap.addError("start_date", "exception.scheduler.override.invalid.dates");
      throwValidationException(errorMap);
    }
    try {
      startDate = DateUtil.parseDate((String) timingsInfo.get("start_date"));
      endDate = DateUtil.parseDate((String) timingsInfo.get("end_date"));
    } catch (Exception exe) {
      errorMap.addError("start_date", "exception.scheduler.override.invalid.dates");
      throwValidationException(errorMap);
    }

    List<Map> availabilityList = (List<Map>) timingsInfo.get("availability_details");
    String startTimeStr = (String) availabilityList.get(0).get("start_time");
    for (int i = 0; i < availabilityList.size(); i++) {
      Map availabilityDetail = (Map) availabilityList.get(i);
      if (availabilityDetail.get("start_time") != null
          && !availabilityDetail.get("start_time").equals("")) {
        try {
          slotFromTime = DateUtil.parseTime((String) availabilityDetail.get("start_time"));
        } catch (ParseException exe) {
          errorMap.addError("start_time", "exception.scheduler.override.invalid.time");
          List<Map> validatetempl = new ArrayList<Map>();
          ValidationException ex = new ValidationException(errorMap);
          validatetempl.add(ex.getErrors());
          Map<String, Object> availabilityMap = new HashMap<String, Object>();
          availabilityMap.put("availability_details", validatetempl);
          Map<String, Object> nestedException = new HashMap<String, Object>();
          nestedException.put("timings", availabilityMap);
          throw new NestableValidationException(nestedException);
        }
        availabilityDetail.put("start_time", slotFromTime);
      } else {
        errorMap.addError("start_time", "exception.scheduler.override.invalid.time");
        List<Map> validatetempl = new ArrayList<Map>();
        ValidationException ex = new ValidationException(errorMap);
        validatetempl.add(ex.getErrors());
        Map<String, Object> availabilityMap = new HashMap<String, Object>();
        availabilityMap.put("availability_details", validatetempl);
        Map<String, Object> nestedException = new HashMap<String, Object>();
        nestedException.put("timings", availabilityMap);
        throw new NestableValidationException(nestedException);
      }
      if (availabilityDetail.get("end_time") != null
          && !availabilityDetail.get("end_time").equals("")) {
        try {
          slotToTime = DateUtil.parseTime((String) availabilityDetail.get("end_time"));
        } catch (ParseException exe) {
          errorMap.addError("end_time", "exception.scheduler.override.invalid.time");
          List<Map> validatetempl = new ArrayList<Map>();
          ValidationException ex = new ValidationException(errorMap);
          validatetempl.add(ex.getErrors());
          Map<String, Object> availabilityMap = new HashMap<String, Object>();
          availabilityMap.put("availability_details", validatetempl);
          Map<String, Object> nestedException = new HashMap<String, Object>();
          nestedException.put("timings", availabilityMap);
          throw new NestableValidationException(nestedException);
        }
        availabilityDetail.put("end_time", slotToTime);
      } else {
        errorMap.addError("end_time", "exception.scheduler.override.invalid.time");
        List<Map> validatetempl = new ArrayList<Map>();
        ValidationException ex = new ValidationException(errorMap);
        validatetempl.add(ex.getErrors());
        Map<String, Object> availabilityMap = new HashMap<String, Object>();
        availabilityMap.put("availability_details", validatetempl);
        Map<String, Object> nestedException = new HashMap<String, Object>();
        nestedException.put("timings", availabilityMap);
        throw new NestableValidationException(nestedException);
      }
      if (availabilityDetail.get("center_id") != null) {
        centerId = (Integer) availabilityDetail.get("center_id");
        Integer maxCentersDefault = (Integer) prefService.getPreferences().get(
            "max_centers_inc_default");
        /*
         * if (maxCentersDefault > 1 && centerId == 0) { errorMap.addError("center_id",
         * "exception.scheduler.override.invalid.time"); List<Map> validatetempl = new
         * ArrayList<Map>(); ValidationException ex = new ValidationException(errorMap);
         * validatetempl.add(ex.getErrors()); Map<String, Object> availabilityMap = new
         * HashMap<String, Object>(); availabilityMap.put("availability_details", validatetempl);
         * Map<String, Object> nestedException = new HashMap<String, Object>();
         * nestedException.put("timings", availabilityMap); throw new
         * NestableValidationException(nestedException); }
         */
        availabilityDetail.put("center_id", centerId);
      } else {
        if (((String) availabilityDetail.get("availability_status")).equalsIgnoreCase("A")) {
          
          errorMap.addError("center_id", "exception.scheduler.override.invalid.center_id");
          List<Map> validatetempl = new ArrayList<Map>();
          ValidationException ex = new ValidationException(errorMap);
          validatetempl.add(ex.getErrors());
          Map<String, Object> availabilityMap = new HashMap<String, Object>();
          availabilityMap.put("availability_details", validatetempl);
          Map<String, Object> nestedException = new HashMap<String, Object>();
          nestedException.put("timings", availabilityMap);
          throw new NestableValidationException(nestedException);
        }
      }
      if (slotFromTime.after(slotToTime) || slotFromTime.equals(slotToTime)) {
        errorMap.addError("end_time", "exception.scheduler.override.starttime.after.endtime");
        List<Map> validatetempl = new ArrayList<Map>();
        ValidationException ex = new ValidationException(errorMap);
        validatetempl.add(ex.getErrors());
        Map<String, Object> availabilityMap = new HashMap<String, Object>();
        availabilityMap.put("availability_details", validatetempl);
        Map<String, Object> nestedException = new HashMap<String, Object>();
        nestedException.put("timings", availabilityMap);
        throw new NestableValidationException(nestedException);
      }
      Map originalavailabilityDetail = new HashMap();
      originalavailabilityDetail.putAll(availabilityDetail);
      originalAvailabilityList.add(originalavailabilityDetail);
    }
    Timestamp startTimeStamp = null;
    Timestamp currentTimeStamp = null;
    try {
      startTimeStamp = DateUtil.parseTimestamp(startDateStr + " " + startTimeStr);
      currentTimeStamp = DateUtil.getCurrentTimestamp();
    } catch (ParseException exception) {
      errorMap.addError("end_time", "exception.scheduler.override.invalid.time");
      List<Map> validatetempl = new ArrayList<Map>();
      ValidationException ex = new ValidationException(errorMap);
      validatetempl.add(ex.getErrors());
      Map<String, Object> availabilityMap = new HashMap<String, Object>();
      availabilityMap.put("availability_details", validatetempl);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("timings", availabilityMap);
      throw new NestableValidationException(nestedException);
    }
    Calendar cal1 = Calendar.getInstance();
    Calendar cal2 = Calendar.getInstance();
    cal1.setTime(startDate);
    cal2.setTime(endDate);
    cal2.add(Calendar.DAY_OF_MONTH, 1);
    while (cal1.before(cal2)) {
      Calendar currCal = Calendar.getInstance();
      currCal.setTime(DateUtil.getCurrentDate());
      if (currCal.after(cal1) && !("A".equals(actionRightsMap.get("allow_backdated_app"))
          || String.valueOf(actionRightsMap.get("all_rights")).equals("true"))) {
        errorMap.addError("start_date", "exception.scheduler.override.old.dates");
        ValidationException ex = new ValidationException(errorMap);
        Map<String, Object> nestedException = new HashMap<String, Object>();
        nestedException.put("timings", ex.getErrors());
        throw new NestableValidationException(nestedException);
      } else if (startTimeStamp.before(currentTimeStamp)
          && !("A".equals(actionRightsMap.get("allow_backdated_app"))
          || String.valueOf(actionRightsMap.get("all_rights")).equals("true"))
          && timingsInfo.get("override_for").equals("H")) {
        errorMap.addError("start_time", "exception.scheduler.override.old.dates");
        List<Map> validatetempl = new ArrayList<Map>();
        ValidationException ex = new ValidationException(errorMap);
        validatetempl.add(ex.getErrors());
        Map<String, Object> availabilityMap = new HashMap<String, Object>();
        availabilityMap.put("availability_details", validatetempl);
        Map<String, Object> nestedException = new HashMap<String, Object>();
        nestedException.put("timings", availabilityMap);
        throw new NestableValidationException(nestedException);
      }
      timingsInfo.clear();
      timingsInfo.putAll(originalTimingsInfoMap);
      List tempList = new ArrayList();
      for (Map availabilityDetail : originalAvailabilityList) {
        tempList.add(availabilityDetail);
      }
      timingsInfo.remove("availability_details");
      timingsInfo.put("availability_details", tempList);
      timingsInfo.remove("start_date");
      timingsInfo.remove("end_date");
      timingsInfo.put("start_date", new java.sql.Date(cal1.getTimeInMillis()));
      timingsInfo.put("end_date", new java.sql.Date(cal1.getTimeInMillis()));
      mapList.add(saveOverrideDetails(params, originalAvailabilityList));
      cal1.add(Calendar.DAY_OF_MONTH, 1);
    }
    return mapList;
  }

  /**
   * Save the override details.
   *
   * @param params
   *          the params
   * @param originalAvailabilityList
   *          the original availability list
   * @return map
   */
  @SuppressWarnings({ "unchecked", "rawtypes", "unused" })
  public Map saveOverrideDetails(Map params, List<Map> originalAvailabilityList) {
    Map<String, Object> timingsInfo = (Map<String, Object>) params.get("timings");
    List<BasicDynaBean> existingAvailabiltyRecords = new ArrayList<BasicDynaBean>();
    java.sql.Date fromDate = null;
    java.sql.Date toDate = null;
    String cancelAppt = (String) timingsInfo.get("cancel_existing_appointments");
    List<Map> availabilityList = (List<Map>) timingsInfo.get("availability_details");
    List<Map> finalResponseList = new ArrayList();
    Map availInfoMap = new HashMap();
    fromDate = (java.sql.Date) timingsInfo.get("start_date");
    toDate = (java.sql.Date) timingsInfo.get("end_date");
    Calendar cal1 = Calendar.getInstance();
    cal1.setTime(fromDate);
    Calendar cal2 = Calendar.getInstance();
    cal2.setTime(toDate);
    int weekDayNo = (cal1.get(Calendar.DAY_OF_WEEK) - 1);
    Integer maxCentersDefault = (Integer) prefService.getPreferences().get(
        "max_centers_inc_default");
    int resAvailId = 0;
    boolean success = true;
    boolean existingRecords = false;
    String warningMessage = null;
    BasicDynaBean bean = resourceOverrideRepository.getBean();
    List errors = new ArrayList();
    ConversionUtils.copyToDynaBean(params, bean, errors);
    Map overrideDetailsMap = new HashMap();
    overrideDetailsMap.put("fromDate", fromDate);
    overrideDetailsMap.put("toDate", toDate);
    String resType = (timingsInfo.get("resource_type") != null ? (String) timingsInfo
        .get("resource_type") : "DOC");
    overrideDetailsMap.put("resType", resType);
    String resName = (String) timingsInfo.get("resource_id");
    overrideDetailsMap.put("resName", resName);
    Map overlapMap = new HashMap();
    /* request is from API */
    if (timingsInfo.get("override_for").equals("H")) {
      availInfoMap.put("resName", resName);
      availInfoMap.put("resType", resType);
      availInfoMap.put("weekDayNo", weekDayNo);
      availInfoMap.put("maxCentersDefault", maxCentersDefault);
      availInfoMap.put("start_date", fromDate);
      availInfoMap.put("end_date", toDate);
      availInfoMap.put("availability_details", availabilityList);
      getAvailabilityDetails(availInfoMap, overlapMap);
      availabilityList = (List<Map>) availInfoMap.get("availability_details");
      timingsInfo.remove("availability_details");
      timingsInfo.put("availability_details", availabilityList);
    } else {
      String overrideForAllCenters = timingsInfo
          .get("override_for_all_centers") != null ? (String) timingsInfo
          .get("override_for_all_centers") : "Y";
      if (overrideForAllCenters.equalsIgnoreCase("N")) {
        List overrideList = new ArrayList();
        overrideForOneCenterForEntireDay(overrideList, resType, resName,
                    fromDate, toDate, weekDayNo, 
                    (String) availabilityList.get(0).get("availability_status"),
                    (String) availabilityList.get(0).get("remarks"), 
                    (String) availabilityList.get(0).get("visit_mode"));

        availabilityList = overrideList;
      } else {
        existingRecords = overrideExists(overrideDetailsMap, existingAvailabiltyRecords);
        if (existingRecords) {
          ValidationErrorMap errorMap = new ValidationErrorMap();
          errorMap.addError("start_date", "exception.scheduler.override.date.alreadyexists.record");
          throwValidationException(errorMap);
        }
        Map resourcesInfo = new HashMap();
        getResourceAvailabilityList(resourcesInfo, resType, resName, fromDate, toDate, weekDayNo);
        List resourceAvailabilityList = (List) resourcesInfo.get("resources_list");
        BasicDynaBean overrideBean = (BasicDynaBean) resourcesInfo.get("override_bean");
        if (overrideBean != null) {
          resAvailId = (Integer) overrideBean.get("res_avail_id");
          resourceDetailsRepository.delete("res_avail_id", resAvailId);
          resourceOverrideRepository.delete("res_avail_id", resAvailId);
        }
        Map slotMap = availabilityList.get(0);
        slotMap.put("default_value", "false");
        slotMap.put("r_delete", "false");
      }
    }
    if (!existingRecords) {
      if (cal1.compareTo(cal2) == 0) {
        bean = resourceOverrideRepository.getBean();
        ConversionUtils.copyToDynaBean(params, bean, errors);
        resAvailId = (Integer) resourceOverrideRepository.getNextId();
        bean.set("res_avail_id", resAvailId);
        bean.set("availability_date", new java.sql.Date(cal1.getTime().getTime()));
        bean.set("res_sch_name", resName);
        bean.set("res_sch_type", resType);
        success = resourceOverrideRepository.insert(bean) > 0;
        if (saveResourceOverride(availabilityList, resAvailId) == null) {
          success = true;
        } else {
          success = false;
        }
      } /*
         * else { if (success) { while (cal1.before(cal2)) { bean =
         * resourceOverrideRepository.getBean(); ConversionUtils.copyToDynaBean(params, bean,
         * errors); resAvailId = (Integer) resourceOverrideRepository.getNextId();
         * bean.set("res_avail_id", resAvailId); bean.set("availability_date", new
         * java.sql.Date(cal1.getTime().getTime())); bean.set("res_sch_name", resName);
         * bean.set("res_sch_type", resType); success = resourceOverrideRepository.insert(bean) > 0;
         * if (saveResourceOverride(availabilityList, resAvailId) == null) { success = true; } else
         * { success = false; break; } cal1.add(Calendar.DATE, 1); } } if (success) { bean =
         * resourceOverrideRepository.getBean(); ConversionUtils.copyToDynaBean(params, bean,
         * errors); resAvailId = (Integer) resourceOverrideRepository.getNextId();
         * bean.set("res_avail_id", resAvailId); bean.set("availability_date", new
         * java.sql.Date(cal2.getTime().getTime())); bean.set("res_sch_name", resName);
         * bean.set("res_sch_type", resType); success = resourceOverrideRepository.insert(bean) > 0;
         * if (saveResourceOverride(availabilityList, resAvailId) == null) { success = true; } else
         * { success = false; } } }
         */
      if (success) {
        warningMessage = getResourceAppointments(resName, resType, null,
            new java.sql.Date(fromDate.getTime()), new java.sql.Date(toDate.getTime()));
        success = true;
      }
      if (warningMessage != null) {
        params.put("warningMesssage", warningMessage);
      }
    } else {
      success = false;
    }

    if (overlapMap.get("overlap") != null && (Boolean) overlapMap.get("overlap") == true) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      if (overlapMap.get("culprit") != null) {
        errorMap.addError((String) overlapMap.get("culprit"),
            "exception.scheduler.override.overlapping.slots");
      }
      if (overlapMap.get("culprit2") != null) {
        errorMap.addError((String) overlapMap.get("culprit2"),
            "exception.scheduler.override.overlapping.slots");
      }
      List<Map> validatetempl = new ArrayList<Map>();
      ValidationException ex = new ValidationException(errorMap);
      validatetempl.add(ex.getErrors());
      Map<String, Object> availabilityMap = new HashMap<String, Object>();
      availabilityMap.put("availability_details", validatetempl);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("timings", availabilityMap);
      throw new NestableValidationException(nestedException);
    }

    if (success && PractoBookHelper.isPractoAdvantageEnabled()) {
      try {
        PractoBookHelper.addUpdateOverridesToPracto((String) bean.get("res_sch_name"),
            new java.sql.Date(fromDate.getTime()), new java.sql.Date(toDate.getTime()),
            RequestContext.getCenterId() == 0 ? null : RequestContext.getCenterId());
      } catch (Exception exe) {
        ValidationErrorMap errorMap = new ValidationErrorMap();
        errorMap.addError("practo", "exception.scheduler.practo.shareinfo");
        ValidationException ex = new ValidationException(errorMap);
        Map<String, Object> nestedException = new HashMap<String, Object>();
        nestedException.put("practo", ex.getErrors());
        throw new NestableValidationException(nestedException);
      }
    }
    Boolean orderSetApptPresent = false;
    if (success) {
      if (cancelAppt.equalsIgnoreCase("Y")) {
        for (Map slot : originalAvailabilityList) {
          if (((String) slot.get("availability_status")).equalsIgnoreCase("Y")) {
            continue;
          }
          Map slotMap = new HashMap();
          SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
          slotMap.put("start_date", new String[] { format.format(timingsInfo.get("start_date")) });
          slotMap.put("end_date", new String[] { format.format(timingsInfo.get("end_date")) });
          slotMap.put("skip_status", new String[] { "Cancel" });
          slotMap.put("max_centers_inc_default", new String[] { maxCentersDefault.toString() });
          slotMap.put("center_id", new String[] { slot.get("center_id").toString() });
          slotMap.put("resources", new String[] { resName });
          slotMap.put("start_time", slot.get("start_time"));
          slotMap.put("end_time", slot.get("end_time"));
          slotMap.put("visit_mode", slot.get("visit_mode"));
          slotMap.put("screen", params.get("screen"));
          orderSetApptPresent = cancelApptsOnOverride(slotMap);
        }
      }
      Map tempMap = new HashMap();
      List pushList = new ArrayList();
      for (Map slot : availabilityList) {
        Map tempPushMap = new HashMap();
        tempPushMap.put("from_time", slot.get("start_time"));
        tempPushMap.put("to_time", slot.get("end_time"));
        tempPushMap.put("availability_status", slot.get("availability_status"));
        tempPushMap.put("remarks", slot.get("remarks"));
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        tempPushMap.put("availability_date", sdf.format(timingsInfo.get("start_date")));
        tempPushMap.put("center_id", slot.get("center_id"));
        tempPushMap.put("visit_mode", slot.get("visit_mode"));
        if (slot.get("r_delete").equals("false")) {
          finalResponseList.add(tempPushMap);
        }
      }
      for (Map slot : finalResponseList) {
        Map pushSlot = new HashMap();
        pushSlot.putAll(slot);
        String status = (String) pushSlot.get("availability_status");
        Integer centerId = (Integer) pushSlot.get("center_id");
        int loggedInCenter = RequestContext.getCenterId();
        if (status.equalsIgnoreCase("N")) {
          pushSlot.put("center_id",0);
          pushSlot.put("availability_status","N");
        }
        pushList.add(pushSlot);
      }
      SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
      tempMap.put("availability_date", sdf.format(timingsInfo.get("start_date")));
      tempMap.put("resource_id", resName);
      tempMap.put("overrides", pushList);
      Map pushMap = new HashMap();
      pushMap.put("resource_calendar", Arrays.asList(new Object[] { tempMap }));
      pushToWebSockets(pushMap);
      finalResponseList = pushList;
    }
    if (params.get("r_delete") != null) {
      params.put("resourceBean", bean);
      params.put("redirectStatus", success);
      return params;
    } else {
      timingsInfo.remove("availability_details");
      String date = null;
      SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
      date = sdf.format(timingsInfo.get("start_date"));

      timingsInfo.put("start_date", date);
      timingsInfo.put("end_date", date);
      timingsInfo.put("availability_details", finalResponseList);
      timingsInfo.put("order_set_present", orderSetApptPresent);
      return timingsInfo;
    }

  }

  /**
   * Push to web sockets.
   *
   * @param pushMap
   *          the push map
   */
  private void pushToWebSockets(Map pushMap) {
    this.pushService.push(WEBSOCKET_PUSH_CHANNEL, pushMap);
  }

  /**
   * Cancel appts on override.
   *
   * @param params          the params
   * @return the boolean
   */
  private Boolean cancelApptsOnOverride(Map params) {

    String category = params.get("res_type") != null ? (String) params.get("res_type") : "DOC";
    AppointmentCategory apptCategory = appointmentCategoryFactory.getInstance(category
        .toUpperCase(Locale.ENGLISH));
    List<BasicDynaBean> appts = appointmentService.getAppointments(apptCategory, params);

    Boolean orderSetPresent = false;
    for (BasicDynaBean appt : appts) {

      String apptStatus = (String) appt.get("appointment_status");
      if (apptStatus.equalsIgnoreCase("noshow") 
          || apptStatus.equalsIgnoreCase("arrived")
          || apptStatus.equalsIgnoreCase("completed")) {
        continue;
      }
      Integer packageId = (Integer) appt.get("package_id"); 
      if (packageId != null) {
        orderSetPresent = true;
        continue;
      }
      Timestamp apptTime = (Timestamp) appt.get("appointment_time");
      int duration = (int) appt.get("duration");
      long longTime = apptTime.getTime();
      longTime =  longTime + (duration * 60 * 1000);
      Timestamp endTimestamp = new java.sql.Timestamp(longTime);
      Time startTime = (Time) params.get("start_time");
      Time endTime = (Time) params.get("end_time");
      Timestamp startTimeL = null;
      Timestamp endTimeL = null;
      String[] startDate = (String[]) params.get("start_date");
      try {
        DateUtil dateUtil = new DateUtil();
        startTimeL = dateUtil.parseTheTimestamp(startDate[0] + " " + startTime.toString());
        endTimeL = dateUtil.parseTheTimestamp(startDate[0] + " " + endTime.toString());
      } catch (ParseException e1) {
        e1.printStackTrace();
      }
      if (endTimestamp.before(startTimeL) 
          || endTimestamp.equals(startTimeL) 
          || endTimeL.before(apptTime) 
          || endTimeL.equals(apptTime)) {
        continue;
      }
      if (params.get("screen") != null && params.get("screen").equals("blockCalendar")
          && !((String)appt.get("is_patient_group_accessible")).equalsIgnoreCase("Y")) {
        ValidationErrorMap validationErrors = new ValidationErrorMap();
        validationErrors.addError("confidential_patient","exception.cannot.block.calendar");
        ValidationException ex = new ValidationException(validationErrors);
        Map<String, Object> availabilityMap = new HashMap<String, Object>();
        availabilityMap.put("availability_details", ex.getErrors());
        Map<String, Object> nestedException = new HashMap<String, Object>();
        nestedException.put("timings", availabilityMap);
        throw new NestableValidationException(nestedException);
      }
      Map<String, Object> cancelParams = new HashMap<String, Object>();
      cancelParams.put("appointment_id", appt.get("appointment_id"));
      cancelParams.put("appointment_status", "Cancel");
      cancelParams.put("cancel_reason", "doctor is on leave");
      cancelParams.put("cancel_type", "Other");
      cancelParams.put("category", "DOC");
      List<Map> cancelParamsList = new ArrayList();
      cancelParamsList.add(cancelParams);
      Map updateStatusMap = new HashMap();
      updateStatusMap.put("update_app_status", cancelParamsList);
      try {
        Map<String, Object> response = appointmentService.updateAppointmentsStatus(updateStatusMap);
        appointmentService.pushUpdateAppointmentStatusToRedis(response);
      } catch (Exception exe) {
        exe.printStackTrace();
      }
    }
    return orderSetPresent;
  }

  /**
   * Format time.
   *
   * @param param
   *          the param
   * @return the java.sql. time
   */
  private java.sql.Time formatTime(String param) {

    SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");
    if (param != null & !param.equals("")) {
      try {
        return new java.sql.Time(timeFormatter.parse(param).getTime());
      } catch (ParseException exe) {
        // TODO Auto-generated catch block
        exe.printStackTrace();
      }
    }
    return null;
  }

  /**
   * Get the availability details.
   *
   * @param availableInfo          the available info
   * @param overlapMap          the overlap map
   */
  public void getAvailabilityDetails(Map availableInfo, Map overlapMap) {
    Time slotFromTime = null;
    Time slotToTime = null;
    Integer centerId = null;
    String availabilityStatus = null;
    String remarks = null;
    String visitMode = null;
    List<Map> availabilityList = 
        (List<Map>) availableInfo.get("availability_details");
    Map availabilityDetails = null;

    if (availabilityList != null && availabilityList.size() > 0) {
      for (int i = 0; i < availabilityList.size(); i++) {
        availabilityDetails = (Map) availabilityList.get(i);
        slotFromTime = (Time) availabilityDetails.get("start_time");
        slotToTime = (Time) availabilityDetails.get("end_time");
        centerId = (Integer) availabilityDetails.get("center_id");
        availabilityStatus = (String) availabilityDetails.get("availability_status");
        remarks = (String) availabilityDetails.get("remarks");
        visitMode = (String) availabilityDetails.get("visit_mode");
        if (remarks.length() > 100) {
          ValidationErrorMap errorMap = new ValidationErrorMap();
          errorMap.addError("remarks", "exception.scheduler.override.remarks.over100");
          List<Map> validatetempl = new ArrayList<Map>();
          ValidationException ex = new ValidationException(errorMap);
          validatetempl.add(ex.getErrors());
          Map<String, Object> availabilityMap = new HashMap<String, Object>();
          availabilityMap.put("availability_details", validatetempl);
          Map<String, Object> nestedException = new HashMap<String, Object>();
          nestedException.put("timings", availabilityMap);
          throw new NestableValidationException(nestedException);
        }
        
        availableInfo.put("from_time", slotFromTime);
        availableInfo.put("to_time", slotToTime);
        availableInfo.put("center_id", centerId);
        availableInfo.put("availability_status", availabilityStatus);
        availableInfo.put("remarks", remarks);
        availableInfo.put("visit_mode", visitMode);

        if (availabilityStatus.equals("A")) {
          getAvailableScheduledSlots(availableInfo, overlapMap);
        } else {
          getNonAvailableScheduledSlots(availableInfo, overlapMap);
        }
      }
    }
  }

  /**
   * Get the available scheduled slots.
   *
   * @param availInfoMap          the avail info map
   * @param overlapMap          the overlap map
   */
  public void getAvailableScheduledSlots(Map availInfoMap, Map overlapMap) {
    BasicDynaBean nonAvailBean = null;
    int loggedIncenter = RequestContext.getCenterId();
    String errorMsg = null;
    Integer maxCentersDefault = (Integer) prefService.getAllPreferences().get(
        "max_centers_inc_default");
    int weekDayNo = (Integer) availInfoMap.get("weekDayNo");
    String resourceName = (String) availInfoMap.get("resName");
    String resourceType = (String) availInfoMap.get("resType");
    java.sql.Date fromDate = (java.sql.Date) availInfoMap.get("start_date");
    java.sql.Date toDate = (java.sql.Date) availInfoMap.get("end_date");
    java.sql.Time slotFromTime = (Time) availInfoMap.get("from_time");
    java.sql.Time slotToTime = (Time) availInfoMap.get("to_time");
    String availRemarks = (String) availInfoMap.get("remarks");
    int availResourceCenter = (Integer) availInfoMap.get("center_id");
    String availVisitMode = (String) availInfoMap.get("visit_mode");
    BasicDynaBean overrideBean = null;
    List resourceNonAvailabilityList = new ArrayList();
    Map resourcesInfo = new HashMap();
    int loggedCenterId = RequestContext.getCenterId();
    if (loggedCenterId != availResourceCenter && availResourceCenter != 0) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("center_id", "exception.scheduler.override.invalid.center_id");
      List<Map> validatetempl = new ArrayList<Map>();
      ValidationException ex = new ValidationException(errorMap);
      validatetempl.add(ex.getErrors());
      Map<String, Object> availabilityMap = new HashMap<String, Object>();
      availabilityMap.put("availability_details", validatetempl);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("timings", availabilityMap);
      throw new NestableValidationException(nestedException);
    }
    List availabilityList = new ArrayList();
    Integer resAvailId = 0;
    List<Map> overrideList = new ArrayList<Map>();

    if (false) {
      /* Update the time slots Centerwise */
      markResourceAvailableCenterwise(availInfoMap);
    } else {
      /* Update time slots for non center schema */
      resourcesInfo.put("resources_list", resourceNonAvailabilityList);
      resourcesInfo.put("override_bean", overrideBean);
      getResourceAvailabilityList(resourcesInfo, resourceType, resourceName, fromDate, toDate,
          weekDayNo);
      resourceNonAvailabilityList = (List) resourcesInfo.get("resources_list");
      overrideBean = (BasicDynaBean) resourcesInfo.get("override_bean");

      Boolean found = false;
      Boolean doctorAvailableInOtherCenter = false;
      List changedIndexList = new ArrayList();
      int counter = 0;
      if (resourceNonAvailabilityList != null && resourceNonAvailabilityList.size() > 0) {
        for (int i = 0; i < resourceNonAvailabilityList.size(); i++) {
          nonAvailBean = (BasicDynaBean) resourceNonAvailabilityList.get(i);
          if (nonAvailBean != null) {
            Time fromTime = (Time) nonAvailBean.get("from_time");
            Time toTime = (Time) nonAvailBean.get("to_time");
            String availabilityStatus = (String) nonAvailBean.get("availability_status");
            String remarks = (String) nonAvailBean.get("remarks");
            Integer dbAvailCenterId = (Integer) nonAvailBean.get("center_id");
            String visitMode = (String) nonAvailBean.get("visit_mode");

            if (fromTime != null && toTime != null) {
              if (availabilityStatus.equals("N")
                  && (slotFromTime.equals(fromTime) || slotFromTime.after(fromTime))
                  && (slotToTime.equals(toTime) || slotToTime.before(toTime))) {
                /*
                 * if (maxCentersDefault > 1) { if ((loggedCenterId != 0 && dbAvailCenterId != null)
                 * && (availabilityStatus.equals("N") && dbAvailCenterId != 0)) { if
                 * (!dbAvailCenterId.equals(loggedCenterId)) {
                 * 
                 * } } }
                 */
                found = true;
                /* if time slots are surrounding with existing time slots */
                if (!slotFromTime.equals(fromTime)) {
                  addOverrideDetailsIntoList(overrideList, fromTime, slotFromTime, "N", remarks,
                      null,null);
                  counter++;
                }
                addOverrideDetailsIntoList(overrideList, slotFromTime, slotToTime, "A",
                    availRemarks, availResourceCenter,availVisitMode);
                changedIndexList.add(counter);
                counter++;

                if (!slotToTime.equals(toTime)) {
                  addOverrideDetailsIntoList(overrideList, slotToTime, toTime, "N", remarks,
                      null,null);
                  counter++;
                }

              } else {
                /* updating the existing time slots */
                if (availabilityStatus.equals("A") && (slotFromTime.after(fromTime))
                    && (slotToTime.after(toTime)) && slotFromTime.before(toTime)) {
                  if (dbAvailCenterId != availResourceCenter && dbAvailCenterId != 0
                      && maxCentersDefault > 1) {
                    doctorAvailableInOtherCenter = true;
                  }
                  overlapMap.put("culprit", "start_time");
                } else if (availabilityStatus.equals("A") && (slotFromTime.before(fromTime))
                    && (slotToTime.before(toTime)) && fromTime.before(slotToTime)) {
                  if (dbAvailCenterId != availResourceCenter && dbAvailCenterId != 0
                      && maxCentersDefault > 1) {
                    doctorAvailableInOtherCenter = true;
                  }
                  overlapMap.put("culprit2", "end_time");
                } else if (availabilityStatus.equals("A")
                    && (((slotFromTime.equals(fromTime) 
                        || slotFromTime.after(fromTime)) && (slotToTime
                        .equals(toTime) || slotToTime.before(toTime))) 
                        || ((slotFromTime
                        .equals(fromTime) || slotFromTime.before(fromTime)) 
                        && (slotToTime
                        .equals(toTime) || slotToTime.after(toTime))))) {
                  if (dbAvailCenterId != availResourceCenter && dbAvailCenterId != 0
                      && maxCentersDefault > 1) {
                    doctorAvailableInOtherCenter = true;
                  }
                  overlapMap.put("culprit", "start_time");
                  // overlapMap.put("culprit2", "end_time");
                }
                addOverrideDetailsIntoList(overrideList, fromTime, toTime, availabilityStatus,
                    remarks, dbAvailCenterId,visitMode);
                counter++;
              }
            }
          }
        }
      }
      if (doctorAvailableInOtherCenter) {
        ValidationErrorMap errorMap = new ValidationErrorMap();
        errorMap.addError("center_id", "exception.scheduler.doctor.available.otherslot");
        List<Map> validatetempl = new ArrayList<Map>();
        ValidationException ex = new ValidationException(errorMap);
        validatetempl.add(ex.getErrors());
        Map<String, Object> availabilityMap = new HashMap<String, Object>();
        availabilityMap.put("availability_details", validatetempl);
        Map<String, Object> nestedException = new HashMap<String, Object>();
        nestedException.put("timings", availabilityMap);
        throw new NestableValidationException(nestedException);
      }
      mergingSimilarSlots(overrideList,availRemarks,changedIndexList,availVisitMode);
      if (!found) {
        if (overlapMap.isEmpty()) {

          ValidationErrorMap errorMap = new ValidationErrorMap();
          errorMap.addError("start_time",
              "covers 2 entries in database need to override one by one");
          List<Map> validatetempl = new ArrayList<Map>();
          ValidationException ex = new ValidationException(errorMap);
          validatetempl.add(ex.getErrors());
          Map<String, Object> availabilityMap = new HashMap<String, Object>();
          availabilityMap.put("availability_details", validatetempl);
          Map<String, Object> nestedException = new HashMap<String, Object>();
          nestedException.put("timings", availabilityMap);
          throw new NestableValidationException(nestedException);
        }
        overlapMap.put("overlap", true);
      }
      availInfoMap.put("availability_details", overrideList);
    }
    if (overrideBean != null) {
      resAvailId = (Integer) overrideBean.get("res_avail_id");
      resourceDetailsRepository.delete("res_avail_id", resAvailId);
      resourceOverrideRepository.delete("res_avail_id", resAvailId);
    }
  }

  /**
   * Get the non available scheduled slots.
   *
   * @param nonAailInfoMap          the non Aail info map
   * @param overlapMap          the overlap map
   */
  public void getNonAvailableScheduledSlots(Map nonAailInfoMap, Map overlapMap) {
    BasicDynaBean nonAvailBean = null;
    String errorMsg = null;
    String resourceName = (String) nonAailInfoMap.get("resName");
    String resourceType = (String) nonAailInfoMap.get("resType");
    int weekDayNo = (Integer) nonAailInfoMap.get("weekDayNo");
    Integer maxCentersDefault = (Integer) prefService.getAllPreferences().get(
        "max_centers_inc_default");
    java.sql.Date fromDate = (java.sql.Date) nonAailInfoMap.get("start_date");
    java.sql.Date toDate = (java.sql.Date) nonAailInfoMap.get("end_date");
    java.sql.Time slotFromTime = (Time) nonAailInfoMap.get("from_time");
    java.sql.Time slotToTime = (Time) nonAailInfoMap.get("to_time");
    String noAvlRemarks = (String) nonAailInfoMap.get("remarks");
    String noAvlVisitMode = (String) nonAailInfoMap.get("visit_mode");
    int availResourceCenter = (Integer) nonAailInfoMap.get("center_id");
    BasicDynaBean overrideBean = null;
    Map resourcesInfo = new HashMap();

    int loggedCenterId = RequestContext.getCenterId();
    if (loggedCenterId != availResourceCenter && availResourceCenter != 0) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("center_id", "exception.scheduler.override.invalid.center_id");
      List<Map> validatetempl = new ArrayList<Map>();
      ValidationException ex = new ValidationException(errorMap);
      validatetempl.add(ex.getErrors());
      Map<String, Object> availabilityMap = new HashMap<String, Object>();
      availabilityMap.put("availability_details", validatetempl);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("timings", availabilityMap);
      throw new NestableValidationException(nestedException);
    }
    Integer resAvailId = 0;
    List<Map> overrideList = new ArrayList<Map>();
    List resourceAvailabilityList = new ArrayList();
    if (false) {
      /* Update the time slots Centerwise */
      markResourceNonAvailableCenterwise(nonAailInfoMap);
    } else {
      /* non center schema update */
      resourcesInfo.put("resources_list", resourceAvailabilityList);
      resourcesInfo.put("override_bean", overrideBean);

      getResourceAvailabilityList(resourcesInfo, resourceType, resourceName, fromDate, toDate,
          weekDayNo);

      resourceAvailabilityList = (List) resourcesInfo.get("resources_list");
      overrideBean = (BasicDynaBean) resourcesInfo.get("override_bean");

      Boolean found = false;
      List changedIndexList = new ArrayList();
      int counter = 0;

      if (resourceAvailabilityList != null && resourceAvailabilityList.size() > 0) {
        for (int i = 0; i < resourceAvailabilityList.size(); i++) {
          nonAvailBean = (BasicDynaBean) resourceAvailabilityList.get(i);
          Time fromTime = (Time) nonAvailBean.get("from_time");
          Time toTime = (Time) nonAvailBean.get("to_time");
          String availabilityStatus = (String) nonAvailBean.get("availability_status");
          String remarks = (String) nonAvailBean.get("remarks");
          Integer dbNonAvailCenterId = (Integer) nonAvailBean.get("center_id");
          String visitMode = (String) nonAvailBean.get("visit_mode");

          if (fromTime != null && toTime != null) {
            if (availabilityStatus.equals("A")
                && (slotFromTime.equals(fromTime) || slotFromTime.after(fromTime))
                && (slotToTime.equals(toTime) || slotToTime.before(toTime))) {
              /* if time slots are surrounding with existing time slots */
              found = true;
              if (maxCentersDefault > 1 && dbNonAvailCenterId != 0  
                  && availResourceCenter != dbNonAvailCenterId) {
                ValidationErrorMap errorMap = new ValidationErrorMap();
                errorMap.addError("start_time", "exception.scheduler.override.overlapping.slots");
                List<Map> validatetempl = new ArrayList<Map>();
                ValidationException ex = new ValidationException(errorMap);
                validatetempl.add(ex.getErrors());
                Map<String, Object> availabilityMap = new HashMap<String, Object>();
                availabilityMap.put("availability_details", validatetempl);
                Map<String, Object> nestedException = new HashMap<String, Object>();
                nestedException.put("timings", availabilityMap);
                throw new NestableValidationException(nestedException);
              }
              if (!slotFromTime.equals(fromTime)) {
                addOverrideDetailsIntoList(overrideList, fromTime, 
                    slotFromTime, "A", remarks,
                    dbNonAvailCenterId, visitMode);
                counter++;
              }
              addOverrideDetailsIntoList(overrideList, slotFromTime, 
                  slotToTime, "N", noAvlRemarks,
                  null, null);
              changedIndexList.add(counter);
              counter++;

              if (!slotToTime.equals(toTime)) {
                addOverrideDetailsIntoList(overrideList, slotToTime, 
                    toTime, "A", remarks,
                    dbNonAvailCenterId, visitMode);
                counter++;
              }

            } else {
              if (availabilityStatus.equals("A") 
                  && (slotFromTime.after(fromTime) || slotFromTime.equals(fromTime))
                  && slotFromTime.before(toTime)) {
                
                int tcounter = 0;
                while (slotToTime.after(((Time) ((BasicDynaBean) resourceAvailabilityList
                    .get(i + tcounter)).get("to_time"))) || slotToTime.equals(((Time) 
                        ((BasicDynaBean) resourceAvailabilityList
                        .get(i + tcounter)).get("to_time")))) {
                  
                  nonAvailBean = (BasicDynaBean) resourceAvailabilityList.get(i + tcounter);
                  Time fromTimeM = (Time) nonAvailBean.get("from_time");
                  Time toTimeM = (Time) nonAvailBean.get("to_time");
                  String availabilityStatusM = (String) nonAvailBean.get("availability_status");
                  String remarksM = (String) nonAvailBean.get("remarks");
                  Integer dbNonAvailCenterIdM = (Integer) nonAvailBean.get("center_id");
                  String visitModeM = (String) nonAvailBean.get("visit_mode");
                  if ((availabilityStatusM.equals("N")) 
                      || (maxCentersDefault > 1 && dbNonAvailCenterId != 0  
                      && availResourceCenter != dbNonAvailCenterIdM 
                      && availabilityStatusM.equals("A"))) {
                    ValidationErrorMap errorMap = new ValidationErrorMap();
                    errorMap.addError("start_time",
                        "exception.scheduler.override.overlapping.slots");
                    List<Map> validatetempl = new ArrayList<Map>();
                    ValidationException ex = new ValidationException(errorMap);
                    validatetempl.add(ex.getErrors());
                    Map<String, Object> availabilityMap = new HashMap<String, Object>();
                    availabilityMap.put("availability_details", validatetempl);
                    Map<String, Object> nestedException = new HashMap<String, Object>();
                    nestedException.put("timings", availabilityMap);
                    throw new NestableValidationException(nestedException);
                  }
                  if (tcounter == 0) {
                    if (!slotFromTime.equals(fromTimeM)) {
                      addOverrideDetailsIntoList(overrideList, fromTimeM, 
                          slotFromTime, "A", remarks,
                          dbNonAvailCenterId, visitMode);
                      counter++;
                    }
                    addOverrideDetailsIntoList(overrideList, slotFromTime, 
                        toTimeM, "N", noAvlRemarks,
                        null, null);
                    changedIndexList.add(counter);
                    counter++;
                  } else {
                    addOverrideDetailsIntoList(overrideList, fromTimeM, 
                        toTimeM, "N", noAvlRemarks,
                        null, null);
                  }
                  changedIndexList.add(counter);
                  tcounter++;
                }
                nonAvailBean = (BasicDynaBean) resourceAvailabilityList.get(i + tcounter);
                Time fromTimeM = (Time) nonAvailBean.get("from_time");
                Time toTimeM = (Time) nonAvailBean.get("to_time");
                String availabilityStatusM = (String) nonAvailBean.get("availability_status");
                String remarksM = (String) nonAvailBean.get("remarks");
                Integer dbNonAvailCenterIdM = (Integer) nonAvailBean.get("center_id");
                String visitModeM = (String) nonAvailBean.get("visit_mode");
                
                if (toTimeM.after(slotToTime)) {
                  
                  if ((availabilityStatusM.equals("N")) 
                      || (maxCentersDefault > 1 && dbNonAvailCenterId != 0  
                      && availResourceCenter != dbNonAvailCenterIdM 
                      && availabilityStatusM.equals("A"))) {
                    ValidationErrorMap errorMap = new ValidationErrorMap();
                    errorMap.addError("start_time",
                        "exception.scheduler.override.overlapping.slots");
                    List<Map> validatetempl = new ArrayList<Map>();
                    ValidationException ex = new ValidationException(errorMap);
                    validatetempl.add(ex.getErrors());
                    Map<String, Object> availabilityMap = new HashMap<String, Object>();
                    availabilityMap.put("availability_details", validatetempl);
                    Map<String, Object> nestedException = new HashMap<String, Object>();
                    nestedException.put("timings", availabilityMap);
                    throw new NestableValidationException(nestedException);
                  }
                  addOverrideDetailsIntoList(overrideList, fromTimeM, slotToTime, "N", noAvlRemarks,
                      null,null);
                  changedIndexList.add(counter);
                  counter++;
                  addOverrideDetailsIntoList(overrideList, slotToTime, toTimeM, "A", remarks,
                      dbNonAvailCenterIdM, visitModeM);
                  counter++;
                } else {
                  tcounter--;
                }
                i = i + tcounter;
                found = true;
                continue;
              }
              /* updating the existing time slots */
              if (availabilityStatus.equals("N") && (slotFromTime.after(fromTime))
                  && (slotToTime.after(toTime)) && slotFromTime.before(toTime)) {
                overlapMap.put("culprit", "start_time");
              } else if (availabilityStatus.equals("N") && (slotFromTime.before(fromTime))
                  && (slotToTime.before(toTime)) && fromTime.before(slotToTime)) {
                overlapMap.put("culprit2", "end_time");
              } else if (availabilityStatus.equals("N")
                  && (((slotFromTime.equals(fromTime) 
                      || slotFromTime.after(fromTime)) && (slotToTime
                      .equals(toTime) || slotToTime.before(toTime))) || ((slotFromTime
                      .equals(fromTime) || slotFromTime.before(fromTime)) && (slotToTime
                      .equals(toTime) || slotToTime.after(toTime))))) {
                overlapMap.put("culprit", "start_time");
                // overlapMap.put("culprit2", "end_time");
              }
              addOverrideDetailsIntoList(overrideList, fromTime, toTime, availabilityStatus,
                  remarks, dbNonAvailCenterId, visitMode);
              counter++;
            }
          }
        }
      }
      // do not merge remarks HMS-27548
      //mergingSimilarSlots(overrideList,noAvlRemarks,changedIndexList);
      mergingSimilarSlots(overrideList,noAvlRemarks,changedIndexList,noAvlVisitMode);
      if (!found) {
        if (overlapMap.isEmpty()) {

          ValidationErrorMap errorMap = new ValidationErrorMap();
          errorMap.addError("start_time",
              "covers 2 entries in database need to override one by one");
          List<Map> validatetempl = new ArrayList<Map>();
          ValidationException ex = new ValidationException(errorMap);
          validatetempl.add(ex.getErrors());
          Map<String, Object> availabilityMap = new HashMap<String, Object>();
          availabilityMap.put("availability_details", validatetempl);
          Map<String, Object> nestedException = new HashMap<String, Object>();
          nestedException.put("timings", availabilityMap);
          throw new NestableValidationException(nestedException);
        }
        overlapMap.put("overlap", true);
      }
      nonAailInfoMap.put("availability_details", overrideList);
    }
    if (overrideBean != null) {
      resAvailId = (Integer) overrideBean.get("res_avail_id");
      resourceDetailsRepository.delete("res_avail_id", resAvailId);
      resourceOverrideRepository.delete("res_avail_id", resAvailId);
    }
  }

  /**
   * Mark the resource available centerwise.
   *
   * @param availInfoMap
   *          the avail info map
   */
  public void markResourceAvailableCenterwise(Map availInfoMap) {

    BasicDynaBean nonAvailBean = null;
    String errorMsg = null;
    Integer maxCentersDefault = (Integer) availInfoMap.get("maxCentersDefault");
    int weekDayNo = (Integer) availInfoMap.get("weekDayNo");
    java.sql.Date fromDate = (java.sql.Date) availInfoMap.get("start_date");
    java.sql.Date toDate = (java.sql.Date) availInfoMap.get("end_date");
    String resourceName = (String) availInfoMap.get("resName");
    String resourceType = (String) availInfoMap.get("resType");
    java.sql.Time slotFromTime = (Time) availInfoMap.get("from_time");
    java.sql.Time slotToTime = (Time) availInfoMap.get("to_time");
    String availRremarks = (String) availInfoMap.get("remarks");
    Integer centerId = (Integer) availInfoMap.get("center_id");
    String availVisitMode = (String) availInfoMap.get("visit_mode");
    BasicDynaBean overrideBean = null;

    Integer resAvailId = 0;
    List overrideList = new ArrayList();
    List resourceNonAvailabilityList = new ArrayList();

    Map resourcesInfo = new HashMap();
    resourcesInfo.put("resources_list", resourceNonAvailabilityList);
    resourcesInfo.put("override_bean", overrideBean);

    getResourceAvailabilityList(resourcesInfo, resourceType, resourceName, fromDate, toDate,
        weekDayNo);

    resourceNonAvailabilityList = (List) resourcesInfo.get("resources_list");
    overrideBean = (BasicDynaBean) resourcesInfo.get("override_bean");
    AppointmentCategory appointmentCategory = appointmentCategoryFactory.getInstance(resourceType);
    int loggedCenterId = RequestContext.getCenterId();
    /*
     * This logic will update the availabilityStatus as N when other centers are available in these
     * time slots eg: we try to insert the resource override in BRS from timings are 2 to 4 . at
     * same time CHALL also having same resource from 2 to 4 so this time it should update CHALLA's
     * status as N
     */
    resourceNonAvailabilityList = appointmentCategory.filterVisitTimingsByCenter(
        resourceNonAvailabilityList, loggedCenterId);

    if (overrideBean != null) {
      resAvailId = (Integer) overrideBean.get("res_avail_id");
      resourceDetailsRepository.delete("res_avail_id", resAvailId);
    }

    if (resourceNonAvailabilityList != null && resourceNonAvailabilityList.size() > 0) {
      for (int i = 0; i < resourceNonAvailabilityList.size(); i++) {
        nonAvailBean = (BasicDynaBean) resourceNonAvailabilityList.get(i);
        Time fromTime = (Time) nonAvailBean.get("from_time");
        Time toTime = (Time) nonAvailBean.get("to_time");
        String availabilityStatus = (String) nonAvailBean.get("availability_status");
        String remarks = (String) nonAvailBean.get("remarks");
        Integer dbAvailCenterId = (Integer) nonAvailBean.get("center_id");
        String visitMode = (String) nonAvailBean.get("visit_mode");

        if (fromTime != null && toTime != null) {
          if ((slotFromTime.equals(fromTime) || slotFromTime.after(fromTime))
              && (slotToTime.equals(toTime) || slotToTime.before(toTime))) {

            if ((loggedCenterId != 0 && dbAvailCenterId != null)
                && (availabilityStatus.equals("N") && dbAvailCenterId != 0)) {
              if (!dbAvailCenterId.equals(loggedCenterId)) {
                // show error flash message on submit
                /*
                 * If resource is available in other center with this time slots then it wil throw
                 * error
                 */
                ValidationErrorMap errorMap = new ValidationErrorMap();
                errorMap.addError("center_id", "exception.scheduler.doctor.available.otherslot");
                ValidationException ex = new ValidationException(errorMap);
                Map<String, Object> nestedException = new HashMap<String, Object>();
                nestedException.put("patient", ex.getErrors());
                throw new NestableValidationException(nestedException);
              }
            }
            /* Updating the time slots surrounding time slots (new time slots are updating here) */
            if (!slotFromTime.equals(fromTime)) {
              if (dbAvailCenterId != null 
                  && availabilityStatus.equals("N") 
                  && dbAvailCenterId != 0) {
                addOverrideDetailsIntoList(overrideList, fromTime, slotFromTime, "A",
                    availRremarks, dbAvailCenterId,availVisitMode);
              } else {
                addOverrideDetailsIntoList(overrideList, fromTime, slotFromTime, "N", 
                    remarks, null,null);
              }
            }

            addOverrideDetailsIntoList(
                overrideList, 
                slotFromTime, slotToTime, "A", availRremarks,
                centerId,availVisitMode);

            if (!slotToTime.equals(toTime)) {
              if (dbAvailCenterId != null 
                  && availabilityStatus.equals("N") 
                  && dbAvailCenterId != 0) {
                addOverrideDetailsIntoList(overrideList, slotToTime, toTime, "A", 
                    availRremarks,
                    dbAvailCenterId, availVisitMode);
              } else {
                addOverrideDetailsIntoList(overrideList, slotToTime, toTime, "N", 
                    remarks, null,null);
              }
            }

          } else {
            /* Updating the existing time slots */
            addOverrideDetailsIntoList(overrideList, fromTime, toTime, availabilityStatus, 
                remarks,
                dbAvailCenterId,visitMode);
          }
        }
      }
    }

    availInfoMap.put("availability_details", overrideList);
  }

  /**
   * mark the resource non available centerwise.
   *
   * @param nonAailInfoMap
   *          the non Aail info map
   */
  public void markResourceNonAvailableCenterwise(Map nonAailInfoMap) {

    BasicDynaBean nonAvailBean = null;

    java.sql.Time slotFromTime = (Time) nonAailInfoMap.get("from_time");
    java.sql.Time slotToTime = (Time) nonAailInfoMap.get("to_time");
    String errorMsg = null;
    Integer maxCentersDefault = (Integer) nonAailInfoMap.get("maxCentersDefault");
    int weekDayNo = (Integer) nonAailInfoMap.get("weekDayNo");
    String resourceName = (String) nonAailInfoMap.get("resName");
    String resourceType = (String) nonAailInfoMap.get("resType");
    java.sql.Date fromDate = (java.sql.Date) nonAailInfoMap.get("start_date");
    java.sql.Date toDate = (java.sql.Date) nonAailInfoMap.get("end_date");
    String noAvlRemarks = (String) nonAailInfoMap.get("remarks");
    Integer centerId = (Integer) nonAailInfoMap.get("center_id");
    String noAvlVisitMode = (String) nonAailInfoMap.get("visit_mode");
    Integer resAvailId = 0;
    List overrideList = new ArrayList();
    List<BasicDynaBean> resourceAvailabilityList = new ArrayList();
    BasicDynaBean overrideBean = null;

    Map resourcesInfo = new HashMap();
    resourcesInfo.put("resources_list", resourceAvailabilityList);
    resourcesInfo.put("override_bean", overrideBean);

    getResourceAvailabilityList(resourcesInfo, resourceType, resourceName, fromDate, toDate,
        weekDayNo);

    resourceAvailabilityList = (List) resourcesInfo.get("resources_list");
    overrideBean = (BasicDynaBean) resourcesInfo.get("override_bean");
    int loggedCenterId = RequestContext.getCenterId();
    AppointmentCategory appointmentCategory = appointmentCategoryFactory.getInstance(resourceType);
    resourceAvailabilityList = appointmentCategory.filterVisitTimingsByCenter(
        resourceAvailabilityList, loggedCenterId);

    if (overrideBean != null) {
      resAvailId = (Integer) overrideBean.get("res_avail_id");
      resourceOverrideRepository.delete("res_avail_id", resAvailId);
    }

    if (resourceAvailabilityList != null && resourceAvailabilityList.size() > 0) {
      for (int i = 0; i < resourceAvailabilityList.size(); i++) {
        nonAvailBean = (BasicDynaBean) resourceAvailabilityList.get(i);
        Time fromTime = (Time) nonAvailBean.get("from_time");
        Time toTime = (Time) nonAvailBean.get("to_time");
        String availabilityStatus = (String) nonAvailBean.get("availability_status");
        String remarks = (String) nonAvailBean.get("remarks");
        Integer resAvailCenterId = (Integer) nonAvailBean.get("center_id");
        String visitMode = (String) nonAailInfoMap.get("visit_mode");

        if (fromTime != null && toTime != null) {
          if ((slotFromTime.equals(fromTime) || slotFromTime.after(fromTime))
              && (slotToTime.equals(toTime) || slotToTime.before(toTime))) {

            if (loggedCenterId != 0) {
              /* If resource is available in all centers then it should throw error */
              if (resAvailCenterId != null && resAvailCenterId.equals(0)) {
                // show error flash message on submit
                ValidationErrorMap errorMap = new ValidationErrorMap();
                errorMap.addError("center-id", 
                    "exception.scheduler.doctor.mapped.othercenter");
                ValidationException ex = new ValidationException(errorMap);
                Map<String, Object> nestedException = new HashMap<String, Object>();
                nestedException.put("patient", ex.getErrors());
                throw new NestableValidationException(nestedException);
              }
            }
            /* Updating the time slots surrounding time slots (new time slots are updating here) */
            if (!slotFromTime.equals(fromTime)) {
              addOverrideDetailsIntoList(overrideList, fromTime, slotFromTime, "A", 
                  remarks,
                  resAvailCenterId,visitMode);
            }

            addOverrideDetailsIntoList(overrideList, slotFromTime, slotToTime, "N", 
                null, null, null);

            if (!slotToTime.equals(toTime)) {
              addOverrideDetailsIntoList(overrideList, slotToTime, toTime, "A", 
                  remarks,
                  resAvailCenterId,visitMode);
            }

          } else {
            /* Updating the existing time slots */
            addOverrideDetailsIntoList(overrideList, fromTime, toTime, availabilityStatus, 
                remarks,
                resAvailCenterId, visitMode);
          }
        }
      }
    }
    nonAailInfoMap.put("availability_details", overrideList);
  }

  /*
   * will convert the row of availability_detail into Map. so eventually will get number of
   * availability_details into List. (no. of map)
   */
  /**
   * Add the override details into list.
   * 
   * @param overrideList
   *          the override list
   * @param fromTime
   *          the from time
   * @param toTime
   *          the to time
   * @param availStatus
   *          the avail status
   * @param remarks
   *          the remarks
   * @param centerId
   *          the center id
   * @param visitMode
   *          the slot type
   */
  public void addOverrideDetailsIntoList(List overrideList, Time fromTime, Time toTime,
      String availStatus, String remarks, Integer centerId, String visitMode) {
    Map overrideDetails = new HashMap();
    overrideDetails.put("start_time", fromTime);
    overrideDetails.put("end_time", toTime);
    overrideDetails.put("availability_status", availStatus);
    overrideDetails.put("remarks", remarks);
    overrideDetails.put("center_id", centerId);
    overrideDetails.put("default_value", "false");
    overrideDetails.put("r_delete", "false");
    if (visitMode != null && !visitMode.isEmpty()) {
      overrideDetails.put("visit_mode", visitMode);
    } else {
      overrideDetails.put("visit_mode", DEFAULT_VISIT_MODE);
    }
    overrideList.add(overrideDetails);
  }

  /**
   * Get the resource availability list.
   *
   * @param resourcesInfo          the reources info
   * @param resourceType          the resource type
   * @param resourceName          the resource name
   * @param fromDate          the from date
   * @param toDate          the to date
   * @param weekDayNo          the week day no
   */
  public void getResourceAvailabilityList(Map resourcesInfo, 
      String resourceType,
      String resourceName, java.sql.Date fromDate, 
      java.sql.Date toDate, int weekDayNo) {

    Integer resAvailId = 0;
    Integer maxCentersDefault = 1;

    List resourcesList = (List) resourcesInfo.get("resources_list");
    BasicDynaBean overrideBean = (BasicDynaBean) resourcesInfo.get("override_bean");

    resourcesList = resourceRepo.getResourceOverrides(resourceType, 
        fromDate, toDate, resourceName,
        null, null, maxCentersDefault);
    if (resourcesList != null && resourcesList.size() > 0) {
      overrideBean = (BasicDynaBean) resourcesList.get(0);
    }

    if (resourcesList != null && resourcesList.size() < 1) {
      resourcesList = resourceRepo.getDefaultResourceAvailabilities(resourceName, 
          weekDayNo,
          resourceType, null, null, maxCentersDefault);
    }
    if (resourcesList != null && resourcesList.size() < 1) {
      resourcesList = resourceRepo.getDefaultResourceAvailabilities("*", weekDayNo, 
          resourceType,
          null, null, maxCentersDefault);
    }

    resourcesInfo.put("resources_list", resourcesList);
    resourcesInfo.put("override_bean", overrideBean);
  }

  /**
   * Throw the validation exception.
   * 
   * @param errorMap
   *          the error map
   */
  public void throwValidationException(ValidationErrorMap errorMap) {
    ValidationException ex = new ValidationException(errorMap);
    Map<String, Object> nestedException = new HashMap<String, Object>();
    nestedException.put("availability_details", ex.getErrors());
    throw new NestableValidationException(nestedException);
  }

  /**
   * Override for one center for entire day.
   *
   * @param overrideList          the override list
   * @param resourceType          the resource type
   * @param resourceName          the resource name
   * @param fromDate          the from date
   * @param toDate          the to date
   * @param weekDayNo          the week day no
   * @param newStatus          the new status
   * @param newRemarks the new remarks
   */
  public void overrideForOneCenterForEntireDay(List<Map> overrideList, String resourceType,
      String resourceName, java.sql.Date fromDate, java.sql.Date toDate, int weekDayNo,
      String newStatus, String newRemarks, String newVisitMode) {

    Map resourcesInfo = new HashMap();
    BasicDynaBean overrideBean = null;
    List resourceAvailabilityList = new ArrayList();

    resourcesInfo.put("resources_list", resourceAvailabilityList);
    resourcesInfo.put("override_bean", overrideBean);
    getResourceAvailabilityList(resourcesInfo, resourceType, resourceName, fromDate, toDate,
        weekDayNo);
    resourceAvailabilityList = (List) resourcesInfo.get("resources_list");
    overrideBean = (BasicDynaBean) resourcesInfo.get("override_bean");

    List changedIndexList = new ArrayList();
    int counter = 0;
    if (resourceAvailabilityList != null && resourceAvailabilityList.size() > 0) {
      for (int i = 0; i < resourceAvailabilityList.size(); i++) {

        BasicDynaBean availBean = (BasicDynaBean) resourceAvailabilityList.get(i);
        String availabilityStatus = (String) availBean.get("availability_status");
        Integer dbAvailCenterId = (Integer) availBean.get("center_id");
        Time fromTime = (Time) availBean.get("from_time");
        Time toTime = (Time) availBean.get("to_time");
        String remarks = (String) availBean.get("remarks");
        String visitMode = (String) availBean.get("visit_mode");
        Integer loggedCenterId = RequestContext.getCenterId();
        if (newStatus.equals("N")) {
          if (availabilityStatus.equals("A") 
              && (loggedCenterId.equals(dbAvailCenterId) || dbAvailCenterId == 0) ) {
            addOverrideDetailsIntoList(overrideList, fromTime, 
                toTime, "N", newRemarks, null,null);
            changedIndexList.add(counter);
            counter++;
          } else {
            addOverrideDetailsIntoList(overrideList, fromTime, 
                toTime, availabilityStatus, remarks,
                dbAvailCenterId,visitMode);
            counter++;
          }
        } else {
          if (availabilityStatus.equals("N")) {
            addOverrideDetailsIntoList(overrideList, fromTime, 
                toTime, "A", newRemarks, loggedCenterId,newVisitMode);
            changedIndexList.add(counter);
            counter++;
          } else {
            addOverrideDetailsIntoList(overrideList, fromTime, 
                toTime, availabilityStatus, remarks,
                dbAvailCenterId,visitMode);
            counter++;
          }
        }
      }
      mergingSimilarSlots(overrideList,newRemarks,changedIndexList,newVisitMode);
      if (overrideBean != null) {
        Integer resAvailId = (Integer) overrideBean.get("res_avail_id");
        resourceDetailsRepository.delete("res_avail_id", resAvailId);
        resourceOverrideRepository.delete("res_avail_id", resAvailId);
      }

    }
  }
  
  /**
   * Merging similar slots.
   *
   * @param overrideList the override list
   * @param remarks the remarks
   * @param changedIndexList the changed index list
   * @param visitMode the visit mode
   */
  public void mergingSimilarSlots(List<Map> overrideList,String remarks,
            List changedIndexList, String visitMode) {
    
    Boolean flag = false;
    for (int i = 1; i < overrideList.size(); i++) {
      if ((overrideList.get(i).get("availability_status").equals("N") 
              && (overrideList.get(i - 1).get("availability_status").equals("N"))) 
              || (overrideList.get(i).get("availability_status")
              .equals(overrideList.get(i - 1).get("availability_status")) 
                  && overrideList.get(i).get("center_id")
                  .equals(overrideList.get(i - 1).get("center_id")))) {
        overrideList.get(i).put("start_time",overrideList.get(i - 1).get("start_time"));
        overrideList.get(i - 1).put("r_delete", "true");
        if (changedIndexList.contains(i) 
              || changedIndexList.contains(i - 1)) {
          flag = true;
        }
        if (flag) {
          if (remarks != null) {
            overrideList.get(i).put("remarks",remarks);
          }
          if (visitMode != null && !visitMode.isEmpty()) {
            overrideList.get(i).put("visit_mode", visitMode);
          } else {
            overrideList.get(i).put("visit_mode", DEFAULT_VISIT_MODE);
          }
        }
      } else {
        flag = false;
      }
    }

    for (int i = 0; i < overrideList.size(); i++) {
      if (overrideList.get(i).get("availability_status").equals("N")) {
        overrideList.get(i).put("center_id",null);
      }
    }
  }
  
  /**
   * Gets the resource override detail beans.
   *
   * @param overrideMap the override map
   * @param resAvailId the res avail id
   * @param overrideDetailsList the override details list
   */
  public void getResourceOverrideDetailBeans(List<Map> overrideMap, int resAvailId,
      List<BasicDynaBean> overrideDetailsList) {
    for (Map overrideDetails : overrideMap) {
      BasicDynaBean schDefaultAvailBean = resourceDetailsRepository.getBean();
      int resAvailDdetailsId = (Integer) resourceDetailsRepository.getNextId();
      schDefaultAvailBean.set("res_avail_details_id", resAvailDdetailsId);
      schDefaultAvailBean.set("remarks", overrideDetails.get("remarks"));
      schDefaultAvailBean.set("availability_status", overrideDetails.get("availability_status"));
      schDefaultAvailBean.set("center_id", (Integer) overrideDetails.get("center_id"));
      schDefaultAvailBean.set("res_avail_id", resAvailId);
      schDefaultAvailBean.set("visit_mode", (String) overrideDetails.get("visit_mode"));
      try {
        schDefaultAvailBean.set("from_time",
            DateUtil.parseTime((String) overrideDetails.get("start_time")));
        schDefaultAvailBean.set("to_time",
            DateUtil.parseTime((String) overrideDetails.get("end_time")));
      } catch (ParseException exp) {
        throw new ValidationException("exception.scheduler.override.invalid.time");
      }

      overrideDetailsList.add(schDefaultAvailBean);
    }
  }
  
  /**
   * Check override exists.
   *
   * @param startDate the start date
   * @param endDate the end date
   * @param resourceIds the resource ids
   * @return true, if successful
   */
  public boolean checkOverrideExists(Date startDate, Date endDate, String[] resourceIds) {
    if (resourceIds != null && resourceIds.length > 0) {
      return resourceOverrideRepository.checkOverrideExists(startDate, endDate, resourceIds);
    }
    return false;
  }

  /**
   * Delete existing override.
   *
   * @param startDate the start date
   * @param endDate the end date
   * @param resourceIds the resource ids
   * @return true, if successful
   */
  public boolean deleteExistingOverride(Date startDate, Date endDate, String[] resourceIds) {
    if (resourceIds != null && resourceIds.length > 0) {
      return resourceOverrideRepository.deleteExistingOverride(startDate, endDate, resourceIds);
    }
    return false;
  }

  /**
   * Insert bulk overrides.
   *
   * @param requestParams the request params
   */
  @Transactional(rollbackFor = Exception.class)
  public void insertBulkOverrides(Map requestParams) {
    // TODO check access right
    HashMap<String, List<Map>> cardOverrideDetailsMap = new HashMap<String, List<Map>>();
    HashMap<String, String[]> cardDowMap = new HashMap<String, String[]>();
    java.sql.Date startDate = null;
    java.sql.Date endDate = null;
    try {
      startDate = DateUtil.parseDate(((String[]) requestParams.get("from_date"))[0]);
      endDate = DateUtil.parseDate(((String[]) requestParams.get("to_date"))[0]);
    } catch (ParseException exp) {
      throw new ValidationException("exception.scheduler.override.invalid.dates");
    }
    String[] toTime = (String[]) requestParams.get("to_time");
    String[] status = (String[]) requestParams.get("availability_status");
    String[] remarks = (String[]) requestParams.get("remarks");
    String[] visitMode = (String[]) requestParams.get("visit_mode");
    String resType = ((String[]) requestParams.get("res_sch_type"))[0];
    boolean shouldOverrideExisting = false;
    if (requestParams.get("override_existing") != null
        && ((String[]) requestParams.get("override_existing"))[0] != null
        && ((String[]) requestParams.get("override_existing"))[0].equals("on")) {
      shouldOverrideExisting = true;
    }
    String[] resIds = (String[]) requestParams.get("res_sch_name");
    String[] cardId = (String[]) requestParams.get("day_of_week");
    Integer centerId = null;
    if (requestParams.get("select_center_id") != null
        && ((String[]) requestParams.get("select_center_id"))[0] != null) {
      centerId = Integer.parseInt(((String[]) requestParams.get("select_center_id"))[0]);
    } else {
      centerId = RequestContext.getCenterId();
    }
    String[] fromTime = (String[]) requestParams.get("from_time");
    boolean overridePresent = checkOverrideExists(startDate, endDate, resIds);

    if (!shouldOverrideExisting) {
      if (overridePresent) {
        throw new HMSException("ui.label.override.exists");
      }
    } else {
      deleteExistingOverride(startDate, endDate, resIds);
    }

    List<String> allDow = new ArrayList<>();
    Integer centersIncDefault = (Integer) prefService.getAllPreferences()
        .get("max_centers_inc_default");
    for (int i = 0; i < fromTime.length; i++) {
      String card = cardId[i];
      // no dow selected in the card
      if (requestParams.get("dow" + card) == null) {
        continue;
      } else {
        cardDowMap.put(card, (String[]) requestParams.get("dow" + card));
        allDow.addAll(Arrays.asList((String[]) requestParams.get("dow" + card)));
      }
      if (fromTime != null && !fromTime[i].equals("")) {
        Map availabilityDetail = new HashMap();
        availabilityDetail.put("start_time", fromTime[i]);
        availabilityDetail.put("end_time", toTime[i]);
        availabilityDetail.put("availability_status", status[i]);
        availabilityDetail.put("remarks", remarks[i]); 
        availabilityDetail.put("center_id", status[i].equalsIgnoreCase("N") ? null : centerId);
        if (visitMode != null && visitMode[i] != null && !visitMode[i].isEmpty()) {
          availabilityDetail.put("visit_mode", visitMode[i]);
        } else {
          availabilityDetail.put("visit_mode", DEFAULT_VISIT_MODE);
        }
        if (cardOverrideDetailsMap.containsKey(card)) {
          cardOverrideDetailsMap.get(card).add(availabilityDetail);
        } else {
          List<Map> newList = new ArrayList<Map>();
          newList.add(availabilityDetail);
          cardOverrideDetailsMap.put(card, newList);
        }

      }
    }

    HashMap<String, List<java.sql.Date>> cardDatesMap = generateCardDatesMap(startDate, endDate,
        cardDowMap);

    for (String resourceId : resIds) {
      List<BasicDynaBean> overrideList = new ArrayList<BasicDynaBean>();
      List<BasicDynaBean> overrideDetailsList = new ArrayList<BasicDynaBean>();
      for (String cardIdd : cardDatesMap.keySet()) {
        for (Date availableDate : cardDatesMap.get(cardIdd)) {
          BasicDynaBean bean = resourceOverrideRepository.getBean();
          int resAvailId = (Integer) resourceOverrideRepository.getNextId();
          bean.set("res_avail_id", resAvailId);
          bean.set("availability_date", availableDate);
          bean.set("res_sch_name", resourceId);
          bean.set("res_sch_type", resType);
          overrideList.add(bean);
          List<Map> overrideMap = cardOverrideDetailsMap.get(cardIdd);
          getResourceOverrideDetailBeans(overrideMap, resAvailId, overrideDetailsList);

        }
      }
      resourceOverrideRepository.batchInsert(overrideList);
      resourceDetailsRepository.batchInsert(overrideDetailsList);
      getResourceAppointments(resourceId, resType, null,
          new java.sql.Date(startDate.getTime()), new java.sql.Date(endDate.getTime()));
    }

  }

  /**
   * Generate card dates map.
   *
   * @param startDate the start date
   * @param endDate the end date
   * @param cardDowMap the card dow map
   * @return the hash map
   */
  private HashMap<String, List<java.sql.Date>> generateCardDatesMap(java.sql.Date startDate,
      java.sql.Date endDate, HashMap<String, String[]> cardDowMap) {
    HashMap<String, List<java.sql.Date>> cardDatesMap = new HashMap<String, List<java.sql.Date>>();
    Calendar cal = Calendar.getInstance();
    cal.setTime(startDate);
    int startDateDow = cal.get(Calendar.DAY_OF_WEEK) - 1;

    Calendar cal1 = Calendar.getInstance();
    Calendar cal2 = Calendar.getInstance();

    for (String cardIdd : cardDowMap.keySet()) {
      List<java.sql.Date> dates = new ArrayList<java.sql.Date>();
      String[] dowArray = cardDowMap.get(cardIdd);

      for (String dayStr : dowArray) {
        cal1.setTime(startDate);
        Integer day = Integer.parseInt(dayStr);
        int diff = day >= startDateDow ? day - startDateDow : 7 - (startDateDow - day);
        cal1.add(Calendar.DAY_OF_MONTH, diff);
        cal2.setTime(endDate);
        cal2.add(Calendar.DAY_OF_MONTH, 1);
        while (cal1.before(cal2)) {
          java.sql.Date time = new java.sql.Date(cal1.getTimeInMillis());
          dates.add(time);
          cal1.add(Calendar.DAY_OF_MONTH, 7);
        }
      }
      cardDatesMap.put(cardIdd, dates);
    }
    return cardDatesMap;

  }

  public boolean resourceOverrideExists(String resSchId, 
      Timestamp currentDateTime, String visitMode) {
    return resourceOverrideRepository.resourceOverrideExists(resSchId, currentDateTime, visitMode);
  }

}
