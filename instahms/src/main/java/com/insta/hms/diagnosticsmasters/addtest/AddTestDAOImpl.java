package com.insta.hms.diagnosticsmasters.addtest;

import au.com.bytecode.opencsv.CSVWriter;
import com.bob.hms.adminmasters.organization.OrgMasterDao;
import com.bob.hms.common.AutoIncrementId;
import com.bob.hms.common.Constants;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;
import com.insta.hms.auditlog.AuditLogDao;
import com.insta.hms.common.CommonUtils;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.diagnosticmodule.laboratory.LaboratoryBO;
import com.insta.hms.diagnosticsmasters.Result;
import com.insta.hms.diagnosticsmasters.Test;
import com.insta.hms.diagnosticsmasters.TestTemplate;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.Order.OrderMasterDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * The Class AddTestDAOImpl.
 */
public class AddTestDAOImpl {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(AddTestDAOImpl.class);
  private static final GenericDAO testResultRangesDAO = new GenericDAO("test_result_ranges");

  /** The con. */
  Connection con = null;
  
  /**
   * Instantiates a new adds the test DAO impl.
   *
   * @param con
   *          the con
   */
  /*
   * Constructor with connection. Required for non-static methods
   */
  public AddTestDAOImpl(Connection con) {
    this.con = con;
  }

  /**
   * Gets the next test id.
   *
   * @return the next test id
   * @throws SQLException
   *           the SQL exception
   */
  public static String getNextTestId() throws SQLException {
    return AutoIncrementId.getNewIncrId("Test_ID", "Diagnostics", "Diagnostic");
  }

  /***************************************************************************
   * Static query-only methods: connection is acquired in these.
   **************************************************************************/

  public static final String GET_DIAG_DEPTS = " SELECT DDEPT_ID, DDEPT_NAME "
      + "|| '(' || d.dept_name || ')' as DDEPT_NAME "
      + " FROM DIAGNOSTICS_DEPARTMENTS dd join department d on(d.dept_id = dd.category) "
      + " WHERE dd.STATUS='A' ORDER BY dd.category ";

  /**
   * Gets the diag departments.
   *
   * @return the diag departments
   * @throws SQLException
   *           the SQL exception
   */
  public static List getDiagDepartments() throws SQLException {
    return DataBaseUtil.simpleQueryToArrayList(GET_DIAG_DEPTS);
  }

  /** The Constant TEST_FORMATS. */
  public static final String TEST_FORMATS = "SELECT TESTFORMAT_ID,FORMAT_NAME from TEST_FORMAT "
      + "order by FORMAT_NAME";

  /**
   * Gets the report formats.
   *
   * @return the report formats
   * @throws SQLException
   *           the SQL exception
   */
  public static List getReportFormats() throws SQLException {
    return DataBaseUtil.simpleQueryToArrayList(TEST_FORMATS);
  }

  /** The Constant GET_DEPT_IDS. */
  private static final String GET_DEPT_IDS = "SELECT ddept_id FROM diagnostics_departments";

  /**
   * Gets the all depts ids.
   *
   * @return the all depts ids
   * @throws SQLException
   *           the SQL exception
   */
  public static ArrayList<String> getAllDeptsIds() throws SQLException {
    ArrayList<String> al = null;
    Connection con = DataBaseUtil.getReadOnlyConnection();
    PreparedStatement ps = con.prepareStatement(GET_DEPT_IDS);
    al = DataBaseUtil.queryToOnlyArrayList(ps);
    ps.close();
    con.close();
    return al;
  }

  /** The Constant EXIST_TEST_QUERY. */
  private static final String EXIST_TEST_QUERY = "SELECT d.diag_code, d.test_name, d.test_id, "
      + " d.ddept_id, d.conduction_format, dd.ddept_name, dc.charge FROM diagnostics d "
      + " JOIN diagnostics_departments dd USING (ddept_id) "
      + " LEFT OUTER JOIN diagnostic_charges dc ON (d.test_id = dc.test_id "
      + " AND dc.org_name = 'ORG0001' " + "   AND dc.bed_type = 'GENERAL' AND dc.priority = 'R') "
      + " ORDER BY dd.ddept_name, d.test_name offset ? limit ? ";

  /**
   * Gets the existing tests.
   *
   * @param offset
   *          the offset
   * @param pageSize
   *          the page size
   * @return the existing tests
   * @throws SQLException
   *           the SQL exception
   */
  public static ArrayList getExistingTests(int offset, int pageSize) throws SQLException {
    PreparedStatement ps = null;
    Connection con = null;
    ArrayList diagList = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(EXIST_TEST_QUERY);
      ps.setInt(1, offset);
      ps.setInt(2, pageSize);
      diagList = DataBaseUtil.queryToArrayList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return diagList;
  }

  /** The Constant TEST_NAME_QUERY. */
  private static final String TEST_NAME_QUERY = "SELECT test_name,ddept_id from diagnostics";

  /**
   * Gets the test names.
   *
   * @return the test names
   * @throws SQLException
   *           the SQL exception
   */
  public static ArrayList getTestNames() throws SQLException {
    PreparedStatement ps = null;
    Connection con = null;
    ArrayList diagList = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(TEST_NAME_QUERY);

      diagList = DataBaseUtil.queryToArrayList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return diagList;
  }

  /**
   * Gets the test bean.
   *
   * @param testId
   *          the test id
   * @return the test bean
   * @throws SQLException
   *           the SQL exception
   */
  public static BasicDynaBean getTestBean(String testId) throws SQLException {
    return new GenericDAO("diagnostics").findByKey("test_id", testId);
  }

  /**
   * Gets test name of a single test by taking test id.
   *
   * @param testid
   *          the testid
   * @return the test name
   * @throws SQLException
   *           the SQL exception
   */
  public static String getTestName(String testid) throws SQLException {
    Connection con = DataBaseUtil.getReadOnlyConnection();
    PreparedStatement ps = con
        .prepareStatement("SELECT test_name,ddept_id from diagnostics WHERE TEST_ID=?");
    ps.setString(1, testid);
    String testname = DataBaseUtil.getStringValueFromDb(ps);
    ps.close();
    con.close();
    return testname;

  }

  /** The Constant TEST_DETAILS. */
  private static final String TEST_DETAILS = "SELECT d.diag_code, d.test_name, d.test_id, "
      + "d.ddept_id, dd.ddept_name, d.type_of_specimen,d.remarks, "
      + " d.status, d.sample_needed, d.conduction_format,d.additional_info_reqts,  "
      + " d.conduction_applicable, d.hl7_export_code, "
      + " dcr.charge as routine_charge, dcs.charge as stat_charge, dcsc.charge as schedule_charge, "
      + " dd.category,d.service_sub_group_id, d.conducting_doc_mandatory,d.prior_auth_required,"
      + " d.sample_collection_instructions,d.conduction_instructions, "
      + " d.insurance_category_id,d.results_validation,d.allow_rate_increase, "
      + "d.allow_rate_decrease, "
      + " d.dependent_test_id, dependent.test_name as dependent_test_name, d.sample_type_id, "
      + " d.results_entry_applicable,d.conducting_role_id,d.mandate_additional_info"
      + " FROM diagnostics d "
      + " LEFT JOIN diagnostics dependent ON (d.dependent_test_id = dependent.test_id) "
      + " JOIN diagnostics_departments dd ON (dd.ddept_id=d.ddept_id) "
      + " LEFT OUTER JOIN diagnostic_charges dcr ON (d.test_id = dcr.test_id "
      + "AND dcr.org_name='ORG0001' " + "   AND dcr.bed_type = 'GENERAL' AND dcr.priority = 'R') "
      + " LEFT OUTER JOIN diagnostic_charges dcs ON (d.test_id = dcs.test_id "
      + "AND dcs.org_name='ORG0001' " + "   AND dcs.bed_type = 'GENERAL' AND dcs.priority = 'S') "
      + " LEFT OUTER JOIN diagnostic_charges dcsc ON (d.test_id = dcsc.test_id "
      + "AND dcsc.org_name='ORG0001' "
      + "   AND dcsc.bed_type = 'GENERAL' AND dcsc.priority = 'SC') " + " WHERE d.test_id=?";

  /** The Constant GET_TEST_CATEGORY. */
  private static final String GET_TEST_CATEGORY = "SELECT dd.category "
      + "FROM diagnostics_departments dd JOIN "
      + " diagnostics d  using(ddept_id)  where d.test_id = ? ";

  /**
   * Gets the test category.
   *
   * @param testId
   *          the test id
   * @return the test category
   * @throws SQLException
   *           the SQL exception
   */
  public static String getTestCategory(String testId) throws SQLException {
    String category = null;
    PreparedStatement ps = null;
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_TEST_CATEGORY);
      ps.setString(1, testId);
      category = DataBaseUtil.getStringValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return category;
  }

  /** The Constant GET_PRESCRIPTION_TYPE. */
  private static final String GET_PRESCRIPTION_TYPE = "SELECT d.house_status FROM Diagnostics d"
      + " where d.test_id =?";

  /**
   * Gets the test prescription type.
   *
   * @param testId
   *          the test id
   * @return the test prescription type
   * @throws SQLException
   *           the SQL exception
   */
  public static String getTestPrescriptionType(String testId) throws SQLException {
    String presType = null;
    PreparedStatement ps = null;
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_PRESCRIPTION_TYPE);
      ps.setString(1, testId);
      String temp = DataBaseUtil.getStringValueFromDb(ps);
      if (temp != null) {
        if (temp.equals("I")) {
          presType = LaboratoryBO.HOSPITAL_TEST; // these
          // prescription will
          // be conducted in
          // hospital it self
        } else if (temp.equals("O")) {
          presType = LaboratoryBO.OUTHOUST_TEST; // these
          // prescriptions
          // will be sent to
          // the outhouse
          // hospitals.
        }
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return presType;
  }

  /**
   * Gets the test details 1.
   *
   * @param testId
   *          the test id
   * @return the test details 1
   * @throws SQLException
   *           the SQL exception
   */
  public static List<BasicDynaBean> getTestDetails1(String testId) throws SQLException {
    PreparedStatement ps = null;
    Connection con = null;
    List<BasicDynaBean> testDetails = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(TEST_DETAILS);
      ps.setString(1, testId);
      testDetails = DataBaseUtil.queryToDynaList(ps);

      // if(interfaceList!=null)testDetails.addAll((ArrayList)interfaceList);

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return testDetails;
  }

  /** The interface details. */
  private static String INTERFACE_DETAILS = "SELECT interface_name from "
      + " diagnostics_export_interface where test_id = ?";

  /**
   * Gets the hl 7 interface details.
   *
   * @param testId
   *          the test id
   * @return the hl 7 interface details
   * @throws SQLException
   *           the SQL exception
   */
  @SuppressWarnings("unchecked")
  public static ArrayList<String> getHl7InterfaceDetails(String testId) throws SQLException {
    PreparedStatement ps = null;
    Connection con = null;
    ArrayList<String> interfaceDetails = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(INTERFACE_DETAILS);
      ps.setString(1, testId);
      interfaceDetails = DataBaseUtil.queryToOnlyArrayList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return interfaceDetails;
  }

  /** The Constant TEST_RESULTS. */
  private static final String TEST_RESULTS = " SELECT array_to_string(array_agg(hcm.center_name),"
      + "',') as centers,dmm.method_name,trc.status,array_to_string(array_agg(hcm.center_id),',') "
      + "as numcenter,trm.* FROM TEST_RESULTS_MASTER trm "
      + " LEFT JOIN diag_methodology_master as dmm USING (method_id) "
      + " LEFT JOIN test_results_center as trc USING (resultlabel_id) "
      + " LEFT JOIN hospital_center_master hcm on hcm.center_id=trc.center_id"
      + " WHERE TEST_ID = ?"
      + " GROUP BY trm.resultlabel_id ,trc.status,dmm.method_name,trm.test_id,trm.resultlabel,"
      + " trm.units,trm.display_order,trm.expr_4_calc_result,trm.code_type,"
      + " trm.result_code,trm.data_allowed,trm.source_if_list,trm.resultlabel_short,"
      + " trm.hl7_export_code,trm.method_id, trm.default_value" + " ORDER BY trm.display_order ";

  /** The Constant TEST_RESULTS_CENTERWISE. */
  private static final String TEST_RESULTS_CENTERWISE = " SELECT array_to_string("
      + "array_agg(hcm.center_name),',') as centers,dmm.method_name,trc.status,trm.* "
      + " FROM TEST_RESULTS_MASTER trm "
      + " LEFT JOIN diag_methodology_master as dmm USING (method_id) "
      + " LEFT JOIN test_results_center as trc USING (resultlabel_id) "
      + " LEFT JOIN hospital_center_master hcm on hcm.center_id=trc.center_id"
      + " WHERE TEST_ID = ? AND (trc.center_id = 0 OR trc.center_id = ?) and trc.status ='A'"
      + " GROUP BY trm.resultlabel_id ,dmm.method_name,trc.status,trm.test_id,trm.resultlabel,"
      + "trm.units,trm.display_order,trm.expr_4_calc_result,trm.code_type,"
      + " trm.result_code,trm.data_allowed,trm.source_if_list,trm.resultlabel_short,"
      + " trm.hl7_export_code,trm.method_id, trm.default_value" + " ORDER BY trm.display_order ";

  /**
   * Gets the test results.
   *
   * @param testId
   *          the test id
   * @return the test results
   * @throws SQLException
   *           the SQL exception
   */
  public static ArrayList getTestResults(String testId) throws SQLException {

    PreparedStatement ps = null;
    Connection con = null;
    ArrayList testDetails = null;
    int centerId = RequestContext.getCenterId();
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(TEST_RESULTS);
      ps.setString(1, testId);
      testDetails = DataBaseUtil.queryToArrayList(ps);

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return testDetails;
  }

  /** The Constant existing_labels. */
  private static final String existing_labels = " SELECT DISTINCT(trm.resultlabel_id),trm.*,"
      + "  trs.*, dmm.*, (CASE WHEN patient_gender = 'M' THEN 'Male' "
      + " WHEN patient_gender = 'F' THEN 'Female' "
      + " WHEN patient_gender = 'O' THEN 'Others' ELSE '' END) as gender "
      + " FROM TEST_RESULTS_MASTER trm " + " JOIN test_results_center trc using(resultlabel_id) "
      + " JOIN test_result_ranges trs using(resultlabel_id) "
      + " LEFT JOIN diag_methodology_master dmm USING(method_id)"
      + " WHERE TEST_ID = ? AND trc.status = 'A' order by display_order,priority";

  /** The Constant existing_labels_center_wise. */
  private static final String existing_labels_center_wise = " SELECT *,(CASE WHEN "
      + "patient_gender = 'M' THEN 'Male' WHEN patient_gender = 'F' THEN 'Female' "
      + " WHEN patient_gender = 'O' THEN 'Others' ELSE '' END) as gender,"
      + " trs.reference_range_txt,range_for_all, "
      + " dmm.method_name, (SELECT TEXTCAT_COMMACAT(trc.center_id::varchar) as center_id_app "
      + " FROM TEST_RESULTS_MASTER " + " JOIN test_results_center trc using(resultlabel_id) "
      + " WHERE TEST_ID = ? AND resultlabel_id = trm.resultlabel_id GROUP BY resultlabel_id)"
      + "  as center_id_app " + " FROM TEST_RESULTS_MASTER trm "
      + " JOIN test_results_center trc using(resultlabel_id) "
      + " JOIN test_result_ranges trs using(resultlabel_id) "
      + " LEFT JOIN diag_methodology_master dmm USING(method_id)"
      + " WHERE TEST_ID = ? AND (trc.center_id = 0 OR trc.center_id = ?) AND trc.status = 'A' "
      + " order by display_order,priority";

  /**
   * Gets the existing lables.
   *
   * @param testIdParam
   *          the test id
   * @return the existing lables
   * @throws SQLException
   *           the SQL exception
   */
  public static List getExistingLables(String testIdParam) throws SQLException {
    PreparedStatement ps = null;
    Connection con = null;
    int centerId = RequestContext.getCenterId();
    try {
      con = DataBaseUtil.getConnection();
      if (centerId != 0
          && GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1) {
        ps = con.prepareStatement(existing_labels_center_wise);
        ps.setString(1, testIdParam);
        ps.setString(2, testIdParam);
        ps.setInt(3, centerId);
      } else {
        ps = con.prepareStatement(existing_labels);
        ps.setString(1, testIdParam);
      }
      return DataBaseUtil.queryToDynaList(ps);

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * ************************************************************************* Updation/insertion
   * methods: require a DAO object constructed with a connection
   * ************************************************************************.
   */

  private static final String INSERT_TEST = "INSERT INTO diagnostics "
      + " (test_id, test_name,ddept_id, sample_needed, diag_code,"
      + "  sample_type_id, conduction_format,"
      + " conduction_applicable,username,service_sub_group_id, conducting_doc_mandatory, "
      + " hl7_export_code,sample_collection_instructions,"
      + " conduction_instructions,insurance_category_id,prior_auth_required,"
      + " remarks,allow_rate_increase,allow_rate_decrease,dependent_test_id,"
      + " results_entry_applicable,updated_timestamp,conducting_role_id,mandate_additional_info,"
      + "  additional_info_reqts) "
      + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,? )";

  /**
   * Insert test.
   *
   * @param test
   *          the test
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean insertTest(Test test) throws SQLException {
    PreparedStatement ps = null;
    boolean success = true;

    ps = con.prepareStatement(INSERT_TEST);

    ps.setString(1, test.getTestId());
    ps.setString(2, test.getTestName());
    ps.setString(3, test.getDdeptId());
    ps.setString(4, test.getSampleNeed());
    ps.setString(5, test.getDiagCode());
    ps.setInt(6,
        (test.getSpecimen() != null && !test.getSpecimen().equals("")) ? test.getSpecimen() : 0);
    ps.setString(7, test.getReportGroup());
    ps.setBoolean(8, test.isConduction_applicable());
    ps.setString(9, test.getUserName());
    ps.setInt(10, test.getServiceSubGroupId());
    ps.setString(11, test.getConducting_doc_mandatory());
    ps.setString(12,
        (test.getHl7ExportCode() != null && !test.getHl7ExportCode().equals(""))
            ? test.getHl7ExportCode()
            : null);
    ps.setString(13, test.getSampleCollectionInstructions());
    ps.setString(14, test.getConductionInstructions());
    ps.setInt(15, test.getInsurance_category_id());
    ps.setString(16, test.getPreAuthReq());
    ps.setString(17, test.getRemarks());
    ps.setBoolean(18, test.isAllow_rate_increase());
    ps.setBoolean(19, test.isAllow_rate_decrease());
    ps.setString(20, test.getDependent_test_id());
    ps.setBoolean(21, test.isResults_entry_applicable());
    ps.setTimestamp(22, DateUtil.getCurrentTimestamp());
    ps.setString(23, CommonUtils.getCommaSeparatedString(test.getConductingRoleIds()));
    ps.setString(24, test.getMandate_additional_info());
    ps.setString(25, test.getTest_additional_info());
    int inc = ps.executeUpdate();
    if (inc <= 0) {
      success = false;
      logger.error("Failed to insert test: " + inc);
    }
    ps.close();

    return success;
  }

  /** The insert hl7 interface. */
  private static final String INSERT_HL7_INTERFACE = "INSERT INTO diagnostics_export_interface "
      + "(test_id, interface_name, item_type) " + "VALUES(?,?,?)";

  /**
   * Insert multiple interface corresponding one testId
   * Example :
   * test_id interface_name ------- ------------ 1 iSite 2 PowerScribe.
   *
   * @param testId
   *          the test id
   * @param interfaceNames
   *          the interface names
   * @param itemTypes
   *          the item types
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean insertHl7Interfaces(String testId, String[] interfaceNames, String[] itemTypes)
      throws SQLException {

    boolean isSuccess = true;

    PreparedStatement ps = null;
    ps = con.prepareStatement(INSERT_HL7_INTERFACE);

    for (int i = 0; i < interfaceNames.length; i++) {
      if (!interfaceNames[i].equals("")) {
        ps.setString(1, testId);
        ps.setString(2, interfaceNames[i]);
        ps.setString(3, itemTypes[i]);
        ps.addBatch();
      }
    }

    int[] count = ps.executeBatch();
    isSuccess = count != null;
    return isSuccess;
  }

  /** The Constant INSERT_RESULTS. */
  private static final String INSERT_RESULTS = "INSERT INTO test_results_master "
      + " (test_id, resultlabel, units, display_order, resultlabel_id,"
      + " expr_4_calc_result,code_type,result_code,data_allowed,source_if_list,"
      + "resultlabel_short,hl7_export_code, method_id, default_value ) "
      + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";

  /**
   * Insert results.
   *
   * @param results
   *          the results
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws Exception
   *           the exception
   */
  public boolean insertResults(List<Result> results) throws SQLException, IOException, Exception {

    PreparedStatement ps = con.prepareStatement(INSERT_RESULTS);
    boolean success = true;

    Iterator<Result> it = results.iterator();
    int inc = 0;
    while (it.hasNext()) {
      Result res = it.next();
      ps.setString(1, res.getTestId());
      ps.setString(2, res.getResultLabel());
      ps.setString(3, res.getUnits());
      if ("".equals(res.getOrder())) {
        ps.setInt(4, 0);
      } else {
        ps.setInt(4, Integer.parseInt(res.getOrder()));
      }

      ps.setInt(5, Integer.parseInt(res.getResultlabel_id()));
      ps.setString(6, res.getExpression());
      ps.setString(7, res.getCode_type());
      ps.setString(8, res.getResult_code());
      ps.setString(9, res.getDataAllowed());
      ps.setString(10, res.getSourceIfList());
      ps.setString(11, res.getResultLabelShort());
      ps.setString(12,
          (res.getHl7_interface() != null && !res.getHl7_interface().equals("")) 
          ? res.getHl7_interface() : null);
      ps.setObject(13, res.getMethodId());
      ps.setString(14, res.getDefaultValue());
      ps.addBatch();

      inc++;
    }

    int[] updates = ps.executeBatch();
    ps.close();

    for (int p = 0; p < updates.length; p++) {
      if (updates[p] <= 0) {
        success = false;
        logger.error("Failed to insert result at " + p + ": " + updates[p]);
        break;
      }
    }

    return success;
  }

  /**
   * Insert result ranges.
   *
   * @param con
   *          the con
   * @param addedResultRanges
   *          the added result ranges
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public boolean insertResultRanges(Connection con, List<BasicDynaBean> addedResultRanges)
      throws SQLException, IOException {
    boolean sucess = true;
    sucess &= testResultRangesDAO.insertAll(con, addedResultRanges);
    return sucess;
  }

  /** The Constant INSERT_TEMPLATES. */
  private static final String INSERT_TEMPLATES = "INSERT INTO test_template_master"
      + " (test_id,format_name)" + " values(?,?)";

  /**
   * Insert templates.
   *
   * @param templates
   *          the templates
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean insertTemplates(TestTemplate templates) throws SQLException {
    boolean success = false;
    PreparedStatement ps = con.prepareStatement(INSERT_TEMPLATES);

    ps.setString(1, templates.getTestId());
    ps.setString(2, templates.getTemplateId());

    int inc = ps.executeUpdate();
    if (inc > 0) {
      success = true;
    }

    return success;
  }

  /** The Constant GET_HL7_INTERFACE_RECORDS. */
  private static final String GET_HL7_INTERFACE_RECORDS = "select dei.* , hli.status from "
      + " diagnostics_export_interface dei left join hl7_lab_interfaces hli"
      + " using(interface_name) where dei.test_id=? and hli.status = 'A'";

  /**
   * Gets the hl 7 mapping details.
   *
   * @param testId
   *          the test id
   * @return the hl 7 mapping details
   * @throws SQLException
   *           the SQL exception
   */
  public static List getHl7MappingDetails(String testId) throws SQLException {

    PreparedStatement ps = null;
    Connection con = null;
    List<BasicDynaBean> hl7Details = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_HL7_INTERFACE_RECORDS);
      ps.setString(1, testId);
      hl7Details = DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return hl7Details;
  }

  /** The Constant DELETE_TEMPLATES. */
  private static final String DELETE_TEMPLATES = "DELETE FROM test_template_master where test_id=?";

  /**
   * Delete templates.
   *
   * @param testId
   *          the test id
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean deleteTemplates(String testId) throws SQLException {
    boolean status = false;
    PreparedStatement ps = con.prepareStatement(DELETE_TEMPLATES);
    ps.setString(1, testId);

    int res = ps.executeUpdate();
    if (res >= 0) {
      status = true;
    }

    return status;
  }

  /** The Constant INSERT_TEST_CHARGE. */
  private static final String INSERT_TEST_CHARGE = "INSERT INTO diagnostic_charges(test_id,"
      + "org_name,charge,bed_type,priority,discount,username)VALUES(?," + "?,?,?,?,?,?)  ";

  /** The Constant CHECK_FOR_TESTCHARGE. */
  private static final String CHECK_FOR_TESTCHARGE = "SELECT COUNT(*) FROM diagnostic_charges "
      + " WHERE test_id=? AND bed_type=? AND org_name=? AND priority=? ";

  /** The Constant UPDATE_TEST_CHARGE. */
  private static final String UPDATE_TEST_CHARGE = "UPDATE diagnostic_charges SET charge=?,"
      + " discount=?,username=? WHERE " + "test_id=? AND bed_type=? AND org_name=? AND priority=? ";

  /**
   * Adds the OR edit test charges.
   *
   * @param tcList
   *          the tc list
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean addOREditTestCharges(ArrayList<TestCharge> tcList) throws SQLException {
    boolean status = false;

    PreparedStatement rps = con.prepareStatement(CHECK_FOR_TESTCHARGE);
    PreparedStatement ps = con.prepareStatement(INSERT_TEST_CHARGE);
    PreparedStatement ups = con.prepareStatement(UPDATE_TEST_CHARGE);
    Iterator<TestCharge> it = tcList.iterator();

    while (it.hasNext()) {
      TestCharge tc = it.next();
      rps.setString(1, tc.getTestId());
      rps.setString(2, tc.getBedType());
      rps.setString(3, tc.getOrgId());
      rps.setString(4, tc.getPriority());

      String count = DataBaseUtil.getStringValueFromDb(rps);
      if (count.equals("0")) {
        ps.setString(1, tc.getTestId());
        ps.setString(2, tc.getOrgId());
        ps.setBigDecimal(3, tc.getCharge());
        ps.setString(4, tc.getBedType());
        ps.setString(5, tc.getPriority());
        ps.setBigDecimal(6, tc.getDiscount());
        ps.setString(7, tc.getUserName());
        ps.addBatch();
      } else {
        ups.setBigDecimal(1, tc.getCharge());
        ups.setBigDecimal(2, tc.getDiscount());
        ups.setString(3, tc.getUserName());
        ups.setString(4, tc.getTestId());
        ups.setString(5, tc.getBedType());
        ups.setString(6, tc.getOrgId());
        ups.setString(7, tc.getPriority());

        ups.addBatch();
      }

    }

    do {
      int[] batch1 = ps.executeBatch();
      status = DataBaseUtil.checkBatchUpdates(batch1);
      if (!status) {
        break;
      }

      int[] batch2 = ups.executeBatch();
      status = DataBaseUtil.checkBatchUpdates(batch2);

    } while (false);

    rps.close();
    ups.close();
    ps.close();

    return status;
  }

  /**
   * Update test charge list.
   *
   * @param chargeList
   *          the charge list
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean updateTestChargeList(List<TestCharge> chargeList) throws SQLException {

    PreparedStatement ups = con.prepareStatement(UPDATE_TEST_CHARGE);

    for (TestCharge tc : chargeList) {
      int inc = 1;
      ups.setBigDecimal(inc++, tc.getCharge());
      ups.setBigDecimal(inc++, tc.getDiscount());
      ups.setString(inc++, tc.getUserName());
      ups.setString(inc++, tc.getTestId());
      ups.setString(inc++, tc.getBedType());
      ups.setString(inc++, tc.getOrgId());
      ups.setString(inc++, tc.getPriority());

      ups.addBatch();
    }

    int[] results = ups.executeBatch();
    boolean status = DataBaseUtil.checkBatchUpdates(results);
    ups.close();

    return status;
  }

  /** The Constant UPDATE_APPLICABLE. */
  private static final String UPDATE_APPLICABLE = "UPDATE test_org_details SET applicable=? WHERE "
      + "test_id=? AND org_id=?";

  /**
   * Update rate plan applicable list.
   *
   * @param ratePlanApplicableList
   *          the rate plan applicable list
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean updateRatePlanApplicableList(List<TestCharge> ratePlanApplicableList)
      throws SQLException {

    PreparedStatement ps = con.prepareStatement(UPDATE_APPLICABLE);

    for (TestCharge rpa : ratePlanApplicableList) {
      ps.setBoolean(1, rpa.getApplicable());
      ps.setString(2, rpa.getTestId());
      ps.setString(3, rpa.getOrgId());
      ps.addBatch();
    }

    int[] results = ps.executeBatch();
    boolean status = DataBaseUtil.checkBatchUpdates(results);
    ps.close();

    return status;
  }

  /** The Constant UPDATE_TEST. */
  private static final String UPDATE_TEST = "UPDATE diagnostics "
      + "  SET test_name=?, sample_needed=?, "
      + "  sample_type_id=?, conduction_format=?, status=?, "
      + "  conduction_applicable=?, diag_code=?, username=?, service_sub_group_id=?, "
      + "  conducting_doc_mandatory=?, hl7_export_code=?, "
      + "  sample_collection_instructions=? , conduction_instructions = ? , "
      + "  insurance_category_id=?,prior_auth_required=?,results_validation = ?,remarks = ?,"
      + "  allow_rate_increase=?, allow_rate_decrease=? , dependent_test_id=?,"
      + "  results_entry_applicable = ?,"
      + "  updated_timestamp= ?, conducting_role_id = ?,mandate_additional_info=?, "
      + "  additional_info_reqts=? " + "  WHERE test_id=?";

  /**
   * Update test.
   *
   * @param test
   *          the test
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean updateTest(Test test) throws SQLException {
    PreparedStatement ps = con.prepareStatement(UPDATE_TEST);
    boolean success = true;
    int inc = 1;
    ps.setString(inc++, test.getTestName());
    ps.setString(inc++, test.getSampleNeed());
    ps.setInt(inc++,
        (test.getSpecimen() != null && !test.getSpecimen().equals("")) ? test.getSpecimen() : 0);
    ps.setString(inc++, test.getReportGroup());
    ps.setString(inc++, test.getTestStatus());
    ps.setBoolean(inc++, test.isConduction_applicable());
    ps.setString(inc++, test.getDiagCode());
    ps.setString(inc++, test.getUserName());
    ps.setInt(inc++, test.getServiceSubGroupId());
    ps.setString(inc++, test.getConducting_doc_mandatory());
    ps.setString(inc++,
        (test.getHl7ExportCode() != null && !test.getHl7ExportCode().equals(""))
            ? test.getHl7ExportCode()
            : null);
    ps.setString(inc++, test.getSampleCollectionInstructions());
    ps.setString(inc++, test.getConductionInstructions());
    ps.setInt(inc++, test.getInsurance_category_id());
    ps.setString(inc++, test.getPreAuthReq());
    ps.setString(inc++, test.getResultsValidation());
    ps.setString(inc++, test.getRemarks());
    ps.setBoolean(inc++, test.isAllow_rate_increase());
    ps.setBoolean(inc++, test.isAllow_rate_decrease());
    ps.setString(inc++, test.getDependent_test_id());
    ps.setBoolean(inc++, test.isResults_entry_applicable());
    ps.setTimestamp(inc++, DateUtil.getCurrentTimestamp());
    ps.setString(inc++, CommonUtils.getCommaSeparatedString(test.getConductingRoleIds()));
    ps.setString(inc++, test.getMandate_additional_info());
    ps.setString(inc++, test.getTest_additional_info());
    ps.setString(inc++, test.getTestId());

    int rows = ps.executeUpdate();
    ps.close();

    if (rows <= 0) {
      success = false;
    }
    return success;
  }

  /**
   * For updating interface name corresponding each test its need to be delete the interface rows
   * and reinsert the interface rows
   * Cause :
   * test_id interface_name ------ ------------ 1 iSite 1 PowerScribe 1 iPlatina
   * Now Updated one requred only iSite and PowerScribe so we need to detlete and re-insert in this
   * table.
   * @param testId
   *          the test id
   * @param interfaceNames
   *          the interface names
   * @param itemTypes
   *          the item types
   * @param hl7mappingDeleted
   *          the hl 7 mapping deleted
   * @return boolean
   * @throws SQLException
   *           the SQL exception
   */

  public boolean updateHl7Interface(String testId, String[] interfaceNames, String[] itemTypes,
      String[] hl7mappingDeleted) throws SQLException {
    boolean success = true;

    if (deleteHl7Interface(testId)) {
      for (int i = 0; i < interfaceNames.length; i++) {
        if (hl7mappingDeleted[i].equals("false") && !hl7mappingDeleted[i].equals("")
            && !interfaceNames[i].equals("")) {
          success = updateHl7Interfaces(testId, interfaceNames[i], itemTypes[i]);
        }
      }
    } else {
      for (int i = 0; i < interfaceNames.length; i++) {
        if (hl7mappingDeleted[i].equals("false") && !hl7mappingDeleted[i].equals("")
            && !interfaceNames[i].equals("")) {
          success = updateHl7Interfaces(testId, interfaceNames[i], itemTypes[i]);
        }
      }
    }
    return success;
  }

  /**
   * Update hl 7 interfaces.
   *
   * @param testId
   *          the test id
   * @param interfaceName
   *          the interface name
   * @param itemType
   *          the item type
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean updateHl7Interfaces(String testId, String interfaceName, String itemType)
      throws SQLException {
    PreparedStatement ps = null;
    ps = con.prepareStatement(INSERT_HL7_INTERFACE);
    ps.setString(1, testId);
    ps.setString(2, interfaceName);
    ps.setString(3, itemType);

    int count = ps.executeUpdate();
    boolean isSuccess = count > 0;
    return isSuccess;
  }

  /** The delete interface. */
  private static final String DELETE_INTERFACE = "DELETE FROM diagnostics_export_interface "
      + " WHERE test_id = ? ";

  /**
   * Deleting All interface name rows corresponding testId.
   *
   * @param testId
   *          the test id
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */

  public boolean deleteHl7Interface(String testId) throws SQLException {
    boolean success = false;
    int updatedRow = 0;
    PreparedStatement ps = con.prepareStatement(DELETE_INTERFACE);
    ps.setString(1, testId);
    updatedRow = ps.executeUpdate();
    if (updatedRow > 0) {
      success = true;
    }
    return success;
  }

  /** The Constant UPDATE_TEST_RESULTS. */
  private static final String UPDATE_TEST_RESULTS = " UPDATE test_results_master SET "
      + "resultlabel=?, units=?, display_order=?, "
      + " expr_4_calc_result = ? , code_type = ?, result_code = ? ,data_allowed = ?, "
      + "source_if_list =?,"
      + " resultlabel_short=?,hl7_export_code=?, method_id=?, default_value = ?"
      + " WHERE test_id=? AND resultlabel_id=? ";

  /**
   * Update results.
   *
   * @param results
   *          the results
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws Exception
   *           the exception
   */
  public boolean updateResults(ArrayList<Result> results)
      throws SQLException, IOException, Exception {
    PreparedStatement ps = con.prepareStatement(UPDATE_TEST_RESULTS);
    boolean success = true;

    Iterator<Result> it = results.iterator();
    while (it.hasNext()) {
      Result res = it.next();
      ps.setString(1, res.getResultLabel());
      ps.setString(2, res.getUnits());
      if (res.getOrder().equals("")) {
        ps.setInt(3, 0);
      } else {
        ps.setInt(3, Integer.parseInt(res.getOrder()));
      }
      ps.setString(4, res.getExpression());
      ps.setString(5, res.getCode_type());
      ps.setString(6, res.getResult_code());
      ps.setString(7, res.getDataAllowed());
      ps.setString(8, res.getSourceIfList());
      ps.setString(9, res.getResultLabelShort());
      ps.setString(10,
          (res.getHl7_interface() != null && !res.getHl7_interface().equals("")) 
          ? res.getHl7_interface() : null);
      ps.setObject(11, res.getMethodId());
      ps.setString(12, res.getDefaultValue());
      ps.setString(13, res.getTestId());
      ps.setInt(14, Integer.parseInt(res.getResultlabel_id()));

      ps.addBatch();
    }

    int[] updates = ps.executeBatch();
    ps.close();

    for (int p = 0; p < updates.length; p++) {
      if (updates[p] <= 0) {
        success = false;
        logger.error("Failed to update test result at " + p + ": " + updates[p]);
        break;
      }
    }

    return success;
  }

  /**
   * Update result ranges.
   *
   * @param con
   *          the con
   * @param modifiedResultsRanges
   *          the modified results ranges
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public boolean updateResultRanges(Connection con, List<BasicDynaBean> modifiedResultsRanges)
      throws SQLException, IOException {
    Map key = new HashMap();
    boolean sucess = true;

    for (BasicDynaBean modifedResultRange : modifiedResultsRanges) {
      key.put("resultlabel_id", modifedResultRange.get("resultlabel_id"));
      sucess &= testResultRangesDAO.update(con, modifedResultRange.getMap(),
          key) > 0;
    }
    return sucess;
  }

  /** The Constant DELETE_TEST_RESULTS. */
  public static final String DELETE_TEST_RESULTS = "DELETE FROM test_results_master "
      + " WHERE test_id=? AND resultlabel_id=?";

  /**
   * Delete results.
   *
   * @param results
   *          the results
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public boolean deleteResults(ArrayList<Result> results) throws SQLException, IOException {
    PreparedStatement ps = con.prepareStatement(DELETE_TEST_RESULTS);
    boolean success = true;

    Iterator<Result> it = results.iterator();
    while (it.hasNext()) {
      Result res = it.next();
      ps.setString(1, res.getTestId());
      ps.setInt(2, Integer.parseInt(res.getResultlabel_id()));
      ps.addBatch();
    }

    int[] updates = ps.executeBatch();
    ps.close();

    for (int p = 0; p < updates.length; p++) {
      if (updates[p] <= 0) {
        success = false;
        logger.error("Failed to delete test result at " + p + ": " + updates[p]);
        break;
      }
    }
    return success;
  }

  /**
   * Delete result ranges.
   *
   * @param con
   *          the con
   * @param deletedResultsRanges
   *          the deleted results ranges
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public boolean deleteResultRanges(Connection con, List<Result> deletedResultsRanges)
      throws SQLException, IOException {
    boolean sucess = true;
    BasicDynaBean bean = null;

    for (Result modifedResultRange : deletedResultsRanges) {
      bean = testResultRangesDAO.findByKey("resultlabel_id",
          new Integer(modifedResultRange.getResultlabel_id()));
      if (bean != null) {
        sucess &= testResultRangesDAO.delete(con, "resultlabel_id",
            new Integer(modifedResultRange.getResultlabel_id()));
      }
    }
    return sucess;

  }

  /** The Constant GET_DISCOUNT. */
  private static final String GET_DISCOUNT = "SELECT discount FROM  diagnostic_charges WHERE "
      + "test_id=? AND bed_type=? AND org_name=? AND priority='R' ";

  /** The Constant GET_ROUTINE_CHARGE. */
  private static final String GET_ROUTINE_CHARGE = "SELECT charge FROM  diagnostic_charges WHERE "
      + "test_id=? AND bed_type=? AND org_name=? AND priority='R' ";

  /**
   * Edits the test charges.
   *
   * @param orgId
   *          the org id
   * @param testid
   *          the testid
   * @return the map
   * @throws SQLException
   *           the SQL exception
   */
  public Map editTestCharges(String orgId, String testid) throws SQLException {

    LinkedHashMap<String, ArrayList<String>> map = new LinkedHashMap<>();
    ArrayList<String> beds = new ArrayList<>();
    ArrayList<String> regularCharge = new ArrayList<>();
    ArrayList<String> discount = new ArrayList<>();
    /*
     * ArrayList<String> statCharge = new ArrayList<String>(); ArrayList<String> scheduleCharge =
     * new ArrayList<String>();
     */

    BedMasterDAO bddao = new BedMasterDAO();
    ArrayList<Hashtable<String, String>> bedTypes = bddao.getUnionOfAllBedTypes();
    Iterator<Hashtable<String, String>> it = bedTypes.iterator();

    PreparedStatement dps = con.prepareStatement(GET_DISCOUNT);
    PreparedStatement rps = con.prepareStatement(GET_ROUTINE_CHARGE);
    // PreparedStatement scps = con.prepareStatement(GET_SCHEDULE_CHARGE);

    while (it.hasNext()) {
      Hashtable<String, String> ht = it.next();
      String bedType = ht.get("BED_TYPE");
      beds.add(bedType);

      dps.setString(1, testid);
      dps.setString(2, bedType);
      dps.setString(3, orgId);

      rps.setString(1, testid);
      rps.setString(2, bedType);
      rps.setString(3, orgId);

      /*
       * scps.setString(1, testid); scps.setString(2, bedType); scps.setString(3, orgId);
       */
      discount.add(DataBaseUtil.getStringValueFromDb(dps));
      regularCharge.add(DataBaseUtil.getStringValueFromDb(rps));
      /*
       * statCharge.add(DataBaseUtil.getStringValueFromDB(sps));
       * scheduleCharge.add(DataBaseUtil.getStringValueFromDB(scps));
       */

    }

    // sps.close();
    rps.close();
    // scps.close();

    map.put("CHARGES", beds);
    map.put("REGULARCHARGE", regularCharge);
    map.put("DISCOUNT", discount);
    /*
     * map.put("STATCHARGE", statCharge); map.put("SCHEDULECHARGE", scheduleCharge);
     */

    logger.debug("{}", map);
    return map;
  }

  /** The Constant ALL_TEST_NAME. */
  private static final String ALL_TEST_NAME = "SELECT distinct test_id,test_name FROM "
      + " diagnostics ORDER BY test_id ";

  /**
   * Gets the all test names.
   *
   * @return the all test names
   * @throws SQLException
   *           the SQL exception
   */
  public static ArrayList getAllTestNames() throws SQLException {
    ArrayList list = new ArrayList();

    Connection con = null;
    con = DataBaseUtil.getReadOnlyConnection();
    PreparedStatement ps = con.prepareStatement(ALL_TEST_NAME);
    list = DataBaseUtil.queryToArrayList(ps);
    ps.close();
    con.close();

    return list;
  }

  /**
   * Gets the diagnostic charges.
   *
   * @param testid
   *          the testid
   * @param orgid
   *          the orgid
   * @param bedtype
   *          the bedtype
   * @return the diagnostic charges
   */
  public static List getDiagnosticCharges(String testid, String orgid, String bedtype) {
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    List cl = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      String orgquery = "select org_id from organization_details where org_name=?";
      String generalorgid = DataBaseUtil.getStringValueFromDb(orgquery,
          Constants.getConstantValue("ORG"));
      String generalbedtype = Constants.getConstantValue("BEDTYPE");

      String chargequery = "select charge,bed_type,priority from diagnostic_charges where "
          + " test_id =? and org_name=? and bed_type=?";
      ps = con.prepareStatement(chargequery);
      ps.setString(1, testid);
      ps.setString(2, orgid);
      ps.setString(3, bedtype);

      cl = DataBaseUtil.queryToArrayList(ps);
      logger.debug("{}", cl);

      if (cl.size() <= 0) {
        ps.setString(1, testid);
        ps.setString(2, generalorgid);
        ps.setString(3, generalbedtype);
        cl = DataBaseUtil.queryToArrayList(ps);
        logger.debug("{}", cl);
      }

    } catch (Exception exp) {
      logger.debug("Exception in getDiagnosticCharges method", exp);
    } finally {
      try {
        if (rs != null) {
          rs.close();
        }
        if (ps != null) {
          ps.close();
        }
        if (con != null) {
          con.close();
        }
      } catch (Exception exp) {
        logger.debug("Exception in getDiagnosticCharges method", exp);
      }
    }

    return cl;
  }

  /** The Constant GET_TEST_DEPT_CHARGE_QUERY. */
  /*
   * Retrieves a list of tests, their departments and corresponding charges. Charges can vary
   * depending on bed type and organization, so we need these as input to this method.
   */
  private static final String GET_TEST_DEPT_CHARGE_QUERY = " SELECT dc.test_id, d.test_name,"
      + " dc.charge, " + " d.ddept_id,tod.item_code,dc.discount FROM diagnostic_charges dc "
      + " JOIN diagnostics d ON (dc.test_id = d.test_id and d.status = 'A') "
      + " JOIN test_org_details tod ON (dc.test_id = tod.test_id and "
      + " tod.org_id = dc.org_name and tod.applicable) " + " WHERE priority='R' "
      + " AND bed_type=? AND org_name=? ";

  /**
   * Gets the test dept charges.
   *
   * @param bedType
   *          the bed type
   * @param orgid
   *          the orgid
   * @return the test dept charges
   * @throws SQLException
   *           the SQL exception
   */
  public static List getTestDeptCharges(String bedType, String orgid) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    List list = null;

    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_TEST_DEPT_CHARGE_QUERY);
      ps.setString(1, bedType);
      ps.setString(2, orgid);
      // ps.setString(3, bedType);
      // ps.setString(4, orgid);
      list = DataBaseUtil.queryToArrayList(ps);
    } finally {
      if (ps != null) {
        ps.close();
      }
      if (con != null) {
        con.close();
      }
    }

    return list;
  }

  /** The Constant GET_TEMPLATE_LIST. */
  private static final String GET_TEMPLATE_LIST = "SELECT format_name FROM test_template_master"
      + " where test_id =?";

  /**
   * Gets the template list.
   *
   * @param testId
   *          the test id
   * @return the template list
   * @throws SQLException
   *           the SQL exception
   */
  public static ArrayList<String> getTemplateList(String testId) throws SQLException {
    ArrayList<String> al = null;
    Connection con = DataBaseUtil.getReadOnlyConnection();
    PreparedStatement ps = con.prepareStatement(GET_TEMPLATE_LIST);
    ps.setString(1, testId);

    al = DataBaseUtil.queryToOnlyArrayList(ps);

    ps.close();
    con.close();

    return al;
  }

  /**
   * Gets the value or template.
   *
   * @param testid
   *          the testid
   * @return the value or template
   * @throws SQLException
   *           the SQL exception
   */
  public static StringBuffer getValueOrTemplate(String testid) throws SQLException {
    Connection con = null;
    ResultSet rs = null;
    PreparedStatement ps = null;
    StringBuffer value = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement("select conduction_format from diagnostics where test_id=?");
      ps.setString(1, testid);
      if (DataBaseUtil.getStringValueFromDb(ps).equalsIgnoreCase("Y")) {
        String templateQuery = "SELECT tf.format_name FROM test_format tf join test_template_master"
            + " ttm on (tf.testformat_id= ttm.format_name) and ttm.test_id = ?";
        ps = con.prepareStatement(templateQuery);
        ps.setString(1, testid);
        rs = ps.executeQuery();
        value = new StringBuffer("Template( ");
        boolean templateExists = false;
        String template = null;
        while (rs.next()) {
          templateExists = true;
          template = rs.getString(1);
          if (template != null && !template.equals("")) {
            if (template.length() > 20) {
              template = template.substring(0, 20);
              template = template.concat("..");
            }
          } else {
            template = "No Template";
          }

          value.append(template);
          value.append(", ");
        }
        if (!templateExists) {
          template = "No Template";
          value.append(template);
          value.append(", ");
        }
        value = new StringBuffer(value.substring(0, value.length() - 6));
        value.append(')');
      } else {
        ps = con.prepareStatement("select count(*) from test_results_master where test_id=?");
        ps.setString(1, testid);
        value = new StringBuffer("Values(");
        value.append(DataBaseUtil.getStringValueFromDb(ps));
        value.append(')');
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }
    return value;
  }

  /**
   * Gets the test id.
   *
   * @param prescribedid
   *          the prescribedid
   * @return the test id
   * @throws SQLException
   *           the SQL exception
   */
  public static String getTestId(String prescribedid) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    String testid = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement("SELECT TEST_ID FROM TESTS_PRESCRIBED WHERE PRESCRIBED_ID=?");
      ps.setInt(1, Integer.parseInt(prescribedid));
      testid = DataBaseUtil.getStringValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return testid;
  }

  /** The Constant UPDATE_TIMESTAMP. */
  private static final String UPDATE_TIMESTAMP = "UPDATE diag_test_timestamp set "
      + " test_timestamp=test_timestamp+1;";

  /**
   * Update diagnostic time stamp.
   *
   * @throws SQLException
   *           the SQL exception
   */
  public void updateDiagnosticTimeStamp() throws SQLException {
    PreparedStatement ps = con.prepareStatement(UPDATE_TIMESTAMP);
    ps.executeUpdate();
    ps.close();
  }

  /** The Constant COPY_GENERAL_CHARGES_TO_INACTIVE_BEDS. */
  /*
   * Copy the GENERAL bed type charges to all inactive bed types for the given test ID. These
   * charges will not be inserted normally when the test is created, since only active bed charges
   * are input from the user.
   */
  private static final String COPY_GENERAL_CHARGES_TO_INACTIVE_BEDS = " INSERT INTO "
      + " diagnostic_charges (org_name, bed_type, test_id, charge, priority) "
      + " SELECT abo.org_id, abo.bed_type, dc.test_id, dc.charge, 'R' "
      + " FROM all_beds_orgs_view abo "
      + "   JOIN diagnostic_charges dc ON (dc.org_name = abo.org_id AND dc.bed_type = 'GENERAL') "
      + " WHERE abo.bed_type IN ( "
      + "     SELECT DISTINCT intensive_bed_type FROM icu_bed_charges WHERE bed_status = 'I' "
      + "     UNION ALL SELECT DISTINCT bed_type FROM bed_details WHERE bed_status = 'I') "
      + "   AND dc.test_id=? ";

  /**
   * Copy general charges to inactive beds.
   *
   * @param id
   *          the id
   * @throws SQLException
   *           the SQL exception
   */
  public void copyGeneralChargesToInactiveBeds(String id) throws SQLException {
    PreparedStatement ps = con.prepareStatement(COPY_GENERAL_CHARGES_TO_INACTIVE_BEDS);
    ps.setString(1, id);
    ps.executeUpdate();
    ps.close();
  }

  /**
   * Group increase test charges.
   *
   * @param orgId
   *          the org id
   * @param bedTypes
   *          the bed types
   * @param testIds
   *          the test ids
   * @param amount
   *          the amount
   * @param isPercentage
   *          the is percentage
   * @param roundTo
   *          the round to
   * @param updateTable
   *          the update table
   * @param userName
   *          the user name
   * @throws SQLException
   *           the SQL exception
   */
  public void groupIncreaseTestCharges(String orgId, List<String> bedTypes, List<String> testIds,
      BigDecimal amount, boolean isPercentage, BigDecimal roundTo, String updateTable,
      String userName) throws SQLException {

    if (roundTo.compareTo(BigDecimal.ZERO) == 0) {
      groupIncreaseChargesNoRoundOff(orgId, bedTypes, testIds, amount, isPercentage, updateTable,
          userName);
    } else {
      groupIncreaseChargesWithRoundOff(orgId, bedTypes, testIds, amount, isPercentage, roundTo,
          updateTable, userName);
    }
  }

  /** The Constant GROUP_INCR_TEST_CHARGES. */
  /*
   * Group increase charges: takes in: - orgId to update (reqd) - list of bed types to update
   * (optional, if not given, all bed types) - list of test IDs to update (optional, if not given,
   * all tests) - amount to increase by (can be negative for a decrease) - whether the amount is a
   * percentage instead of an abs. amount - an amount to be rounded to (nearest). Rounding to 0 is
   * invalid.
   *
   * The new amount will not be allowed to go less than zero.
   */
  private static final String GROUP_INCR_TEST_CHARGES = " UPDATE diagnostic_charges "
      + " SET charge = GREATEST( round((charge+?)/?,0)*?, 0), username = ?" + " WHERE org_name=? ";

  /** The Constant GROUP_INCR_TEST_CHARGES_PERCENTAGE. */
  private static final String GROUP_INCR_TEST_CHARGES_PERCENTAGE = " UPDATE diagnostic_charges "
      + " SET charge = GREATEST( round(charge*(100+?)/100/?,0)*?, 0), username = ?"
      + " WHERE org_name=? ";

  /** The Constant GROUP_INCR_TEST_DISCOUNTS. */
  private static final String GROUP_INCR_TEST_DISCOUNTS = " UPDATE diagnostic_charges "
      + " SET discount = LEAST(GREATEST( round((discount+?)/?,0)*?, 0), charge), username = ? "
      + " WHERE org_name=? ";

  /** The Constant GROUP_INCR_TEST_DISCOUNT_PERCENTAGE. */
  private static final String GROUP_INCR_TEST_DISCOUNT_PERCENTAGE = " UPDATE diagnostic_charges "
      + " SET discount = LEAST(GREATEST( round(discount*(100+?)/100/?,0)*?, 0), charge),"
      + " username = ?  WHERE org_name=? ";

  /** The Constant GROUP_APPLY_DISCOUNTS. */
  private static final String GROUP_APPLY_DISCOUNTS = " UPDATE diagnostic_charges"
      + " SET discount = LEAST(GREATEST( round((charge+?)/?,0)*?, 0), charge), username = ?"
      + " WHERE org_name=? ";

  /** The Constant GROUP_APPLY_DISCOUNT_PERCENTAGE. */
  private static final String GROUP_APPLY_DISCOUNT_PERCENTAGE = " UPDATE diagnostic_charges"
      + " SET discount =LEAST(GREATEST( round(charge+(charge*?/100/?),0)*?, 0), charge),"
      + " username = ? WHERE org_name=? ";

  /** The Constant AUDIT_LOG_HINT. */
  private static final String AUDIT_LOG_HINT = ":GUP";

  /**
   * Group increase charges with round off.
   *
   * @param orgId
   *          the org id
   * @param bedTypes
   *          the bed types
   * @param testIds
   *          the test ids
   * @param amount
   *          the amount
   * @param isPercentage
   *          the is percentage
   * @param roundTo
   *          the round to
   * @param updateTable
   *          the update table
   * @param userName
   *          the user name
   * @throws SQLException
   *           the SQL exception
   */
  public void groupIncreaseChargesWithRoundOff(String orgId, List<String> bedTypes,
      List<String> testIds, BigDecimal amount, boolean isPercentage, BigDecimal roundTo,
      String updateTable, String userName) throws SQLException {

    StringBuilder query = null;
    if (updateTable != null && updateTable.equals("UPDATECHARGE")) {
      query = new StringBuilder(
          isPercentage ? GROUP_INCR_TEST_CHARGES_PERCENTAGE : GROUP_INCR_TEST_CHARGES);

    } else if (updateTable.equals("UPDATEDISCOUNT")) {
      query = new StringBuilder(
          isPercentage ? GROUP_INCR_TEST_DISCOUNT_PERCENTAGE : GROUP_INCR_TEST_DISCOUNTS);
    } else {
      query = new StringBuilder(
          isPercentage ? GROUP_APPLY_DISCOUNT_PERCENTAGE : GROUP_APPLY_DISCOUNTS);
    }

    SearchQueryBuilder.addWhereFieldOpValue(true, query, "bed_type", "IN", bedTypes);
    SearchQueryBuilder.addWhereFieldOpValue(true, query, "test_id", "IN", testIds);

    PreparedStatement ps = con.prepareStatement(query.toString());

    // sanity: round to zero is not allowed, can cause div/0
    if (roundTo.equals(BigDecimal.ZERO)) {
      roundTo = BigDecimal.ONE;
    }

    int inc = 1;
    ps.setBigDecimal(inc++, amount);
    ps.setBigDecimal(inc++, roundTo);
    ps.setBigDecimal(inc++, roundTo); // roundTo appears twice in the query
    String userNameWithHint = ((null == userName) ? "" : userName) + AUDIT_LOG_HINT;
    ps.setString(inc++, userNameWithHint);
    ps.setString(inc++, orgId);

    if (bedTypes != null) {
      for (String bedType : bedTypes) {
        ps.setString(inc++, bedType);
      }
    }
    if (testIds != null) {
      for (String testId : testIds) {
        ps.setString(inc++, testId);
      }
    }

    ps.executeUpdate();
    if (ps != null) {
      ps.close();
    }
  }

  /** The Constant GROUP_INCR_TEST_CHARGES_NO_ROUNDOFF. */
  private static final String GROUP_INCR_TEST_CHARGES_NO_ROUNDOFF = " UPDATE diagnostic_charges "
      + " SET charge = GREATEST( charge + ?, 0), username = ?" + " WHERE org_name=? ";

  /** The Constant GROUP_INCR_TEST_CHARGES_PERCENTAGE_NO_ROUNDOFF. */
  private static final String GROUP_INCR_TEST_CHARGES_PERCENTAGE_NO_ROUNDOFF = " UPDATE "
      + " diagnostic_charges SET charge = GREATEST(charge +(charge * ? / 100 ) , 0), username = ?"
      + " WHERE org_name=? ";

  /** The Constant GROUP_INCR_TEST_DISCOUNTS_NO_ROUNDOFF. */
  private static final String GROUP_INCR_TEST_DISCOUNTS_NO_ROUNDOFF = " UPDATE diagnostic_charges "
      + " SET discount = LEAST(GREATEST( discount + ?, 0), charge), username = ? "
      + " WHERE org_name=? ";

  /** The Constant GROUP_INCR_TEST_DISCOUNT_PERCENTAGE_NO_ROUNDOFF. */
  private static final String GROUP_INCR_TEST_DISCOUNT_PERCENTAGE_NO_ROUNDOFF = " UPDATE "
      + " diagnostic_charges "
      + " SET discount = LEAST(GREATEST(discount +(discount * ? / 100 ) , 0), charge),"
      + " username = ? WHERE org_name=? ";

  /** The Constant GROUP_APPLY_DISCOUNTS_NO_ROUNDOFF. */
  private static final String GROUP_APPLY_DISCOUNTS_NO_ROUNDOFF = " UPDATE diagnostic_charges"
      + " SET discount = LEAST(GREATEST( charge + ?, 0), charge), username = ?"
      + " WHERE org_name=? ";

  /** The Constant GROUP_APPLY_DISCOUNT_PERCENTAGE_NO_ROUNDOFF. */
  private static final String GROUP_APPLY_DISCOUNT_PERCENTAGE_NO_ROUNDOFF = " UPDATE "
      + " diagnostic_charges"
      + " SET discount = LEAST(GREATEST( charge + (charge * ? / 100) , 0), charge), username = ?"
      + " WHERE org_name=? ";

  /**
   * Group increase charges no round off.
   *
   * @param orgId
   *          the org id
   * @param bedTypes
   *          the bed types
   * @param testIds
   *          the test ids
   * @param amount
   *          the amount
   * @param isPercentage
   *          the is percentage
   * @param updateTable
   *          the update table
   * @param userName
   *          the user name
   * @throws SQLException
   *           the SQL exception
   */
  public void groupIncreaseChargesNoRoundOff(String orgId, List<String> bedTypes,
      List<String> testIds, BigDecimal amount, boolean isPercentage, String updateTable,
      String userName) throws SQLException {

    StringBuilder query = null;
    if (updateTable != null && updateTable.equals("UPDATECHARGE")) {
      query = new StringBuilder(isPercentage ? GROUP_INCR_TEST_CHARGES_PERCENTAGE_NO_ROUNDOFF
          : GROUP_INCR_TEST_CHARGES_NO_ROUNDOFF);

    } else if (updateTable.equals("UPDATEDISCOUNT")) {
      query = new StringBuilder(isPercentage ? GROUP_INCR_TEST_DISCOUNT_PERCENTAGE_NO_ROUNDOFF
          : GROUP_INCR_TEST_DISCOUNTS_NO_ROUNDOFF);
    } else {
      query = new StringBuilder(isPercentage ? GROUP_APPLY_DISCOUNT_PERCENTAGE_NO_ROUNDOFF
          : GROUP_APPLY_DISCOUNTS_NO_ROUNDOFF);
    }

    SearchQueryBuilder.addWhereFieldOpValue(true, query, "bed_type", "IN", bedTypes);
    SearchQueryBuilder.addWhereFieldOpValue(true, query, "test_id", "IN", testIds);

    PreparedStatement ps = con.prepareStatement(query.toString());
    String userNameWithHint = ((null == userName) ? "" : userName) + AUDIT_LOG_HINT;
    int inc = 1;
    ps.setBigDecimal(inc++, amount);
    ps.setString(inc++, userNameWithHint);
    ps.setString(inc++, orgId);

    if (bedTypes != null) {
      for (String bedType : bedTypes) {
        ps.setString(inc++, bedType);
      }
    }
    if (testIds != null) {
      for (String testId : testIds) {
        ps.setString(inc++, testId);
      }
    }

    ps.executeUpdate();
    if (ps != null) {
      ps.close();
    }
  }

  /** The Constant GET_ORG_ITEM_CODE. */
  private static final String GET_ORG_ITEM_CODE = "SELECT * FROM test_org_details where "
      + " test_id = ? AND org_id = ?";

  /**
   * Gets the org item code.
   *
   * @param orgId
   *          the org id
   * @param testId
   *          the test id
   * @return the org item code
   * @throws SQLException
   *           the SQL exception
   */
  public static List<BasicDynaBean> getOrgItemCode(String orgId, String testId)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    List<BasicDynaBean> list = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_ORG_ITEM_CODE);
      ps.setString(1, testId);
      ps.setString(2, orgId);
      list = DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return list;
  }

  /** The Constant GET_TEST_NOT_APPLICABLE. */
  private static final String GET_TEST_NOT_APPLICABLE = "SELECT org_id FROM test_org_details "
      + " where test_id = ? AND applicable = false AND org_id != ? ";

  /**
   * Gets the test not applicable rate plans.
   *
   * @param testId
   *          the test id
   * @param orgId
   *          the org id
   * @return the test not applicable rate plans
   * @throws SQLException
   *           the SQL exception
   */
  public static List<String> getTestNotApplicableRatePlans(String testId, String orgId)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    List<String> list = new ArrayList<>();
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_TEST_NOT_APPLICABLE);
      ps.setString(1, testId);
      ps.setString(2, orgId);
      list = DataBaseUtil.queryToStringList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return list;
  }

  /** The Constant INSERT_ITEM_CODE. */
  private static final String INSERT_ITEM_CODE = "INSERT INTO test_org_details(test_id,"
      + "org_id,applicable,item_code, code_type)VALUES(?,?,?,?,?)  ";

  /** The Constant CHECK_FOR_ITEM_CODE. */
  private static final String CHECK_FOR_ITEM_CODE = "SELECT COUNT(*) FROM  test_org_details WHERE "
      + "test_id=? AND org_Id=?";

  /** The Constant UPDATE_ITEM_CODE. */
  private static final String UPDATE_ITEM_CODE = "UPDATE test_org_details SET applicable=?, "
      + " item_code=?, code_type=? WHERE test_id=? AND org_id=?";

  /**
   * Adds the OR edit item code.
   *
   * @param testCodes
   *          the test codes
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean addOREditItemCode(ArrayList<TestCharge> testCodes) throws SQLException {
    boolean status = false;

    PreparedStatement rps = con.prepareStatement(CHECK_FOR_ITEM_CODE);
    PreparedStatement ps = con.prepareStatement(INSERT_ITEM_CODE);
    PreparedStatement ups = con.prepareStatement(UPDATE_ITEM_CODE);

    for (TestCharge tc : testCodes) {

      rps.setString(1, tc.getTestId());
      rps.setString(2, tc.getOrgId());
      String count = DataBaseUtil.getStringValueFromDb(rps);
      if (count.equals("0")) {
        ps.setString(1, tc.getTestId());
        ps.setString(2, tc.getOrgId());
        ps.setBoolean(3, tc.getApplicable());
        ps.setString(4, tc.getOrgItemCode());
        ps.setString(5, tc.getCodeType());
        ps.addBatch();

      } else {
        ups.setBoolean(1, tc.getApplicable());
        ups.setString(2, tc.getOrgItemCode());
        ups.setString(3, tc.getCodeType());
        ups.setString(4, tc.getTestId());
        ups.setString(5, tc.getOrgId());
        ups.addBatch();
      }
    }
    do {
      int[] batch1 = ps.executeBatch();
      status = DataBaseUtil.checkBatchUpdates(batch1);
      if (!status) {
        break;
      }

      int[] batch2 = ups.executeBatch();
      status = DataBaseUtil.checkBatchUpdates(batch2);

    } while (false);

    return status;
  }

  /**
   * Gets the next sequence.
   *
   * @return the next sequence
   * @throws SQLException
   *           the SQL exception
   */
  public static int getNextSequence() throws SQLException {
    Connection con = null;
    con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement("select nextval('resultlabel_seq')");
      return DataBaseUtil.getIntValueFromDb(ps);
    } finally {
      con.close();
      ps.close();
    }

  }

  /** The Constant CATEGORY_WISE_TESTS. */
  public static final String CATEGORY_WISE_TESTS = "select d.test_id, d.test_name from"
      + "  diagnostics d join"
      + " diagnostics_departments dd on d.ddept_id=dd.ddept_id and d.status='A' and dd.category=? "
      + " order by d.test_name";

  /**
   * Gets the tests.
   *
   * @param category
   *          the category
   * @return the tests
   * @throws SQLException
   *           the SQL exception
   */
  public static List getTests(String category) throws SQLException {
    return DataBaseUtil.queryToDynaList(CATEGORY_WISE_TESTS, category);
  }

  /** The Constant APP_TESTS_FOR_RATEPLAN. */
  private static final String APP_TESTS_FOR_RATEPLAN = " select d.test_name, d.test_id from "
      + " diagnostics d "
      + " join test_org_details tod on tod.org_id=? and tod.applicable and tod.test_id=d.test_id";

  /**
   * Gets the app tests for rate plan.
   *
   * @param ratePlan
   *          the rate plan
   * @return the app tests for rate plan
   * @throws SQLException
   *           the SQL exception
   */
  public static List<BasicDynaBean> getAppTestsForRatePlan(String ratePlan) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(APP_TESTS_FOR_RATEPLAN);
      ps.setString(1, ratePlan);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The get all tests. */
  private static String GET_ALL_TESTS = " select test_name from diagnostics ";

  /**
   * Gets the all tests.
   *
   * @return the all tests
   */
  public static List getAllTests() {
    PreparedStatement ps = null;
    ArrayList testList = null;
    Connection con = null;

    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_ALL_TESTS);
      testList = DataBaseUtil.queryToArrayList1(ps);
    } catch (SQLException exp) {
      logger.debug(exp.toString());
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

    return testList;
  }

  /** The Constant ALL_LABORATORY_TESTS. */
  private static final String ALL_LABORATORY_TESTS = "select d.test_id,d.test_name from  "
      + " diagnostics d,diagnostics_departments dd "
      + " where d.ddept_id=dd.ddept_id and dd.category='DEP_LAB' and d.status='A'";

  /**
   * Gets the all laboratory tests.
   *
   * @return the all laboratory tests
   * @throws SQLException
   *           the SQL exception
   */
  public static ArrayList getAllLaboratoryTests() throws SQLException {
    ArrayList list = new ArrayList();

    Connection con = null;
    con = DataBaseUtil.getReadOnlyConnection();
    PreparedStatement ps = con.prepareStatement(ALL_LABORATORY_TESTS);
    list = DataBaseUtil.queryToArrayList(ps);
    ps.close();
    con.close();

    return list;
  }

  /** The Constant ALL_RADIOLOGY_TESTS. */
  private static final String ALL_RADIOLOGY_TESTS = "select d.test_id,d.test_name from  "
      + " diagnostics d,diagnostics_departments dd "
      + " where d.ddept_id=dd.ddept_id and dd.category='DEP_RAD' and d.status='A'";

  /**
   * Gets the all radiolody tests.
   *
   * @return the all radiolody tests
   * @throws SQLException
   *           the SQL exception
   */
  public static ArrayList getAllRadiolodyTests() throws SQLException {
    ArrayList list = new ArrayList();

    Connection con = null;
    con = DataBaseUtil.getReadOnlyConnection();
    PreparedStatement ps = con.prepareStatement(ALL_RADIOLOGY_TESTS);
    list = DataBaseUtil.queryToArrayList(ps);
    ps.close();
    con.close();

    return list;
  }

  /** The Constant TEST_CHARGE_QUERY. */
  /*
   * Gets the basic test details and the charge associated with the test, only routine charges are
   * considered (we are not handling S and SC priorities).
   */
  private static final String TEST_CHARGE_QUERY = " SELECT "
      + "  d.diag_code, tod.item_code as rate_plan_code, d.test_name, d.test_id, d.status, "
      + "  d.ddept_id, d.conduction_format, dd.ddept_name, dc.charge, dd.category, dc.discount, "
      + "  d.conduction_applicable,tod.applicable,d.service_sub_group_id, tod.code_type, "
      + "  d.conducting_doc_mandatory, d.insurance_category_id,tod.org_id,"
      + "  d.allow_rate_increase,d.allow_rate_decrease,dependent.test_name as dependent_test_name"
      + "  ,d.dependent_test_id, d.results_entry_applicable, d.billing_group_id  "
      + " FROM diagnostics d "
      + " LEFT JOIN diagnostics dependent ON (d.dependent_test_id = dependent.test_id) "
      + "  JOIN diagnostics_departments dd ON  (dd.ddept_id = d.ddept_id) "
      + "  JOIN diagnostic_charges dc ON (d.test_id = dc.test_id) "
      + "  JOIN test_org_details tod ON  (tod.test_id = dc.test_id AND tod.org_id = dc.org_name ) "
      + " WHERE dc.test_id =? AND dc.org_name =? " + "  AND dc.bed_type =? AND dc.priority = 'R' ";

  /** The Constant QUERY_FOR_TEST_CHARGE. */
  private static final String QUERY_FOR_TEST_CHARGE = " SELECT "
      + "  d.diag_code, tod.item_code as rate_plan_code, d.test_name, d.test_id, d.status, "
      + "  d.ddept_id, d.conduction_format, dd.ddept_name, dc.charge, dd.category, dc.discount, "
      + "  d.conduction_applicable,tod.applicable,d.service_sub_group_id, tod.code_type, "
      + "  d.conducting_doc_mandatory, d.insurance_category_id,tod.org_id,"
      + "  d.allow_rate_increase,d.allow_rate_decrease,dependent.test_name as dependent_test_name"
      + "  ,d.dependent_test_id, d.results_entry_applicable, d.billing_group_id,  "
      + "  CASE WHEN is_outhouse_test(d.test_id,?) THEN 'O' ELSE 'I' END AS house_status  "
      + " FROM diagnostics d "
      + " LEFT JOIN diagnostics dependent ON (d.dependent_test_id = dependent.test_id) "
      + "  JOIN diagnostics_departments dd ON  (dd.ddept_id = d.ddept_id) "
      + "  JOIN diagnostic_charges dc ON (d.test_id = dc.test_id) "
      + "  JOIN test_org_details tod ON  (tod.test_id = dc.test_id AND tod.org_id = dc.org_name ) "
      + " WHERE dc.test_id =? AND dc.org_name =? " + "  AND dc.bed_type =? AND dc.priority = 'R' ";

  /**
   * Gets the test details.
   *
   * @param testId
   *          the test id
   * @param bedType
   *          the bed type
   * @param orgId
   *          the org id
   * @param centerId
   *          the center id
   * @return the test details
   * @throws SQLException
   *           the SQL exception
   */
  public static BasicDynaBean getTestDetails(String testId, String bedType, String orgId,
      int centerId) throws SQLException {
    PreparedStatement ps = null;
    BasicDynaBean bean = null;
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      String generalorgid = "ORG0001";
      String generalbedtype = "GENERAL";

      if (bedType == null || bedType.equals("")) {
        bedType = generalbedtype;
      }
      if (orgId == null || orgId.equals("")) {
        orgId = generalorgid;
      }

      ps = con.prepareStatement(QUERY_FOR_TEST_CHARGE);
      ps.setInt(1, centerId);
      ps.setString(2, testId);
      ps.setString(3, orgId);
      ps.setString(4, bedType);
      List list = DataBaseUtil.queryToDynaList(ps);
      if (!list.isEmpty()) {
        bean = (BasicDynaBean) list.get(0);
      }
      if (bean == null) {
        ps = con.prepareStatement(QUERY_FOR_TEST_CHARGE);
        ps.setInt(1, centerId);
        ps.setString(2, testId);
        ps.setString(3, generalorgid);
        ps.setString(4, generalbedtype);
        list = DataBaseUtil.queryToDynaList(ps);
        bean = (BasicDynaBean) list.get(0);
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return bean;
  }
  
  /**
   * Gets the test details.
   *
   * @param testId
   *          the test id
   * @return the test details
   * @throws SQLException
   *           the SQL exception
   */
  public static ArrayList getTestDetails(String testId) throws SQLException {

    PreparedStatement ps = null;
    Connection con = null;
    ArrayList testDetails = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(TEST_DETAILS);
      ps.setString(1, testId);
      testDetails = DataBaseUtil.queryToArrayList(ps);
      // ArrayList interfaceList = getHl7InterfaceDetails(testId);
      // if(interfaceList!=null)testDetails.addAll((ArrayList)interfaceList);

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return testDetails;
  }

  /**
   * Gets the test details.
   *
   * @param testId
   *          the test id
   * @param bedType
   *          the bed type
   * @param orgId
   *          the org id
   * @return the test details
   * @throws SQLException
   *           the SQL exception
   */
  public static BasicDynaBean getTestDetails(String testId, String bedType, String orgId)
      throws SQLException {
    PreparedStatement ps = null;
    BasicDynaBean bean = null;
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      String generalorgid = "ORG0001";
      String generalbedtype = "GENERAL";

      if (bedType == null || bedType.equals("")) {
        bedType = generalbedtype;
      }
      if (orgId == null || orgId.equals("")) {
        orgId = generalorgid;
      }

      ps = con.prepareStatement(TEST_CHARGE_QUERY);
      ps.setString(1, testId);
      ps.setString(2, orgId);
      ps.setString(3, bedType);
      List list = DataBaseUtil.queryToDynaList(ps);
      if (!list.isEmpty()) {
        bean = (BasicDynaBean) list.get(0);
      }
      if (bean == null) {
        ps = con.prepareStatement(TEST_CHARGE_QUERY);
        ps.setString(1, testId);
        ps.setString(2, generalorgid);
        ps.setString(3, generalbedtype);
        list = DataBaseUtil.queryToDynaList(ps);
        bean = (BasicDynaBean) list.get(0);
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return bean;
  }

  /*
   * Returns test charges for all bed types for the given rate plan. Each bed type's charge will be
   * a column in the returned list. Since the available bed types can only be determined at runtime,
   * we have to construct the query dynamically.
   *
   * This is used for displaying the test charges for each bed in the main test list master screen
   *
   */
  /**
   * Gets the test charges for all bed types.
   *
   * @param orgId
   *          the org id
   * @param bedTypes
   *          the bed types
   * @param testIds
   *          the test ids
   * @return the test charges for all bed types
   * @throws SQLException
   *           the SQL exception
   */
  public static List<BasicDynaBean> getTestChargesForAllBedTypes(String orgId,
      List<String> bedTypes, List<String> testIds) throws SQLException {

    Connection con = null;
    PreparedStatement ps = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = getTestChargesForAllBedTypesStmt(con, orgId, bedTypes, testIds);
      return DataBaseUtil.queryToDynaListWithCase(ps);

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the test charges for all bed types CSV.
   *
   * @param orgId
   *          the org id
   * @param bedTypes
   *          the bed types
   * @param writer
   *          the w
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public static void getTestChargesForAllBedTypesCSV(String orgId, List<String> bedTypes,
      CSVWriter writer) throws SQLException, IOException {

    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = getTestChargesForAllBedTypesStmt(con, orgId, bedTypes, null);
      rs = ps.executeQuery();
      writer.writeAll(rs, true);
      writer.flush();

    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }
  }

  /**
   * Gets the test charges for all bed types stmt.
   *
   * @param con
   *          the con
   * @param orgId
   *          the org id
   * @param bedTypes
   *          the bed types
   * @param testIds
   *          the test ids
   * @return the test charges for all bed types stmt
   * @throws SQLException
   *           the SQL exception
   */
  public static PreparedStatement getTestChargesForAllBedTypesStmt(Connection con, String orgId,
      List<String> bedTypes, List<String> testIds) throws SQLException {

    StringBuilder query = new StringBuilder();
    query.append("SELECT d.test_id, test_name, applicable AS "
        + DataBaseUtil.quoteIdent("Rate Plan Applicable", true));
    query.append(", item_code AS " + DataBaseUtil.quoteIdent("Itemcode", true));
    for (String bedType : bedTypes) {
      query.append(", (SELECT charge FROM diagnostic_charges dc WHERE "
          + " dc.test_id=d.test_id AND bed_type=? AND org_name=?) AS "
          + DataBaseUtil.quoteIdent(bedType, true));
      query.append(", (SELECT discount FROM diagnostic_charges dc WHERE "
          + " dc.test_id=d.test_id AND bed_type=? AND org_name=?) AS "
          + DataBaseUtil.quoteIdent(bedType + "(Discount)", true));
    }
    query.append(" FROM diagnostics d  "
        + " JOIN test_org_details tod ON (d.test_id=tod.test_id AND tod.org_id=? )");
    if (testIds == null) {
      query.append(" WHERE d.status='A' ");
    }

    SearchQueryBuilder.addWhereFieldOpValue(false, query, "d.test_id", "IN", testIds);

    PreparedStatement ps = null;
    ps = con.prepareStatement(query.toString());

    int inc = 1;
    for (String bedType : bedTypes) {
      ps.setString(inc++, bedType);
      ps.setString(inc++, orgId);
      ps.setString(inc++, bedType);
      ps.setString(inc++, orgId);
    }
    ps.setString(inc++, orgId);
    if (testIds != null) {
      for (String testId : testIds) {
        ps.setString(inc++, testId);
      }
    }
    return ps;
  }

  /** The Constant SEARCH_TEST_FIELDS. */
  /*
   * Search the tests: returns a PagedList suitable for a dashboard type list of tests.
   */
  private static final String SEARCH_TEST_FIELDS = "SELECT *";

  /** The Constant SEARCH_TEST_COUNT. */
  private static final String SEARCH_TEST_COUNT = "SELECT count(*)";

  /** The Constant SEARCH_TEST_TABLES. */
  private static final String SEARCH_TEST_TABLES = " FROM (SELECT tod.test_id, tod.applicable,"
      + " tod.item_code,d.diag_code AS alias_item_code, d.test_name, d.status,d.conduction_format, "
      + "d.ddept_id, dd.ddept_name, "
      + " tod.org_id, tod.code_type, d.service_sub_group_id,od.org_name,'diagnostics'::text as"
      + " chargeCategory, tod.is_override " + " FROM test_org_details tod "
      + " JOIN diagnostics d ON (d.test_id = tod.test_id) "
      + " JOIN organization_details od on(od.org_id=tod.org_id) "
      + " JOIN diagnostics_departments dd ON (d.ddept_id = dd.ddept_id)) AS foo";

  /**
   * Search tests.
   *
   * @param requestParams
   *          the request params
   * @param pagingParams
   *          the paging params
   * @return the paged list
   * @throws ParseException
   *           the parse exception
   * @throws SQLException
   *           the SQL exception
   */
  public static PagedList searchTests(Map requestParams, Map pagingParams)
      throws ParseException, SQLException {

    Connection con = null;
    SearchQueryBuilder qb = null;
    Map map = requestParams;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      qb = new SearchQueryBuilder(con, SEARCH_TEST_FIELDS, SEARCH_TEST_COUNT, SEARCH_TEST_TABLES,
          pagingParams);
      qb.addFilterFromParamMap(map);
      qb.addSecondarySort("test_id");
      qb.build();

      return qb.getMappedPagedList();
    } finally {
      qb.close();
      DataBaseUtil.closeConnections(con, null);
    }

  }

  /** The Constant BACKUP_CHARGES. */
  private static final String BACKUP_CHARGES = " INSERT INTO diagnostic_charges_backup (user_name,"
      + " bkp_time, org_name, bed_type, test_id, charge, discount) "
      + " SELECT ?, current_timestamp, org_name, bed_type, test_id, charge, discount "
      + " FROM diagnostic_charges WHERE org_name=?";

  /**
   * Backup charges.
   *
   * @param orgId
   *          the org id
   * @param user
   *          the user
   * @throws SQLException
   *           the SQL exception
   */
  public void backupCharges(String orgId, String user) throws SQLException {
    PreparedStatement ps = con.prepareStatement(BACKUP_CHARGES);
    ps.setString(1, user);
    ps.setString(2, orgId);
    ps.execute();
    ps.close();
  }

  /**
   * Gets the testcharge.
   *
   * @param testid
   *          the testid
   * @param selectedorgname
   *          the selectedorgname
   * @param priority
   *          the priority
   * @return the testcharge
   */
  public static String gettestcharge(String testid, String selectedorgname, String priority) {
    Connection con = null;
    String xml = null;
    Statement stm = null;
    PreparedStatement ps = null;
    PreparedStatement ps1 = null;

    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement("select ORG_ID from organization_details where org_name=?");
      ps.setString(1, selectedorgname);
      String orgid = DataBaseUtil.getStringValueFromDb(ps);
      ps1 = con.prepareStatement(
          "select charge from diagnostic_charges where test_id =? and org_name=? AND BED_TYPE=? "
              + " AND priority=?");
      ps1.setString(1, testid);
      ps1.setString(2, orgid);
      ps1.setString(3, Constants.getConstantValue("BEDTYPE"));
      ps1.setString(4, priority);
      xml = DataBaseUtil.getStringValueFromDb(ps1);

    } catch (Exception exp) {
      exp.printStackTrace();
    } finally {
      try {
        if (ps != null) {
          ps.close();
        }
        if (ps1 != null) {
          ps1.close();
        }
        if (stm != null) {
          stm.close();
        }
        if (con != null) {
          con.close();
        }
      } catch (Exception exp) {
        exp.printStackTrace();
      }

    }

    return xml;
  }

  /** The Constant GET_TEST_DETAILS. */
  private static final String GET_TEST_DETAILS = ""
      + "SELECT  d.test_id,d.test_name,d.sample_needed,dd.ddept_name,"
      + "st.sample_type as type_of_specimen,d.conduction_format,d.status, "
      + "sg.service_group_name, ssg.service_sub_group_name,"
      + "d.conduction_applicable, conducting_doc_mandatory,mandate_additional_info, dcr.charge, "
      + "d.results_entry_applicable,d.diag_code,iic.insurance_category_name ,prior_auth_required,"
      + "allow_rate_increase,allow_rate_decrease,hl7_export_code " + "FROM diagnostics d "
      + "JOIN diagnostics_departments dd USING(ddept_id) "
      + "JOIN service_sub_groups ssg using(service_sub_group_id) "
      + "JOIN service_groups sg using(service_group_id) " + "JOIN item_insurance_categories iic "
      + " ON (iic.insurance_category_id = d.insurance_category_id) "
      + "LEFT JOIN diagnostic_charges dcr ON (d.test_id = dcr.test_id AND dcr.org_name='ORG0001' "
      + "AND dcr.bed_type = 'GENERAL' AND dcr.priority = 'R') "
      + "LEFT JOIN sample_type st ON (st.sample_type_id = d.sample_type_id)  "
      + "WHERE d.status='A' ORDER BY test_id";

  /** The Constant GET_TEST_RESULT_LABEL_DETAILS_FOR_CENTER. */
  private static final String GET_TEST_RESULT_LABEL_DETAILS_FOR_CENTER = ""
      + "SELECT trm.resultlabel_id,d.test_name,dd.ddept_name, trm.resultlabel,"
      + "TEXTCAT_COMMACAT(COALESCE(hcm.center_name,'') ) as center_name, "
      + "dmm.method_name,trm.units,trm.display_order " + "FROM test_results_master trm "
      + "JOIN test_results_center trc ON (trc.resultlabel_id = trm.resultlabel_id) "
      + "JOIN hospital_center_master hcm ON (hcm.center_id = trc.center_id) "
      + "JOIN diagnostics d USING(test_id) " + "JOIN diagnostics_departments dd USING(ddept_id)"
      + "LEFT JOIN diag_methodology_master dmm ON(dmm.method_id = trm.method_id) "
      + "WHERE trc.status = 'A' "
      + "GROUP BY trm.test_id,trm.resultlabel_id, d.test_name, dd.ddept_name, trm.resultlabel, "
      + "dmm.method_name, trm.units, trm.display_order ORDER BY test_id";

  /** The Constant GET_TEST_RESULT_LABEL_DETAILS. */
  private static final String GET_TEST_RESULT_LABEL_DETAILS = ""
      + "SELECT trm.resultlabel_id,d.test_name,dd.ddept_name, trm.resultlabel,"
      + "dmm.method_name,trm.units,trm.display_order " + "FROM test_results_master trm "
      + "JOIN diagnostics d USING(test_id) " + "JOIN diagnostics_departments dd USING(ddept_id)"
      + "LEFT JOIN diag_methodology_master dmm ON(dmm.method_id = trm.method_id) "
      + "GROUP BY trm.test_id,trm.resultlabel_id, d.test_name, dd.ddept_name, trm.resultlabel, "
      + "dmm.method_name, trm.units, trm.display_order ORDER BY test_id";

  /**
   * Gets the all test details.
   *
   * @return the all test details
   * @throws SQLException
   *           the SQL exception
   */
  public static List<BasicDynaBean> getAllTestDetails() throws SQLException {

    List list = null;
    PreparedStatement ps = null;
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_TEST_DETAILS);
      list = DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

    return list;
  }

  /**
   * Gets the all test result label details.
   *
   * @return the all test result label details
   * @throws SQLException
   *           the SQL exception
   */
  public static List<BasicDynaBean> getAllTestResultLabelDetails() throws SQLException {

    List list = null;
    PreparedStatement ps = null;
    Connection con = null;
    int maxCentersIncDefault = (Integer) GenericPreferencesDAO.getAllPrefs()
        .get("max_centers_inc_default");
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      if (maxCentersIncDefault > 1) {
        ps = con.prepareStatement(GET_TEST_RESULT_LABEL_DETAILS_FOR_CENTER);
      } else {
        ps = con.prepareStatement(GET_TEST_RESULT_LABEL_DETAILS);
      }
      list = DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

    return list;
  }

  /** The Constant GET_RESULT_LABELS. */
  public static final String GET_RESULT_LABELS = "SELECT * FROM test_results_master "
      + "WHERE test_id=? AND resultlabel_id=?";

  /**
   * Gets the test result labels.
   *
   * @param testId
   *          the test id
   * @param resultLabelId
   *          the result label id
   * @return the test result labels
   * @throws SQLException
   *           the SQL exception
   */
  public static BasicDynaBean getTestResultLabels(String testId, int resultLabelId)
      throws SQLException {

    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_RESULT_LABELS);
      ps.setString(1, testId);
      ps.setInt(2, resultLabelId);

      List list = DataBaseUtil.queryToDynaList(ps);
      if (list.size() > 0) {
        return (BasicDynaBean) list.get(0);
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return null;
  }

  /** The Constant TESTS_NAMESAND_iDS. */
  private static final String TESTS_NAMESAND_iDS = "select test_name,test_id from diagnostics";

  /**
   * Gets the tests names and ids.
   *
   * @return the tests names and ids
   * @throws SQLException
   *           the SQL exception
   */
  public static List getTestsNamesAndIds() throws SQLException {

    return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(TESTS_NAMESAND_iDS));
  }

  /** The get diag department. */
  private static String GET_DIAG_DEPARTMENT = "SELECT DDEPT_ID, DDEPT_NAME || "
      + "'(' || d.dept_name || ')' as DDEPT_NAME " + "FROM DIAGNOSTICS_DEPARTMENTS dd "
      + "join department d on(d.dept_id = dd.category) "
      + "WHERE dd.STATUS='A' ORDER BY dd.category ";

  /**
   * Gets the diag department hash map.
   *
   * @return the diag department hash map
   * @throws SQLException
   *           the SQL exception
   */
  public static HashMap getDiagDepartmentHashMap() throws SQLException {

    HashMap<String, String> diagDeptHashMap = new HashMap<>();
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    con = DataBaseUtil.getReadOnlyConnection();
    ps = con.prepareStatement(GET_DIAG_DEPARTMENT);
    rs = ps.executeQuery();
    while (rs.next()) {
      diagDeptHashMap.put(rs.getString("ddept_name"), rs.getString("ddept_id"));
    }
    DataBaseUtil.closeConnections(con, ps, rs);

    return diagDeptHashMap;
  }

  /** The Constant TEST_TEMPLATES. */
  private static final String TEST_TEMPLATES = "SELECT d.test_name, dd.ddept_name, tf.format_name "
      + "FROM test_template_master trm " + "JOIN diagnostics d using(test_id) "
      + "JOIN diagnostics_departments dd using(ddept_id) "
      + "JOIN test_format tf on(trm.format_name = tf.testformat_id) " + "order by test_id";

  /**
   * Gets the test templates.
   *
   * @return the test templates
   * @throws SQLException
   *           the SQL exception
   */
  public static List<BasicDynaBean> getTestTemplates() throws SQLException {
    Connection con = null;
    PreparedStatement pstmt = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      pstmt = con.prepareStatement(TEST_TEMPLATES);
      return DataBaseUtil.queryToDynaList(pstmt);

    } finally {
      DataBaseUtil.closeConnections(con, pstmt);

    }

  }

  /** The get diag deps. */
  private static String GET_DIAG_DEPS = "SELECT DDEPT_ID, DDEPT_NAME "
      + "FROM DIAGNOSTICS_DEPARTMENTS dd " + "join department d on(d.dept_id = dd.category) "
      + "WHERE dd.STATUS='A' ORDER BY dd.category ";

  /**
   * Gets the diag dep data.
   *
   * @return the diag dep data
   * @throws SQLException
   *           the SQL exception
   */
  public static HashMap getDiagDepData() throws SQLException {

    HashMap<String, String> diagDeptHashMap = new HashMap<>();
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    con = DataBaseUtil.getReadOnlyConnection();
    ps = con.prepareStatement(GET_DIAG_DEPS);
    rs = ps.executeQuery();
    while (rs.next()) {
      diagDeptHashMap.put(rs.getString("ddept_name"), rs.getString("ddept_id"));
    }
    DataBaseUtil.closeConnections(con, ps, rs);

    return diagDeptHashMap;
  }

  /**
   * Gets the test result ids.
   *
   * @return the test result ids
   * @throws SQLException
   *           the SQL exception
   */
  public static Map getTestResultIds() throws SQLException {
    Connection con = null;
    Map<Integer, String> testRstMap = new HashMap<>();
    PreparedStatement pstmt = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      pstmt = con.prepareStatement("select * from test_results_master");
      List list = DataBaseUtil.queryToDynaList(pstmt);
      Iterator it = list.iterator();
      while (it.hasNext()) {
        BasicDynaBean bean = (BasicDynaBean) it.next();
        testRstMap.put((Integer) bean.get("resultlabel_id"), (String) bean.get("test_id"));
      }

    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }
    return testRstMap;
  }

  /**
   * Gets the test tmt master data.
   *
   * @return the test tmt master data
   * @throws SQLException
   *           the SQL exception
   */
  public static Map<String, String> getTestTmtMasterData() throws SQLException {
    Connection con = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    Map<String, String> tmtMasterData = new HashMap<>();
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      pstmt = con.prepareStatement("select testformat_id,format_name FROM test_format");
      rs = pstmt.executeQuery();
      while (rs.next()) {
        tmtMasterData.put(rs.getString(2), rs.getString(1));
      }
    } finally {
      DataBaseUtil.closeConnections(con, pstmt);

    }
    return tmtMasterData;
  }

  /**
   * Gets the order alias.
   *
   * @param type
   *          the type
   * @param deptId
   *          the dept id
   * @param groupId
   *          the group id
   * @param subGrpId
   *          the sub grp id
   * @return the order alias
   * @throws SQLException
   *           the SQL exception
   */
  public static String getOrderAlias(String type, String deptId, String groupId, String subGrpId)
      throws SQLException {

    BasicDynaBean masterCounts = new OrderMasterDAO().getMastersCounts(type, deptId);
    BasicDynaBean serviceGroup = new GenericDAO("service_groups").findByKey("service_group_id",
        new Integer(groupId));
    BasicDynaBean serviceSubGroup = new GenericDAO("service_sub_groups")
        .findByKey("service_sub_group_id", new Integer(subGrpId));
    String groupCode = (String) serviceGroup.get("service_group_code") == null ? ""
        : (String) serviceGroup.get("service_group_code");
    String subGrpCode = (String) serviceSubGroup.get("service_sub_group_code") == null ? ""
        : (String) serviceSubGroup.get("service_sub_group_code");
    String count = (masterCounts == null) ? "" : masterCounts.get("count").toString();

    return groupCode + subGrpCode + count;

  }

  /** The Constant INSERT_TEST_CHARGE_PLUS. */
  private static final String INSERT_TEST_CHARGE_PLUS = "INSERT INTO diagnostic_charges"
      + " (test_id,org_name,charge,bed_type,priority,username)"
      + "(SELECT test_id,?,ROUND(charge + ?), bed_type, priority,? FROM diagnostic_charges"
      + "  WHERE org_name=?)";

  /** The Constant INSERT_TEST_CHARGE_MINUS. */
  private static final String INSERT_TEST_CHARGE_MINUS = "INSERT INTO diagnostic_charges"
      + " (test_id,org_name,charge,bed_type,priority,username)"
      + "(SELECT test_id,?, GREATEST(ROUND(charge - ?), 0), bed_type, priority,? FROM"
      + "  diagnostic_charges WHERE org_name=?)";

  /** The Constant INSERT_TEST_CHARGE_BY. */
  private static final String INSERT_TEST_CHARGE_BY = "INSERT INTO diagnostic_charges"
      + " (test_id,org_name,charge,bed_type,priority,username)"
      + "(SELECT test_id,?,doroundvarying(charge,?,?), bed_type, priority,? FROM"
      + "  diagnostic_charges WHERE org_name=?)";

  /** The Constant INSERT_TEST_WITH_DISCOUNTS_BY. */
  private static final String INSERT_TEST_WITH_DISCOUNTS_BY = "INSERT INTO diagnostic_charges"
      + " (test_id,org_name,charge,discount,bed_type,priority,username)"
      + "(SELECT test_id,?,doroundvarying(charge,?,?), doroundvarying(discount,?,?), bed_type,"
      + "  priority,? FROM diagnostic_charges WHERE org_name=?)";

  /**
   * Adds the org for tests.
   *
   * @param con
   *          the con
   * @param newOrgId
   *          the new org id
   * @param varianceType
   *          the variance type
   * @param varianceValue
   *          the variance value
   * @param varianceBy
   *          the variance by
   * @param useValue
   *          the use value
   * @param baseOrgId
   *          the base org id
   * @param nearstRoundOfValue
   *          the nearst round of value
   * @param userName
   *          the user name
   * @param orgName
   *          the org name
   * @return true, if successful
   * @throws Exception
   *           the exception
   */
  public static boolean addOrgForTests(Connection con, String newOrgId, String varianceType,
      Double varianceValue, Double varianceBy, boolean useValue, String baseOrgId,
      Double nearstRoundOfValue, String userName, String orgName) throws Exception {

    return addOrgForTests(con, newOrgId, varianceType, varianceValue, varianceBy, useValue,
        baseOrgId, nearstRoundOfValue, userName, orgName, false);

  }

  /**
   * Adds the org for tests.
   *
   * @param con
   *          the con
   * @param newOrgId
   *          the new org id
   * @param varianceType
   *          the variance type
   * @param varianceValue
   *          the variance value
   * @param varianceBy
   *          the variance by
   * @param useValue
   *          the use value
   * @param baseOrgId
   *          the base org id
   * @param nearstRoundOfValue
   *          the nearst round of value
   * @param userName
   *          the user name
   * @param orgName
   *          the org name
   * @param updateDiscounts
   *          the update discounts
   * @return true, if successful
   * @throws Exception
   *           the exception
   */
  public static boolean addOrgForTests(Connection con, String newOrgId, String varianceType,
      Double varianceValue, Double varianceBy, boolean useValue, String baseOrgId,
      Double nearstRoundOfValue, String userName, String orgName, boolean updateDiscounts)
      throws Exception {
    boolean status = false;
    PreparedStatement ps = null;
    GenericDAO.alterTrigger(con, "DISABLE", "diagnostic_charges",
        "z_diagnostictest_charges_audit_trigger");

    if (useValue) {
      if (varianceType.equals("Incr")) {
        ps = con.prepareStatement(INSERT_TEST_CHARGE_PLUS);
        ps.setString(1, newOrgId);
        ps.setDouble(2, varianceValue);
        ps.setString(3, userName);
        ps.setString(4, baseOrgId);

        int inc = ps.executeUpdate();
        logger.debug(Integer.toString(inc));
        if (inc >= 0) {
          status = true;
        }
      } else {
        ps = con.prepareStatement(INSERT_TEST_CHARGE_MINUS);
        ps.setString(1, newOrgId);
        ps.setDouble(2, varianceValue);
        ps.setString(3, userName);
        ps.setString(4, baseOrgId);

        int inc = ps.executeUpdate();
        logger.debug(Integer.toString(inc));
        if (inc >= 0) {
          status = true;
        }
      }
    } else {
      if (!varianceType.equals("Incr")) {
        varianceBy = new Double(-varianceBy);
      }
      /*
       * ps = con.prepareStatement(INSERT_TEST_CHARGE_BY); ps.setString(1, newOrgId);
       * ps.setBigDecimal(2, new BigDecimal(varianceBy)); ps.setBigDecimal(3, new
       * BigDecimal(nearstRoundOfValue)); ps.setString(4, userName); ps.setString(5, baseOrgId);
       * 
       * int inc = ps.executeUpdate();
       */
      int inc = insertChargesByPercent(con, newOrgId, baseOrgId, userName, varianceBy,
          nearstRoundOfValue, updateDiscounts);
      logger.debug(Integer.toString(inc));
      if (inc >= 0) {
        status = true;
      }
    }
    if (null != ps) {
      ps.close();
    }

    status &= new AuditLogDao("Master", "diagnostic_charges_audit_log").logMasterChange(con,
        userName, "INSERT", "org_id", orgName);

    return status;
  }

  /**
   * Insert charges by percent.
   *
   * @param con
   *          the con
   * @param newOrgId
   *          the new org id
   * @param baseOrgId
   *          the base org id
   * @param userName
   *          the user name
   * @param varianceBy
   *          the variance by
   * @param nearstRoundOfValue
   *          the nearst round of value
   * @param updateDiscounts
   *          the update discounts
   * @return the int
   * @throws Exception
   *           the exception
   */
  private static int insertChargesByPercent(Connection con, String newOrgId, String baseOrgId,
      String userName, Double varianceBy, Double nearstRoundOfValue, boolean updateDiscounts)
      throws Exception {

    int ndx = 1;
    int numCharges = 1;

    PreparedStatement pstmt = null;
    try {
      pstmt = con.prepareStatement(
          updateDiscounts ? INSERT_TEST_WITH_DISCOUNTS_BY : INSERT_TEST_CHARGE_BY);
      pstmt.setString(ndx++, newOrgId);

      for (int i = 0; i < numCharges; i++) {
        pstmt.setBigDecimal(ndx++, new BigDecimal(varianceBy));
        pstmt.setBigDecimal(ndx++, new BigDecimal(nearstRoundOfValue));
      }

      if (updateDiscounts) { // go one more round setting the parameters
        for (int i = 0; i < numCharges; i++) {
          pstmt.setBigDecimal(ndx++, new BigDecimal(varianceBy));
          pstmt.setBigDecimal(ndx++, new BigDecimal(nearstRoundOfValue));
        }
      }

      pstmt.setString(ndx++, userName);
      pstmt.setString(ndx++, baseOrgId);

      return pstmt.executeUpdate();

    } finally {
      if (null != pstmt) {
        pstmt.close();
      }
    }
  }

  /** The Constant INSERT_TEST_CODES_FOR_ORG. */
  private static final String INSERT_TEST_CODES_FOR_ORG = "INSERT INTO test_org_details "
      + " SELECT test_id, ?, applicable, item_code, code_type, ?, 'N'"
      + " FROM test_org_details WHERE  org_id = ?";

  /**
   * Adds the org codes for tests.
   *
   * @param con
   *          the con
   * @param newOrgId
   *          the new org id
   * @param varianceType
   *          the variance type
   * @param varianceValue
   *          the variance value
   * @param varianceBy
   *          the variance by
   * @param useValue
   *          the use value
   * @param baseOrgId
   *          the base org id
   * @param nearstRoundOfValue
   *          the nearst round of value
   * @param userName
   *          the user name
   * @return true, if successful
   * @throws Exception
   *           the exception
   */
  public static boolean addOrgCodesForTests(Connection con, String newOrgId, String varianceType,
      Double varianceValue, Double varianceBy, boolean useValue, String baseOrgId,
      Double nearstRoundOfValue, String userName) throws Exception {
    boolean status = false;
    PreparedStatement ps = null;
    BasicDynaBean obean = new OrgMasterDao().findByKey(con, "org_id", newOrgId);
    String rateSheetId = ("N".equals(obean.get("is_rate_sheet")) ? baseOrgId : null);
    try {
      ps = con.prepareStatement(INSERT_TEST_CODES_FOR_ORG);
      ps.setString(1, newOrgId);
      ps.setString(2, rateSheetId);
      ps.setString(3, baseOrgId);

      int inc = ps.executeUpdate();
      logger.debug(Integer.toString(inc));
      if (inc >= 0) {
        status = true;
      }

    } finally {
      if (ps != null) {
        ps.close();
      }
    }
    return status;
  }

  /** The Constant UPDATE_DIAG_CHARGES_PLUS. */
  private static final String UPDATE_DIAG_CHARGES_PLUS = "UPDATE diagnostic_charges totab SET "
      + " charge = round(fromtab.charge + ?)" + " FROM diagnostic_charges fromtab"
      + " WHERE totab.org_name = ? AND fromtab.org_name = ?"
      + " AND totab.test_id = fromtab.test_id AND totab.bed_type = fromtab.bed_type"
      + "  AND totab.is_override='N'";

  /** The Constant UPDATE_DIAG_CHARGES_MINUS. */
  private static final String UPDATE_DIAG_CHARGES_MINUS = "UPDATE diagnostic_charges totab SET "
      + " charge = GREATEST(round(fromtab.charge - ?), 0)" + " FROM diagnostic_charges fromtab"
      + " WHERE totab.org_name = ? AND fromtab.org_name = ?"
      + " AND totab.test_id = fromtab.test_id AND totab.bed_type = fromtab.bed_type AND "
      + " totab.is_override='N'";

  /** The Constant UPDATE_DIAG_CHARGES_BY. */
  private static final String UPDATE_DIAG_CHARGES_BY = "UPDATE diagnostic_charges totab SET "
      + " charge = doroundvarying(fromtab.charge,?,?)" + " FROM diagnostic_charges fromtab"
      + " WHERE totab.org_name = ? AND fromtab.org_name = ?"
      + " AND totab.test_id = fromtab.test_id AND totab.bed_type = fromtab.bed_type AND"
      + "  totab.is_override='N'";

  /**
   * Update org for tests.
   *
   * @param con
   *          the con
   * @param orgId
   *          the org id
   * @param varianceType
   *          the variance type
   * @param varianceValue
   *          the variance value
   * @param varianceBy
   *          the variance by
   * @param useValue
   *          the use value
   * @param baseOrgId
   *          the base org id
   * @param nearstRoundOfValue
   *          the nearst round of value
   * @param userName
   *          the user name
   * @param orgName
   *          the org name
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public static boolean updateOrgForTests(Connection con, String orgId, String varianceType,
      Double varianceValue, Double varianceBy, boolean useValue, String baseOrgId,
      Double nearstRoundOfValue, String userName, String orgName) throws SQLException, IOException {

    boolean status = false;
    PreparedStatement pstmt = null;
    GenericDAO.alterTrigger("DISABLE", "diagnostic_charges",
        "z_diagnostictest_charges_audit_trigger");

    if (useValue) {

      if (varianceType.equals("Incr")) {
        pstmt = con.prepareStatement(UPDATE_DIAG_CHARGES_PLUS);
      } else {
        pstmt = con.prepareStatement(UPDATE_DIAG_CHARGES_MINUS);
      }

      pstmt.setBigDecimal(1, new BigDecimal(varianceValue));
      pstmt.setString(2, orgId);
      pstmt.setString(3, baseOrgId);

      int inc = pstmt.executeUpdate();
      if (inc >= 0) {
        status = true;
      }

    } else {

      pstmt = con.prepareStatement(UPDATE_DIAG_CHARGES_BY);
      if (!varianceType.equals("Incr")) {
        varianceBy = new Double(-varianceBy);
      }

      pstmt.setBigDecimal(1, new BigDecimal(varianceBy));
      pstmt.setBigDecimal(2, new BigDecimal(nearstRoundOfValue));
      pstmt.setString(3, orgId);
      pstmt.setString(4, baseOrgId);

      int inc = pstmt.executeUpdate();
      if (inc >= 0) {
        status = true;
      }

    }
    status &= new AuditLogDao("Master", "diagnostic_charges_audit_log").logMasterChange(con,
        userName, "UPDATE", "org_id", orgName);

    pstmt.close();

    return status;
  }

  /** The Constant TEST_ORG_DETAILS. */
  private static final String TEST_ORG_DETAILS = " select d.*, dorg.org_id, dorg.applicable, "
      + " dorg.item_code, dorg.code_type,od.org_name, dorg.base_rate_sheet_id "
      + " FROM diagnostics  d " + " JOIN test_org_details dorg on dorg.test_id = d.test_id "
      + " JOIN organization_details od on (od.org_id = dorg.org_id) "
      + " WHERE d.test_id=? and dorg.org_id=? ";

  /**
   * Gets the test org details.
   *
   * @param testId
   *          the test id
   * @param orgId
   *          the org id
   * @return the test org details
   * @throws SQLException
   *           the SQL exception
   */
  public static BasicDynaBean getTestOrgDetails(String testId, String orgId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(TEST_ORG_DETAILS);
      ps.setString(1, testId);
      ps.setString(2, orgId);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_ALL_CHARGES_FOR_ORG. */
  private static final String GET_ALL_CHARGES_FOR_ORG = " select test_id,bed_type,charge, discount"
      + "  from diagnostic_charges where org_name=? ";

  /**
   * Gets the all charges for org.
   *
   * @param orgId
   *          the org id
   * @param testId
   *          the test id
   * @return the all charges for org
   * @throws SQLException
   *           the SQL exception
   */
  public static List<BasicDynaBean> getAllChargesForOrg(String orgId, String testId)
      throws SQLException {

    Connection con = null;
    PreparedStatement ps = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_ALL_CHARGES_FOR_ORG + "and test_id=? ");
      ps.setString(1, orgId);
      ps.setString(2, testId);
      return DataBaseUtil.queryToDynaList(ps);

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_DERIVED_RATE_PLAN_DETAILS. */
  private static final String GET_DERIVED_RATE_PLAN_DETAILS = "select rp.org_id,od.org_name, "
      + " case when rate_variation_percent<0 then 'Increase By'"
      + "  else 'Decrease By' end as discormarkup, "
      + " rate_variation_percent,round_off_amount,tod.applicable,tod.test_id,rp.base_rate_sheet_id "
      + " from rate_plan_parameters rp " + " join organization_details od on(od.org_id=rp.org_id) "
      + " join test_org_details tod on (tod.org_id = rp.org_id) "
      + " where rp.base_rate_sheet_id =?  and test_id=? ";

  /**
   * Gets the derived rate plan details.
   *
   * @param baseRateSheetId
   *          the base rate sheet id
   * @param testId
   *          the test id
   * @return the derived rate plan details
   * @throws SQLException
   *           the SQL exception
   */
  public static List<BasicDynaBean> getDerivedRatePlanDetails(String baseRateSheetId, String testId)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_DERIVED_RATE_PLAN_DETAILS);
      ps.setString(1, baseRateSheetId);
      ps.setString(2, testId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_AVAILABLE_CENTERS. */
  private static final String GET_AVAILABLE_CENTERS = " SELECT distinct(hcm.center_id),"
      + "  hcm.center_name FROM hospital_center_master hcm ";

  /**
   * Gets the center details.
   *
   * @param resultsCenterNames
   *          the results center names
   * @return the center details
   * @throws Exception
   *           the exception
   */
  public static List getCenterDetails(String[] resultsCenterNames) throws Exception {
    StringBuilder builder = new StringBuilder(GET_AVAILABLE_CENTERS);
    DataBaseUtil.addWhereFieldInList(builder, "center_name", Arrays.asList(resultsCenterNames),
        false);
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(builder.toString());
      int inc = 1;
      if (resultsCenterNames != null) {
        for (String val : resultsCenterNames) {
          ps.setString(inc++, val.trim());
        }
      }
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the result center next sequence.
   *
   * @return the result center next sequence
   * @throws SQLException
   *           the SQL exception
   */
  public static int getResultCenterNextSequence() throws SQLException {
    Connection con = null;
    con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement("select nextval('test_results_center_seq')");
      return DataBaseUtil.getIntValueFromDb(ps);
    } finally {
      con.close();
      ps.close();
    }
  }

  /**
   * Gets the available centers.
   *
   * @return the available centers
   * @throws SQLException
   *           the SQL exception
   */
  public static HashMap getAvailableCenters() throws SQLException {

    HashMap<String, String> availableCenterHashMap = new HashMap<>();
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    con = DataBaseUtil.getReadOnlyConnection();
    ps = con.prepareStatement(GET_AVAILABLE_CENTERS);
    rs = ps.executeQuery();
    while (rs.next()) {
      availableCenterHashMap.put(rs.getString("center_name"), rs.getString("center_id"));
    }
    DataBaseUtil.closeConnections(con, ps, rs);

    return availableCenterHashMap;
  }

  /** The Constant GET_SAVED_CENTERS. */
  private static final String GET_SAVED_CENTERS = " SELECT hcm.center_name "
      + " FROM hospital_center_master hcm "
      + " JOIN test_results_center trc ON (trc.center_id = hcm.center_id) "
      + " WHERE trc.resultlabel_id = ? ";

  /**
   * Gets the saved centers.
   *
   * @param resultLabelID
   *          the result label ID
   * @return the saved centers
   * @throws SQLException
   *           the SQL exception
   */
  public static List<String> getSavedCenters(Integer resultLabelID) throws SQLException {
    return DataBaseUtil.queryToList(GET_SAVED_CENTERS, resultLabelID);
  }

  /** The Constant DELETE_RESULTS_CENTER. */
  private static final String DELETE_RESULTS_CENTER = "DELETE FROM test_results_center WHERE "
      + " resultlabel_id = ? AND center_id = ? ";

  /**
   * Delete results center.
   *
   * @param resultLabelId
   *          the result label id
   * @param centerId
   *          the center id
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public static boolean deleteResultsCenter(int resultLabelId, int centerId) throws SQLException {
    boolean status = false;
    Connection con = null;
    con = DataBaseUtil.getConnection();
    PreparedStatement ps = con.prepareStatement(DELETE_RESULTS_CENTER);
    ps.setInt(1, resultLabelId);
    ps.setInt(2, centerId);
    int res = ps.executeUpdate();
    if (res >= 0) {
      status = true;
    }
    return status;
  }

  /** The Constant GET_INSURANCE_CATEGORY. */
  private static final String GET_INSURANCE_CATEGORY = " SELECT insurance_category_id, "
      + " insurance_category_name FROM item_insurance_categories";

  /**
   * Gets the insurance category data.
   *
   * @return the insurance category data
   * @throws SQLException
   *           the SQL exception
   */
  public static Map<String, Integer> getInsuranceCategoryData() throws SQLException {

    HashMap<String, Integer> insuranceCategoryHashMap = new HashMap<>();
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_INSURANCE_CATEGORY);
      rs = ps.executeQuery();
      while (rs.next()) {
        insuranceCategoryHashMap.put(rs.getString("insurance_category_name"),
            rs.getInt("insurance_category_id"));
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }
    return insuranceCategoryHashMap;
  }

  /** The Constant GET_TESTS_ITEM_SUB_GROUP_TAX_DETAILS. */
  private static final String GET_TESTS_ITEM_SUB_GROUP_TAX_DETAILS = "SELECT isg.item_subgroup_id,"
      + "  isg.subgroup_code, isg.item_subgroup_name, ig.group_code "
      + " FROM diagnostics_item_sub_groups disg "
      + " JOIN item_sub_groups isg ON(disg.item_subgroup_id = isg.item_subgroup_id) "
      + " JOIN item_groups ig ON (ig.item_group_id = isg.item_group_id) "
      + " WHERE disg.test_id = ? ";

  /**
   * Gets the diagnostics item sub group tax details.
   *
   * @param itemId
   *          the item id
   * @return the diagnostics item sub group tax details
   * @throws SQLException
   *           the SQL exception
   */
  public List<BasicDynaBean> getDiagnosticsItemSubGroupTaxDetails(String itemId)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_TESTS_ITEM_SUB_GROUP_TAX_DETAILS);
      ps.setString(1, itemId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_CATEGORY_ID_BASED_ON_PLAN. */
  private static final String GET_CATEGORY_ID_BASED_ON_PLAN = "select dicm.diagnostic_test_id "
      + " as item_id, " + " COALESCE(pipd.insurance_category_id,0)"
      + " as primary_insurance_category_id,"
      + " COALESCE(sipd.insurance_category_id,0) as secondary_insurance_category_id"
      + " from diagnostic_test_insurance_category_mapping dicm "
      + " Left join insurance_plan_details pipd ON pipd.insurance_category_id="
      + " dicm.insurance_category_id " + " and pipd.plan_id =?" + " and pipd.patient_type=?"
      + " Left join insurance_plan_details sipd ON sipd.insurance_category_id= "
      + " dicm.insurance_category_id " + " and sipd.plan_id =?" + " and sipd.patient_type=?"
      + " Left join item_insurance_categories foo ON "
      + " foo.insurance_category_id = pipd.insurance_category_id "
      + " Left join item_insurance_categories foo1 ON "
      + " foo1.insurance_category_id = sipd.insurance_category_id";

  /**
   * Gets the cat id based on plan ids.
   *
   * @param itemIds
   *          the item ids
   * @param planIds
   *          the plan ids
   * @param visitType
   *          the visit type
   * @return the cat id based on plan ids
   * @throws SQLException
   *           the SQL exception
   */
  public List<BasicDynaBean> getCatIdBasedOnPlanIds(List<String> itemIds, Set<Integer> planIds,
      String visitType) throws SQLException {

    Object[] planId = planIds.toArray();

    List<Object> args = new ArrayList<>();
    args.add((int) planId[0]);
    args.add(visitType);
    if (planId.length > 1) {
      args.add((int) planId[1]);
    } else {
      args.add(-1); // return default
    }
    args.add(visitType);
    args.addAll(itemIds);
    String[] placeHolderArr = new String[itemIds.size()];
    Arrays.fill(placeHolderArr, "?");
    String placeHolders = StringUtils.arrayToCommaDelimitedString(placeHolderArr);
    String query = GET_CATEGORY_ID_BASED_ON_PLAN + " where dicm.diagnostic_test_id in ( "
        + placeHolders + ") " + " order by foo.priority,foo1.priority limit 1;";
    return DataBaseUtil.queryToDynaList(query, args.toArray());
  }

}
