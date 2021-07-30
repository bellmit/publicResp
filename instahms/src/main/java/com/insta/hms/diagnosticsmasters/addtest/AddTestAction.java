package com.insta.hms.diagnosticsmasters.addtest;

import com.bob.hms.adminmasters.organization.OrgMasterDao;
import com.bob.hms.adminmasters.organization.RateMasterDao;
import com.bob.hms.common.AutoIncrementId;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.HsSfWorkbookUtils;
import com.bob.hms.common.RequestContext;
import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.diagnosticmodule.prescribetest.DiagnoDAOImpl;
import com.insta.hms.diagnosticsmasters.Result;
import com.insta.hms.diagnosticsmasters.ResultRangesDAO;
import com.insta.hms.diagnosticsmasters.Test;
import com.insta.hms.diagnosticsmasters.TestTemplate;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.DiagCenterResultsApplicability.DiagResultsCenterApplicabilityDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.ServiceSubGroup.ServiceSubGroupDAO;
import com.insta.hms.xls.exportimport.ChargesImportExporter;
import com.insta.hms.xls.exportimport.DetailsImportExporter;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class AddTestAction.
 */
public class AddTestAction extends DispatchAction {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(AddTestAction.class);

  /** The test chargesdao. */
  TestChargesDAO testChargesdao = new TestChargesDAO();
  
  private static final GenericDAO hl7LabInterfaces = new GenericDAO("hl7_lab_interfaces");
  private static final GenericDAO mrdSupportedCodes = new GenericDAO("mrd_supported_codes");
  private static final GenericDAO histoImpressionMasterDAO =
      new GenericDAO("histo_impression_master");
  private static final GenericDAO microAbstAntibioticMasterDAO =
      new GenericDAO("micro_abst_antibiotic_master");
  private static final GenericDAO diagnosticsDao = new GenericDAO("diagnostics");
  private static final GenericDAO diagMethodologyMasterDAO =
      new GenericDAO("diag_methodology_master");
  private static final GenericDAO testOrgDetails = new GenericDAO("test_org_details");
  private static final GenericDAO testResultsMaster = new GenericDAO("test_results_master");
  
  
  /**
   * Lists all the tests as a dashboard. (pages/masters/hosp/diagnostics/TestList.jsp).
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param req
   *          the req
   * @param response
   *          the response
   * @return the action forward
   * @throws SQLException
   *           the SQL exception
   * @throws Exception
   *           the exception
   */
  public ActionForward listTests(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse response) throws SQLException, Exception {

    Map requestParams = new HashMap();
    requestParams.putAll(req.getParameterMap());
    String orgId = req.getParameter("org_id");
    if (orgId == null || orgId.equals("")) {
      String[] orgIdArray = { "ORG0001" };
      requestParams.put("org_id", orgIdArray);
      orgId = "ORG0001";
    }

    PagedList list = AddTestDAOImpl.searchTests(requestParams,
        ConversionUtils.getListingParameter(requestParams));

    List<String> testIds = new ArrayList<>();
    for (Map obj : (List<Map>) list.getDtoList()) {
      testIds.add((String) obj.get("test_id"));
    }

    List<String> bedTypes = new BedMasterDAO().getUnionOfBedTypes();
    List testChargesList = AddTestDAOImpl.getTestChargesForAllBedTypes(orgId, bedTypes, testIds);
    Map testChargesMap = ConversionUtils.listBeanToMapMap(testChargesList, "test_id");

    JSONSerializer js = new JSONSerializer().exclude("class");

    req.setAttribute("pagedList", list);
    req.setAttribute("bedTypes", bedTypes);
    req.setAttribute("testCharges", testChargesMap);
    req.setAttribute("testnames", js.serialize(AddTestDAOImpl.getAllTestNames()));
    req.setAttribute("orgId", orgId);
    req.setAttribute("loggedInCenter", RequestContext.getCenterId());
    return mapping.findForward("getTestListScreen");
  }

  /**
   * Gets the adds the test.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the adds the test
   * @throws SQLException
   *           the SQL exception
   */
  public ActionForward getAddTest(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException {
    saveToken(request);
    request.setAttribute("newEdit", "new");
    request.setAttribute("method", "insertTestDetails");
    setAttributes(request);
    JSONSerializer js = new JSONSerializer().exclude("class");
    request.setAttribute("testList", js.serialize((AddTestDAOImpl.getTestNames())));
    request.setAttribute("orgId", request.getParameter("orgId"));
    request.setAttribute("serviceSubGroupsList",
        js.serialize(ServiceSubGroupDAO.getAllActiveServiceSubGroups()));
    request.setAttribute("hl7Interfaces", hl7LabInterfaces.listAll(null, "status", "A"));
    request.setAttribute("codeTypesJSON", js.serialize(ConversionUtils.copyListDynaBeansToMap(
        mrdSupportedCodes.listAll(null, "code_category", "Observations"))));
    request.setAttribute("impressions",
        histoImpressionMasterDAO.findAllByKey("status", "A"));
    request.setAttribute("antibiotics", microAbstAntibioticMasterDAO.listAll());
    request.setAttribute("test_timestamp", new DiagnoDAOImpl().getCountFromDiagTimeStamp());
    request.setAttribute("methodologies", js.serialize(ConversionUtils.copyListDynaBeansToMap(
        diagMethodologyMasterDAO.findAllByKey("status", "I"))));
    List centers = CenterMasterDAO.getCentersList();
    request.setAttribute("centers_json",
        js.deepSerialize(ConversionUtils.copyListDynaBeansToMap(centers)));
    return mapping.findForward("getAddTestScreen");
  }

  /**
   * Gets the edits the test.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the edits the test
   * @throws SQLException
   *           the SQL exception
   */
  public ActionForward getEditTest(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException {

    saveToken(request);
    JSONSerializer js = new JSONSerializer().exclude("class");
    String testId = request.getParameter("testid");
    AddTestForm atf = (AddTestForm) form;

    List<BasicDynaBean> testDetails = AddTestDAOImpl.getTestDetails1(testId);

    BasicDynaBean bean = null;
    for (int i = 0; i < testDetails.size(); i++) {
      bean = testDetails.get(i);
      String testName = (String) (bean).get("test_name");
      testId = (String) (bean).get("test_id");

      BigDecimal statCharge = (BigDecimal) (bean).get("stat_charge");
      BigDecimal scheduleCharge = (BigDecimal) (bean).get("schedule_charge");
      Integer typeOfSpeciman = (Integer) (bean).get("sample_type_id");
      request.setAttribute("typeOfSpeciman", typeOfSpeciman);
      request.setAttribute("testName", testName);
      List centers = CenterMasterDAO.getCentersList();
      request.setAttribute("centers_json",
          js.deepSerialize(ConversionUtils.copyListDynaBeansToMap(centers)));
      int serviceSubGroupId = (Integer) (bean).get("service_sub_group_id");
      String groupId = new ServiceSubGroupDAO().findByKey("service_sub_group_id", serviceSubGroupId)
          .get("service_group_id").toString();
      request.setAttribute("groupId", groupId);
      request.setAttribute("serviceSubGroup", serviceSubGroupId);

      atf.setTestName(testName);
      atf.setTestId(testId);
      String diagCode = (String) (bean).get("diag_code");
      atf.setDiagCode(diagCode);
      atf.setSpecimen(typeOfSpeciman);
      String needOfSample = (String) (bean).get("sample_needed");
      atf.setSampleNeed(needOfSample);
      String ddeptId = (String) (bean).get("ddept_id");
      atf.setDdeptId(ddeptId);
      BigDecimal routineCharge = (BigDecimal) (bean).get("routine_charge");
      atf.setRoutineCharge(routineCharge.toString());
      /*
       * atf.setStatCharge(statCharge.toString()); atf.setScheduleCharge(scheduleCharge.toString());
       */
      String conductionInTemplate = (String) (bean).get("conduction_format");
      atf.setReportGroup(conductionInTemplate);
      atf.setTestStatus((String) (bean).get("status"));
      atf.setConduction_applicable((Boolean) bean.get("conduction_applicable"));
      atf.setResults_entry_applicable((Boolean) bean.get("results_entry_applicable"));
      atf.setConducting_doc_mandatory((String) bean.get("conducting_doc_mandatory"));
      atf.setHl7ExportCode((String) bean.get("hl7_export_code"));
      // atf.setHl7ExportCode((String[])bean.get("hl7_export_code"));
      atf.setSampleCollectionInstructions((String) bean.get("sample_collection_instructions"));
      atf.setConductionInstructions((String) bean.get("conduction_instructions"));
      atf.setPreAuthReq((String) bean.get("prior_auth_required"));
      atf.setResultsValidation((String) bean.get("results_validation"));
      atf.setRemarks((String) (bean).get("remarks"));
      atf.setAllow_rate_increase((Boolean) bean.get("allow_rate_increase"));
      atf.setAllow_rate_decrease((Boolean) bean.get("allow_rate_decrease"));
      atf.setMandate_additional_info((String) bean.get("mandate_additional_info"));
      atf.setTest_additional_info((String) (bean).get("additional_info_reqts"));
      String conductingRoleId = (String) bean.get("conducting_role_id");
      atf.setConductingRoleIds(conductingRoleId != null ? conductingRoleId.split(",") : null);
      String ddeptName = (String) (bean).get("ddept_name");
      request.setAttribute("ddeptName", ddeptName);
      request.setAttribute("reportGroup", conductionInTemplate);
      request.setAttribute("cApplicable", bean.get("conduction_applicable"));
      request.setAttribute("ResEntryApplicable", bean.get("results_entry_applicable"));
      request.setAttribute("cDocRequired", bean.get("conducting_doc_mandatory"));
      request.setAttribute("allRateIncr", bean.get("allow_rate_increase"));
      request.setAttribute("allRateDcr", bean.get("allow_rate_decrease"));
      request.setAttribute("testDetails", bean);
      request.setAttribute("loggedInCenter", RequestContext.getCenterId());
      request.setAttribute("testIdforCenter", testId);
      if (null != conductionInTemplate && conductionInTemplate.equals("T")) {
        ArrayList<String> templateList = AddTestDAOImpl.getTemplateList(testId);
        String[] templates = null;
        templates = populateListValuesTOArray(templates, templateList);
        atf.setFormatName(templates);
      }

      /*
       * Getting interface for each test and setting it into form for displaying on UI as a selected
       */
      ArrayList<String> interfaceList = AddTestDAOImpl.getHl7InterfaceDetails(testId);
      String[] interfaceNames = null;
      interfaceNames = populateListValuesTOArray(interfaceNames, interfaceList);
      atf.setHl7ExportInterface(interfaceNames);

    }

    // request.setAttribute("testDetails", js.serialize(testDetails));
    request.setAttribute("testResults", AddTestDAOImpl.getTestResults(testId));
    request.setAttribute("testsRanges", ConversionUtils
        .copyListDynaBeansToMap(ResultRangesDAO.listAllTestResultReferences(testId)));
    request.setAttribute("newEdit", "edit");
    request.setAttribute("method", "updateTestDetails");
    setAttributes(request);
    request.setAttribute("testId", testId);
    String orgId = request.getParameter("orgId");
    request.setAttribute("orgId", orgId);
    String orgName = request.getParameter("orgName");
    request.setAttribute("orgName", orgName);
    request.setAttribute("insurance_category_id", bean.get("insurance_category_id"));
    request.setAttribute("serviceSubGroupsList",
        js.serialize(ServiceSubGroupDAO.getAllActiveServiceSubGroups()));
    request.setAttribute("hl7Interfaces", hl7LabInterfaces.listAll(null, "status", "A"));
    request.setAttribute("codeTypesJSON", js.serialize(ConversionUtils.copyListDynaBeansToMap(
        mrdSupportedCodes.listAll(null, "code_category", "Observations"))));
    request.setAttribute("impressions",
        histoImpressionMasterDAO.findAllByKey("status", "A"));
    request.setAttribute("antibiotics", microAbstAntibioticMasterDAO.listAll());
    request.setAttribute("test_timestamp", new DiagnoDAOImpl().getCountFromDiagTimeStamp());
    request.setAttribute("methodologies", js.serialize(ConversionUtils.copyListDynaBeansToMap(
        diagMethodologyMasterDAO.findAllByKey("status", "I"))));
    request.setAttribute("results_json", js.deepSerialize(ConversionUtils
        .copyListDynaBeansToMap(DiagResultsCenterApplicabilityDAO.getResultsListForJson(testId))));
    request.setAttribute("hl7mappingRecords", AddTestDAOImpl.getHl7MappingDetails(testId));
    return mapping.findForward("getAddTestScreen");
  }

  /**
   * Insert test details.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws Exception
   *           the exception
   */
  public ActionForward insertTestDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {
    if (isTokenValid(request, true)) {
      resetToken(request);
      Map requestMap = request.getParameterMap();
      String[] codeType = (String[]) requestMap.get("code_type");
      String[] resultCode = (String[]) requestMap.get("result_code");
      String[] dataAllowed = (String[]) requestMap.get("data_allowed");
      String[] sourceIfList = (String[]) requestMap.get("source_if_list");
      String[] hl7Interface = (String[]) requestMap.get("hl7_interface");
      String[] methodIds = (String[]) requestMap.get("methodId");
      String[] conductingRoles = request.getParameterValues("conductingRoleId");
      FlashScope scope = FlashScope.getScope(request);

      request.setAttribute("loggedInCenter", RequestContext.getCenterId());
      AddTestForm atf = (AddTestForm) form;
      JSONSerializer js = new JSONSerializer().exclude("class");
      Test test = new Test();
      BeanUtils.copyProperties(test, atf);
      test.setConductingRoleIds(conductingRoles);
      test.setUserName((String) request.getSession(false).getAttribute("userid"));
      ArrayList<Result> results = new ArrayList<>();
      boolean validExpr = true;
      Result res = null;
      List<BasicDynaBean> checkBean = hl7LabInterfaces.listAll(null, "status",
          "A");

      if (atf.getUnits() != null) {
        for (int i = 0; i < atf.getUnits().length - 1; i++) {
          String[] resultlabelIdArray = new String[atf.getUnits().length - 1];
          int resultlabelId = AddTestDAOImpl.getNextSequence();
          resultlabelIdArray[i] = new Integer(resultlabelId).toString();
          atf.setResultlabel_id(resultlabelIdArray);
          if (checkBean != null && checkBean.size() > 0) {
            res = new Result(test.getTestId(), atf.getResultLabel()[i],
                atf.getResultLabelShort()[i], atf.getUnits()[i], atf.getOrder()[i],
                atf.getResultlabel_id()[i], atf.getExpression()[i], atf.getHl7_interface()[i]);
            res.setCode_type(codeType[i]);
            res.setResult_code(resultCode[i]);
            res.setDataAllowed(dataAllowed[i]);
            res.setSourceIfList(sourceIfList[i]);
            res.setHl7_interface(hl7Interface[i]);
            res.setMethodId(
                (methodIds[i] != null && !methodIds[i].equals("")) ? Integer.parseInt(methodIds[i])
                    : null);
            res.setDefaultValue(atf.getDefaultValue()[i]);
          } else {
            res = new Result(test.getTestId(), atf.getResultLabel()[i],
                atf.getResultLabelShort()[i], atf.getUnits()[i], atf.getOrder()[i],
                atf.getResultlabel_id()[i], atf.getExpression()[i]);
            res.setCode_type(codeType[i]);
            res.setResult_code(resultCode[i]);
            res.setDataAllowed(dataAllowed[i]);
            res.setSourceIfList(sourceIfList[i]);
            res.setMethodId(
                (methodIds[i] != null && !methodIds[i].equals("")) ? Integer.parseInt(methodIds[i])
                    : null);
            res.setDefaultValue(atf.getDefaultValue()[i]);
          }
          results.add(res);
        }
      }
      ArrayList<TestTemplate> templateList = new ArrayList<>();
      String[] template = atf.getFormatName();
      TestTemplate testTemplate = null;
      if (template != null) {
        for (int i = 0; i < template.length; i++) {
          testTemplate = new TestTemplate();
          testTemplate.setTestId(null);
          testTemplate.setTemplateId(template[i]);
          templateList.add(testTemplate);
        }
      }

      TestBO bo = new TestBO();
      StringBuilder msg = new StringBuilder();
      boolean success = bo.addNewTest(test, results, templateList, msg);
      ActionRedirect redirect = new ActionRedirect(mapping.findForward("showtest"));
      if (success) {
        // atf.reset(mapping, request);
        scope.success("New test has been added successfully....");
        redirect.addParameter("testid", test.getTestId());
        redirect.addParameter("orgId", request.getParameter("orgId"));
        redirect.addParameter("testName", atf.getTestName());
        redirect.addParameter("orgName", atf.getOrgName());
        redirect.addParameter("serviceSubGroup", test.getServiceSubGroupId());
        redirect.addParameter("insurance_category_id", atf.getInsurance_category_id());
      } else {
        redirect = new ActionRedirect(mapping.findForward("addtest"));
        if (msg.toString().isEmpty()) {
          scope.error("Failed to save test details....");
        } else {
          scope.error(msg.toString());
        }
      }
      redirect.addParameter(FlashScope.FLASH_KEY, scope.key());
      return redirect;
    } else {
      ActionRedirect redirect = new ActionRedirect(mapping.findForward("addtest"));
      FlashScope scope = FlashScope.getScope(request);
      scope.error("Failed to Insert Test Details....");
      redirect.addParameter(FlashScope.FLASH_KEY, scope.key());
      return redirect;
    }
  }

  /**
   * Update test details.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws Exception
   *           the exception
   */
  public ActionForward updateTestDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {

    AddTestForm atf = (AddTestForm) form;
    atf.setRoutineCharge("0");
    request.setAttribute("loggedInCenter", RequestContext.getCenterId());

    Map<String, String[]> requestMap = request.getParameterMap();
    String[] codeType = requestMap.get("code_type");
    String[] resultCode = requestMap.get("result_code");
    String[] dataAllowed = requestMap.get("data_allowed");
    String[] sourceInList = requestMap.get("source_if_list");
    String[] hl7Interface = requestMap.get("hl7_interface");
    String[] methodIds = requestMap.get("methodId");
    String[] prevMethodIds = requestMap.get("prevMethodId");
    String[] conductingRoles = request.getParameterValues("conductingRoleId");
    String[] hl7Deleted = requestMap.get("hl7_mapping_deleted");
    Test test = new Test();
    if (atf.getDiagCode() != null && atf.getDiagCode().equals("")) {
      atf.setDiagCode(null);
    }
    BeanUtils.copyProperties(test, atf);
    test.setConductingRoleIds(conductingRoles);
    test.setUserName((String) request.getSession(false).getAttribute("userid"));

    ArrayList<Result> addedResults = new ArrayList<>();
    ArrayList<Result> modifiedResults = new ArrayList<>();
    ArrayList<Result> deletedResults = new ArrayList<>();
    boolean validExpr = true;
    FlashScope flash = FlashScope.getScope(request);
    Result res = null;

    List<BasicDynaBean> checkBean = hl7LabInterfaces.listAll(null, "status",
        "A");
    if (atf.getResultOp() != null) {
      for (int i = 0; i < atf.getResultOp().length; i++) {

        String op = atf.getResultOp()[i];
        if (op.equals("add")) {
          String[] resultlabelIdArray = new String[atf.getResultOp().length];
          int resultlabelId = AddTestDAOImpl.getNextSequence();
          resultlabelIdArray[i] = new Integer(resultlabelId).toString();
          atf.setResultlabel_id(resultlabelIdArray);
          if (checkBean != null && checkBean.size() > 0) {
            res = new Result(test.getTestId(), atf.getResultLabel()[i],
                atf.getResultLabelShort()[i], atf.getUnits()[i], atf.getOrder()[i],
                atf.getResultlabel_id()[i], atf.getExpression()[i], atf.getHl7_interface()[i]);
            res.setCode_type(codeType[i]);
            res.setResult_code(resultCode[i]);
            res.setDataAllowed(dataAllowed[i]);
            res.setSourceIfList(sourceInList[i]);
            res.setHl7_interface(hl7Interface[i]);
            res.setMethodId(
                (methodIds[i] != null && !methodIds[i].equals("")) ? Integer.parseInt(methodIds[i])
                    : null);
            res.setDefaultValue(atf.getDefaultValue()[i]);
          } else {
            res = new Result(test.getTestId(), atf.getResultLabel()[i],
                atf.getResultLabelShort()[i], atf.getUnits()[i], atf.getOrder()[i],
                atf.getResultlabel_id()[i], atf.getExpression()[i]);
            res.setCode_type(codeType[i]);
            res.setResult_code(resultCode[i]);
            res.setDataAllowed(dataAllowed[i]);
            res.setSourceIfList(sourceInList[i]);
            res.setMethodId(
                (methodIds[i] != null && !methodIds[i].equals("")) ? Integer.parseInt(methodIds[i])
                    : null);
            res.setDefaultValue(atf.getDefaultValue()[i]);
          }
          addedResults.add(res);

        } else if (op.equals("mod")) {
          if (checkBean != null && checkBean.size() > 0) {
            res = new Result(test.getTestId(), atf.getResultLabel()[i],
                atf.getResultLabelShort()[i], atf.getUnits()[i], atf.getOrder()[i],
                atf.getResultlabel_id()[i], atf.getExpression()[i], atf.getHl7_interface()[i]);
            res.setCode_type(codeType[i]);
            res.setResult_code(resultCode[i]);
            res.setDataAllowed(dataAllowed[i]);
            res.setSourceIfList(sourceInList[i]);
            res.setHl7_interface(hl7Interface[i]);
            res.setMethodId(
                (methodIds[i] != null && !methodIds[i].equals("")) ? Integer.parseInt(methodIds[i])
                    : null);
            // r.setPrevMethodId((prevMethodIds[i] != null && !prevMethodIds[i].equals("")) ?
            // Integer.parseInt(prevMethodIds[i]) : null);
            res.setDefaultValue(atf.getDefaultValue()[i]);
          } else {
            res = new Result(test.getTestId(), atf.getResultLabel()[i],
                atf.getResultLabelShort()[i], atf.getUnits()[i], atf.getOrder()[i],
                atf.getResultlabel_id()[i], atf.getExpression()[i]);
            res.setCode_type(codeType[i]);
            res.setResult_code(resultCode[i]);
            res.setDataAllowed(dataAllowed[i]);
            res.setSourceIfList(sourceInList[i]);
            res.setMethodId(
                (methodIds[i] != null && !methodIds[i].equals("")) ? Integer.parseInt(methodIds[i])
                    : null);
            // r.setPrevMethodId((prevMethodIds[i] != null && !prevMethodIds[i].equals("")) ?
            // Integer.parseInt(prevMethodIds[i]) : null);
            res.setDefaultValue(atf.getDefaultValue()[i]);
          }
          modifiedResults.add(res);

        } else if (op.equals("del")) {
          if (checkBean != null && checkBean.size() > 0) {
            res = new Result(test.getTestId(), atf.getResultLabel()[i],
                atf.getResultLabelShort()[i], atf.getUnits()[i], atf.getOrder()[i],
                atf.getResultlabel_id()[i], atf.getExpression()[i], atf.getHl7_interface()[i]);
            res.setMethodId(
                (methodIds[i] != null && !methodIds[i].equals("")) ? Integer.parseInt(methodIds[i])
                    : null);
            // r.setPrevMethodId((prevMethodIds[i] != null && !prevMethodIds[i].equals("")) ?
            // Integer.parseInt(prevMethodIds[i]) : null);
          } else {
            res = new Result(test.getTestId(), atf.getResultLabel()[i],
                atf.getResultLabelShort()[i], atf.getUnits()[i], atf.getOrder()[i],
                atf.getResultlabel_id()[i], atf.getExpression()[i]);
            res.setMethodId(
                (methodIds[i] != null && !methodIds[i].equals("")) ? Integer.parseInt(methodIds[i])
                    : null);
            // r.setPrevMethodId((prevMethodIds[i] != null && !prevMethodIds[i].equals("")) ?
            // Integer.parseInt(prevMethodIds[i]) : null);
          }
          deletedResults.add(res);
        }
      }
    }
    ArrayList<TestTemplate> templateList = new ArrayList<>();
    String[] template = atf.getFormatName();
    TestTemplate temp = null;
    if (template != null) {
      for (int i = 0; i < template.length; i++) {
        temp = new TestTemplate();
        temp.setTestId(test.getTestId());
        temp.setTemplateId(template[i]);
        templateList.add(temp);
      }
    }

    TestBO bo = new TestBO();
    StringBuilder msg = new StringBuilder();
    boolean success = bo.updateTestDetails(test, addedResults, modifiedResults, deletedResults,
        templateList, msg);

    if (success) {
      flash.put("success", "Test details updated successfully");
      atf.reset(mapping, request);
    } else {
      if (msg.toString().isEmpty()) {
        flash.put("error", "Error updating test details");
      } else {
        flash.put("error", msg);
      }
    }

    ActionRedirect redirect = new ActionRedirect(mapping.findForward("showtest"));
    redirect.addParameter("testid", test.getTestId());
    redirect.addParameter("orgId", request.getParameter("orgId"));
    redirect.addParameter("testName", atf.getTestName());
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

    return redirect;
  }

  /**
   * Sets the attributes.
   *
   * @param request
   *          the new attributes
   * @throws SQLException
   *           the SQL exception
   */
  private void setAttributes(HttpServletRequest request) throws SQLException {
    JSONSerializer js = new JSONSerializer().exclude("class");
    request.setAttribute("diagdepts", js.serialize(AddTestDAOImpl.getDiagDepartments()));
    request.setAttribute("reportformats", AddTestDAOImpl.getReportFormats());
  }

  /**
   * Populate list values TO array.
   *
   * @param array1
   *          the a
   * @param arrayList
   *          the al
   * @return the string[]
   */
  private static String[] populateListValuesTOArray(String[] array1, ArrayList<String> arrayList) {
    Iterator<String> it = arrayList.iterator();
    array1 = new String[arrayList.size()];

    int inc = 0;
    while (it.hasNext()) {
      array1[inc++] = it.next();
    }

    return array1;
  }

  /**
   * Edits the test charges.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws Exception
   *           the exception
   */
  public ActionForward editTestCharges(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {

    AddTestForm atf = (AddTestForm) form;
    String orgId = request.getParameter("orgId");
    String testid = request.getParameter("testid");
    String chargeType = request.getParameter("chargeType");

    ArrayList<Hashtable<String, String>> testDetails = AddTestDAOImpl.getTestDetails(testid);
    Iterator<Hashtable<String, String>> it = testDetails.iterator();

    if (it.hasNext()) {
      Hashtable<String, String> ht = it.next();
      atf.setTestName(ht.get("TEST_NAME"));
      atf.setTestId(testid);
      atf.setOrgId(orgId);
      atf.setChargeType(chargeType);
      BasicDynaBean bean = OrgMasterDao.getOrgdetailsDynaBean(orgId);
      atf.setOrgName((String) bean.get("org_name"));
    }

    List<BasicDynaBean> list = AddTestDAOImpl.getOrgItemCode(orgId, testid);
    if (!list.isEmpty()) {
      BasicDynaBean bean = list.get(0);
      atf.setOrgItemCode((String) bean.get("item_code"));
      atf.setApplicable((Boolean) bean.get("applicable"));
      atf.setCodeType((String) bean.get("code_type"));
    }

    List activeRateSheets = OrgMasterDao.getActiveOrgIdNamesExcludeOrg(orgId);
    request.setAttribute("activeRateSheets", activeRateSheets);

    List<String> notApplicableRatePlans = AddTestDAOImpl.getTestNotApplicableRatePlans(testid,
        orgId);
    request.setAttribute("ratePlansNotApplicable", notApplicableRatePlans);

    TestBO bo = new TestBO();
    Map map = bo.editTestCharges(orgId, testid);
    request.setAttribute("testid", testid);
    request.setAttribute("chargeMap", map);
    request.setAttribute("method", "updateTestCharges");
    JSONSerializer js = new JSONSerializer().exclude("class");
    request.setAttribute("testsList", js.serialize(AddTestDAOImpl.getTestsNamesAndIds()));
    request.setAttribute("codeType", atf.getCodeType());
    List<BasicDynaBean> derivedRatePlanDetails = testChargesdao.getDerivedRatePlanDetails(orgId,
        testid);
    if (derivedRatePlanDetails.size() < 0) {
      request.setAttribute("derivedRatePlanDetails", js.serialize(Collections.EMPTY_LIST));
    } else {
      request.setAttribute("derivedRatePlanDetails",
          js.serialize(ConversionUtils.copyListDynaBeansToMap(derivedRatePlanDetails)));
    }
    setAttributes(request);
    request.setAttribute("loggedInCenter", RequestContext.getCenterId());
    return mapping.findForward("getEditCharges");
  }

  /**
   * Update test charges.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws Exception
   *           the exception
   */
  public ActionForward updateTestCharges(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {

    AddTestForm atf = (AddTestForm) form;
    String testId = atf.getTestId();
    String orgId = atf.getOrgId();

    String[] bedType = atf.getBedTypes();
    Double[] regularcharge = atf.getRegularCharges();
    Double[] discount = atf.getDiscount();
    FlashScope flash = FlashScope.getScope(request);

    String[] derivedRateplanIds = request.getParameterValues("ratePlanId");
    String[] ratePlanApplicable = request.getParameterValues("applicable");

    ArrayList<TestCharge> al = new ArrayList<>();

    TestCharge tc = null;
    for (int i = 0; i < bedType.length; i++) {
      tc = new TestCharge();
      tc.setBedType(bedType[i]);
      tc.setCharge(new BigDecimal(regularcharge[i]));
      tc.setDiscount(new BigDecimal(discount[i]));
      tc.setOrgId(orgId);
      tc.setPriority("R");
      tc.setTestId(testId);
      tc.setUserName((String) request.getSession(false).getAttribute("userid"));
      al.add(tc);
    }

    boolean stat = true;

    Connection con = null;
    try {

      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      // Reset Not Applicable first.
      Map<String, String> keys = new HashMap<>();
      keys.put("test_id", testId);
      keys.put("org_id", orgId);

      Map<String, Boolean> fields = new HashMap<>();
      fields.put("applicable", true);
      int result = testOrgDetails.update(con, fields, keys);
      stat = (result > 0);

    } finally {
      DataBaseUtil.commitClose(con, stat);
    }

    TestBO bo = new TestBO();

    stat = stat
        && bo.updateTestCharge(al, testId, orgId, true, atf.getOrgItemCode(), atf.getCodeType());

    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      if (null != derivedRateplanIds && derivedRateplanIds.length > 0) {
        stat = stat && testChargesdao.updateOrgForDerivedRatePlans(con, derivedRateplanIds,
            ratePlanApplicable, testId);
        stat = stat && testChargesdao.updateChargesForDerivedRatePlans(con, orgId,
            derivedRateplanIds, bedType, regularcharge, testId, discount, ratePlanApplicable);
      }
      RateMasterDao rdao = new RateMasterDao();
      List<BasicDynaBean> allDerivedRatePlanIds = rdao.getDerivedRatePlanIds(orgId);
      if (null != allDerivedRatePlanIds) {
        testChargesdao.updateApplicableflagForDerivedRatePlans(con, allDerivedRatePlanIds,
            "diagnostics", "test_id", testId, "test_org_details", orgId);
      }
    } finally {
      DataBaseUtil.commitClose(con, stat);
    }

    if (stat) {
      flash.put("success", "Test charges updated successfully");
    } else {
      flash.put("error", "Error updating test charges");
    }

    ActionRedirect redirect = new ActionRedirect(mapping.findForward("showcharges"));
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    redirect.addParameter("orgId", orgId);
    redirect.addParameter("testid", testId);
    redirect.addParameter("testName", atf.getTestName());
    request.setAttribute("loggedInCenter", RequestContext.getCenterId());
    return redirect;
  }

  /**
   * Group Update: called from the main test list screen, updates the charges of all/selected tests
   * by a formula: +/- a certain amount or percentage.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws Exception
   *           the exception
   */
  public ActionForward groupUpdate(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {

    AddTestForm af = (AddTestForm) form;

    String orgId = af.getOrgId();
    String amtType = af.getAmtType();
    BigDecimal amount = af.getAmount();
    BigDecimal roundOff = af.getRoundOff();
    String updateTable = af.getUpdateTable();
    String userName = (String) request.getSession().getAttribute("userid");

    if (af.getIncType().equals("-")) {
      amount = amount.negate();
    }

    List<String> selectTests = null;
    if ((af.getSelectTest() != null) && !af.getAllTests().equals("yes")) {
      selectTests = Arrays.asList(af.getSelectTest());
    }

    List<String> bedTypes = null;
    if ((af.getSelectBedType() != null) && !af.getAllBedTypes().equals("yes")) {
      bedTypes = Arrays.asList(af.getSelectBedType());
    }

    Connection con = null;
    boolean success = false;

    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      AddTestDAOImpl dao = new AddTestDAOImpl(con);
      dao.groupIncreaseTestCharges(orgId, bedTypes, selectTests, amount, amtType.equals("%"),
          roundOff, updateTable, (String) request.getSession(false).getAttribute("userid"));
      dao.updateDiagnosticTimeStamp();
      success = true;
      if (success) {
        con.commit();
      }
    } finally {
      DataBaseUtil.commitClose(con, success);
    }

    if (success) {
      testChargesdao.updateChargesForDerivedRatePlans(orgId, userName, "tests", false);
    }

    FlashScope flash = FlashScope.getScope(request);
    if (success) {
      flash.put("success", "Charges updated successfully");
    } else {
      flash.put("error", "Error updating charges");
    }

    ActionRedirect redirect = new ActionRedirect(
        request.getHeader("Referer").replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

    return redirect;
  }

  /*
   * Export the set of test charges for each bed type as a XLS. Called from the main tests list
   * screen.
   */

  /** The import exporter. */
  private static ChargesImportExporter importExporter;

  static {

    importExporter = new ChargesImportExporter("diagnostics", "test_org_details",
        "diagnostic_charges", "diagnostics_departments", "test_id", "ddept_id", "ddept_name",
        new String[] { "test_name", "status" }, new String[] { "Test Name", "Status" },
        new String[] { "applicable", "item_code" }, new String[] { "Applicable", "Code" },
        new String[] { "charge", "discount" }, new String[] { "Charge", "Discount" });

    importExporter.setItemWhereFieldKeys(new String[] { "test_id" });
    importExporter.setOrgWhereFieldKeys(new String[] { "test_id", "org_id" });
    importExporter.setChargeWhereFieldKeys(new String[] { "test_id", "org_name" });
    importExporter.setMandatoryFields(new String[] { "test_name" });
    importExporter.setItemName("test_name");
    importExporter.setChgTabOrgColName("org_name");
  }

  /**
   * Export test charges CSV.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the action forward
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public ActionForward exportTestChargesCSV(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws SQLException, java.io.IOException {

    String orgId = req.getParameter("orgId");
    String orgName = (String) OrgMasterDao.getOrgdetailsDynaBean(orgId).get("org_name");
    XSSFWorkbook workbook = new XSSFWorkbook();
    XSSFSheet workSheet = workbook.createSheet("TEST CHARGES");
    importExporter.exportCharges(orgId, workSheet, null, "A");

    res.setHeader("Content-type", "application/vnd.ms-excel");
    res.setHeader("Content-disposition",
        "attachment; filename=" + "\"TestRates_" + orgName + ".xls\"");
    res.setHeader("Readonly", "true");

    java.io.OutputStream outputStream = res.getOutputStream();
    workbook.write(outputStream);
    outputStream.flush();
    outputStream.close();

    return null;

  }

  /**
   * Import a XLS file to update a set of test charges.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws Exception
   *           the exception
   */
  public ActionForward importTestChargesXLS(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws SQLException, IOException, Exception {

    String orgId = request.getParameter("org_id");
    this.errors = new StringBuilder();

    FlashScope flash = FlashScope.getScope(request);
    String referer = request.getHeader("Referer");
    referer = referer.replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", "");
    String userId = (String) request.getSession().getAttribute("userid");
    Connection con = DataBaseUtil.getConnection();
    AddTestDAOImpl dao = new AddTestDAOImpl(con);
    /*
     * Keep a backup of the rates for safety: TODO: be able to checkpoint and revert to a previous
     * version if required.
     */
    dao.backupCharges(orgId, userId);
    if (con != null) {
      con.close();
    }
    importExporter.setUseAuditLogHint(true);
    AddTestForm suForm = (AddTestForm) form;
    XSSFWorkbook workBook = new XSSFWorkbook(suForm.getXlsChargeFile().getInputStream());
    XSSFSheet sheet = workBook.getSheetAt(0);
    importExporter.importCharges(true, orgId, sheet, userId, this.errors);
    String userName = (String) request.getSession().getAttribute("userid");
    testChargesdao.updateChargesForDerivedRatePlans(orgId, userName, "tests", true);

    if (this.errors.length() > 0) {
      flash.put("error", this.errors);
    } else {
      flash.put("info", "File successfully uploaded");
    }
    ActionRedirect redirect = new ActionRedirect(referer);
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;
  }

  /**
   * Export test details to xls.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the action forward
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public ActionForward exportTestDetailsToXls(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws SQLException, java.io.IOException {

    List<String> diagColumnNames = Arrays.asList(new String[] { "test_id", "Test Name",
        "Sample Needed", "Dept Name", "Type Of Specimen", "Conduct In Report Format", "Status",
        "Service Group Name", "Service Sub Group Name", "Conduction Applicable",
        "Conducting Doctor Mandatory", "mandate additional info", "Unit Charge",
        "Results Entry Applicable", "Alias", "Insurance Category", "Pre Auth Required",
        "Allow Rate Increase", "Allow Rate Decrease", "Interface Test Code" });

    List<String> testResulLabelsColumnNames = null;
    int maxCentersIncDefault = (Integer) GenericPreferencesDAO.getAllPrefs()
        .get("max_centers_inc_default");

    if (maxCentersIncDefault > 1) {
      testResulLabelsColumnNames = Arrays.asList(new String[] { "resultlabel_id", "Test Name",
          "Dept Name", "Resultlabel", "Center", "Methodology", "Units", "Display Order" });
    } else {
      testResulLabelsColumnNames = Arrays.asList(new String[] { "resultlabel_id", "Test Name",
          "Dept Name", "Resultlabel", "Methodology", "Units", "Display Order" });
    }

    XSSFWorkbook workbook = new XSSFWorkbook();
    // Sheet for diagnostic table
    XSSFSheet diagWorkSheet = workbook.createSheet("DIAGNOSTICS");
    List<BasicDynaBean> testDetails = AddTestDAOImpl.getAllTestDetails();
    Map<String, List> columnNamesMap = new HashMap<>();
    columnNamesMap.put("mainItems", diagColumnNames);
    HsSfWorkbookUtils.createPhysicalCellsWithValues(testDetails, columnNamesMap, diagWorkSheet,
        true);
    testDetails = null;

    // Sheet for test_results_master table
    XSSFSheet testResultLabelworksheet = workbook.createSheet("TEST RESULTS");
    List<BasicDynaBean> testResultLabelDetails = AddTestDAOImpl.getAllTestResultLabelDetails();
    Map<String, List> columnNamesMap2 = new HashMap<>();
    columnNamesMap2.put("mainItems", testResulLabelsColumnNames);
    HsSfWorkbookUtils.createPhysicalCellsWithValues(testResultLabelDetails, columnNamesMap2,
        testResultLabelworksheet, true);
    testResultLabelDetails = null;

    // Sheet for test_template_master table
    XSSFSheet testTemplateWorkSheet = workbook.createSheet("TEST TEMPLATE");
    List<BasicDynaBean> testTemplateDetails = AddTestDAOImpl.getTestTemplates();
    List<String> testTemplateColumnNames = Arrays
        .asList(new String[] { "Test Name", "Dept Name", "Format Name" });
    Map<String, List> columnNamesMap3 = new HashMap<>();
    columnNamesMap3.put("mainItems", testTemplateColumnNames);
    HsSfWorkbookUtils.createPhysicalCellsWithValues(testTemplateDetails, columnNamesMap3,
        testTemplateWorkSheet, false);
    testTemplateDetails = null;

    // Sheet for diag_tat_center_master table
    String sheetName = "TEST TAT DETAILS";
    List<String> testTATColumnNames = Arrays
        .asList(new String[] { "Tat Center Id", "Test Name", "Center Id", "Center Name",
            "Logistics TAT", "Processing Days", "Conduction Start Time", "Conduction TAT" });

    Map<String, List> columnNamesMap4 = new HashMap<>();
    columnNamesMap4.put("mainItems", testTATColumnNames);
    int testTATDetailsCount = TestTATDAO.getTATDetailsCount();
    int limit = 50000;
    List<BasicDynaBean> sublist = new ArrayList<>(limit);
    if (testTATDetailsCount >= limit) {
      for (int i = 0; i < (int) (Math.ceil((testTATDetailsCount / (double) limit))); i++) {
        sublist = TestTATDAO.getTATDetails((limit * i), limit);
        XSSFSheet testTATSheet = workbook.createSheet(sheetName + i);
        HsSfWorkbookUtils.createPhysicalCellsWithValues(sublist, columnNamesMap4, testTATSheet,
            true);

        sublist = null;
      }
    } else {
      sublist = TestTATDAO.getTATDetails(0, testTATDetailsCount);
      XSSFSheet testTATSheet = workbook.createSheet(sheetName);
      HsSfWorkbookUtils.createPhysicalCellsWithValues(sublist, columnNamesMap4, testTATSheet, true);
      sublist = null;
    }
    res.setHeader("Content-type", "application/vnd.ms-excel");
    res.setHeader("Content-disposition", "attachment; filename=TestDefinationDetails.xls");
    res.setHeader("Readonly", "true");
    java.io.OutputStream os = res.getOutputStream();
    workbook.write(os);
    os.flush();
    os.close();

    return null;
  }

  /** The details impor exp. */
  public static DetailsImportExporter detailsImporExp;

  static {
    detailsImporExp = new DetailsImportExporter("diagnostics", "test_org_details",
        "diagnostic_charges");

  }

  /**
   * Import test details from xls.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   */
  public ActionForward importTestDetailsFromXls(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws java.io.IOException, SQLException {

    this.errors = new StringBuilder();
    Connection con = null;
    boolean success = false;
    Map<String, String> aliasMap = new HashMap<>();

    aliasMap.put("test name", "test_name");
    aliasMap.put("sample needed", "sample_needed");
    aliasMap.put("type of specimen", "type_of_specimen");
    aliasMap.put("status", "status");
    aliasMap.put("code", "diag_code");
    aliasMap.put("conduct in report format", "conduction_format");
    aliasMap.put("conduction applicable", "conduction_applicable");
    aliasMap.put("dept name", "ddept_id");
    aliasMap.put("conducting doctor mandatory", "conducting_doc_mandatory");
    aliasMap.put("mandate additional info", "mandate_additional_info");
    aliasMap.put("service sub group name", "service_sub_group_name");
    aliasMap.put("service group name", "service_group_name");
    aliasMap.put("unit charge", "unit charge");
    aliasMap.put("alias", "diag_code");
    aliasMap.put("results entry applicable", "results_entry_applicable");
    aliasMap.put("insurance category", "insurance_category_id");
    aliasMap.put("pre auth required", "prior_auth_required");
    aliasMap.put("allow rate increase", "allow_rate_increase");
    aliasMap.put("allow rate decrease", "allow_rate_decrease");
    aliasMap.put("interface test code", "hl7_export_code");

    List<String> mandatoryList = Arrays.asList("test_name", "status", "ddept_id",
        "service_sub_group_name", "sample_needed", "service_group_name", "mandate_additional_info");

    String referer = request.getHeader("Referer");
    referer = referer.replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", "");
    FlashScope flash = FlashScope.getScope(request);
    String userName = (String) request.getSession(false).getAttribute("userid");
    AddTestForm serviceForm = (AddTestForm) form;
    ByteArrayInputStream byteStream = new ByteArrayInputStream(
        serviceForm.getXlsTestFile().getFileData());
    XSSFWorkbook workBook = new XSSFWorkbook(byteStream);
    XSSFSheet sheet = workBook.getSheetAt(0);
    importTestDetails(sheet, aliasMap, mandatoryList, errors, userName);

    importTestResults(workBook.getSheetAt(1), errors);

    importTestTemplates(workBook.getSheetAt(2), errors);
    if (workBook.getNumberOfSheets() > 3) {
      for (int i = 3; i < workBook.getNumberOfSheets(); i++) {
        if (workBook.getSheetAt(i) != null) {
          importTATDetails(workBook.getSheetAt(i), errors);
        }
      }
    }

    if (this.errors.length() > 0) {
      flash.put("error", this.errors);
    } else {
      flash.put("info", "File successfully uploaded");
    }
    ActionRedirect redirect = new ActionRedirect(referer);
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;

  }

  /**
   * Import test details.
   *
   * @param sheet
   *          the sheet
   * @param aliasUnmsToDBnmsMap
   *          the alias unms to D bnms map
   * @param mandatoryFields
   *          the mandatory fields
   * @param errors
   *          the errors
   * @param userName
   *          the user name
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  private void importTestDetails(XSSFSheet sheet, Map aliasUnmsToDBnmsMap,
      List<String> mandatoryFields, StringBuilder errors, String userName)
      throws SQLException, IOException {

    Iterator rowIterator = sheet.rowIterator();
    XSSFRow row1 = (XSSFRow) rowIterator.next();
    String sheetName = sheet.getSheetName();
    String userNameWithHint = userName + ":XLS";
    this.errors = errors;

    List exceptFields = Arrays.asList(new String[] { "service_group_name", "service_sub_group_name",
        "unit charge", "type_of_specimen" });

    BasicDynaBean mainBean = diagnosticsDao.getBean();
    List<BasicDynaBean> orgList = new OrgMasterDao().getAllOrgIdNames();
    List<String> bedTypes = BedMasterDAO.getUnionOfBedTypes();

    row1.getLastCellNum();
    String[] headers = new String[row1.getLastCellNum()];
    String[] xlHeaders = new String[row1.getLastCellNum()];

    detailsImporExp.setTableDbName("test_name");
    detailsImporExp.setDeptName("ddept_id");

    for (int i = 0; i < headers.length; i++) {

      XSSFCell cell = row1.getCell(i);
      if (cell == null) {
        headers[i] = null; /* putting null values, if found */
      } else {

        String header = cell.getStringCellValue().toLowerCase();
        String dbName = (String) (aliasUnmsToDBnmsMap.get(header) == null ? header
            : aliasUnmsToDBnmsMap.get(header));
        headers[i] = dbName;
        xlHeaders[i] = header;

        if (mainBean.getDynaClass().getDynaProperty(dbName) == null
            && !exceptFields.contains(dbName)) {
          addError(0, "Unknown header found in header " + dbName + " in the sheet " + sheetName);
          headers[i] = null;
          xlHeaders[i] = null;
        }

      }

    }

    for (String mfield : mandatoryFields) {
      if (!Arrays.asList(headers).contains(mfield)) {
        addError(0, "Mandatory field " + mfield + " is missing cannot process further in the sheet "
            + sheetName);
        return;
      }
    }

    GenericDAO sampTypeDAO = new GenericDAO("sample_type");

    GenericDAO chargeDAO = new GenericDAO("diagnostic_charges");
    BasicDynaBean tableBean = diagnosticsDao.getBean();
    BasicDynaBean chgBean = chargeDAO.getBean();
    BasicDynaBean orgBean = testOrgDetails.getBean();
    BasicDynaBean itemBean = null;
    Map deptMap = AddTestDAOImpl.getDiagDepData();
    Map<String, Integer> insuransCatMap = AddTestDAOImpl.getInsuranceCategoryData();
    List<String> inactiveBedList = detailsImporExp.getInactiveBeds();

    nxtLine: while (rowIterator.hasNext()) {
      XSSFRow row = (XSSFRow) rowIterator.next();
      int lineNumber = row.getRowNum() + 1;
      itemBean = diagnosticsDao.getBean();
      Map<String, String> keys = null;
      String operation = "update";
      String newId = null;
      String itemId = null;
      String itemName = null;
      Object itemDept = null;
      String beanId = null;
      Object grpId = null;
      String subGrpName = null;
      BasicDynaBean existOrNot = null;
      BasicDynaBean subGrpBean = null;
      boolean lineHasErrors = false;
      String sampleNeeded = null;
      String typeOfSample = null;
      String reportFormat = null;
      BigDecimal unitCharge = null;
      Integer insuranceId = null;

      nxtCell: for (int j = 0; j < headers.length; j++) {

        if (headers[j] == null) {
          continue nxtCell;
        }
        Object cellVal = null;
        DynaProperty property = null;

        XSSFCell rowcell = row.getCell(j);
        property = tableBean.getDynaClass().getDynaProperty(headers[j]);
        try {
          if (rowcell != null && !rowcell.equals("") && !exceptFields.contains(headers[j])) {

            /* check the id */
            if (headers[j].equals("test_id")) {

              itemId = rowcell.getStringCellValue();
              if (itemId == null) {
                operation = "insert";
              }
              continue nxtCell;

            } else if (headers[j].equals("ddept_id")) {
              String exlDbName = rowcell.getStringCellValue();
              cellVal = deptMap.get(exlDbName);
              itemDept = cellVal;
              if (cellVal == null) {
                addError(lineNumber,
                    "Department " + exlDbName + " not exist in the sheet " + sheetName);
                lineHasErrors = true;
                /* through error that dept not exist */
              }
            } else {
              Class type = property.getType();
              if (type == java.lang.String.class) {
                cellVal = rowcell.getStringCellValue();
              } else if (type == java.lang.Boolean.class) {
                cellVal = rowcell.getBooleanCellValue();
              } else if (type == java.math.BigDecimal.class) {
                cellVal = rowcell.getNumericCellValue();
              }
            }

          }
          if (headers[j].equals("test_id") && cellVal == null) {
            operation = "insert";
          } else if (headers[j].equals("test_name")) {
            itemName = (String) cellVal;
            itemBean.set(headers[j], cellVal);

          } else if (headers[j].equals("service_group_name")) {
            if (rowcell == null || rowcell.equals("")) {
              addError(lineNumber,
                  "service_group_name should not be null in the sheet " + sheetName);
              lineHasErrors = true;
              continue nxtCell;
            }
            cellVal = rowcell.getStringCellValue();
            grpId = detailsImporExp.getGrpId(cellVal.toString());
            if (grpId == null) {
              addError(lineNumber, "Service Group Id not exist in the sheet " + sheetName);
              lineHasErrors = true;
            }
            continue nxtCell;
          } else if (headers[j].equals("service_sub_group_name")) {
            if (rowcell == null || rowcell.equals("")) {
              addError(lineNumber,
                  "service_sub_group_name should not be null in the sheet " + sheetName);
              lineHasErrors = true;
              continue nxtCell;
            }
            subGrpName = rowcell.getStringCellValue();
            continue nxtCell;
          } else if (headers[j].equals("mandate_additional_info")
              && (cellVal == null || cellVal.equals(""))) {
            addError(lineNumber, headers[j] + " should not be null in the sheet " + sheetName);
            lineHasErrors = true;
            continue nxtCell;
          } else if (mandatoryFields.contains(headers[j]) && cellVal == null) {
            addError(lineNumber, headers[j] + " should not be null in the sheet " + sheetName);
            lineHasErrors = true;
            continue nxtCell;

          } else if (headers[j].equals("sample_needed") || headers[j].equals("type_of_specimen")) {
            if (headers[j].equals("sample_needed")) {
              sampleNeeded = rowcell.getStringCellValue();
              itemBean.set("sample_needed", sampleNeeded);
            } else {
              if (rowcell == null) {
                continue nxtCell;
              } else {
                typeOfSample = rowcell.getStringCellValue();
                itemBean.set("type_of_specimen", typeOfSample);
                List<String> sampleTypeId = new ArrayList<>();
                sampleTypeId.add("sample_type_id");
                Map<String, Object> identifier = new HashMap<>();
                identifier.put("sample_type", typeOfSample);
                BasicDynaBean bean = sampTypeDAO.findByKey(sampleTypeId, identifier);
                if (bean != null) {
                  itemBean.set("sample_type_id",
                      sampTypeDAO.findByKey(sampleTypeId, identifier).get("sample_type_id"));
                }
              }
            }

          } else if (headers[j].equals("conduction_format")) {
            reportFormat = rowcell.getStringCellValue();
            itemBean.set("conduction_format", reportFormat);

          } else if (headers[j].equals("unit charge")) {
            if (operation.equals("insert")) {
              unitCharge = new BigDecimal(rowcell.getNumericCellValue());
            }
          } else if (headers[j].equals("insurance_category_id")) {
            if (rowcell != null && !rowcell.equals("")) {
              insuranceId = insuransCatMap.get(rowcell.getStringCellValue().toString());
              itemBean.set("insurance_category_id", insuranceId);
            }
          } else {
            itemBean.set(headers[j], ConvertUtils.convert(cellVal, property.getType()));

          }
        } catch (Exception ex) {

          if (property != null) {
            addError(lineNumber,
                "Conversion error: Cell value" + " could not be converted to " + property.getType()
                    + " below headers of " + headers[j] + " in the sheet " + sheetName);
            lineHasErrors = true;
          } else {
            addError(lineNumber,
                "Conversion error: Cell value"
                    + " could not be converted to class java.lang.String below headers of "
                    + headers[j] + " in the sheet " + sheetName);
            lineHasErrors = true;
          }
          continue; /* next cell */
        }
      }
      if (itemName != null && itemDept != null) {
        existOrNot = detailsImporExp.getBean(itemName, itemDept, "A");
      }
      if (existOrNot != null) {
        beanId = (String) existOrNot.get("test_id");
      }
      if (operation.equals("update") && existOrNot != null) {
        if (!itemId.equals(beanId)) {
          addError(lineNumber, "Duplicate entry cannot updated in the sheet " + sheetName);
          lineHasErrors = true;
        }
      } else {
        if (beanId != null) {
          addError(lineNumber, "Duplicate entry cannot inserted in the sheet " + sheetName);
          lineHasErrors = true;
        }
      }
      if (grpId != null && subGrpName != null) {

        subGrpBean = detailsImporExp.getSubGrpInf(Integer.parseInt(grpId.toString()), subGrpName);
        if (subGrpBean == null) {
          addError(lineNumber, "Group and Subgroup is not matching in the sheet " + sheetName);
          lineHasErrors = true;
        } else {
          itemBean.set("service_sub_group_id", subGrpBean.get("service_sub_group_id"));
        }
      }

      if (lineHasErrors) {
        continue nxtLine;
      }

      /* updating or inserting part */
      Connection con = null;
      boolean success = false;

      try {
        /* get the diag code */

        String orderAlias = AddTestDAOImpl.getOrderAlias("Diag", itemDept.toString(),
            grpId.toString(), subGrpBean.get("service_sub_group_id").toString());

        con = DataBaseUtil.getReadOnlyConnection();
        con.setAutoCommit(false);

        if (operation.equals("update")) {
          itemBean.set("username", userNameWithHint);
          keys = new HashMap<>();
          keys.put("test_id", itemId);

          success = diagnosticsDao.update(con, itemBean.getMap(), keys) > 0;

          if (success) {
            con.commit();
          }
        } else {
          newId = AutoIncrementId.getNewIncrId("Test_ID", "Diagnostics", "Diagnostic");
          itemBean.set("test_id", newId);
          itemBean.set("diag_code", orderAlias);
          success = diagnosticsDao.insert(con, itemBean);

          /* insert org details */
          for (BasicDynaBean org : orgList) {

            orgBean.set("test_id", newId);
            orgBean.set("org_id", org.get("org_id"));
            orgBean.set("applicable", true);
            success &= testOrgDetails.insert(con, orgBean);

          }

          for (BasicDynaBean org : orgList) {
            for (String bedName : bedTypes) {

              chgBean.set("test_id", newId);
              chgBean.set("org_name", org.get("org_id"));
              chgBean.set("bed_type", bedName);
              chgBean.set("priority", "R");
              chgBean.set("username", userName);

              for (String chg : new String[] { "charge" }) {
                chgBean.set(chg, unitCharge);
              }
              success &= chargeDAO.insert(con, chgBean);

            }
          }

          if (!inactiveBedList.isEmpty()) {
            for (BasicDynaBean org : orgList) {
              for (String bedName : inactiveBedList) {
                chgBean.set("test_id", newId);
                chgBean.set("org_name", org.get("org_id"));
                chgBean.set("bed_type", bedName);
                chgBean.set("priority", "R");
                chgBean.set("username", userName);

                for (String chg : new String[] { "charge" }) {
                  chgBean.set(chg, unitCharge);
                }
                success &= chargeDAO.insert(con, chgBean);
              }
            }
          }

        }
      } finally {
        DataBaseUtil.commitClose(con, success);
      }
    }

  }

  /**
   * Import test results.
   *
   * @param sheet
   *          the sheet
   * @param errors
   *          the errors
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  private void importTestResults(XSSFSheet sheet, StringBuilder errors)
      throws SQLException, IOException {

    String sheetName = sheet.getSheetName();

    this.errors = errors;
    Map<String, String> aliasUnmsToDBnmsMap = new HashMap<>();
    aliasUnmsToDBnmsMap.put("reference ranges", "reference_ranges");
    aliasUnmsToDBnmsMap.put("units", "units");
    aliasUnmsToDBnmsMap.put("display order", "display_order");
    aliasUnmsToDBnmsMap.put("resultlabel", "resultlabel");
    int maxCentersIncDefault = (Integer) GenericPreferencesDAO.getAllPrefs()
        .get("max_centers_inc_default");
    if (maxCentersIncDefault > 1) {
      aliasUnmsToDBnmsMap.put("center", "center_name");
    }
    aliasUnmsToDBnmsMap.put("resultlabel_id", "resultlabel_id");
    aliasUnmsToDBnmsMap.put("test name", "test_name");
    aliasUnmsToDBnmsMap.put("dept name", "ddept_name");
    aliasUnmsToDBnmsMap.put("methodology", "method_id");

    List<String> exceptFields = null;
    if (maxCentersIncDefault > 1) {
      exceptFields = Arrays
          .asList(new String[] { "test_name", "ddept_name", "method_id", "center_name" });
    } else {
      exceptFields = Arrays.asList(new String[] { "test_name", "ddept_name", "method_id" });
    }
    Map<Integer, String> testResultIds = AddTestDAOImpl.getTestResultIds();

    BasicDynaBean mainBean = testResultsMaster.getBean();
    List<String> mandatoryFields = null;
    Iterator rowIterator = sheet.rowIterator();
    if (maxCentersIncDefault > 1) {
      mandatoryFields = Arrays.asList(new String[] { "test_name", "ddept_name", "center_name" });
    } else {
      mandatoryFields = Arrays.asList(new String[] { "test_name", "ddept_name" });
    }
    XSSFRow row1 = (XSSFRow) rowIterator.next();
    row1.getLastCellNum();
    String[] headers = new String[row1.getLastCellNum()];
    String[] xlHeaders = new String[row1.getLastCellNum()];
    List<String> columns = Arrays.asList(new String[] { "test_id", "resultlabel", "method_id" });
    Map<String, Object> identifiers = new HashMap<>();

    for (int i = 0; i < headers.length; i++) {

      XSSFCell cell = row1.getCell(i);
      if (cell == null) {
        headers[i] = null; /* putting null values, if found */
      } else {

        String header = cell.getStringCellValue().toLowerCase();
        String dbName = aliasUnmsToDBnmsMap.get(header) == null ? header
            : aliasUnmsToDBnmsMap.get(header);
        headers[i] = dbName;
        xlHeaders[i] = header;

        if (mainBean.getDynaClass().getDynaProperty(dbName) == null
            && !exceptFields.contains(dbName)) {
          addError(0, "Unknown header found in header " + dbName + " in the sheet " + sheetName);
          headers[i] = null;
          xlHeaders[i] = null;
        }

      }

    }

    for (String mfield : mandatoryFields) {
      if (!Arrays.asList(headers).contains(mfield)) {
        addError(0, "Mandatory field " + mfield + " is missing cannot process further in the sheet "
            + sheetName);
        return;
      }
    }

    GenericDAO resultsCenterDAO = new GenericDAO("test_results_center");
    BasicDynaBean tableBean = testResultsMaster.getBean();
    BasicDynaBean itemBean = null;
    BasicDynaBean resultsCenterBean = null;
    List<BasicDynaBean> centerIdsList = null;
    Map deptMap = AddTestDAOImpl.getDiagDepData();
    Map centerMap = AddTestDAOImpl.getAvailableCenters();

    nxtLine: while (rowIterator.hasNext()) {
      XSSFRow row = (XSSFRow) rowIterator.next();
      int lineNumber = row.getRowNum() + 1;
      itemBean = testResultsMaster.getBean();
      resultsCenterBean = resultsCenterDAO.getBean();

      Map<String, Object> keys = null;
      String operation = "update";
      int newId = 0;
      Double itemId = null;
      String itemName = null;
      String commaSeparatedCenterNames = null;
      String[] centerNames = null;
      List<String> insertCenterList = new ArrayList<>();
      List<String> savedCenters = null;
      Object itemDept = null;
      String beanId = null;
      BasicDynaBean existOrNot = null;
      boolean lineHasErrors = false;

      nxtCell: for (int j = 0; j < headers.length; j++) {

        if (headers[j] == null) {
          continue nxtCell;
        }
        Object cellVal = null;
        DynaProperty property = null;

        XSSFCell rowcell = row.getCell(j);
        property = tableBean.getDynaClass().getDynaProperty(headers[j]);
        try {
          if (rowcell != null && !rowcell.equals("") && !exceptFields.contains(headers[j])) {

            /* check the id */
            if (headers[j].equals("resultlabel_id")) {

              itemId = rowcell.getNumericCellValue();
              // int val = itemId;
              if (itemId == null) {
                operation = "insert";
              }
              continue nxtCell;

            } else if (headers[j].equals("reference_ranges")) {
              switch (rowcell.getCellType()) {
                case XSSFCell.CELL_TYPE_NUMERIC: {
                  cellVal = rowcell.getNumericCellValue();
                  break;
                }
  
                case XSSFCell.CELL_TYPE_STRING: {
                  cellVal = rowcell.getStringCellValue();
                  break;
                }
                default:
                  break;
              }

            } else {
              Class type = property.getType();
              if (type == java.lang.String.class) {
                cellVal = rowcell.getStringCellValue();
              } else if (type == java.lang.Boolean.class) {
                cellVal = rowcell.getBooleanCellValue();
              } else if (type == java.math.BigDecimal.class) {
                cellVal = rowcell.getNumericCellValue();
              } else if (type == java.lang.Integer.class) {
                cellVal = rowcell.getNumericCellValue();
              }
            }

          }
          if (headers[j].equals("resultlabel_id") && cellVal == null) {
            operation = "insert";
          } else if (headers[j].equals("test_name")) {

            itemName = rowcell.getStringCellValue();
          } else if (headers[j].equals("center_name")) {
            commaSeparatedCenterNames = rowcell.getStringCellValue();
            centerNames = commaSeparatedCenterNames.split(",");
            for (int i = 0; i < centerNames.length; i++) {
              cellVal = centerMap.get(centerNames[i].trim());
              if (cellVal == null) {
                addError(lineNumber,
                    "Center " + centerNames[i] + " not exist in the sheet " + sheetName);
                lineHasErrors = true;
              }
            }
            centerIdsList = AddTestDAOImpl.getCenterDetails(centerNames);
            if (operation.equals("update")) {
              savedCenters = AddTestDAOImpl.getSavedCenters(itemId.intValue());
              for (int i = 0; i < centerNames.length; i++) {
                if (centerMap.get(centerNames[i].trim()) != null) {
                  if (savedCenters.contains(centerNames[i].trim())) {
                    savedCenters.remove(centerNames[i].trim());
                  } else {
                    if (!insertCenterList.contains(centerNames[i].trim())) {
                      insertCenterList.add(centerNames[i].trim());
                    }
                  }
                }
              }
            }
          } else if (headers[j].equals("ddept_name")) {
            String exlDbName = rowcell.getStringCellValue();
            cellVal = deptMap.get(exlDbName);
            itemDept = cellVal;
            if (cellVal == null) {
              addError(lineNumber,
                  "Department " + exlDbName + " not exist in the sheet " + sheetName);
              lineHasErrors = true;
              /* through error that dept not exist */
            }

          } else if (headers[j].equalsIgnoreCase("method_id")) {
            String methodology = null == rowcell ? null : rowcell.getStringCellValue();
            System.out.println(methodology);
            if (methodology == null || methodology.equals("")) {
              continue nxtCell;
            }
            BasicDynaBean methodBean =
                diagMethodologyMasterDAO.findByKey("method_name", methodology);
            if (methodBean == null) {
              addError(lineNumber, headers[j] + " there is no master value found for the "
                  + methodology + " in the master.");
            } else {
              cellVal = methodBean.get("method_id");
              itemBean.set(headers[j], ConvertUtils.convert(cellVal, property.getType()));
            }
          } else if (mandatoryFields.contains(headers[j]) && cellVal == null) {
            addError(lineNumber, headers[j] + " should not be null in the sheet " + sheetName);
            lineHasErrors = true;
            continue nxtCell;

          } else {
            itemBean.set(headers[j], ConvertUtils.convert(cellVal, property.getType()));

          }
        } catch (Exception ex) {

          if (property != null) {
            addError(lineNumber,
                "Conversion error: Cell value" + " could not be converted to " + property.getType()
                    + " below headers of " + headers[j] + " in the sheet " + sheetName);
          } else {
            addError(lineNumber,
                "Conversion error: Cell value"
                    + " could not be converted to class java.lang.String below headers of "
                    + headers[j] + " in the sheet " + sheetName);
          }
          continue; /* next cell */
        }
      }
      List<BasicDynaBean> testResultBean = null;

      if (itemName != null && itemDept != null) {
        existOrNot = detailsImporExp.getBean(itemName, itemDept, null);
      }
      if (existOrNot != null) {
        beanId = (String) existOrNot.get("test_id");
        identifiers.put("test_id", beanId);
        identifiers.put("resultlabel", itemBean.get("resultlabel"));
        identifiers.put("method_id", itemBean.get("method_id"));

        TestResultsDAO rdao = new TestResultsDAO();
        testResultBean = rdao.getExistingResultsList(beanId, (String) itemBean.get("resultlabel"),
            itemBean.get("method_id"));

      } else {
        addError(lineNumber,
            "there is no master value found for the test name and department in the sheet "
                + sheetName);

      }

      if (operation.equals("insert") && testResultBean != null && testResultBean.size() != 0) {
        lineHasErrors = true;
        addError(lineNumber, "Duplicate entry cannot inserted into Test Results master.. ");
      }

      if (lineHasErrors) {
        continue nxtLine;
      }

      /* updating or inserting part */
      Connection con = null;
      boolean success = false;

      try {
        con = DataBaseUtil.getReadOnlyConnection();
        con.setAutoCommit(false);

        if (operation.equals("update")) {

          keys = new HashMap<>();
          keys.put("test_id", beanId);
          keys.put("resultlabel_id", itemId.intValue());
          success = testResultsMaster.update(con, itemBean.getMap(), keys) > 0;

          if (success) {

            for (int i = 0; i < insertCenterList.size(); i++) {
              resultsCenterBean.set("result_center_id",
                  AddTestDAOImpl.getResultCenterNextSequence());
              resultsCenterBean.set("resultlabel_id", itemId.intValue());
              resultsCenterBean.set("center_id",
                  Integer.parseInt(centerMap.get(insertCenterList.get(i)).toString()));
              resultsCenterBean.set("status", "A");
              success = resultsCenterDAO.insert(con, resultsCenterBean);
            }
            if (savedCenters != null) {
              for (int i = 0; i < savedCenters.size(); i++) {
                success = AddTestDAOImpl.deleteResultsCenter(itemId.intValue(),
                    Integer.parseInt(centerMap.get(savedCenters.get(i)).toString()));
              }
            }
          }

          if (success) {
            con.commit();
          }
        } else {
          newId = AddTestDAOImpl.getNextSequence();
          itemBean.set("resultlabel_id", newId);
          itemBean.set("test_id", beanId);
          success = testResultsMaster.insert(con, itemBean);

          if (success) {
            if (maxCentersIncDefault > 1) {
              for (int i = 0; i < centerIdsList.size(); i++) {
                BasicDynaBean resultCenterIdBean = centerIdsList.get(i);
                int centerId = (Integer) resultCenterIdBean.get("center_id");
                resultsCenterBean.set("result_center_id",
                    AddTestDAOImpl.getResultCenterNextSequence());
                resultsCenterBean.set("resultlabel_id", newId);
                resultsCenterBean.set("center_id", centerId);
                resultsCenterBean.set("status", "A");
                success = resultsCenterDAO.insert(con, resultsCenterBean);
              }
            } else {
              resultsCenterBean.set("result_center_id",
                  AddTestDAOImpl.getResultCenterNextSequence());
              resultsCenterBean.set("resultlabel_id", newId);
              resultsCenterBean.set("center_id", 0);
              resultsCenterBean.set("status", "A");
              success = resultsCenterDAO.insert(con, resultsCenterBean);
            }
          }
        }
      } finally {
        DataBaseUtil.commitClose(con, success);
      }
    }
  }

  /**
   * Import test templates.
   *
   * @param sheet
   *          the sheet
   * @param errors
   *          the errors
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  private void importTestTemplates(XSSFSheet sheet, StringBuilder errors)
      throws SQLException, IOException {

    String sheetName = sheet.getSheetName();

    this.errors = errors;
    Map<String, String> aliasUnmsToDBnmsMap = new HashMap<>();
    aliasUnmsToDBnmsMap.put("test name", "test_name");
    aliasUnmsToDBnmsMap.put("dept name", "ddept_name");
    aliasUnmsToDBnmsMap.put("format name", "format_name");

    List<String> exceptFields = Arrays.asList(new String[] { "test_name", "ddept_name" });
    Map<String, String> formatMasterData = AddTestDAOImpl.getTestTmtMasterData();
    Map<String, String> deletedIds = new HashMap<>();

    GenericDAO testTemplateMasterDAO = new GenericDAO("test_template_master");
    BasicDynaBean mainBean = testTemplateMasterDAO.getBean();
    List<String> mandatoryFields = Arrays
        .asList(new String[] { "test_name", "ddept_name", "format_name" });
    Iterator rowIterator = sheet.rowIterator();
    XSSFRow row1 = (XSSFRow) rowIterator.next();
    row1.getLastCellNum();
    String[] headers = new String[row1.getLastCellNum()];
    String[] xlHeaders = new String[row1.getLastCellNum()];

    for (int i = 0; i < headers.length; i++) {

      XSSFCell cell = row1.getCell(i);
      if (cell == null) {
        headers[i] = null; /* putting null values, if found */
      } else {

        String header = cell.getStringCellValue().toLowerCase();
        String dbName = aliasUnmsToDBnmsMap.get(header) == null ? header
            : aliasUnmsToDBnmsMap.get(header);
        headers[i] = dbName;
        xlHeaders[i] = header;

        if (mainBean.getDynaClass().getDynaProperty(dbName) == null
            && !exceptFields.contains(dbName)) {
          addError(0, "Unknown header found in header " + dbName + " in the sheet " + sheetName);
          headers[i] = null;
          xlHeaders[i] = null;
        }

      }

    }

    for (String mfield : mandatoryFields) {
      if (!Arrays.asList(headers).contains(mfield)) {
        addError(0, "Mandatory field " + mfield + " is missing cannot process further in the sheet "
            + sheetName);
        return;
      }
    }

    BasicDynaBean tableBean = testTemplateMasterDAO.getBean();
    BasicDynaBean itemBean = null;
    Map deptMap = AddTestDAOImpl.getDiagDepData();

    nxtLine: while (rowIterator.hasNext()) {
      XSSFRow row = (XSSFRow) rowIterator.next();
      int lineNumber = row.getRowNum() + 1;
      itemBean = testTemplateMasterDAO.getBean();
      String itemName = null;
      Object itemDept = null;
      String beanId = null;
      BasicDynaBean existOrNot = null;
      boolean lineHasErrors = false;
      String formatId = null;

      nxtCell: for (int j = 0; j < headers.length; j++) {

        if (headers[j] == null) {
          continue nxtCell;
        }
        Object cellVal = null;
        DynaProperty property = null;

        XSSFCell rowcell = row.getCell(j);
        property = tableBean.getDynaClass().getDynaProperty(headers[j]);
        try {
          if (rowcell != null && !rowcell.equals("")) {

            if (headers[j].equals("test_name")) {

              itemName = rowcell.getStringCellValue();
              continue nxtCell;

            } else if (headers[j].equals("ddept_name")) {
              String exlDbName = rowcell.getStringCellValue();
              cellVal = deptMap.get(exlDbName);
              itemDept = cellVal;
              if (cellVal == null) {
                addError(lineNumber,
                    "Department " + exlDbName + " not exist in the sheet " + sheetName);
                lineHasErrors = true;
                /* through error that dept not exist */
              }

            } else if (headers[j].equals("format_name")) {
              String formatName = rowcell.getStringCellValue();
              formatId = formatMasterData.get(formatName);
              if (formatId != null) {
                cellVal = formatId;
              } else {
                addError(lineNumber, "No master value found for " + formatName
                    + "on below headers of " + headers[j] + " in the sheet " + sheetName);
                lineHasErrors = true;
                continue nxtCell;
              }

            }

          }

          if (mandatoryFields.contains(headers[j]) && cellVal == null) {
            addError(lineNumber, headers[j] + " should not be null in the sheet " + sheetName);
            lineHasErrors = true;
            continue nxtCell;

          }
        } catch (Exception ex) {

          if (property != null) {
            addError(lineNumber,
                "Conversion error: Cell value" + " could not be converted to " + property.getType()
                    + " below headers of " + headers[j] + " in the sheet " + sheetName);
          } else {
            addError(lineNumber,
                "Conversion error: Cell value"
                    + " could not be converted to class java.lang.String below headers of "
                    + headers[j] + " in the sheet " + sheetName);
          }
          continue; /* next cell */
        }
      }
      if (itemName != null && itemDept != null) {
        existOrNot = detailsImporExp.getBean(itemName, itemDept, null);
      }
      if (existOrNot != null) {
        beanId = (String) existOrNot.get("test_id");

      } else {
        addError(lineNumber,
            "there is no master value found for the test name and department in the sheet "
                + sheetName);
        lineHasErrors = true;

      }

      if (lineHasErrors) {
        continue nxtLine;
      }

      /* updating or inserting part */
      Connection con = null;
      boolean success = false;

      try {
        con = DataBaseUtil.getReadOnlyConnection();
        con.setAutoCommit(false);

        if (deletedIds.get(beanId) == null) {
          diagMethodologyMasterDAO.delete(con, "test_id", beanId);
          deletedIds.put(beanId, "");
        }

        itemBean.set("test_id", beanId);
        itemBean.set("format_name", formatId);
        success = diagMethodologyMasterDAO.insert(con, itemBean);

      } finally {
        DataBaseUtil.commitClose(con, success);
      }
    }

  }

  /** The errors. */
  private StringBuilder errors;

  /**
   * Adds the error.
   *
   * @param line
   *          the line
   * @param msg
   *          the msg
   */
  private void addError(int line, String msg) {

    if (line > 0) {

      this.errors.append("Line ").append(line).append(": ");

    } else {

      this.errors.append("Error in header: ");
    }

    this.errors.append(msg).append("<br>");
    logger.error("Line " + line + ": " + msg);

  }

  /**
   * Import TAT details.
   *
   * @param sheet
   *          the sheet
   * @param errors
   *          the errors
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public void importTATDetails(XSSFSheet sheet, StringBuilder errors)
      throws SQLException, IOException {
    String sheetName = sheet.getSheetName();

    this.errors = errors;
    Map<String, String> aliasUnmsToDBnmsMap = new HashMap<>();
    aliasUnmsToDBnmsMap.put("tat center id", "tat_center_id");
    aliasUnmsToDBnmsMap.put("center id", "center_id");
    aliasUnmsToDBnmsMap.put("test name", "test_name");
    aliasUnmsToDBnmsMap.put("center name", "center_name");
    aliasUnmsToDBnmsMap.put("logistics tat", "logistics_tat_hours");
    aliasUnmsToDBnmsMap.put("processing days", "processing_days");
    aliasUnmsToDBnmsMap.put("conduction start time", "conduction_start_time");
    aliasUnmsToDBnmsMap.put("conduction tat", "conduction_tat_hours");

    TestTATDAO tatDAO = new TestTATDAO("diag_tat_center_master");
    List<String> mandatoryFields = Arrays.asList(new String[] { "test_name", "center_name" });
    Iterator rowIterator = sheet.rowIterator();
    XSSFRow row1 = (XSSFRow) rowIterator.next();
    String[] headers = new String[row1.getLastCellNum()];
    String[] xlHeaders = new String[row1.getLastCellNum()];

    for (int i = 0; i < headers.length; i++) {

      XSSFCell cell = row1.getCell(i);
      if (cell == null) {
        headers[i] = null; /* putting null values, if found */
      } else {

        String header = cell.getStringCellValue().toLowerCase();
        String dbName = aliasUnmsToDBnmsMap.get(header) == null ? header
            : aliasUnmsToDBnmsMap.get(header);
        headers[i] = dbName;
        xlHeaders[i] = header;

      }

    }

    for (String mfield : mandatoryFields) {
      if (!Arrays.asList(headers).contains(mfield)) {
        addError(0, "Mandatory field " + mfield + " is missing cannot process further in the sheet "
            + sheetName);
        return;
      }
    }

    BasicDynaBean tatBean = null;
    nxtLine: while (rowIterator.hasNext()) {
      XSSFRow row = (XSSFRow) rowIterator.next();
      int lineNumber = row.getRowNum() + 1;
      tatBean = tatDAO.getBean();
      String testName = null;
      String centerName = null;
      BigDecimal logTAThours = null;
      String tatCenterId = null;
      Integer centerId = null;
      BigDecimal conductionTAThours = null;
      Time condStartTime = null;
      String processingDays = new String();
      boolean lineHasErrors = false;

      nxtCell: for (int j = 0; j < headers.length; j++) {

        if (headers[j] == null) {
          continue nxtCell;
        }
        Object cellVal = null;
        DynaProperty property = null;

        XSSFCell rowcell = row.getCell(j);
        property = tatBean.getDynaClass().getDynaProperty(headers[j]);
        try {
          if (rowcell != null && !rowcell.equals("")
              && (rowcell.getCellType() != rowcell.CELL_TYPE_BLANK)) {
            if (headers[j].equals("tat_center_id")) {
              tatCenterId = rowcell.getStringCellValue();
              continue nxtCell;
            } else if (headers[j].equals("test_name")) {
              testName = rowcell.getStringCellValue();

              continue nxtCell;
            } else if (headers[j].equals("center_name")) {
              centerName = rowcell.getStringCellValue();

              continue nxtCell;
            } else if (headers[j].equals("center_id")) {
              Double rowCellDouble = rowcell.getNumericCellValue();
              centerId = rowCellDouble.intValue();
              continue nxtCell;
            } else if (headers[j].equals("logistics_tat_hours")) {
              Double rowCellDouble = rowcell.getNumericCellValue();
              logTAThours = new BigDecimal(rowCellDouble);
              continue nxtCell;
            } else if (headers[j].equals("processing_days")) {
              if (rowcell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                rowcell.setCellType(Cell.CELL_TYPE_STRING);
              }
              processingDays = rowcell.getStringCellValue();
              continue nxtCell;

            } else if (headers[j].equals("conduction_start_time")) {
              if (rowcell.getDateCellValue() != null) {
                condStartTime = new Time(rowcell.getDateCellValue().getTime());
              }
              continue nxtCell;
            } else if (headers[j].equals("conduction_tat_hours")) {
              Double rowCellDouble = rowcell.getNumericCellValue();
              conductionTAThours = new BigDecimal(rowCellDouble);
              continue nxtCell;

            }

          }

          if (mandatoryFields.contains(headers[j]) && cellVal == null) {
            addError(lineNumber, headers[j] + " should not be null in the sheet " + sheetName);
            lineHasErrors = true;
            continue nxtCell;

          }
        } catch (Exception ex) {

          if (property != null) {
            addError(lineNumber,
                "Conversion error: Cell value" + " could not be converted to " + property.getType()
                    + " below headers of " + headers[j] + " in the sheet " + sheetName);
          } else {
            addError(lineNumber,
                "Conversion error: Cell value"
                    + " could not be converted to class java.lang.String below headers of "
                    + headers[j] + " in the sheet " + sheetName);
          }
          continue; /* next cell */
        }
      }
      if (lineHasErrors) {
        continue nxtLine;
      }

      /* updating or inserting part */
      Connection con = null;
      boolean success = true;

      try {
        con = DataBaseUtil.getReadOnlyConnection();
        con.setAutoCommit(false);
        tatBean.set("logistics_tat_hours", logTAThours);
        tatBean.set("processing_days", processingDays.toString());
        tatBean.set("conduction_start_time", condStartTime);
        tatBean.set("conduction_tat_hours", conductionTAThours);
        if (tatCenterId != null) {
          tatBean.set("tat_center_id", tatCenterId);
          success &= tatDAO.update(con, tatBean.getMap(), "tat_center_id",
              tatBean.get("tat_center_id")) > 0;
        }

      } catch (Exception exp) {
        logger.error("exception", exp);
      } finally {
        DataBaseUtil.commitClose(con, success);
      }
    }

  }

}
