package com.insta.hms.master.OTConsumablesMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

public class OTConsumablesMasterDAO extends GenericDAO {
	public OTConsumablesMasterDAO(){
		super("ot_consumables");
	}
	private static String OT_CONSUMABLE_FIELDS = " SELECT *";

	private static String OT_CONSUMABLE_COUNT = "SELECT count(*)";

	private static String OT_CONSUMABLE_TABLES = " FROM (SELECT otc.consumable_id, im.medicine_name as item_name, " +
			" opm.operation_name, otc.qty_needed, otc.operation_id, otc.status" +
			" FROM store_item_details im, operation_master opm, ot_consumables otc " +
			" WHERE otc.consumable_id = im.medicine_id AND otc.operation_id = opm.op_id) AS foo";

	public PagedList getOtConsumabels(Map requestParams, Map<LISTING, Object> pagingParams)
				throws ParseException, SQLException {

		Connection con = null;
		SearchQueryBuilder qb = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			qb =
				new SearchQueryBuilder(con, OT_CONSUMABLE_FIELDS, OT_CONSUMABLE_COUNT, OT_CONSUMABLE_TABLES,
							pagingParams);
			qb.addFilterFromParamMap(requestParams);
			qb.addSecondarySort("operation_id");
			qb.build();

			return qb.getMappedPagedList();
		} finally {
			qb.close();
			DataBaseUtil.closeConnections(con, null);
		}
	}
	private static String OPERATION_LIST_FOR_NEW_CONSUMABLE = "select op_id,operation_name from operation_master " +
			" where op_id not in (Select operation_id from ot_consumables) AND status='A' order by operation_name";
	public List getOperationsListForNewConsumable()throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(OPERATION_LIST_FOR_NEW_CONSUMABLE);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	private static String OPERATION_CONSUMABLES = "SELECT opm.operation_name,opm.op_id,otc.consumable_id," +
		" (case when otc.status='A' then 'true' else 'false' end ) as status,i.medicine_name as item_name,otc.qty_needed FROM  ot_consumables otc" +
		" join operation_master opm on(opm.op_id = otc.operation_id)" +
		" join store_item_details i on(otc.consumable_id = i.medicine_id) WHERE  otc.operation_id = ? ";

	public List getOperationConsumables(String operation_id) throws SQLException{

	Connection con = null;
	PreparedStatement ps = null;
	try {
		 con = DataBaseUtil.getReadOnlyConnection();
		 ps = con.prepareStatement(OPERATION_CONSUMABLES);
		 ps.setString(1, operation_id);
		 return DataBaseUtil.queryToDynaList(ps);
	}finally {
		DataBaseUtil.closeConnections(con, ps);
	}

}
	private static String FIND_OT_AND_CONSUMABLE = "SELECT * FROM  ot_consumables WHERE consumable_id = ? AND operation_id = ? ";

	public BasicDynaBean findOTByConsumable(int consumableid,String operation_id) throws SQLException{

		Connection con = null;
		PreparedStatement ps = null;
		try {
			 con = DataBaseUtil.getReadOnlyConnection();
			 ps = con.prepareStatement(FIND_OT_AND_CONSUMABLE);
			 ps.setInt(1, consumableid);
			 ps.setString(2, operation_id);

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

	private static final String OTCONSUMABLES_NAMESAND_iDS="SELECT operation_name,op_id FROM operation_master ";

	  public static List getOpConsumablesNamesAndIds() throws SQLException{

		  return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(OTCONSUMABLES_NAMESAND_iDS));
	  }



  private static final String OPERATION_NAMES="SELECT op_id,operation_name FROM operation_master  WHERE op_id " +
  		                                      " IN (Select operation_id from ot_consumables) AND status='A' ";

    public static List getOperationNames() throws SQLException{

    	return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(OPERATION_NAMES));
    }
}
