package com.insta.hms.master.SupplierRateContract;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.QueryBuilder;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SupplierRateContractDAO  extends GenericDAO {
	
	public SupplierRateContractDAO() {
		super("store_supplier_contracts");
	}
	
	private static final String GET_SUPPLIER_RATE_CONTRACT_DET =
			" SELECT ssc.*, sm.supplier_name,sm.cust_supplier_code " +
			" FROM store_supplier_contracts ssc JOIN supplier_master sm ON (sm.supplier_code = ssc.supplier_code) " +
			" WHERE ssc.supplier_rate_contract_id = ?";

	public static BasicDynaBean getSupplierRateContractDetails (String supplierRateContractId) throws SQLException{
		return DataBaseUtil.queryToDynaBean(GET_SUPPLIER_RATE_CONTRACT_DET, Integer.valueOf(supplierRateContractId));
	}
	
	private static final String GET_SUPPLIER_RATE_CONTRACT_DETAILS_FOR_DEFAULT =
			" SELECT *,'DEFAULT SUPPLIER' as supplier_name FROM store_supplier_contracts WHERE supplier_rate_contract_id = ?";
	public static BasicDynaBean getSupplierRateContractDetailsForDefaultSupplier (String supplierRateContractId) throws SQLException{
		return DataBaseUtil.queryToDynaBean(GET_SUPPLIER_RATE_CONTRACT_DETAILS_FOR_DEFAULT, Integer.valueOf(supplierRateContractId));
	}

	private static final String SUPPLIER_RATE_CONTRACT_FILEDS = "SELECT * ";
	private static final String SUPPLIER_RATE_CONTRACT_FROM = " FROM (select ssc.*,coalesce(sm.supplier_name,'DEFAULT SUPPLIER') as supplier_name,sm.cust_supplier_code from store_supplier_contracts ssc "
						    +" left join supplier_master sm using(supplier_code)) as foo ";
			
	private static final String COUNT = " SELECT count(supplier_rate_contract_id) ";

	public PagedList getSupplierContractItemDetails(Map params, Map<LISTING, Object> listingParams) throws ParseException, SQLException{
		
		Connection con = null;
		SearchQueryBuilder qb= null;
		try{
			con =DataBaseUtil.getConnection();
			qb = new SearchQueryBuilder(con, SUPPLIER_RATE_CONTRACT_FILEDS, COUNT, SUPPLIER_RATE_CONTRACT_FROM, listingParams);
			qb.addFilterFromParamMap(params);
			qb.build();
			return qb.getMappedPagedList();
		} finally {
			DataBaseUtil.closeConnections(con, null);
			if (qb != null) qb.close();
		}
	}
	
	public List<BasicDynaBean> getContractNames() throws SQLException{
		GenericDAO dao = new GenericDAO("store_supplier_contracts");
		List<String> listColumns = new ArrayList<String>();
		listColumns.add("supplier_rate_contract_id"); listColumns.add("supplier_rate_contract_name");
		return dao.listAll(listColumns, "supplier_rate_contract_name");
	}
	
	public static final String GET_ALL_SUPPLIERS = "SELECT *," +
			"	CASE WHEN supplier_city IS NOT NULL AND  TRIM(supplier_city) != ''  THEN supplier_city||' - '||supplier_name ELSE supplier_name END as supplier_name_with_city FROM SUPPLIER_MASTER ORDER BY SUPPLIER_NAME ";

		
	public static final String GET_ALL_CENTER_SUPPLIERS = "select sm.*, sm.supplier_name,scm.center_id,sm.cust_supplier_code, "
			+ "		CASE WHEN supplier_city IS NOT NULL AND  TRIM(supplier_city) != ''  THEN supplier_city||' - '||supplier_name ELSE supplier_name END as supplier_name_with_city "
			+ "		from supplier_master sm  "
			+ "		left join supplier_center_master scm on(scm.supplier_code=sm.supplier_code) "
			+ " 	where scm.center_id IN(?,0) order by sm.supplier_name ";
		
	public static ArrayList listAllcentersforAPo(int centerId)throws SQLException {

		Connection con = null;
		PreparedStatement pstmt = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			if (centerId != 0 && GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1) { 
				pstmt = con.prepareStatement(GET_ALL_CENTER_SUPPLIERS);
				pstmt.setInt(1, centerId);
			} else {
				pstmt = con.prepareStatement(GET_ALL_SUPPLIERS);
			}
			return DataBaseUtil.queryToArrayList(pstmt);

		} finally {
			DataBaseUtil.closeConnections(con, pstmt);
		}
	}
		
	public static final String GET_ALL_CENTER_SUPPLIERS_WITH_ACTIVE = "select sm.*, sm.supplier_name,scm.center_id,sm.cust_supplier_code, "
			+ "		CASE WHEN supplier_city IS NOT NULL AND  TRIM(supplier_city) != ''  THEN supplier_city||' - '||supplier_name ELSE supplier_name END as supplier_name_with_city "
			+ "		from supplier_master sm  "
			+ "		left join supplier_center_master scm on(scm.supplier_code=sm.supplier_code) "
			+ " 	where scm.center_id IN(?,0) AND scm.status='A' AND sm.status='A' order by sm.supplier_name ";
		
	public static ArrayList listAllcentersforAPoActive(int centerId)throws SQLException {

		Connection con = null;
		PreparedStatement pstmt = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			if (centerId != 0 && GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1) { 
				pstmt = con.prepareStatement(GET_ALL_CENTER_SUPPLIERS_WITH_ACTIVE);
				pstmt.setInt(1, centerId);
			} else {
				pstmt = con.prepareStatement(GET_ALL_SUPPLIERS);
			}
			return DataBaseUtil.queryToArrayList(pstmt);

		} finally {
			DataBaseUtil.closeConnections(con, pstmt);
		}
	}
		
	public static ArrayList getSupplierRateContract(List<String> centerIdList, String supplierCode, Timestamp startDate, Timestamp endDate, String supplierRateContractId)throws SQLException {

		Connection con = null;
		PreparedStatement pstmt = null;
		StringBuilder GET_SUPPLIER_RATE_CONTRACT_ACTIVE_WITH_MUTIPLE_CENTERE = new StringBuilder("select distinct ssc.supplier_rate_contract_name from store_supplier_contracts ssc "
				+ "JOIN store_supplier_contracts_center_applicability sscca ON (ssc.supplier_rate_contract_id = sscca.supplier_rate_contract_id) "
				+ "where ssc.supplier_code = ? AND ssc.status='A' AND sscca.status='A' AND "
				+ "(ssc.validity_start_date >= ? OR ssc.validity_end >= ?) AND (ssc.validity_start_date <= ? OR ssc.validity_end <= ? )");
		if(supplierRateContractId != null) {
			GET_SUPPLIER_RATE_CONTRACT_ACTIVE_WITH_MUTIPLE_CENTERE.append(" AND ssc.supplier_rate_contract_id != ? ");
		} 
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			/*String centerIdSplit[] = centerIds.split(",");
			List<String> centerIdList = Arrays.asList(centerIdSplit);*/
			QueryBuilder.addWhereFieldOpValue(true, GET_SUPPLIER_RATE_CONTRACT_ACTIVE_WITH_MUTIPLE_CENTERE,"sscca.center_id", "IN", centerIdList);
			pstmt = con.prepareStatement(GET_SUPPLIER_RATE_CONTRACT_ACTIVE_WITH_MUTIPLE_CENTERE.toString());
			pstmt.setString(1, supplierCode);
			pstmt.setTimestamp(2, startDate);
			pstmt.setTimestamp(3, startDate);
			pstmt.setTimestamp(4, endDate);
			pstmt.setTimestamp(5, endDate);
			int centerIdCount = 5;
			if(supplierRateContractId != null) {
				pstmt.setInt(6, Integer.parseInt(supplierRateContractId));
				centerIdCount = 6;
			}			
			Iterator<String> centerIdListIterator = centerIdList.iterator();
			while(centerIdListIterator.hasNext()) {
				centerIdCount++;
				pstmt.setInt(centerIdCount, Integer.parseInt(centerIdListIterator.next()));
			}
			ArrayList resultSet = new ArrayList(DataBaseUtil.queryToArrayList1(pstmt));
			return resultSet;

		} finally {
			DataBaseUtil.closeConnections(con, pstmt);
		}
	}
	
	
	public static final String GET_SUPPLIER_RATE_CONTRACT_ACTIVE_CENTER = "select sscca.center_id from store_supplier_contracts ssc "
			+ "JOIN store_supplier_contracts_center_applicability sscca ON (ssc.supplier_rate_contract_id = sscca.supplier_rate_contract_id) "
			+ "where ssc.supplier_code = ? AND ssc.supplier_rate_contract_id != ? AND ssc.status='A' AND sscca.status='A' AND ("
			+ "(ssc.validity_start_date >= (select validity_start_date from store_supplier_contracts sscc where sscc.supplier_rate_contract_id =  ?) "
			+ " OR ssc.validity_end >= (select validity_start_date from store_supplier_contracts sscc where sscc.supplier_rate_contract_id =  ?))" 
			+" AND " 
			+" (ssc.validity_start_date <= (select validity_end from store_supplier_contracts sscc where sscc.supplier_rate_contract_id =  ?) "
			+ "OR ssc.validity_end <= (select validity_end from store_supplier_contracts sscc where sscc.supplier_rate_contract_id =  ?) ))  ";
				
	public static ArrayList getEditSupplierRateContractCenters(String supplierRateContractId, String supplierCode)throws SQLException {

		Connection con = null;
		PreparedStatement pstmt = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			pstmt = con.prepareStatement(GET_SUPPLIER_RATE_CONTRACT_ACTIVE_CENTER);
			pstmt.setString(1, supplierCode);
			pstmt.setInt(2, Integer.parseInt(supplierRateContractId));
			pstmt.setInt(3, Integer.parseInt(supplierRateContractId));
			pstmt.setInt(4, Integer.parseInt(supplierRateContractId));
			pstmt.setInt(5, Integer.parseInt(supplierRateContractId));
			pstmt.setInt(6, Integer.parseInt(supplierRateContractId));
			ArrayList resultSet = new ArrayList(DataBaseUtil.queryToArrayList1(pstmt));
			return resultSet;

		} finally {
			DataBaseUtil.closeConnections(con, pstmt);
		}
	}
	
	public static final String GET_CENTER = "select sscca.center_id from store_supplier_contracts ssc "
			+ "JOIN store_supplier_contracts_center_applicability sscca ON (ssc.supplier_rate_contract_id = sscca.supplier_rate_contract_id) "
			+ "where ssc.supplier_rate_contract_id = ? AND sscca.status='A'";
	
				
	public static ArrayList getCenterForSupplierRateContract(String supplierRateContractId)throws SQLException {

		Connection con = null;
		PreparedStatement pstmt = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			pstmt = con.prepareStatement(GET_CENTER);
			pstmt.setInt(1, Integer.parseInt(supplierRateContractId));
			ArrayList resultSet = new ArrayList(DataBaseUtil.queryToArrayList1(pstmt));
			return resultSet;

		} finally {
			DataBaseUtil.closeConnections(con, pstmt);
		}
	}
	
	public static final String GET_SUPPLIER_NAME_FROM_CONTRACT = "SELECT supplier_code,status FROM store_supplier_contracts WHERE supplier_rate_contract_id = ?";
	public static BasicDynaBean getSupplierNameFromContract(String supplierRateContractId) throws SQLException {
		Connection con = null;
		PreparedStatement pstmt = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			pstmt = con.prepareStatement(GET_SUPPLIER_NAME_FROM_CONTRACT);
			pstmt.setInt(1, Integer.parseInt(supplierRateContractId));
			return DataBaseUtil.queryToDynaBean(pstmt);

		} finally {
			DataBaseUtil.closeConnections(con, pstmt);
		}
	}
	
	private static final String GETSUPPLIERS = "SELECT SUPPLIER_CODE,SUPPLIER_NAME FROM SUPPLIER_MASTER  ORDER BY SUPPLIER_NAME";

	public static List getSupplierList() throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GETSUPPLIERS);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
		
		

}
