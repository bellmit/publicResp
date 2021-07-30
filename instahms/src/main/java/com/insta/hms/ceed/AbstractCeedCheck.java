package com.insta.hms.ceed;

import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.ceed.generated_test.GatewaySoap;
import com.insta.hms.ceed.generated_test.Response;
import com.insta.hms.ceed.generated_test.Response.ClaimEdit.Edit;
import com.insta.hms.common.AppInit;
import com.insta.hms.vitalForm.VisitVitalsDAO;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.ws.Holder;

/*
 * This is abstract class for ceed integration
 */
public abstract class AbstractCeedCheck {

  enum FLAG {
    ERROR, SUCCESS
  }

  Logger logger = LoggerFactory.getLogger(AbstractCeedCheck.class);

  // This map contains valid activity code types for a service type.
  Map<String, List<String>> validActivityCodeTypesMap = new HashMap<String, List<String>>();

  // This map contains valid diagnosis code types for a service type.
  Map<String, List<String>> validDiagnosisCodeTypesMap = new HashMap<String, List<String>>();

  // This list has all valid encounter types
  List<String> validEncounterTypes = new ArrayList<String>();

  // This list has valid encounter start types
  List<String> validEncounterStartTypes = new ArrayList<String>();

  // This list has valid encounter end types
  List<String> validEncounterEndTypes = new ArrayList<String>();

  // This list has valid observation types
  List<String> validObservationTypes = new ArrayList<String>();

  // This list has valid gender values
  List<String> validGenderValues = new ArrayList<String>();

  // This is ceed transaction failure message
  String ceedTransactionFailureMessage = "CEED: Transaction failed.";

  /**
   * Constructor.
   */
  public AbstractCeedCheck() {
    validActivityCodeTypesMap.put("1", Arrays.asList("CPT4v2012", "CPT4v2011", "HCPCS", "ASL"));
    validActivityCodeTypesMap.put("2", Arrays.asList("CPT4v2012", "CPT4v2011", "HCPCS"));

    validDiagnosisCodeTypesMap.put("1", Arrays.asList("ICD9", "ICD10"));
    validDiagnosisCodeTypesMap.put("2", Arrays.asList("ICD9", "ICD10"));

    validEncounterTypes = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "12", "13",
        "15", "41", "42");
    validEncounterStartTypes = Arrays.asList("1", "2", "3", "4", "5", "6", "7");
    validEncounterEndTypes = Arrays.asList("1", "2", "3", "4", "5", "6", "7");

    validObservationTypes = Arrays.asList("LOINC", "Text", "File", "UniversalDental", "Financial",
        "Grouping", "Modifier");
    validGenderValues = Arrays.asList("M", "F", "U");
  }

  /**
   * Get all activities for a consultation.
   * 
   * @param consultationId Consultation ID
   * @param visitId        Visit ID
   * @return List of activities
   * @throws SQLException the SQL Exception
   */
  public abstract List<BasicDynaBean> getActivities(String consultationId, String visitId)
      throws SQLException;

  /**
   * Insert activities.
   * 
   * @param con            Database Connection
   * @param allActivities  List of activities to be inserted
   * @param claimId        Claim ID
   * @param consultationId Consultation ID
   * @param visitId        Visit ID
   * @param serviceType    Service Type
   * @return Response Map
   * @throws SQLException the SQL Exception
   * @throws IOException  the IO Exception
   */
  public abstract Map insertActivitiesInCeedIntegrationDetails(Connection con,
      List<BasicDynaBean> allActivities, int claimId, Integer consultationId, String visitId,
      String serviceType) throws SQLException, IOException;

  /**
   * Process Response.
   * 
   * @param con      Database Connection
   * @param response the Response
   * @param claimId  Claim ID
   * @return Response Map
   * @throws SQLException          the SQL Exception
   * @throws NumberFormatException the Number Format Exception
   * @throws IOException           the IO Exception
   */
  public abstract Map processResponse(Connection con, Holder<Response> response, int claimId)
      throws NumberFormatException, SQLException, IOException;

  /**
   * Insert request details.
   * @param con            Database Connection
   * @param claimId        Claim ID
   * @param consultationId Consultation ID
   * @param visitId        Visit ID
   * @param status         Status to set
   * @param serviceType    Service Type
   * @return returns true or false representing success of operation
   * @throws SQLException the SQL exception
   * @throws IOException  the IO Exception
   */
  public abstract boolean insertRequestDetailsInCeedMain(Connection con, int claimId,
      Integer consultationId, String visitId, char status, char serviceType)
      throws SQLException, IOException;

  /**
   * Get observations.
   * 
   * @param visitId Visit ID
   * @return Response Map
   * @throws SQLException the SQL Exception
   */
  public abstract Map getObservations(String visitId) throws SQLException;

  /**
   * Get encounter details.
   * 
   * @param visitId Visit ID
   * @return Response Map
   * @throws SQLException the SQL Exception
   */
  public abstract BasicDynaBean getEncounterDetails(String visitId) throws SQLException;

  /**
   * Checks if a code type is valid or not.
   * 
   * @param codeType       Code type to validate
   * @param validCodeTypes List of valid code types
   * @return true or false based on validation
   */
  public boolean isValidCodeType(String codeType, List<String> validCodeTypes) {
    return codeType != null && validCodeTypes.contains(codeType);
  }

  /**
   * Get transaction failure error message map.
   * 
   * @param ceedTransactionFailureMessage CEED transaction failure message
   * @return transaction failure error message map
   */
  public Map returnTransactionFailureMessageMap(String ceedTransactionFailureMessage) {
    Map<String, Object> ret = new HashMap<>();
    ret.put("flag", FLAG.ERROR);
    ret.put("message", ceedTransactionFailureMessage);
    return ret;
  }

  /**
   * Get list of diagnosis for a visit.
   * 
   * @param visitId     Visit ID
   * @param serviceType Service Type
   * @return list of diagnosis for a visit
   * @throws SQLException the SQL Exception
   */
  public List<BasicDynaBean> getDiagnoses(String visitId, String serviceType) throws SQLException {
    // get list of all diagnoses
    List<BasicDynaBean> diagnoses = CeedDAO.getAllDiagnoses(visitId);

    List<String> validDiagnosisCodeTypes = (List<String>) validDiagnosisCodeTypesMap
        .get(serviceType);
    List<BasicDynaBean> diagnosesToSend = new ArrayList<BasicDynaBean>();

    Iterator<BasicDynaBean> iter = diagnoses.iterator();
    while (iter.hasNext()) {
      BasicDynaBean bean = iter.next();
      String diagnosisCodeType = (String) bean.get("code_type_classification");
      String diagnosisCode = (String) bean.get("icd_code");
      if (isValidCodeType(diagnosisCodeType, validDiagnosisCodeTypes) && diagnosisCode != null) {
        diagnosesToSend.add(bean);
      }
    }
    return diagnosesToSend;
  }

  /**
   * performs ceed request by generating xml, sending request to service, processing response.
   * @param con            Database Connection
   * @param consId         Consultation ID
   * @param visitId        Visit ID
   * @param serviceType    Service Type
   * @return Message 
   * @throws NumberFormatException the Number Format Exception
   * @throws SQLException the SQL exception
   * @throws IOException  the IO Exception
   * @throws TemplateException the Template Exception
   */
  public String performCeedCheck(Connection con, String consId, String visitId, String serviceType)
      throws NumberFormatException, SQLException, IOException, TemplateException {
    // get encounter type
    BasicDynaBean encounterDetails = getEncounterDetails(visitId);
    String encounterType = null;
    String encounterStartType = null;
    String encounterEndType = null;

    if (encounterDetails != null) {
      Integer encounterTypeInt = (Integer) encounterDetails.get("encounter_type");

      if (encounterTypeInt != null) {
        encounterType = encounterTypeInt.toString();
      }

      Integer encounterStartTypeFromDB = (Integer) encounterDetails.get("encounter_start_type");
      Integer encounterEndTypeFromDB = (Integer) encounterDetails.get("encounter_end_type");
      if (encounterStartTypeFromDB != null
          && validateEncounterStartType((encounterStartTypeFromDB).toString())) {
        encounterStartType = (encounterStartTypeFromDB).toString();
      }
      if (encounterEndTypeFromDB != null
          && validateEncounterEndType((encounterEndTypeFromDB).toString())) {
        encounterEndType = (encounterEndTypeFromDB).toString();
      }
    }

    if (!validateEncounterType(encounterType)) {
      return "CEED : Invalid Encounter Type. Ceed Code Check cannot be done.";
    }

    logger.debug("Get diagnoses and Activities Start.");
    // get list of all diagnoses
    List<BasicDynaBean> diagnoses = getDiagnoses(visitId, serviceType);

    // error out if number of diagnoses is zero
    if (diagnoses.isEmpty()) {
      return "CEED : Zero Diagnoses with code type "
          + validDiagnosisCodeTypesMap.get(serviceType).toString()
          + "! CEED Code Check Cannot be done.";
    }
    // get next claim id
    int claimId = CeedDAO.getNextClaimId();
    // get list of all activities
    List<BasicDynaBean> allActivities = getActivities(consId, visitId);

    // insert ceed request details in database before sending the request
    Integer consultationId = null;
    if (consId != null) {
      consultationId = Integer.parseInt(consId);
    }

    Map ret = insertActivitiesInCeedIntegrationDetails(con, allActivities, claimId, consultationId,
        visitId, serviceType);
    if (ret.get("flag").equals(FLAG.ERROR)) {
      return (String) ret.get("message");
    }
    List<DynaBean> activitiesToSend = (List<DynaBean>) ret.get("list");
    int noActivitiesToSend = activitiesToSend.size();
    // error out if number of activities with CPT or HCPCS code are zero
    if (noActivitiesToSend == 0) {
      return "CEED : Zero Activities with code type "
          + validActivityCodeTypesMap.get(serviceType).toString()
          + " ! CEED Code Check Cannot be done.";
    }

    Map observationsToSend = getObservations(visitId);

    VisitDetailsDAO rdao = new VisitDetailsDAO();
    BasicDynaBean visitBean = rdao.findByKey("patient_id", visitId);
    BasicDynaBean personDetails = CeedDAO.getPersonDetails((String) visitBean.get("mr_no"));
    BasicDynaBean vitalBean = VisitVitalsDAO.getVisitVitalWeightBean(visitId);

    ret = generateRequestXml(visitId, claimId, encounterType, encounterStartType,
        encounterEndType, personDetails, diagnoses, activitiesToSend, observationsToSend,
        vitalBean);

    // error out if there is any error while generating xml
    if (ret.get("flag") == FLAG.ERROR) {
      return (String) ret.get("message");
    }

    String generatedXml = null;
    if (ret.get("flag") == FLAG.SUCCESS) {
      generatedXml = (String) ret.get("message");
    }

    // log request xml
    logger.debug("CEED Request :\n"
        + generatedXml);

    // create holders for response
    Holder<Integer> dhcegResult = new Holder<Integer>();
    Holder<Response> response = new Holder<Response>();
    Holder<String> infoMessage = new Holder<String>();

    // get credentials to send ceed request
    BasicDynaBean credentialsBean = CeedDAO.getCredentialsBean("ceed");
    String userName = (String) credentialsBean.get("userid");
    String password = (String) credentialsBean.get("password");

    logger.debug("Ceed Start.");
    // update request time stamp
    CeedDAO.updateRequestTime(con, claimId);
    // send ceed request
    try {
      sendRequest(serviceType, generatedXml, userName, password, dhcegResult, response,
          infoMessage);
    } catch (CEEDInternetConnectionException ex) {
      return "CEED : Web Service not reachable. CEED Request couldn't be sent."
          + " Check your Internet Connection.";
    }

    // generate and log response xml
    logger.debug("CEED Response :\n"
        + generateResponseXml(response));

    logger.debug("{}", dhcegResult.value);
    logger.debug("Ceed End.");
    // if request is not processed error out
    if (dhcegResult.value == 0) {
      CeedDAO.updateResponseInfo(con, claimId, infoMessage.value);
      return "CEED : "
          + infoMessage.value;
    } else { // otherwise
      ret = processResponse(con, response, claimId);
      if (ret.get("flag").equals(FLAG.ERROR)) {
        return (String) ret.get("message");
      }
    }

    return null;
  }

  /**
   * convert list of edits to map.
   * @param edits List of edits
   * @return Map containing edits
   */
  public Map getAllEdits(List<Edit> edits) {
    Map editsMap = new HashMap();
    Iterator<Edit> itr = edits.iterator();

    while (itr.hasNext()) {
      Edit edit = itr.next();
      String editSubType = edit.getSubType();
      String editCode = edit.getCode();
      String editRank = edit.getRank().toString();
      String editComment = edit.getComment();

      Map editvaluemap = new HashMap();

      editvaluemap.put("edit_sub_type", editSubType);
      editvaluemap.put("edit_code", editCode);
      editvaluemap.put("edit_rank", editRank);
      editvaluemap.put("edit_comment", editComment);

      String editId = edit.getID();
      editsMap.put(editId, editvaluemap);
    }

    return editsMap;
  }

  /**
   * Generate request XML.
   * @param visitId Visit ID
   * @param claimId Claim ID
   * @param encounterType Encounter Type
   * @param encounterStartType Encounter Start Type
   * @param encounterEndType Encounter End Type
   * @param personDetails Patient Details
   * @param diagnoses List of diagnosis
   * @param activities List of activities
   * @param observations List of observations
   * @param vitalBean Vital bean
   * @return Response Map
   * @throws IOException  the IO Exception
   * @throws TemplateException the Template Exception
   */
  public Map generateRequestXml(String visitId, int claimId, String encounterType,
      String encounterStartType, String encounterEndType, BasicDynaBean personDetails,
      List diagnoses, List activities, Map observations, BasicDynaBean vitalBean)
      throws IOException, TemplateException {

    HashMap<String, Object> map = new HashMap<>();

    String weight = null;
    if (vitalBean != null && (String) vitalBean.get("param_label") != null
        && ((String) vitalBean.get("param_label")).equalsIgnoreCase("Weight")) {
      weight = (String) vitalBean.get("param_value");
      if (isValidWeight(weight)) {
        String units = ((String) vitalBean.get("param_uom"));
        if (units.equalsIgnoreCase("kgs") || units.equalsIgnoreCase("kg")) {
          // sending ceil of double value in ceed request
          Double weightdouble = Double.parseDouble(weight);
          int weightint = new Double(Math.floor(weightdouble + 0.50)).intValue();
          map.put("weight", weightint);
        } else {
          Map<String, Object> ret = new HashMap<>();
          ret.put("flag", FLAG.ERROR);
          ret.put("message", "CEED : Weight is not in kgs");

          return ret;
        }
      }
    }

    // process person details
    String name = (String) personDetails.get("patname");
    String gender = (String) personDetails.get("patient_gender");
    Date dateofbirth = (Date) personDetails.get("dateofbirth");

    map.put("name", name);
    if (validateGender(gender)) {
      map.put("gender", gender);
    }
    map.put("dateofbirth", dateofbirth);
    map.put("patientId", visitId);
    map.put("claimId", claimId);
    map.put("encounterType", encounterType);
    map.put("encounterStartType", encounterStartType);
    map.put("encounterEndType", encounterEndType);
    map.put("diagnoses", diagnoses);
    map.put("activities", activities);

    if (observations != null) {
      map.put("observations", observations);
    }

    Configuration cfg = AppInit.getFmConfig();
    String ceedftlpath = "/Ceed/ceedrequest.ftl";
    Template tmpl = cfg.getTemplate(ceedftlpath.trim());
    StringWriter writer = new StringWriter();
    tmpl.process(map, writer);
    Map ret = new HashMap();

    ret.put("flag", FLAG.SUCCESS);
    ret.put("message", writer.toString());

    return ret;
  }

  /**
   * Generate response XML.
   * @param responseholder response holder
   * @return XML markup
   * @throws IOException the IO Exception
   * @throws TemplateException the Template Exception
   */
  public String generateResponseXml(Holder<Response> responseholder)
      throws IOException, TemplateException {
    Response response = responseholder.value;
    Configuration cfg = AppInit.getFmConfig();
    String ceedftlpath = "/Ceed/ceedresponse.ftl";
    Template tmpl = cfg.getTemplate(ceedftlpath.trim());

    HashMap map = new HashMap();
    map.put("response", response);
    StringWriter writer = new StringWriter();
    tmpl.process(map, writer);

    String responseXml = writer.toString();
    return responseXml;
  }

  /**
   * Validate weight.
   * @param weight weight value to validate
   * @return true or false based on validation
   */
  public boolean isValidWeight(String weight) {
    if (weight == null || weight.isEmpty()) {
      return false;
    }
    try {
      Double.parseDouble(weight);
    } catch (NumberFormatException ex) {
      return false;
    }
    return true;
  }

  /**
   * Send request.
   * @param service the service
   * @param generatedXml genrated XML
   * @param userName ceed user name
   * @param password ceed password
   * @param dhcegResult the dhceg result
   * @param response the response
   * @param infoMessage the information message
   * @throws CEEDInternetConnectionException connection errors
   */
  public void sendRequest(String service, String generatedXml, String userName, String password,
      Holder<Integer> dhcegResult, Holder<Response> response, Holder<String> infoMessage)
      throws CEEDInternetConnectionException {

    GatewaySoap ceedgtwysoap = null;
    try {
      ceedgtwysoap = CEEDUtil.getGateWaySoap();
    } catch (CEEDInternetConnectionException ex) {
      throw ex;
    }

    SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss:SSS");
    logger.debug("Start "
        + df.format(new Date(Calendar.getInstance().getTimeInMillis())));
    try {
      ceedgtwysoap.dhceg(service, generatedXml, userName, password, dhcegResult, response,
          infoMessage);
    } catch (Throwable ex) {
      logger.error(ex.getMessage());
      throw new CEEDInternetConnectionException(ex.getMessage());
    }
    logger.debug("End "
        + df.format(new Date(Calendar.getInstance().getTimeInMillis())));
  }

  /**
   * Validate encounter type.
   * @param encounterType encounter type to validate
   * @return true or false based on validation
   */
  public boolean validateEncounterType(String encounterType) {
    return encounterType != null && validEncounterTypes.contains(encounterType);
  }

  /**
   * Validate encounter start type.
   * @param encounterStartType encounter start type to validate
   * @return true or false based on validation
   */
  public boolean validateEncounterStartType(String encounterStartType) {
    return encounterStartType != null && validEncounterTypes.contains(encounterStartType);
  }

  /**
   * Validate encounter end type.
   * @param encounterEndType encounter end type to validate
   * @return true or false based on validation
   */
  public boolean validateEncounterEndType(String encounterEndType) {
    return encounterEndType != null && validEncounterTypes.contains(encounterEndType);
  }

  /**
   * Validate observation type.
   * @param observationType observation type to validate
   * @return true or false based on validation
   */
  public boolean validateObservationType(String observationType) {
    return observationType != null && validEncounterTypes.contains(observationType);
  }

  /**
   * Validate Gender.
   * @param gender gender to validate
   * @return true or false based on validation
   */
  public boolean validateGender(String gender) {
    return gender != null && validGenderValues.contains(gender);
  }
}
