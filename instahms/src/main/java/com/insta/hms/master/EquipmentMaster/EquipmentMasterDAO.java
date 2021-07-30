package com.insta.hms.master.EquipmentMaster;

import com.bob.hms.common.AutoIncrementId;
import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class EquipmentMasterDAO extends GenericDAO {

	static Logger logger = LoggerFactory.getLogger(EquipmentMasterDAO.class);

	public EquipmentMasterDAO() {
		super("equipment_master");
	}

	public String getNextId() throws SQLException {
		return AutoIncrementId.getNewIncrUniqueId("EQ_ID", "EQUIPMENT_MASTER","EQIPMENTID");

	}

	/*
	 * Search: returns a PagedList suitable for a dashboard type list
	 */
 	private static final String EQUIPMENT_FIELDS = "SELECT *";
 	private static final String EQUIPMENT_COUNT = "SELECT count(*)";
 	private static final String EQUIPMENT_FROM_TABLES = " FROM (SELECT em.equipment_name, em.eq_id," +
 		" em.status, dept.dept_name, em.dept_id, em.service_sub_group_id,'equipment'::text as chargeCategory, " +
 		" eod.org_id, eod.is_override, od.org_name "+
 		" FROM equipment_master em "+
 		" JOIN equip_org_details eod on (eod.equip_id = em.eq_id) "+
 		" JOIN organization_details od on (od.org_id = eod.org_id) "+
 		" JOIN department dept ON (dept.dept_id = em.dept_id) ) AS foo";

	public PagedList search(Map requestParams, Map pagingParams) throws ParseException, SQLException {

		Connection con = null;
		SearchQueryBuilder qb = null;
		try {
			con = DataBaseUtil.getConnection();
			qb = new SearchQueryBuilder(con, EQUIPMENT_FIELDS, EQUIPMENT_COUNT,
						EQUIPMENT_FROM_TABLES, pagingParams);
			qb.addFilterFromParamMap(requestParams);
			qb.addSecondarySort("eq_id");
			qb.build();

			return qb.getMappedPagedList();
		} finally {
			qb.close();
			DataBaseUtil.closeConnections(con, null);
		}
	}

	public boolean insert(Connection con, BasicDynaBean b) throws SQLException, java.io.IOException {
		try {
			super.insert(con, b);
			return true;
		} catch (SQLException e) {
			if (!DataBaseUtil.isDuplicateViolation(e))
				throw (e);
		}
		return false;
	}

	public int update(Connection con, Map columnData, String keyName, Object keyValue)
		throws SQLException, IOException {
		try {
			int rows = super.update(con, columnData, keyName, keyValue);
			return rows;
		} catch (SQLException e) {
			if (!DataBaseUtil.isDuplicateViolation(e))
				throw (e);
		}
		return 0;
	}

	public List<String> getAllNames() throws SQLException {
		return getColumnList("equipment_name");
	}

	private static final String EQUIPMENT_DETAILS =
		" select em.*, ec.org_id, bed_type" +
		" FROM equipment_master  em " +
		" JOIN equipement_charges  ec on ec.equip_id = em.eq_id" +
		" WHERE em.eq_id=? and ec.org_id=? ";

	public BasicDynaBean getEquipmentDetails(String equipmentId, String orgId) throws SQLException {
		List<BasicDynaBean> l = DataBaseUtil.queryToDynaList(EQUIPMENT_DETAILS, equipmentId, orgId);
		if (l.size() > 0)
			return l.get(0);
		else
			return null;
	}

	private static final String EQUIPMENT_NAMES = "SELECT eq_id, equipment_name FROM equipment_master";

	public static List<BasicDynaBean> getNames()throws SQLException {

		Connection con = null;
		PreparedStatement pstmt = null;
		List<BasicDynaBean> list = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			pstmt = con.prepareStatement(EQUIPMENT_NAMES);
			list = DataBaseUtil.queryToArrayList(pstmt);

			return list;

		} finally {
			DataBaseUtil.closeConnections(con, pstmt);
		}
	}

   private static final String EQUIPMENTS_NAMESAND_iDS="select equipment_name,eq_id from equipment_master";

   public static List getEquipmentsNamesAndIds() throws SQLException{

	   return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(EQUIPMENTS_NAMESAND_iDS));
   }

   public  static final String GET_EQUIPMENT_DETAILS=
	   								"SELECT eq.eq_id,eq.equipment_name,d.dept_name,eq.status, sgr.service_group_name, ssgr.service_sub_group_name, " +
   										 "eq.min_duration,eq.incr_duration, eq.equipment_code, " +
   										 "eq.duration_unit_minutes, eq.slab_1_threshold " +
   									"FROM equipment_master eq " +
   									"JOIN department d USING(dept_id) " +
   									"JOIN service_sub_groups ssgr USING(service_sub_group_id) " +
   									"JOIN service_groups sgr USING(service_group_id) " +
   									"WHERE eq.status='A' ORDER BY eq_id";

   public static  List<BasicDynaBean> getEquipmentDetails() throws SQLException{

	   List eqipmentList=null;
		PreparedStatement ps=null;
		Connection con=null;
		con=DataBaseUtil.getReadOnlyConnection();
		ps=con.prepareStatement(GET_EQUIPMENT_DETAILS);
		eqipmentList = DataBaseUtil.queryToDynaList(ps);
		DataBaseUtil.closeConnections(con, ps);
		return eqipmentList;

   }

	private static final String GET_EQUIPMENT_DEF = "SELECT eq.eq_id,eq.equipment_name,eq.dept_id,eq.status," +
			" coalesce(eq.min_duration,0) as min_duration, eq.slab_1_threshold, eq.duration_unit_minutes, coalesce(eq.incr_duration,0)as incr_duration ," +
			" eq.equipment_code,dept.dept_name,eq.charge_basis FROM equipment_master eq " +
			" JOIN department dept ON" +
			" eq.dept_id = dept.dept_id  AND eq.eq_id = ?";

	public ArrayList<Hashtable<String,String>> getEquipmentDef(String equipmentId)throws SQLException{
		Connection con = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps = con.prepareStatement(GET_EQUIPMENT_DEF) ;
		ps.setString(1,equipmentId );
		ArrayList<Hashtable<String,String>> al= DataBaseUtil.queryToArrayList(ps);
		ps.close();
		con.close();
		return al;
	}
	
	private static final String GET_EQUIPMENT_ITEM_SUBGROUP_DETAILS = "select eisg.item_subgroup_id,isg.item_subgroup_name,ig.item_group_id,item_group_name,igt.item_group_type_id,igt.item_group_type_name " +
			" from equipment_item_sub_groups eisg "+
			" left join item_sub_groups isg on (isg.item_subgroup_id = eisg.item_subgroup_id) "+
			" left join equipment_master em on (em.eq_id = eisg.eq_id) "+
			" left join item_groups ig on (ig.item_group_id = isg.item_group_id)"+
			" left join item_group_type igt on (igt.item_group_type_id = ig.item_group_type_id)"+
			" where eisg.eq_id = ? "; 

		public static List<BasicDynaBean> getEquipItemSubGroupDetails(String eqId) throws SQLException {
			List list = null;
			Connection con = null;
		    PreparedStatement ps = null;
		 try{
			 con=DataBaseUtil.getReadOnlyConnection();
			 ps=con.prepareStatement(GET_EQUIPMENT_ITEM_SUBGROUP_DETAILS);
			 ps.setString(1, eqId);
			 list = DataBaseUtil.queryToDynaList(ps);
		 }finally {
			 DataBaseUtil.closeConnections(con, ps);
		 }
		return list;
		}

	private static final String GET_EQUIPMENT_ITEM_SUB_GROUP_TAX_DETAILS = "SELECT isg.item_subgroup_id, isg.subgroup_code, isg.item_subgroup_name, ig.group_code "+
			" FROM equipment_item_sub_groups eisg "+
			" JOIN item_sub_groups isg ON(eisg.item_subgroup_id = isg.item_subgroup_id) "+
			" JOIN item_groups ig ON (ig.item_group_id = isg.item_group_id) " +
			" WHERE eisg.eq_id = ? ";
	
	public List<BasicDynaBean> getEquipmentItemSubGroupTaxDetails(String itemId) throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_EQUIPMENT_ITEM_SUB_GROUP_TAX_DETAILS);
			ps.setString(1, itemId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

}
