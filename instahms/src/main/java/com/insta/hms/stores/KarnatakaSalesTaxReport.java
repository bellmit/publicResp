package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DynaBeanBuilder;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.StoreMaster.StoreMasterDAO;
import com.lowagie.text.DocumentException;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This report gives consolidated sales tax information.
 *
 * @author sirisha
 */
public class KarnatakaSalesTaxReport extends BaseAction {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(KarnatakaSalesTaxReport.class);

  /** The cash credit sales return sales taxs. */
  private static String cash_credit_sales_return_sales_taxs = "SELECT sum(goodsmtax)+"
      + "sum(goodscamount)+sum(packedgoodsmamount)+sum(packedgoodscamount)+sum(othergoodscamount)"
      + "+sum(othergoodsmamount)+sum(goodsmamount)+sum(goodsctax)+sum(packedgoodsmtax)+"
      + "sum(packedgoodsctax)+sum(othersctax)+sum(othersmtax) as billed_value,"
      + "   sum(goodsmamount) as mamt,sum(goodsmtax) as mtax,"
      + "   sum(goodscamount) as camt,sum(goodsctax) as ctax, "
      + " sum(packedgoodsmamount) as packagedmamt,sum(packedgoodsmtax) as packagedmtax,"
      + "   sum(packedgoodscamount) as packagedcamt,sum(packedgoodsctax) as packagedctax,"
      + "   sum(othergoodscamount) as otherscamt,sum(othersctax) as othersctax,"
      + "   sum(othergoodsmamount) as othersmamt,sum(othersmtax) as othersmtax"
      + "   ,type,charge_head FROM( " + " SELECT "
      + " sum(ssd.amount-ssd.tax) as goodsmamount,sum(ssd.tax) as goodsmtax,"
      + " 0 as goodscamount,0 as goodsctax," + " 0 as packedgoodsmamount,0 as packedgoodsmtax,"
      + " 0 as packedgoodscamount,0 as packedgoodsctax,"
      + " 0 as othergoodscamount,0 as othersctax," + " 0 as othergoodsmamount,0 as othersmtax,"
      + " bc.charge_head,COALESCE(sir.tax_rate,COALESCE(ssir.tax_rate,sid.tax_rate))::text"
      + " as text_rate,type FROM bill_charge bc "
      + " JOIN store_sales_main sm ON(sm.bill_no = bc.bill_no AND bc.act_remarks ="
      + " 'No. '||sm.sale_id)  JOIN store_sales_details ssd USING(sale_id) "
      + " JOIN store_item_details sid ON(sid.medicine_id = ssd.medicine_id)  "
      + " LEFT JOIN store_item_rates sir ON (ssd.medicine_id = sir.medicine_id AND"
      + " sir.store_rate_plan_id = 0)  LEFT JOIN store_item_rates ssir ON (ssd.medicine_id"
      + " = ssir.medicine_id AND ssir.store_rate_plan_id = sm.store_rate_plan_id) "
      + " WHERE charge_head IN ('PHMED','PHCMED','PHRET','PHCRET') AND COALESCE("
      + " sir.tax_rate,COALESCE(ssir.tax_rate,sid.tax_rate)) = ? AND "
      + " COALESCE(sir.tax_type,COALESCE(ssir.tax_type,sid.tax_type)) IN ( 'M','MB') "
      + " AND date(sale_date) BETWEEN ? AND ? #storeid1# "
      + " group by charge_head,sid.tax_rate,sir.tax_rate,ssir.tax_rate,type "

      + " UNION ALL " + " SELECT " + " 0 as goodsmamount ,0 as goodsmtax,"
      + " sum(ssd.amount-ssd.tax) as goodscamount,sum(ssd.tax) as goodsctax,"
      + " 0 as packedgoodsmamount, 0 as packedgoodsmtax,"
      + " 0 as packedgoodscamount,0 as packedgoodsctax,"
      + " 0 as othergoodscamount,0 as othersctax," + " 0 as othergoodsmamount,0 as othersmtax,"
      + " bc.charge_head,COALESCE(sir.tax_rate,COALESCE(ssir.tax_rate,sid.tax_rate))::text as"
      + " text_rate,type  FROM bill_charge bc "
      + " JOIN store_sales_main sm ON(sm.bill_no = bc.bill_no AND bc.act_remarks ="
      + " 'No. '||sm.sale_id) JOIN store_sales_details ssd USING(sale_id) "
      + " JOIN store_item_details sid ON(sid.medicine_id = ssd.medicine_id) "
      + " LEFT JOIN store_item_rates sir ON (ssd.medicine_id = sir.medicine_id AND "
      + " sir.store_rate_plan_id = 0) LEFT JOIN store_item_rates ssir ON (ssd.medicine_id = "
      + " ssir.medicine_id AND ssir.store_rate_plan_id = sm.store_rate_plan_id) "
      + " WHERE charge_head IN ('PHMED','PHCMED','PHRET','PHCRET') AND COALESCE(sir.tax_rate,"
      + " COALESCE(ssir.tax_rate,sid.tax_rate)) = ? AND COALESCE(sir.tax_type,"
      + " COALESCE(ssir.tax_type,sid.tax_type)) IN ('C','CB') AND date(sale_date)"
      + " BETWEEN ? AND ? #storeid2# "
      + " group by charge_head,sid.tax_rate,sir.tax_rate,ssir.tax_rate,type "

      + " UNION " + " SELECT " + " 0 as goodsmamount ,0 as goodsmtax,"
      + " 0 as goodscamount,0 as goodsctax,"
      + " sum(ssd.amount-ssd.tax) as packedgoodsmamount,sum(ssd.tax) as packedgoodsmtax, "
      + " 0 as packedgoodscamount,0 as packedgoodsctax ,"
      + " 0 as othergoodscamount,0 as othersctax," + " 0 as othergoodsmamount,0 as othersmtax,"
      + " bc.charge_head,COALESCE(sir.tax_rate,COALESCE(ssir.tax_rate,sid.tax_rate))::text as"
      + " text_rate,type  FROM bill_charge bc "
      + " JOIN store_sales_main sm ON(sm.bill_no = bc.bill_no AND bc.act_remarks ="
      + " 'No. '||sm.sale_id)  JOIN store_sales_details ssd USING(sale_id) "
      + " JOIN store_item_details sid ON(sid.medicine_id = ssd.medicine_id) "
      + " LEFT JOIN store_item_rates sir ON (ssd.medicine_id = sir.medicine_id AND "
      + " sir.store_rate_plan_id = 0) LEFT JOIN store_item_rates ssir ON (ssd.medicine_id ="
      + " ssir.medicine_id AND ssir.store_rate_plan_id = sm.store_rate_plan_id) "
      + " WHERE charge_head IN ('PHMED','PHCMED','PHRET','PHCRET') AND COALESCE("
      + " sir.tax_rate,COALESCE(ssir.tax_rate,sid.tax_rate)) = ? AND "
      + " COALESCE(sir.tax_type,COALESCE(ssir.tax_type,sid.tax_type)) IN ( 'M','MB') AND"
      + " date(sale_date) BETWEEN ? AND ? #storeid3#  "
      + " group by charge_head,sid.tax_rate,sir.tax_rate,ssir.tax_rate,type "

      + " UNION " + " SELECT " + " 0 as goodsmamount ,0 as goodsmtax,"
      + " 0 as goodscamount,0 as goodsctax," + " 0 as packedgoodsmamount,0 as packedgoodsmtax,"
      + " sum(ssd.amount-ssd.tax) as packedgoodscamount,sum(ssd.tax) as packedgoodsctax"
      + " ,0 as othergoodscamount,0 as othersctax," + " 0 as othergoodsmamount,"
      + " 0 as othersmtax, bc.charge_head,COALESCE(sir.tax_rate,"
      + "COALESCE(ssir.tax_rate,sid.tax_rate))::text as text_rate,type "
      + " FROM bill_charge bc "
      + " JOIN store_sales_main sm ON(sm.bill_no = bc.bill_no AND bc.act_remarks ="
      + " 'No. '||sm.sale_id)  JOIN store_sales_details ssd USING(sale_id) "
      + " JOIN store_item_details sid ON(sid.medicine_id = ssd.medicine_id) "
      + " LEFT JOIN store_item_rates sir ON (ssd.medicine_id = sir.medicine_id AND "
      + " sir.store_rate_plan_id = 0)  LEFT JOIN store_item_rates ssir ON (ssd.medicine_id"
      + " = ssir.medicine_id AND ssir.store_rate_plan_id = sm.store_rate_plan_id) "
      + " WHERE charge_head IN ('PHMED','PHCMED','PHRET','PHCRET') AND COALESCE(sir.tax_rate,"
      + " COALESCE(ssir.tax_rate,sid.tax_rate)) = ? AND "
      + " COALESCE(sir.tax_type,COALESCE(ssir.tax_type,sid.tax_type)) IN ('C','CB') AND"
      + " date(sale_date) BETWEEN ? AND ?  #storeid4# "
      + " group by charge_head,sid.tax_rate,sir.tax_rate,ssir.tax_rate,type "

      + " UNION " + " SELECT " + " 0 as goodsmamount ,0 as goodsmtax,"
      + " 0 as goodscamount,0 as goodsctax," + " 0 as packedgoodsmamount,0 as packedgoodsmtax,"
      + " 0 as packedgoodscamount,0 as packedgoodsctax"
      + " ,sum(ssd.amount-ssd.tax) as othergoodscamount,sum(ssd.tax) as othersctax,"
      + " 0 as othergoodsmamount,0 as othersmtax," + " bc.charge_head,'Others' as"
      + " tax_rate,type  FROM bill_charge bc "
      + " JOIN store_sales_main sm ON(sm.bill_no = bc.bill_no AND bc.act_remarks ="
      + " 'No. '||sm.sale_id) JOIN store_sales_details ssd USING(sale_id) "
      + " JOIN store_item_details sid ON(sid.medicine_id = ssd.medicine_id) "
      + " LEFT JOIN store_item_rates sir ON (ssd.medicine_id = sir.medicine_id AND "
      + " sir.store_rate_plan_id = 0)  LEFT JOIN store_item_rates ssir ON (ssd.medicine_id"
      + " = ssir.medicine_id AND ssir.store_rate_plan_id = sm.store_rate_plan_id) "
      + " WHERE charge_head IN ('PHMED','PHCMED','PHRET','PHCRET') AND "
      + " COALESCE(sir.tax_rate,COALESCE(ssir.tax_rate,sid.tax_rate)) NOT IN(?,?) AND  "
      + " COALESCE(sir.tax_type,COALESCE(ssir.tax_type,sid.tax_type)) IN ('C','CB') AND "
      + " date(sale_date) BETWEEN ? AND ? #storeid5# "
      + " group by charge_head,type "

      + " UNION " + " SELECT 0 as goodsmamount ,0 as goodsmtax,"
      + " 0 as goodscamount,0 as goodsctax," + " 0 as packedgoodsmamount,0 as packedgoodsmtax,"
      + " 0 as packedgoodscamount,0 as packedgoodsctax"
      + " ,0 as othergoodscamount,0 as othersctax,"
      + " sum(ssd.amount-ssd.tax) as othergoodsmamount,sum(ssd.tax) as othersmtax,"
      + " bc.charge_head," + " 'Others' as tax_rate,type " + " FROM bill_charge bc "
      + " JOIN store_sales_main sm ON(sm.bill_no = bc.bill_no AND bc.act_remarks ="
      + " 'No. '||sm.sale_id) JOIN store_sales_details ssd USING(sale_id) "
      + " JOIN store_item_details sid ON(sid.medicine_id = ssd.medicine_id) "
      + " LEFT JOIN store_item_rates sir ON (ssd.medicine_id = sir.medicine_id AND "
      + " sir.store_rate_plan_id = 0) LEFT JOIN store_item_rates ssir ON (ssd.medicine_id ="
      + " ssir.medicine_id AND ssir.store_rate_plan_id = sm.store_rate_plan_id) "
      + " WHERE charge_head IN ('PHMED','PHCMED','PHRET','PHCRET') "
      + " AND COALESCE(sir.tax_rate,COALESCE(ssir.tax_rate,sid.tax_rate)) NOT IN(?,?) AND  "
      + " COALESCE(sir.tax_type,COALESCE(ssir.tax_type,sid.tax_type)) IN ('M','MB') AND"
      + " date(sale_date) BETWEEN ? AND ?  #storeid6# "
      + " group by charge_head,type " + " ) as foo GROUP BY type,charge_head";

  /** The no of bills. */
  private static String no_of_bills = "SELECT count(*),sum(amount) as amount,sum(amount)-"
      + "sum(discount)-sum(round_off) as net_amount,sum(discount) as discount,sum(round_off)"
      + " as round_off,type,charge_head FROM (SELECT charge_head,sum(sm.total_item_amount) as"
      + " amount,type,sum(sm.discount) as discount,sum(sm.round_off) as round_off from bill "
      + " JOIN bill_charge bc USING(bill_no) "
      + " JOIN store_sales_main sm ON(sm.bill_no = bc.bill_no AND bc.act_remarks ="
      + " 'No. '||sm.sale_id) WHERE charge_head = ? AND type = ? "
      + " AND date(sm.sale_date) BETWEEN ? AND ?  AND NOT is_tpa  #storeid# GROUP BY bc.bill_no,"
      + " charge_head,sm.type) AS foo GROUP BY  charge_head,type";

  /** The no of sponsor bills. */
  private static String no_of_sponsor_bills = "SELECT count(*),sum(amount) as amount,sum(amount)-"
      + " sum(discount)-sum(round_off) as net_amount,sum(discount) as discount,sum(round_off) as"
      + " round_off,tpa_name,type,charge_head FROM (SELECT charge_head,sum(sm.total_item_amount)"
      + " as amount,type,sum(sm.discount) as discount,sum(sm.round_off) as round_off,tpa_name "
      + " from bill b " + " JOIN patient_registration pr ON(pr.patient_id = b.visit_id)"
      + " JOIN bill_charge bc USING(bill_no) "
      + " JOIN store_sales_main sm ON(sm.bill_no = bc.bill_no AND bc.act_remarks ="
      + " 'No. '||sm.sale_id)  JOIN tpa_master tm ON(pr.primary_sponsor_id = tm.tpa_id)"
      + " WHERE charge_head IN ('PHMED','PHCMED','PHRET','PHCRET')"
      + "   AND date(sm.sale_date) BETWEEN ? AND ?  AND b.is_tpa AND pr.primary_sponsor_id IS"
      + " NOT NULL #storeid#  GROUP BY bc.bill_no,charge_head,sm.type,tpa_name) as foo "
      + " GROUP BY  charge_head,type,tpa_name";

  /**
   * Gets the screen.
   *
   * @param map the map
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the screen
   * @throws Exception the exception
   */
  public ActionForward getScreen(ActionMapping map, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws Exception {
    return map.findForward("taxReportScreen");
  }

  /**
   * Prints the report.
   *
   * @param map the map
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   * @throws DocumentException the document exception
   * @throws ParseException the parse exception
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward printReport(ActionMapping map, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws SQLException, IOException, TemplateException,
      DocumentException, ParseException, Exception {

    String[] storeId = req.getParameterValues("store_id");
    String centerId = req.getParameter("center_id");
    String[] chargeHeads = { "PH", "PHC", "PH", "PHC" };
    String[] types = { "MED", "RET" };

    String storeClause = "";
    Boolean hasStores = storeId != null && storeId.length > 0;
    if (hasStores) {
      String[] placeholdersArr = new String[storeId.length];
      Arrays.fill(placeholdersArr, "?");
      storeClause = " AND sm.store_id IN ("
          + StringUtils.arrayToCommaDelimitedString(placeholdersArr) + ")";
    }
    GenericDAO storesPrefs = new GenericDAO("store_preferences");
    BasicDynaBean storesPrefsBean = storesPrefs.getRecord();
    BigDecimal goodsTaxRate = (BigDecimal) storesPrefsBean.get("goods_tax_rate");
    List<Object> args = new ArrayList<>();
    args.add(goodsTaxRate);
    java.sql.Date fromDate = DataBaseUtil.parseDate(req.getParameter("fromDate"));
    args.add(fromDate);
    java.sql.Date toDate = DataBaseUtil.parseDate(req.getParameter("toDate"));
    args.add(toDate);
    String query = cash_credit_sales_return_sales_taxs;
    query = query.replaceAll("#storeid1#", storeClause);
    if (hasStores) {
      args.addAll(Arrays.asList(storeId));
    }
    args.add(goodsTaxRate);
    args.add(fromDate);
    args.add(toDate);
    query = query.replaceAll("#storeid2#", storeClause);
    if (hasStores) {
      args.addAll(Arrays.asList(storeId));
    }
    BigDecimal packagedGoodsTaxRate = (BigDecimal) storesPrefsBean.get("packaged_goods_tax_rate");
    args.add(packagedGoodsTaxRate);
    args.add(fromDate);
    args.add(toDate);
    query = query.replaceAll("#storeid3#", storeClause);
    if (hasStores) {
      args.addAll(Arrays.asList(storeId));
    }
    args.add(packagedGoodsTaxRate);
    args.add(fromDate);
    args.add(toDate);
    query = query.replaceAll("#storeid4#", storeClause);
    if (hasStores) {
      args.addAll(Arrays.asList(storeId));
    }
    args.add(goodsTaxRate);
    args.add(packagedGoodsTaxRate);
    args.add(fromDate);
    args.add(toDate);
    query = query.replaceAll("#storeid5#", storeClause);
    if (hasStores) {
      args.addAll(Arrays.asList(storeId));
    }
    args.add(goodsTaxRate);
    args.add(packagedGoodsTaxRate);
    args.add(fromDate);
    args.add(toDate);
    query = query.replaceAll("#storeid6#", storeClause);
    if (hasStores) {
      args.addAll(Arrays.asList(storeId));
    }

    try (Connection con = DataBaseUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(query)) {
      // get information for the 1st section of the report i.e,consolidated amount,tax amount
      List<BasicDynaBean> salesTax = DataBaseUtil.queryToDynaList(ps);
      Map<String, Object> params = new HashMap<String, Object>();
      params.put("from_date", DataBaseUtil.dateFormatter.format(fromDate));
      params.put("to_date", DataBaseUtil.dateFormatter.format(toDate));

      BasicDynaBean printPref = PrintConfigurationsDAO
          .getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PHARMACY, 0);

      BasicDynaBean cashSalesBean = getCashCreditTaxBean();
      BasicDynaBean creditSalesBean = getCashCreditTaxBean();
      BasicDynaBean cashReturnsBean = getCashCreditTaxBean();
      BasicDynaBean creditReturnsBean = getCashCreditTaxBean();
      for (BasicDynaBean tax : salesTax) {
        if (((String) tax.get("type")).equals("S")) {
          if (((String) tax.get("charge_head")).equals("PHMED")) {
            cashSalesBean = tax;
          } else {
            creditSalesBean = tax;
          }
        } else {
          if (((String) tax.get("charge_head")).equals("PHRET")) {
            cashReturnsBean = tax;
          } else {
            creditReturnsBean = tax;
          }
        }
      }

      params.put("cash_sales_bean", cashSalesBean);
      params.put("credit_sales_bean", creditSalesBean);
      params.put("cash_returns_bean", cashReturnsBean);
      params.put("credit_returns_bean", creditReturnsBean);

      // get information for 2nd section of the report that is no of bills and amounts of non
      // sponsor pharmacy bills.
      BasicDynaBean noOfcashSalesBean = getNoOfNonTPABean();
      BasicDynaBean noOfcreditSalesBean = getNoOfNonTPABean();
      BasicDynaBean noOfcashReturnsBean = getNoOfNonTPABean();
      BasicDynaBean noOfcreditReturnsBean = getNoOfNonTPABean();

      for (String type : types) {
        for (String chargeHead : chargeHeads) {
          try (PreparedStatement psBills = con
              .prepareStatement(no_of_bills.replaceAll("#storeid#", storeClause))) {
            List<Object> argsBills = new ArrayList<>();
            argsBills.add(chargeHead.concat(type));
            argsBills.add(type.equals("MED") ? "S" : "R");
            argsBills.add(fromDate);
            argsBills.add(toDate);
            if (hasStores) {
              argsBills.addAll(Arrays.asList(storeId));
            }
            BasicDynaBean noofBills = DataBaseUtil.queryToDynaBean(psBills);
            if (noofBills != null) {
              if (type.equals("MED")) {
                if (chargeHead.equals("PH") && noofBills != null) {
                  noOfcashSalesBean = noofBills;
                } else if (noofBills != null) {
                  noOfcreditSalesBean = noofBills;
                }
              } else {
                if (chargeHead.equals("PH") && noofBills != null) {
                  noOfcashReturnsBean = noofBills;
                } else if (noofBills != null) {
                  noOfcreditReturnsBean = noofBills;
                }
              }
            }
          }
        }
      }
      params.put("cash_sales_bills", noOfcashSalesBean);
      params.put("credit_sales_bills", noOfcreditSalesBean);
      params.put("cash_returns_bills", noOfcashReturnsBean);
      params.put("credit_returns_bills", noOfcreditReturnsBean);

      // get information for 2nd section of the report that is no of bills and amounts of all
      // sponsor pharmacy bills.
      try (PreparedStatement psTpaBills = con
          .prepareStatement(no_of_sponsor_bills.replaceAll("#storeid#", storeClause))) {
        List<Object> argsBills = new ArrayList<>();
        argsBills.add(fromDate);
        argsBills.add(toDate);
        if (hasStores) {
          argsBills.addAll(Arrays.asList(storeId));
        }
        params.put("sponsor_bills",
            ConversionUtils.listBeanToMapListBean(DataBaseUtil.queryToDynaList(psTpaBills),
                "tpa_name"));
      }

      String storesName = "";
      for (String store : storeId) {
        if (store.isEmpty()) {
          continue;
        }
        storesName = storesName.concat(new StoreMasterDAO().getDeptName(store)).concat(",");
      }
      params.put("storeName",
          (!storesName.isEmpty() ? storesName.substring(0, storesName.length() - 1) : "All"));
      params.put("center_name",
          centerId.isEmpty()
              ? "All"
              : new GenericDAO("hospital_center_master")
                  .findByKey("center_id", Integer.parseInt(centerId)).get("center_name"));
      // need tax rates for headers
      params.put("goods_tax_rate", goodsTaxRate);
      params.put("packaged_goods_tax_rate", packagedGoodsTaxRate);

      StringWriter writer = new StringWriter();
      Template ftlTemplate = AppInit.getFmConfig().getTemplate("KarnatakaSalesTaxReport.ftl");

      ftlTemplate.process(params, writer);

      String content = writer.toString();

      HtmlConverter hc = new HtmlConverter();
      OutputStream os = res.getOutputStream();
      res.setContentType("application/pdf");
      hc.writePdf(os, content);// no print prefs needed

    }
    return null;
  }

  /**
   * Gets the cash credit tax bean.
   *
   * @return the cash credit tax bean
   */
  public BasicDynaBean getCashCreditTaxBean() {
    DynaBeanBuilder builder = new DynaBeanBuilder();
    builder.add("billed_value", BigDecimal.class);
    builder.add("mamt", BigDecimal.class);
    builder.add("mtax", BigDecimal.class);
    builder.add("camt", BigDecimal.class);
    builder.add("ctax", BigDecimal.class);
    builder.add("packagedmamt", BigDecimal.class);;
    builder.add("packagedmtax", BigDecimal.class);
    builder.add("packagedcamt", BigDecimal.class);
    builder.add("packagedctax", BigDecimal.class);
    builder.add("otherscamt", BigDecimal.class);
    builder.add("othersctax", BigDecimal.class);
    builder.add("othersmamt", BigDecimal.class);
    builder.add("othersmtax", BigDecimal.class);
    builder.add("charge_head", String.class);
    builder.add("type", String.class);

    BasicDynaBean bean = builder.build();
    bean.set("billed_value", BigDecimal.ZERO);
    bean.set("mamt", BigDecimal.ZERO);
    bean.set("mtax", BigDecimal.ZERO);
    bean.set("camt", BigDecimal.ZERO);
    bean.set("ctax", BigDecimal.ZERO);
    bean.set("packagedmamt", BigDecimal.ZERO);;
    bean.set("packagedmtax", BigDecimal.ZERO);
    bean.set("packagedcamt", BigDecimal.ZERO);
    bean.set("packagedctax", BigDecimal.ZERO);
    bean.set("otherscamt", BigDecimal.ZERO);
    bean.set("othersctax", BigDecimal.ZERO);
    bean.set("othersmamt", BigDecimal.ZERO);
    bean.set("othersmtax", BigDecimal.ZERO);
    bean.set("charge_head", "PHMED");
    bean.set("type", "S");

    return bean;

  }

  /**
   * Gets the no of non TPA bean.
   *
   * @return the no of non TPA bean
   */
  public BasicDynaBean getNoOfNonTPABean() {
    DynaBeanBuilder builder = new DynaBeanBuilder();
    builder.add("count", Integer.class);
    builder.add("amount", BigDecimal.class);
    builder.add("net_amount", BigDecimal.class);
    builder.add("discount", BigDecimal.class);
    builder.add("round_off", BigDecimal.class);
    builder.add("type", String.class);

    BasicDynaBean bean = builder.build();
    bean.set("count", 0);
    bean.set("amount", BigDecimal.ZERO);
    bean.set("net_amount", BigDecimal.ZERO);
    bean.set("discount", BigDecimal.ZERO);
    bean.set("round_off", BigDecimal.ZERO);
    bean.set("type", "S");

    return bean;

  }

}
