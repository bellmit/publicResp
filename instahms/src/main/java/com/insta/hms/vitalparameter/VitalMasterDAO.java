package com.insta.hms.vitalparameter;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.emr.EMRDoc;
import com.insta.hms.emr.EMRInterface;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;

import freemarker.core.InvalidReferenceException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class VitalMasterDAO.
 */
public class VitalMasterDAO extends GenericDAO {
  
  /** The log. */
  static Logger log = LoggerFactory.getLogger(VitalMasterDAO.class);

  /**
   * Instantiates a new vital master DAO.
   */
  public VitalMasterDAO() {
    super("vital_parameter_master");
  }

  /** The Constant GET_VITAL_FIELDS. */
  public static final String GET_VITAL_FIELDS = " SELECT * ";

  /** The Constant GET_VITAL_COUNT. */
  private static final String GET_VITAL_COUNT = "SELECT count(param_id) ";

  /** The Constant GET_VITAL_TABLE. */
  private static final String GET_VITAL_TABLE = " FROM vital_parameter_master ";

  /**
   * Insert vital parameter details.
   *
   * @param con the con
   * @param fields the fields
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean insertVitalParameterDetails(Connection con, HashMap fields) throws SQLException {
    return DataBaseUtil.dynaInsert(con, "vital_parameter_master", fields);
  }

  /** The Constant GET_ALL_PARAMS. */
  private static final String GET_ALL_PARAMS = " SELECT param_id, param_label, param_order,"
      + " param_uom, mandatory_in_tx, visit_type, expr_for_calc_result, system_vital "
      + " FROM vital_parameter_master WHERE param_status='A' ";

  /**
   * Gets the all params.
   *
   * @param container the container
   * @param visit the visit
   * @return the all params
   * @throws SQLException the SQL exception
   */
  public List getAllParams(String container, String visit) throws SQLException {
    container = container == null ? "" : container;
    visit = visit == null ? "" : visit;
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      String query = GET_ALL_PARAMS;
      if (!visit.equals("") && !container.equals("V")) {
        query += " AND param_container IN ('I','O') AND (visit_type = ? OR visit_type is null) ";
      } else {
        query += " AND param_container = 'V' AND (visit_type = ? OR visit_type is null) ";
      }
      query += " ORDER BY param_container DESC, param_order ASC";
      ps = con.prepareStatement(query);
      if (!visit.equals("")) {
        ps.setString(1, visit);
      }
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_VITAL_PARAMS. */
  private static final String GET_VITAL_PARAMS = " SELECT param_id, param_label, param_order,"
      + " param_uom, mandatory_in_tx, expr_for_calc_result, system_vital "
      + " FROM vital_parameter_master WHERE param_status='A' AND param_container='V'"
      + " AND (visit_type=? OR visit_type is null)"
      + " ORDER BY param_order ASC";

  /**
   * Gets the active vital params.
   *
   * @param visitType the visit type
   * @return the active vital params
   * @throws SQLException the SQL exception
   */
  public List getActiveVitalParams(String visitType) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(GET_VITAL_PARAMS);
      ps.setString(1, visitType);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the vital list.
   *
   * @param filters the filters
   * @param pagingParams the paging params
   * @return the vital list
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   */
  public PagedList getVitalList(Map filters, Map pagingParams) throws SQLException, ParseException {

    Connection con = null;
    con = DataBaseUtil.getReadOnlyConnection();
    try {
      SearchQueryBuilder qb = null;
      qb = new SearchQueryBuilder(con, GET_VITAL_FIELDS, GET_VITAL_COUNT, GET_VITAL_TABLE,
          pagingParams);

      // add the value for the initial where clause

      qb.addFilterFromParamMap(filters);
      qb.addSecondarySort("param_id");
      qb.build();
      return qb.getMappedPagedList();
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /**
   * Gets the selected vital list.
   *
   * @param paramId the param id
   * @return the selected vital list
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getSelectedVitalList(int paramId) throws SQLException {

    PreparedStatement ps = null;
    Connection con = DataBaseUtil.getConnection();
    ps = con.prepareStatement(GET_VITAL_FIELDS + GET_VITAL_TABLE + "WHERE PARAM_ID=? ");
    ps.setInt(1, paramId);
    List vitalList = DataBaseUtil.queryToDynaList(ps);
    DataBaseUtil.closeConnections(con, ps);
    return vitalList;
  }

  /**
   * Update vital details.
   *
   * @param con the con
   * @param paramId the param id
   * @param fields the fields
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean updateVitalDetails(Connection con, int paramId, Map fields) throws SQLException {
    HashMap key = new HashMap();
    key.put("param_id", paramId);
    int rows = DataBaseUtil.dynaUpdate(con, "vital_parameter_master", fields, key);
    return (rows == 1);
  }

  /** The get all vitals. */
  private static String GET_ALL_VITALS = " select param_id,param_label,param_order,param_status"
      + " from vital_parameter_master ";

  /**
   * Gets the all vitals.
   *
   * @return the all vitals
   */
  public static List getAllVitals() {
    PreparedStatement ps = null;
    ArrayList vitals = null;
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_ALL_VITALS);
      vitals = DataBaseUtil.queryToArrayList(ps);
    } catch (SQLException exe) {
      log.error("", exe);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return vitals;
  }

  /** The emr vitals. */
  private static final String EMR_VITALS = " SELECT vital_reading_id, patient_id, date_time,"
      + " vv.user_name,pr.visit_type,pr.reg_date FROM visit_vitals vv"
      + " JOIN patient_registration pr USING (patient_id) ";

  /**
   * Gets the all EMR vitals.
   *
   * @param patientId the patient id
   * @param mrNo the mr no
   * @param allVisitsDocs the all visits docs
   * @return the all EMR vitals
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   */
  public List<EMRDoc> getAllEMRVitals(String patientId, String mrNo, boolean allVisitsDocs)
      throws SQLException, ParseException {
    List<EMRDoc> emrVitals = new ArrayList<EMRDoc>();
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      if (allVisitsDocs) {
        ps = con.prepareStatement(EMR_VITALS + " WHERE pr.mr_no = ?");
        ps.setString(1, mrNo);
      } else {
        ps = con.prepareStatement(EMR_VITALS + " WHERE pr.patient_id=?");
        ps.setString(1, patientId);
      }
      ResultSet rs = ps.executeQuery();
      Map vitalsMap = new HashMap();
      BasicDynaBean printpref = PrintConfigurationsDAO
          .getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT);
      while (rs.next()) {
        if (!vitalsMap.containsValue(rs.getString("patient_id"))) {
          EMRDoc dtoObj = new EMRDoc();
          populateEMRVitals(dtoObj, rs, printpref);
          emrVitals.add(dtoObj);
          vitalsMap.put("patient_id", rs.getString("patient_id"));
        }
      }

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return emrVitals;
  }

  /** The emr intake output. */
  private static final String EMR_INTAKE_OUTPUT = " SELECT  pr.patient_id,"
      + " max(vv.date_time) as date_time,"
      + " max(vv.user_name) as user_name, pr.visit_type, pr.reg_date"
      + " FROM visit_vitals vv"
      + " JOIN patient_registration pr USING (patient_id)"
      + " JOIN vital_reading vr ON (vv.vital_reading_id=vr.vital_reading_id)"
      + " JOIN  vital_parameter_master vpm ON (vr.param_id=vpm.param_id"
      + " AND param_container IN ('O', 'I') ) ";
  
  /** The group by for io. */
  private static final String GROUP_BY_FOR_IO = "group by pr.patient_id, pr.visit_type,"
      + " pr.reg_date";

  /**
   * Gets the all EMR intake outputs.
   *
   * @param patientId the patient id
   * @param mrNo the mr no
   * @param allVisitsDocs the all visits docs
   * @return the all EMR intake outputs
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   */
  public List<EMRDoc> getAllEMRIntakeOutputs(String patientId, String mrNo, boolean allVisitsDocs)
      throws SQLException, ParseException {
    List<EMRDoc> emrIntakeOutput = new ArrayList<EMRDoc>();
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      if (allVisitsDocs) {
        ps = con.prepareStatement(EMR_INTAKE_OUTPUT + " WHERE pr.mr_no = ? " + GROUP_BY_FOR_IO);
        ps.setString(1, mrNo);
      } else {
        ps = con.prepareStatement(EMR_INTAKE_OUTPUT + " WHERE pr.patient_id=? " + GROUP_BY_FOR_IO);
        ps.setString(1, patientId);
      }
      ResultSet rs = ps.executeQuery();
      BasicDynaBean printpref = PrintConfigurationsDAO
          .getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT);
      while (rs.next()) {
        EMRDoc dtoObj = new EMRDoc();
        populateEMRIntakeOutput(dtoObj, rs, printpref);
        emrIntakeOutput.add(dtoObj);
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return emrIntakeOutput;
  }

  /**
   * Populate EMR intake output.
   *
   * @param dtoObj the dto obj
   * @param rs the rs
   * @param printpref the printpref
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   */
  private void populateEMRIntakeOutput(EMRDoc dtoObj, ResultSet rs, BasicDynaBean printpref)
      throws SQLException, ParseException {
    dtoObj.setDocid("");
    dtoObj.setTitle("Intake/Output");

    dtoObj.setType("SYS_IVP");
    dtoObj.setDate(rs.getDate("date_time"));
    dtoObj.setDoctor("");
    dtoObj.setUpdatedBy(rs.getString("user_name"));
    dtoObj.setVisitid(rs.getString("patient_id"));
    dtoObj.setProvider(EMRInterface.Provider.IntakeOutputParamProvider);

    dtoObj.setAuthorized(true);
    int printerId = (Integer) printpref.get("printer_id");
    dtoObj.setPrinterId(printerId);
    String displayUrl = "/IntakeOutput/genericIntakeOutputForm.do?method=generateReport&visitId="
        + rs.getString("patient_id") + "&vitalReadingId=0&printerId=" + printerId;

    dtoObj.setPdfSupported(true);
    dtoObj.setDocid(rs.getString("patient_id"));
    dtoObj.setVisitDate(rs.getDate("reg_date"));

    dtoObj.setDisplayUrl(displayUrl);

  }

  /**
   * Populate EMR vitals.
   *
   * @param dtoObj the dto obj
   * @param rs the rs
   * @param printpref the printpref
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   */
  private static void populateEMRVitals(EMRDoc dtoObj, ResultSet rs, BasicDynaBean printpref)
      throws SQLException, ParseException {

    dtoObj.setDocid("");
    if (rs.getString("visit_type").equalsIgnoreCase("I")) {
      dtoObj.setTitle("IP Vitals (Old)");
      dtoObj.setType("SYS_IVP");
    } else {
      dtoObj.setTitle("OP Vitals");
      dtoObj.setType("SYS_VP");
    }
    // TODO: this should be Hospital Services, not Rx.
    dtoObj.setDate(rs.getDate("date_time"));
    dtoObj.setDoctor("");
    dtoObj.setUpdatedBy(rs.getString("user_name"));
    dtoObj.setVisitid(rs.getString("patient_id"));
    dtoObj.setProvider(EMRInterface.Provider.VitalParamProvider);

    dtoObj.setAuthorized(true);
    int printerId = (Integer) printpref.get("printer_id");
    dtoObj.setPrinterId(printerId);
    String displayUrl = "/vitalForm/genericVitalFormPrint.do?method=generateReport&visitId="
        + rs.getString("patient_id") + "&vitalReadingId=0&printerId=" + printerId;

    dtoObj.setPdfSupported(true);
    dtoObj.setDocid(rs.getString("patient_id"));
    dtoObj.setVisitDate(rs.getDate("reg_date"));

    dtoObj.setDisplayUrl(displayUrl);
  }

  /** The Constant GET_ALL_RESULT_RANGES. */
  private static final String GET_ALL_RESULT_RANGES = "SELECT * FROM vital_reference_range_master"
      + " WHERE param_id = ? ORDER BY priority";

  /**
   * Gets the result ranges.
   *
   * @param paramId the param id
   * @return the result ranges
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getResultRanges(int paramId) throws SQLException {
    Connection con = null;
    PreparedStatement pstmt = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      pstmt = con.prepareStatement(GET_ALL_RESULT_RANGES);
      pstmt.setInt(1, paramId);

      return DataBaseUtil.queryToDynaList(pstmt);
    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }
  }

  /** The Constant REFERENCE_RANGE_LIST. */
  private static final String REFERENCE_RANGE_LIST = "SELECT param_id FROM "
      + "vital_reference_range_master GROUP BY param_id";

  /**
   * Gets the reference range list.
   *
   * @return the reference range list
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getReferenceRangeList() throws SQLException {

    return DataBaseUtil.queryToDynaList(REFERENCE_RANGE_LIST);

  }

  /**
   * Checks if is t expression valid.
   *
   * @param con the con
   * @param expression the expression
   * @return true, if is t expression valid
   * @throws ArithmeticException the arithmetic exception
   * @throws Exception the exception
   */
  public boolean istExpressionValid(Connection con, String expression) throws ArithmeticException,
      Exception {
    boolean valid = false;
    List<BasicDynaBean> vitalMaster = new GenericDAO("vital_parameter_master").listAll();
    BasicDynaBean vitalMasterrBean = null;
    StringWriter writer = new StringWriter();

    try {
      expression = "<#setting number_format=\"##.##\">\n" + expression;
      HashMap<String, Object> resultParams = new HashMap<String, Object>();
      Map<String, Object> results = new HashMap<String, Object>();
      List values = new ArrayList();
      for (int i = 0; i < vitalMaster.size(); i++) {
        vitalMasterrBean = vitalMaster.get(i);
        resultParams.put((String) vitalMasterrBean.get("param_label"), 1);
        values.add(1);
      }
      results.put("results", resultParams);
      results.put("values", values);
      
      Template expressionTemplate = new Template("expression", new StringReader(expression),
          new Configuration());
      expressionTemplate.process(results, writer);
    } catch (InvalidReferenceException ine) {
      log.error("", ine);
      return false;
    } catch (TemplateException exe) {
      log.error("", exe);
      return false;
    } catch (freemarker.core.ParseException exe) {
      log.error("", exe);
      return false;
    } catch (ArithmeticException exe) {
      log.error("", exe);
      return false;
    } catch (Exception exe) {
      log.error("", exe);
      return false;
    }
    valid = !writer.toString().contains("[^.\\d]");

    try {
      if (!writer.toString().trim().isEmpty()) {
        BigDecimal validNumber = new BigDecimal(writer.toString());
      }
    } catch (NumberFormatException ne) {
      log.error("", ne);
      valid = false;
    }
    return valid;
  }

  /** The Constant GET_ALL_VITAL_PARAMS. */
  private static final String GET_ALL_VITAL_PARAMS = " SELECT vm.param_id, param_label,"
      + " param_order, param_uom, CASE WHEN vdd.mandatory IS NOT NULL THEN vdd.mandatory"
      + " ELSE 'N' END as mandatory_in_tx,"
      + " vm.visit_type, expr_for_calc_result, system_vital "
      + " FROM vital_parameter_master vm"
      + " LEFT JOIN vitals_default_details vdd ON (vm.param_id = vdd.param_id AND dept_id=?"
      + " AND center_id=? AND vdd.visit_type =?)"
      + " WHERE param_status='A' AND param_container = 'V'"
      + " ORDER BY param_container DESC, param_order ASC";

  /**
   * Gets the all vital params.
   *
   * @param deptId the dept id
   * @param centerId the center id
   * @param visit the visit
   * @return the all vital params
   * @throws SQLException the SQL exception
   */
  public List getAllVitalParams(String deptId, int centerId, String visit) throws SQLException {
    visit = visit == null ? "" : visit;
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      String query = GET_ALL_VITAL_PARAMS;
      ps = con.prepareStatement(query);
      ps.setString(1, deptId);
      ps.setInt(2, centerId);
      ps.setString(3, visit);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  private static final String GET_UNIQUE_VITALS_FOR_PATIENT =
      " SELECT param_id, param_label, param_order," + " param_uom, mandatory_in_tx,"
          + " expr_for_calc_result, system_vital " + " FROM vital_parameter_master "
          + " JOIN (Select DISTINCT param_id " + "   from visit_vitals "
          + "   join vital_reading using (vital_reading_id) "
          + "   where patient_id = ?) AS foo using (param_id) " + " ORDER BY param_order ASC";

  public List<BasicDynaBean> getUniqueVitalsforPatient(String patientId) {
    return DatabaseHelper.queryToDynaList(GET_UNIQUE_VITALS_FOR_PATIENT, patientId);
  }

}
