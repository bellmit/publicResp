package com.insta.hms.mdm.resourceavailability;

import static com.insta.hms.jobs.common.QuartzJobHelper.buildJob;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.batchjob.builders.AppointmentRescheduleJob;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.SearchQueryAssembler;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.scheduler.AppointmentCategoryFactory;
import com.insta.hms.core.scheduler.AppointmentService;
import com.insta.hms.core.scheduler.ResourceCategory;
import com.insta.hms.exception.DuplicateEntityException;
import com.insta.hms.exception.EntityNotFoundException;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.integration.book.BookIntegrationService;
import com.insta.hms.jobs.JobService;
import com.insta.hms.mdm.MasterService;
import com.insta.hms.mdm.centers.CenterRepository;
import com.insta.hms.mdm.doctors.DoctorRepository;
import com.insta.hms.mdm.doctors.DoctorService;
import com.insta.hms.mdm.resourceoverride.ResourceOverrideRepository;
import com.insta.hms.mdm.resourceoverride.ResourceOverrideService;
import com.insta.hms.resourcescheduler.ResourceDTO;
import flexjson.JSONSerializer;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


// TODO: Auto-generated Javadoc
/** The Class ResourceAvailabilityService. */
@Service
public class ResourceAvailabilityService extends MasterService {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(ResourceAvailabilityService.class);

  /** The Constant DAO regionDAO. */
  @LazyAutowired private ResourceAvailabilityRepository resAvailRepository;

  /** The res override repository. */
  @LazyAutowired private ResourceOverrideRepository resOverrideRepository;

  /** The center repo. */
  @LazyAutowired private CenterRepository centerRepo;

  /** The doctor service. */
  @LazyAutowired private DoctorService doctorService;

  /** The res def availability repo. */
  @LazyAutowired private ResourceDefaultAvailabilityRepository resDefAvailabilityRepo;

  /** The generic preferences service. */
  @LazyAutowired private GenericPreferencesService genericPreferencesService;

  /** The book integration service. */
  @LazyAutowired private BookIntegrationService bookIntegrationService;

  /** The appointment service. */
  @LazyAutowired private AppointmentService appointmentService;

  /** The session service. */
  @LazyAutowired private SessionService sessionService;

  /** The job service. */
  @LazyAutowired private JobService jobService;

  /** The resource override service. */
  @LazyAutowired
  private ResourceOverrideService resourceOverrideService;
  
  /** The appointment category factory. */
  @LazyAutowired
  private AppointmentCategoryFactory appointmentCategoryFactory;
  
  /** The doctor repository. */
  @LazyAutowired
  private DoctorRepository doctorRepository;
  
  /** Default value for visit mode. */
  private static final String DEFAULT_VISIT_MODE = "I";
  
  /**
   * Instantiates a new resource availability service.
   *
   * @param repository the repository
   * @param validator the validator
   */
  
  public ResourceAvailabilityService(
      ResourceAvailabilityRepository repository, ResourceAvailabilityValidator validator) {
    super(repository, validator);
  }

  /* (non-Javadoc)
   * @see com.insta.hms.mdm.MasterService#getSearchQueryAssembler(java.util.Map, java.util.Map)
   */
  @SuppressWarnings("rawtypes")
  @Override
  protected SearchQueryAssembler getSearchQueryAssembler(
      Map params, Map<LISTING, Object> listingParams) {
    Map<String, Object> parameterMap = new HashMap<String, Object>(params);
    List list = new ArrayList();
    list.add("*");
    list.add("SUR");
    list.add("SER");
    list.add("TST");
    List statusList = new ArrayList();
    statusList.add("A");
    SearchQueryAssembler qb = null;
    qb =
        new SearchQueryAssembler(
            ResourceAvailabilityRepository.Category_FIELDS,
            ResourceAvailabilityRepository.Category_COUNT,
            ResourceAvailabilityRepository.Category_TABLES,
            listingParams);
    qb.addFilter(qb.STRING, "foo.resource_status", "IN", statusList);
    qb.addFilter(qb.STRING, "foo.generic_resource_type_status", "IN", statusList);
    qb.addFilterFromParamMap(params);
    qb.addFilter(qb.STRING, "res_sch_type", "NOT IN", list);
    qb.addSecondarySort("res_sch_type");
    qb.build();

    return qb;
  }

  /**
   * Gets the list page data.
   *
   * @param requestParams the request params
   * @return the list page data
   */
  
  public Map<String, List<BasicDynaBean>> getListPageData(Map requestParams) {
    Map<String, List<BasicDynaBean>> refData = new HashMap<String, List<BasicDynaBean>>();
    List<BasicDynaBean> resourceBean = getCategoryDescription(true);
    refData.put("categoryDescripton", resourceBean);
    return refData;
  }

  /**
   * Sets the attributes.
   *
   * @param category the category
   * @param resourceType the resource type
   * @param resSchId the res sch id
   * @param mav the mav
   */
  public void setAttributes(String category, String resourceType, int resSchId, ModelAndView mav) {
    if (resourceType != null && !resourceType.equals("")) {
      if (resourceType.equals("DOC")) {
        category = "DOC";
      } else if (resourceType.equals("SER") || resourceType.equals("SRID")) {
        category = "SER";
      } else if (resourceType.equals("TST") || resourceType.equals("EQID")) {
        category = "TST";
      } else if (resourceType.equals("THID") || resourceType.equals("SUR")) {
        category = "SUR";
      } else {
        category = resourceType;
      }
    }

    List<BasicDynaBean> resourceBean = null;
    mav.addObject(
        "category_default_duration",
        DatabaseHelper.getInteger(
            "select default_duration from scheduler_master where res_sch_name='*' "
          + "and res_sch_type=? ",
            resourceType));

    resourceBean = resAvailRepository.getResourceDetails(category, resSchId);
    mav.addObject("ResourceBean", resourceBean);

    mav.addObject("ResourceBeanJSON", ConversionUtils.copyListDynaBeansToMap(resourceBean));

    boolean needPrimaryResource = false;
    List resourceTypeList =
        ConversionUtils.copyListDynaBeansToMap(
            resAvailRepository.getResourceTypes(needPrimaryResource));
    mav.addObject("resourceTypeListJSON", resourceTypeList);

    int loggedCenterId = RequestContext.getCenterId();
    mav.addObject(
        "DoctorsJSON",
        ConversionUtils.copyListDynaBeansToMap(
            resAvailRepository.getResourceMasterList("DOC", loggedCenterId)));

    mav.addObject("TheatresJSON", resAvailRepository.getResourceMasterList("THID", loggedCenterId));

    mav.addObject(
        "EquipmentsJSON",
        ConversionUtils.copyListDynaBeansToMap(
            resAvailRepository.getResourceMasterList("EQID", loggedCenterId)));

    List serviceResourcesListJson = resAvailRepository.getServResourceSchedules("SNP", 0);
    mav.addObject(
        "serviceResourcesListJson", ConversionUtils.listBeanToListMap(serviceResourcesListJson));

    mav.addObject(
        "genericResourceListJson",
        ConversionUtils.copyListDynaBeansToMap(
            resAvailRepository.getResourceMasterList("GEN", loggedCenterId)));

    if (category.equals("SUR")) {
      List operations =
          ConversionUtils.copyListDynaBeansToMap(
              resAvailRepository.getResourceMasterList("OPID", loggedCenterId));
      mav.addObject(
          "OperationsJSON",
          ConversionUtils.copyListDynaBeansToMap(
              resAvailRepository.getResourceMasterList("OPID", loggedCenterId)));
    } else if (category.equals("SER")) {
      mav.addObject(
          "ServicesJSON",
          ConversionUtils.copyListDynaBeansToMap(
              resAvailRepository.getResourceMasterList("SERV", loggedCenterId)));
    } else if (category.equals("TST")) {
      mav.addObject(
          "TestsJSON",
          ConversionUtils.copyListDynaBeansToMap(
              resAvailRepository.getResourceMasterList("DGC", loggedCenterId)));
      mav.addObject(
          "LabTechniciansJSON",
          ConversionUtils.copyListDynaBeansToMap(
              resAvailRepository.getResourceMasterList("LABTECH", loggedCenterId)));
    } else if (category.equals("BED")) {
      mav.addObject(
          "BedsJSON",
          ConversionUtils.copyListDynaBeansToMap(
              resAvailRepository.getResourceMasterList("BED", loggedCenterId)));
    }

    mav.addObject(
        "allResourcesList",
        ConversionUtils.listBeanToListMap(resAvailRepository.getAllResources()));

    List resourceAvailabilities = resAvailRepository.getResourceAvailabilities(resSchId);
    mav.addObject("resourceAvailabilitiesList", resourceAvailabilities);
    if (resourceAvailabilities != null && resourceAvailabilities.size() != 0) {
      mav.addObject(
          "resourceAvailableMap",
          ConversionUtils.listBeanToMapBean(resourceAvailabilities, "day_of_week_text"));
    }
  }

  /**
   * Save category details.
   *
   * @param params the params
   * @return the map
   */
  public Map<String, String> saveCategoryDetails(Map params) {
    BasicDynaBean exists = null;
    List errorList = new ArrayList();
    String method = ((String[]) params.get("method"))[0];
    
    Integer resSchId = 0;
    if (method.equals("update")) {
      resSchId = Integer.parseInt(((String[]) params.get("res_sch_id"))[0]);
    }
    String resSchName = ((String[]) params.get("schedulerName"))[0];
    String resourceType = ((String[]) params.get("res_sch_type"))[0];
    String resourceName = ((String[]) params.get("_resource_name"))[0];
    String warningMessage = null;
    SimpleDateFormat timeFormatterSecs = new SimpleDateFormat("HH:mm");
    
    String[] resourceDelete = (String[]) params.get("rDelete");
    String[] resourceTypes = (String[]) params.get("resource_name");
    String[] resourceValues = (String[]) params.get("resource_value");
    String[] itemIds = (String[]) params.get("itemId");
    String[] daysOfWeek = ((String[]) params.get("day_of_week"));
    String[] fromTime = (String[]) params.get("from_time");
    String[] toTime = (String[]) params.get("to_time");
    String[] visitMode = (String[]) params.get("visit_mode"); 
    Integer loggedInCenterId = RequestContext.getCenterId();
    BasicDynaBean schDefaultAvailBean = null;
    
    List resourceInsertList = new ArrayList();
    List resourceUpdateList = new ArrayList();
    List resourceDeleteList = new ArrayList();

    exists = resAvailRepository.getCategoryDetailsByResource(resSchName, resourceType, resSchId);
    if (method.equals("create")) {
      resSchId = resAvailRepository.getNextSequence();
    }

    int resources = 0;
    if (resourceTypes != null) {
      resources = resourceTypes.length;
    }
    Map messageMap = new HashMap();
    String status = ((String[]) params.get("status"))[0];
    messageMap.put("res_sch_id", resSchId);
    BasicDynaBean bean = resAvailRepository.getBean();
    bean.set("res_sch_id", resSchId);
    bean.set("status", status);
    bean.set("res_sch_type", resourceType);

    String category = null;
    if (resourceType != null) {
      if (resourceType.equals("SUR") || resourceType.equals("THID")) {
        category = "OPE";
      } else if (resourceType.equals("TST") || resourceType.equals("EQID")) {
        category = "DIA";
      } else if (resourceType.equals("SER") || resourceType.equals("SRID")) {
        category = "SNP";
      } else if (resourceType.equals("DOC")) {
        category = "DOC";
        // bean.set("dept", req.getParameter("dept1"));
      } else {
        category = resAvailRepository.getCategoryType(resourceType);
      }
      bean.set("res_sch_category", category);
      if (!resourceType.equals("DOC") && ((String[]) params.get("dept")) != null) {
        bean.set("dept", ((String[]) params.get("dept"))[0]);
      }
    }
    if (resourceType.equals("DOC") && ((String[]) params.get("dept1")) != null) {
      bean.set("dept", ((String[]) params.get("dept1"))[0]);
    }
    if (bean.get("dept") == null || bean.get("dept").equals("")) {
      bean.set("dept", "*");
    }
    if (((String[]) params.get("schedulerName")) != null) {
      bean.set("res_sch_name", resSchName);
    }
    if (((String[]) params.get("description")) != null) {
      bean.set("description", ((String[]) params.get("description"))[0]);
    }
    if (((String[]) params.get("duration")) != null) {
      bean.set("default_duration", new Integer(((String[]) params.get("duration"))[0]).intValue());
    }

    int totDefaultResourceInDb = ResourceAvailabilityRepository.getTotalResources();
    if (resSchId > (totDefaultResourceInDb + 1)) {
      int defaultCategoryDuration = resAvailRepository.getDefaultCategoryDuration(category);
      int defaultCategoryHeight = resAvailRepository.getDefaultCategoryHeight(category);

      int resHeight = new Integer(((String[]) params.get("duration"))[0]).intValue();
      resHeight = (resHeight / defaultCategoryDuration) * defaultCategoryHeight;
      bean.set("height_in_px", resHeight);
    } else {
      bean.set("height_in_px", new Integer(((String[]) params.get("height_in_px"))[0]).intValue());
    }

    for (int i = 0; i < resources; i++) {
      if (resourceTypes[i] != null) {
        if (!resourceTypes[i].equals("") && !resourceValues[i].equals("")) {
          ResourceDTO rdto = new ResourceDTO();
          rdto.setRes_sch_id(resSchId);
          rdto.setResourceId(resourceValues[i]);
          rdto.setResourceType(resourceTypes[i]);
          rdto.setCenterId(loggedInCenterId);
          if (itemIds != null) {
            if (itemIds[i].equals("") || itemIds[i].equals("*")) {
              if (resourceDelete[i].equals("N")) {
                resourceInsertList.add(rdto);
              } else {
                resourceDeleteList.add(rdto);
              }
            } else {
              rdto.setItem_id(itemIds[i]);
              if (resourceDelete[i].equals("N")) {
                resourceUpdateList.add(rdto);
              } else {
                resourceDeleteList.add(rdto);
              }
            }
          }
        }
      }
    }

    boolean success = true;
    Integer incCounter = 0;
    String message = "Failed";
    String warnMessage = null;
    int warnMessageCount = 0;

    List<BasicDynaBean> schedulerCenterList = doctorService.getResourceBelongingCenter(resSchName);
    for (BasicDynaBean doctor : schedulerCenterList) {
      if (bookIntegrationService.isBookEnabled(resSchName, (Integer) doctor.get("center_id"))) {
        warnMessageCount++;
      }
    }

    boolean isRestrict =
        status.equalsIgnoreCase("i")
            && resourceType.equals("DOC")
            && bookIntegrationService.isPractoAdvantageEnabled()
            && warnMessageCount > 0
            && method.equals("update");

    if (method.equals("create")) {
      Map columnValues = new HashMap();
      columnValues.put("res_sch_name", resSchName);
      columnValues.put("res_sch_type", resourceType);
      columnValues.put("status", "A");
      boolean isExists =
          ((ResourceAvailabilityRepository) getRepository()).findByKey(columnValues) != null;
      if (isExists) {
        if (resourceName != null) {
          throw new DuplicateEntityException(new String[] {"Category", resourceName});
        }
      } else {
        success = ((ResourceAvailabilityRepository) getRepository()).insert(bean) > 0;
      }
      message = "Category master details inserted successfully..";
    } else {
      if (isRestrict) {
        warnMessage =
            "This doctor can not be marked in-active because"
                + " \" Practo Book \" is enabled for this doctor. Try again after"
                + " disabling Practo Book or contact your Administrator.";
      } else {
        BasicDynaBean resourceBean =
            ((ResourceAvailabilityRepository) getRepository())
                .getCategoryDetailsByResourceId(resSchName, resourceType, resSchId);
        if (resourceBean == null) {
          Map<String, Object> keys = new HashMap<String, Object>();
          keys.put("res_sch_id", bean.get("res_sch_id"));
          incCounter = resAvailRepository.update(bean, keys);
          if (incCounter > 0) {
            success = true;
            message = "Category master details updated successfully..";
          }
        } else {
          if (resourceName != null) {
            throw new DuplicateEntityException(new String[] {"Category", resourceName});
          }
        }
      }
    }

    if (!isRestrict) {
      if (!resourceDeleteList.isEmpty()) {
        success = resAvailRepository.deleteResources(resourceDeleteList);
      }
      if (!resourceInsertList.isEmpty()) {
        success = resAvailRepository.insertResources(resourceInsertList);
      }
      if (!resourceUpdateList.isEmpty()) {
        success = resAvailRepository.updateResources(resourceUpdateList);
      }
    }

    if (resSchId != 0) {
      if (method.equals("create")) {
        for (int k = 0; k < daysOfWeek.length; k++) {
          if (fromTime != null
              && fromTime[k] != null
              && !fromTime[k].equals("")
              && toTime != null
              && toTime[k] != null
              && !toTime[k].equals("")) {

            schDefaultAvailBean = resDefAvailabilityRepo.getBean();
            ConversionUtils.copyIndexToDynaBean(params, k, schDefaultAvailBean, errorList);
            try {
              if (fromTime != null && fromTime[k] != null && !fromTime[k].equals("")) {
                schDefaultAvailBean.set(
                    "from_time", new java.sql.Time(timeFormatterSecs.parse(fromTime[k]).getTime()));
              } else {
                schDefaultAvailBean.set("from_time", null);
              }
              schDefaultAvailBean.set("res_sch_id", resSchId);
            } catch (ParseException exception) {
              logger.debug("Failed to parse the date details" + exception.getMessage());
            }

            if (resourceType != null && resourceType.equalsIgnoreCase("DOC") 
                    && visitMode[k] != null
                    && !visitMode[k].isEmpty()) {
              schDefaultAvailBean.set("visit_mode", visitMode[k]);
            } else {
              schDefaultAvailBean.set("visit_mode", DEFAULT_VISIT_MODE);
            }
            success = resDefAvailabilityRepo.insert(schDefaultAvailBean) > 0;
            if (!success) {
              success = false;
              break;
            }
          }
        }
      } else {
        if (isRestrict) {
          warnMessage =
              "This doctor can not be marked in-active because"
                  + " \" Practo Book \" is enabled for this doctor. Try again after"
                  + " disabling Practo Book or contact your Administrator.";
        } else {
          List existsList = resAvailRepository.getResourceAvailabilities(resSchId);
          if (existsList != null && existsList.size() != 0) {
            success = resDefAvailabilityRepo.delete("res_sch_id", resSchId) > 0;
          }
          for (int k = 0; k < daysOfWeek.length; k++) {
            if (fromTime != null
                && fromTime[k] != null
                && !fromTime[k].equals("")
                && toTime != null
                && toTime[k] != null
                && !toTime[k].equals("")) {

              schDefaultAvailBean = resDefAvailabilityRepo.getBean();
              ConversionUtils.copyIndexToDynaBean(params, k, schDefaultAvailBean, errorList);
              try {
                if (fromTime != null && fromTime[k] != null && !fromTime[k].equals("")) {
                  schDefaultAvailBean.set(
                      "from_time",
                      new java.sql.Time(timeFormatterSecs.parse(fromTime[k]).getTime()));
                } else {
                  schDefaultAvailBean.set("from_time", null);
                }
                schDefaultAvailBean.set("res_sch_id", resSchId);
              } catch (ParseException exception) {
                logger.debug("Failed to parse the date details" + exception.getMessage());
              }
              if (resourceType != null && resourceType.equalsIgnoreCase("DOC") 
                      && visitMode[k] != null
                      && !visitMode[k].isEmpty()) {
                schDefaultAvailBean.set("visit_mode", visitMode[k]);
              } else {
                schDefaultAvailBean.set("visit_mode", DEFAULT_VISIT_MODE);
              }
              success = resDefAvailabilityRepo.insert(schDefaultAvailBean) > 0;
            }
          }
          if (success) {
            boolean updateDoctors = true;
            List<String> doctorIdlist = new ArrayList<String>();
            doctorIdlist.add(resSchName);
            if (bookIntegrationService.isPractoAdvantageEnabled()
                && resourceType.equals("DOC")
                && !resSchName.equals("*")) {
              if (loggedInCenterId != 0) {
                if (bookIntegrationService.isBookEnabled(resSchName, loggedInCenterId)) {
                  bookIntegrationService.registerDoctors(
                      doctorIdlist, loggedInCenterId, updateDoctors);
                }
              } else {
                List<BasicDynaBean> schedulerDoctorCenterList =
                    doctorService.getResourceBelongingCenter(resSchName);
                for (BasicDynaBean doctor : schedulerDoctorCenterList) {
                  if (bookIntegrationService.isBookEnabled(
                      resSchName, (Integer) doctor.get("center_id"))) {
                    bookIntegrationService.registerDoctors(
                        doctorIdlist, (Integer) doctor.get("center_id"), updateDoctors);
                  }
                }
              }
            }
            warningMessage = getResourceAppointments(resSchId, resourceType);
            success = true;
          } else {
            message = "Failed";
          }
        }
      }
    }
    messageMap.put("book_enable_warn_message", warnMessage);
    messageMap.put("success_or_error_message", message);
    messageMap.put("warning_message", warningMessage);
    return messageMap;
  }

  /**
   * Gets the category description.
   *
   * @return the category description
   */
  public List<BasicDynaBean> getCategoryDescription(boolean primaryCategory) {
    return ((ResourceAvailabilityRepository) getRepository())
        .getCategoryDescription(primaryCategory);
  }

  /**
   * Gets the resource timings by duration.
   *
   * @param params the params
   * @return the resource timings by duration
   */
  public String getResourceTimingsByDuration(Map params) {

    long slotDuration = 0;
    String screenId = ((String[]) params.get("screenId"))[0];
    java.sql.Time scheduleStartTime = null;
    java.sql.Time scheduleEndTime = null;

    List<BasicDynaBean> timingList = new ArrayList<BasicDynaBean>();
    try {
      scheduleStartTime = new Time(DataBaseUtil.parseTime("00:00").getTime());
      scheduleEndTime = new Time(DataBaseUtil.parseTime("23:59").getTime());
    } catch (ParseException exception) {
      logger.debug("Failed to parse the date details" + exception.getMessage());
    }
    Long startTime = scheduleStartTime.getTime();
    Long endTime = scheduleEndTime.getTime();
    Time time = null;
    BasicDynaBean bean = null;

    if (screenId != null && !screenId.equals("resourceAvailability")) {
      int duration = Integer.parseInt(((String[]) params.get("duration"))[0]);
      slotDuration = duration * 60L * 1000;
      if (startTime != null && endTime != null) {
        for (long slotTime = startTime.longValue();
            slotTime < endTime.longValue();
            slotTime = slotTime + slotDuration) {
          bean = resDefAvailabilityRepo.getBean();
          time = new java.sql.Time(slotTime);
          bean.set("from_time", time);
          timingList.add(bean);
        }
        bean = resDefAvailabilityRepo.getBean();
        time = new java.sql.Time(endTime);
        bean.set("from_time", time);
        timingList.add(bean);
      }
    } else {
      String resourceType = ((String[]) params.get("resource_type"))[0];
      String schedulerName = ((String[]) params.get("scheduler_name"))[0];
      List<String> columns = null;
      LinkedHashMap<String, Object> identifiers = new LinkedHashMap<String, Object>();
      identifiers.put("res_sch_type", resourceType);
      identifiers.put("res_sch_name", schedulerName);
      identifiers.put("status", "A");
      BasicDynaBean schBean = resAvailRepository.findByKey(identifiers);
      int schDuration = resAvailRepository.getSchedulerDuration(resourceType);
      if (schBean != null) {
        schDuration = (Integer) schBean.get("default_duration");
        slotDuration = schDuration * 60L * 1000;
        if (startTime != null && endTime != null) {
          for (long slotTime = startTime.longValue();
              slotTime < endTime.longValue();
              slotTime = slotTime + slotDuration) {
            bean = resDefAvailabilityRepo.getBean();
            time = new java.sql.Time(slotTime);
            bean.set("from_time", time);
            timingList.add(bean);
          }
          bean = resDefAvailabilityRepo.getBean();
          time = new java.sql.Time(endTime);
          bean.set("from_time", time);
          timingList.add(bean);
        }
      } else {
        slotDuration = schDuration * 60L * 1000;
        if (startTime != null && endTime != null) {
          for (long slotTime = startTime.longValue();
              slotTime < endTime.longValue();
              slotTime = slotTime + slotDuration) {
            bean = resDefAvailabilityRepo.getBean();
            time = new java.sql.Time(slotTime);
            bean.set("from_time", time);
            timingList.add(bean);
          }
          bean = resDefAvailabilityRepo.getBean();
          time = new java.sql.Time(endTime);
          bean.set("from_time", time);
          timingList.add(bean);
        }
      }
    }

    String responseContent =
        new JSONSerializer().serialize(ConversionUtils.listBeanToListMap(timingList));
    return responseContent;
  }

  /**
   * Gets the scheduler center.
   *
   * @param params the params
   * @return the scheduler center
   */
  public String getSchedulerCenter(Map params) {
    String doctorId = ((String[]) params.get("doctor_id"))[0];
    List<Map> schedulerCenterList =
        ConversionUtils.copyListDynaBeansToMap(doctorService.getResourceBelongingCenter(doctorId));

    Map<String, List> schedulerCentermap = new HashMap<String, List>();

    schedulerCentermap.put("centerlist", schedulerCenterList);
    String schedulerCenterLists =
        new JSONSerializer().exclude("class").deepSerialize(schedulerCentermap);
    return schedulerCenterLists;
  }

  /**
   * Gets the resource appointments.
   *
   * @param resSchId the res sch id
   * @param resourceType the resource type
   * @return the resource appointments
   */
  public String getResourceAppointments(int resSchId, String resourceType) {
    String message = null;

    if (resourceType != null
        && (resourceType.equals("DOC")
            || resourceType.equals("EQID")
            || resourceType.equals("THID")
            || resourceType.equals("SRID"))) {

      String resourceId =
          (String) resAvailRepository.findByKey("res_sch_id", resSchId).get("res_sch_name");
      List<BasicDynaBean> resourceUnavailabilityDetails = null;
      boolean flag = true;
      DateUtil du = new DateUtil();
      List<Integer> appointmentIds = new ArrayList<Integer>();
      List<String> appointmentStatus = new ArrayList<String>();

      resourceUnavailabilityDetails = resAvailRepository.getNonAvailResourceDetails(resSchId);
      List<BasicDynaBean> resourceAppointmetList =
          resAvailRepository.getResourceAppointmentDetails(resourceType, resourceId);
      if (resourceAppointmetList != null && resourceAppointmetList.size() > 0) {
        for (BasicDynaBean resourceAppDetailsBean : resourceAppointmetList) {
          Timestamp apptStartDateTime =
              (Timestamp) resourceAppDetailsBean.get("appointment_start_time");
          Timestamp apptEndDateTime =
              (Timestamp) resourceAppDetailsBean.get("end_appointment_time");
          Calendar cal = Calendar.getInstance();
          cal.setTime(new Date(apptStartDateTime.getTime()));
          Integer apptDayofweek = cal.get(Calendar.DAY_OF_WEEK) - 1;
          if (resourceUnavailabilityDetails != null && resourceUnavailabilityDetails.size() > 0) {
            for (BasicDynaBean resourceTimingsBean : resourceUnavailabilityDetails) {
              Integer dayofWeek = (Integer) resourceTimingsBean.get("day_of_week");
              if (apptDayofweek.equals(dayofWeek)) {
                Time fromTime = (Time) resourceTimingsBean.get("from_time");
                Time toTime = (Time) resourceTimingsBean.get("to_time");
                Timestamp fromDateTime = null;
                Timestamp endDateTime = null;
                try {
                  fromDateTime =
                      DateUtil.parseTimestamp(
                          du.getDateFormatter().format(apptStartDateTime)
                              + " "
                              + fromTime.toString());
                  endDateTime =
                      DateUtil.parseTimestamp(
                          du.getDateFormatter().format(apptEndDateTime) + " " + toTime.toString());
                } catch (ParseException exception) {
                  logger.debug("Failed to parse the date details" + exception.getMessage());
                }

                if ((apptStartDateTime.getTime() <= fromDateTime.getTime()
                        && apptStartDateTime.getTime() > fromDateTime.getTime())
                    || (apptStartDateTime.getTime() >= fromDateTime.getTime()
                        && apptStartDateTime.getTime() < endDateTime.getTime())) {
                  String appointStatus = (String) resourceAppDetailsBean.get("appointment_status");
                  Integer appointmentId = (Integer) resourceAppDetailsBean.get("appointment_id");
                  message =
                      "There are appointments existing for these timeslots "
                          + "marked non available.Please reschedule.";
                  
                  List filterValues = new ArrayList();
                  BasicDynaBean resDefValBean = null;
                  // don't send message in case separate default availability 
                  // defined for the resources
                  if (resourceId.equals("*")) {
                    filterValues.add(resourceAppDetailsBean.get("resource_id"));
                    filterValues.add(dayofWeek);
                    filterValues.add((Integer)resourceAppDetailsBean.get("center_id"));
                    resDefValBean = resAvailRepository
                        .getDefaultAvaibilityForResource(filterValues);                    
                  }
                  if (resourceType.equals("DOC") && resDefValBean == null) {
                    if (appointStatus.equalsIgnoreCase("Booked")
                        || appointStatus.equalsIgnoreCase("Confirmed")
                            && !(appointmentService.getAppointmentSource(appointmentId) != null
                                && appointmentService
                                    .getAppointmentSource(appointmentId)
                                    .equalsIgnoreCase("practo"))) {
                      appointmentIds.add(appointmentId);
                      appointmentStatus.add(appointStatus);
                    }
                  }
                }
              }
            }
          }
        }
      }
      if (appointmentIds != null
          && appointmentIds.size() > 0
          && appointmentStatus != null
          && appointmentStatus.size() > 0) {
        sendWarningMessage(appointmentIds, appointmentStatus);
      }
    }

    return message;
  }

  /**
   * Gets the category details.
   *
   * @param resSchId the res sch id
   * @return the category details
   */
  public BasicDynaBean getCategoryDetails(int resSchId) {
    return resAvailRepository.getCategoryDetails(resSchId);
  }

  /**
   * Gets the resource master list.
   *
   * @param resourceType the resource type
   * @param loggedCenterId the logged center id
   * @return the resource master list
   */
  public List<BasicDynaBean> getResourceMasterList(String resourceType, int loggedCenterId) {
    return ((ResourceAvailabilityRepository) getRepository())
        .getResourceMasterList(resourceType, loggedCenterId);
  }

  /**
   * Gets the all resources.
   *
   * @return the all resources
   */
  public List<BasicDynaBean> getAllResources() {
    return ((ResourceAvailabilityRepository) getRepository()).getAllResources();
  }

  /**
   * Gets the resource details.
   *
   * @param resourceType the resource type
   * @param resourceName the resource name
   * @return the resource details
   */
  public BasicDynaBean getResourceDetails(String resourceType, String resourceName) {
    return resAvailRepository.getResourceDetails(resourceType, resourceName);
  }

  /**
   * Gets the resource availtiming list.
   *
   * @param resAvailId the res avail id
   * @return the resource availtiming list
   */
  public static List<BasicDynaBean> getResourceAvailtimingList(int resAvailId) {
    return ResourceOverrideRepository.getResourceAvailtimingList(resAvailId);
  }


  /**
   * Gets the default resource availabilities.
   *
   * @param resourceId the resource id
   * @param dayOfWeek the day of week
   * @param resourceType the resource type
   * @param availabilityStatus the availability status
   * @param centerId the center id
   * @return the default resource availabilities
   */
  
  public List<BasicDynaBean> getDefaultResourceAvailabilities(
      String resourceId,
      Integer dayOfWeek,
      String resourceType,
      String availabilityStatus,
      Integer centerId) {

    int centersIncDefault = (Integer) genericPreferencesService.getAllPreferences()
        .get("max_centers_inc_default");
    return resAvailRepository.getDefaultResourceAvailabilities(
        resourceId, dayOfWeek, resourceType, availabilityStatus, centerId, centersIncDefault);
  }

  /**
   * Send warning message.
   *
   * @param appointmentIds the appointment ids
   * @param appointmentStatus the appointment status
   */
  private void sendWarningMessage(List appointmentIds, List<String> appointmentStatus) {
    String userName = (String) sessionService.getSessionAttributes().get("userId");
    Map<String, Object> jobData = new HashMap<String, Object>();
    jobData.put("userName", userName);
    jobData.put("eventData", appointmentIds);
    jobData.put("status", appointmentStatus);
    jobData.put("schema", RequestContext.getSchema());
    String uniqueName = DateUtil.getCurrentIso8601Timestamp() + "" + RequestContext.getUserName();
    jobService.scheduleImmediate(
        buildJob("AppointmentRescheduleJob" + uniqueName, AppointmentRescheduleJob.class, jobData));
  }

  /**
   * Gets the resource bean.
   *
   * @param resSchId the res sch id
   * @return the resource bean
   */
  public BasicDynaBean getResourceBean(Integer resSchId) {
    return resAvailRepository.findByKey("res_sch_id", resSchId);
  }

  /**
   * Gets the available slots.
   *
   * @param dateStr the date str
   * @param resourceId the resource id
   * @param centerId the center id
   * @param bookedSlot the booked slot
   * @param fromDate1 the from date 1
   * @param toDate1 the to date 1
   * @param visitMode the visit mode
   * @return the available slots
   */
  public Map<String, Object> getAvailableSlots(String dateStr, String resourceId, Integer centerId,
      String bookedSlot,String fromDate1, String toDate1, String visitMode,String firstAvailable) {
    if (!doctorRepository.exist("doctor_id", resourceId)) {
      throw new EntityNotFoundException(new String[] { "Resource", "Resouce Id", resourceId });
    }
    Date currentDate = DateUtil.getCurrentDate();
    Date date = currentDate;
    Calendar cal = Calendar.getInstance();
    
    if (dateStr != null || fromDate1 != null) {
      try {
        if (fromDate1 != null) {
          date = DateUtil.parseIso8601Date(fromDate1);
        } else {
          date = DateUtil.parseIso8601Date(dateStr);
        }
      } catch (ParseException exception) {
        throw new ValidationException("exceptopn.scheduler.invalid.date");
      }
      if (date.before(currentDate)) {
        date = currentDate;
      }
    }
    cal.setTime(date);
    cal.add(Calendar.DATE, 30);
    Date date1 = new Date(cal.getTimeInMillis()); 
    
    cal.setTime(date);
    cal.add(Calendar.DATE, 1);
    // if_to_date is passed check is it with in 30 days 
    Date toDate = new Date(cal.getTimeInMillis()); 
    if (toDate1 != null) {
      try {
        toDate = DateUtil.parseIso8601Date(toDate1);
      } catch (ParseException exception) {
        throw new ValidationException("exceptopn.scheduler.invalid.date");
      }
      if (toDate.before(date)) {
        toDate = date;
      } else if (toDate.after(date1)) {
        toDate = date1;
      }
    } else if (!StringUtils.isEmpty(firstAvailable) && firstAvailable.equalsIgnoreCase("N")) {
      toDate = date;
    }
    boolean isSharingAlreadyBookedSlots = !(bookedSlot != null && bookedSlot.equalsIgnoreCase("I"));
    String category = ResourceCategory.DOC.name().toUpperCase(Locale.ENGLISH);
    List<Map<String, Object>> timeSlots = new ArrayList<>();
    List<BasicDynaBean> visitTimingsList = getVisitTimings(date, resourceId, centerId, category,
        visitMode);
    Date firstAvaDate = null;
    if (!visitTimingsList.isEmpty()) {
      timeSlots = generateSlots(visitTimingsList, date, resourceId, isSharingAlreadyBookedSlots,
          category, visitMode);
      firstAvaDate = date;
      convertToUtcSlots(timeSlots, date);
    }
    int dayCounter = (int) DateUtil.getDifferenceDays(date, toDate, "days");
    while ((timeSlots.isEmpty()
        || (!StringUtils.isEmpty(firstAvailable) && firstAvailable.equalsIgnoreCase("N")))
        && dayCounter > 0) {
      dayCounter--;
      cal.setTime(date);
      cal.add(Calendar.DATE, 1);
      date = new Date(cal.getTimeInMillis()); 
      visitTimingsList = getVisitTimings(date, resourceId, centerId, category,visitMode);
      if (!visitTimingsList.isEmpty()) {
        List<Map<String, Object>> timeList = generateSlots(visitTimingsList, date, resourceId,
            isSharingAlreadyBookedSlots, category,visitMode);
        convertToUtcSlots(timeList, date);
        if (firstAvaDate == null) {
          firstAvaDate = date;
        }
        timeSlots.addAll(timeList);
      }
    }
    Map<String, Object> slotsMap = new HashMap<>();
    slotsMap.put("slots", timeSlots);
    if (!timeSlots.isEmpty()) {
      slotsMap.put("first_available_date", firstAvaDate);
    } else {
      slotsMap.put("first_available_date", "");
    }
    return slotsMap;
  }

  /**
   * Convert to utc slots.
   *
   * @param timeSlots the time slots
   * @param date the date
   */
  private void convertToUtcSlots(List<Map<String, Object>> timeSlots, Date date) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    cal.add(Calendar.DAY_OF_MONTH, -1);
    Date dayBefore = new Date(cal.getTimeInMillis());
    for (int i = 0; i < timeSlots.size(); i++) {
      Time utc = null;
      try {
        utc = DateUtil.parseIso8601Time("00:00:00Z");
      } catch (ParseException exception) {
        logger.error("Parse Exception", exception);
      }
      Map<String, Object> slotMap = timeSlots.get(i);
      Time slot = (Time) slotMap.get("timeslot");
      if (slot.before(utc)) {
        slotMap.put("timeslot",
            DateUtil.formatIso8601Date(dayBefore) + "T" + DateUtil.formatIso8601Time(slot));
      } else {
        slotMap.put("timeslot",
            DateUtil.formatIso8601Date(date) + "T" + DateUtil.formatIso8601Time(slot));
      }
    }
  }


  
  /**
   * Gets the visit timings.
   *
   * @param date the date
   * @param resourceId the resource id
   * @param centerId the center id
   * @param category the category
   * @param toDate the to date
   * @param visitMode the visit mode
   * @return the visit timings
   */
  
  private List<BasicDynaBean> getVisitTimings(Date date, String resourceId, Integer centerId,
      String category, String visitMode) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    int weekDayNo = (cal.get(Calendar.DAY_OF_WEEK) - 1);

    // get resource override for particular resourceId
    List<BasicDynaBean> visitTimingsList = resourceOverrideService.getResourceOverrides(
        resourceId, category, date, date, null, null);
    if (visitTimingsList.isEmpty()) {
      // get default resource availability for particular resourceId
      visitTimingsList = getDefaultResourceAvailabilities(resourceId, weekDayNo, category, null,
          null);
    }
    if (visitTimingsList.isEmpty()) {
      // get default resource availability for resourceId as *
      visitTimingsList = getDefaultResourceAvailabilities("*", weekDayNo, category, null, null);
      for (BasicDynaBean visitimTiming : visitTimingsList) {
        visitimTiming.set("center_id", centerId);
      }

    }
    if (!visitTimingsList.isEmpty()) {
      if (visitMode != null && !visitMode.equals("")
          && (visitMode.toUpperCase().equals("I") || visitMode.toUpperCase().equals("O"))) {
        visitTimingsList = filterVisitTimingsByVisitMode(visitTimingsList, visitMode);
      }
      visitTimingsList = filterAvailableTimingsByCenter(visitTimingsList, centerId);
    }
    return visitTimingsList;
  }

  /**
   * Filter available timings by center.
   *
   * @param visitTimingsList the visit timings list
   * @param centerId the center id
   * @return the list
   */
  private List<BasicDynaBean> filterAvailableTimingsByCenter(List<BasicDynaBean> visitTimingsList,
      Integer centerId) {
    List<BasicDynaBean> availableVisitTimingsList = new ArrayList<>();
    for (BasicDynaBean visitTiming : visitTimingsList) {
      if (visitTiming.get("availability_status").equals("A")) {
        if (centerId == null) {
          if (visitTiming.get("center_id") == null) {
            visitTiming.set("center_id", 0);
          }
          availableVisitTimingsList.add(visitTiming);
        } else if (visitTiming.get("center_id") != null && (((Integer) visitTiming
            .get("center_id")).equals(centerId) || ((Integer) visitTiming.get("center_id"))
            .equals(0))) {
          if (centerId != null && ((Integer) visitTiming.get("center_id")).equals(0)) {
            visitTiming.set("center_id", centerId);
          }
          availableVisitTimingsList.add(visitTiming);
        }
      }
    }
    return availableVisitTimingsList;
  }

  /**
   * Generate slots.
   *
   * @param timingList the timing list
   * @param date the date
   * @param resourceId the resource id
   * @param isSharingAlreadyBookedSlots the is sharing already booked slots
   * @param category the category
   * @return the list
   */
  private List<Map<String, Object>> generateSlots(List<BasicDynaBean> timingList, Date date,
      String resourceId, boolean isSharingAlreadyBookedSlots, String category,String visitMode) {
    int minToMilliSec = 60 * 1000;
    Calendar cal = Calendar.getInstance();
    List<Map<String, Object>> timeList = new ArrayList<>();
    Integer overbook = 0;
    if (isSharingAlreadyBookedSlots) {
      BasicDynaBean doctorBean = doctorService.getDoctorById(resourceId);
      overbook = doctorBean.get("overbook_limit") != null ? (Integer) doctorBean
          .get("overbook_limit") : 99;
    }
    int defaultDuration = resAvailRepository.getDefaultDuration(resourceId, category);
    long defaultDurationMs = (defaultDuration * minToMilliSec);

    for (int i = 0; i < timingList.size(); i++) {
      BasicDynaBean resourceAvailableBean = timingList.get(i);
      Time fromTime = (Time) resourceAvailableBean.get("from_time");
      Long startTime = (Long) fromTime.getTime();
      int startMinute = fromTime.getMinutes() + (fromTime.getHours() * 60);
      int remainderFromTime = startMinute % defaultDuration;
      if (remainderFromTime != 0) {
        int minToadd = defaultDuration - remainderFromTime;
        startTime = startTime + (minToadd * minToMilliSec);
      }

      Time toTime = (Time) resourceAvailableBean.get("to_time");
      Long endTime = (Long) toTime.getTime();
      int endMinute = toTime.getMinutes() + (toTime.getHours() * 60);
      // handling 23:59 end time case
      if (endMinute == 1439) {
        endMinute = 1440;
      }
      int remainderToTime = endMinute % defaultDuration;
      if (remainderToTime != 0) {
        int minToSubtract = remainderToTime;
        endTime = endTime - (minToSubtract * minToMilliSec);
      }
      for (long j = startTime; j < endTime; j = j + defaultDurationMs) {
        Map<String, Object> map = new HashMap<>();
        map.put("timeslot", new java.sql.Time(j));
        map.put("center_id", (Integer) resourceAvailableBean.get("center_id"));
        map.put("visit_mode", (String)resourceAvailableBean.get("visit_mode"));
        timeList.add(map);
      }
    }
    if (overbook == null) {
      return timeList;
    }
 
    List<BasicDynaBean> appointmentList = appointmentService.getAppointmentCountAndTime(resourceId,
        date);
    for (BasicDynaBean appBean : appointmentList) {
      Integer apptDuration = (Integer) appBean.get("duration");
      Long apptDurationMs = (long) (apptDuration * 60 * 1000);
      Time apptTime = ((Time) appBean.get("time"));
      Long apptTimeLong = apptTime.getTime();
      for (long l = apptTimeLong; l < apptTimeLong + (apptDurationMs)
          ; l = l + defaultDurationMs) {
        Long appCount = (Long) appBean.get("count");
        if (appCount.intValue() > overbook) {
          Map<String, Object> map = new HashMap<>();
          map.put("timeslot", new java.sql.Time(l));
          map.put("center_id", (Integer) appBean.get("center_id"));
          String test = null;
          for (Map timeslot : timeList) {
            if (new java.sql.Time(l).equals(timeslot.get("timeslot"))) {
              test = (String) timeslot.get("visit_mode");
            }
          }
          map.put("visit_mode", test);
          timeList.remove(map);
        }
      }
    }
    return timeList;
  }

  /**
   * Gets the first available slots.
   *
   * @param dateStr the date str
   * @param resourceIds the resource ids
   * @param centerId the center id
   * @param bookedSlot the booked slot
   * @param fromDate the from date
   * @param visitMode the visit mode
   * @return the first available slots
   */
  public Map<String, Object> getFirstAvailableSlots(String dateStr, String[] resourceIds,
      Integer centerId, String bookedSlot, String fromDate, String visitMode) {
    boolean isSharingAlreadyBookedSlots = !(bookedSlot != null && bookedSlot.equalsIgnoreCase("I"));
    String category = ResourceCategory.DOC.name().toUpperCase(Locale.ENGLISH);
    Date currentDate = DateUtil.getCurrentDate();
    Date date = currentDate;
    
    if (dateStr != null || fromDate != null) {
      try {
        if (fromDate != null) {
          date = DateUtil.parseIso8601Date(fromDate);
        } else {
          date = DateUtil.parseIso8601Date(dateStr);
        }
      } catch (ParseException exception) {
        throw new ValidationException("exceptopn.scheduler.invalid.date");
      }
      if (date.before(currentDate)) {
        date = currentDate;
      }
    }
    Map<String, Object> firstSlotMap = new HashMap<>();
    try {
      for (String resourceId : resourceIds) {
        if (!doctorRepository.exist("doctor_id", resourceId)) {
          throw new EntityNotFoundException(new String[] { "Resource", "Resouce Id", resourceId });
        }
        Date day = date;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int counter = 0;
        List<Map<String, Object>> timeCenterList = getDoctorFirstAvailableSlot(day, resourceId,
            centerId, category, isSharingAlreadyBookedSlots, visitMode);
        while ((timeCenterList.isEmpty() && counter < 10)) {
          cal.add(Calendar.DATE, 1);
          day = new Date(cal.getTimeInMillis());
          counter++;
          timeCenterList = getDoctorFirstAvailableSlot(day, resourceId, centerId, category,
              isSharingAlreadyBookedSlots, visitMode);
        }
        firstSlotMap.put(resourceId, timeCenterList);
      }
    } catch (ParseException exception) {
      throw new ValidationException("exceptopn.scheduler.invalid.date");
    }
    Map<String, Object> slotsMap = new HashMap<>();
    slotsMap.put("slots", firstSlotMap);
    return slotsMap;
  }

  /**
   * Gets the doctor first available slot.
   *
   * @param d1 the d 1
   * @param resourceId the resource id
   * @param centerId the center id
   * @param category the category
   * @param isSharingAlreadyBookedSlots the is sharing already booked slots
   * @param visitMode the visit mode
   * @return the doctor first available slot
   * @throws ParseException the parse exception
   */
  private List<Map<String, Object>> getDoctorFirstAvailableSlot(Date d1, String resourceId,
      Integer centerId, String category, boolean isSharingAlreadyBookedSlots, String visitMode) 
          throws ParseException {
    List<BasicDynaBean> timingList = getVisitTimings(d1, resourceId, centerId, category, visitMode);
    List<Map<String, Object>> timeSlotsList = new ArrayList<>();
    Map<String, Object> timeSlotsMap = new HashMap<>();
    Integer overbook = 0;
    if (isSharingAlreadyBookedSlots) {
      BasicDynaBean doctorBean = doctorService.getDoctorById(resourceId);
      overbook = doctorBean.get("overbook_limit") != null ? (Integer) doctorBean
          .get("overbook_limit") : 99;
    }
    int defaultDuration = resAvailRepository.getDefaultDuration(resourceId, category);
    long slotDuration = (defaultDuration * 60 * 1000);

    if (timingList.isEmpty()) {
      return timeSlotsList;
    }
    for (int i = 0; i < timingList.size(); i++) {
      BasicDynaBean resourceAvailableBean = timingList.get(i);
      Long resourceFromTime = (Long) ((Time) resourceAvailableBean.get("from_time")).getTime();
      Long resourceToTime = (Long) ((Time) resourceAvailableBean.get("to_time")).getTime();
      Long remainderFromTime = resourceFromTime % slotDuration;
      if (!remainderFromTime.equals(0L)) {
        if (resourceFromTime < 0) {
          resourceFromTime = resourceFromTime - remainderFromTime;
        } else {
          resourceFromTime = resourceFromTime + (slotDuration - remainderFromTime);
        }
      }
      Long remainderToTime = resourceToTime % slotDuration;
      if (!remainderToTime.equals(0L)) {
        if (resourceToTime < 0) {
          resourceToTime = resourceToTime - (slotDuration + remainderToTime);
        } else {
          resourceToTime = resourceToTime - (remainderToTime);
        }
      }
      SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
      Time time = DateUtil.getCurrentTime();
      Long currentTime = sdf.parse(time.toString()).getTime();
      Date currentDate = DateUtil.getCurrentDate();
      boolean notToday = d1.getTime() != currentDate.getTime();
      if (notToday || (currentTime > resourceFromTime && currentTime < resourceToTime)
          || (currentTime < resourceFromTime && currentTime < resourceToTime)) {
        for (long j = resourceFromTime; j < resourceToTime; j = j + slotDuration) {
          if (notToday || j > currentTime) {
            Time startTime = new java.sql.Time(j);
            Time endTime = new java.sql.Time(startTime.getTime() + slotDuration);
            java.sql.Timestamp startTimestamp = DateUtil.timestampFromDateTime(d1, startTime);
            java.sql.Timestamp endTimestamp = DateUtil.timestampFromDateTime(d1, endTime);
            List<BasicDynaBean> appBean = appointmentService.getResourceAppointments(resourceId,
                category, startTimestamp, endTimestamp);
            if (appBean == null || appBean.size() < overbook + 1) {
              Time utc = DateUtil.parseIso8601Time("00:00:00Z");
              if (startTime.before(utc)) {
                Calendar cal1 = Calendar.getInstance();
                cal1.setTime(d1);
                cal1.add(Calendar.DAY_OF_MONTH, -1);
                Date date = new Date(cal1.getTimeInMillis());
                timeSlotsMap.put("timeslot",
                    DateUtil.formatIso8601Date(date) + "T" + DateUtil.formatIso8601Time(startTime));
              } else {
                timeSlotsMap.put("timeslot",
                    DateUtil.formatIso8601Date(d1) + "T" + DateUtil.formatIso8601Time(startTime));
              }
              timeSlotsMap.put("center_id", (Integer) resourceAvailableBean.get("center_id"));
              timeSlotsMap.put("visit_mode", (String)resourceAvailableBean.get("visit_mode"));
              timeSlotsList.add(timeSlotsMap);
              return timeSlotsList;
            }
          }

        }
      }
    }
    return timeSlotsList;

  }
  
  /**
   * Filter visit timings by visit mode.
   *
   * @param resourceAvailability the resource availability
   * @param visitMode the visit mode
   * @return the list
   */
  public List<BasicDynaBean> filterVisitTimingsByVisitMode(List<BasicDynaBean> resourceAvailability,
      String visitMode) {
    for (BasicDynaBean visitTiming : resourceAvailability) {
      if (visitTiming.get("availability_status").equals("A")) {
        String availabilityMode = (String) visitTiming.get("visit_mode");
        if (availabilityMode.equals(visitMode.toUpperCase()) || availabilityMode.equals("B")) {
          continue;
        }
        visitTiming.set("availability_status", "N");
      }
    }
    return resourceAvailability;
  }

  /**
   * Resource availibilites exists.
   *
   * @param resSchId the res sch id
   * @param currentDateTime the current date time
   * @param visitMode the visit mode
   * @return true, if successful
   */
  public boolean resourceAvailibilitesExists(String resSchId, 
       Timestamp currentDateTime, String visitMode) {
    boolean exists = resourceOverrideService.resourceOverrideExists(resSchId, 
        currentDateTime ,visitMode );
    if (exists) {
      return true;
    } else {
      return resAvailRepository.resourceAvailibilitesExists(resSchId, visitMode);
    }
  }
}
