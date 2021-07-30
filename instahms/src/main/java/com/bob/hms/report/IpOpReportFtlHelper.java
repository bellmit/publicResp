package com.bob.hms.report;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FtlHelper;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.lowagie.text.DocumentException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

/**
 * The Class IpOpReportFTLHelper.
 */
public class IpOpReportFtlHelper extends FtlHelper {

  /** The cfg. */
  private Configuration cfg = null;

  /**
   * Instantiates a new ip op report FTL helper.
   */
  public IpOpReportFtlHelper() {
    cfg = AppInit.getFmConfig();
  }

  /**
   * The Enum ReturnType.
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
   * Instantiates a new ip op report FTL helper.
   *
   * @param cfg
   *          the cfg
   */
  public IpOpReportFtlHelper(Configuration cfg) {
    this.cfg = cfg;
  }

  /** The Constant OP_STATS. */
  private static final String OP_STATS = " SELECT doctor_name as dept_name,doc_id, " + " SUM"
      + "(m_first_visit) as m_first_visit, SUM(m_re_visit) as m_revisit, " + " SUM"
      + "(m_referred) as m_referred " + " ,SUM(f_first_visit) as f_first_visit, SUM"
      + "(f_re_visit) as f_revisit, SUM(f_referred) as f_referred " + " FROM ( SELECT d"
      + ".doctor_name, dc.doctor_name as doc_id," + " CASE WHEN (pr.revisit='N' AND "
      + "patient_gender = 'M') THEN 1 else 0 end as m_first_visit, " + " CASE WHEN (pr"
      + ".revisit='N' AND patient_gender = 'F') THEN 1 else 0 end as f_first_visit, " + " "
      + "CASE WHEN (pr.revisit='Y' AND patient_gender='M') THEN 1 else 0 END AS m_re_visit, "
      + " CASE WHEN (pr.revisit='Y' AND patient_gender='F') THEN 1 else 0 END AS " + "f_re_visit, "
      + " CASE WHEN ( ((dc.presc_doctor_id IS NOT NULL AND dc" + ".presc_doctor_id != '') AND "
      + " (dc.presc_doctor_id != dc.doctor_name)) AND "
      + "patient_gender='M') THEN 1 ELSE 0 end as m_referred, " + " CASE WHEN ( ((dc"
      + ".presc_doctor_id IS NOT NULL AND dc.presc_doctor_id != '') AND " + " (dc"
      + ".presc_doctor_id != dc.doctor_name)) AND patient_gender='F') THEN 1 ELSE 0 end as "
      + "f_referred " + "  FROM doctor_consultation dc" + " JOIN patient_registration pr on"
      + "(pr.patient_id=dc.patient_id) " + " JOIN patient_details pd on (pd.mr_no = pr.mr_no)" + " "
      + " JOIN doctors d on (d.doctor_id = dc.doctor_name ) WHERE pr.visit_type= 'o' "
      + " AND pr.reg_date between ? AND ? AND (0 = ? OR pr.center_id = ?) )as foo  GROUP BY"
      + " doc_id,dept_name ";

  /** The Constant OP_STATS_DEPT_WISE. */
  private static final String OP_STATS_DEPT_WISE = "SELECT dept_name,SUM(m_first_visit) " + " as"
      + " m_first_visit, SUM(m_re_visit) as m_revisit, " + " SUM(m_referred) as m_referred "
      + " ,SUM(f_first_visit) as f_first_visit, SUM(f_re_visit) as f_revisit, SUM(f_referred) "
      + "as f_referred " + " FROM ( SELECT dept.dept_name, d.doctor_name, dc.doctor_name as "
      + "doc_id, " + " CASE WHEN ( pr.revisit = 'N' AND patient_gender = 'M') THEN 1 else 0 "
      + "end as m_first_visit," + " CASE WHEN ( pr.revisit = 'N' AND patient_gender = 'F') "
      + "THEN 1 else 0 end as f_first_visit," + " CASE WHEN ( pr.revisit = 'Y' AND "
      + "patient_gender= 'M') THEN 1 else 0 END AS m_re_visit," + " CASE WHEN ( pr.revisit = "
      + "'Y' AND patient_gender= 'F') THEN 1 else 0 END AS f_re_visit," + " CASE WHEN ( ((dc"
      + ".presc_doctor_id IS NOT NULL AND dc.presc_doctor_id != '') AND " + " (dc"
      + ".presc_doctor_id != dc.doctor_name)) AND patient_gender='M') THEN 1 ELSE 0 end as "
      + "m_referred, " + " CASE WHEN ( ((dc.presc_doctor_id IS NOT NULL AND dc.presc_doctor_id"
      + " != '') AND " + " (dc.presc_doctor_id != dc.doctor_name)) AND patient_gender='F') "
      + "THEN 1 ELSE 0 end as f_referred " + " FROM doctor_consultation dc " + " JOIN "
      + "patient_registration pr on(pr.patient_id=dc.patient_id)  " + " JOIN patient_details "
      + "pd on(pd.mr_no=pr.mr_no) " + " JOIN doctors d on (d.doctor_id = dc.doctor_name) "
      + " JOIN department dept on (dept.dept_id = pr.admitted_dept) " + " WHERE pr"
      + ".visit_type= 'o' AND pr.reg_date between ? AND ? AND (0 = ? OR pr.center_id = ?))as "
      + "foo  GROUP BY dept_name";

  /**
   * Gets the op stats ftl report.
   *
   * @param con
   *          the con
   * @param params
   *          the params
   * @param out
   *          the out
   * @return the op stats ftl report
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws TemplateException
   *           the template exception
   * @throws DocumentException
   *           the document exception
   * @throws XPathExpressionException
   *           the x path expression exception
   * @throws TransformerException
   *           the transformer exception
   */
  public String getOpStatsFtlReport(Connection con, Map params, OutputStream out)
      throws SQLException, IOException, TemplateException, DocumentException,
      XPathExpressionException, TransformerException {

    String format = (String) params.get("format");
    java.sql.Date fromDate = (java.sql.Date) params.get("fromDate");
    java.sql.Date toDate = (java.sql.Date) params.get("toDate");
    int center = (Integer) params.get("center");
    String report = (String) params.get("report");
    PreparedStatement ps = null;

    try {
      if (report.equals("ops")) {
        ps = con.prepareStatement(OP_STATS);
      } else {
        ps = con.prepareStatement(OP_STATS_DEPT_WISE);
      }

      ps.setDate(1, fromDate);
      ps.setDate(2, toDate);
      ps.setInt(3, center);
      ps.setInt(4, center);
      List stats = DataBaseUtil.queryToDynaList(ps);
      params.put("list", ConversionUtils.copyListDynaBeansToMap(stats));
      params.put("fromDate", fromDate);
      params.put("toDate", toDate);
      params.put("center", center);
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    Template template = cfg.getTemplate("OPStatisticsReport.ftl");
    HtmlConverter hc = new HtmlConverter();
    String textReport = null;
    StringWriter writer = new StringWriter();
    template.process(params, writer);
    String htmlContent = writer.toString();
    BasicDynaBean printprefs = PrintConfigurationsDAO
        .getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT);
    if (format.equals("pdf")) {
      hc.writePdf(out, htmlContent);
      out.close();
    } else {

      textReport = new String(hc.getText(htmlContent, "", printprefs, true, true));
    }
    return textReport;
  }

  /** The Constant IP_STATS_DEPT_WISE. */
  private static final String IP_STATS_DEPT_WISE = "SELECT dept_name, sum(COALESCE( po, 0)) as "
      + "PO, " + " sum(COALESCE( admits, 0)) as ADMISSION, " + " sum(COALESCE( discharge, 0)) "
      + "as DISCHARGE, sum(COALESCE(noofdeath,0)) as DEATH, " + " sum(COALESCE( po, 0))+sum"
      + "(COALESCE( admits, 0))-sum(COALESCE( discharge, 0))" + " -sum(COALESCE(noofdeath,0)"
      + ") as IPDAYS, " + " sum(COALESCE(durstay,0)) as durstay, sum(COALESCE(mlc,0)) as mlc, " + ""
      + " sum(COALESCE(beds,0)) as beds, " + " (sum(COALESCE( po, 0))+sum(COALESCE( "
      + "admits, 0))-sum(COALESCE( discharge, 0))" + " -sum(COALESCE(noofdeath,0)))/365 as "
      + "abo, " + " CASE WHEN (sum(COALESCE(beds,0))=0::int) THEN 0 " + " ELSE round((((sum"
      + "(COALESCE( po, 0))+sum(COALESCE( admits, 0))-sum(COALESCE( discharge, 0))" + " -sum"
      + "(COALESCE(noofdeath,0))) " + " /365/sum(COALESCE(beds,0))) * 100),2)  END as borate " + ""
      + " FROM ( "
      // previous occupancy
      + " SELECT dept_name, sum(m_po+f_po+c_po) as po, 0 as admits, 0 as discharge,0 as "
      + "noofdeath, " + " 0 as durstay, 0 as mlc, 0 as beds " + " FROM (SELECT dep.dept_name," + " "
      + " CASE WHEN (patient_gender = 'M' AND get_patient_age(dateofbirth, "
      + "expected_dob) >=14)  " + " THEN COUNT(patient_id) ELSE 0 END as m_po, " + " CASE "
      + "WHEN (patient_gender = 'F' AND get_patient_age(dateofbirth, expected_dob) >=14) "
      + " THEN COUNT(patient_id) ELSE 0  END as f_po, " + " CASE WHEN ( get_patient_age"
      + "(dateofbirth, expected_dob) <14) " + " THEN COUNT(patient_id) ELSE 0  END as c_po "
      + " FROM patient_registration pr " + " JOIN patient_details pd using (mr_no) " + " "
      + "LEFT JOIN discharge_type_master dtm ON(dtm.discharge_type_id = pr.discharge_type_id)" + ""
      + " LEFT JOIN department dep ON (pr.admitted_dept = dep.dept_id) " + " WHERE "
      + "visit_type ='i' AND date(pr.reg_date) <  ?  " + " AND (date(discharge_date) >=  ? OR"
      + " pr.status ='A') AND coalesce(dtm.discharge_type, '')!='Admission Cancelled' " + // 1,2
      " AND (0 = ? OR  pr.center_id =? ) " + // 3,4
      " GROUP BY admitted_dept, dep.dept_name, patient_gender, dateofbirth, expected_dob) "
      + "as foo " + " GROUP BY  dept_name UNION "
      // admission
      + " SELECT dept_name, 0 as po, sum(m_admits+f_admits+c_admits) as admits, 0 as "
      + "discharge,0 as noofdeath, " + " 0 as durstay, 0 as mlc, 0 as beds " + " FROM (SELECT"
      + " dep.dept_name, " + " CASE WHEN (patient_gender = 'M' AND get_patient_age"
      + "(dateofbirth, expected_dob) >=14 ) " + " THEN COUNT(*) ELSE 0 END as m_admits, " + ""
      + " CASE WHEN (patient_gender = 'F' AND get_patient_age(dateofbirth, expected_dob) "
      + ">=14) " + " THEN COUNT(*) ELSE 0 END as f_admits, " + " CASE WHEN ( get_patient_age"
      + "(dateofbirth, expected_dob) <14) " + " THEN COUNT(*) " + " ELSE 0 " + " END as "
      + "c_admits " + " FROM patient_registration pr " + " LEFT JOIN discharge_type_master "
      + "dtm ON(dtm.discharge_type_id = pr.discharge_type_id)" + " JOIN patient_details pd "
      + "using (mr_no) " + " LEFT JOIN department dep ON (pr.admitted_dept = dep.dept_id)"
      + " WHERE visit_type = 'i' AND coalesce(dtm.discharge_type, '')!='Admission Cancelled' "
      + "AND pr.reg_date BETWEEN  ? and  ? " + // 5,6
      " AND (0 = ? OR  pr.center_id =? ) " + // 7,8

      " GROUP BY dep.dept_name, patient_gender, dateofbirth, expected_dob) as foo " + " "
      + "group by dept_name  UNION "
      // discharge
      + " SELECT dept_name, 0 as po, 0 as admits, sum"
      + "(m_noofdischarge+f_noofdischarge+c_noofdischarge) " + " as discharge, 0 as "
      + "noofdeath, 0 as durstay,0 as mlc, 0 as beds " + " FROM (SELECT dep.dept_name, " + " "
      + "CASE WHEN (patient_gender = 'M' AND get_patient_age(dateofbirth, expected_dob) >=14)" + " "
      + " THEN COUNT(*) ELSE 0  END as m_noofdischarge, " + " CASE WHEN (patient_gender"
      + " = 'F' AND get_patient_age(dateofbirth, expected_dob) >=14) " + " THEN COUNT(*) ELSE"
      + " 0 END as f_noofdischarge, " + " CASE WHEN ( get_patient_age(dateofbirth, "
      + "expected_dob) <14) " + " THEN COUNT(*) ELSE 0 END as c_noofdischarge " + " FROM "
      + "patient_registration pr " + " join patient_details pd using (mr_no) " + " LEFT JOIN "
      + "discharge_type_master dtm ON(dtm.discharge_type_id = pr.discharge_type_id)" + " left"
      + " join department dep ON (pr.admitted_dept = dep.dept_id) " + " WHERE visit_type = "
      + "'i' AND pr.discharge_flag='D' AND " + " (coalesce(dtm.discharge_type, '') NOT IN "
      + "('Death', 'Admission Cancelled')) AND " + " discharge_date BETWEEN  ? and  ? "
      // 9,10
      + " AND (0 = ? OR  pr.center_id =? ) "
      // 11,12
      + " GROUP BY dep.dept_name, patient_gender, dateofbirth, expected_dob) as foo " + " "
      + "GROUP BY dept_name UNION "
      // deaths
      + " SELECT dept_name, 0 as po, 0 as admits, 0 as discharge, sum"
      + "(m_noofdeath+f_noofdeath+c_noofdeath) " + " as noofdeath, 0 as durstay, 0 as mlc, 0 "
      + "as beds " + " FROM (SELECT dep.dept_name, " + " CASE WHEN (patient_gender = 'M' AND "
      + "get_patient_age(dateofbirth, expected_dob) >=14) " + " THEN COUNT(*) ELSE 0 END as "
      + "m_noofdeath, " + " CASE WHEN (patient_gender = 'F' AND get_patient_age(dateofbirth, "
      + "expected_dob) >=14) " + " THEN COUNT(*) ELSE 0 END as f_noofdeath, " + " CASE WHEN ("
      + " get_patient_age(dateofbirth, expected_dob) <14) " + " THEN COUNT(*) ELSE 0 END as "
      + "c_noofdeath " + " FROM patient_registration pr " + " JOIN patient_details pd using "
      + "(mr_no) " + " LEFT JOIN discharge_type_master dtm ON(dtm.discharge_type_id = pr"
      + ".discharge_type_id)" + " LEFT JOIN department dep ON (pr.admitted_dept = dep"
      + ".dept_id) " + " WHERE visit_type = 'i' AND dtm.discharge_type = 'Death' " + " AND "
      + "discharge_date BETWEEN  ? and  ? " + // 13,14
      " AND (0 = ? OR  pr.center_id =? ) " + // 15,16
      " GROUP BY dep.dept_name, patient_gender, dateofbirth, expected_dob) as foo " + " "
      + "group by dept_name UNION "
      // mlc
      + " SELECT dept_name, 0 as po, 0 as admits, 0 as discharge, 0 as noofdeath, " + " 0 as "
      + "durstay, sum(m_mlc+f_mlc+c_mlc) as mlc, 0 as beds " + " FROM (SELECT dep.dept_name, " + ""
      + " CASE WHEN (patient_gender = 'M' AND get_patient_age(dateofbirth, expected_dob)"
      + " >=14) " + " THEN COUNT(mlc_status) ELSE 0 " + " END as m_mlc, " + " CASE WHEN "
      + "(patient_gender = 'F' AND get_patient_age(dateofbirth, expected_dob) >=14) " + " "
      + "THEN COUNT(mlc_status) ELSE 0 END as f_mlc, " + " CASE WHEN ( get_patient_age"
      + "(dateofbirth, expected_dob) <14) " + " THEN COUNT(mlc_status) ELSE 0 END as c_mlc "
      + " FROM patient_registration pr " + " join patient_details pd using (mr_no) " + " "
      + "LEFT JOIN discharge_type_master dtm ON(dtm.discharge_type_id = pr.discharge_type_id)" + ""
      + " LEFT JOIN department dep ON (pr.admitted_dept = dep.dept_id) " + " WHERE "
      + "visit_type = 'i' AND mlc_status = 'Y' AND coalesce(dtm.discharge_type, '')"
      + "!='Admission Cancelled' " + " AND pr.reg_date BETWEEN  ?  AND  ?  " + // 17,18
      " AND (0 = ? OR  pr.center_id =? ) " + // 19,20
      " GROUP BY dep.dept_name, patient_gender, dateofbirth, expected_dob) as foo " + " "
      + "group by dept_name  UNION "
      // beds
      + " SELECT dept_name, 0 as po, 0 as admits, 0 as discharge, 0 as noofdeath, " + " 0 as "
      + "durstay, 0 as mlc, sum(beds) as beds " + " FROM ( SELECT dep.dept_name, COUNT"
      + "(distinct bed_id) AS beds " + " FROM patient_registration pr " + " JOIN "
      + "ip_bed_details using (patient_id) " + " JOIN bed_names using (bed_id) " + " LEFT "
      + "JOIN department dep ON (pr.admitted_dept = dep.dept_id) " + " WHERE reg_date BETWEEN"
      + "  ?  AND  ? " + // 21,22
      " AND (0 = ? OR  pr.center_id =? ) " + // 23,24

      " GROUP BY dep.dept_name ) as foo " + " GROUP BY dept_name  UNION "
      // duration of stay
      + " SELECT dept_name,0 as po, 0 as admits, 0 as discharge,0 as noofdeath, " + " sum"
      + "(coalesce(ROUND(m_dur_stay/(60*60*24), 0), 0)+coalesce(ROUND(f_dur_stay/(60*60*24), "
      + "0), 0)+  " + " coalesce(ROUND(c_dur_stay/(60*60*24), 0), 0)) as durstay,0 as mlc, 0 "
      + "as beds " + " FROM (SELECT dep.dept_name, "
      + " case when discharge_date is not null and discharge_date = pr.reg_date " + " then "
      + "(case when (patient_gender = 'M' AND get_patient_age(dateofbirth, expected_dob) "
      + ">=14) " + " then sum(extract (EPOCH from timestamp_smaller(coalesce(timedate_pl"
      + "(discharge_time, " + " discharge_date), ?),?)- " + // 25,26
      " timestamp_larger(timedate_pl(pr.reg_time, pr.reg_date),  ?))::numeric)+(24*60*60) "
      + "else 0 end) " + " else (case when (patient_gender = 'M' AND get_patient_age"
      + "(dateofbirth, expected_dob) >=14) " + " then sum(extract (EPOCH from "
      + "timestamp_smaller(coalesce(timedate_pl(discharge_time, " + " discharge_date), ?), ?)" + " "
      // 27,28,29
      + " -timestamp_larger(timedate_pl(pr.reg_time, pr.reg_date),  ?))::numeric) else 0 end "
      + ") " + " end as m_dur_stay , " + // 30

      " case when discharge_date is not null and discharge_date = pr.reg_date " + " then "
      + "(case when (patient_gender = 'F' AND get_patient_age(dateofbirth, expected_dob) "
      + ">=14) " + " then sum(extract (EPOCH from timestamp_smaller(coalesce(timedate_pl"
      + "(discharge_time, " + " discharge_date), ?), ?) " + // 31,32
      " -timestamp_larger(timedate_pl(pr.reg_time, pr.reg_date), ?))::numeric)+(24*60*60) "
      + "else 0 end) " + " else ( case when (patient_gender = 'F' AND get_patient_age"
      + "(dateofbirth, expected_dob) >=14) " + " then sum(extract (EPOCH from "
      + "timestamp_smaller(coalesce(timedate_pl(discharge_time, " + " discharge_date), ?), ?)" + " "
      + // 33,34,35
      " -timestamp_larger(timedate_pl(pr.reg_time, pr.reg_date), ?))::numeric) else 0 end  " + ") "
      + // 36
      " end as f_dur_stay ,"

      + " case when discharge_date is not null and discharge_date = pr.reg_date " + " then "
      + "(CASE WHEN (get_patient_age(dateofbirth, expected_dob) <14) " + " THEN sum(extract "
      + "(EPOCH from timestamp_smaller(coalesce(timedate_pl(discharge_time, " + " "
      + "discharge_date), ?),?)- " + // 37,38
      " timestamp_larger(timedate_pl(pr.reg_time, pr.reg_date), ?))::numeric)+(24*60*60) "
      + "else 0 end ) " + " else ( CASE WHEN (get_patient_age(dateofbirth, expected_dob) <14)" + " "
      + " THEN sum(extract (EPOCH from timestamp_smaller(coalesce(timedate_pl" + "(discharge_time, "
      + " discharge_date), ?), ?) " + // 39,40,41
      " -timestamp_larger(timedate_pl(pr.reg_time, pr.reg_date), ?))::numeric) else 0 end  " + ") "
      + " end AS c_dur_stay " + // 42
      " FROM patient_registration pr " + " join patient_details pd using (mr_no) " + " LEFT"
      + " JOIN department dep ON (pr.admitted_dept=dep.dept_id) " + " WHERE visit_type ='i' "
      + "AND " + " ( ( date(pr.reg_date) between ? and ? OR " + // 43, 44
      " date(discharge_date) between ? and ?) OR ( date(pr.reg_date) < ?  AND "
      // 45
      + " ( date(discharge_date)  > ? OR (pr.status ='A' and date(discharge_date) >= ?) ) ) )" + " "
      // 46,
      // 47
      + " AND (0 = ? OR  pr.center_id =? ) "// 48, 49
      + " GROUP BY dep.dept_name, patient_gender, dateofbirth, expected_dob, discharge_date, "
      + "pr.reg_date ) " + " as foo group by dept_name " + " ) as foo2 " + " GROUP BY dept_name "
      + " ORDER BY dept_name ";

  /** The Constant IP_STATS_DEPT_WISE_WITH_GENDER_BREAKUP. */
  private static final String IP_STATS_DEPT_WISE_WITH_GENDER_BREAKUP = "SELECT dept_name, "
      + "patient_gender, " + " sum(admits) as admits,sum(discharge) as discharge, sum"
      + "(admits+po-discharge-death) as total_occupancy " + " FROM ( SELECT dep.dept_name,"
      + "case when get_patient_age(dateofbirth, expected_dob) <14 " + " THEN 'C' ELSE "
      + "patient_gender END AS patient_gender, 0 as admits,0 as discharge, " + " count(*) as "
      + "po, 0 as death " + " FROM patient_registration pr " + " join patient_details pd "
      + "using (mr_no) " + " LEFT JOIN discharge_type_master dtm ON(dtm.discharge_type_id = "
      + "pr.discharge_type_id)" + " LEFT JOIN department dep ON (pr.admitted_dept = dep"
      + ".dept_id) " + " WHERE visit_type ='i' AND coalesce(dtm.discharge_type, '')"
      + "!='Admission Cancelled' " + " AND date(pr.reg_date) < ? AND dep.dept_name is not "
      + "null AND " + " (date(discharge_date) >= ? OR pr.status ='A') " + // 1,2
      " AND (0 = ? OR  pr.center_id =? ) " + // 3,4
      " GROUP BY dep.dept_name, patient_gender, dateofbirth, expected_dob  UNION ALL "
      + " SELECT dep.dept_name, case when get_patient_age(dateofbirth, expected_dob) <14 "
      + " THEN 'C' ELSE patient_gender END AS patient_gender, count(*) as admits,0 as "
      + "discharge," + " 0 as po, 0 as death " + " FROM patient_registration pr " + " join "
      + "patient_details pd using (mr_no) " + " LEFT JOIN discharge_type_master dtm ON(dtm"
      + ".discharge_type_id = pr.discharge_type_id)" + " LEFT JOIN department dep ON (pr"
      + ".admitted_dept = dep.dept_id) " + " WHERE visit_type = 'i' AND coalesce(dtm"
      + ".discharge_type, '')!='Admission Cancelled' AND dep.dept_name is not null AND " + " "
      + "pr.reg_date BETWEEN ? and ? " + // 5,6
      " AND (0 = ? OR  pr.center_id =? ) " + // 7,8
      " GROUP BY dep.dept_name, patient_gender, dateofbirth, expected_dob  UNION ALL "
      + " SELECT dep.dept_name, case when get_patient_age(dateofbirth, expected_dob) <14 "
      + " THEN 'C' ELSE patient_gender END AS patient_gender, 0 as admits,count(*) as "
      + "discharge, " + " 0 as po, 0 as death " + " FROM patient_registration pr " + " join "
      + "patient_details pd using (mr_no) " + " LEFT JOIN discharge_type_master dtm ON(dtm"
      + ".discharge_type_id = pr.discharge_type_id)" + " left join department dep ON (pr"
      + ".admitted_dept = dep.dept_id) " + " WHERE visit_type = 'i' AND pr.discharge_flag='D'"
      + " AND " + " (coalesce(dtm.discharge_type,'') not in ('Death', 'Admission Cancelled'))"
      + " AND dep.dept_name is not null AND" + " discharge_date BETWEEN ? and ? " // 9,10
      + " AND (0 = ? OR  pr.center_id =? ) " // 11,12
      + " GROUP BY dep.dept_name, patient_gender, dateofbirth, expected_dob  UNION ALL "
      + " SELECT dep.dept_name,case when get_patient_age(dateofbirth, expected_dob) <14 " + ""
      + " THEN 'C' ELSE patient_gender END AS patient_gender, 0 as admits,0 as discharge, "
      + " 0 as po, count(*) as death " + " FROM patient_registration pr " + " join "
      + "patient_details pd using (mr_no) " + " LEFT JOIN discharge_type_master dtm ON(dtm"
      + ".discharge_type_id = pr.discharge_type_id)" + " LEFT JOIN department dep ON (pr"
      + ".admitted_dept = dep.dept_id) " + " WHERE visit_type = 'i' AND dtm.discharge_type = "
      + "'Death' AND dep.dept_name is not null AND " + " discharge_date BETWEEN ? and ? "
      // 13,14
      + " AND (0 = ? OR  pr.center_id =? ) " + // 15,16
      " GROUP BY dep.dept_name, patient_gender, dateofbirth, expected_dob) as foo " + " "
      + "GROUP BY dept_name, patient_gender " + " ORDER BY dept_name ";

  /** The Constant IP_STATS_CONSULTANT_WISE. */
  private static final String IP_STATS_CONSULTANT_WISE = " SELECT doctor_name as dept_name, "
      + " sum(COALESCE( po, 0)) as PO, sum(COALESCE( admits, 0)) as ADMISSION, " + " sum"
      + "(COALESCE( discharge, 0)) as DISCHARGE, sum(COALESCE(noofdeath,0)) as DEATH, " + " "
      + "sum(COALESCE( po, 0))+sum(COALESCE( admits, 0))-sum(COALESCE( discharge, 0)) " + " "
      + "-sum(COALESCE(noofdeath,0)) as IPDAYS,sum(COALESCE(durstay,0)) as durstay " + " FROM"
      + " ( SELECT doctor_name, sum(m_po+f_po+c_po) as po, 0 as admits, 0 as discharge, " + ""
      + " 0 as noofdeath, 0 as durstay " + " FROM (  SELECT CASE WHEN (patient_gender = 'M' "
      + "AND get_patient_age(dateofbirth, expected_dob) >=14 ) " + " THEN 1 ELSE 0 END as "
      + "m_po, " + " CASE WHEN (patient_gender = 'F' AND get_patient_age(dateofbirth, "
      + "expected_dob) >=14 ) " + " THEN 1 ELSE 0 END as f_po, " + " CASE WHEN ( "
      + "get_patient_age(dateofbirth, expected_dob) <14) " + " THEN 1 ELSE 0 END as c_po, "
      + " coalesce((SELECT d.doctor_name FROM doctor_consultation dc " + " LEFT JOIN doctors "
      + "d ON (dc.doctor_name=d.doctor_id) " + " WHERE pr.patient_id=dc.patient_id  ORDER BY "
      + "consultation_id LIMIT 1), '') as doctor_name " + " FROM patient_registration pr "
      + " join patient_details pd using (mr_no) " + " LEFT JOIN discharge_type_master dtm ON"
      + "(dtm.discharge_type_id = pr.discharge_type_id)" + " WHERE visit_type ='i' AND "
      + "coalesce(dtm.discharge_type, '')!='Admission Cancelled' AND date(pr.reg_date) < ? "
      + "AND  " + " (date(discharge_date) >= ? OR pr.status ='A')" + " AND (0 = ? OR  pr"
      + ".center_id =? ) " + ") as foo " + " group by doctor_name  UNION ALL "
      + " SELECT doctor_name, 0 as po, sum(m_admits+f_admits+c_admits) as admits, 0 as "
      + "discharge, " + " 0 as noofdeath, 0 as durstay " + " FROM (SELECT coalesce((SELECT d"
      + ".doctor_name FROM doctor_consultation dc " + " LEFT JOIN doctors d ON (dc"
      + ".doctor_name = d.doctor_id) WHERE pr.patient_id=dc.patient_id " + " ORDER BY "
      + "consultation_id LIMIT 1), '') as doctor_name, " + " CASE WHEN (patient_gender = 'M' "
      + "AND get_patient_age(dateofbirth, expected_dob) >=14 ) " + " THEN 1 ELSE 0 END as "
      + "m_admits, " + " CASE WHEN (patient_gender = 'F' AND get_patient_age(dateofbirth, "
      + "expected_dob) >=14 ) " + " THEN 1 ELSE 0 END as f_admits, " + " CASE WHEN ( "
      + "get_patient_age(dateofbirth, expected_dob) <14) " + " THEN 1 ELSE 0 END as c_admits " + ""
      + " FROM patient_registration pr " + " join patient_details pd using (mr_no)  "
      + " LEFT JOIN discharge_type_master dtm ON(dtm.discharge_type_id = pr" + ".discharge_type_id)"
      + " JOIN doctor_consultation dc on(pr.patient_id=dc.patient_id)" + " "
      + " WHERE visit_type = 'i' AND coalesce(dtm.discharge_type, '')!='Admission "
      + "Cancelled' AND pr.reg_date BETWEEN  ? and ? " + " AND (0 = ? OR  pr.center_id =? ) "
      + ") as foo  GROUP BY doctor_name  UNION ALL "
      + " SELECT doctor_name, 0 as po, 0 as admits, sum"
      + "(m_noofdischarge+f_noofdischarge+c_noofdischarge) " + " as discharge, 0 as "
      + "noofdeath, 0 as durstay " + " FROM (SELECT CASE WHEN (patient_gender = 'M' AND "
      + "get_patient_age(dateofbirth, expected_dob) >=14 ) " + " THEN 1 ELSE 0 END as "
      + "m_noofdischarge, " + " CASE WHEN (patient_gender = 'F' AND get_patient_age"
      + "(dateofbirth, expected_dob) >=14 ) " + " THEN 1 ELSE 0 END as f_noofdischarge, " + ""
      + " CASE WHEN ( get_patient_age(dateofbirth, expected_dob) <14) " + " THEN 1 ELSE 0 END"
      + " as c_noofdischarge, coalesce((SELECT d.doctor_name FROM doctor_consultation dc "
      + " LEFT JOIN doctors d ON (dc.doctor_name = d.doctor_id) WHERE pr.patient_id=dc"
      + ".patient_id " + " ORDER BY consultation_id  LIMIT 1), '') as doctor_name " + " FROM "
      + "patient_registration pr " + " join patient_details pd using (mr_no) " + " LEFT JOIN "
      + "discharge_type_master dtm ON(dtm.discharge_type_id = pr.discharge_type_id)" + " "
      + "WHERE visit_type = 'i' AND pr.discharge_flag='D' AND  " + " (coalesce(dtm"
      + ".discharge_type,'') NOT IN ('Death', 'Admission Cancelled')) AND  " + " "
      + "discharge_date BETWEEN  ? and ?" + " AND (0 = ? OR  pr.center_id =? ) " + " ) as foo" + " "
      + " group by doctor_name  UNION ALL "
      + " SELECT doctor_name, 0 as po, 0 as admits, 0 as discharge, sum"
      + "(m_noofdeath+f_noofdeath+c_noofdeath) " + " as noofdeath, 0 as durstay " + " FROM "
      + "(SELECT CASE WHEN (patient_gender = 'M' AND get_patient_age(dateofbirth, "
      + "expected_dob) >=14 ) " + " THEN 1 ELSE 0 END as m_noofdeath, " + " CASE WHEN "
      + "(patient_gender = 'F' AND get_patient_age(dateofbirth, expected_dob) >=14 ) " + " "
      + "THEN 1 ELSE 0 END as f_noofdeath, " + " CASE WHEN ( get_patient_age(dateofbirth, "
      + "expected_dob) <14) " + " THEN 1 ELSE 0 END as c_noofdeath, " + " coalesce((SELECT d"
      + ".doctor_name " + " FROM doctor_consultation dc LEFT JOIN doctors d ON (dc"
      + ".doctor_name = d.doctor_id) " + " WHERE pr.patient_id=dc.patient_id ORDER BY "
      + "consultation_id LIMIT 1), '') as doctor_name " + " FROM patient_registration pr "
      + " join patient_details pd using (mr_no) " + " LEFT JOIN discharge_type_master dtm ON"
      + "(dtm.discharge_type_id = pr.discharge_type_id)" + " WHERE visit_type = 'i' AND dtm"
      + ".discharge_type = 'Death' AND " + " discharge_date BETWEEN  ? and ? " + " AND (0 = ?"
      + " OR  pr.center_id =? ) " + ") as foo " + " group by doctor_name  UNION ALL "
      + " SELECT doctor_name,0 as po, 0 as admits, 0 as discharge,0 as noofdeath, " + " sum"
      + "(coalesce(ROUND(m_dur_stay/(60*60*24), 0), 0)+coalesce(ROUND(f_dur_stay/(60*60*24), "
      + "0), 0)+ " + " coalesce(ROUND(c_dur_stay/(60*60*24), 0), 0)) as durstay " + " FROM "
      + "(SELECT  doctor_name,case when discharge_date is not null and discharge_date = "
      + "pr_reg_date  " + " then (case when (patient_gender = 'M' AND get_patient_age"
      + "(dateofbirth, expected_dob) >=14 )  " + " then sum(extract (EPOCH from "
      + "timestamp_smaller(coalesce(timedate_pl(pr_discharge_time, " + " pr_discharge_date),"
      + "?),?)-timestamp_larger(timedate_pl(pr_reg_time, " + " pr_reg_date), ?))::numeric)+"
      + "(20*60*60) else 0 end ) " + " else (case when (patient_gender = 'M' AND "
      + "get_patient_age(dateofbirth, expected_dob) >=14 ) " + " then sum(extract (EPOCH from"
      + " timestamp_smaller(coalesce(timedate_pl(pr_discharge_time, " + " pr_discharge_date),"
      + "?), ?) " + " -timestamp_larger(timedate_pl(pr_reg_time, pr_reg_date), ?))::numeric) "
      + "else 0 end  ) " + " end as m_dur_stay, "
      + " case when discharge_date is not null and discharge_date = pr_reg_date " + " then "
      + "(case when (patient_gender = 'F' AND get_patient_age(dateofbirth, expected_dob) >=14"
      + " ) " + " then sum(extract (EPOCH from timestamp_smaller(coalesce(timedate_pl"
      + "(pr_discharge_time, " + " pr_discharge_date),?), ?)-timestamp_larger(timedate_pl"
      + "(pr_reg_time, " + " pr_reg_date), ?))::numeric)+(20*60*60) else 0 end ) " + " else "
      + "(case when (patient_gender = 'F' AND get_patient_age(dateofbirth, expected_dob) >=14"
      + " ) " + " then sum(extract (EPOCH from timestamp_smaller(coalesce(timedate_pl"
      + "(pr_discharge_time, " + " pr_discharge_date), ?), ?)-timestamp_larger(timedate_pl"
      + "(pr_reg_time, " + " pr_reg_date), ?))::numeric) else 0 end) end as f_dur_stay, "
      + " case when discharge_date is not null and discharge_date = pr_reg_date " + " then "
      + "(case when ( get_patient_age(dateofbirth, expected_dob) <14 ) " + " then sum(extract"
      + " (EPOCH from timestamp_smaller(coalesce(timedate_pl(pr_discharge_time, " + " "
      + "pr_discharge_date), ?), ?)-timestamp_larger(timedate_pl(pr_reg_time, " + " "
      + "pr_reg_date), ?))::numeric)+(20*60*60) else 0 end ) " + " else (case when ( "
      + "get_patient_age(dateofbirth, expected_dob) <14 ) " + " then sum(extract (EPOCH from "
      + "timestamp_smaller(coalesce(timedate_pl(pr_discharge_time, " + " pr_discharge_date), "
      + "?), ?)-timestamp_larger(timedate_pl(pr_reg_time, " + " pr_reg_date), ?))::numeric) "
      + "else 0 end ) end as c_dur_stay "
      + " FROM (select pr.reg_date as pr_reg_date, pr.discharge_date as pr_discharge_date, pr"
      + ".discharge_time as " + " pr_discharge_time, pr.reg_time as pr_reg_time , pr.*, pd.*," + " "
      + " coalesce((SELECT d.doctor_name " + " FROM doctor_consultation dc " + " LEFT "
      + "JOIN doctors d ON (dc.doctor_name = d.doctor_id) " + " WHERE pr.patient_id=dc"
      + ".patient_id " + " ORDER BY consultation_id LIMIT 1), '') as doctor_name " + " FROM "
      + "patient_registration pr " + " join patient_details pd using (mr_no) " + " WHERE "
      + "visit_type ='i' AND ( ( date(pr.reg_date) between ? and ? OR " + " date"
      + "(discharge_date) between ? and ?) OR ( date(pr.reg_date) <  ?  " + " AND ( date"
      + "(discharge_date)  > ? OR (status ='A' and date(discharge_date) >= ?) )" + " AND (0 ="
      + " ? OR  pr.center_id =? ) " + " ) ) ) as foo " + " group by doctor_name, "
      + "patient_gender, dateofbirth, expected_dob, discharge_date, pr_reg_date )as foo " + ""
      + " group by doctor_name) as foo " + " WHERE doctor_name !='' " + " GROUP BY "
      + "doctor_name " + " ORDER BY doctor_name ";

  /** The Constant IP_STATS_CONSULTANT_WISE_WITH_GENDER_BREAKUP. */
  private static final String IP_STATS_CONSULTANT_WISE_WITH_GENDER_BREAKUP = "SELECT "
      + "doctor_name " + " as dept_name, patient_gender, sum(admits) as admits, " + " sum"
      + "(discharge) as discharge,sum(admits+po-discharge-death) as total_occupancy " + " "
      + "FROM ( SELECT coalesce((SELECT d.doctor_name " + " FROM doctor_consultation dc " + ""
      + " LEFT JOIN doctors d ON (dc.doctor_name=d.doctor_id) " + " WHERE pr.patient_id=dc"
      + ".patient_id " + " ORDER BY consultation_id LIMIT 1), '') as doctor_name, " + " case "
      + "when get_patient_age(dateofbirth, expected_dob) <14  THEN 'C' ELSE patient_gender "
      + " END AS patient_gender, count(*) as po, 0 as admits, 0 as discharge, 0 as death "
      + " FROM patient_registration pr " + " join patient_details pd using (mr_no) " + " "
      + "LEFT JOIN discharge_type_master dtm ON(dtm.discharge_type_id = pr.discharge_type_id)" + ""
      + " WHERE visit_type ='i' AND coalesce(dtm.discharge_type, '')!='Admission "
      + "Cancelled' AND date(pr.reg_date) < ? AND (date(discharge_date) >= ? OR pr.status " + "='A'"
      + " AND (0 = ? OR  pr.center_id =? ) " + ") "
      + " GROUP BY doctor_name, patient_gender, dateofbirth, expected_dob "
      + " UNION ALL  SELECT coalesce((SELECT d.doctor_name FROM doctor_consultation dc "
      + " LEFT JOIN "
      + "doctors d ON (dc.doctor_name = d.doctor_id) WHERE pr.patient_id=dc.patient_id  " + ""
      + " ORDER BY consultation_id LIMIT 1), '') as doctor_name, " + " case when "
      + "get_patient_age(dateofbirth, expected_dob) <14  THEN 'C' " + " ELSE patient_gender "
      + "END AS patient_gender, " + " 0 as po, count(*) as admits, 0 as discharge, 0 as death" + " "
      + " FROM patient_registration pr  " + " join patient_details pd using (mr_no) "
      + " LEFT JOIN discharge_type_master dtm ON(dtm.discharge_type_id = pr" + ".discharge_type_id)"
      + " WHERE visit_type = 'i' AND coalesce(dtm.discharge_type, '')"
      + "!='Admission Cancelled' AND pr.reg_date BETWEEN ? and ? " + " AND (0 = ? OR  pr"
      + ".center_id =? ) " + " GROUP BY doctor_name, patient_gender, dateofbirth, "
      + "expected_dob " + " UNION ALL "
      + " SELECT coalesce((SELECT d.doctor_name  FROM doctor_consultation dc " + " LEFT JOIN "
      + "doctors d ON (dc.doctor_name = d.doctor_id) WHERE pr.patient_id=dc.patient_id " + " "
      + "ORDER BY consultation_id LIMIT 1), '') as doctor_name, " + " case when "
      + "get_patient_age(dateofbirth, expected_dob) <14  THEN 'C' ELSE patient_gender " + " "
      + "END AS patient_gender, " + " 0 as po, 0 as admits, count(*) as discharge, 0 as death" + " "
      + " FROM patient_registration pr  " + " join patient_details pd using (mr_no) "
      + " LEFT JOIN discharge_type_master dtm ON(dtm.discharge_type_id = pr" + ".discharge_type_id)"
      + " WHERE visit_type = 'i' AND pr.discharge_flag='D' AND " + " "
      + "(coalesce(dtm.discharge_type, '') NOT IN ('Death', 'Admission Cancelled')) AND "
      + "discharge_date BETWEEN ? and ?" + " AND (0 = ? OR  pr.center_id =? ) " + " GROUP BY "
      + "doctor_name, patient_gender, dateofbirth, expected_dob " + " UNION ALL "
      + " SELECT  coalesce((SELECT d.doctor_name FROM doctor_consultation dc " + " LEFT JOIN "
      + "doctors d ON (dc.doctor_name = d.doctor_id) WHERE pr.patient_id=dc.patient_id " + " "
      + "ORDER BY consultation_id LIMIT 1), '') as doctor_name, " + " case when "
      + "get_patient_age(dateofbirth, expected_dob) <14  THEN 'C' ELSE patient_gender " + " "
      + "END AS patient_gender, 0 as po, 0 as admits, 0 as discharge, count(*) as death  "
      + " FROM patient_registration pr  " + " join patient_details pd using (mr_no)  " + " "
      + "LEFT JOIN discharge_type_master dtm ON(dtm.discharge_type_id = pr.discharge_type_id)" + ""
      + " WHERE visit_type = 'i' AND dtm.discharge_type = 'Death' AND discharge_date "
      + "BETWEEN ? and ? " + " AND (0 = ? OR  pr.center_id =? ) " + " GROUP BY doctor_name, "
      + "patient_gender, dateofbirth, expected_dob) as foo  " + " WHERE doctor_name != '' "
      + " GROUP BY doctor_name, patient_gender  " + " ORDER BY doctor_name";

  /**
   * Gets the IP stats dept wise ftl report.
   *
   * @param con
   *          the con
   * @param params
   *          the params
   * @param out
   *          the out
   * @return the IP stats dept wise ftl report
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws TemplateException
   *           the template exception
   * @throws DocumentException
   *           the document exception
   * @throws XPathExpressionException
   *           the x path expression exception
   * @throws TransformerException
   *           the transformer exception
   * @throws ParseException
   *           the parse exception
   */
  public String getIpStatsDeptWiseFtlReport(Connection con, Map params, OutputStream out)
      throws SQLException, IOException, TemplateException, DocumentException,
      XPathExpressionException, TransformerException, ParseException {

    String format = (String) params.get("format");
    java.sql.Date fromDate = (java.sql.Date) params.get("fromDate");
    java.sql.Date toDate = (java.sql.Date) params.get("toDate");
    java.sql.Timestamp toTimeStamp = null;
    int center = (Integer) params.get("center");
    DateUtil dateUtil = new DateUtil();

    if (toDate.getTime() > DateUtil.getCurrentDate().getTime()) {
      toTimeStamp = DateUtil.parseTimestamp(
          dateUtil.getDateFormatter().format(DateUtil.getCurrentDate()), "23:59:59");
    } else {
      toTimeStamp = DateUtil.parseTimestamp(dateUtil.getDateFormatter().format(toDate), "23:59:59");
    }

    String report = (String) params.get("report");

    PreparedStatement ps = null;

    try {

      if (report.equals("ipsd") || report.equals("ipsdabo")) {
        ps = con.prepareStatement(IP_STATS_DEPT_WISE);
      }
      if (report.equals("ipsdg")) {
        ps = con.prepareStatement(IP_STATS_DEPT_WISE_WITH_GENDER_BREAKUP);
      }
      if (report.equals("ipsc")) {
        ps = con.prepareStatement(IP_STATS_CONSULTANT_WISE);
      }
      if (report.equals("ipscg")) {
        ps = con.prepareStatement(IP_STATS_CONSULTANT_WISE_WITH_GENDER_BREAKUP);
      }

      int index = 0;

      ps.setDate(++index, fromDate); // 1,2
      ps.setDate(++index, fromDate);

      ps.setInt(++index, center); // 3,4
      ps.setInt(++index, center);

      ps.setDate(++index, fromDate); // 5,6
      ps.setDate(++index, toDate);

      ps.setInt(++index, center); // 7,8
      ps.setInt(++index, center);

      ps.setDate(++index, fromDate); // 9,10
      ps.setDate(++index, toDate);

      ps.setInt(++index, center); // 11,12
      ps.setInt(++index, center);

      ps.setDate(++index, fromDate); // 13,14
      ps.setDate(++index, toDate);

      ps.setInt(++index, center); // 15,16
      ps.setInt(++index, center);

      if (report.equals("ipsd") || report.equals("ipsdabo")) {
        ps.setDate(++index, fromDate); // 17,18
        ps.setDate(++index, toDate);
        ps.setInt(++index, center); // 19,20
        ps.setInt(++index, center);
        ps.setDate(++index, fromDate); // 21,22
        ps.setDate(++index, toDate);
        ps.setInt(++index, center); // 23,24
        ps.setInt(++index, center);
      }

      if (report.equals("ipsd") || report.equals("ipsdabo") || report.equals("ipsc")) {
        ps.setTimestamp(++index, toTimeStamp); // 25
        ps.setTimestamp(++index, toTimeStamp); // 26
        ps.setDate(++index, fromDate);// 27

        ps.setTimestamp(++index, toTimeStamp);// 28
        ps.setTimestamp(++index, toTimeStamp);// 29
        ps.setDate(++index, fromDate);// 30

        ps.setTimestamp(++index, toTimeStamp);// 31
        ps.setTimestamp(++index, toTimeStamp);// 32
        ps.setDate(++index, fromDate);// 33

        ps.setTimestamp(++index, toTimeStamp); // 34
        ps.setTimestamp(++index, toTimeStamp); // 35
        ps.setDate(++index, fromDate); // 36

        ps.setTimestamp(++index, toTimeStamp); // 37
        ps.setTimestamp(++index, toTimeStamp); // 38
        ps.setDate(++index, fromDate);

        ps.setTimestamp(++index, toTimeStamp); // 39
        ps.setTimestamp(++index, toTimeStamp); // 40
        ps.setDate(++index, fromDate);

        ps.setDate(++index, fromDate); // 41
        ps.setDate(++index, toDate);// 42
        ps.setDate(++index, fromDate);// 43
        ps.setDate(++index, toDate);// 44
        ps.setDate(++index, fromDate);// 45
        ps.setDate(++index, toDate);// 46

        ps.setDate(++index, fromDate);// 47

        ps.setInt(++index, center); // 48,49
        ps.setInt(++index, center);
      }

      List list = DataBaseUtil.queryToDynaList(ps);

      List<BasicDynaBean> ipStatsReport = list;

      if (report.equals("ipsd") || report.equals("ipsc") || report.equals("ipsdabo")) {
        Map poMap = ConversionUtils.listBeanToMapNumeric(ipStatsReport, "dept_name", "po");
        Map admitsMap = ConversionUtils.listBeanToMapNumeric(ipStatsReport, "dept_name",
            "admission");

        if (report.equals("ipsdabo")) {
          Map mlcMap = ConversionUtils.listBeanToMapNumeric(ipStatsReport, "dept_name", "mlc");
          Map bedsMap = ConversionUtils.listBeanToMapNumeric(ipStatsReport, "dept_name", "beds");
          Map boRateMap = ConversionUtils.listBeanToMapNumeric(ipStatsReport, "dept_name",
              "borate");
          Map aboMap = ConversionUtils.listBeanToMapNumeric(ipStatsReport, "dept_name", "abo");
          params.put("MlcMap", mlcMap);
          params.put("BedsMap", bedsMap);
          params.put("BoRateMap", boRateMap);
          params.put("AboMap", aboMap);
        }

        Map dischargeMap = ConversionUtils.listBeanToMapNumeric(ipStatsReport, "dept_name",
            "discharge");
        Map deathMap = ConversionUtils.listBeanToMapNumeric(ipStatsReport, "dept_name", "death");
        Map ipDaysMap = ConversionUtils.listBeanToMapNumeric(ipStatsReport, "dept_name", "ipdays");
        Map durStayMap = ConversionUtils.listBeanToMapNumeric(ipStatsReport, "dept_name",
            "durstay");

        params.put("POMap", poMap);
        params.put("AdmitsMap", admitsMap);
        params.put("DischargeMap", dischargeMap);
        params.put("DeathMap", deathMap);
        params.put("IPDaysMap", ipDaysMap);
        params.put("DurStayMap", durStayMap);

        List<String> departments = new ArrayList();

        Set poKeys = poMap.keySet();

        Iterator itr = poKeys.iterator();

        while (itr.hasNext()) {
          Object ob = itr.next();
          if (ob != null) {
            departments.add((String) ob);
          }
        }

        params.put("departments", departments);

      }

      if (report.equals("ipsdg") || report.equals("ipscg")) {
        Map admitsMap = ConversionUtils.listBeanToMapMapNumeric(ipStatsReport, "dept_name",
            "patient_gender", "admits");

        Map dischargeMap = ConversionUtils.listBeanToMapMapNumeric(ipStatsReport, "dept_name",
            "patient_gender", "discharge");

        Map totalOccupancyMap = ConversionUtils.listBeanToMapMapNumeric(ipStatsReport, "dept_name",
            "patient_gender", "total_occupancy");

        params.put("GAdmitsMap", admitsMap);
        params.put("GDischargeMap", dischargeMap);
        params.put("GTotalOccupancyMap", totalOccupancyMap);

        List<String> departments = new ArrayList();

        Set admitKeys = admitsMap.keySet();

        Iterator itr = admitKeys.iterator();

        while (itr.hasNext()) {
          Object ob = itr.next();
          if (ob != null) {
            departments.add((String) ob);
          }
        }

        params.put("departments", departments);
      }

      params.put("fromDate", fromDate);
      params.put("toDate", toDate);
      params.put("center", center);
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    Template template = cfg.getTemplate("IPStatisticsReport.ftl");
    HtmlConverter hc = new HtmlConverter();
    String textReport = null;
    StringWriter writer = new StringWriter();
    template.process(params, writer);
    String htmlContent = writer.toString();
    BasicDynaBean printprefs = PrintConfigurationsDAO
        .getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT);
    if (format.equals("pdf")) {
      hc.writePdf(out, htmlContent);
      out.close();
    } else {

      textReport = new String(hc.getText(htmlContent, "", printprefs, true, true));
    }
    return textReport;

  }

}
