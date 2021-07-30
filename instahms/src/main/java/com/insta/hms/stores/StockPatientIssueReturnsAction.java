package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.Registration.PatientInsurancePlanDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.advancedpackages.PatientPackagesDAO;
import com.insta.hms.billing.Bill;
import com.insta.hms.billing.BillChargeClaimDAO;
import com.insta.hms.billing.BillChargeTaxDAO;
import com.insta.hms.billing.BillDAO;
import com.insta.hms.billing.ChargeDAO;
import com.insta.hms.billing.ChargeDTO;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.core.billing.AllocationService;
import com.insta.hms.core.billing.BillService;
import com.insta.hms.core.billing.BillChargeService;
import com.insta.hms.core.inventory.StoresHelper;
import com.insta.hms.insurance.SponsorBO;
import com.insta.hms.integration.scm.inventory.ScmOutBoundInvService;
import com.insta.hms.master.Accounting.ChargeHeadsDAO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.PrintTemplates.PrintTemplate;
import com.insta.hms.master.PrintTemplates.PrintTemplatesDAO;
import com.insta.hms.master.StoresItemMaster.StoresItemDAO;
import com.insta.hms.modules.ModulesDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import flexjson.JSONSerializer;
import freemarker.template.Template;

public class StockPatientIssueReturnsAction extends BaseAction {

  private static Logger log = LoggerFactory.getLogger(StockPatientIssueReturnsAction.class);
  private static PatientInsurancePlanDAO patInsrPlanDao = new PatientInsurancePlanDAO();
  private static PatientInsurancePlanDAO insPlanDAO = new PatientInsurancePlanDAO();
  private static StockPatientIssueReturnsDAO returnsDAO = new StockPatientIssueReturnsDAO();

  private static BillChargeClaimDAO chgClaimDAO = new BillChargeClaimDAO();
  private static CenterMasterDAO centerMasterDao = new CenterMasterDAO();
  private static ModulesDAO modulesDao = new ModulesDAO();
  private static ScmOutBoundInvService scmOutService = ApplicationContextProvider
        .getBean(ScmOutBoundInvService.class);
  private static PatientPackagesDAO patpkgDao = new PatientPackagesDAO();

  private static JSONSerializer js = new JSONSerializer().exclude("class");
  
  private static final GenericDAO storeItemDetailsDAO = new GenericDAO("store_item_details");
  private static final GenericDAO storeCategoryMasterDAO = new GenericDAO("store_category_master");
  
  private static BillService billService = 
      ApplicationContextProvider.getBean(BillService.class);

  private static final BillChargeService billChargeService =
      ApplicationContextProvider.getBean(BillChargeService.class);

  private static final String ITEM_DETAIL_LIST_QUERY = " SELECT S.MEDICINE_ID,C.identification,sibd.mrp,I.tax_rate,I.MEDICINE_NAME, "
      + " (CASE WHEN C.ISSUE_TYPE='C' THEN 'CONSUMABLE' WHEN C.ISSUE_TYPE='L' THEN 'REUSABLE' "
      + " WHEN C.ISSUE_TYPE='P' THEN 'PERMANENT' END ) AS ISSUE_TYPE ,"
      + " S.DEPT_ID,sibd.BATCH_NO,COALESCE(sibd.exp_dt,null) AS EXP_DT, "
      + " C.billable,S.QTY,I.PACKAGE_TYPE AS PKG_TYPE,i.issue_base_unit as pkg_size,I.CUST_ITEM_CODE "
      + "   FROM STORE_STOCK_DETAILS S "
      + "  JOIN store_item_batch_details sibd USING(item_batch_id)"
      + " JOIN store_item_details I ON (I.MEDICINE_ID = S.medicine_id) "
      + " JOIN store_category_master C ON(C.CATEGORY_ID =I.MED_CATEGORY_ID) "
      + " WHERE S.ASSET_APPROVED='Y' ";
  
  private final AllocationService allocationService = (AllocationService) ApplicationContextProvider
      .getApplicationContext().getBean("allocationService");

  public ActionForward show(ActionMapping am, ActionForm af, HttpServletRequest req,
      HttpServletResponse res) throws Exception {

    String patientIndentno = req.getParameter("patient_indent_no");
    String patientId = "";
    Map visitDetailsMap = null;
    List<Bill> activeBills = new ArrayList<>();
    if (req.getParameter("patient_id") != null) {
      patientId = req.getParameter("patient_id");
      visitDetailsMap = VisitDetailsDAO.getPatientVisitDetailsMap(patientId);
    }

    activeBills = BillDAO.getAllActiveBills(patientId);
    if (patientId.trim().length() > 0) {
      Connection con = DataBaseUtil.getConnection(true);
      ChargeDAO billChargeDAO = new ChargeDAO(con);
      List<BasicDynaBean> pkgIssued = billChargeDAO.getInvStaticPackagesIssued(activeBills);
      req.setAttribute("packages", js.deepSerialize(ConversionUtils
          .copyListDynaBeansToMap(pkgIssued)));
    }
    req.setAttribute("patient", visitDetailsMap);
    // multi-payer : set visit plan details
    setIssuedClaimDetails(req);

    req.setAttribute("indentStore", req.getParameter("indentStore"));

    List<BasicDynaBean> visitIssueReturns = returnsDAO.getVisitIssuedItemsDetails(
        req.getParameter("patient_id"),
        (visitDetailsMap != null ? (Integer) visitDetailsMap.get("plan_id") : 0));
    req.setAttribute("issuedItemsJSON",
        js.deepSerialize(ConversionUtils.listBeanToMapListMap(visitIssueReturns, "dept_id")));

    req.setAttribute("issuedItemsMedicineWiseMapJSON",
        js.deepSerialize(ConversionUtils.listBeanToMapListMap(visitIssueReturns, "medicine_id")));

    List<BasicDynaBean> activeBillsExPkg = billService.getVisitOpenBillsExcludingPackageBills(patientId);
    req.setAttribute("bills", ConversionUtils.listBeanToListMap(activeBillsExPkg));
    req.setAttribute("billsJSON", js.deepSerialize(ConversionUtils.listBeanToListMap(activeBillsExPkg)));
    req.setAttribute("itemTaxDetails", js.serialize(ConversionUtils
        .copyListDynaBeansToMap(returnsDAO.getVisitIssuedItemsTaxDetails(patientId))));
    int planId = visitDetailsMap != null ? (Integer) visitDetailsMap.get("plan_id") : 0;

    int storeRatePlanId = 0;
    if (visitDetailsMap != null && visitDetailsMap.get("store_rate_plan_id") != null)
      storeRatePlanId = (Integer) visitDetailsMap.get("store_rate_plan_id");

    Map allBatches = new HashMap();
    if (patientIndentno != null) // set returnable indents
      getIndentDetails(req, allBatches, req.getParameter("patient_id"), planId, "R",
          storeRatePlanId, patientIndentno);
    req.setAttribute("allBatches", js.deepSerialize(allBatches));

    if (patientIndentno != null) {
      List<BasicDynaBean> returnIndentItems = returnsDAO
          .getReturnIndentItemsDetails(patientIndentno);
      req.setAttribute("returnIndentItems", js
          .deepSerialize(ConversionUtils.listBeanToMapListMap(returnIndentItems, "item_batch_id")));
    }

    BasicDynaBean prefs = GenericPreferencesDAO.getAllPrefs();
    req.setAttribute("prefs", prefs.getMap());
    HttpSession session = req.getSession(false);
    String store_id = (String) session.getAttribute("pharmacyStoreId");
    if (store_id != null && !store_id.equals("")) {
      req.setAttribute("store_id", store_id);
      BasicDynaBean store = new GenericDAO("stores").findByKey("dept_id",
          Integer.parseInt(store_id));
      String store_name = store.get("dept_name").toString();
      req.setAttribute("store_name", store_name);
      req.setAttribute("store_id", store_id);
    }
    String username = (String) session.getAttribute("userid");
    BasicDynaBean uBean = new GenericDAO("u_user").findByKey("emp_username", username);
    if (uBean != null)
      req.setAttribute("isSharedLogIn", uBean.get("is_shared_login"));

    List<BasicDynaBean> sugGroupList = PurchaseOrderDAO.getAllSubGroups();
    List<BasicDynaBean> groupList = PurchaseOrderDAO.getAllGroups();
    req.setAttribute("subGroupListJSON",
        js.serialize(ConversionUtils.listBeanToListMap(sugGroupList)));
    req.setAttribute("groupList", ConversionUtils.listBeanToListMap(groupList));
    req.setAttribute("groupListJSON", js.serialize(ConversionUtils.listBeanToListMap(groupList)));
    return am.findForward("addshow");
  }

  public void getIndentDetails(HttpServletRequest req, Map allBatches, String visitId, int planId,
      String indentType, int storeRatePlanId, String patientIndentno) throws SQLException {

    StoresPatientIndentDAO indentDAO = new StoresPatientIndentDAO();
    String indentStore = req.getParameter("indentStore") == null
        ? "0"
        : req.getParameter("indentStore");

    String deptId = req.getParameter("storeId");
    BasicDynaBean storeDetails = StoreDAO.findByStore(Integer.parseInt(deptId));
    Integer centerId = (Integer) storeDetails.get("center_id");
    String healthAuthority = CenterMasterDAO.getHealthAuthorityForCenter(centerId);

    String visitType = VisitDetailsDAO.getVisitType(visitId);
    List<BasicDynaBean> indents = indentDAO.getIndentsForReturnProcess(patientIndentno, indentType,
        "I");
    List<BasicDynaBean> indentDetailsLIst = new ArrayList<BasicDynaBean>();
    List<Integer> medicines = new ArrayList<Integer>();
    // only finalized indents are to process
    indentDetailsLIst
        .addAll(StoresPatientIndentDAO.getIndentDetailsForReturnProcess(patientIndentno, "F", "I"));
    for (BasicDynaBean indentDet : indentDetailsLIst) {
      medicines.add((Integer) indentDet.get("medicine_id"));
    }
    if (indentDetailsLIst.size() > 0) {
      List<BasicDynaBean> stock = MedicineStockDAO.getAllStoreMedicineStockWithPatAmtsInDept(
          medicines, planId, visitType, true, storeRatePlanId, healthAuthority,
          ((String) storeDetails.get("use_batch_mrp")).equals("Y"));
      allBatches.putAll(ConversionUtils.listBeanToMapListMap(stock, "medicine_id"));
    }
    if (indentDetailsLIst.size() > 0) {
      req.setAttribute("patIndentDetailsJSON", js
          .deepSerialize(ConversionUtils.listBeanToMapListMap(indentDetailsLIst, "indent_store")));
      req.setAttribute("indentsListJSON",
          js.deepSerialize(ConversionUtils.listBeanToMapListMap(indents, "indent_store")));
    }
  }

  public ActionForward create(ActionMapping am, ActionForm af, HttpServletRequest req,
      HttpServletResponse res) throws Exception {

    GenericDAO stockIssueReturnMainDAO = new GenericDAO("store_issue_returns_main");
    GenericDAO stockIssueReturnDetailsDAO = new GenericDAO("store_issue_returns_details");
    GenericDAO issueReturnReferencesDAO = new GenericDAO("patient_issue_returns_issue_charge_details");
    GenericDAO stockIssueDAO = new GenericDAO("stock_issue_details");
    GenericDAO patPkgContConsumedDAO= new GenericDAO("patient_package_content_consumed");
    GenericDAO pkgDAO = new GenericDAO("packages");
    BillChargeTaxDAO billChargeTaxDao = new BillChargeTaxDAO();
    ChargeDTO chargeDTO = null;
    Map reqMap = req.getParameterMap();
    String[] medicineId = (String[]) reqMap.get("medicine_id");
    String[] billNo = (String[]) reqMap.get("bill_no");
    String[] rates = (String[]) reqMap.get("unit_rate");
    String[] medicineName = (String[]) reqMap.get("medicine_name");
    String[] patIndentNoRef = (String[]) reqMap.get("patientIndentNoRef");
    String[] disStatus = (String[]) reqMap.get("dispensedMedicine");
    String[] indentItemId = (String[]) reqMap.get("indent_item_id");
    String[] patIndentNo = (String[]) reqMap.get("patient_indent_no");
    String[] patInsClaimAmt = (String[]) reqMap.get("insurance_claim_amt");
    String[] pri_patInsClaimAmt = (String[]) reqMap.get("pri_insurance_claim_amt");
    String[] sec_patInsClaimAmt = (String[]) reqMap.get("sec_insurance_claim_amt");
    String[] planId = (String[]) reqMap.get("plan_id");
    String[] itemTaxAmount = (String[]) reqMap.get("tax_amount");
    String[] itemOriginalTaxAmount = (String[]) reqMap.get("original_tax_amount");
    String[] itemTaxRate = (String[]) reqMap.get("tax_rate");
    Integer loggedInCenterId = (Integer) req.getSession().getAttribute("centerId");

    StoresHelper storeHelper = new StoresHelper();
    String billnostr = (billNo != null && billNo[0] != null && !billNo[0].isEmpty()
        ? billNo[0]
        : null);

    String[][] claimAmts = new String[pri_patInsClaimAmt.length][pri_patInsClaimAmt.length];;
    for (int i = 0; i < pri_patInsClaimAmt.length; i++) {
      claimAmts[i][0] = pri_patInsClaimAmt[i];
      claimAmts[i][1] = sec_patInsClaimAmt[i];
    }

    String packageId = reqMap.get("package_id") != null
        ? ((String[])reqMap.get("package_id"))[0] : null;
    String patPackageId = reqMap.get("activePackage") != null
        ? ((String[])reqMap.get("activePackage"))[0] : null;
    Integer pkgId = 0;
    Integer patPkgId = 0;
    String pkgName = "";
    if (StringUtils.isNotBlank(packageId) && NumberUtils.isParsable(packageId)) {
      pkgId = Integer.parseInt(packageId);
    }
    if (StringUtils.isNotBlank(patPackageId) && NumberUtils.isParsable(patPackageId)) {
      patPkgId = Integer.parseInt(patPackageId);
    }
    Boolean isMultiPkg = false;
    if (pkgId > 0) {
      BasicDynaBean pkgBean = pkgDAO.findByKey("package_id", pkgId);
      if (pkgBean != null) {
        pkgName = (String) pkgBean.get("package_name");
        isMultiPkg = (Boolean) pkgBean.get("multi_visit_package");
      }
    }
    String pkgChargeIdRef = reqMap.get("pkg_charge_id_ref") != null
        ? ((String[])reqMap.get("pkg_charge_id_ref"))[0] : null;

    BigDecimal newReturnId = new BigDecimal(DataBaseUtil.getNextSequence("store_issue_returns_sequence"));
    BasicDynaBean mainBean = stockIssueReturnMainDAO.getBean();
    BasicDynaBean detailBean = null;
    ConversionUtils.copyToDynaBean(reqMap, mainBean);
    mainBean.set("user_return_no", newReturnId);
    String visitId = (String) mainBean.get("returned_by");

    Connection con = null;
    boolean success = true;
    boolean allSuccess = false;

    List<String> lockedClaimIssueCharges = new ArrayList<>();
    ActionRedirect redirect = new ActionRedirect(am.findForwardConfig("addRedirect"));
    redirect.addParameter("patient_id", mainBean.get("returned_by"));

    Map<String, String> indentDisStatusMap = new HashMap<String, String>();
        List<Map<String, Object>> cacheIssueTxns = new ArrayList<>();
        BasicDynaBean module = modulesDao.findByKey("module_id", "mod_scm");
    if (patIndentNoRef != null) {
      for (int i = 0; i < patIndentNoRef.length; i++) {
        indentDisStatusMap.put(patIndentNoRef[i], disStatus[i]);
      }
    }
    /**
     * 1.Issue returns main insert 2.Issue returns details insert 3.updateStock 4.Update Return qty
     * in stock_issue_details 5.Insert charges 6.Update the original bill of issue insurence claim
     * amount,return qty and all 7.Update dispense status of indents (if indents exists)
     */
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      ChargeDAO chargeDAO = new ChargeDAO(con);
      Map visitDetailsMap = VisitDetailsDAO
          .getPatientVisitDetailsMap((String) mainBean.get("returned_by"));
      String visitType = (String) visitDetailsMap.get("visit_type");
      BasicDynaBean chargeHeadBean = new ChargeHeadsDAO().findByKey("chargehead_id",
          ChargeDTO.CH_INVENTORY_RETURNS);

      // 1.Insert into issue return main table
      mainBean.set("date_time", GenericDAO.getDateAndTime());
      if (pkgId > 0) {
        mainBean.set("package_id", pkgId);
      }
      success = stockIssueReturnMainDAO.insert(con, mainBean);
      List<BasicDynaBean> groupList = PurchaseOrderDAO.getAllGroups();
      ArrayList<Map<String, Object>> taxDetailsList = new ArrayList<Map<String, Object>>();
      BigDecimal totalAmount = BigDecimal.ZERO;
      BigDecimal totalDisc = BigDecimal.ZERO;
      BigDecimal totalTax = BigDecimal.ZERO;
      int latestIssueBillingGrp = 0;
      List<BasicDynaBean> returnReferencesList = new ArrayList<BasicDynaBean>();
      BasicDynaBean issueReturnReferenceBean = null;

      for (int i = 0; i < medicineId.length - 1; i++) {
        detailBean = stockIssueReturnDetailsDAO.getBean();
        ConversionUtils.copyIndexToDynaBean(reqMap, i, detailBean);
        int itemReturnNo = stockIssueReturnDetailsDAO.getNextSequence();
        detailBean.set("item_return_no", itemReturnNo);
        detailBean.set("user_return_no", newReturnId);

        BigDecimal rate = null;
        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal quantity = BigDecimal.ZERO;
        BigDecimal amount = BigDecimal.ZERO;
        BigDecimal taxAmt = BigDecimal.ZERO;
        BigDecimal originalTaxAmt;
        BigDecimal taxRate = BigDecimal.ZERO;

        quantity = (BigDecimal) detailBean.get("qty");

        if (billNo != null && billNo[0] != null && !billNo[0].isEmpty()) {
          rate = new BigDecimal(rates[i]);
          discount = (BigDecimal) detailBean.get("discount");
          amount = (rate.multiply(quantity)).subtract(discount.multiply(quantity));
        }
        detailBean.set("amount", amount);
        taxAmt = new BigDecimal(itemTaxAmount[i]);
        taxRate = new BigDecimal(itemTaxRate[i]);
        originalTaxAmt = new BigDecimal(itemOriginalTaxAmount[i]);
        // Get the item issue details for allocating
        // return qty, return claim, return amount against issue item.
        List<BasicDynaBean> issues = new StockPatientIssueReturnsDAO().getVisitItemIssues(con,
            (String) mainBean.get("returned_by"), (Integer) detailBean.get("medicine_id"),
            (Integer) detailBean.get("item_batch_id"), (Integer) mainBean.get("dept_to"),
            pkgId);

        // holds the issue charges against which returns are being made
        List<ChargeDTO> updateChargeList = new ArrayList<ChargeDTO>();
        BigDecimal redQty = BigDecimal.ZERO;
        BigDecimal retCostValue = BigDecimal.ZERO;
        BigDecimal remQty = (BigDecimal) detailBean.get("qty");
        BasicDynaBean cBean = null;
        boolean claimable = true;
        BasicDynaBean itemBean = storeItemDetailsDAO.findByKey("medicine_id",
            detailBean.get("medicine_id"));
        if (itemBean != null)
          cBean = storeCategoryMasterDAO.findByKey("category_id",
              itemBean.get("med_category_id"));
        if (cBean != null)
          claimable = (Boolean) cBean.get("claimable");

        int insuranceCategoryId = 0;
        BigDecimal claimAmt = BigDecimal.ZERO;

        if (itemBean != null)
          insuranceCategoryId = Integer.parseInt(itemBean.get("insurance_category_id").toString());

        StockFIFODAO stockFIFODAO = new StockFIFODAO();
        Map statusMap = null;
        BigDecimal costValue = BigDecimal.ZERO;

        BigDecimal totalIssueQty = BigDecimal.ZERO;
        String chargeId = chargeDAO.getNextChargeId();
        BigDecimal qtyForTax = (BigDecimal) detailBean.get("qty");
        for (BasicDynaBean issue : issues) {
          BasicDynaBean issueBean = stockIssueDAO.findByKey("item_issue_no",
              issue.get("item_issue_no"));
          issueReturnReferenceBean = issueReturnReferencesDAO.getBean();

          totalIssueQty = totalIssueQty
              .add(((BigDecimal) issue.get("qty")).subtract((BigDecimal) issue.get("return_qty")));

          // No remaining quantity i.e no issue items to return quantity.
          if (remQty.compareTo(BigDecimal.ZERO) == 0)
            break; // adjusted return qty in all issues

          // Item total issued quantity is returned.
          if (((BigDecimal) issue.get("qty")).compareTo((BigDecimal) issue.get("return_qty")) == 0)
            continue;

          redQty = remQty.compareTo(
              ((BigDecimal) issue.get("qty")).subtract((BigDecimal) issue.get("return_qty"))) > 0
                  ? ((BigDecimal) issue.get("qty")).subtract((BigDecimal) issue.get("return_qty"))
                  : remQty;
          remQty = remQty.subtract(redQty);

          // Return Quantity in issue
          issueBean.set("return_qty", ((BigDecimal) issueBean.get("return_qty")).add(redQty));
          BigDecimal[] finalclaimAmts = new BigDecimal[claimAmts.length];

          BigDecimal issueRetAmount = (billNo == null
              ? BigDecimal.ZERO
              : (rate.multiply(redQty)).subtract(discount.multiply(redQty)));

          if ((Integer) visitDetailsMap.get("plan_id") != 0) {
            if (issueBean.get("insurance_claim_amt") != null
                && ((BigDecimal) issueBean.get("insurance_claim_amt"))
                    .compareTo(BigDecimal.ZERO) > 0)
              // Return claim amount is the claim amount as per plan.
              for (int c = 0; c < finalclaimAmts.length; c++) {

                finalclaimAmts[c] = (claimAmts[i][c] == null || claimAmts[i][c].isEmpty())
                    ? BigDecimal.ZERO
                    : new BigDecimal(claimAmts[i][c]);
                claimAmt = finalclaimAmts[c].add(claimAmt);
              }
          } else {
            if (claimable) {
              // Return claim amount is same as claim amount if no plan.
              claimAmt = issueRetAmount;
            }
          }
          // Fetch the original item and set the qty, amount and claim amt.
          BasicDynaBean issuebean = StockUserIssueDAO
              .getIssueItemCharge(issueBean.get("item_issue_no").toString());
          StockUserReturnDAO.setIssueItemsForReturns(redQty, issueRetAmount, claimAmt,
              updateChargeList, issuebean);

          totalDisc = totalDisc.subtract(((BigDecimal) issuebean.get("discount")).multiply(redQty));
          for(ChargeDTO issueCharge: updateChargeList) {
            if(Boolean.TRUE.equals(issueCharge.getIsClaimLocked())){
              lockedClaimIssueCharges.add(issueCharge.getChargeId());
            }
          }

          // add to stock
          statusMap = stockFIFODAO.addStock(con, (Integer) mainBean.get("dept_to"),
              (Integer) issue.get("item_issue_no"), "U", redQty, (String) mainBean.get("username"),
              "UserReturns", null, "UR", itemReturnNo);

          if (!(Boolean) statusMap.get("transaction_lot_exists")) {// this is true if sales happened
                                                                   // before fifo
            success &= stockFIFODAO.addToEarlierStock(con,
                (Integer) detailBean.get("item_batch_id"), (Integer) mainBean.get("dept_to"),
                redQty);
          }

          success &= (Boolean) statusMap.get("status");

          // set cost value
          costValue = costValue.add((BigDecimal) statusMap.get("costValue"));
          retCostValue = (BigDecimal) issueBean.get("return_cost_value");

          issueBean.set("return_cost_value",
              retCostValue.add((BigDecimal) statusMap.get("costValue")));

          // Update original issue return_qty
          success &= stockIssueDAO.update(con, issueBean.getMap(), "item_issue_no",
              issue.get("item_issue_no")) > 0;
          if (pkgId > 0 && success) {
            BasicDynaBean patPkgItemConsumedBean = patPkgContConsumedDAO.findByKey(
                "prescription_id", issueBean.get("item_issue_no"));
            if (patPkgItemConsumedBean != null) {
              BigDecimal qtyConsumed = ((BigDecimal)issueBean.get("qty")).subtract((BigDecimal)
                  issueBean.get("return_qty"));
              patPkgItemConsumedBean.set("quantity", Double.valueOf(qtyConsumed.toString())
                  .intValue());
              success &= patPkgContConsumedDAO.update(con, patPkgItemConsumedBean.getMap(),
                  "patient_package_consumed_id", patPkgItemConsumedBean.
                  get("patient_package_consumed_id")) > 0;
            }
          }
          //always need latest issue billing group and hence letting overridden
          if (null != issuebean) {
            latestIssueBillingGrp = (int)issuebean.get("billing_group_id");
            issueReturnReferenceBean.set("issue_charge_id", issuebean.get("charge_id"));
            issueReturnReferenceBean.set("return_charge_id", chargeId);
          }

          //store original issue details
          issueReturnReferenceBean.set("patient_id", visitId);
          issueReturnReferenceBean.set("item_issue_no", issue.get("item_issue_no"));
          issueReturnReferenceBean.set("item_return_no", itemReturnNo);

          returnReferencesList.add(issueReturnReferenceBean);

        }

        // Insert return details
        detailBean.set("cost_value", costValue);
        success &= stockIssueReturnDetailsDAO.insert(con, detailBean);

        PreparedStatement ps = null;
        ArrayList<Object> itemdetails = new ArrayList<>();
        try {
          ps = con.prepareStatement(ITEM_DETAIL_LIST_QUERY + " and S.batch_no=? and S.medicine_id=?");
          ps.setString(1, (String) detailBean.get("batch_no"));
          ps.setInt(2, (Integer) detailBean.get("medicine_id"));
          itemdetails = DataBaseUtil.queryToArrayList(ps);
        } finally {
          if (ps != null) {
            ps.close();
          }
        }

        String billable = "";
        String issueType = "";

        for (int l = 0; l < itemdetails.size(); l++) {
          Hashtable table = (Hashtable) itemdetails.get(l);
          billable = table.get("BILLABLE").toString();
          issueType = table.get("ISSUE_TYPE").toString();
        }

        if (billNo != null && !billNo[0].isEmpty() && !issueType.equalsIgnoreCase("REUSABLE")) {

          if (billable.equalsIgnoreCase("t")) {
            BillDAO billDao = new BillDAO(con);
            Bill bill = billDao.getBill(billNo[0]);
            String billStatus = bill.getStatus();

            if (billStatus != null && !billStatus.equals("A")) {
              success = false;
              redirect.addParameter("message1", "Bill status is not open: cannot return items");
            } else {

              List<ChargeDTO> actChargeList = new ArrayList<ChargeDTO>();
              int subGroupId = 0;

              claimAmt = BigDecimal.ZERO;

              if (itemBean != null) {
                insuranceCategoryId = (Integer) itemBean.get("insurance_category_id");
                subGroupId = (Integer) itemBean.get("service_sub_group_id");

                if ((null != planId) && !(planId[0].equals("0"))) {
                  // Return claim amount is the claim amount as per plan.
                  if (patInsClaimAmt != null)
                    claimAmt = new BigDecimal(patInsClaimAmt[i]);
                } else {
                  if (claimable) {
                    // Return claim amount is same as claim amount if no plan.
                    claimAmt = amount;
                  }
                }
              }

              if (totalIssueQty.subtract((BigDecimal) detailBean.get("qty"))
                  .compareTo(BigDecimal.ZERO) < 0) {
                success = false;
                FlashScope flash = FlashScope.getScope(req);
                flash.put("error", "Insufficient sold quantity to set off against issue item: "
                    + itemBean.get("medicine_name"));
                redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
                return redirect;
              }

              // Create return charge
              visitType = bill.getVisitType();
              String patientId = bill.getVisitId();
              // Process request map and set tax details list.
              for (int j = 0; j < groupList.size(); j++) {
                BasicDynaBean groupBean = groupList.get(j);
                Map taxSubDetails = storeHelper.getTaxDetailsMap(reqMap, i,
                    (Integer) groupBean.get("item_group_id"));
                if (taxSubDetails.size() > 0) {
                  taxSubDetails.put("charge_id", chargeId);
                  taxDetailsList.add(taxSubDetails);
                }
              }
              chargeDTO = new ChargeDTO(ChargeDTO.CG_RETURNS, ChargeDTO.CH_INVENTORY_RETURNS, rate,
                  ((BigDecimal) detailBean.get("qty")).negate(),
                  ConversionUtils.setScale(discount.multiply(quantity).negate()), "",
                  detailBean.get("medicine_id").toString(), medicineName[i], null, false, 0,
                  subGroupId, insuranceCategoryId, visitType, patientId, null);
              chargeDTO.setBillNo(billNo[0]);
              chargeDTO.setChargeId(chargeId);
              chargeDTO.setActivityDetails("PHI", (Integer) detailBean.get("item_return_no"), "Y",
                  null);
              chargeDTO.setUsername((String) req.getSession(false).getAttribute("userid"));
              if (pkgId > 0) {
                chargeDTO.setActRemarks(pkgName);
                if (!isMultiPkg) {
                  chargeDTO.setChargeGroup(ChargeDTO.CG_PACKAGE);
                }
              } else {
                chargeDTO.setActRemarks((String) mainBean.get("reference"));
              }
              chargeDTO.setInsuranceClaimAmount(claimAmt.negate());
              chargeDTO.setAllowRateIncrease((Boolean) chargeHeadBean.get("allow_rate_increase"));
              chargeDTO.setAllowRateDecrease((Boolean) chargeHeadBean.get("allow_rate_decrease"));
              chargeDTO.setTaxAmt(taxAmt.negate());
              chargeDTO.setBillingGroupId(latestIssueBillingGrp);
              
              // multi-payer
              BigDecimal[] finalclaimAmts = new BigDecimal[claimAmts.length];
              for (int c = 0; c < finalclaimAmts.length; c++) {
                finalclaimAmts[c] = (claimAmts[i][c] == null || claimAmts[i][c].isEmpty())
                    ? BigDecimal.ZERO
                    : new BigDecimal(claimAmts[i][c]).negate();
              }
              // chargeDTO.setClaimAmounts(finalclaimAmts);

              /*
               * As part of insurance 3.0 returns flow is changed. We are setting insurance claim
               * amount and return insurance claim amount is zero for return entries, and setting
               * return insurance claim amount to zero for actual issue. So that it will work like
               * sales returns flow. --Anupama
               */
              BigDecimal[] zeroClaimAmts = new BigDecimal[claimAmts.length];
              for (int indx = 0; indx < zeroClaimAmts.length; indx++) {
                zeroClaimAmts[indx] = BigDecimal.ZERO;
              }
              chargeDTO.setClaimAmounts(zeroClaimAmts);
              if (pkgId > 0) {
                chargeDTO.setPackageId(pkgId);
                if (StringUtils.isNotBlank(pkgChargeIdRef)) {
                  chargeDTO.setChargeRef(pkgChargeIdRef);
                }
                totalAmount = totalAmount.add(chargeDTO.getAmount());
                totalDisc = totalDisc.add(chargeDTO.getDiscount());
                totalTax = totalTax.add(chargeDTO.getTaxAmt());
              }


              actChargeList.add(chargeDTO);

              for (ChargeDTO ret : actChargeList) {
                ret.setInsuranceClaimAmount(BigDecimal.ZERO);
                ret.setReturnInsuranceClaimAmt(BigDecimal.ZERO);
              }

              // Insert return charge into bill charge.
              success &= chargeDAO.insertCharges(actChargeList);

              if ( module != null &&
                  ((String)module.get("activation_status")).equals("Y")) {
                cacheIssueTransactions(cacheIssueTxns, mainBean, detailBean, visitDetailsMap,
                    billNo[0], loggedInCenterId, chargeDTO);
              }

              // Update return amounts against issue charge in bill charge.
              for (ChargeDTO iss : updateChargeList) {
                BigDecimal returnTaxAmtForIssue = taxAmt
                    .divide(qtyForTax, 3, BigDecimal.ROUND_HALF_DOWN).multiply(iss.getReturnQty());
                BigDecimal returnOriginalTaxAmtForIssue = originalTaxAmt
                    .divide(qtyForTax, 3, BigDecimal.ROUND_HALF_DOWN).multiply(iss.getReturnQty());
                iss.setReturnInsuranceClaimAmt(BigDecimal.ZERO);
                iss.setReturnTaxAmt(returnTaxAmtForIssue);
                iss.setReturnOriginalTaxAmt(returnOriginalTaxAmtForIssue);
              }
              success &= chargeDAO.updateSaleChargesWithTax(updateChargeList);

              // multi-payer
              success &= updateIssueReturnClaimDetails(con, patientId, actChargeList, billnostr);
            }
          }
        }

        // update indents
        if (patIndentNoRef != null && patIndentNoRef[0] != null && !patIndentNoRef[0].equals("")) {
          success &= new StoresPatientIndentDAO().updateIndentDetailsDispenseStatus(con,
              (String) mainBean.get("returned_by"), indentDisStatusMap,
              (BigDecimal) detailBean.get("qty"), medicineId[i],
              (Integer) detailBean.get("item_return_no"), "item_issue_no");
          success &= StoresPatientIndentDAO.updateIndentDispenseStatus(con,
              (String) mainBean.get("returned_by"));
        }
      }

      allSuccess = success;
      if (allSuccess) {
        allSuccess &= issueReturnReferencesDAO.insertAll(con, returnReferencesList);
        BasicDynaBean billChargeTaxBean;
        Iterator<Map<String, Object>> taxDetailsListIterator = taxDetailsList.iterator();
        while (taxDetailsListIterator.hasNext()) {
          Map<String, Object> taxDetails = taxDetailsListIterator.next();
          billChargeTaxBean = billChargeTaxDao.getBean();
          billChargeTaxBean.set("tax_rate", taxDetails.get("tax_rate"));
          List<String> groupCodes = StoresItemDAO
              .getGroupCodes((Integer) taxDetails.get("item_subgroup_id"));
          if (groupCodes != null && groupCodes.contains("KSACEX")
              && visitDetailsMap.get("nationality_id") != null
              && visitDetailsMap.get("nationality_id")
                  .equals(centerMasterDao.getCountryBean(loggedInCenterId).get("country_id"))) {
            billChargeTaxBean.set("tax_amount", (BigDecimal.ZERO));
          } else {
            billChargeTaxBean.set("tax_amount", ((BigDecimal) taxDetails.get("tax_amt")).negate());
          }
          billChargeTaxBean.set("original_tax_amt",
              ((BigDecimal) taxDetails.get("original_tax_amt")).negate());
          billChargeTaxBean.set("tax_sub_group_id", taxDetails.get("item_subgroup_id"));
          billChargeTaxBean.set("charge_id", taxDetails.get("charge_id"));
          allSuccess &= billChargeTaxDao.insert(con, billChargeTaxBean);
        }
      }

      if (allSuccess && pkgId > 0 && StringUtils.isNotBlank(pkgChargeIdRef)) {
        ChargeDTO pkgDTO = chargeDAO.getCharge(pkgChargeIdRef);
        BigDecimal pkgAmount = pkgDTO.getAmount();
        BigDecimal pkgDisc = pkgDTO.getDiscount();
        BigDecimal pkgTax = pkgDTO.getTaxAmt();
        BigDecimal pkgRate = pkgDTO.getActRate();
        pkgAmount = pkgAmount.subtract(totalAmount.subtract(totalDisc));
        pkgDisc = pkgDisc.subtract(totalDisc);
        pkgTax = pkgTax.subtract(totalTax);
        pkgRate = pkgRate.subtract(totalAmount);
        pkgDTO.setAmount(pkgAmount);
        pkgDTO.setDiscount(pkgDisc);
        pkgDTO.setTaxAmt(pkgTax);
        pkgDTO.setActRate(pkgRate);
        chargeDAO.updateCharge(pkgChargeIdRef, pkgDTO);
      }
      if (allSuccess && pkgId > 0 && patPkgId > 0) {
        updatePatientPkgStatus(con,patPkgId);
      }
    } catch (Exception ex){
      allSuccess = false;
      throw ex;
    }finally {
      DataBaseUtil.commitClose(con, allSuccess);
      if (!cacheIssueTxns.isEmpty() && module != null && allSuccess &&
          ((String)module.get("activation_status")).equals("Y")) {
        scmOutService.scheduleIssueReturnTxns(cacheIssueTxns);
      }
      if (allSuccess) {
        // update stock timestamp
        StockFIFODAO stockFIFODAO = new StockFIFODAO();
        stockFIFODAO.updateStockTimeStamp();
        stockFIFODAO.updateStoresStockTimeStamp((Integer) mainBean.get("dept_to"));
        if (billNo != null && billNo[0] != null && !billNo[0].equals("")) {
          BillDAO.resetTotalsOrReProcess(billNo[0]);
          
          // Call the allocation job and update the patient payments for the created bill.
          allocationService.updateBillTotal(billNo[0]);
          Integer centerId = (Integer) req.getSession().getAttribute("centerId");
          // Call the allocation method
          allocationService.allocate(billNo[0], centerId);
          
          // Calls the allocation job to update allocation for closed and finalized bills if it have inventory items.
          List<String> billNumber = BillDAO.getClosedAndFinalizedBillHavingChargeHead(visitId,"INVITE");
          for (String bnumber : billNumber) {
            allocationService.updateBillTotal(bnumber);
            allocationService.allocate(bnumber, centerId);
          }
        }
      }
      billChargeService.unlockChargeClaim(lockedClaimIssueCharges);
      new SponsorBO().recalculateSponsorAmount(visitId);
      billChargeService.lockChargeClaim(lockedClaimIssueCharges);
    }
    redirect.addParameter("returnNo", mainBean.get("user_return_no"));
    return redirect;
  }

  public ActionForward isSponsorBill(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse res)
      throws IOException, SQLException, ParseException {

    String billNo = request.getParameter("billNo");
    String medId = request.getParameter("medId");
    Boolean isInsurance = false;
    boolean claimable = false;
    BasicDynaBean itemBean = storeItemDetailsDAO.findByKey("medicine_id",
        Integer.parseInt(medId));
    if (itemBean != null) {
      BasicDynaBean cBean = storeCategoryMasterDAO.findByKey("category_id",
          itemBean.get("med_category_id"));
      if (cBean != null)
        claimable = (Boolean) cBean.get("claimable");
    }

    if (BillDAO.checkIfsponsorBill(billNo) && claimable) {
      isInsurance = true;
    }
    String resp = js.serialize(isInsurance);
    res.setContentType("text/plain");
    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    res.getWriter().write(resp);
    res.flushBuffer();
    return null;
  }

  /**
   * Sets insurance plan related details in the map
   * 
   * @param resultMap
   */
  private void setIssuedClaimDetails(HttpServletRequest req) throws SQLException {

    List<BasicDynaBean> visitPlans = patInsrPlanDao.getPlanDetails(req.getParameter("patient_id"));
    req.setAttribute("patient_plan_details",
        js.deepSerialize(ConversionUtils.listBeanToListMap(visitPlans)));
    req.setAttribute("visit_issued_claim_details", js.deepSerialize(ConversionUtils
        .listBeanToListMap(returnsDAO.getVisitIssuesClaimDetails(req.getParameter("patient_id")))));
  }

  /**
   * Inserts claim detaikls of issued return items
   * 
   * @param con
   * @param visitId
   * @param actChargeList
   * @return
   * @throws SQLException
   * @throws IOException
   */
  private boolean updateIssueReturnClaimDetails(Connection con, String visitId,
      List<ChargeDTO> actChargeList, String billNo) throws SQLException, IOException {
    List<BasicDynaBean> planList = insPlanDAO.getPlanDetails(con, visitId);
    boolean sucess = true;
    int[] planIds = new int[planList.size()];
    for (int j = 0; j < planList.size(); j++) {
      planIds[j] = (Integer) planList.get(j).get("plan_id");
    }
    if (planIds.length > 0)
      chgClaimDAO.insertBillChargeClaims(con, actChargeList, planIds, visitId, billNo);

    return sucess;
  }

  @IgnoreConfidentialFilters
  public ActionForward printPatientIssuesReturn(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws SQLException, IOException, Exception {

    PrintTemplatesDAO printTemplateDAO = new PrintTemplatesDAO();
    String returnNo = request.getParameter("returnNo");

    BasicDynaBean printprefs = PrintConfigurationsDAO
        .getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_STORE);
    List<BasicDynaBean> patientIssueReturnDetails = StockPatientIssueReturnsDAO
        .getPatientIssueReturnInfo(returnNo);
    Map ftlParams = new HashMap();
    ftlParams.put("patientIssueReturnList", patientIssueReturnDetails);

    PrintTemplate template = PrintTemplate.PatientIssueReturnPrintTemplate;

    String templateContent = printTemplateDAO.getCustomizedTemplate(template);

    Template t = null;
    if (templateContent == null || templateContent.equals("")) {
      t = AppInit.getFmConfig().getTemplate(template.getFtlName() + ".ftl");
    } else {
      StringReader reader = new StringReader(templateContent);
      t = new Template(null, reader, AppInit.getFmConfig());
    }

    HtmlConverter htmlConverter = new HtmlConverter();

    StringWriter writer = new StringWriter();
    t.process(ftlParams, writer);
    String printContent = writer.toString();
    if (printprefs.get("print_mode").equals("P")) {
      OutputStream os = response.getOutputStream();
      response.setContentType("application/pdf");
      htmlConverter.writePdf(os, printContent, "PatientIssueReturnPrintTemplate", printprefs, false,
          false, true, true, true, false);
    } else {
      String textReport = null;
      textReport = new String(htmlConverter.getText(printContent, "PatientIssueReturnPrintTemplate",
          printprefs, true, true));
      request.setAttribute("textReport", textReport);
      request.setAttribute("textColumns", printprefs.get("text_mode_column"));
      request.setAttribute("printerType", "DMP");
      return mapping.findForward("textPrintApplet");
    }

    return null;
  }

  private void cacheIssueTransactions(List<Map<String,Object>> cacheIssueTxns,
      BasicDynaBean issueMain, BasicDynaBean issueDetails, Map patient, String billNo,
      Integer centerId, ChargeDTO charge) throws SQLException {
    GenericDAO billChargeDAO = new GenericDAO("bill_charge");
    BasicDynaBean chargeBean = billChargeDAO.getBean();
    chargeBean.set("amount", charge.getAmount());
    chargeBean.set("discount", charge.getDiscount());
    Map<String, Object> data = scmOutService.getIssueReturnsMap(issueMain, issueDetails, patient,
        billNo, centerId, chargeBean);
    if(!data.isEmpty()) {
      cacheIssueTxns.add(data);
    }
  }

  public ActionForward getPkgIssuedItems(ActionMapping am, ActionForm af, HttpServletRequest req,
      HttpServletResponse res) throws SQLException, IOException {

    String visitId = req.getParameter("visit_id");
    Integer packageId = Integer.parseInt((String) req.getParameter("package_id"));
    Integer patPkgId = Integer.parseInt((String) req.getParameter("patPkgId"));
    HashMap<String,Object> responseMap = new HashMap<>();

    if (packageId > 0 && patPkgId > 0) {
      List<BasicDynaBean> issuedItems = returnsDAO.getPkgIssuedItemDetails(visitId, packageId, patPkgId);
      responseMap.put("pkgItemsList", ConversionUtils.listBeanToMapListMap(issuedItems, "dept_id"));
    }

    res.setContentType("text/javascript");
    js.deepSerialize(responseMap, res.getWriter());

    return null;
  }

  protected void updatePatientPkgStatus(Connection con,Integer patPkgId) throws SQLException {
    GenericDAO patPkgContentsDAO = new GenericDAO("patient_package_contents");
    List<BasicDynaBean> patPkgContents = patPkgContentsDAO
        .findAllByKey(con, "patient_package_id", patPkgId);
    List<BasicDynaBean> patPkgContentsConsumed = patpkgDao
        .getPatPkgContentsConsumed(con,patPkgId);

    String status = "C";
    if (patPkgContents.size() != patPkgContentsConsumed.size()) {
      status = "P";
    } else {
      for (BasicDynaBean patientPackageContentConsumed : patPkgContentsConsumed) {
        if (!patientPackageContentConsumed.get("content_qty").toString()
            .equals(patientPackageContentConsumed.get("consumed_qty").toString())) {
          status = "P";
        }
      }
    }

    GenericDAO patPackagesDAO = new GenericDAO("patient_packages");
    BasicDynaBean patPkgBean = patPackagesDAO.findByKey(con,"pat_package_id", patPkgId);
    if (patPkgBean != null) {
      patPkgBean.set("status", status);
      try {
        patPackagesDAO.update(con, patPkgBean.getMap(), "pat_package_id", patPkgId);
      } catch (IOException| SQLException ex) {
        log.error("Failed while updating patient_packages status : ", ex);
      }
    }

  }
}
