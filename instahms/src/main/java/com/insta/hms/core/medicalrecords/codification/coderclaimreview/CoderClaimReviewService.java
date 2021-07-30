package com.insta.hms.core.medicalrecords.codification.coderclaimreview;

import static com.insta.hms.jobs.common.QuartzJobHelper.buildJob;

import com.bob.hms.common.RequestContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insta.hms.batchjob.builders.CoderReviewUpdateMessageJob;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.StringUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.clinical.outpatient.DoctorConsultationService;
import com.insta.hms.core.patient.registration.PatientRegistrationRepository;
import com.insta.hms.exception.HMSException;
import com.insta.hms.jobs.JobService;
import com.insta.hms.mdm.BeanConversionService;
import com.insta.hms.security.usermanager.RoleRepository;
import com.insta.hms.security.usermanager.UserRepository;
import com.insta.hms.security.usermanager.UserService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

/**
 * The Class CoderClaimReviewService.
 */
@Service
public class CoderClaimReviewService implements BeanConversionService {

  /** The codification message types service. */
  @LazyAutowired
  private CodificationMessageTypesService codificationMessageTypesService;

  /** The user service. */
  @LazyAutowired
  private UserService userService;

  /** The coder claim review repository. */
  @LazyAutowired
  private CoderClaimReviewRepository coderClaimReviewRepository;

  /** The patient registration repository. */
  @LazyAutowired
  private PatientRegistrationRepository patientRegistrationRepository;

  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;

  /** The coder ticket details service. */
  @LazyAutowired
  CoderTicketDetailsService coderTicketDetailsService;

  /** The coder ticket details repository. */
  @LazyAutowired
  CoderTicketDetailsRepository coderTicketDetailsRepository;

  /** The ticket recipients repository. */
  @LazyAutowired
  TicketRecipientsRepository ticketRecipientsRepository;

  /** The ticket comments repository. */
  @LazyAutowired
  TicketCommentsRepository ticketCommentsRepository;

  /** The ticket comments service. */
  @LazyAutowired
  TicketCommentsService ticketCommentsService;

  /** The doctor consultation service. */
  @LazyAutowired
  DoctorConsultationService doctorConsultationService;

  /** The gen pref service. */
  @LazyAutowired
  private GenericPreferencesService genPrefService;

  /** The coder claim review activity service. */
  @LazyAutowired
  private CoderClaimReviewActivityService coderClaimReviewActivityService;

  /** The role repository. */
  @LazyAutowired
  private RoleRepository roleRepository;

  /** The codification message types repository. */
  @LazyAutowired
  CodificationMessageTypesRepository codificationMessageTypesRepository;

  /** The user repository. */
  @LazyAutowired
  UserRepository userRepository;

  /** The coder claim review validator. */
  @LazyAutowired
  private CoderClaimReviewValidator coderClaimReviewValidator;

  /** The job service. */
  @LazyAutowired
  private JobService jobService;

  /** The message util. */
  @LazyAutowired
  private MessageUtil messageUtil;

  /** The Constant PATIENT_ID. */
  private static final String PATIENT_ID = "patient_id";

  /** The Constant ACTION_RIGHTS_MAP. */
  private static final String ACTION_RIGHTS_MAP = "actionRightsMap";

  /** The Constant ACTIVITY. */
  private static final String ACTIVITY = "activity";

  /** The Constant ASSIGNED_TO_ROLE. */
  private static final String ASSIGNED_TO_ROLE = "assigned_to_role";

  /** The Constant REVIEW_TYPE_ID. */
  private static final String REVIEW_TYPE_ID = "review_type_id";

  /** The Constant ASSIGNED_TO. */
  private static final String ASSIGNED_TO = "assignedto";

  /** The Constant TICKET_ID. */
  private static final String TICKET_ID = "ticket_id";

  /** The Constant USER_ID. */
  private static final String USER_ID = "user_id";

  /** The Constant URL_RIGHTS_MAP. */
  private static final String URL_RIGHTS_MAP = "urlRightsMap";

  /** The Constant USER_ID_SESSION. */
  private static final String USER_ID_SESSION = "userId";

  /** The Constant UPDATE_MRD. */
  private static final String UPDATE_MRD = "update_mrd";

  /** The Constant ASSIGNEE. */
  private static final String ASSIGNEE = "assignee";

  /** The Constant CHANGE_SET. */
  private static final String CHANGE_SET = "changeset";

  /** The Constant CREATED_SET. */
  private static final String CREATED_SET = "created_by";

  /** The Constant EMP_USERNAME. */
  private static final String EMP_USERNAME = "emp_username";

  /** The Constant NEW_VALUE. */
  private static final String NEW_VALUE = "new_value";

  /** The Constant OLD_VALUE. */
  private static final String OLD_VALUE = "old_value";

  /** The Constant REVIEW_TYPE. */
  private static final String REVIEW_TYPE = "review_type";

  /** The Constant ROLE_ID. */
  private static final String ROLE_ID = "roleId";

  /** The Constant ROLE_ID_DB. */
  private static final String ROLE_ID_DB = "role_id";

  /** The log. */
  private static Logger log = LoggerFactory
      .getLogger(CoderClaimReviewService.class);

  /** The Constant LESS_THAN_A_MONTH. */
  private static final Integer LESS_THAN_A_MONTH = -30;

  /** The Constant PORT_80. */
  private static final Integer PORT_80 = 80;

  /** The Constant PORT_447. */
  private static final Integer PORT_447 = 447;

  /**
   * Gets the adds the edit page data.
   *
   * @param parameters
   *          the parameters
   * @return the adds the edit page data
   */
  public Map<String, List<BasicDynaBean>> getAddEditPageData(
      Map<String, String[]> parameters) {
    Map<String, List<BasicDynaBean>> map = new HashMap<>();
    map.put("reviewTypeRoles",
        codificationMessageTypesService.getRoleForCenter());
    map.put("consultationDetails", null);
    map.put("patientDetails", null);
    map.put("ticketDetails", null);
    map.put("loggedInUser",
        (List<BasicDynaBean>) Arrays.asList(userService.getLoggedUser()));
    Integer patientCenterId = 0;
    if (parameters != null && parameters.containsKey(PATIENT_ID)) {
      String patientId = parameters.get(PATIENT_ID)[0];
      map.put("consultationDetails",
          doctorConsultationService.getConsultationDetails(patientId));
      List<BasicDynaBean> patientDetails = this.getPatientDetails(patientId);
      patientCenterId = (!patientDetails.isEmpty())
          ? (Integer) patientDetails.get(0).get("center_id") : 0;
      map.put("patientDetails", patientDetails);
    }
    map.put("users", userService.getUserWithDefaultCenter(patientCenterId));
    String keyColumn = coderClaimReviewRepository.getKeyColumn();
    Integer reviewTypeId = 0;
    if (parameters != null && parameters.containsKey(keyColumn)) {
      try {
        Integer ticketId = Integer.parseInt(parameters.get(keyColumn)[0]);
        List<BasicDynaBean> ticketsList = getTicketDetails(ticketId);
        if (ticketsList != null && !ticketsList.isEmpty()) {
          BasicDynaBean ticketDetails = ticketsList.get(0);
          reviewTypeId = (Integer) ticketDetails.get("review_type_id");          
        } 
        map.put("ticketDetails", ticketsList);
        map.put("commentsList",
            coderClaimReviewActivityService.getCommentsCount(ticketId));
      } catch (Exception ex) {
        throw new HMSException(HttpStatus.BAD_REQUEST, "exception.bad.request",
            null);
      }
    }
    if (reviewTypeId > 0) {
      map.put("messageTypeList",
          codificationMessageTypesService.getMessageTypesList(reviewTypeId));
    } else {
      map.put("messageTypeList",
          codificationMessageTypesService.getMessageTypesList());
    }
    BasicDynaBean genricPrefs = genPrefService.getPreferences();
    map.put("genricPrefs", (List<BasicDynaBean>) Arrays.asList(genricPrefs));

    return map;
  }

  /**
   * Gets the ticket details.
   *
   * @param ticketId
   *          the ticket id
   * @return the ticket details
   */
  public List<BasicDynaBean> getTicketDetails(Integer ticketId) {
    return coderClaimReviewRepository.getTicketDetails(ticketId);
  }

  /**
   * Gets the patient details.
   *
   * @param patientId
   *          the patient id
   * @return the patient details
   */
  public List<BasicDynaBean> getPatientDetails(String patientId) {
    return coderClaimReviewRepository.getPatientDetails(patientId);
  }

  /**
   * Gets the list.
   *
   * @param paramMap
   *          the param map
   * @return the list
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> getList(Map<String, String[]> paramMap) {
    String loggedInRoleIdStr = sessionService.getSessionAttributes()
        .get(ROLE_ID).toString();
    String codificationStatus = "";
    Map<String, Object> urlRightsMap = (Map<String, Object>) sessionService
        .getSessionAttributes(new String[] { URL_RIGHTS_MAP })
        .get(URL_RIGHTS_MAP);
    if (paramMap.containsKey(PATIENT_ID)) {
      codificationStatus = patientRegistrationRepository
          .getVisitCodificationStatus((String) paramMap.get(PATIENT_ID)[0]);
    }
    String loggedInUserIdStr = sessionService.getSessionAttributes()
        .get(USER_ID_SESSION).toString();
    Integer loggedInRoleId = (Integer) sessionService.getSessionAttributes()
        .get(ROLE_ID);
    // If Loggedin user role is other than super admin and Administrator.
    // then, if
    // accessForAllCoderReview is 'N' access, then show only related to his
    // role
    String reviewTypeFilter = "all";
    if (loggedInRoleId > 2) {
      if (paramMap.containsKey(ASSIGNED_TO_ROLE)
          || paramMap.containsKey(ASSIGNED_TO)
          || paramMap.containsKey(CREATED_SET)
          || paramMap.containsKey(REVIEW_TYPE_ID)) {
        if (paramMap.containsKey(ASSIGNED_TO_ROLE)) {
          paramMap.put(ASSIGNED_TO_ROLE, new String[] { loggedInRoleIdStr });
          reviewTypeFilter = ASSIGNED_TO_ROLE;
          paramMap.put("assigned_to_role@cast", new String[] { "y" });
        } else if (paramMap.containsKey(ASSIGNED_TO)) {
          paramMap.put(ASSIGNED_TO, new String[] { loggedInUserIdStr });
          reviewTypeFilter = ASSIGNED_TO;
        } else if (paramMap.containsKey(CREATED_SET)) {
          paramMap.put(CREATED_SET, new String[] { loggedInUserIdStr });
          reviewTypeFilter = CREATED_SET;
        }
      } else if (urlRightsMap.get(UPDATE_MRD).equals("A")) {
        paramMap.put(CREATED_SET, new String[] { loggedInUserIdStr });
        reviewTypeFilter = CREATED_SET;
      } else {
        paramMap.put(ASSIGNED_TO, new String[] { loggedInUserIdStr });
        reviewTypeFilter = ASSIGNED_TO;
      }
    }
    // show codification from past one month.
    String dateRange = (paramMap.containsKey("date_range"))
        ? paramMap.get("date_range")[0] : null;
    String monthStartDate = null;
    Map<String, Object> responseMap = new HashMap<>();
    if (dateRange != null && dateRange.equals("month")) {
      SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.DATE, LESS_THAN_A_MONTH);
      Date openDt = cal.getTime();
      monthStartDate = dateFormat.format(openDt);

      paramMap.put("created_at", new String[] { monthStartDate, "" });
      paramMap.put("created_at@op", new String[] { "ge,le" });
      paramMap.put("created_at@cast", new String[] { "y" });
      paramMap.remove("date_range");
    }
    PagedList pagedList = coderClaimReviewRepository.search(paramMap);
    responseMap.put("urlRightsMap", urlRightsMap);
    responseMap.put("codificationStatus", codificationStatus);
    responseMap.put("loggedInUserIdStr", loggedInUserIdStr);
    responseMap.put("loggedInRoleId", loggedInRoleId);
    BasicDynaBean userBean = userService.findByKey(EMP_USERNAME,
        loggedInUserIdStr);
    String doctorId = (String) userBean.get("doctor_id");
    responseMap.put("isDoctor", doctorId != null && !doctorId.isEmpty());
    responseMap.put("reviewTypeFilter", reviewTypeFilter);
    responseMap.put("pagedList", pagedList);
    responseMap.put("created_at", new String[] { monthStartDate, "" });

    return responseMap;
  }

  /**
   * Gets the activity map.
   *
   * @param params
   *          the params
   * @return the activity map
   */
  public List<List<Map>> getActivityMap(Map<String, String[]> params) {
    if (params != null && params.containsKey("id")) {
      List<BasicDynaBean> activity = coderClaimReviewActivityService
          .getCommentsAndActivity(Integer.parseInt(params.get("id")[0]));
      return getActivityMap(activity);
    }
    return Collections.emptyList();
  }

  /**
   * Gets the activity map.
   *
   * @param activityBeanList
   *          the activity bean list
   * @return the activity map
   */
  private List<List<Map>> getActivityMap(List<BasicDynaBean> activityBeanList) {
    List<List<Map>> activity = new ArrayList<List<Map>>();
    List<Map> changeSetList = null;
    int prevChangeset = 0;
    for (BasicDynaBean activityItemBean : activityBeanList) {
      if (prevChangeset != (Integer) activityItemBean.get(CHANGE_SET)) {
        if (changeSetList != null && !changeSetList.isEmpty()) {
          activity.add(changeSetList);
        }
        changeSetList = new ArrayList<Map>();
      }

      Map<String, Object> activityItemMap = new HashMap(
          activityItemBean.getMap());

      switch ((String) activityItemMap.get(ACTIVITY)) {
        case CoderClaimReviewActivityRepository.ACTIVITY_UPDATE_ROLE:
          activityItemMap.put(OLD_VALUE,
                roleRepository
                    .findByKey("role_id",
                        Integer
                            .parseInt((String) activityItemMap.get(OLD_VALUE)))
                    .get("role_name"));
          activityItemMap.put(NEW_VALUE,
                roleRepository
                    .findByKey("role_id",
                        Integer
                            .parseInt((String) activityItemMap.get(NEW_VALUE)))
                    .get("role_name"));
          break;
        case CoderClaimReviewActivityRepository.ACTIVITY_UPDATE_ASSIGNEE:
          BasicDynaBean oldUserBean = userRepository.findByKey(EMP_USERNAME,
              activityItemMap.get(OLD_VALUE));
          activityItemMap.put(OLD_VALUE,
              (oldUserBean != null) ? oldUserBean.get("temp_username") : null);
          BasicDynaBean newUserBean = userRepository.findByKey(EMP_USERNAME,
              activityItemMap.get(NEW_VALUE));
          activityItemMap.put(NEW_VALUE,
              (newUserBean != null) ? newUserBean.get("temp_username") : null);
          break;
        case CoderClaimReviewActivityRepository.ACT_UPDATE_MESSAGE_TYPE:
          activityItemMap
              .put(OLD_VALUE,
                codificationMessageTypesRepository
                    .findByKey(REVIEW_TYPE_ID,
                        Integer
                            .parseInt((String) activityItemMap.get(OLD_VALUE)))
                    .get(REVIEW_TYPE));
          activityItemMap
              .put(NEW_VALUE,
                codificationMessageTypesRepository
                    .findByKey(REVIEW_TYPE_ID,
                        Integer
                            .parseInt((String) activityItemMap.get(NEW_VALUE)))
                    .get(REVIEW_TYPE));

          break;

        case CoderClaimReviewActivityRepository.ACTIVITY_CREATE_REVIEW:
          try {
            JSONObject activityJsonObject = new JSONObject(
                (String) activityItemMap.get(NEW_VALUE));
            activityJsonObject
                .put("role",
                  roleRepository
                      .findByKey(ROLE_ID_DB,
                          Integer.parseInt(
                              activityJsonObject.getString("role_id")))
                      .get("role_name"));

            activityJsonObject.put(REVIEW_TYPE,
                codificationMessageTypesRepository
                  .findByKey(REVIEW_TYPE_ID,
                      Integer.parseInt(
                          activityJsonObject.getString(REVIEW_TYPE_ID)))
                  .get(REVIEW_TYPE));

            if (!StringUtil
                .isNullOrEmpty((String) activityJsonObject.get(ASSIGNEE))) {
              BasicDynaBean userBean = userRepository.findByKey(EMP_USERNAME,
                  activityJsonObject.get(ASSIGNEE));
              activityJsonObject.put(ASSIGNEE, userBean.get("temp_username"));
            }

            activityItemMap.put(NEW_VALUE, activityJsonObject.toString());
          } catch (JSONException exp) {
            activityItemMap.put(NEW_VALUE, "{}");
            log.info("Create activities exception message ::" + exp.getMessage());
          }
          break;
        default:
          break;
      }
      changeSetList.add(activityItemMap);
      prevChangeset = (Integer) activityItemBean.get(CHANGE_SET);
    }
    if (changeSetList != null && !changeSetList.isEmpty()) {
      activity.add(changeSetList);
    }

    return activity;
  }
  
  /**
   * Find by Primary Key.
   *
   * @param params
   *          the params
   * @return the basic dyna bean
   */
  public BasicDynaBean findByPk(Map params) {
    return coderClaimReviewRepository.findByPk(params);
  }

  /**
   * Creates the review.
   *
   * @param paramMap
   *          the param map
   * @return the map
   */
  @Transactional(rollbackFor = Exception.class)
  public Map<String, String> createReview(
      Map<String, Map<String, Object>> paramMap) {
    BasicDynaBean parentBean = coderClaimReviewRepository.getBean();
    String loggedInUserIdStr = sessionService.getSessionAttributes()
        .get(USER_ID_SESSION).toString();
    String keyColumn = coderClaimReviewRepository.getKeyColumn();
    Map<String, Object> urlRightsMap = (Map<String, Object>) sessionService
        .getSessionAttributes(new String[] { URL_RIGHTS_MAP })
        .get(URL_RIGHTS_MAP);

    parentBean.set(CREATED_SET, loggedInUserIdStr);
    parentBean.set("updated_by", loggedInUserIdStr);
    Map<String, Object> requestObject = paramMap.get("requestObject");
    ConversionUtils.copyToDynaBean(requestObject, parentBean);
    coderClaimReviewValidator.validateInsert(parentBean);
    Integer result = coderClaimReviewRepository.insert(parentBean);
    Map<String, String> responseMap = new HashMap<>();
    if (result > 0) {
      BasicDynaBean newTicketBean = coderClaimReviewRepository
          .getRecentInsertedId();
      Integer ticketId = (Integer) newTicketBean.get(keyColumn);

      coderTicketDetailsService
          .updateTicketDetails(paramMap.get("requestObject"), ticketId);

      BasicDynaBean activityBean = coderClaimReviewActivityService.getBean();
      activityBean.set(TICKET_ID, ticketId);
      activityBean.set(USER_ID, loggedInUserIdStr);
      activityBean.set(ACTIVITY,
          CoderClaimReviewActivityRepository.ACTIVITY_CREATE_REVIEW);

      JSONObject newReviewObject = new JSONObject();
      try {
        newReviewObject.put("body", requestObject.get("body"));
        newReviewObject.put("title", requestObject.get("title"));
        newReviewObject.put("createdBy", loggedInUserIdStr);
        newReviewObject.put("role_id", requestObject.get("role_id"));
        newReviewObject.put("status", requestObject.get("status"));
        newReviewObject.put(REVIEW_TYPE_ID, requestObject.get(REVIEW_TYPE_ID));
        newReviewObject.put(ASSIGNEE, requestObject.get("recipientsDropDown"));
      } catch (JSONException exp) {
        log.info("Unable to prepage JSON object. Exception message ::"
            + exp.getMessage());
      }

      activityBean.set(NEW_VALUE, newReviewObject.toString());
      activityBean.set(CHANGE_SET,
          DatabaseHelper.getNextSequence("review_activity_changeset"));

      coderClaimReviewActivityService.add(activityBean);

      responseMap.put("createStatus", "success");
      responseMap.put(TICKET_ID, String.valueOf(newTicketBean.get("id")));
      responseMap.put(PATIENT_ID, parentBean.get(PATIENT_ID).toString());
      BasicDynaBean reviewCategory = codificationMessageTypesService
          .getReviewCategoryDetails(
              Integer.parseInt(requestObject.get(REVIEW_TYPE_ID).toString()));
      String recipient = (String) requestObject.get("recipientsDropDown");
      String reviewCategoryEmailStatus = "";
      reviewCategoryEmailStatus = (reviewCategory != null)
          ? reviewCategory.get("send_email").toString() : "N";
      if (!StringUtil.isNullOrEmpty(recipient)
          && urlRightsMap.get(UPDATE_MRD).equals("A")
          && reviewCategoryEmailStatus.equals("Y")) {
        Map<String, String> params = new HashMap<>();
        params.put("id", newTicketBean.get("id").toString());
        params.put(PATIENT_ID, (String) parentBean.get(PATIENT_ID));
        sendNotificationEmail(activityBean, recipient, params);
      }
    } else {
      responseMap.put("createStatus", "falied");
    }
    return responseMap;
  }

  /**
   * Gets the review Url.
   *
   * @param ticketId
   *          the ticket id
   * @param patientId
   *          the patient id
   * @return the review URL
   */
  private String getReviewUrl(String ticketId, String patientId) {

    if (ticketId == null || patientId == null) {
      return null;
    }

    StringBuilder builder = new StringBuilder();
    HttpServletRequest request = RequestContext.getHttpRequest();
    String domain = null;
    try {
      domain = new URL(request.getRequestURL().toString()).getHost();
    } catch (MalformedURLException exp) {
      log.info("Unable to get domain name ::" + exp.getMessage());
    }
    builder.append(request.getScheme()).append("://").append(domain);
    int port = request.getServerPort();
    if (port != PORT_80 && port != PORT_447) {
      builder.append(":").append(port);
    }
    builder.append(request.getContextPath()).append("/coderreviews/show.htm?&id=").append(ticketId)
        .append("&patient_id=").append(patientId);
    return builder.toString();
  }

  /**
   * Send notification email.
   *
   * @param activityBean
   *          the activity bean
   * @param recipient
   *          the recipient
   * @param params
   *          the params
   */
  private void sendNotificationEmail(BasicDynaBean activityBean,
      String recipient, Map<String, String> params) {
    Set<String> recipients = new HashSet<>();
    recipients.add(recipient);
    try {
      sendNotificationEmail(activityBean, recipients, params);
    } catch (Exception exp) {
      log.info("Unable to send mail. Exception message ::" + exp.getMessage());
    }
  }

  /**
   * Send notification email.
   *
   * @param activityBean
   *          the activity bean
   * @param recipients
   *          the recipients
   * @param params
   *          the params
   */
  private void sendNotificationEmail(BasicDynaBean activityBean,
      Set<String> recipients, Map<String, String> params) {
    List<BasicDynaBean> activityBeans = new ArrayList<>();
    if (activityBeans.isEmpty()) {
      activityBeans.add(activityBean);
    }
    try {
      sendNotificationEmail(activityBeans, recipients, params);
    } catch (Exception exp) {
      log.info("Unable to send email. Exception message ::" + exp.getMessage());
    }
  }

  /**
   * Send notification email.
   *
   * @param activityBeans
   *          the activity beans
   * @param recipients
   *          the recipients
   * @param params
   *          the params
   */
  @SuppressWarnings("unchecked")
  private void sendNotificationEmail(List<BasicDynaBean> activityBeans,
      Set<String> recipients, Map<String, String> params) {
    if (activityBeans.isEmpty()) {
      return;
    }
    List<Map> activityMaps = getActivityMap(activityBeans).get(0);
    for (String recipient : recipients) {

      BasicDynaBean recipientBean = userService.findByKey(EMP_USERNAME,
          recipient);

      if (recipientBean != null && !StringUtil
          .isNullOrEmpty((String) recipientBean.get("email_id"))) {
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("receipient_id__", recipient);
        messageData.put("receipient_type__", "");
        messageData.put("recipient_name",
            (String) recipientBean.get("temp_username"));

        List<Map> activityData = new ArrayList<Map>();

        for (Map activityMap : activityMaps) {
          if ((String) activityMap.get(
              ACTIVITY) == CoderClaimReviewActivityRepository.ACTIVITY_CREATE_REVIEW) {
            ObjectMapper mapper = new ObjectMapper();
            Map createActivityData;
            try {
              createActivityData = mapper.readValue(
                  new ByteArrayInputStream(
                      activityMap.get(NEW_VALUE).toString().getBytes()),
                  Map.class);
              createActivityData.put(ACTIVITY,
                  CoderClaimReviewActivityRepository.ACTIVITY_CREATE_REVIEW);
              activityData.add(createActivityData);
            } catch (IOException exp) {
              log.info("Unable to save activity. Exception message ::"
                  + exp.getMessage());
            }
          } else {
            activityData.add(activityMap);
          }
        }

        Map firstActivity = activityMaps.get(0);
        messageData.put("change_by", firstActivity.get(USER_ID));
        messageData.put("change_at", firstActivity.get("change_at"));
        messageData.put("activity_data", activityData);
        messageData.put("recipient_email",
            (String) recipientBean.get("email_id"));
        messageData.put("link_to_ticket", getReviewUrl(
            (String) params.get("id"), (String) params.get(PATIENT_ID)));

        Map<String, Object> jobData = new HashMap<>();
        jobData.put("eventData", messageData);
        jobData.put("userName", sessionService.getSessionAttributes()
            .get(USER_ID_SESSION).toString());
        jobData.put("schema", RequestContext.getSchema());
        jobData.put("eventId", "coder_review_update");
        jobService.scheduleImmediate(buildJob(
            "CoderReviewUpdate_" + recipient + '_'
                + firstActivity.get(TICKET_ID) + '_'
                + firstActivity.get(CHANGE_SET),
            CoderReviewUpdateMessageJob.class, jobData));
      } else {
        log.info("Email ID not available for user " + recipient
            + ". Skipping email.");
      }
    }

  }

  /**
   * Update review.
   *
   * @param paramMap
   *          the param map
   * @return the map
   */
  @SuppressWarnings("unchecked")
  @Transactional
  public Map<String, String> updateReview(Map<String, String[]> paramMap) {
    Map<String, Object> reqFlattenMap = ConversionUtils.flatten(paramMap);

    Map<String, String[]> requestObject = paramMap;
    String keyColumn = coderClaimReviewRepository.getKeyColumn();
    BasicDynaBean parentBean = this.toBean(requestObject);
    String loggedInUserIdStr = sessionService.getSessionAttributes()
        .get(USER_ID_SESSION).toString();
    Integer ticketId = Integer.parseInt((String) reqFlattenMap.get(TICKET_ID));

    parentBean.set("updated_by", loggedInUserIdStr);
    // getting current time
    Calendar calendar = Calendar.getInstance();
    java.util.Date now = calendar.getTime();
    java.sql.Timestamp currentTime = new java.sql.Timestamp(now.getTime());
    parentBean.set("updated_at", currentTime);

    Map<String, Object> column = new HashMap<>();
    column.put(keyColumn,
        Integer.parseInt((String) reqFlattenMap.get(keyColumn)));

    Map<String, String> responseMap = new HashMap<>();
    Set<String> recipientList = new HashSet<>();
    Map<String, Object[]> changeMap = null;
    BasicDynaBean oldReview = null;
    BasicDynaBean newReview = null;
    List<BasicDynaBean> activityBeanList = Collections.EMPTY_LIST;
    if (requestObject.containsKey("updateButton") 
        || (requestObject.containsKey("commentSubmit") 
            && "changed".equals(requestObject.get("review_type_changed")[0]))) {

      oldReview = coderClaimReviewRepository.findByKey(column);
      coderClaimReviewValidator.validateUpdate(parentBean);
      coderClaimReviewRepository.update(parentBean, column);
      newReview = coderClaimReviewRepository.findByKey(column);

      changeMap = getChangeMap(oldReview, newReview);

      BasicDynaBean oldReviewDetails = coderTicketDetailsRepository
          .findByKey(TICKET_ID, ticketId);
      BasicDynaBean oldRecipientDetails = ticketRecipientsRepository
          .findByKey(TICKET_ID, ticketId);
      coderTicketDetailsService.updateTicketDetails(reqFlattenMap, ticketId);
      BasicDynaBean newReviewDetails = coderTicketDetailsRepository
          .findByKey(TICKET_ID, ticketId);
      BasicDynaBean newRecipientDetails = ticketRecipientsRepository
          .findByKey(TICKET_ID, ticketId);

      changeMap.putAll(getChangeMap(oldReviewDetails, newReviewDetails));
      changeMap.putAll(getChangeMap(oldRecipientDetails, newRecipientDetails));

      if (oldRecipientDetails != null) {
        recipientList.add((String) oldRecipientDetails.get(USER_ID));
      }
      if (newRecipientDetails != null) {
        recipientList.add((String) newRecipientDetails.get(USER_ID));
      }
      responseMap.put("updateStatus", "ticketUpdated");
      activityBeanList = processChangeMap(changeMap,
          (Integer) ticketId);
      for (BasicDynaBean activityBean : activityBeanList) {
        coderClaimReviewActivityService.add(activityBean);
      }
      
    } 
    if (requestObject.containsKey("commentSubmit")) {
      
      Map<String, Object[]> commentsChangeMap = getChangeMap(null, null);
      commentsChangeMap.put("comment",
          new String[] { null, (String) reqFlattenMap.get("commentText") });
      if (ticketRecipientsRepository.findByKey(TICKET_ID, ticketId) != null) {
        recipientList.add((String) ticketRecipientsRepository
            .findByKey(TICKET_ID, ticketId).get(USER_ID));
      }
      responseMap.put("updateStatus", "commentInserted");
      activityBeanList = processChangeMap(commentsChangeMap,
          (Integer) ticketId);
      for (BasicDynaBean activityBean : activityBeanList) {
        coderClaimReviewActivityService.add(activityBean);
      }
      Map<String, Object> ticketKey = new HashMap<>();
      ticketKey.put("id", ticketId);
      BasicDynaBean coderClaimReviewBean = coderClaimReviewRepository
          .findByKey(ticketKey);
      if (coderClaimReviewBean != null) {
        String status = (String) coderClaimReviewBean.get("status");
        Map<String, Object> urlRightsMaps = (Map<String, Object>) sessionService
            .getSessionAttributes(new String[] { "urlRightsMap" }).get("urlRightsMap");

        if (status.equalsIgnoreCase("open") 
            && !"A".equals(urlRightsMaps.get("update_mrd"))) {
          coderClaimReviewBean.set("status", "inprogress");
        }
        if (status.equalsIgnoreCase("inprogress") 
            && "A".equals(urlRightsMaps.get("update_mrd"))) {
          coderClaimReviewBean.set("status", "open");
        }
        coderClaimReviewRepository.update(coderClaimReviewBean, ticketKey);
      }
    }
    
    BasicDynaBean reviewDetails = coderTicketDetailsRepository
        .findByKey(TICKET_ID, ticketId);
    BasicDynaBean reviewCategory = codificationMessageTypesService
        .getReviewCategoryDetails((Integer) reviewDetails.get(REVIEW_TYPE_ID));
    String reviewCategoryEmailStatus = "";
    reviewCategoryEmailStatus = (reviewCategory != null)
        ? reviewCategory.get("send_email").toString() : "N";
    if (!recipientList.isEmpty() && reviewCategoryEmailStatus.equals("Y")) {
      Map<String, String> params = new HashMap<>();
      params.put("id", (String) reqFlattenMap.get("id"));
      params.put(PATIENT_ID, (String) reqFlattenMap.get(PATIENT_ID));
      sendNotificationEmail(activityBeanList, recipientList, params);
    }

    responseMap.put(TICKET_ID, (String) reqFlattenMap.get("id"));
    responseMap.put(PATIENT_ID, (String) reqFlattenMap.get(PATIENT_ID));

    return responseMap;
  }

  /**
   * convert request params to bean.
   *
   * @param requestParams
   *          the request params
   * @return the basic dyna bean
   * @see com.insta.hms.mdm.BeanConversionService#toBean(java.util.Map)
   */
  public BasicDynaBean toBean(Map<String, String[]> requestParams) {
    return toBean(requestParams, null);
  }

  /**
   * convert to Bean.
   * 
   * @see com.insta.hms.mdm.BeanConversionService#toBean(java.util.Map,
   *      java.util.Map)
   */
  public BasicDynaBean toBean(Map<String, String[]> requestParams,
      Map<String, MultipartFile> fileMap) {
    List<String> errorFields = new ArrayList<>();
    Map<String, Object> multipartReqParams = new HashMap<String, Object>(
        requestParams);
    if (fileMap != null && !fileMap.isEmpty()) {
      multipartReqParams.putAll(fileMap);
    }
    BasicDynaBean bean = coderClaimReviewRepository.getBean();
    ConversionUtils.copyToDynaBean(multipartReqParams, bean, errorFields);
    return bean;
  }

  /**
   * To bean.
   *
   * @param requestBody
   *          the request body
   * @return the basic dyna bean
   */
  public BasicDynaBean toBean(ModelMap requestBody) {
    BasicDynaBean bean = coderClaimReviewRepository.getBean();
    List<String> errorFields = new ArrayList<>();
    ConversionUtils.copyJsonToDynaBean(requestBody, bean, errorFields, true);
    return bean;
  }

  /**
   * (non-Javadoc)
   * 
   * @see com.insta.hms.mdm.BeanConversionService#toBeanList(java.util.Map,
   *      org.apache.commons.beanutils.BasicDynaBean)
   */
  public Map<String, List<BasicDynaBean>> toBeanList(
      Map<String, String[]> requestParams, BasicDynaBean type) {
    return null;
  }

  /**
   * Checks if the logged in user is authorized.
   *
   * @param id
   *          the id
   * @return true, if is authorized
   */
  @SuppressWarnings("unchecked")
  public boolean isAuthorized(int id) {
    List<BasicDynaBean> ticketList = coderClaimReviewRepository
        .getTicketDetails(id);
    if (ticketList.isEmpty()) {
      return false;
    }
    BasicDynaBean ticket = ticketList.get(0);
    Map<String, Object> actionRightsMap = null;
    int loggedInRoleId = (int) sessionService.getSessionAttributes()
        .get(ROLE_ID);

    actionRightsMap = (Map<String, Object>) sessionService
        .getSessionAttributes(new String[] { ACTION_RIGHTS_MAP })
        .get(ACTION_RIGHTS_MAP);
    String accessForAllCoders = (String) actionRightsMap
        .get("access_for_all_coder_reviews");
    Map<String, Object> urlRightsMap = (Map<String, Object>) sessionService
        .getSessionAttributes(new String[] { URL_RIGHTS_MAP })
        .get(URL_RIGHTS_MAP);
    BasicDynaBean categoryCountBean = null;
    int ticketCategoryId = (int) ticket.get("category_id");
    categoryCountBean = coderClaimReviewRepository
        .getCategoriesCountForRoleId(ticketCategoryId, loggedInRoleId);
    Boolean isCategoryAllowed = false;
    if (categoryCountBean != null) {
      isCategoryAllowed = (Integer.parseInt(
          (String) categoryCountBean.get("categories_count").toString()) > 0)
              ? true : false;
    }
    String loggedInUserIdStr = sessionService.getSessionAttributes()
        .get(USER_ID_SESSION).toString();
    BasicDynaBean userBean = userService.findByKey(EMP_USERNAME,
        loggedInUserIdStr);
    String doctorId = (String) userBean.get("doctor_id");
    // it should match for one of the following conditions
    // 1. loggedIn user role user may be 1 or 2 or
    // logged role may have 'allow access for all coders' preference
    // selected 'Y'
    // 2. loggedIn user is Doctor, Then he must be recipientUser/assignedTo
    // 3. loggedIn user must be same as recipientUser/assignedTo
    // 4. loggedIn user must be a coder
    // else it will fails
    String assignedTo = (String) ticket.get(ASSIGNED_TO);
    int ticketRoleId = (int) ticket.get(ASSIGNED_TO_ROLE);
    return ((loggedInRoleId < 2
        || (accessForAllCoders != null && accessForAllCoders.equals("Y")))
        || (doctorId != null && !doctorId.isEmpty() && null != assignedTo
            && loggedInUserIdStr.equals(assignedTo))
        || (null != assignedTo && loggedInUserIdStr.equals(assignedTo))
        || (null == assignedTo && loggedInRoleId == ticketRoleId)
        || (urlRightsMap.get(UPDATE_MRD).equals("A")) || isCategoryAllowed);

  }

  /**
   * Gets the change map.
   *
   * @param oldBean
   *          the old bean
   * @param newBean
   *          the new bean
   * @return the change map
   */
  public Map<String, Object[]> getChangeMap(BasicDynaBean oldBean,
      BasicDynaBean newBean) {
    Map<String, Object> oldBeanMap;
    Map<String, Object> newBeanMap;

    if (oldBean != null) {
      oldBeanMap = oldBean.getMap();
    } else {
      oldBeanMap = new HashMap<String, Object>();
    }

    if (newBean != null) {
      newBeanMap = newBean.getMap();
    } else {
      newBeanMap = new HashMap<>();
    }

    Map<String, Object[]> changeMap = new HashMap<>();

    Set<String> keys = new HashSet<String>(oldBeanMap.keySet());
    keys.addAll(newBeanMap.keySet());

    for (String key : keys) {
      if (oldBeanMap.get(key) != null
          && !oldBeanMap.get(key).equals(newBeanMap.get(key))
          || newBeanMap.get(key) != null
              && !newBeanMap.get(key).equals(oldBeanMap.get(key))) {
        Object[] params = new Object[] { oldBeanMap.get(key),
            newBeanMap.get(key) };
        changeMap.put(key, params);
      }
    }
    return changeMap;
  }

  /**
   * Process change map.
   *
   * @param changeMap
   *          the change map
   * @param ticketId
   *          the ticket id
   * @return the list
   */
  public List<BasicDynaBean> processChangeMap(Map<String, Object[]> changeMap,
      Integer ticketId) {
    Integer changeset = DatabaseHelper
        .getNextSequence("review_activity_changeset");
    List<BasicDynaBean> changeList = new ArrayList<>();
    for (Entry<String, Object[]> entry : changeMap.entrySet()) {
      String key = entry.getKey();
      BasicDynaBean activityBean = coderClaimReviewActivityService.getBean();
      String activity = null;
      switch (key) {
        case "title":
          activity = CoderClaimReviewActivityRepository.ACTIVITY_UPDATE_TITLE;
          break;
        case "body":
          activity = CoderClaimReviewActivityRepository.ACTIVITY_UPDATE_BODY;
          break;
        case "status":
          activity = CoderClaimReviewActivityRepository.ACTIVITY_UPDATE_STATUS;
          break;
        case REVIEW_TYPE_ID:
          activity = CoderClaimReviewActivityRepository.ACT_UPDATE_MESSAGE_TYPE;
          break;
        case ASSIGNED_TO_ROLE:
          activity = CoderClaimReviewActivityRepository.ACTIVITY_UPDATE_ROLE;
          break;
        case "comment":
          activity = CoderClaimReviewActivityRepository.ACTIVITY_COMMENT;
          break;
        case USER_ID:
          activity = CoderClaimReviewActivityRepository.ACTIVITY_UPDATE_ASSIGNEE;
          break;
        default:
          break;
      }

      if (activity == null) {
        continue;
      }

      activityBean.set(OLD_VALUE, changeMap.get(key)[0] != null
          ? changeMap.get(key)[0].toString() : null);
      activityBean.set(NEW_VALUE, changeMap.get(key)[1] != null
          ? changeMap.get(key)[1].toString() : null);
      activityBean.set(USER_ID, sessionService.getSessionAttributes()
          .get(USER_ID_SESSION).toString());
      activityBean.set(CHANGE_SET, changeset);
      activityBean.set(TICKET_ID, ticketId);
      activityBean.set(ACTIVITY, activity);

      changeList.add(activityBean);
    }
    return changeList;
  }

  /**
   * Get codification status.
   * 
   * @param patientId 
   *          patient id
   * @return String 
   *          codification status
   */
  public String getCodificationStatus(String patientId) {
    BasicDynaBean bean = coderClaimReviewRepository.getCodificationStatus(patientId);
    if (bean != null) {
      return bean.get("codification_status").toString();
    } else {
      return null;
    }
  }
}
