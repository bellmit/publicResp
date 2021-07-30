package com.insta.hms.mdm.addeditdiagnostics;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.diagnosticsmasters.addtest.TestCharge;
import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;
import com.insta.hms.mdm.ReferenceDataConverter;
import com.insta.hms.mdm.diagnosticcharges.DiagnosticChargeService;
import com.insta.hms.mdm.diagnostics.DiagnosticTestService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: Auto-generated Javadoc

/**
 * The Class AddEditDiagnosticController.
 *
 * @author anil.n
 */
@Controller
@RequestMapping(URLRoute.ADD_EDIT_DIAG_TEST_DETAILS_PATH)
public class AddEditDiagnosticController extends MasterController {

  static Logger logger = LoggerFactory.getLogger(AddEditDiagnosticController.class);

  @Autowired
  private DiagnosticTestService diagTestService;

  @Autowired
  private ReferenceDataConverter converter;

  @Autowired
  private DiagnosticChargeService diagnosticChargeService;

  @Autowired
  private MessageUtil messageUtil;

  /**
   * Instantiates a new adds the edit diagnostic controller.
   *
   * @param service
   *          the service
   */
  public AddEditDiagnosticController(DiagnosticTestService service) {
    super(service, MasterResponseRouter.DIAG_TEST_ROUTER);
  }

  /* (non-Javadoc) @see com.insta.hms.mdm.MasterController#getReferenceLists(java.util.Map) */
  /**
   * For add test details, overriding getReferenceLists() method from MasterController.
   *
   * @param params
   *          the params
   * @return the reference lists
   */
  @Override
  protected Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    return ((DiagnosticTestService) getService()).getAddPageData(params);
  }

  /**
   * (non-Javadoc).
   *
   * @param request
   *          the request
   * @param response
   *          the response
   * @param redirect
   *          the redirect
   * @return the model and view
   * @see com.insta.hms.mdm.MasterController#create(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse,
   *      org.springframework.web.servlet.mvc.support.RedirectAttributes)
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @RequestMapping(value = "/create", method = RequestMethod.POST)
  public ModelAndView create(HttpServletRequest request, HttpServletResponse response,
      RedirectAttributes redirect) {

    Map<String, String[]> parameters = request.getParameterMap();
    String orgId = request.getParameter("orgId");
    StringBuilder msg = new StringBuilder();
    boolean success;
    String testId = diagTestService.getNextTestId();
    success = diagTestService.insertTestDetails(request, testId, msg);
    if (!success) {
      redirect.addFlashAttribute("info", messageUtil.getMessage("flash.update.failed", null));
      redirect.addFlashAttribute("orgId", orgId);
      return new ModelAndView(URLRoute.ADD_EDIT_TEST_REDIRECT_TO_ADD);
    }
    if (msg != null && !"".equals(msg)) {
      redirect.addFlashAttribute("error", msg.toString());
    }
    redirect.addAttribute("testid", testId);
    redirect.addAttribute("orgId", orgId);
    response.setStatus(HttpStatus.CREATED.value());
    return new ModelAndView(URLRoute.ADD_EDIT_TEST_REDIRECT_TO_SHOW);
  }

  /**
   * (non-Javadoc).
   *
   * @param req
   *          the req
   * @param response
   *          the response
   * @return the model and view
   * @see com.insta.hms.mdm.MasterController#show(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @RequestMapping(value = "/show", method = RequestMethod.GET)
  public ModelAndView show(HttpServletRequest req, HttpServletResponse response) {

    ModelAndView mav = new ModelAndView();
    String testId = req.getParameter("testid");
    String orgId = req.getParameter("orgId");
    List<BasicDynaBean> testDeatailsList;
    Map mergeMap = null;
    testDeatailsList = diagTestService.getTestDetails(testId);
    mergeMap = diagTestService.getListEditPageData(testDeatailsList, testId, orgId);
    mav.addAllObjects(mergeMap);
    mav.setViewName(router.route("show"));
    response.setStatus(HttpStatus.OK.value());
    return mav;
  }

  /**
   * (non-Javadoc).
   *
   * @param req
   *          the req
   * @param resp
   *          the resp
   * @param redirect
   *          the redirect
   * @return the model and view
   * @see com.insta.hms.mdm.MasterController#update(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse,
   *      org.springframework.web.servlet.mvc.support.RedirectAttributes)
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @RequestMapping(value = "/update", method = RequestMethod.POST)
  public ModelAndView update(HttpServletRequest req, HttpServletResponse resp,
      RedirectAttributes redirect) {

    boolean success = true;
    String testId = req.getParameter("testId");
    StringBuilder msg = new StringBuilder();
    try {
      success = diagTestService.updateTestDetails(req, testId, msg);
    } catch (Exception exception) {
      logger.debug("Failed to update the test details" + exception.getMessage());
    }
    if (msg != null && !"".equals(msg)) {
      redirect.addFlashAttribute("error", msg.toString());
    }
    redirect.addAttribute("testid", testId);
    redirect.addAttribute("orgId", req.getParameter("orgId"));
    redirect.addAttribute("test_name", req.getParameter("test_name"));
    resp.setStatus(HttpStatus.CREATED.value());
    return new ModelAndView(URLRoute.ADD_EDIT_TEST_REDIRECT_TO_SHOW);
  }

  /**
   * Editcharge.
   *
   * @param req
   *          the req
   * @param resp
   *          the resp
   * @return the model and view
   * @throws SQLException
   *           the SQL exception
   */
  @RequestMapping(value = "/editcharge", method = RequestMethod.GET)
  public ModelAndView editcharge(HttpServletRequest req, HttpServletResponse resp)
      throws SQLException {

    ModelAndView mav = new ModelAndView();
    String orgId = req.getParameter("orgId");
    String testId = req.getParameter("testid");
    String chargeType = req.getParameter("chargeType");

    Map mergeMap = diagTestService.getListEditChargeData(chargeType, testId, orgId);
    mav.addAllObjects(mergeMap);
    mav.setViewName(URLRoute.EDIT_CHARGE_PATH);
    resp.setStatus(HttpStatus.CREATED.value());
    return mav;
  }

  /**
   * Updatecharge.
   *
   * @param req
   *          the req
   * @param resp
   *          the resp
   * @param redirect
   *          the redirect
   * @return the model and view
   */
  @RequestMapping(value = "/updatecharge", method = RequestMethod.POST)
  public ModelAndView updatecharge(HttpServletRequest req, HttpServletResponse resp,
      RedirectAttributes redirect) {

    String orgId = req.getParameter("orgId");
    String testId = req.getParameter("testid");

    String[] bedType = req.getParameterValues("bedTypes");
    String[] regularchargeStr = req.getParameterValues("regularCharges");
    Double[] regularcharge = new Double[regularchargeStr.length];
    for (int i = 0; i < regularcharge.length; i++) {
      regularcharge[i] = Double.parseDouble(regularchargeStr[i]);
    }
    String[] discountStr = req.getParameterValues("discount");
    Double[] discount = new Double[discountStr.length];
    for (int i = 0; i < discountStr.length; i++) {
      discount[i] = Double.parseDouble(discountStr[i]);
    }

    String[] derivedRateplanIds = req.getParameterValues("ratePlanId");
    String[] ratePlanApplicable = req.getParameterValues("applicable");
    boolean success;
    String orgItemCode = req.getParameter("orgItemCode");
    String codeType = req.getParameter("codeType");
    ArrayList<TestCharge> al = new ArrayList<TestCharge>();

    TestCharge tc = null;
    for (int i = 0; i < bedType.length; i++) {
      tc = new TestCharge();
      tc.setBedType(bedType[i]);
      tc.setCharge(new BigDecimal(regularcharge[i]));
      tc.setDiscount(new BigDecimal(discount[i]));
      tc.setOrgId(orgId);
      tc.setPriority("R");
      tc.setTestId(testId);
      tc.setUserName((String) req.getSession(false).getAttribute("userid"));
      al.add(tc);
    }
    success = diagTestService.updateTestCharges(al, testId, orgId, true, orgItemCode, codeType,
        derivedRateplanIds, ratePlanApplicable, bedType, regularcharge, discount);

    if (success) {
      redirect.addFlashAttribute("info",
          messageUtil.getMessage("flash.updated.successfully", null));
    } else {
      redirect.addFlashAttribute("error", messageUtil.getMessage("flash.update.failed", null));
    }
    redirect.addAttribute("testid", testId);
    redirect.addAttribute("orgId", orgId);
    redirect.addAttribute("test_name", req.getParameter("testName"));

    return new ModelAndView(URLRoute.EDIT_TEST_CHARGE_REDIRECT_TO_SHOW);
  }

  /**
   * Gets the codes list of code type.
   *
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the codes list of code type
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  @RequestMapping(value = "/getCodesListOfCodeType", method = RequestMethod.GET)
  public Map getCodesListOfCodeType(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    String searchInput = req.getParameter("query");
    String codeType = req.getParameter("codeType");
    String patientType = (req.getParameter("patientType") != null
        && !"".equals(req.getParameter("patientType").trim())) ? req.getParameter("patientType")
            : "";
    String dialogType = req.getParameter("dialog_type");
    List codesList = null;
    codesList = diagTestService.getCodesListOfCodeType(searchInput, codeType, patientType,
        dialogType);
    Map icdmap = new HashMap();
    icdmap.put("result", ConversionUtils.listBeanToListMap(codesList));
    return icdmap;
  }
}
