package com.insta.hms.integration.insurance.erxprescription;

import static com.insta.hms.jobs.common.QuartzJobHelper.buildJob;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.CancelERxJob;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.PushService;
import com.insta.hms.common.SendErxJob;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.clinical.consultation.prescriptions.PrescriptionIntegratorServie;
import com.insta.hms.core.clinical.consultation.prescriptions.PrescriptionsService;
import com.insta.hms.core.clinical.forms.FormParameter;
import com.insta.hms.core.clinical.outpatient.DoctorConsultationService;
import com.insta.hms.erxprescription.ERxRequestSender;
import com.insta.hms.erxprescription.ERxResponse;
import com.insta.hms.erxprescription.ERxResponseProcessor;
import com.insta.hms.erxprescription.ERxWebService;
import com.insta.hms.eservice.EResponseProcessor;
import com.insta.hms.eservice.EResult;
import com.insta.hms.integration.insurance.pbm.PBMPrescriptionsService;
import com.insta.hms.integration.insurance.pbmauthorization.PBMService;
import com.insta.hms.jobs.JobService;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.mdm.doctors.DoctorService;
import com.insta.hms.mdm.formcomponents.FormComponentsService;
import com.insta.hms.mdm.insurancecompanies.InsuranceCompanyService;
import com.insta.hms.pbmauthorization.PBMPrescriptionHelper;
import com.insta.hms.redis.RedisMessagePublisher;
import com.insta.hms.util.MapWrapper;
import com.sun.xml.ws.client.ClientTransportException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.xml.ws.WebServiceException;

// TODO: Auto-generated Javadoc
/**
 * The Class ERxService.
 */
@Service
public class ERxService extends PrescriptionIntegratorServie {

  /** The log. */
  Logger log = LoggerFactory.getLogger(ERxService.class);

  /** The bean factory. */
  @Autowired
  private BeanFactory beanFactory;

  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;

  /** The pbm prescriptions service. */
  @LazyAutowired
  private PBMPrescriptionsService pbmPrescriptionsService;

  /** The doctor service. */
  @LazyAutowired
  private DoctorService doctorService;

  /** The erx request generator. */
  @LazyAutowired
  private ERxRequestGenerator erxRequestGenerator;

  /** The prescriptions service. */
  @LazyAutowired
  private PrescriptionsService prescriptionsService;

  /** The generic preferences service. */
  @LazyAutowired
  private GenericPreferencesService genericPreferencesService;

  /** The center service. */
  @LazyAutowired
  private CenterService centerService;

  /** The insurance company service. */
  @LazyAutowired
  private InsuranceCompanyService insuranceCompanyService;

  /** The doctor consultation service. */
  @LazyAutowired
  private DoctorConsultationService doctorConsultationService;

  /** The message util. */
  @LazyAutowired
  private MessageUtil messageUtil;

  /** The pbm service. */
  @LazyAutowired
  private PBMService pbmService;

  /** The erx response repository. */
  @LazyAutowired
  private ERxResponseRepository erxResponseRepository;

  /** The pbmhelper. */
  PBMPrescriptionHelper pbmhelper = new PBMPrescriptionHelper();

  /** redisKeyAliveTime. */
  private static final int REDIS_KEY_ALIVE_TIME = 12;

  /** The job service. */
  @LazyAutowired
  private JobService jobService;

  /** The redis template. */
  @LazyAutowired
  private RedisTemplate<String, Object> redisTemplate;

  /** Redis key template. for sending JOb */
  public static String redisKeyTemplateForSendingERx = "schema:%s;visitId:%s;type:sendERx";

  public static String redisKeyTemplateForCancellingERx = "schema:%s;visitId:%s;type:cancelERx";

  /** Redis value template. */
  private String redisValueTemplate = "status:%s;startedAt:%s;completedAt:%s;message:%s";

  @LazyAutowired
  private PushService pushService;

  @LazyAutowired
  private RedisMessagePublisher redisMessagePublisher;

  private static final String WEB_SOCKET_PUSH_CHANNEL = "/topic/erx";

  private static final String ERX_IN_PROGRESS = ERxStatus.IN_PROGRESS.getStatus();

  private static final String ERX_FAILED = ERxStatus.FAILED.getStatus();

  private static final String ERX_SUCCESS = ERxStatus.SUCCESS.getStatus();

  /**
   * Send Erx request.
   *
   * @param id is consultationId
   * @param patientId is the patientId
   * @param centerId is the centerId
   * @param userId is the userId
   * @param path is the contextPath
   * @param pbmPrescId is the prescriptionId
   * @param xml is the XML file generated
   */
  public void sendERxRequest(Object id, String patientId, Integer centerId, String userId,
      String path, Integer pbmPrescId, String xml, String schema) {

    String message = null;
    Map<String, Object> erxResponseMap = null;
    String requestType = "eRxRequest";
    BasicDynaBean hospitalCenterBean = centerService.getDhpoInfoCenterWise(patientId);

    if (hospitalCenterBean == null || hospitalCenterBean.get("dhpo_facility_user_id") == null
        || "".equals(hospitalCenterBean.get("dhpo_facility_user_id"))) {

      message = messageUtil.getMessage("exception.dhpo.facility.user");
      erxResponseMap = getResponse(message, false);
      this.publishERxResponseToRedisPublisher(erxResponseMap, patientId, requestType, schema,
          userId, id, ERX_FAILED, message, redisKeyTemplateForSendingERx);
      return;
    }

    String dhpoFacilityUser = (String) hospitalCenterBean.get("dhpo_facility_user_id");
    String dhpoFacilityPassword = (String) hospitalCenterBean.get("dhpo_facility_password");

    BasicDynaBean doctorBean = doctorService.getDoctorByConsId(id);

    String dhpoClinicianUser = (String) doctorBean.get("dhpo_clinician_user_id");
    String dhpoClinicianPassword = (String) doctorBean.get("dhpo_clinician_password");

    try {
      new ERxWebService().getRemoteService();
    } catch (Exception ex) {
      message = messageUtil.getMessage("exception.dhpo.server.con.error");
      if (ex instanceof SocketException) {
        erxResponseMap = getResponse(message + ex.getMessage(), false);
        this.publishERxResponseToRedisPublisher(erxResponseMap, patientId, requestType, schema,
            userId, id, ERX_FAILED, message, redisKeyTemplateForSendingERx);
        return;
      }
      erxResponseMap = getResponse(message + ex.getMessage(), false);
      this.publishERxResponseToRedisPublisher(erxResponseMap, patientId, requestType, schema,
          userId, id, ERX_FAILED, message, redisKeyTemplateForSendingERx);
      return;
    }

    ERxRequestSender sender = new ERxRequestSender(dhpoFacilityUser, dhpoFacilityPassword,
        dhpoClinicianUser, dhpoClinicianPassword);

    Map<String, Object> filterMap = new HashMap<String, Object>();
    filterMap.put("pbm_presc_id", pbmPrescId);
    BasicDynaBean pbmPresBean = pbmPrescriptionsService.findByKey(filterMap);

    // Uploads ERx Request and returns the ERx Response
    String xmlFileName =
        pbmPresBean.get("erx_file_name") != null ? (String) pbmPresBean.get("erx_file_name")
            : "TEST.XML";
    ERxResponse erxResponse = null;
    try {
      erxResponse = sender.sendErxRequest(xml, xmlFileName);
    } catch (ClientTransportException clientTransportException) {
      message = "Failed to submit eRx Request : Connection to eClaimLink timed out";
      log.error(message, clientTransportException);
      erxResponseMap = getResponse(message, false);
      this.publishERxResponseToRedisPublisher(erxResponseMap, patientId, requestType, schema,
          userId, id, ERX_FAILED, message, redisKeyTemplateForSendingERx);
      return;
    } catch (SocketTimeoutException socketTimeoutException) {
      message = "Failed to submit eRx Request : Connection to eClaimLink timed out";
      log.error(message, socketTimeoutException);
      erxResponseMap = getResponse(message, false);
      this.publishERxResponseToRedisPublisher(erxResponseMap, patientId, requestType, schema,
          userId, id, ERX_FAILED, message, redisKeyTemplateForSendingERx);
      return;
    } catch (WebServiceException webServiceException) {
      message = "Failed to submit eRx Request: Error in connection.";
      log.error(message, webServiceException);
      erxResponseMap = getResponse(message, false);
      this.publishERxResponseToRedisPublisher(erxResponseMap, patientId, requestType, schema,
          userId, id, ERX_FAILED, message, redisKeyTemplateForSendingERx);
      return;
    } catch (Exception exception) {
      if (exception instanceof SocketException) {
        message = messageUtil.getMessage("exception.dhpo.server.con.error");
        log.error(message, exception);
        erxResponseMap = getResponse(message + exception.getMessage(), false);
        this.publishERxResponseToRedisPublisher(erxResponseMap, patientId, requestType, schema,
            userId, id, ERX_FAILED, message, redisKeyTemplateForSendingERx);
        return;
      }
      message = "Failed to submit eRx Request";
      log.error(message, exception);
      erxResponseMap = getResponse(exception.getMessage(), false);
      this.publishERxResponseToRedisPublisher(erxResponseMap, patientId, requestType, schema,
          userId, id, ERX_FAILED, message, redisKeyTemplateForSendingERx);
      return;
    }

    if (erxResponse != null && erxResponse.isError()) {
      String errorMessage = erxResponse.getErrorMessage();
      Object errorReport = erxResponse.getErrorReport();

      // From PBM, we have known that the error report would be a excel file.
      // Hence, here we are not encoding and decoding error report as a file for user
      // to take
      // action.
      // Instead, read the first file entry and write to the browser directly
      // (or) show the error message if any.
      // While framework changes are done in PBM, need to follow the same.

      if (errorReport != null) {
        EResponseProcessor.CsvStreamProcessor errProcessor =
            new EResponseProcessor.CsvStreamProcessor();

        // saves the sent error file from erx webservice to database.
        String msg = saveErrorFileAndReturnErrorResponse(erxResponse, errProcessor, xmlFileName);

        if (msg != null) {
          String[] msgArray = msg.split(",");
          msg = String.join(" ",Arrays.copyOfRange(msgArray, 8, msgArray.length));
          erxResponseMap = getResponse(msg.trim(), false);
          this.publishERxResponseToRedisPublisher(erxResponseMap, patientId, requestType, schema,
              userId, id, ERX_FAILED, message, redisKeyTemplateForSendingERx);
          return;

        } else {
          message = messageUtil.getMessage("exception.failed.to.save.errorfile");
          erxResponseMap = getResponse(message, false);
          this.publishERxResponseToRedisPublisher(erxResponseMap, patientId, requestType, schema,
              userId, id, ERX_FAILED, message, redisKeyTemplateForSendingERx);
          return;
        }

      } else if (errorMessage != null && !errorMessage.equals("")) {
        erxResponseMap = getResponse(errorMessage, false);
        this.publishERxResponseToRedisPublisher(erxResponseMap, patientId, requestType, schema,
            userId, id, ERX_FAILED, errorMessage, redisKeyTemplateForSendingERx);
        return;
      }
    }

    // we use this for responses which return a bunch of simple properties
    // as out parameters
    EResponseProcessor.SimpleParameterProcessor processor =
        new ERxResponseProcessor.SimpleParameterProcessor(new String[] {"eRxReferenceNo"});

    EResult result = null;
    try {
      if (erxResponse != null) {
        result = processor.process(erxResponse);
      }
    } catch (IOException exception) {
      log.error("", exception);
      erxResponseMap = getResponse(exception.getMessage(), false);
      this.publishERxResponseToRedisPublisher(erxResponseMap, patientId, requestType, schema,
          userId, id, ERX_FAILED, message, redisKeyTemplateForSendingERx);
      return;
    }
    if (result != null && result instanceof MapWrapper) {
      log.info("Result retrieved: result is a map");
      MapWrapper resultObj = (MapWrapper) result;
      resultObj.put("pbm_presc_id", pbmPrescId);

      // save the eRxReference no. to the database
      if (!pbmPrescriptionsService.saveResponse(resultObj.getMap())) {
        message = messageUtil.getMessage("exception.save.response.error");
        erxResponseMap =
            getResponse(messageUtil.getMessage("exception.save.response.error"), false);
        this.publishERxResponseToRedisPublisher(erxResponseMap, patientId, requestType, schema,
            userId, id, ERX_FAILED, message, redisKeyTemplateForSendingERx);
        return;
      }
    }
    pbmService.updatePBMPrescriptionId(id, pbmPrescId);
    message = messageUtil.getMessage("msg.erx.request.success");
    BasicDynaBean erxdetailsBean = pbmPrescriptionsService.getConsErxDetails(pbmPrescId);
    erxResponseMap = getResponse(message, true);
    erxResponseMap.put("details", erxdetailsBean.getMap());
    this.publishERxResponseToRedisPublisher(erxResponseMap, patientId, requestType, schema, userId,
        id, ERX_SUCCESS, message, redisKeyTemplateForSendingERx);
    return;
  }

  /**
   * Cancel Erx request.
   *
   * @param patientId the patientId being consulted
   * @param id is the consultationId
   * @param path is the Request context path
   * @param xml is the file generated
   */
  public void cancelERxRequest(String patientId, Object id, Integer pbmPrescId, String path,
      String xml, String userId, String schema, int centerId) {

    String message = null;
    Map<String, Object> erxResponseMap = null;
    String requestType = "eRxCancellation";
    // used in case if erx cancel fails then need to revert
    // pbm_prescription to sent status
    // else cancel button will be disabled in front-end
    String erxRequestSentType = "eRxRequest";
    String userActionForErxRequest = "Request";

    BasicDynaBean hospitalCenterBean = centerService.getDhpoInfoCenterWise(patientId);
    if (hospitalCenterBean == null || (null == hospitalCenterBean.get("dhpo_facility_user_id")
        || hospitalCenterBean.get("dhpo_facility_user_id").equals(""))) {

      // to reverse erx status to that of sent in pbm_prescription
      pbmPrescriptionsService.saveErxRequestDetails(pbmPrescId, userId, erxRequestSentType,
          userActionForErxRequest, centerId, patientId);
      message = messageUtil.getMessage("exception.dhpo.facility.user");
      erxResponseMap = getResponse(message, false);
      this.publishERxResponseToRedisPublisher(erxResponseMap, patientId, requestType, schema,
          userId, id.toString(), ERX_FAILED, message, redisKeyTemplateForCancellingERx);
      return;
    }

    String dhpoFacilityUser = (String) hospitalCenterBean.get("dhpo_facility_user_id");
    String dhpoFacilityPassword = (String) hospitalCenterBean.get("dhpo_facility_password");

    BasicDynaBean doctorBean = doctorService.getDoctorByConsId(id);

    String dhpoClinicianUser = (String) doctorBean.get("dhpo_clinician_user_id");
    String dhpoClinicianPassword = (String) doctorBean.get("dhpo_clinician_password");

    try {
      new ERxWebService().getRemoteService();
    } catch (Exception ex) {
      // to reverse erx status to that of sent in pbm_prescription
      pbmPrescriptionsService.saveErxRequestDetails(pbmPrescId, userId, erxRequestSentType,
          userActionForErxRequest, centerId, patientId);
      message = messageUtil.getMessage("exception.dhpo.server.con.error");
      if (ex instanceof SocketException) {
        erxResponseMap = getResponse(message + ex.getMessage(), false);
        this.publishERxResponseToRedisPublisher(erxResponseMap, patientId, requestType, schema,
            userId, id.toString(), ERX_FAILED, message, redisKeyTemplateForCancellingERx);
        return;
      }
      erxResponseMap = getResponse(message + ex.getMessage(), false);
      this.publishERxResponseToRedisPublisher(erxResponseMap, patientId, requestType, schema,
          userId, id.toString(), ERX_FAILED, message, redisKeyTemplateForCancellingERx);
      return;
    }

    ERxRequestSender sender = new ERxRequestSender(dhpoFacilityUser, dhpoFacilityPassword,
        dhpoClinicianUser, dhpoClinicianPassword);

    Map<String, Object> filterMap = new HashMap<>();
    filterMap.put("pbm_presc_id", pbmPrescId);
    BasicDynaBean pbmPresBean = pbmPrescriptionsService.findByKey(filterMap);

    // Uploads ERx Request and returns the ERx Response
    String xmlFileName =
        pbmPresBean.get("erx_file_name") != null ? (String) pbmPresBean.get("erx_file_name")
            : "TEST.XML";

    // File name cannot be the same as the original request, DHPO errors out. So we
    // append with the
    // Cancel as an indicator and
    // the timestamp so that there are no duplicates in case of multiple
    // cancellation

    if (requestType.equalsIgnoreCase("eRxCancellation") && null != xmlFileName
        && !xmlFileName.trim().equals("")) {
      xmlFileName = "Cancel_" + (new Date().getTime()) + "_" + xmlFileName;
    }

    ERxResponse erxResponse = null;

    try {
      erxResponse = sender.sendERxRequestWithTimeOut(xml, xmlFileName, 30000);
    } catch (ClientTransportException clientTransportException) {
      // to reverse erx status to that of sent in pbm_prescription
      pbmPrescriptionsService.saveErxRequestDetails(pbmPrescId, userId, erxRequestSentType,
          userActionForErxRequest, centerId, patientId);
      message = "Failed to cancel eRx Request : Connection to eClaimLink timed out";
      log.error(message, clientTransportException);
      erxResponseMap = getResponse(message, false);
      this.publishERxResponseToRedisPublisher(erxResponseMap, patientId, requestType, schema,
          userId, id.toString(), ERX_FAILED, message, redisKeyTemplateForCancellingERx);
      return;
    } catch (SocketTimeoutException socketTimeoutException) {
      // to reverse erx status to that of sent in pbm_prescription
      pbmPrescriptionsService.saveErxRequestDetails(pbmPrescId, userId, erxRequestSentType,
          userActionForErxRequest, centerId, patientId);
      message = "Failed to cancel eRx Request : Connection to eClaimLink timed out";
      log.error(message, socketTimeoutException);
      erxResponseMap = getResponse(message, false);
      this.publishERxResponseToRedisPublisher(erxResponseMap, patientId, requestType, schema,
          userId, id.toString(), ERX_FAILED, message, redisKeyTemplateForCancellingERx);
      return;
    } catch (WebServiceException webServiceException) {
      message = "Failed to cancel eRx request: Error in connection.";
      log.error(message, webServiceException);
      erxResponseMap = getResponse(message, false);
      this.publishERxResponseToRedisPublisher(erxResponseMap, patientId, requestType, schema,
          userId, id.toString(), ERX_FAILED, message, redisKeyTemplateForCancellingERx);
      return;
    } catch (Exception exception) {
      // to reverse erx status to that of sent in pbm_prescription
      pbmPrescriptionsService.saveErxRequestDetails(pbmPrescId, userId, erxRequestSentType,
          userActionForErxRequest, centerId, patientId);
      message = "Failed to cancel eRx request";
      if (exception instanceof SocketException) {
        erxResponseMap = getResponse(message + exception.getMessage(), false);
        this.publishERxResponseToRedisPublisher(erxResponseMap, patientId, requestType, schema,
            userId, id.toString(), ERX_FAILED, message, redisKeyTemplateForCancellingERx);
        return;
      }
      log.error(message, exception);
      erxResponseMap = getResponse(message, false);
      this.publishERxResponseToRedisPublisher(erxResponseMap, patientId, requestType, schema,
          userId, id.toString(), ERX_FAILED, message, redisKeyTemplateForCancellingERx);
      return;
    }

    if (erxResponse != null && erxResponse.isError()) {
      String errorMessage = erxResponse.getErrorMessage();
      Object errorReport = erxResponse.getErrorReport();

      // From PBM, we have known that the error report would be a excel file.
      // Hence, here we are not encoding and decoding error report as a file for user
      // to take
      // action.
      // Instead, read the first file entry and write to the browser directly
      // (or) show the error message if any.
      // While framework changes are done in PBM, need to follow the same.

      if (errorReport != null && erxResponse != null) {
        EResponseProcessor.CsvStreamProcessor errProcessor =
            new EResponseProcessor.CsvStreamProcessor();

        // saves the sent error file from erx webservice to database.
        String msg = saveErrorFileAndReturnErrorResponse(erxResponse, errProcessor, xmlFileName);
        if (msg != null) {
          String[] msgArray = msg.split(",");
          msg = String.join(" ",Arrays.copyOfRange(msgArray, 8, msgArray.length));

          //Bug HMS-36312 fix ->
          //if the response is cancelled from the DHPO server, then forceful cancel is initiated
          if (pbmPrescId != null) {
            String wantedStr = pbmPrescId + " was cancelled before";
            if (Pattern.compile(Pattern.quote(wantedStr),
                    Pattern.CASE_INSENSITIVE).matcher(msg).find()) {
              if (pbmPrescriptionsService.saveCancelResponse(id, pbmPrescId)) {
                message = messageUtil.getMessage("msg.erx.cancel.success");
                erxResponseMap = getResponse(message, true);
                this.publishERxResponseToRedisPublisher(erxResponseMap, patientId,
                        requestType, schema, userId,
                        id.toString(), ERX_SUCCESS, message, redisKeyTemplateForCancellingERx);
                return;
              }
            }
          }

          // to reverse erx status to that of sent in pbm_prescription
          pbmPrescriptionsService.saveErxRequestDetails(pbmPrescId, userId, erxRequestSentType,
              userActionForErxRequest, centerId, patientId);

          erxResponseMap = getResponse(msg.trim(), false);
          this.publishERxResponseToRedisPublisher(erxResponseMap, patientId, requestType, schema,
              userId, id.toString(), ERX_FAILED, message, redisKeyTemplateForCancellingERx);
          return;
        } else {
          // to reverse erx status to that of sent in pbm_prescription
          pbmPrescriptionsService.saveErxRequestDetails(pbmPrescId, userId, erxRequestSentType,
              userActionForErxRequest, centerId, patientId);

          message = messageUtil.getMessage("exception.failed.to.save.errorfile");
          erxResponseMap = getResponse(message, false);
          this.publishERxResponseToRedisPublisher(erxResponseMap, patientId, requestType, schema,
              userId, id.toString(), ERX_FAILED, message, redisKeyTemplateForCancellingERx);
          return;
        }
      } else if (errorMessage != null && !errorMessage.equals("")) {
        // to reverse erx status to that of sent in pbm_prescription
        pbmPrescriptionsService.saveErxRequestDetails(pbmPrescId, userId, erxRequestSentType,
            userActionForErxRequest, centerId, patientId);

        erxResponseMap = getResponse(message, false);
        this.publishERxResponseToRedisPublisher(erxResponseMap, patientId, requestType, schema,
            userId, id.toString(), ERX_FAILED, message, redisKeyTemplateForCancellingERx);
        return;
      }
    }

    // Empty the erx ref. no and de-attach prescriptions from request.
    if (!pbmPrescriptionsService.saveCancelResponse(id, pbmPrescId)) {
      // to reverse erx status to that of sent in pbm_prescription
      pbmPrescriptionsService.saveErxRequestDetails(pbmPrescId, userId, erxRequestSentType,
          userActionForErxRequest, centerId, patientId);

      message = messageUtil.getMessage("exception.marking.closed.error");
      erxResponseMap = getResponse(message, false);
      this.publishERxResponseToRedisPublisher(erxResponseMap, patientId, requestType, schema,
          userId, id.toString(), ERX_FAILED, message, redisKeyTemplateForCancellingERx);
      return;
    }

    message = messageUtil.getMessage("msg.erx.cancel.success");
    erxResponseMap = getResponse(message, true);
    this.publishERxResponseToRedisPublisher(erxResponseMap, patientId, requestType, schema, userId,
        id.toString(), ERX_SUCCESS, message, redisKeyTemplateForCancellingERx);
    return;
  }

  /**
   * Validate erx.
   *
   * @param pbmPrescId the pbm presc id
   * @param centerId the center id
   * @return the string
   */
  public String validateErx(int pbmPrescId, Integer centerId) {

    String path = RequestContext.getHttpRequest().getContextPath();
    BasicDynaBean genPrefs = genericPreferencesService.getAllPreferences();

    Map<String, Object> key = new HashMap<String, Object>();
    key.put("pbm_presc_id", pbmPrescId);
    BasicDynaBean pbmPresBean = pbmPrescriptionsService.findByKey(key);

    if (null == pbmPresBean) {
      return messageUtil.getMessage("exception.erx.pbm.id.error");
    }

    Integer erxCenterId = centerId;
    if (null != pbmPresBean.get("erx_center_id")) {
      erxCenterId = (Integer) pbmPresBean.get("erx_center_id");
    }
    String facilityId = null;

    if (erxCenterId != null && erxCenterId != 0) {
      BasicDynaBean centerbean = centerService.findByKey(centerId);
      facilityId = centerbean.get("hospital_center_service_reg_no") != null
          ? (String) centerbean.get("hospital_center_service_reg_no")
          : "";
      String msgTxt = pbmhelper.urlString(path, "center-name", new Integer(centerId).toString(),
          (String) centerbean.get("center_name"));

      if (facilityId == null || facilityId.trim().equals("")) {
        return messageUtil.getMessage("exception.facility.id.error.multi.center",
            new Object[] {msgTxt});
      }
    } else {
      facilityId = genPrefs.get("hospital_service_regn_no") != null
          ? (String) genPrefs.get("hospital_service_regn_no")
          : "";

      if (facilityId == null || facilityId.trim().equals("")) {
        return messageUtil.getMessage("exception.facility.id.error.single.center");
      }
    }

    BasicDynaBean erxConsBean = pbmPrescriptionsService.getConsErxDetails(pbmPrescId);

    if (null == erxConsBean) {
      return messageUtil.getMessage("exception.erx.cons.id.error");
    }

    // only do following validations if its an insurance patient
    // ignore following validation for selfpay sponsor patients and cash patients
    Boolean isSelfpay = (erxConsBean.get("is_selfpay_sponsor") != null
        || erxConsBean.get("primary_sponsor_id") == null);

    if (!isSelfpay) {
      String patientId = (String) erxConsBean.get("patient_id");
      int planId = erxConsBean.get("plan_id") != null ? (Integer) erxConsBean.get("plan_id") : 0;
      String insuranceCoId = (String) erxConsBean.get("primary_insurance_co");
      String healthAuthority =
          (String) centerService.findByKey(erxCenterId).get("health_authority");

      BasicDynaBean insubean =
          insuranceCompanyService.getInsuranceCompanyCode(healthAuthority, insuranceCoId);
      String insuranceCoName = insubean != null ? (String) insubean.get("insurance_co_name") : "";
      String payerId = insubean != null ? (String) insubean.get("insurance_co_code") : "";

      if (planId == 0) {
        String msgTxt = pbmhelper.urlString(path, "insurance", patientId, patientId);
        return messageUtil.getMessage("exception.plan.id.error") + msgTxt;
      }

      if (insuranceCoId == null || insuranceCoId.trim().equals("")) {
        String msgTxt = pbmhelper.urlString(path, "insurance", patientId, patientId);
        return messageUtil.getMessage("exception.insurance.company.id.error") + msgTxt;
      }

      if (payerId == null || payerId.trim().equals("")) {
        String msgTxt = "Insurance Company: "
            + pbmhelper.urlString(path, "company", insuranceCoId, insuranceCoName);
        return messageUtil.getMessage("exception.payer.id.error") + msgTxt;
      }
    }
    return null;
  }

  /**
   * Save error file.
   *
   * @param erxResponse the e rxresponse
   * @param errProcessor the err processor
   * @param errorFileName the error file name
   * @return the basic dyna bean
   */
  public String saveErrorFileAndReturnErrorResponse(ERxResponse erxResponse,
      EResponseProcessor.CsvStreamProcessor errProcessor, String errorFileName) {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try {
      errProcessor.process(erxResponse, outputStream);
    } catch (IOException exception) {
      log.error("Error Processing ERxResponse", exception);
    }
    BasicDynaBean mbean = erxResponseRepository.getBean();
    mbean.set("request_id", "");
    mbean.set("response_id", erxResponseRepository.getNextSequence());
    mbean.set("response_content", new ByteArrayInputStream(outputStream.toByteArray()));
    mbean.set("response_content_type", "application/vnd.ms-excel");
    mbean.set("response_file_name", errorFileName);
    if (erxResponseRepository.insert(mbean) > 0) {
      return new String( outputStream.toByteArray(), StandardCharsets.UTF_8);
    } else {
      return null;
    }
  }

  /**
   * Gets the error string.
   *
   * @param errorList the error list
   * @return the error string
   */
  private String getErrorString(List<String> errorList) {

    StringBuilder errStr = new StringBuilder(
        "Error(s) while XML data check. " + "ERx Request XML could not be generated.<br/>"
            + "Please correct (or) update the following.<br/>");

    for (String err : errorList) {
      errStr.append("<br/>" + err);
    }
    return errStr.toString();
  }

  /**
   * Gets the response.
   *
   * @param message the message
   * @param success the success
   * @return the response
   */
  private Map<String, Object> getResponse(String message, boolean success) {
    Map<String, Object> responseData = new HashMap<String, Object>();
    responseData.put("success", success);
    responseData.put("message", message);
    return responseData;
  }

  /**
   * Gets the test mode.
   *
   * @return the test mode
   */
  private boolean getTestMode() {
    return false;
  }

  /**
   * Schedules the Job.
   *
   * @param params is the formParameter of consultation
   * @param schema is the schema
   * @param userName is the user logged in
   * @param centerId is the hospital centerId
   * @return returns the map that contains message for user
   */
  public Map<String, Object> scheduleErxJob(FormParameter params, String schema, String userName,
      Integer centerId) {

    Integer pbmPrescId = prescriptionsService.getErxConsPBMId(params.getId());
    if (null == pbmPrescId) {
      return getResponse(messageUtil.getMessage("exception.erx.pbm.id.error"), false);
    }

    String errStr = validateErx(pbmPrescId, centerId);
    log.debug("Error String :" + errStr);
    if (errStr != null) {
      return getResponse(errStr, false);
    }

    String userAction = "Request";
    String requestType = "eRxRequest";
    if (!pbmPrescriptionsService.saveErxRequestDetails(pbmPrescId, userName, requestType,
        userAction, centerId, params.getPatientId())) {
      return getResponse(messageUtil.getMessage("exception.saving.erx.data.error"), false);
    }

    ERxRequestGenerator generator = beanFactory.getBean(ERxRequestGenerator.class, getTestMode());
    String message = null;
    Map<String, Object> erxResponseMap = null;

    List<String> errorList = new ArrayList<>();
    String xml = null;
    try {
      xml = generator.generateRequestXML(params.getId(), requestType, errorList);
    } catch (Exception exception) {
      log.error("ERX xml generation failed : ", exception);
      message = messageUtil.getMessage("exception.erx.xml.generation.error");
      erxResponseMap = getResponse(message, false);
      return erxResponseMap;
    }

    if (null == xml) {
      message = getErrorString(errorList);
      erxResponseMap = getResponse(getErrorString(errorList), false);
      return erxResponseMap;
    }

    String patientId = params.getPatientId();
    // In case of IP we use visitId (string) and OP we use consultationId (integer)
    Object consultationId = params.getId();
    if (patientId == null || patientId.isEmpty() || consultationId == null) {
      log.error("Patient Id is " + patientId + " Consultation Id is " + consultationId);
      message = "Invalid Patient Id or Consultaition Id";
      erxResponseMap = getResponse(message, false);
      return erxResponseMap;
    }

    Map<String, Object> map = new HashMap<>();
    map.put("id", consultationId);
    map.put("patientId", patientId);
    map.put("schema", schema);
    map.put("userName", userName);
    map.put("centerId", centerId);
    map.put("path", RequestContext.getHttpRequest().getContextPath());
    map.put("pbmPrescId", pbmPrescId);
    map.put("xmlData", xml);
    String redisKey = String.format(redisKeyTemplateForSendingERx, schema, consultationId);
    String startedAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    String redisValue = String.format(redisValueTemplate, ERX_IN_PROGRESS, startedAt, "", null);
    redisTemplate.opsForValue().set(redisKey, redisValue);
    redisTemplate.expire(redisKey, REDIS_KEY_ALIVE_TIME, TimeUnit.HOURS);
    jobService.scheduleHighPriorityImmediate(buildJob(
        "ERX_" + params.getId() + "_" + System.currentTimeMillis(), SendErxJob.class, map));
    message = "Submitted ERx request successfully, will be notified once response is received";
    erxResponseMap = getResponse(message, true);
    log.info("ERX request sent : " + xml);
    return erxResponseMap;
  }

  /**
   * Schedule Cancel ERx Job.
   *
   * @param params formParameter
   * @return returns the map that contains message for user
   */

  public Map<String, Object> scheduleCancelERxJob(FormParameter params) {

    String message = null;
    Map<String, Object> erxResponseMap = null;
    // In case of IP we use visitId (string) and OP we use consultationId (integer)
    Object consId = params.getId();

    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();

    Integer centerId = (Integer) sessionAttributes.get("centerId");

    ERxRequestGenerator generator = beanFactory.getBean(ERxRequestGenerator.class, getTestMode());
    List<String> errorList = new ArrayList<String>();

    int pbmPrescId = prescriptionsService.getErxConsPBMId(consId);

    // Validate Facility ID and Payer ID
    message = validateErx(pbmPrescId, centerId);

    log.debug("Error String :" + message);
    if (message != null) {
      erxResponseMap = getResponse(message, false);
      return erxResponseMap;
    }

    String requestType = "eRxCancellation";
    String userId = (String) sessionAttributes.get("userId");
    String userAction = "Cancel";

    // Generate Erx Presc. Id and save erx_presc_id and center_id before
    if (!pbmPrescriptionsService.saveErxRequestDetails(pbmPrescId, userId, requestType, userAction,
        centerId, params.getPatientId())) {
      message = messageUtil.getMessage("exception.saving.erxcancel.data.error");
      erxResponseMap = getResponse(message, false);
      return erxResponseMap;
    }

    String xml = null;
    try {
      // generating XML.
      xml = generator.generateRequestXML(consId, requestType, errorList);
    } catch (Exception exception) {
      log.error("ERX xml generation failed : " + exception);
      message = messageUtil.getMessage("exception.erx.xml.generation.error");
      erxResponseMap = getResponse(message, false);
      return erxResponseMap;
    }

    if (null == xml) {
      message = getErrorString(errorList);
      erxResponseMap = getResponse(message, false);
      return erxResponseMap;
    }
    String patientId = params.getPatientId();
    if (patientId == null || patientId.isEmpty() || consId == null) {
      log.error("Patient Id is " + patientId + " Consultation Id is " + consId);
      message = "Invalid Patient Id or Consultaition Id";
      erxResponseMap = getResponse(message, false);
      return erxResponseMap;
    }

    String schema = RequestContext.getSchema();

    Map<String, Object> map = new HashMap<>();

    map.put("schema", schema);
    map.put("userId", userId);
    map.put("centerId", centerId);
    map.put("patientId", patientId);
    map.put("path", RequestContext.getHttpRequest().getContextPath());
    map.put("pbmPrescId", pbmPrescId);
    map.put("id", consId);
    map.put("xml", xml);



    String redisKey = String.format(redisKeyTemplateForCancellingERx, schema, consId);
    String startedAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    String redisValue = String.format(redisValueTemplate, ERX_IN_PROGRESS, startedAt, "", null);
    redisTemplate.opsForValue().set(redisKey, redisValue);

    redisTemplate.expire(redisKey, REDIS_KEY_ALIVE_TIME, TimeUnit.HOURS);
    jobService.scheduleHighPriorityImmediate(
        buildJob("ERX_" + consId + "_" + System.currentTimeMillis(), CancelERxJob.class, map));
    message = "Submitted ERx cancellation request successfully, will be notified once response "
        + "is received";
    erxResponseMap = getResponse(message, true);
    return erxResponseMap;
  }


  /**
   * This function publishes response to redis broker.
   *
   * @param erxResponse response to be published
   * @param patientId patientId
   * @param requestType whether send or cancel
   * @param schema schema
   * @param userId loggedInUserId
   * @param consultationId consultationId
   * @param status whether success or false
   * @param message message to be sent
   * @param redisKeyTemplate redisKeyTemplate
   */
  public void publishERxResponseToRedisPublisher(Map<String, Object> erxResponse, String patientId,
      String requestType, String schema, String userId, Object consultationId, String status,
      String message, String redisKeyTemplate) {
    String redisKey = String.format(redisKeyTemplate, schema, consultationId);
    String redisValue = (String) redisTemplate.opsForValue().get(redisKey);
    String[] valueArray = redisValue.split(";");
    String startedAt = valueArray[1].split(":")[1];
    String endedAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    redisValue = String.format(redisValueTemplate, status, startedAt, endedAt, message);
    if (status == ERX_SUCCESS) {
      redisTemplate.delete(redisKey);
    } else {
      redisTemplate.opsForValue().set(redisKey, redisValue);
    }
    StringBuilder detailStringBuilder = new StringBuilder();
    if (erxResponse.get("details") != null) {
      Map<String, Object> detailsMap = (Map) erxResponse.get("details");
      for (Map.Entry<String, Object> detailMEntry : detailsMap.entrySet()) {
        detailStringBuilder.append(detailMEntry.getKey()).append("=")
            .append(detailMEntry.getValue()).append(",");
      }
    }

    final String semiColon = ";";
    StringBuilder messageToBePublished = new StringBuilder();
    messageToBePublished.append(schema).append(semiColon)
        .append(WEB_SOCKET_PUSH_CHANNEL).append(semiColon)
        .append(erxResponse.get("message")).append(semiColon)
        .append(erxResponse.get("success")).append(semiColon)
        .append(requestType).append(semiColon)
        .append(userId).append(semiColon)
        .append(detailStringBuilder);
    redisMessagePublisher.publishERxResponse(RedisMessagePublisher.REDIS_ERX_RESPONSE_PUSH_CHANNEL,
        messageToBePublished);
  }

  /**
   * This function pushes response to webSocket which is based on PatientId.
   *
   * @param erxResponse response to be sent to socket
   */
  public void pushERxResponseToWebSocket(String userId,
      Map<String, Object> erxResponse, String channel) {
    this.pushService.pushToUser(userId, channel, erxResponse);
  }

}
