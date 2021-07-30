package com.insta.hms.master.ServiceConsumableMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ServiceConsumableMasterDAO extends GenericDAO{
	public String status = "Active";
	public ServiceConsumableMasterDAO() {
		super("service_consumables");
	}

	private static String SERV_FIELDS = " SELECT *" ;

	private static String SERV_COUNT = "SELECT count(*)";

	private static String SERV_TABLES = " FROM (SELECT sc.consumable_id,im.medicine_name as item_name,s.service_name," +
			" sc.quantity_needed,sc.service_id,sc.status " +
			"	FROM store_item_details im" +
			" JOIN service_consumables sc ON  sc.consumable_id=im.medicine_id" +
			" JOIN services s USING (service_id)) AS foo";

	public PagedList getServiceConsumbaleDetailPages(Map requestParams, Map<LISTING, Object> pagingParams)
				throws ParseException, SQLException {
		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			SearchQueryBuilder qb =
			new SearchQueryBuilder(con, SERV_FIELDS, SERV_COUNT, SERV_TABLES, pagingParams);
			qb.addFilterFromParamMap(requestParams);
			qb.addSecondarySort("service_id");
			qb.build();
			PagedList l = qb.getMappedPagedList();
			qb.close();
			return l;
		}finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	public String getConsumables(String service_id)throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		StringBuffer consumables = new StringBuffer();
		int count = 0;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement("select i.medicine_name as item_name,sc.status from store_item_details i join service_consumables sc on(i.medicine_id = sc.consumable_id) where sc.service_id=?  ");
			ps.setString(1, service_id);
			rs = ps.executeQuery();
			while(rs.next()){
				if(rs.getString("status").equalsIgnoreCase("I"))status = "Inactive";
				count++;
				if(count == 1)consumables.append(rs.getString("medicine_name"));
				else consumables.append(",").append(rs.getString("medicine_name"));
			}
			return consumables.toString();
		}finally{
			if(con!= null)con.close();
			if(ps != null)ps.close();
			if(rs != null)rs.close();
		}
	}
	private static String FIND_RECORD_BY_SERVICE = "SELECT s.service_name,sc.service_id,sc.consumable_id," +
			" (case when sc.status='A' then 'true' else 'false' end ) as status,i.medicine_name as item_name,sc.quantity_needed FROM  service_consumables sc" +
			" join services s using(service_id)" +
			" join store_item_details i on(sc.consumable_id = i.medicine_id) WHERE  sc.service_id = ? ";

	public List findTestBykey(String serviceid) throws SQLException{

		Connection con = null;
		PreparedStatement ps = null;
		try {
			 con = DataBaseUtil.getReadOnlyConnection();
			 ps = con.prepareStatement(FIND_RECORD_BY_SERVICE);
			 ps.setString(1, serviceid);
			 List list = DataBaseUtil.queryToDynaList(ps);
			 return list;
		}finally {
			DataBaseUtil.closeConnections(con, ps);
		}

	}
	private static String FIND_RECORD_BY_BOTH = "SELECT * FROM  service_consumables WHERE consumable_id = ? AND service_id = ? ";

	public BasicDynaBean findTestBykey(int consumableid,String serviceid) throws SQLException{

		Connection con = null;
		PreparedStatement ps = null;
		try {
			 con = DataBaseUtil.getReadOnlyConnection();
			 ps = con.prepareStatement(FIND_RECORD_BY_BOTH);

			 ps.setInt(1, consumableid);
			 ps.setString(2, serviceid);

			 List list = DataBaseUtil.queryToDynaList(ps);

			 if(list.size() > 0){
				 return (BasicDynaBean) list.get(0);
			 }else{
				 return null;
			 }
		}finally {
			DataBaseUtil.closeConnections(con, ps);
		}

	}
	private static String GET_ALL_RECORDS = " SELECT consumable_id,service_id from service_consumables ";

	public ArrayList getAllRecords() throws SQLException{

		ArrayList reagentList = new ArrayList();
		Connection con = null;
		PreparedStatement ps = null;

		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_ALL_RECORDS);

			reagentList = DataBaseUtil.queryToArrayList(ps);

		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		return reagentList;
	}
	private static String SERVICES_NOT_IN_CONSUMABLE_MASTER = "select service_id,service_name " +
			" from services where status != 'I' AND service_id not in (Select service_id from service_consumables) ORDER BY service_name ";
	public List getServiceToMapConsumables()throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(SERVICES_NOT_IN_CONSUMABLE_MASTER);
			return DataBaseUtil.queryToArrayList(ps);
		}finally{
			if(con != null)con.close();
			if(ps != null)ps.close();
		}
	}

	public static String getServiceName(String serviceid)throws SQLException{
		Connection con = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps = con.prepareStatement("SELECT service_name from services WHERE service_id=?");
		ps.setString(1, serviceid);
		String servicename = DataBaseUtil.getStringValueFromDb(ps);
		ps.close();
		con.close();
		return servicename;

	}

	public static final String SERVICE_CONSUMABLES = "SELECT s.service_name,sc.service_id," +
			"sc.consumable_id," +
			" (case when sc.status='A' then 'true' else 'false' end ) as status,i.medicine_name ,sc.quantity_needed as qty," +
			"0 as reagent_usage_seq,0 as ref_no FROM  service_consumables sc" +
			" join services s using(service_id)" +
			" join store_item_details i on(sc.consumable_id = i.medicine_id) WHERE  sc.service_id = ?";
	public List getServiceConsumables(String serviceId)throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(SERVICE_CONSUMABLES);
			ps.setString(1, serviceId);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}


	private static final String SERVICECONSUMABLES_NAMESAND_iDS="select service_name,service_id from services ";

	  public static List getServiceConsumablesNamesAndIds() throws SQLException{

		  return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(SERVICECONSUMABLES_NAMESAND_iDS));
	  }
}


