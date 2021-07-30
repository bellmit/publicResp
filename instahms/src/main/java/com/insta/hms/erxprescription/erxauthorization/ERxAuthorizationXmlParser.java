/**
 *
 */

package com.insta.hms.erxprescription.erxauthorization;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.eservice.EResult;
import com.insta.hms.eservice.EResultParser;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.TpaMaster.TpaMasterDAO;
import com.insta.hms.pbmauthorization.PBMPrescriptionsDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

/**
 * The Class ERxAuthorizationXmlParser.
 *
 * @author lakshmi
 */
public class ERxAuthorizationXmlParser implements EResultParser {

  /**
   * The digester.
   */
  private Digester digester;
  
  private static final GenericDAO insuranceDenialCodesDAO =
      new GenericDAO("insurance_denial_codes");

  /**
   * Instantiates a new e rx authorization xml parser.
   */
  public ERxAuthorizationXmlParser() {
    digester = new Digester();
    digester.setValidating(false);

    digester.addObjectCreate("Prior.Authorization",
        "com.insta.hms.erxprescription.erxauthorization.PriorAuthorization");

    // Header
    digester.addObjectCreate("Prior.Authorization/Header",
        "com.insta.hms.erxprescription.erxauthorization.PriorAuthorizationHeader");
    digester.addBeanPropertySetter("Prior.Authorization/Header/SenderID",
        "senderID");
    digester.addBeanPropertySetter("Prior.Authorization/Header/ReceiverID",
        "receiverID");
    digester.addBeanPropertySetter(
        "Prior.Authorization/Header/TransactionDate",
        "transactionDate");
    digester.addBeanPropertySetter("Prior.Authorization/Header/RecordCount",
        "recordCount");
    digester.addBeanPropertySetter(
        "Prior.Authorization/Header/DispositionFlag",
        "dispositionFlag");
    digester.addSetNext("Prior.Authorization/Header", "setHeader");

    // Authorization
    digester.addObjectCreate("Prior.Authorization/Authorization",
        "com.insta.hms.erxprescription.erxauthorization.PriorAuthAuthorization");
    digester.addBeanPropertySetter(
        "Prior.Authorization/Authorization/Result",
        "authorizationResult");
    digester.addBeanPropertySetter("Prior.Authorization/Authorization/ID",
        "authorizationID");
    digester.addBeanPropertySetter(
        "Prior.Authorization/Authorization/IDPayer",
        "authorizationIDPayer");
    digester.addBeanPropertySetter(
        "Prior.Authorization/Authorization/DenialCode", "denialCode");
    digester.addBeanPropertySetter(
        "Prior.Authorization/Authorization/Start", "start");
    digester.addBeanPropertySetter("Prior.Authorization/Authorization/End",
        "end");
    digester.addBeanPropertySetter(
        "Prior.Authorization/Authorization/Limit", "limit");
    digester.addBeanPropertySetter(
        "Prior.Authorization/Authorization/Comments", "comments");

    // Prior Auth. Unbound Activity
    digester.addObjectCreate("Prior.Authorization/Authorization/Activity",
        "com.insta.hms.erxprescription.erxauthorization.PriorAuthorizationActivity");
    digester.addBeanPropertySetter(
        "Prior.Authorization/Authorization/Activity/ID", "activityID");
    digester.addBeanPropertySetter(
        "Prior.Authorization/Authorization/Activity/Type",
        "activityType");
    digester.addBeanPropertySetter(
        "Prior.Authorization/Authorization/Activity/Code",
        "activityCode");
    digester.addBeanPropertySetter(
        "Prior.Authorization/Authorization/Activity/Quantity",
        "quantity");
    digester.addBeanPropertySetter(
        "Prior.Authorization/Authorization/Activity/Net", "net");
    digester.addBeanPropertySetter(
        "Prior.Authorization/Authorization/Activity/List", "list");
    digester.addBeanPropertySetter(
        "Prior.Authorization/Authorization/Activity/PatientShare",
        "patientShare");
    digester.addBeanPropertySetter(
        "Prior.Authorization/Authorization/Activity/PaymentAmount",
        "paymentAmount");
    digester.addBeanPropertySetter(
        "Prior.Authorization/Authorization/Activity/DenialCode",
        "activityDenialCode");

    // Activity Unbound Observation
    digester.addObjectCreate(
        "Prior.Authorization/Authorization/Activity/Observation",
        "com.insta.hms.erxprescription.erxauthorization.PriorActivityObservation");
    digester.addBeanPropertySetter(
        "Prior.Authorization/Authorization/Activity/Observation/Type",
        "type");
    digester.addBeanPropertySetter(
        "Prior.Authorization/Authorization/Activity/Observation/Code",
        "code");
    digester.addBeanPropertySetter(
        "Prior.Authorization/Authorization/Activity/Observation/Value",
        "value");
    digester.addBeanPropertySetter(
        "Prior.Authorization/Authorization/Activity/Observation/ValueType",
        "valueType");

    digester.addSetNext(
        "Prior.Authorization/Authorization/Activity/Observation",
        "addObservation");

    digester.addSetNext("Prior.Authorization/Authorization/Activity",
        "addActivity");

    digester.addSetNext("Prior.Authorization/Authorization",
        "setAuthorization");
  }

  /**
   * Parses the.
   *
   * @param xml the xml
   * @return the e result
   * @throws IOException  Signals that an I/O exception has occurred.
   * @throws SAXException the SAX exception
   */
  public EResult parse(String xml) throws IOException, SAXException {
    PriorAuthorization auth = (PriorAuthorization) new ERxAuthorizationXmlParser().digester
        .parse(new StringReader(xml));
    return auth;
  }

  /**
   * The pbmdao.
   */
  PBMPrescriptionsDAO pbmdao = new PBMPrescriptionsDAO();

  /**
   * The pat med presc DAO.
   */
  GenericDAO patMedPrescDAO = new GenericDAO(
      "patient_medicine_prescriptions");

  /**
   * Validate prior authorization xml.
   *
   * @param desc             the desc
   * @param pbmPriorAuthBean the pbm prior auth bean
   * @param insuCompId       the insu comp id
   * @param tpaId            the tpa id
   * @param centerId         the center id
   * @return the string
   * @throws Exception the exception
   */
  public String validatePriorAuthorizationXml(PriorAuthorization desc,
      BasicDynaBean pbmPriorAuthBean, String insuCompId,
      String tpaId, Integer centerId) throws Exception {
    BasicDynaBean genPrefs = GenericPreferencesDAO.getAllPrefs();
    SimpleDateFormat dtFmt = new SimpleDateFormat("dd/MM/yyyy hh:mm");
    String errorMsg = "";
    String erxServiceRegNo = null;
    Integer erxCenterId = null;

    HttpSession session = RequestContext.getSession();

    PriorAuthorizationHeader header = desc.getHeader();
    if (header == null) {
      errorMsg = "Prior Auth. XML parsing failed: Prior Authorization Header element missing...";
      return errorMsg;
    } else {
      String transactionDate = header.getTransactionDate();

      erxCenterId = pbmPriorAuthBean.get("erx_center_id") != null
          ? (Integer) pbmPriorAuthBean.get("erx_center_id")
          : 0;
      if (erxCenterId != null && erxCenterId != 0) {
        BasicDynaBean centerbean = new CenterMasterDAO()
            .findByKey("center_id", erxCenterId);
        erxServiceRegNo = centerbean
            .get("hospital_center_service_reg_no") != null
            ? (String) centerbean
            .get("hospital_center_service_reg_no")
            : "";
      } else {
        erxServiceRegNo = genPrefs
            .get("hospital_service_regn_no") != null
            ? (String) genPrefs
            .get("hospital_service_regn_no")
            : "";
      }

      if (transactionDate.trim().length() <= 10) {
        transactionDate = transactionDate + " 00:00";
      }

      String senderId = header.getSenderID();
      if (senderId == null || senderId.equals("")) {
        errorMsg = "Prior Auth. XML parsing failed: SenderID not found in Header...";
        return errorMsg;
      }

      if (!TpaMasterDAO.getTPACode(tpaId, erxCenterId).equals(senderId)) {
        errorMsg = "Prior Auth. XML parsing failed: Insurance Company is not same as the ERx "
            + "Request sender...";
        return errorMsg;
      }

      String receiverID = header.getReceiverID();
      if (receiverID == null || receiverID.equals("")) {
        errorMsg = "Prior Auth. XML parsing failed: ReceiverID not found in Header...";
        return errorMsg;
      }

      if (erxServiceRegNo == null
          || !(erxServiceRegNo).equals(receiverID)) {
        errorMsg = "Prior Auth. XML parsing failed: Receiver ID not same as the center/pref. "
            + "service registration no. ...";
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

      List<String> columns = new ArrayList<String>();
      columns.add("pbm_presc_id");
      columns.add("erx_reference_no");
      columns.add("erx_presc_id");

      Map<String, Object> field = new HashMap<String, Object>();
      field.put("erx_presc_id",
          desc.getAuthorization().getAuthorizationID());
      BasicDynaBean prescbean = pbmdao.findByKey(columns, field);

      if (prescbean == null) {
        errorMsg = "Prior Auth. XML parsing failed: Invalid Auth. ID (or) No Request exists with "
            + "ID: " + authReqID;
        return errorMsg;
      }

      int pbmPrescId = (Integer) prescbean.get("pbm_presc_id");

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

      /*
       * if (end != null && (dtFmt.parse(end) == null || (new
       * java.util.Date()).before(dtFmt.parse(end)))) { errorMsg =
       * "Prior Auth. XML parsing failed: End Date value for Authorization is invalid..."
       * ; return errorMsg; }
       */

      String end = priorAuth.getEnd();
      if (end != null && (dtFmt.parse(end) == null)) {
        errorMsg = "Prior Auth. XML parsing failed: End Date value for Authorization is invalid...";
        return errorMsg;
      }

      String authDenialCode = priorAuth.getDenialCode();
      if (authDenialCode != null && !authDenialCode.trim().equals("")
          && insuranceDenialCodesDAO.findByKey("denial_code", authDenialCode) == null) {
        errorMsg =
            "Prior Auth. XML parsing failed: Denial Code value not valid for Authorization" + "...";
        return errorMsg;
      }

      ArrayList activities = priorAuth.getActivities();

      if (activities == null || activities.isEmpty()
          || activities.size() < 1) {
        errorMsg = "Prior Auth. XML parsing failed: No Activities found for Authorization with "
            + "PBM Prescription ID "
            + pbmPrescId + "...";
        return errorMsg;
      } else {
        for (int j = 0; j < activities.size(); j++) {
          PriorAuthorizationActivity activity = (PriorAuthorizationActivity) activities
              .get(j);
          String activityId = activity.getActivityID();
          String type = activity.getActivityType();
          String code = activity.getActivityCode();
          BigDecimal net = activity.getNet();
          String activityDenialCode = activity
              .getActivityDenialCode();
          BigDecimal quantity = activity.getQuantity();
          BigDecimal list = activity.getList();
          BigDecimal patientShare = activity.getPatientShare();
          BigDecimal paymentAmount = activity.getPaymentAmount();

          ArrayList observations = activity.getObservations();

          if (activityId == null || activityId.equals("")) {
            errorMsg = "Prior Auth. XML parsing failed: ID not found for Activity in "
                + "Authorization with PBM Prescription ID "
                + pbmPrescId + "...";
            return errorMsg;
          } else {

            int medPresId = Integer.parseInt(activityId);
            BasicDynaBean activityBean = patMedPrescDAO
                .findByKey("op_medicine_pres_id", medPresId);
            if (activityBean == null) {
              errorMsg = "Prior Auth. XML parsing failed: ID "
                  + activityId
                  + " for Activity not valid in Authorization with PBM Prescription ID "
                  + pbmPrescId + "...";
              return errorMsg;
            }

            Integer actPbmPrescId = activityBean
                .get("pbm_presc_id") != null
                ? (Integer) activityBean
                .get("pbm_presc_id")
                : null;
            if (actPbmPrescId == null
                || actPbmPrescId.intValue() != pbmPrescId) {
              errorMsg = "Prior Auth. XML parsing failed: Invalid ID "
                  + activityId
                  + " for Activity found in Authorization with PBM Prescription ID "
                  + pbmPrescId + "...";
              return errorMsg;
            }

            if (type == null || type.equals("")) {
              errorMsg = "Prior Auth. XML parsing failed: Type value not found for Activity "
                  + activityId
                  + " in Authorization with PBM Prescription ID "
                  + pbmPrescId + "...";
              return errorMsg;
            }

            if (code == null || code.equals("")) {
              errorMsg = "Prior Auth. XML parsing failed: Code value not found for Activity "
                  + activityId
                  + " in Authorization with PBM Prescription ID "
                  + pbmPrescId + "...";
              return errorMsg;
            }

            /*
             * if (net == null || net.equals("")) { errorMsg =
             * "Prior Auth. XML parsing failed: Net value not found for Activity "
             * +activityId+" in Authorization with PBM Prescription ID "
             * +pbmPrescId+"..."; return errorMsg; }
             */

            if (activityDenialCode != null && !activityDenialCode.trim().equals("")
                && insuranceDenialCodesDAO.findByKey("denial_code", activityDenialCode) == null) {
              errorMsg = "Prior Auth. XML parsing failed: Denial Code value not valid for Activity "
                  + activityId + " in Authorization with PBM Prescription ID " + pbmPrescId + "...";
              return errorMsg;
            }
          }
        } // end of activity validation
      }
    } // end of authorization validation

    return errorMsg;
  }
}
