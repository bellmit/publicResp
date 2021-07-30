package com.bob.hms.otmasters.opemaster;

import com.bob.hms.common.AutoIncrementId;
import com.bob.hms.common.Constants;
import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class OperationMasterDAO.
 */
public class OperationMasterDAO {

  /** The logger. */
  Logger logger = LoggerFactory.getLogger(OperationMasterDAO.class);

  /** The operation master dao. */
  private static OperationMasterDAO operationMasterDao = new OperationMasterDAO();
  
  /** The bddao. */
  private static BedMasterDAO bddao = new BedMasterDAO();

  public static final String GET_DEPARTMENTS = "SELECT * FROM DEPARTMENT"
      + " WHERE STATUS='A' ORDER BY DEPT_NAME";

  /**
   * Gets the departments.
   *
   * @return the departments
   */
  public ArrayList getDepartments() {

    return DataBaseUtil
        .queryToArrayList(GET_DEPARTMENTS);
  }


  /** Rate Plan ID fetch query. **/
  private static final String RP_QUERY = "select org_id from organization_details where org_name=?";

  private static final String CHARGE_QUERY = 
      "select surg_asstance_charge as charge,surgeon_charge,anesthetist_charge,"
      + " surg_asst_discount,surg_discount,anest_discount, "
      + " item_code from operation_charges oc"
      + " join operation_org_details ood on oc.op_id = ood.operation_id "
      + " and oc.org_id = ood.org_id "
      + " where op_id=? and bed_type=? and oc.org_id=? ";

  /**
   * Gets the operation charge.
   *
   * @param operationid the operationid
   * @param bedtype the bedtype
   * @param orgid the orgid
   * @return the operation charge
   */
  public List getOperationCharge(String operationid, String bedtype, String orgid) {
    List cl = null;
    try (Connection con = DataBaseUtil.getConnection(); 
        PreparedStatement ps = con.prepareStatement(CHARGE_QUERY)) {
      String generalOrgId = DataBaseUtil.getStringValueFromDb(RP_QUERY, 
          Constants.getConstantValue("ORG"));
      String generalbedtype = Constants.getConstantValue("BEDTYPE");

      ps.setString(1, operationid);
      ps.setString(2, bedtype);
      ps.setString(3, orgid);

      cl = DataBaseUtil.queryToArrayList(ps);
      logger.debug("{}", cl);
      if (cl.isEmpty()) {
        ps.setString(1, operationid);
        ps.setString(2, generalbedtype);
        ps.setString(3, generalOrgId);
        cl = DataBaseUtil.queryToArrayList(ps);
        logger.debug("{}", cl);
      }

    } catch (Exception exception) {
      logger.error("Exception occured in getOperationCharge", exception);
    }
    return cl;
  }

  /** The op charge query. */
  /*
   * Get the operation details as well as charges associated with it
   */
  private static final String OP_CHARGE_QUERY =
      "SELECT " + "  surg_asstance_charge, surgeon_charge, anesthetist_charge, "
          + "  surg_asst_discount, surg_discount, anest_discount, "
          + "  om.operation_code, ood.item_code, om.op_id, "
          + "  om.operation_name, om.dept_id, om.conduction_applicable,"
          + "  ood.applicable,om.service_sub_group_id, "
          + "  ood.code_type, om.insurance_category_id,allow_rate_increase,"
          + "  allow_rate_decrease,billing_group_id  "
          + " FROM operation_charges oc "
          + "  JOIN operation_master om ON (om.op_id = oc.op_id) "
          + "  JOIN operation_org_details ood "
          + "  ON (oc.op_id = ood.operation_id and oc.org_id = ood.org_id) "
          + " WHERE oc.op_id=? and oc.bed_type=? and oc.org_id=? ";

  /**
   * Gets the surgery charge.
   *
   * @param opId the op id
   * @param bedType the bed type
   * @param orgId the org id
   * @return the surgery charge
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getSurgeryCharge(String opId, String bedType, String orgId)
      throws SQLException {
    return DataBaseUtil.queryToDynaBean(OP_CHARGE_QUERY, new String[] {opId, bedType, orgId});
  }

  /**
   * Gets the operation charge bean.
   *
   * @param opId the op id
   * @param bedType the bed type
   * @param orgId the org id
   * @return the operation charge bean
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getOperationChargeBean(String opId, String bedType, String orgId)
      throws SQLException {
    BasicDynaBean opechargebean = operationMasterDao.getSurgeryCharge(opId, bedType, orgId);
    if (opechargebean == null) {
      opechargebean = operationMasterDao.getSurgeryCharge(opId, "GENERAL", "ORG0001");
    }
    return opechargebean;
  }

  /** The Constant GET_OPERATION_DEPT. */
  private static final String GET_OPERATION_DEPT = " SELECT o.op_id, o.operation_name, o.dept_id, "
      + "  oc.surg_asstance_charge as charge, oc.surgeon_charge, "
      + " oc.anesthetist_charge,ood.item_code, "
      + "  oc.surg_asst_discount, oc.surg_discount, oc.anest_discount ,ood.applicable "
      + " FROM operation_master o JOIN operation_charges oc USING (op_id) "
      + " JOIN operation_org_details ood ON "
      + " (ood.operation_id = o.op_id and ood.org_id = oc.org_id) "
      + " AND  ood.applicable"
      + " WHERE o.status='A' ";

  /** The Constant GET_OPERATION_DEPT_CHARGES. */
  private static final String GET_OPERATION_DEPT_CHARGES = GET_OPERATION_DEPT
      + " AND oc.bed_type=? AND oc.org_id=? ORDER BY o.operation_name";

  /**
  * Gets the operation dept charges.
  *
  * @param bedType the bed type
  * @param orgid the orgid
  * @return the operation dept charges
  * @throws SQLException the SQL exception
  */
  public List getOperationDeptCharges(String bedType, String orgid) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    List list = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_OPERATION_DEPT_CHARGES);
      ps.setString(1, bedType);
      ps.setString(2, orgid);
      // ps.setString(3, bedType);
      // ps.setString(4, orgid);
      list = DataBaseUtil.queryToArrayList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return list;
  }


  /**
   * Gets the operation charges for new operation.
   *
   * @return the operation charges for new operation
   * @throws Exception the exception
   */
  public Map getOperationChargesForNewOperation() throws Exception {

    LinkedHashMap<String, ArrayList<String>> map = new LinkedHashMap<String, ArrayList<String>>();
    ArrayList<String> beds = new ArrayList<String>();
    ArrayList<String> surgeonCharge = new ArrayList<String>();
    ArrayList<String> anesthetist = new ArrayList<String>();
    ArrayList<String> surgicalCharge = new ArrayList<String>();

    ArrayList<Hashtable<String, String>> bedTypes = bddao.getUnionOfAllBedTypes();
    Iterator<Hashtable<String, String>> it = bedTypes.iterator();
    while (it.hasNext()) {
      Hashtable<String, String> ht = it.next();
      beds.add(ht.get("BED_TYPE"));
      surgeonCharge.add(null);
      anesthetist.add(null);
      surgicalCharge.add(null);
    }
    map.put("CHARGES", beds);
    map.put("Surgeon Charge", surgeonCharge);
    map.put("Anesthetist Charge", anesthetist);
    map.put("Surgical Assistance Charge", surgicalCharge);

    return map;
  }



  /** The Constant GET_ALL_CHARGES. */
  private static final String GET_ALL_CHARGES =
      "SELECT OPC.surgeon_charge,OPC.surg_asstance_charge,OPC.anesthetist_charge FROM"
          + " operation_charges OPC WHERE OPC.org_id =? AND OPC.bed_type=? AND OPC.op_id=?";

  /**
   * Gets the operation charges.
   *
   * @param operationId the operation id
   * @param orgId the org id
   * @return the operation charges
   * @throws Exception the exception
   */
  public Map getOperationCharges(String operationId, String orgId) throws Exception {
    ArrayList<String> beds = new ArrayList<String>();
    ArrayList<String> surgeonCharge = new ArrayList<String>();
    ArrayList<String> anesthetist = new ArrayList<String>();
    ArrayList<String> surgicalCharge = new ArrayList<String>();
    ArrayList<Hashtable<String, String>> bedTypes = bddao.getUnionOfAllBedTypes();
    Iterator<Hashtable<String, String>> it = bedTypes.iterator();
    Connection con = DataBaseUtil.getReadOnlyConnection();
    PreparedStatement ps = con.prepareStatement(GET_ALL_CHARGES);

    while (it.hasNext()) {
      Hashtable<String, String> ht = it.next();
      String bedType = ht.get("BED_TYPE");
      beds.add(bedType);
      ps.setString(3, operationId);
      ps.setString(2, bedType);
      ps.setString(1, orgId);

      ArrayList<Hashtable<String, String>> al = DataBaseUtil.queryToArrayList(ps);
      Iterator<Hashtable<String, String>> chargeIt = al.iterator();
      if (chargeIt.hasNext()) {
        Hashtable<String, String> chargeht = chargeIt.next();
        surgeonCharge.add(chargeht.get("SURGEON_CHARGE"));
        anesthetist.add(chargeht.get("ANESTHETIST_CHARGE"));
        surgicalCharge.add(chargeht.get("SURG_ASSTANCE_CHARGE"));
      } else {
        surgeonCharge.add(null);
        anesthetist.add(null);
        surgicalCharge.add(null);
      }
    }

    ps.close();
    con.close();
    LinkedHashMap<String, ArrayList<String>> map = new LinkedHashMap<String, ArrayList<String>>();
    map.put("CHARGES", beds);
    map.put("Surgeon Charge", surgeonCharge);
    map.put("Anesthetist Charge", anesthetist);
    map.put("Surgical Assistance Charge", surgicalCharge);

    return map;
  }



  /**
   * Gets the next operation id.
   *
   * @return the next operation id
   * @throws SQLException the SQL exception
   */
  public String getNextOperationId() throws SQLException {
    String id = null;
    id = AutoIncrementId.getNewIncrUniqueId("OP_ID", "OPERATION_MASTER", "OPERATIONID");
    return id;
  }



  /** The Constant GET_OPERATION_DEF. */
  private static final String GET_OPERATION_DEF =
      "SELECT OP.operation_name,OP.operation_code,OP.status,OP.op_id,DEPT.dept_name,"
          + "DEPT.dept_id FROM operation_master OP "
          + "JOIN department DEPT ON OP.dept_id = DEPT.dept_id WHERE OP.op_id=? ";

  /**
   * Gets the opreation def.
   *
   * @param operationId the operation id
   * @return the opreation def
   * @throws SQLException the SQL exception
   */
  public ArrayList<Hashtable<String, String>> getOpreationDef(String operationId)
      throws SQLException {
    ArrayList<Hashtable<String, String>> al = null;
    Connection con = DataBaseUtil.getReadOnlyConnection();
    PreparedStatement ps = con.prepareStatement(GET_OPERATION_DEF);
    ps.setString(1, operationId);
    al = DataBaseUtil.queryToArrayList(ps);
    ps.close();
    con.close();


    return al;
  }



  /** The Constant ADD_NEW_OPERATION. */
  private static final String ADD_NEW_OPERATION =
      "INSERT INTO operation_master(op_id,operation_name,dept_id,operation_code,status)"
          + "VALUES(?,?,?,?,?)";
  
  /** The Constant UPDATE_OPERATION. */
  private static final String UPDATE_OPERATION =
      "UPDATE operation_master SET operation_name=?,operation_code=?,"
      + "status=?,dept_id=? WHERE op_id=? ";

  /** The Constant CHECK_FOR_EXISTENCE. */
  private static final String CHECK_FOR_EXISTENCE =
      "SELECT count(*) FROM operation_master WHERE op_id=?";

  /**
   * Adds the or update def.
   *
   * @param con the con
   * @param op the op
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean addOrUpdateDef(Connection con, Operation op) throws SQLException {
    boolean status = false;
    PreparedStatement ps = con.prepareStatement(ADD_NEW_OPERATION);
    PreparedStatement ps1 = con.prepareStatement(CHECK_FOR_EXISTENCE);
    PreparedStatement ups = con.prepareStatement(UPDATE_OPERATION);
    ps1.setString(1, op.getOperationId());
    String count = DataBaseUtil.getStringValueFromDb(ps1);


    if (count.equals("0")) {
      ps.setString(1, op.getOperationId());
      ps.setString(2, op.getOperationName());
      ps.setString(3, op.getDeptId());
      ps.setString(4, op.getOperationCode());
      ps.setString(5, op.getStatus());

      int count1 = ps.executeUpdate();
      if (count1 > 0) {
        status = true;
      }
    } else {
      ups.setString(1, op.getOperationName());
      ups.setString(2, op.getOperationCode());
      ups.setString(3, op.getStatus());
      ups.setString(4, op.getDeptId());
      ups.setString(5, op.getOperationId());

      int count2 = ups.executeUpdate();
      if (count2 > 0) {
        status = true;
      }
    }
    ps.close();
    ps1.close();
    ups.close();

    return status;
  }


  /** The Constant ADD_OPERATION_CHARGES. */
  private static final String ADD_OPERATION_CHARGES =
      "INSERT INTO operation_charges(op_id,org_id,bed_type,"
          + "surg_asstance_charge,surgeon_charge,anesthetist_charge)" + " VALUES(?,?,?," + "?,?,?)";

  /** The Constant UPDATE_OPERATION_CHARGES. */
  private static final String UPDATE_OPERATION_CHARGES =
      "UPDATE operation_charges SET surg_asstance_charge=?,surgeon_charge=?,anesthetist_charge=?"
          + " WHERE op_id=? AND org_id=? AND bed_type=?";

  /** The Constant CHECK_FOR_EXISTENCEOF_CHARGES. */
  private static final String CHECK_FOR_EXISTENCEOF_CHARGES =
      "SELECT COUNT(*) FROM operation_charges WHERE op_id=? AND org_id=? AND bed_type=?";

  /**
   * Adds the or update charges.
   *
   * @param con the con
   * @param opChargeList the op charge list
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean addOrUpdateCharges(Connection con, ArrayList<OperationCharges> opChargeList)
      throws SQLException {
    boolean status = false;
    PreparedStatement ps = con.prepareStatement(ADD_OPERATION_CHARGES);
    PreparedStatement ups = con.prepareStatement(UPDATE_OPERATION_CHARGES);
    PreparedStatement rps = con.prepareStatement(CHECK_FOR_EXISTENCEOF_CHARGES);

    Iterator<OperationCharges> it = opChargeList.iterator();
    while (it.hasNext()) {
      OperationCharges op = it.next();
      rps.setString(1, op.getOperationId());
      rps.setString(2, op.getOrgId());
      rps.setString(3, op.getBedType());
      String count = DataBaseUtil.getStringValueFromDb(rps);
      if (count.equals("0")) {
        ps.setString(1, op.getOperationId());
        ps.setString(2, op.getOrgId());
        ps.setString(3, op.getBedType());
        ps.setDouble(4, op.getSurgicaAsstCharge());
        ps.setDouble(5, op.getSurgeonCharge());
        ps.setDouble(6, op.getAnesthetistCharge());
        ps.addBatch();

      } else {
        ups.setDouble(1, op.getSurgicaAsstCharge());
        ups.setDouble(2, op.getSurgeonCharge());
        ups.setDouble(3, op.getAnesthetistCharge());
        ups.setString(4, op.getOperationId());
        ups.setString(5, op.getOrgId());
        ups.setString(6, op.getBedType());
        ups.addBatch();

      }
    }


    do {
      int[] count1 = ps.executeBatch();
      status = DataBaseUtil.checkBatchUpdates(count1);
      if (!status) {
        break;
      }

      int[] count2 = ups.executeBatch();
      status = DataBaseUtil.checkBatchUpdates(count2);


    } while (false);



    ps.close();
    ups.close();
    rps.close();
    return status;
  }


  /** The Constant OPERATION_FIELDS. */
  private static final String OPERATION_FIELDS =
      "SELECT OP.operation_name,OP.op_id,OP.operation_code,op.status,ood.item_code,ood.applicable ";
  
  /** The Constant OPERATION_COUNT. */
  private static final String OPERATION_COUNT = "SELECT count(*) ";
  
  /** The Constant OPERATION_FROM_TABLES. */
  private static final String OPERATION_FROM_TABLES =
      "FROM operation_master OP JOIN operation_org_details ood "
          + "ON (ood.operation_id = OP.op_id ) ";

  /** The Constant GET_SURGEON_CHARGE. */
  private static final String GET_SURGEON_CHARGE =
      "SELECT OPC.surgeon_charge,OPC.bed_type FROM operation_charges OPC "
          + " WHERE opc.bed_type=? AND opc.org_id=? AND opc.op_id=?";
  
  /** The Constant GET_ANESTHETIST_CHARGE. */
  private static final String GET_ANESTHETIST_CHARGE =
      "SELECT OPC.anesthetist_charge,OPC.bed_type FROM operation_charges OPC"
          + " WHERE opc.bed_type=? AND opc.org_id=? AND opc.op_id=?";

  /** The Constant GET_SURG_ASSTANCE_CHARGE. */
  private static final String GET_SURG_ASSTANCE_CHARGE =
      "SELECT OPC.surg_asstance_charge,OPC.bed_type FROM operation_charges OPC"
          + " WHERE opc.bed_type=? AND opc.org_id=? AND opc.op_id=?";


  /**
   * Gets the operation details.
   *
   * @param statusList the status list
   * @param chargeType the charge type
   * @param orgId the org id
   * @param deptList the dept list
   * @param pageNum the page num
   * @param operationNameFilter the operation name filter
   * @param applicable the applicable
   * @return the operation details
   * @throws Exception the exception
   */
  public PagedList getOperationDetails(ArrayList<String> statusList, String chargeType,
      String orgId, ArrayList<String> deptList, int pageNum, String operationNameFilter,
      String applicable) throws Exception {

    LinkedHashMap<String, ArrayList> map = new LinkedHashMap<String, ArrayList>();
    map.put("surgeon_charge", null);
    map.put("anesthetist_charge", null);
    map.put("surg_asstance_charge", null);

    ArrayList<Map> al = new ArrayList<Map>();
    Connection con = DataBaseUtil.getReadOnlyConnection();
    int count = 0;

    if (chargeType.equals("SC")) {

      SearchQueryBuilder qb = new SearchQueryBuilder(con, OPERATION_FIELDS, OPERATION_COUNT,
          OPERATION_FROM_TABLES, null, null, "OP.operation_name", false, 25, pageNum);
      qb.addFilter(SearchQueryBuilder.STRING, "op.dept_id", "IN", deptList);
      qb.addFilter(SearchQueryBuilder.STRING, "op.status", "IN", statusList);
      qb.addFilter(SearchQueryBuilder.STRING, "OP.operation_name", "ilike", operationNameFilter);
      qb.addFilter(SearchQueryBuilder.STRING, "ood.org_id", "=", orgId);
      if (applicable != null) {
        if (applicable.equals("Y")) {
          qb.addFilter(SearchQueryBuilder.STRING, "applicable", "is", true);
        } else {
          qb.addFilter(SearchQueryBuilder.STRING, "applicable", "is", false);
        }
      }
      qb.build();
      PreparedStatement psData = qb.getDataStatement();
      PreparedStatement psCount = qb.getCountStatement();
      count = Integer.parseInt((DataBaseUtil.getStringValueFromDb(psCount)));
      psData.close();
      psCount.close();
      PreparedStatement ps = null;
      ps = con.prepareStatement(GET_SURGEON_CHARGE);

      ArrayList<String> headers = new ArrayList<String>();
      headers.add("Select");
      headers.add("Operation Name");
      headers.add("Organization Code");
      ArrayList<String> bedTypes = BedMasterDAO.getUnionOfBedTypes();
      headers.addAll(bedTypes);
      ArrayList<List> allOperations = new ArrayList<List>();
      allOperations.add(headers);

      ArrayList<Hashtable<String, String>> operations = DataBaseUtil.queryToArrayList(psData);
      Iterator<Hashtable<String, String>> it = operations.iterator();
      while (it.hasNext()) {
        Hashtable<String, String> ht = it.next();
        String operationId = ht.get("OP_ID");
        String operationName = ht.get("OPERATION_NAME");
        String operationStatus = ht.get("STATUS");
        ArrayList<String> operationRecord = new ArrayList<String>();
        operationRecord.add(operationStatus);
        operationRecord.add(operationId);
        operationRecord.add(operationName);


        /*
         * psItem = con.prepareStatement(
         * "select item_code from operation_org_details  where org_id=? and operation_id = ?");
         * psItem.setString(1, orgId); psItem.setString(2, operationId);
         * operationRecord.add(DataBaseUtil.getStringValueFromDB(psItem));
         */

        String itemCode = ht.get("ITEM_CODE");
        operationRecord.add(itemCode);
        operationRecord.add(ht.get("APPLICABLE").equals("t") ? "Y" : "N");
        Iterator<String> beds = bedTypes.iterator();
        while (beds.hasNext()) {
          String bed = beds.next();
          ps.setString(1, bed);
          ps.setString(2, orgId);
          ps.setString(3, operationId);
          operationRecord.add(DataBaseUtil.getStringValueFromDb(ps));
        }
        allOperations.add(operationRecord);
      }
      logger.debug("{}", allOperations);
      if (ps != null) {
        ps.close();
      }
      map.put("surgeon_charge", allOperations);

    } else if (chargeType.equals("AC")) {
      SearchQueryBuilder qb = new SearchQueryBuilder(con, OPERATION_FIELDS, OPERATION_COUNT,
          OPERATION_FROM_TABLES, null, null, "OP.operation_name", false, 25, pageNum);
      qb.addFilter(SearchQueryBuilder.STRING, "op.dept_id", "IN", deptList);
      qb.addFilter(SearchQueryBuilder.STRING, "op.status", "IN", statusList);
      qb.addFilter(SearchQueryBuilder.STRING, "OP.operation_name", "ilike", operationNameFilter);
      qb.addFilter(SearchQueryBuilder.STRING, "ood.org_id", "=", orgId);
      if (applicable != null) {
        if (applicable.equals("Y")) {
          qb.addFilter(SearchQueryBuilder.STRING, "applicable", "is", true);
        } else {
          qb.addFilter(SearchQueryBuilder.STRING, "applicable", "is", false);
        }
      }

      qb.build();
      PreparedStatement psData = qb.getDataStatement();
      PreparedStatement psCount = qb.getCountStatement();
      count = Integer.parseInt((DataBaseUtil.getStringValueFromDb(psCount)));
      psData.close();
      psCount.close();
      PreparedStatement ps = null;
      ps = con.prepareStatement(GET_ANESTHETIST_CHARGE);
      ArrayList<String> headers = new ArrayList<String>();
      headers.add("Select");
      headers.add("Operation Name");
      headers.add("Organization Code");
      ArrayList<String> bedTypes = BedMasterDAO.getUnionOfBedTypes();
      headers.addAll(bedTypes);
      ArrayList<List> allOperations = new ArrayList<List>();
      allOperations.add(headers);

      ArrayList<Hashtable<String, String>> operations = DataBaseUtil.queryToArrayList(psData);
      Iterator<Hashtable<String, String>> it = operations.iterator();
      while (it.hasNext()) {
        Hashtable<String, String> ht = it.next();
        String operationId = ht.get("OP_ID");
        String operationName = ht.get("OPERATION_NAME");
        String operationStatus = ht.get("STATUS");
        ArrayList<String> operationRecord = new ArrayList<String>();
        operationRecord.add(operationStatus);
        operationRecord.add(operationId);
        operationRecord.add(operationName);

        String itemCode = ht.get("ITEM_CODE");
        operationRecord.add(itemCode);
        operationRecord.add(ht.get("APPLICABLE").equals("t") ? "Y" : "N");

        Iterator<String> beds = bedTypes.iterator();
        while (beds.hasNext()) {
          String bed = beds.next();
          ps.setString(1, bed);
          ps.setString(2, orgId);
          ps.setString(3, operationId);
          operationRecord.add(DataBaseUtil.getStringValueFromDb(ps));
        }
        allOperations.add(operationRecord);
      }
      logger.debug("{}", allOperations);
      if (ps != null) {
        ps.close();
      }
      map.put("anesthetist_charge", allOperations);

    } else if (chargeType.equals("SAC")) {
      PreparedStatement ps = null;
      SearchQueryBuilder qb = new SearchQueryBuilder(con, OPERATION_FIELDS, OPERATION_COUNT,
          OPERATION_FROM_TABLES, null, null, "OP.operation_name", false, 25, pageNum);
      qb.addFilter(SearchQueryBuilder.STRING, "op.dept_id", "IN", deptList);
      qb.addFilter(SearchQueryBuilder.STRING, "op.status", "IN", statusList);
      qb.addFilter(SearchQueryBuilder.STRING, "OP.operation_name", "ilike", operationNameFilter);
      qb.addFilter(SearchQueryBuilder.STRING, "ood.org_id", "=", orgId);
      if (applicable != null) {
        if (applicable.equals("Y")) {
          qb.addFilter(SearchQueryBuilder.STRING, "applicable", "is", true);
        } else {
          qb.addFilter(SearchQueryBuilder.STRING, "applicable", "is", false);
        }
      }
      qb.build();
      PreparedStatement psData = qb.getDataStatement();
      PreparedStatement psCount = qb.getCountStatement();
      count = Integer.parseInt((DataBaseUtil.getStringValueFromDb(psCount)));
      psData.close();
      psCount.close();
      
      ArrayList<String> headers = new ArrayList<String>();
      headers.add("Select");
      headers.add("Operation Name");
      headers.add("Organization Code");
      ArrayList<String> bedTypes = BedMasterDAO.getUnionOfBedTypes();
      headers.addAll(bedTypes);
      ArrayList<List> allOperations = new ArrayList<List>();
      allOperations.add(headers);

      ArrayList<Hashtable<String, String>> operations = DataBaseUtil.queryToArrayList(psData);
      Iterator<Hashtable<String, String>> it = operations.iterator();
      while (it.hasNext()) {
        Hashtable<String, String> ht = it.next();
        String operationId = ht.get("OP_ID");
        String operationName = ht.get("OPERATION_NAME");
        String operationStatus = ht.get("STATUS");
        ArrayList<String> operationRecord = new ArrayList<String>();
        operationRecord.add(operationStatus);
        operationRecord.add(operationId);
        operationRecord.add(operationName);

        String itemCode = ht.get("ITEM_CODE");
        operationRecord.add(itemCode);
        operationRecord.add(ht.get("APPLICABLE").equals("t") ? "Y" : "N");

        Iterator<String> beds = bedTypes.iterator();
        ps = con.prepareStatement(GET_SURG_ASSTANCE_CHARGE);
        while (beds.hasNext()) {
          String bed = beds.next();
          ps.setString(1, bed);
          ps.setString(2, orgId);
          ps.setString(3, operationId);
          operationRecord.add(DataBaseUtil.getStringValueFromDb(ps));
        }
        allOperations.add(operationRecord);
      }
      logger.debug("{}", allOperations);
      if (ps != null) {
        ps.close();
      }
      map.put("surg_asstance_charge", allOperations);
    }

    con.close();
    al.add(map);
    return new PagedList(al, count, 25, pageNum);
  }


  /** The Constant GROUP_UPDATE_FOR_SURGEONCHARGE. */
  private static final String GROUP_UPDATE_FOR_SURGEONCHARGE =
      "UPDATE operation_charges SET surgeon_charge=round(?) WHERE "
          + " op_id=? AND org_id=? AND bed_type=?";
  
  /** The Constant GET_SURGEON_CHARGE_FOR_GROUP. */
  private static final String GET_SURGEON_CHARGE_FOR_GROUP =
      "SELECT coalesce(OPC.surgeon_charge,0) as surgeon_charge ,OPC.bed_type "
      + " FROM operation_charges OPC "
      + " WHERE opc.bed_type=? AND opc.org_id=? AND opc.op_id=?";


  /**
   * Group update for surgeon charges.
   *
   * @param con the con
   * @param opclist the opclist
   * @param varinaceVal the varinace val
   * @param variancePer the variance per
   * @param varianceType the variance type
   * @param useValue the use value
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean groupUpdateForSurgeonCharges(Connection con, ArrayList<OperationCharges> opclist,
      Double varinaceVal, Double variancePer, String varianceType, boolean useValue)
      throws SQLException {
    boolean status = false;
    Iterator<OperationCharges> it = opclist.iterator();
    PreparedStatement ps = con.prepareStatement(GROUP_UPDATE_FOR_SURGEONCHARGE);
    PreparedStatement rps = con.prepareStatement(GET_SURGEON_CHARGE_FOR_GROUP);

    while (it.hasNext()) {
      OperationCharges oc = it.next();
      rps.setString(1, oc.getBedType());
      rps.setString(2, oc.getOrgId());
      rps.setString(3, oc.getOperationId());
      String chargeStr = DataBaseUtil.getStringValueFromDb(rps);
      double charge = 0.0;
      if (chargeStr == null) {
        chargeStr = "0";
      }
      charge = new Double(chargeStr).doubleValue();
      if (useValue) {
        if (varianceType.equals("Incr")) {
          charge += varinaceVal.doubleValue();
        } else {
          charge -= varinaceVal.doubleValue();
        }

      } else {
        if (varianceType.equals("Incr")) {
          charge += charge * (variancePer.doubleValue() / 100);
        } else {
          charge -= charge * (variancePer.doubleValue() / 100);
        }
      }

      if (charge < 0) {
        charge = 0;
      }

      ps.setDouble(1, charge);
      ps.setString(2, oc.getOperationId());
      ps.setString(3, oc.getOrgId());
      ps.setString(4, oc.getBedType());

      ps.addBatch();

    }


    int[] count = ps.executeBatch();
    status = DataBaseUtil.checkBatchUpdates(count);


    if (ps != null) {
      ps.close();
    }
    rps.close();

    return status;
  }

  /** The Constant GROUP_UPDATE_FOR_ANESTHETISTCHARGE. */
  private static final String GROUP_UPDATE_FOR_ANESTHETISTCHARGE =
      "UPDATE operation_charges SET anesthetist_charge=round(?) WHERE "
          + " op_id=? AND org_id=? AND bed_type=?";
  
  /** The Constant GET_ANESTHEST_CHARGE_FOR_GROUP. */
  private static final String GET_ANESTHEST_CHARGE_FOR_GROUP =
      "SELECT coalesce(OPC.anesthetist_charge,0) as surgeon_charge ,OPC.bed_type "
      + " FROM operation_charges OPC "
      + " WHERE opc.bed_type=? AND opc.org_id=? AND opc.op_id=?";


  /**
   * Group update for anesthetist charges.
   *
   * @param con the con
   * @param opclist the opclist
   * @param varinaceVal the varinace val
   * @param variancePer the variance per
   * @param varianceType the variance type
   * @param useValue the use value
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean groupUpdateForAnesthetistCharges(Connection con,
      ArrayList<OperationCharges> opclist, Double varinaceVal, Double variancePer,
      String varianceType, boolean useValue) throws SQLException {
    boolean status = false;
    Iterator<OperationCharges> it = opclist.iterator();
    PreparedStatement ps = con.prepareStatement(GROUP_UPDATE_FOR_ANESTHETISTCHARGE);
    PreparedStatement rps = con.prepareStatement(GET_ANESTHEST_CHARGE_FOR_GROUP);

    while (it.hasNext()) {
      OperationCharges oc = it.next();
      rps.setString(1, oc.getBedType());
      rps.setString(2, oc.getOrgId());
      rps.setString(3, oc.getOperationId());
      String chargeStr = DataBaseUtil.getStringValueFromDb(rps);
      double charge = 0.0;
      if (chargeStr == null) {
        chargeStr = "0";
      }
      charge = new Double(chargeStr).doubleValue();
      if (useValue) {
        if (varianceType.equals("Incr")) {
          charge += varinaceVal.doubleValue();

        } else {
          charge -= varinaceVal.doubleValue();
        }

      } else {
        if (varianceType.equals("Incr")) {
          charge += charge * (variancePer.doubleValue() / 100);
        } else {
          charge -= charge * (variancePer.doubleValue() / 100);
        }
      }

      if (charge < 0) {
        charge = 0;
      }


      ps.setDouble(1, charge);
      ps.setString(2, oc.getOperationId());
      ps.setString(3, oc.getOrgId());
      ps.setString(4, oc.getBedType());

      ps.addBatch();

    }


    int[] count = ps.executeBatch();
    status = DataBaseUtil.checkBatchUpdates(count);


    if (ps != null) {
      ps.close();
    }
    rps.close();

    return status;
  }

  /** The Constant GROUP_UPDATE_FOR_SURGEONASSTCHARGE. */
  private static final String GROUP_UPDATE_FOR_SURGEONASSTCHARGE =
      "UPDATE operation_charges SET anesthetist_charge=round(?) WHERE "
          + " op_id=? AND org_id=? AND bed_type=?";
  
  /** The Constant GET_SURGEONASST_CHARGE_FOR_GROUP. */
  private static final String GET_SURGEONASST_CHARGE_FOR_GROUP =
      "SELECT coalesce(OPC.anesthetist_charge,0) as surgeon_charge ,OPC.bed_type "
      + " FROM operation_charges OPC "
      + " WHERE opc.bed_type=? AND opc.org_id=? AND opc.op_id=?";


  /**
   * Group update for surgeon ass charges.
   *
   * @param con the con
   * @param opclist the opclist
   * @param varinaceVal the varinace val
   * @param variancePer the variance per
   * @param varianceType the variance type
   * @param useValue the use value
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean groupUpdateForSurgeonAssCharges(Connection con,
      ArrayList<OperationCharges> opclist, Double varinaceVal, Double variancePer,
      String varianceType, boolean useValue) throws SQLException {
    boolean status = false;
    Iterator<OperationCharges> it = opclist.iterator();
    PreparedStatement ps = con.prepareStatement(GROUP_UPDATE_FOR_SURGEONASSTCHARGE);
    PreparedStatement rps = con.prepareStatement(GET_SURGEONASST_CHARGE_FOR_GROUP);

    while (it.hasNext()) {
      OperationCharges oc = it.next();
      rps.setString(1, oc.getBedType());
      rps.setString(2, oc.getOrgId());
      rps.setString(3, oc.getOperationId());
      String chargeStr = DataBaseUtil.getStringValueFromDb(rps);
      double charge = 0.0;
      if (chargeStr == null) {
        chargeStr = "0";
      }
      charge = new Double(chargeStr).doubleValue();
      if (useValue) {
        if (varianceType.equals("Incr")) {
          charge += varinaceVal.doubleValue();

        } else {
          charge -= varinaceVal.doubleValue();
        }

      } else {
        if (varianceType.equals("Incr")) {
          charge += charge * (variancePer.doubleValue() / 100);
        } else {
          charge -= charge * (variancePer.doubleValue() / 100);
        }
      }

      if (charge < 0) {
        charge = 0;
      }


      ps.setDouble(1, charge);
      ps.setString(2, oc.getOperationId());
      ps.setString(3, oc.getOrgId());
      ps.setString(4, oc.getBedType());

      ps.addBatch();

    }


    int[] count = ps.executeBatch();
    status = DataBaseUtil.checkBatchUpdates(count);


    if (ps != null) {
      ps.close();
    }
    rps.close();

    return status;
  }


  /** The Constant OPERATIONSDETAILS. */
  public static final String OPERATIONSDETAILS =
      "SELECT DISTINCT OPM.OP_ID,OPM.OPERATION_NAME,OP.SURG_ASSTANCE_CHARGE,"
          + "DEP.DEPT_NAME,OPM.OPERATION_CODE,OP.SURGEON_CHARGE,OP.ANESTHETIST_CHARGE "
          + "FROM OPERATION_MASTER OPM,DEPARTMENT DEP, "
          + "OPERATION_CHARGES OP WHERE DEP.DEPT_ID=OPM.DEPT_ID "
          + "AND OPM.STATUS='A' AND OPM.OP_ID = OP.OP_ID ";

  /**
   * Method to get all details of operation including charges.
   *
   * @return an arraylist of operation names,ids,charges
   * @throws SQLException the SQL exception
   */
  public List operationChargeDetails() throws SQLException {
    Connection con = DataBaseUtil.getReadOnlyConnection();
    PreparedStatement ps = con.prepareStatement(OPERATIONSDETAILS);
    List ol = DataBaseUtil.queryToArrayList(ps);
    ps.close();
    con.close();
    return ol;
  }

  /** The Constant OPERATIONSLIST. */
  public static final String OPERATIONSLIST =
      " SELECT distinct (opm.operation_name||'-'||dep.dept_name) as operation, "
      + " opm.op_id, opm.operation_name, "
      + " dep.dept_name, opm.operation_code, opm.dept_id " + " FROM operation_master opm "
      + " JOIN department dep ON (dep.dept_id=opm.dept_id) " + " WHERE opm.status='A'";

  /**
   * Gets the operations list.
   *
   * @return the operations list
   * @throws SQLException the SQL exception
   */
  public static List getOperationsList() throws SQLException {
    Connection con = DataBaseUtil.getReadOnlyConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(OPERATIONSLIST);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }


  /** The Constant CHECK_FOR_DUPLICATE. */
  private static final String CHECK_FOR_DUPLICATE =
      "SELECT count(*) FROM OPERATION_MASTER WHERE operation_name=?";

  /**
   * Check duplicate.
   *
   * @param newOperatioName the new operatio name
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public static boolean checkDuplicate(String newOperatioName) throws SQLException {
    boolean status = true;
    Connection con = DataBaseUtil.getReadOnlyConnection();
    PreparedStatement ps = con.prepareStatement(CHECK_FOR_DUPLICATE);
    ps.setString(1, newOperatioName);

    String count = DataBaseUtil.getStringValueFromDb(ps);
    if (count.equals("0")) {
      status = false;
    }
    ps.close();
    con.close();

    return status;
  }


  /** The Constant GET_ALL_OPERATION_NAMES. */
  private static final String GET_ALL_OPERATION_NAMES =
      " SELECT distinct operation_name,op_id FROM operation_master ORDER BY operation_name ";

  /**
   * Gets the all operation names.
   *
   * @return the all operation names
   * @throws SQLException the SQL exception
   */
  public ArrayList getAllOperationNames() throws SQLException {
    ArrayList list = new ArrayList();
    Connection con = null;
    con = DataBaseUtil.getReadOnlyConnection();
    PreparedStatement ps = con.prepareStatement(GET_ALL_OPERATION_NAMES);
    list = DataBaseUtil.queryToArrayList(ps);
    ps.close();
    con.close();
    return list;
  }


  /** The Constant GET_ALL_OPERATIONS_COUNT. */
  private static final String GET_ALL_OPERATIONS_COUNT = "SELECT count(*) FROM operation_master";

  /**
   * Gets the all operations count.
   *
   * @return the all operations count
   * @throws SQLException the SQL exception
   */
  public static int getAllOperationsCount() throws SQLException {
    int count = 0;
    Connection con = DataBaseUtil.getReadOnlyConnection();
    PreparedStatement ps = con.prepareStatement(GET_ALL_OPERATIONS_COUNT);
    String countStr = DataBaseUtil.getStringValueFromDb(ps);
    if (countStr != null && !countStr.equals("")) {
      count = Integer.parseInt(countStr);
    }
    DataBaseUtil.closeConnections(con, ps);
    return count;
  }



  /** The Constant UPDATE_ALL_SURGEON_CHARGES_PLUS. */
  private static final String UPDATE_ALL_SURGEON_CHARGES_PLUS =
      "UPDATE operation_charges SET surgeon_charge = ROUND(surgeon_charge + ?)"
          + " WHERE bed_type=? AND org_id=?";

  /** The Constant UPDATE_ALL_SURGEON_CHARGES_MINUS. */
  private static final String UPDATE_ALL_SURGEON_CHARGES_MINUS =
      "UPDATE operation_charges SET surgeon_charge = ROUND(surgeon_charge - ?)"
          + " WHERE bed_type=? AND org_id=?";
  
  /** The Constant UPDATE_ALL_SURGEON_CHARGES_BY. */
  private static final String UPDATE_ALL_SURGEON_CHARGES_BY =
      "UPDATE operation_charges SET surgeon_charge = doroundvarying(surgeon_charge,?,? )"
          + "  WHERE bed_type=? AND org_id=?";

  /** The Constant UPDATE_ALL_AASNT_CHARGES_PLUS. */
  private static final String UPDATE_ALL_AASNT_CHARGES_PLUS =
      "UPDATE operation_charges SET surg_asstance_charge = ROUND(surg_asstance_charge + ?)"
          + " WHERE bed_type=? AND org_id=?";

  /** The Constant UPDATE_ALL_AASNT_CHARGES_MINUS. */
  private static final String UPDATE_ALL_AASNT_CHARGES_MINUS =
      "UPDATE operation_charges SET surg_asstance_charge = ROUND(surg_asstance_charge - ?)"
          + " WHERE bed_type=? AND org_id=?";
  
  /** The Constant UPDATE_ALL_AASNT_CHARGES_BY. */
  private static final String UPDATE_ALL_AASNT_CHARGES_BY =
      "UPDATE operation_charges SET surg_asstance_charge = "
      + " doroundvarying(surg_asstance_charge,?,? )"
      + " WHERE bed_type=? AND org_id=?";

  /** The Constant UPDATE_ALL_ANESTHETIST_CHARGES_PLUS. */
  private static final String UPDATE_ALL_ANESTHETIST_CHARGES_PLUS =
      "UPDATE operation_charges SET anesthetist_charge = ROUND(anesthetist_charge + ?)"
          + " WHERE bed_type=? AND org_id=?";

  /** The Constant UPDATE_ALL_ANESTHETIST_CHARGES_MINUS. */
  private static final String UPDATE_ALL_ANESTHETIST_CHARGES_MINUS =
      "UPDATE operation_charges SET anesthetist_charge = ROUND(anesthetist_charge - ?)"
          + " WHERE bed_type=? AND org_id=?";
  
  /** The Constant UPDATE_ALL_ANESTHETIST_CHARGES_BY. */
  private static final String UPDATE_ALL_ANESTHETIST_CHARGES_BY =
      "UPDATE operation_charges SET anesthetist_charge = doroundvarying(anesthetist_charge,?,? )"
          + " WHERE bed_type=? AND org_id=?";



  /**
   * Update all operations.
   *
   * @param con the con
   * @param varinaceVal the varinace val
   * @param variancePer the variance per
   * @param varianceType the variance type
   * @param useValue the use value
   * @param groupUpdatComponent the group updat component
   * @param groupBeds the group beds
   * @param orgId the org id
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean updateAllOperations(Connection con, Double varinaceVal, Double variancePer,
      String varianceType, boolean useValue, String groupUpdatComponent, String[] groupBeds,
      String orgId) throws SQLException {

    boolean status = false;
    PreparedStatement ps = null;
    if (useValue) {
      if (varianceType.equals("Incr")) {
        if (groupUpdatComponent.equals("SC")) {
          ps = con.prepareStatement(UPDATE_ALL_SURGEON_CHARGES_PLUS);
        }
        if (groupUpdatComponent.equals("AC")) {
          ps = con.prepareStatement(UPDATE_ALL_AASNT_CHARGES_PLUS);
        }
        if (groupUpdatComponent.equals("SAC")) {
          ps = con.prepareStatement(UPDATE_ALL_ANESTHETIST_CHARGES_PLUS);
        }

      } else {
        if (groupUpdatComponent.equals("SC")) {
          ps = con.prepareStatement(UPDATE_ALL_SURGEON_CHARGES_MINUS);
        }
        if (groupUpdatComponent.equals("AC")) {
          ps = con.prepareStatement(UPDATE_ALL_AASNT_CHARGES_MINUS);
        }
        if (groupUpdatComponent.equals("SAC")) {
          ps = con.prepareStatement(UPDATE_ALL_ANESTHETIST_CHARGES_MINUS);
        }

      }

      if (groupBeds != null) {
        for (int i = 0; i < groupBeds.length; i++) {
          ps.setDouble(1, varinaceVal);
          ps.setString(2, groupBeds[i]);
          ps.setString(3, orgId);

          ps.addBatch();
        }
      }


    } else {
      if (groupUpdatComponent.equals("SC")) {
        ps = con.prepareStatement(UPDATE_ALL_SURGEON_CHARGES_BY);
      }
      if (groupUpdatComponent.equals("AC")) {
        ps = con.prepareStatement(UPDATE_ALL_AASNT_CHARGES_BY);
      }
      if (groupUpdatComponent.equals("SAC")) {
        ps = con.prepareStatement(UPDATE_ALL_ANESTHETIST_CHARGES_BY);
      }

      variancePer = new Double(-variancePer);
      for (int i = 0; i < groupBeds.length; i++) {
        ps.setBigDecimal(1, new BigDecimal(variancePer));
        ps.setBigDecimal(2, new BigDecimal(10));
        ps.setString(3, groupBeds[i]);
        ps.setString(4, orgId);

        ps.addBatch();
      }
    }

    int[] count = ps.executeBatch();
    status = DataBaseUtil.checkBatchUpdates(count);

    if (ps != null) {
      ps.close();
    }

    return status;
  }



  /** The Constant GET_ORG_ITEM_COUNT. */
  public static final String GET_ORG_ITEM_COUNT =
      "SELECT count(*)  FROM operation_org_details WHERE operation_id = ? AND org_id = ?";
  
  /** The Constant INSERT_OPERATION_ITEM_CODE. */
  public static final String INSERT_OPERATION_ITEM_CODE =
      "INSERT INTO operation_org_details(operation_id, org_id, "
          + "item_code, applicable) values (?, ?, ?, ?)";
  
  /** The Constant UPDATE_OPERATION_ITEM_CODE. */
  public static final String UPDATE_OPERATION_ITEM_CODE =
      "UPDATE  operation_org_details SET item_code = ?, applicable = ? "
          + "WHERE operation_id = ? AND org_id = ?";

  /**
   * Adds the OR edit item code.
   *
   * @param con the con
   * @param itemCodes the item codes
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean addOREditItemCode(Connection con, ArrayList<OperationCharges> itemCodes)
      throws SQLException {

    boolean status = false;
    PreparedStatement countPs = con.prepareStatement(GET_ORG_ITEM_COUNT);
    PreparedStatement insertPs = con.prepareStatement(INSERT_OPERATION_ITEM_CODE);
    PreparedStatement updatePs = con.prepareStatement(UPDATE_OPERATION_ITEM_CODE);
    try {

      for (OperationCharges itemCode : itemCodes) {

        countPs.setString(1, itemCode.getOperationId());
        countPs.setString(2, itemCode.getOrgId());
        String itCount = DataBaseUtil.getStringValueFromDb(countPs);

        if (itCount.equals("0")) {
          insertPs.setString(1, itemCode.getOperationId());
          insertPs.setString(2, itemCode.getOrgId());
          insertPs.setString(3, itemCode.getOrgItemCode());
          insertPs.setBoolean(4, itemCode.getApplicable());
          insertPs.addBatch();
        } else {
          updatePs.setString(1, itemCode.getOrgItemCode());
          updatePs.setBoolean(2, itemCode.getApplicable());
          updatePs.setString(3, itemCode.getOperationId());
          updatePs.setString(4, itemCode.getOrgId());
          updatePs.addBatch();
        }
      }

      do {
        int[] countA = insertPs.executeBatch();
        status = DataBaseUtil.checkBatchUpdates(countA);
        if (!status) {
          break;
        }

        int[] countB = updatePs.executeBatch();
        status = DataBaseUtil.checkBatchUpdates(countB);

      } while (false);


    } finally {
      if (countPs != null) {
        countPs.close();
      }
      if (insertPs != null) {
        insertPs.close();
      }
      if (updatePs != null) {
        updatePs.close();
      }
    }
    return status;
  }

  /** The Constant GET_ORG_ITEM_CODE. */
  private static final String GET_ORG_ITEM_CODE =
      "SELECT * FROM operation_org_details where operation_id = ? AND org_id = ?";

  /**
   * Gets the org item code.
   *
   * @param orgId the org id
   * @param operationId the operation id
   * @return the org item code
   * @throws SQLException the SQL exception
   */
  public static List getOrgItemCode(String orgId, String operationId) throws SQLException {
    int count = 0;
    Connection con = null;
    PreparedStatement ps = null;
    List list = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_ORG_ITEM_CODE);
      ps.setString(1, operationId);
      ps.setString(2, orgId);
      list = DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

    return list;
  }

  /** The get department. */
  private static String GET_DEPARTMENT = "SELECT dept_name, dept_id FROM department";

  /**
   * Gets the department hash map.
   *
   * @return the department hash map
   * @throws SQLException the SQL exception
   */
  public static HashMap getDepartmentHashMap() throws SQLException {

    HashMap<String, String> servDeptHashMap = new HashMap<String, String>();
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    con = DataBaseUtil.getReadOnlyConnection();
    ps = con.prepareStatement(GET_DEPARTMENT);
    rs = ps.executeQuery();
    while (rs.next()) {
      servDeptHashMap.put(rs.getString("dept_name"), rs.getString("dept_id"));
    }
    DataBaseUtil.closeConnections(con, ps, rs);

    return servDeptHashMap;
  }


  /** The Constant GET_OPERATION_ITEM_SUB_GROUP_TAX_DETAILS. */
  private static final String GET_OPERATION_ITEM_SUB_GROUP_TAX_DETAILS =
      "SELECT isg.item_subgroup_id, isg.subgroup_code, isg.item_subgroup_name, ig.group_code "
          + " FROM operation_item_sub_groups oisg "
          + " JOIN item_sub_groups isg ON(oisg.item_subgroup_id = isg.item_subgroup_id) "
          + " JOIN item_groups ig ON (ig.item_group_id = isg.item_group_id) "
          + " WHERE oisg.op_id = ? ";

  /**
   * Gets the operation item sub group tax details.
   *
   * @param itemId the item id
   * @return the operation item sub group tax details
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getOperationItemSubGroupTaxDetails(String itemId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_OPERATION_ITEM_SUB_GROUP_TAX_DETAILS);
      ps.setString(1, itemId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

}
