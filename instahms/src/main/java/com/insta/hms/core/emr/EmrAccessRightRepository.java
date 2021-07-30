package com.insta.hms.core.emr;

import com.bob.hms.common.AutoIncrementId;
import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericRepository;
import com.insta.hms.emr.EMRDoc;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/**
 * EMR access Repository.
 */
@Repository
public class EmrAccessRightRepository extends GenericRepository {

  static Logger log = LoggerFactory.getLogger(EmrAccessRightRepository.class);

  private static String rules = "SELECT rule_type FROM emr_access_rule WHERE "
      + " role_id=? and rule_type='ROLE' UNION ALL SELECT distinct rule_type "
      + " FROM emr_access_rule WHERE rule_type='DOC'";

  /**
   * Instantiates a new GenericRepository.
   */
  public EmrAccessRightRepository() {
    super("emr_access_rule");
  }

  /**
   * Get Emr access rule.
   * @param role_id
   * @return returns list of rules
   * @throws SQLException throws sql exception.
   */
  public List<BasicDynaBean> getRules(int role_id) throws SQLException {
    return DataBaseUtil.queryToDynaList(rules, role_id+"");
  }

  /**
   * Get next rule id.
   * @return returns next unique rule id.
   * @throws SQLException throws sql exception.
   */
  public static String getAccessRightRuleId() throws SQLException{
    return AutoIncrementId.getSequenceId("accessRightRule_sequence","Rule Number");
  }

  /**
   * Get  user attributes.
   * @param userId logged in user id
   * @param centerId logged in user id
   * @param roleId logged in role id
   * @return returns map of user attributes
   * @throws SQLException throws sql exception.
   */
  public Map getUserAttributesMap(String userId, int centerId, int roleId) throws SQLException {
    // Given a user Id this should return a map containing the following
    // center_id, role_id, user_id are available in the RequestContext.getSession().getAttributes
    // dept_id should be taken from the u_user table (this is not available for all users)
    log.debug("Start:getUserAttributesMap::roleId==>"+roleId+" centerId==>"+centerId+" userId==>"+userId);
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    ResultSet rs = null;
    Map usermap = null;
    try {
      // RC : Need to take the role_id / center_id from session. If that is null then use
      // the query to get from the database
      String query = "";
      if(null == String.valueOf(centerId) || null == String.valueOf(roleId)){
        query = "SELECT u.emp_username,u.role_id,u.center_id, " +
            "	case when u.scheduler_dept_id is null or u.scheduler_dept_id ='' "+
            " 	then d.dept_id else u.scheduler_dept_id end as dept_id "+
            " 	FROM u_user u LEFT JOIN doctors d ON (d.doctor_id=u.doctor_id) " +
            " 	WHERE emp_username=?";
      }else{
        query = "SELECT u.emp_username," +
            "	case when u.scheduler_dept_id is null or u.scheduler_dept_id ='' "+
            " 	then d.dept_id else u.scheduler_dept_id end as dept_id "+
            " 	FROM u_user u LEFT JOIN doctors d ON (d.doctor_id=u.doctor_id) " +
            " 	WHERE emp_username=?";
      }
      ps = con.prepareStatement(query);
      ps.setString(1, userId);
      rs = ps.executeQuery();
      if (rs.next()) {
        usermap = new HashMap();
        usermap.put("user_id", rs.getString("emp_username"));
        if(null == String.valueOf(centerId) || null == String.valueOf(roleId)){
          usermap.put("role_id", rs.getInt("role_id"));
          usermap.put("center_id", rs.getInt("center_id"));
        }else{
          usermap.put("role_id",roleId);
          usermap.put("center_id", centerId);
        }
        usermap.put("dept_id", rs.getString("dept_id"));
      }
      log.debug("End:getUserAttributesMap::roleId==>"+usermap.get("role_id")+" deptId==>"+usermap.get("dept_id")+" centerId==>"+usermap.get("center_id")+" user_id==>"+usermap.get("user_id"));
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return usermap;
  }

  /**
   * Query the database to get the record that correspond to the document type id and rule type = doc.
   * @param doc EMR document
   * @param docDeptId doc type id
   * @return returns rule details for a doc type
   * @throws SQLException throws sql exception
   */
  public BasicDynaBean getRuleDetailsBasedonDocType(EMRDoc doc, String docDeptId) throws SQLException {
    String DOCS_BY_DOCUMENTTYPEID = " SELECT ear.rule_id, ear.rule_type, ear.doc_type_id, ear.doc_sub_type, ear.center_access,"+
        "ear.dept_access, ear.role_access, ear.user_access, ear.doc_access, ear.role_id,eard.entity_id"+
        " FROM emr_access_rule ear LEFT JOIN emr_access_rule_details eard ON (ear.rule_id=eard.rule_id)"+
        " WHERE rule_type='DOC' AND doc_type_id=? ";

    String DOCS_BY_DOCUMENTSUBTYPEID = " SELECT ear.rule_id, ear.rule_type, ear.doc_type_id, ear.doc_sub_type, ear.center_access,"+
        "ear.dept_access, ear.role_access, ear.user_access, ear.doc_access, ear.role_id,eard.entity_id"+
        " FROM emr_access_rule ear LEFT JOIN emr_access_rule_details eard ON (ear.rule_id=eard.rule_id)"+
        " WHERE rule_type='DOC' AND doc_type_id=? AND doc_sub_type=?";

    if("SYS_CONSULT".equalsIgnoreCase(doc.getType())){
      return DataBaseUtil.queryToDynaBean(DOCS_BY_DOCUMENTSUBTYPEID, new String[] {doc.getType(), docDeptId});
    }else{
      return DataBaseUtil.queryToDynaBean(DOCS_BY_DOCUMENTTYPEID, doc.getType());
    }
  }

  /**
   * Get Document attributes.
   * @param doc doc type
   * @param userId user id
   * @param centerId center id
   * @param roleId role id
   * @return returns
   * @throws SQLException throws sql
   */
  public Map getDocumentAttributesMap(EMRDoc doc, String userId, int centerId, int roleId) throws SQLException {
    // Given a document create a map containing the following
    // user_id (the owner of the document)
    // 	check username in emrdoc, if null, take the doctor and query u_user for doc_id and take the corresponding username from u_user
    // role_id (role of the owner of the document), role_id corresponding to user_id from u_user / u_role table
    // dept_id (dept of the document / owner of the document in that order),take the scheduler_dept_id for user_id
    // if null take dept_id from doctors where doctor_id = user_id
    // if null take the dept_id from patient_registration where patient_id = emrdoc.visit_id
    // center_id (center of the document / owner of the document in that order)
    // center_id from patient_registrationgetRuleDetails where patient_id = emrdoc.visit_id
    // doc_type, type from emrdoc
    // None of this coming from session, all from database.
    Map docmap = new HashMap();
    String owner = null;
    String username = doc.getUserName();
    String updatedBy = doc.getUpdatedBy();
    String visitId = doc.getVisitid();
    String docType = doc.getType();
    String doctor = doc.getDoctor();//"DOC0018"
    log.debug("Start:getDocumentAttributesMap::username="+username+" doctor="+doctor+" visitId="+visitId+" docType="+docType);
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      // RC : Identify the owner of the document
      if (null != doctor && !doctor.equals("")) {
        String usrnameQuery ="SELECT emp_username FROM u_user where doctor_id=?";
        ps = con.prepareStatement(usrnameQuery);
        ps.setString(1, doctor);
        owner = DataBaseUtil.getStringValueFromDb(ps);

        ps.close();
      }
      if(null==owner || "".equalsIgnoreCase(owner)){
        // owner = username;
        owner = (null != username && !"".equals(username)) ? username : updatedBy ;
      }
			/*if(null==owner || "".equalsIgnoreCase(owner)){
				// RC : take the doctor from the visit
				// patient_registration.doctor
				String doctorQuery ="SELECT doctor FROM patient_registration where patient_id='"+visitId+"'";
				String doctorId = DataBaseUtil.getStringValueFromDB(doctorQuery);

				String usrnameQuery ="SELECT emp_username FROM u_user where doctor_id='"+doctorId+"'";
				owner = DataBaseUtil.getStringValueFromDB(usrnameQuery);
			}*/
      docmap.put("user_id", owner);
      if(null!=docType || !"".equalsIgnoreCase(docType)){
        docmap.put("docType", docType);
      }

      //RC : check for owner == null / empty. If null or empty then take only visit department
      // check for owner == null / empty. If null or empty then take only visit center
      String query = "";
      if(null==owner || "".equalsIgnoreCase(owner)){
        query = " SELECT distinct pr.dept_name,pr.center_id" +
            " FROM patient_registration pr " +
            " WHERE pr.patient_id=?";
      }else{
        query = " SELECT distinct u.role_id,u.emp_username, " +
            " CASE WHEN (u.scheduler_dept_id is null OR u.scheduler_dept_id ='') THEN " +
            "	coalesce(d.dept_id,pr.dept_name) ELSE u.scheduler_dept_id end AS dept_id, "+
            " CASE WHEN (u.center_id is null) THEN " +
            "	coalesce(pr.center_id, dcm.center_id) ELSE u.center_id end AS center_id "+
            " FROM u_user u " +
            " LEFT JOIN doctors d ON (d.doctor_id=u.doctor_id) "+
            " LEFT JOIN doctor_center_master dcm ON (d.doctor_id=dcm.doctor_id)"+
            " LEFT JOIN patient_registration pr ON (pr.patient_id=?) " +
            " WHERE u.emp_username=?";
      }
      ps = con.prepareStatement(query);
      int index=1;
      if (null==owner || "".equalsIgnoreCase(owner)) {
        ps.setString(index++, visitId);
      } else {
        ps.setString(index++, visitId);
        ps.setString(index++, owner);
      }
      //ps.setString(1, owner);
      rs = ps.executeQuery();
      if (rs.next()) {
        if(null==owner || "".equalsIgnoreCase(owner)){
          docmap.put("dept_id", rs.getString("dept_name"));
          docmap.put("center_id", rs.getInt("center_id"));
        }else{
          docmap.put("role_id", rs.getInt("role_id"));
          docmap.put("user_id", rs.getString("emp_username"));
          docmap.put("dept_id", rs.getString("dept_id"));
          docmap.put("center_id", rs.getInt("center_id"));
        }
      }

      log.debug("End:getDocumentAttributesMap::roleId==>"+docmap.get("role_id")+" deptId==>"+docmap.get("dept_id")+" centerId==>"+docmap.get("center_id")+" docType==>"+docType);

    } catch (RuntimeException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }finally {
      DataBaseUtil.closeConnections(con, ps);
    }

    return docmap;
  }

  public static String DOCS_BY_ROLEID =" SELECT ear.rule_id, ear.rule_type, ear.doc_type_id, ear.doc_sub_type, ear.center_access,"+
      "ear.dept_access, ear.role_access, ear.user_access, ear.doc_access, ear.role_id,eard.entity_id"+
      " FROM emr_access_rule ear LEFT JOIN emr_access_rule_details eard ON (ear.rule_id=eard.rule_id)"+
      " WHERE rule_type='ROLE' AND role_id=? ";

  /**
   * Get documents by role id.
   * @param roleId role id
   * @return returns BasicDynaBean
   * @throws SQLException throws sql exception
   */
  public BasicDynaBean getRuleDetailsBasedonRoleId(String roleId) throws SQLException {
    // Query the database to get the record that correspond to the document type id and rule type = doc
    // return that as a bean. This can go into the DAO
    return DataBaseUtil.queryToDynaBean(DOCS_BY_ROLEID, roleId);
  }

  public static String ALLOWED_CENTERS =" SELECT entity_id FROM emr_access_rule_details"+
      " WHERE entity_type='C' AND rule_id=?";

  /**
   * Get allowed centers mapped for a rule.
   * @param ruleId rule id
   * @return returns list of BasicDynaBean
   * @throws Exception throws exception
   */
  public List<BasicDynaBean> getAllowedCenters(String ruleId) throws Exception {
    return DataBaseUtil.queryToDynaList(ALLOWED_CENTERS, ruleId);
  }

  public static String ALLOWED_DEPARTMENTS =" SELECT entity_id FROM emr_access_rule_details"+
      " WHERE entity_type='D' AND rule_id=?";

  /**
   * Get allowed department for rule id
   * @param ruleId rule id.
   * @return returns list of BasicDynaBean
   * @throws Exception throws exception
   */
  public List<BasicDynaBean> getAllowedDepartments(String ruleId) throws Exception {
    return DataBaseUtil.queryToDynaList(ALLOWED_DEPARTMENTS, ruleId);
  }

  public static String ALLOWED_ROLES =" SELECT entity_id FROM emr_access_rule_details"+
      " WHERE entity_type='R' AND rule_id=?";

  /**
   * Get allowed roles for rule id.
   * @param ruleId rule id
   * @return returns list of BasicDynaBeans
   * @throws Exception throws exception
   */
  public List<BasicDynaBean> getAllowedRoles(String ruleId) throws Exception {
    return DataBaseUtil.queryToDynaList(ALLOWED_ROLES, ruleId);
  }

  public static String ALLOWED_USERS =" SELECT entity_id FROM emr_access_rule_details"+
      " WHERE entity_type='U' AND rule_id=?";

  /**
   * Get allowed users for rule id.
   * @param ruleId rule id.
   * @return returns list of BasicDynaBeans
   * @throws Exception throws exception
   */
  public List<BasicDynaBean> getAllowedUsers(String ruleId) throws Exception {
    return DataBaseUtil.queryToDynaList(ALLOWED_USERS, ruleId);
  }

  public static String ALLOWED_DOCUMENTS_TYPES =" SELECT entity_id FROM emr_access_rule_details"+
      " WHERE entity_type='T' AND rule_id=?";

  /**
   * Get allowed document types.
   * @param ruleId rule id.
   * @return returns List BasicDynaBeans
   * @throws Exception throws exception
   */
  public List<BasicDynaBean> getAllowedDocumentTypes(String ruleId) throws Exception {
    return DataBaseUtil.queryToDynaList(ALLOWED_DOCUMENTS_TYPES, ruleId);
  }
}
