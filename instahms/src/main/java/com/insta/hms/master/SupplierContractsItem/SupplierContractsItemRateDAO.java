/**
 * 
 */
package com.insta.hms.master.SupplierContractsItem;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ashokkumar
 *
 */
public class SupplierContractsItemRateDAO extends GenericDAO {
	
	public SupplierContractsItemRateDAO() {
		super("store_supplier_contracts_item_rates");
	}
	
	public List<BasicDynaBean> getItems() throws SQLException{
		GenericDAO dao = new GenericDAO("store_item_details");
		List<String> listColumns = new ArrayList<String>();
		listColumns.add("medicine_id"); listColumns.add("medicine_name");
		return dao.listAll(listColumns, "medicine_name");
	}
	
	public List<BasicDynaBean> getSupplierContracts() throws SQLException{
		GenericDAO dao = new GenericDAO("store_supplier_contracts");
		List<String> listColumns = new ArrayList<String>();
		listColumns.add("supplier_rate_contract_id"); listColumns.add("supplier_rate_contract_name");
		return dao.listAll(listColumns, null, null, "supplier_rate_contract_name");
	}
	
	private static  String SUPPLIER_ITEM_FILEDS = "SELECT * ";
	private static  String SUPPLIER_ITEM_FROM = " FROM (select ssc.supplier_rate_contract_id,ssc.supplier_rate_contract_name,ssc.validity_start_date,ssc.validity_end,"
			               +" case when ssc.supplier_code = '-1' then 'DEFAULT SUPPLIER' else sm.supplier_name end as supplier_name,sm.supplier_code,sm.cust_supplier_code,"
			               +" ssci.supplier_rate,ssci.discount,sid.medicine_name,ssci.medicine_id,ssc.status,sid.status as _item_status,sm.status as _supplier_status,ssci.mrp  "
			               +" from store_supplier_contracts_item_rates ssci join store_supplier_contracts ssc using(supplier_rate_contract_id)"
                           +" left join supplier_master sm on(sm.supplier_code=ssc.supplier_code) "
                           +" join store_item_details sid on(sid.medicine_id=ssci.medicine_id)) as foo ";
			
	private static  String COUNT = " SELECT count(supplier_rate_contract_id) ";

	public PagedList getSupplierContractItemDetails(Map params, Map<LISTING, Object> listingParams) throws ParseException, SQLException{
		
		Connection con = null;
		SearchQueryBuilder qb= null;
		try{
			con =DataBaseUtil.getConnection();
			qb = new SearchQueryBuilder(con, SUPPLIER_ITEM_FILEDS, COUNT, SUPPLIER_ITEM_FROM, listingParams);
			qb.addFilterFromParamMap(params);
			qb.build();
			return qb.getMappedPagedList();
		} finally {
			DataBaseUtil.closeConnections(con, null);
			if (qb != null) qb.close();
		}
	}
	
	private static  String SUPPLIER_CONTRACT_FILEDS = "SELECT * ";
	private static  String SUPPLIER_CONTRACT_FROM ="FROM (select ssc.*,ssci.medicine_id,"
	    + "ssci.supplier_rate,ssci.discount,ssci.mrp,coalesce(sm.supplier_name,"
	    + "'DEFAULT SUPPLIER') as supplier_name,sm.cust_supplier_code,sm.status as "
	    + "sup_status,sid.status as item_status,sid.medicine_name, "
	    + "ssci.margin, ssci.margin_type, sid.issue_base_unit "
	    + "from store_supplier_contracts_item_rates ssci"
      +" join store_supplier_contracts ssc using(supplier_rate_contract_id) "
      +" left join supplier_master sm using(supplier_code)"
      +" join store_item_details sid on(sid.medicine_id=ssci.medicine_id))as foo ";

	public PagedList getSupplierContractDetails(Map params, Map<LISTING, Object> listingParams) throws ParseException, SQLException{
		
		Connection con = null;
		SearchQueryBuilder qb= null;
		try{
			con =DataBaseUtil.getConnection();
			qb = new SearchQueryBuilder(con, SUPPLIER_CONTRACT_FILEDS, COUNT, SUPPLIER_CONTRACT_FROM, listingParams);
			qb.addFilterFromParamMap(params);
			qb.build();
			return qb.getMappedPagedList();
		} finally {
			DataBaseUtil.closeConnections(con, null);
			if (qb != null) qb.close();
		}
	}
	
	private static String SUPPLIER_DEFAULT_ITEM_RATE ="SELECT supplier_rate, 'true' as supplier_rate_validation, sscir.discount, sscir.mrp,supplier_code, sscir.margin, sscir.margin_type "
			+ "FROM  store_supplier_contracts ssc "
			+ "JOIN store_supplier_contracts_center_applicability sscca ON (sscca.supplier_rate_contract_id = ssc.supplier_rate_contract_id "
			+ "AND sscca.status = 'A'  and ssc.supplier_code IN ('-1',?) and ssc.status = 'A' and ssc.validity_start_date <= current_date "
			+ "and ssc.validity_end >= current_date   AND sscca.center_id = 0 ) "
			+ "JOIN store_supplier_contracts_item_rates sscir on    "
			+ "(ssc.supplier_rate_contract_id = sscir.supplier_rate_contract_id and sscir.medicine_id=? )";
	
	private static String SUPPLIER_ITEM_RATE ="select COALESCE(rate1,rate2,rate3) as supplier_rate, 'true' as supplier_rate_validation, COALESCE(discount1,discount2,discount3) as discount, COALESCE(mrp1,mrp2,mrp3) as mrp "
			+ ", supplier_code,center_id, margin, margin_type "
			+ "from (SELECT supplier_rate::text as rate1, null as rate2, null as rate3, discount::text as discount1, null as discount2, null as discount3, mrp::text as mrp1, null as mrp2, null as mrp3  "
			+ ", supplier_code,center_id, sscir.margin, sscir.margin_type "
			+ "FROM  store_supplier_contracts ssc "
			+ "JOIN store_supplier_contracts_center_applicability sscca ON (sscca.supplier_rate_contract_id = ssc.supplier_rate_contract_id "
			+ "AND sscca.status = 'A'  and ssc.supplier_code= ? and ssc.status = 'A' and ssc.validity_start_date <= current_date "
			+ "and ssc.validity_end >= current_date   AND sscca.center_id = ? )   "
			+ "JOIN store_supplier_contracts_item_rates sscir on (ssc.supplier_rate_contract_id = sscir.supplier_rate_contract_id and sscir.medicine_id=? ) "
			+ "union all "
			+ "SELECT null as rate1,supplier_rate::text as rate2,null as rate3, null as discount1, discount::text as discount2, null as discount3, null as mrp1, mrp::text as mrp2, null as mrp3  "
			+ ", supplier_code,center_id, sscir.margin, sscir.margin_type "
			+ "FROM  store_supplier_contracts ssc "
			+ "JOIN store_supplier_contracts_center_applicability sscca ON (sscca.supplier_rate_contract_id = ssc.supplier_rate_contract_id "
			+ "AND sscca.status = 'A'  and ssc.supplier_code= ? and ssc.status = 'A' and ssc.validity_start_date <= current_date "
			+ "and ssc.validity_end >= current_date   AND sscca.center_id = 0 )   "
			+ "JOIN store_supplier_contracts_item_rates sscir on    "
			+ "(ssc.supplier_rate_contract_id = sscir.supplier_rate_contract_id and sscir.medicine_id=? )"
			+ "union all "
			+ "SELECT null as rate1, null as rate2, supplier_rate::text as rate3, null as discount1, null as discount2, discount::text as discount3, null as mrp1, null as mrp2, mrp::text as mrp3  "
			+ ", supplier_code,center_id, sscir.margin, sscir.margin_type "
			+ "FROM  store_supplier_contracts ssc "
			+ "JOIN store_supplier_contracts_center_applicability sscca ON (sscca.supplier_rate_contract_id = ssc.supplier_rate_contract_id "
			+ "AND sscca.status = 'A'  and ssc.supplier_code= '-1' and ssc.status = 'A' and ssc.validity_start_date <= current_date "
			+ "and ssc.validity_end >= current_date )   "
			+ "JOIN store_supplier_contracts_item_rates sscir on    "
			+ "(ssc.supplier_rate_contract_id = sscir.supplier_rate_contract_id and sscir.medicine_id=? )"
			+ ") as foo"; 
	
	public List<BasicDynaBean> getSupplierItemRateValue(Object itemId,String suppId,int centerId) throws ParseException, SQLException{
	
		Connection con = null;
		PreparedStatement ps = null;
			try{
				con = DataBaseUtil.getConnection();
				if(centerId == 0){
					ps = con.prepareStatement(SUPPLIER_DEFAULT_ITEM_RATE);
					ps.setString(1, suppId);
					ps.setObject(2, itemId);
				}else{
					ps = con.prepareStatement(SUPPLIER_ITEM_RATE);
					ps.setString(1, suppId);
					ps.setInt(2, centerId);
					ps.setObject(3, itemId);
					ps.setString(4, suppId);
					ps.setObject(5, itemId);
					ps.setObject(6, itemId);
				}
				return DataBaseUtil.queryToDynaList(ps);
			} finally {
				DataBaseUtil.closeConnections(con, ps);
			}
	}
	
	
	private static final String GET_MIN_RATE_SUPPLIERS = "SELECT ssc.supplier_rate_contract_id, "
	    + "ssc.supplier_rate_contract_name, sm.supplier_name, sscir.supplier_rate, "
	    + "sscir.supplier_rate * ((100 - COALESCE(sscir.discount,0))/100) "
	    + "as discounted_supplier_rate "
	    + "FROM store_supplier_contracts ssc "
	    + "LEFT JOIN supplier_master sm ON (ssc.supplier_code = sm.supplier_code) "
	    + "LEFT JOIN store_supplier_contracts_item_rates sscir ON (ssc.supplier_rate_contract_id = sscir.supplier_rate_contract_id) "
	    + "LEFT JOIN store_supplier_contracts_center_applicability ssca ON (ssc.supplier_rate_contract_id = ssca.supplier_rate_contract_id) "
	    + "WHERE center_id in (?, 0) AND ssca.status = 'A' AND ssc.status = 'A' AND medicine_id = ? AND ssc.validity_start_date <= current_date "
	    + "AND sscir.supplier_rate * ((100 - COALESCE(sscir.discount,0))/100) = "
	    + "(select min(sscir.supplier_rate * ((100 - COALESCE(sscir.discount,0))/100)) from store_supplier_contracts ssc "
	    + "LEFT JOIN supplier_master sm ON (ssc.supplier_code = sm.supplier_code) "
	    + "LEFT JOIN store_supplier_contracts_item_rates sscir ON (ssc.supplier_rate_contract_id = sscir.supplier_rate_contract_id) "
	    + "LEFT JOIN store_supplier_contracts_center_applicability ssca ON (ssc.supplier_rate_contract_id = ssca.supplier_rate_contract_id) "
	    + "WHERE ssca.center_id in (?, 0) AND ssca.status = 'A' AND ssc.status = 'A' AND medicine_id = ? AND ssc.validity_start_date <= current_date)";
	public List<BasicDynaBean> getMinimumRateSupplierContracts(Integer medicineId, Integer centerId) throws SQLException{
	  Connection con = null;
    PreparedStatement ps = null;
      try{
        con = DataBaseUtil.getConnection();
        ps = con.prepareStatement(GET_MIN_RATE_SUPPLIERS);
        ps.setInt(1, centerId);
        ps.setInt(2, medicineId);
        ps.setInt(3, centerId);
        ps.setInt(4, medicineId);
        return DataBaseUtil.queryToDynaList(ps);
      } finally {
        DataBaseUtil.closeConnections(con, ps);
      }
	}
	
	public BasicDynaBean getSupplierItemDetails(int supplierRatezcontractId,int medicineId)
	throws SQLException{
		HashMap<String, Object> findKeys = new HashMap<String, Object>();
		findKeys.put("supplier_rate_contract_id", supplierRatezcontractId);
		findKeys.put("medicine_id", medicineId);
		
		return findByKey(findKeys);
	}
}
