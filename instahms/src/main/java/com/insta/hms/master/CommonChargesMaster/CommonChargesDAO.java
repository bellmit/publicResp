package com.insta.hms.master.CommonChargesMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import org.apache.commons.beanutils.BasicDynaBean;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CommonChargesDAO extends GenericDAO{
	public CommonChargesDAO(){
		super("common_charges_master");
	}
	public static final String CHARGES ="SELECT * ";

	public static final String CHARGES_COUNT = "SELECT count(*)";

	public static final String FROM_TABLE = "FROM (SELECT ccm.*,chargegroup_name,chargehead_name  FROM common_charges_master ccm " +
			 " JOIN chargehead_constants ON (charge_type = chargehead_id) "+
			 " JOIN chargegroup_constants USING(chargegroup_id))AS foo";

	public PagedList getAllCommonCharges(Map filter,Map listing)throws SQLException, ParseException{
		Connection con = null;
		try{
			con = DataBaseUtil.getConnection();
			SearchQueryBuilder qb = new SearchQueryBuilder(con,CHARGES,CHARGES_COUNT,FROM_TABLE,listing);
			qb.addFilterFromParamMap(filter);
			qb.build();
			PagedList list = qb.getMappedPagedList();
			return list;
		}finally{
			if(con != null)con.close();
		}
	}
	public BasicDynaBean getCommonCharge(String charge_name) throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement("SELECT * FROM common_charges_master WHERE charge_name = ?");
			ps.setString(1, charge_name);
			return DataBaseUtil.queryToDynaBean(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps ,rs);
		}
	}
	private String GET_CHARGE = "SELECT charge FROM common_charges_master WHERE charge_name = ? ";

	private String UPDATE_CHARGE = "UPDATE common_charges_master SET charge = ? WHERE charge_name = ? ";

	public boolean groupUpdate(Connection con ,ArrayList chargesToUpdate,Double variancePer,
			Double varianceValue,String allRecords ,String varianceType,boolean useValue) throws Exception{
		Iterator charges = chargesToUpdate.iterator();


		PreparedStatement ps = con.prepareStatement(GET_CHARGE);
		PreparedStatement ps1 = con.prepareStatement(UPDATE_CHARGE);

		double charge = 0.0;
		while(charges.hasNext()){
			String chargeName = charges.next().toString();
			ps.setString(1, chargeName);
			String chargeStr = DataBaseUtil.getStringValueFromDb(ps);

			if(chargeStr == null){
				chargeStr = "0";
			}
			charge = new Double(chargeStr).doubleValue();
			if(useValue){
				if(varianceType.equals("Incr")){
					charge = charge +varianceValue.doubleValue();
				}else{
					charge = charge - varianceValue.doubleValue();
				}
			}else{
				if(varianceType.equals("Incr")){
					charge += charge * (variancePer.doubleValue()/100);
				}else{
					charge -= charge * (variancePer.doubleValue()/100);
				}
			}
			if (charge <0){
				charge = 0;
			}

			ps1.setDouble(1, charge);
			ps1.setString(2, chargeName);

			ps1.addBatch();
		}
		int a[] = ps1.executeBatch();
		ps1.close();ps.close();
		return DataBaseUtil.checkBatchUpdates(a);
	}
	private static String UPDATE_ALL_RECORDS_PLUS = "UPDATE common_charges_master SET " +
			"					charge = ROUND(charge + ?) ";

	private static String UPDATE_ALL_RECORDS_MINUS = "UPDATE common_charges_master SET " +
			"					charge = ROUND(charge - ?) ";

	private static String UPDATE_ALL_RECORDS_BY = "UPDATE common_charges_master SET " +
			"					charge = ROUND((charge + (charge*?)/100)) ";

	public boolean updateAllRecords(Connection con,Double variancePer,
			Double varianceValue,String varianceType,boolean useValue)throws Exception{
		PreparedStatement ps = null;
		boolean sucess = false;
		if(useValue){
			if(varianceType.equals("Incr")){
				ps=con.prepareStatement(UPDATE_ALL_RECORDS_PLUS);
			}else{
				ps=con.prepareStatement(UPDATE_ALL_RECORDS_MINUS);
			}

				ps.setDouble(1, varianceValue);
				int i = ps.executeUpdate();
				if (i>0){
					sucess = true;
				}
		}else{
			if(!varianceType.equals("Incr")){
				variancePer =  new Double(-variancePer);
			}
			ps = con.prepareStatement(UPDATE_ALL_RECORDS_BY);
			ps.setBigDecimal(1, new BigDecimal(variancePer));

			int i = ps.executeUpdate();
			if(i>0){
				sucess = true;
			}
		}
		ps.close();
		return sucess;
	}
	public static final String GET_COMMON_CHARGES = "SELECT charge_name, charge,charge_type " +
	" FROM common_charges_master WHERE status='A' ORDER BY charge_name";

	public List getCommonCharges() throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		List list = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_COMMON_CHARGES);
			list = DataBaseUtil.queryToArrayList(ps);
		} finally {
			if (ps != null) ps.close();
			if (con != null) con.close();
		}
		return list;
	}
	private static final String CHARGES_TO_FILTER = "SELECT * FROM common_charges_master" +
			" where charge_type = ?";
	public List filterCharges(String filterColumn,String filterValue)throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(CHARGES_TO_FILTER);
			ps.setString(1, filterValue);
			return DataBaseUtil.queryToArrayList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_COMMON_CHARGES_ITEM_SUB_GROUP_TAX_DETAILS = "SELECT isg.item_subgroup_id, isg.subgroup_code, isg.item_subgroup_name, ig.group_code "+
			" FROM common_item_sub_groups cisg "+
			" JOIN item_sub_groups isg ON(cisg.item_subgroup_id = isg.item_subgroup_id) "+
			" JOIN item_groups ig ON (ig.item_group_id = isg.item_group_id) " +
			" WHERE cisg.charge_name = ? ";
	
	public List<BasicDynaBean> getCommonChargesItemSubGroupTaxDetails(String itemId) throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_COMMON_CHARGES_ITEM_SUB_GROUP_TAX_DETAILS);
			ps.setString(1, itemId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_COMMON_CHARGE_ITEM_SUBGROUP_DETAILS = "select cisg.item_subgroup_id,isg.item_subgroup_name,ig.item_group_id,item_group_name,igt.item_group_type_id,igt.item_group_type_name " +
			" from common_item_sub_groups cisg "+
			" left join item_sub_groups isg on (isg.item_subgroup_id = cisg.item_subgroup_id) "+
			" left join common_charges_master ccm on (ccm.charge_name = cisg.charge_name) "+
			" left join item_groups ig on (ig.item_group_id = isg.item_group_id)"+
			" left join item_group_type igt on (igt.item_group_type_id = ig.item_group_type_id)"+
			" where cisg.charge_name = ? "; 

	public static List<BasicDynaBean> getOpItemSubGroupDetails(String chargeName) throws SQLException {
		List list = null;
		Connection con = null;
	    PreparedStatement ps = null;
		 try{
			 con=DataBaseUtil.getReadOnlyConnection();
			 ps=con.prepareStatement(GET_COMMON_CHARGE_ITEM_SUBGROUP_DETAILS);
			 ps.setString(1, chargeName);
			 list = DataBaseUtil.queryToDynaList(ps);
		 }finally {
			 DataBaseUtil.closeConnections(con, ps);
		 }
		return list;
	}

	 public static List<BasicDynaBean> getActiveInsuranceCategories(String chargeName)
	      throws SQLException {
	       PreparedStatement ps = null;
	       Connection con = null;
	       try{
	         con = DataBaseUtil.getConnection();
	         ps = con.prepareStatement(SELECT_INSURANCE_CATEGORY_IDS);
	         ps.setString(1, chargeName);

	         return DataBaseUtil.queryToDynaList(ps);
	       } finally{
	         DataBaseUtil.closeConnections(con, ps);
	       }
	   }

	   private static final String SELECT_INSURANCE_CATEGORY_IDS = "SELECT insurance_category_id "
	       + "FROM common_charges_insurance_category_mapping WHERE charge_name =?";

}
