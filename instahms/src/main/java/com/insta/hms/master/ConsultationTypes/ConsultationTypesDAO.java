package com.insta.hms.master.ConsultationTypes;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class ConsultationTypesDAO extends GenericDAO {

	public ConsultationTypesDAO() {
		super("consultation_types");
	}

	private static final String CONS_TYPE_DETAILS = "SELECT consultation_type_id, consultation_type," +
			" status, consultation_code, patient_type, doctor_charge_type, charge_head, CASE WHEN duration " +
            " IS NULL THEN (select default_duration from scheduler_master where res_sch_category='DOC' AND " +
            " dept='*' AND res_sch_name='*' AND res_sch_type='DOC' AND status='A') ELSE duration END as duration " +
			" FROM consultation_types " +
			" WHERE status='A' ORDER BY consultation_type_id";


	public static List<BasicDynaBean> getConsultationTypeDetails() throws SQLException {

		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			pstmt = con.prepareStatement(CONS_TYPE_DETAILS);

			return DataBaseUtil.queryToDynaList(pstmt);
		}finally {
			DataBaseUtil.closeConnections(con, pstmt);
		}
	}

	private static final String CONSULTATION_ORG_DETAILS =
		" select ct.*, corg.org_id, corg.applicable, corg.item_code, corg.code_type,od.org_name " +
		" FROM consultation_types  ct " +
		" JOIN consultation_org_details corg on corg.consultation_type_id = ct.consultation_type_id "+
		" JOIN organization_details od on (od.org_id = corg.org_id) "+
		" WHERE ct.consultation_type_id=? and corg.org_id=? ";

	public BasicDynaBean consultationTypeOrgDetails(int consultationTypeId, String orgId) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;

		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(CONSULTATION_ORG_DETAILS);
			ps.setInt(1, consultationTypeId);
			ps.setString(2, orgId); 
			return DataBaseUtil.queryToDynaBean(ps);
		}finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public List<String> getAllNames() throws SQLException {
		return getColumnList("consultation_type");
	}

	private static final String GET_CONSULTATION_TYPES_FOR_IP = "SELECT * FROM consultation_types WHERE patient_type in(?) AND status ='A'";

	public static List<BasicDynaBean> getConsultationTypes(String visitType) throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_CONSULTATION_TYPES_FOR_IP);
			ps.setString(1, visitType);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_CONSULTATION_TYPE_ITEM_SUB_GROUP_TAX_DETAILS = "SELECT isg.item_subgroup_id, isg.subgroup_code, isg.item_subgroup_name, ig.group_code "+
			" FROM consultation_item_sub_groups cisg "+
			" JOIN item_sub_groups isg ON(cisg.item_subgroup_id = isg.item_subgroup_id) "+
			" JOIN item_groups ig ON (ig.item_group_id = isg.item_group_id) " +
			" WHERE cisg.consultation_type_id = ? ";
	
	public List<BasicDynaBean> getConsultationTypeItemSubGroupTaxDetails(Integer itemId) throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_CONSULTATION_TYPE_ITEM_SUB_GROUP_TAX_DETAILS);
			ps.setInt(1, itemId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	} 

	private static final String GET_CONSULTATION_ITEM_SUBGROUP_DETAILS = "select cisg.item_subgroup_id,isg.item_subgroup_name,ig.item_group_id,item_group_name,igt.item_group_type_id,igt.item_group_type_name " +
			" from consultation_item_sub_groups cisg "+
			" left join item_sub_groups isg on (isg.item_subgroup_id = cisg.item_subgroup_id) "+
			" left join consultation_types ct on (ct.consultation_type_id = cisg.consultation_type_id) "+
			" left join item_groups ig on (ig.item_group_id = isg.item_group_id)"+
			" left join item_group_type igt on (igt.item_group_type_id = ig.item_group_type_id)"+
			" where cisg.consultation_type_id = ? ";

	public static List<BasicDynaBean> getConsultationItemSubGroupDetails(Integer consultation_type_id) throws SQLException {
		List list = null;
		Connection con = null;
	    PreparedStatement ps = null;
		 try{
			 con=DataBaseUtil.getReadOnlyConnection();
			 ps=con.prepareStatement(GET_CONSULTATION_ITEM_SUBGROUP_DETAILS);
			 ps.setInt(1, consultation_type_id);
			 list = DataBaseUtil.queryToDynaList(ps);
		 }finally {
			 DataBaseUtil.closeConnections(con, ps);
		 }
		return list;
	}

  public List<BasicDynaBean> getActiveInsuranceCategories(Integer consultationTypeId)
   throws SQLException {
    PreparedStatement ps = null;
    Connection con = null;
    try{
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(SELECT_INSURANCE_CATEGORY_IDS);
      ps.setInt(1, consultationTypeId);

      return DataBaseUtil.queryToDynaList(ps);
    } finally{
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  private static final String SELECT_INSURANCE_CATEGORY_IDS = "SELECT insurance_category_id "
      + "FROM consultation_types_insurance_category_mapping WHERE consultation_type_id =?";

}