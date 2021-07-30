/**
 *
 */

package com.insta.hms.eauthorization.priorauth;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.eauthorization.EAuthPrescriptionActivitiesDAO;
import com.insta.hms.eauthorization.EAuthPrescriptionDAO;
import com.insta.hms.eservice.EResult;
import com.insta.hms.eservice.EResultParser;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.TpaMaster.TpaMasterDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.servlet.http.HttpSession;

/**
 * The Class EAuthAuthorizationXmlParser.
 *
 * @author lakshmi
 */
public class EAuthAuthorizationXmlParser implements EResultParser {

  /**
   * The digester.
   */
  private Digester digester;
  
  private static final GenericDAO insuranceDenialCodesDAO =
      new GenericDAO("insurance_denial_codes");

  /**
   * Instantiates a new e auth authorization xml parser.
   */
  public EAuthAuthorizationXmlParser() {
    digester = new Digester();
    digester.setValidating(false);

    digester.addObjectCreate("Prior.Authorization", "com.insta.hms.eauthorization.priorauth"
        + ".PriorAuthorization");

    // Header
    digester.addObjectCreate("Prior.Authorization/Header", "com.insta.hms.eauthorization"
        + ".priorauth.PriorAuthorizationHeader");
    digester.addBeanPropertySetter("Prior.Authorization/Header/SenderID", "senderID");
    digester.addBeanPropertySetter("Prior.Authorization/Header/ReceiverID", "receiverID");
    digester.addBeanPropertySetter("Prior.Authorization/Header/TransactionDate", "transactionDate");
    digester.addBeanPropertySetter("Prior.Authorization/Header/RecordCount", "recordCount");
    digester.addBeanPropertySetter("Prior.Authorization/Header/DispositionFlag", "dispositionFlag");
    digester.addSetNext("Prior.Authorization/Header", "setHeader");


    // Authorization
    digester.addObjectCreate("Prior.Authorization/Authorization", "com.insta.hms.eauthorization"
        + ".priorauth.PriorAuthAuthorization");
    digester.addBeanPropertySetter("Prior.Authorization/Authorization/Result",
        "authorizationResult");
    digester.addBeanPropertySetter("Prior.Authorization/Authorization/ID", "authorizationID");
    digester.addBeanPropertySetter("Prior.Authorization/Authorization/IDPayer",
        "authorizationIDPayer");
    digester.addBeanPropertySetter("Prior.Authorization/Authorization/DenialCode", "denialCode");
    digester.addBeanPropertySetter("Prior.Authorization/Authorization/Start", "start");
    digester.addBeanPropertySetter("Prior.Authorization/Authorization/End", "end");
    digester.addBeanPropertySetter("Prior.Authorization/Authorization/Limit", "limit");
    digester.addBeanPropertySetter("Prior.Authorization/Authorization/Comments", "comments");


    // Prior Auth. Unbound Activity
    digester.addObjectCreate("Prior.Authorization/Authorization/Activity", "com.insta.hms"
        + ".eauthorization.priorauth.PriorAuthorizationActivity");
    digester.addBeanPropertySetter("Prior.Authorization/Authorization/Activity/ID", "activityID");
    digester.addBeanPropertySetter("Prior.Authorization/Authorization/Activity/Type",
        "activityType");
    digester.addBeanPropertySetter("Prior.Authorization/Authorization/Activity/Code",
        "activityCode");
    digester.addBeanPropertySetter("Prior.Authorization/Authorization/Activity/Quantity",
        "quantity");
    digester.addBeanPropertySetter("Prior.Authorization/Authorization/Activity/Net", "net");
    digester.addBeanPropertySetter("Prior.Authorization/Authorization/Activity/List", "list");
    digester.addBeanPropertySetter("Prior.Authorization/Authorization/Activity/PatientShare",
        "patientShare");
    digester.addBeanPropertySetter("Prior.Authorization/Authorization/Activity/PaymentAmount",
        "paymentAmount");
    digester.addBeanPropertySetter("Prior.Authorization/Authorization/Activity/DenialCode",
        "activityDenialCode");


    // Activity Unbound Observation
    digester.addObjectCreate("Prior.Authorization/Authorization/Activity/Observation", "com.insta"
        + ".hms.eauthorization.priorauth.PriorActivityObservation");
    digester.addBeanPropertySetter("Prior.Authorization/Authorization/Activity/Observation/Type",
        "type");
    digester.addBeanPropertySetter("Prior.Authorization/Authorization/Activity/Observation/Code",
        "code");
    digester.addBeanPropertySetter(
        "Prior.Authorization/Authorization/Activity/Observation/Value",
        "value");
    digester.addBeanPropertySetter(
        "Prior.Authorization/Authorization/Activity/Observation"
            + "/ValueType",
        "valueType");

    digester.addSetNext("Prior.Authorization/Authorization/Activity/Observation", "addObservation");

    digester.addSetNext("Prior.Authorization/Authorization/Activity", "addActivity");

    digester.addSetNext("Prior.Authorization/Authorization", "setAuthorization");
  }

  /**
   * Parses the.
   *
   * @param xml the xml
   * @return the e result
   * @throws IOException  Signals that an I/O exception has occurred.
   * @throws SAXException the SAX exception
   */
  public EResult parse(String xml)
      throws IOException, SAXException {
    PriorAuthorization auth =
        (PriorAuthorization) new EAuthAuthorizationXmlParser()
            .digester.parse(new StringReader(xml));
    return auth;
  }

  /**
   * Parses the.
   *
   * @param is the is
   * @return the e result
   * @throws IOException  Signals that an I/O exception has occurred.
   * @throws SAXException the SAX exception
   */
  public EResult parse(InputStream is)
      throws IOException, SAXException {
    PriorAuthorization auth =
        (PriorAuthorization) new EAuthAuthorizationXmlParser().digester.parse(is);
    return auth;
  }


  /**
   * The e auth presc DAO.
   */
  EAuthPrescriptionDAO eauthPrescDAO = new EAuthPrescriptionDAO();

  /**
   * The preauth req DAO.
   */
  GenericDAO preauthReqDAO = new GenericDAO("preauth_prescription_request");

  /**
   * The preauth req app DAO.
   */
  GenericDAO preauthReqAppDAO = new GenericDAO("preauth_request_approval_details");

  /**
   * The preauth act DAO.
   */
  EAuthPrescriptionActivitiesDAO preauthActDAO = new EAuthPrescriptionActivitiesDAO();

  /**
   * Validate prior authorization xml.
   *
   * @param desc           the desc
   * @param eauthBean      the e auth bean
   * @param eauthRequestId the e auth request id
   * @return the string
   * @throws Exception the exception
   */
  public String validatePriorAuthorizationXml(PriorAuthorization desc,
      BasicDynaBean eauthBean, String eauthRequestId) throws Exception {
    SimpleDateFormat dtFmt = new SimpleDateFormat("dd/MM/yyyy hh:mm");
    String errorMsg = "";
    String serviceRegNo = null;

    HttpSession session = RequestContext.getSession();

    int eauthPrescId = (Integer) eauthBean.get("preauth_presc_id");
    String insuranceCoId = (String) eauthBean.get("preauth_payer_id");
    BasicDynaBean preauthbean = eauthPrescDAO.getEAuthPresc(eauthPrescId, insuranceCoId, false);

    PriorAuthorizationHeader header = desc.getHeader();
    if (header == null) {
      errorMsg = "Prior Auth. XML parsing failed: Prior Authorization Header element missing...";
      return errorMsg;
    } else {
      String senderId = header.getSenderID();
      String transactionDate = header.getTransactionDate();

      int center = preauthbean.get("center_id") != null ? (Integer) preauthbean.get("center_id")
          : 0;
      BasicDynaBean centerbean = new CenterMasterDAO().findByKey("center_id", center);
      serviceRegNo = centerbean != null ? (String) centerbean.get("hospital_center_service_reg_no"
      ) : null;

      if (transactionDate.trim().length() <= 10) {
        transactionDate = transactionDate + " 00:00";
      }

      if (senderId == null || senderId.equals("")) {
        errorMsg = "Prior Auth. XML parsing failed: SenderID not found in Header...";
        return errorMsg;
      }

      if (!TpaMasterDAO.getTpaPayerID((String) preauthbean.get("tpa_id")).get("payer_id")
          .equals(senderId)) {
        errorMsg = "Prior Auth. XML parsing failed: TPA is not same as the E-Authorization sender"
            + "...";
        return errorMsg;
      }

      String receiverID = header.getReceiverID();
      if (receiverID == null || receiverID.equals("")) {
        errorMsg = "Prior Auth. XML parsing failed: ReceiverID not found in Header...";
        return errorMsg;
      }

      if (serviceRegNo == null || !(serviceRegNo).equals(receiverID)) {
        errorMsg = "Prior Auth. XML parsing failed: Receiver ID not same as the center service "
           + "registration no. ...";
        return errorMsg;
      }

      if (transactionDate == null || transactionDate.equals("")) {
        errorMsg = "Prior Auth. XML parsing failed: TransactionDate not found in Header...";
        return errorMsg;
      } else if (dtFmt.parse(transactionDate) == null) {
        errorMsg = "Prior Auth. XML parsing failed: TransactionDate is not a valid date...";
        return errorMsg;
      }

      int recordCount = header.getRecordCount();
      if (recordCount < 0) {
        errorMsg = "Prior Auth. XML parsing failed: RecordCount is not valid...";
        return errorMsg;
      }

      String dispositionFlag = header.getDispositionFlag();

      if (dispositionFlag == null || dispositionFlag.equals("")) {
        errorMsg = "Prior Auth. XML parsing failed: DispositionFlag not found in Header...";
        return errorMsg;
      } else if (!dispositionFlag.equalsIgnoreCase("PRODUCTION")
          && !dispositionFlag.equalsIgnoreCase("TEST")) {
        errorMsg = "Prior Auth. XML parsing failed: DispositionFlag value is not valid...";
        return errorMsg;
      }
    } // End of header validation

    PriorAuthAuthorization priorAuth = desc.getAuthorization();
    if (priorAuth == null) {
      errorMsg = "Prior Auth. XML parsing failed: No Authorization element found...";
      return errorMsg;
    } else {
      String authReqID = priorAuth.getAuthorizationID();
      String authResult = priorAuth.getAuthorizationResult();
      BigDecimal limit = priorAuth.getLimit();
      String comments = priorAuth.getComments();

      if (authReqID == null || authReqID.equals("")) {
        errorMsg = "Prior Auth. XML parsing failed: ID not found for Authorization...";
        return errorMsg;
      }

      if (eauthRequestId != null && !authReqID.equals(eauthRequestId)) {
        errorMsg = "Prior Auth. XML parsing failed: Authorization.ID " + authReqID
            + " does not match with E-Auth Request ID: " + eauthRequestId;
        return errorMsg;
      }

      BasicDynaBean preauthReqAppBean = preauthReqAppDAO.findByKey("preauth_request_id",
          authReqID);
      BasicDynaBean preauthReqBean = preauthReqDAO.findByKey("preauth_request_id", authReqID);
      if (preauthReqAppBean == null || preauthReqBean == null) {
        errorMsg = "Prior Auth. XML parsing failed: Invalid Auth. ID (or) No Request exists with "
            + "ID: " + authReqID;
        return errorMsg;
      }

      String requestType = (String) preauthReqAppBean.get("preauth_request_type");
      String preauthStatus = (String) preauthbean.get("preauth_status");

      if (requestType.equals("Authorization")) {
        if (preauthStatus == null || !preauthStatus.equals("S")) {
          errorMsg = "Prior Auth. XML parsing failed: E-Auth Prescription " + eauthPrescId + " is"
              + " not marked as Sent...";
          return errorMsg;
        }
      }

      String authIdPayer = priorAuth.getAuthorizationIDPayer();
      if (authIdPayer == null || authIdPayer.equals("")) {
        errorMsg = "Prior Auth. XML parsing failed: IDPayer not found for Authorization...";
        return errorMsg;
      }

      String start = priorAuth.getStart();

      if (start != null && (dtFmt.parse(start) == null
          || (new java.util.Date()).before(dtFmt.parse(start)))) {
        errorMsg = "Prior Auth. XML parsing failed: Start Date value for Authorization is invalid"
           + "...";
        return errorMsg;
      }

      String end = priorAuth.getEnd();

      if (end != null && (dtFmt.parse(end) == null)) {
        errorMsg = "Prior Auth. XML parsing failed: End Date value for Authorization is invalid...";
        return errorMsg;
      }

      String authDenialCode = priorAuth.getDenialCode();
      if (authDenialCode != null && !authDenialCode.trim().equals("")
          && insuranceDenialCodesDAO.findByKey("denial_code",
          authDenialCode) == null) {
        errorMsg = "Prior Auth. XML parsing failed: Denial Code value (" + authDenialCode + ") "
            + "not valid for Authorization...";
        return errorMsg;
      }


      ArrayList activities = priorAuth.getActivities();
      if ((requestType.equals("Authorization"))
          && (activities == null || activities.isEmpty() || activities.size() < 1)) {
        errorMsg = "Prior Auth. XML parsing failed: No Activities found for Authorization with "
            + "E-Auth Prescription ID " + eauthPrescId + "...";
        return errorMsg;
      } else {
        for (int j = 0; j < activities.size(); j++) {
          PriorAuthorizationActivity activity = (PriorAuthorizationActivity) activities.get(j);
          String activityId = activity.getActivityID();
          String type = activity.getActivityType();
          String code = activity.getActivityCode();
          BigDecimal net = activity.getNet();
          String activityDenialCode = activity.getActivityDenialCode();
          BigDecimal quantity = activity.getQuantity();
          BigDecimal list = activity.getList();
          BigDecimal patientShare = activity.getPatientShare();
          BigDecimal paymentAmount = activity.getPaymentAmount();

          ArrayList observations = activity.getObservations();

          if (activityId == null || activityId.equals("")) {
            errorMsg = "Prior Auth. XML parsing failed: ID not found for Activity in "
                + "Authorization with E-Auth Prescription ID " + eauthPrescId + "...";
            return errorMsg;
          } else {

            String actId = activityId.split("-")[0];
            int prescActId = Integer.parseInt(actId);
            BasicDynaBean activityBean = preauthActDAO.findByKey("preauth_act_id", prescActId);
            if (activityBean == null) {
              errorMsg = "Prior Auth. XML parsing failed: ID " + activityId + " for Activity not "
                  + "valid in Authorization with E-Auth Prescription ID " + eauthPrescId + "...";
              return errorMsg;
            }

            Integer actPrescId = activityBean.get("preauth_presc_id") != null
                ? (Integer) activityBean.get("preauth_presc_id") : null;
            if (actPrescId == null || actPrescId.intValue() != eauthPrescId) {
              errorMsg = "Prior Auth. XML parsing failed: Invalid ID " + activityId + " for "
                  + "Activity found in Authorization with E-Auth Prescription ID "
                  + eauthPrescId + "...";
              return errorMsg;
            }

            if (type == null || type.equals("")) {
              errorMsg =
                  "Prior Auth. XML parsing failed: Type value not found for Activity "
                      + activityId + " in Authorization with E-Auth Prescription ID "
                      + eauthPrescId + "...";
              return errorMsg;
            }

            if (code == null || code.equals("")) {
              errorMsg =
                  "Prior Auth. XML parsing failed: Code value not found for Activity "
                      + activityId + " in Authorization with E-Auth Prescription ID "
                      + eauthPrescId + "...";
              return errorMsg;
            }

            if (net == null || net.equals("")) {
              errorMsg =
                  "Prior Auth. XML parsing failed: Net value not found for Activity " + activityId
                      + " in Authorization with E-Auth Prescription ID " + eauthPrescId + "...";
              return errorMsg;
            }

            if (activityDenialCode != null && !activityDenialCode.trim().equals("")
                && insuranceDenialCodesDAO.findByKey("denial_code",
                activityDenialCode) == null) {
              errorMsg = "Prior Auth. XML parsing failed: Denial Code value not valid for "
                  + "Activity " + activityId + " in Authorization with E-Auth Prescription ID "
                  + eauthPrescId + "...";
              return errorMsg;
            }
          }
        } // end of activity validation
      }
    } // end of authorization validation

    return errorMsg;
  }
}
