package com.insta.hms.mdm.diagnostics;

import com.insta.hms.common.MessageUtil;
import com.insta.hms.exception.InvalidFileFormatException;
import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.BulkDataController;
import com.insta.hms.mdm.MasterResponseRouter;
import com.insta.hms.mdm.ReferenceDataConverter;
import com.insta.hms.mdm.bulk.CsVModelAndView;
import com.insta.hms.mdm.diagnosticcharges.DiagnosticChargeService;
import com.insta.hms.mdm.diagtatcenter.DiagTatCenterService;
import com.insta.hms.mdm.diagtestresults.DiagTestResultService;
import com.insta.hms.mdm.diagtesttemplates.DiagTestTemplateService;
import com.insta.hms.mdm.organization.OrganizationService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class DiagnosticTestController.
 *
 * @author anil.n
 */

@Controller
@RequestMapping(URLRoute.DIAG_TEST_DETAILS_PATH)
public class DiagnosticTestController extends BulkDataController {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(DiagnosticTestController.class);

  /** The message util. */
  @Autowired
  private MessageUtil messageUtil;

  /** The diag test service. */
  @Autowired
  private DiagnosticTestService diagTestService;

  /** The converter. */
  @Autowired
  private ReferenceDataConverter converter;

  /** The diag test template service. */
  @Autowired
  private DiagTestTemplateService diagTestTemplateService;

  /** The diag test result service. */
  @Autowired
  private DiagTestResultService diagTestResultService;

  /** The diag tat center service. */
  @Autowired
  private DiagTatCenterService diagTatCenterService;

  /** The diagnostic charge service. */
  @Autowired
  private DiagnosticChargeService diagnosticChargeService;

  /** The organization service. */
  @Autowired
  private OrganizationService organizationService;

  /** The Constant FILE_NAME. */
  static final String FILE_NAME = "diagnostics";
  
  /** The Constant ERROR. */
  private static final String ERROR = "error";
  
  /** The Constant HEADERS. */
  private static final String HEADERS = "headers";
  
  /** The Constant RESULT. */
  private static final String RESULT = "result";
  
  /** The Constant WARNINGS. */
  private static final String WARNINGS = "warnings";

  /**
   * Instantiates a new diagnostic test controller.
   *
   * @param service
   *          the service
   */
  public DiagnosticTestController(DiagnosticTestService service) {
    super(service, MasterResponseRouter.DIAG_TEST_ROUTER, FILE_NAME);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.mdm.MasterController#getFilterLookupLists(java.util.Map)
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  protected Map<String, List<BasicDynaBean>> getFilterLookupLists(Map params) {

    return ((DiagnosticTestService) getService()).getListPageData(params);
  }

  /**
   * Download test details.
   *
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the model and view
   */
  @RequestMapping(value = "/testDetailsExport", method = RequestMethod.GET)
  public ModelAndView downloadTestDetails(HttpServletRequest request, 
      HttpServletResponse response) {

    Map<String, List<String[]>> csvData = diagTestService.exportData();

    CsVModelAndView mav = new CsVModelAndView("Test_Details");
    mav.addHeader(csvData.get(HEADERS).get(0));
    mav.addData(csvData.get("rows"));
    return mav;
  }

  /**
   * Download test results.
   *
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the model and view
   */
  @RequestMapping(value = "/testResultsExport", method = RequestMethod.GET)
  public ModelAndView downloadTestResults(HttpServletRequest request, 
      HttpServletResponse response) {

    Map<String, List<String[]>> csvData = diagTestResultService.exportData();

    CsVModelAndView mav = new CsVModelAndView("Test_Results");
    mav.addHeader(csvData.get(HEADERS).get(0));
    mav.addData(csvData.get("rows"));
    return mav;
  }

  /**
   * Download test template.
   *
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the model and view
   */
  @RequestMapping(value = "/testTemplateExport", method = RequestMethod.GET)
  public ModelAndView downloadTestTemplate(HttpServletRequest request, 
      HttpServletResponse response) {

    Map<String, List<String[]>> csvData = diagTestTemplateService.exportData();

    CsVModelAndView mav = new CsVModelAndView("Test_Template");
    mav.addHeader(csvData.get(HEADERS).get(0));
    mav.addData(csvData.get("rows"));
    return mav;
  }

  /**
   * Download test tat details.
   *
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the model and view
   */
  @RequestMapping(value = "/testTatExport", method = RequestMethod.GET)
  public ModelAndView downloadTestTatDetails(HttpServletRequest request,
      HttpServletResponse response) {

    Map<String, List<String[]>> csvData = diagTatCenterService.exportData();

    CsVModelAndView mav = new CsVModelAndView("Test_Tat_Details");
    mav.addHeader(csvData.get(HEADERS).get(0));
    mav.addData(csvData.get("rows"));
    return mav;
  }

  /**
   * Download test charges.
   *
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the model and view
   */
  @RequestMapping(value = "/testChargesExport", method = RequestMethod.GET)
  public ModelAndView downloadTestCharges(HttpServletRequest request, 
      HttpServletResponse response) {

    String orgId = request.getParameter("orgId");
    Map<String, List<String[]>> csvData = diagnosticChargeService.exportData(orgId);
    BasicDynaBean bean = organizationService.getOrgdetailsDynaBean(orgId);
    String orgName = (String) bean.get("org_name");
    CsVModelAndView mav = new CsVModelAndView("TestRates_" + orgName);
    mav.addHeader(csvData.get(HEADERS).get(0));
    mav.addData(csvData.get("rows"));
    return mav;
  }

  /**
   * Import templates.
   *
   * @param request
   *          the request
   * @param mmap
   *          the mmap
   * @param response
   *          the response
   * @param redirect
   *          the redirect
   * @param file
   *          the file
   * @return the model and view
   * @throws ParseException
   *           the parse exception
   */
  @RequestMapping(value = "/importTemplates", method = RequestMethod.POST)
  public ModelAndView importTemplates(HttpServletRequest request, ModelMap mmap,
      HttpServletResponse response, RedirectAttributes redirect,
      @RequestPart("uploadFile") MultipartFile file) throws ParseException {

    ModelAndView mav = new ModelAndView();
    Map<String, MultiValueMap<Object, Object>> feedback = new HashMap<>();
    String error = diagTestTemplateService.importData(file, feedback);

    if (null != error) {
      redirect.addFlashAttribute(ERROR, messageUtil.getMessage(error, null));
      throw new InvalidFileFormatException(error);
    } else {
      redirect.addFlashAttribute("info", feedback.get(RESULT).toSingleValueMap());
      redirect.addFlashAttribute(ERROR, feedback.get(WARNINGS).toSingleValueMap());
    }

    mav.setViewName(URLRoute.DIAG_TEST_DETAILS_PATH_REDIRECT);
    return mav;
  }

  /**
   * Import result labels.
   *
   * @param request
   *          the request
   * @param mmap
   *          the mmap
   * @param response
   *          the response
   * @param redirect
   *          the redirect
   * @param file
   *          the file
   * @return the model and view
   * @throws ParseException
   *           the parse exception
   */
  @RequestMapping(value = "/importResultLabels", method = RequestMethod.POST)
  public ModelAndView importResultLabels(HttpServletRequest request, ModelMap mmap,
      HttpServletResponse response, RedirectAttributes redirect,
      @RequestPart("uploadFile") MultipartFile file) throws ParseException {

    ModelAndView mav = new ModelAndView();
    Map<String, MultiValueMap<Object, Object>> feedback = new HashMap<>();
    String error = diagTestResultService.importData(file, feedback);

    if (null != error) {
      redirect.addFlashAttribute(ERROR, messageUtil.getMessage(error, null));
      throw new InvalidFileFormatException(error);
    } else {
      redirect.addFlashAttribute("info", feedback.get(RESULT).toSingleValueMap());
      redirect.addFlashAttribute(ERROR, feedback.get(WARNINGS).toSingleValueMap());
    }

    mav.setViewName(URLRoute.DIAG_TEST_DETAILS_PATH_REDIRECT);
    return mav;
  }

  /**
   * Import test details.
   *
   * @param request
   *          the request
   * @param mmap
   *          the mmap
   * @param response
   *          the response
   * @param redirect
   *          the redirect
   * @param file
   *          the file
   * @return the model and view
   * @throws ParseException
   *           the parse exception
   */
  @RequestMapping(value = "/importTestDetails", method = RequestMethod.POST)
  public ModelAndView importTestDetails(HttpServletRequest request, ModelMap mmap,
      HttpServletResponse response, RedirectAttributes redirect,
      @RequestPart("uploadFile") MultipartFile file) throws ParseException {

    ModelAndView mav = new ModelAndView();
    Map<String, MultiValueMap<Object, Object>> feedback = new HashMap<>();
    String error = diagTestService.importData(file, feedback);

    if (null != error) {
      redirect.addFlashAttribute(ERROR, messageUtil.getMessage(error, null));
      throw new InvalidFileFormatException(error);
    } else {
      redirect.addFlashAttribute("info", feedback.get(RESULT).toSingleValueMap());
      redirect.addFlashAttribute(ERROR, feedback.get(WARNINGS).toSingleValueMap());
    }

    mav.setViewName(URLRoute.DIAG_TEST_DETAILS_PATH_REDIRECT);
    return mav;
  }

  /**
   * Import test charges.
   *
   * @param request
   *          the request
   * @param mmap
   *          the mmap
   * @param response
   *          the response
   * @param redirect
   *          the redirect
   * @param file
   *          the file
   * @return the model and view
   * @throws ParseException
   *           the parse exception
   */
  @RequestMapping(value = "/importTestCharges", method = RequestMethod.POST)
  public ModelAndView importTestCharges(HttpServletRequest request, ModelMap mmap,
      HttpServletResponse response, RedirectAttributes redirect,
      @RequestPart("uploadFile") MultipartFile file) throws ParseException {

    String orgId = request.getParameter("orgId");
    String userId = (String) request.getSession().getAttribute("userid");
    Map<String, MultiValueMap<Object, Object>> feedback = new HashMap<>();
    diagnosticChargeService.backUpcharges(orgId, userId);
    String error = diagnosticChargeService.importCsvData(file, feedback, orgId);
    diagnosticChargeService.updateChargesForDerivedRatePlans(orgId, userId, "tests", true);

    if (null != error) {
      redirect.addFlashAttribute(ERROR, messageUtil.getMessage(error, null));
      throw new InvalidFileFormatException(error);
    } else {
      redirect.addFlashAttribute("info", feedback.get(RESULT).toSingleValueMap());
      redirect.addFlashAttribute(ERROR, feedback.get(WARNINGS).toSingleValueMap());
    }
    redirect.addAttribute("org_id", orgId);
    ModelAndView mav = new ModelAndView();
    mav.setViewName(URLRoute.DIAG_TEST_DETAILS_PATH_REDIRECT);
    return mav;
  }

  /**
   * Import test tat details.
   *
   * @param request
   *          the request
   * @param mmap
   *          the mmap
   * @param response
   *          the response
   * @param redirect
   *          the redirect
   * @param file
   *          the file
   * @return the model and view
   * @throws ParseException
   *           the parse exception
   */
  @RequestMapping(value = "/importTatDetails", method = RequestMethod.POST)
  public ModelAndView importTestTatDetails(HttpServletRequest request, ModelMap mmap,
      HttpServletResponse response, RedirectAttributes redirect,
      @RequestPart("uploadFile") MultipartFile file) throws ParseException {

    ModelAndView mav = new ModelAndView();
    Map<String, MultiValueMap<Object, Object>> feedback = new HashMap<>();
    String error = diagTatCenterService.importData(file, feedback);

    if (null != error) {
      redirect.addFlashAttribute(ERROR, messageUtil.getMessage(error, null));
      throw new InvalidFileFormatException(error);
    } else {
      redirect.addFlashAttribute("info", feedback.get(RESULT).toSingleValueMap());
      redirect.addFlashAttribute(ERROR, feedback.get(WARNINGS).toSingleValueMap());
    }

    mav.setViewName(URLRoute.DIAG_TEST_DETAILS_PATH_REDIRECT);
    return mav;
  }

  /**
   * Group update.
   *
   * @param request
   *          the request
   * @param mmap
   *          the mmap
   * @param response
   *          the response
   * @param redirect
   *          the redirect
   * @return the model and view
   * @throws ParseException
   *           the parse exception
   */
  @RequestMapping(value = "/groupUpdate", method = RequestMethod.POST)
  public ModelAndView groupUpdate(HttpServletRequest request, ModelMap mmap,
      HttpServletResponse response, RedirectAttributes redirect) throws ParseException {

    BigDecimal amount = new BigDecimal(request.getParameter("amount"));
    String userName = (String) request.getSession().getAttribute("userid");
    String[] selectTest = request.getParameterValues("selectTest");
    String[] selectBedType = request.getParameterValues("selectBedType");
    if (request.getParameter("incType").equals("-")) {
      amount = amount.negate();
    }
    List<String> selectTests = null;
    if ((selectTest != null)) {
      selectTests = Arrays.asList(selectTest);
    }
    List<String> bedTypes = null;
    if ((selectBedType != null)) {
      bedTypes = Arrays.asList(selectBedType);
    }
    boolean success = true;
    String orgId = request.getParameter("orgId");
    String amtType = request.getParameter("amtType");
    BigDecimal roundOff = new BigDecimal(request.getParameter("roundOff"));
    String updateTable = request.getParameter("updateTable");
    success = diagnosticChargeService.groupIncreaseOrDecreaseTestCharges(orgId, bedTypes,
        selectTests, amount, amtType.equals("%"), roundOff, updateTable, (String) request
            .getSession(false).getAttribute("userid"));

    if (success) {
      diagnosticChargeService.updateChargesForDerivedRatePlans(orgId, userName, "tests", false);
    }

    if (success) {
      redirect
          .addFlashAttribute("info", messageUtil.getMessage("flash.updated.successfully", null));
    } else {
      redirect.addFlashAttribute(ERROR, messageUtil.getMessage("flash.update.failed", null));
    }
    ModelAndView mav = new ModelAndView();
    mav.setViewName(URLRoute.DIAG_TEST_DETAILS_PATH_REDIRECT);
    return mav;
  }

}