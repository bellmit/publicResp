package com.insta.hms.master.AnaesthesiaTypeMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

public class AnaesthesiaTypeMasterDAO extends GenericDAO {

	static Logger logger = LoggerFactory.getLogger(AnaesthesiaTypeMasterDAO.class);

	public AnaesthesiaTypeMasterDAO() {
		super("anesthesia_type_master");
	}

	public String getNextId() throws SQLException {
		return DataBaseUtil.getNextPatternId("anesthesia_type_master");
	}

	private static final String ANAESTHESIA_DETAILS =
		" select am.*, aorg.org_id, aorg.applicable, aorg.item_code, aorg.code_type,od.org_name " +
		" FROM anesthesia_type_master  am " +
		" JOIN anesthesia_type_org_details aorg on aorg.anesthesia_type_id = am.anesthesia_type_id "+
		" JOIN organization_details od on (od.org_id = aorg.org_id) "+
		" WHERE am.anesthesia_type_id=? and aorg.org_id=? ";

	public BasicDynaBean anaesthesiaTypeDetails(String anesthesiaTypeId, String orgId) throws SQLException {
		List<BasicDynaBean> l = DataBaseUtil.queryToDynaList(ANAESTHESIA_DETAILS, anesthesiaTypeId, orgId);
		if (l.size() > 0)
			return l.get(0);
		else
			return null;
	}
	private static final String ANAESTHESIA_TYPE_NAMESAND_iDS="select anesthesia_type_name," +
			" anesthesia_type_id " +
			" from anesthesia_type_master";

   public static List getAnaesthesiaNamesAndIds() throws SQLException{

	   return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(ANAESTHESIA_TYPE_NAMESAND_iDS));
   }

   private static final String ANAESTHESIA_TYPE_NAMES = "SELECT anesthesia_type_id, anesthesia_type_name " +
   		" FROM anesthesia_type_master";

	public static List<BasicDynaBean> getNames()throws SQLException {

		Connection con = null;
		PreparedStatement pstmt = null;
		List<BasicDynaBean> list = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			pstmt = con.prepareStatement(ANAESTHESIA_TYPE_NAMES);
			list = DataBaseUtil.queryToArrayList(pstmt);

			return list;

		} finally {
			DataBaseUtil.closeConnections(con, pstmt);
		}
	}

	private static final String ANAESTHESIA_TYPE_FIELDS = "SELECT *";
 	private static final String ANAESTHESIA_TYPE_COUNT = "SELECT count(*)";
 	private static final String ANAESTHESIA_TYPE_FROM_TABLES = " FROM (SELECT am.anesthesia_type_name, " +
 			"am.anesthesia_type_id, am.status, am.service_sub_group_id, ad.org_id, ad.applicable " +
 			" FROM anesthesia_type_master am " +
 			" JOIN anesthesia_type_org_details ad ON(ad.anesthesia_type_id = am.anesthesia_type_id)"+
 		" ) AS foo";

	public PagedList search(Map requestParams, Map pagingParams) throws ParseException, SQLException {

		Connection con = null;
		SearchQueryBuilder qb = null;
		try {
			con = DataBaseUtil.getConnection();
			qb = new SearchQueryBuilder(con, ANAESTHESIA_TYPE_FIELDS, ANAESTHESIA_TYPE_COUNT,
					ANAESTHESIA_TYPE_FROM_TABLES, pagingParams);
			qb.addFilterFromParamMap(requestParams);
			qb.addSecondarySort("anesthesia_type_id");
			qb.build();

			return qb.getMappedPagedList();
		} finally {
			qb.close();
			DataBaseUtil.closeConnections(con, null);
		}
	}

	public List<String> getAllNames() throws SQLException {
		return getColumnList("anesthesia_type_name");
	}

	public  static final String GET_ANAESTHESIA_TYPE_DETAILS=
			"SELECT am.anesthesia_type_id,am.anesthesia_type_name,am.status, " +
				 "sg.service_group_name, ssg.service_sub_group_name, am.min_duration," +
				 "am.incr_duration,am.slab_1_threshold, am.duration_unit_minutes " +
			"FROM anesthesia_type_master am " +
			"JOIN service_sub_groups ssg using(service_sub_group_id) " +
	   		"JOIN service_groups sg using(service_group_id) " +
	   		"WHERE am.status = 'A' " +
			"ORDER BY anesthesia_type_id";

	public static  List<BasicDynaBean> getAnaesthesiaDetails() throws SQLException{

		List anaesthesiaTypeList=null;
		PreparedStatement ps=null;
		Connection con=null;
		con=DataBaseUtil.getReadOnlyConnection();
		ps=con.prepareStatement(GET_ANAESTHESIA_TYPE_DETAILS);
		anaesthesiaTypeList = DataBaseUtil.queryToDynaList(ps);
		DataBaseUtil.closeConnections(con, ps);
		return anaesthesiaTypeList;
	}
	
	private static final String GET_ANAESTHESIA_TYPE_ITEM_SUB_GROUP_TAX_DETAILS = "SELECT isg.item_subgroup_id, isg.subgroup_code, isg.item_subgroup_name, ig.group_code "+
			" FROM anesthesia_item_sub_groups aisg "+
			" JOIN item_sub_groups isg ON(aisg.item_subgroup_id = isg.item_subgroup_id) "+
			" JOIN item_groups ig ON (ig.item_group_id = isg.item_group_id) " +
			" WHERE aisg.anesthesia_type_id = ? ";
	
	public List<BasicDynaBean> getAnaesthesiaTypeSubGroupTaxDetails(String itemId) throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_ANAESTHESIA_TYPE_ITEM_SUB_GROUP_TAX_DETAILS);
			ps.setString(1, itemId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	
	private static final String GET_ANAESTHESIA_ITEM_SUBGROUP_DETAILS = "select aisg.item_subgroup_id,isg.item_subgroup_name,ig.item_group_id,item_group_name,igt.item_group_type_id,igt.item_group_type_name " +
			" from anesthesia_item_sub_groups aisg "+
			" left join item_sub_groups isg on (isg.item_subgroup_id = aisg.item_subgroup_id) "+
			" left join anesthesia_type_master atm on (atm.anesthesia_type_id = aisg.anesthesia_type_id) "+
			" left join item_groups ig on (ig.item_group_id = isg.item_group_id)"+
			" left join item_group_type igt on (igt.item_group_type_id = ig.item_group_type_id)"+
			" where aisg.anesthesia_type_id = ? "; 

	public static List<BasicDynaBean> getAnaesthesiaItemSubGroupDetails(String anesthesiatypeId) throws SQLException {
		List list = null;
		Connection con = null;
	    PreparedStatement ps = null;
		 try{
			 con=DataBaseUtil.getReadOnlyConnection();
			 ps=con.prepareStatement(GET_ANAESTHESIA_ITEM_SUBGROUP_DETAILS);
			 ps.setString(1, anesthesiatypeId);
			 list = DataBaseUtil.queryToDynaList(ps);
		 }finally {
			 DataBaseUtil.closeConnections(con, ps);
		 }
		return list;
	}

	public List<BasicDynaBean> getActiveInsuranceCategories(String anesthesiaTypeId)
	    throws SQLException {
	     PreparedStatement ps = null;
	     Connection con = null;
	     try{
	       con = DataBaseUtil.getConnection();
	       ps = con.prepareStatement(SELECT_INSURANCE_CATEGORY_IDS);
	       ps.setString(1, anesthesiaTypeId);

	       return DataBaseUtil.queryToDynaList(ps);
	     } finally{
	       DataBaseUtil.closeConnections(con, ps);
	     }
	 }

	 private static final String SELECT_INSURANCE_CATEGORY_IDS = "SELECT insurance_category_id "
	     + "FROM anesthesia_types_insurance_category_mapping WHERE anesthesia_type_id =?";

}
