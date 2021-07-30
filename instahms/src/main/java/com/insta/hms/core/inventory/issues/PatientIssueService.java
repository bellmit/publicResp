package com.insta.hms.core.inventory.issues;

import com.bob.hms.common.DateUtil;
import com.insta.hms.billing.ChargeDTO;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FtlProcessor;
import com.insta.hms.common.GenericRepository;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.common.taxation.ItemTaxDetails;
import com.insta.hms.common.taxation.TaxCalculator;
import com.insta.hms.common.taxation.TaxContext;
import com.insta.hms.core.billing.AllocationService;
import com.insta.hms.core.billing.BillChargeClaimService;
import com.insta.hms.core.billing.BillChargeService;
import com.insta.hms.core.billing.BillChargeTaxService;
import com.insta.hms.core.billing.BillHelper;
import com.insta.hms.core.billing.BillRepository;
import com.insta.hms.core.billing.BillService;
import com.insta.hms.core.clinical.adt.BedNamesRepository;
import com.insta.hms.core.clinical.order.packageitems.MultiVisitPackageService;
import com.insta.hms.core.clinical.order.packageitems.PackageOrderItemService;
import com.insta.hms.core.insurance.SponsorService;
import com.insta.hms.core.inventory.StoresHelper;
import com.insta.hms.core.inventory.patientindent.StorePatientIndentDetailsService;
import com.insta.hms.core.inventory.patientindent.StorePatientIndentService;
import com.insta.hms.core.inventory.stockmgmt.StockRepository;
import com.insta.hms.core.inventory.stockmgmt.StockService;
import com.insta.hms.core.inventory.stocks.StockFifoService;
import com.insta.hms.core.inventory.stocks.StoreGRNDetailsRepository;
import com.insta.hms.core.inventory.stocks.StoreItemCodesRepository;
import com.insta.hms.core.inventory.stocks.StoreStockDetailsService;
import com.insta.hms.core.patient.PatientDetailsService;
import com.insta.hms.core.patient.registration.PatientInsurancePlansService;
import com.insta.hms.core.patient.registration.PatientRegistrationRepository;
import com.insta.hms.core.patient.registration.PatientRegistrationService;
import com.insta.hms.core.patient.registration.RegistrationService;
import com.insta.hms.documents.PrintConfigurationRepository;
import com.insta.hms.exception.HMSException;
import com.insta.hms.insurance.AdvanceInsuranceCalculator;
import com.insta.hms.integration.scm.inventory.ScmOutBoundInvService;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.mdm.chargeheads.ChargeHeadsService;
import com.insta.hms.mdm.discountplans.DiscountPlanService;
import com.insta.hms.mdm.healthauthoritypreferences.HealthAuthorityPreferencesService;
import com.insta.hms.mdm.insuranceplans.InsurancePlanService;
import com.insta.hms.mdm.item.StoreItemDetailsRepository;
import com.insta.hms.mdm.item.StoreItemDetailsService;
import com.insta.hms.mdm.orderkit.OrderkitService;
import com.insta.hms.mdm.ordersets.PackageChargesService;
import com.insta.hms.mdm.ordersets.PackageService;
import com.insta.hms.mdm.ordersets.PackagesDTO;
import com.insta.hms.mdm.ordersets.PackagesRepository;
import com.insta.hms.mdm.organization.OrganizationRepository;
import com.insta.hms.mdm.packages.PatientPackageContentConsumedService;
import com.insta.hms.mdm.printtemplates.PrintTemplate;
import com.insta.hms.mdm.printtemplates.PrintTemplateService;
import com.insta.hms.mdm.storeitembatchdetails.StoreItemBatchDetailsService;
import com.insta.hms.mdm.stores.StoreRepository;
import com.insta.hms.mdm.stores.StoreService;
import com.insta.hms.mdm.taxgroups.TaxGroupService;
import com.insta.hms.mdm.taxsubgroups.TaxSubGroupService;
import com.insta.hms.mdm.tpas.TpaService;
import com.lowagie.text.DocumentException;

import flexjson.JSONSerializer;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

/**
 * This class is used to for patient issue operations.
 * 
 * @author irshadmohammed
 *
 */
@Service
public class PatientIssueService {

  /** The calculators. */
  Map<String, TaxCalculator> calculators = new HashMap<String, TaxCalculator>();

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(PatientIssueService.class);

  /** The registration service. */
  @LazyAutowired
  private RegistrationService registrationService;

  /** The bill service. */
  @LazyAutowired
  private BillService billService;

  /** The Stock service. */
  @LazyAutowired
  private StockService stockService;

  /** The Store service. */
  @LazyAutowired
  private StoreService storeService;

  /** The store item batch details service. */
  @LazyAutowired
  private StoreItemBatchDetailsService storeItemBatchDetailsService;

  /** The patient registration service. */
  @LazyAutowired
  private PatientRegistrationService patientRegistrationService;

  /** The patient registration repository. */
  @LazyAutowired
  private PatientRegistrationRepository patientRegistrationRepo;

  /** The Stock Repository. */
  @LazyAutowired
  private StockRepository stockRepository;

  /** The store item details repository. */
  @LazyAutowired
  private StoreItemDetailsRepository storeItemDetailsRepository;

  /** The ftl processor. */
  @LazyAutowired
  private FtlProcessor ftlProcessor;

  /** The TaxGroup Service. */
  @LazyAutowired
  private TaxGroupService itemGroupService;

  /** The Tax Subgroup Service. */
  @LazyAutowired
  private TaxSubGroupService itemSubGroupService;

  /** Item Service. */
  @LazyAutowired
  StoreItemDetailsService itemService;

  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;

  /** The center service. */
  @LazyAutowired
  private CenterService centerService;

  /** The patient details service. */
  @LazyAutowired
  private PatientDetailsService patientDetailsService;
  
  @LazyAutowired
  private RegistrationService regService;

  /** The tpa service. */
  @LazyAutowired
  private TpaService tpaService;

  /** The stock issue details repository. */
  @LazyAutowired
  private StockIssueDetailsRepository stockIssueDetailsRepository;

  /** The generic preferences service. */
  @LazyAutowired
  private GenericPreferencesService genericPreferencesService;

  /** The organization repository. */
  @LazyAutowired
  private OrganizationRepository organizationRepository;

  /** The stock issue main repository. */
  @LazyAutowired
  private StockIssueMainRepository stockIssueMainRepository;

  /** The store stock details service. */
  @LazyAutowired
  private StoreStockDetailsService storeStockDetailsService;

  /** The admission repository. */
  @LazyAutowired
  private AdmissionRepository admissionRepository;

  /** The bed names repository. */
  @LazyAutowired
  private BedNamesRepository bedNamesRepository;

  /** The store gatepass repository. */
  @LazyAutowired
  private StoreGatePassRepository storeGatepassRepository;

  /** The store consignment invoice repository. */
  @LazyAutowired
  private StoreConsignmentInvoiceRepository storeConsignmentInvoiceRepository;

  /** The store GRN details repository. */
  @LazyAutowired
  private StoreGRNDetailsRepository storeGRNDetailsRepository;

  /** The bill repository. */
  @LazyAutowired
  private BillRepository billRepository;

  /** The store repository. */
  @LazyAutowired
  private StoreRepository storeRepository;

  /** The bill charge service. */
  @LazyAutowired
  private BillChargeService billChargeService;

  /** The health authority preferences service. */
  @LazyAutowired
  private HealthAuthorityPreferencesService healthAuthorityPreferencesService;

  /** The store item codes repository. */
  @LazyAutowired
  private StoreItemCodesRepository storeItemCodesRepository;

  /** The charge heads service. */
  @LazyAutowired
  private ChargeHeadsService chargeHeadsService;

  /** The patient insurance plans service. */
  @LazyAutowired
  private PatientInsurancePlansService patientInsurancePlansService;

  /** The bill charge claim service. */
  @LazyAutowired
  private BillChargeClaimService billChargeClaimService;

  /** The store patient indent details service. */
  @LazyAutowired
  private StorePatientIndentDetailsService storePatientIndentDetailsService;

  /** The stock fifo service. */
  @LazyAutowired
  private StockFifoService stockFifoService;

  /** The bill charge tax service. */
  @LazyAutowired
  private BillChargeTaxService billChargeTaxService;

  /** The sponsor service. */
  @LazyAutowired
  private SponsorService sponsorService;

  /** The store patient indent service. */
  @LazyAutowired
  private StorePatientIndentService storePatientIndentService;

  /** The orderkit service. */
  @LazyAutowired
  private OrderkitService orderkitService;

  /** The print configuration repository. */
  @LazyAutowired
  private PrintConfigurationRepository printConfigurationRepository;

  /** The print template service. */
  @LazyAutowired
  private PrintTemplateService printTemplateService;

  /** The insurance plan service. */
  @LazyAutowired
  private InsurancePlanService insurancePlanService;

  /** The scm outbound inv service. */
  @LazyAutowired
  private ScmOutBoundInvService scmOutService;

  /** Allocation service. */
  @LazyAutowired
  private AllocationService allocationService;

  /** The packages service. */
  @LazyAutowired
  private PackageService pkgService;

  /** The package charges service. */
  @LazyAutowired
  private PackageChargesService pkgChargesService;

  /** The bill helper. */
  @LazyAutowired
  private BillHelper billHelper;

  /** The multi pkg service. */
  @LazyAutowired
  private MultiVisitPackageService multiPkgService;

  /** The patient package content consumed service. */
  @LazyAutowired
  private PatientPackageContentConsumedService patPkgContentConsumedService;

  /** The package order item service. */
  @LazyAutowired
  private PackageOrderItemService pkgOrderItemService;

  @LazyAutowired
  private PackagesRepository pkgRepo;
  
  /** The Discount plan service. */
  @LazyAutowired
  private DiscountPlanService discountPlanService;

  /**
   * Sets the tax calculators.
   *
   * @param calculators
   *          the new tax calculators
   */
  @Autowired
  public void setTaxCalculators(List<TaxCalculator> calculators) {
    for (TaxCalculator calculator : calculators) {
      if (calculator instanceof IssueTaxCalculator) {
        String[] supportedGroups = ((IssueTaxCalculator) calculator).getSupportedGroups();
        for (String group : supportedGroups) {
          this.calculators.put(group, calculator);
        }
      }
    }
  }

  /**
   * Gets the tax calculator.
   *
   * @param groupCode
   *          the group code
   * @return the tax calculator
   */
  public TaxCalculator getTaxCalculator(String groupCode) { // - will look something like below
    if (null == groupCode || null == calculators || calculators.isEmpty()) {
      return null;
    }
    return calculators.get(groupCode.trim().toUpperCase());
  }

  /**
   * This method is use to get orderkit items with batches.
   *
   * @param params
   *          the params
   * @return the order kit items
   */
  public Map<String, Object> getOrderKitItems(Map<String, Object> params) {

    BasicDynaBean genericPreferences = genericPreferencesService.getAllPreferences();
    Map<String, Object> orderKitDetailsMap = new HashMap<String, Object>();
    boolean includeZeroStock = !genericPreferences.get("stock_negative_sale").equals("D");
    int orderkitId = Integer.parseInt((String) params.get("order_kit_id"));
    int deptId = Integer.parseInt((String) params.get("storeId"));
    String planIdStr = (String) params.get("planId");
    String storeRatePlanIdStr = (String) params.get("storeRatePlanId");
    int planId = ((planIdStr != null && planIdStr.equals("undefined") && planIdStr.equals(""))
        ? Integer.parseInt((String) params.get("planId")) : -1);
    String visitId = (String) (params.get("visitId") != null ? params.get("visitId") : "-1");
    String ratePlanId = (String) (params.get("ratePlanId") != null ? params.get("ratePlanId")
        : "-1");
    int storeRatePlanId = ((storeRatePlanIdStr != null && !storeRatePlanIdStr.equals("undefined")
        && !storeRatePlanIdStr.equals("null") && !storeRatePlanIdStr.equals(""))
            ? Integer.parseInt(storeRatePlanIdStr) : -1);
    String visitType = ((String) params.get("visitType")).trim();
    String stockNegativeSale = (String) genericPreferences.get("stock_negative_sale");

    String issueTypeStr = (String) params.get("issueType");
    String[] issueTypes = null; // default: no filter
    if (issueTypeStr != null && !issueTypeStr.equals("")) {
      issueTypes = issueTypeStr.split("");
    }

    JSONSerializer js = new JSONSerializer().exclude("class");
    List<BasicDynaBean> orderKitItems = orderkitService.getOrderKitItemsDetails(orderkitId);

    List<BasicDynaBean> availMedicineList = new ArrayList<>();

    Map<Integer, String> orderKitItemsStatus = orderkitService.getOrderkitItemsStockStatus(deptId,
        orderkitId, issueTypes, includeZeroStock);

    Map<String, String> summarizedOrderKitItemsStatusMap = new LinkedHashMap<>();
    List<Integer> medicineList = new ArrayList<>();

    Iterator<BasicDynaBean> medicineListIterator = orderKitItems.iterator();

    int unavailableMedicineCount = 0;

    while (medicineListIterator.hasNext()) {
      BasicDynaBean medicineBean = medicineListIterator.next();
      Integer medicineId = (Integer) medicineBean.get("medicine_id");
      BigDecimal qtyNeeded = (BigDecimal) medicineBean.get("qty_needed");
      String medicineName = (String) medicineBean.get("medicine_name");
      if (orderKitItemsStatus.containsKey(medicineId)) {
        String medicineStockStatus = orderKitItemsStatus.get(medicineId);
        String[] medicineStatus = medicineStockStatus.split("@");
        Double inStockQty = Double.parseDouble(medicineStatus[0]);
        if (inStockQty > 0 || stockNegativeSale.equals("A") || stockNegativeSale.equals("W")) {
          availMedicineList.add(medicineBean);
        }
        if (inStockQty < qtyNeeded.doubleValue()) {
          unavailableMedicineCount++;
        }
        summarizedOrderKitItemsStatusMap.put(medicineName, orderKitItemsStatus.get(medicineId));
      } else {
        summarizedOrderKitItemsStatusMap.put(medicineName, "0@" + qtyNeeded);
        unavailableMedicineCount++;
      }
      medicineList.add(medicineId);

    }
    BasicDynaBean storeDetails = storeService.findByStore(deptId);
    Integer centerId = (Integer) storeDetails.get("center_id");
    String healthAuthority = centerService.getHealthAuthority(centerId);

    List<BasicDynaBean> nonIssuableItems = orderkitService.getNonIssuableItems(orderkitId);
    List<BasicDynaBean> stock = stockService.getOrderKitMedicineStockWithPatAmtsInDept(medicineList,
        deptId, planId, visitType, includeZeroStock, storeRatePlanId, healthAuthority);
    orderKitDetailsMap.put("medBatches",
        ConversionUtils.listBeanToMapListMap(stock, "medicine_id"));
    orderKitDetailsMap.put("order_kit_items_status", summarizedOrderKitItemsStatusMap);
    orderKitDetailsMap.put("order_kit_items",
        ConversionUtils.copyListDynaBeansToMap(availMedicineList));
    orderKitDetailsMap.put("total_items_status",
        unavailableMedicineCount + "@" + orderKitItems.size());
    orderKitDetailsMap.put("nonissuableitems",
        ConversionUtils.copyListDynaBeansToMap(nonIssuableItems));
    return orderKitDetailsMap;
  }

  /**
   * Gets the patient details.
   *
   * @param params
   *          the params
   * @return the patient details
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> getPatientDetails(Map<String, Object> params) {
    Map<String, Object> responseMap = new HashMap<String, Object>();
    String visitId = (String) params.get("visit_id");
    Integer storeId = Integer.parseInt((String) params.get("storeId"));
    String indentNo = (String) params.get("patient_indent_no");

    Map<String, Object> patientDetails = registrationService.getPatientVisitDetails(visitId);
    if (patientDetails.get("visit") != null) {
      Map<String, Object> visitDetails = (Map<String, Object>) patientDetails.get("visit");
      visitDetails.put("refdoctorname", registrationService.getVisitReferralDoctorName(visitId));
      List<BasicDynaBean> activeBills = billService.getVisitOpenBillsExcludingPackageBills(visitId);
      List<String> activeBillNos = new ArrayList<>();
      for (BasicDynaBean bill : activeBills) {
        String billNo = (String) bill.get("bill_no");
        activeBillNos.add(billNo);
      }
      List<BasicDynaBean> activePackages = null;
      if (!activeBillNos.isEmpty()) {
        activePackages = pkgService.getActivePackagesForBills(activeBillNos);
      }

      List<BasicDynaBean> multiBills = billService.getVisitOpenMvpBills(visitId);
      String mrNo = (String) visitDetails.get("mr_no");
      List<BasicDynaBean> multiPackages =  multiPkgService.getMultiVisitPackages(mrNo);
      visitDetails.put("bills", ConversionUtils
          .copyListDynaBeansToMap(activeBills));
      visitDetails.put("packages", ConversionUtils
          .copyListDynaBeansToMap(activePackages));
      visitDetails.put("multi_packages", ConversionUtils
          .copyListDynaBeansToMap(multiPackages));
      visitDetails.put("multi_package_bills", ConversionUtils
          .copyListDynaBeansToMap(multiBills));
      patientDetails.put("visit", visitDetails);

      int planId = 0;
      int storeRatePlanId = 0;

      if (visitDetails.get("store_rate_plan_id") != null) {
        storeRatePlanId = (int) visitDetails.get("store_rate_plan_id");
      }

      Map<String, Object> indentDetails = null;

      if (visitDetails.get("plan_id") != null) {
        planId = (Integer) visitDetails.get("plan_id");
      }
      indentDetails = getIndentDetails(params, patientDetails,
          visitDetails.get("patient_id").toString(), planId, storeRatePlanId, storeId, null,
          indentNo);

      if (indentDetails != null) {
        patientDetails.putAll(indentDetails);
      }
    }

    if (patientDetails.get("insurance") != null) {
      for (Map<String, Object> insurancePlan : (List<Map>) patientDetails.get("insurance")) {
        BasicDynaBean insurancePlanBean = insurancePlanService
            .findByKey((Integer) insurancePlan.get("plan_id"));
        insurancePlan.put("plan_exclusions", insurancePlanBean.get("plan_exclusions"));
        insurancePlan.put("plan_notes", insurancePlanBean.get("plan_notes"));
      }
    }

    responseMap.put("patient_details", patientDetails);
    return responseMap;
  }

  /**
   * Gets the indent details.
   *
   * @param params
   *          the params
   * @param patientDetails
   *          the patient details
   * @param visitId
   *          the visit id
   * @param planId
   *          the plan id
   * @param storeRatePlanId
   *          the store rate plan id
   * @param storeId
   *          the store id
   * @param processType
   *          the process type
   * @param patientIndentNo
   *          the patient indent no
   * @return the indent details
   */
  private Map<String, Object> getIndentDetails(Map<String, Object> params,
      Map<String, Object> patientDetails, String visitId, int planId, int storeRatePlanId,
      int storeId, String processType, String patientIndentNo) {

    Map<String, Object> indentDetails = new HashMap<>();

    List<BasicDynaBean> indentDetailsList = storePatientIndentService
        .getIndentDetailsForProcessOfIndentStore(visitId, "F", "I", storeId, patientIndentNo);
    int deptId = Integer.parseInt((String) params.get("storeId"));

    String visitType = (String) patientRegistrationService.findByKey("patient_id", visitId)
        .get("visit_type");

    BasicDynaBean store = storeService.findByStore(deptId);

    String healthAuthority = centerService.getHealthAuthority((Integer) store.get("center_id"));
    List<Integer> medicines = new ArrayList<>();

    for (BasicDynaBean indentDetail : indentDetailsList) {
      medicines.add((Integer) indentDetail.get("medicine_id"));
    }

    if (!indentDetailsList.isEmpty()) {
      List<BasicDynaBean> stock = stockService.getMedicineStockWithPatAmtsInDept(medicines, deptId,
          planId, visitType, true, storeRatePlanId, healthAuthority);
      HashMap medBatches = patientDetails.get("medBatches") == null ? new HashMap()
          : (HashMap) patientDetails.get("medBatches");
      medBatches.putAll(ConversionUtils.listBeanToMapListMap(stock, "medicine_id"));
      indentDetails.put("medBatches", medBatches);
    }

    indentDetails.put("indentsList", ConversionUtils.copyListDynaBeansToMap(
        storePatientIndentService.getIndentsForProcess(visitId, "I", storeId, patientIndentNo)));

    indentDetails.put("patIndentDetails",
        ConversionUtils.copyListDynaBeansToMap(indentDetailsList));

    return indentDetails;
  }

  /**
   * Gets the item tax details.
   *
   * @param paramMap
   *          the param map
   * @return the item tax details
   */
  public Map<String, Object> getItemTaxDetails(Map<String, Object> paramMap) {
    Map<String, Object> taxDetails = new HashMap<String, Object>();
    List<Map<Integer, Object>> taxSplitList = new ArrayList<Map<Integer, Object>>();
    String changeType = (String) paramMap.get("change_type");
    String[] itemSubGroupIds = null;
    if (paramMap.get("taxSubgroupIds") != null) {
      String itemSubgroupId = (String) paramMap.get("taxSubgroupIds");
      if (!itemSubgroupId.trim().isEmpty()) {
        if (itemSubgroupId.contains(",")) {
          itemSubGroupIds = itemSubgroupId.split("\\,");
        } else {
          itemSubGroupIds = new String[] { itemSubgroupId };
        }
        paramMap.put("item_subgroup_id", itemSubGroupIds);
        String[] medicineIds = new String[itemSubGroupIds.length];
        for (int i = 0; i < itemSubGroupIds.length; i++) {
          medicineIds[i] = paramMap.get("medicine_id") != null
              ? paramMap.get("medicine_id").toString() : null;
        }
        paramMap.put("medicine_id", medicineIds);
      }

    } else {
      paramMap.put("item_subgroup_id", new String[0]);
    }

    BasicDynaBean stockIssueDetailsBean = toDetailsBean(paramMap);
    stockIssueDetailsBean.set("amount", paramMap.get("rate"));
    stockIssueDetailsBean.set("discount", paramMap.get("discount_per"));
    BasicDynaBean billBean = null;
    BasicDynaBean patientBean = null;

    if (paramMap.get("bill_no") != null && !((String) paramMap.get("bill_no")).isEmpty()) {
      billBean = billService.findByKey((String) paramMap.get("bill_no"));
    }
    if (paramMap.get("mr_no") != null && !((String) paramMap.get("mr_no")).isEmpty()) {
      patientBean = patientDetailsService.findByKey((String) paramMap.get("mr_no"));
    }
    BasicDynaBean tpaBean = toTPABean(paramMap);

    if (null != stockIssueDetailsBean.get("medicine_id")) {

      List<BasicDynaBean> subGroupsBeans = new ArrayList<BasicDynaBean>();
      if (null != itemSubGroupIds && itemSubGroupIds.length > 0) {
        Integer[] sg = new Integer[itemSubGroupIds.length];
        for (int i = 0; i < itemSubGroupIds.length; i++) {
          sg[i] = Integer.valueOf(itemSubGroupIds[i]);
        }
        Map<String, Object[]> filter = new HashMap<String, Object[]>();
        filter.put("item_subgroup_id", sg);
        subGroupsBeans = itemSubGroupService.getSubGroups(filter);
      } else if (changeType.equals("B")) {
        String visitStoreRatePlanIdStr = (String) paramMap.get("visitStoreRatePlanId");
        if ((visitStoreRatePlanIdStr != null) && !visitStoreRatePlanIdStr.equals("")) {
          int visitStoreRatePlanId = Integer.parseInt(visitStoreRatePlanIdStr);
          subGroupsBeans = itemService.getSubgroups(
              (Integer) stockIssueDetailsBean.get("medicine_id"), visitStoreRatePlanId);
        }
        if (subGroupsBeans.isEmpty()) {
          BasicDynaBean storeBean = storeService.findByStore((int) paramMap.get("store_id"));
          Object storeRatePlanId = storeBean.get("store_rate_plan_id");
          if (null != storeRatePlanId) {
            subGroupsBeans = itemService.getSubgroups(
                (Integer) stockIssueDetailsBean.get("medicine_id"), (int) storeRatePlanId);
          }
        }
        if (subGroupsBeans.isEmpty()) {
          subGroupsBeans = itemService
              .getSubgroups((Integer) stockIssueDetailsBean.get("medicine_id"));
        }
      }

      ItemTaxDetails itemTaxDetails = getTaxBean(stockIssueDetailsBean);
      // Get center bean
      Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
      Integer centerId = (Integer) sessionAttributes.get("centerId");
      BasicDynaBean centerBean = centerService.findByKey(centerId);
      BasicDynaBean tpa = null;
      if (tpaBean.get("tpa_id") != null) {
        Map<String, Object> tpaMap = new HashMap<String, Object>();
        tpaMap.put("tpa_id", tpaBean.get("tpa_id"));
        tpa = tpaService.findByPk(tpaMap);
      }
      BasicDynaBean patBean = null;
      if (patientBean.get("mr_no") != null) {
        patBean = patientDetailsService.findByKey((String) patientBean.get("mr_no"));
      } else {
        patBean = patientBean;
      }
      BasicDynaBean visitBean = regService.findByKey((String)billBean.get("visit_id"));

      TaxContext taxContext = getTaxContext(stockIssueDetailsBean, subGroupsBeans, billBean,
          patBean, centerBean, tpa, visitBean);
      for (BasicDynaBean subGroup : subGroupsBeans) {
        TaxCalculator calculator = getTaxCalculator((String) subGroup.get("group_code"));
        if (null != calculator) {
          itemTaxDetails.setSugbroupId((Integer) subGroup.get("item_subgroup_id"));
          Map<Integer, Object> taxMap = calculator.calculateTaxes(itemTaxDetails, taxContext);
          taxSplitList.add(taxMap);
        }
      }

      taxDetails.put("medicine_id", stockIssueDetailsBean.get("medicine_id"));
      taxDetails.put("net_amount", null != itemTaxDetails.getNetAmount()
          ? itemTaxDetails.getNetAmount() : getNetAmount(itemTaxDetails));
      taxDetails.put("discount_amount", null != itemTaxDetails.getDiscount()
          ? itemTaxDetails.getDiscount() : getDiscountAmount(itemTaxDetails));
      if (null != itemTaxDetails.getOriginalTax()) {
        taxDetails.put("original_tax", itemTaxDetails.getOriginalTax());
      }
      taxDetails.put("tax_map", taxSplitList);
      taxDetails.put("tax_basis", "M");
    }
    return taxDetails;
  }

  /**
   * This method is used to calculate amounts for a list of items.
   *
   * @param reqMap
   *          the req map
   * @return the all amounts
   */
  public List<Map<String, Object>> getAllAmounts(Map<String, String[]> reqMap) {
    List<Map<String, Object>> responseList = new ArrayList<Map<String, Object>>();
    String visitId = reqMap.get("mrno") != null ? reqMap.get("mrno")[0] : null;
    String storeRatePlanId = reqMap.get("visitStoreRatePlanId") != null
        ? reqMap.get("visitStoreRatePlanId")[0] : null;
    String[] hdeleted = reqMap.get("hdeleted");
    String billNo = reqMap.get("bill_no") != null ? reqMap.get("bill_no")[0] : null;
    String storeStr = reqMap.get("store") != null ? reqMap.get("store")[0] : null;
    String[] chargeIds = reqMap.get("temp_charge_id");
    String[] medicineIds = reqMap.get("medicine_id");
    String[] itemBatchIds = reqMap.get("item_batch_id");
    String[] qtyVals = reqMap.get("issue_qty");
    String[] pkgSizes = reqMap.get("issue_base_unit");
    String[] issueTypes = reqMap.get("item_unit");
    String[] isTpa = reqMap.get("is_tpa");
    String[] mrNo = reqMap.get("mr_no_hid");
    String[] taxSubgroups = reqMap.get("tax_sub_group_ids");
    String[] mrp = reqMap.get("mrpHid");
    String[] discountPer = reqMap.get("discountHid");

    int index = 0;
    for (String chargeId : chargeIds) {
      Map<String, Object> amountsMap = new HashMap<String, Object>();
      BigDecimal rate = BigDecimal.ZERO;

      if (chargeId == null || chargeId.equals("") || hdeleted[index].equals("true")) {
        index++;
        continue;
      }
      Map<String, Object> paramMap = new HashMap<String, Object>();
      Map<String, Object> responseMap = new HashMap<String, Object>();

      if (visitId != null) {
        paramMap.put("visit_id", visitId);
      }
      if (mrNo != null) {
        paramMap.put("mr_no", mrNo[0]);
      }
      if (isTpa != null) {
        paramMap.put("tpa_id", isTpa[0]);
      }

      if (billNo != null) {
        paramMap.put("bill_no", billNo);
      }
      if (storeStr != null) {
        paramMap.put("store_id", Integer.parseInt(storeStr));
      }
      if (storeRatePlanId != null) {
        paramMap.put("visitStoreRatePlanId", storeRatePlanId);
      }
      if (medicineIds != null && medicineIds[index] != null) {
        paramMap.put("medicine_id", Integer.parseInt(medicineIds[index]));
      }
      if (itemBatchIds != null && itemBatchIds[index] != null) {
        paramMap.put("item_batch_id", Integer.parseInt(itemBatchIds[index]));
      }
      if (issueTypes != null && issueTypes[index] != null) {
        paramMap.put("issue_type", issueTypes[index]);
      }
      if (qtyVals != null && qtyVals[index] != null) {
        paramMap.put("qty", new BigDecimal(qtyVals[index]));
      }
      if (pkgSizes != null && pkgSizes[index] != null) {
        paramMap.put("pkg_size", new BigDecimal(pkgSizes[index]));
      }
      if (taxSubgroups != null && taxSubgroups[index] != null) {
        paramMap.put("taxSubgroupIds", taxSubgroups[index]);
      }

      paramMap.put("change_type", "Q");
      paramMap.put("rate", new BigDecimal(mrp[index]));
      paramMap.put("discount_per", new BigDecimal(discountPer[index]));
      paramMap.put("chargehead", ChargeDTO.CH_INVENTORY_ITEM);

      if (visitId != null && storeStr != null && medicineIds[index] != null
          && itemBatchIds[index] != null && issueTypes[index] != null && pkgSizes[index] != null
          && billNo != null) {
        rate = getItemAmounts(paramMap, amountsMap);
        paramMap.put("rate", rate);// Override with actual mrp.
        paramMap.put("discount_per", stockService.getItemDiscountAmounts(paramMap, amountsMap));
        responseMap.put("amount_details", amountsMap);
        responseMap.put("tax_details", getItemTaxDetails(paramMap));
        responseMap.put("temp_charge_id", chargeId);
        responseList.add(responseMap);
      }
      index++;
    }

    return responseList;
  }

  /**
   * Gets the item amounts.
   *
   * @param params
   *          the params
   * @return the item amounts
   */
  public Map<String, Object> getItemAmounts(Map<String, Object> params) {
    Map<String, Object> responseMap = new HashMap<String, Object>();
    Map<String, Object> amountsMap = new HashMap<String, Object>();
    String visitId = "";
    int storeId = -10;
    int medicineId = -1;
    int itemBatchId = -1;
    String issueType = "I";
    String changeType = "Q";
    BigDecimal discountPer = BigDecimal.ZERO;
    BigDecimal rate = BigDecimal.ZERO;
    BigDecimal qty = BigDecimal.ZERO;
    BigDecimal pkgSize = BigDecimal.ONE;
    String billNo = "";
    String taxSubgroupIds = "";
    String tpaId = "";
    String mrNo = "";

    Map<String, Object> paramMap = new HashMap<String, Object>();

    if (params.get("visit_id") != null) {
      visitId = (String) params.get("visit_id");
      paramMap.put("visit_id", visitId);
    }
    if (params.get("mr_no") != null) {
      mrNo = (String) params.get("mr_no");
      paramMap.put("mr_no", mrNo);
    }
    if (params.get("tpa_id") != null) {
      tpaId = (String) params.get("tpa_id");
      paramMap.put("tpa_id", tpaId);
    }
    if (params.get("bill_no") != null) {
      billNo = (String) params.get("bill_no");
      paramMap.put("bill_no", billNo);
    }
    if (params.get("store_id") != null) {
      storeId = Integer.parseInt((String) params.get("store_id"));
      paramMap.put("store_id", storeId);
    }
    if (params.get("medicine_id") != null) {
      medicineId = Integer.parseInt((String) params.get("medicine_id"));
      paramMap.put("medicine_id", medicineId);
    }
    if (params.get("item_batch_id") != null) {
      itemBatchId = Integer.parseInt((String) params.get("item_batch_id"));
      paramMap.put("item_batch_id", itemBatchId);
    }
    if (params.get("discount_per") != null) {
      discountPer = new BigDecimal((String) params.get("discount_per"));
    }
    if (params.get("rate") != null) {
      rate = new BigDecimal((String) params.get("rate"));
      paramMap.put("rate", rate);
    }
    if (params.get("issue_type") != null) {
      issueType = (String) params.get("issue_type");
      paramMap.put("issue_type", issueType);
    }
    if (params.get("visitStoreRatePlanId") != null) {
      paramMap.put("visitStoreRatePlanId", params.get("visitStoreRatePlanId"));
    }
    if (params.get("tax_sub_group_ids") != null) {
      taxSubgroupIds = (String) params.get("tax_sub_group_ids");
      paramMap.put("taxSubgroupIds", taxSubgroupIds);
    }
    if (params.get("qty") != null && !((String) params.get("qty")).isEmpty()) {
      qty = new BigDecimal((String) params.get("qty"));
      paramMap.put("qty", qty);
    }
    if (params.get("pkg_size") != null && !((String) params.get("pkg_size")).isEmpty()) {
      pkgSize = new BigDecimal((String) params.get("pkg_size"));
      paramMap.put("pkg_size", pkgSize);
    }
    if (params.get("change_type") != null && !((String) params.get("change_type")).isEmpty()) {
      changeType = (String) params.get("change_type");
      paramMap.put("change_type", changeType);
    }
    paramMap.put("discount_per", discountPer);
    paramMap.put("chargehead", ChargeDTO.CH_INVENTORY_ITEM);
    if (params.get("visit_id") != null && params.get("store_id") != null
        && params.get("medicine_id") != null && params.get("item_batch_id") != null
        && params.get("issue_type") != null && params.get("pkg_size") != null
        && params.get("bill_no") != null) {
      rate = getItemAmounts(paramMap, amountsMap);
      paramMap.put("rate", rate);// Override with actual mrp.
      paramMap.put("discount_per", stockService.getItemDiscountAmounts(paramMap, amountsMap));
      responseMap.put("amount_details", amountsMap);
      responseMap.put("tax_details", getItemTaxDetails(paramMap));
    }
    return responseMap;
  }

  /**
   * Gets the item amounts.
   *
   * @param paramMap
   *          the param map
   * @param amountsMap
   *          the amounts map
   * @return the item amounts
   */
  public BigDecimal getItemAmounts(Map<String, Object> paramMap, Map<String, Object> amountsMap) {
    BigDecimal sellingPrice = BigDecimal.ZERO;
    if (null != paramMap.get("visit_id") && null != paramMap.get("medicine_id")
        && null != paramMap.get("item_batch_id") && null != paramMap.get("qty")) {

      BigDecimal pkgSize = (BigDecimal) paramMap.get("pkg_size");
      BigDecimal discountPer = (BigDecimal) paramMap.get("discount_per");
      BigDecimal rate = (BigDecimal) paramMap.get("rate");
      BigDecimal qty = (BigDecimal) paramMap.get("qty");
      String changeType = (String) paramMap.get("change_type");
      int storeId = (Integer) paramMap.get("store_id");
      String billNo = (String) paramMap.get("bill_no");

      BasicDynaBean storeItemBatchBean = toItemBatchDetailsBean(paramMap);
      BasicDynaBean patientBean = toPatientRegistrationBean(paramMap);

      sellingPrice = getItemIssuePrice(patientBean, storeItemBatchBean, storeId, qty, discountPer,
          billNo);
      if (sellingPrice == null) {
        sellingPrice = BigDecimal.ZERO;
      }

      if (changeType != null && !changeType.isEmpty()
          && (changeType.equals("A") || changeType.equals("T"))) {
        amountsMap.put("mrp", rate);
        amountsMap.put("original_mrp", sellingPrice);
        amountsMap.put("original_unit_mrp", ConversionUtils.divide(sellingPrice, pkgSize));
        amountsMap.put("unit_mrp", ConversionUtils.divide(rate, pkgSize));
        sellingPrice = rate;
      } else {
        amountsMap.put("mrp", sellingPrice);
        amountsMap.put("original_mrp", sellingPrice);
        amountsMap.put("original_unit_mrp", ConversionUtils.divide(sellingPrice, pkgSize));
        amountsMap.put("unit_mrp", ConversionUtils.divide(sellingPrice, pkgSize));
      }

    }
    return sellingPrice;
  }

  /**
   * To item batch details bean.
   *
   * @param reqMap
   *          the req map
   * @return the basic dyna bean
   */
  public BasicDynaBean toItemBatchDetailsBean(Map<String, Object> reqMap) {
    BasicDynaBean storeItemBatchDetailsBean = storeItemBatchDetailsService
        .findByPk(Collections.singletonMap("item_batch_id", reqMap.get("item_batch_id")));
    ConversionUtils.copyToDynaBean(reqMap, storeItemBatchDetailsBean, null, true);
    return storeItemBatchDetailsBean;
  }

  /**
   * To patient registration bean.
   *
   * @param reqMap
   *          the req map
   * @return the basic dyna bean
   */
  public BasicDynaBean toPatientRegistrationBean(Map<String, Object> reqMap) {
    BasicDynaBean patientBean = registrationService
        .getPatientBeanWithBedTypeValue((String) reqMap.get("visit_id"));
    ConversionUtils.copyToDynaBean(reqMap, patientBean, null, true);
    return patientBean;
  }

  /**
   * Gets the item issue price.
   *
   * @param patientBean
   *          the patient bean
   * @param storeItemBatchDetailsBean
   *          the store item batch details bean
   * @param storeId
   *          the store id
   * @param qty
   *          the qty
   * @param discount
   *          the discount
   * @param billNo
   *          the bill no
   * @return the item issue price
   */
  @SuppressWarnings("unchecked")
  public BigDecimal getItemIssuePrice(BasicDynaBean patientBean,
      BasicDynaBean storeItemBatchDetailsBean, Integer storeId, BigDecimal qty, BigDecimal discount,
      String billNo) {

    BigDecimal issueRate = BigDecimal.ZERO;
    BigDecimal expressionMrp = null;
    BigDecimal batchMrp = null;
    String expression = null;
    Integer medicineId = null;
    Integer itemBatchId = null;
    if (storeItemBatchDetailsBean != null) {
      medicineId = (Integer) storeItemBatchDetailsBean.get("medicine_id");
      itemBatchId = (Integer) storeItemBatchDetailsBean.get("item_batch_id");
      batchMrp = storeItemBatchDetailsBean.get("mrp") != null
          ? (BigDecimal) storeItemBatchDetailsBean.get("mrp") : BigDecimal.ZERO;
      issueRate = batchMrp;
    }
    BasicDynaBean storeBean = storeService.findByPk(Collections.singletonMap("dept_id", storeId));
    int centerId = (Integer) storeBean.get("center_id");
    Map<String, Object> results = new HashMap<String, Object>();

    BasicDynaBean itemExpressionBean = stockRepository.getSellingPriceExpr(medicineId, itemBatchId,
        billNo, storeId);

    if (itemExpressionBean != null) {
      expressionMrp = itemExpressionBean.get("mrp") != null
          ? (BigDecimal) itemExpressionBean.get("mrp") : BigDecimal.ZERO;

      if (itemExpressionBean.get("issue_rate_master_expr") != null) {
        expression = (String) itemExpressionBean.get("issue_rate_master_expr");

      } else if (itemExpressionBean.get("visit_rate_plan_expr") != null) {
        expression = (String) itemExpressionBean.get("visit_rate_plan_expr");

      } else if (itemExpressionBean.get("use_batch_mrp") != null
          && itemExpressionBean.get("use_batch_mrp").equals("Y")
          && itemExpressionBean.get("mrp") != null) {
        expression = expressionMrp.toString();

      } else if (itemExpressionBean.get("store_rate_plan_expr") != null) {
        expression = (String) itemExpressionBean.get("store_rate_plan_expr");

      } else if (itemExpressionBean.get("item_selling_price") != null) {
        expression = (String) itemExpressionBean.get("item_selling_price");

      } else {
        expression = expressionMrp.toString();
      }

      BasicDynaBean itemBean = storeItemDetailsRepository
          .findByPk(Collections.singletonMap("medicine_id", medicineId));

      if (itemBean != null && itemBean.get("item_selling_price") != null) {
        results.put("mrp", (BigDecimal) itemBean.get("item_selling_price"));
        results.put("item_selling_price", (BigDecimal) itemBean.get("item_selling_price"));

      } else {
        results.put("mrp", batchMrp);
      }
      results.put("bed_type", (String) patientBean.get("bed_type"));
      results.put("discount", discount);
      results.put("center_id", centerId);
      results.put("store_id", storeId);

      Map packageCpDetails = getPackageCPDetails(storeId, itemBatchId, qty);
      if (packageCpDetails != null) {
        results.putAll(packageCpDetails);
      }
    }

    if (!StringUtils.isEmpty(expression)) {
      String expressionValueStr = ftlProcessor.process(expression, results);
      if (!StringUtils.isEmpty(expressionValueStr)) {
        issueRate = new BigDecimal(expressionValueStr);
      }
    }
    return issueRate;
  }

  /**
   * Gets the package CP details.
   *
   * @param storeId
   *          the store id
   * @param itemBatchId
   *          the item batch id
   * @param quantity
   *          the quantity
   * @return the package CP details
   */
  public Map getPackageCPDetails(Integer storeId, Integer itemBatchId, BigDecimal quantity) {
    BasicDynaBean genericPreferences = genericPreferencesService.getAllPreferences();
    BigDecimal remainingQty = quantity;
    List<BasicDynaBean> itemStock = stockService.getBatchSortedLotDetails(storeId, itemBatchId);
    Map results = null;
    BigDecimal maxPackageCP = BigDecimal.ZERO;
    BigDecimal packageCP = BigDecimal.ZERO;
    BigDecimal transactionQty = BigDecimal.ZERO;
    int medicineId = 0;

    if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
      return null;
    }

    for (int i = 0; i < itemStock.size(); i++) {
      BasicDynaBean stockDetails = itemStock.get(i);
      if (remainingQty.compareTo(BigDecimal.ZERO) <= 0) {
        break;
      }
      results = null;

      BigDecimal stockQty = (BigDecimal) stockDetails.get("qty");
      BigDecimal lotPackageCP = (BigDecimal) stockDetails.get("package_cp");
      if (stockQty.longValue() > 0) {
        packageCP = new BigDecimal(Math.max(packageCP.doubleValue(), lotPackageCP.doubleValue()),
            MathContext.DECIMAL64);

        if (stockQty.compareTo(remainingQty) > 0) {
          // no problem -- use up all what we need.
          transactionQty = remainingQty;
        } else {
          transactionQty = stockQty;
        }
        if (transactionQty.compareTo(BigDecimal.ZERO) <= 0) {
          // no luck in this lot. Try next one.
          continue;
        }

        remainingQty = remainingQty.subtract(transactionQty);
        medicineId = (Integer) stockDetails.get("medicine_id");
        results = new HashMap(stockDetails.getMap());
      }
    }

    String stockNegativeSale = (String) genericPreferences.get("stock_negative_sale");
    if (medicineId == 0
        && (stockNegativeSale.equalsIgnoreCase("A") || stockNegativeSale.equalsIgnoreCase("W"))) {
      if (itemStock.size() > 0) {
        BasicDynaBean stockDetails = itemStock.get(itemStock.size() - 1);
        BigDecimal lotPackageCP = (BigDecimal) stockDetails.get("package_cp");
        packageCP = new BigDecimal(Math.max(packageCP.doubleValue(), lotPackageCP.doubleValue()),
            MathContext.DECIMAL64);
        medicineId = (Integer) stockDetails.get("medicine_id");
        results = new HashMap(stockDetails.getMap());
      }
    }

    if (results != null) {
      BasicDynaBean maxitemStock = stockService.getCPDetails(storeId, itemBatchId);
      maxPackageCP = maxitemStock != null && maxitemStock.get("max_package_cp") != null
          ? (BigDecimal) maxitemStock.get("max_package_cp") : BigDecimal.ZERO;

      if (packageCP.doubleValue() <= 0) {
        packageCP = maxPackageCP;
      }
      results.put("package_cp", packageCP);
      results.put("max_cp", maxPackageCP);
    }

    return results;
  }

  /**
   * This is dummy method added for UI changes. Improve it once taxation for backend is implemented.
   *
   * @return the supported tax groups
   */
  public Map getSupportedTaxGroups() {
    String[] groupCodes = null;
    Map groupMap = new HashMap();
    Map subGroupMap = new HashMap();
    Map result = new HashMap();
    if (null != calculators && calculators.size() > 0) {
      groupCodes = calculators.keySet().toArray(new String[0]);
      if (null != groupCodes) {
        List<BasicDynaBean> groups = itemGroupService.findByGroupCodes(groupCodes);
        groupMap = ConversionUtils.listBeanToMapMap(groups, "item_group_id");
        for (BasicDynaBean group : groups) {
          Integer groupId = (Integer) group.get("item_group_id");
          Map map = new HashMap();
          map.put("item_group_id", groupId);
          List<BasicDynaBean> sgBeans = itemSubGroupService.findByCriteria(map);
          Map subgroups = (null != sgBeans && sgBeans.size() > 0)
              ? ConversionUtils.listBeanToMapListMap(sgBeans, "item_group_id")
              : emptyMap(groupId);
          subGroupMap.putAll(subgroups);
        }
      }
    }
    result.put("item_groups", groupMap);
    result.put("item_subgroups", subGroupMap);
    return result;
  }

  /**
   * Empty map.
   *
   * @param itemGroupId
   *          the item group id
   * @return the map
   */
  private Map emptyMap(Integer itemGroupId) {
    Map map = new HashMap();
    map.put(itemGroupId, Collections.EMPTY_MAP);
    return map;
  }

  /**
   * Gets the tax context.
   *
   * @param salesDetails
   *          the sales details
   * @param subGroups
   *          the sub groups
   * @param billBean
   *          the bill bean
   * @param patientBean
   *          the patient bean
   * @param centerBean
   *          the center bean
   * @param tpaBean
   *          the tpa bean
   * @return the tax context
   */
  public TaxContext getTaxContext(BasicDynaBean salesDetails, List<BasicDynaBean> subGroups,
      BasicDynaBean billBean, BasicDynaBean patientBean, BasicDynaBean centerBean,
      BasicDynaBean tpaBean, BasicDynaBean visitBean) {
    TaxContext context = new TaxContext();
    context.setItemBean(salesDetails);
    context.setSubgroups(subGroups);
    context.setBillBean(billBean);
    context.setCenterBean(centerBean);
    context.setPatientBean(patientBean);
    context.setVisitBean(visitBean);
    context.setTpaBean(tpaBean);
    return context;
  }

  /**
   * Gets the net amount.
   *
   * @param itemTaxDetails
   *          the item tax details
   * @return the net amount
   */
  private BigDecimal getNetAmount(ItemTaxDetails itemTaxDetails) {
    BigDecimal netAmt = BigDecimal.ZERO;
    netAmt = ConversionUtils.setScale(itemTaxDetails.getMrp().multiply((ConversionUtils
        .divideHighPrecision(itemTaxDetails.getQty(), itemTaxDetails.getPkgSize()))));
    return netAmt;
  }

  /**
   * Gets the discount amount.
   *
   * @param itemTaxDetails
   *          the item tax details
   * @return the discount amount
   */
  private BigDecimal getDiscountAmount(ItemTaxDetails itemTaxDetails) {
    BigDecimal disountAmt = BigDecimal.ZERO;
    disountAmt = ConversionUtils.setScale(itemTaxDetails.getMrp()
        .multiply((ConversionUtils.divideHighPrecision(itemTaxDetails.getQty(),
            itemTaxDetails.getPkgSize())))
        .multiply(itemTaxDetails.getDiscountPercent()).divide(BigDecimal.valueOf(100)));
    return disountAmt;
  }

  /**
   * Gets the tax bean.
   *
   * @param paramMap
   *          the param map
   * @return the tax bean
   */
  protected ItemTaxDetails getTaxBean(Map<String, Object> paramMap) {
    BigDecimal mrp = BigDecimal.ZERO;
    BigDecimal qty = BigDecimal.ZERO;
    BigDecimal discount = BigDecimal.ZERO;

    if (paramMap.get("qty") != null) {
      qty = (BigDecimal) paramMap.get("qty");
    }
    if (paramMap.get("rate") != null) {
      mrp = (BigDecimal) paramMap.get("rate");
    }
    BigDecimal pkgSize = BigDecimal.ZERO;
    if (paramMap.get("pkg_size") != null) {
      pkgSize = (BigDecimal) paramMap.get("pkg_size");
    }
    if (paramMap.get("discount_per") != null) {
      discount = (BigDecimal) paramMap.get("discount_per");
    }

    BigDecimal costPrice = BigDecimal.ZERO;
    BigDecimal adjMrp = BigDecimal.ZERO;
    BigDecimal bonusQty = BigDecimal.ZERO;
    String taxBasis = "M";
    String discountType = "I";
    ItemTaxDetails itemTaxDetails = new ItemTaxDetails();
    itemTaxDetails.setCostPrice(costPrice);
    itemTaxDetails.setMrp(mrp);
    itemTaxDetails.setQty(qty);
    itemTaxDetails.setBonusQty(bonusQty);
    itemTaxDetails.setPkgSize(pkgSize);
    itemTaxDetails.setDiscountPercent(discount);
    itemTaxDetails.setTaxBasis(taxBasis);
    itemTaxDetails.setAdjMrp(adjMrp);
    itemTaxDetails.setItemId(paramMap.get("medicine_id"));
    itemTaxDetails.setDiscountType(discountType);

    return itemTaxDetails;
  }

  /**
   * This method is used to set the tax details and return it as a bean.
   *
   * @param stockIssueDetailsBean
   *          the stock issue details bean
   * @return the tax bean
   */
  protected ItemTaxDetails getTaxBean(BasicDynaBean stockIssueDetailsBean) {
    BigDecimal mrp = BigDecimal.ZERO;
    BigDecimal qty = BigDecimal.ZERO;
    BigDecimal pkgSize = BigDecimal.ZERO;
    BigDecimal discount = BigDecimal.ZERO;

    if (stockIssueDetailsBean.get("qty") != null) {
      qty = (BigDecimal) stockIssueDetailsBean.get("qty");
    }
    if (stockIssueDetailsBean.get("amount") != null) {
      mrp = (BigDecimal) stockIssueDetailsBean.get("amount");
    }
    if (stockIssueDetailsBean.get("pkg_size") != null) {
      pkgSize = (BigDecimal) stockIssueDetailsBean.get("pkg_size");
    }
    if (stockIssueDetailsBean.get("discount") != null) {
      discount = (BigDecimal) stockIssueDetailsBean.get("discount");
    }

    String taxBasis = "M";
    String discountType = "I";
    BigDecimal adjMrp = BigDecimal.ZERO;
    BigDecimal bonusQty = BigDecimal.ZERO;
    BigDecimal costPrice = BigDecimal.ZERO;
    ItemTaxDetails itemTaxDetails = new ItemTaxDetails();
    itemTaxDetails.setCostPrice(costPrice);
    itemTaxDetails.setMrp(mrp);
    itemTaxDetails.setQty(qty);
    itemTaxDetails.setBonusQty(bonusQty);
    itemTaxDetails.setPkgSize(pkgSize);
    itemTaxDetails.setDiscountPercent(discount);
    itemTaxDetails.setTaxBasis(taxBasis);
    itemTaxDetails.setAdjMrp(adjMrp);
    itemTaxDetails.setItemId(stockIssueDetailsBean.get("medicine_id"));
    itemTaxDetails.setDiscountType(discountType);

    return itemTaxDetails;
  }

  /**
   * To details bean.
   *
   * @param paramMap
   *          the param map
   * @return the basic dyna bean
   */
  public BasicDynaBean toDetailsBean(Map paramMap) {
    BasicDynaBean stockIssueDetailsBean = stockIssueDetailsRepository.getBean();
    ConversionUtils.copyToDynaBean(paramMap, stockIssueDetailsBean, null, true);
    return stockIssueDetailsBean;
  }

  /**
   * To bill bean.
   *
   * @param paramMap
   *          the param map
   * @return the basic dyna bean
   */
  public BasicDynaBean toBillBean(Map paramMap) {
    BasicDynaBean billBean = billService.getBean();
    ConversionUtils.copyToDynaBean(paramMap, billBean, null, true);
    return billBean;
  }

  /**
   * To patient bean.
   *
   * @param paramMap
   *          the param map
   * @return the basic dyna bean
   */
  public BasicDynaBean toPatientBean(Map paramMap) {
    BasicDynaBean patientBean = patientDetailsService.getBean();
    ConversionUtils.copyToDynaBean(paramMap, patientBean, null, true);
    return patientBean;
  }

  /**
   * To TPA bean.
   *
   * @param paramMap
   *          the param map
   * @return the basic dyna bean
   */
  public BasicDynaBean toTPABean(Map paramMap) {
    BasicDynaBean tpaBean = tpaService.getBean();
    ConversionUtils.copyToDynaBean(paramMap, tpaBean, null, true);
    return tpaBean;
  }

  /**
   * Update issue claim details.
   *
   * @param visitId
   *          the visit id
   * @param chargeBean
   *          the charge bean
   * @param billNo
   *          the bill no
   * @return true, if successful
   */
  private boolean updateIssueClaimDetails(String visitId, BasicDynaBean chargeBean, 
        BasicDynaBean billBean) {
    List<BasicDynaBean> planList = patientInsurancePlansService.getPlanDetails(visitId);
    boolean isInsuranceBill = false;
    if (null != billBean) {
      isInsuranceBill = (Boolean) billBean.get("is_tpa");
    }

    int[] planIds = new int[planList.size()];
    for (int j = 0; j < planList.size(); j++) {
      planIds[j] = (Integer) planList.get(j).get("plan_id");
    }
    if (planIds.length > 0 && isInsuranceBill) {
      return billChargeClaimService.insertBillChargeClaims(
          Arrays.asList(new BasicDynaBean[] { chargeBean }), planIds, visitId, billBean, null,
          null);
    }

    return true;

  }

  /**
   * Gets the insurance category payable status.
   *
   * @param params
   *          the params
   * @return the insurance category payable status
   */
  public Map<String, Object> getInsuranceCategoryPayableStatus(Map<String, String[]> params) {
    int medicineId = params.get("medicineId") != null && params.get("medicineId").length > 0
        ? Integer.parseInt((String) params.get("medicineId")[0]) : 0;
    String visitId = (String) (params.get("visitId") != null && params.get("visitId").length > 0
        ? params.get("visitId")[0] : "");
    String visitType = (String) (params.get("visitType") != null
        && params.get("visitType").length > 0 ? params.get("visitType")[0] : "o");

    Integer pkgId = params.get("packageId") != null && params.get("packageId").length > 0
        ? Integer.parseInt((String) params.get("packageId")[0]) : 0;

    int insuranceCategoryId;

    if (pkgId > 0) {
      insuranceCategoryId = (int) pkgRepo.findByKey("package_id", pkgId)
          .get("insurance_category_id");
    } else {
      insuranceCategoryId = (int) storeItemDetailsRepository.findByKey("medicine_id", medicineId)
        .get("insurance_category_id");
    }

    BasicDynaBean claimableStatus = patientInsurancePlansService
        .getInsuranceCategoryPayableStatus(visitId, insuranceCategoryId, visitType);
    return claimableStatus.getMap();
  }

  /**
   * Save patient issue details.
   *
   * @param req
   *          the req
   * @return the map
   * @throws SQLException exception
   */
  // TODO reduce cognitive complexity of 311 (O _ O). Allowed is 15.
  @Transactional
  public Map<String, Object> savePatientIssueDetails(HttpServletRequest req,
      List<Map<String, Object>> cacheIssueTxns) throws SQLException {
    Map<String, Object> responseMap = new HashMap<>();

    BasicDynaBean userdynaBean = null;
    BasicDynaBean usermaindynaBean = null;
    BasicDynaBean invmaindynaBean = null;
    BasicDynaBean pkgDetails = null;
    String wardNo = null;
    boolean result = true;
    int itemissueid = 0;
    int gtpassId = 0;
    String msg = "";
    String patOrgID = "";
    BasicDynaBean ratePlanBean = null;
    BigDecimal ratePlanDiscount = BigDecimal.ZERO;
    String pharmacyBasis = "mrp";
    String ratePlanDiscType = "";

    String[] mrno = null;
    String[] hospUser = null;

    String issueDate = req.getParameter("issueDate");
    String issueTime = req.getParameter("issueTime");

    Map<String, String[]> itemsMap = req.getParameterMap();
    String[] itemNames = (String[]) itemsMap.get("item_name");

    for (int i = 0; i < itemNames.length - 1; i++) {
      try {
        itemNames[i] = URLDecoder.decode(itemNames[i], "UTF-8");
      } catch (UnsupportedEncodingException ex) {
        logger.debug("", ex);
      }
    }

    String[] itemIdentifiers = (String[]) itemsMap.get("item_identifier");
    String[] origUnitMrp = (String[]) itemsMap.get("origUnitMrpHid");
    String[] unitMrp = (String[]) itemsMap.get("unit_mrp");
    String[] discountAmt = (String[]) itemsMap.get("discountAmtHid");
    String[] amt = (String[]) itemsMap.get("amt");
    String[] insCategory = (String[]) itemsMap.get("insurancecategory");
    String[] billingGroup = (String[]) itemsMap.get("billinggroup");
    String[] patPkgContentId = (String[]) itemsMap.get("pat_pkg_content_id");
    String[] pkgObjId = (String[]) itemsMap.get("pack_ob_id");
    String[] itemBatchId = (String[]) itemsMap.get("item_batch_id");
    String[] pkgmrp = (String[]) itemsMap.get("pkg_mrp");
    String[] issueType = (String[]) itemsMap.get("issueType");
    String[] taxAmt = (String[]) itemsMap.get("tax_amt");
    String[] originalTaxAmt = (String[]) itemsMap.get("original_tax");

    String[] editedRate = (String[]) itemsMap.get("mrpHid");
    String[] userEntereddiscountRate = (String[]) itemsMap.get("discountHid");
    String[] priInsClaimAmts = (String[]) itemsMap.get("pri_ins_amt");
    String[] secInsClaimAmts = (String[]) itemsMap.get("sec_ins_amt");
    String[][] claimAmts = new String[priInsClaimAmts.length][secInsClaimAmts.length];
    String[] billableItems = (String[]) itemsMap.get("item_billable_hidden");
    String[] medicineIds = (String[]) itemsMap.get("medicine_id");

    /** Check if this is being called from Patient Issue or Stock User Issue */
    mrno = (String[]) itemsMap.get("mrno");
    for (int i = 0; i < priInsClaimAmts.length; i++) {
      if (priInsClaimAmts[i] != null && !priInsClaimAmts[i].trim().isEmpty()) {
        claimAmts[i][0] = priInsClaimAmts[i];
      }
      if (secInsClaimAmts[i] != null && !secInsClaimAmts[i].trim().isEmpty()) {
        claimAmts[i][1] = secInsClaimAmts[i];
      }
    }

    String[] issueReason = (String[]) itemsMap.get("reason");
    String[] itemCheck = (String[]) itemsMap.get("hdeleted");
    BasicDynaBean genericPreferences = genericPreferencesService.getAllPreferences();
    String saleType = (String) genericPreferences.get("stock_negative_sale");
    boolean allowNegative = !saleType.equals("D");
    String[] itemUnit = (String[]) itemsMap.get("item_unit");
    String[] pkgSize = (String[]) itemsMap.get("pkg_unit");
    String[] disStatus = (String[]) itemsMap.get("dispense_status");
    String packageId = itemsMap.get("package_id") != null
        ? itemsMap.get("package_id")[0] : null;
    String patPackageId = itemsMap.get("activePackage") != null
        ? itemsMap.get("activePackage")[0] : null;
    Integer pkgId = 0;
    Integer patPkgId = 0;
    String patientMrNo = null;
    String pkgChargeIdRef = itemsMap.get("pkg_charge_id_ref") != null
        ? itemsMap.get("pkg_charge_id_ref")[0] : null;
    List<BasicDynaBean> groupList = itemGroupService.getTaxItemGroups();
    StoresHelper storeHelper = new StoresHelper();
    ArrayList<Map<String, Object>> taxDetailsList = new ArrayList<Map<String, Object>>();
    Integer centerId = -1;
    if (StringUtils.isNotBlank(packageId) && NumberUtils.isParsable(packageId)) {
      pkgId = Integer.parseInt(packageId);
    }
    if (StringUtils.isNotBlank(patPackageId) && NumberUtils.isParsable(patPackageId)) {
      patPkgId = Integer.parseInt(patPackageId);
    }
    Boolean isMultiPkg = false;
    String pkgName = "";
    String submissionBatchType = null;
    if (pkgId > 0) {
      BasicDynaBean pkgBean = pkgService.getPackageById(pkgId);
      isMultiPkg = (Boolean) pkgBean.get("multi_visit_package");
      pkgName = (String) pkgBean.get("package_name");
      submissionBatchType = (String) pkgBean.get("submission_batch_type");
    }

    GenericRepository modules = new GenericRepository("modules_activated");
    BasicDynaBean module = modules.findByKey("module_id", "mod_scm");
    Map<String, String> indentDisStatusMap = new HashMap<String, String>();
    String gt = req.getParameter("gatepass");
    String[] stype = (String[]) itemsMap.get("stype");
    String[] patientIndentNoRef = (String[]) itemsMap.get("patient_indent_no_ref");
    if (patientIndentNoRef != null) {
      // possible in case of user issue
      for (int i = 0; i < patientIndentNoRef.length; i++) {
        if (patientIndentNoRef[i].isEmpty()) {
          continue;
        }
        indentDisStatusMap.put(patientIndentNoRef[i], disStatus[i]);
      }
    }

    boolean gatepass = false;
    if (gt != null) {
      gatepass = true;
    }

    boolean conInsertion = false;
    for (int i = 0; i < stype.length - 1; i++) {
      if (stype[i].equalsIgnoreCase("true")) {
        conInsertion = true;
        break;
      }

    }

    String[] issueQty = (String[]) itemsMap.get("issue_qty");
    BigDecimal[] issueQtyconv = new BigDecimal[issueQty.length - 1];
    String kitIdentifier = null;
    BigDecimal mrp = new BigDecimal(0);
    BigDecimal vat = new BigDecimal(0);
    String chargeId = null;
    Boolean billable = false;
    String categoryId = null;

    String groupName = BillChargeService.CG_INVENTORY;
    String headName = BillChargeService.CH_INVENTORY_ITEM;
    String patientId = null;
    String bedType = null;

    boolean isnegative = true;
    String isShared = req.getParameter("isSharedLogIn");
    HttpSession session = req.getSession(false);
    String userName = isShared == null ? (String) session.getAttribute("userid")
        : isShared.equals("Y") ? req.getParameter("authUser")
            : (String) session.getAttribute("userid");

    if (mrno != null) {
      // The patient details tab now returns patient id instead of mrno
      patientId = mrno[0];
    }
    
    BasicDynaBean bedBean = null;
    BasicDynaBean patBean = patientRegistrationService.findByKey("patient_id", patientId);
    int bedId = 0;
    String patType = null;
    if (patBean != null) {
      patType = (String) patBean.get("visit_type");
    }

    if (patType != null && patType.equals("i")) {
      bedBean = admissionRepository.findByKey("patient_id", patientId);
    }

    if (bedBean != null) {
      bedId = (Integer) bedBean.get("bed_id");
    }

    BasicDynaBean wardBean = bedNamesRepository.findByKey("bed_id", bedId);

    if (wardBean != null) {
      wardNo = (String) wardBean.get("ward_no");
    }
    if (patBean != null) {
      bedType = patBean.get("bed_type").toString();
      patOrgID = patBean.get("org_id").toString();
      ratePlanBean = organizationRepository.findByKey("org_id", patOrgID);
      ratePlanDiscount = (BigDecimal) ratePlanBean.get("pharmacy_discount_percentage");
      ratePlanDiscType = (String) ratePlanBean.get("pharmacy_discount_type");
    }

    Map<String, Object> storeParams = new HashMap<>();
    String[] stores = (String[]) itemsMap.get("store");
    storeParams.put("dept_id", Integer.parseInt(stores[0]));
    storeParams.put("status", "A");
    BasicDynaBean storeBean = storeService.findByPk(storeParams);
    Integer storeRatePlanId = (storeBean != null && storeBean.get("store_rate_plan_id") == null ? 0
        : (Integer) storeBean.get("store_rate_plan_id"));
    Integer visitRtoreRatePlanId = (ratePlanBean == null
        || ratePlanBean.get("store_rate_plan_id") == null ? 0
            : (Integer) ratePlanBean.get("store_rate_plan_id"));
    // setting revenue a/c group a/c to store wise preferences
    BasicDynaBean store = storeRepository.findByKey("dept_id",
            Integer.parseInt(stores[0]));
    if (storeBean != null) {
      centerId = (Integer) store.get("center_id");
    }
    String healthAuthority = (String) centerService.findByKey(centerId)
            .get("health_authority");
    BasicDynaBean healthAuthorityPreferences = healthAuthorityPreferencesService
            .findByKey(healthAuthority);
    String[] drugCodeTypes = ((String) healthAuthorityPreferences.get("drug_code_type"))
            .split(",");

    String[] billNos = (String[]) itemsMap.get("bill_no");
    BasicDynaBean billBean = null;
    if (billNos != null && !billNos.equals("") && !billNos[0].isEmpty() 
          && !billNos[0].equalsIgnoreCase("C")) {
      String billNo = billNos[0];
      billBean = billRepository.findByKey("bill_no", billNo);
    }
    for (int i = 0; i < issueQty.length - 1; i++) {
      issueQtyconv[i] = ConversionUtils.setScale(new BigDecimal(issueQty[i]));
    }
    String userType = "Patient";
    String issuedTo = mrno[0];
    int newIssueId = stockIssueMainRepository.getNextSequence();
    int inserted = 0;

    BigDecimal totalAmount = BigDecimal.ZERO;
    BigDecimal totalDisc = BigDecimal.ZERO;
    BigDecimal totalTax = BigDecimal.ZERO;

    int itemSize = itemNames.length - 1;
    for (int i = 0; i < itemSize; i++) {
      itemissueid = stockIssueDetailsRepository.getNextSequence();
      boolean deletedrow = false;
      if (itemCheck != null) {
        if (itemCheck[i].equals("true")) {
          deletedrow = true;
        } else {
          deletedrow = false;
        }
      }
      if (!deletedrow) {
        Integer medicineId =  Integer.parseInt(medicineIds[i]);
        if (saleType.equalsIgnoreCase("D")) {
          boolean qtyAvailable = storeStockDetailsService.isQuantityAvailable(
              Integer.parseInt(stores[0]), medicineId, Integer.parseInt(itemBatchId[i]),
              issueQtyconv[i]);

          if (!qtyAvailable) {
            throw new HMSException("exception.inventory.issues.stock.not.available",
                new String[] { itemNames[i] });
          }
        }
        String visitType = "";
        if (billableItems[i] != null || !billableItems[i].isEmpty()) {
          billable = Boolean.parseBoolean(billableItems[i]);
        } else {
          BasicDynaBean item = storeStockDetailsService.getItemDetails(visitRtoreRatePlanId,
              storeRatePlanId, itemIdentifiers[i], medicineId);
          billable = (Boolean) item.get("billable");
        }
        pkgDetails = storeStockDetailsService.getPackageMrpAndCP(medicineId, itemIdentifiers[i]);

        BigDecimal rate = null;
        BigDecimal origRate = null;
        BigDecimal discount = null;
        BigDecimal amount = null;
        BigDecimal claim = null;
        int insuranceCategoryId = 0;
        int billingGroupId = -1;

        if (billable != null && billable && !billNos[0].equalsIgnoreCase("C")
            && !billNos[0].isEmpty()) {
          rate = ConversionUtils.setScale(new BigDecimal(unitMrp[i]));
          origRate = ConversionUtils.setScale(new BigDecimal(origUnitMrp[i]));
          discount = ConversionUtils.setScale(new BigDecimal(discountAmt[i]));
          amount = ConversionUtils.setScale(new BigDecimal(amt[i]));
          claim = ConversionUtils.setScale(new BigDecimal(priInsClaimAmts[i]));
          insuranceCategoryId = Integer.parseInt(insCategory[i]);

        }

        // set bean of stock_issue_details table
        userdynaBean = stockIssueDetailsRepository.getBean();
        userdynaBean.set("user_issue_no", new BigDecimal(newIssueId));
        userdynaBean.set("medicine_id", medicineId);
        userdynaBean.set("batch_no", itemIdentifiers[i]);
        userdynaBean.set("item_batch_id", Integer.parseInt(itemBatchId[i]));
        userdynaBean.set("qty", issueQtyconv[i]);
        userdynaBean.set("return_qty", new BigDecimal(0));
        userdynaBean.set("vat", vat);
        userdynaBean.set("item_issue_no", itemissueid);
        userdynaBean.set("amount", amount);
        // check here
        userdynaBean.set("discount", discount);
        userdynaBean.set("pkg_size", pkgDetails.get("issue_base_unit"));
        userdynaBean.set("pkg_cp", pkgDetails.get("package_cp"));
        userdynaBean.set("pkg_mrp", ((pkgmrp != null && pkgmrp[i] != null && !pkgmrp[i].isEmpty())
            ? new BigDecimal(pkgmrp[i]) : BigDecimal.ZERO));
        userdynaBean.set("item_unit", itemUnit[i]);
        userdynaBean.set("issue_pkg_size", (pkgSize[i] != null && !pkgSize[i].equals(""))
            ? ConversionUtils.setScale(new BigDecimal(pkgSize[i])) : BigDecimal.ONE);

        userdynaBean.set("insurance_claim_amt", claim);
        if (billingGroup[i] != null && billingGroup[i] != "") {
          try {
            billingGroupId = Integer.parseInt(billingGroup[i]);
          } catch (NumberFormatException ex) {
            billingGroupId = -1;
          }
        }

        usermaindynaBean = stockIssueMainRepository.getBean();
        usermaindynaBean.set("user_issue_no", new BigDecimal(newIssueId));

        try {
          if (issueDate != null && !issueDate.equals("")) {
            if (issueTime != null && !issueTime.equals("")) {
              usermaindynaBean.set("date_time", DateUtil.parseTimestamp(issueDate, issueTime));
            } else {
              usermaindynaBean.set("date_time", DateUtil.parseTimestamp(issueDate, new DateUtil()
                  .getTimeFormatter().format(DateUtil.getCurrentTimestamp()).toString()));
            }
          } else {
            usermaindynaBean.set("date_time", stockIssueMainRepository.getDateAndTime());
          }
        } catch (ParseException ex) {
          logger.error("", ex);
          throw new HMSException(HttpStatus.BAD_REQUEST,
              "exception.instasection.datetime.parse.error", new String[] { ex.getMessage() });
        }

        usermaindynaBean.set("dept_from", Integer.parseInt(stores[0]));
        usermaindynaBean.set("user_type", userType);
        usermaindynaBean.set("issued_to", issuedTo);
        usermaindynaBean.set("reference", issueReason[0]);

        usermaindynaBean.set("username", userName);
        if (pkgId > 0) {
          usermaindynaBean.set("package_id", pkgId);
        }

        usermaindynaBean.set("ward_no", wardNo);
        if (gatepass == true) {
          BasicDynaBean gatepassBean = storeGatepassRepository.getBean();
          int gatePassId = storeGatepassRepository.getNextSequence();
          usermaindynaBean.set("gatepass_id", gatePassId);
          String gatePassNo = "G".concat(Integer.toString(gatePassId));
          gatepassBean.set("gatepass_id", gatePassId);
          gatepassBean.set("gatepass_no", gatePassNo);
          if (userType.equals("Patient")) {
            gatepassBean.set("txn_type", "Issue to Patient");
          } else {
            gatepassBean.set("txn_type", "Issue to Hospital");
          }
          gatepassBean.set("created_date", DateUtil.getCurrentTimestamp());
          gatepassBean.set("dept_id", Integer.parseInt(stores[0]));
          result = storeGatepassRepository.insert(gatepassBean) > 0;
          if (result) {
            gtpassId = gatePassId;
          }
        }

        // consignment invoice code

        if (conInsertion && isnegative) {
          if (stype[i].equalsIgnoreCase("true")) {
            List<BasicDynaBean> itemDetails = storeItemDetailsRepository
                .getRemainingQuantity(medicineId, itemIdentifiers[i]);
            boolean newIssueQty = true;
            BigDecimal issqty = null;
            BigDecimal dedQty = null;
            for (BasicDynaBean itemDetailBean : itemDetails) {
              String invoiceNo = (String) itemDetailBean.get("invoice_no");
              String suppId = (String) itemDetailBean.get("supplier_id");
              int suppInvId = (int) itemDetailBean.get("supplier_invoice_id");

              if (newIssueQty) {
                issqty = new BigDecimal(issueQty[i]);
              }

              invmaindynaBean = storeConsignmentInvoiceRepository.getBean();
              invmaindynaBean.set("supplier_invoice_id", suppInvId);
              // invmaindynaBean.set("invoice_no",invoiceNo);
              String grnNo = (String) itemDetailBean.get("grn_no");
              invmaindynaBean.set("grn_no", grnNo);
              invmaindynaBean.set("con_invoice_date",
                  storeConsignmentInvoiceRepository.getDateAndTime());
              invmaindynaBean.set("amount_payable", new BigDecimal("0"));
              invmaindynaBean.set("status", "O");
              invmaindynaBean.set("username", userName);
              invmaindynaBean.set("issue_id", newIssueId);
              invmaindynaBean.set("medicine_id", medicineId);
              invmaindynaBean.set("batch_no", itemIdentifiers[i]);
              if (billable) {
                invmaindynaBean.set("bill_no", billNos[0]);
              } else {
                invmaindynaBean.set("bill_no", "");
              }
              invmaindynaBean.set("amount", amount);
              BigDecimal qty = (BigDecimal) itemDetailBean.get("qty");
              BigDecimal remQty = qty.subtract(issqty);
              if (remQty.intValue() <= 0) {
                invmaindynaBean.set("qty", qty);
                dedQty = qty;
              } else {
                invmaindynaBean.set("qty", issqty);
                dedQty = issqty;
              }
              issqty = issqty.subtract(qty);
              if (result && isnegative) {
                if (storeConsignmentInvoiceRepository.insert(invmaindynaBean) > 0) {
                  result = storeGRNDetailsRepository.updateQuantity(grnNo, medicineId,
                      itemIdentifiers[i], dedQty) > 0;
                } else {
                  result = false;
                }
              }
              newIssueQty = false;
              if (remQty.intValue() == 0 || issqty.intValue() <= 0 || remQty.intValue() > 0) {
                break;
              }

            }

          }

        }

        if (result && inserted == 0 && isnegative) {
          if (stockIssueMainRepository.insert(usermaindynaBean) > 0) {
            inserted = 1;
            result = true;
          } else {
            result = false;
            inserted = 1;
          }
        }
        if (result && isnegative) {

          // reducing stock
          Map statusMap = stockFifoService.reduceStock(Integer.parseInt(stores[0]),
              Integer.parseInt(itemBatchId[i]), "U", issueQtyconv[i], null,
              (String) req.getSession(false).getAttribute("userid"), "UserIssue",
              (Integer) userdynaBean.get("item_issue_no"), allowNegative);

          result &= (Boolean) statusMap.get("status");

          // set total cost value
          userdynaBean.set("cost_value", statusMap.get("costValue"));
          result &= stockIssueDetailsRepository.insert(userdynaBean) > 0;

          if (billable) {
            if (!billNos[0].equalsIgnoreCase("C") && !billNos[0].isEmpty()) {

              String billStatus = (String) billBean.get("status");
              if (billStatus != null && !billStatus.equals("A")) {
                result = false;
                responseMap.put("message1",
                    "Bill : " + billNos[0] + " is not open: cannot issue items");
              } else {
                // charge
                chargeId = billChargeService.getNextPrefixedId();
                BasicDynaBean masterItemBean = storeItemDetailsRepository.findByKey("medicine_id",
                    new Integer(medicineId));
                visitType = (String) billBean.get("visit_type");

                BasicDynaBean billChargeBean = billChargeService.setBillChargeBean(groupName,
                    headName, rate, issueQtyconv[i], discount,
                    "".equals(pkgObjId[i]) ? medicineId.toString() : pkgObjId[i], itemNames[i],
                    null, (Integer) masterItemBean.get("service_sub_group_id"), insuranceCategoryId,
                    false);

                billChargeBean.set("insurance_category_id", 0);
                billChargeBean.set("bill_no", billNos[0]);
                billChargeBean.set("charge_id", chargeId);
                billChargeBean.set("orig_rate", origRate);
                billChargeBean.set("activity_conducted", "Y");
                billChargeBean.set("conducted_datetime", null);
                billChargeBean.set("username", userName);
                billChargeBean.set("user_remarks", issueReason[0]);

                int accountGroup = (Integer) store.get("account_group");
                billChargeBean.set("account_group", accountGroup > 0 ? accountGroup : 1);
                billChargeBean.set("insurance_claim_amount", claim);
                billChargeBean.set("insurance_category_id", insuranceCategoryId);
                if (billingGroupId > 0) {
                  billChargeBean.set("billing_group_id", billingGroupId);
                }

                if (pkgId > 0) {
                  billChargeBean.set("package_id", pkgId);
                  if (!isMultiPkg) {
                    billChargeBean.set("charge_ref", pkgChargeIdRef);
                    billChargeBean.set("charge_group", "PKG");
                    billChargeBean.set("submission_batch_type", submissionBatchType);
                  }
                }

                String visitId = (String) billBean.get("visit_id");
                if (visitId != null && visitId != "") {
                  BasicDynaBean visitDetails = patientRegistrationRepo
                      .findByKey("patient_id",visitId);
                  if (visitDetails != null) {
                    billChargeBean.set("revenue_department_id",
                        (String) visitDetails.get("dept_name"));
                  }
                }
                String allowZeroClaimfor = (String) masterItemBean.get("allow_zero_claim_amount");
                if (null != visitType
                    && (visitType.equalsIgnoreCase(allowZeroClaimfor) 
                    || "b".equals(allowZeroClaimfor))) {
                  billChargeBean.set("allow_zero_claim", true);
                }

                BasicDynaBean chargeHeadBean = chargeHeadsService.findByKey(headName);
                if (pkgId > 0) {
                  billChargeBean.set("act_remarks", pkgName);
                } else {
                  billChargeBean.set("act_remarks", "No. " + newIssueId);
                }
                billChargeBean.set("allow_rate_decrease",
                    (Boolean) chargeHeadBean.get("allow_rate_decrease"));
                billChargeBean.set("allow_rate_increase",
                    (Boolean) chargeHeadBean.get("allow_rate_increase"));
                billChargeBean.set("tax_amt", new BigDecimal(taxAmt[i]));
                billChargeBean.set("original_tax_amt", new BigDecimal(originalTaxAmt[i]));

                for (int j = 0; j < groupList.size(); j++) {
                  BasicDynaBean groupBean = groupList.get(j);
                  Map taxSubDetails = storeHelper.getTaxDetailsMap(itemsMap, i,
                      (Integer) groupBean.get("item_group_id"));
                  if (taxSubDetails.size() > 0) {
                    taxSubDetails.put("charge_id", chargeId);
                    taxDetailsList.add(taxSubDetails);
                  }
                }

                // multi-payer
                BigDecimal[] finalclaimAmts = new BigDecimal[claimAmts.length];
                for (int c = 0; c < finalclaimAmts.length; c++) {
                  finalclaimAmts[c] = claimAmts[i][c] != null ? new BigDecimal(claimAmts[i][c])
                      : BigDecimal.ZERO;
                }
                try {

                  if (issueDate != null && !issueDate.equals("")) {
                    if (issueTime != null && !issueTime.equals("")) {
                      billChargeBean.set("posted_date",
                          DateUtil.parseTimestamp(issueDate, issueTime));
                    } else {
                      usermaindynaBean.set("date_time",
                          DateUtil.parseTimestamp(issueDate, new DateUtil().getTimeFormatter()
                              .format(DateUtil.getCurrentTimestamp()).toString()));
                    }

                  } else {
                    billChargeBean.set("posted_date", stockIssueMainRepository.getDateAndTime());
                  }
                } catch (ParseException ex) {
                  logger.error("",ex);
                  throw new HMSException(HttpStatus.BAD_REQUEST,
                      "exception.instasection.datetime.parse.error",
                      new String[] { ex.getMessage() });
                }
                BasicDynaBean itemCodeBean = storeItemCodesRepository.getDrugCodeType(medicineId,
                    drugCodeTypes);
                if (itemCodeBean != null) {
                  if (itemCodeBean.get("code_type") != null) {
                    billChargeBean.set("code_type", (String) itemCodeBean.get("code_type"));
                  }
                  if (itemCodeBean.get("item_code") != null) {
                    billChargeBean.set("act_rate_plan_item_code",
                        (String) itemCodeBean.get("item_code"));
                  }
                }

                result &= billChargeService.insertCharge(billChargeBean, itemissueid, "PHI",
                    (String) billChargeBean.get("activity_conducted"), medicineId.toString(),
                    null) > 0;
                    
                if (pkgId > 0 && StringUtils.isNotBlank(patPkgContentId[i])) {
                  Integer patPkgContId = Integer.parseInt(patPkgContentId[i]);
                  BigDecimal qty = (BigDecimal) billChargeBean.get("act_quantity");
                  BasicDynaBean pkgConsumedBean = patPkgContentConsumedService.getBean();
                  pkgConsumedBean.set("patient_package_content_id", patPkgContId);
                  pkgConsumedBean.set("quantity", qty.intValue());
                  pkgConsumedBean.set("prescription_id", userdynaBean.get("item_issue_no"));
                  pkgConsumedBean.set("bill_charge_id", billChargeBean.get("charge_id"));
                  pkgConsumedBean.set("item_type", "Inventory");
                  result &= patPkgContentConsumedService.save(pkgConsumedBean) > 0;
                  totalAmount = totalAmount.add((BigDecimal) billChargeBean.get("amount"));
                  totalDisc = totalDisc.add((BigDecimal) billChargeBean.get("discount"));
                  totalTax = totalTax.add((BigDecimal) billChargeBean.get("tax_amt"));
                }

                if ( module != null
                    && ((String)module.get("activation_status")).equals("Y")) {
                  cacheIssueTransactions(cacheIssueTxns,usermaindynaBean,userdynaBean,patBean,
                      billNos[0], centerId, billChargeBean);
                }
                // multi-payer
                if ((Boolean) billBean.get("is_tpa")) {
                  result &= updateIssueClaimDetails(visitId, billChargeBean, billBean);
                }
              }
            } else {
              result &= true;
            }
          } else {
            result &= true;
          }

          // update indent dispense status

          if (indentDisStatusMap.size() > 0) {

            result &= storePatientIndentDetailsService.updateIndentDetailsDispenseStatus(patientId,
                indentDisStatusMap, (BigDecimal) userdynaBean.get("qty"), medicineId,
                (Integer) userdynaBean.get("item_issue_no"), "item_issue_no");

            // if user selects Close All as dispense status,we ll update dispense status of the
            // indent to 'C' even if its not dispensed
            for (String key : indentDisStatusMap.keySet()) {
              if (indentDisStatusMap.get(key).equals("all")) {
                result &= storePatientIndentDetailsService.closeAllIndents(key);
              }
            }

            result &= storePatientIndentDetailsService.updateIndentDispenseStatus(patientId);
            // I represents 'Issue' in process_type column of store_patient_indent_main table
            result &= storePatientIndentDetailsService.updateProcessType(patientId, "I");

          }
        } else {
          result = false;
        }
      }
    }

    if (result && isnegative) {
      msg = "Successfully Issued Items";
      responseMap.put("message", newIssueId);
      // redirect.addParameter("message", newIssueId);
      BasicDynaBean billChargeTaxBean;
      Iterator<Map<String, Object>> taxDetailsListIterator = taxDetailsList.iterator();
      while (taxDetailsListIterator.hasNext()) {
        Map<String, Object> taxDetails = taxDetailsListIterator.next();
        billChargeTaxBean = billChargeTaxService.getBean();
        billChargeTaxBean.set("tax_rate", taxDetails.get("tax_rate"));
        billChargeTaxBean.set("tax_amount", taxDetails.get("tax_amt"));
        billChargeTaxBean.set("original_tax_amt", taxDetails.get("tax_amt"));
        billChargeTaxBean.set("tax_sub_group_id", taxDetails.get("item_subgroup_id"));
        billChargeTaxBean.set("charge_id", taxDetails.get("charge_id"));
        result &= billChargeTaxService.insert(billChargeTaxBean);
      }

      if (result && pkgId > 0 && patPkgId > 0) {
        pkgOrderItemService.updatePatientPackageStatus(patPkgId);
      }

      if (result && pkgId > 0 && StringUtils.isNotBlank(pkgChargeIdRef)) {
        BasicDynaBean pkgBillChargeBean = billChargeService.findByKey("charge_id", pkgChargeIdRef);
        if (pkgBillChargeBean != null) {
          BigDecimal pkgAmount = (BigDecimal) pkgBillChargeBean.get("amount");
          BigDecimal pkgDisc = (BigDecimal) pkgBillChargeBean.get("discount");
          BigDecimal pkgTax = (BigDecimal) pkgBillChargeBean.get("tax_amt");
          pkgAmount = pkgAmount.subtract(totalAmount);
          pkgDisc = pkgDisc.subtract(totalDisc);
          pkgTax = pkgTax.subtract(totalTax);
          BigDecimal pkgRate = (BigDecimal) pkgBillChargeBean.get("act_rate");
          pkgRate = pkgRate.subtract(totalAmount.add(totalDisc));
          pkgBillChargeBean.set("amount", pkgAmount);
          pkgBillChargeBean.set("discount", pkgDisc);
          pkgBillChargeBean.set("tax_amt", pkgTax);
          pkgBillChargeBean.set("original_tax_amt", pkgTax);
          pkgBillChargeBean.set("act_rate", pkgRate);
          Map<String, Object> keys = new HashMap<String, Object>();
          keys.put("charge_id", pkgChargeIdRef);
          billChargeService.update(pkgBillChargeBean, keys);
          Boolean chargeTaxExists = billChargeTaxService.exist("charge_id", pkgChargeIdRef);
          if (pkgTax.compareTo(BigDecimal.ZERO) == 0 && chargeTaxExists) {
            billChargeTaxService.updateBillChargeTaxAmtToZero(pkgChargeIdRef);
          }
        }
      }

      // update stock timestamp
      stockFifoService.updateStockTimeStamp();
      stockFifoService.updateStoresStockTimeStamp(Integer.parseInt(stores[0]));

      try {
        if (userType != null && userType.equals("Patient")) {
          if (billNos != null && billNos[0] != null && !billNos[0].equals("")) {
            billService.resetTotalsOrReProcess(billNos[0]);

            // Call the allocation job and update the patient payments for the created bill.
            allocationService.updateBillTotal(billNos[0]);
            // Call the Allocation method.
            allocationService.allocate(billNos[0], centerId);
          }
        }
      } catch (ParseException ex) {
        logger.error("",ex);
        throw new HMSException(HttpStatus.BAD_REQUEST,
            "exception.instasection.datetime.parse.error", new String[] { ex.getMessage() });
      }

    } else {
      throw new PatientIssueException("exception.failed.to.issue.items");
    }
    sponsorService.recalculateSponsorAmount(patientId);

    if (!cacheIssueTxns.isEmpty() && module != null
        && ((String)module.get("activation_status")).equals("Y")) {
      scmOutService.scheduleIssueTxns(cacheIssueTxns);
    }

    if (responseMap.get("message") == null) {
      responseMap.put("message", newIssueId);
    }
    responseMap.put("gtpass", gatepass);
    if (billNos != null && billNos[0] != null) {
      responseMap.put("billNo", billNos[0]);
    }
    responseMap.put("fromOTScreen", req.getParameter("fromOTScreen"));
    responseMap.put("operation_details_id", req.getParameter("operation_details_id"));
    responseMap.put("visitIdForOT", patientId);
    return responseMap;
  }

  /**
   * Generate gate passprint for issue.
   *
   * @param params
   *          the params
   * @return the map
   * @throws TemplateNotFoundException
   *           the template not found exception
   * @throws MalformedTemplateNameException
   *           the malformed template name exception
   * @throws ParseException
   *           the parse exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws TemplateException
   *           the template exception
   * @throws XPathExpressionException
   *           the x path expression exception
   * @throws SQLException
   *           the SQL exception
   * @throws DocumentException
   *           the document exception
   * @throws TransformerException
   *           the transformer exception
   */
  public Map<String, Object> generateGatePassprintForIssue(Map<String, Object> params)
      throws TemplateNotFoundException, MalformedTemplateNameException,
      freemarker.core.ParseException, IOException, TemplateException, XPathExpressionException,
      SQLException, DocumentException, TransformerException {

    Map<String, Object> responseMap = new HashMap<>();
    Template template = null;
    Map<String, Object> templateParams = new HashMap<>();
    String issNo = (String) params.get("issNo");

    if (issNo != null) {

      List<BasicDynaBean> gatePassItemList = stockIssueMainRepository
          .getIssuedItemList(Integer.parseInt(issNo));
      templateParams.put("items", gatePassItemList);
      templateParams.put("type", "Issue");
      String templateContent = printTemplateService
          .getCustomizedTemplate(PrintTemplate.Gate_pass_print);

      if (templateContent == null || templateContent.equals("")) {
        template = AppInit.getFmConfig()
            .getTemplate(PrintTemplate.Gate_pass_print.getFtlName() + ".ftl");
      } else {
        StringReader reader = new StringReader(templateContent);
        template = new Template("GatePassPrint.ftl", reader, AppInit.getFmConfig());
      }
      StringWriter writer = new StringWriter();
      template.process(templateParams, writer);
      String printContent = writer.toString();
      HtmlConverter hc = new HtmlConverter();
      BasicDynaBean printprefs = printConfigurationRepository
          .getPageOptions(PrintConfigurationRepository.PRINT_TYPE_STORE);
      if (printprefs.get("print_mode").equals("P")) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        hc.writePdf(os, printContent, "GatePassPrint", printprefs, false, false, true, true, true,
            false);
        responseMap.put("type", "pdf");
        responseMap.put("data", os.toByteArray());
      } else {
        String textReport = null;
        textReport = new String(
            hc.getText(printContent, "GatePassPrintText", printprefs, true, true));

        responseMap.put("type", "text");

        responseMap.put("textReport", textReport);
        responseMap.put("textColumns", printprefs.get("text_mode_column"));
        responseMap.put("printerType", "DMP");
      }
    }
    return responseMap;
  }

  /**
   * Gets the claim amount.
   *
   * @param params
   *          the params
   * @return the claim amount
   */
  public Map<String, Object> getClaimAmount(Map<String, Object> params) {

    Map<String, Object> result = new HashMap();

    int planId = params.get("plan_id") != null ? Integer.parseInt((String) params.get("plan_id"))
        : 0;
    BigDecimal amount = params.get("amount") != null ? new BigDecimal((String) params.get("amount"))
        : BigDecimal.ZERO;
    String visitType = (String) params.get("visit_type");
    int categoryId = params.get("category_id") != null
        ? Integer.parseInt((String) params.get("category_id")) : 0;
    boolean firstOfCategory = params.get("foc").equals("true");
    BigDecimal discount = params.get("discount") != null
        ? new BigDecimal((String) params.get("discount")) : BigDecimal.ZERO;

    BigDecimal claimAmt = null;
    try {
      claimAmt = new AdvanceInsuranceCalculator().calculateClaim(amount, discount, null, planId,
          firstOfCategory, visitType, categoryId);
    } catch (SQLException ex) {
      logger.error("", ex);
    }

    result.put("claimAmt", claimAmt);
    return result;
  }

  private void cacheIssueTransactions(List<Map<String,Object>> cacheIssueTxns,
      BasicDynaBean issueMain, BasicDynaBean issueDetails, BasicDynaBean  patient,
      String billNo, Integer centerId, BasicDynaBean billCharge ) {
    Map<String, Object> data = scmOutService.getIssueMap(issueMain, issueDetails,
        patient.getMap(), billNo, centerId, billCharge);
    if (!data.isEmpty()) {
      cacheIssueTxns.add(data);
    }
  }
  
  /**
   * Gets the package details.
   *
   * @param params the params
   * @return the package details
   */
  public Map<String, Object> getPackageDetails(Map<String , Object> params) {
    String visitId = (String) params.get("visit_id");
    String billNo = (String) params.get("bill_no");
    String tpaId = (String) params.get("tpa_id");
    Integer packageId = Integer.parseInt((String) params.get("package_id"));

    BasicDynaBean billBean = billService.findByKey(billNo);
    BasicDynaBean tpaBean = null;
    if (StringUtils.isNotEmpty(tpaId)) {
      tpaBean = tpaService.getDetails(tpaId);
    }

    List<BasicDynaBean> subGroupCodes = new ArrayList<>();
    subGroupCodes = pkgService.getPackageItemSubGroupTaxDetails(packageId);

    BasicDynaBean patientDetails = patientRegistrationService.findByKey("patient_id", visitId);
    String bedType = (String) patientDetails.get("bed_type");
    String orgId =  (String) patientDetails.get("org_id");
    String visitType = (String) patientDetails.get("visit_type");
    int planId = 0;
    planId = (Integer) patientDetails.get("plan_id");
    BasicDynaBean planBean = insurancePlanService.findByKey(planId);
    
    int discountPlanId = 0;
    if (null != planBean && null != planBean.get("discount_plan_id")) {
      discountPlanId = (Integer) planBean.get("discount_plan_id");
    }
    
    List<BasicDynaBean> discountPlanDetails = discountPlanService.listAllDiscountPlanDetails(null,
        "discount_plan_id", discountPlanId, "priority");
    
    BasicDynaBean packageCharges = null;
    List<BasicDynaBean> pkgInvDetails = new ArrayList<>();
    packageCharges = pkgChargesService.getPackageCharges(packageId,
        orgId, bedType);

    BasicDynaBean patientBean = patientDetailsService.getPatientDetailsForVisit(visitId);
    Integer patPkgId = Integer.parseInt((String) params.get("patPkgId"));
    pkgInvDetails = pkgService.getMultiPkgDetails(patPkgId);
    if (pkgInvDetails == null || pkgInvDetails.isEmpty()) {
      pkgInvDetails = pkgService.getPkgInvDetails(patPkgId, orgId, bedType);
    }

    BigDecimal discount = BigDecimal.ZERO;
    BigDecimal amount = BigDecimal.ZERO;
    if (packageCharges != null) {
      discount = (BigDecimal) packageCharges.get("discount");
      amount = (BigDecimal) packageCharges.get("charge");
    }
    BigDecimal discPer = BigDecimal.ZERO;
    if (amount.compareTo(BigDecimal.ZERO) != 0) {
      discPer = discount.divide(amount, 16, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
    }

    Integer storeId = Integer.parseInt((String) params.get("storeId"));
    BasicDynaBean store = storeService.findByStore(storeId);
    Integer centerId = (Integer) store.get("center_id");
    BasicDynaBean centerBean = centerService.findByKey(centerId);
    String healthAuthority = (String) centerBean.get("health_authority");

    List<Integer> medicines = new ArrayList<>();
    Map<Integer, Long> medReqQty = new HashMap<>();
    Map<Integer, Map<String,Object>> pkgTaxDetails = new HashMap<>();
    for (BasicDynaBean pkgDetail : pkgInvDetails) {
      Integer medId = Integer.parseInt((String)pkgDetail.get("medicine_id"));
      Integer insCatId = (Integer)pkgDetail.get("insurance_category_id");
      Long qty = (Long) pkgDetail.get("quantity");
      BigDecimal rate = (BigDecimal) pkgDetail.get("charge");
      BigDecimal itemAmount = rate.divide(new BigDecimal((Integer)
          pkgDetail.get("total_quantity")), 4, RoundingMode.HALF_UP);
      BigDecimal itemDiscount = itemAmount.multiply(discPer)
          .divide(new BigDecimal(100), 4, RoundingMode.HALF_UP);
      
      BigDecimal discountPlanDiscount = getDiscountPlanDiscount("PKGPKG",packageId, 
          insCatId, discountPlanDetails, rate, new BigDecimal(qty));
      
      itemDiscount = itemDiscount.add(discountPlanDiscount);
      
      BigDecimal discItemAmount = itemAmount.subtract(itemDiscount);
      medicines.add(medId);
      medReqQty.put(medId, qty);

      Map<Integer,Object> taxMap = getPkgItemTaxMap(medId, discItemAmount, billBean,
          centerBean, patientBean, tpaBean, subGroupCodes);
      Map<String,Object> itemTaxDetails = new HashMap<>();
      itemTaxDetails.put("unit_mrp", itemAmount);
      itemTaxDetails.put("original_mrp", itemAmount);
      itemTaxDetails.put("discount_amt", itemDiscount);
      itemTaxDetails.put("discount_per", discPer);
      itemTaxDetails.put("tax_map", taxMap);
      pkgTaxDetails.put(medId, itemTaxDetails);
    }

    HashMap<String, Object> medBatches = new HashMap<>();
    HashMap<String, Object> pkgItemDetail = new HashMap<>();
    if (!pkgInvDetails.isEmpty()) {
      for (BasicDynaBean pkgItem : pkgInvDetails) {
        pkgItemDetail.put(pkgItem.get("medicine_id").toString(), pkgItem);
      }
    }

    if (!medicines.isEmpty()) {
      List<BasicDynaBean> stock = stockService.getPackageMedicineStockInStore(medicines,
          storeId, planId, visitType, healthAuthority);

      medBatches.putAll(ConversionUtils.listBeanToMapListMap(stock, "medicine_id"));
    }

    List<BasicDynaBean> pkgItems = pkgService.getPkgItems(medicines);

    List<Map<String, Object>> pkgItemList = new ArrayList<>();

    for (BasicDynaBean item : pkgItems) {
      Map<String, Object> pkgItemMap =  new HashMap<>(item.getMap());
      pkgItemMap.put("qty", medReqQty.get((Integer)pkgItemMap.get("medicine_id")));
      String qtyUnit = (String) pkgItemMap.get("qty_unit");
      String pkgUom = (String) pkgItemMap.get("package_uom");
      if ("I".equals(qtyUnit)) {
        pkgItemMap.put("uom","");
      } else {
        pkgItemMap.put("uom", pkgUom);
      }
      BasicDynaBean pkgItem = (BasicDynaBean) pkgItemDetail.get(
          pkgItemMap.get("medicine_id").toString());
      pkgItemMap.put("patient_package_content_id", pkgItem.get("patient_package_content_id"));
      pkgItemMap.put("pack_ob_id", pkgItem.get("pack_ob_id"));
      pkgItemList.add(pkgItemMap);
    }

    Map<String, Object> responseMap = new HashMap<String, Object>();
    responseMap.put("medBatches", medBatches);
    responseMap.put("pkgItemDetails", pkgItemList);
    responseMap.put("pkgTaxDetails", pkgTaxDetails);

    PackagesDTO pkgDto = pkgService.findById(packageId);

    Boolean isMultiPkg = pkgDto.getPackagesModel().getMultiVisitPackage();

    if (isMultiPkg) {
      responseMap.put("multi_visit", true);
    } else {
      responseMap.put("multi_visit", false);
    }

    return responseMap;
  }

  private BigDecimal getDiscountPlanDiscount(String chargeHead, Integer packageId, Integer insCatId,
      List<BasicDynaBean> discountPlanDetails, 
      BigDecimal actRate, BigDecimal actQuantity) {
    BigDecimal discountPlanDiscAmt = BigDecimal.ZERO;
    BigDecimal disPerc = BigDecimal.ZERO;
    BasicDynaBean chargeBean = billChargeService.getBean();
    chargeBean.set("insurance_category_id", insCatId);
    chargeBean.set("charge_head", chargeHead);
    chargeBean.set("act_description_id", packageId.toString());
    BasicDynaBean discountRule = discountPlanService.getDiscountRule(chargeBean,
        discountPlanDetails);
    
    if (discountRule != null) {

      if (((String) discountRule.get("discount_type")).equals("P")) {
        disPerc = (BigDecimal) discountRule.get("discount_value");
      }

      if (((String) discountRule.get("discount_type")).equals("A")) {
        discountPlanDiscAmt = (BigDecimal) discountRule.get("discount_value");
      } else {
        discountPlanDiscAmt = (actRate.multiply(actQuantity)).multiply(disPerc)
          .divide(new BigDecimal(100));
      }
    }
    
    return discountPlanDiscAmt;
  }

  /**
   * Gets the pkg item tax map.
   *
   * @param medicineId the medicine id
   * @param amount the amount
   * @param billBean the bill bean
   * @param centerBean the center bean
   * @param patientBean the patient bean
   * @param tpaBean the tpa bean
   * @param subGroupCodes the sub group codes
   * @return the pkg item tax map
   */
  protected Map<Integer,Object> getPkgItemTaxMap(Integer medicineId, BigDecimal amount,
      BasicDynaBean billBean, BasicDynaBean centerBean, BasicDynaBean patientBean,
      BasicDynaBean tpaBean, List<BasicDynaBean> subGroupCodes) {
    ItemTaxDetails itemTaxDetails = new ItemTaxDetails();
    itemTaxDetails.setAmount(amount);

    TaxContext taxContext = new TaxContext();
    taxContext.setCenterBean(centerBean);
    taxContext.setBillBean(billBean);
    taxContext.setPatientBean(patientBean);
    if (tpaBean != null) {
      taxContext.setTpaBean(tpaBean);
    }

    Map<Integer, Object> taxChargesMap = new HashMap<Integer, Object>();
    taxChargesMap = billHelper.getTaxChargesMap(itemTaxDetails, taxContext, subGroupCodes);

    return taxChargesMap;
  }

  /**
   * get Issue Charges.
   * @param cdto the ChargeDTO
   * @param isInsurance the isInsurance
   * @param planIds the planIds
   * @param visitType the visitType
   * @param patientId the patientId
   * @param sellingPrice the sellingPrice
   * @param disc the disc
   * @return list of ChargeDTO
   * @throws SQLException the SQLException
   */
  public List<ChargeDTO> getIssueCharges(ChargeDTO cdto, boolean isInsurance, int[] planIds,
      String visitType, String patientId, BigDecimal sellingPrice, BigDecimal disc)
          throws SQLException {
    ChargeDTO chrgdto = new ChargeDTO(cdto.getChargeGroup(), cdto.getChargeHead(), sellingPrice,
        cdto.getActQuantity(), disc, "", cdto.getActDescriptionId(), cdto.getActDescription(), null,
        isInsurance, cdto.getServiceSubGroupId(), cdto.getInsuranceCategoryId(), visitType,
        patientId, cdto.getFirstOfCategory());
    chrgdto.setActRatePlanItemCode(cdto.getActRatePlanItemCode());
    chrgdto.setCodeType(cdto.getCodeType());
    chrgdto.setAllowDiscount(cdto.isAllowDiscount());
    chrgdto.setAllowRateIncrease(cdto.isAllowRateIncrease());
    chrgdto.setAllowRateDecrease(cdto.isAllowRateDecrease());
    chrgdto.setActRemarks(cdto.getActRemarks());

    if (null != cdto.getBillingGroupId()) {
      chrgdto.setBillingGroupId(cdto.getBillingGroupId());
    }
    return Arrays.asList(chrgdto);
  }
  
  public Map<String, BasicDynaBean> getVisitIssuedItemMap(String billNo) {
    return stockIssueMainRepository.getVisitIssuedItemMap(billNo);
  }
  
  public Map<String, BasicDynaBean> getVisitIssueReturnedItemMap(String billNo) {
    return stockIssueMainRepository.getVisitIssueReturnedItemMap(billNo);
  }

}
