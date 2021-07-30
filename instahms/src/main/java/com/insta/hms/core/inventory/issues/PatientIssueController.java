package com.insta.hms.core.inventory.issues;

import com.insta.hms.common.BaseController;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.core.billing.BillService;
import com.insta.hms.core.insurance.SponsorService;
import com.insta.hms.core.inventory.PageRoute;
import com.insta.hms.core.inventory.URLRoute;
import com.insta.hms.core.inventory.stockmgmt.StockService;
import com.insta.hms.core.patient.registration.PatientInsurancePlansService;
import com.insta.hms.core.patient.registration.RegistrationService;
import com.insta.hms.mdm.discountplans.DiscountPlanService;
import com.insta.hms.mdm.orderkit.OrderkitService;
import com.insta.hms.mdm.organization.OrganizationService;
import com.insta.hms.mdm.stores.StoreService;
import com.insta.hms.mdm.storestockmaintimestamp.StoreStockMainTimeStampService;
import com.insta.hms.mdm.taxgroups.TaxGroupService;
import com.insta.hms.mdm.taxsubgroups.TaxSubGroupService;
import com.insta.hms.security.usermanager.UserService;
import com.lowagie.text.DocumentException;

import flexjson.JSONSerializer;
import freemarker.template.TemplateException;
import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;


import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

// TODO: Auto-generated Javadoc
/**
 * This class is used for patient issue screen operations.
 * 
 * @author irshadmohammed
 *
 */
@Controller
@RequestMapping(URLRoute.PATIENT_ISSUES)
public class PatientIssueController extends BaseController {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(PatientIssueController.class);

  /** The generic preferences service. */
  @LazyAutowired
  private GenericPreferencesService genericPreferencesService;

  /** The store service. */
  @LazyAutowired
  private StoreService storeService;

  /** The store stock main time stamp service. */
  @LazyAutowired
  private StoreStockMainTimeStampService storeStockMainTimeStampService;

  /** The user service. */
  @LazyAutowired
  private UserService userService;

  /** The orderkit service. */
  @LazyAutowired
  private OrderkitService orderkitService;

  /** The discount plan service. */
  @LazyAutowired
  private DiscountPlanService discountPlanService;

  /** The organization service. */
  @LazyAutowired
  private OrganizationService organizationService;

  /** The registration service. */
  @LazyAutowired
  private RegistrationService registrationService;

  /** The bill service. */
  @LazyAutowired
  private BillService billService;

  /** The patient insurance plans service. */
  @LazyAutowired
  private PatientInsurancePlansService patientInsurancePlansService;

  /** The patient issue service. */
  @LazyAutowired
  private PatientIssueService patientIssueService;

  /** The stock service. */
  @LazyAutowired
  private StockService stockService;

  /** The TaxGroup Service. */
  @LazyAutowired
  private TaxGroupService itemGroupService;

  /** The Tax Subgroup Service. */
  @LazyAutowired
  private TaxSubGroupService itemSubGroupService;

  /** The sponsor service. */
  @LazyAutowired
  SponsorService sponsorService;


  /** The js. */
  JSONSerializer js = new JSONSerializer().exclude("class");

  /**
   * Gets the patient issue screen.
   *
   * @param req
   *          the req
   * @param resp
   *          the resp
   * @return the patient issue screen
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = { "", URLRoute.PATIENT_ISSUES_SCREEN }, method = RequestMethod.GET)
  public ModelAndView getPatientIssueScreen(HttpServletRequest req, HttpServletResponse resp) {
    ModelAndView model = new ModelAndView();
    Map<String, Object> params = getParameterMap(req);
    HttpSession session = req.getSession(false);

    String msg = (String) params.get("message");
    Integer roleId = (Integer) session.getAttribute("roleId");
    String userName = (String) session.getAttribute("userid");

    List<BasicDynaBean> storesList = null;
    if (roleId == 1 || roleId == 2) {
      storesList = storeService.listAllActive();
    } else {
      storesList = storeService.findByUser(userName);
    }
    final List<BasicDynaBean> organizationDetailsList = organizationService.lookup(true);
    Map<String, Object> filterMap = new HashMap<>();
    filterMap.put("item_group_type_id", "TAX");
    final List<BasicDynaBean> itemGroupList = itemGroupService.lookup(true,filterMap);
    final List<BasicDynaBean> itemSubGroupList = itemSubGroupService.lookup(true);

    if (msg == null || msg.isEmpty()) {
      msg = "0";
    }
    BasicDynaBean userBean = userService.findByKey("emp_username", userName);
    if (userBean != null) {
      model.addObject("isSharedLogIn", userBean.get("is_shared_login"));
    }

    model.addObject("message", msg);// Used as conditional check too
    /* Required for newUX flow */
    String isNewUX = (String) params.get("isNewUX");
    model.addObject("isNewUX", isNewUX);

    BasicDynaBean genericPrefBean = genericPreferencesService.getAllPreferences();
    model.addObject("generic_prefs", genericPrefBean.getMap());
    int stockMainTimeStamp = storeStockMainTimeStampService.getMedicineMainTimestamp();
    model.addObject("stock_timestamp", stockMainTimeStamp);
    model.addObject("operation_details_id", params.get("operation_details_id"));
    model.addObject("msg", params.get("msg") != null ? params.get("msg") : "");
    model.addObject("flag", params.get("flag") != null ? params.get("flag") : "");
    String storeId = (String) session.getAttribute("pharmacyStoreId");
    model.addObject("store_id", storeId);
    if ((roleId == 1 || roleId == 2) && (storeId == null || storeId.isEmpty())) {
      model.addObject("store_id", 0);
    }
    model.addObject("gtpass", params.get("gtpass") != null ? (String) params.get("gtpass") : false);
    List<BasicDynaBean> orderkitList = orderkitService.lookup(true);
    model.addObject("order_kits", ConversionUtils.copyListDynaBeansToMap(orderkitList));
    List<BasicDynaBean> discountPlanDetailsList = discountPlanService
        .listDiscountPlanDetails(null, "priority");
    model.addObject("discount_plans",
        ConversionUtils.copyListDynaBeansToMap(discountPlanDetailsList));
    model.addObject("stores", ConversionUtils.copyListDynaBeansToMap(storesList));
    model.addObject("org_details", ConversionUtils.copyListDynaBeansToMap(organizationDetailsList));
    model.addObject("subGroupListJSON",
        js.serialize(ConversionUtils.listBeanToListMap(itemSubGroupList)));
    model.addObject("groupList", ConversionUtils.listBeanToListMap(itemGroupList));
    model.addObject("groupListJSON",
        js.serialize(ConversionUtils.listBeanToListMap(itemGroupList)));
    String visitId = (String) params.get("visit_id");
    model.addObject("visit_id", visitId);
    String billNo = (String) params.get("bill_no");
    model.addObject("bill_no", billNo);
    model.setViewName(PageRoute.PATIENT_ISSUES_ADD);
    return model;
  }

  /**
   * Gets the patient details.
   *
   * @param req
   *          the req
   * @param resp
   *          the resp
   * @return the patient details
   */
  @RequestMapping(value = URLRoute.GET_PATIENT_DETAILS, method = RequestMethod.GET)
  public Map<String, Object> getPatientDetails(HttpServletRequest req, HttpServletResponse resp) {
    Map<String, Object> responseMap = new HashMap<String, Object>();
    Map<String, Object> params = getParameterMap(req);
    if (params.get("visit_id") != null) {
      String visitId = (String) params.get("visit_id");
      responseMap = patientIssueService.getPatientDetails(params);
    }
    return responseMap;
  }

  /**
   * Gets the item batch details.
   *
   * @param req
   *          the req
   * @param resp
   *          the resp
   * @return the item details
   */
  @SuppressWarnings("unchecked")
  @RequestMapping(value = URLRoute.GET_ITEM_BATCH_DETAILS, method = RequestMethod.GET)
  @IgnoreConfidentialFilters
  public List<Map<String, Object>> getItemBatchDetails(HttpServletRequest req,
      HttpServletResponse resp) {
    List<Map<String, Object>> response = new ArrayList<Map<String, Object>>();
    Map<String, Object> params = getParameterMap(req);
    int storeId = -10;
    int medicineId = -1;
    if (params.get("store_id") != null && !((String) params.get("store_id")).isEmpty()) {
      storeId = Integer.parseInt((String) params.get("store_id"));
    }
    if (params.get("medicine_id") != null && !((String) params.get("medicine_id")).isEmpty()) {
      medicineId = Integer.parseInt((String) params.get("medicine_id"));
    }
    if (params.get("store_id") != null && params.get("medicine_id") != null) {
      response = ConversionUtils
          .copyListDynaBeansToMap(stockService.getItemDetails(storeId, medicineId));
    }
    return response;
  }

  /**
   * Gets the item amount details.
   *
   * @param req
   *          the req
   * @param resp
   *          the resp
   * @return the item amount details
   */
  @RequestMapping(value = URLRoute.GET_ITEM_AMOUNT_DETAILS, method = RequestMethod.POST)
  public Map<String, Object> getItemAmountDetails(HttpServletRequest req,
      HttpServletResponse resp) {
    Map<String, Object> params = getParameterMap(req);
    return patientIssueService.getItemAmounts(params);
  }

  /**
   * Save patient issue details.
   *
   * @param req
   *          the req
   * @param resp
   *          the resp
   * @return the model and view
   */
  @RequestMapping(value = URLRoute.SAVE_ISSUE_DETAILS, method = RequestMethod.POST)
  public ModelAndView savePatientIssueDetails(HttpServletRequest req, HttpServletResponse resp) {
    ModelAndView model = new ModelAndView();
    try {
      List<Map<String, Object>> cacheIssueTxns = new ArrayList<>();
      model.addAllObjects(patientIssueService.savePatientIssueDetails(req, cacheIssueTxns));
    } catch (SQLException exception) {
      model.addObject("flag", false);
      model.addObject("msg", exception);
    } catch (PatientIssueException ex) {
      model.addObject("flag", false);
      model.addObject("msg", ex.getMessageKey());
    }
    model.setViewName("redirect:/patientissues/add");
    return model;
  }

  /**
   * Generate gate passprint for issue.
   *
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the object
   */
  @RequestMapping(value = URLRoute.GENERATE_GATE_PASS, method = RequestMethod.GET)
  @IgnoreConfidentialFilters
  public Object generateGatePassprintForIssue(HttpServletRequest req, HttpServletResponse res) {
    try {
      Map<String, Object> responseMap = patientIssueService
          .generateGatePassprintForIssue(getParameterMap(req));
      if (responseMap.get("type").equals("pdf")) {

        byte[] data = (byte[]) responseMap.get("data");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/pdf"));
        headers.set("Content-Disposition", "inline; filename='" + "output.pdf" + "'");
        return new ResponseEntity<byte[]>(data, headers, HttpStatus.OK);
      } else {
        ModelAndView mav = new ModelAndView();
        mav.addAllObjects(responseMap);
        mav.setViewName("redirect:/pages/Common/PrintTextReport.jsp");
        return mav;
      }
    } catch (XPathExpressionException | IOException | TemplateException | SQLException
        | DocumentException | TransformerException ex) {
      logger.error("", ex);
    }

    return null;

  }

  /**
   * Gets supported tax groups.
   *
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the tax groups.
   */
  @SuppressWarnings("unchecked")
  @RequestMapping(value = URLRoute.GET_SUPPORTED_TAX_GROUPS, method = RequestMethod.GET)
  @IgnoreConfidentialFilters
  public Map<String, Object> getSupportedTaxGroups(HttpServletRequest request,
      HttpServletResponse response) {
    Map<String, Object> taxGroups = patientIssueService.getSupportedTaxGroups();
    return taxGroups;
  }

  /**
   * This method will calculate amounts for all items in screen.
   *
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the bulk item amounts details
   */
  @RequestMapping(value = URLRoute.GET_BULK_ITEM_AMOUNT_DETAILS, method = RequestMethod.POST)
  public List<Map<String, Object>> getBulkItemAmountsDetails(HttpServletRequest req,
      HttpServletResponse res) {
    return patientIssueService.getAllAmounts(req.getParameterMap());
  }

  /**
   * Gets the insurance category payable status.
   *
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the insurance category payable status
   */
  @RequestMapping(value = URLRoute.GET_INSURANCE_CATEGORY_PAYABLE_STATUS,
      method = RequestMethod.GET)
  @IgnoreConfidentialFilters
  public ResponseEntity<List> getInsuranceCategoryPayableStatus(final HttpServletRequest req,
      HttpServletResponse res) {
    List<Map<String, Object>> result = new ArrayList<>();
    result.add(patientIssueService.getInsuranceCategoryPayableStatus(req.getParameterMap()));
    return new ResponseEntity<List>(result, HttpStatus.OK);
  }

  /**
   * Gets the order kit items.
   *
   * @param req
   *          the req
   * @param resp
   *          the resp
   * @return the order kit items
   */
  @RequestMapping(value = URLRoute.GET_ORDERKIT_ITEMS, method = RequestMethod.GET)
  @IgnoreConfidentialFilters
  public Map<String, Object> getOrderKitItems(HttpServletRequest req, HttpServletResponse resp) {
    return patientIssueService.getOrderKitItems(getParameterMap(req));
  }

  /**
   * Gets the issues charge claims.
   *
   * @param req
   *          the req
   * @param resp
   *          the resp
   * @return the issues charge claims
   */
  @RequestMapping(value = URLRoute.GET_ISSUES_CHARGE_CLAIMS, method = RequestMethod.POST)
  public ResponseEntity<Map<Integer, Map>> getIssuesChargeClaims(HttpServletRequest req,
      HttpServletResponse resp) {
    return new ResponseEntity<Map<Integer, Map>>(
        sponsorService.getIssuesChargeClaims(req.getParameterMap()), HttpStatus.OK);
  }

  /**
   * Gets the claim amount.
   *
   * @param req
   *          the req
   * @param resp
   *          the resp
   * @return the claim amount
   */
  @RequestMapping(value = URLRoute.GET_CLAIM_AMOUNT, method = RequestMethod.POST)
  public Map<String, Object> getClaimAmount(HttpServletRequest req, HttpServletResponse resp) {
    return (patientIssueService.getClaimAmount(getParameterMap(req)));
  }

  /**
   * Gets the package details.
   *
   * @param req the req
   * @param resp the resp
   * @return the package details
   */
  @RequestMapping(value = URLRoute.GET_PKG_DETAILS, method = RequestMethod.GET)
  public Map<String, Object> getPackageDetails(HttpServletRequest req, HttpServletResponse resp) {
    Map<String, Object> pkgDetailsMap = new HashMap<>();
    Map<String, Object> params = getParameterMap(req);
    if (params.get("visit_id") != null) {
      pkgDetailsMap = patientIssueService.getPackageDetails(params);
    }

    return pkgDetailsMap;
  }

}
