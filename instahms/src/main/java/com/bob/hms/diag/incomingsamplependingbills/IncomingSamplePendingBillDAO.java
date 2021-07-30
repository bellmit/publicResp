package com.bob.hms.diag.incomingsamplependingbills;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.billing.Bill;
import com.insta.hms.billing.BillDAO;
import com.insta.hms.billing.ChargeDTO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.instaapi.common.DbUtil;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.util.StringUtils;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

/**
 * The Class IncomingSamplePendingBillDAO.
 *
 * @author lakshmi.p
 */
public class IncomingSamplePendingBillDAO {

  /** The con. */
  Connection con = null;

  /**
   * Instantiates a new incoming sample pending bill DAO.
   *
   * @param con the con
   */
  public IncomingSamplePendingBillDAO(Connection con) {
    this.con = con;
  }

  /** The Constant GET_INCOMING_SAMPLE_FIELDS. */
  public static final String GET_INCOMING_SAMPLE_FIELDS = " SELECT  isr.billno,isr.patient_name,"
      + " isr.incoming_visit_id, ih.hospital_name,"
      + "isr.patient_other_info, b.status,b.bill_type, isr.center_id";

  /** The Constant GET_INCOMING_SAMPLE_FIELDS_COUNT. */
  private static final String GET_INCOMING_SAMPLE_FIELDS_COUNT = " SELECT"
      + " count(distinct(isr.billno)) ";

  /** The Constant GET_INCOMING_SAMPLE_FIELDS_TABLES. */
  private static final String GET_INCOMING_SAMPLE_FIELDS_TABLES = " FROM"
      + " incoming_sample_registration isr "
      + "  join incoming_hospitals  ih on isr.orig_lab_name = ih.hospital_id"
      + "  left join patient_details pd using(mr_no)" + "  join bill b on b.bill_no = isr.billno ";

  /** The Constant GET_INCOMING_SAMPLE_FIELDS_INIT_WHERE. */
  private static final String GET_INCOMING_SAMPLE_FIELDS_INIT_WHERE = " WHERE "
      + "( patient_confidentiality_check(COALESCE(pd.patient_group, 0),pd.mr_no) )";

  /** The Constant GET_INCOMING_SAMPLE_FIELDS_GROUP_BY. */
  private static final String GET_INCOMING_SAMPLE_FIELDS_GROUP_BY = " isr.billno, isr.patient_name,"
      + "isr.incoming_visit_id,"
      + "ih.hospital_name,b.status,b.bill_type,isr.patient_other_info, isr.center_id ";

  /**
   * Gets the incoming sample pending bills list.
   *
   * @param billNo        the bill no
   * @param status        the status
   * @param type          the type
   * @param pageNum       the page num
   * @param customerName  the customer name
   * @param hospName      the hosp name
   * @param fromDate      the from date
   * @param toDate        the to date
   * @param sortFeild     the sort feild
   * @param sortReverse   the sort reverse
   * @param category      the category
   * @param patOtherInfo  the pat other info
   * @param requestCenter the request center
   * @return the incoming sample pending bills list
   * @throws SQLException the SQL exception
   */
  public static PagedList getIncomingSamplePendingBillsList(String billNo, List status, List type,
      int pageNum, String customerName, String hospName, Date fromDate, Date toDate,
      String sortFeild, boolean sortReverse, String category, String patOtherInfo,
      String requestCenter) throws SQLException {

    Connection con = null;
    ArrayList sampleBillList = null;
    int totalCount = 0;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      if (sortFeild == null || sortFeild.equals("")) {
        sortFeild = "isr.billno";
        sortReverse = true;
      }

      SearchQueryBuilder qb = null;
      qb = new SearchQueryBuilder(con, GET_INCOMING_SAMPLE_FIELDS, GET_INCOMING_SAMPLE_FIELDS_COUNT,
          GET_INCOMING_SAMPLE_FIELDS_TABLES, GET_INCOMING_SAMPLE_FIELDS_INIT_WHERE,
          GET_INCOMING_SAMPLE_FIELDS_GROUP_BY, sortFeild, sortReverse, 20, pageNum);

      // add the value for the initial where clause
      qb.addFilter(SearchQueryBuilder.STRING, "isr.patient_name", "ILIKE", customerName);
      qb.addFilter(SearchQueryBuilder.STRING, "isr.billno", "=", billNo);
      qb.addFilter(SearchQueryBuilder.STRING, "ih.hospital_name", "ILIKE", hospName);
      // qb.addFilter(SearchQueryBuilder.DATE, "pmsm.sale_date", ">=", fromDate);
      // qb.addFilter(SearchQueryBuilder.DATE, "pmsm.sale_date", "<=", toDate);
      qb.addFilter(qb.STRING, "b.status", "IN", status);
      qb.addFilter(qb.STRING, "b.bill_type", "IN", type);
      qb.addFilter(qb.STRING, "isr.category", "=", category);
      qb.addFilter(qb.STRING, "isr.patient_other_info", "=", patOtherInfo);
      if (!(GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1
          && Integer.parseInt(requestCenter) == 0)) {
        qb.addFilter(qb.INTEGER, "isr.center_id", "=", Integer.parseInt(requestCenter));
      }
      qb.addSecondarySort("isr.patient_name");
      qb.addSecondarySort("ih.hospital_name", true);

      qb.build();
      PreparedStatement psData = qb.getDataStatement();
      PreparedStatement psCount = qb.getCountStatement();
      sampleBillList = DataBaseUtil.queryToArrayList(psData);
      totalCount = Integer.parseInt(DataBaseUtil.getStringValueFromDb(psCount));
      psData.close();
      psCount.close();

    } finally {
      DbUtil.closeConnections(con, null);
    }

    return new PagedList(sampleBillList, totalCount, 20, pageNum);

  }

  /** The get incoming patient details. */
  public static String GET_INCOMING_PATIENT_DETAILS = "SELECT isr.patient_name, "
      + " get_patient_age(null,null,isr.isr_dateofbirth,isr.patient_age)as patient_age,"
      + " get_patient_age_in(null,null,isr.isr_dateofbirth,isr.age_unit) as age_unit, "
      + " isr.patient_gender, isr.billno,ih.hospital_name,b.bill_type,b.status,"
      + " isr.incoming_visit_id,"
      + " coalesce(d.doctor_name,r.referal_name) as doctor_name, category"
      + " FROM incoming_sample_registration isr "
      + " join incoming_hospitals ih on isr.orig_lab_name = ih.hospital_id "
      + " join bill b on b.bill_no = isr.billno"
      + " left join referral r on r.referal_no = isr.referring_doctor  "
      + " left join doctors d on d.doctor_id = isr.referring_doctor  "
      + " WHERE isr.incoming_visit_id = ?";

  /**
   * Gets the incoming patient details.
   *
   * @param incomingVisitId the incoming visit id
   * @return the incoming patient details
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getIncomingPatientDetails(String incomingVisitId)
      throws SQLException {
    BasicDynaBean incPatBean = null;
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_INCOMING_PATIENT_DETAILS);
      ps.setString(1, incomingVisitId);
      incPatBean = DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DbUtil.closeConnections(con, ps);
    }
    return incPatBean;
  }

  /** The get incoming sample details. */
  public static String GET_INCOMING_SAMPLE_DETAILS = "SELECT bc.act_description as test_name,"
      + "isrd.orig_sample_no, "
      + " bc.act_description_id as test_id,bc.act_rate,bc.act_quantity,"
      + " bc.amount,bc.discount,bc.charge_id,bc.status, tp.sample_no "
      + " FROM tests_prescribed tp " + " JOIN incoming_sample_registration_details isrd "
      + " ON (isrd.prescribed_id = tp.prescribed_id " + " and isrd.incoming_visit_id=tp.pat_id ) "
      + " JOIN bill_activity_charge bac ON (bac.activity_id::text = tp.prescribed_id::text "
      + " AND bac.activity_code='DIA') " + " JOIN bill_charge bc on (bc.charge_id=bac.charge_id) "
      + " WHERE bc.bill_no =? AND bc.charge_head = ? ";

  /**
   * Gets the sample details list.
   *
   * @param billNo   the bill no
   * @param category the category
   * @return the sample details list
   * @throws SQLException the SQL exception
   */
  public static List getSampleDetailsList(String billNo, String category) throws SQLException {
    List sampleDetailsList = null;
    Connection con = null;
    PreparedStatement ps = null;
    try {

      String chargeHead = null;
      if (category.equals("DEP_LAB")) {
        chargeHead = ChargeDTO.CH_DIAG_LAB;
      } else {
        chargeHead = ChargeDTO.CH_DIAG_RAD;
      }

      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_INCOMING_SAMPLE_DETAILS);
      ps.setString(1, billNo);
      ps.setString(2, chargeHead);
      sampleDetailsList = DataBaseUtil.queryToDynaList(ps);
    } finally {
      DbUtil.closeConnections(con, ps);
    }
    return sampleDetailsList;
  }

  /** The package details. */
  public static String PACKAGE_DETAILS = "SELECT bc.act_description as pack_name,"
      + " null as orig_sample_no, "
      + " bc.act_description_id as pack_id,bc.act_rate,bc.act_quantity, "
      + " bc.amount,bc.discount,bc.charge_id,bc.status, null as sample_no  "
      + " FROM package_prescribed pp " + " JOIN bill_activity_charge bac "
      + " ON (bac.activity_id::text = pp.prescription_id::text AND bac.activity_code='PKG')  "
      + " JOIN bill_charge bc on (bc.charge_id=bac.charge_id "
      + " and bc.act_description_id::text=pp.package_id::text) "
      + " WHERE bc.bill_no =? AND bc.charge_head = 'PKGPKG' ";

  /**
   * Gets the package details.
   *
   * @param billNo the bill no
   * @return the package details
   * @throws SQLException the SQL exception
   */
  public static List getPackageDetails(String billNo) throws SQLException {
    List packageDetailsList = null;
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(PACKAGE_DETAILS);
      ps.setString(1, billNo);
      packageDetailsList = DataBaseUtil.queryToDynaList(ps);
    } finally {
      ps.close();
      con.close();
    }
    return packageDetailsList;
  }

  /** The Constant TEST_DETAILS_IN_PACKAGE. */
  private static final String TEST_DETAILS_IN_PACKAGE = "SELECT bc.act_description_id as pack_id,"
      + "  d.test_name,"
      + " isrd.orig_sample_no, tp.sample_no FROM incoming_sample_registration isr "
      + " JOIN bill_charge bc on (isr.billno = bc.bill_no and bc.charge_head='PKGPKG') "
      + " JOIN bill_activity_charge bac on (bac.charge_id = bc.charge_id) "
      + " JOIN tests_prescribed tp on (tp.prescribed_id::text = bac.activity_id::text) "
      + " JOIN incoming_sample_registration_details isrd "
      + " on (isrd.prescribed_id = tp.prescribed_id) "
      + " JOIN diagnostics d on (d.test_id = tp.test_id) "
      + " WHERE isr.incoming_visit_id=? and bac.activity_code !='PKG' ";

  /**
   * Gets the test details in package.
   *
   * @param incomingVisitId the incoming visit id
   * @param packageIds      the package ids
   * @return the test details in package
   * @throws SQLException the SQL exception
   */
  public static List getTestDetailsInPackage(String incomingVisitId, String[] packageIds)
      throws SQLException {
    List testList = null;
    StringBuilder query = new StringBuilder(TEST_DETAILS_IN_PACKAGE);
    if (packageIds != null && packageIds.length > 0 && !packageIds[0].equals("0")
        && !packageIds[0].isEmpty()) {
      query.append(" AND bc.act_description_id IN (");
      String[] placeholdersArr = new String[packageIds.length];
      Arrays.fill(placeholdersArr, "?");
      query.append(StringUtils.arrayToCommaDelimitedString(placeholdersArr));
      query.append(")");
    }
    List<Object> args = new ArrayList<>();
    args.add(incomingVisitId);
    args.addAll(Arrays.asList(packageIds));
    try (Connection con = DataBaseUtil.getReadOnlyConnection();
        PreparedStatement ps = con.prepareStatement(query.toString())) {
      ListIterator<Object> argsIterator = args.listIterator();
      while (argsIterator.hasNext()) {
        ps.setObject(argsIterator.nextIndex() + 1, argsIterator.next());
      }
      testList = DataBaseUtil.queryToDynaList(ps);
    }
    return testList;
  }

  /** The update bill status. */
  private static String UPDATE_BILL_STATUS = "UPDATE bill SET "
      + " status=?, discharge_status=?, finalized_date=?, closed_date=?, closed_by=?, "
      + "mod_time=?, payment_status = ?, last_finalized_at = ?  WHERE bill_no=?";

  /**
   * Update bill status.
   *
   * @param con      the con
   * @param billNo   the bill no
   * @param closedby the closedby
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean updateBillStatus(Connection con, String billNo, String closedby)
      throws SQLException {
    boolean success = true;
    BillDAO bdao = new BillDAO(con);
    Bill bill = new Bill();
    String billStatus = null;
    String disChargeStatus = null;
    PreparedStatement ps = null;
    java.sql.Timestamp now = null;
    try {
      billStatus = bill.BILL_STATUS_CLOSED;
      disChargeStatus = bill.BILL_DISCHARGE_OK;
      now = new Timestamp(new java.util.Date().getTime());
      ps = con.prepareStatement(UPDATE_BILL_STATUS);
      ps.setString(1, billStatus);
      ps.setString(2, disChargeStatus);
      ps.setTimestamp(3, now);
      ps.setTimestamp(4, now);
      ps.setString(5, closedby);
      ps.setTimestamp(6, now);
      ps.setString(7, "P");
      ps.setTimestamp(8, now);
      ps.setString(9, billNo);

      int count = ps.executeUpdate();
      if (count > 0) {
        success = true;
      } else {
        success = false;
      }
    } finally {
      ps.close();
    }
    return success;
  }

  /** The Constant TESTS_CONDUCTED. */
  public static final String TESTS_CONDUCTED = "select * from tests_prescribed tp "
      + " join  incoming_sample_registration isr on isr.incoming_visit_id = tp.pat_id "
      + " where isr.billno = ? and tp.conducted= 'N' ";

  /**
   * Check tests conducted in bill.
   *
   * @param billno the billno
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public static boolean checkTestsConductedInBill(String billno) throws SQLException {
    List<BasicDynaBean> conductedTestsList = null;
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(TESTS_CONDUCTED);
      ps.setString(1, billno);
      conductedTestsList = DataBaseUtil.queryToDynaList(ps);
    } finally {
      ps.close();
      con.close();
    }
    /*
     * for (BasicDynaBean b : conductedTestsList) { if(b.get("test_conducted").equals("N")) {
     * success = true; break; } }
     */
    return conductedTestsList != null && conductedTestsList.size() > 0;
  }

}
