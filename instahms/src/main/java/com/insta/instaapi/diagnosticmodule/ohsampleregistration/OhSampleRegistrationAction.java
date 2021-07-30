package com.insta.instaapi.diagnosticmodule.ohsampleregistration;

import com.bob.hms.common.RequestContext;
import com.bob.hms.diag.ohsampleregistration.OhSampleRegistrationBO;
import com.bob.hms.diag.ohsampleregistration.OhSampleRegistrationDAO;
import com.insta.hms.billing.ChargeDTO;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.diagnosticsmasters.addtest.AddTestDAOImpl;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.orders.OrderBO;
import com.insta.instaapi.common.ApiException;
import com.insta.instaapi.common.ApiResponse;
import com.insta.instaapi.common.ApiUtil;
import com.insta.instaapi.common.ScreenRights;
import com.insta.instaapi.common.ServletContextUtil;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// RC API: We should consider creating a APIAction base class from which all API end points should 
// derive. The API action should do all the routine stuff to be done by all APIs - like setting 
// session map, response type etc. Should also evaluate if it is possible to define a error object 
// which can give some consistency to what errors codes are sent for what conditions. Leaving this 
// to programmer discretion and spread all over the API code is risky. It can also be implemented as
// a filter if that makes sense. 
// Typical API method should look like below -
/* 
 * public ActionForward execute() {
 *  APIParameterParser parser = new APIParameterParser();
 *  APIData map = parser.parse(request);
 *  if !(getValidator().validate(map)) {
 *    sendErrorResponse();
 *  } else {
 *    returnValue = bo.businessMethod();
 *    if (success) {
 *      sendSuccessResponse();
 *    } else {
 *      sendErrorResponse();
 *    }
 * }
 */

public class OhSampleRegistrationAction extends Action {

  static Logger logger = LoggerFactory.getLogger(OhSampleRegistrationAction.class);

  static ArrayList<String> apiLog = new ArrayList<String>();

  @SuppressWarnings("rawtypes")
  private Map parseSessionParams(HttpServletRequest request) {

    String requestHandlerKey = ApiUtil.getRequestKey(request);

    ServletContext ctx = servlet.getServletContext();
    Map<String, Object> sessionMap = ServletContextUtil.getContextParametersMap(ctx);
    logger.info("requested sessionMap = " + sessionMap);
    Map sessionParameters = null;

    if (sessionMap != null && !sessionMap.isEmpty()) {
      sessionParameters = (Map) sessionMap.get(requestHandlerKey);
    }
    return sessionParameters;
  }

  private boolean isScreenRights(String requestHandalerKey, ServletContext ctx)
      throws SQLException {
    // Check the rights for ISR screen
    boolean isScreenRights = ScreenRights.getScreenRights(requestHandalerKey, ctx,
        "diagIncomingSampleReg");
    return isScreenRights;
  }

  @SuppressWarnings({ "rawtypes", "unchecked", })
  @Override
  public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException {

    apiLog.add("Processing ISR");
    logger.info("Processing ISR");
    OhSampleRegistrationBO ohBo = new OhSampleRegistrationBO();

    Map<String, Object> responseMap = new HashMap<String, Object>();
    Map sessionParams = parseSessionParams(request);

    // As per Anupama mam discussion, initially centerId will be hard coded as 0.
    // After listing all the center from the database it will set to the requested centeId.
    // successMsg ="valid request token,please login again";
    logger.info("Processing save prescription");
    try {

      if (sessionParams == null || sessionParams.isEmpty()) {
        throw new ApiException.InvalidRequestTokenException("Invalid Request Tocken");
      }
      RequestContext.setConnectionDetails(
          new String[] { "", "", (String) sessionParams.get("hospital_name"), "", "0" });

      boolean isScreenRights = isScreenRights(ApiUtil.getRequestKey(request),
          servlet.getServletContext());

      if (!isScreenRights) {
        throw new ApiException.InvalidScreenRightsException(
            "You don't have screen rights for Incoming Sample Registration");
      }

      Map<String, String[]> reqParam = request.getParameterMap();

      ValidateAPIParam.validate(reqParam);

      int centerIdInt = 0; // For non center specific hospital.
      List<BasicDynaBean> centerMaster = new GenericDAO("hospital_center_master").listAll();

      if (centerMaster.size() > 1) {
        String paramCenterName = ((String[]) reqParam.get("centerName"))[0];
        for (BasicDynaBean basicDynaBean : centerMaster) {

          if (((String) basicDynaBean.get("center_name")).equalsIgnoreCase(paramCenterName)
              && ((String) basicDynaBean.get("status")).equals("A")) {

            centerIdInt = (Integer) basicDynaBean.get("center_id");
            apiLog.add("++++++++++++++++++++++++++++++++++++++");
            apiLog.add("centerName : " + basicDynaBean.get("center_name"));
            apiLog.add("centerId : " + basicDynaBean.get("center_id"));
            apiLog.add("++++++++++++++++++++++++++++++++++++++");

          }

        }

        if (centerIdInt == 0) {
          throw new ApiException.DataNotFoundException(
              "Requested hospital is a center specific hospital, "
                  + "Please send the correct center name");
        }
      }

      RequestContext.setConnectionDetails(new String[] { "", "",
          (String) sessionParams.get("hospital_name"), "", Integer.toBinaryString(centerIdInt) });

      // RC API : We need a parameterParser class which can be instantiated and invoked to get a map
      // of all the parameters coming in from the request. We can provide a generic implementation
      // which can parse all the parameters coming in. Individual APIs should be able to provide
      // their own implementations for special parsing / defaults

      String[] testIds = reqParam.get("testId");
      // Constant if it is null then it will take by default GENERAL
      String salutation = request.getParameter("salutation");
      String patientName = request.getParameter("patientname");
      if (salutation != null) {
        patientName = salutation.concat(request.getParameter("patientname"));
      }
      String genderValue = request.getParameter("gender");

      Map<String, String> hisParams = new HashMap<String, String>();
      hisParams.put("hisToken", request.getParameter("hisToken"));
      hisParams.put("hisPatientID", request.getParameter("his_patientID"));

      if (!genderValue.trim().equals("M") && !genderValue.trim().equals("F")) {
        throw new ApiException.InvalidDataException(
            "Patient gender are not correct, It sholud be M/F");
      }
      String[] sampleTypeIds = reqParam.get("sampleTypeId");
      BasicDynaBean diagGenericPref = GenericPreferencesDAO.getdiagGenericPref();
      String isAutoGenerateSampleReq = (String) diagGenericPref.get("autogenerate_sampleid");
      if (sampleTypeIds == null && isAutoGenerateSampleReq.equalsIgnoreCase("Y")) {
        sampleTypeIds = new String[testIds.length];
      }
      String[] billRatePlan = reqParam.get("bill_rate_plan_id");
      if (billRatePlan == null || billRatePlan[0] == "") {
        billRatePlan = new String[] { "ORG0001" };
      }

      // It will be always DEP_LAB
      String category = request.getParameter("category");
      if (category == null) {
        category = "DEP_LAB";
      }

      // Below variable required for Bill
      String bedType = "GENERAL";

      BigDecimal qty = BigDecimal.ONE;

      // All lookup variable declared below
      // {"N"}, Lookup item is package or not (String[])reqParam.get("isPackage")
      String[] isPackages = new String[testIds.length];
      // {"N"},Lookup, If item is package and template type then yes
      // (String[])reqParam.get("testINpackage"),
      String[] testINpackage = new String[testIds.length];
      // {"O"}, Lookup Diagnostics table outhouse_type (String[])reqParam.get("houseType"),
      String[] houseType = new String[testIds.length];
      // {""};// Lookup data in case of package item (String[])reqParam.get("packageRefs"),
      String[] packageRefs = new String[testIds.length];
      // Lokkup data if the test is mapped with outsources
      String[] outSourceDestType = new String[testIds.length];
      // Lookup data if the test is mapped with outsources
      String[] outhouseId = new String[testIds.length];
      // Related to charge Lookup (String[])reqParam.get("charge"),
      String[] chargeStr = new String[testIds.length];
      // Lookup (String[])reqParam.get("disc"),
      String[] discStr = new String[testIds.length];
      // Lookup (String[])reqParam.get("amt");
      String[] amtStr = new String[testIds.length];

      // RC : Consider writing a query (or use an existing DAO method) which will get all the
      // details to be validated in one go
      // conducting doctor & sample ID should be queried in one go and validated
      // Yashwant-ANS :: sample_type_id depends on each index of testId thats why doing in for loop

      apiLog.add("Mapping Billcharge for each test");
      logger.info("Mapping Billcharge for each test");

      for (int i = 0; i < testIds.length; i++) {
        apiLog.add("Finding testDetails of testId :" + testIds[i]);

        BasicDynaBean testDetails = AddTestDAOImpl.getTestDetails(testIds[i], bedType,
            billRatePlan[0]);
        String status = (String) testDetails.get("status");

        if ("I".equals(status)) {
          throw new ApiException.InvalidDataException(
              "InActive tests cannot be order Test_Id : " + testIds[i]);
        }

        List<ChargeDTO> itemsCharge = OrderBO.getTestCharges(testDetails, qty, false, null, "in",
            null, null, null);

        List<BasicDynaBean> incTestDetailsList = new OhSampleRegistrationDAO()
            .getTestDetailsList(testIds[i], centerIdInt);

        if (incTestDetailsList.size() == 0) {
          houseType[i] = "I";
          outSourceDestType[i] = "";
          outhouseId[i] = "";
        } else if (incTestDetailsList.size() == 1) {
          houseType[i] = "O";
          outSourceDestType[i] = (String) incTestDetailsList.get(0).get("outsource_dest_type");
          outhouseId[i] = incTestDetailsList.get(0).get("outsource_dest_id").toString();
        } else {
          throw new ApiException.InvalidDataException(
              " Multiple outsource associated for the Test_Id :" + testIds[i]);
        }

        if (itemsCharge.get(0).getActRate().compareTo(BigDecimal.ZERO) == 0) {
          throw new ApiException.InvalidDataException(
              "Item charge are not defined  Test_Id :" + testIds[i]);
        }
        chargeStr[i] = itemsCharge.get(0).getActRate().toString();
        discStr[i] = itemsCharge.get(0).getDiscount().toString();
        amtStr[i] = Double
            .toString((Double.parseDouble(chargeStr[0]) - Double.parseDouble(discStr[0])));
        apiLog.add(testIds[i] + " : Mapped with the charge details");

        List<BasicDynaBean> testDetail = new GenericDAO("diagnostics").findAllByKey("test_id",
            testIds[i]);

        isPackages[i] = "N";
        testINpackage[i] = "N";
        // houseType[i]="";
        packageRefs[i] = "";

        String sampTypeId = null;
        // check for the sample type id(in case of Hl7 its coming)
        if (sampleTypeIds[i] == null) {
          sampTypeId = testDetail.get(0).get("sample_type_id").toString();
        } else { 
          sampTypeId = sampleTypeIds[i];
        }
        
        if ("n".equalsIgnoreCase(((String) testDetail.get(0).get("sample_needed")))) {
          sampleTypeIds[i] = "";
        } else if (((String) testDetail.get(0).get("sample_needed")).equalsIgnoreCase("Y")
            && (sampTypeId == null || sampTypeId.equals("0"))) {
          throw new ApiException.DataNotFoundException(
              "Missing sample_type_id in diagnostics master for  test_id " + testIds[i]);
        } else {
          sampleTypeIds[i] = sampTypeId;
        }
      }

      // RC : The BO method should be such that it can handle defaults. We should not force all the
      // parameters all the time.
      // Yashwant :: I need more clarity how it will be?

      apiLog.add("Validation and mapping done, processing registration ...");
      logger.info("Validation and mapping done, processing registration ...");
      String originalLabName = request.getParameter("orginalLabName");
      logger.info("Incoming Hospital Name : " + originalLabName);
      apiLog.add("Incoming Hospital Name : " + originalLabName);
      patientName = patientName.trim();
      String ageStr = request.getParameter("agefield");
      int age = Integer.parseInt(ageStr);
      if (age < 0) {
        throw new ApiException.InvalidDataException("Please enter correct patient age");
      }

      String[] originalSampleNo = reqParam.get("orig_sample_no");
      String[] sampleIds = reqParam.get("sampleId");
      String[] conductingDoctorId = reqParam.get("conducting_doctor_id");
      String referalDoctorname = request.getParameter("referralDoctorName");
      // Discount authorizer name is used from SELECT disc_auth_id,disc_auth_name,center_id
      // FROM discount_authorizer where status='A'
      String discountAuth = request.getParameter("discountAuthName");
      String phoneNo = request.getParameter("phone_no");
      String patOtherInfo = request.getParameter("patient_other_info");
      String referralDocId = request.getParameter("referralDocId");
      // being used with hl7 isr
      String colSampleDate = request.getParameter("colSampleDate");
      String userName = (String) sessionParams.get("customer_user_id");
      String userid = (String) sessionParams.get("customer_user_id");
      String billType = "BL";
      Map<String, String> visitMap = ohBo.processPrescriptions(testIds, isPackages, sampleIds,
          originalLabName, referalDoctorname, sampleTypeIds, isAutoGenerateSampleReq, category,
          originalSampleNo, centerIdInt, patientName, billType, userName, billRatePlan,
          discountAuth, phoneNo, ageStr, patOtherInfo, genderValue, referralDocId, outhouseId,
          packageRefs, userid, houseType, amtStr, discStr, conductingDoctorId, chargeStr,
          testINpackage, diagGenericPref, outSourceDestType, colSampleDate, hisParams);

      responseMap.put("patient_name", patientName);
      if (visitMap != null) {
        responseMap.put("INHOUSE_VISIT_ID", visitMap.get(patientName));
        apiLog.add(
            " PATIENT_NAME : " + patientName + "  INHOUSE_VISIT_ID " + visitMap.get(patientName));
        logger.info("{}", apiLog.add(
            " PATIENT_NAME : " + patientName + "  INHOUSE_VISIT_ID " + visitMap.get(patientName)));

      }

      apiLog.add("ISR completed successfully");
      logger.info("ISR completed successfully");
      responseMap.put("apiLog", new ArrayList(apiLog));
      ApiResponse.sendSuccessResponse(response, responseMap);
      return null;

    } catch (RuntimeException ex) {
      responseMap.put("apiLog", new ArrayList(apiLog));
      ApiResponse.sendErrorResponse(response, responseMap, ex);

      apiLog.add("ERROR : Exception Message : " + ex.getMessage());
      apiLog.add("ERROR : Exception StackTrace : " + ex);
      logger.error("", ex);
      ex.printStackTrace();
      return null;

    } catch (Exception ex) {

      responseMap.put("apiLog", new ArrayList(apiLog));
      ApiResponse.sendErrorResponse(response, responseMap, ex);

      apiLog.add("ERROR : Exception Message : " + ex.getMessage());
      apiLog.add("ERROR : Exception StackTrace : " + ex);
      logger.error("", ex);
      ex.printStackTrace();
      return null;

    } finally {

      apiLog.clear();

    }
  }

  static class ValidateAPIParam {

    // RC API : This method qualifies to be in the base class (may be an abstract method)
    @SuppressWarnings("rawtypes")
    private static void validate(Map requestParams) throws RuntimeException {

      apiLog.add("Validating api params");
      logger.info("Validating api params");

      logger.info(
          "patientname, agefield, gender, orginalLabName, testId, orig_sample_no, centerName");
      apiLog.add("---------------------------------------------------------");
      apiLog.add("Mandatory Fields Are : ");
      // Added mandatory fields in log , in a same sequence as <b>validateMandatoryFields</b> method
      // used.
      apiLog.add("patientname, agefield, gender, orginalLabName");
      apiLog.add("testId, orig_sample_no, centerName");
      apiLog.add("---------------------------------------------------------");

      validateMandatoryFields(requestParams.get("patientname"), requestParams.get("agefield"),
          requestParams.get("gender"), requestParams.get("orginalLabName"),
          requestParams.get("testId"), requestParams.get("orig_sample_no"),
          requestParams.get("centerName"));

      apiLog.add("Mandatory fields validation done, all fields are OK");
      String[] origSampleNo = (String[]) requestParams.get("orig_sample_no");
      String[] testIds = (String[]) requestParams.get("testId");
      if (origSampleNo.length != testIds.length) {
        throw new ApiException.MandatoryFieldsMissingException(
            "Mandatory fields are not supplied Field original_sample_no");
      }

      for (int i = 0; i < testIds.length; i++) {
        if (origSampleNo[i] == null || origSampleNo.equals("")) {
          throw new ApiException.MandatoryFieldsMissingException(
              " Original sample number is mandatory ");
        }
      }

      // validateMandatoryFields(responseMap, ageStr, gender, ....)
      // include other validations specific to this API such as
      // center id, original sample number, sample type id
    }

    private static void validateMandatoryFields(Object... args) throws RuntimeException {
      apiLog.add("Validating mandatory fields");
      logger.info("Validating mandatory fields");
      int count = 0;
      for (Object obj : args) {
        count++;
        if (obj == null) {
          apiLog.add("Missing argument index : " + count);
          throw new ApiException.MandatoryFieldsMissingException(
              "Mandatory fields are not supplied in request Parameter!"
                  + " Pls check url and list of mandatory Field");
        } else if ((obj instanceof String) && "".equals((String) obj)) {
          apiLog.add("Mossing argument index : " + count);
          throw new ApiException.MandatoryFieldsMissingException(
              "Mandatory fields might be empty or not supplied in request Parameter "
                  + " Pls check url and list of mandatory Field");
        }
        if (obj == null || (obj instanceof String[] && ((String[]) obj).length == 0)) {
          apiLog.add("Mossing argument index : " + count);
          throw new ApiException.MandatoryFieldsMissingException(
              "Mandatory fields might be empty or not supplied in request Parameter "
                  + " Pls check url and list of mandatory Field");
        } else if ((obj instanceof String[]) && ("".equals(((String[]) obj)[0]))) {
          apiLog.add("Mossing argument index : " + count);
          throw new ApiException.MandatoryFieldsMissingException(
              "Mandatory fields might be empty or not supplied in request Parameter "
                  + " Pls check url and list of mandatory Field");
        }
      }
    }

  }

}
