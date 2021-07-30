package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.Registration.PatientInsurancePlanDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.billing.Bill;
import com.insta.hms.billing.BillActivityCharge;
import com.insta.hms.billing.BillChargeClaimDAO;
import com.insta.hms.billing.BillChargeTaxDAO;
import com.insta.hms.billing.BillDAO;
import com.insta.hms.billing.ChargeDAO;
import com.insta.hms.billing.ChargeDTO;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.core.inventory.StoresHelper;
import com.insta.hms.integration.scm.inventory.ScmOutBoundInvService;
import com.insta.hms.insurance.AdvanceInsuranceCalculator;
import com.insta.hms.insurance.SponsorBO;
import com.insta.hms.master.Accounting.ChargeHeadsDAO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.HealthAuthorityPreferences.HealthAuthorityPreferencesDAO;
import com.insta.hms.master.PlanMaster.PlanMasterDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.PrintTemplates.PrintTemplate;
import com.insta.hms.master.PrintTemplates.PrintTemplatesDAO;
import com.insta.hms.modules.ModulesDAO;

import flexjson.JSONSerializer;
import freemarker.template.Template;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * The Class StockUserIssueAction.
 */
public class StockUserIssueAction extends DispatchAction {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(StockUserIssueAction.class);

  /** The js. */
  static JSONSerializer js = new JSONSerializer().exclude("class");

  /** The pan master DAO. */
  private static PlanMasterDAO panMasterDAO = new PlanMasterDAO();

  /** The pat insr plan dao. */
  private static PatientInsurancePlanDAO patInsrPlanDao = new PatientInsurancePlanDAO();

  /** The ins plan DAO. */
  private static PatientInsurancePlanDAO insPlanDAO = new PatientInsurancePlanDAO();

  /** The chg claim DAO. */
  private static BillChargeClaimDAO chgClaimDAO = new BillChargeClaimDAO();

  /** The store patient indent dao. */
  private static StoresPatientIndentDAO storePatientIndentDao = new StoresPatientIndentDAO();

  private static ModulesDAO modulesDao = new ModulesDAO();
  private static ScmOutBoundInvService scmOutService = ApplicationContextProvider
      .getBean(ScmOutBoundInvService.class);
  
  private static final GenericDAO genericPreferencesDAO = new GenericDAO("generic_preferences");
  private static final GenericDAO storesDAO = new GenericDAO("stores");
  private static final GenericDAO storeHospUserDAO = new GenericDAO("store_hosp_user");
  private static final GenericDAO storeConsignmentInvoiceDAO = new GenericDAO("store_consignment_invoice");
  private static final GenericDAO organizationDetailsDAO = new GenericDAO("organization_details");
  private static final GenericDAO stockIssueDetailsDAO = new GenericDAO("stock_issue_details");
  private static final GenericDAO stockIssueMainDAO = new GenericDAO("stock_issue_main");
  private static final GenericDAO storeGatePassDAO = new GenericDAO("store_gatepass");


  /**
   * DPV-- 11/08/2010 -- This query has changed during Stores Integration. Not using inventory
   * tables anymore, refering to pharmacy tables instead. These pharma tables contains consolidated
   * data of both types
   */

  private static final String ITEM_DETAIL_LIST_QUERY_WITH_INS_FIELDS = "    SELECT sibd.batch_no,"
      + "s.medicine_id,c.identification, COALESCE(sir.tax_rate,COALESCE(ssir.tax_rate,p.tax_rate))"
      + " as tax_rate, COALESCE(sir.tax_type,COALESCE(ssir.tax_type,p.tax_type)) as tax_type,"
      + " p.medicine_name, CASE WHEN issue_rate_expr IS NOT NULL THEN 'Y' ELSE 'N' END AS"
      + " is_markup_rate, (CASE " + " WHEN c.issue_type='C'  THEN 'CONSUMABLE'"
      + " WHEN c.issue_type='L'  THEN 'REUSABLE'" + " WHEN c.issue_type='P'  THEN 'PERMANENT' "
      + " WHEN c.issue_type='R' THEN 'RETAILABLE'"
      + " END ) AS issue_type ,s.dept_id,sibd.item_batch_id,"
      + " COALESCE(sibd.exp_dt,NULL) AS exp_dt,c.billable,s.qty,p.package_type, "
      + " p.issue_base_unit,c.category_id,"
      + " s.consignment_stock, p.issue_units,c.billable,COALESCE(c.discount,0) AS"
      + " meddisc,p.tax_type, p.item_barcode_id, CASE "
      + " WHEN ipd.insurance_payable = 'N' THEN 0::numeric ELSE ipd.patient_amount "
      + " END AS patient_amount, " + " CASE  "
      + " WHEN ipd.insurance_payable = 'N' THEN 100::numeric ELSE ipd.patient_percent "
      + " END AS patient_percent, " + " CASE "
      + " WHEN ipd.insurance_payable = 'N' THEN 0::numeric ELSE ipd.patient_amount_per_category "
      + " END AS patient_amount_per_category, "
      + " ipd.patient_amount_cap, p.insurance_category_id, true AS first_of_category, "
      + " p.package_uom,issue_rate_expr,  p.control_type_id, sict.control_type_name, "
      + " ssir.selling_price_expr as visit_selling_expr, sir.selling_price_expr as"
      + " store_selling_expr ";

  /** The Constant ITEM_DETAIL_LIST_QUERY_WITH_INS_TABLES. */
  private static final String ITEM_DETAIL_LIST_QUERY_WITH_INS_TABLES = "  FROM"
      + " (SELECT medicine_id,sum(qty) as qty,item_batch_id,batch_no,sum(qty_in_use) as"
      + " qty_in_use,sum(qty_maint) as qty_maint, sum(qty_retired) as qty_retired,sum(qty_lost)"
      + " as qty_lost,sum(qty_kit) as qty_kit, sum(qty_unknown) as qty_unknown,"
      + " consignment_stock,asset_approved,dept_id FROM store_stock_details  "
      + " GROUP BY batch_no,item_batch_id,medicine_id,consignment_stock,asset_approved,dept_id "
      + " ORDER BY medicine_id) as s " + " JOIN store_item_batch_details sibd USING(item_batch_id)"
      + " JOIN store_item_details p ON (p.medicine_id = s.medicine_id)"
      + " JOIN store_category_master c ON(c.category_id =p.med_category_id)  "
      + " LEFT JOIN store_item_controltype sict ON(sict.control_type_id = p.control_type_id) "
      + " LEFT JOIN store_item_issue_rates siir ON (siir.medicine_id = s.medicine_id) "
      + " LEFT JOIN store_item_rates ssir ON (p.medicine_id = ssir.medicine_id AND "
      + " ssir.store_rate_plan_id = ?) "
      + " LEFT JOIN store_item_rates sir ON( sir.medicine_id = s.medicine_id AND "
      + " sir.store_rate_plan_id = ?)   LEFT OUTER JOIN ( SELECT pd.*, insurance_payable  "
      + " FROM insurance_plan_details pd"
      + " JOIN item_insurance_categories iic  ON (pd.insurance_category_id="
      + "  iic.insurance_category_id) and plan_id = ? and   patient_type = ? "
      + " ) AS ipd ON ipd.insurance_category_id=p.insurance_category_id ";

  /** The Constant ITEM_DETAIL_LIST_QUERY_WITH_INS_AND_PAT_AMT_ZERO_FIELDS_MRP. */
  private static final String ITEM_DETAIL_LIST_QUERY_WITH_INS_AND_PAT_AMT_ZERO_FIELDS_MRP =
      " coalesce(sibd.mrp,0) as mrp ";

  /** The Constant ITEM_DETAIL_LIST_QUERY_WITH_INS_AND_PAT_AMT_ZERO_FIELDS. */
  private static final String ITEM_DETAIL_LIST_QUERY_WITH_INS_AND_PAT_AMT_ZERO_FIELDS =
      "SELECT sibd.batch_no,s.medicine_id,c.identification,"
      + " COALESCE(sir.tax_rate,COALESCE(ssir.tax_rate,p.tax_rate)) as tax_rate,"
      + " COALESCE(sir.tax_type,COALESCE(ssir.tax_type,p.tax_type)) as tax_type,p.medicine_name, "
      + " CASE " + " WHEN issue_rate_expr IS NOT NULL THEN 'Y' ELSE 'N'" + " END AS is_markup_rate,"
      + " (CASE "
      + " WHEN c.issue_type='C'  THEN 'CONSUMABLE' WHEN c.issue_type='L'  THEN 'REUSABLE' "
      + " WHEN c.issue_type='P'  THEN 'PERMANENT' WHEN c.issue_type='R' THEN 'RETAILABLE' "
      + " END ) AS issue_type ,s.dept_id,sibd.item_batch_id,"
      + " COALESCE(sibd.exp_dt,NULL) AS exp_dt,c.billable,s.qty,p.package_type,"
      + " p.issue_base_unit,c.category_id,"
      + " s.consignment_stock, p.issue_units,c.billable,COALESCE(c.discount,0) AS meddisc,"
      + " p.tax_type, p.item_barcode_id,0::NUMERIC AS patient_amount_per_category, CASE"
      + " WHEN ipd.insurance_payable = 'N' THEN 0::numeric ELSE ipd.patient_amount"
      + " END AS patient_amount," + " CASE WHEN ipd.insurance_payable = 'N' THEN 100::numeric"
      + " ELSE ipd.patient_percent END AS patient_percent, "
      + " ipd.patient_amount_cap, p.insurance_category_id, false AS first_of_category,"
      + " p.package_uom, issue_rate_expr, p.control_type_id, sict.control_type_name, "
      + " ssir.selling_price_expr as visit_selling_expr, sir.selling_price_expr as"
      + " store_selling_expr, ";

  /** The Constant ITEM_DETAIL_LIST_QUERY_WITH_INS_AND_PAT_AMT_ZERO_TABLES. */
  private static final String ITEM_DETAIL_LIST_QUERY_WITH_INS_AND_PAT_AMT_ZERO_TABLES = "  FROM "
      + " (SELECT medicine_id,sum(qty) as qty,item_batch_id,batch_no,sum(qty_in_use) as"
      + " qty_in_use,sum(qty_maint) as qty_maint, sum(qty_retired) as qty_retired,sum(qty_lost)"
      + " as qty_lost,sum(qty_kit) as qty_kit, sum(qty_unknown) as qty_unknown,consignment_stock,"
      + " asset_approved,dept_id FROM store_stock_details  GROUP BY batch_no,item_batch_id,"
      + " medicine_id,consignment_stock,asset_approved,dept_id ORDER BY medicine_id) as s "
      + " JOIN store_item_batch_details sibd USING(item_batch_id)"
      + " JOIN store_item_details p ON (p.medicine_id = s.medicine_id)"
      + " JOIN store_category_master c ON(c.category_id =p.med_category_id)"
      + " LEFT JOIN store_item_controltype sict ON(sict.control_type_id = p.control_type_id) "
      + " LEFT JOIN store_item_issue_rates siir ON (siir.medicine_id = s.medicine_id) "
      + " LEFT JOIN store_item_rates ssir ON (p.medicine_id = ssir.medicine_id AND "
      + " ssir.store_rate_plan_id = ?) "
      + " LEFT JOIN  store_item_rates sir ON( sir.medicine_id = s.medicine_id AND"
      + " sir.store_rate_plan_id = ?) LEFT OUTER JOIN " + " ( SELECT pd.*, insurance_payable"
      + " FROM insurance_plan_details pd " + " JOIN item_insurance_categories iic "
      + " ON (pd.insurance_category_id= iic.insurance_category_id "
      + " and plan_id = ? and patient_type = ? )"
      + " ) AS ipd ON ipd.insurance_category_id=p.insurance_category_id ";

  /** The Constant ITEM_DETAIL_LIST_QUERY. */
  private static final String ITEM_DETAIL_LIST_QUERY = " SELECT s.medicine_id,c.identification,"
      + " coalesce(sibd.mrp,0) as mrp,"
      + " COALESCE(sir.tax_rate,COALESCE(ssir.tax_rate,p.tax_rate)) as tax_rate,"
      + "  COALESCE(sir.tax_type,COALESCE(ssir.tax_type,p.tax_type)) as tax_type,p.medicine_name,"
      + " CASE" + " WHEN issue_rate_expr IS NOT NULL THEN 'Y' ELSE 'N'" + " END AS is_markup_rate,"
      + " (CASE"
      + " WHEN c.issue_type='C'  THEN 'CONSUMABLE' WHEN c.issue_type='L'  THEN 'REUSABLE' "
      + " WHEN c.issue_type='P'  THEN 'PERMANENT' WHEN c.issue_type='R' THEN 'RETAILABLE' "
      + " END ) AS issue_type ,s.dept_id,sibd.batch_no,sibd.item_batch_id,"
      + " COALESCE(sibd.exp_dt,NULL) AS exp_dt,c.billable,s.qty,p.package_type,"
      + " p.issue_base_unit,c.category_id," + " s.consignment_stock, p.issue_units,c.billable,"
      + " COALESCE(c.discount,0) AS meddisc,p.tax_type, "
      + " p.item_barcode_id,'' as patient_amount, '' as patient_amount_per_category,"
      + " '' as patient_percent, '' as patient_amount_cap ,"
      + " p.insurance_category_id, true AS first_of_category, "
      + " p.package_uom,issue_rate_expr, p.control_type_id, sict.control_type_name, "
      + " ssir.selling_price_expr as visit_selling_expr, sir.selling_price_expr"
      + " as store_selling_expr ";

  /** The Constant ITEM_DETAIL_LIST_QUERY_TABELS. */
  private static final String ITEM_DETAIL_LIST_QUERY_TABELS =
      " FROM (SELECT medicine_id,sum(qty) as qty,item_batch_id,batch_no,sum(qty_in_use) as"
      + " qty_in_use,sum(qty_maint) as qty_maint,  sum(qty_retired) as qty_retired,"
      + " sum(qty_lost) as qty_lost,sum(qty_kit) as qty_kit, "
      + "  sum(qty_unknown) as qty_unknown,consignment_stock,asset_approved,dept_id "
      + "  FROM store_stock_details GROUP BY batch_no,item_batch_id,medicine_id,"
      + " consignment_stock,asset_approved,dept_id  ORDER BY medicine_id) as s "
      + " JOIN store_item_batch_details sibd USING(item_batch_id)"
      + " JOIN store_item_details p ON (p.medicine_id = s.medicine_id)"
      + " LEFT JOIN store_item_issue_rates siir ON (siir.medicine_id = s.medicine_id)"
      + " JOIN store_category_master c ON(c.category_id =p.med_category_id) "
      + " LEFT JOIN store_item_controltype sict ON(sict.control_type_id = p.control_type_id) "
      + " LEFT JOIN store_item_rates ssir ON (p.medicine_id = ssir.medicine_id AND "
      + " ssir.store_rate_plan_id = ?) ";

  /** The Constant ITEM_SELLING_PRICE_BATCH_MRP_NO_MRP. */
  private static final String ITEM_SELLING_PRICE_BATCH_MRP_NO_MRP = "  ,"
      + " coalesce(COALESCE(p.item_selling_price, sibd.mrp),0) as mrp ";

  /** The Constant ITEM_SELLING_PRICE_BATCH_MRP_NO. */
  private static final String ITEM_SELLING_PRICE_BATCH_MRP_NO = " ,COALESCE("
      + " COALESCE(p.item_selling_price, sibd.mrp),0) as orig_mrp, "
      + " ((coalesce(COALESCE(p.item_selling_price, sibd.mrp),0))/p.issue_base_unit) "
      + " as unit_mrp,  COALESCE(p.item_selling_price, sibd.mrp) as selling_price ";

  /** The Constant ITEM_SELLING_PRICE_BATCH_MRP_YES_MRP. */
  private static final String ITEM_SELLING_PRICE_BATCH_MRP_YES_MRP = " ,coalesce("
      + " COALESCE(ssir.selling_price," + "  COALESCE(p.item_selling_price, sibd.mrp))"
      + " ,0) as mrp";

  /** The Constant ITEM_SELLING_PRICE_BATCH_MRP_YES. */
  private static final String ITEM_SELLING_PRICE_BATCH_MRP_YES = " ,COALESCE(COALESCE("
      + " p.item_selling_price, sibd.mrp),0) as orig_mrp, "
      + " ((coalesce(COALESCE(p.item_selling_price, sibd.mrp),0))/p.issue_base_unit)"
      + " as unit_mrp,  COALESCE(p.item_selling_price, sibd.mrp) as selling_price ";

  /** The Constant ITEM_DETAIL_LIST_QUERY_WHERE. */
  private static final String ITEM_DETAIL_LIST_QUERY_WHERE = "  WHERE s.asset_approved='Y' ";

  /** The Constant STORE_ITEM_RATES_JOIN. */
  private static final String STORE_ITEM_RATES_JOIN = " LEFT JOIN store_item_rates sir"
      + " ON( sir.medicine_id = s.medicine_id  AND sir.store_rate_plan_id = ?)";

  /** The Constant KIT_LIST. */
  private static final String KIT_LIST = " SELECT distinct(kit_name),kit_id,kit_type "
      + " FROM store_kit_stock_main " + " JOIN store_kit_main using(kit_id)  "
      + " WHERE issued='N' and status='A'";

  /** The Constant KIT_DETAILS. */
  private static final String KIT_DETAILS = " SELECT kit_name,kit_identifier "
      + " FROM store_kit_main " + " JOIN store_kit_stock_main using(kit_id)";

  /**
   * Gets the stock issue screen.
   *
   * @param am
   *          the am
   * @param af
   *          the af
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the stock issue screen
   * @throws Exception
   *           the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getStockIssueScreen(ActionMapping am, ActionForm af, HttpServletRequest req,
      HttpServletResponse res) throws Exception {
    Connection con = null;
    PreparedStatement ps = null;

    try {
      con = DataBaseUtil.getConnection();
      String msg = req.getParameter("message");

      if (msg != null) {
        req.setAttribute("message", msg);
      } else {
        req.setAttribute("message", "0");
      }

      HttpSession session = req.getSession(false);
      String storeId = (String) session.getAttribute("pharmacyStoreId");
      int roleId = (Integer) session.getAttribute("roleId");
      if (storeId != null && !storeId.equals("")) {
        BasicDynaBean store = storesDAO.findByKey("dept_id",
            Integer.parseInt(storeId));
        req.setAttribute("store_id", storeId);
        String storeName = store.get("dept_name").toString();
        req.setAttribute("store_name", storeName);
      }
      if (storeId != null && storeId.equals("")) {
        req.setAttribute("store_id", storeId);
      }
      if (roleId == 1 || roleId == 2) {
        if (storeId != null && !storeId.equals("")) {
          req.setAttribute("store_id", storeId);
        } else {
          req.setAttribute("store_id", 0);
        }
      }
      String hospitalName = genericPreferencesDAO.getRecord().get("hospital_name")
          .toString();
      req.setAttribute("hospital", hospitalName);
      if (req.getParameter("gtpass") != null) {
        req.setAttribute("gtpass", req.getParameter("gtpass"));
      } else {
        req.setAttribute("gtpass", false);
      }

      String transaction = req.getParameter("_transaction");
      List groupItemDetails = null;
      if (transaction != null) {
        List<HashMap> items = new ArrayList();
        HashMap itemMap = null;

        Map<String, String[]> params = req.getParameterMap();
        String[] selected = params.get("_selected");
        String[] medicineId = params.get("_medicine_id");
        String[] deptId = params.get("_dept_id");
        String[] batchNo = params.get("_batchno");

        for (int i = 0; i < medicineId.length; i++) {
          if (selected[i].equals("Y")) {
            itemMap = new HashMap();
            itemMap.put("itemId", medicineId[i]);
            itemMap.put("itemIdentifier", batchNo[i]);
            items.add(itemMap);
          }
        }

        BasicDynaBean storeBean = StoreDAO.findByStore(Integer.parseInt(deptId[0]));
        groupItemDetails = StockUserIssueDAO.getGroupItemDetails(
            ITEM_DETAIL_LIST_QUERY + ITEM_DETAIL_LIST_QUERY_TABELS, deptId[0], items,
            (Integer) storeBean.get("store_rate_plan_id"));
        req.setAttribute("groupStoreId", deptId[0]);
        BasicDynaBean grpStore = storesDAO.findByKey("dept_id",
            Integer.parseInt(deptId[0]));
        req.setAttribute("grpStoreItem_unit", grpStore != null ? grpStore.get("sale_unit") : "I");
        req.setAttribute("type", req.getParameter("_type"));
        req.setAttribute("groupItemDetails",
            js.serialize(ConversionUtils.copyListDynaBeansToMap(groupItemDetails)));
      } else {
        req.setAttribute("groupItemDetails", js.serialize(groupItemDetails));
      }

      req.setAttribute("hospuserlist", js.serialize(ConversionUtils.copyListDynaBeansToMap(
          storeHospUserDAO.listAll(null, "status", "A", "hosp_user_name"))));
      req.setAttribute("stock_negative_sale",
          genericPreferencesDAO.getRecord().getMap().get("stock_negative_sale"));
      req.setAttribute("flag", req.getParameter("flag"));
      req.setAttribute("msg", req.getParameter("msg"));
      req.setAttribute("sale_margin", genericPreferencesDAO.getRecord().getMap()
          .get("pharmacy_sale_margin_in_per"));
      req.setAttribute("stock_timestamp", MedicineStockDAO.getMedicineTimestamp());

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return am.findForward("addshow");
  }

  /**
   * Gets the patient issue screen.
   *
   * @param am
   *          the am
   * @param af
   *          the af
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the patient issue screen
   * @throws Exception
   *           the exception
   */
  public ActionForward getPatientIssueScreen(ActionMapping am, ActionForm af,
      HttpServletRequest req, HttpServletResponse res) throws Exception {
    /**
     * RC:We should not used DB operations in Action class.
     */
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();

      String msg = req.getParameter("message");
      // RC:Call generic preference dao to get bean at once and use it where it needed.
      String hospitalName = genericPreferencesDAO.getRecord(con)
          .get("hospital_name").toString();
      // RC:Better to set all set attribute at one place.
      req.setAttribute("hospital", hospitalName);
      // RC:Initialize all variables at one place.
      if (msg != null) {
        req.setAttribute("message", msg);
      } else {
        req.setAttribute("message", "0");
      }
      // RC:Didn't see any use of this field.
      String msg1 = req.getParameter("message1");

      if (msg1 != null) {
        req.setAttribute("info", msg1);
      }
      if (req.getParameter("gtpass") != null) {
        req.setAttribute("gtpass", req.getParameter("gtpass"));
      } else {
        req.setAttribute("gtpass", false);
      }

      HttpSession session = req.getSession(false);
      String storeId = (req.getParameter("indentStore") == null
          ? (String) session.getAttribute("pharmacyStoreId")
          : req.getParameter("indentStore"));
      int roleId = (Integer) session.getAttribute("roleId");
      if (storeId != null && !storeId.equals("")) {
        BasicDynaBean store = storesDAO.findByKey("dept_id",
            Integer.parseInt(storeId));
        String storeName = store.get("dept_name").toString();
        req.setAttribute("store_name", storeName);
        req.setAttribute("store_id", storeId);
      }
      if (storeId != null && storeId.equals("")) {
        req.setAttribute("store_id", storeId);
      }
      if (roleId == 1 || roleId == 2) {
        if (storeId != null && !storeId.equals("")) {
          req.setAttribute("store_id", storeId);
        } else {
          req.setAttribute("store_id", 0);
        }
      }
      String visitId = "";
      visitId = req.getParameter("visitId");
      String billNo = req.getParameter("billNo");
      if ((null != visitId) && (!(visitId.equals("")))) {
        req.setAttribute("visitIdFromBill", visitId);
      }
      if ((null != billNo) && (!(billNo.equals("")))) {
        req.setAttribute("billNo", billNo);
      }
      /**
       * RC:Didn't see any use of this block of code we can remove this. Same block of code need to
       * be cleaned in js file.
       */
      String transaction = req.getParameter("_transaction");
      List groupItemDetails = null;
      if (transaction != null) {
        List<HashMap> items = new ArrayList();
        HashMap itemMap = null;
        String[] select = req.getParameterValues("_select");

        Map<String, String[]> params = req.getParameterMap();
        String[] selected = params.get("_selected");
        String[] medicineId = params.get("_medicine_id");
        String[] deptId = params.get("_dept_id");
        String[] batchNo = params.get("_batchno");

        for (int i = 0; i < medicineId.length; i++) {
          if (selected[i].equals("Y")) {
            itemMap = new HashMap();
            itemMap.put("itemId", medicineId[i]);
            itemMap.put("itemIdentifier", batchNo[i]);
            items.add(itemMap);
          }
        }

        BasicDynaBean storeBean = StoreDAO.findByStore(Integer.parseInt(deptId[0]));
        groupItemDetails = StockUserIssueDAO.getGroupItemDetails(
            ITEM_DETAIL_LIST_QUERY + ITEM_DETAIL_LIST_QUERY_TABELS, deptId[0], items,
            (Integer) storeBean.get("store_rate_plan_id"));
        req.setAttribute("groupStoreId", deptId[0]);
        req.setAttribute("type", req.getParameter("_type"));
        req.setAttribute("groupItemDetails",
            js.serialize(ConversionUtils.copyListDynaBeansToMap(groupItemDetails)));

        BasicDynaBean grpStore = storesDAO.findByKey("dept_id",
            Integer.parseInt(deptId[0]));
        req.setAttribute("grpStoreItem_unit", grpStore != null ? grpStore.get("sale_unit") : "I");

      } else {
        req.setAttribute("groupItemDetails", js.serialize(groupItemDetails));
      }
      // RC:Better to use dao class to get user list.
      // RC:Not needed this is for issue to user not for patient.
      req.setAttribute("hospuserlist", js.serialize(
          ConversionUtils.copyListDynaBeansToMap(storeHospUserDAO.listAll())));
      // RC:Instead of calling multiple times better we can call once and use it where it needed.
      req.setAttribute("stock_negative_sale",
          genericPreferencesDAO.getRecord().getMap().get("stock_negative_sale"));
      req.setAttribute("action_id", am.getActionId());
      // RC:Instead of calling multiple times better we can call once and use it where it needed.
      req.setAttribute("sale_margin", genericPreferencesDAO.getRecord().getMap()
          .get("pharmacy_sale_margin_in_per"));
      // RC:Better to call dao class for this.
      req.setAttribute("orgDiscounts",
          js.serialize(ConversionUtils.copyListDynaBeansToMap(organizationDetailsDAO.listAll(null, "status", "A"))));
      // RC:Better to have a clean flash message mechanism here instead setting flags. and using
      // msg.
      req.setAttribute("flag", req.getParameter("flag"));
      req.setAttribute("msg", req.getParameter("msg"));
      req.setAttribute("stock_timestamp", MedicineStockDAO.getMedicineTimestamp());

      String username = (String) session.getAttribute("userid");
      // RC:Better to call dao class for this.
      BasicDynaBean ubean = new GenericDAO("u_user").findByKey("emp_username", username);
      if (ubean != null) {
        req.setAttribute("isSharedLogIn", ubean.get("is_shared_login"));
      }
      req.setAttribute("actionId", am.getProperty("action_id"));
      // RC:Instead of calling multiple times better we can call once and use it where it needed.
      BasicDynaBean prefs = GenericPreferencesDAO.getAllPrefs();
      req.setAttribute("prefs", prefs.getMap());

      req.setAttribute("operation_details_id", req.getParameter("operation_details_id"));
      req.setAttribute("fromOTScreen", req.getParameter("fromOTScreen"));
      // RC:Didn't see any use of this.
      String fromOTScreen = req.getParameter("fromOTScreen");
      if (null != fromOTScreen && !fromOTScreen.equals("") && fromOTScreen.equals("Y")) {
        if (null != visitId && !visitId.equals("")) {
          req.setAttribute("visitIdForOT", visitId);
        } else {
          req.setAttribute("visitIdForOT", req.getParameter("visitIdForOT"));
        }
      }

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    List<String> orderKitDetailsList = new ArrayList<String>();
    orderKitDetailsList.add("order_kit_id");
    orderKitDetailsList.add("order_kit_name");
    // RC:Better to call dao class for this.
    req.setAttribute("orderkitJSON", js.deepSerialize(ConversionUtils.copyListDynaBeansToMap(
        new GenericDAO("order_kit_main").listAll(orderKitDetailsList, "status", "A"))));
    // RC:Better to call dao class for this.
    req.setAttribute("discountPlansJSON", js.serialize(ConversionUtils
        .listBeanToListMap(new GenericDAO("discount_plan_details").listAll(null, "priority"))));
    return am.findForward("addshow");
  }

  /**
   * Gets the item details.
   *
   * @param am
   *          the am
   * @param af
   *          the af
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the item details
   * @throws Exception
   *           the exception
   */
  public ActionForward getItemDetails(ActionMapping am, ActionForm af, HttpServletRequest req,
      HttpServletResponse res) throws Exception {
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      res.setContentType("text/plain");
      res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
      String stockNegativeSale = (String) genericPreferencesDAO.getRecord().getMap()
          .get("stock_negative_sale");
      String itemName = req.getParameter("item_name");
      String storeId = req.getParameter("storeid");
      String planId = req.getParameter("planId");
      String visitType = req.getParameter("visitType");
      String visitId = req.getParameter("visitId");
      String storeRatePlanId = req.getParameter("store_rate_plan_id");
      Boolean firstOfCategory = true;

      BasicDynaBean storeBean = StoreDAO.findByStore(Integer.parseInt(storeId));

      if (stockNegativeSale.equalsIgnoreCase("D")) { // stock negative sale no
        if ((null != planId) && (!(planId.equals("0")))) { // insurence visit

          List<BasicDynaBean> stckBean = (List) new StockUserIssueDAO(con)
              .getItemsAndDetailsWithIns(
                  getItemDetailsQuery(true, false, false,
                      ((String) storeBean.get("use_batch_mrp")).equals("Y")),
                  itemName, storeId, planId, visitType, storeRatePlanId);
          for (BasicDynaBean h : stckBean) {
            firstOfCategory = VisitDetailsDAO.getIsFirstOfCategory(con, visitId,
                (Integer) h.get("insurance_category_id"));
          }
          if (firstOfCategory) {
            res.getWriter()
                .write(js.serialize(ConversionUtils
                    .copyListDynaBeansToMap(new StockUserIssueDAO(con).getItemsAndDetailsWithIns(
                        getItemDetailsQuery(true, false, true,
                            ((String) storeBean.get("use_batch_mrp")).equals("Y")),
                        itemName, storeId, planId, visitType, storeRatePlanId))));
          } else {

            res.getWriter()
                .write(js.serialize(ConversionUtils
                    .copyListDynaBeansToMap(new StockUserIssueDAO(con).getItemsAndDetailsWithIns(
                        getItemDetailsQuery(true, false, false,
                            ((String) storeBean.get("use_batch_mrp")).equals("Y")),
                        itemName, storeId, planId, visitType, storeRatePlanId))));
          }
        } else if (null != visitId) {
          res.getWriter()
              .write(js.serialize(ConversionUtils
                  .copyListDynaBeansToMap(new StockUserIssueDAO(con).getItemsAndDetails(
                      getItemDetailsQuery(false, false, false,
                          ((String) storeBean.get("use_batch_mrp")).equals("Y")),
                      itemName, storeId, storeRatePlanId))));
        } else {
          res.getWriter()
              .write(js.serialize(ConversionUtils
                  .copyListDynaBeansToMap(new StockUserIssueDAO(con).getItemsAndDetails(
                      getItemDetailsQuery(false, false, false,
                          ((String) storeBean.get("use_batch_mrp")).equals("Y")),
                      itemName, storeId, "0"))));
        }
      } else {
        if ((null != planId) && (!(planId.equals("0")))) {
          List<BasicDynaBean> stckBean = (List) new StockUserIssueDAO(con)
              .getItemsAndDetailsWithIns(
                  getItemDetailsQuery(true, true, false,
                      ((String) storeBean.get("use_batch_mrp")).equals("Y")),
                  itemName, storeId, planId, visitType, storeRatePlanId);
          for (BasicDynaBean h : stckBean) {
            firstOfCategory = VisitDetailsDAO.getIsFirstOfCategory(con, visitId,
                (Integer) h.get("insurance_category_id"));
          }
          if (firstOfCategory) {
            res.getWriter()
                .write(js.serialize(ConversionUtils
                    .copyListDynaBeansToMap(new StockUserIssueDAO(con).getItemsAndDetailsWithIns(
                        getItemDetailsQuery(true, true, true,
                            ((String) storeBean.get("use_batch_mrp")).equals("Y")),
                        itemName, storeId, planId, visitType, storeRatePlanId))));
          } else {
            res.getWriter()
                .write(js.serialize(ConversionUtils
                    .copyListDynaBeansToMap(new StockUserIssueDAO(con).getItemsAndDetailsWithIns(
                        getItemDetailsQuery(true, true, false,
                            ((String) storeBean.get("use_batch_mrp")).equals("Y")),
                        itemName, storeId, planId, visitType, storeRatePlanId))));
          }
        } else if (null != visitId) {
          res.getWriter()
              .write(js.serialize(ConversionUtils
                  .copyListDynaBeansToMap(new StockUserIssueDAO(con).getItemsAndDetails(
                      getItemDetailsQuery(false, true, false,
                          ((String) storeBean.get("use_batch_mrp")).equals("Y")),
                      itemName, storeId, storeRatePlanId))));
        } else {
          res.getWriter()
              .write(js.serialize(ConversionUtils
                  .copyListDynaBeansToMap(new StockUserIssueDAO(con).getItemsAndDetails(
                      getItemDetailsQuery(false, true, false,
                          ((String) storeBean.get("use_batch_mrp")).equals("Y")),
                      itemName, storeId, "0"))));
        }
      }

      res.flushBuffer();
      return null;
    } finally {
      if (con != null) {
        con.close();
      }
    }
  }

  /*
   * public ActionForward getKitDetails(ActionMapping am, ActionForm af, HttpServletRequest req,
   * HttpServletResponse res) throws Exception{ Connection con = null; try{ con =
   * DBUtil.getConnection(); res.setContentType("text/plain");
   * res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
   * res.getWriter().write(js.serialize(ConversionUtils.copyListDynaBeansToMap(DBUtil.
   * queryToDynaList(KIT_DETAILS+" where kit_name='"+req.getParameter("kit_name")+"' and issued='N'"
   * )))); res.flushBuffer(); return null; }finally{ if(con != null)con.close(); } }
   */

  /**
   * Save items issued.
   *
   * @param am
   *          the am
   * @param af
   *          the af
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the action forward
   * @throws Exception
   *           the exception
   */
  public ActionForward saveItemsIssued(ActionMapping am, ActionForm af, HttpServletRequest req,
      HttpServletResponse res) throws Exception {
    BasicDynaBean userdynaBean = null;
    BasicDynaBean usermaindynaBean = null;
    BasicDynaBean invmaindynaBean = null;
    BasicDynaBean pkgDetails = null;
    ResultSet rs = null;
    String issuedTo = null;
    String wardNo = null;
    boolean result = true;
    int newIssueId = 0;
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

    String tranType = req.getParameter("tran_type");
    String issueDate = req.getParameter("issueDate");
    String issueTime = req.getParameter("issueTime");

    Map itemsMap = req.getParameterMap();
    String[] itemNames = (String[]) itemsMap.get("item_name");

    for (int i = 0; i < itemNames.length - 1; i++) {
      itemNames[i] = URLDecoder.decode(itemNames[i], "UTF-8");
    }

    int itemSize = itemNames.length - 1;

    String[] itemIdentifiers = (String[]) itemsMap.get("item_identifier");
    String[] origUnitMrp = (String[]) itemsMap.get("origUnitMrpHid");
    String[] unitMrp = (String[]) itemsMap.get("unitMrpHid");
    String[] discountAmt = (String[]) itemsMap.get("discountAmtHid");
    String[] amt = (String[]) itemsMap.get("amt");
    String[] insClaimAmts = (String[]) itemsMap.get("patIncClaimAmt");
    String[] insCategory = (String[]) itemsMap.get("insurancecategory");
    String[] itemBatchId = (String[]) itemsMap.get("itemBatchId");
    String[] pkgmrp = (String[]) itemsMap.get("pkgmrp");
    String[] issueType = (String[]) itemsMap.get("issueType");

    String[] editedRate = (String[]) itemsMap.get("mrpHid");
    String[] userEntereddiscountRate = (String[]) itemsMap.get("discountHid");
    String[] priInsClaimAmts = (String[]) itemsMap.get("pri_patIncClaimAmt");
    String[] secInsClaimAmts = (String[]) itemsMap.get("sec_patIncClaimAmt");
    String[][] claimAmts = new String[priInsClaimAmts.length][priInsClaimAmts.length];;

    /** Check if this is being called from Patient Issue or Stock User Issue */
    if ((null != tranType) && (tranType.equalsIgnoreCase("Patient"))) {
      mrno = (String[]) itemsMap.get("mrno");
      for (int i = 0; i < priInsClaimAmts.length; i++) {
        claimAmts[i][0] = priInsClaimAmts[i];
        claimAmts[i][1] = secInsClaimAmts[i];
      }
    } else {
      hospUser = (String[]) itemsMap.get("issued_to");
    }

    String[] issueReason = (String[]) itemsMap.get("reason");
    String userType = "";
    String[] itemCheck = (String[]) itemsMap.get("hdeleted");
    String[] itemorkits = (String[]) itemsMap.get("itemorkit");
    String[] stype = (String[]) itemsMap.get("stype");
    String gt = req.getParameter("gatepass");
    String salemargin = GenericPreferencesDAO.getGenericPreferences().getPharmacySaleMargin();
    String saleType = GenericPreferencesDAO.getGenericPreferences().getStockNegativeSale();
    String[] itemUnit = (String[]) itemsMap.get("itemUnit");
    String[] pkgSize = (String[]) itemsMap.get("pkgUnit");
    String[] disStatus = (String[]) itemsMap.get("dispense_status");
    String[] patientIndentNoRef = (String[]) itemsMap.get("patient_indent_no_ref");
    Integer centerId = -1;
    BasicDynaBean module = modulesDao.findByKey("module_id", "mod_scm");
    List<Map<String, Object>> cacheIssueTxns = new ArrayList<>();

    Map<String, String> indentDisStatusMap = new HashMap<String, String>();
    if (patientIndentNoRef != null) { // possible in case of user issue
      for (int i = 0; i < patientIndentNoRef.length; i++) {
        if (patientIndentNoRef[i].isEmpty()) {
          continue;
        }
        indentDisStatusMap.put(patientIndentNoRef[i], disStatus[i]);
      }
    }
    ActionRedirect redirect = null;

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
    String billable = null;
    String categoryId = null;

    Connection con = null;
    ChargeDTO chargeDTO;
    ChargeDAO chargeDAO = new ChargeDAO(con);
    BillActivityCharge activityDTO = new BillActivityCharge();
    String gropName = ChargeDTO.CG_INVENTORY;
    String headName = ChargeDTO.CH_INVENTORY_ITEM;
    String patientId = null;
    String bedType = null;
    ChargeHeadsDAO chargeHeadDAO = new ChargeHeadsDAO();

    boolean isnegative = true;
    HttpSession session = req.getSession(false);
    String isShared = req.getParameter("isSharedLogIn");
    String userName = isShared == null
        ? (String) session.getAttribute("userid")
        : isShared.equals("Y")
            ? req.getParameter("authUser")
            : (String) session.getAttribute("userid");

    if (mrno != null && mrno.length > 0) {
      // The patient details tab now returns patient id instead of mrno
      patientId = mrno[0];
    }
    if (patientId != null) {
      BasicDynaBean pd = VisitDetailsDAO.getPatientVisitDetailsBean(patientId);
      bedType = pd.get("bill_bed_type").toString();
      patOrgID = pd.get("org_id").toString();

      ratePlanBean = organizationDetailsDAO.findByKey("org_id", patOrgID);
      ratePlanDiscount = (BigDecimal) ratePlanBean.get("pharmacy_discount_percentage");
      ratePlanDiscType = (String) ratePlanBean.get("pharmacy_discount_type");
    }

    String[] stores = (String[]) itemsMap.get("storeId");
    BasicDynaBean storeBean = StoreDAO.findByStore(Integer.parseInt(stores[0]));
    int storeRatePlanId = (storeBean.get("store_rate_plan_id") == null
        ? 0
        : (Integer) storeBean.get("store_rate_plan_id"));
    int visitRtoreRatePlanId = (ratePlanBean == null
        || ratePlanBean.get("store_rate_plan_id") == null
            ? 0
            : (Integer) ratePlanBean.get("store_rate_plan_id"));

    String[] billNO = (String[]) itemsMap.get("bill_no");
    for (int i = 0; i < issueQty.length - 1; i++) {
      issueQtyconv[i] = ConversionUtils.setScale(new BigDecimal(issueQty[i]));
    }
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      con.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
      /** If called from patient issue, redirect to patient issue, else to stock user issue */

      if (mrno != null && mrno.length > 0) {
        redirect = new ActionRedirect("StockPatientIssue.do");
        redirect.addParameter("_method", "getPatientIssueScreen");
      } else {
        redirect = new ActionRedirect("StockUserIssue.do");
        redirect.addParameter("_method", "getStockIssueScreen");
      }

      if (mrno != null && mrno.length > 0) {
        userType = "Patient";
        issuedTo = mrno[0];
      } else {
        userType = (issueType != null && issueType[0] != null && issueType[0].equals("d")
            ? "Dept"
            : "Ward");
        // get hosp_user_id for already existing user if not creats a user and brings the id
        if (issueType != null && issueType[0] != null && issueType[0].equals("u")) {
          result = new StockUserIssueDAO(con).checkUser(hospUser[0]);
          userType = "Hospital";
        }
        if (result) {
          issuedTo = hospUser[0];
        }
      }

      newIssueId = Integer
          .parseInt(new StockUserIssueDAO(con).getSequenceId("store_issue_sequence"));
      int k1 = 0;
      if (itemorkits[0].equalsIgnoreCase("kit")) {
        kitIdentifier = itemIdentifiers[0];
        List kitDetails = new StockUserIssueDAO(con).getKitDetails(itemIdentifiers[0]);
        itemNames = new String[kitDetails.size()];
        itemIdentifiers = new String[kitDetails.size()];
        issueQtyconv = new BigDecimal[kitDetails.size()];
        for (int kit = 0; kit < kitDetails.size(); kit++) {
          Hashtable table = (Hashtable) kitDetails.get(kit);
          itemNames[kit] = (String) table.get("MEDICINE_NAME");
          itemIdentifiers[kit] = (String) table.get("BATCH_NO");
          issueQtyconv[kit] = new BigDecimal((String) table.get("QTY"));
        }
        itemSize = itemNames.length;
      }

      for (int i = 0; i < itemSize; i++) {
        itemissueid = Integer
            .parseInt(new StockUserIssueDAO(con).getSequenceId("store_issue_details_sequence"));
        boolean deletedrow = false;
        if (itemCheck != null) {
          if (itemCheck[i].equals("true")) {
            deletedrow = true;
          } else {
            deletedrow = false;
          }
        }
        if (!deletedrow) {

          String planId = "";
          String visitType = "";
          Hashtable item = null;
          String medicineId = "";
          try (PreparedStatement ps = con.prepareStatement(
              ITEM_DETAIL_LIST_QUERY + ITEM_DETAIL_LIST_QUERY_TABELS + STORE_ITEM_RATES_JOIN
              + ITEM_DETAIL_LIST_QUERY_WHERE + " and S.batch_no=? and S.medicine_id=? ")) {
            ps.setInt(1, visitRtoreRatePlanId);
            ps.setInt(2, storeRatePlanId);
            ps.setString(3, itemIdentifiers[i]);
            medicineId = new StockUserIssueDAO(con).getItemId(itemNames[i]);
            ps.setInt(4, Integer.parseInt(medicineId));
  
            item = (Hashtable) DataBaseUtil.queryToArrayList(ps).get(0);
          }
          billable = item.get("BILLABLE").toString();

          pkgDetails = StockUserIssueDAO.getPackageMrpAndCP(Integer.parseInt(medicineId),
              itemIdentifiers[i]);

          BigDecimal rate = null;
          BigDecimal origRate = null;
          BigDecimal discount = null;
          BigDecimal amount = null;
          BigDecimal claim = null;
          int insuranceCategoryId = 0;

          if (billable != null && billable.equalsIgnoreCase("t") && !billNO[0].equalsIgnoreCase("C")
              && !billNO[0].isEmpty()) {
            rate = ConversionUtils.setScale(new BigDecimal(unitMrp[i]));
            origRate = ConversionUtils.setScale(new BigDecimal(origUnitMrp[i]));
            discount = ConversionUtils.setScale(new BigDecimal(discountAmt[i]));
            amount = ConversionUtils.setScale(new BigDecimal(amt[i]));
            claim = ConversionUtils.setScale(new BigDecimal(priInsClaimAmts[i]));
            insuranceCategoryId = Integer.parseInt(insCategory[i]);

          }

          // set bean of stock_issue_details table
          userdynaBean = stockIssueDetailsDAO.getBean();
          userdynaBean.set("user_issue_no", new BigDecimal(newIssueId));
          userdynaBean.set("medicine_id", Integer.parseInt(medicineId));
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
          userdynaBean.set("pkg_mrp",
              ((pkgmrp != null && pkgmrp[i] != null && !pkgmrp[i].isEmpty())
                  ? new BigDecimal(pkgmrp[i])
                  : BigDecimal.ZERO));
          userdynaBean.set("item_unit", itemUnit[i]);
          userdynaBean.set("issue_pkg_size",
              (pkgSize[i] != null && !pkgSize[i].equals(""))
                  ? ConversionUtils.setScale(new BigDecimal(pkgSize[i]))
                  : BigDecimal.ONE);

          if (userType.equalsIgnoreCase("Patient")) {
            userdynaBean.set("insurance_claim_amt", claim);
          }

          usermaindynaBean = stockIssueMainDAO.getBean();
          // set bean of stock_issue_main table
          usermaindynaBean.set("user_issue_no", new BigDecimal(newIssueId));
          if (issueDate != null && !issueDate.equals("")) {
            if (issueTime != null && !issueTime.equals("")) {
              usermaindynaBean.set("date_time", DateUtil.parseTimestamp(issueDate, issueTime));
            } else {
              usermaindynaBean.set("date_time", DateUtil.parseTimestamp(issueDate, new DateUtil()
                  .getTimeFormatter().format(DateUtil.getCurrentTimestamp()).toString()));
            }
          } else {
            usermaindynaBean.set("date_time", DataBaseUtil.getDateandTime());
          }

          usermaindynaBean.set("dept_from", Integer.parseInt(stores[0]));
          usermaindynaBean.set("user_type", userType);
          usermaindynaBean.set("issued_to", issuedTo);
          usermaindynaBean.set("reference", issueReason[0]);

          usermaindynaBean.set("username", userName);
          BasicDynaBean bedBean = null;
          BasicDynaBean patBean = new GenericDAO("patient_registration").findByKey("patient_id",
              issuedTo);
          int bedId = 0;
          String patType = null;
          if (patBean != null) {
            patType = (String) patBean.get("visit_type");
          }

          if (patType != null && patType.equals("i")) {
            bedBean = new GenericDAO("admission").findByKey("patient_id", issuedTo);
          }

          if (bedBean != null) {
            bedId = (Integer) bedBean.get("bed_id");
          }

          BasicDynaBean wardBean = new GenericDAO("bed_names").findByKey("bed_id", bedId);

          if (wardBean != null) {
            wardNo = (String) wardBean.get("ward_no");
          }

          usermaindynaBean.set("ward_no", wardNo);
          if (gatepass == true) {
            BasicDynaBean gtBean = storeGatePassDAO.getBean();
            int gatePassId = StoresSupplierReturnsDAO.getStoreGatePassNextId();
            usermaindynaBean.set("gatepass_id", gatePassId);
            String gatePassNo = "G".concat(Integer.toString(gatePassId));
            gtBean.set("gatepass_id", gatePassId);
            gtBean.set("gatepass_no", gatePassNo);
            if (userType.equals("Patient")) {
              gtBean.set("txn_type", "Issue to Patient");
            } else {
              gtBean.set("txn_type", "Issue to Hospital");
            }
            gtBean.set("created_date", DateUtil.getCurrentTimestamp());
            gtBean.set("dept_id", Integer.parseInt(stores[0]));
            result = storeGatePassDAO.insert(con, gtBean);
            if (result) {
              gtpassId = gatePassId;
            }
          }

          // consignment invoice code

          if (conInsertion && isnegative) {
            if (stype[i].equalsIgnoreCase("true")) {
              String query = "select (total_qty-ing.issue_qty) as qty,grn_no,invoice_no,"
                  + " supplier_id,billable,supplier_invoice_id "
                  + " from store_grn_details ing join store_grn_main using (grn_no)"
                  + " join store_invoice using(supplier_invoice_id)"
                  + " join store_item_details using(medicine_id) join store_category_master"
                  + "  on (category_id=med_category_id) where medicine_id=? and batch_no=? and"
                  + " (total_qty-ing.issue_qty) > 0 order by grn_date asc";
              try (PreparedStatement ps = con.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE,
                  ResultSet.CONCUR_READ_ONLY)) {
                ps.setInt(1, Integer.parseInt(medicineId));
                ps.setString(2, itemIdentifiers[i]);
                rs = ps.executeQuery();
                boolean newIssueQty = true;
                BigDecimal issqty = null;
                BigDecimal dedQty = null;
                while (rs.next()) {
                  String invoiceNo = rs.getString("invoice_no");
                  String suppId = rs.getString("supplier_id");
                  int suppInvId = rs.getInt("supplier_invoice_id");
  
                  if (newIssueQty) {
                    issqty = new BigDecimal(issueQty[i]);
                  }
  
                  invmaindynaBean = storeConsignmentInvoiceDAO.getBean();
                  invmaindynaBean.set("supplier_invoice_id", suppInvId);
                  // invmaindynaBean.set("invoice_no",invoiceNo);
                  String grnNo = rs.getString("grn_no");
                  invmaindynaBean.set("grn_no", grnNo);
                  invmaindynaBean.set("con_invoice_date", DataBaseUtil.getDateandTime());
                  invmaindynaBean.set("amount_payable", new BigDecimal("0"));
                  invmaindynaBean.set("status", "O");
                  invmaindynaBean.set("username", userName);
                  invmaindynaBean.set("issue_id", newIssueId);
                  invmaindynaBean.set("medicine_id", Integer.parseInt(medicineId));
                  invmaindynaBean.set("batch_no", itemIdentifiers[i]);
                  if (billable != null && billable.equalsIgnoreCase("t")) {
                    invmaindynaBean.set("bill_no", billNO[0]);
                  } else {
                    invmaindynaBean.set("bill_no", "");
                  }
                  String rateQuery = "select round(package_cp/issue_base_unit,2) as rate from"
                      + " store_stock_details join store_item_details using(medicine_id)"
                      + " where medicine_id=' " + medicineId + "' and batch_no='" + itemIdentifiers[i]
                      + "' and dept_id='" + stores[0] + "'";
  
                  invmaindynaBean.set("amount", amount);
                  BigDecimal qty = rs.getBigDecimal("qty");
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
                    if (storeConsignmentInvoiceDAO.insert(con, invmaindynaBean)) {
                      result = new StockUserIssueDAO(con).updateGRNQTY(grnNo,
                          Integer.parseInt(medicineId), itemIdentifiers[i], dedQty);
                    } else {
                      result = false;
                    }
                  }
                  newIssueQty = false;
                  if (remQty.intValue() == 0 || issqty.intValue() <= 0 || remQty.intValue() > 0) {
                    break;
                  }
  
                }
            } finally {
              if (rs != null) {
                rs.close();
              }
            }

            }

          }

          if (result && k1 == 0 && isnegative) {
            if (stockIssueMainDAO.insert(con, usermaindynaBean)) {
              k1 = 1;
              if (itemorkits[0].equalsIgnoreCase("kit")) {
                if (new StockUserIssueDAO(con).updateKit(kitIdentifier)) {
                  result = true;
                  k1 = 1;
                } else {
                  result = false;
                }
              }
            } else {
              result = false;
              k1 = 1;
            }
          }
          if (result && isnegative) {

            StockFIFODAO stockFIFODAO = new StockFIFODAO();

            // reducing stock
            Map statusMap = stockFIFODAO.reduceStock(con, Integer.parseInt(stores[0]),
                Integer.parseInt(itemBatchId[i]), "U", issueQtyconv[i],
                (itemorkits[0].equals("kit") ? issueQtyconv[i] : null),
                (String) req.getSession(false).getAttribute("userid"), "UserIssue",
                (Integer) userdynaBean.get("item_issue_no"));

            result &= (Boolean) statusMap.get("status");

            // set total cost value
            userdynaBean.set("cost_value", statusMap.get("costValue"));
            result &= stockIssueDetailsDAO.insert(con, userdynaBean);

            Map<String, Object> patientMap = new HashMap<>();

            // if(new
            // StockUserReturnDAO(con).updateStock(Integer.parseInt(medicine_id),
            // Integer.parseInt(itemBatchId[i]),issueQtyconv[i].floatValue(),
            // Integer.parseInt(stores[0]),1,itemorkits[0], userName)){

            if (billable != null && billable.equalsIgnoreCase("t")) {
              if (!billNO[0].equalsIgnoreCase("C") && !billNO[0].isEmpty()) {

                BillDAO billDao = new BillDAO(con);
                String billNo = billNO[0];
                Bill bill = billDao.getBill(billNo);

                String billStatus = bill.getStatus();
                if (billStatus != null && !billStatus.equals("A")) {
                  result = false;
                  redirect.addParameter("message1",
                      "Bill : " + billNo + " is not open: cannot issue items");
                } else {
                  // setting revenue a/c group a/c to store wise preferences
                  BasicDynaBean store = storesDAO.findByKey("dept_id",
                      Integer.parseInt(stores[0]));

                  chargeId = chargeDAO.getNextChargeId();
                  if (storeBean != null) {
                    centerId = (Integer) store.get("center_id");
                  }
                  BasicDynaBean masterItemBean = new PharmacymasterDAO().findByKey("medicine_id",
                      new Integer(medicineId));

                  visitType = bill.getVisitType();
                  String visitId = bill.getVisitId();

                  chargeDTO = new ChargeDTO(gropName, headName, rate, issueQtyconv[i], discount, "",
                      medicineId, itemNames[i], null, false, 0,
                      (Integer) masterItemBean.get("service_sub_group_id"), 0, visitType, visitId,
                      null);

                  chargeDTO.setBillNo(billNO[0]);
                  chargeDTO.setChargeId(chargeId);
                  chargeDTO.setOriginalRate(origRate);
                  chargeDTO.setActivityDetails("PHI", itemissueid, "Y", null);
                  chargeDTO.setUsername(userName);
                  chargeDTO.setUserRemarks(issueReason[0]);
                  int accountGroup = (Integer) store.get("account_group");
                  chargeDTO.setAccount_group(accountGroup);
                  chargeDTO.setInsuranceClaimAmount(claim);
                  chargeDTO.setInsuranceCategoryId(insuranceCategoryId);
                  chargeDTO.setActRemarks("No. " + newIssueId);
                  BasicDynaBean chargeHeadBean = chargeHeadDAO.findByKey("chargehead_id", headName);
                  chargeDTO
                      .setAllowRateDecrease((Boolean) chargeHeadBean.get("allow_rate_decrease"));
                  chargeDTO
                      .setAllowRateIncrease((Boolean) chargeHeadBean.get("allow_rate_increase"));

                  // multi-payer
                  BigDecimal[] finalclaimAmts = new BigDecimal[claimAmts.length];
                  for (int c = 0; c < finalclaimAmts.length; c++) {
                    finalclaimAmts[c] = claimAmts[i][c] != null
                        ? new BigDecimal(claimAmts[i][c])
                        : BigDecimal.ZERO;
                  }
                  chargeDTO.setClaimAmounts(finalclaimAmts);

                  if (issueDate != null && !issueDate.equals("")) {
                    if (issueTime != null && !issueTime.equals("")) {
                      chargeDTO.setPostedDate(DateUtil.parseTimestamp(issueDate, issueTime));
                    } else {
                      usermaindynaBean.set("date_time",
                          DateUtil.parseTimestamp(issueDate, new DateUtil().getTimeFormatter()
                              .format(DateUtil.getCurrentTimestamp()).toString()));
                    }

                  } else {
                    chargeDTO.setPostedDate(DataBaseUtil.getDateandTime());
                  }
                  String healthAuthority = HealthAuthorityPreferencesDAO
                      .getHealthAuthorityPreferences(
                          CenterMasterDAO.getHealthAuthorityForCenter(centerId))
                      .getHealth_authority();
                  String[] drugCodeTypes = HealthAuthorityPreferencesDAO
                      .getHealthAuthorityPreferences(healthAuthority).getDrug_code_type();
                  BasicDynaBean itemCodeBean = StoreItemCodesDAO
                      .getDrugCodeType(Integer.parseInt(medicineId), drugCodeTypes);
                  if (itemCodeBean != null) {
                    if (itemCodeBean.get("code_type") != null) {
                      chargeDTO.setCodeType((String) itemCodeBean.get("code_type"));
                    }
                    if (itemCodeBean.get("item_code") != null) {
                      chargeDTO.setActRatePlanItemCode((String) itemCodeBean.get("item_code"));
                    }
                  }
                  String allowZeroClaimfor = (String) masterItemBean.get("allow_zero_claim_amount");
                  if (visitType.equalsIgnoreCase(allowZeroClaimfor)
                      || "b".equals(allowZeroClaimfor)) {
                    chargeDTO.setAllowZeroClaim(true);
                  }
                  List actChargeList = new ArrayList();
                  actChargeList.add(chargeDTO);

                  if (new ChargeDAO(con).insertCharges(actChargeList)) {
                    result &= true;
                  } else {
                    result = false;
                  }

                  if (module != null && result &&
                      ((String)module.get("activation_status")).equals("Y")) {
                    cacheIssueTransactions(cacheIssueTxns, usermaindynaBean,
                      userdynaBean, patientMap, billNO[0], centerId, chargeDTO);
                  }

                  // multi-payer
                  result &= updateIssueClaimDetails(con, visitId, actChargeList, billNO[0]);
                }
              } else {
                result &= true;
              }
            } else {
              result &= true;
            }
            // update indent dispense status

            boolean itemWithIndent = false;
            // List<String> medicineIds = new ArrayList<>();

            for (String indentNo : indentDisStatusMap.keySet()) {
              List<String> medicineIds = storePatientIndentDao.getMedicinesForIndent(con, indentNo);
              if (storePatientIndentDao.getMedicinesForIndent(con, indentNo).contains(medicineId)) {
                itemWithIndent = true;
                break;
              }
            }

            if (indentDisStatusMap.size() > 0 && itemWithIndent) {

              result &= storePatientIndentDao.updateIndentDetailsDispenseStatus(con, patientId,
                  indentDisStatusMap, (BigDecimal) userdynaBean.get("qty"), medicineId,
                  (Integer) userdynaBean.get("item_issue_no"), "item_issue_no");

              if (!result) {
                break;
              }

              // if user selects Close All as dispense status,we ll update dispense status of the
              // indent to 'C' even if its not dispensed
              for (String key : new HashSet<String>(indentDisStatusMap.keySet())) {
                if (indentDisStatusMap.get(key).equals("all")) {
                  result &= storePatientIndentDao.closeAllIndents(con, key);
                }
              }

              result &= StoresPatientIndentDAO.updateIndentDispenseStatus(con, patientId);
              // I represents 'Issue' in process_type column of store_patient_indent_main table
              result &= StoresPatientIndentDAO.updateProcessType(con, patientId, "I");

            }
          } else {
            result = false;
          }
          if (saleType.equalsIgnoreCase("D")) {
            boolean qtyAvailable = EditStockDetailsDAO.checkQtyAvailable(con, stores[0], medicineId,
                Integer.parseInt(itemBatchId[i]), issueQtyconv[i]);

            if (!qtyAvailable) {
              con.rollback();
              redirect.addParameter("flag", false);
              redirect.addParameter("msg",
                  "Stock not available for : " + itemNames[i] + "  -  " + itemIdentifiers[i] + "");
              return redirect;
            }
          }
        }
      }

      if (result && isnegative) {
        msg = "Successfully Issued Items";
        redirect.addParameter("message", newIssueId);
        con.commit();
        if (!cacheIssueTxns.isEmpty() && module != null &&
            ((String)module.get("activation_status")).equals("Y")) {
          scmOutService.scheduleIssueTxns(cacheIssueTxns);
        }

        // update stock timestamp
        StockFIFODAO stockFIFODAO = new StockFIFODAO();
        stockFIFODAO.updateStockTimeStamp();
        stockFIFODAO.updateStoresStockTimeStamp(Integer.parseInt(stores[0]));

        if (userType != null && userType.equals("Patient")) {
          if (billNO != null && billNO[0] != null && !billNO[0].equals("")) {
            BillDAO.resetTotalsOrReProcess(billNO[0]);
          }
        }

      } else {
        msg = "Failed to Issue";
        redirect.addParameter("message", null);
        log.error(msg);
        if (con != null) {
          con.rollback();
        }
      }
    } finally {

      if (con != null) {
        con.close();
      }
    }
    FlashScope flash = FlashScope.getScope(req);
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    flash.info(msg);

    if (null != patientId) {
      new SponsorBO().recalculateSponsorAmount(patientId);
    }

    if (redirect.getParameterString().indexOf("message") == -1) {
      redirect.addParameter("message", newIssueId);
    }
    redirect.addParameter("gtpass", gatepass);
    if (billNO != null && billNO[0] != null) {
      redirect.addParameter("billNo", billNO[0]);
    }
    redirect.addParameter("fromOTScreen", req.getParameter("fromOTScreen"));
    redirect.addParameter("operation_details_id", req.getParameter("operation_details_id"));
    redirect.addParameter("visitIdForOT", patientId);
    return redirect;
  }

  /**
   * Save patient items issued.
   *
   * @param am
   *          the am
   * @param af
   *          the af
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the action forward
   * @throws Exception
   *           the exception
   */
  public ActionForward savePatientItemsIssued(ActionMapping am, ActionForm af,
      HttpServletRequest req, HttpServletResponse res) throws Exception {
    BasicDynaBean userdynaBean = null;
    BasicDynaBean usermaindynaBean = null;
    BasicDynaBean invmaindynaBean = null;
    BasicDynaBean pkgDetails = null;
    ResultSet rs = null;
    String issuedTo = null;
    String wardNo = null;
    boolean result = true;
    int newIssueId = 0;
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

    String tranType = "Patient";
    String issueDate = req.getParameter("issueDate");
    String issueTime = req.getParameter("issueTime");

    Map itemsMap = req.getParameterMap();
    String[] itemNames = (String[]) itemsMap.get("item_name");

    for (int i = 0; i < itemNames.length - 1; i++) {
      itemNames[i] = URLDecoder.decode(itemNames[i], "UTF-8");
    }

    int itemSize = itemNames.length - 1;

    String[] itemIdentifiers = (String[]) itemsMap.get("item_identifier");
    String[] origUnitMrp = (String[]) itemsMap.get("origUnitMrpHid");
    String[] unitMrp = (String[]) itemsMap.get("unit_mrp");
    String[] discountAmt = (String[]) itemsMap.get("discountAmtHid");
    String[] amt = (String[]) itemsMap.get("amt");
    String[] insCategory = (String[]) itemsMap.get("insurancecategory");
    String[] itemBatchId = (String[]) itemsMap.get("item_batch_id");
    String[] pkgmrp = (String[]) itemsMap.get("pkg_mrp");
    String[] issueType = (String[]) itemsMap.get("issueType");
    String[] taxAmt = (String[]) itemsMap.get("tax_amt");

    String[] editedRate = (String[]) itemsMap.get("mrpHid");
    String[] userEntereddiscountRate = (String[]) itemsMap.get("discountHid");
    String[] priInsClaimAmts = (String[]) itemsMap.get("pri_ins_amt");
    String[] secInsClaimAmts = (String[]) itemsMap.get("sec_ins_amt");
    String[][] claimAmts = new String[priInsClaimAmts.length][secInsClaimAmts.length];
    BillChargeTaxDAO billChargeTaxDao = new BillChargeTaxDAO();

    /** Check if this is being called from Patient Issue or Stock User Issue */
    if ((null != tranType) && (tranType.equalsIgnoreCase("Patient"))) {
      mrno = (String[]) itemsMap.get("mrno");
      for (int i = 0; i < priInsClaimAmts.length; i++) {
        if (priInsClaimAmts[i] != null && !priInsClaimAmts[i].trim().isEmpty()) {
          claimAmts[i][0] = priInsClaimAmts[i];
        }
        if (secInsClaimAmts[i] != null && !secInsClaimAmts[i].trim().isEmpty()) {
          claimAmts[i][1] = secInsClaimAmts[i];
        }
      }
    } else {
      hospUser = (String[]) itemsMap.get("issued_to");
    }

    String[] issueReason = (String[]) itemsMap.get("reason");
    String userType = "";
    String[] itemCheck = (String[]) itemsMap.get("hdeleted");
    String[] stype = (String[]) itemsMap.get("stype");
    String gt = req.getParameter("gatepass");
    String saleType = GenericPreferencesDAO.getGenericPreferences().getStockNegativeSale();
    String salemargin = GenericPreferencesDAO.getGenericPreferences().getPharmacySaleMargin();
    String[] itemUnit = (String[]) itemsMap.get("item_unit");
    String[] pkgSize = (String[]) itemsMap.get("pkg_unit");
    String[] disStatus = (String[]) itemsMap.get("dispense_status");
    String[] patientIndentNoRef = (String[]) itemsMap.get("patient_indent_no_ref");
    List<BasicDynaBean> groupList = PurchaseOrderDAO.getAllGroups();
    StoresHelper storeHelper = new StoresHelper();
    ArrayList<Map<String, Object>> taxDetailsList = new ArrayList<Map<String, Object>>();
    Integer centerId = -1;

    Map<String, String> indentDisStatusMap = new HashMap<String, String>();
    if (patientIndentNoRef != null) { // possible in case of user issue
      for (int i = 0; i < patientIndentNoRef.length; i++) {
        if (patientIndentNoRef[i].isEmpty()) {
          continue;
        }
        indentDisStatusMap.put(patientIndentNoRef[i], disStatus[i]);
      }
    }

    ActionRedirect redirect = null;

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
    String billable = null;
    String categoryId = null;

    ChargeDTO chargeDTO;
    Connection con = null;
    ChargeDAO chargeDAO = new ChargeDAO(con);

    BillActivityCharge activityDTO = new BillActivityCharge();
    String gropName = ChargeDTO.CG_INVENTORY;
    String headName = ChargeDTO.CH_INVENTORY_ITEM;
    String patientId = null;
    String bedType = null;
    ChargeHeadsDAO chargeHeadDAO = new ChargeHeadsDAO();

    boolean isnegative = true;
    String isShared = req.getParameter("isSharedLogIn");
    HttpSession session = req.getSession(false);
    String userName = isShared == null
        ? (String) session.getAttribute("userid")
        : isShared.equals("Y")
            ? req.getParameter("authUser")
            : (String) session.getAttribute("userid");

    if (mrno != null && mrno.length > 0) {
      // The patient details tab now returns patient id instead of mrno
      patientId = mrno[0];
    }
    if (patientId != null) {
      BasicDynaBean pd = VisitDetailsDAO.getPatientVisitDetailsBean(patientId);
      bedType = pd.get("bill_bed_type").toString();
      patOrgID = pd.get("org_id").toString();

      ratePlanBean = organizationDetailsDAO.findByKey("org_id", patOrgID);
      ratePlanDiscount = (BigDecimal) ratePlanBean.get("pharmacy_discount_percentage");
      ratePlanDiscType = (String) ratePlanBean.get("pharmacy_discount_type");
    }

    String[] stores = (String[]) itemsMap.get("store");
    BasicDynaBean storeBean = StoreDAO.findByStore(Integer.parseInt(stores[0]));
    int storeRatePlanId = (storeBean.get("store_rate_plan_id") == null
        ? 0
        : (Integer) storeBean.get("store_rate_plan_id"));
    int visitRtoreRatePlanId = (ratePlanBean == null
        || ratePlanBean.get("store_rate_plan_id") == null
            ? 0
            : (Integer) ratePlanBean.get("store_rate_plan_id"));

    String[] billNO = (String[]) itemsMap.get("bill_no");
    for (int i = 0; i < issueQty.length - 1; i++) {
      issueQtyconv[i] = ConversionUtils.setScale(new BigDecimal(issueQty[i]));
    }
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      /** If called from patient issue, redirect to patient issue, else to stock user issue */
      redirect = new ActionRedirect("/patientissues/add.htm");

      if (mrno != null && mrno.length > 0) {
        userType = "Patient";
        issuedTo = mrno[0];
      } else {
        userType = (issueType != null && issueType[0] != null && issueType[0].equals("d")
            ? "Dept"
            : "Ward");
        // get hosp_user_id for already existing user if not creats a user and brings the id
        if (issueType != null && issueType[0] != null && issueType[0].equals("u")) {
          result = new StockUserIssueDAO(con).checkUser(hospUser[0]);
          userType = "Hospital";
        }
        if (result) {
          issuedTo = hospUser[0];
        }
      }

      newIssueId = Integer
          .parseInt(new StockUserIssueDAO(con).getSequenceId("store_issue_sequence"));
      int k1 = 0;
      /*
       * if(itemorkits[0].equalsIgnoreCase("kit")) { kit_identifier = itemIdentifiers[0]; List
       * kit_details = new StockUserIssueDAO(con).getKitDetails(itemIdentifiers[0]); itemNames = new
       * String[kit_details.size()]; itemIdentifiers =new String[kit_details.size()]; issueQtyconv =
       * new BigDecimal[kit_details.size()]; for(int kit=0; kit<kit_details.size();kit++){ Hashtable
       * table = (Hashtable)kit_details.get(kit); itemNames[kit] =
       * (String)table.get("MEDICINE_NAME") ; itemIdentifiers[kit] = (String) table.get("BATCH_NO")
       * ; issueQtyconv[kit] = new BigDecimal((String)table.get("QTY")); } itemSize =
       * itemNames.length; }
       */

      for (int i = 0; i < itemSize; i++) {
        itemissueid = Integer
            .parseInt(new StockUserIssueDAO(con).getSequenceId("store_issue_details_sequence"));
        boolean deletedrow = false;
        if (itemCheck != null) {
          if (itemCheck[i].equals("true")) {
            deletedrow = true;
          } else {
            deletedrow = false;
          }
        }
        if (!deletedrow) {
          String medicineId = new StockUserIssueDAO(con).getItemId(itemNames[i]);

          if (saleType.equalsIgnoreCase("D")) {
            boolean qtyAvailable = EditStockDetailsDAO.checkQtyAvailable(con, stores[0], medicineId,
                Integer.parseInt(itemBatchId[i]), issueQtyconv[i]);

            if (!qtyAvailable) {
              con.rollback();
              redirect.addParameter("flag", false);
              redirect.addParameter("msg",
                  "Stock not available for : " + itemNames[i] + "  -  " + itemIdentifiers[i] + "");
              return redirect;
            }
          }
          String planId = "";
          String visitType = "";
          Hashtable item = null;
          try (PreparedStatement ps = con.prepareStatement(
              ITEM_DETAIL_LIST_QUERY + ITEM_DETAIL_LIST_QUERY_TABELS + STORE_ITEM_RATES_JOIN
              + ITEM_DETAIL_LIST_QUERY_WHERE + " and S.batch_no=? and S.medicine_id=? ")) {
            ps.setInt(1, visitRtoreRatePlanId);
            ps.setInt(2, storeRatePlanId);
            ps.setString(3, itemIdentifiers[i]);
            ps.setInt(4, Integer.parseInt(medicineId));
  
            item = (Hashtable) DataBaseUtil.queryToArrayList(ps).get(0);
          } finally {
            DataBaseUtil.closeConnections(con,null);
          }
          billable = item.get("BILLABLE").toString();

          pkgDetails = StockUserIssueDAO.getPackageMrpAndCP(Integer.parseInt(medicineId),
              itemIdentifiers[i]);

          BigDecimal rate = null;
          BigDecimal origRate = null;
          BigDecimal discount = null;
          BigDecimal amount = null;
          BigDecimal claim = null;
          int insuranceCategoryId = 0;

          if (billable != null && billable.equalsIgnoreCase("t") && !billNO[0].equalsIgnoreCase("C")
              && !billNO[0].isEmpty()) {
            rate = ConversionUtils.setScale(new BigDecimal(unitMrp[i]));
            origRate = ConversionUtils.setScale(new BigDecimal(origUnitMrp[i]));
            discount = ConversionUtils.setScale(new BigDecimal(discountAmt[i]));
            amount = ConversionUtils.setScale(new BigDecimal(amt[i]));
            claim = ConversionUtils.setScale(new BigDecimal(priInsClaimAmts[i]));
            insuranceCategoryId = Integer.parseInt(insCategory[i]);

          }

          // set bean of stock_issue_details table
          userdynaBean = stockIssueDetailsDAO.getBean();
          userdynaBean.set("user_issue_no", new BigDecimal(newIssueId));
          userdynaBean.set("medicine_id", Integer.parseInt(medicineId));
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
          userdynaBean.set("pkg_mrp",
              ((pkgmrp != null && pkgmrp[i] != null && !pkgmrp[i].isEmpty())
                  ? new BigDecimal(pkgmrp[i])
                  : BigDecimal.ZERO));
          userdynaBean.set("item_unit", itemUnit[i]);
          userdynaBean.set("issue_pkg_size",
              (pkgSize[i] != null && !pkgSize[i].equals(""))
                  ? ConversionUtils.setScale(new BigDecimal(pkgSize[i]))
                  : BigDecimal.ONE);

          if (userType.equalsIgnoreCase("Patient")) {
            userdynaBean.set("insurance_claim_amt", claim);
          }

          usermaindynaBean = stockIssueMainDAO.getBean();
          // set bean of stock_issue_main table
          usermaindynaBean.set("user_issue_no", new BigDecimal(newIssueId));
          if (issueDate != null && !issueDate.equals("")) {
            if (issueTime != null && !issueTime.equals("")) {
              usermaindynaBean.set("date_time", DateUtil.parseTimestamp(issueDate, issueTime));
            } else {
              usermaindynaBean.set("date_time", DateUtil.parseTimestamp(issueDate, new DateUtil()
                  .getTimeFormatter().format(DateUtil.getCurrentTimestamp()).toString()));
            }
          } else {
            usermaindynaBean.set("date_time", DataBaseUtil.getDateandTime());
          }

          usermaindynaBean.set("dept_from", Integer.parseInt(stores[0]));
          usermaindynaBean.set("user_type", userType);
          usermaindynaBean.set("issued_to", issuedTo);
          usermaindynaBean.set("reference", issueReason[0]);

          usermaindynaBean.set("username", userName);
          BasicDynaBean bedBean = null;
          BasicDynaBean patBean = new GenericDAO("patient_registration").findByKey("patient_id",
              issuedTo);
          int bedId = 0;
          String patType = null;
          if (patBean != null) {
            patType = (String) patBean.get("visit_type");
          }

          if (patType != null && patType.equals("i")) {
            bedBean = new GenericDAO("admission").findByKey("patient_id", issuedTo);
          }

          if (bedBean != null) {
            bedId = (Integer) bedBean.get("bed_id");
          }

          BasicDynaBean wardBean = new GenericDAO("bed_names").findByKey("bed_id", bedId);

          if (wardBean != null) {
            wardNo = (String) wardBean.get("ward_no");
          }
          
          usermaindynaBean.set("ward_no", wardNo);
          if (gatepass == true) {
            BasicDynaBean gtBean = storeGatePassDAO.getBean();
            int gatePassId = StoresSupplierReturnsDAO.getStoreGatePassNextId();
            usermaindynaBean.set("gatepass_id", gatePassId);
            String gatePassNo = "G".concat(Integer.toString(gatePassId));
            gtBean.set("gatepass_id", gatePassId);
            gtBean.set("gatepass_no", gatePassNo);
            if (userType.equals("Patient")) {
              gtBean.set("txn_type", "Issue to Patient");
            } else {
              gtBean.set("txn_type", "Issue to Hospital");
            }
            gtBean.set("created_date", DateUtil.getCurrentTimestamp());
            gtBean.set("dept_id", Integer.parseInt(stores[0]));
            result = storeGatePassDAO.insert(con, gtBean);
            if (result) {
              gtpassId = gatePassId;
            }
          }

          // consignment invoice code

          if (conInsertion && isnegative) {
            if (stype[i].equalsIgnoreCase("true")) {
              String query = "select (total_qty-ing.issue_qty) as qty,grn_no,invoice_no,"
                  + " supplier_id,billable,supplier_invoice_id "
                  + " from store_grn_details ing join store_grn_main using (grn_no)"
                  + " join store_invoice using(supplier_invoice_id)"
                  + " join store_item_details using(medicine_id) join store_category_master"
                  + "  on (category_id=med_category_id) where medicine_id=? and batch_no=?"
                  + " and (total_qty-ing.issue_qty) > 0 order by grn_date asc";
              try (PreparedStatement ps = con.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE,
                  ResultSet.CONCUR_READ_ONLY)) {
                ps.setInt(1, Integer.parseInt(medicineId));
                ps.setString(2, itemIdentifiers[i]);
                rs = ps.executeQuery();
                boolean newIssueQty = true;
                BigDecimal issqty = null;
                BigDecimal dedQty = null;
                while (rs.next()) {
                  String invoiceNo = rs.getString("invoice_no");
                  String suppId = rs.getString("supplier_id");
                  int suppInvId = rs.getInt("supplier_invoice_id");
  
                  if (newIssueQty) {
                    issqty = new BigDecimal(issueQty[i]);
                  }
  
                  invmaindynaBean = storeConsignmentInvoiceDAO.getBean();
                  invmaindynaBean.set("supplier_invoice_id", suppInvId);
                  // invmaindynaBean.set("invoice_no",invoiceNo);
                  String grnNo = rs.getString("grn_no");
                  invmaindynaBean.set("grn_no", grnNo);
                  invmaindynaBean.set("con_invoice_date", DataBaseUtil.getDateandTime());
                  invmaindynaBean.set("amount_payable", new BigDecimal("0"));
                  invmaindynaBean.set("status", "O");
                  invmaindynaBean.set("username", userName);
                  invmaindynaBean.set("issue_id", newIssueId);
                  invmaindynaBean.set("medicine_id", Integer.parseInt(medicineId));
                  invmaindynaBean.set("batch_no", itemIdentifiers[i]);
                  if (billable != null && billable.equalsIgnoreCase("t")) {
                    invmaindynaBean.set("bill_no", billNO[0]);
                  } else {
                    invmaindynaBean.set("bill_no", "");
                  }
                  String rateQuery = "select round(package_cp/issue_base_unit,2) as rate from"
                      + " store_stock_details" + " join store_item_details using(medicine_id)"
                      + " where medicine_id='" + medicineId + "' and batch_no='" + itemIdentifiers[i]
                      + "' and dept_id='" + stores[0] + "'";
  
                  invmaindynaBean.set("amount", amount);
                  BigDecimal qty = rs.getBigDecimal("qty");
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
                    if (storeConsignmentInvoiceDAO.insert(con, invmaindynaBean)) {
                      result = new StockUserIssueDAO(con).updateGRNQTY(grnNo,
                          Integer.parseInt(medicineId), itemIdentifiers[i], dedQty);
                    } else {
                      result = false;
                    }
                  }
                  newIssueQty = false;
                  if (remQty.intValue() == 0 || issqty.intValue() <= 0 || remQty.intValue() > 0) {
                    break;
                  }
  
                }
              } finally {
                if (rs != null) {
                  rs.close();
                }
              }

            }

          }

          if (result && k1 == 0 && isnegative) {
            if (stockIssueMainDAO.insert(con, usermaindynaBean)) {
              k1 = 1;
              result = true;
            } else {
              result = false;
              k1 = 1;
            }
          }
          if (result && isnegative) {

            StockFIFODAO stockFIFODAO = new StockFIFODAO();

            // reducing stock
            Map statusMap = stockFIFODAO.reduceStock(con, Integer.parseInt(stores[0]),
                Integer.parseInt(itemBatchId[i]), "U", issueQtyconv[i], null,
                (String) req.getSession(false).getAttribute("userid"), "UserIssue",
                (Integer) userdynaBean.get("item_issue_no"));

            result &= (Boolean) statusMap.get("status");

            // set total cost value
            userdynaBean.set("cost_value", statusMap.get("costValue"));
            result = stockIssueDetailsDAO.insert(con, userdynaBean);

            // if(new
            // StockUserReturnDAO(con).updateStock(Integer.parseInt(medicine_id),
            // Integer.parseInt(itemBatchId[i]),issueQtyconv[i].floatValue(),
            // Integer.parseInt(stores[0]),1,itemorkits[0], userName)){

            if (billable != null && billable.equalsIgnoreCase("t")) {
              if (!billNO[0].equalsIgnoreCase("C") && !billNO[0].isEmpty()) {

                BillDAO billDao = new BillDAO(con);
                String billNo = billNO[0];
                Bill bill = billDao.getBill(billNo);

                String billStatus = bill.getStatus();
                if (billStatus != null && !billStatus.equals("A")) {
                  result = false;
                  redirect.addParameter("message1",
                      "Bill : " + billNo + " is not open: cannot issue items");
                } else {
                  // setting revenue a/c group a/c to store wise preferences
                  BasicDynaBean store = storesDAO.findByKey("dept_id",
                      Integer.parseInt(stores[0]));

                  chargeId = chargeDAO.getNextChargeId();
                  if (storeBean != null) {
                    centerId = (Integer) store.get("center_id");
                  }

                  BasicDynaBean masterItemBean = new PharmacymasterDAO().findByKey("medicine_id",
                      new Integer(medicineId));

                  visitType = bill.getVisitType();
                  String visitId = bill.getVisitId();

                  chargeDTO = new ChargeDTO(gropName, headName, rate, issueQtyconv[i], discount, "",
                      medicineId, itemNames[i], null, false, 0,
                      (Integer) masterItemBean.get("service_sub_group_id"), 0, visitType, visitId,
                      null);

                  chargeDTO.setBillNo(billNO[0]);
                  chargeDTO.setChargeId(chargeId);
                  chargeDTO.setOriginalRate(origRate);
                  chargeDTO.setActivityDetails("PHI", itemissueid, "Y", null);
                  chargeDTO.setUsername(userName);
                  chargeDTO.setUserRemarks(issueReason[0]);
                  int accountGroup = (Integer) store.get("account_group");
                  chargeDTO.setAccount_group(accountGroup);
                  chargeDTO.setInsuranceClaimAmount(claim);
                  chargeDTO.setInsuranceCategoryId(insuranceCategoryId);
                  chargeDTO.setActRemarks("No. " + newIssueId);
                  BasicDynaBean chargeHeadBean = chargeHeadDAO.findByKey("chargehead_id", headName);
                  chargeDTO
                      .setAllowRateDecrease((Boolean) chargeHeadBean.get("allow_rate_decrease"));
                  chargeDTO
                      .setAllowRateIncrease((Boolean) chargeHeadBean.get("allow_rate_increase"));
                  chargeDTO.setTaxAmt(new BigDecimal(taxAmt[i]));
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
                    finalclaimAmts[c] = claimAmts[i][c] != null
                        ? new BigDecimal(claimAmts[i][c])
                        : BigDecimal.ZERO;
                  }
                  chargeDTO.setClaimAmounts(finalclaimAmts);

                  if (issueDate != null && !issueDate.equals("")) {
                    if (issueTime != null && !issueTime.equals("")) {
                      chargeDTO.setPostedDate(DateUtil.parseTimestamp(issueDate, issueTime));
                    } else {
                      usermaindynaBean.set("date_time",
                          DateUtil.parseTimestamp(issueDate, new DateUtil().getTimeFormatter()
                              .format(DateUtil.getCurrentTimestamp()).toString()));
                    }

                  } else {
                    chargeDTO.setPostedDate(DataBaseUtil.getDateandTime());
                  }
                  String healthAuthority = HealthAuthorityPreferencesDAO
                      .getHealthAuthorityPreferences(
                          CenterMasterDAO.getHealthAuthorityForCenter(centerId))
                      .getHealth_authority();
                  String[] drugCodeTypes = HealthAuthorityPreferencesDAO
                      .getHealthAuthorityPreferences(healthAuthority).getDrug_code_type();
                  BasicDynaBean itemCodeBean = StoreItemCodesDAO
                      .getDrugCodeType(Integer.parseInt(medicineId), drugCodeTypes);
                  if (itemCodeBean != null) {
                    if (itemCodeBean.get("code_type") != null) {
                      chargeDTO.setCodeType((String) itemCodeBean.get("code_type"));
                    }
                    if (itemCodeBean.get("item_code") != null) {
                      chargeDTO.setActRatePlanItemCode((String) itemCodeBean.get("item_code"));
                    }
                  }
                  List actChargeList = new ArrayList();
                  actChargeList.add(chargeDTO);

                  if (new ChargeDAO(con).insertCharges(actChargeList)) {
                    result = true;
                  } else {
                    result = false;
                  }
                  // multi-payer
                  result = updateIssueClaimDetails(con, visitId, actChargeList, billNO[0]);
                }
              } else {
                result = true;
              }
            } else {
              result = true;
            }

            // update indent dispense status

            if (indentDisStatusMap.size() > 0) {

              result &= new StoresPatientIndentDAO().updateIndentDetailsDispenseStatus(con,
                  patientId, indentDisStatusMap, (BigDecimal) userdynaBean.get("qty"), medicineId,
                  (Integer) userdynaBean.get("item_issue_no"), "item_issue_no");

              // if user selects Close All as dispense status,we ll update dispense status of the
              // indent to 'C' even if its not dispensed
              StoresPatientIndentDAO storesPatDAO = new StoresPatientIndentDAO();
              for (String key : new HashSet<String>(indentDisStatusMap.keySet())) {
                if (indentDisStatusMap.get(key).equals("all")) {
                  result &= storesPatDAO.closeAllIndents(con, key);
                }
              }

              result &= StoresPatientIndentDAO.updateIndentDispenseStatus(con, patientId);
              // I represents 'Issue' in process_type column of store_patient_indent_main table
              result &= StoresPatientIndentDAO.updateProcessType(con, patientId, "I");

            }
          } else {
            result = false;
          }
        }
      }

      if (result && isnegative) {
        msg = "Successfully Issued Items";
        redirect.addParameter("message", newIssueId);
        BasicDynaBean billChargeTaxBean;
        Iterator<Map<String, Object>> taxDetailsListIterator = taxDetailsList.iterator();
        while (taxDetailsListIterator.hasNext()) {
          Map<String, Object> taxDetails = taxDetailsListIterator.next();
          billChargeTaxBean = billChargeTaxDao.getBean();
          billChargeTaxBean.set("tax_rate", taxDetails.get("tax_rate"));
          billChargeTaxBean.set("tax_amount", taxDetails.get("tax_amt"));
          billChargeTaxBean.set("original_tax_amt", taxDetails.get("tax_amt"));
          billChargeTaxBean.set("tax_sub_group_id", taxDetails.get("item_subgroup_id"));
          billChargeTaxBean.set("charge_id", taxDetails.get("charge_id"));
          result &= billChargeTaxDao.insert(con, billChargeTaxBean);
        }
        if (result) {
          con.commit();
        }

        // update stock timestamp
        StockFIFODAO stockFIFODAO = new StockFIFODAO();
        stockFIFODAO.updateStockTimeStamp();
        stockFIFODAO.updateStoresStockTimeStamp(Integer.parseInt(stores[0]));

        if (userType != null && userType.equals("Patient")) {
          if (billNO != null && billNO[0] != null && !billNO[0].equals("")) {
            BillDAO.resetTotalsOrReProcess(billNO[0]);
          }
        }

      } else {
        msg = "Issue Items Failed";
        redirect.addParameter("message", null);
        log.error(msg);
        con.rollback();
      }
    } finally {

      if (con != null) {
        con.close();
      }
    }

    new SponsorBO().recalculateSponsorAmount(patientId);

    if (redirect.getParameterString().indexOf("message") == -1) {
      redirect.addParameter("message", newIssueId);
    }
    redirect.addParameter("gtpass", gatepass);
    if (billNO != null && billNO[0] != null) {
      redirect.addParameter("billNo", billNO[0]);
    }

    redirect.addParameter("fromOTScreen", req.getParameter("fromOTScreen"));
    redirect.addParameter("operation_details_id", req.getParameter("operation_details_id"));
    redirect.addParameter("visitIdForOT", patientId);
    return redirect;
  }

  /**
   * Gets the kit item details.
   *
   * @param am
   *          the am
   * @param af
   *          the af
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the kit item details
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ServletException
   *           the servlet exception
   * @throws SQLException
   *           the SQL exception
   */
  public ActionForward getKitItemDetails(ActionMapping am, ActionForm af, HttpServletRequest req,
      HttpServletResponse res) throws IOException, ServletException, SQLException {

    String kitname = req.getParameter("item_name");
    String kitId = StockUserIssueDAO.kitNameToId(kitname);
    ArrayList<String> kitdet = StockUserIssueDAO.getKitItemDetails(kitId);
    String kit = js.serialize(kitdet);
    res.setContentType("text/plain");
    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    res.getWriter().write(kit);
    res.flushBuffer();
    return null;
  }

  /**
   * Gets the patient details JSON.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param res
   *          the res
   * @return the patient details JSON
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  public ActionForward getPatientDetailsJSON(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse res)
      throws IOException, SQLException, ParseException {

    res.setContentType("text/javascript");
    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

    String mrno = request.getParameter("mrno");
    BasicDynaBean pddet = VisitDetailsDAO.getPatientVisitDetailsBean(mrno);
    HashMap resultMap = new HashMap();
    resultMap.putAll(pddet.getMap());
    resultMap.put("patient_details_plan_details", ConversionUtils.listBeanToListMap(
        patInsrPlanDao.getVisitPlanSponsorsDetails((String) pddet.get("patient_id"))));
    String patientIndentNo = request.getParameter("patient_indent_no");

    int storeRatePlanId = 0;
    int planId = 0;
    if (pddet.get("store_rate_plan_id") != null) {
      storeRatePlanId = (Integer) pddet.get("store_rate_plan_id");
    }

    if (pddet != null) {
      if (pddet.get("plan_id") != null) {
        planId = (Integer) pddet.get("plan_id");
      }
      getIndentDetails(request, resultMap, pddet.get("patient_id").toString(), planId,
          storeRatePlanId, "I", Integer.parseInt(request.getParameter("storeId")), patientIndentNo);
    }

    // multi-payer : set visit plan details
    setPlanDetails(resultMap);
    resultMap.put("visitTotalPatientDue",
        BillDAO.getVisitPatientDue((String) pddet.get("patient_id")));

    res.getWriter().write(js.deepSerialize(resultMap));
    res.flushBuffer();

    return null;
  }

  /**
   * Gets the indent details.
   *
   * @param req
   *          the req
   * @param patientIndentDetails
   *          the patient indent details
   * @param visitId
   *          the visit id
   * @param planId
   *          the plan id
   * @param storeRatePlanId
   *          the store rate plan id
   * @param indentType
   *          the indent type
   * @param indentStore
   *          the indent store
   * @param patientIndentNo
   *          the patient indent no
   * @throws SQLException
   *           the SQL exception
   */
  public void getIndentDetails(HttpServletRequest req, HashMap patientIndentDetails, String visitId,
      int planId, int storeRatePlanId, String indentType, int indentStore, String patientIndentNo)
      throws SQLException {

    List<BasicDynaBean> indents = storePatientIndentDao.getIndentsForProcess(visitId, indentType,
        indentStore, patientIndentNo);
    List<BasicDynaBean> indentDetailsLIst = new ArrayList<BasicDynaBean>();
    List<Integer> medicines = new ArrayList<Integer>();
    String visitType = VisitDetailsDAO.getVisitType(visitId);
    String deptIdStr = req.getParameter("storeId");
    if ((deptIdStr == null) || deptIdStr.equals("")) {
      log.error("getStockJSON: Store ID is required");
      return;
    }
    int deptId = Integer.parseInt(deptIdStr);
    BasicDynaBean storeDetails = StoreDAO.findByStore(deptId);
    Integer centerId = (Integer) storeDetails.get("center_id");
    String healthAuthority = CenterMasterDAO.getHealthAuthorityForCenter(centerId);

    // only finalized indents are to process
    indentDetailsLIst.addAll(StoresPatientIndentDAO.getIndentDetailsForProcessOfIndentStore(visitId,
        "F", indentType, indentStore, patientIndentNo));

    for (BasicDynaBean indentDet : indentDetailsLIst) {
      medicines.add((Integer) indentDet.get("medicine_id"));
    }

    if (indentDetailsLIst.size() > 0) {
      List<BasicDynaBean> stock = MedicineStockDAO.getMedicineStockWithPatAmtsInDeptIndent(
          medicines, deptId, planId, visitType, true, storeRatePlanId, healthAuthority);

      HashMap medBatches = patientIndentDetails.get("medBatches") == null
          ? new HashMap()
          : (HashMap) patientIndentDetails.get("medBatches");
      medBatches.putAll(ConversionUtils.listBeanToMapListMap(stock, "medicine_id"));
      patientIndentDetails.put("medBatches", medBatches);
    }

    if (indentDetailsLIst.size() > 0) {
      patientIndentDetails.put("patIndentDetails",
          ConversionUtils.copyListDynaBeansToMap(indentDetailsLIst));
      patientIndentDetails.put("indentsList", ConversionUtils.copyListDynaBeansToMap(indents));
    }
  }

  /**
   * Generate gate passprint for issue.
   *
   * @param am
   *          the am
   * @param af
   *          the af
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the action forward
   * @throws SQLException
   *           the SQL exception
   * @throws Exception
   *           the exception
   */
  public ActionForward generateGatePassprintForIssue(ActionMapping am, ActionForm af,
      HttpServletRequest req, HttpServletResponse res) throws SQLException, Exception {

    Connection con = null;
    Template template = null;
    Map params = new HashMap();
    String issNo = req.getParameter("issNo");

    if (issNo != null) {

      List<BasicDynaBean> gatePassItemList = StockUserIssueDAO.getIssuedItemList(issNo);
      params.put("items", gatePassItemList);
      params.put("type", "Issue");
      PrintTemplatesDAO printtemplatedao = new PrintTemplatesDAO();
      String templateContent = printtemplatedao
          .getCustomizedTemplate(PrintTemplate.Gate_pass_print);

      if (templateContent == null || templateContent.equals("")) {
        template = AppInit.getFmConfig()
            .getTemplate(PrintTemplate.Gate_pass_print.getFtlName() + ".ftl");
      } else {
        StringReader reader = new StringReader(templateContent);
        template = new Template("GatePassPrint.ftl", reader, AppInit.getFmConfig());
      }
      StringWriter writer = new StringWriter();
      template.process(params, writer);
      String printContent = writer.toString();
      HtmlConverter hc = new HtmlConverter();
      BasicDynaBean printprefs = PrintConfigurationsDAO
          .getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_STORE);
      if (printprefs.get("print_mode").equals("P")) {
        OutputStream os = res.getOutputStream();
        res.setContentType("application/pdf");
        hc.writePdf(os, printContent, "GatePassPrint", printprefs, false, false, true, true, true,
            false);
        return null;
      } else {
        String textReport = null;
        textReport = new String(
            hc.getText(printContent, "GatePassPrintText", printprefs, true, true));
        req.setAttribute("textReport", textReport);
        req.setAttribute("textColumns", printprefs.get("text_mode_column"));
        req.setAttribute("printerType", "DMP");
        return am.findForward("textPrintApplet");
      }
    }
    return null;
  }

  /**
   * Gets the markup rates.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param res
   *          the res
   * @return the markup rates
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  public ActionForward getMarkupRates(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse res)
      throws IOException, SQLException, ParseException {

    String categoryId = request.getParameter("category_id");
    String bedType = request.getParameter("bed_type");
    res.setContentType("text/plain");
    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    String rateQuery = "select markup_rate from category_bed_markups where category_id=?"
        + " and bed_type=?";

    res.getWriter().write(js.serialize(DataBaseUtil.getStringValueFromDb(rateQuery, 
        new Object[]{categoryId, bedType})));
    res.flushBuffer();
    return null;

  }

  /**
   * Checks if is sponsor bill.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param res
   *          the res
   * @return the action forward
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  public ActionForward isSponsorBill(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse res)
      throws IOException, SQLException, ParseException {
    String billNo = request.getParameter("billNo");
    String medName = request.getParameter("medName");
    String isInsurance = "N";
    String medId = StoresDBTablesUtil.itemNameToId(medName);
    boolean claimable = false;
    BasicDynaBean itemBean = new GenericDAO("store_item_details").findByKey("medicine_id",
        Integer.parseInt(medId));
    if (itemBean != null) {
      BasicDynaBean cbean = new GenericDAO("store_category_master").findByKey("category_id",
          itemBean.get("med_category_id"));
      if (cbean != null) {
        claimable = (Boolean) cbean.get("claimable");
      }
    }
    if (BillDAO.checkIfsponsorBill(billNo) && claimable) {
      isInsurance = "Y";
    }
    res.setContentType("text/plain");
    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    res.getWriter().write(js.serialize(isInsurance));
    res.flushBuffer();
    return null;
  }

  /**
   * Sets insurance plan related details in the map.
   *
   * @param resultMap
   *          the new plan details
   * @throws SQLException
   *           the SQL exception
   */
  private void setPlanDetails(Map resultMap) throws SQLException {
    List<BasicDynaBean> visitPlans = patInsrPlanDao
        .getPlanDetails((String) resultMap.get("patient_id"));
    resultMap.put("patient_plan_details", ConversionUtils.listBeanToListMap(visitPlans));

    String visitType = VisitDetailsDAO.getVisitType((String) resultMap.get("patient_id"));
    Map visitPlanMasterDetailsMap = new HashMap<Integer, List<BasicDynaBean>>();
    for (BasicDynaBean visitPlan : visitPlans) {
      visitPlanMasterDetailsMap.put(visitPlan.get("plan_id"), ConversionUtils.listBeanToListMap(
          panMasterDAO.getInsuPlanDetails((Integer) visitPlan.get("plan_id"), visitType)));
    }

    resultMap.put("visit_plans_master_details", visitPlanMasterDetailsMap);
  }

  /**
   * Inserts claim details of issued items.
   *
   * @param con
   *          the con
   * @param visitId
   *          the visit id
   * @param actChargeList
   *          the act charge list
   * @param billNo
   *          the bill no
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  private boolean updateIssueClaimDetails(Connection con, String visitId,
      List<ChargeDTO> actChargeList, String billNo) throws SQLException, IOException {
    List<BasicDynaBean> planList = insPlanDAO.getPlanDetails(con, visitId);
    BasicDynaBean billBean = BillDAO.getBillBean(con, billNo);
    boolean isInsuranceBill = false;
    if (null != billBean) {
      isInsuranceBill = (Boolean) billBean.get("is_tpa");
    }

    boolean sucess = true;
    int[] planIds = new int[planList.size()];
    for (int j = 0; j < planList.size(); j++) {
      planIds[j] = (Integer) planList.get(j).get("plan_id");
    }

    if (planIds.length > 0 && isInsuranceBill) {
      sucess = chgClaimDAO.insertBillChargeClaims(con, actChargeList, planIds, visitId, billNo);
    }

    return sucess;
  }

  /**
   * Gets the claim amount.
   *
   * @param am
   *          the am
   * @param af
   *          the af
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the claim amount
   * @throws Exception
   *           the exception
   */
  public ActionForward getClaimAmount(ActionMapping am, ActionForm af, HttpServletRequest req,
      HttpServletResponse res) throws Exception {

    int planId = req.getParameter("plan_id") != null
        ? Integer.parseInt(req.getParameter("plan_id"))
        : 0;
    BigDecimal amount = req.getParameter("amount") != null
        ? new BigDecimal(req.getParameter("amount"))
        : BigDecimal.ZERO;
    String visitType = req.getParameter("visit_type");
    int categoryId = req.getParameter("category_id") != null
        ? Integer.parseInt(req.getParameter("category_id"))
        : 0;
    boolean firstOfCategory = req.getParameter("foc").equals("true");
    BigDecimal discount = req.getParameter("discount") != null
        ? new BigDecimal(req.getParameter("discount"))
        : BigDecimal.ZERO;

    BigDecimal claimAMt = new AdvanceInsuranceCalculator().calculateClaim(amount, discount, null,
        planId, firstOfCategory, visitType, categoryId);

    res.setContentType("text/plain");
    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    res.getWriter().write(js.serialize(claimAMt));
    res.flushBuffer();
    return null;

  }

  /**
   * Gets the item details query.
   *
   * @param isInsuVisit
   *          the is insu visit
   * @param stockNegative
   *          the stock negative
   * @param firstOfCat
   *          the first of cat
   * @param useBatchMRP
   *          the use batch MRP
   * @return the item details query
   */
  private String getItemDetailsQuery(boolean isInsuVisit, boolean stockNegative, boolean firstOfCat,
      boolean useBatchMRP) {
    StringBuilder query = null;
    if (isInsuVisit) {
      if (firstOfCat) {
        query = new StringBuilder(ITEM_DETAIL_LIST_QUERY_WITH_INS_FIELDS);
        query.append(ITEM_SELLING_PRICE_BATCH_MRP_NO_MRP);
        query.append(
            useBatchMRP ? ITEM_SELLING_PRICE_BATCH_MRP_YES : ITEM_SELLING_PRICE_BATCH_MRP_NO);
        query.append(ITEM_DETAIL_LIST_QUERY_WITH_INS_TABLES);
        query.append(ITEM_DETAIL_LIST_QUERY_WHERE);
        if (stockNegative) {
          query.append(
              " AND (C.identification='B' OR S.qty>0) and medicine_name=?" + " and dept_id=?  ");
        } else {
          query.append(" and S.qty>0 and medicine_name=? and dept_id=?  ");
        }

      } else {

        query = new StringBuilder(ITEM_DETAIL_LIST_QUERY_WITH_INS_AND_PAT_AMT_ZERO_FIELDS);
        query.append(ITEM_DETAIL_LIST_QUERY_WITH_INS_AND_PAT_AMT_ZERO_FIELDS_MRP);
        query.append(
            useBatchMRP ? ITEM_SELLING_PRICE_BATCH_MRP_YES : ITEM_SELLING_PRICE_BATCH_MRP_NO);
        query.append(ITEM_DETAIL_LIST_QUERY_WITH_INS_AND_PAT_AMT_ZERO_TABLES);
        query.append(ITEM_DETAIL_LIST_QUERY_WHERE);
        if (stockNegative) {
          query.append(
              " AND (C.identification='B' OR S.qty>0) and medicine_name=? and" + " dept_id=?  ");
        } else {
          query.append(" and S.qty>0 and medicine_name=? and dept_id=?  ");
        }

      }
    } else {
      query = new StringBuilder(ITEM_DETAIL_LIST_QUERY);
      query
          .append(useBatchMRP ? ITEM_SELLING_PRICE_BATCH_MRP_YES : ITEM_SELLING_PRICE_BATCH_MRP_NO);
      query.append(ITEM_DETAIL_LIST_QUERY_TABELS);
      query.append(STORE_ITEM_RATES_JOIN);
      query.append(ITEM_DETAIL_LIST_QUERY_WHERE);
      if (stockNegative) {
        query.append(" AND (C.identification='B' OR S.qty>0) and medicine_name=? and dept_id=? ");
      } else {
        query.append(" and S.qty>0 and medicine_name=? and dept_id=? ");
      }
    }

    return query.toString();

  }

  /**
   * Used to print a patient issue pdf based on issue no.
   *
   * @author irshad
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
  @IgnoreConfidentialFilters
  public ActionForward printPatientIssues(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws SQLException, IOException, Exception {

    PrintTemplatesDAO printTemplateDAO = new PrintTemplatesDAO();
    String issueNo = request.getParameter("issNo");

    List<BasicDynaBean> patientIssueDetails = StockUserIssueDAO
        .getPatientIssueInfo(Integer.valueOf(issueNo));
    List<BasicDynaBean> taxSummary = StockUserIssueDAO.getTaxSummary(Integer.valueOf(issueNo));

    Map ftlParams = new HashMap();
    ftlParams.put("patientIssueList", patientIssueDetails);
    ftlParams.put("taxSummary", taxSummary);

    BasicDynaBean printprefs = PrintConfigurationsDAO
        .getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_STORE);
    PrintTemplate template = PrintTemplate.PatientIssuePrintTemplate;
    String templateContent = printTemplateDAO.getCustomizedTemplate(template);

    Template tplt = null;
    if (templateContent == null || templateContent.equals("")) {
      tplt = AppInit.getFmConfig().getTemplate(template.getFtlName() + ".ftl");
    } else {
      StringReader reader = new StringReader(templateContent);
      tplt = new Template(null, reader, AppInit.getFmConfig());
    }

    HtmlConverter htmlConverter = new HtmlConverter();
    StringWriter writer = new StringWriter();
    tplt.process(ftlParams, writer);
    String printContent = writer.toString();
    if (printprefs.get("print_mode").equals("P")) {
      OutputStream os = response.getOutputStream();
      response.setContentType("application/pdf");
      htmlConverter.writePdf(os, printContent, "PatientIssuePrintTemplate", printprefs, false,
          false, true, true, true, false);
    } else {
      String textReport = null;
      textReport = new String(
          htmlConverter.getText(printContent, "PatientIssuePrintTemplate", printprefs, true, true));
      request.setAttribute("textReport", textReport);
      request.setAttribute("textColumns", printprefs.get("text_mode_column"));
      request.setAttribute("printerType", "DMP");
      return mapping.findForward("textPrintApplet");
    }
    return null;
  }

  /**
   * Gets the insurance category payable status.
   *
   * @param am
   *          the am
   * @param af
   *          the af
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the insurance category payable status
   * @throws Exception
   *           the exception
   */
  public ActionForward getInsuranceCategoryPayableStatus(ActionMapping am, ActionForm af,
      HttpServletRequest req, HttpServletResponse res) throws Exception {

    int medicineId = req.getParameter("medicineId") != null
        ? Integer.parseInt(req.getParameter("medicineId"))
        : 0;
    String visitId = req.getParameter("visitId") != null ? req.getParameter("visitId") : "";
    String visitType = req.getParameter("visitType") != null ? req.getParameter("visitType") : "o";
    BasicDynaBean calimableStatus = MedicineSalesDAO.getInsuranceCategoryPayableStatus(visitId,
        medicineId, visitType);
    List<BasicDynaBean> calimableStatusList = new ArrayList<BasicDynaBean>();
    calimableStatusList.add(calimableStatus);
    res.setContentType("text/plain");
    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    res.getWriter()
        .write(js.serialize(ConversionUtils.copyListDynaBeansToMap(calimableStatusList)));
    res.flushBuffer();
    return null;

  }

  /**
   * This method is use to get issue rate for an item based on issue rate expression.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param res
   *          the res
   * @return the issue price JSON
   * @throws Exception
   *           the exception
   */
  public ActionForward getIssuePriceJSON(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse res) throws Exception {

    res.setContentType("text/javascript");
    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    BigDecimal maxCP = BigDecimal.ZERO;
    int centerId = (Integer) request.getSession().getAttribute("centerId");
    int itemBatchId = Integer.parseInt(request.getParameter("itemBatchId"));
    int storeId = Integer.parseInt(request.getParameter("storeId"));
    BigDecimal qty = new BigDecimal(request.getParameter("qty"));
    String bedType = request.getParameter("bedtype");
    String discountStr = request.getParameter("discount");
    double discount = Double.parseDouble(discountStr != null ? discountStr : "0.0");
    maxCP = StockUserIssueDAO.getIssueRate(itemBatchId, qty, storeId, centerId, bedType, discount);
    res.getWriter().write(js.deepSerialize(maxCP));
    res.flushBuffer();
    return null;
  }

  /**
   * This method is use to get orderkit items with batches.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param res
   *          the res
   * @return the order kit items JSON
   * @throws Exception
   *           the exception
   */
  public ActionForward getOrderKitItemsJSON(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse res) throws Exception {

    res.setContentType("text/javascript");
    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    Map<String, Object> orderKitDetailsMap = new HashMap<String, Object>();

    int orderKitId = Integer.parseInt(request.getParameter("order_kit_id"));
    int deptId = Integer.parseInt(request.getParameter("storeId"));
    String planIdStr = request.getParameter("planId");
    String storeRatePlanIdStr = request.getParameter("storeRatePlanId");
    int planId = ((planIdStr != null && planIdStr.equals("undefined") && planIdStr.equals(""))
        ? Integer.parseInt(request.getParameter("planId"))
        : -1);
    String visitId = (request.getParameter("visitId") != null
        ? request.getParameter("visitId")
        : "-1");
    String ratePlanId = (request.getParameter("ratePlanId") != null
        ? request.getParameter("ratePlanId")
        : "-1");
    int storeRatePlanId = ((storeRatePlanIdStr != null && !storeRatePlanIdStr.equals("undefined")
        && !storeRatePlanIdStr.equals("null") && !storeRatePlanIdStr.equals(""))
            ? Integer.parseInt(storeRatePlanIdStr)
            : -1);
    String visitType = request.getParameter("visitType").trim();

    String issueTypeStr = request.getParameter("issueType");
    String[] issueTypes = null; // default: no filter
    if (issueTypeStr != null && !issueTypeStr.equals("")) {
      issueTypes = issueTypeStr.split("");
    }

    // to get all order kit items
    List<BasicDynaBean> orderKitItems = StockUserIssueDAO.getOrderKitItemsDetails(orderKitId);

    // list of available medicines in stock
    List<BasicDynaBean> availMedicineList = new ArrayList<BasicDynaBean>();

    // to get all items status available in store
    Map<Integer, String> orderKitItemsStatus = StockUserIssueDAO.getOrderKitItemsStockStatus(deptId,
        orderKitId, issueTypes);

    Map<String, String> summarizedOrderKitItemsStatusMap = new LinkedHashMap<String, String>();
    List<Integer> medicineList = new ArrayList<Integer>();

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
        if (inStockQty > 0) {
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
    BasicDynaBean storeDetails = StoreDAO.findByStore(deptId);
    Integer centerId = (Integer) storeDetails.get("center_id");
    String healthAuthority = CenterMasterDAO.getHealthAuthorityForCenter(centerId);

    List<BasicDynaBean> stock = MedicineStockDAO.getOrderKitMedicineStockWithPatAmtsInDept(
        medicineList, deptId, planId, visitType, true, storeRatePlanId, healthAuthority);
    orderKitDetailsMap.put("medBatches",
        ConversionUtils.listBeanToMapListMap(stock, "medicine_id"));
    orderKitDetailsMap.put("order_kit_items_status", summarizedOrderKitItemsStatusMap);
    orderKitDetailsMap.put("order_kit_items",
        ConversionUtils.copyListDynaBeansToMap(availMedicineList));
    orderKitDetailsMap.put("total_items_status",
        unavailableMedicineCount + "@" + orderKitItems.size());
    // list of non issuable items
    List<BasicDynaBean> nonIssuableItems = StockUserIssueDAO.getNonIssuableItems(orderKitId,
        issueTypes);
    orderKitDetailsMap.put("nonissuableitems",
        ConversionUtils.copyListDynaBeansToMap(nonIssuableItems));
    res.getWriter().write(js.deepSerialize(orderKitDetailsMap));
    res.flushBuffer();
    return null;
  }

  private void cacheIssueTransactions(List<Map<String,Object>> cacheIssueTxns,
       BasicDynaBean issueMain, BasicDynaBean issueDetails, Map patient,
       String billNo, Integer centerId, ChargeDTO charge) throws SQLException {
    GenericDAO billChargeDAO = new GenericDAO("bill_charge");
    BasicDynaBean chargeBean = billChargeDAO.getBean();
    chargeBean.set("amount", charge.getAmount());
    chargeBean.set("discount", charge.getDiscount());
     Map<String, Object> data = scmOutService.getIssueMap(issueMain, issueDetails, patient,
         billNo, centerId, chargeBean);
     if(!data.isEmpty()) {
       cacheIssueTxns.add(data);
     }
   }
}
