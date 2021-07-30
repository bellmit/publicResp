package com.bob.hms.report;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FtlHelper;
import com.lowagie.text.DocumentException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;

/**
 * The Class RevenueAccrualFTLHelper.
 */
public class RevenueAccrualFtlHelper extends FtlHelper {

  /** The cfg. */
  private Configuration cfg = null;

  /**
   * Instantiates a new revenue accrual FTL helper.
   */
  public RevenueAccrualFtlHelper() {
    cfg = AppInit.getFmConfig();
  }

  /**
   * The Enum return_type.
   */
  public enum ReturnType {

    /** The pdf. */
    PDF,
    /** The pdf bytes. */
    PDF_BYTES,
    /** The text bytes. */
    TEXT_BYTES
  }

  ;

  /**
   * Instantiates a new revenue accrual FTL helper.
   *
   * @param cfg
   *          the cfg
   */
  public RevenueAccrualFtlHelper(Configuration cfg) {
    this.cfg = cfg;
  }

  /** The Constant REVENUE_ACCRUAL. */
  private static final String REVENUE_ACCRUAL = " SELECT account_group, opened_by, "
      + "patient_type, " + " sum(amount) as amount, sum(ins_claim_amount) as claim_amount  "
      + " FROM (  " + " SELECT b.opened_by, " + " CASE WHEN al.field_name::text = "
      + "'amount'::text THEN al.new_value::numeric - " + " COALESCE(al.old_value::numeric, "
      + "0::numeric) " + " ELSE CASE WHEN (al.field_name::text = 'status' and al"
      + ".new_value::text = 'X'::text)  " + " THEN 0::numeric - bc.amount ELSE 0 END " + " END"
      + " AS amount, " + " CASE WHEN al.field_name::text = 'insurance_claim_amount'::text THEN"
      + " al.new_value::numeric - " + " COALESCE(al.old_value::numeric, 0::numeric) " + " ELSE"
      + " CASE WHEN (al.field_name::text = 'status' and al.new_value::text = 'X'::text) " + " "
      + "THEN 0::numeric - bc.insurance_claim_amount ELSE 0 END " + " END AS ins_claim_amount,"
      + " " + " case when b.is_tpa='f' and (pr.insurance_company is null or pr"
      + ".insurance_company='') then 'Cash'  " + " when  b.is_tpa='t' and (pr"
      + ".insurance_company is not null or pr.insurance_company!='')  " + " and  pcm"
      + ".category_name ='General' then 'Insurance' " + " when b.is_tpa='t' and (pr"
      + ".insurance_company is null or pr.insurance_company='') then 'Corporate' " + " when  b"
      + ".is_tpa='t' and (pr.insurance_company is not null or pr.insurance_company!='') " + " "
      + "and  pcm.category_name !='General' then 'Copay_Credit' else 'Cash' end as "
      + "patient_type, " + " case when bc.charge_head in ('PHMED', 'PHCMED', 'PHRET', "
      + "'PHCRET') then 'Pharmacy' " + " else 'Hospital' end as account_group " + " FROM "
      + "bill_charge_audit_log al " + " JOIN bill_charge bc on (bc.charge_id = al.charge_id) "
      + " JOIN bill b on (b.bill_no = bc.bill_no) " + " LEFT JOIN patient_registration pr on"
      + " (pr.patient_id = b.visit_id) " + " LEFT JOIN patient_category_master pcm on (pcm"
      + ".category_id = pr.patient_category_id) " + " WHERE date(al.mod_time) BETWEEN ? and ? " + ""
      + " and (al.operation::text = 'UPDATE'::text OR al.operation::text = "
      + "'INSERT'::text) AND " + " (al.field_name::text = 'amount'::text OR al"
      + ".field_name::text = 'insurance_claim_amount'::text " + " OR al.field_name::text = "
      + "'status'::text)) as foo " + " WHERE patient_type is not null and account_group is not"
      + " null " + " GROUP BY account_group, opened_by, patient_type ";

  /**
   * Gets the revnue accrual ftl report.
   *
   * @param con
   *          the con
   * @param params
   *          the params
   * @param out
   *          the out
   * @return the revnue accrual ftl report
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws TemplateException
   *           the template exception
   * @throws DocumentException
   *           the document exception
   * @throws TransformerException
   *           the transformer exception
   * @throws ParseException
   *           the parse exception
   */
  public byte[] getRevnueAccrualFtlReport(Connection con, Map params, Object out)
      throws SQLException, IOException, TemplateException, DocumentException, TransformerException,
      ParseException {

    String format = (String) params.get("format");
    java.sql.Date fromDate = (java.sql.Date) params.get("fromDate");
    java.sql.Date toDate = (java.sql.Date) params.get("toDate");

    PreparedStatement ps = null;

    try {

      ps = con.prepareStatement(REVENUE_ACCRUAL);
      ps.setDate(1, fromDate);
      ps.setDate(2, toDate);

      List<BasicDynaBean> hospitalRevenueReport = DataBaseUtil.queryToDynaList(ps);

      Map amountMapGroup = ConversionUtils.listBeanToMapMapMapNumeric(hospitalRevenueReport,
          "account_group", "opened_by", "patient_type", "amount");

      Map amountMap = ConversionUtils.listBeanToMapMapNumeric(hospitalRevenueReport, "opened_by",
          "patient_type", "amount");

      params.put("aMap", amountMap);

      List<String> openedbygroup = new ArrayList();
      openedbygroup.addAll(amountMap.keySet());

      List<String> accountgroup = new ArrayList();
      accountgroup.addAll(amountMapGroup.keySet());

      Map claimamountMapGroup = ConversionUtils.listBeanToMapMapMapNumeric(hospitalRevenueReport,
          "account_group", "opened_by", "patient_type", "claim_amount");

      params.put("AmountMap", amountMapGroup);
      params.put("ClaimamountMap", claimamountMapGroup);
      params.put("fromDate", fromDate);
      params.put("toDate", toDate);
      params.put("openedbygroup", openedbygroup);
      params.put("accountgroup", accountgroup);
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    Template template = cfg.getTemplate("HospitalRevenueAccrualReport.ftl");
    StringWriter writer = new StringWriter();
    template.process(params, writer);
    return getFtlReport(template, params, format, out);

  }
}
